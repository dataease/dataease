<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="sizeFormBar"
        :model="sizeForm"
        label-width="80px"
        size="mini"
      >

        <el-divider
          content-position="center"
          class="divider-style"
        >{{ $t('chart.chart_bar') }}
        </el-divider>
        <el-form-item
          :label="$t('chart.adapt')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.barDefault"
            @change="changeBarSizeCase('barDefault')"
          >{{ $t('chart.adapt') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          :label="$t('chart.bar_width')"
          class="form-item form-item-slider"
        >
          <el-row>
            <el-col :span="22">
              <el-slider
                v-model="sizeForm.barWidthPercent"
                :disabled="sizeForm.barDefault"
                show-input
                :show-input-controls="false"
                input-size="mini"
                :min="1"
                :max="100"
                @change="changeBarSizeCase('barWidthPercent')"
              />
            </el-col>
            <el-col :span="2">
              <div style="height: 38px;display: flex;align-items: center;">
                %
              </div>
            </el-col>
          </el-row>
        </el-form-item>

        <!--line-begin-->
        <el-divider
          content-position="center"
          class="divider-style"
        >{{ $t('chart.chart_line') }}
        </el-divider>
        <el-form-item
          :label="$t('chart.line_width')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineWidth"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="10"
            @change="changeBarSizeCase('lineWidth')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('chart.line_symbol')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.lineSymbol"
            :placeholder="$t('chart.line_symbol')"
            @change="changeBarSizeCase('lineSymbol')"
          >
            <el-option
              v-for="item in lineSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('chart.line_symbol_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.lineSymbolSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="0"
            :max="20"
            @change="changeBarSizeCase('lineSymbolSize')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('chart.line_smooth')"
          class="form-item"
        >
          <el-checkbox
            v-model="sizeForm.lineSmooth"
            @change="changeBarSizeCase('lineSmooth')"
          >{{ $t('chart.line_smooth') }}
          </el-checkbox>
        </el-form-item>
        <!--line-end-->

        <!--scatter-begin-->
        <el-divider
          content-position="center"
          class="divider-style"
        >{{ $t('chart.chart_scatter') }}
        </el-divider>
        <el-form-item
          :label="$t('chart.bubble_symbol')"
          class="form-item"
        >
          <el-select
            v-model="sizeForm.scatterSymbol"
            :placeholder="$t('chart.line_symbol')"
            @change="changeBarSizeCase('scatterSymbol')"
          >
            <el-option
              v-for="item in lineSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('chart.bubble_size')"
          class="form-item form-item-slider"
        >
          <el-slider
            v-model="sizeForm.scatterSymbolSize"
            show-input
            :show-input-controls="false"
            input-size="mini"
            :min="1"
            :max="40"
            @change="changeBarSizeCase('scatterSymbolSize')"
          />
        </el-form-item>
        <!--scatter-end-->

      </el-form>
    </el-col>
  </div>
</template>

<script>
import {CHART_FONT_FAMILY, CHART_FONT_LETTER_SPACE, DEFAULT_SIZE} from '../../utils/map'

export default {
  name: 'SizeSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
  },
  data() {
    return {
      sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
      lineSymbolOptions: [
        // { name: this.$t('chart.line_symbol_none'), value: 'none' },
        {name: this.$t('chart.line_symbol_circle'), value: 'circle'},
        {name: this.$t('chart.line_symbol_rect'), value: 'square'},
        {name: this.$t('chart.line_symbol_triangle'), value: 'triangle'},
        {name: this.$t('chart.line_symbol_diamond'), value: 'diamond'}
      ],
      fontSize: [],
      fontFamily: CHART_FONT_FAMILY,
      fontLetterSpace: CHART_FONT_LETTER_SPACE
    }
  },
  watch: {
    'chart': {
      handler: function () {
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
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.size) {
          this.sizeForm = customAttr.size

          this.sizeForm.barDefault = this.sizeForm.barDefault !== undefined ? this.sizeForm.barDefault : DEFAULT_SIZE.barDefault;
          this.sizeForm.barWidthPercent = this.sizeForm.barWidthPercent !== undefined ? this.sizeForm.barWidthPercent : DEFAULT_SIZE.barWidthPercent;

          this.sizeForm.lineWidth = this.sizeForm.lineWidth !== undefined ? this.sizeForm.lineWidth : DEFAULT_SIZE.lineWidth;
          this.sizeForm.lineSymbol = this.sizeForm.lineSymbol !== undefined ? this.sizeForm.lineSymbol : DEFAULT_SIZE.lineSymbol;
          this.sizeForm.lineSymbolSize = this.sizeForm.lineSymbolSize !== undefined ? this.sizeForm.lineSymbolSize : DEFAULT_SIZE.lineSymbolSize;
          this.sizeForm.lineSmooth = this.sizeForm.lineSmooth !== undefined ? this.sizeForm.lineSmooth : DEFAULT_SIZE.lineSmooth;

          this.sizeForm.scatterSymbol = this.sizeForm.scatterSymbol !== undefined ? this.sizeForm.scatterSymbol : DEFAULT_SIZE.scatterSymbol;
          this.sizeForm.scatterSymbolSize = this.sizeForm.scatterSymbolSize !== undefined ? this.sizeForm.scatterSymbolSize : DEFAULT_SIZE.scatterSymbolSize;

        }
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
    changeBarSizeCase(modifyName) {
      this.$emit('onSizeChange', this.sizeForm)
    },
  }
}
</script>

<style scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.form-item ::v-deep .el-checkbox__label {
  font-size: 12px;
}

.form-item ::v-deep .el-radio__label {
  font-size: 12px;
}

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.el-divider--horizontal {
  margin: 10px 0
}

.divider-style ::v-deep .el-divider__text {
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}
</style>
