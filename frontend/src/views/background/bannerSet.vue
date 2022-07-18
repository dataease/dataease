<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ $t('panel.rotate_pictures') }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">

      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ $t('panel.Number_of_rotation_shows') }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.options.slidesPerView" :min="1" :max="10" />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ $t('panel.pictureGap') }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.options.pictureGap" :min="0" :max="100" />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ $t('panel.Rotation_interval') }}</span>
        </el-col>
        <el-col :span="7">
          <el-input-number v-model="curComponent.options.rotationTime" :min="1" :max="100" />
        </el-col>
        <el-col :span="2">
          <span class="params-title">{{ $t('panel.second') }}</span>
        </el-col>
      </el-row>
      <!-- 轮播的图片 -->
      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ $t('panel.Upload_pictures') }}</span>
        </el-col>
      </el-row>
      <el-row style="padding:10px">
        <el-upload
          action=""
          accept=".jpeg,.jpg,.png,.gif,.svg"
          class="avatar-uploader"
          list-type="picture-card"
          :on-preview="handlePictureCardPreview"
          :on-remove="handleRemove"
          :http-request="upload"
          :file-list="fileList"
          :on-change="onChange"
        >
          <i class="el-icon-plus" />
        </el-upload>
        <el-dialog :visible.sync="dialogVisible">
          <img width="100%" :src="dialogImageUrl" alt="">
        </el-dialog>
      </el-row>
      <el-row v-if="curComponent.options.bannerImgList.length">
        <el-row style="height: 50px;overflow: hidden;">
          <el-col :span="4">
            <span class="params-title">添加文字描述</span>
          </el-col>
        </el-row>
        <el-collapse v-model="activeName">
          <el-collapse-item v-for="(item,index) in curComponent.options.bannerImgList" :key="index" :title="`图${index+1}`" :name="index">
            <el-row style="padding:5px;">
              <el-col :span="2">
                <span class="params-title">{{ $t('commons.title') }}</span>
              </el-col>
              <el-col :span="8">
                <el-input v-model="item.imgTitle" placeholder="请输入内容" />
              </el-col>
            </el-row>
            <el-row style="padding:5px;">
              <el-col :span="2">
                <span class="params-title">{{ $t('panel.content') }}</span>
              </el-col>
              <el-col :span="20">
                <el-input
                  v-model="item.imgContent"
                  type="textarea"
                  :rows="2"
                  placeholder="请输入内容"
                />
              </el-col>
            </el-row>
            <el-row style="padding:5px;">
              <el-col :span="3">
                <span class="params-title">{{ $t('chart.text_fontsize') }}</span>
              </el-col>
              <el-col :span="8">
                <el-input-number v-model="item.imgFontSize" :min="10" />
              </el-col>
            </el-row>
            <el-row style="padding:5px;">
              <el-col :span="3">
                <span class="params-title">{{ $t('chart.text_color') }}</span>
              </el-col>
              <el-col :span="8">
                <el-color-picker
                  v-model="item.imgFontColor"
                  class="color-picker-style"
                  :predefine="predefineColors"
                />
              </el-col>
            </el-row>
            <el-row style="padding:5px;">
              <el-col :span="3">
                <span class="params-title">{{ $t('chart.box_background') }}</span>
              </el-col>
              <el-col :span="8">
                <el-color-picker
                  v-model="item.imgBackgroundColor"
                  class="color-picker-style"
                  :predefine="predefineColors"
                />
              </el-col>
            </el-row>
            <el-row style="padding:5px;">
              <el-col :span="3">
                <span class="params-title">{{ $t('chart.box_background_opacity') }}</span>
              </el-col>
              <el-col :span="8">
                <el-input-number v-model="item.imgOpacity" :min="0" :max="1" :step="0.1" />
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>
      </el-row>
    </el-row>
    <el-row class="root-class">
      <el-col :span="24">
        <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { queryBackground } from '@/api/background/background'
// import BackgroundItem from '@/views/background/BackgroundItem'
import { mapState } from 'vuex'
// import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'Background',
  // components: { BackgroundItem },
  props: {
    // eslint-disable-next-line vue/require-default-prop
    element: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      activeName: null,
      input: '',
      BackgroundShowMap: {},
      imgUrlList: [],
      checked: false,
      backgroundOrigin: {},
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null,
      predefineColors: COLOR_PANEL,
      textData: [],
      options: [{
        value: 1000,
        label: '1秒'
      }, {
        value: 2000,
        label: '双皮奶'
      }, {
        value: 3000,
        label: '蚵仔煎'
      }, {
        value: 4000,
        label: '龙须面'
      }, {
        value: 5000,
        label: '北京烤鸭'
      }]
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
  },
  created() {
    this.init()
  },
  mounted() {

  },

  methods: {
    init() {
      console.log('componentData--------', this.componentData, this.curComponent)
      if (this.curComponent.options.bannerImgList) {
        this.curComponent.options.bannerImgList.forEach(res => {
          this.fileList.push(res)
        })
      }
      // if (this.curComponent && this.curComponent.commonBackground && this.curComponent.commonBackground.outerImage && typeof (this.curComponent.commonBackground.outerImage) === 'string') {
      //   this.curComponent.options.bannerImgList.forEach(res => {
      //     this.fileList.push({ url: res })
      //   })
      // }
      console.log('init,filelist.....', this.fileList)
      // this.backgroundOrigin = deepCopy(this.curComponent.commonBackground)
      this.backgroundOrigin = deepCopy(this.curComponent.options)
      console.log(this.backgroundOrigin)
      this.queryBackground()
    },
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    cancel() {
      this.curComponent.options = this.backgroundOrigin

      console.log('this.curComponent.commonBackground.boxWidth=====', this.curComponent.commonBackground)
      this.$emit('backgroundSetClose')
    },
    save() {
      console.log('this.fileList', this.fileList)

      console.log('this.imgUrlList', this.imgUrlList)
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      console.log('const canvasStyleData', canvasStyleData)
      canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      console.log('remove', file, fileList)
      var _this = this
      _this.fileList = fileList
      _this.curComponent.options.bannerImgList = []
      _this.fileList.forEach(item => {
        if (item.raw) {
          const reader = new FileReader()
          reader.onload = function() {
            console.log('reader.result7777777', reader.result)
            _this.curComponent.options.bannerImgList.push({
              url: reader.result,
              imgTitle: '',
              imgContent: '', // 有点问题这里
              imgFontSize: 10,
              imgFontColor: '#000000',
              imgBackgroundColor: '#ffffff',
              imgOpacity: 0.5
            })
          }
          reader.readAsDataURL(item.raw)
        } else {
          _this.curComponent.options.bannerImgList.push(item)
        }
      })
      // this.uploadDisabled = false
      // this.panel.imageUrl = null
      // this.fileList = []
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      console.log('file---', file)
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    onChange(file, fileList) {
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        this.fileList = []
        return
      }
      console.log('file, fileList', file, fileList)
      var _this = this
      _this.uploadDisabled = true
      // const reader = new FileReader()
      // reader.onload = function() {
      // console.log('reader.result', reader.result)
      // _this.imgUrlList.push(reader.result)
      // _this.curComponent.commonBackground.outerImage = reader.result
      // }
      // reader.readAsDataURL(file.raw)
      _this.fileList = fileList
      _this.curComponent.options.bannerImgList = []
      _this.fileList.forEach(item => {
        console.log('itemssss', item)
        if (item.raw) {
          const reader = new FileReader()
          reader.onload = function() {
            console.log('reader.result6666666', reader.result)
            // _this.imgUrlList.push(reader.result)
            _this.curComponent.options.bannerImgList.push({
              url: reader.result,
              imgTitle: '',
              imgContent: '',
              imgFontSize: 10,
              imgFontColor: '#000000',
              imgBackgroundColor: '#ffffff',
              imgOpacity: 0.5
            })
          }
          reader.readAsDataURL(item.raw)
        } else {
          _this.curComponent.options.bannerImgList.push(item)
        }
      })
      console.log('222222', file, fileList)
    },
    upload(file) {
      console.log('this is upload', file)
    }

  }
}
</script>

<style scoped>
  .el-card-template {
    min-width: 260px;
    min-width: 460px;
    width: 100%;
    height: 100%;
  }

  .main-row{
    height: 140px;
    overflow-y: auto;
  }

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
  .avatar-uploader>>>.el-upload {
    width: 120px;
    height: 80px;
    line-height: 90px;
  }
  .avatar-uploader>>>.el-upload-list li{
    width: 120px !important;
    height: 80px !important;
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
  .main-content{
    border:1px solid #E6E6E6;
  }

  .params-title{
    font-weight: bold;
    line-height: 40px;
    margin-left: 10px;
    font-size: 14px;
  }

  .params-title-small{
    font-weight: bold;
    line-height: 40px;
    margin-left: 10px;
    font-size: 12px;
  }
</style>
