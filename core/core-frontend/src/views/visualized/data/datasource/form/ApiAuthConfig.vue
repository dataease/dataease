<script lang="ts" setup>
import { onBeforeMount, reactive, watch, PropType } from 'vue'
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

watch(props.request, () => {
  initData()
})
const authConfig = reactive<AuthConfig>({
  verification: '',
  username: '',
  password: ''
})

const options = [{ name: 'No Auth' }, { name: 'Basic Auth' }]
const change = () => {
  if (authConfig.verification === 'Basic Auth') {
    authConfig.verification = 'Basic Auth'
    emits('authConfigChange', authConfig)
  } else {
    authConfig.verification = 'No Auth'
    emits('authConfigChange', authConfig)
  }
}
const initData = () => {
  if (props.request.authManager) {
    Object.assign(authConfig, props.request.authManager)
  }
}

const emits = defineEmits(['authConfigChange'])
</script>

<template>
  <el-form ref="authConfig" :model="authConfig" label-position="top">
    <el-form-item :label="t('datasource.verification_method')" prop="verification">
      <el-select
        v-model="authConfig.verification"
        :placeholder="t('datasource.verification_method')"
        filterable
        @change="change"
      >
        <el-option v-for="item in options" :key="item.name" :label="item.name" :value="item.name" />
      </el-select>
    </el-form-item>
    <el-row :gutter="24">
      <el-col :span="12">
        <el-form-item
          v-if="authConfig.verification != undefined && authConfig.verification != 'No Auth'"
          :label="t('datasource.username')"
          prop="username"
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
          v-if="authConfig.verification != undefined && authConfig.verification != 'No Auth'"
          :label="t('datasource.password')"
          prop="password"
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
