<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="marginForm" :model="marginForm" label-width="80px" size="mini" :rules="rules">
        <el-form-item v-show="showProperty('marginModel')" :label="$t('chart.margin_model')" class="form-item">
          <el-radio-group v-model="marginForm.marginModel" size="mini" @change="changeMarginStyle('marginModel')">
            <el-radio-button label="auto">{{ $t('chart.margin_model_auto') }}</el-radio-button>
            <el-radio-button label="absolute">{{ $t('chart.margin_model_absolute') }}</el-radio-button>
            <el-radio-button label="relative">{{ $t('chart.margin_model_relative') }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <div v-show="showProperty('marginModel') && marginForm.marginModel !== 'auto'">
          <el-form-item v-show="showProperty('marginTop')" :label="$t('chart.text_pos_top')" class="form-item" prop="marginTop">
            <el-input v-model="marginForm.marginTop" :placeholder="$t('chart.margin_placeholder')" oninput="value=value.replace(/[^\d]/g,'')" @change="changeMarginStyle(marginForm.marginTop, 'marginTop')">
              <template v-if="unitSuffix" slot="append">{{ unitSuffix }}</template>
            </el-input>
          </el-form-item>

          <el-form-item v-show="showProperty('marginBottom')" :label="$t('chart.text_pos_bottom')" class="form-item" prop="marginBottom">
            <el-input v-model="marginForm.marginBottom" :placeholder="$t('chart.margin_placeholder')" oninput="value=value.replace(/[^\d]/g,'')" @change="changeMarginStyle(marginForm.marginBottom, 'marginBottom')">
              <template v-if="unitSuffix" slot="append">{{ unitSuffix }}</template>
            </el-input>
          </el-form-item>

          <el-form-item v-show="showProperty('marginLeft')" :label="$t('chart.text_pos_left')" class="form-item" prop="marginLeft">
            <el-input v-model="marginForm.marginLeft" :placeholder="$t('chart.margin_placeholder')" oninput="value=value.replace(/[^\d]/g,'')" @change="changeMarginStyle(marginForm.marginLeft, 'marginLeft')">
              <template v-if="unitSuffix" slot="append">{{ unitSuffix }}</template>
            </el-input>
          </el-form-item>

          <el-form-item v-show="showProperty('marginRight')" :label="$t('chart.text_pos_right')" class="form-item" prop="marginRight">
            <el-input v-model="marginForm.marginRight" :placeholder="$t('chart.margin_placeholder')" oninput="value=value.replace(/[^\d]/g,'')" @change="changeMarginStyle(marginForm.marginRight, 'marginRight')">
              <template v-if="unitSuffix" slot="append">{{ unitSuffix }}</template>
            </el-input>
          </el-form-item>

        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_MARGIN_STYLE } from '../../chart/chart'
import { getMarginUnit } from '@/views/chart/chart/common/common'
export default {
  name: 'MarginSelector',
  props: {
    param: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      marginForm: JSON.parse(JSON.stringify(DEFAULT_MARGIN_STYLE)),
      rules: {
        marginTop: [
          { validator: this.validateMarginNumber, trigger: ['blur', 'change'] }
        ],
        marginBottom: [
          { validator: this.validateMarginNumber, trigger: ['blur', 'change'] }
        ],
        marginLeft: [
          { validator: this.validateMarginNumber, trigger: ['blur', 'change'] }
        ],
        marginRight: [
          { validator: this.validateMarginNumber, trigger: ['blur', 'change'] }
        ]
      }
    }
  },
  computed: {
    unitSuffix() {
      return getMarginUnit(this.marginForm)
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
      if (chart.customStyle) {
        let customStyle = null
        if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
          customStyle = JSON.parse(JSON.stringify(chart.customStyle))
        } else {
          customStyle = JSON.parse(chart.customStyle)
        }
        if (customStyle.margin) {
          this.marginForm = customStyle.margin
        }
      }
    },

    changeMarginStyle(value, modifyName) {
      this.marginForm['modifyName'] = modifyName
      this.$emit('onMarginChange', this.marginForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    },
    validateMarginNumber(rule, value, callBack) {
      if (value == null || value === '') {
        callBack()
        return
      }
      if (value < 0 || value > 100) {
        callBack(new Error(this.$t('chart.margin_placeholder')))
        this.marginForm[rule.field] = 0
      }
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
</style>
