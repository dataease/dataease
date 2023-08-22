<template>
  <el-row>
    <el-row style="width: 100%" :class="themes">
      <el-col :span="12" style="padding-right: 4px">
        <el-row class="custom-item-text-row"
          ><span class="custom-item-text bl">{{ t('visualization.inner_padding') }}</span>
        </el-row>
        <el-row class="function-area">
          <el-input-number
            :effect="themes"
            controls-position="right"
            :min="0"
            :max="100"
            size="small"
            v-model="state.commonBackground.innerPadding"
            @change="onBackgroundChange"
          ></el-input-number>
        </el-row>
      </el-col>
      <el-col :span="12" style="padding-left: 4px">
        <el-row class="custom-item-text-row"
          ><span class="custom-item-text bl">{{ t('visualization.board_radio') }}</span>
        </el-row>
        <el-row class="function-area">
          <el-input-number
            :effect="themes"
            controls-position="right"
            :min="0"
            :max="100"
            size="small"
            v-model="state.commonBackground.borderRadius"
            @change="onBackgroundChange"
          ></el-input-number>
        </el-row>
      </el-col>
    </el-row>
    <el-row style="width: 100%" class="margin-top16">
      <el-col :span="12" style="padding-right: 4px">
        <el-row class="custom-item-text-row">
          <el-checkbox
            :effect="themes"
            v-model="state.commonBackground.backgroundColorSelect"
            @change="onBackgroundChange"
          >
            {{ $t('chart.color') }}
          </el-checkbox>
        </el-row>
        <el-row class="function-area" style="margin-left: 20px">
          <el-color-picker
            v-model="state.commonBackground.backgroundColor"
            :effect="themes"
            :disabled="!state.commonBackground.backgroundColorSelect"
            size="small"
            class="color-picker-style"
            :predefine="state.predefineColors"
            @change="onBackgroundChange"
          />
        </el-row>
      </el-col>
      <el-col :span="12" style="padding-left: 4px">
        <el-row class="custom-item-text-row"
          ><span class="custom-item-text bl">{{ $t('chart.not_alpha') }}</span>
        </el-row>
        <el-row class="function-area">
          <el-input-number
            controls-position="right"
            :effect="themes"
            :disabled="!state.commonBackground.backgroundColorSelect"
            v-model="state.commonBackground.alpha"
            :min="0"
            :max="100"
            size="small"
            @change="onBackgroundChange"
          ></el-input-number>
        </el-row>
      </el-col>
    </el-row>

    <el-row style="width: 100%" class="custom-row margin-top16">
      <el-row class="custom-item-text-row">
        <el-checkbox
          :effect="themes"
          v-model="state.commonBackground.backgroundImageEnable"
          @change="onBackgroundChange"
          >{{ t('visualization.background') }}
        </el-checkbox>
      </el-row>
      <el-row class="function-area custom-row" style="margin-left: 20px">
        <el-row>
          <el-radio-group
            :effect="themes"
            v-model="state.commonBackground.backgroundType"
            @change="onBackgroundChange"
          >
            <el-radio :effect="themes" label="outerImage">{{ t('visualization.photo') }}</el-radio>
            <el-radio :effect="themes" label="innerImage">{{ t('visualization.board') }}</el-radio>
          </el-radio-group>
        </el-row>
        <el-row>
          <el-row
            v-show="state.commonBackground.backgroundType === 'innerImage'"
            style="width: 100%"
            class="margin-top8"
          >
            <el-color-picker
              v-model="state.commonBackground.innerImageColor"
              :effect="themes"
              :title="t('visualization.border_color_setting')"
              style="position: absolute; top: -3px; left: 60px"
              size="small"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="onBackgroundChange"
            />
            <el-select
              v-model="state.commonBackground.innerImage"
              :effect="themes"
              placeholder="选择边框..."
              style="width: 155px; margin-left: 8px"
              size="small"
              @change="onBackgroundChange"
            >
              <el-option
                v-for="(item, index) in state.BackgroundShowMap['default']"
                :key="index"
                :label="item.name"
                :value="item.url"
              >
                <Icon
                  style="width: 120px; height: 70px"
                  :style="{ color: state.commonBackground.innerImageColor }"
                  class-name="svg-background"
                  :name="mainIconClass(item)"
                />
              </el-option>
            </el-select>
          </el-row>
          <el-col
            v-show="state.commonBackground.backgroundType === 'outerImage'"
            style="width: 130px !important; height: 80px; overflow: hidden; text-align: left"
            class="margin-top8"
          >
            <el-upload
              action=""
              :effect="themes"
              accept=".jpeg,.jpg,.png,.gif,.svg"
              class="avatar-uploader"
              list-type="picture-card"
              :class="{ disabled: state.uploadDisabled }"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :http-request="upload"
              :file-list="state.fileList"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <el-dialog
              top="25vh"
              width="600px"
              :append-to-body="true"
              :destroy-on-close="true"
              v-model="state.dialogVisible"
            >
              <img width="550" :src="state.dialogImageUrl" />
            </el-dialog>
          </el-col>
        </el-row>
      </el-row>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { queryVisualizationBackground } from '@/api/visualization/visualizationBackground'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { computed, onMounted, reactive, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { imgUrlTrans } from '@/utils/imgUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { uploadFileResult } from '@/api/staticResource'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const emits = defineEmits(['onBackgroundChange'])

const props = defineProps({
  componentPosition: {
    type: String,
    default: 'dashboard'
  },
  themes: {
    type: String,
    default: 'dark'
  },
  commonBackgroundPop: {
    type: Object,
    required: true
  }
})

const { componentPosition, themes } = toRefs(props)

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

const mainIconClass = itemUrl => {
  return itemUrl.url.replace('board/', '').replace('.svg', '')
}

const queryBackground = () => {
  queryVisualizationBackground().then(response => {
    state.BackgroundShowMap = response.data
  })
}

const init = () => {
  state.commonBackground = deepCopy(props.commonBackgroundPop)
  if (state.commonBackground['outerImage']) {
    state.fileList.push({ url: imgUrlTrans(state.commonBackground['outerImage']) })
  } else {
    state.fileList = []
  }
}
queryBackground()
const commitStyle = () => {
  snapshotStore.recordSnapshot('BackgroundOverallCommon-themeChange')
}
const onChangeType = () => {
  commitStyle()
}
const handleRemove = (file, fileList) => {
  state.uploadDisabled = false
  state.commonBackground['outerImage'] = null
  state.fileList = []
  onBackgroundChange()
  commitStyle()
}
const handlePictureCardPreview = file => {
  state.dialogImageUrl = file.url
  state.dialogVisible = true
}
const upload = file => {
  uploadFileResult(file.file, fileUrl => {
    state.commonBackground['outerImage'] = fileUrl
    onBackgroundChange()
  })
}

const onBackgroundChange = () => {
  emits('onBackgroundChange', state.commonBackground)
}

onMounted(() => {
  init()
})

watch(
  () => props.commonBackgroundPop,
  () => {
    init()
  }
)
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

.avatar-uploader {
  width: 120px;
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
:deep(.ed-upload--picture-card) {
  background: none;
}
:deep(.ed-upload-list__item) {
  background: none;
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

.border-area {
  flex-direction: row;
}

.bl {
  justify-content: flex-start;
  display: flex;
}

.br {
  flex: 1;
  justify-content: flex-end;
  display: flex;
}

.function-area {
  margin-top: 8px;
}

.margin-top16 {
  margin-top: 16px !important;
}

.margin-top8 {
  margin-top: 8px !important;
}
.custom-item-text-row {
  display: flex;
  font-size: 12px;
}

.ed-select-dropdown__item {
  height: 80px !important;
  text-align: center;
  padding: 5px 15px;
}

.ed-radio {
  font-weight: 400;
  height: 20px;
}
.ed-checkbox {
  font-weight: 400;
  height: 20px;
}

.custom-item-text-row {
  display: flex;
}

.custom-item-text {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 20px;
  color: #646a73 !important;
}

:deep(.ed-input-number) {
  width: 100%;
}

:deep(.ed-input__inner) {
  text-align: left;
}

:deep(.ed-checkbox--dark) {
  color: #646a73 !important;
  font-weight: 400 !important;
}
:deep(.ed-radio--dark .ed-radio__label) {
  color: #646a73 !important;
  font-size: 12px !important;
  font-weight: 400 !important;
}

:deep(.ed-checkbox__label) {
  font-size: 12px !important;
}
</style>
