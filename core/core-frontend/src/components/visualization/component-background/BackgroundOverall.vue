<template>
  <el-row class="custom-row" style="width: 100%; padding: 8px">
    <el-row style="height: 50px; overflow: hidden">
      <el-col :span="8">
        <span class="params-title">{{ t('visualization.inner_padding') }}</span>
      </el-col>
      <el-col :span="16">
        <el-slider
          v-model="state.commonBackground.innerPadding"
          show-input
          :show-input-controls="false"
          input-size="mini"
          :max="50"
          @change="themeChange('innerPadding')"
        />
      </el-col>
    </el-row>
    <el-row style="height: 50px; overflow: hidden">
      <el-col :span="8">
        <span class="params-title">{{ t('visualization.board_radio') }}</span>
      </el-col>
      <el-col :span="16">
        <el-slider
          v-model="state.commonBackground.borderRadius"
          show-input
          :show-input-controls="false"
          input-size="mini"
          @change="themeChange('borderRadius')"
        />
      </el-col>
    </el-row>

    <el-row style="height: 40px; overflow: hidden">
      <el-col :span="6" style="padding-top: 5px">
        <el-checkbox
          v-model="state.commonBackground.backgroundColorSelect"
          @change="themeChange('backgroundColorSelect')"
        >
          {{ $t('chart.color') }}
        </el-checkbox>
      </el-col>
      <el-col :span="3" style="padding-top: 5px">
        <el-color-picker
          v-model="state.commonBackground.color"
          :disabled="!state.commonBackground.backgroundColorSelect"
          size="mini"
          class="color-picker-style"
          :predefine="state.predefineColors"
          @change="themeChange('color')"
        />
      </el-col>
      <el-col :span="5">
        <span class="params-title-small">{{ $t('chart.not_alpha') }}</span>
      </el-col>
      <el-col :span="10">
        <el-slider
          v-model="state.commonBackground.alpha"
          :disabled="!state.commonBackground.backgroundColorSelect"
          show-input
          :show-input-controls="false"
          input-size="mini"
          @change="themeChange('alpha')"
        />
      </el-col>
    </el-row>

    <el-row style="height: 50px">
      <el-col :span="4" style="padding-top: 5px">
        <el-checkbox v-model="state.commonBackground.enable" @change="themeChange('enable')"
          >{{ t('visualization.background') }}
        </el-checkbox>
      </el-col>
      <el-col :span="20">
        <span class="tips-area"> Tips:{{ t('visualization.choose_background_tips') }} </span>
      </el-col>
    </el-row>
    <el-row v-if="state.commonBackground.enable" style="padding-left: 10px">
      <el-row style="height: 80px; margin-top: 0; margin-bottom: 20px; overflow: hidden">
        <el-col :span="5">
          <el-radio
            v-model="state.commonBackground.backgroundType"
            label="outerImage"
            @change="themeChange('backgroundType')"
            >{{ t('visualization.photo') }}
          </el-radio>
        </el-col>
        <el-col style="width: 130px !important">
          <el-upload
            action=""
            accept=".jpeg,.jpg,.png,.gif,.svg"
            class="avatar-uploader"
            list-type="picture-card"
            :class="{ disabled: state.uploadDisabled }"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
            :http-request="upload"
            :file-list="state.fileList"
          >
            <i class="el-icon-plus" />
          </el-upload>
          <el-dialog
            top="25vh"
            width="600px"
            :append-to-body="true"
            :destroy-on-close="true"
            v-model:visible="state.dialogVisible"
          >
            <img width="100%" :src="state.dialogImageUrl" />
          </el-dialog>
        </el-col>
      </el-row>
      <el-row>
        <el-col style="position: relative">
          <el-radio
            v-model="state.commonBackground.backgroundType"
            label="innerImage"
            @change="themeChange('backgroundType')"
            >{{ t('visualization.board') }}
          </el-radio>
          <el-color-picker
            v-model="state.commonBackground.innerImageColor"
            :title="t('visualization.border_color_setting')"
            style="position: absolute; top: -3px; left: 60px"
            size="mini"
            class="color-picker-style"
            :predefine="state.predefineColors"
            @change="themeChange('innerImageColor')"
          />
        </el-col>
      </el-row>
      <el-row>
        <el-col :style="customStyle" class="main-col">
          <el-row v-for="(value, key) in state.BackgroundShowMap" :key="key" class="border-area">
            <el-col v-for="item in value" :key="item.id" :span="12">
              <background-item-overall
                :common-background="state.commonBackground"
                :template="item"
                @borderChange="themeChange('innerImage')"
              />
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { queryVisualizationBackground } from '@/api/visualization/visualizationBackground'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { computed, onMounted, reactive } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { imgUrlTrans } from '@/utils/imgUtils'
import eventBus from '@/utils/eventBus'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { uploadFileResult } from '@/api/staticResource'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, componentData } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()

const state = reactive({
  commonBackground: {},
  BackgroundShowMap: {},
  checked: false,
  backgroundOrigin: {},
  fileList: [],
  dialogImageUrl: '',
  dialogVisible: false,
  uploadDisabled: false,
  panel: null,
  predefineColors: COLOR_PANEL
})

const customStyle = computed(() => {
  let style = {}
  if (canvasStyleData.value.openCommonStyle) {
    if (
      canvasStyleData.value.dashboard.backgroundType === 'image' &&
      canvasStyleData.value.dashboard.imageUrl
    ) {
      style = {
        background: `url(${imgUrlTrans(canvasStyleData.value.dashboard.imageUrl)}) no-repeat`,
        ...style
      }
    } else if (canvasStyleData.value.dashboard.backgroundType === 'color') {
      style = {
        background: canvasStyleData.value.dashboard.color,
        ...style
      }
    }
  }
  if (!style['background']) {
    style['background'] = '#FFFFFF'
  }
  return style
})

const queryBackground = () => {
  queryVisualizationBackground().then(response => {
    state.BackgroundShowMap = response.data
  })
}

const init = () => {
  state.commonBackground = canvasStyleData.value.component.chartCommonStyle
  if (
    state.commonBackground &&
    state.commonBackground['outerImage'] &&
    typeof state.commonBackground['outerImage'] === 'string'
  ) {
    state.fileList.push({ url: imgUrlTrans(state.commonBackground['outerImage']) })
  }
  queryBackground()
}
const commitStyle = () => {
  snapshotStore.recordSnapshot()
}
const onChangeType = () => {
  commitStyle()
}
const handleRemove = (file, fileList) => {
  state.uploadDisabled = false
  state.commonBackground['outerImage'] = null
  themeChange('outerImage')
  state.fileList = []
  commitStyle()
}
const handlePictureCardPreview = file => {
  state.dialogImageUrl = file.url
  state.dialogVisible = true
}
const upload = file => {
  uploadFileResult(file.file, fileUrl => {
    state.commonBackground['outerImage'] = fileUrl
    themeChange('outerImage')
  })
}

const themeChange = modifyName => {
  componentData.value.forEach((item, index) => {
    if (item.type === 'view') {
      item.commonBackground[modifyName] = state.commonBackground[modifyName]
    }
  })
  snapshotStore.recordSnapshot()
}

onMounted(() => {
  init()
  eventBus.on('onThemeColorChange', init)
})
</script>

<style scoped lang="less">
.tips-area {
  color: #909399;
  font-size: 8px;
  margin-left: 10px;
  line-height: 40px;
}
.ed-card-template {
  width: 100%;
  height: 100%;
}

.main-col {
  background-size: 100% 100% !important;
  padding-left: 10px;
  margin-top: 10px;
  height: 230px;
  overflow-y: auto;
  flex-direction: row;
}

.root-class {
  margin: 15px 0px 5px;
  text-align: center;
}

.avatar-uploader :deep(.ed-upload) {
  width: 120px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader :deep(.ed-upload-list) li {
  width: 120px !important;
  height: 80px !important;
}

.disabled :deep(.ed-upload--picture-card) {
  display: none;
}

.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.ed-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px;
}

.ed-form-item {
  margin-bottom: 6px;
}

.main-content {
}

.params-title {
  font-weight: 400 !important;
  font-size: 14px !important;
  color: #1f2329 !important;
  line-height: 40px;
}

.params-title-small {
  font-size: 12px !important;
  color: #1f2329 !important;
  line-height: 40px;
}

:deep(.ed-slider__input) {
  width: 40px;
  padding-left: 0px;
  padding-right: 0px;
}

:deep(.ed-input__inner) {
  padding: 0px !important;
}

:deep(.ed-slider__runway) {
  margin-right: 60px !important;
}
.ed-row {
  flex-direction: column;
}

.border-area {
  flex-direction: row;
}
</style>
