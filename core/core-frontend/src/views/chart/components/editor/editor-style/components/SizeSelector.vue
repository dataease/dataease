<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  CHART_FONT_FAMILY,
  DEFAULT_SIZE,
  CHART_FONT_LETTER_SPACE
} from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onSizeChange'])

watch(
  () => props.chart,
  () => {
    init()
  }
)

const state = reactive({
  sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
  fontSize: []
})

const lineSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const liquidShapeOptions = [
  { name: t('chart.liquid_shape_circle'), value: 'circle' },
  { name: t('chart.liquid_shape_diamond'), value: 'diamond' },
  { name: t('chart.liquid_shape_triangle'), value: 'triangle' },
  { name: t('chart.liquid_shape_pin'), value: 'pin' },
  { name: t('chart.liquid_shape_rect'), value: 'rect' }
]

const pageSizeOptions = [
  { name: '10' + t('chart.table_page_size_unit'), value: '10' },
  { name: '20' + t('chart.table_page_size_unit'), value: '20' },
  { name: '50' + t('chart.table_page_size_unit'), value: '50' },
  { name: '100' + t('chart.table_page_size_unit'), value: '100' }
]

const alignOptions = [
  { name: t('chart.table_align_left'), value: 'left' },
  { name: t('chart.table_align_center'), value: 'center' },
  { name: t('chart.table_align_right'), value: 'right' }
]

const fontFamily = CHART_FONT_FAMILY

const fontLetterSpace = CHART_FONT_LETTER_SPACE

const lineTypeOptions = [
  { name: t('chart.map_line_type_line'), value: 'line' },
  { name: t('chart.map_line_type_arc'), value: 'arc' },
  { name: t('chart.map_line_type_arc_3d'), value: 'arc3d' }
]

const changeBarSizeCase = () => {
  emit('onSizeChange', state.sizeForm)
}

const initFontSize = () => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.size) {
      state.sizeForm = customAttr.size
    }
  }
}

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="sizeFormBar" :model="state.sizeForm" label-width="80px" size="small">
        <!--bar-begin-->
        <div v-show="props.chart.type.includes('bar')">
          <el-form-item :label="t('chart.adapt')" class="form-item">
            <el-checkbox
              v-model="state.sizeForm.barDefault"
              @change="changeBarSizeCase('barDefault')"
              >{{ t('chart.adapt') }}</el-checkbox
            >
          </el-form-item>
          <el-form-item :label="t('chart.bar_gap')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.barGap"
              :disabled="state.sizeForm.barDefault"
              :min="0"
              :max="5"
              :step="0.1"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('barGap')"
            />
          </el-form-item>
        </div>
        <!--bar-end-->

        <!--line-begin-->
        <div v-show="props.chart.type.includes('line') || props.chart.type.includes('area')">
          <el-form-item :label="t('chart.line_width')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.lineWidth"
              :min="0"
              :max="10"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('lineWidth')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.line_symbol')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.lineSymbol"
              :placeholder="t('chart.line_symbol')"
              @change="changeBarSizeCase('lineSymbol')"
            >
              <el-option
                v-for="item in lineSymbolOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.line_symbol_size')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.lineSymbolSize"
              :min="0"
              :max="20"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('lineSymbolSize')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.line_smooth')" class="form-item">
            <el-checkbox
              v-model="state.sizeForm.lineSmooth"
              @change="changeBarSizeCase('lineSmooth')"
              >{{ t('chart.line_smooth') }}
            </el-checkbox>
          </el-form-item>
        </div>
        <!--line-end-->

        <!--pie-begin-->
        <div v-show="props.chart.type.includes('pie')">
          <el-form-item
            :label="t('chart.pie_inner_radius_percent')"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.pieInnerRadius"
              :min="0"
              :max="100"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('pieInnerRadius')"
            />
          </el-form-item>
          <el-form-item
            :label="t('chart.pie_outer_radius_size')"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.pieOuterRadius"
              :min="0"
              :max="100"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('pieOuterRadius')"
            />
          </el-form-item>
        </div>
        <!--pie-end-->

        <!--table-begin-->
        <div v-show="props.chart.type.includes('table')">
          <el-form-item label-width="100px" :label="t('chart.table_page_mode')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.tablePageMode"
              :placeholder="t('chart.table_page_mode')"
              @change="changeBarSizeCase('tablePageMode')"
            >
              <el-option :label="t('chart.page_mode_page')" value="page" />
              <el-option :label="t('chart.page_mode_pull')" value="pull" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.tablePageMode === 'page'"
            label-width="100px"
            :label="t('chart.table_page_size')"
            class="form-item"
          >
            <el-select
              effect="dark"
              v-model="state.sizeForm.tablePageSize"
              :placeholder="t('chart.table_page_size')"
              @change="changeBarSizeCase('tablePageSize')"
            >
              <el-option
                v-for="item in pageSizeOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_title_fontsize')"
            class="form-item"
          >
            <el-select
              effect="dark"
              v-model="state.sizeForm.tableTitleFontSize"
              :placeholder="t('chart.table_title_fontsize')"
              @change="changeBarSizeCase('tableTitleFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_item_fontsize')"
            class="form-item"
          >
            <el-select
              effect="dark"
              v-model="state.sizeForm.tableItemFontSize"
              :placeholder="t('chart.table_item_fontsize')"
              @change="changeBarSizeCase('tableItemFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_header_align')"
            class="form-item"
          >
            <el-select
              effect="dark"
              v-model="state.sizeForm.tableHeaderAlign"
              :placeholder="t('chart.table_header_align')"
              @change="changeBarSizeCase('tableHeaderAlign')"
            >
              <el-option
                v-for="option in alignOptions"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label-width="100px" :label="t('chart.table_item_align')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.tableItemAlign"
              :placeholder="t('chart.table_item_align')"
              @change="changeBarSizeCase('tableItemAlign')"
            >
              <el-option
                v-for="option in alignOptions"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_title_height')"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.tableTitleHeight"
              :min="20"
              :max="100"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('tableTitleHeight')"
            />
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_item_height')"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.tableItemHeight"
              :min="20"
              :max="100"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('tableItemHeight')"
            />
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_column_width_config')"
            class="form-item"
          >
            <el-radio-group
              v-model="state.sizeForm.tableColumnMode"
              @change="changeBarSizeCase('tableColumnMode')"
            >
              <el-radio effect="dark" label="adapt"
                ><span>{{ t('chart.table_column_adapt') }}</span></el-radio
              >
              <el-radio effect="dark" label="custom">
                <span>{{ t('chart.table_column_custom') }}</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.tableColumnMode === 'custom'"
            label=""
            label-width="100px"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.tableColumnWidth"
              :min="10"
              :max="500"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('tableColumnWidth')"
            />
          </el-form-item>
          <el-form-item label-width="100px" :label="t('chart.table_show_index')" class="form-item">
            <el-radio-group
              v-model="state.sizeForm.showIndex"
              input-size="small"
              @change="changeBarSizeCase('showIndex')"
            >
              <el-radio effect="dark" :label="true">{{ t('panel.yes') }}</el-radio>
              <el-radio effect="dark" :label="false">{{ t('panel.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.showIndex"
            label-width="100px"
            :label="t('chart.table_index_desc')"
            class="form-item"
          >
            <el-input
              effect="dark"
              v-model="state.sizeForm.indexLabel"
              type="text"
              @blur="changeBarSizeCase('indexLabel')"
            />
          </el-form-item>
        </div>
        <!--table-end-->

        <!--gauge-begin-->
        <div v-show="props.chart.type.includes('gauge')">
          <!--          <el-form-item :label="t('chart.min')" class="form-item">-->
          <!--            <el-radio-group-->
          <!--              v-model="state.sizeForm.gaugeMinType"-->
          <!--              size="small"-->
          <!--              @change="changeQuotaField('min')"-->
          <!--            >-->
          <!--              <el-radio-button effect="dark" label="fix">{{ t('chart.fix') }}</el-radio-button>-->
          <!--              &lt;!&ndash;              <el-radio-button label="dynamic">{{ t('chart.dynamic') }}</el-radio-button>&ndash;&gt;-->
          <!--            </el-radio-group>-->
          <!--          </el-form-item>-->
          <el-form-item
            :label="t('chart.min')"
            v-if="state.sizeForm.gaugeMinType === 'fix'"
            class="form-item form-item-slider"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.gaugeMin"
              size="small"
              @change="changeBarSizeCase('gaugeMin')"
            />
          </el-form-item>
          <!--          <el-form-item-->
          <!--            v-if="sizeForm.gaugeMinType === 'dynamic'"-->
          <!--            class="form-item form-flex"-->
          <!--          >-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.gaugeMinField.id"-->
          <!--              :placeholder="t('chart.field')"-->
          <!--              @change="changeQuotaField('min', true)"-->
          <!--            >-->
          <!--              <el-option-->
          <!--                v-for="item in quotaData"-->
          <!--                :key="item.id"-->
          <!--                :label="item.name"-->
          <!--                :value="item.id"-->
          <!--              >-->
          <!--                <span style="float: left">-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 0"-->
          <!--                    icon-class="field_text"-->
          <!--                    class="field-icon-text"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 1"-->
          <!--                    icon-class="field_time"-->
          <!--                    class="field-icon-time"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 2 || item.deType === 3"-->
          <!--                    icon-class="field_value"-->
          <!--                    class="field-icon-value"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 5"-->
          <!--                    icon-class="field_location"-->
          <!--                    class="field-icon-location"-->
          <!--                  />-->
          <!--                </span>-->
          <!--                <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>-->
          <!--              </el-option>-->
          <!--            </el-select>-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.gaugeMinField.summary"-->
          <!--              :placeholder="t('chart.summary')"-->
          <!--              @change="changeQuotaField('min')"-->
          <!--            >-->
          <!--              <el-option v-if="validMinField" key="sum" value="sum" :label="t('chart.sum')" />-->
          <!--              <el-option v-if="validMinField" key="avg" value="avg" :label="t('chart.avg')" />-->
          <!--              <el-option v-if="validMinField" key="max" value="max" :label="t('chart.max')" />-->
          <!--              <el-option v-if="validMinField" key="min" value="min" :label="t('chart.min')" />-->
          <!--              <el-option-->
          <!--                v-if="validMinField"-->
          <!--                key="stddev_pop"-->
          <!--                value="stddev_pop"-->
          <!--                :label="t('chart.stddev_pop')"-->
          <!--              />-->
          <!--              <el-option-->
          <!--                v-if="validMinField"-->
          <!--                key="var_pop"-->
          <!--                value="var_pop"-->
          <!--                :label="t('chart.var_pop')"-->
          <!--              />-->
          <!--              <el-option key="count" value="count" :label="t('chart.count')" />-->
          <!--              <el-option-->
          <!--                v-if="minField.id !== 'count'"-->
          <!--                key="count_distinct"-->
          <!--                value="count_distinct"-->
          <!--                :label="t('chart.count_distinct')"-->
          <!--              />-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->

          <!--          <el-form-item :label="t('chart.max')" class="form-item">-->
          <!--            <el-radio-group-->
          <!--              v-model="state.sizeForm.gaugeMaxType"-->
          <!--              size="small"-->
          <!--              @change="changeQuotaField('max')"-->
          <!--            >-->
          <!--              <el-radio-button effect="dark" label="fix">{{ t('chart.fix') }}</el-radio-button>-->
          <!--              &lt;!&ndash;              <el-radio-button label="dynamic">{{ t('chart.dynamic') }}</el-radio-button>&ndash;&gt;-->
          <!--            </el-radio-group>-->
          <!--          </el-form-item>-->
          <el-form-item
            v-if="state.sizeForm.gaugeMaxType === 'fix'"
            class="form-item form-item-slider"
            :label="t('chart.max')"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.gaugeMax"
              size="small"
              @change="changeBarSizeCase('gaugeMax')"
            />
          </el-form-item>
          <!--          <el-form-item-->
          <!--            v-if="sizeForm.gaugeMaxType === 'dynamic'"-->
          <!--            class="form-item form-flex"-->
          <!--          >-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.gaugeMaxField.id"-->
          <!--              :placeholder="t('chart.field')"-->
          <!--              @change="changeQuotaField('max', true)"-->
          <!--            >-->
          <!--              <el-option-->
          <!--                v-for="item in quotaData"-->
          <!--                :key="item.id"-->
          <!--                :label="item.name"-->
          <!--                :value="item.id"-->
          <!--              >-->
          <!--                <span style="float: left">-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 0"-->
          <!--                    icon-class="field_text"-->
          <!--                    class="field-icon-text"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 1"-->
          <!--                    icon-class="field_time"-->
          <!--                    class="field-icon-time"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 2 || item.deType === 3"-->
          <!--                    icon-class="field_value"-->
          <!--                    class="field-icon-value"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 5"-->
          <!--                    icon-class="field_location"-->
          <!--                    class="field-icon-location"-->
          <!--                  />-->
          <!--                </span>-->
          <!--                <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>-->
          <!--              </el-option>-->
          <!--            </el-select>-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.gaugeMaxField.summary"-->
          <!--              :placeholder="t('chart.summary')"-->
          <!--              @change="changeQuotaField('max')"-->
          <!--            >-->
          <!--              <el-option v-if="validMaxField" key="sum" value="sum" :label="t('chart.sum')" />-->
          <!--              <el-option v-if="validMaxField" key="avg" value="avg" :label="t('chart.avg')" />-->
          <!--              <el-option v-if="validMaxField" key="max" value="max" :label="t('chart.max')" />-->
          <!--              <el-option v-if="validMaxField" key="min" value="min" :label="t('chart.min')" />-->
          <!--              <el-option-->
          <!--                v-if="validMaxField"-->
          <!--                key="stddev_pop"-->
          <!--                value="stddev_pop"-->
          <!--                :label="t('chart.stddev_pop')"-->
          <!--              />-->
          <!--              <el-option-->
          <!--                v-if="validMaxField"-->
          <!--                key="var_pop"-->
          <!--                value="var_pop"-->
          <!--                :label="t('chart.var_pop')"-->
          <!--              />-->
          <!--              <el-option key="count" value="count" :label="t('chart.count')" />-->
          <!--              <el-option-->
          <!--                v-if="maxField.id !== 'count'"-->
          <!--                key="count_distinct"-->
          <!--                value="count_distinct"-->
          <!--                :label="t('chart.count_distinct')"-->
          <!--              />-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->

          <el-form-item :label="t('chart.start_angle')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.gaugeStartAngle"
              :min="-360"
              :max="360"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('gaugeStartAngle')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.end_angle')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.gaugeEndAngle"
              :min="-360"
              :max="360"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('gaugeEndAngle')"
            />
          </el-form-item>
        </div>
        <!--gauge-end-->

        <!--liquid-begin-->
        <div v-show="props.chart.type.includes('liquid')">
          <el-form-item :label="t('chart.liquid_shape')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.liquidShape"
              :placeholder="t('chart.liquid_shape')"
              @change="changeBarSizeCase('liquidShape')"
            >
              <el-option
                v-for="item in liquidShapeOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <!--          <el-form-item :label="t('chart.liquid_max')" class="form-item">-->
          <!--            <el-radio-group-->
          <!--              v-model="state.sizeForm.liquidMaxType"-->
          <!--              size="small"-->
          <!--              @change="changeQuotaField('max')"-->
          <!--            >-->
          <!--              <el-radio-button effect="dark" label="fix">{{ t('chart.fix') }}</el-radio-button>-->
          <!--              &lt;!&ndash;              <el-radio-button label="dynamic">{{ t('chart.dynamic') }}</el-radio-button>&ndash;&gt;-->
          <!--            </el-radio-group>-->
          <!--          </el-form-item>-->
          <el-form-item
            v-if="state.sizeForm.liquidMaxType === 'fix'"
            class="form-item form-item-slider"
            :label="t('chart.liquid_max')"
          >
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.liquidMax"
              :min="1"
              size="small"
              @change="changeBarSizeCase('liquidMax')"
            />
          </el-form-item>
          <!--          <el-form-item-->
          <!--            v-if="state.sizeForm.liquidMaxType === 'dynamic'"-->
          <!--            class="form-item form-flex"-->
          <!--          >-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.liquidMaxField.id"-->
          <!--              :placeholder="t('chart.field')"-->
          <!--              @change="changeQuotaField('max', true)"-->
          <!--            >-->
          <!--              <el-option-->
          <!--                v-for="item in quotaData"-->
          <!--                :key="item.id"-->
          <!--                :label="item.name"-->
          <!--                :value="item.id"-->
          <!--              >-->
          <!--                <span style="float: left">-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 0"-->
          <!--                    icon-class="field_text"-->
          <!--                    class="field-icon-text"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 1"-->
          <!--                    icon-class="field_time"-->
          <!--                    class="field-icon-time"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 2 || item.deType === 3"-->
          <!--                    icon-class="field_value"-->
          <!--                    class="field-icon-value"-->
          <!--                  />-->
          <!--                  <svg-icon-->
          <!--                    v-if="item.deType === 5"-->
          <!--                    icon-class="field_location"-->
          <!--                    class="field-icon-location"-->
          <!--                  />-->
          <!--                </span>-->
          <!--                <span style="float: left; color: #8492a6; font-size: 12px">{{ item.name }}</span>-->
          <!--              </el-option>-->
          <!--            </el-select>-->
          <!--            <el-select-->
          <!--              v-model="sizeForm.liquidMaxField.summary"-->
          <!--              :placeholder="t('chart.summary')"-->
          <!--              @change="changeQuotaField('max')"-->
          <!--            >-->
          <!--              <el-option v-if="validLiquidMaxField" key="sum" value="sum" :label="t('chart.sum')" />-->
          <!--              <el-option v-if="validLiquidMaxField" key="avg" value="avg" :label="t('chart.avg')" />-->
          <!--              <el-option v-if="validLiquidMaxField" key="max" value="max" :label="t('chart.max')" />-->
          <!--              <el-option v-if="validLiquidMaxField" key="min" value="min" :label="t('chart.min')" />-->
          <!--              <el-option-->
          <!--                v-if="validLiquidMaxField"-->
          <!--                key="stddev_pop"-->
          <!--                value="stddev_pop"-->
          <!--                :label="t('chart.stddev_pop')"-->
          <!--              />-->
          <!--              <el-option-->
          <!--                v-if="validLiquidMaxField"-->
          <!--                key="var_pop"-->
          <!--                value="var_pop"-->
          <!--                :label="t('chart.var_pop')"-->
          <!--              />-->
          <!--              <el-option key="count" value="count" :label="t('chart.count')" />-->
          <!--              <el-option-->
          <!--                v-if="liquidMaxField.id !== 'count'"-->
          <!--                key="count_distinct"-->
          <!--                value="count_distinct"-->
          <!--                :label="t('chart.count_distinct')"-->
          <!--              />-->
          <!--            </el-select>-->
          <!--          </el-form-item>-->

          <el-form-item :label="t('chart.radar_size')" class="form-item form-item-slider">
            <el-input-number
              effect="dark"
              v-model="state.sizeForm.liquidSize"
              :min="1"
              :max="100"
              size="small"
              controls-position="right"
              @change="changeBarSizeCase('liquidSize')"
            />
          </el-form-item>
        </div>
        <!--liquid-end-->

        <!--text&label-start-->
        <div v-show="props.chart.type.includes('text') || props.chart.type.includes('label')">
          <el-form-item :label="t('chart.quota_font_size')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.quotaFontSize"
              :placeholder="t('chart.quota_font_size')"
              @change="changeBarSizeCase('quotaFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.quota_font_family')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.quotaFontFamily"
              :placeholder="t('chart.quota_font_family')"
              @change="changeBarSizeCase('quotaFontFamily')"
            >
              <el-option
                v-for="option in fontFamily"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.quota_text_style')" class="form-item">
            <el-checkbox
              effect="dark"
              v-model="state.sizeForm.quotaFontIsItalic"
              @change="changeBarSizeCase('quotaFontIsItalic')"
              >{{ t('chart.italic') }}</el-checkbox
            >
            <el-checkbox
              effect="dark"
              v-model="state.sizeForm.quotaFontIsBolder"
              @change="changeBarSizeCase('quotaFontIsBolder')"
              >{{ t('chart.bolder') }}</el-checkbox
            >
          </el-form-item>
          <el-form-item :label="t('chart.quota_letter_space')" class="form-item">
            <el-select
              effect="dark"
              v-model="state.sizeForm.quotaLetterSpace"
              :placeholder="t('chart.quota_letter_space')"
              @change="changeBarSizeCase('quotaLetterSpace')"
            >
              <el-option
                v-for="option in fontLetterSpace"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.font_shadow')" class="form-item">
            <el-checkbox
              effect="dark"
              v-model="state.sizeForm.quotaFontShadow"
              @change="changeBarSizeCase('quotaFontShadow')"
              >{{ t('chart.font_shadow') }}</el-checkbox
            >
          </el-form-item>
          <el-divider />
          <el-form-item :label="t('chart.dimension_show')" class="form-item">
            <el-checkbox
              effect="dark"
              v-model="state.sizeForm.dimensionShow"
              @change="changeBarSizeCase('dimensionShow')"
              >{{ t('chart.show') }}</el-checkbox
            >
          </el-form-item>
          <div v-show="state.sizeForm.dimensionShow">
            <el-form-item :label="t('chart.dimension_font_size')" class="form-item">
              <el-select
                effect="dark"
                v-model="state.sizeForm.dimensionFontSize"
                :placeholder="t('chart.dimension_font_size')"
                @change="changeBarSizeCase('dimensionFontSize')"
              >
                <el-option
                  v-for="option in state.fontSize"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('chart.dimension_font_family')" class="form-item">
              <el-select
                effect="dark"
                v-model="state.sizeForm.dimensionFontFamily"
                :placeholder="t('chart.dimension_font_family')"
                @change="changeBarSizeCase('dimensionFontFamily')"
              >
                <el-option
                  v-for="option in fontFamily"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('chart.dimension_text_style')" class="form-item">
              <el-checkbox
                effect="dark"
                v-model="state.sizeForm.dimensionFontIsItalic"
                @change="changeBarSizeCase('dimensionFontIsItalic')"
                >{{ t('chart.italic') }}</el-checkbox
              >
              <el-checkbox
                effect="dark"
                v-model="state.sizeForm.dimensionFontIsBolder"
                @change="changeBarSizeCase('dimensionFontIsBolder')"
                >{{ t('chart.bolder') }}</el-checkbox
              >
            </el-form-item>
            <el-form-item :label="t('chart.dimension_letter_space')" class="form-item">
              <el-select
                effect="dark"
                v-model="state.sizeForm.dimensionLetterSpace"
                :placeholder="t('chart.dimension_letter_space')"
                @change="changeBarSizeCase('dimensionLetterSpace')"
              >
                <el-option
                  v-for="option in fontLetterSpace"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('chart.font_shadow')" class="form-item">
              <el-checkbox
                effect="dark"
                v-model="state.sizeForm.dimensionFontShadow"
                @change="changeBarSizeCase('dimensionFontShadow')"
                >{{ t('chart.font_shadow') }}</el-checkbox
              >
            </el-form-item>
            <el-divider />
            <el-form-item :label="t('chart.space_split')" class="form-item">
              <el-input-number
                effect="dark"
                v-model="state.sizeForm.spaceSplit"
                :min="0"
                size="small"
                @change="changeBarSizeCase('spaceSplit')"
              />
            </el-form-item>
            <el-form-item :label="t('chart.h_position')" class="form-item">
              <el-select
                effect="dark"
                v-model="state.sizeForm.hPosition"
                :placeholder="t('chart.h_position')"
                @change="changeBarSizeCase('hPosition')"
              >
                <el-option value="start" :label="t('chart.p_left')">{{
                  t('chart.p_left')
                }}</el-option>
                <el-option value="center" :label="t('chart.p_center')">{{
                  t('chart.p_center')
                }}</el-option>
                <el-option value="end" :label="t('chart.p_right')">{{
                  t('chart.p_right')
                }}</el-option>
              </el-select>
            </el-form-item>
            <el-form-item :label="t('chart.v_position')" class="form-item">
              <el-select
                effect="dark"
                v-model="state.sizeForm.vPosition"
                :placeholder="t('chart.v_position')"
                @change="changeBarSizeCase('vPosition')"
              >
                <el-option value="start" :label="t('chart.p_top')">{{
                  t('chart.p_top')
                }}</el-option>
                <el-option value="center" :label="t('chart.p_center')">{{
                  t('chart.p_center')
                }}</el-option>
                <el-option value="end" :label="t('chart.p_bottom')">{{
                  t('chart.p_bottom')
                }}</el-option>
              </el-select>
            </el-form-item>
          </div>
        </div>
        <!--text&label-end-->
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
