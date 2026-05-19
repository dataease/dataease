<script lang="ts" setup>
import { ref } from 'vue'
import ExportExcel from './ExportExcel.vue'

const drawerLoading = ref(false)
const drawer = ref(false)

const handleClose = () => {
  exportExcelRef.value.handleClose()
  drawer.value = false
}

const exportExcelRef = ref()
const init = params => {
  drawer.value = true
  exportExcelRef.value.init(params)
}

defineExpose({
  init
})
</script>

<template>
  <el-drawer
    v-loading="drawerLoading"
    modal-class="de-export-excel"
    :title="$t('data_export.export_center')"
    v-model="drawer"
    direction="rtl"
    size="1000px"
    append-to-body
    :before-close="handleClose"
  >
    <ExportExcel ref="exportExcelRef"></ExportExcel>
  </el-drawer>
</template>

<style lang="less">
.de-export-excel {
  .ed-drawer__body {
    padding-bottom: 24px;

    .ed-tabs {
      margin-top: -25px;
    }
  }
  .ed-drawer__header {
    border-bottom: none;
  }
}
</style>
