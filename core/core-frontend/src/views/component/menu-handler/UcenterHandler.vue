<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
import { bindApi } from '../login/bind'
import { getQueryString, isLarkPlatform } from '@/utils/utils'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()

const emits = defineEmits(['loaded'])
const initBind = () => {
  const state = getQueryString('state')
  const code = getQueryString('code')
  if(state?.includes('fit2cloud-dingtalk-qr_de_bind')) {
    bindHandler(5, state, code)
  } else if (state?.includes('fit2cloud-larksuite-qr_de_bind')) {
    bindHandler(7, state, code)
  } else if (state?.includes('fit2cloud-wecom-qr_de_bind')) {
    bindHandler(6, state, code)
  } else if (state?.includes('fit2cloud-lark-qr_de_bind')) {
    bindHandler(4, state, code)
  }
}

const bindHandler = (origin: number, state: string, code: string) => {
  bindApi(origin, { state, code }).then(res => {
    if (!res.msg) {
      ElMessage.success(t('role.bind_success'))
    }
  }).finally(() => {
    setTimeout(() => {
      window.location.href = window.location.origin + window.location.pathname + '#/user-center/index?tab=1'
    }, 1500);
  })
}
onMounted(() => {
  emits('loaded', [{id: 1, link: '/user-center/index?tab=1', label: t('common.personal_info')}])
  emits('loaded', [{id: 2, link: '/user-center/index?tab=2', label: t('user.change_password')}])
  emits('loaded', [{id: 3, link: '/user-center/index?tab=3', label: 'API Key'}])
  if (isLarkPlatform()) {
    initBind()
  }
})
</script>