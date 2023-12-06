<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="styleForm" :model="styleForm" label-width="80px" size="mini" >
        <el-form-item :label="$t('chart.background')" class="form-item">
          <!-- <el-color-picker v-model="colorForm.color" class="color-picker-style" :predefine="predefineColors" @change="changeBackgroundStyle" /> -->
            <el-radio-group v-model="styleForm.baseMapTheme" @change="changeBackgroundStyle">
                <el-radio label="light">{{$t('plugin_view_symbol_map.light')}}</el-radio>
                <el-radio label="dark">{{$t('plugin_view_symbol_map.dark')}}</el-radio>
            </el-radio-group>
        </el-form-item>

      </el-form>
    </el-col>
  </div>
</template>

<script>
import {  DEFAULT_BASE_MAP_STYLE } from '@/utils/map'

export default {
  name: 'BaseMapStyleSelector',
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
      styleForm: JSON.parse(JSON.stringify(DEFAULT_BASE_MAP_STYLE)),
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
      this.$emit('onChangeBaseMapForm', this.styleForm)
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
        if (customStyle.baseMapStyle) {
          this.styleForm = customStyle.baseMapStyle
        }
      }
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

.form-item ::v-deep .el-checkbox__label {
  font-size: 12px;
}
.form-item ::v-deep .el-radio__label {
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
</style>
