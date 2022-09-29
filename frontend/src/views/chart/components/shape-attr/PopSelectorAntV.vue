<template>
  <div style="width: 100%">
    <el-col>
      <el-form  ref="popForm" :model="popForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="popForm.popShow" @change="changePopCase">{{$t('chart.show')}}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.pop_open')" class="form-item">
          <el-radio-group v-model="popForm.popOpen" size="mini" @change="changePopCase">
            <el-radio-button label="top">{{$t('chart.text_pos_top')}}</el-radio-button>
            <el-radio-button label="bottom">{{$t('chart.text_pos_bottom')}}</el-radio-button>
            <el-radio-button label="left">{{$t('chart.text_pos_left')}}</el-radio-button>
            <el-radio-button label="right">{{$t('chart.text_pos_right')}}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('chart.pop_left')">
          <el-slider v-model="popForm.popLeft" show-input :show-input-controls="false" input-size="mini" :min="-1000" :step="1" :max="1000" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_top')">
          <el-slider v-model="popForm.popTop" show-input :show-input-controls="false" input-size="mini" :min="-1000" :step="1" :max="1000" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_title_color')" class="form-item">
          <el-color-picker v-model="popForm.popTitleColor" class="color-picker-style" :predefine="predefineColors" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_title_background')" class="form-item">
          <el-color-picker v-model="popForm.popTitleBackground" class="color-picker-style" :predefine="predefineColors" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_title_lineHeight')" class="form-item">
          <el-slider v-model="popForm.popHeight" show-input :show-input-controls="false" input-size="mini" :min="10" :step="1" :max="100" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_title_position')" class="form-item">
          <el-radio-group v-model="popForm.popPosition" size="mini" @change="changePopCase">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('chart.pop_content_color')" class="form-item">
          <el-color-picker v-model="popForm.popContentColor" class="color-picker-style" :predefine="predefineColors" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_content_background')" class="form-item">
          <el-color-picker v-model="popForm.popContentBackground" class="color-picker-style" :predefine="predefineColors" @change="changePopCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.pop_content_lineHeight')" class="form-item">
          <el-slider v-model="popForm.popContentHeight" show-input :show-input-controls="false" input-size="mini" :min="10" :step="1" :max="100" @change="changePopCase" />
        </el-form-item>
        <!-- <el-form-item :label="$t('chart.pop_content_border_style')" class="form-item">
          <el-select v-model="popForm.popContentBorderBottomStyle">
            <el-option v-for="(item,index) in lineStyle" :key="index" :label="item.name" :value="item.value" />
          </el-select>
        </el-form-item> -->
        <el-form-item :label="$t('chart.pop_content_border_color')" class="form-item">
          <el-color-picker v-model="popForm.popContentBorderBottomColor" class="color-picker-style" :predefine="predefineColors" @change="changePopCase" />
        </el-form-item>
        <!-- <el-form-item :label="$t('chart.pop_content_border_width')" class="form-item">
          <el-slider v-model="popForm.popContentBorderBottomWidth" show-input :show-input-controls="false" input-size="mini" :min="0" :step="1" :max="10" @change="changePopCase" />
        </el-form-item> -->
      </el-form>
    </el-col>
  </div>
</template>
<script>
import {COLOR_PANEL, DEFAULT_LABEL } from '../../chart/chart'
export default {
  name: 'FocusSelector',
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
      popForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      predefineColors: COLOR_PANEL,
      lineStyle: [
        {name: '实线',value: 'solid'},
        {name: '虚线',value: 'dashed'},
        {name: '点',value: 'dotted'},
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
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.label) {
          this.popForm = customAttr.label

          this.popForm.popShow = this.popForm.popShow? this.popForm.popShow : DEFAULT_LABEL.popShow
          this.popForm.popOpen = this.popForm.popOpen? this.popForm.popOpen : DEFAULT_LABEL.popOpen
          this.popForm.popLeft = this.popForm.popLeft? this.popForm.popLeft : DEFAULT_LABEL.popLeft
          this.popForm.popTop = this.popForm.popTop? this.popForm.popTop : DEFAULT_LABEL.popTop
          this.popForm.popTitleColor = this.popForm.popTitleColor? this.popForm.popTitleColor : DEFAULT_LABEL.popTitleColor
          this.popForm.popTitleBackground = this.popForm.popTitleBackground? this.popForm.popTitleBackground : DEFAULT_LABEL.popTitleBackground
          this.popForm.popHeight = this.popForm.popHeight? this.popForm.popHeight : DEFAULT_LABEL.popHeight
          this.popForm.popPosition = this.popForm.popPosition? this.popForm.popPosition : DEFAULT_LABEL.popPosition
          this.popForm.popContentColor = this.popForm.popContentColor? this.popForm.popContentColor : DEFAULT_LABEL.popContentColor
          this.popForm.popContentBackground = this.popForm.popContentBackground? this.popForm.popContentBackground : DEFAULT_LABEL.popContentBackground
          this.popForm.popContentHeight = this.popForm.popContentHeight? this.popForm.popContentHeight : DEFAULT_LABEL.popContentHeight
          this.popForm.popContentBorderBottomColor = this.popForm.popContentBorderBottomColor? this.popForm.popContentBorderBottomColor : DEFAULT_LABEL.popContentBorderBottomColor
        }
      }
    },
    
    changePopCase() {
      this.$emit('onLabelChange', this.popForm)
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
  span{font-size: 12px}

.el-form-item{
  margin-bottom: 6px;
}
.el-divider--horizontal {
  margin: 10px 0
}
.divider-style>>>.el-divider__text{
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
</style>