<template />
<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted } from 'vue'
const { t } = useI18n()

const emits = defineEmits(['loaded'])
const panelInfo = {
  title: t('dataset.row_permissions'),
  name: 'row'
}
onMounted(() => {
  emits('loaded', panelInfo)
})
</script>