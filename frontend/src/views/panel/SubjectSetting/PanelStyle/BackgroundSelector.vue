<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="250"
        trigger="click"
      >
        <el-col>
          <el-row>
            <el-col :span="6">
              <el-radio v-model="panel.backgroundType" label="color" @change="onChangeType">{{ $t('chart.color') }}</el-radio>
            </el-col>
            <el-col :span="18">
              <el-color-picker v-model="panel.color" size="mini" style="cursor: pointer;z-index: 1004;" />
            </el-col>
          </el-row>
          <el-row style="height: 60px;margin-top:10px;overflow: hidden">
            <el-col :span="6">
              <el-radio v-model="panel.backgroundType" label="image" @change="onChangeType">{{ $t('panel.photo') }}</el-radio>
            </el-col>
            <el-col :span="18">
              <el-upload
                action=""
                accept=".jpeg,.jpg,.png,.gif"
                class="avatar-uploader"
                list-type="picture-card"
                :class="{disabled:uploadDisabled}"
                :on-preview="handlePictureCardPreview"
                :on-remove="handleRemove"
                :http-request="upload"
                :file-list="fileList"
                :on-change="onChange"
              >
                <i class="el-icon-plus" />
              </el-upload>
              <el-dialog top="25vh" width="600px" :modal-append-to-body="false" :visible.sync="dialogVisible">
                <img width="100%" :src="dialogImageUrl" alt="">
              </el-dialog>
            </el-col>
          </el-row>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.background') }} <i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  name: 'BackgroundSelector',
  data() {
    return {
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null
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
      this.$store.commit('recordSnapshot')
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
    onChange(file, fileList) {
      var _this = this
      _this.uploadDisabled = true
      const reader = new FileReader()
      reader.onload = function() {
        _this.panel.imageUrl = reader.result
        this.commitStyle()
      }
      reader.readAsDataURL(file.raw)
    },
    upload(file) {
      // console.log('this is upload')
    }
  }
}
</script>

<style scoped>
  .avatar-uploader>>>.el-upload {
    width: 100px;
    height: 60px;
    line-height: 70px;
  }
  .avatar-uploader>>>.el-upload-list li{
    width: 100px !important;
    height: 60px !important;
  }
  .disabled>>>.el-upload--picture-card {
    display: none;
  }
  .shape-item{
    padding: 6px;
    border: none;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .form-item-slider>>>.el-form-item__label{
    font-size: 12px;
    line-height: 38px;
  }
  .form-item>>>.el-form-item__label{
    font-size: 12px;
  }
  .el-select-dropdown__item{
    padding: 0 20px;
  }
  span{
    font-size: 12px
  }
  .el-form-item{
    margin-bottom: 6px;
  }
</style>
