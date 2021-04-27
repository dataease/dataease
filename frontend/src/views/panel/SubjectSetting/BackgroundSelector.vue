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
              <el-radio v-model="backgroundForm.backgroundType" label="color">颜色</el-radio>
            </el-col>
            <el-col :span="18">
              <colorPicker v-model="backgroundForm.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeBackgroundStyle" />
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="6">
              <el-radio v-model="backgroundForm.backgroundType" label="image">图片</el-radio>
            </el-col>
            <el-col :span="18">
              <el-upload
                v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
                class="avatar-uploader"
                action=""
                accept=".jpeg,.jpg,.png,.gif"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :on-exceed="handleExceed"
                :on-error="handleError"
                :http-request="handleAvatarSuccess"
                :on-change="onChange"
              >
                <img v-if="imageUrl" :src="imageUrl" class="avatar">
                <i v-else class="el-icon-plus avatar-uploader-icon" />
              </el-upload>
            </el-col>
          </el-row>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">背景<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>
import { DEFAULT_BACKGROUND_COLOR } from '@/views/panel/panel'

export default {
  name: 'BackgroundSelector',
  props: {
  },
  data() {
    return {
      backgroundForm: JSON.parse(JSON.stringify(DEFAULT_BACKGROUND_COLOR)),
      systemParams: [],
      filesTmp: [],
      suffixes: new Set(['png', 'jpg', 'gif', 'jpeg']),
      files: [],
      imageUrl: ''
    }
  },
  watch: {

  },
  mounted() {
  },
  methods: {
    handleAvatarSuccess(file) {
      console.log('file.name')
      // var _this = this
      // var event = event || window.event
      // file = event.target.files[0]
      // var reader = new FileReader()
      // // 转base64
      // reader.onload = function(e) {
      //   _this.imageUrl = e.target.result // 将图片路径赋值给src
      //   _this.guideImage = e.target.result // 将图片路径赋值给src
      // }
      // reader.readAsDataURL(file)
    },
    onChange(file, fileList) {
      var _this = this
      const reader = new FileReader()
      reader.onload = function() {
        _this.imageUrl = reader.result
      }
      reader.readAsDataURL(file.raw)
    },
    changeBackgroundStyle() {
      this.$emit('onChangeBackgroundForm', this.backgroundForm)
    },
    handleExceed(files, fileList) {
      this.$warning(this.$t('test_track.case.import.upload_limit_count'))
    },
    handleError(e) {
      this.$warning('error' + JSON.stringify(e))
    },
    uploadValidate(file) {
      const suffix = file.name.substring(file.name.lastIndexOf('.') + 1)
      if (!this.suffixes.has(suffix)) {
        this.$warning(this.$t('test_track.case.import.upload_limit_format'))
        return false
      }

      if (file.size / 1024 / 1024 > 5) {
        this.$warning(this.$t('test_track.case.import.upload_limit_size'))
        return false
      }
      this.errList = []
      return true
    },
    upload(file) {
      this.imageUrl = file.url
      const reader = new FileReader()
      reader.onload = (res) => {
        this.imageUrl.src = res.target.result
      }
      reader.readAsDataURL(file)
    },
    removeValue(paramKey) {
      this.systemParams.forEach((systemParam) => {
        if (systemParam.paramKey === paramKey) {
          systemParam.fileName = null
          systemParam.file = null
        }
      })
    }
  }
}
</script>

<style scoped>
  .avatar-uploader>>>.el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .avatar-uploader>>>.el-upload:hover {
    border-color: #409EFF;
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 60px;
    line-height: 60px;
    text-align: center;
  }
  .avatar {
    width: 100px;
    height: 60px;
    display: block;
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
