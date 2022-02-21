<template>
  <div style="width: 100%">
    <el-col>
      <el-row>
        <el-col :span="5" class="col-label-item">
          <el-radio v-model="colorForm.backgroundType" label="color" @change="changeBackgroundStyle"><span class="label-item">{{ $t('chart.color') }}</span></el-radio>
        </el-col>
        <el-col :span="19">
          <el-color-picker v-model="colorForm.color" :predefine="predefineColors" size="mini" style="cursor: pointer;z-index: 1004;" @change="changeBackgroundStyle" />
        </el-col>
      </el-row>
      <el-row style="height: 60px;margin-top:10px;overflow: hidden">
        <el-col :span="5" class="col-label-item">
          <el-radio v-model="colorForm.backgroundType" label="image" @change="changeBackgroundStyle"><span class="label-item">{{ $t('panel.photo') }}</span></el-radio>
        </el-col>
        <el-col :span="9">
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
      </el-row>
      <el-row>
        <el-col :span="5" class="col-label-item">
          <span class="label-item">{{ $t('chart.not_alpha') }}</span>
        </el-col>
        <el-col :span="19">
          <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="5" class="col-label-item">
          <span class="label-item">{{ $t('chart.border_radius') }}</span>
        </el-col>
        <el-col :span="19">
          <el-slider v-model="colorForm.borderRadius" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />
        </el-col>
      </el-row>
    </el-col>
    <!--      <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini" :disabled="param && !hasDataPermission('manage',param.privileges)">-->
    <!--        &lt;!&ndash;        <el-form-item :label="$t('chart.color')" class="form-item">&ndash;&gt;-->
    <!--        &lt;!&ndash;          <el-color-picker v-model="colorForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeBackgroundStyle" />&ndash;&gt;-->
    <!--        &lt;!&ndash;        </el-form-item>&ndash;&gt;-->
    <!--        <el-form-item :label="$t('chart.not_alpha')" class="form-item form-item-slider">-->
    <!--          <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />-->
    <!--        </el-form-item>-->

    <!--        <el-form-item :label="$t('chart.border_radius')" class="form-item form-item-slider">-->
    <!--          <el-slider v-model="colorForm.borderRadius" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />-->
    <!--        </el-form-item>-->
    <!--      </el-form>-->
    <!--    </el-col>-->
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_BACKGROUND_COLOR } from '../../chart/chart'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  name: 'BackgroundColorSelector',
  props: {
    param: {
      type: Object,
      required: false
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      colorForm: JSON.parse(JSON.stringify(DEFAULT_BACKGROUND_COLOR)),
      predefineColors: COLOR_PANEL
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.init()
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    changeBackgroundStyle() {
      this.$emit('onChangeBackgroundForm', this.colorForm)
    },
    init() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.background) {
          this.colorForm = customStyle.background
        }
      }
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
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
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
  .label-item{
    font-size: 12px;
    font-weight: bold;
  }
.col-label-item{
  text-align: right;
  padding-right: 10px;
}
</style>
