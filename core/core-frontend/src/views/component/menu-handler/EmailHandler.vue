<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
const { t } = useI18n()

const emits = defineEmits(['loaded'])

onMounted(() => {
  emits('loaded', { name: 'email', label: t('setting_email.title') })
})
</script>