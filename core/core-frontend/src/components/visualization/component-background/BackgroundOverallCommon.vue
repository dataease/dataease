<template>
  <div style="width: 100%" ref="bgForm">
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
    <el-form label-position="top" style="width: 100%">
      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item
            :label="t('visualization.inner_padding')"
            class="form-item w100"
            :class="'form-item-' + themes"
          >
            <el-input-number
              style="width: 100%"
              :effect="themes"
              controls-position="right"
              size="middle"
              :min="0"
              :max="100"
              v-model="state.commonBackground.innerPadding"
              @change="onBackgroundChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            :label="t('visualization.board_radio')"
            class="form-item w100"
            :class="'form-item-' + themes"
          >
            <el-input-number
              style="width: 100%"
              :effect="themes"
              controls-position="right"
              size="middle"
              :min="0"
              :max="100"
              v-model="state.commonBackground.borderRadius"
              @change="onBackgroundChange"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.commonBackground.backgroundColorSelect"
          @change="onBackgroundChange"
        >
          {{ $t('chart.color') }}
        </el-checkbox>
      </el-form-item>

      <div class="indented-container">
        <div class="indented-item">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-color-picker
              v-model="state.commonBackground.backgroundColor"
              :effect="themes"
              :disabled="!state.commonBackground.backgroundColorSelect"
              :trigger-width="computedBackgroundColorPickerWidth"
              is-custom
              show-alpha
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="onBackgroundChange"
            />
          </el-form-item>
        </div>
      </div>

      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.commonBackground.backgroundImageEnable"
          @change="onBackgroundChange"
        >
          {{ t('visualization.background') }}
        </el-checkbox>
      </el-form-item>

      <div class="indented-container">
        <div class="indented-item">
          <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
            <el-radio-group
              :effect="themes"
              :disabled="!state.commonBackground.backgroundImageEnable"
              v-model="state.commonBackground.backgroundType"
              @change="onBackgroundChange"
            >
              <el-radio :effect="themes" label="outerImage">{{
                t('visualization.photo')
              }}</el-radio>
              <el-radio :effect="themes" label="innerImage">{{
                t('visualization.board')
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <div class="indented-item" v-if="state.commonBackground.backgroundType === 'innerImage'">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-color-picker
              v-model="state.commonBackground.innerImageColor"
              :disabled="!state.commonBackground.backgroundImageEnable"
              :effect="themes"
              :title="t('visualization.border_color_setting')"
              style="position: absolute; top: -3px; left: 60px"
              is-custom
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="onBackgroundChange"
            />
          </el-form-item>
          <el-form-item
            class="form-item fill"
            style="padding-left: 8px"
            :class="'form-item-' + themes"
          >
            <el-select
              :style="{ width: computedBackgroundBorderSelectWidth + 'px' }"
              v-model="state.commonBackground.innerImage"
              size="middle"
              :effect="themes"
              :disabled="!state.commonBackground.backgroundImageEnable"
              placeholder="选择边框..."
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
          </el-form-item>
        </div>
        <div
          class="indented-item"
          v-if="state.commonBackground.backgroundType === 'outerImage'"
          :class="{
            disabled: !state.commonBackground.backgroundImageEnable || state.uploadDisabled
          }"
        >
          <div class="avatar-uploader-container">
            <el-upload
              action=""
              :effect="themes"
              accept=".jpeg,.jpg,.png,.gif,.svg"
              class="avatar-uploader"
              list-type="picture-card"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :before-upload="beforeUploadCheck"
              :http-request="upload"
              :file-list="state.fileList"
              :disabled="!state.commonBackground.backgroundImageEnable"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <el-row>
              <span v-if="!state.commonBackground['outerImage']" class="image-hint"
                >支持JPG、PNG、GIF</span
              >
              <span
                v-if="state.commonBackground['outerImage']"
                class="re-update-span"
                @click="goFile"
                >重新上传</span
              >
            </el-row>
          </div>
          <el-dialog
            top="25vh"
            width="600px"
            :append-to-body="true"
            :destroy-on-close="true"
            v-model="state.dialogVisible"
          >
            <img width="550" :src="state.dialogImageUrl" />
          </el-dialog>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { queryVisualizationBackground } from '@/api/visualization/visualizationBackground'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import elementResizeDetectorMaker from 'element-resize-detector'
import { ElMessage } from 'element-plus-secondary'
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const emits = defineEmits(['onBackgroundChange'])
const files = ref(null)
const maxImageSize = 15000000

const props = withDefaults(
  defineProps<{
    componentPosition?: string
    themes?: EditorTheme
    commonBackgroundPop: any
    backgroundColorPickerWidth?: number
    backgroundBorderSelectWidth?: number
  }>(),
  {
    themes: 'dark',
    componentPosition: 'dashboard',
    backgroundColorPickerWidth: 50,
    backgroundBorderSelectWidth: 108
  }
)

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

const goFile = () => {
  files.value.click()
}

const sizeMessage = () => {
  ElMessage.success('图片大小不符合')
}

const reUpload = e => {
  const file = e.target.files[0]
  if (file.size > maxImageSize) {
    sizeMessage()
  }
  uploadFileResult(file, fileUrl => {
    state.commonBackground['outerImage'] = fileUrl
    state.fileList = [{ url: imgUrlTrans(state.commonBackground['outerImage']) }]
    onBackgroundChange()
  })
}

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
  snapshotStore.recordSnapshot()
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
  snapshotStore.recordSnapshotCache()
  emits('onBackgroundChange', state.commonBackground)
}

const bgForm = ref()
const containerWidth = ref()

const computedBackgroundColorPickerWidth = computed(() => {
  if (containerWidth.value <= 240) {
    return 50
  } else {
    return props.backgroundColorPickerWidth
  }
})
const computedBackgroundBorderSelectWidth = computed(() => {
  if (containerWidth.value <= 240) {
    return 108
  } else {
    return props.backgroundBorderSelectWidth
  }
})

onMounted(() => {
  init()
  const erd = elementResizeDetectorMaker()
  containerWidth.value = bgForm.value?.offsetWidth
  erd.listenTo(bgForm.value, element => {
    nextTick(() => {
      containerWidth.value = bgForm.value?.offsetWidth
    })
  })
})

watch(
  () => props.commonBackgroundPop,
  () => {
    init()
  }
)
</script>

<style scoped lang="less">
:deep(.ed-form-item) {
  display: block;
  margin-bottom: 16px;
}
.avatar-uploader-container {
  margin-bottom: 16px;
}
.avatar-uploader {
  width: 90px;
  height: 80px;
  overflow: hidden;
}
.avatar-uploader {
  width: 90px;
  :deep(.ed-upload) {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }

  :deep(.ed-upload-list li) {
    width: 80px !important;
    height: 80px !important;
  }

  :deep(.ed-upload--picture-card) {
    background: #eff0f1;
    border: 1px dashed #dee0e3;
    border-radius: 4px;

    .ed-icon {
      color: #1f2329;
    }

    &:hover {
      .ed-icon {
        color: #3370ff;
      }
    }
  }
}

.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ed-select-dropdown__item {
  height: 80px !important;
  text-align: center;
  padding: 5px 15px;
}

.indented-container {
  width: 100%;
  padding-left: 22px;

  .indented-item {
    width: 100%;
    display: flex;

    .fill {
      flex: 1;
    }

    &.disabled {
      cursor: not-allowed;
      color: #8f959e;

      :deep(.avatar-uploader) {
        width: 90px;
        pointer-events: none;
      }

      :deep(.ed-upload--picture-card) {
        cursor: not-allowed;

        .ed-icon {
          color: #8f959e;
        }
      }

      &:hover {
        .ed-icon {
          color: #8f959e;
        }
      }
    }
  }
}
.form-item {
  &.margin-bottom-8 {
    margin-bottom: 8px !important;
  }
  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }
}

.re-update-span {
  cursor: pointer;
  color: #3370ff;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.image-hint {
  color: #8f959e;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}
</style>
