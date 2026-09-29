<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
import { bindApi } from '../login/bind'
import { getQueryString, isLarkPlatform } from '@/utils/utils'
import { ElMessage } from 'element-plus-secondary'
import {
  pendingBinding,
  clearBinding,
  bindingReturnPath,
  bindingCallbackPath
} from '@/utils/platformBinding'
const { t } = useI18n()

const emits = defineEmits(['loaded'])
const initBind = () => {
  const state = getQueryString('state')
  const code = getQueryString('code')
  const pending = pendingBinding(state)
  if (!pending || !code) return
  if (pending.returnPath !== bindingReturnPath()) {
    window.location.replace(
      bindingCallbackPath(pending) + window.location.search + window.location.hash
    )
    return
  }
  // Clear before issuing the request to prevent repeated mounts from submitting it twice.
  clearBinding(pending.state)
  bindApi(pending, code)
    .then(res => {
      if (!res.msg) {
        ElMessage.success(t('role.bind_success'))
      }
    })
    .finally(() => {
      window.location.replace(
        window.location.origin + pending.returnPath + '#/user-center/index?tab=1'
      )
    })
}

onMounted(() => {
  emits('loaded', [{ id: 1, link: '/user-center/index?tab=1', label: t('common.personal_info') }])
  emits('loaded', [{ id: 2, link: '/user-center/index?tab=2', label: t('user.change_password') }])
  emits('loaded', [{ id: 3, link: '/user-center/index?tab=3', label: 'API Key' }])
  if (isLarkPlatform()) {
    initBind()
  }
})
</script>
