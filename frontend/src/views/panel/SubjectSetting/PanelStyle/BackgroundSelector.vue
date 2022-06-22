<template>
  <div style="width: 100%;">
    <el-form ref="overallSettingForm" :model="overallSettingForm" size="mini">
      <el-col style="padding-bottom: 10px;">
        <el-row>
          <el-col class="custom-item">
            <el-radio v-model="panel.backgroundType" label="color" style="float: right" @change="onChangeType">
              <span style="font-size: 12px">{{ $t('chart.color') }}</span>
            </el-radio>
          </el-col>
          <el-col :span="10">
            <el-color-picker
              v-model="panel.color"
              :predefine="predefineColors"
              size="mini"
              class="color-picker-custom"
              @change="onChangeType"
            />
          </el-col>
        </el-row>
        <el-row style="height: 60px;margin-top:10px;overflow: hidden">
          <el-col class="custom-item">
            <el-radio v-model="panel.backgroundType" label="image" style="float: right" @change="onChangeType">
              <span style="font-size: 12px">{{ $t('panel.photo') }}</span>
            </el-radio>
          </el-col>
          <el-col :span="15">
            <el-upload
              action=""
              accept=".jpeg,.jpg,.png,.gif"
              class="avatar-uploader"
              list-type="picture-card"
              :http-request="upload"
              :class="{disabled:uploadDisabled}"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :file-list="fileList"
            >
              <i class="el-icon-plus" />
            </el-upload>
            <el-dialog top="25vh" width="600px" :modal-append-to-body="false" :visible.sync="dialogVisible">
              <img width="100%" :src="dialogImageUrl" alt="">
            </el-dialog>
          </el-col>
        </el-row>
      </el-col>
    </el-form>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'
import { uploadFileResult } from '@/api/staticResource/staticResource'

export default {
  name: 'BackgroundSelector',
  data() {
    return {
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null,
      predefineColors: COLOR_PANEL,
      overallSettingForm: {}
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
    this.panel = this.canvasStyleData.panel
    if (this.panel.imageUrl && typeof (this.panel.imageUrl) === 'string') {
      this.fileList.push({ url: this.panel.imageUrl })
    }
  },
  methods: {
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.panel.imageUrl = null
      this.fileList = []
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    upload(file) {
      const _this = this
      uploadFileResult(file, (fileUrl) => {
        _this.$store.state.styleChangeTimes++
        _this.panel.imageUrl = fileUrl
        _this.commitStyle()
      })
    }
  }
}
</script>

<style scoped>
.avatar-uploader {
  margin-left: 10px;
}

.avatar-uploader ::v-deep .el-upload {
  width: 100px;
  height: 60px;
  line-height: 70px;
}

.avatar-uploader ::v-deep .el-upload-list li {
  width: 100px !important;
  height: 60px !important;
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
  margin-left: 10px;
  cursor: pointer;
  z-index: 1004;
}

.custom-item{
  width: 70px;
}
</style>
