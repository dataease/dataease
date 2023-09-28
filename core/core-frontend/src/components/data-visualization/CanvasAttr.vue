<script setup lang="tsx">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { ElFormItem, ElIcon, ElMessage } from 'element-plus-secondary'
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
  <div class="attr-container de-collapse-style">
    <el-collapse v-model="canvasAttrActiveNames">
      <el-collapse-item effect="dark" title="尺寸" name="size">
        <el-form label-position="left" :label-width="14">
          <el-row :gutter="8" class="m-size">
            <el-col :span="12">
              <el-form-item class="form-item form-item-dark" label="W">
                <el-input-number
                  effect="dark"
                  size="middle"
                  :min="600"
                  :max="4096"
                  v-model="canvasStyleData.width"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item class="form-item form-item-dark" label="H">
                <el-input-number
                  effect="dark"
                  size="middle"
                  :min="600"
                  :max="4096"
                  v-model="canvasStyleData.height"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-collapse-item>
      <el-collapse-item effect="dark" title="背景" name="background">
        <el-form label-position="top">
          <el-form-item class="form-item form-item-dark margin-bottom-8">
            <el-radio-group effect="dark" v-model="canvasStyleData.backgroundType">
              <el-radio effect="dark" label="backgroundColor">颜色</el-radio>
              <el-radio effect="dark" label="background">图片</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            class="form-item form-item-dark"
            v-if="canvasStyleData.backgroundType === 'backgroundColor'"
          >
            <el-color-picker
              v-model="canvasStyleData.backgroundColor"
              :trigger-width="108"
              is-custom
              size="small"
              show-alpha
            />
          </el-form-item>
          <el-row v-if="canvasStyleData.backgroundType === 'background'" class="img-area">
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
          <el-row v-if="canvasStyleData.backgroundType === 'background'">
            <span v-if="!canvasStyleData.background" class="image-hint">支持JPG、PNG、GIF</span>
            <span v-if="canvasStyleData.background" class="re-update-span" @click="goFile"
              >重新上传</span
            >
          </el-row>
        </el-form>
      </el-collapse-item>
      <el-collapse-item effect="dark" title="配色" name="color" class="no-padding no-border-bottom">
        <component-color-selector themes="dark" @onColorChange="onColorChange" />
      </el-collapse-item>
      <el-collapse-item effect="dark" title="刷新配置" name="overallSetting">
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
  </div>
</template>

<style lang="less" scoped>
:deep(.ed-collapse-item) {
  &:first-child {
    .ed-collapse-item__header {
      border-top: none;
    }
  }
}

.de-collapse-style {
  :deep(.ed-collapse-item__header) {
    height: 36px !important;
    line-height: 36px !important;
    font-size: 12px !important;
    padding: 0 !important;
    font-weight: 500 !important;

    .ed-collapse-item__arrow {
      margin: 0 6px 0 8px;
    }
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px 8px 0;
    border: none;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}

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

:deep(.ed-collapse-item__wrap) {
  border-bottom: none;
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

.m-size {
  :deep(.ed-form-item) {
    display: flex !important;
  }
}

:deep(.ed-form-item) {
  .ed-radio.ed-radio--small .ed-radio__inner {
    width: 14px;
    height: 14px;
  }
  .ed-input__inner {
    font-size: 12px;
    font-weight: 400;
  }
  .ed-input {
    --ed-input-height: 28px;

    .ed-input__suffix {
      height: 26px;
    }
  }
  .ed-input-number {
    width: 100%;

    .ed-input-number__decrease {
      --ed-input-number-controls-height: 13px;
    }
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }
  .ed-select {
    width: 100%;
    .ed-input__inner {
      height: 26px !important;
    }
  }
  .ed-checkbox {
    .ed-checkbox__label {
      font-size: 12px;
    }
  }
  .ed-color-picker {
    .ed-color-picker__mask {
      height: 26px;
      width: calc(100% - 2px) !important;
    }
  }
  .ed-radio {
    height: 20px;
    .ed-radio__label {
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }
  }

  &.margin-bottom-8 {
    margin-bottom: 8px;
  }
}
:deep(.ed-checkbox__label) {
  color: #1f2329;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
}
:deep(.ed-checkbox--dark) {
  .ed-checkbox__label {
    color: @dv-canvas-main-font-color;
  }
}

:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
}
:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @canvas-main-font-color-dark;
  }
}
.no-padding {
  :deep(.ed-collapse-item__content) {
    padding: 0;
  }
}
.no-border-bottom {
  :deep(.ed-collapse-item__wrap) {
    border-bottom: none;
  }
}
</style>
