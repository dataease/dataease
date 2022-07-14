<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ '图标库设置' }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">

      <!-- <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="6">
          <span class="params-title">{{ '当前组件关联导航' }}</span>
        </el-col>
        <el-col :span="10">
          <el-select v-model="curComponent.showName" clearable placeholder="请选择">
            <el-option
              v-for="(item,index) in options"
              :key="index"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-col>
      </el-row> -->
      <el-row>
        <el-col :span="6">
          <span class="params-title">{{ '系统默认图标' }}</span>
        </el-col>
      </el-row>
      <!-- <el-row style="padding:10px">
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
      </el-row> -->
      <el-row>
        <!-- 具体图标 -->
        <el-col v-for="(item,index) in systemIcon" :key="index" :span="2">
          <div class="icon_class" :class="selectObj.icon===item.icon?'changeColor':''">
            <i :class="item.icon" style="fontSize:28px;cursor: pointer;" @click="changeIcon(item)" />
          </div>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="6">
          <span class="params-title">{{ '自定义图标' }}</span>
        </el-col>
      </el-row>
      <el-row>
        <!-- 具体图标 -->
        <el-col v-for="(item,index) in customIcon" :key="index" :span="2">
          <div class="icon_class" :class="selectObj.icon===item.icon?'changeColor':''">
            <svg-icon :icon-class="item.icon" style="fontSize:28px;cursor: pointer;" @click="changeIcon(item)" />
          </div>
        </el-col>
      </el-row>
      <!-- <el-row>
        <el-col>
          <svg-icon icon-class="from-people" style="fontSize:28px;cursor: pointer;" />
          <svg-icon icon-class="humidity-worter" style="fontSize:28px;cursor: pointer;color:#f99" />
        </el-col>
      </el-row> -->
      <!-- <div class="sys_class">232</div> -->
      <!-- <el-row>
        <el-col :span="6">
          <span class="params-title">{{ '设置' }}</span>
        </el-col>
      </el-row> -->
      <el-row>
        <el-col :span="4">
          <span class="params-title">{{ '颜色' }}</span>
        </el-col>
        <el-col :span="6">
          <el-color-picker v-model="fontColor" />
        </el-col>
        <el-col :span="4">
          <span class="params-title">{{ '图标大小' }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="iconSize" :min="12" :max="400" label="图标大小" />
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
import { CUSTOM_DATA } from './custom.js'

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
      iconSize: 12,
      fontColor: null,
      selectObj: {
        type: '',
        icon: ''
      },
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
      options: [],
      systemIcon: [
        {
          type: 'system',
          icon: 'el-icon-share'
        },
        {
          type: 'system',
          icon: 'el-icon-delete'
        },
        {
          type: 'system',
          icon: 'el-icon-user'
        },
        {
          type: 'system',
          icon: 'el-icon-setting'
        },
        {
          type: 'system',
          icon: 'el-icon-star-off'
        },
        {
          type: 'system',
          icon: 'el-icon-zoom-in'
        },
        {
          type: 'system',
          icon: 'el-icon-zoom-out'
        },
        {
          type: 'system',
          icon: 'el-icon-remove-outline'
        },
        {
          type: 'system',
          icon: 'el-icon-circle-plus-outline'
        },
        {
          type: 'system',
          icon: 'el-icon-circle-check'
        },
        {
          type: 'system',
          icon: 'el-icon-circle-close'
        },
        {
          type: 'system',
          icon: 'el-icon-minus'
        },
        {
          type: 'system',
          icon: 'el-icon-plus'
        },
        {
          type: 'system',
          icon: 'el-icon-check'
        },
        {
          type: 'system',
          icon: 'el-icon-close'
        },
        {
          type: 'system',
          icon: 'el-icon-picture-outline'
        },
        {
          type: 'system',
          icon: 'el-icon-picture-outline-round'
        },
        {
          type: 'system',
          icon: 'el-icon-upload'
        },
        {
          type: 'system',
          icon: 'el-icon-upload2'
        },
        {
          type: 'system',
          icon: 'el-icon-download'
        },
        {
          type: 'system',
          icon: 'el-icon-camera'
        },
        {
          type: 'system',
          icon: 'el-icon-video-camera'
        },
        {
          type: 'system',
          icon: 'el-icon-bell'
        },
        {
          type: 'system',
          icon: 'el-icon-back'
        },
        {
          type: 'system',
          icon: 'el-icon-right'
        },
        {
          type: 'system',
          icon: 'el-icon-bottom'
        },
        {
          type: 'system',
          icon: 'el-icon-top'
        },
        {
          type: 'system',
          icon: 'el-icon-arrow-left'
        },
        {
          type: 'system',
          icon: 'el-icon-arrow-right'
        },
        {
          type: 'system',
          icon: 'el-icon-arrow-down'
        },
        {
          type: 'system',
          icon: 'el-icon-arrow-up'
        },
        {
          type: 'system',
          icon: 'el-icon-video-pause'
        },
        {
          type: 'system',
          icon: 'el-icon-video-play'
        },
        {
          type: 'system',
          icon: 'el-icon-refresh'
        },
        {
          type: 'system',
          icon: 'el-icon-date'
        },
        {
          type: 'system',
          icon: 'el-icon-edit-outline'
        },
        {
          type: 'system',
          icon: 'el-icon-folder'
        },
        {
          type: 'system',
          icon: 'el-icon-folder-opened'
        },
        {
          type: 'system',
          icon: 'el-icon-tickets'
        },
        {
          type: 'system',
          icon: 'el-icon-document-copy'
        },
        {
          type: 'system',
          icon: 'el-icon-document'
        },
        {
          type: 'system',
          icon: 'el-icon-printer'
        },
        {
          type: 'system',
          icon: 'el-icon-paperclip'
        },
        {
          type: 'system',
          icon: 'el-icon-monitor'
        },
        {
          type: 'system',
          icon: 'el-icon-collection-tag'
        },
        {
          type: 'system',
          icon: 'el-icon-discount'
        },
        {
          type: 'system',
          icon: 'el-icon-link'
        },
        {
          type: 'system',
          icon: 'el-icon-connection'
        },
        {
          type: 'system',
          icon: 'el-icon-open'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-round'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-line-round'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-dot-round'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-square'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-dot-square'
        },
        {
          type: 'system',
          icon: 'el-icon-chat-line-square'
        },
        {
          type: 'system',
          icon: 'el-icon-message'
        },
        {
          type: 'system',
          icon: 'el-icon-postcard'
        },
        {
          type: 'system',
          icon: 'el-icon-position'
        },
        {
          type: 'system',
          icon: 'el-icon-microphone'
        },
        {
          type: 'system',
          icon: 'el-icon-switch-button'
        },
        {
          type: 'system',
          icon: 'el-icon-location-outline'
        },
        {
          type: 'system',
          icon: 'el-icon-location-information'
        },
        {
          type: 'system',
          icon: 'el-icon-map-location'
        },
        {
          type: 'system',
          icon: 'el-icon-light-rain'
        },
        {
          type: 'system',
          icon: 'el-icon-lightning'
        },
        {
          type: 'system',
          icon: 'el-icon-heavy-rain'
        },
        {
          type: 'system',
          icon: 'el-icon-sunny'
        },
        {
          type: 'system',
          icon: 'el-icon-cloudy'
        },
        {
          type: 'system',
          icon: 'el-icon-partly-cloudy'
        },
        {
          type: 'system',
          icon: 'el-icon-cloudy-and-sunny'
        },
        {
          type: 'system',
          icon: 'el-icon-sunrise'
        },
        {
          type: 'system',
          icon: 'el-icon-sunrise-1'
        },
        {
          type: 'system',
          icon: 'el-icon-sunset'
        },
        {
          type: 'system',
          icon: 'el-icon-moon'
        },
        {
          type: 'system',
          icon: 'el-icon-moon-night'
        }

      ],
      customIcon: CUSTOM_DATA
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
    this.selectObj.icon = this.curComponent.iconData.icon
    this.selectObj.type = this.curComponent.iconData.type
    this.iconSize = this.curComponent.iconData.fontSize
    this.fontColor = this.curComponent.iconData.color
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
    queryBackground() {
      queryBackground().then(response => {
        this.BackgroundShowMap = response.data
      })
    },
    cancel() {
      console.log('this.curComponent.commonBackground.boxWidth=====', this.curComponent.commonBackground)
      this.$emit('backgroundSetClose')
    },
    changeIcon(item) {
      this.selectObj = item
    },
    save() {
      this.curComponent.iconData.color = this.fontColor
      this.curComponent.iconData.fontSize = this.iconSize
      this.curComponent.iconData.icon = this.selectObj.icon
      this.curComponent.iconData.type = this.selectObj.type
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
      // _this.curComponent.options.bannerImgList = []
      _this.fileList.forEach(item => {
        console.log('itemssss', item)
        if (item.raw) {
          const reader = new FileReader()
          reader.onload = function() {
            console.log('reader.result6666666', reader.result)
            // _this.imgUrlList.push(reader.result)
            // _this.curComponent.options.bannerImgList.push(reader.result)
          }
          reader.readAsDataURL(item.raw)
        } else {
          // _this.curComponent.options.bannerImgList.push(item.url)
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
  .icon_class{
    text-align:center;
    /* font-size:18px; */
    height:50px;
    line-height:50px;
  }
  .changeColor{
    color:#409EFF;
  }

  .params-title-small{
    font-weight: bold;
    line-height: 40px;
    margin-left: 10px;
    font-size: 12px;
  }
</style>
