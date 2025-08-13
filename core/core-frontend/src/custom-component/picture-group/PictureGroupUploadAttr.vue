<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

import { storeToRefs } from 'pinia'
import { ElIcon } from 'element-plus-secondary'
import { ref, onMounted, onBeforeUnmount, watch, PropType } from 'vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { imgUrlTrans } from '@/utils/imgUtils'
import eventBus from '@/utils/eventBus'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { toRefs } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: {
          urlList: []
        }
      }
    }
  },
  view: {
    type: Object,
    required: true
  }
})

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { element } = toRefs(props)

const { curComponent } = storeToRefs(dvMainStore)

const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const uploadDisabled = ref(false)
const files = ref(null)

const handlePictureCardPreview = file => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

const handleRemove = file => {
  uploadDisabled.value = false
  let file_static_part = file.url.split('static-resource/')[1]
  let index = element.value.propValue['urlList'].findIndex(
    item => item.url.split('static-resource/')[1] === file_static_part
  )
  if (index !== -1) {
    element.value.propValue['urlList'].splice(index, 1)
    useEmitt().emitter.emit('calcData-' + element.value.id)
  }
  snapshotStore.recordSnapshotCache('picture-handleRemove')
}
async function upload(file) {
  if (element.value.propValue.urlList.length < 10) {
    uploadFileResult(file.file, fileUrl => {
      snapshotStore.recordSnapshotCache('pic-upload')
      element.value.propValue.urlList.unshift({ name: file.file.name, url: fileUrl })
      useEmitt().emitter.emit('calcData-' + element.value.id)
    })
  }
}

const onStyleChange = () => {
  snapshotStore.recordSnapshotCache('pic-onStyleChange')
}

const goFile = () => {
  files.value.click()
}

const fileListInit = () => {
  fileList.value = []
  if (element.value.propValue.urlList && element.value.propValue.urlList.length > 0) {
    element.value.propValue.urlList.forEach(urlInfo => {
      fileList.value.push({ name: urlInfo.name, url: imgUrlTrans(urlInfo.url) })
    })
  }
}
const init = () => {
  fileListInit()
}

watch(
  () => element.value.propValue['urlList'],
  () => {
    init()
  }
)

onMounted(() => {
  init()
  eventBus.on('uploadImg', goFile)
})
onBeforeUnmount(() => {
  eventBus.off('uploadImg', goFile)
})
</script>

<template>
  <el-collapse-item :effect="themes" :title="t('visualization.pic_group')" name="picture">
    <el-row class="img-area" :class="`img-area_${themes}`">
      <el-col style="width: 130px !important">
        <el-upload
          :themes="themes"
          :limit="10"
          action=""
          accept=".jpeg,.jpg,.png,.gif,.svg"
          class="avatar-uploader"
          list-type="picture-card"
          :class="{ disabled: uploadDisabled }"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :before-upload="beforeUploadCheck"
          :http-request="upload"
          multiple
          :file-list="fileList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <img-view-dialog v-model="dialogVisible" :image-url="dialogImageUrl"></img-view-dialog>
      </el-col>
    </el-row>
    <el-row>
      <span style="margin-top: 2px" class="image-hint" :class="`image-hint_${themes}`">
        {{ t('visualization.pic_upload_tips2') }}
      </span>
    </el-row>
    <el-row class="pic-adaptor">
      <el-form-item
        v-if="curComponent.style.adaptation"
        class="form-item form-item-custom"
        :class="'form-item-' + themes"
        :label="t('visualization.pic_adaptor_type')"
        size="small"
        :effect="themes"
      >
        <el-radio-group
          size="small"
          v-model="curComponent.style.adaptation"
          @change="onStyleChange"
          :effect="themes"
        >
          <el-radio label="adaptation" :effect="themes">{{
            t('visualization.pic_adaptation')
          }}</el-radio>
          <el-radio label="original" :effect="themes">{{
            t('visualization.pic_original')
          }}</el-radio>
          <el-radio label="equiratio" :effect="themes">{{
            t('visualization.pic_equiratio')
          }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-row>
  </el-collapse-item>
</template>

<style lang="less" scoped>
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
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 8px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}

.disabled :deep(.el-upload--picture-card) {
  display: none;
}

.avatar-uploader :deep(.ed-upload) {
  width: 80px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader :deep(.ed-upload-list li) {
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
      color: var(--ed-color-primary);
    }
  }
}
.img-area {
  overflow: hidden;

  &.img-area_dark {
    :deep(.ed-upload-list__item).is-success {
      border-color: #434343;
    }
    :deep(.ed-upload--picture-card) {
      background: #373737;
      border-color: #434343;
      .ed-icon {
        color: #ebebeb;
      }
      &:hover {
        .ed-icon {
          color: var(--ed-color-primary);
        }
      }
    }
  }

  &.img-area_light {
    :deep(.ed-upload-list__item).is-success {
      border-color: #dee0e3;
    }
  }
}

.image-hint {
  color: #8f959e;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
  margin-top: 2px;
  &.image-hint_dark {
    color: #757575;
  }
}

.re-update-span {
  cursor: pointer;
  color: var(--ed-color-primary);
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.pic-adaptor {
  margin: 8px 0 8px 0;
  :deep(.ed-form-item__content) {
    margin-top: 8px !important;
  }
}

.form-item-custom {
  .ed-radio {
    margin-right: 2px !important;
  }
}

.drag-data {
  padding-top: 8px;
  padding-bottom: 16px;

  .tree-btn {
    width: 100%;
    margin-top: 8px;
    background: #fff;
    height: 32px;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    display: flex;
    color: #cccccc;
    align-items: center;
    cursor: pointer;
    justify-content: center;
    font-size: 12px;
    &.tree-btn--dark {
      background: rgba(235, 235, 235, 0.05);
      border-color: #5f5f5f;
    }

    &.active {
      color: #3370ff;
      border-color: #3370ff;
    }
  }

  &.no-top-border {
    border-top: none !important;
  }
  &.no-top-padding {
    padding-top: 0 !important;
  }
  &:nth-child(n + 2) {
    border-top: 1px solid @side-outline-border-color;
  }
  &:first-child {
    border-top: none !important;
  }
}
</style>
