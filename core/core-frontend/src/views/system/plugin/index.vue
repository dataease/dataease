<script lang="ts" setup>
import { ref, watch } from 'vue'
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  jsname: propTypes.string.def(''),
  menuid: propTypes.number.def(null),
  noLayout: propTypes.bool.def(false)
})
const plugin = ref()
const loadComponent = (type: string) => {
  import(`../../../../../../de-xpack/xpack-frontend/src/${type}/index.vue`).then((res: any) => {
    plugin.value = res.default
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
</script>

<template>
  <component :is="plugin"></component>
</template>

<style lang="less" scoped></style>
