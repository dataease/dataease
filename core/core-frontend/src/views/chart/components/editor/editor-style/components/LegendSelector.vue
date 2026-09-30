<script lang="tsx" setup>
import icon_leftAlign_outlined from '@/assets/svg/icon_left-align_outlined.svg'
import icon_horizontalAlign_outlined from '@/assets/svg/icon_horizontal-align_outlined.svg'
import icon_rightAlign_outlined from '@/assets/svg/icon_right-align_outlined.svg'
import icon_topAlign_outlined from '@/assets/svg/icon_top-align_outlined.svg'
import icon_verticalAlign_outlined from '@/assets/svg/icon_vertical-align_outlined.svg'
import icon_bottomAlign_outlined from '@/assets/svg/icon_bottom-align_outlined.svg'
import { computed, onMounted, reactive, watch, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_MISC
} from '@/views/chart/components/editor/util/chart'
import { ElCol, ElFormItem, ElRow, ElSpace } from 'element-plus-secondary'
import { cloneDeep, get, set } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { getDynamicColorScale } from '@/views/chart/components/js/util'
import CustomSortEdit from '@/views/chart/components/editor/drag-item/components/CustomSortEdit.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import chartViewManager from '@/views/chart/components/js/panel'
import { mapRangeKey } from '@/views/chart/components/js/panel/charts/map/mapLegend'
import MapRangeEditor from './MapRangeEditor.vue'
import MapLegendEditor from './MapLegendEditor.vue'
import HeatmapLegendEditor from './HeatmapLegendEditor.vue'
const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus } = storeToRefs(dvMainStore)
const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: any
    themes?: EditorTheme
    propertyInner: Array<string>
  }>(),
  { themes: 'dark' }
)
useEmitt({
  name: 'map-default-range',
  callback: args => mapDefaultRange(args)
})
const emit = defineEmits(['onLegendChange', 'onMiscChange'])
const toolTip = computed(() => {
  return props.themes || 'dark'
})
watch(
  () => props.chart.customStyle,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL
const iconSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const state = reactive({
  legendForm: {
    ...JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE)),
    miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC)) as ChartMiscAttr
  },
  showCustomSort: false,
  customSortField: null
})

const chartType = computed(() => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  return chart?.type
})

const supportLegendOrient = computed(() => {
  return chartViewManager.getChartView(props.chart.render, props.chart.type)?.legendCapabilities
    ?.orient
})

const supportsTileLegend = computed(() => {
  const view = chartViewManager.getChartView(props.chart.render, props.chart.type)
  if (view?.library !== 'g2' || view.legendCapabilities?.type === 'continuous') return false
  if (view.legendCapabilities?.type === 'dynamic')
    return props.chart.extColor?.[0]?.groupType === 'd'
  return true
})

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  for (let i = 50; i <= 200; i = i + 10) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const sizeList = computed(() => {
  const arr = []
  for (let i = 4; i <= 20; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const changeLegendStyle = prop => {
  emit('onLegendChange', state.legendForm, prop)
}

const changeHeatmapLegend = (value: HeatmapLegendOptions) => {
  state.legendForm.heatmap = value
  changeLegendStyle('heatmap')
}

const changeMapLegend = (value: MapLegendOptions) => {
  state.legendForm.map = value
  changeLegendStyle('map')
}

const changeMisc = prop => {
  // 仅对子弹图区间图例字段做合并保护，避免覆盖 fixedRange/showType。
  if (typeof prop === 'string' && prop.startsWith('bullet.')) {
    const latestMisc = cloneDeep(props.chart?.customAttr?.misc || state.legendForm.miscForm)
    set(latestMisc, prop, get(state.legendForm.miscForm, prop))
    emit('onMiscChange', { data: latestMisc, requestData: true }, prop)
    return
  }
  emit('onMiscChange', { data: state.legendForm.miscForm, requestData: true }, prop)
}

const legendSort = ref()
const init = () => {
  legendSort.value?.blur()
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    const miscStyle = cloneDeep(props.chart.customAttr.misc)
    if (customStyle.legend) {
      state.legendForm = { ...DEFAULT_LEGEND_STYLE, ...customStyle.legend }
      state.legendForm.miscForm = miscStyle
      if (chartType.value === 'map') {
        // 解决存量地图，没有设置mapAutoLegend的情况，设置默认值
        if (!state.legendForm.miscForm.hasOwnProperty('mapAutoLegend')) {
          state.legendForm.miscForm.mapAutoLegend = true
        }
        if (!state.legendForm.miscForm.hasOwnProperty('mapLegendRangeType')) {
          state.legendForm.miscForm.mapLegendRangeType = 'quantize'
        }
        if (!state.legendForm.miscForm.hasOwnProperty('mapLegendCustomRange')) {
          state.legendForm.miscForm.mapLegendCustomRange = []
        }
        initMapCustomRange()
      }
    }
  }
}
const showProperty = prop => props.propertyInner?.includes(prop)
const mapDefaultRange = args => {
  if (args.from !== 'map' || String(args.chartId) !== String(props.chart.id)) return
  if (state.legendForm.miscForm.mapLegendRangeType !== 'custom') {
    state.legendForm.miscForm.mapLegendMax = args.data.max
    state.legendForm.miscForm.mapLegendMin = args.data.min
  }
}
const initMapCustomRange = () => {
  const misc = state.legendForm.miscForm
  if (!misc.mapAutoLegend && misc.mapLegendRangeType === 'custom') {
    if (misc.mapLegendCustomRange.length < 2) calcMapCustomRange()
    misc.mapLegendNumber = misc.mapLegendCustomRange.length - 1
  }
}
const calcMapCustomRange = () => {
  const misc = state.legendForm.miscForm
  const min = Number.isFinite(misc.mapLegendMin) ? misc.mapLegendMin : 0
  const max =
    Number.isFinite(misc.mapLegendMax) && misc.mapLegendMax > min ? misc.mapLegendMax : min + 1
  const count = Math.min(9, Math.max(1, misc.mapLegendNumber || DEFAULT_MISC.mapLegendNumber))
  const ranges = getDynamicColorScale(min, max, count)
  misc.mapLegendCustomRange = [ranges[0].value[0], ...ranges.map(item => item.value[1])]
}
const changeLegendCustomType = (prop?) => {
  // Retain the manual boundaries while another mode is active.
  initMapCustomRange()
  prop ? changeMisc(prop) : ''
}
const changeLegendNumber = (prop?) => {
  if (!state.legendForm.miscForm.mapLegendNumber) {
    state.legendForm.miscForm.mapLegendNumber = DEFAULT_MISC.mapLegendNumber
  }
  prop ? changeMisc(prop) : ''
}
const changeMapBoundaries = (values: number[]) => {
  state.legendForm.miscForm.mapLegendCustomRange = values
  state.legendForm.miscForm.mapLegendNumber = values.length - 1
  const labels = state.legendForm.map?.rangeLabels
  if (labels) {
    const keys = new Set(
      values.slice(0, -1).map((value, index) => mapRangeKey([value, values[index + 1]]))
    )
    const rangeLabels = Object.fromEntries(Object.entries(labels).filter(([key]) => keys.has(key)))
    if (Object.keys(rangeLabels).length !== Object.keys(labels).length) {
      state.legendForm.map = { ...state.legendForm.map, rangeLabels }
      changeLegendStyle('map')
    }
  }
  emit(
    'onMiscChange',
    { data: state.legendForm.miscForm, requestData: false },
    'mapLegendCustomRange'
  )
}
const customSort = []
const changeLegendSort = sort => {
  if (sort === 'custom') {
    state.customSortField = cloneDeep(props.chart.xAxisExt?.[0])
    if (!state.customSortField) {
      return
    }
    state.showCustomSort = true
  } else {
    state.showCustomSort = false
    state.legendForm.sort = sort
    changeLegendStyle('sort')
  }
}
const closeCustomSort = () => {
  state.showCustomSort = false
}
const saveCustomSort = () => {
  state.showCustomSort = false
  state.legendForm.customSort = customSort
  changeLegendStyle('customSort')
}
const customSortChange = list => {
  customSort.splice(0, customSort.length, ...list)
}
onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="legendForm"
    :disabled="!state.legendForm.show"
    :model="state.legendForm"
    label-position="top"
    size="small"
  >
    <HeatmapLegendEditor
      v-if="
        chartType === 't-heatmap' &&
        chart.extColor?.[0]?.groupType === 'q' &&
        chartViewManager.getChartView(chart.render, chart.type)?.library === 'g2'
      "
      :model-value="state.legendForm.heatmap"
      :chart="chart"
      :themes="themes"
      @update:model-value="changeHeatmapLegend"
    />
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          :label="t('chart.icon')"
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('icon')"
        >
          <el-select
            :effect="themes"
            v-model="state.legendForm.icon"
            :placeholder="t('chart.icon')"
            @change="changeLegendStyle('icon')"
          >
            <el-option
              v-for="item in iconSymbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-col>

      <el-col :span="12">
        <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('icon')">
          <template #label>&nbsp;</template>
          <el-select
            :effect="themes"
            v-model="state.legendForm.size"
            size="small"
            @change="changeLegendStyle('size')"
          >
            <el-option
              v-for="option in sizeList"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item v-if="showProperty('showRange')" class="form-item" :class="'form-item-' + themes">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.legendForm.showRange"
        @change="changeLegendStyle('showRange')"
        :label="t('chart.show_range_bg')"
      />
    </el-form-item>
    <div
      style="flex: 1; display: flex; width: 100%"
      v-if="showProperty('showRange') && state.legendForm.showRange"
    >
      <el-form-item
        :label="t('chart.icon')"
        class="form-item"
        :class="'form-item-' + themes"
        style="flex: 1; min-width: 0"
      >
        <el-select
          :effect="themes"
          v-model="state.legendForm.miscForm.bullet.bar.ranges.symbol"
          @change="changeMisc('bullet.bar.ranges.symbol')"
        >
          <el-option
            v-for="item in iconSymbolOptions"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        style="flex: 1; min-width: 0; padding-left: 8px"
      >
        <template #label>&nbsp;</template>
        <el-select
          :effect="themes"
          v-model="state.legendForm.miscForm.bullet.bar.ranges.symbolSize"
          size="small"
          @change="changeMisc('bullet.bar.ranges.symbolSize')"
        >
          <el-option
            v-for="option in sizeList"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </div>
    <el-space>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('color')"
        :label="t('chart.text')"
      >
        <el-color-picker
          v-model="state.legendForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeLegendStyle('color')"
          :effect="themes"
          is-custom
        />
      </el-form-item>

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('fontSize')"
      >
        <template #label> &nbsp; </template>
        <el-tooltip :content="t('chart.font_size')" :effect="toolTip" placement="top">
          <el-select
            style="width: 108px"
            :effect="themes"
            v-model="state.legendForm.fontSize"
            :placeholder="t('chart.text_fontsize')"
            size="small"
            @change="changeLegendStyle('fontSize')"
          >
            <el-option
              v-for="option in fontSizeList"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-tooltip>
      </el-form-item>
    </el-space>
    <el-space style="width: 100%">
      <div v-if="chartType === 'map'">
        <el-row>
          <el-col>
            <el-form-item
              class="form-item"
              :class="'form-item-' + themes"
              :label="t('chart.legend')"
              prop="miscForm.mapAutoLegend"
            >
              <el-radio
                size="small"
                :effect="themes"
                v-model="state.legendForm.miscForm.mapAutoLegend"
                :value="true"
                @change="changeLegendCustomType('mapAutoLegend')"
                style="width: 80px"
              >
                {{ t('chart.margin_model_auto') }}
              </el-radio>
              <el-radio
                size="small"
                :effect="themes"
                v-model="state.legendForm.miscForm.mapAutoLegend"
                :value="false"
                @change="changeLegendCustomType('mapAutoLegend')"
              >
                {{ t('chart.custom_case') }}
              </el-radio>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="!state.legendForm.miscForm.mapAutoLegend">
          <el-row>
            <el-col>
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                :label="t('chart.legend_range_division')"
                prop="miscForm.mapLegendRangeType"
              >
                <el-radio
                  size="small"
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendRangeType"
                  :value="'quantize'"
                  @change="changeLegendCustomType('mapLegendRangeType')"
                  style="width: 75px"
                >
                  {{ t('chart.legend_equal_range') }}
                </el-radio>
                <el-radio
                  size="small"
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendRangeType"
                  :value="'custom'"
                  @change="changeLegendCustomType('mapLegendRangeType')"
                >
                  {{ t('chart.legend_custom_range') }}
                </el-radio>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-if="state.legendForm.miscForm.mapLegendRangeType === 'quantize'">
            <el-col>
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                :label="t('chart.legend_num')"
              >
                <el-input-number
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendNumber"
                  :precision="0"
                  :min="1"
                  :max="9"
                  :step="1"
                  :controls="true"
                  controls-position="right"
                  @change="changeLegendNumber('mapLegendNumber')"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <MapRangeEditor
            v-if="state.legendForm.miscForm.mapLegendRangeType === 'custom'"
            :model-value="state.legendForm.miscForm.mapLegendCustomRange"
            :themes="themes"
            @update:model-value="changeMapBoundaries"
          />
          <el-row :gutter="8" v-if="state.legendForm.miscForm.mapLegendRangeType === 'quantize'">
            <el-col :span="12">
              <el-form-item
                :label="t('chart.min')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-input-number
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendMin"
                  size="small"
                  controls-position="right"
                  @change="changeMisc('mapLegendMin')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                :label="t('chart.max')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-input-number
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendMax"
                  size="small"
                  controls-position="right"
                  @change="changeMisc('mapLegendMax')"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </div>
    </el-space>
    <MapLegendEditor
      v-if="chartType === 'map'"
      :model-value="state.legendForm.map"
      :chart="chart"
      :themes="themes"
      @update:model-value="changeMapLegend"
    />

    <el-form-item
      :label="t('chart.orient')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="supportLegendOrient && showProperty('orient')"
    >
      <el-radio-group
        v-model="state.legendForm.orient"
        size="small"
        @change="changeLegendStyle('orient')"
      >
        <el-radio :effect="themes" value="horizontal">{{ t('chart.horizontal') }}</el-radio>
        <el-radio :effect="themes" value="vertical">{{ t('chart.vertical') }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item
      v-if="supportsTileLegend"
      :label="t('chart.legend_display_mode')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        v-model="state.legendForm.displayMode"
        @change="changeLegendStyle('displayMode')"
      >
        <el-radio :effect="themes" value="pagination">{{ t('chart.legend_pagination') }}</el-radio>
        <el-radio :effect="themes" value="tile">{{ t('chart.legend_tile') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="supportsTileLegend && state.legendForm.displayMode === 'tile'"
      :label="t('chart.legend_tile_overflow')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        v-model="state.legendForm.tileOverflow"
        @change="changeLegendStyle('tileOverflow')"
      >
        <el-radio :effect="themes" value="scroll">{{ t('chart.legend_scroll') }}</el-radio>
        <el-radio :effect="themes" value="adaptive">{{ t('chart.legend_adaptive') }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-space>
      <el-form-item
        :label="t('chart.text_position')"
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('hPosition')"
      >
        <el-radio-group
          class="icon-radio-group"
          v-model="state.legendForm.hPosition"
          @change="changeLegendStyle('hPosition')"
        >
          <el-radio value="left">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_left') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'left' }"
              >
                <el-icon>
                  <Icon name="icon_left-align_outlined"
                    ><icon_leftAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio value="center" :disabled="state.legendForm.vPosition === 'center'">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_center') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.hPosition === 'center'
                }"
              >
                <el-icon>
                  <Icon name="icon_horizontal-align_outlined"
                    ><icon_horizontalAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio value="right">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_right') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'right' }"
              >
                <el-icon>
                  <Icon name="icon_right-align_outlined"
                    ><icon_rightAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <div
        v-if="showProperty('orient')"
        class="position-divider"
        :class="'position-divider--' + themes"
      ></div>

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('vPosition')"
      >
        <template #label>&nbsp;</template>
        <el-radio-group
          class="icon-radio-group"
          v-model="state.legendForm.vPosition"
          @change="changeLegendStyle('vPosition')"
        >
          <el-radio value="top">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_top') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.vPosition === 'top' }"
              >
                <el-icon>
                  <Icon name="icon_top-align_outlined"
                    ><icon_topAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio value="center" :disabled="state.legendForm.hPosition === 'center'">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_center') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.vPosition === 'center'
                }"
              >
                <el-icon>
                  <Icon name="icon_vertical-align_outlined"
                    ><icon_verticalAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio value="bottom">
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.text_pos_bottom') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.vPosition === 'bottom'
                }"
              >
                <el-icon>
                  <Icon name="icon_bottom-align_outlined"
                    ><icon_bottomAlign_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-space>
    <el-form-item
      class="form-item"
      v-if="showProperty('legendSort') && !batchOptStatus"
      :class="'form-item-' + themes"
      :label="t('chart.legend_sort')"
    >
      <el-select
        v-model="state.legendForm.sort"
        size="small"
        :effect="themes"
        :disabled="!chart.xAxisExt?.length"
        ref="legendSort"
        @change="changeLegendSort"
      >
        <el-option :label="t('chart.none')" value="none" />
        <el-option :label="t('chart.asc')" value="asc" />
        <el-option :label="t('chart.desc')" value="desc" />
        <el-option
          value="custom"
          :label="t('visualization.custom_sort')"
          @click="changeLegendSort('custom')"
        />
      </el-select>
    </el-form-item>
  </el-form>
  <el-dialog
    v-if="state.showCustomSort"
    v-model="state.showCustomSort"
    :title="t('chart.custom_sort') + t('chart.sort')"
    :visible="state.showCustomSort"
    :close-on-click-modal="false"
    destroy-on-close
    width="372px"
    class="dialog-css custom_sort_dialog"
  >
    <custom-sort-edit
      field-type="xAxisExt"
      :chart="chart"
      :field="state.customSortField"
      :origin-sort-list="state.legendForm.customSort"
      @on-sort-change="customSortChange"
    />
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeCustomSort">{{ t('chart.cancel') }} </el-button>
        <el-button type="primary" @click="saveCustomSort">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.icon-btn {
  font-size: 16px;
  line-height: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 6px;
  padding-top: 4px;

  color: #1f2329;

  cursor: pointer;

  &.dark {
    color: #a6a6a6;
    &.active {
      color: var(--ed-color-primary);
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }

  &.active {
    color: var(--ed-color-primary);
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
  }
}

.is-disabled {
  .icon-btn {
    color: #8f959e;
    cursor: not-allowed;

    &:hover {
      background-color: inherit;
    }

    &.active {
      background-color: #f5f7fa;
      &:hover {
        background-color: #f5f7fa;
      }
    }
    &.dark {
      color: #5f5f5f;
      &.active {
        background-color: #373737;
        &:hover {
          background-color: #373737;
        }
      }
    }
  }
}

.icon-radio-group {
  :deep(.ed-radio) {
    margin-right: 8px;

    &:last-child {
      margin-right: 0;
    }
  }
  :deep(.ed-radio__input) {
    display: none;
  }
  :deep(.ed-radio__label) {
    padding: 0;
  }
}
.position-divider {
  width: 1px;
  height: 18px;
  margin-top: 14px;
  background: rgba(31, 35, 41, 0.15);

  &.position-divider--dark {
    background: rgba(235, 235, 235, 0.15);
  }
}
.text_ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 80px;
  display: inline-block !important;
}
</style>
