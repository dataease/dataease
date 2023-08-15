<template>
  <el-dialog
    ref="enlargeDialog"
    :append-to-body="true"
    v-model="dialogShow"
    width="70vw"
    trigger="click"
  >
    <el-button icon="Picture" size="small" class="export-button" @click="downloadViewImage">{{
      t('chart.export_img')
    }}</el-button>
    <div class="enlarge-outer" ref="viewContainer">
      <component-wrapper
        v-if="dialogShow"
        class="enlarge-wrapper"
        :view-info="viewInfo"
        :config="config"
        show-position="viewDialog"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
import { ref } from 'vue'
import { toPng } from 'html-to-image'
import { useI18n } from '@/hooks/web/useI18n'
const dialogShow = ref(false)
const viewInfo = ref(null)
const config = ref(null)
const canvasStyleData = ref(null)
const viewContainer = ref(null)
const { t } = useI18n()

const dialogInit = (canvasStyle, view, item) => {
  dialogShow.value = true
  viewInfo.value = view
  config.value = item
  canvasStyleData.value = canvasStyle
}
const downloadViewImage = () => {
  htmlToImage()
}
const htmlToImage = () => {
  toPng(viewContainer.value)
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', viewInfo.value.title)
      a.href = dataUrl
      a.click()
    })
    .catch(error => {
      console.error('oops, something went wrong!', error)
    })
}

defineExpose({
  dialogInit
})
</script>

<style lang="less" scoped>
.export-button {
  position: absolute;
  right: 60px;
  top: 18px;
}
.enlarge-outer {
  position: relative;
  height: 65vh;
  .enlarge-wrapper {
    width: 100%;
    height: 100%;
  }
}
</style>
