<script lang="ts" setup>
import { ref, watch, useAttrs, shallowRef } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { proxyMapping } from "./mapping"
const props = defineProps({
  jsname: propTypes.string.def('')
})

const plugin = shallowRef()
const loading = ref(false)
const attrs = useAttrs()

const importPlugin = (path: string) => {
  if (path.startsWith('/')) {
    path = path.substring(1)
  }
  const pathArray = path.split('/')
  let promise = null
  if (pathArray.length === 1) {
    promise = import(`../${pathArray[0]}.vue`)
  } else if (pathArray.length === 2) {
    promise = import(`../${pathArray[0]}/${pathArray[1]}.vue`)
  } else if (pathArray.length === 3) {
    promise = import(`../${pathArray[0]}/${pathArray[1]}/${pathArray[2]}.vue`)
  } else if (pathArray.length === 4) {
    promise = import(`../${pathArray[0]}/${pathArray[1]}/${pathArray[2]}/${pathArray[3]}.vue`)
  } else if (pathArray.length === 5) {
    promise = import(`../${pathArray[0]}/${pathArray[1]}/${pathArray[2]}/${pathArray[3]}/${pathArray[4]}.vue`)
  } else if (pathArray.length === 6) {
    promise = import(`../${pathArray[0]}/${pathArray[1]}/${pathArray[2]}/${pathArray[3]}/${pathArray[4]}/${pathArray[5]}.vue`)
  }
  promise
    .then((res: any) => {
      plugin.value = res.default
    })
    .catch(e => {
      console.error(e)
    })
}

watch(
  () => props.jsname,
  () => {
    const path = proxyMapping[props.jsname]
    if (path) {
        importPlugin(path)
    }
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
