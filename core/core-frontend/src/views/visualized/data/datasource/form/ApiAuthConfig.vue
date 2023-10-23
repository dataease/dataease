<script lang="ts" setup>
import { PropType, toRefs } from 'vue'
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

const { request } = toRefs(props)

const options = [{ name: 'No Auth' }, { name: 'Basic Auth' }]
const change = () => {
  const { username, password, verification } = request.value.authManager
  const isBasic = verification === 'Basic Auth'
  request.value.authManager.username = isBasic ? username : ''
  request.value.authManager.password = isBasic ? password : ''
}
</script>

<template>
  <el-form label-position="top">
    <el-form-item class="api-auth-config" :label="t('datasource.verification_method')">
      <el-select
        v-model="request.authManager.verification"
        @change="change"
        :placeholder="t('datasource.verification_method')"
      >
        <el-option v-for="item in options" :key="item.name" :label="item.name" :value="item.name" />
      </el-select>
    </el-form-item>
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item
          v-if="request.authManager.verification === 'Basic Auth'"
          :label="t('datasource.username')"
        >
          <el-input
            v-model="request.authManager.username"
            :placeholder="t('datasource.username')"
            class="ms-http-input"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          v-if="request.authManager.verification === 'Basic Auth'"
          :label="t('datasource.password')"
        >
          <el-input
            v-model="request.authManager.password"
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
