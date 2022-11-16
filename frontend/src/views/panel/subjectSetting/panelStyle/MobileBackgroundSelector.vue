<template>
  <div style="width: 100%;margin-top: 12px">
    <el-form
      ref="overallMobileSettingForm"
      size="mini"
    >
      <el-col>
        <el-checkbox v-model="mobileSetting.customSetting">{{ $t('panel.mobile_style_setting_tips') }}</el-checkbox>
      </el-col>
      <el-col>
        <el-radio-group
          v-model="mobileSetting.backgroundType"
          size="mini"
          :disabled="!mobileSetting.customSetting"
          @change="onChangeType()"
        >
          <el-radio label="color">{{ $t('chart.color') }}</el-radio>
          <el-radio label="image">{{ $t('panel.photo') }}</el-radio>
        </el-radio-group>
      </el-col>
      <el-col
        v-show="mobileSetting.backgroundType==='color'"
        :span="10"
      >
        <el-color-picker
          v-model="mobileSetting.color"
          :predefine="predefineColors"
          size="mini"
          class="color-picker-custom"
          :disabled="!mobileSetting.customSetting"
          @change="onChangeType"
        />
      </el-col>
      <el-col
        v-show="mobileSetting.backgroundType==='image'"
        :span="10"
      >
        <el-upload
          action=""
          accept=".jpeg,.jpg,.png,.gif"
          class="avatar-uploader"
          list-type="picture-card"
          :http-request="upload"
          :class="{disabled:uploadDisabled}"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :disabled="!mobileSetting.customSetting"
          :file-list="fileList"
        >
          <i class="el-icon-plus" />
        </el-upload>
        <el-dialog
          top="25vh"
          width="600px"
          :modal-append-to-body="false"
          :visible.sync="dialogVisible"
        >
          <img
            width="100%"
            :src="dialogImageUrl"
            alt=""
          >
        </el-dialog>
      </el-col>
      <el-col v-show="mobileSetting.backgroundType==='image'">
        <span
          v-show="!mobileSetting.imageUrl"
          class="image-hint"
        >{{ $t('panel.panel_background_image_tips') }}</span>
        <span
          v-show="mobileSetting.imageUrl && mobileSetting.customSetting"
          class="re-update-span"
          @click="goFile"
        >{{ $t('panel.reUpload') }}</span>
      </el-col>
      <input
        id="input"
        ref="files"
        type="file"
        accept=".jpeg,.jpg,.png,.gif"
        hidden
        @click="e => {e.target.value = '';}"
        @change="reUpload"
      >
    </el-form>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { deepCopy, imgUrlTrans } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { uploadFileResult } from '@/api/staticResource/staticResource'

export default {
  name: 'MobileBackgroundSelector',
  data() {
    return {
      maxImageSize: 15000000,
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      mobileSetting: null,
      predefineColors: COLOR_PANEL
    }
  },
  computed: mapState([
    'canvasStyleData'
  ]),
  watch: {
    // deep监听panel 如果改变 提交到 store
  },
  created() {
    // 初始化赋值
    this.mobileSetting = this.canvasStyleData.panel.mobileSetting
    if (this.mobileSetting.imageUrl && typeof (this.mobileSetting.imageUrl) === 'string') {
      this.fileList.push({ url: imgUrlTrans(this.mobileSetting.imageUrl) })
    }
  },
  methods: {
    goFile() {
      this.$refs.files.click()
    },
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      canvasStyleData.panel.mobileSetting = this.mobileSetting
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.mobileSetting.imageUrl = null
      this.fileList = []
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    upload(file) {
      const _this = this
      if (file.size > this.maxImageSize) {
        this.sizeMessage()
      }
      uploadFileResult(file.file, (fileUrl) => {
        _this.$store.commit('canvasChange')
        _this.mobileSetting.imageUrl = fileUrl
        _this.fileList = [{ url: imgUrlTrans(this.mobileSetting.imageUrl) }]
        _this.commitStyle()
      })
    },
    reUpload(e) {
      const file = e.target.files[0]
      const _this = this
      if (file.size > this.maxImageSize) {
        this.sizeMessage()
      }
      uploadFileResult(file, (fileUrl) => {
        _this.$store.commit('canvasChange')
        _this.mobileSetting.imageUrl = fileUrl
        _this.fileList = [{ url: imgUrlTrans(this.mobileSetting.imageUrl) }]
        _this.commitStyle()
      })
    },
    sizeMessage() {
      this.$notify({
        message: this.$t('panel.image_size_tips'),
        position: 'top-left'
      })
    }
  }
}
</script>

<style scoped>
.avatar-uploader {
  position: relative;
  margin-left: 0px;
  margin-top: 8px;
  height: 80px;
  width: 80px;
  line-height: 80px;
  overflow: hidden;
}

.avatar-uploader ::v-deep .el-upload {
  width: 80px;
  height: 80px;
  line-height: 80px;
}

.avatar-uploader ::v-deep .el-upload-list li {
  width: 80px !important;
  height: 80px !important;
}

.disabled ::v-deep .el-upload--picture-card {
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

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.color-picker-custom {
  margin-left: 0px;
  cursor: pointer;
  margin-top: 8px;
  z-index: 1004;
}

.custom-item {
  width: 70px;
}

.re-update-span {
  cursor: pointer;
  color: #3370FF;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.image-hint {
  color: #8F959E;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.custom-item-text {
  font-weight: 400 !important;
  font-size: 14px !important;
  color: var(--TextPrimary, #1F2329) !important;
  line-height: 22px;
}
</style>
