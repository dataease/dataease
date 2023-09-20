<template>
  <div style="width: 100%">
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          :label="t('visualization.inner_padding')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            controls-position="right"
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
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            controls-position="right"
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
            is-custom
            :trigger-width="108"
            size="small"
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
          <el-input-number
            controls-position="right"
            :effect="themes"
            :disabled="!state.commonBackground.backgroundColorSelect"
            v-model="state.commonBackground.alpha"
            :min="0"
            :max="100"
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
            <el-radio :effect="themes" label="outerImage">{{ t('visualization.photo') }}</el-radio>
            <el-radio :effect="themes" label="innerImage">{{ t('visualization.board') }}</el-radio>
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
            size="small"
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
            style="width: 108px"
            v-model="state.commonBackground.innerImage"
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
          <span class="hint">支持JPG、PNG、GIF</span>
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
  </div>
</template>

<script setup lang="ts">
import { queryVisualizationBackground } from '@/api/visualization/visualizationBackground'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { computed, onMounted, reactive, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { imgUrlTrans } from '@/utils/imgUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
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
.avatar-uploader-container {
  margin-bottom: 16px;

  .hint {
    color: #8f959e;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
  }
}
.avatar-uploader {
  height: 80px;
  overflow: hidden;
}
.avatar-uploader {
  :deep(.ed-upload) {
    width: 120px;
    height: 80px;
    line-height: 90px;
  }

  :deep(.ed-upload-list li) {
    width: 120px !important;
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
</style>
