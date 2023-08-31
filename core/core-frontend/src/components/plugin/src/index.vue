<script lang="ts" setup>
import { ref, watch, useAttrs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { load } from '@/api/plugin'
import noLic from './nolic.vue'
import { useCache } from '@/hooks/web/useCache'
const props = defineProps({
  jsname: propTypes.string.def(''),
  menuid: propTypes.number.def(null),
  noLayout: propTypes.bool.def(false)
})

const { wsCache } = useCache()

const plugin = ref()

const loading = ref(false)

const attrs = useAttrs()

const showNolic = () => {
  plugin.value = noLic
  loading.value = false
}

const importPlugin = (path: string) => {
  import(`../../../../../../de-xpack/xpack-frontend/src/${path}/index.vue`)
    .then((res: any) => {
      plugin.value = res.default
    })
    .catch(e => {
      console.log(e)
      showNolic()
    })
}
const loadComponent = (type: string) => {
  loading.value = true
  const path = wsCache.get(`de-plugin-${type}`)
  if (path) {
    importPlugin(path)
    loading.value = false
    return
  }
  load(type)
    .then(response => {
      let path = response.data
      wsCache.set(`de-plugin-${type}`, path)
      importPlugin(path)
    })
    .catch(() => {
      showNolic()
    })
    .finally(() => {
      loading.value = false
    })
}
watch(
  () => props.jsname,
  () => {
    loadComponent(props.jsname)
  },
  {
    immediate: true
  }
)
const pluginProxy = ref(null)
const invokeMethod = param => {
  const { methodName, args } = param
  pluginProxy.value[methodName](args)
}
defineExpose({
  invokeMethod
})
</script>

<template>
  <component ref="pluginProxy" :is="plugin" v-loading="loading" v-bind="attrs"></component>
</template>

<style lang="less" scoped></style>
