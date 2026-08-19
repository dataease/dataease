<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
const { t } = useI18n()

const emits = defineEmits(['loaded'])
const panelInfo = {
  title: t('dataset.column_permissions'),
  name: 'column'
}
onMounted(() => {
  emits('loaded', panelInfo)
})
</script>