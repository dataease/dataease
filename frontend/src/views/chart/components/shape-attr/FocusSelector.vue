<template>
  <div style="width: 100%">
    <el-col>
      <el-form v-show="chart.type && chart.type === 'graph'" ref="focusFormGraph" :model="focusForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.dragEnable')" class="form-item">
          <el-checkbox v-model="focusForm.dragEnable" @change="changeFocusCase">启用</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.nodalRepulsion')" class="form-item">
          <el-slider v-model="focusForm.repulsion" show-input :show-input-controls="false" input-size="mini" :min="50" :max="1000" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.nodeSpacing')" class="form-item">
          <el-slider v-model="focusForm.edgeLength" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.centripetalGravitation')" class="form-item">
          <el-slider v-model="focusForm.gravity" show-input :show-input-controls="false" input-size="mini" :min="0" :step="0.1" :max="1" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.reductionRate')" class="form-item">
          <el-slider v-model="focusForm.reductionRate" show-input :show-input-controls="false" input-size="mini" :min="5" :step="5" :max="100" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.shadowBlur')" class="form-item">
          <el-slider v-model="focusForm.shadowBlur" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.variety_depth')" class="form-item">
          <el-slider v-model="focusForm.variety_depth" show-input :show-input-controls="false" input-size="mini" :min="0.1" :step="0.1" :max="1" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.scaleLimit_max')" class="form-item">
          <el-slider v-model="focusForm.scaleLimitMax" show-input :show-input-controls="false" input-size="mini" :min="0" :step="0.1" :max="50" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.scaleLimit_min')" class="form-item">
          <el-slider v-model="focusForm.scaleLimitMin" show-input :show-input-controls="false" input-size="mini" :min="0" :step="0.1" :max="1" @change="changeFocusCase" />
        </el-form-item>
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
      focusForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      predefineColors: COLOR_PANEL,
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
          this.focusForm = customAttr.label
        }
      }
    },
    
    changeFocusCase() {
      this.$emit('onLabelChange', this.focusForm)
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