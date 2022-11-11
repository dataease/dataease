<template>
  <div>
    <p style="font-size: 18px;">弹窗设置</p>
    <div style="margin-top: 10px;">
      <el-row class="text_row">
        <el-col :span="4" class="text_col_4">
          <span>单击弹窗</span>
        </el-col>
        <el-col :span="8">
          <el-checkbox v-model="curComponent.options.isPopVisible">启用</el-checkbox>
        </el-col>
      </el-row>
      <el-row class="text_row">
        <el-col :span="4" class="text_col_4">
          <span>标题</span>
        </el-col>
        <el-col :span="8">
          <el-input v-model="curComponent.options.popTitle" size="small"></el-input>
        </el-col>
      </el-row>
      <el-row class="text_row">
        <el-col :span="4" class="text_col_4">
          <span>展示图片</span>
        </el-col>
        <el-col style="margin-top: 10px;padding-left: 20px;">
          <el-col style="position:relative;text-align: center;" 
            :span="6" v-show="fileList.length" 
            v-for="(item,index) in fileList" :key="index"
          >
            <img :src="item.url" alt="" style="width: 90%;">
            <i class="el-icon-delete del_img" @click="handleRemove(index)"></i>
          </el-col>
          <el-col :span="6">
            <el-upload
              action=""
              accept=".jpeg,.jpg,.png,.gif,.svg"
              class="avatar-uploader"
              :multiple="true"
              list-type="picture-card"
              :http-request="upload"
              :show-file-list="false"
              :file-list="fileList"
              :on-change="onChange"
            >
            <!-- :limit="1" -->
              <i class="el-icon-plus" />
            </el-upload>
            <span>
              <i class="el-icon-warning" /> <span>上传的文件大小不能超过10MB!</span>
            </span>
          </el-col>
          
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
  name: 'textPopSet',
  props: {
    element: {
      type: Object,
      require: true
    }
  },
  data() {
    return {
      predefineColors: COLOR_PANEL,
      fileList: [],
      updataUrl: '',
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
    if (this.curComponent.options === undefined) {
      this.curComponent.options = {
        isPopVisible: false,
        popImgList: []
      }
    }
    this.fileList = this.curComponent.options.popImgList
  },
  methods: {
    save() {
      console.log('保存',this.fileList)
      this.curComponent.options.popImgList = this.fileList
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    cancel() {
      this.$emit('backgroundSetClose')
    },
    handlePictureCardPreview(file) {
      console.log('file---', file)
      // this.dialogImageUrl = file.url
      // this.dialogVisible = true
    },
    handleRemove(index) {
      console.log('remove',index)
      this.fileList.splice(index,1)
    },
    upload(file) {
      console.log('this is upload', file)
    },
    onChange(file, fileList) {
      console.log('change', fileList,this.fileList)
      if (file.size / 1024 / 1024 > 10) {
        this.$message.error('上传的文件大小不能超过 10MB!')
        return
      }
      var _this = this
      const reader = new FileReader()
      reader.onload = function() {
        _this.fileList.push(
          {url: reader.result}
        )
        console.log('reader.result6666666', reader.result,_this.fileList)
      }
      reader.readAsDataURL(file.raw)
      console.log('222222', file, fileList)
    },
  }
}
</script>
<style lang="scss">
.root-class {
  margin: 15px 0px 5px;
  text-align: center;
}

.text_row {
  margin: 10px 20px;

  .text_col_4 {
    text-align: center;
    font-weight: bold;
  }
}
.del_img {
  position: absolute;
  top: 40%;
  left: 46%;
  color: #dfe4ed;
  font-size: 16px;
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