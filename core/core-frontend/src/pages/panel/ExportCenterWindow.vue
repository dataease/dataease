<script lang="ts" setup>
import { defineAsyncComponent, ref, nextTick } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'

const ExportExcel = defineAsyncComponent(
  () => import('@/views/visualized/data/dataset/ExportExcel.vue')
)

const ExportExcelRef = ref()
const exportCenterWindow = ref(false)
const exportDialogShow = ref(false)
const resetForm = () => {
  exportCenterWindow.value = false
  exportDialogShow.value = false
}
const showExportCenterWindow = () => {
  exportDialogShow.value = false
  exportCenterWindow.value = true
  nextTick(() => {
    ExportExcelRef.value.init()
  })
}

useEmitt({
  name: 'ExportCenterWindow',
  callback: showExportCenterWindow
})
</script>

<template>
  <el-dialog
    :title="$t('data_export.export_center')"
    v-model="exportDialogShow"
    width="800px"
    :before-close="resetForm"
  >
    <ExportExcel v-if="exportCenterWindow" ref="ExportExcelRef"></ExportExcel>
  </el-dialog>
</template>
