<template>
  <el-row>
    <el-row>
      <el-col :span="24">
        <span style="font-weight:600;margin-right: 20px;font-size: 14px">{{ $t('panel.component_style') }}</span>
      </el-col>
    </el-row>
    <el-row class="main-content" disabled="!curComponent.commonBackground.enable">

      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ $t('panel.inner_padding') }}</span>
        </el-col>
        <el-col :span="15">
          <el-slider v-model="curComponent.commonBackground.innerPadding" show-input :show-input-controls="false" input-size="mini" :max="15" />
        </el-col>
      </el-row>
      <el-row style="height: 50px;overflow: hidden">
        <el-col :span="3">
          <span class="params-title">{{ $t('panel.board_radio') }}</span>
        </el-col>
        <el-col :span="15">
          <el-slider v-model="curComponent.commonBackground.borderRadius" show-input :show-input-controls="false" input-size="mini" />
        </el-col>
      </el-row>

      <el-row style="height: 50px">
        <el-col :span="3" style="padding-left: 10px;padding-top: 5px">
          <el-checkbox v-model="curComponent.commonBackground.enable">{{ $t('panel.background') }}</el-checkbox>
        </el-col>
        <el-col :span="21">
          <span style="color: #909399; font-size: 8px;margin-left: 3px;line-height: 30px">
            Tips:{{ $t('panel.choose_background_tips') }}
          </span>
        </el-col>
      </el-row>
      <el-row v-if="curComponent.commonBackground.enable">
        <el-row style="height: 40px;overflow: hidden">
          <el-col :span="3" style="padding-left: 10px;padding-top: 5px">
            <el-radio v-model="curComponent.commonBackground.backgroundType" label="color" @change="onChangeType">颜色</el-radio>
          </el-col>
          <el-col :span="1" style="padding-top: 5px">
            <el-color-picker v-model="curComponent.commonBackground.color" size="mini" class="color-picker-style" :predefine="predefineColors" />
          </el-col>
          <el-col :span="3">
            <span class="params-title-small">不透明度：</span>
          </el-col>
          <el-col :span="11">
            <el-slider v-model="curComponent.commonBackground.alpha" show-input :show-input-controls="false" input-size="mini" />
          </el-col>
        </el-row>
        <el-row style="height: 80px;margin-top:10px;margin-bottom:20px;overflow: hidden">
          <el-col :span="3" style="padding-left: 10px">
            <el-radio v-model="curComponent.commonBackground.backgroundType" label="outerImage" @change="onChangeType">{{ $t('panel.photo') }}</el-radio>
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
            <el-dialog top="25vh" width="600px" :modal-append-to-body="false" :visible.sync="dialogVisible">
              <img width="100%" :src="dialogImageUrl" alt="">
            </el-dialog>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="3" style="padding-left: 10px">
            <el-radio v-model="curComponent.commonBackground.backgroundType" label="innerImage" @change="onChangeType">边框</el-radio>
          </el-col>
          <el-col :span="21" class="main-row">
            <el-row v-for="(value, key) in BackgroundShowMap" :key="key">
              <el-col :span="24"><span>{{ key }}</span> </el-col>
              <el-col
                v-for="item in value"
                :key="item.id"
                :span="6"
              >
                <background-item
                  :template="item"
                />
              </el-col>
            </el-row>
          </el-col>
        </el-row>
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
import BackgroundItem from '@/views/background/BackgroundItem'
import { mapState } from 'vuex'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'
import { COLOR_PANEL } from '@/views/chart/chart/chart'

export default {
  name: 'Background',
  components: { BackgroundItem },
  data() {
    return {
      BackgroundShowMap: {},
      checked: false,
      backgroundOrigin: {},
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      panel: null,
      predefineColors: COLOR_PANEL
    }
  },
  created() {
    this.init()
  },
  mounted() {

  },
  computed: {
    ...mapState([
      'curComponent',
      'componentData'
    ])
  },
  methods: {
    init() {
      if (this.curComponent && this.curComponent.commonBackground && this.curComponent.commonBackground.outerImage && typeof (this.curComponent.commonBackground.outerImage) === 'string') {
        this.fileList.push({ url: this.curComponent.commonBackground.outerImage })
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
      this.curComponent.commonBackground.enable = this.backgroundOrigin.enable
      this.curComponent.commonBackground.backgroundType = this.backgroundOrigin.backgroundType
      this.curComponent.commonBackground.color = this.backgroundOrigin.color
      this.curComponent.commonBackground.innerImage = this.backgroundOrigin.innerImage
      this.curComponent.commonBackground.outerImage = this.backgroundOrigin.outerImage
      this.curComponent.commonBackground.alpha = this.backgroundOrigin.alpha
      this.curComponent.commonBackground.borderRadius = this.backgroundOrigin.borderRadius
      this.curComponent.commonBackground.innerPadding = this.backgroundOrigin.innerPadding
      this.$emit('backgroundSetClose')
    },
    save() {
      this.$store.commit('recordSnapshot')
      this.$emit('backgroundSetClose')
    },
    commitStyle() {
      const canvasStyleData = deepCopy(this.canvasStyleData)
      canvasStyleData.panel = this.panel
      this.$store.commit('setCanvasStyle', canvasStyleData)
      this.$store.commit('recordSnapshot', 'commitStyle')
    },
    onChangeType() {
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
        _this.curComponent.commonBackground.outerImage = reader.result
      }
      reader.readAsDataURL(file.raw)
    },
    upload(file) {
      // console.log('this is upload')
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
