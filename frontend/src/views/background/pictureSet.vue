<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ '图库' }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content">
      <el-row style="height: 80px;margin-top:10px;margin-bottom:5px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ '选择分组' }}</span>
        </el-col>
        <el-col :span="5">
          <el-select v-model="groupName" placeholder="请选择分组">
            <el-option
              v-for="item in allImgData"
              :key="item.name"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-col>
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
          <el-button type="primary" size="mini" @click="upadtaEven()">上传</el-button>
          <!-- <span class="params-title" >{{ '上传' }}</span> -->
        </el-col>

      </el-row>
      <el-row>
        <el-col :offset="11" :span="8">
          <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="3">
          <span class="params-title">{{ '选中图片：' }}</span>
        </el-col>
        <el-col v-show="changImg!==''" :span="6" style="height:108px;margin-bottom:20px;overflow-y:scroll;">
          <img :src="changImg" class="img_class">
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="3" style="padding-left:10px;margin-bottom:20px;padding-top:3px;">
          <el-button type="primary" size="mini" @click="addGroup()">添加分组</el-button>
        </el-col>
        <el-col v-show="addGrop" :span="5" style="padding-left:10px;">
          <el-input v-model="addInput" placeholder="请输入内容" />
        </el-col>
        <el-col v-show="addGrop" :span="3" style="padding-left:10px;margin-bottom:20px;padding-top:3px;">
          <el-button type="primary" size="mini" @click="addSubmit()">确定</el-button>
        </el-col>
      </el-row>

      <el-row v-show="loadingKey">
        <el-col :span="12">
          <span class="params-title">{{ '图片库使劲加载中，请稍后。。。' }}</span>
          <!-- <i class="el-icon-loading" /> -->
        </el-col>
      </el-row>
      <!-- <el-row :gutter="20" style="marginTop:20px;">
        <el-col v-for="(item,index) in allImgData" :key="index" style="height:108px;margin-bottom:20px;" :span="6">
          <div class="img_Box" @click="clickImg(item)">
            <img :src="item.url" class="img_class">
          </div>
        </el-col>
      </el-row> -->
      <el-row>
        <el-col>
          <el-collapse v-model="activeNames" @change="handleChange">
            <el-collapse-item v-for="(ited,index) in allImgData" :key="index" :title="ited.name" :name="ited.name">
              <template slot="title">
                <!-- <i class="header-icon el-icon-info" /> -->
                <span style="width:600px">{{ ited.name }}</span>
                <!-- <el-input v-show="ited.editKey" v-model="ited.name" style="width:180px" placeholder="请输入内容" /> -->
                <i style="margin-left:0px" class="el-icon-edit editClass" @click.stop="editBtn(ited)" />
                <!-- <el-button v-show="!ited.editKey" style="margin-left:20px" type="primary" size="mini" @click.native.stop="editBtn(ited)">修改</el-button> -->
                <!-- <el-button v-show="ited.editKey" type="primary" size="mini" @click.native="submiBtn(ited)">确定</el-button> -->
                <i style="margin-left:20px;color:red" class="el-icon-delete editClass" @click.stop="deletGrop(ited)" />
                <!-- <el-button type="danger" size="mini" @click.native.stop="deletGrop(ited)">删除</el-button> -->
              </template>
              <el-row :gutter="10" style="padding:10px;">
                <el-col v-for="(item,indexs) in ited.str" :key="indexs" style="height:108px;margin-bottom:20px; position:relative;" :span="6">
                  <div class="img_Box" @click="clickImg(item)">
                    <img :src="item.url" class="img_class">
                  </div>
                  <i class="el-icon-circle-close dele_btn" @click.stop="delectBtn(item)" />
                </el-col>
              </el-row>
            </el-collapse-item>
          </el-collapse>
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
import { updateName, queryBackground, uploadImgUrl, delName, getAllImgList, deletImg } from '@/api/background/background'
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
      addInput: '',
      addGrop: false,
      groupName: '',
      groupOps: [],
      activeNames: [],
      imgList: [
        {
          name: '苏州'
        }, {
          name: '北京'
        }, {
          name: '长沙'
        }, {
          name: '武汉'
        }
      ],
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
      loadingKey: true,
      oldName: ''
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
    handleChange(val) {
      console.log(val, this.activeNames)
    },
    clickImg(res) {
      this.changImg = res.url
      this.imgInfo = res
    },
    delectBtn(item) {
      console.log('删除按钮----', item)
      deletImg(item.id).then(res => {
        console.log('删除数据', res)
        // this.$message.success('删除成功')
        this.getAllImg('删除成功')
      })
    },
    getAllImg(key) {
      getAllImgList().then(res => {
        console.log('获取所有图片数据', res)
        this.allImgData = res.data
        this.allImgData.forEach(ele => {
          ele.editKey = false
        })
        if (key) {
          this.$message.success(key)
        }
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
      if (this.imgInfo.url) {
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
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        this.fileList = []
        return
      }
      console.log('file, fileList', file, fileList)
      // var _this = this
      // const reader = new FileReader()

      // reader.onload = e => {
      //   const img = e.target.result
      //   const image = new Image()
      //   image.src = img

      //  allImg:[
      //       {
      //         name: '苏州',
      //         url: [
      //           '11111','2222'
      //         ]
      //       },
      //       {
      //         name: '北京',
      //         url: [
      //           '22222'
      //         ]
      //       }
      //     ]

      //   {
      //     url:'222'
      //     name:

      //   }

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
    addGroup() { // 添加分组
      // this.allImgData.push({
      this.addGrop = true
      // })
    },
    deletGrop(ele) {
      this.$confirm('此操作将永久删除当前分组下的所有图片, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delName({ name: ele.name }).then(res => {
          this.getAllImg('删除成功')
        })
        // this.$message({
        //   type: 'success',
        //   message: '删除成功!'
        // })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    submiBtn(ele) {
      ele.editKey = false
    },
    editBtn(ele) {
      console.log('----', ele)
      this.oldName = ele.name
      // ele.editKey = true
      this.$prompt('请输入新的分组名称', '编辑', {
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }).then(({ value }) => {
        updateName({ oldName: ele.name, newName: value }).then(res => {
          console.log('更新名字', res)
          this.getAllImg('更新成功')
        })
        // this.$message({
        //   type: 'success',
        //   message: '你的邮箱是: ' + value
        // })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消修改'
        })
      })
    },
    addSubmit() {
      if (this.addInput === '') {
        return
      }
      this.allImgData.push({
        name: this.addInput
      })
      this.addInput = ''
      this.addGrop = false
    },
    upadtaEven() {
      if (this.imgUrlInfo === '' || this.groupName === '') {
        return
      }
      const params = {
        url: this.imgUrlInfo,
        name: this.groupName
      }
      uploadImgUrl(params).then(res => {
        console.log('请求结果', res)
        if (res.success) {
          // this.$message.success('上传成功')
          this.uploadDisabled = false
          this.fileList = []
          this.imgUrlInfo = ''
          this.groupName = ''
          this.getAllImg('上传成功')
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
    /* height:100%; */
  }
  .img_Box{
    height:108px;
    cursor: pointer;

    overflow-y: scroll;
  }
  .editClass{
    cursor: pointer;
  }
  .dele_btn{
    position:absolute;
    right:-5px;
    font-size:20px;
    color:red;
    top:-10px;
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
    border-bottom:none;
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
