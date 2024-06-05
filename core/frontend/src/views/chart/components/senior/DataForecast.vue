<template>
  <div>
    <el-form
      ref="forecastForm"
      :model="forecastCfg"
      :rules="rules"
      label-width="80px"
      size="mini"
      @submit.native.prevent
    >
      <el-form-item
        class="form-item"
        :label="$t('chart.forecast_enable')"
      >
        <el-checkbox
          v-model="forecastCfg.enable"
          @change="onForecastChange"
        />
      </el-form-item>
      <el-form-item
        class="form-item"
        :label="$t('chart.forecast_all_period')"
      >
        <el-checkbox
          v-model="forecastCfg.allPeriod"
          :disabled="!forecastCfg.enable"
          @change="onForecastChange"
        />
        <el-tooltip
          class="item"
          effect="dark"
          placement="bottom"
        >
          <div
            slot="content"
          >
            {{ $t('chart.forecast_all_period_tip') }}
          </div>
          <i
            class="el-icon-info"
            style="cursor: pointer;color: #606266;margin-left: 4px;"
          />
        </el-tooltip>
      </el-form-item>
      <el-form-item
        v-if="!forecastCfg.allPeriod"
        class="form-item"
        prop="trainingPeriod"
        :label="$t('chart.forecast_training_period')"
      >
        <el-input-number
          v-model="forecastCfg.trainingPeriod"
          :disabled="!forecastCfg.enable"
          :min="5"
          :max="1000"
          :step="1"
          :precision="0"
          step-strictly
          size="mini"
          @change="onForecastChange"
        />
        <el-tooltip
          class="item"
          effect="dark"
          placement="bottom"
        >
          <div
            slot="content"
          >{{ $t('chart.forecast_training_period_tip') }}
          </div>
          <i
            class="el-icon-info"
            style="cursor: pointer;color: #606266;margin-left: 4px;"
          />
        </el-tooltip>
      </el-form-item>
      <el-form-item
        class="form-item"
        prop="period"
        :label="$t('chart.forecast_period')"
      >
        <el-input-number
          v-model="forecastCfg.period"
          :disabled="!forecastCfg.enable"
          :min="1"
          :max="100"
          :precision="0"
          step-strictly
          size="mini"
          @change="onForecastChange"
        />
      </el-form-item>
      <el-form-item
        v-show="false"
        class="form-item"
        :label="$t('chart.forecast_confidence_interval')"
      >
        <el-select
          v-model="forecastCfg.ciType"
          :disabled="!forecastCfg.enable"
          @change="onForecastChange"
        >
          <el-option
            v-for="item in ciOptions"
            :key="item.name"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-show="false"
        v-if="forecastCfg.ciType === 'custom'"
        class="form-item"
        prop="confidenceInterval"
        :label="$t('chart.custom_case')"
      >
        <el-input-number
          v-model="forecastCfg.confidenceInterval"
          :disabled="!forecastCfg.enable"
          :max="0.99"
          :min="0.75"
          :step="0.01"
          :precision="2"
          step-strictly
          size="mini"
          @change="onForecastChange"
        />
      </el-form-item>
      <el-form-item
        class="form-item"
        :label="$t('chart.forecast_model')"
      >
        <el-select
          v-model="forecastCfg.algorithm"
          :disabled="!forecastCfg.enable"
          @change="onForecastChange"
        >
          <el-option
            v-for="item in algorithmOptions"
            :key="item.name"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="forecastCfg.algorithm === 'polynomial-regression'"
        class="form-item"
        prop="degree"
        :label="$t('chart.forecast_degree')"
      >
        <el-input-number
          v-model="forecastCfg.degree"
          :disabled="!forecastCfg.enable"
          :max="10"
          :min="1"
          :precision="0"
          step-strictly
          size="mini"
          @change="onForecastChange"
        />
      </el-form-item>
    </el-form>
  </div>
</template>
<script>

export default {
  name: 'DataForecast',
  props: {
    chart: {
      required: true,
      type: Object
    }
  },
  data() {
    return {
      forecastCfg: {
        enable: false,
        period: 3,
        allPeriod: true,
        trainingPeriod: 120,
        confidenceInterval: 0.95,
        ciType: 0.95,
        algorithm: 'linear-regression',
        customCi: 0.95,
        degree: 3
      },
      algorithmOptions: [
        { name: this.$t('chart.linear_regression'), value: 'linear-regression' },
        { name: this.$t('chart.polynomial_regression'), value: 'polynomial-regression' }
      ],
      ciOptions: [
        { name: '90%', value: 0.90 },
        { name: '95%', value: 0.95 },
        { name: '99%', value: 0.99 },
        { name: '自定义', value: 'custom' }
      ],
      rules: {
        trainingPeriod: [{ required: true, trigger: 'change', message: this.$t('commons.cannot_be_null') }],
        period: [{ required: true, trigger: 'change', message: this.$t('commons.cannot_be_null') }],
        degree: [{ required: true, trigger: 'change', message: this.$t('commons.cannot_be_null') }],
        confidenceInterval: [{ required: true, trigger: 'change', message: this.$t('commons.cannot_be_null') }]
      }
    }
  },
  watch: {
    chart: {
      handler: function() {
        this.init()
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.senior) {
        let senior = null
        if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
          senior = JSON.parse(JSON.stringify(chart.senior))
        } else {
          senior = JSON.parse(chart.senior)
        }
        if (senior.forecastCfg) {
          this.forecastCfg = senior.forecastCfg
        }
      }
    },
    onForecastChange() {
      this.$refs.forecastForm.validate((valid) => {
        if (!valid) {
          return
        }
        if (this.forecastCfg.ciType !== 'custom') {
          this.forecastCfg.confidenceInterval = this.forecastCfg.ciType
        }
        this.$emit('onForecastChange', this.forecastCfg)
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.form-item-slider ::v-deep .el-form-item__label {
  font-size: 12px;
  line-height: 38px;
}

.form-item-range-slider ::v-deep .el-form-item__content {
  padding-right: 6px
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
</style>
