<template>
  <div>
    <el-form
      ref="form"
      :model="formatterItem.formatterCfg"
      label-width="80px"
      size="mini"
      class="formatter-form"
    >
      <el-form-item :label="$t('chart.value_formatter_type')">
        <el-radio-group
          v-model="formatterItem.formatterCfg.type"
          @change="getExampleValue"
        >
          <el-radio
            v-for="radio in typeList"
            :key="radio.value"
            :label="radio.value"
          >{{ $t('chart.' + radio.name) }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        v-show="formatterItem.formatterCfg.type !== 'auto'"
        :label="$t('chart.value_formatter_decimal_count')"
      >
        <el-input-number
          v-model="formatterItem.formatterCfg.decimalCount"
          :min="0"
          :max="10"
          size="mini"
          @change="getExampleValue"
        />
      </el-form-item>

      <el-form-item
        v-show="formatterItem.formatterCfg.type !== 'percent'"
        :label="$t('chart.value_formatter_unit')"
      >
        <el-select
          v-model="formatterItem.formatterCfg.unit"
          :placeholder="$t('chart.pls_select_field')"
          size="mini"
          @change="getExampleValue"
        >
          <el-option
            v-for="item in unitList"
            :key="item.value"
            :label="$t('chart.' + item.name)"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('chart.value_formatter_suffix')">
        <el-input
          v-model="formatterItem.formatterCfg.suffix"
          size="mini"
          clearable
          :placeholder="$t('commons.input_content')"
          @change="getExampleValue"
        />
      </el-form-item>

      <el-form-item :label="$t('chart.value_formatter_thousand_separator')">
        <el-checkbox
          v-model="formatterItem.formatterCfg.thousandSeparator"
          @change="getExampleValue"
        />
      </el-form-item>

      <el-form-item :label="$t('chart.value_formatter_example')">
        <span>{{ exampleResult }}</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { formatterItem, formatterType, unitList, valueFormatter } from '@/views/chart/chart/formatter'

export default {
  name: 'ValueFormatterEdit',
  props: {
    formatterItem: {
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
      typeList: formatterType,
      unitList: unitList,
      exampleResult: '20000000'
    }
  },
  created() {
    this.init()
  },
  mounted() {
    this.getExampleValue()
  },
  methods: {
    init() {
      if (!this.formatterItem.formatterCfg) {
        this.formatterItem.formatterCfg = formatterItem
      }
    },
    getExampleValue() {
      this.exampleResult = valueFormatter(20000000, this.formatterItem.formatterCfg)
    }
  }
}
</script>

<style scoped>
.el-form-item{
  margin-bottom: 10px!important;
}
.formatter-form ::v-deep .el-form-item__label{
  font-size: 12px!important;
  font-weight: 400!important;
}
.formatter-form ::v-deep .el-radio__label{
  font-size: 12px!important;
  font-weight: 400!important;
}
.el-select-dropdown__item ::v-deep span{
  font-size: 12px!important;
}
.exp-style{
  color: #C0C4CC;
  font-size: 12px;
}
</style>
