<template>
  <div style="width: 100%;">
    <el-col>
      <el-form v-show="chart.type" ref="labelForm" :model="labelForm" label-width="80px" size="mini">
<!--        <el-form-item :label="$t('chart.show')" class="form-item">
          <el-checkbox v-model="labelForm.show" @change="changeAttr">{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="labelForm.show">
          <el-form-item :label="$t('chart.text_fontsize')" class="form-item">
            <el-select v-model="labelForm.fontSize" :placeholder="$t('chart.text_fontsize')" size="mini"
                       @change="changeAttr">
              <el-option v-for="option in fontSize" :key="option.value" :label="option.name" :value="option.value"/>
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('chart.text_color')" class="form-item">
            <el-color-picker v-model="labelForm.color" class="color-picker-style" :predefine="predefineColors"
                             @change="changeAttr"/>
          </el-form-item>
        </div>-->
<!--        <el-form-item :label="$t('plugin_view_racebar.slider_auto')" class="form-item">
          <el-checkbox v-model="labelForm.auto" @change="changeAttr">{{
              $t('plugin_view_racebar.slider_auto')
            }}
          </el-checkbox>
        </el-form-item>-->
        <el-form-item :label="$t('plugin_view_racebar.slider_timeout')" class="form-item">
          <el-input-number :min="1" :step="1" v-model="labelForm.timeout" @change="changeAttr">{{
              $t('plugin_view_racebar.slider_timeout')
            }}
          </el-input-number>
          s
        </el-form-item>
        <el-form-item :label="$t('plugin_view_racebar.slider_repeat')" class="form-item">
          <el-checkbox v-model="labelForm.repeat" @change="changeAttr">{{
              $t('plugin_view_racebar.slider_repeat')
            }}
          </el-checkbox>
        </el-form-item>
        <el-form-item :label="$t('plugin_view_racebar.slider_max')" class="form-item">
          <el-input-number :min="3" :step="1" v-model="labelForm.max" @change="changeAttr" />
        </el-form-item>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import {DEFAULT_SLIDER, COLOR_PANEL} from '../../utils/map'

export default {
  name: 'SliderSetting',
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
      labelForm: JSON.parse(JSON.stringify(DEFAULT_SLIDER)),
      isSetting: false,
      predefineColors: COLOR_PANEL,
      fontSize: [],
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
        if (customAttr.slider) {
          this.labelForm = customAttr.slider
        } else {
          this.labelForm = JSON.parse(JSON.stringify(DEFAULT_SLIDER))
        }
      }
    },
    init() {
      const arr = []
      for (let i = 10; i <= 40; i = i + 2) {
        arr.push({
          name: i + '',
          value: i + ''
        })
      }
      this.fontSize = arr
    },
    changeAttr() {
      if (!this.labelForm.show) {
        this.isSetting = false
      }
      this.$emit('onChange', this.labelForm)
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

.el-select-dropdown__item {
  padding: 0 20px;
}

span {
  font-size: 12px
}

.el-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
</style>
