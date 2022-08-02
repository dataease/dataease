<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ '天气组件设置' }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">

      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ '字体颜色' }}</span>
        </el-col>
        <el-col :span="8">
          <el-color-picker v-model="curComponent.weatherStyle.color" />
        </el-col>

      </el-row>
      <el-row>
        <el-col :span="4">
          <span class="params-title">{{ '字体大小' }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.weatherStyle.fontSize" :min="1" />
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="4">
          <span class="params-title">{{ '图标大小' }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.weatherStyle.iocnSize" :min="1" />
        </el-col>
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
      value1: [],
      input: '',
      // options: [],
      navInfoLis: [
        {
          name: '',
          relation: []
          // options: []
        }
      ],
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
      options: []
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
    // optionsData() {
    //   let data = []
    //   this.navInfoLis.forEach(res => {
    //     data = data.concat(res.relation)
    //   })
    //   const opSetInfo = this.options
    //   opSetInfo.forEach(item => {
    //     data.forEach(e => {
    //       if (e === item.id) {
    //         item.disabled = true
    //       }
    //     })
    //   })
    //   console.log(data)
    //   return opSetInfo
    // }
  },
  created() {
    // this.init()

  },
  mounted() {
    console.log('this.curComponent', this.curComponent)
    console.log('this.componentData', this.componentData)
    // let dataArr = []
    // this.componentData.forEach(res => {
    //   if (res.type === 'de-nav') {
    //     console.log('res', res)
    //     this.options.push.apply(this.options, res.options.navTabList)
    //   }
    // })
    console.log('this.options', this.options)
  },

  methods: {
    addNavInfo() {
      console.log('this.navInfoLis', this.navInfoLis)
      this.navInfoLis.push({
        name: '',
        relation: []
      })
    },
    changeAssembly(e) {
      // 组件不能重复选择
      console.log(e)
      // this.options.forEach(res => {
      //   res.disabled = false
      //   console.log('res', res)
      // })
      // e.forEach(item => {
      //   this.options.forEach(res => {
      //     if (item === res.id) {
      //       res.disabled = true
      //     }
      //   })
      // })
    },
    blurSelect(e) {
      console.log('失焦', e)
    },
    deleteNav(item, index) {
      this.navInfoLis.splice(index, 1)
    },
    init() {
      console.log('componentData--------', this.componentData, this.curComponent)
      if (this.curComponent.options.bannerImgList) {
        this.curComponent.options.bannerImgList.forEach(res => {
          this.fileList.push({ url: res })
        })
      }
      this.backgroundOrigin = deepCopy(this.curComponent.commonBackground)
      this.queryBackground()
    },
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    cancel() {
      // this.curComponent.commonBackground.enable = this.backgroundOrigin.enable
      // this.curComponent.commonBackground.backgroundType = this.backgroundOrigin.backgroundType
      // this.curComponent.commonBackground.color = this.backgroundOrigin.color
      // this.curComponent.commonBackground.innerImage = this.backgroundOrigin.innerImage
      // this.curComponent.commonBackground.outerImage = this.backgroundOrigin.outerImage
      // this.curComponent.commonBackground.alpha = this.backgroundOrigin.alpha
      // this.curComponent.commonBackground.borderRadius = this.backgroundOrigin.borderRadius
      // this.curComponent.commonBackground.innerPadding = this.backgroundOrigin.innerPadding
      // this.curComponent.commonBackground.boxWidth = Math.floor(this.backgroundOrigin.boxWidth)
      // this.curComponent.commonBackground.boxHeight = Math.floor(this.backgroundOrigin.boxHeight)
      console.log('this.curComponent.commonBackground.boxWidth=====', this.curComponent.commonBackground)
      this.$emit('backgroundSetClose')
    },
    save() {
      // this.curComponent.options.navTabList = this.navInfoLis
      // this.navInfoLis.forEach(ele => {
      //   ele.relation.forEach(item => {
      //     this.componentData.forEach(res => {
      //       if (res.id === item) {
      //         res.showName = ele.name
      //       }
      //     })
      //   })
      // })
      // console.log('this.fileList', this.fileList)

      // console.log('this.imgUrlList', this.imgUrlList)
      // this.$store.commit('recordSnapshot')
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
      console.log(file, fileList)
      var _this = this
      _this.fileList = fileList
      _this.curComponent.options.bannerImgList = []
      _this.fileList.forEach(item => {
        console.log('itemssss', item)
        if (item.raw) {
          const reader = new FileReader()
          reader.onload = function() {
            console.log('reader.result7777777', reader.result)
            _this.curComponent.options.bannerImgList.push(reader.result)
          }
          reader.readAsDataURL(item.raw)
        } else {
          _this.curComponent.options.bannerImgList.push(item.url)
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
            _this.curComponent.options.bannerImgList.push(reader.result)
          }
          reader.readAsDataURL(item.raw)
        } else {
          _this.curComponent.options.bannerImgList.push(item.url)
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
