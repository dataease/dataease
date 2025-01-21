<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { ElRow } from 'element-plus-secondary'
import { fieldType } from '@/utils/attr'
import { cloneDeep, defaultsDeep, isEmpty } from 'lodash-es'
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
  name: 'gauge-default-data',
  callback: args => gaugeOrLiquidDefaultRangeData(args)
})
useEmitt({
  name: 'liquid-default-data',
  callback: args => gaugeOrLiquidDefaultRangeData(args)
})
const emit = defineEmits(['onMiscChange'])

watch(
  () => props.quotaFields,
  () => {
    initField()
  },
  { deep: true }
)

const validLiquidMaxField = computed(() => {
  return isValidField(state.liquidMaxField)
})
const validMinField = computed(() => {
  return isValidField(state.minField)
})
const validMaxField = computed(() => {
  return isValidField(state.maxField)
})

const state = reactive({
  miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC)),
  minField: {},
  maxField: {},
  liquidMaxField: {},
  quotaData: [],
  // 是否已处理没有 y 轴字段的情况
  liquidProcessedNoYAxis: false,
  gaugeProcessedNoYAxis: false
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
  const maxTypeKey = props.chart.type === 'liquid' ? 'liquidMaxType' : 'gaugeMaxType'
  const maxValueKey = props.chart.type === 'liquid' ? 'liquidMax' : 'gaugeMax'
  if (!props.chart.yAxis.length) {
    state.miscForm[maxTypeKey] = 'fix'
    state.miscForm[maxValueKey] = undefined
  }
}

const initField = () => {
  // 数据集字段中，如果没有 y 轴字段，直接返回
  const yAxisInDataset = props.quotaFields.find(ele => ele.id === props.chart.yAxis?.[0]?.id)
  if (!yAxisInDataset) {
    return
  }
  // 过滤掉记录数字段以及计算字段
  state.quotaData = props.quotaFields.filter(ele => ele.id !== '-1' && ele.extField !== 2)
  if (!isEmpty(state.miscForm.gaugeMinField.id)) {
    state.minField = getQuotaField(state.miscForm.gaugeMinField.id)
  }
  if (!isEmpty(state.miscForm.gaugeMaxField.id)) {
    state.maxField = getQuotaField(state.miscForm.gaugeMaxField.id)
  }
  if (!isEmpty(state.miscForm.liquidMaxField.id)) {
    state.liquidMaxField = getQuotaField(state.miscForm.liquidMaxField.id)
  }
  if (props.quotaFields.length) {
    initDynamicDefaultField()
  }
}
const COUNT_DE_TYPE = [0, 1, 5]
const NUMBER_DE_TYPE = [1, 2, 3]
const getFieldSummaryByDeType = (deType: number) => {
  return COUNT_DE_TYPE.includes(deType) || !deType ? 'count' : 'sum'
}
const initDynamicDefaultField = () => {
  const yAxisField = props.chart.yAxis?.[0]
  const yAxisId = yAxisField?.id
  // 没有字表字段时，清空动态字段
  if (quotaData.value?.length === 0) {
    if (isLiquid.value && state.miscForm.liquidMaxType === 'dynamic') {
      state.miscForm.liquidMax = state.miscForm.liquidMax
        ? state.miscForm.liquidMax
        : cloneDeep(defaultMaxValue.gaugeMax)
      state.miscForm.liquidMaxType = 'fix'
      state.miscForm.liquidMaxField = {}
      changeMisc('liquidMaxField', true)
    }
    if (isGauge.value && state.miscForm.gaugeMaxType === 'dynamic') {
      state.miscForm.gaugeMax = state.miscForm.gaugeMax
        ? state.miscForm.gaugeMax
        : cloneDeep(defaultMaxValue.liquidMax)
      state.miscForm.gaugeMaxType = 'fix'
      state.miscForm.gaugeMaxField = {}
      state.miscForm.gaugeMinType = 'fix'
      state.miscForm.gaugeMinField = {}
      changeMisc('gaugeMaxField', true)
    }
  } else {
    // 查找 quotaData 中是否存在 chart.yAxis[0].id
    const yAxisExists = props.quotaFields.find(ele => ele.id === yAxisId)
    // 如果不存在
    if (!yAxisExists) {
      if (isLiquid.value) {
        state.liquidProcessedNoYAxis = true
        state.miscForm.liquidMaxField.id = ''
        state.miscForm.liquidMaxField.summary = ''
        state.liquidMaxField = {}
        changeMisc('liquidMaxField', true)
      }
      if (isGauge.value) {
        state.gaugeProcessedNoYAxis = true
        state.miscForm.gaugeMaxField.id = ''
        state.miscForm.gaugeMaxField.summary = ''
        state.maxField = {}
        changeMisc('gaugeMaxField', true)
      }
    }
    if (isLiquid.value) {
      // 如果 liquidMaxType 是动态类型，重置 liquidMax
      if (state.miscForm.liquidMaxType === 'dynamic') {
        state.miscForm.liquidMax = undefined
        // 检查 yAxisId 和 yAxisField 的条件
        const isValidYAxisField =
          yAxisId && (yAxisField.extField === 2 || NUMBER_DE_TYPE.includes(yAxisField.deType))
        const isMatchingFieldId =
          state.miscForm.liquidMaxField.id === yAxisId || !state.miscForm.liquidMaxField.id
        // 如果条件满足，设置 liquidMaxField.id 和相关属性
        if (isValidYAxisField && isMatchingFieldId) {
          state.miscForm.liquidMaxField.id = getDynamicFieldId()
          state.liquidMaxField = getQuotaField(state.miscForm.liquidMaxField.id)
          const summary = state.miscForm.liquidMaxField.summary
          state.miscForm.liquidMaxField.summary = summary
            ? summary
            : getFieldSummaryByDeType(state.liquidMaxField?.deType)

          // 更新处理状态并触发 changeMisc
          if (!state.liquidProcessedNoYAxis) {
            state.liquidProcessedNoYAxis = true
            changeMisc('liquidMaxField', true)
          }
        }
      }
      if (!state.miscForm.liquidMax && state.miscForm.liquidMaxType === 'fix') {
        state.miscForm.liquidMax = cloneDeep(defaultMaxValue.liquidMax)
      }
    } else {
      if (state.miscForm.gaugeMaxType === 'dynamic') {
        state.miscForm.gaugeMax = undefined

        // 拖入的字段是计算字段，并且当前选择的是该字段时，取其他字段
        if (
          yAxisId &&
          (yAxisField.extField === 2 || NUMBER_DE_TYPE.includes(yAxisField.deType)) &&
          (state.miscForm.gaugeMaxField.id === yAxisId || !state.miscForm.gaugeMaxField.id)
        ) {
          // 根据查找结果设置 gaugeMaxField.id
          state.miscForm.gaugeMaxField.id = getDynamicFieldId()
          state.maxField = getQuotaField(state.miscForm.gaugeMaxField.id)
          // 设置 summary 和 maxField
          const summary = state.miscForm.gaugeMaxField.summary
          state.miscForm.gaugeMaxField.summary = summary
            ? summary
            : getFieldSummaryByDeType(state.maxField?.deType)
          if (!state.gaugeProcessedNoYAxis) {
            state.gaugeProcessedNoYAxis = true
            changeMisc('gaugeMaxField', true)
          }
        }
      }
      if (!state.miscForm.gaugeMax && state.miscForm.gaugeMaxType === 'fix') {
        state.miscForm.gaugeMax = cloneDeep(defaultMaxValue.gaugeMax)
      }
    }
  }
}

const getDynamicFieldId = () => {
  // 返回yAxis字段ID
  const curFieldObj = quotaData.value?.find(item => item.id === props.chart.yAxis?.[0]?.id)
  if (curFieldObj) return curFieldObj.id
  // 返回第一个数字类型字段ID
  return quotaData.value?.filter(item => NUMBER_DE_TYPE.includes(item.deType))?.[0]?.id
}

const changeQuotaField = (type: string, resetSummary?: boolean) => {
  const isCountField = props.chart.yAxis?.[0]?.id === '-1'
  const isDynamic =
    type === 'min'
      ? state.miscForm.gaugeMinType === 'dynamic'
      : state.miscForm.gaugeMaxType === 'dynamic'
  const field = type === 'min' ? state.miscForm.gaugeMinField : state.miscForm.gaugeMaxField
  const maxValueKey = props.chart.type === 'liquid' ? 'liquidMax' : 'gaugeMax'
  if (isDynamic) {
    if (!field.id && !isCountField) setDynamicFieldId(field)
    if (!field.summary || resetSummary) field.summary = 'count'
    if (field.id && field.summary) {
      if (type === 'min') state.minField = getQuotaField(field.id)
      else state.maxField = getQuotaField(field.id)
      changeMisc(`${maxValueKey}Field`, true)
    }
  } else {
    if (type === 'max' && isLiquid) {
      state.miscForm.liquidMax = state.miscForm.liquidMax || cloneDeep(defaultMaxValue.liquidMax)
    } else if (type === 'max') {
      state.miscForm.gaugeMax = state.miscForm.gaugeMax || cloneDeep(defaultMaxValue.gaugeMax)
    }
    changeMisc(`${maxValueKey}Field`, true)
  }
}

const setDynamicFieldId = fieldObj => {
  const yAxisField = props.chart.yAxis?.[0]
  if (
    yAxisField?.extField === 2 ||
    yAxisField?.id === '-1' ||
    !NUMBER_DE_TYPE.includes(yAxisField?.deType)
  ) {
    fieldObj.id = getDynamicFieldId()
  } else {
    fieldObj.id = yAxisField?.id
  }
}

const getQuotaField = id => {
  return quotaData.value.find(ele => ele.id === id) || {}
}

const isValidField = field => {
  return field.id !== '-1' && quotaData.value.findIndex(ele => ele.id === field.id) !== -1
}

const showProperty = prop => props.propertyInner?.includes(prop)
const wordCloudDefaultDataRange = args => {
  state.miscForm.wordCloudAxisValueRange.max = args.data.max
  state.miscForm.wordCloudAxisValueRange.min = args.data.min
  state.miscForm.wordCloudAxisValueRange.fieldId = props.chart.yAxis?.[0]?.id
}
const defaultMaxValue = {
  gaugeMax: undefined,
  liquidMax: undefined
}
const gaugeOrLiquidDefaultRangeData = args => {
  if (args.data.type === 'gauge') {
    defaultMaxValue.gaugeMax = cloneDeep(args.data.max)
    if (!state.miscForm.gaugeMax || defaultMaxValue.gaugeMax !== state.miscForm.gaugeMax) {
      state.miscForm.gaugeMax = cloneDeep(defaultMaxValue.gaugeMax)
      changeMisc('gaugeMaxField', false)
    }
  }
  if (args.data.type === 'liquid') {
    defaultMaxValue.liquidMax = cloneDeep(args.data.max)
    if (!state.miscForm.liquidMax || defaultMaxValue.liquidMax !== state.miscForm.liquidMax) {
      state.miscForm.liquidMax = cloneDeep(defaultMaxValue.liquidMax)
      changeMisc('liquidMaxField', false)
    }
  }
}
/**
 * 校验最大值的输入
 */
const changeMaxValidate = prop => {
  if (prop === 'gaugeMax' && !state.miscForm.gaugeMax) {
    state.miscForm.gaugeMax = cloneDeep(defaultMaxValue.gaugeMax)
  }
  if (prop === 'liquidMax' && !state.miscForm.liquidMax) {
    state.miscForm.liquidMax = cloneDeep(defaultMaxValue.liquidMax)
  }
  changeMisc(prop, true)
}
const addAxis = (form: AxisEditForm) => {
  state.quotaData = []
  if (form.axis[0]?.id) {
    const uniqueIds = new Set(state.quotaData.map(item => item.id))
    state.quotaData = [
      ...props.quotaFields.filter(
        ele => ele.id !== '-1' && ele.extField !== 2 && !uniqueIds.has(ele.id)
      )
    ]
  }
  const maxTypeKey = isLiquid.value ? 'liquidMaxType' : 'gaugeMaxType'
  const maxValueKey = isLiquid.value ? 'liquidMax' : 'gaugeMax'
  if (form.axis[0]?.id === '-1') {
    state.miscForm[maxTypeKey] = 'fix'
    state.miscForm[maxValueKey] = cloneDeep(defaultMaxValue[maxValueKey])
  } else {
    state.miscForm[maxTypeKey] = 'dynamic'
    state.miscForm[maxValueKey + 'Field']['id'] = getDynamicFieldId()
    state.miscForm[maxValueKey + 'Field']['summary'] = 'sum'
  }
  if (isGauge.value) {
    state.miscForm.gaugeMinType = 'fix'
    state.miscForm.gaugeMin = 0
    state.miscForm.gaugeMinField.id = ''
    state.miscForm.gaugeMinField.summary = ''
  }
  if (!quotaData.value.length) {
    state.miscForm[maxTypeKey] = 'fix'
    state.miscForm[maxValueKey] = cloneDeep(defaultMaxValue[maxValueKey])
  }
  changeMisc(`${maxValueKey}Field`, false)
}

onMounted(() => {
  init()
  initField()
  useEmitt({ name: 'addAxis', callback: addAxis })
  useEmitt({ name: 'chart-data-change', callback: initField })
  useEmitt({ name: 'chart-type-change', callback: initField })
})

/**
 * 不包含记录数字段以及计算字段
 */
const quotaData = computed(() => {
  return state.quotaData.filter(item => NUMBER_DE_TYPE.includes(item.deType))
})
const isLiquid = computed(() => props.chart.type === 'liquid')
const isGauge = computed(() => props.chart.type === 'gauge')
</script>

<template>
  <el-form :model="state.miscForm">
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
          @change="changeMisc('gaugeMin')"
        />
      </el-form-item>
      <el-row
        :gutter="8"
        v-if="showProperty('gaugeMinField') && state.miscForm.gaugeMinType === 'dynamic'"
      >
        <el-col :span="12">
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
        <el-col :span="12">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              :placeholder="t('chart.summary')"
              v-model="state.miscForm.gaugeMinField.summary"
              @change="changeQuotaField('min')"
            >
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
              <el-option key="count" value="count" :label="t('chart.count')" />
              <el-option
                v-if="state.minField.id !== '-1'"
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
          @change="changeMaxValidate('gaugeMax')"
        />
      </el-form-item>
      <el-row
        :gutter="8"
        v-if="showProperty('gaugeMaxField') && state.miscForm.gaugeMaxType === 'dynamic'"
      >
        <el-col :span="12">
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
        <el-col :span="12">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-select
              :effect="themes"
              v-model="state.miscForm.gaugeMaxField.summary"
              :placeholder="t('chart.summary')"
              @change="changeQuotaField('max')"
            >
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
              <el-option key="count" value="count" :label="t('chart.count')" />
              <el-option
                v-if="state.maxField.id !== '-1'"
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
        :min="1"
        size="small"
        controls-position="right"
        @blur="changeMaxValidate('liquidMax')"
      />
    </el-form-item>

    <el-row
      :gutter="8"
      v-if="showProperty('liquidMaxField') && state.miscForm.liquidMaxType === 'dynamic'"
    >
      <el-col :span="12">
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
      <el-col :span="12">
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-select
            :effect="themes"
            v-model="state.miscForm.liquidMaxField.summary"
            :placeholder="t('chart.summary')"
            @change="changeQuotaField('max')"
          >
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
            <el-option key="count" value="count" :label="t('chart.count')" />
            <el-option
              v-if="state.liquidMaxField.id !== '-1'"
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
