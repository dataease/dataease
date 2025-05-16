<script setup lang="ts">
import { ref, defineAsyncComponent, nextTick } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import configGlobal from '@/components/config-global/src/ConfigGlobal.vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const exportExcelRef = ref()
const exportExcelRefShow = ref(false)
const ExportExcel = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/ExportExcel.vue')
)
const exportExcelCenter = params => {
  exportExcelRefShow.value = true
  nextTick(() => {
    exportExcelRef.value.init(params)
  })
}
useEmitt({
  name: 'data-export-center',
  callback: exportExcelCenter
})
</script>
<template>
  <config-global>
    <router-view :key="route.path" />
    <ExportExcel v-if="exportExcelRefShow" ref="exportExcelRef"></ExportExcel>
  </config-global>
</template>
