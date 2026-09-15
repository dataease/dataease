<script setup lang="ts">
import { ref, defineAsyncComponent, nextTick } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import configGlobal from '@/components/config-global/src/ConfigGlobal.vue'
const exportExcelRef = ref()
const exportExcelRefShow = ref(false)
const ExportExcelDraw = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/ExportExcelDraw.vue')
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
    <router-view />
    <ExportExcelDraw ref="exportExcelRef"></ExportExcelDraw>
  </config-global>
</template>
