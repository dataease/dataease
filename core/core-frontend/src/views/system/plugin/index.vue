<script lang="ts" setup>
import { ref, watch } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { load } from '@/api/plugin'
import noLic from './nolic.vue'
const props = defineProps({
  jsname: propTypes.string.def(''),
  menuid: propTypes.number.def(null),
  noLayout: propTypes.bool.def(false)
})
const plugin = ref()

const loading = ref(false)

const showNolic = () => {
  plugin.value = noLic
  loading.value = false
}
const loadComponent = (type: string) => {
  loading.value = true
  load(type)
    .then(response => {
      let path = response.data
      import(`../../../../../../de-xpack/xpack-frontend/src/${path}/index.vue`)
        .then((res: any) => {
          plugin.value = res.default
        })
        .catch(() => {
          showNolic()
        })
    })
    .catch(() => {
      showNolic()
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
  <component :is="plugin" v-loading="loading"></component>
</template>

<style lang="less" scoped></style>
