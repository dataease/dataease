<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="tooltipForm" :model="tooltipForm" label-width="80px" size="mini" :disabled="!hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="tooltipForm.show" @change="changeTooltipAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="tooltipForm.show">
          <el-form-item :label="$t('chart.trigger_position')" class="form-item">
            <el-radio-group v-model="tooltipForm.trigger" size="mini" @change="changeTooltipAttr">
              <el-radio-button label="item">{{ $t('chart.tooltip_item') }}</el-radio-button>
              <el-radio-button label="axis">{{ $t('chart.tooltip_axis') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="tooltipForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTooltipAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="tooltipForm.textStyle.color" class="color-picker-style" @change="changeTooltipAttr" />
          </el-form-item>
          <el-form-item class="form-item">
            <span slot="label">
              <span class="span-box">
                <span>{{ $t('chart.content_formatter') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    模板变量有 {a}, {b}，{c}，{d}，分别表示系列名，数据名，数据值等。
                    <br>
                    在 触发位置 为 '坐标轴' 的时候，会有多个系列的数据，此时可以通过 {a0}, {a1}, {a2} 这种后面加索引的方式表示系列的索引。
                    <br>
                    不同图表类型下的 {a}，{b}，{c}，{d} 含义不一样。 其中变量{a}, {b}, {c}, {d}在不同图表类型下代表数据含义为：
                    <br><br>
                    折线（区域）图、柱状（条形）图、仪表盘 : {a}（系列名称），{b}（类目值），{c}（数值）
                    <br>
                    <!--                    散点图（气泡）图 : {a}（系列名称），{b}（数据名称），{c}（数值数组）, {d}（无）-->
                    <!--                    <br>-->
                    <!--                    地图 : {a}（系列名称），{b}（区域名称），{c}（合并数值）, {d}（无）-->
                    <!--                    <br>-->
                    饼图、漏斗图: {a}（系列名称），{b}（数据项名称），{c}（数值）, {d}（百分比）
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </span>
            <el-input v-model="tooltipForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" :placeholder="$t('chart.formatter_plc')" @blur="changeTooltipAttr" />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
    <!--    <div style="width: 100%">-->
    <!--      <el-popover-->
    <!--        v-model="isSetting"-->
    <!--        placement="right"-->
    <!--        width="400"-->
    <!--        trigger="click"-->
    <!--      >-->
    <!--        <el-col>-->
    <!--          <el-form ref="tooltipForm" :model="tooltipForm" label-width="80px" size="mini">-->
    <!--            &lt;!&ndash;            <el-form-item :label="$t('chart.show')" class="form-item">&ndash;&gt;-->
    <!--            &lt;!&ndash;              <el-checkbox v-model="tooltipForm.show" @change="changeTooltipAttr">{{ $t('chart.show') }}</el-checkbox>&ndash;&gt;-->
    <!--            &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--            <el-form-item :label="$t('chart.trigger_position')" class="form-item">-->
    <!--              <el-radio-group v-model="tooltipForm.trigger" size="mini" @change="changeTooltipAttr">-->
    <!--                <el-radio-button label="item">{{ $t('chart.tooltip_item') }}</el-radio-button>-->
    <!--                <el-radio-button label="axis">{{ $t('chart.tooltip_axis') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">-->
    <!--              <el-select v-model="tooltipForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeTooltipAttr">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_color')" class="form-item">-->
    <!--              <colorPicker v-model="tooltipForm.textStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeTooltipAttr" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item class="form-item">-->
    <!--              <span slot="label">-->
    <!--                <span class="span-box">-->
    <!--                  <span>{{ $t('chart.content_formatter') }}</span>-->
    <!--                  <el-tooltip class="item" effect="dark" placement="bottom">-->
    <!--                    <div slot="content">-->
    <!--                      模板变量有 {a}, {b}，{c}，{d}，{e}，分别表示系列名，数据名，数据值等。-->
    <!--                      <br>-->
    <!--                      在 trigger 为 'axis' 的时候，会有多个系列的数据，此时可以通过 {a0}, {a1}, {a2} 这种后面加索引的方式表示系列的索引。-->
    <!--                      <br>-->
    <!--                      不同图表类型下的 {a}，{b}，{c}，{d} 含义不一样。 其中变量{a}, {b}, {c}, {d}在不同图表类型下代表数据含义为：-->
    <!--                      <br><br>-->
    <!--                      折线（区域）图、柱状（条形）图、K线图 : {a}（系列名称），{b}（类目值），{c}（数值）, {d}（无）-->
    <!--                      <br>-->
    <!--                      散点图（气泡）图 : {a}（系列名称），{b}（数据名称），{c}（数值数组）, {d}（无）-->
    <!--                      <br>-->
    <!--                      地图 : {a}（系列名称），{b}（区域名称），{c}（合并数值）, {d}（无）-->
    <!--                      <br>-->
    <!--                      饼图、仪表板、漏斗图: {a}（系列名称），{b}（数据项名称），{c}（数值）, {d}（百分比）-->
    <!--                    </div>-->
    <!--                    <i class="el-icon-info" style="cursor: pointer;" />-->
    <!--                  </el-tooltip>-->
    <!--                </span>-->
    <!--              </span>-->
    <!--              <el-input v-model="tooltipForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" :placeholder="$t('chart.formatter_plc')" @blur="changeTooltipAttr" />-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" size="mini" class="shape-item" :disabled="!tooltipForm.show || !hasDataPermission('manage',param.privileges)">-->
    <!--          {{ $t('chart.tooltip') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--          <el-switch-->
    <!--            v-model="tooltipForm.show"-->
    <!--            :disabled="!hasDataPermission('manage',param.privileges)"-->
    <!--            class="switch-style"-->
    <!--            @click.stop.native-->
    <!--            @change="changeTooltipAttr"-->
    <!--          />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_TOOLTIP } from '../../chart/chart'

export default {
  name: 'TooltipSelector',
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
      tooltipForm: JSON.parse(JSON.stringify(DEFAULT_TOOLTIP)),
      fontSize: [],
      isSetting: false
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
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.tooltip) {
          this.tooltipForm = customAttr.tooltip
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 20; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeTooltipAttr() {
      if (!this.tooltipForm.show) {
        this.isSetting = false
      }
      this.$emit('onTooltipChange', this.tooltipForm)
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

.switch-style{
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style{
  cursor: pointer;
  z-index: 1003;
}
</style>
