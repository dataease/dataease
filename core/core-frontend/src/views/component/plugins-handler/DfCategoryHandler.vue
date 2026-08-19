<template>
  <div class="ds-catelog-handler-container" />
</template>

<script lang="ts" setup>
import request from '@/config/axios'

const emits = defineEmits(['loadDfPlugin'])
const loadDfCategory = () => {
  request.get({ url: '/xpackComponent/dfPlugins' }).then(res => {
    emits('loadDfPlugin', res.data)
  })
}
loadDfCategory()

</script>

<style lang="less" scoped>
.ds-catelog-handler-container {
  display: none;
}
</style>
