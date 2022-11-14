<template>
  <div>
    <p style="font-size: 18px;">跳转设置</p>
    <div style="margin-top: 10px;">
      <el-row class="jump_row">
        <el-col :span="12">
          <el-col :span="6" class="jump_col_4">
            名称颜色:
          </el-col>
          <el-col :span="18">
            <el-color-picker v-model="curComponent.options.color" class="color-picker-style" :predefine="predefineColors" />
          </el-col>
        </el-col>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-col :span="6" class="jump_col_4">
            展示内容:
          </el-col>
          <el-col :span="18">
            <el-input v-model="curComponent.options.placeholder" size="small"></el-input>
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="6" class="jump_col_4">
            名称背景:
          </el-col>
          <el-col :span="18">
            <el-row>
              <el-radio-group v-model="curComponent.options.nameType" style="width:100%;">
                <el-col :span="11">
                  <el-radio label="color">颜色</el-radio>
                </el-col>
                <el-col :span="13">
                  <el-radio label="back">图片</el-radio>
                </el-col>
              </el-radio-group>
            </el-row>
            <el-row style="margin-top: 10px;">
              <el-col :span="11">
                <el-color-picker v-model="curComponent.options.nameBgColor" class="color-picker-style" :predefine="predefineColors" />
              </el-col>
              <el-col :span="13" v-if="nameUrl === ''">
                <el-upload
                  action=""
                  accept=".jpeg,.jpg,.png,.gif,.svg"
                  class="avatar-uploader"
                  list-type="picture-card"
                  :http-request="upload"
                  :file-list="nameList"
                  :on-change="onNameChange"
                  :limit="1"
                >
                  <i class="el-icon-plus" />
                </el-upload>
                <span>
                  <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
                </span>
              </el-col>
              <el-col :span="12" v-else>
                <div style="width: 100%;overflow-y:scroll;position: relative;">
                  <img :src="nameUrl" alt="" style="width: 100%"/>
                  <i class="el-icon-delete del_img" @click="handleNameRemove"></i>
                </div>
              </el-col>
            </el-row>
          </el-col>
        </el-col>
        <el-col :span="12">
          <el-col :span="6" class="jump_col_4">
            背景:
          </el-col>
          <el-col :span="18">
            <el-row>
              <el-radio-group v-model="curComponent.options.bgType" style="width:100%;">
                <el-col :span="12">
                  <el-radio label="color">颜色</el-radio>
                </el-col>
                <el-col :span="12">
                  <el-radio label="back">图片</el-radio>
                </el-col>
              </el-radio-group>
            </el-row>
            <el-row style="margin-top: 10px;">
              <el-col :span="12">
                <el-color-picker v-model="curComponent.options.jumpBgColor" class="color-picker-style" :predefine="predefineColors" />
              </el-col>
              <el-col :span="12" v-if="updataUrl === ''">
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
                  :limit="1"
                >
                  <i class="el-icon-plus" />
                </el-upload>
                <span>
                  <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
                </span>
              </el-col>
              <el-col :span="12" v-else>
                <div style="width: 100%;overflow-y:scroll;position: relative;">
                  <img :src="updataUrl" alt="" style="width: 100%"/>
                  <i class="el-icon-delete del_img" @click="handleRemove"></i>
                </div>
              </el-col>
            </el-row>
          </el-col>
        </el-col>
        
        
        
      </el-row>
      <el-row>
        <el-col>
          <el-button type="primary" @click="addLink" size="small">新增</el-button>
        </el-col>
      </el-row>
      <el-row>
        <el-col v-for="(item,index) in curComponent.options.jumpList"  :key="index">
          <el-row class="jump_row">
            <el-col :span="4" class="jump_col_4">名称：</el-col>
            <el-col :span="10">
              <el-input v-model="item.jumpName" size="small"></el-input>
            </el-col>
            <el-col :span="4" v-if="curComponent.options.jumpList.length>1" style="padding-left: 5px;">
              <el-button type="danger" size="small" @click="delLink(index)">删除</el-button>
            </el-col>
          </el-row>
          <el-row class="jump_row">
            <el-col :span="4" class="jump_col_4">链接：</el-col>
            <el-col :span="10">
              <el-input v-model="item.jumpLink" size="small"></el-input>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </div>
    <el-row class="root-class">
      <el-col :span="24">
        <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'jumpSet',
  props: {
    element: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      predefineColors: COLOR_PANEL,
      uploadDisabled: false,
      fileList: [],
      updataUrl: '',
      nameUrl: '',
      nameList: [],
    }
  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
  },
  mounted() {
    console.log(this.curComponent)
    this.updataUrl = this.curComponent.options.jumpBgImg? this.curComponent.options.jumpBgImg : ''
  },
  methods: {
    save() {
      console.log(this.updataUrl)
      this.curComponent.options.jumpBgImg = this.updataUrl
      this.curComponent.options.nameBgImg = this.nameUrl
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    cancel() {

      this.$emit('backgroundSetClose')
    },
    addLink() {
      this.curComponent.options.jumpList.push({jumpName: '', jumpLink: ''})
    },
    delLink(index) {
      console.log(index)
      this.curComponent.options.jumpList.splice(index,1)
    },
    handlePictureCardPreview(file) {
      console.log('file---', file)
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    handleRemove(file, fileList) {
      console.log(file, fileList)
      this.uploadDisabled = false
      // this.panel.imageUrl = null
      this.curComponent.options.jumpBgImg = ''
      this.updataUrl = ""
      this.fileList = []
      // this.commitStyle()
    },
    handleNameRemove(file,fileList) {
      console.log(file,fileList)
      this.curComponent.options.nameBgImg = ""
      this.nameUrl = ""
      this.nameList = []
    },
    upload(file) {
      console.log('this is upload', file)
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
    onNameChange(file,fileList) {
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        this.nameList = []
        return
      }
      var _this = this
      const reader = new FileReader()
      reader.onload = function() {
        _this.nameUrl = reader.result
        console.log('reader.result111111', reader.result)
      }
      reader.readAsDataURL(file.raw)
    }
  }
}
</script>
<style lang="scss">
.root-class {
  margin: 15px 0px 5px;
  text-align: center;
}

.jump_row {
  margin: 10px 20px;

  .jump_col_4 {
    text-align: center;
    font-weight: bold;
  }
}

.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}

.del_img {
  position: absolute;
  top: 40%;
  left: 46%;
  color: #ffffff;
  font-size: 16px;
}
</style>