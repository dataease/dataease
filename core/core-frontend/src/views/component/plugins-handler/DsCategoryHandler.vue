<template>
  <div class="ds-catelog-handler-container" />
</template>

<script lang="ts" setup>
import request from '@/config/axios'

const emits = defineEmits(['loadDsPlugin'])
const loadDsCategory = () => {
  request.get({ url: '/xpackComponent/dsPlugins' }).then(res => {
    emits('loadDsPlugin', res.data)
  })
}
loadDsCategory()

</script>

<style lang="less" scoped>
.ds-catelog-handler-container {
  display: none;
}
</style>
