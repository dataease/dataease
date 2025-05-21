<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref } from 'vue'

const { t } = useI18n()

defineProps({
  errMsg: {
    type: String,
    required: true,
    default: ''
  }
})

const dialogVisible = ref(false)
function showInfo() {
  dialogVisible.value = true
}
</script>

<template>
  <div class="canvas-content error-info">
    <span
      >[{{ t('chart.chart_error_tips')
      }}<a style="color: #0969da; cursor: pointer" @click="showInfo">{{
        t('chart.chart_show_error_info')
      }}</a
      >]</span
    >
    <el-dialog
      :append-to-body="true"
      v-model="dialogVisible"
      width="80%"
      :close-on-click-modal="false"
      center
    >
      <el-main style="height: 400px">
        <span style="white-space: pre-line" v-html="errMsg"></span>
      </el-main>
      <template #footer>
        <span class="m-dialog-footer">
          <el-button @click="dialogVisible = false">{{ t('commons.close') }}</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.error-info {
  background-color: #ece7e7;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  color: #9ea6b2;
  width: 100%;
  height: 100%;
  flex-direction: column;
}
.m-dialog-footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}
</style>
