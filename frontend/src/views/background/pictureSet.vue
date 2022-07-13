<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ '图库' }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">
      <el-row style="height: 80px;margin-top:10px;margin-bottom:20px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ '上传图片' }}</span>
        </el-col>
        <el-col style="width: 130px!important;">
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
          <el-dialog :visible.sync="dialogVisible">
            <img width="100%" :src="dialogImageUrl" alt="">
          </el-dialog>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="upadtaEven()">上传</el-button>
          <!-- <span class="params-title" >{{ '上传' }}</span> -->
        </el-col>

        <el-col :span="3">
          <span class="params-title">{{ '选中图片：' }}</span>
        </el-col>
        <el-col v-show="changImg!==''" :span="5" style="height:80px;margin-bottom:20px;">
          <img :src="changImg" class="img_class">
        </el-col>
      </el-row>
      <el-row v-show="loadingKey">
        <el-col :span="12">
          <span class="params-title">{{ '图片库使劲加载中，请稍后。。。' }}</span>
          <!-- <i class="el-icon-loading" /> -->
        </el-col>
      </el-row>
      <el-row :gutter="20" style="marginTop:20px;">
        <el-col v-for="(item,index) in allImgData" :key="index" style="height:108px;margin-bottom:20px;" :span="6">
          <div class="img_Box" @click="clickImg(item)">
            <img :src="item.url" class="img_class">
          </div>
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
import { queryBackground, uploadImgUrl, getAllImgList } from '@/api/background/background'
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
      imgUrlInfo: '',
      allImgData: [],
      changImg: '',
      imgInfo: {},
      loadingKey: true
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
    console.log('componentData--------', this.curComponent)
  },

  methods: {
    clickImg(res) {
      this.changImg = res.url
      this.imgInfo = res
    },
    getAllImg() {
      getAllImgList().then(res => {
        console.log('获取所有图片数据', res)
        this.allImgData = res.data
        this.loadingKey = false
      })
    },
    addNavInfo() {
      console.log('this.navInfoLis', this.navInfoLis)
      this.navInfoLis.push({
        name: '',
        relation: []
      })
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
      console.log('this.curComponent.commonBackground.boxWidth=====', this.curComponent.commonBackground)
      this.$emit('backgroundSetClose')
    },
    save() {
      const image = new Image()
      image.src = this.imgInfo.url

      image.onload = _ => {
        const width = image.width
        const height = image.height
        console.log('width', width, height)
        // 然后就可以做需要的操作了
        this.curComponent.picData = this.imgInfo.url
        this.curComponent.style.width = image.width
        this.curComponent.style.height = image.height
      }

      console.log('this.fileList', this.fileList)
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
      this.fileList = []
      this.commitStyle()
    },
    handlePictureCardPreview(file) {
      console.log('file---', file)
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    onChange(file, fileList) {
      console.log('file, fileList', file, fileList)
      // var _this = this
      // const reader = new FileReader()

      // reader.onload = e => {
      //   const img = e.target.result
      //   const image = new Image()
      //   image.src = img

      //   image.onload = _ => {
      //     const width = image.width
      //     const height = image.height
      //     console.log('width', width, height)
      //     console.log('img', img)
      //     // 然后就可以做需要的操作了
      //   }
      // }
      // reader.readAsDataUrl(file.raw)
      // this.imgchecked(file)
      const reader = new FileReader()
      reader.onload = (res) => {
        console.log('reader.result6666666', file, reader.result)
        this.imgUrlInfo = res.target.result
        // this.uploadImg(res.target.result)
        // var image = new Image()
        // console.log('22222222222')
        // image.src = res.target.result
        // image.onload = () => {
        //   console.log('获取图片数据', image.width, image.height)

        // }
      }
      reader.readAsDataURL(file.raw)
      console.log('222222', file, fileList)
    },
    uploadImg(item) {
      console.log('上传的图片---', item)
    },
    upadtaEven() {
      if (this.imgUrlInfo === '') {
        return
      }
      const params = {
        url: this.imgUrlInfo,
        imgDetailed: ''
      }
      uploadImgUrl(params).then(res => {
        console.log('请求结果', res)
        if (res.success) {
          this.$message.success('上传成功')
          this.uploadDisabled = false
          this.fileList = []
          this.imgUrlInfo = ''
          this.getAllImg()
        }
      })
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
  .img_class{
    width:100%;
    height:100%;
  }
  .img_Box{
    height:108px;
    cursor: pointer;
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
