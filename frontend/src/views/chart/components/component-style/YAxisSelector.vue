<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="axisForm" :model="axisForm" label-width="80px" size="mini" :disabled="!hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="axisForm.show" @change="changeYAxisStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="axisForm.show">
          <el-form-item :label="$t('chart.position')" class="form-item">
            <el-radio-group v-model="axisForm.position" size="mini" @change="changeYAxisStyle">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.name')" class="form-item">
            <el-input v-model="axisForm.name" size="mini" @blur="changeYAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_name_color')" class="form-item">
            <el-color-picker v-model="axisForm.nameTextStyle.color" class="color-picker-style" @change="changeYAxisStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.axis_name_fontsize')" class="form-item">
            <el-select v-model="axisForm.nameTextStyle.fontSize" :placeholder="$t('chart.axis_name_fontsize')" @change="changeYAxisStyle">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-divider />
          <el-form-item :label="$t('chart.axis_show')" class="form-item">
            <el-checkbox v-model="axisForm.splitLine.show" @change="changeYAxisStyle">{{ $t('chart.axis_show') }}</el-checkbox>
          </el-form-item>
          <span v-show="axisForm.splitLine.show">
            <el-form-item :label="$t('chart.axis_color')" class="form-item">
              <el-color-picker v-model="axisForm.splitLine.lineStyle.color" class="el-color-picker" @change="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_width')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.splitLine.lineStyle.width" :min="1" :max="10" show-input :show-input-controls="false" input-size="mini" @change="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_type')" class="form-item">
              <el-radio-group v-model="axisForm.splitLine.lineStyle.type" size="mini" @change="changeYAxisStyle">
                <el-radio-button label="solid">{{ $t('chart.axis_type_solid') }}</el-radio-button>
                <el-radio-button label="dashed">{{ $t('chart.axis_type_dashed') }}</el-radio-button>
                <el-radio-button label="dotted">{{ $t('chart.axis_type_dotted') }}</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </span>
          <el-divider />
          <el-form-item :label="$t('chart.axis_label_show')" class="form-item">
            <el-checkbox v-model="axisForm.axisLabel.show" @change="changeYAxisStyle">{{ $t('chart.axis_label_show') }}</el-checkbox>
          </el-form-item>
          <span v-show="axisForm.axisLabel.show">
            <el-form-item :label="$t('chart.axis_label_color')" class="form-item">
              <el-color-picker v-model="axisForm.axisLabel.color" class="el-color-picker" @change="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_label_rotate')" class="form-item form-item-slider">
              <el-slider v-model="axisForm.axisLabel.rotate" show-input :show-input-controls="false" :min="-90" :max="90" input-size="mini" @change="changeYAxisStyle" />
            </el-form-item>
            <el-form-item :label="$t('chart.axis_label_fontsize')" class="form-item">
              <el-select v-model="axisForm.axisLabel.fontSize" :placeholder="$t('chart.axis_label_fontsize')" @change="changeYAxisStyle">
                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
              </el-select>
            </el-form-item>
          </span>
          <el-divider />
          <el-form-item :label="$t('chart.content_formatter')" class="form-item">
            <el-input v-model="axisForm.axisLabel.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeYAxisStyle" />
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
    <!--          <el-form ref="axisForm" :model="axisForm" label-width="80px" size="mini">-->
    <!--            &lt;!&ndash;            <el-form-item :label="$t('chart.show')" class="form-item">&ndash;&gt;-->
    <!--            &lt;!&ndash;              <el-checkbox v-model="axisForm.show" @change="changeYAxisStyle">{{ $t('chart.show') }}</el-checkbox>&ndash;&gt;-->
    <!--            &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--            <el-form-item :label="$t('chart.position')" class="form-item">-->
    <!--              <el-radio-group v-model="axisForm.position" size="mini" @change="changeYAxisStyle">-->
    <!--                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>-->
    <!--                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.name')" class="form-item">-->
    <!--              <el-input v-model="axisForm.name" size="mini" @blur="changeYAxisStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.rotate')" class="form-item form-item-slider">-->
    <!--              <el-slider v-model="axisForm.axisLabel.rotate" show-input :show-input-controls="false" :min="-90" :max="90" input-size="mini" @change="changeYAxisStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_name_color')" class="form-item">-->
    <!--              <colorPicker v-model="axisForm.nameTextStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeYAxisStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.axis_name_fontsize')" class="form-item form-item-slider">-->
    <!--              <el-select v-model="axisForm.nameTextStyle.fontSize" :placeholder="$t('chart.axis_name_fontsize')" @change="changeYAxisStyle">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-divider />-->
    <!--            <el-form-item :label="$t('chart.axis_show')" class="form-item">-->
    <!--              <el-checkbox v-model="axisForm.splitLine.show" @change="changeYAxisStyle">{{ $t('chart.axis_show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <span v-show="axisForm.splitLine.show">-->
    <!--              <el-form-item :label="$t('chart.axis_color')" class="form-item">-->
    <!--                <colorPicker v-model="axisForm.splitLine.lineStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeYAxisStyle" />-->
    <!--              </el-form-item>-->
    <!--              <el-form-item :label="$t('chart.axis_width')" class="form-item form-item-slider">-->
    <!--                <el-slider v-model="axisForm.splitLine.lineStyle.width" :min="1" :max="10" show-input :show-input-controls="false" input-size="mini" @change="changeYAxisStyle" />-->
    <!--              </el-form-item>-->
    <!--              <el-form-item :label="$t('chart.axis_type')" class="form-item">-->
    <!--                <el-radio-group v-model="axisForm.splitLine.lineStyle.type" size="mini" @change="changeYAxisStyle">-->
    <!--                  <el-radio-button label="solid">{{ $t('chart.axis_type_solid') }}</el-radio-button>-->
    <!--                  <el-radio-button label="dashed">{{ $t('chart.axis_type_dashed') }}</el-radio-button>-->
    <!--                  <el-radio-button label="dotted">{{ $t('chart.axis_type_dotted') }}</el-radio-button>-->
    <!--                </el-radio-group>-->
    <!--              </el-form-item>-->
    <!--            </span>-->
    <!--            <el-divider />-->
    <!--            <el-form-item :label="$t('chart.axis_label_show')" class="form-item">-->
    <!--              <el-checkbox v-model="axisForm.axisLabel.show" @change="changeYAxisStyle">{{ $t('chart.axis_label_show') }}</el-checkbox>-->
    <!--            </el-form-item>-->
    <!--            <span v-show="axisForm.axisLabel.show">-->
    <!--              <el-form-item :label="$t('chart.axis_label_color')" class="form-item">-->
    <!--                <colorPicker v-model="axisForm.axisLabel.color" style="margin-top: 6px;cursor: pointer;z-index: 1004;border: solid 1px black" @change="changeYAxisStyle" />-->
    <!--              </el-form-item>-->
    <!--              <el-form-item :label="$t('chart.axis_label_fontsize')" class="form-item form-item-slider">-->
    <!--                <el-select v-model="axisForm.axisLabel.fontSize" :placeholder="$t('chart.axis_label_fontsize')" @change="changeYAxisStyle">-->
    <!--                  <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--                </el-select>-->
    <!--              </el-form-item>-->
    <!--            </span>-->
    <!--            <el-divider />-->
    <!--            <el-form-item :label="$t('chart.content_formatter')" class="form-item">-->
    <!--              <el-input v-model="axisForm.axisLabel.formatter" type="textarea" :autosize="{ minRows: 4, maxRows: 4}" @blur="changeYAxisStyle" />-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" size="mini" class="shape-item" :disabled="!axisForm.show || !hasDataPermission('manage',param.privileges)">-->
    <!--          {{ $t('chart.yAxis') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--          <el-switch-->
    <!--            v-model="axisForm.show"-->
    <!--            :disabled="!hasDataPermission('manage',param.privileges)"-->
    <!--            class="switch-style"-->
    <!--            @click.stop.native-->
    <!--            @change="changeYAxisStyle"-->
    <!--          />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_YAXIS_STYLE } from '../../chart/chart'

export default {
  name: 'YAxisSelector',
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
      axisForm: JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE)),
      isSetting: false,
      fontSize: []
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
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.yAxis) {
          this.axisForm = customStyle.yAxis
          if (!this.axisForm.splitLine) {
            this.axisForm.splitLine = JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE.splitLine))
          }
          if (!this.axisForm.nameTextStyle) {
            this.axisForm.nameTextStyle = JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE.nameTextStyle))
          }
        }
      }
    },
    init() {
      const arr = []
      for (let i = 6; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeYAxisStyle() {
      if (!this.axisForm.show) {
        this.isSetting = false
      }
      this.$emit('onChangeYAxisForm', this.axisForm)
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 10px 0
  }
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
