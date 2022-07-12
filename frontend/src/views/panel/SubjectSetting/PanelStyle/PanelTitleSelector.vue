<template>
  <div>
    <div style="width: 100%;">
      <el-popover
        placement="right"
        width="400"
        trigger="click"
      >
        <el-col>
          <el-form ref="titleForm" :model="titleForm" label-width="80px" size="mini">
            <el-form-item :label="$t('chart.show')" class="form-item">
              <el-checkbox v-model="titleForm.show" @change="onTitleChange">{{ $t('chart.show') }}</el-checkbox>
            </el-form-item>
            <div v-show="titleForm.show">
              <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
                <el-select v-model="titleForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="onTitleChange">
                  <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item  :label="$t('chart.text_style')" class="form-item">
                <el-select v-model="titleForm.fontFamily" placeholder="请选择" size="mini" @change="onTitleChange">
                  <el-option
                    v-for="item in fontOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
              <el-form-item :label="$t('chart.text_color')" class="form-item">
                <el-color-picker v-model="titleForm.color" class="color-picker-style" :predefine="predefineColors" @change="onTitleChange" />
              </el-form-item>
              <el-form-item v-show="chart.type && chart.type !== 'liquid'" :label="$t('chart.text_h_position')" class="form-item">
                <el-radio-group v-model="titleForm.hPosition" size="mini" @change="onTitleChange">
                  <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
                  <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
                  <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <!-- <el-form-item v-show="chart.type && !chart.type.includes('table') && chart.type !== 'liquid' && !chart.type.includes('text')" :label="$t('chart.text_v_position')" class="form-item">
                <el-radio-group v-model="titleForm.vPosition" size="mini" @change="onTitleChange">
                  <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
                  <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
                  <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
                </el-radio-group>
              </el-form-item> -->
              <el-form-item :label="$t('chart.text_style')" class="form-item">
                <el-checkbox v-model="titleForm.isItalic" @change="onTitleChange">{{ $t('chart.italic') }}</el-checkbox>
                <el-checkbox v-model="titleForm.isBolder" @change="onTitleChange">{{ $t('chart.bolder') }}</el-checkbox>
              </el-form-item>
            </div>
          </el-form>
        </el-col>
        <el-button slot="reference" size="mini" class="shape-item">{{ $t('chart.title') }}<i class="el-icon-setting el-icon--right" /></el-button>
      </el-popover>
    </div>
  </div>
</template>
<script>
import { COLOR_PANEL, DEFAULT_TITLE_STYLE } from '@/views/chart/chart/chart'
import { checkTitle } from '@/api/chart/chart'

export default {
  name: 'PanelTitleSelector',
  props: {
    chart: {
      type: Object,
      required: true
    },
    sourceType: {
      type: String,
      default: 'view',
      required: false
    }
  },
  data() {
    return {
      titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
      fontSize: [],
      predefineColors: COLOR_PANEL,
      fontOptions: ['宋体', '楷体', '黑体', '仿宋', '新宋体']
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.init()
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.text) {
          this.titleForm = customStyle.text
        }
        this.titleForm.title = this.chart.title
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 60; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    onTitleChange() {
      this.$emit('onTitleChange', this.titleForm)
    },
    inputOnInput: function(e) {
      this.$forceUpdate()
    },
  }
}
</script>
<style scoped>
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
</style>