<template>
  <el-row>
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
      v-on:change="reUpload"
    />

    <el-dialog
      top="25vh"
      width="600px"
      :append-to-body="true"
      :destroy-on-close="true"
      v-model="state.dialogVisible"
    >
      <img style="width: 100%" :src="state.dialogImageUrl" />
    </el-dialog>
  </el-row>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { imgUrlTrans } from '@/utils/imgUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { uploadFileResult } from '@/api/staticResource'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const emits = defineEmits(['onImgChange'])
const files = ref(null)
const maxImageSize = 15000000

const props = defineProps({
  imgUrl: {
    type: String
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
const imgUrlInner = ref(null)

const state = reactive({
  fileList: [],
  dialogImageUrl: '',
  dialogVisible: false,
  uploadDisabled: false
})

const init = () => {
  imgUrlInner.value = props.imgUrl
  if (imgUrlInner.value) {
    state.fileList.push({ url: imgUrlTrans(imgUrlInner.value) })
  } else {
    state.fileList = []
  }
}

const handleRemove = (file, fileList) => {
  state.uploadDisabled = false
  imgUrlInner.value = null
  state.fileList = []
  emits('onImgChange')
}
const handlePictureCardPreview = file => {
  state.dialogImageUrl = file.url
  state.dialogVisible = true
}
const upload = file => {
  uploadFileResult(file.file, fileUrl => {
    imgUrlInner.value = fileUrl
    emits('onImgChange', fileUrl)
  })
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
    imgUrlInner.value = fileUrl
    emits('onImgChange', fileUrl)
  })
}

const sizeMessage = () => {
  ElMessage.success('图片大小不符合')
}

onMounted(() => {
  init()
})

watch(
  () => props.imgUrl,
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
  height: 80px;
  overflow: hidden;
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
