<template>
  <div style="width: 100%;">
    <el-col>
      <el-form v-show="chart.type && !chart.type.includes('gauge')" ref="labelForm" :disabled="!hasDataPermission('manage',param.privileges)" :model="labelForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          <el-form-item v-show="chart.type && chart.type.includes('pie')" :label="$t('chart.pie_label_line_show')" class="form-item">
            <el-checkbox v-model="labelForm.labelLine.show" @change="changeLabelAttr">{{ $t('chart.pie_label_line_show') }}</el-checkbox>
          </el-form-item>
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" @change="changeLabelAttr" />
          </el-form-item>
          <el-form-item :label="$t('chart.label_position')" class="form-item">
            <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr">
              <el-option v-for="option in labelPosition" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item">
            <span slot="label">
              <span class="span-box">
                <span>{{ $t('chart.content_formatter') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    字符串模板 模板变量有：<br>{a}：系列名。<br>{b}：数据名。<br>{c}：数据值。<br>{d}：百分比（用于饼图等）。
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </span>
            <el-input v-model="labelForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />
          </el-form-item>
        </div>
      </el-form>

      <el-form v-show="chart.type && chart.type.includes('gauge')" ref="labelForm" :disabled="!hasDataPermission('manage',param.privileges)" :model="labelForm" label-width="80px" size="mini">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
          <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">
            <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('chart.text_color')" class="form-item">
          <el-color-picker v-model="labelForm.color" class="color-picker-style" @change="changeLabelAttr" />
        </el-form-item>
        <el-form-item class="form-item">
          <span slot="label">
            <span class="span-box">
              <span>{{ $t('chart.content_formatter') }}</span>
            </span>
          </span>
          <el-input v-model="labelForm.gaugeFormatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />
        </el-form-item>
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
    <!--          <el-form v-show="chart.type && !chart.type.includes('gauge')" ref="labelForm" :model="labelForm" label-width="80px" size="mini">-->
    <!--            &lt;!&ndash;            <el-form-item :label="$t('chart.show')" class="form-item">&ndash;&gt;-->
    <!--            &lt;!&ndash;              <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>&ndash;&gt;-->
    <!--            &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--            <el-form-item :label="$t('chart.pie_label_line_show')" class="form-item">-->
    <!--              <el-checkbox v-model="labelForm.labelLine.show" @change="changeLabelAttr">{{ $t('chart.pie_label_line_show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">-->
    <!--              <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_color')" class="form-item">-->
    <!--              <colorPicker v-model="labelForm.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeLabelAttr" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.label_position')" class="form-item">-->
    <!--              <el-select v-model="labelForm.position" :placeholder="$t('chart.label_position')" @change="changeLabelAttr">-->
    <!--                <el-option v-for="option in labelPosition" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item class="form-item">-->
    <!--              <span slot="label">-->
    <!--                <span class="span-box">-->
    <!--                  <span>{{ $t('chart.content_formatter') }}</span>-->
    <!--                  <el-tooltip class="item" effect="dark" placement="bottom">-->
    <!--                    <div slot="content">-->
    <!--                      字符串模板 模板变量有：<br>{a}：系列名。<br>{b}：数据名。<br>{c}：数据值。<br>{d}：百分比（用于饼图等）。-->
    <!--                    </div>-->
    <!--                    <i class="el-icon-info" style="cursor: pointer;" />-->
    <!--                  </el-tooltip>-->
    <!--                </span>-->
    <!--              </span>-->
    <!--              <el-input v-model="labelForm.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->

    <!--          <el-form v-show="chart.type && chart.type.includes('gauge')" ref="labelForm" :model="labelForm" label-width="80px" size="mini">-->
    <!--            <el-form-item :label="$t('chart.show')" class="form-item">-->
    <!--              <el-checkbox v-model="labelForm.show" @change="changeLabelAttr">{{ $t('chart.show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">-->
    <!--              <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLabelAttr">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_color')" class="form-item">-->
    <!--              <colorPicker v-model="labelForm.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeLabelAttr" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item class="form-item">-->
    <!--              <span slot="label">-->
    <!--                <span class="span-box">-->
    <!--                  <span>{{ $t('chart.content_formatter') }}</span>-->
    <!--                </span>-->
    <!--              </span>-->
    <!--              <el-input v-model="labelForm.gaugeFormatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeLabelAttr" />-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" size="mini" class="shape-item" :disabled="!labelForm.show || !hasDataPermission('manage',param.privileges)">-->
    <!--          {{ $t('chart.label') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--          <el-switch-->
    <!--            v-model="labelForm.show"-->
    <!--            :disabled="!hasDataPermission('manage',param.privileges)"-->
    <!--            class="switch-style"-->
    <!--            @click.stop.native-->
    <!--            @change="changeLabelAttr"-->
    <!--          />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_LABEL } from '../../chart/chart'

export default {
  name: 'LabelSelector',
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
      labelForm: JSON.parse(JSON.stringify(DEFAULT_LABEL)),
      fontSize: [],
      isSetting: false,
      labelPosition: [
        { name: this.$t('chart.inside'), value: 'inside' },
        { name: this.$t('chart.outside'), value: 'outside' },
        { name: this.$t('chart.center'), value: 'center' },
        { name: this.$t('chart.text_pos_top'), value: 'top' },
        { name: this.$t('chart.text_pos_bottom'), value: 'bottom' },
        { name: this.$t('chart.text_pos_left'), value: 'left' },
        { name: this.$t('chart.text_pos_right'), value: 'right' }
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
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.label) {
          this.labelForm = customAttr.label
          if (!this.labelForm.labelLine) {
            this.labelForm.labelLine = JSON.parse(JSON.stringify(DEFAULT_LABEL.labelLine))
          }
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
    changeLabelAttr() {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.$emit('onLabelChange', this.labelForm)
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
