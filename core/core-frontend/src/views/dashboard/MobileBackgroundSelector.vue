<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { ref, reactive, unref, onMounted, watch } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { imgUrlTrans } from '@/utils/imgUtils'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import { storeToRefs } from 'pinia'
import { useEmitt } from '@/hooks/web/useEmitt'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { cloneDeep } from 'lodash-es'
const MOBILE_SETTING = {
  customSetting: false,
  color: '#ffffff',
  alpha: 100,
  imageUrl: null,
  backgroundType: 'image'
}
const fileList = ref([])

const mobileSetting = reactive({ ...MOBILE_SETTING })
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const init = () => {
  const { imageUrl } = canvasStyleData.value.mobileSetting || {}
  fileList.value = imageUrl ? [{ url: imgUrlTrans(imageUrl) }] : []
}
onMounted(() => {
  init()
})

const commitStyle = () => {
  const canvasStyleDataCopy = cloneDeep(canvasStyleData.value)
  canvasStyleDataCopy.mobileSetting = unref(mobileSetting)
  dvMainStore.setCanvasStyle(canvasStyleDataCopy)
  useEmitt().emitter.emit('onMobileStatusChange', {
    type: 'setCanvasStyle',
    value: JSON.parse(JSON.stringify(unref(canvasStyleDataCopy)))
  })
  snapshotStore.recordSnapshotCache()
}

const upload = file => {
  return uploadFileResult(file.file, fileUrl => {
    mobileSetting.imageUrl = fileUrl
    commitStyle()
  })
}

const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

const handleRemove = () => {
  mobileSetting.imageUrl = null
  dialogImageUrl.value = ''
  fileList.value = []
  commitStyle()
}
</script>

<template>
  <div class="mobile-bg-setting">
    <div class="is-custom">
      <el-checkbox v-model="mobileSetting.customSetting" label="自定义移动端背景" />
    </div>
    <div class="bg-type">
      <el-radio-group
        v-model="mobileSetting.backgroundType"
        :disabled="!mobileSetting.customSetting"
        @change="commitStyle"
      >
        <el-radio label="color">{{ $t('chart.color') }}</el-radio>
        <el-radio label="image">图片</el-radio>
      </el-radio-group>
    </div>
    <div v-show="mobileSetting.backgroundType === 'color'" class="bg-color-config">
      <el-color-picker
        v-model="mobileSetting.color"
        :predefine="COLOR_PANEL"
        :disabled="!mobileSetting.customSetting"
        @change="commitStyle"
      />
      <span> 不透明度 </span>
      <el-slider
        v-model="mobileSetting.alpha"
        show-input
        :show-input-controls="false"
        @change="commitStyle"
      />
    </div>
    <div
      :class="{
        disabled: !mobileSetting.customSetting
      }"
      v-show="mobileSetting.backgroundType === 'image'"
      class="upload-img"
    >
      <el-upload
        action=""
        accept=".jpeg,.jpg,.png,.gif,.svg"
        class="avatar-uploader"
        list-type="picture-card"
        :http-request="upload"
        :on-preview="handlePictureCardPreview"
        :on-remove="handleRemove"
        :before-upload="beforeUploadCheck"
        :file-list="fileList"
        :disabled="!mobileSetting.customSetting"
      >
        <el-icon><Plus /></el-icon>
      </el-upload>
      <img-view-dialog v-model="dialogVisible" :image-url="dialogImageUrl" />
    </div>
    <div v-show="mobileSetting.backgroundType === 'image'" class="image-hint">
      当前支持jpeg,jpg,png,gif,svg文件,大小15M内
    </div>
  </div>
</template>

<style lang="less" scoped>
.mobile-bg-setting {
  width: 50%;
  .bg-color-config {
    margin-top: 10px;
    display: flex;
    align-items: center;
    & > :nth-child(2) {
      margin: 0 20px;
      white-space: nowrap;
    }
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

  .upload-img {
    margin-top: 8px;
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
          color: #bbbfc4;
        }
      }

      &:hover {
        .ed-icon {
          color: #8f959e;
        }
      }
    }
  }

  .image-hint {
    color: #8f959e;
    font-size: 14px;
    line-height: 22px;
    font-weight: 400;
    margin-top: 8px;
  }
}
</style>
