<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ '导航设置' }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">

      <el-row style="height: 50px;overflow: hidden;margin-top:20px;">
        <el-col :span="4">
          <span class="params-title">{{ '字体大小' }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.options.fontSize" :min="1" />
        </el-col>
        <el-col :span="4">
          <span class="params-title">{{ '字体颜色' }}</span>
        </el-col>
        <el-col :span="8">
          <el-color-picker v-model="curComponent.options.color" />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:0px;">
        <el-col :span="4">
          <span class="params-title">{{ '间距' }}</span>
        </el-col>
        <el-col :span="8">
          <el-input-number v-model="curComponent.options.spacing" :min="0" />
        </el-col>
        <el-col :span="4">
          <span class="params-title">{{ '水平对齐方式' }}</span>
        </el-col>
        <el-col :span="8" style="height:40px;line-height:40px;">
          <el-radio-group v-model="curComponent.options.horizontal">
            <el-radio label="left">左</el-radio>
            <el-radio label="center">中</el-radio>
            <el-radio label="right">右</el-radio>
          </el-radio-group>
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:5px;">
        <el-col :span="4">
          <span class="params-title">{{ '展示数量' }}</span>
        </el-col>
        <el-col :span="7">
          <el-input-number v-model="curComponent.options.scrollPage" :min="1" />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:5px;">
        <el-col :span="4">
          <span class="params-title">{{ '滚动间隔时间' }}</span>
        </el-col>
        <el-col :span="7">
          <el-input-number v-model="curComponent.options.autoTime" :min="1" :max="100" />
        </el-col>
        <el-col :span="2">
          <span class="params-title">{{ $t('panel.second') }}</span>
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden;margin-top:5px;">
        <el-col :span="4">
          <span class="params-title">{{ '是否启用滚动' }}</span>
        </el-col>
        <el-col :span="7">
          <el-radio-group v-model="curComponent.options.autoplay">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="4">
          <span class="params-title">{{ '高亮字体颜色' }}</span>
        </el-col>
        <el-col :span="8">
          <el-color-picker v-model="curComponent.options.highlight" />
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="4">
          <span class="params-title">{{ '高亮背景颜色' }}</span>
        </el-col>
        <el-col :span="8">
          <el-color-picker v-model="curComponent.options.highlightBg" />
        </el-col>
      </el-row>
      <el-row style="height: 80px;margin-top:10px;margin-bottom:20px;overflow: hidden">
        <el-col :span="4">
          <span class="params-title">{{ '高亮背景图片' }}</span>
        </el-col>
        <el-col :span="3">
          <el-button size="mini" type="primary" @click="openNewImg()">选择</el-button>
        </el-col>
        <el-col v-show="changImg!==''" :span="7">
          <div style="height:80px;width:120px;overflow-y:scroll;">
            <img :src="changImg" class="img_class">
          </div>
        </el-col>
      </el-row>
      <el-row style="height: 80px;margin-top:10px;margin-bottom:20px;overflow: hidden">
        <el-col :span="4">
          <span class="params-title">{{ '默认背景图片' }}</span>
        </el-col>
        <el-col :span="3">
          <el-button size="mini" type="primary" @click="openNewBgImg()">选择</el-button>
        </el-col>
        <el-col v-show="navBgImg!==''" :span="7">
          <div style="height:80px;width:120px;overflow-y:scroll;">
            <img :src="navBgImg" class="img_class">
          </div>
        </el-col>
      </el-row>
      <el-dialog
        width="750px"
        title="图片库"
        :visible.sync="innerVisible"
        append-to-body
      >
        <el-tabs v-model="activeNameTabs" @tab-click="handleClick">
          <el-tab-pane label="图库选择" name="first">
            <el-row class="bif_box">
              <el-col>
                <el-collapse v-model="activeNames">
                  <el-collapse-item v-for="(ited,index) in allImgData" :key="index" :title="ited.name" :name="ited.name">
                    <template slot="title">
                      <span style="width:600px">{{ ited.name }}</span>
                    </template>
                    <el-row :gutter="10" style="padding:10px;">
                      <el-col v-for="(item,indexs) in ited.str" :key="indexs" style="height:108px;margin-bottom:20px; position:relative;" :span="6">
                        <div class="img_Box" @click="clickImg(item)">
                          <img :src="item.url" class="img_class">
                        </div>
                      </el-col>
                    </el-row>
                  </el-collapse-item>
                </el-collapse>
              </el-col>
            </el-row>
            <el-row style="margin-top:20px;">
              <el-col :span="3">
                <span class="params-title">{{ '选中图片：' }}</span>
              </el-col>
              <el-col :span="6" style="height:108px;margin-bottom:20px;overflow-y:scroll;">
                <img :src="currentlySelected" class="img_class">
              </el-col>
            </el-row>
          </el-tab-pane>
          <el-tab-pane label="上传图片" name="second">
            <el-row style="height: 80px;margin-top:10px;margin-bottom:20px;overflow: hidden">
              <el-col :span="4">
                <span class="params-title">{{ '选择图片' }}</span>
              </el-col>
              <el-col v-show="updataType" style="width: 130px!important;">
                <el-upload
                  action=""
                  accept=".jpeg,.jpg,.png,.gif,.svg"
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
              </el-col>
              <el-col v-show="updataType" :span="7">
                <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>

        <el-row class="root-class">
          <el-col :span="24">
            <el-button size="mini" @click="cancelPicture()">{{ $t('commons.cancel') }}</el-button>
            <el-button type="primary" size="mini" @click="savePicture()">{{ $t('commons.confirm') }}</el-button>
          </el-col>
        </el-row>

      </el-dialog>
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
import { getAllImgList, queryBackground } from '@/api/background/background'
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
      activeNameTabs: 'first',
      value1: [],
      currentlySelected: '',
      allImgData: [],
      activeNames: [],
      updataUrl: '',
      input: '',
      // options: [],
      updataType: true,
      navInfoLis: [
        {
          name: '',
          relation: []
          // options: []
        }
      ],
      innerVisible: false,
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
      navBgImg: '',
      textData: [],
      chengKey: '',
      changImg: '',
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
      'componentData',
      'canvasStyleData'
    ]),
    optionsData() {
      let data = []
      this.navInfoLis.forEach(res => {
        data = data.concat(res.relation)
      })
      const opSetInfo = this.options
      opSetInfo.forEach(item => {
        data.forEach(e => {
          if (e === item.id) {
            item.disabled = true
          }
        })
      })
      console.log(data)
      return opSetInfo
    }
  },
  created() {
    // this.init()
    this.getAllImg()
  },
  mounted() {
    // this.componentData.forEach(res => {
    //   if (res.showName) {
    //     res.showName = ''
    //   }
    // })
    console.log('componentData获取数据--', this.componentData, this.curComponent)
    // const seltOps = []
    // this.navInfoLis = this.element.options.navTabList
    // this.navInfoLis.forEach(res => {
    //   res.relation = []
    // })
    // this.navInfoLis.forEach(ele => {
    //   console.log('ele::::', ele)
    //   this.componentData.forEach(res => {
    //     if (res.showName === ele.name) {
    //       // res.showName = ''
    //       ele.relation.push(res.id)
    //       delete res.showName
    //     }
    //   })
    // })
    // console.log(' this.componentData', this.componentData)
    // const newArrr = deepCopy(this.componentData)
    // newArrr.forEach(ele => {
    //   ele.disabled = true
    //   if (!ele.hasOwnProperty('showName')) {
    //     console.log('满足条件的ele', ele)
    //     ele.disabled = false
    //   }
    //   console.log('ele----', ele)
    // })
    // this.navInfoLis.forEach(res => {
    //   res.relation.forEach(e => {
    //     newArrr.forEach(item => {
    //       if (item.id === e) {
    //         item.disable = true
    //       }
    //     })
    //   })
    // })
    // this.options = deepCopy(newArrr)
    if (this.element.options.heightBgImg && this.element.options.heightBgImg !== '') {
      // this.updataUrl = this.curComponent.options.heightBgImg
      this.changImg = this.curComponent.options.heightBgImg
      // this.fileList.push({ url: this.element.options.heightBgImg })
    }
    if (this.curComponent.options.defaultBg) {
      this.navBgImg = this.curComponent.options.defaultBg
    }
    // this.curComponent.options.defaultBg = this.navBgImg
  },

  methods: {
    handleClick() {

    },
    clickImg(item) {
      this.currentlySelected = item.url
      console.log('图片数据', item)
    },
    cancelPicture() {
      this.innerVisible = false
      // this.changImg = ''
    },
    savePicture() {
      console.log('this.activeNameTabs', this.activeNameTabs)
      if (this.activeNameTabs === 'first') {
        if (this.chengKey === 'highlight') {
          this.changImg = this.currentlySelected
        } else {
          this.navBgImg = this.currentlySelected
        }
      } else {
        if (this.chengKey === 'highlight') {
          this.changImg = this.updataUrl
        } else {
          this.navBgImg = this.updataUrl
        }
      }
      this.innerVisible = false
    },
    getAllImg() {
      getAllImgList().then(res => {
        console.log('获取所有图片数据', res)
        this.allImgData = res.data
      })
    },
    openNewImg() {
      this.currentlySelected = ''
      this.chengKey = 'highlight'
      this.innerVisible = true
    },
    openNewBgImg() {
      this.currentlySelected = ''
      this.chengKey = 'default'
      this.innerVisible = true
    },
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
          // this.updataUrl = res
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
      this.curComponent.options.navTabList = this.navInfoLis
      this.navInfoLis.forEach(ele => {
        ele.relation.forEach(item => {
          this.componentData.forEach(res => {
            if (res.id === item) {
              res.showName = ele.name
            }
          })
        })
      })
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
      console.log('this.fileList', this.fileList)
      // 高亮背景
      this.curComponent.options.heightBgImg = this.changImg
      // 默认背景
      this.curComponent.options.defaultBg = this.navBgImg
      this.commitStyle()

      this.$store.commit('recordSnapshot')

      this.$emit('backgroundSetClose')
    },
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      console.log('const canvasStyleData', canvasStyleData)
      // canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
      this.commitStyle()
    },
    handleRemove(file, fileList) {
      console.log(file, fileList)
      this.uploadDisabled = false
      // this.panel.imageUrl = null
      this.curComponent.options.heightBgImg = ''
      this.fileList = []
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
      const reader = new FileReader()
      reader.onload = function() {
        _this.updataUrl = reader.result
        // _this.curComponent.options.heightBgImg = reader.result
        // _this.commitStyle()
        console.log('reader.result6666666', reader.result)
      }
      reader.readAsDataURL(file.raw)
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
  .img_class{
    width:100%;
    /* height:100%; */
  }
  .img_Box{
    height:108px;
    cursor: pointer;
    overflow-y: scroll;
  }
  .bif_box{
    border-left:1px solid #E6E6E6;
    border-right:1px solid #E6E6E6;
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
