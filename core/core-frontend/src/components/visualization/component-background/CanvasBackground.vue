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
      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox size="small" :effect="themes" v-model="canvasStyleData.backgroundColorSelect">
          {{ $t('chart.color') }}
        </el-checkbox>
      </el-form-item>

      <div class="indented-container">
        <div class="indented-item">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-color-picker
              v-model="canvasStyleData.backgroundColor"
              :effect="themes"
              :disabled="!canvasStyleData.backgroundColorSelect"
              :trigger-width="computedBackgroundColorPickerWidth"
              is-custom
              show-alpha
              class="color-picker-style"
              :predefine="state.predefineColors"
            />
          </el-form-item>
        </div>
      </div>

      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox size="small" :effect="themes" v-model="canvasStyleData.backgroundImageEnable">
          {{ t('visualization.background') }}
        </el-checkbox>
      </el-form-item>

      <div class="indented-container">
        <div
          class="indented-item"
          :class="{
            disabled: !canvasStyleData.backgroundImageEnable
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
              :disabled="!canvasStyleData.backgroundImageEnable"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <el-row>
              <span v-if="!canvasStyleData.background" class="image-hint">支持JPG、PNG、GIF</span>
              <span v-if="canvasStyleData.background" class="re-update-span" @click="goFile"
                >重新上传</span
              >
            </el-row>
          </div>

          <img-view-dialog
            v-model="state.dialogVisible"
            :image-url="state.dialogImageUrl"
          ></img-view-dialog>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { useI18n } from '@/hooks/web/useI18n'
import elementResizeDetectorMaker from 'element-resize-detector'
import { ElMessage } from 'element-plus-secondary'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const emits = defineEmits(['onBackgroundChange'])
const files = ref(null)
const maxImageSize = 15000000

const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)

const state = reactive({
  BackgroundShowMap: {},
  checked: false,
  backgroundOrigin: {},
  fileList: [],
  dialogImageUrl: '',
  dialogVisible: false,
  uploadDisabled: false,
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
    canvasStyleData.value.background = fileUrl
    state.fileList = [{ url: imgUrlTrans(canvasStyleData.value.background) }]
    onBackgroundChange()
  })
}

const init = () => {
  if (canvasStyleData.value.background) {
    state.fileList.push({ url: imgUrlTrans(canvasStyleData.value.background) })
  } else {
    state.fileList = []
  }
}

const handleRemove = (file, fileList) => {
  state.uploadDisabled = false
  canvasStyleData.value.background = null
  state.fileList = []
  onBackgroundChange()
}
const handlePictureCardPreview = file => {
  state.dialogImageUrl = file.url
  state.dialogVisible = true
}
const upload = file => {
  uploadFileResult(file.file, fileUrl => {
    canvasStyleData.value.background = fileUrl
    onBackgroundChange()
  })
}

const onBackgroundChange = () => {
  snapshotStore.recordSnapshotCache()
}

const bgForm = ref()
const containerWidth = ref()

const computedBackgroundColorPickerWidth = 50

onMounted(() => {
  init()
})

watch(
  () => canvasStyleData.value.background,
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
  height: 100px !important;
  text-align: center;
  padding: 0px 5px;
}

.ed-select-dropdown__item.selected::after {
  display: none;
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

<style lang="less">
.board-select {
  min-width: 50px !important;
  width: 304px;
  .ed-scrollbar__view {
    display: grid !important;
    grid-template-columns: repeat(3, 1fr) !important;
  }
  .ed-select-dropdown__item.hover {
    background-color: rgba(0, 0, 0, 0) !important;
  }
  .ed-select-dropdown__item.selected {
    background-color: rgba(0, 0, 0, 0) !important;
  }
}
</style>
