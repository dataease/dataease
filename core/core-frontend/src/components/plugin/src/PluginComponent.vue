<script lang="ts" setup>
import noLic from './nolic.vue'
import { ref, useAttrs, onMounted, shallowRef, nextTick, onBeforeUnmount, watch } from 'vue'
import {
  PluginLoadError,
  PluginMethodQueue,
  isUnavailablePluginError,
  toPluginLoadError
} from './loader'
import type { PluginCallParam, PluginFailPayload, PluginInstanceState } from './loader'
import { loadPluginComponent } from './adapters'

const attrs = useAttrs()
const emits = defineEmits<{
  (event: 'loadFail', payload: PluginFailPayload): void
}>()

const plugin = shallowRef()
const pluginProxy = ref()
const state = ref<PluginInstanceState>('loading')
const methods = new PluginMethodQueue()

let disposed = false
let loadFailEmitted = false
let readyTimer: ReturnType<typeof setTimeout> | null = null
let loadGeneration = 0

const clearReadyTimer = () => {
  if (!readyTimer) return
  clearTimeout(readyTimer)
  readyTimer = null
}

const callPlugin = (param: PluginCallParam) => {
  const proxy = pluginProxy.value as any
  if (!proxy) throw new PluginLoadError('LOAD_FAILED', 'plugin component is not mounted')
  if (typeof proxy.invokeMethod === 'function') return proxy.invokeMethod(param)
  const method = proxy[param.methodName]
  if (typeof method !== 'function') {
    throw new PluginLoadError(
      'METHOD_NOT_FOUND',
      `plugin method is not available: ${param.methodName}`
    )
  }
  return method(...(param.args || []))
}

const bindProxy = () => {
  if (disposed || !pluginProxy.value) return
  if (state.value !== 'loading' && state.value !== 'ready') return
  clearReadyTimer()
  state.value = 'ready'
  methods.bind(callPlugin)
  // 挂载前积压的调用放到本轮渲染之后；插件 mounted 中发起的调用仍走同步契约
  nextTick(() => {
    if (!disposed) methods.drain()
  })
}

const fail = (error: unknown) => {
  if (disposed) return
  clearReadyTimer()
  const failure = toPluginLoadError(error)
  const failedState = isUnavailablePluginError(failure) ? 'unavailable' : 'failed'
  state.value = failedState
  plugin.value = noLic
  methods.close(failure)
  if (!loadFailEmitted) {
    loadFailEmitted = true
    emits('loadFail', {
      state: failedState,
      code: failure.code,
      message: failure.message
    })
  }
}

const invokeMethod = (param: PluginCallParam) => {
  const result = methods.push(param)
  // 调用方多为 fire-and-forget，内部标记已处理以避免未处理拒绝告警
  result.catch(() => undefined)
  return result
}

const startLoad = async (jsname: unknown) => {
  const generation = ++loadGeneration
  clearReadyTimer()
  methods.reset(new PluginLoadError('LOAD_FAILED', 'plugin component switched'))
  loadFailEmitted = false
  state.value = 'loading'
  plugin.value = null
  try {
    const component = await loadPluginComponent(jsname)
    if (disposed || generation !== loadGeneration) return
    plugin.value = component
    await nextTick()
    if (disposed || generation !== loadGeneration) return
    bindProxy()
    if (state.value === 'loading') {
      readyTimer = setTimeout(() => {
        readyTimer = null
        fail(new PluginLoadError('LOAD_FAILED', 'plugin component did not mount'))
      }, 10000)
    }
  } catch (error) {
    if (disposed || generation !== loadGeneration) return
    fail(error)
  }
}

// 同步绑定：插件子组件在 mounted 中触发的调用也需要走同步契约
watch(pluginProxy, bindProxy, { flush: 'sync' })
watch(
  () => attrs.jsname,
  jsname => {
    if (!disposed && jsname) startLoad(jsname)
  }
)

onMounted(() => {
  if (attrs.jsname) startLoad(attrs.jsname)
})

onBeforeUnmount(() => {
  disposed = true
  clearReadyTimer()
  state.value = 'disposed'
  methods.close(new PluginLoadError('DISPOSED', 'plugin component disposed'))
  methods.unbind()
  plugin.value = null
})

defineExpose({
  invokeMethod,
  getState: () => state.value
})
</script>

<template>
  <component
    :key="attrs.jsname"
    ref="pluginProxy"
    :is="plugin"
    v-loading="state === 'loading'"
    v-bind="attrs"
  ></component>
</template>

<style lang="less" scoped></style>
