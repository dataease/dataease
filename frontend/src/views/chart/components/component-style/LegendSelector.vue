<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="legendForm" :model="legendForm" label-width="80px" size="mini" :disabled="!hasDataPermission('manage',param.privileges)">
        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="legendForm.show" @change="changeLegendStyle">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="legendForm.show">
          <el-form-item :label="$t('chart.icon')" class="form-item">
            <el-select v-model="legendForm.icon" :placeholder="$t('chart.icon')" @change="changeLegendStyle">
              <el-option
                v-for="item in iconSymbolOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.orient')" class="form-item">
            <el-radio-group v-model="legendForm.orient" size="mini" @change="changeLegendStyle">
              <el-radio-button label="horizontal">{{ $t('chart.horizontal') }}</el-radio-button>
              <el-radio-button label="vertical">{{ $t('chart.vertical') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="legendForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLegendStyle">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="legendForm.textStyle.color" class="color-picker-style" @change="changeLegendStyle" />
          </el-form-item>
          <el-form-item :label="$t('chart.text_h_position')" class="form-item">
            <el-radio-group v-model="legendForm.hPosition" size="mini" @change="changeLegendStyle">
              <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('chart.text_v_position')" class="form-item">
            <el-radio-group v-model="legendForm.vPosition" size="mini" @change="changeLegendStyle">
              <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
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
    <!--          <el-form ref="legendForm" :model="legendForm" label-width="80px" size="mini">-->
    <!--            &lt;!&ndash;            <el-form-item :label="$t('chart.show')" class="form-item">&ndash;&gt;-->
    <!--            &lt;!&ndash;              <el-checkbox v-model="legendForm.show" @change="changeLegendStyle">{{ $t('chart.show') }}</el-checkbox>&ndash;&gt;-->
    <!--            &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--            <el-form-item :label="$t('chart.icon')" class="form-item">-->
    <!--              <el-select v-model="legendForm.icon" :placeholder="$t('chart.icon')" @change="changeLegendStyle">-->
    <!--                <el-option-->
    <!--                  v-for="item in iconSymbolOptions"-->
    <!--                  :key="item.value"-->
    <!--                  :label="item.name"-->
    <!--                  :value="item.value"-->
    <!--                />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.orient')" class="form-item">-->
    <!--              <el-radio-group v-model="legendForm.orient" size="mini" @change="changeLegendStyle">-->
    <!--                <el-radio-button label="horizontal">{{ $t('chart.horizontal') }}</el-radio-button>-->
    <!--                <el-radio-button label="vertical">{{ $t('chart.vertical') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_fontsize')" class="form-item">-->
    <!--              <el-select v-model="legendForm.textStyle.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini" @change="changeLegendStyle">-->
    <!--                <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value" />-->
    <!--              </el-select>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_color')" class="form-item">-->
    <!--              <colorPicker v-model="legendForm.textStyle.color" style="margin-top: 6px;cursor: pointer;z-index: 999;border: solid 1px black" @change="changeLegendStyle" />-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_h_position')" class="form-item">-->
    <!--              <el-radio-group v-model="legendForm.hPosition" size="mini" @change="changeLegendStyle">-->
    <!--                <el-radio-button label="left">{{ $t('chart.text_pos_left') }}</el-radio-button>-->
    <!--                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>-->
    <!--                <el-radio-button label="right">{{ $t('chart.text_pos_right') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--            <el-form-item :label="$t('chart.text_v_position')" class="form-item">-->
    <!--              <el-radio-group v-model="legendForm.vPosition" size="mini" @change="changeLegendStyle">-->
    <!--                <el-radio-button label="top">{{ $t('chart.text_pos_top') }}</el-radio-button>-->
    <!--                <el-radio-button label="center">{{ $t('chart.text_pos_center') }}</el-radio-button>-->
    <!--                <el-radio-button label="bottom">{{ $t('chart.text_pos_bottom') }}</el-radio-button>-->
    <!--              </el-radio-group>-->
    <!--            </el-form-item>-->
    <!--          </el-form>-->
    <!--        </el-col>-->

    <!--        <el-button slot="reference" size="mini" class="shape-item" :disabled="!legendForm.show || !hasDataPermission('manage',param.privileges)">-->
    <!--          {{ $t('chart.legend') }}<i class="el-icon-setting el-icon&#45;&#45;right" />-->
    <!--          <el-switch-->
    <!--            v-model="legendForm.show"-->
    <!--            :disabled="!hasDataPermission('manage',param.privileges)"-->
    <!--            class="switch-style"-->
    <!--            @click.stop.native-->
    <!--            @change="changeLegendStyle"-->
    <!--          />-->
    <!--        </el-button>-->
    <!--      </el-popover>-->
    <!--    </div>-->
  </div>
</template>

<script>
import { DEFAULT_LEGEND_STYLE } from '../../chart/chart'

export default {
  name: 'LegendSelector',
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
      legendForm: JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE)),
      fontSize: [],
      iconSymbolOptions: [
        { name: this.$t('chart.line_symbol_emptyCircle'), value: 'emptyCircle' },
        { name: this.$t('chart.line_symbol_circle'), value: 'circle' },
        { name: this.$t('chart.line_symbol_rect'), value: 'rect' },
        { name: this.$t('chart.line_symbol_roundRect'), value: 'roundRect' },
        { name: this.$t('chart.line_symbol_triangle'), value: 'triangle' },
        { name: this.$t('chart.line_symbol_diamond'), value: 'diamond' }
      ],
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
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.legend) {
          this.legendForm = customStyle.legend
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
    changeLegendStyle() {
      if (!this.legendForm.show) {
        this.isSetting = false
      }
      this.$emit('onLegendChange', this.legendForm)
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
