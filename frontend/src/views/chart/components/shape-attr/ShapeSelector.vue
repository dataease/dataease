<template>
  <div style="width: 100%">
    <el-col>
      <!-- 词云组件字体大小设置 -->
      <el-form v-show="chart.type && chart.type === 'word-cloud'" ref="shapeFormWord" :model="shapeForm" label-width="100px" size="mini">
        <el-form-item :label="$t('chart.wordShape')" class="form-item">
          <el-select v-model="shapeForm.wordShape" placeholder="请选择" @change="changeShapeCase">
            <el-option v-for="option in shapeData" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>
<script>
import {COLOR_PANEL, DEFAULT_SIZE } from '../../chart/chart'
export default {
  name: 'ShapeSelector',
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
      shapeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      predefineColors: COLOR_PANEL,
      shapeData: [
        {name: '圆形',value: 'circle'},
        {name: '心形',value: 'cardioid'},
        {name: '菱形',value: 'diamond'},
        {name: '三角形',value: 'triangle'},
        {name: '星形',value: 'star'},
        {name: '五边形',value: 'pentagon'},
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
        if (customAttr.size) {
          this.shapeForm = customAttr.size
        }
      }
    },
    
    changeShapeCase() {
      this.$emit('onSizeChange', this.shapeForm)
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