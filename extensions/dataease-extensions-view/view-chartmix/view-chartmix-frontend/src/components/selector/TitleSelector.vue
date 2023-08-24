<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="titleForm" :model="titleForm" label-width="80px" size="mini" >
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="titleForm.show" @change="changeTitleStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="titleForm.show">
          <el-form-item :label="$t('chart.title')" class="form-item">
            <el-input
              v-model="titleForm.title"
              size="mini"
              :placeholder="$t('chart.title')"
              clearable
              @blur="changeTitleStyle"
              @input="inputOnInput($event)"
            />
          </el-form-item>
          <el-form-item :label="$t('chart.font_family')" class="form-item">
            <el-select v-model="titleForm.fontFamily" :placeholder="$t('chart.font_family')" @change="changeTitleStyle('fontFamily')">
              <el-option v-for="option in fontFamily" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="titleForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTitleStyle">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="titleForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeTitleStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.text_h_position')" class="form-item">
            <el-radio-group v-model="titleForm.hPosition" size="mini" @change="changeTitleStyle">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <!-- <el-form-item :label="$t('chart.text_v_position')" class="form-item">
            <el-radio-group v-model="titleForm.vPosition" size="mini" @change="changeTitleStyle">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item> -->
          <el-form-item :label="$t('chart.text_style')" class="form-item">
            <el-checkbox v-model="titleForm.isItalic" @change="changeTitleStyle">{{ $t('chart.italic') }}</el-checkbox>
            <el-checkbox v-model="titleForm.isBolder" @change="changeTitleStyle">{{ $t('chart.bolder') }}</el-checkbox>
          </el-form-item>

          <el-form-item :label="$t('chart.letter_space')" class="form-item">
            <el-select v-model="titleForm.letterSpace" :placeholder="$t('chart.quota_letter_space')" @change="changeTitleStyle('letterSpace')">
              <el-option v-for="option in fontLetterSpace" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.font_shadow')" class="form-item">
            <el-checkbox v-model="titleForm.fontShadow" @change="changeTitleStyle('fontShadow')">{{ $t('chart.font_shadow') }}</el-checkbox>
          </el-form-item>

          <el-form-item :label="$t('chart.remark')" class="form-item">
            <el-checkbox v-model="titleForm.remarkShow" @change="changeTitleStyle('remarkShow')">{{ $t('chart.show') }}</el-checkbox>
          </el-form-item>
          <span v-show="titleForm.remarkShow">
            <el-form-item :label="$t('chart.remark_edit')" class="form-item">
              <el-button
                :title="$t('chart.edit')"
                icon="el-icon-edit"
                type="text"
                size="small"
                @click="editRemark"
              />
            </el-form-item>
            <el-form-item :label="$t('chart.remark_bg_color')" class="form-item">
              <el-color-picker v-model="titleForm.remarkBackgroundColor" class="color-picker-style" :predefine="predefineColors" @change="changeTitleStyle('remarkBackgroundColor')" />
            </el-form-item>
          </span>
        </div>
      </el-form>
    </el-col>

    <!--富文本编辑框-->
    <el-dialog
      v-if="showEditRemark"
      v-dialogDrag
      :title="$t('chart.remark')"
      :visible="showEditRemark"
      :show-close="false"
      width="70%"
      class="dialog-css"
      append-to-body
    >
      <remark-editor :remark="titleForm.remark" @onRemarkChange="onRemarkChange" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeRemark">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="changeRemark">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { COLOR_PANEL, DEFAULT_TITLE_STYLE } from '@/utils/map'

export default {
  name: 'TitleSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
      fontSize: [],
      isSetting: false,
      predefineColors: COLOR_PANEL,
      showEditRemark: false,
      fontFamily: [
        { name: '微软雅黑', value: 'Microsoft YaHei' },
        { name: '宋体', value: 'SimSun' },
        { name: '黑体', value: 'SimHei' },
        { name: '楷体', value: 'KaiTi' }
      ],
      fontLetterSpace: [
        { name: '0px', value: '0' },
        { name: '1px', value: '1' },
        { name: '2px', value: '2' },
        { name: '3px', value: '3' },
        { name: '4px', value: '4' },
        { name: '5px', value: '5' },
        { name: '6px', value: '6' },
        { name: '7px', value: '7' },
        { name: '8px', value: '8' },
        { name: '9px', value: '9' },
        { name: '10px', value: '10' }
      ]
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

          this.titleForm.remarkShow = this.titleForm.remarkShow ? this.titleForm.remarkShow : false
          this.titleForm.remark = this.titleForm.remark ? this.titleForm.remark : ''
          this.titleForm.remarkBackgroundColor = this.titleForm.remarkBackgroundColor ? this.titleForm.remarkBackgroundColor : '#ffffffff'

          this.titleForm.fontFamily = this.titleForm.fontFamily ? this.titleForm.fontFamily : 'Microsoft YaHei'
          this.titleForm.letterSpace = this.titleForm.letterSpace ? this.titleForm.letterSpace : '0'
          this.titleForm.fontShadow = this.titleForm.fontShadow ? this.titleForm.fontShadow : false
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
    changeTitleStyle() {
      if (this.titleForm.title.length < 1) {
        this.$error(this.$t('chart.title_cannot_empty'))
        this.titleForm.title = this.chart.title
        return
      }
      if (!this.titleForm.show) {
        this.isSetting = false
      }
      this.$emit('onTextChange', this.titleForm)
    },
    inputOnInput: function(e) {
      this.$forceUpdate()
    },

    editRemark() {
      this.showEditRemark = true
    },
    closeRemark() {
      this.showEditRemark = false
    },
    changeRemark() {
      this.titleForm.remark = this.tmpRemark
      this.changeTitleStyle('remark')
      this.closeRemark()
    },
    onRemarkChange(val) {
      this.tmpRemark = val
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
.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label{
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

.switch-style{
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}
</style>
