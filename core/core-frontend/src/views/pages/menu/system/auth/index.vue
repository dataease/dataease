<script lang="ts" setup>
import { computed, ref } from 'vue'
import router from '@/router'
import UserConfig from '@/views/menu/system/auth/UserConfig.vue'
import ResourceConfig from '@/views/menu/system/auth/ResourceConfig.vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const activeName = ref('user')
const config = ref(null) as any

const isSystem = computed(() => {
  return router.currentRoute.value.path.includes('/sys-setting/auth')
})

const beforeActiveChange = (newName, oldName) => {
  if (newName !== oldName) {
    return config.value['uncommittedTips']()
  }
  return true
}
</script>

<template>
  <div class="auth-top-bar">
    <el-tabs v-model="activeName" :before-leave="beforeActiveChange">
      <el-tab-pane :label="t('org.user_dimension')" name="user"></el-tab-pane>
      <el-tab-pane :label="t('org.resource_dimension')" name="resources"></el-tab-pane>
    </el-tabs>
  </div>
  <div class="auth-table__content de-search-table">
    <user-config ref="config" v-if="activeName === 'user'" :is-system="isSystem"></user-config>
    <resource-config ref="config" v-else :is-system="isSystem"></resource-config>
  </div>
</template>

<style lang="less" scoped>
.auth-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  :deep(.ed-tabs) {
    width: 100%;
  }
}

.auth-table__content {
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 155px);
  box-sizing: border-box;
  border-radius: 4px;
}
</style>
