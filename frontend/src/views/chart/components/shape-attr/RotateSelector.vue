<template>
  <div style="width: 100%">
    <el-col>
      <el-form v-show="chart.type " ref="rotateFormGraph" :model="rotateForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.innerRotation')" class="form-item">
          <el-slider v-model="rotateForm.alpha" show-input :show-input-controls="false" input-size="mini" :min="0" :max="90" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.externalRatation')" class="form-item">
          <el-slider v-model="rotateForm.beta" show-input :show-input-controls="false" input-size="mini" :min="-45" :max="45" @change="changeFocusCase" />
        </el-form-item>
        <el-form-item :label="$t('chart.depth')" class="form-item">
          <el-slider v-model="rotateForm.depth" show-input :show-input-controls="false" input-size="mini" :min="20" :max="100" @change="changeFocusCase" />
        </el-form-item>

        <el-form-item v-if="chart.type && chart.type.includes('3dpie')" :label="$t('chart.pie_inner_radius')" class="form-item form-item-slider">
          <el-slider v-model="rotateForm.pieInnerRadius" show-input :show-input-controls="false" input-size="mini" :min="0" :max="100" @change="changeFocusCase" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>
<script>
import {COLOR_PANEL, DEFAULT_SIZE } from '../../chart/chart'
export default {
  name: 'RotateSelector',
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
      rotateForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
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
        if (customAttr.size) {
          this.rotateForm = customAttr.size
        }
      }
    },
    
    changeFocusCase() {
      this.$emit('onSizeChange', this.rotateForm)
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