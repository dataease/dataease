<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { ElMessage, ElRow } from 'element-plus-secondary'
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
  }>(),
  { themes: 'dark' }
)

useEmitt({
  name: 'word-cloud-default-data-range',
  callback: args => wordCloudDefaultDataRange(args)
})
const emit = defineEmits(['onMiscChange'])

watch(
  () => props.chart,
  () => {
    initField()
    init()
  },
  { deep: true }
)

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
  if (state.miscForm.gaugeMax <= state.miscForm.gaugeMin) {
    ElMessage.error(t('chart.max_more_than_mix'))
  }
  emit('onMiscChange', { data: state.miscForm, requestData: refresh }, prop)
}

const init = () => {
  const misc = cloneDeep(props.chart.customAttr.misc)
  state.miscForm = defaultsDeep(misc, cloneDeep(DEFAULT_MISC)) as ChartMiscAttr
}

const initField = () => {
  state.quotaData = props.quotaFields.filter(ele => ele.summary !== '' && ele.id !== '-1')
  if (state.miscForm.gaugeMinField.id) {
    state.minField = getQuotaField(state.miscForm.gaugeMinField.id)
  }
  if (state.miscForm.gaugeMaxField.id) {
    state.maxField = getQuotaField(state.miscForm.gaugeMaxField.id)
  }
  if (state.miscForm.liquidMaxField.id) {
    state.liquidMaxField = getQuotaField(state.miscForm.liquidMaxField.id)
  }
}

const changeQuotaField = (type: string, resetSummary?: boolean) => {
  if (type === 'min') {
    if (state.miscForm.gaugeMinType === 'dynamic') {
      if (!state.miscForm.gaugeMinField.id) {
        state.miscForm.gaugeMinField.id = state.quotaData[0]?.id
      }
      if (!state.miscForm.gaugeMinField.summary) {
        state.miscForm.gaugeMinField.summary = 'count'
      }
      if (resetSummary) {
        state.miscForm.gaugeMinField.summary = 'count'
      }
      if (state.miscForm.gaugeMinField.id && state.miscForm.gaugeMinField.summary) {
        state.minField = getQuotaField(state.miscForm.gaugeMinField.id)
        changeMisc('gaugeMinField', true)
      }
    } else {
      if (state.miscForm.gaugeMaxType === 'dynamic') {
        if (state.miscForm.gaugeMaxField.id && state.miscForm.gaugeMaxField.summary) {
          changeMisc('gaugeMinField', true)
        }
      } else {
        changeMisc('gaugeMinField', true)
      }
    }
  } else if (type === 'max') {
    if (props.chart.type === 'liquid') {
      if (!state.miscForm.liquidMaxField.id) {
        state.miscForm.liquidMaxField.id = state.quotaData[0]?.id
      }
      if (!state.miscForm.liquidMaxField.summary) {
        state.miscForm.liquidMaxField.summary = 'count'
      }
      if (resetSummary) {
        state.miscForm.liquidMaxField.summary = 'count'
      }
      if (state.miscForm.liquidMaxField.id && state.miscForm.liquidMaxField.summary) {
        state.maxField = getQuotaField(state.miscForm.liquidMaxField.id)
        changeMisc('liquidMaxField', true)
      }
    } else {
      if (state.miscForm.gaugeMaxType === 'dynamic') {
        if (!state.miscForm.gaugeMaxField.id) {
          state.miscForm.gaugeMaxField.id = state.quotaData[0]?.id
        }
        if (!state.miscForm.gaugeMaxField.summary) {
          state.miscForm.gaugeMaxField.summary = 'count'
        }
        if (resetSummary) {
          state.miscForm.gaugeMaxField.summary = 'count'
        }
        if (state.miscForm.gaugeMaxField.id && state.miscForm.gaugeMaxField.summary) {
          state.maxField = getQuotaField(state.miscForm.gaugeMaxField.id)
          changeMisc('gaugeMaxField', true)
        }
      } else {
        if (state.miscForm.gaugeMinType === 'dynamic') {
          if (state.miscForm.gaugeMinField.id && state.miscForm.gaugeMinField.summary) {
            changeMisc('gaugeMaxField', true)
          }
        } else {
          changeMisc('gaugeMaxField', true)
        }
      }
    }
  }
}

const getQuotaField = id => {
  if (!id) {
    return {}
  }
  const fields = state.quotaData.filter(ele => {
    return ele.id === id
  })
  if (fields.length === 0) {
    return {}
  } else {
    return fields[0]
  }
}

const isValidField = field => {
  return field.id !== '-1' && state.quotaData.findIndex(ele => ele.id === field.id) !== -1
}

const showProperty = prop => props.propertyInner?.includes(prop)
const wordCloudDefaultDataRange = args => {
  state.miscForm.wordCloudAxisValueRange.max = args.data.max
  state.miscForm.wordCloudAxisValueRange.min = args.data.min
  state.miscForm.wordCloudAxisValueRange.fieldId = props.chart.yAxis?.[0]?.id
}
onMounted(() => {
  initField()
  init()
})
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
              v-for="item in state.quotaData"
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
        @change="changeMisc('gaugeMax')"
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
              v-for="item in state.quotaData"
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
        @change="changeMisc('liquidMax')"
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
              v-for="item in state.quotaData"
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
