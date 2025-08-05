<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { ElRow } from 'element-plus-secondary'
import { fieldType } from '@/utils/attr'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { iconFieldMap } from '@/components/icon-group/field-list'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: ChartObj
    themes?: EditorTheme
    quotaFields: Array<any>
    propertyInner?: Array<string>
    mobileInPc?: boolean
  }>(),
  { themes: 'dark', mobileInPc: false }
)

useEmitt({
  name: 'word-cloud-default-data-range',
  callback: args => wordCloudDefaultDataRange(args)
})
useEmitt({
  name: 'gauge-liquid-y-value',
  callback: args => gaugeLiquidYaxisValue(args)
})
useEmitt({
  name: 'chart-type-change',
  callback: () => {
    if (isLiquid.value || isGauge.value) {
      init()
      initField()
      initAxis(props.chart.yAxis[0]?.id)
    }
  }
})
const addAxis = (form: AxisEditForm) => {
  initAxis(form.axis[0]?.id)
}
useEmitt({ name: 'addAxis', callback: addAxis })
const wordCloudDefaultDataRange = ({ data: { max, min } }) => {
  Object.assign(state.miscForm.wordCloudAxisValueRange, {
    max,
    min,
    fieldId: props.chart.yAxis?.[0]?.id
  })
}
const gaugeLiquidYaxisDefaultValue = { gaugeMax: undefined, liquidMax: undefined }
const gaugeLiquidYaxisValue = args => {
  const { type, max } = args.data
  const key = type === 'gauge' ? 'gaugeMax' : type === 'liquid' ? 'liquidMax' : null
  if (key) {
    gaugeLiquidYaxisDefaultValue[key] = cloneDeep(max)
    if (state.miscForm[key] === undefined || state.miscForm[key] === null) {
      state.miscForm[key] = gaugeLiquidYaxisDefaultValue[key]
      changeMisc()
    }
  }
}
const emit = defineEmits(['onMiscChange'])

watch(
  () => props.quotaFields,
  () => {
    init()
    initField()
  },
  { deep: true }
)

const validLiquidMaxField = computed(() => {
  return isValidField(state.miscForm.liquidMaxField)
})
const validMinField = computed(() => {
  return isValidField(state.miscForm.gaugeMinField)
})
const validMaxField = computed(() => {
  return isValidField(state.miscForm.gaugeMaxField)
})
const isValidField = field => {
  return field.id !== '-1' && quotaData.value.findIndex(ele => ele.id === field.id) !== -1
}

const state = reactive({
  miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC)),
  quotaData: []
})

const liquidShapeOptions = [
  { name: t('chart.liquid_shape_circle'), value: 'circle' },
  { name: t('chart.liquid_shape_diamond'), value: 'diamond' },
  { name: t('chart.liquid_shape_triangle'), value: 'triangle' },
  { name: t('chart.liquid_shape_pin'), value: 'pin' },
  { name: t('chart.liquid_shape_rect'), value: 'rect' }
]

const changeMisc = (prop = '', refresh = false) => {
  emit('onMiscChange', { data: state.miscForm, requestData: refresh }, prop)
}

const init = () => {
  const misc = cloneDeep(props.chart.customAttr.misc)
  state.miscForm = defaultsDeep(misc, cloneDeep(DEFAULT_MISC)) as ChartMiscAttr
}

const initField = () => {
  // 数据集字段中，如果没有 y 轴字段，直接返回
  const yAxisInDataset = props.quotaFields.find(ele => ele.id === props.chart.yAxis?.[0]?.id)
  if (!yAxisInDataset) {
    return
  }
  // 过滤掉记录数字段
  state.quotaData = props.quotaFields.filter(ele => ele.id !== '-1')
}
const NUMBER_DE_TYPE = [2, 3]

const getDynamicField = () => {
  return (
    quotaData.value?.find(item => item.id === props.chart.yAxis?.[0]?.id) || quotaData.value?.[0]
  )
}
const changeQuotaField = (type: string, resetSummary?: boolean) => {
  if (isGauge.value) {
    if (type === 'max') {
      const quotaField = getQuotaField(state.miscForm.gaugeMaxField.id || getDynamicField()?.id)
      state.miscForm.gaugeMaxField.id = quotaField.id
      const isDynamic = state.miscForm.gaugeMaxType === 'dynamic'
      if (isDynamic && resetSummary) {
        state.miscForm.gaugeMaxField.summary = quotaField.summary
      }
      if (!isDynamic) {
        state.miscForm.gaugeMax = cloneDeep(gaugeLiquidYaxisDefaultValue.gaugeMax)
        state.miscForm.gaugeMaxField.id = ''
      }
      changeMisc('gaugeMaxField', true)
    }
    if (type === 'min') {
      const quotaField = getQuotaField(state.miscForm.gaugeMinField.id || getDynamicField()?.id)
      state.miscForm.gaugeMinField.id = quotaField.id
      const isDynamic = state.miscForm.gaugeMinType === 'dynamic'
      if (isDynamic && resetSummary) {
        state.miscForm.gaugeMinField.summary = quotaField.summary
      }
      if (!isDynamic) {
        state.miscForm.gaugeMin = state.miscForm.gaugeMin || 0
        state.miscForm.gaugeMinField.id = ''
      }
      changeMisc('gaugeMinField', true)
    }
  }
  if (isLiquid.value) {
    const quotaField = getQuotaField(state.miscForm.liquidMaxField.id || getDynamicField()?.id)
    state.miscForm.liquidMaxField.id = quotaField.id
    const isDynamic = state.miscForm.liquidMaxType === 'dynamic'
    if (isDynamic && resetSummary) {
      state.miscForm.liquidMaxField.summary = quotaField.summary
    }
    if (!isDynamic) {
      state.miscForm.liquidMax = cloneDeep(gaugeLiquidYaxisDefaultValue.liquidMax)
      state.miscForm.liquidMaxField.id = ''
    }
    changeMisc('liquidMaxField', true)
  }
}

const getQuotaField = id => {
  return quotaData.value.find(ele => ele.id === id) || {}
}

const showProperty = prop => props.propertyInner?.includes(prop)

/**
 * 校验最大值的输入
 */
const changeFixedValidate = prop => {
  if (prop === 'gaugeMax' && !state.miscForm.gaugeMax) {
    state.miscForm.gaugeMax = cloneDeep(gaugeLiquidYaxisDefaultValue.gaugeMax)
  }
  if (prop === 'liquidMax' && !state.miscForm.liquidMax) {
    state.miscForm.liquidMax = cloneDeep(gaugeLiquidYaxisDefaultValue.liquidMax)
  }
  if (prop === 'gaugeMin' && !state.miscForm.gaugeMin) {
    state.miscForm.gaugeMin = 0
  }
  changeMisc(prop, true)
}
const initAxis = yAxisId => {
  state.quotaData = []
  if (yAxisId) {
    const uniqueIds = new Set(state.quotaData.map(item => item.id))
    state.quotaData = [
      ...props.quotaFields.filter(ele => ele.id !== '-1' && !uniqueIds.has(ele.id))
    ]
    if (state.quotaData.length) {
      if (isLiquid.value) {
        state.miscForm.liquidMaxType = 'dynamic'
        state.miscForm.liquidMaxField.id = getDynamicField()?.id || state.quotaData[0]?.id
        const quotaField = getQuotaField(state.miscForm.liquidMaxField.id)
        state.miscForm.liquidMaxField.summary = quotaField.summary
      }
      if (isGauge.value) {
        // max
        state.miscForm.gaugeMaxType = 'dynamic'
        state.miscForm.gaugeMaxField.id = getDynamicField()?.id || state.quotaData[0]?.id
        const quotaField = getQuotaField(state.miscForm.gaugeMaxField.id)
        state.miscForm.gaugeMaxField.summary = quotaField.summary
        // min
        state.miscForm.gaugeMinType = 'fix'
        state.miscForm.gaugeMin = 0
        state.miscForm.gaugeMinField.summary = quotaField.summary
      }
      changeMisc()
    } else {
      if (isLiquid.value) {
        state.miscForm.liquidMaxType = 'fix'
        state.miscForm.liquidMax = cloneDeep(gaugeLiquidYaxisDefaultValue.liquidMax) || 0
        state.miscForm.liquidMaxField.id = ''
        state.miscForm.liquidMaxField.summary = ''
        changeMisc('liquidMax', true)
      }
      if (isGauge.value) {
        // max
        state.miscForm.gaugeMaxType = 'fix'
        state.miscForm.gaugeMax = gaugeLiquidYaxisDefaultValue.gaugeMax || 0
        state.miscForm.liquidMaxField.id = ''
        state.miscForm.liquidMaxField.summary = ''
        changeMisc('gaugeMax', true)
        // min
        state.miscForm.gaugeMinType = 'fix'
        state.miscForm.gaugeMin = 0
        state.miscForm.gaugeMinField.id = ''
        state.miscForm.gaugeMinField.summary = ''
        changeMisc('gaugeMin', true)
      }
    }
  }
}

// 校验聚合函数
const validLiquidMaxFieldAgg = computed(() => {
  return isAggField(state.miscForm.liquidMaxField)
})
const validMinFieldAgg = computed(() => {
  return isAggField(state.miscForm.gaugeMinField)
})
const validMaxFieldAgg = computed(() => {
  return isAggField(state.miscForm.gaugeMaxField)
})
const isAggField = field => {
  return quotaData.value.find(ele => ele.id === field.id)?.agg
}
// 校验计算字段和聚合函数
const validLiquidMaxFieldCalcAndAgg = computed(() => {
  return isCalcFieldAndAgg(state.miscForm.liquidMaxField)
})
const validMinFieldCalcAndAgg = computed(() => {
  return isCalcFieldAndAgg(state.miscForm.gaugeMinField)
})
const validMaxFieldCalcAndAgg = computed(() => {
  return isCalcFieldAndAgg(state.miscForm.gaugeMaxField)
})
const isCalcFieldAndAgg = field => {
  return quotaData.value.find(ele => ele.id === field.id && ele.extField === 2 && ele.agg)
}

// 校验数值类型
const validLiquidMaxFieldNum = computed(() => {
  return isNumType(state.miscForm.liquidMaxField)
})
const validMinFieldNum = computed(() => {
  return isNumType(state.miscForm.gaugeMinField)
})
const validMaxFieldNum = computed(() => {
  return isNumType(state.miscForm.gaugeMaxField)
})

const isNumType = field => {
  return quotaData.value.find(ele => ele.id === field.id && NUMBER_DE_TYPE.includes(ele.deType))
}

/**
 * 计算属性
 */
const quotaData = computed(() => {
  return state.quotaData
})
const isLiquid = computed(() => props.chart.type === 'liquid')
const isGauge = computed(() => props.chart.type === 'gauge')
onMounted(() => {
  init()
  initField()
  if (
    (isGauge.value && !state.miscForm.gaugeMaxField.id && !state.miscForm.gaugeMax) ||
    (isLiquid.value && !state.miscForm.liquidMaxField.id && !state.miscForm.liquidMax)
  ) {
    initAxis(props.chart.yAxis[0]?.id)
  }
})
</script>

<template>
  <el-form size="small" :model="state.miscForm">
    <el-row :gutter="8">
      <el-col :span="12" v-show="showProperty('gaugeStartAngle')">
        <el-form-item
          :label="t('chart.start_angle')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            v-model="state.miscForm.gaugeStartAngle"
            :min="-360"
            :max="360"
            size="small"
            controls-position="right"
            @change="changeMisc('gaugeStartAngle')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-show="showProperty('gaugeEndAngle')">
        <el-form-item
          :label="t('chart.end_angle')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            v-model="state.miscForm.gaugeEndAngle"
            :min="-360"
            :max="360"
            size="small"
            controls-position="right"
            @change="changeMisc('gaugeEndAngle')"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <!--gauge-begin-->
    <template v-if="!mobileInPc">
      <el-form-item
        v-show="showProperty('gaugeMinType')"
        class="form-item margin-bottom-8"
        :label="t('chart.min')"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.miscForm.gaugeMinType"
          size="small"
          @change="changeQuotaField('min')"
          :disabled="quotaData.length === 0"
        >
          <el-radio :effect="themes" label="fix">{{ t('chart.fix') }}</el-radio>
          <el-radio :effect="themes" label="dynamic">{{ t('chart.dynamic') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="showProperty('gaugeMin') && state.miscForm.gaugeMinType === 'fix'"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input-number
          :effect="themes"
          v-model="state.miscForm.gaugeMin"
          size="small"
          controls-position="right"
          @blur="changeFixedValidate('gaugeMin')"
        />
      </el-form-item>
      <el-row
        :gutter="8"
        v-if="showProperty('gaugeMinField') && state.miscForm.gaugeMinType === 'dynamic'"
      >
        <el-col :span="validMinFieldCalcAndAgg ? 24 : 12">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              :placeholder="t('chart.field')"
              :class="{ 'invalid-field': !validMinField }"
              v-model="state.miscForm.gaugeMinField.id"
              @change="changeQuotaField('min', true)"
            >
              <el-option
                class="series-select-option"
                v-for="item in quotaData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <el-icon style="margin-right: 8px">
                  <Icon :className="`field-icon-${fieldType[item.deType]}`"
                    ><component
                      class="svg-icon"
                      :class="`field-icon-${fieldType[item.deType]}`"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                {{ item.name }}
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!validMinFieldCalcAndAgg">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              :placeholder="t('chart.summary')"
              v-model="state.miscForm.gaugeMinField.summary"
              @change="changeQuotaField('min')"
            >
              <div v-if="!validMinFieldAgg && validMinFieldNum">
                <el-option v-if="validMinField" key="sum" value="sum" :label="t('chart.sum')" />
                <el-option v-if="validMinField" key="avg" value="avg" :label="t('chart.avg')" />
                <el-option v-if="validMinField" key="max" value="max" :label="t('chart.max')" />
                <el-option v-if="validMinField" key="min" value="min" :label="t('chart.min')" />
                <el-option
                  v-if="validMinField"
                  key="stddev_pop"
                  value="stddev_pop"
                  :label="t('chart.stddev_pop')"
                />
                <el-option
                  v-if="validMinField"
                  key="var_pop"
                  value="var_pop"
                  :label="t('chart.var_pop')"
                />
              </div>
              <el-option key="count" value="count" :label="t('chart.count')" />
              <el-option
                v-if="state.miscForm.gaugeMinField.id !== '-1'"
                key="count_distinct"
                value="count_distinct"
                :label="t('chart.count_distinct')"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item
        v-show="showProperty('gaugeMaxType')"
        class="form-item margin-bottom-8"
        :label="t('chart.max')"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          v-model="state.miscForm.gaugeMaxType"
          size="small"
          @change="changeQuotaField('max')"
          :disabled="quotaData.length === 0"
        >
          <el-radio :effect="themes" label="fix">{{ t('chart.fix') }}</el-radio>
          <el-radio :effect="themes" label="dynamic">{{ t('chart.dynamic') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="showProperty('gaugeMax') && state.miscForm.gaugeMaxType === 'fix'"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input-number
          :effect="themes"
          v-model="state.miscForm.gaugeMax"
          size="small"
          controls-position="right"
          value-on-clear="gaugeLiquidYaxisDefaultValue.gaugeMax"
          @blur="changeFixedValidate('gaugeMax')"
        />
      </el-form-item>
      <el-row
        :gutter="8"
        v-if="showProperty('gaugeMaxField') && state.miscForm.gaugeMaxType === 'dynamic'"
      >
        <el-col :span="validMaxFieldCalcAndAgg ? 24 : 12">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              :placeholder="t('chart.field')"
              :class="{ 'invalid-field': !validMaxField }"
              v-model="state.miscForm.gaugeMaxField.id"
              @change="changeQuotaField('max', true)"
            >
              <el-option
                class="series-select-option"
                v-for="item in quotaData"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              >
                <el-icon style="margin-right: 8px">
                  <Icon :className="`field-icon-${fieldType[item.deType]}`"
                    ><component
                      :class="`field-icon-${fieldType[item.deType]}`"
                      class="svg-icon"
                      :is="iconFieldMap[fieldType[item.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                {{ item.name }}
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!validMaxFieldCalcAndAgg">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              v-model="state.miscForm.gaugeMaxField.summary"
              :placeholder="t('chart.summary')"
              @change="changeQuotaField('max')"
            >
              <div v-if="!validMaxFieldAgg && validMaxFieldNum">
                <el-option v-if="validMaxField" key="sum" value="sum" :label="t('chart.sum')" />
                <el-option v-if="validMaxField" key="avg" value="avg" :label="t('chart.avg')" />
                <el-option v-if="validMaxField" key="max" value="max" :label="t('chart.max')" />
                <el-option v-if="validMaxField" key="min" value="min" :label="t('chart.min')" />
                <el-option
                  v-if="validMaxField"
                  key="stddev_pop"
                  value="stddev_pop"
                  :label="t('chart.stddev_pop')"
                />
                <el-option
                  v-if="validMaxField"
                  key="var_pop"
                  value="var_pop"
                  :label="t('chart.var_pop')"
                />
              </div>
              <el-option key="count" value="count" :label="t('chart.count')" />
              <el-option
                v-if="state.miscForm.gaugeMaxField.id !== '-1'"
                key="count_distinct"
                value="count_distinct"
                :label="t('chart.count_distinct')"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </template>

    <!--gauge-end-->

    <!--liquid-begin-->
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          v-show="showProperty('liquidShape')"
          class="form-item"
          :label="t('chart.liquid_shape')"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.liquidShape"
            :placeholder="t('chart.liquid_shape')"
            @change="changeMisc('liquidShape')"
          >
            <el-option
              v-for="item in liquidShapeOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          v-show="showProperty('liquidSize')"
          class="form-item"
          :label="t('chart.radar_size')"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            v-model="state.miscForm.liquidSize"
            :min="1"
            :max="100"
            size="small"
            controls-position="right"
            @change="changeMisc('liquidSize')"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item
      v-show="showProperty('liquidMaxType')"
      class="form-item margin-bottom-8"
      :label="t('chart.liquid_max')"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        :effect="themes"
        v-model="state.miscForm.liquidMaxType"
        size="small"
        @change="changeQuotaField('max')"
        :disabled="quotaData.length === 0"
      >
        <el-radio :effect="themes" label="fix">
          {{ t('chart.fix') }}
        </el-radio>
        <el-radio :effect="themes" label="dynamic">{{ t('chart.dynamic') }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item
      v-if="showProperty('liquidMaxType') && state.miscForm.liquidMaxType === 'fix'"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        v-model="state.miscForm.liquidMax"
        size="small"
        controls-position="right"
        @blur="changeFixedValidate('liquidMax')"
      />
    </el-form-item>

    <el-row
      :gutter="8"
      v-if="showProperty('liquidMaxField') && state.miscForm.liquidMaxType === 'dynamic'"
    >
      <el-col :span="validLiquidMaxFieldCalcAndAgg ? 24 : 12">
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-select
            :effect="themes"
            :placeholder="t('chart.field')"
            :class="{ 'invalid-field': !validLiquidMaxField }"
            v-model="state.miscForm.liquidMaxField.id"
            @change="changeQuotaField('max', true)"
          >
            <el-option
              class="series-select-option"
              v-for="item in quotaData"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <el-icon style="margin-right: 8px">
                <Icon :className="`field-icon-${fieldType[item.deType]}`"
                  ><component
                    :class="`field-icon-${fieldType[item.deType]}`"
                    class="svg-icon"
                    :is="iconFieldMap[fieldType[item.deType]]"
                  ></component
                ></Icon>
              </el-icon>
              {{ item.name }}
            </el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="!validLiquidMaxFieldCalcAndAgg">
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-select
            :effect="themes"
            v-model="state.miscForm.liquidMaxField.summary"
            :placeholder="t('chart.summary')"
            @change="changeQuotaField('max')"
          >
            <div v-if="!validLiquidMaxFieldAgg && validLiquidMaxFieldNum">
              <el-option v-if="validLiquidMaxField" key="sum" value="sum" :label="t('chart.sum')" />
              <el-option v-if="validLiquidMaxField" key="avg" value="avg" :label="t('chart.avg')" />
              <el-option v-if="validLiquidMaxField" key="max" value="max" :label="t('chart.max')" />
              <el-option v-if="validLiquidMaxField" key="min" value="min" :label="t('chart.min')" />
              <el-option
                v-if="validLiquidMaxField"
                key="stddev_pop"
                value="stddev_pop"
                :label="t('chart.stddev_pop')"
              />
              <el-option
                v-if="validLiquidMaxField"
                key="var_pop"
                value="var_pop"
                :label="t('chart.var_pop')"
              />
            </div>
            <el-option key="count" value="count" :label="t('chart.count')" />
            <el-option
              v-if="state.miscForm.liquidMaxField.id !== '-1'"
              key="count_distinct"
              value="count_distinct"
              :label="t('chart.count_distinct')"
            />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <!--liquid-end-->

    <!-- word-cloud start -->
    <template v-if="showProperty('wordCloudAxisValueRange')">
      <div style="display: flex; flex-direction: row; justify-content: space-between">
        <label class="custom-form-item-label" :class="'custom-form-item-label--' + themes">
          {{ t('chart.axis_value') }}
          <el-tooltip class="item" :effect="toolTip" placement="top">
            <template #content><span v-html="t('chart.axis_tip')"></span></template>
            <span style="vertical-align: middle">
              <el-icon style="cursor: pointer">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </span>
          </el-tooltip>
        </label>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            size="small"
            :effect="props.themes"
            v-model="state.miscForm.wordCloudAxisValueRange.auto"
            @change="changeMisc()"
          >
            {{ t('chart.axis_auto') }}
          </el-checkbox>
        </el-form-item>
      </div>
      <template
        v-if="
          showProperty('wordCloudAxisValueRange') && !state.miscForm.wordCloudAxisValueRange.auto
        "
      >
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_max')"
            >
              <el-input-number
                controls-position="right"
                :effect="props.themes"
                v-model="state.miscForm.wordCloudAxisValueRange.max"
                @change="changeMisc()"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.axis_value_min')"
            >
              <el-input-number
                :effect="props.themes"
                controls-position="right"
                v-model="state.miscForm.wordCloudAxisValueRange.min"
                @change="changeMisc()"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </template>
    <div class="alpha-setting" v-if="showProperty('wordSizeRange')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.word_size_range') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="24">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              v-model="state.miscForm.wordSizeRange"
              range
              :effect="themes"
              :min="1"
              :max="100"
              @change="changeMisc()"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <div class="alpha-setting" v-if="showProperty('wordSpacing')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.word_spacing') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="24">
          <el-form-item
            v-show="showProperty('wordSpacing')"
            class="form-item alpha-slider"
            :class="'form-item-' + themes"
          >
            <el-slider
              v-model="state.miscForm.wordSpacing"
              :min="0"
              :max="20"
              @change="changeMisc()"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <!-- word-cloud end -->
  </el-form>
</template>

<style lang="less" scoped>
.dynamic-value-style {
  :deep(.ed-form-item__content) {
    flex-direction: row;
    justify-content: space-between;
  }

  :deep(.dynamic-item) {
    width: 100px !important;
  }
}

.field-item {
  float: left;
  color: #8492a6;
  font-size: 12px;
}

.margin-bottom-8 {
  margin-bottom: 8px !important;
}

.series-select-option {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 11px;
}

.invalid-field {
  :deep(.ed-input__wrapper) {
    box-shadow: 0 0 0 1px rgb(245, 74, 69) inset !important;
  }
}

.alpha-setting {
  display: flex;
  width: 100%;

  .alpha-slider {
    padding: 0 8px;
    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .alpha-label {
    padding-right: 8px;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    height: 32px;
    line-height: 32px;
    display: inline-flex;
    align-items: flex-start;

    min-width: 56px;

    &.dark {
      color: #a6a6a6;
    }
  }
}
</style>
