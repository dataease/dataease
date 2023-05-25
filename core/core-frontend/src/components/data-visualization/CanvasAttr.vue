<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
import { uploadFileResult } from '@/api/staticResource'
import { ElIcon } from 'element-plus-secondary'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
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
    canvasStyleData.value.background = fileUrl
  })
}
</script>

<template>
  <div class="attr-container">
    <el-row>
      <el-collapse v-model="canvasAttrActiveNames">
        <el-collapse-item title="尺寸" name="size">
          <el-row class="item-show">
            <span style="margin-left: 10px; color: #757575" title="宽">W</span>
            <de-input-num v-model="canvasStyleData.width" :min="600" :max="4096"></de-input-num>
            <span style="margin-left: 10px; color: #757575" title="高">H</span>
            <de-input-num v-model="canvasStyleData.height" :min="600" :max="4096"></de-input-num>
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="背景" name="background">
          <el-row>
            <el-radio-group v-model="canvasStyleData.backgroundType">
              <el-radio label="backgroundColor">颜色</el-radio>
              <el-radio label="background">图片</el-radio>
            </el-radio-group>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'backgroundColor'">
            <el-color-picker v-model="canvasStyleData.backgroundColor" show-alpha></el-color-picker>
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
                :file-list="fileList"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <el-dialog
                top="25vh"
                width="600px"
                :append-to-body="true"
                :destroy-on-close="true"
                v-model:visible="dialogVisible"
              >
                <img width="100%" :src="dialogImageUrl" />
              </el-dialog>
            </el-col>
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="配色方案" name="color">
          <div>配色方案设置</div>
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.item-show {
  display: flex;
  text-align: center;
  padding-top: 18px;
}

.attr-container {
  background-color: rgba(37, 45, 54, 1);
  color: #fff;
  z-index: 20;
  height: 100%;
  width: 100%;
}

:deep(.el-collapse-item__header) {
  background-color: @side-area-background !important;
  color: #ffffff;
  padding-left: 5px;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
  height: 38px !important;
}
:deep(.el-collapse-item__content) {
  background-color: @side-content-background;
  color: #ffffff;
  padding-left: 5px;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.el-collapse) {
  width: 100%;
}

.disabled :deep(.el-upload--picture-card) {
  display: none;
}

.avatar-uploader :deep(.el-upload) {
  width: 120px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader :deep(.el-upload-list li) {
  width: 120px !important;
  height: 80px !important;
}
.img-area {
  height: 80px;
  margin-top: 0px;
  margin-bottom: 20px;
  overflow: hidden;
}
</style>
