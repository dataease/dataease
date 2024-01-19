<script lang="ts" setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  CHART_FONT_FAMILY,
  DEFAULT_MISC,
  CHART_FONT_LETTER_SPACE
} from '@/views/chart/components/editor/util/chart'
import { ElMessage, ElRow } from 'element-plus-secondary'
import { fieldType } from '@/utils/attr'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: any
    themes?: EditorTheme
    quotaFields: Array<any>
    propertyInner?: Array<string>
  }>(),
  { themes: 'dark' }
)

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

const fontFamily = CHART_FONT_FAMILY

const fontLetterSpace = CHART_FONT_LETTER_SPACE

const changeMisc = (prop = '', refresh = false) => {
  if (state.miscForm.gaugeMax <= state.miscForm.gaugeMin) {
    ElMessage.error(t('chart.max_more_than_mix'))
  }
  emit('onMiscChange', { data: state.miscForm, requestData: refresh }, prop)
}

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.misc) {
      state.miscForm = customAttr.misc
    }
  }
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

onMounted(() => {
  initField()
  init()
})
</script>

<template>
  <el-form :model="state.miscForm">
    <el-row :gutter="8" v-if="!props.chart.type.includes('liquid')">
      <el-col :span="12">
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
      <el-col :span="12">
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
    <template v-if="props.chart.type.includes('gauge')">
      <el-form-item
        :label="t('chart.min')"
        class="form-item margin-bottom-8"
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
        v-if="state.miscForm.gaugeMinType === 'fix'"
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
      <el-row :gutter="8" v-if="state.miscForm.gaugeMinType === 'dynamic'">
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
                  <Icon
                    :className="`field-icon-${fieldType[item.deType]}`"
                    :name="`field_${fieldType[item.deType]}`"
                  />
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
        :label="t('chart.max')"
        class="form-item margin-bottom-8"
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
        v-if="state.miscForm.gaugeMaxType === 'fix'"
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
      <el-row :gutter="8" v-if="state.miscForm.gaugeMaxType === 'dynamic'">
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
                  <Icon
                    :className="`field-icon-${fieldType[item.deType]}`"
                    :name="`field_${fieldType[item.deType]}`"
                  />
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
    <template v-if="props.chart.type.includes('liquid')">
      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item
            :label="t('chart.liquid_shape')"
            class="form-item"
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
            :label="t('chart.radar_size')"
            class="form-item"
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
        :label="t('chart.liquid_max')"
        class="form-item margin-bottom-8"
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
        v-if="state.miscForm.liquidMaxType === 'fix'"
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

      <el-row :gutter="8" v-if="state.miscForm.liquidMaxType === 'dynamic'">
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
                  <Icon
                    :className="`field-icon-${fieldType[item.deType]}`"
                    :name="`field_${fieldType[item.deType]}`"
                  />
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
    </template>
    <!--liquid-end-->

    <!--text&label-start-->
    <template v-if="props.chart.type.includes('indicator') || props.chart.type.includes('label')">
      <el-form-item
        :label="t('chart.quota_font_size')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.miscForm.quotaFontSize"
          :placeholder="t('chart.quota_font_size')"
          @change="changeMisc('quotaFontSize')"
        >
          <el-option
            v-for="option in fontSizeList"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="t('chart.quota_font_family')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.miscForm.quotaFontFamily"
          :placeholder="t('chart.quota_font_family')"
          @change="changeMisc('quotaFontFamily')"
        >
          <el-option
            v-for="option in fontFamily"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="t('chart.quota_text_style')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.miscForm.quotaFontIsItalic"
          @change="changeMisc('quotaFontIsItalic')"
        >
          {{ t('chart.italic') }}
        </el-checkbox>
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.miscForm.quotaFontIsBolder"
          @change="changeMisc('quotaFontIsBolder')"
        >
          {{ t('chart.bolder') }}
        </el-checkbox>
      </el-form-item>
      <el-form-item
        :label="t('chart.quota_letter_space')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.miscForm.quotaLetterSpace"
          :placeholder="t('chart.quota_letter_space')"
          @change="changeMisc('quotaLetterSpace')"
        >
          <el-option
            v-for="option in fontLetterSpace"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        :label="t('chart.font_shadow')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.miscForm.quotaFontShadow"
          @change="changeMisc('quotaFontShadow')"
          >{{ t('chart.font_shadow') }}</el-checkbox
        >
      </el-form-item>
      <el-divider />
      <el-form-item
        :label="t('chart.dimension_show')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.miscForm.dimensionShow"
          @change="changeMisc('dimensionShow')"
        >
          {{ t('chart.show') }}
        </el-checkbox>
      </el-form-item>
      <template v-if="state.miscForm.dimensionShow">
        <el-form-item
          :label="t('chart.dimension_font_size')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.dimensionFontSize"
            :placeholder="t('chart.dimension_font_size')"
            @change="changeMisc('dimensionFontSize')"
          >
            <el-option
              v-for="option in fontSizeList"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('chart.dimension_font_family')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.dimensionFontFamily"
            :placeholder="t('chart.dimension_font_family')"
            @change="changeMisc('dimensionFontFamily')"
          >
            <el-option
              v-for="option in fontFamily"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('chart.dimension_text_style')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.miscForm.dimensionFontIsItalic"
            @change="changeMisc('dimensionFontIsItalic')"
          >
            {{ t('chart.italic') }}
          </el-checkbox>
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.miscForm.dimensionFontIsBolder"
            @change="changeMisc('dimensionFontIsBolder')"
          >
            {{ t('chart.bolder') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item
          :label="t('chart.dimension_letter_space')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.dimensionLetterSpace"
            :placeholder="t('chart.dimension_letter_space')"
            @change="changeMisc('dimensionLetterSpace')"
          >
            <el-option
              v-for="option in fontLetterSpace"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('chart.font_shadow')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-checkbox
            size="small"
            :effect="themes"
            v-model="state.miscForm.dimensionFontShadow"
            @change="changeMisc('dimensionFontShadow')"
          >
            {{ t('chart.font_shadow') }}
          </el-checkbox>
        </el-form-item>
        <el-divider />
        <el-form-item
          :label="t('chart.space_split')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :effect="themes"
            v-model="state.miscForm.spaceSplit"
            :min="0"
            size="small"
            controls-position="right"
            @change="changeMisc('spaceSplit')"
          />
        </el-form-item>
        <el-form-item
          :label="t('chart.h_position')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.hPosition"
            :placeholder="t('chart.h_position')"
            @change="changeMisc('hPosition')"
          >
            <el-option value="start" :label="t('chart.p_left')">{{ t('chart.p_left') }}</el-option>
            <el-option value="center" :label="t('chart.p_center')">{{
              t('chart.p_center')
            }}</el-option>
            <el-option value="end" :label="t('chart.p_right')">{{ t('chart.p_right') }}</el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="t('chart.v_position')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            :effect="themes"
            v-model="state.miscForm.vPosition"
            :placeholder="t('chart.v_position')"
            @change="changeMisc('vPosition')"
          >
            <el-option value="start" :label="t('chart.p_top')">{{ t('chart.p_top') }}</el-option>
            <el-option value="center" :label="t('chart.p_center')">{{
              t('chart.p_center')
            }}</el-option>
            <el-option value="end" :label="t('chart.p_bottom')">{{
              t('chart.p_bottom')
            }}</el-option>
          </el-select>
        </el-form-item>
      </template>
    </template>
    <!--text&label-end-->
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
</style>
