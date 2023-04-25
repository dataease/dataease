<script lang="ts" setup>
import { ref } from 'vue'
import UserConfig from './UserConfig.vue'
import ResourceConfig from './ResourceConfig.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const activeName = ref('user')
const config = ref(null)
const handleClick = tab => {
  console.log('tab', tab)
}
const beforeActiveChange = (newName, oldName) => {
  if (newName !== oldName) {
    return config.value['uncommittedTips'](() => {
      activeName.value = newName
    })
  }
  return true
}
</script>

<template>
  <el-tabs v-model="activeName" @tab-click="handleClick" :before-leave="beforeActiveChange">
    <el-tab-pane :label="t('auth.user_dimension')" name="user"></el-tab-pane>
    <el-tab-pane :label="t('auth.resource_dimension')" name="resources"></el-tab-pane>
  </el-tabs>
  <div class="auth-table__content de-search-table">
    <user-config ref="config" v-if="activeName === 'user'"></user-config>
    <resource-config ref="config" v-else></resource-config>
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
