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
            <!-- <el-col :span="6">
              分辨率
            </el-col>
            <el-col :span="8">
              <el-input v-model="panel.color" type="number" placeholder="请输入内容" />
            </el-col> -->
            <!-- <el-col :span="6">
              <el-radio v-model="panel.backgroundType" label="color" @change="onChangeType">{{ $t('chart.color')+'111' }}</el-radio>
            </el-col>

            <el-col :span="18">
              <el-color-picker v-model="panel.color" :predefine="predefineColors" size="mini" style="cursor: pointer;z-index: 1004;" @change="onChangeType" />
            </el-col> -->
          </el-row>
          <el-row>
            <el-col :span="24">
              <!-- {{ $t('chart.canvasWith') }} -->
              <!-- 字体类型 -->
            </el-col>
            <el-col :span="24">
              <!-- <el-input-number v-model="width" :min="1" :max="10000" label="描述文字" @change="handleChange" /> -->
              <el-select v-model="fontValue" placeholder="请选择" @change="handleChange">
                <el-option
                  v-for="item in fontOptions"
                  :key="item"
                  :label="item"
                  :value="item"
                />
              </el-select>
            </el-col>
            <!-- <el-col :span="24">
              {{ $t('chart.canvasHeight') }}
            </el-col>
            <el-col :span="24">
              <el-input-number v-model="height" :min="1" :max="10000" label="描述文字" @change="handleChange" />
            </el-col> -->
          </el-row>

          <!-- <el-row>
            <el-col :span="2">
              H
            </el-col>
            <el-col :span="22">
              <el-input-number v-model="panel.height" :min="1" :max="10000" label="描述文字" @change="handleChange" />
            </el-col>
          </el-row> -->
          <!-- <el-row style="height: 60px;margin-top:10px;overflow: hidden">
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
          </el-row> -->
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ '字体样式' }} <i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>

<script>

import { mapState } from 'vuex'
import { deepCopy } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'SetDrawFont',
  data() {
    return {
      fileList: [],
      fontValue: '',
      width: '1080',
      height: '800',
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null,
      predefineColors: COLOR_PANEL,
      fontOptions: ['宋体', '楷体', '黑体', '仿宋', '新宋体']

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
    this.fontValue = this.canvasStyleData.fontFamily
    console.log(this.canvasStyleData.fontFamily)
    // this.height = this.canvasStyleData.height
    if (this.panel.imageUrl && typeof (this.panel.imageUrl) === 'string') {
      this.fileList.push({ url: this.panel.imageUrl })
    }
  },
  methods: {
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      canvasStyleData.fontFamily = this.fontValue
      // canvasStyleData.height = this.height
      console.log('canvasStyleData', canvasStyleData)
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleChange() {
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
      this.$store.state.styleChangeTimes++
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
