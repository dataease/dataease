<template>
  <template-manage></template-manage>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import TemplateManage from '@/views/template/index.vue'

const { t } = useI18n()

const activeName = ref('basic')
const handleClick = (tab, event: Event) => {
  console.log(tab, event)
}
</script>
