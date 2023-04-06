<script lang="ts" setup>
import { ref } from 'vue'
import UserConfig from './UserConfig.vue'
import ResourceConfig from './ResourceConfig.vue'
const activeName = ref('user')
const handleClick = tab => {
  console.log('tab', tab)
}
</script>

<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane label="按用户配置" name="user"></el-tab-pane>
    <el-tab-pane label="按资源配置" name="resources"></el-tab-pane>
  </el-tabs>
  <div class="auth-table__content de-search-table">
    <user-config v-if="activeName === 'user'"></user-config>
    <resource-config v-else></resource-config>
  </div>
</template>

<style lang="less" scoped>
.auth-table__content {
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 165px);
  box-sizing: border-box;
  margin-top: 12px;
}
</style>
