<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini" :disabled="param && !hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.color')" class="form-item">
          <el-color-picker v-model="colorForm.color" class="color-picker-style" @change="changeBackgroundStyle" />
        </el-form-item>
        <el-form-item :label="$t('chart.not_alpha')" class="form-item form-item-slider">
          <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />
        </el-form-item>
      </el-form>
    </el-col>
    <!--    <div style="width: 100%">-->
    <!--      <el-popover-->
    <!--        placement="right"-->
    <!--        width="400"-->
    <!--        trigger="click"-->
    <!--      >-->
    <!--        <el-col>-->
    <!--          <el-form ref="colorForm" :model="colorForm" label-width="80px" size="mini">-->
    <!--            <el-form-item :label="$t('chart.color')" class="form-item">-->
    <!--              <colorPicker v-model="colorForm.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeBackgroundStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.not_alpha')" class="form-item form-item-slider">-->
    <!--              <el-slider v-model="colorForm.alpha" show-input :show-input-controls="false" input-size="mini" @change="changeBackgroundStyle" />-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" :disabled="param && !hasDataPermission('manage',param.privileges)" size="mini" class="shape-item">{{ $t('chart.background') }}<i class="el-icon-setting el-icon&#45;&#45;right" /></el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_BACKGROUND_COLOR } from '../../chart/chart'

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
      colorForm: JSON.parse(JSON.stringify(DEFAULT_BACKGROUND_COLOR))
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
</style>
