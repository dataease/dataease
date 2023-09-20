<script lang="ts" setup>
import { ref } from 'vue'
import { Icon } from '@/components/icon-custom'
const helpLink = ref('https://dataease.io/docs/')
const openBlank = () => {
  window.open(helpLink.value)
}
</script>

<template>
  <div class="sys-setting">
    <el-icon @click="openBlank">
      <Icon name="docs" />
    </el-icon>
  </div>
</template>

<style lang="less" scoped>
.sys-setting {
  margin: 0 10px;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
}
</style>
