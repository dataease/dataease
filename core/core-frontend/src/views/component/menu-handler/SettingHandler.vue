<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { computed, onMounted } from 'vue'
import { useUserStoreWithOut } from '@/store/modules/user'

const { t } = useI18n()
const userStore = useUserStoreWithOut()
const emits = defineEmits(['loaded'])
const uid = computed(() => userStore.getUid)
onMounted(() => {
  if (uid.value === '1') {
    emits('loaded', [{id: 4,link: '/sys-setting/authentication', label: t('commons.system_setting')}])
  }
})
</script>