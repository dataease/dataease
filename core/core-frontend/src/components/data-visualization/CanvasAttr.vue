<script setup lang="tsx">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import {
  COLOR_CASES,
  COLOR_PANEL,
  DEFAULT_COLOR_CASE
} from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import { imgUrlTrans } from '@/utils/imgUtils'
import { merge } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import ComponentColorSelector from '@/components/dashboard/subject-setting/dashboard-style/ComponentColorSelector.vue'
import OverallSetting from '@/components/dashboard/subject-setting/dashboard-style/OverallSetting.vue'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData, canvasViewInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()
const files = ref(null)
const maxImageSize = 15000000
let initReady = false
let canvasAttrInit = false

const options = ref({
  color: '颜色',
  opacity: '不透明度',
  backgroundColor: '背景色',
  fontSize: '字体大小'
})
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadDisabled = ref(false)

const canvasAttrActiveNames = ref(['size', 'background', 'color'])

const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

const handleRemove = (file, fileList) => {
  uploadDisabled.value = false
  canvasStyleData.value.background = null
  fileList.value = []
  snapshotStore.recordSnapshot()
}
async function upload(file) {
  uploadFileResult(file.file, fileUrl => {
    if (fileUrl) {
      canvasStyleData.value.background = fileUrl
      fileList.value = [{ url: imgUrlTrans(canvasStyleData.value.background) }]
    }
  })
}

const colorCases = COLOR_CASES

const predefineColors = COLOR_PANEL

const state = reactive({
  colorForm: JSON.parse(JSON.stringify(DEFAULT_COLOR_CASE)),
  customColor: null,
  colorIndex: 0
})

const changeColorOption = () => {
  const items = colorCases.filter(ele => {
    return ele.value === state.colorForm.value
  })
  state.colorForm.colors = [...items[0].colors]
  state.customColor = state.colorForm.colors[0]
  state.colorIndex = 0
}

const resetCustomColor = () => {
  changeColorOption()
}

const switchColor = index => {
  state.colorIndex = index
}

const switchColorCase = () => {
  state.colorForm.colors[state.colorIndex] = state.customColor
}

const goFile = () => {
  files.value.click()
}

const reUpload = e => {
  const file = e.target.files[0]
  if (file.size > maxImageSize) {
    sizeMessage()
  }
  uploadFileResult(file, fileUrl => {
    canvasStyleData.value.background = fileUrl
    fileList.value = [{ url: imgUrlTrans(canvasStyleData.value.background) }]
  })
}

const sizeMessage = () => {
  ElMessage.success('图片大小不符合')
}

const init = () => {
  initReady = true
  if (canvasStyleData.value.background) {
    fileList.value = [{ url: imgUrlTrans(canvasStyleData.value.background) }]
  }
  nextTick(() => {
    canvasAttrInit = true
  })
}
watch([() => canvasStyleData.value.background], () => {
  if (!fileList.value.length) {
    init()
  }
})

const onColorChange = val => {
  themeAttrChange('customAttr', 'color', val)
}

const themeAttrChange = (custom, property, value) => {
  console.log('themeAttrChange-' + canvasAttrInit)
  if (canvasAttrInit) {
    Object.keys(canvasViewInfo.value).forEach(function (viewId) {
      const viewInfo = canvasViewInfo.value[viewId]
      if (custom === 'customAttr') {
        merge(viewInfo['customAttr'], value)
      } else {
        Object.keys(value).forEach(function (key) {
          if (viewInfo[custom][property][key] !== undefined) {
            viewInfo[custom][property][key] = value[key]
          }
        })
      }
      useEmitt().emitter.emit('renderChart-' + viewId, viewInfo)
    })
    snapshotStore.recordSnapshot('CanvasAttr-themeAttrChange')
  }
}

onMounted(() => {
  init()
})
</script>

<template>
  <div class="attr-container">
    <el-row>
      <el-collapse v-model="canvasAttrActiveNames">
        <el-collapse-item title="尺寸" name="size">
          <el-row class="item-show">
            <span style="margin-left: 10px; color: #757575" title="宽">W</span>
            <de-input-num
              themes="dark"
              v-model="canvasStyleData.width"
              :min="600"
              :max="4096"
            ></de-input-num>
            <span style="margin-left: 10px; color: #757575" title="高">H</span>
            <de-input-num
              themes="dark"
              v-model="canvasStyleData.height"
              :min="600"
              :max="4096"
            ></de-input-num>
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="背景" name="background">
          <el-row>
            <el-radio-group v-model="canvasStyleData.backgroundType">
              <el-radio label="backgroundColor">颜色</el-radio>
              <el-radio label="background">图片</el-radio>
            </el-radio-group>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'backgroundColor'">
            <el-color-picker
              v-model="canvasStyleData.backgroundColor"
              is-custom
              size="small"
              show-alpha
            ></el-color-picker>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'background'" class="img-area">
            <el-col style="width: 130px !important">
              <el-upload
                action=""
                accept=".jpeg,.jpg,.png,.gif,.svg"
                class="avatar-uploader"
                list-type="picture-card"
                :class="{ disabled: uploadDisabled }"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :before-upload="beforeUploadCheck"
                :http-request="upload"
                :file-list="fileList"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <el-dialog
                top="25vh"
                width="600px"
                :append-to-body="true"
                :destroy-on-close="true"
                v-model="dialogVisible"
              >
                <img width="550" :src="dialogImageUrl" />
              </el-dialog>
            </el-col>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'background'">
            <span v-show="!canvasStyleData.background" class="image-hint">支持JPG、PNG、GIF</span>
            <span v-show="canvasStyleData.background" class="re-update-span" @click="goFile"
              >重新上传</span
            >
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="配色" name="color">
          <component-color-selector themes="dark" @onColorChange="onColorChange" />
        </el-collapse-item>
        <el-collapse-item title="刷新配置" name="overallSetting">
          <overall-setting themes="dark" />
        </el-collapse-item>
      </el-collapse>
      <input
        id="input"
        ref="files"
        type="file"
        accept=".jpeg,.jpg,.png,.gif"
        hidden
        @click="
          e => {
            e.target.value = ''
          }
        "
        @change="reUpload"
      />
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.item-show {
  display: flex;
  text-align: center;
  padding-top: 18px;
  min-width: 220px;
}

.attr-container {
  background-color: rgba(37, 45, 54, 1);
  color: #fff;
  z-index: 20;
  height: 100%;
  width: 100%;
  min-width: 220px;
}

:deep(.ed-collapse-item__header) {
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid @main-collapse-border-dark !important;
  height: 38px !important;
}
:deep(.ed-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

:deep(.ed-collapse-item__wrap) {
  border-bottom: 1px solid rgb(57 57 57) !important;
}
:deep(.ed-collapse) {
  width: 100%;
}

.disabled :deep(.el-upload--picture-card) {
  display: none;
}

.avatar-uploader :deep(.ed-upload) {
  width: 120px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader :deep(.ed-upload-list li) {
  width: 120px !important;
  height: 80px !important;
}
.avatar-uploader :deep(.ed-upload--picture-card) {
  background: rgba(0, 0, 0, 0);
}
.img-area {
  width: 120px;
  height: 80px;
  margin-top: 0px;
  margin-bottom: 20px;
  overflow: hidden;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type :deep(.ed-radio__input) {
  display: none;
}

.ed-radio {
  color: #757575;
}

.custom-color-style {
}

.custom-color-style :deep(.ed-radio) {
  margin: 0 2px 0 0 !important;
  border: 1px solid transparent;
}

.custom-color-style :deep(.ed-radio__label) {
  padding-left: 0;
}

.custom-color-style :deep(.ed-radio.is-checked) {
  border: 1px solid #0a7be0;
}

.image-hint {
  color: #8f959e;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.re-update-span {
  cursor: pointer;
  color: #3370ff;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}
</style>
