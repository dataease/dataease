<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import {
  COLOR_CASES,
  COLOR_PANEL,
  DEFAULT_COLOR_CASE
} from '@/views/chart/components/editor/util/chart'
import { useI18n } from '@/hooks/web/useI18n'
import { imgUrlTrans } from '@/utils/imgUtils'
import Slider from '@/components/dashboard/subject-setting/pre-subject/Slider.vue'
import OverallSetting from '@/components/dashboard/subject-setting/dashboard-style/OverallSetting.vue'
import ComponentColorSelector from '@/components/dashboard/subject-setting/dashboard-style/ComponentColorSelector.vue'
import { adaptCurThemeCommonStyleAll } from '@/utils/canvasStyle'
import eventBus from '@/utils/eventBus'
import ViewSimpleTitle from '@/components/dashboard/subject-setting/dashboard-style/ViewSimpleTitle.vue'
import FilterStyleSimpleSelector from '@/components/dashboard/subject-setting/dashboard-style/FilterStyleSimpleSelector.vue'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { deepCopy } from '@/utils/utils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { merge } from 'lodash-es'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData, componentData, canvasViewInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()
const files = ref(null)
const maxImageSize = 15000000
let initReady = false

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
let canvasAttrInit = false

const canvasAttrActiveNames = ref(['style'])

const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

const handleRemove = (file, fileList) => {
  uploadDisabled.value = false
  canvasStyleData.value.background = null
  fileList.value = []
  snapshotStore.recordSnapshot('DbCanvasAttr-handleRemove')
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
  colorIndex: 0,
  sliderShow: true,
  collapseShow: true
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
    if (fileUrl) {
      canvasStyleData.value.background = fileUrl
      fileList.value = [{ url: imgUrlTrans(canvasStyleData.value.background) }]
    }
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
}

const onSubjectChange = () => {
  state.collapseShow = false
  nextTick(() => {
    init()
    dataMerge()
    state.collapseShow = true
  })
}
const sliderReload = () => {
  state.sliderShow = false
  nextTick(() => {
    state.sliderShow = true
  })
}

const dataMerge = () => {
  adaptCurThemeCommonStyleAll()
  snapshotStore.recordSnapshot('DbCanvasAttr-dataMerge')
}

const handleChange = val => {
  return null
}

const onChangePanelStyle = parma => {
  return null
}

const onColorChange = val => {
  themeAttrChange('customAttr', 'color', val)
}
const onTextChange = val => {
  themeAttrChange('customStyle', 'text', val)
}
const themeAttrChange = (custom, property, value) => {
  if (canvasAttrInit) {
    // console.log('custom=' + custom + ';property=' + property + ';value=' + JSON.stringify(value))
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
    snapshotStore.recordSnapshot('DbCanvasAttr-themeAttrChange')
  }
}

const themeColorChange = () => {
  //do themeColorChange
}

const componentBackgroundChange = val => {
  canvasStyleData.value.component.chartCommonStyle = val
  componentData.value.forEach(component => {
    component.commonBackground = deepCopy(val)
  })
}

watch([() => canvasStyleData.value.background], () => {
  if (!fileList.value.length && !initReady) {
    init()
  }
})

onMounted(() => {
  eventBus.on('onSubjectChange', onSubjectChange)
  nextTick(() => {
    canvasAttrInit = true
  })
})
</script>

<template>
  <div class="attr-container">
    <el-row v-if="state.collapseShow">
      <el-collapse v-model="canvasAttrActiveNames">
        <el-collapse-item title="仪表板风格" name="style">
          <el-row class="item-show">
            <slider></slider>
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="整体配置" name="overallSetting">
          <el-row class="item-show">
            <overall-setting @onThemeColorChange="themeColorChange"></overall-setting>
          </el-row>
        </el-collapse-item>

        <el-collapse-item title="仪表板背景" name="background">
          <el-row class="item-show custom-row">
            <el-row>
              <el-radio-group v-model="canvasStyleData.backgroundType">
                <el-radio label="backgroundColor">填充</el-radio>
                <el-radio label="background">图片</el-radio>
              </el-radio-group>
            </el-row>
            <el-row
              v-show="canvasStyleData.backgroundType === 'backgroundColor'"
              class="margin-top8"
            >
              <el-color-picker
                v-model="canvasStyleData.backgroundColor"
                show-alpha
                is-custom
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
                  :http-request="upload"
                  :before-upload="beforeUploadCheck"
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
              <span v-show="!canvasStyleData.background" class="image-hint"
                >当前支持.jpeg,.jpg,.png,.gif文件,大小不要超过15M</span
              >
              <span v-show="canvasStyleData.background" class="re-update-span" @click="goFile"
                >重新上传</span
              >
            </el-row>
          </el-row>
        </el-collapse-item>
        <el-collapse-item :title="t('visualization.view_style')" name="componentStyle">
          <background-overall-common
            :common-background-pop="canvasStyleData.component.chartCommonStyle"
            component-position="'dashboard'"
            class="item-show"
            themes="light"
            @onBackgroundChange="componentBackgroundChange"
          ></background-overall-common>
        </el-collapse-item>
        <el-collapse-item :title="'视图配色'" name="graphical">
          <component-color-selector
            class="item-show"
            @onColorChange="onColorChange"
          ></component-color-selector>
        </el-collapse-item>
        <el-collapse-item name="viewTitle">
          <template #title>
            {{ t('visualization.chart_title') }}
            <el-switch
              style="margin-right: 10px; margin-left: auto"
              v-model="canvasStyleData.component.chartTitle.show"
              @click.stop.prevent="onTextChange(canvasStyleData.component.chartTitle)"
            />
          </template>
          <view-simple-title class="item-show" @onTextChange="onTextChange"></view-simple-title>
        </el-collapse-item>
        <el-collapse-item :title="t('visualization.filter_component')" name="filterComponent">
          <filter-style-simple-selector class="item-show"></filter-style-simple-selector>
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
  width: 100%;
  display: flex;
  text-align: center;
  padding: 16px 8px 0 8px;
}

.attr-container {
  color: #fff;
  z-index: 20;
  height: 100%;
  width: 100%;
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
  margin-top: 8px;
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

:deep(.ed-collapse-item__header) {
  background-color: #f5f6f7 !important;
  padding-left: 5px;
  height: 38px !important;
}

:deep(.ed-radio__label) {
  font-size: 12px !important;
}
:deep(.ed-checkbox__label) {
  font-size: 12px !important;
}

.ed-radio {
  font-weight: 400;
  height: 20px;
}
.ed-checkbox {
  font-weight: 400;
  height: 20px;
}
.margin-top8 {
  margin-top: 8px !important;
}
</style>
