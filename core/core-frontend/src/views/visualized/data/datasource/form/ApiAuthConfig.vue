<script lang="ts" setup>
import { onBeforeMount, reactive, watch, PropType, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

export interface AuthConfig {
  verification: string
  username: string
  password: string
}

const props = defineProps({
  request: {
    type: Object as PropType<{ authManager: AuthConfig }>,
    default: () => ({})
  }
})
const { t } = useI18n()
onBeforeMount(() => {
  initData()
})

const { request } = toRefs(props)

watch(
  () => request.value,
  () => {
    initData()
  }
)
const authConfig = reactive<AuthConfig>({
  verification: '',
  username: '',
  password: ''
})

const options = [{ name: 'No Auth' }, { name: 'Basic Auth' }]
const change = () => {
  if (!request.value.authManager) {
    request.value.authManager = authConfig
    return
  }
  const { username, password, verification } = authConfig
  const isBasic = verification === 'Basic Auth'
  request.value.authManager.username = isBasic ? username : ''
  request.value.authManager.password = isBasic ? password : ''
}
const initData = () => {
  if (request.value.authManager) {
    Object.assign(authConfig, request.value.authManager)
  }
}
</script>

<template>
  <el-form label-position="top">
    <el-form-item class="api-auth-config" :label="t('datasource.verification_method')">
      <el-select
        v-model="authConfig.verification"
        @change="change"
        :placeholder="t('datasource.verification_method')"
      >
        <el-option v-for="item in options" :key="item.name" :label="item.name" :value="item.name" />
      </el-select>
    </el-form-item>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item
          v-if="authConfig.verification === 'Basic Auth'"
          :label="t('datasource.username')"
        >
          <el-input
            v-model="authConfig.username"
            :placeholder="t('datasource.username')"
            class="ms-http-input"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          v-if="authConfig.verification === 'Basic Auth'"
          :label="t('datasource.password')"
        >
          <el-input
            v-model="authConfig.password"
            type="password"
            :placeholder="t('datasource.password')"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<style lang="less">
.api-auth-config {
  margin-bottom: 16px !important;
}
</style>
