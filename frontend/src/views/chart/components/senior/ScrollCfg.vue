<template>
  <div style="width: 100%;display: block;">
    <el-row class="scroll-style">
      <el-form
        ref="scrollForm"
        :model="scrollForm"
        label-width="80px"
        size="mini"
      >
        <el-form-item
          :label="$t('chart.scroll')"
          class="form-item"
        >
          <el-checkbox
            v-model="scrollForm.open"
            @change="changeScrollCfg"
          >{{ $t('chart.open') }}</el-checkbox>
          <el-tooltip
            class="item"
            effect="dark"
            placement="bottom"
          >
            <div slot="content">
              {{ $t('chart.table_scroll_tip') }}
            </div>
            <i
              class="el-icon-info"
              style="cursor: pointer;color: gray;font-size: 12px;"
            />
          </el-tooltip>
        </el-form-item>
        <span v-show="scrollForm.open">
          <el-form-item
            v-show="!isAutoBreakLine"
            :label="$t('chart.row')"
            class="form-item"
          >
            <el-input-number
              v-model="scrollForm.row"
              :min="1"
              :max="1000"
              :precision="0"
              size="mini"
              @change="changeScrollCfg"
            />
          </el-form-item>
          <el-form-item
            v-show="isAutoBreakLine"
            :label="$t('chart.step')"
            class="form-item"
          >
            <el-input-number
              v-model="scrollForm.step"
              :min="1"
              :max="10000"
              :precision="0"
              size="mini"
              @change="changeScrollCfg"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.interval') + '(ms)'"
            class="form-item"
          >
            <el-input-number
              v-model="scrollForm.interval"
              :min="500"
              :step="1000"
              :precision="0"
              size="mini"
              @change="changeScrollCfg"
            />
          </el-form-item>
        </span>
      </el-form>
    </el-row>
  </div>
</template>

<script>
import { DEFAULT_SCROLL, DEFAULT_SIZE } from '@/views/chart/chart/chart'

export default {
  name: 'ScrollCfg',
  props: {
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      scrollForm: JSON.parse(JSON.stringify(DEFAULT_SCROLL)),
      isAutoBreakLine: false
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
      if (chart.senior) {
        let senior = null
        if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
          senior = JSON.parse(JSON.stringify(chart.senior))
        } else {
          senior = JSON.parse(chart.senior)
        }
        if (senior.scrollCfg) {
          this.scrollForm = senior.scrollCfg
          this.scrollForm.step = senior.scrollCfg.step ? senior.scrollCfg.step : DEFAULT_SCROLL.step
        } else {
          this.scrollForm = JSON.parse(JSON.stringify(DEFAULT_SCROLL))
        }
      }
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.size) {
          if (this.chart.render === 'antv') {
            this.isAutoBreakLine = false
          } else {
            this.isAutoBreakLine = customAttr.size.tableAutoBreakLine ? customAttr.size.tableAutoBreakLine : DEFAULT_SIZE.tableAutoBreakLine
          }
        }
      }
    },
    changeScrollCfg() {
      this.$emit('onScrollCfgChange', this.scrollForm)
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

.scroll-style ::v-deep .el-input-number--mini {
  width: 120px !important;
}
</style>
