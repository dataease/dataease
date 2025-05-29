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
import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { getDynamicColorScale } from '@/views/chart/components/js/util'
import CustomSortEdit from '@/views/chart/components/editor/drag-item/components/CustomSortEdit.vue'

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
  return props.themes === 'dark' ? 'light' : 'dark'
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

const changeMisc = prop => {
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
      state.legendForm = customStyle.legend
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
// 存储地图默认的最大最小值
const mapLegendDefaultRange = {
  max: 0,
  min: 0
}
// 缓存原始的区间数据
let mapLegendCustomRangeCacheList = []
const showProperty = prop => props.propertyInner?.includes(prop)
const mapDefaultRange = args => {
  if (args.from === 'map') {
    const rangeCustom = state.legendForm.miscForm.mapLegendRangeType === 'custom'
    if (!rangeCustom) {
      state.legendForm.miscForm.mapLegendMax = cloneDeep(args.data.max)
      state.legendForm.miscForm.mapLegendMin = cloneDeep(args.data.min)
    }
    state.legendForm.miscForm.mapLegendNumber = cloneDeep(args.data.legendNumber)
    mapLegendCustomRangeCacheList = []
    mapLegendDefaultRange.max = cloneDeep(args.data.max)
    mapLegendDefaultRange.min = cloneDeep(args.data.min)
    const customRange = getDynamicColorScale(
      mapLegendDefaultRange.min,
      mapLegendDefaultRange.max,
      args.data.legendNumber
    )
    customRange.forEach((item, index) => {
      if (index === 0) {
        mapLegendCustomRangeCacheList.push(...item.value)
      } else {
        mapLegendCustomRangeCacheList.push(item.value[1])
      }
    })
  }
}
const initMapCustomRange = () => {
  const legendCustom = state.legendForm.miscForm.mapAutoLegend
  const rangeCustom = state.legendForm.miscForm.mapLegendRangeType === 'custom'
  const rangeCustomValue = state.legendForm.miscForm.mapLegendCustomRange
  // 是自定义，并且自定义类型是自定义区间以及rangeCustomValue长度为0时，根据默认最大最小值计算区间值
  if (legendCustom && rangeCustom && rangeCustomValue.length === 0) {
    calcMapCustomRange()
  }
}
/**
 * 计算自定义区间
 * 最大最小值取等分区间的最大最小值
 */
const calcMapCustomRange = () => {
  const customRange = getDynamicColorScale(
    state.legendForm.miscForm.mapLegendMin,
    state.legendForm.miscForm.mapLegendMax,
    state.legendForm.miscForm.mapLegendNumber
  )
  state.legendForm.miscForm.mapLegendCustomRange = []
  customRange.forEach((item, index) => {
    if (index === 0) {
      state.legendForm.miscForm.mapLegendCustomRange.push(...item.value)
    } else {
      state.legendForm.miscForm.mapLegendCustomRange.push(item.value[1])
    }
  })
}
/**
 * 改变自定义区间类型
 * @param prop
 */
const changeLegendCustomType = (prop?) => {
  const type = state.legendForm.miscForm.mapLegendRangeType
  if (type === 'custom') {
    calcMapCustomRange()
  } else {
    state.legendForm.miscForm.mapLegendCustomRange = []
  }
  prop ? changeMisc(prop) : ''
}
/**
 * 改变自定义区间个数
 * @param prop
 */
const changeLegendNumber = (prop?) => {
  if (!state.legendForm.miscForm.mapLegendNumber) {
    return
  }
  calcMapCustomRange()
  prop ? changeMisc(prop) : ''
}
const changeRangeItem = (prop, index) => {
  if (state.legendForm.miscForm.mapLegendCustomRange[index] === null) {
    state.legendForm.miscForm.mapLegendCustomRange[index] = cloneDeep(
      mapLegendCustomRangeCacheList[index]
    )
  }
  changeMisc(prop)
}
const getMapCustomRange = index => {
  if (index === 0) return t('chart.min')
  if (index === state.legendForm.miscForm.mapLegendNumber) return t('chart.max')
  return ''
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
  }
  changeLegendStyle('sort')
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
      style="flex: 1; display: flex"
      v-if="showProperty('showRange') && state.legendForm.showRange"
    >
      <el-form-item :label="t('chart.icon')" class="form-item" :class="'form-item-' + themes">
        <el-select
          :effect="themes"
          v-model="state.legendForm.miscForm.bullet.bar.ranges.symbol"
          :placeholder="t('chart.icon')"
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
      <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 8px">
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
                :label="true"
                @change="changeMisc('mapAutoLegend')"
                style="width: 80px"
              >
                {{ t('chart.margin_model_auto') }}
              </el-radio>
              <el-radio
                size="small"
                :effect="themes"
                v-model="state.legendForm.miscForm.mapAutoLegend"
                :label="false"
                @change="changeMisc('mapAutoLegend')"
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
                  :label="'quantize'"
                  @change="changeLegendCustomType('mapLegendRangeType')"
                  style="width: 75px"
                >
                  {{ t('chart.legend_equal_range') }}
                </el-radio>
                <el-radio
                  size="small"
                  :effect="themes"
                  v-model="state.legendForm.miscForm.mapLegendRangeType"
                  :label="'custom'"
                  @change="changeLegendCustomType('mapLegendRangeType')"
                >
                  {{ t('chart.legend_custom_range') }}
                </el-radio>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
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
          <div v-if="state.legendForm.miscForm.mapLegendRangeType === 'custom'">
            <el-row
              :gutter="8"
              :key="index"
              v-for="(_value, index) in state.legendForm.miscForm.mapLegendCustomRange"
            >
              <el-col :span="8">
                <label class="ed-form-item__label text_ellipsis" :title="getMapCustomRange(index)">
                  {{ getMapCustomRange(index) }}
                </label>
              </el-col>
              <el-col :span="16">
                <el-form-item class="form-item" :class="'form-item-' + themes">
                  <el-input-number
                    :effect="themes"
                    v-model="state.legendForm.miscForm.mapLegendCustomRange[index]"
                    clearable
                    :value-on-clear="mapLegendCustomRangeCacheList[index]"
                    controls-position="right"
                    @change="changeRangeItem('mapLegendCustomRange', index)"
                    style="margin-bottom: 4px"
                    :step="1"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
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

    <el-form-item
      :label="t('chart.orient')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('orient')"
    >
      <el-radio-group
        v-model="state.legendForm.orient"
        size="small"
        @change="changeLegendStyle('orient')"
      >
        <el-radio :effect="themes" label="horizontal">{{ t('chart.horizontal') }}</el-radio>
        <el-radio :effect="themes" label="vertical">{{ t('chart.vertical') }}</el-radio>
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
          <el-radio label="left">
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
          <el-radio label="center" :disabled="state.legendForm.vPosition === 'center'">
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
          <el-radio label="right">
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
          <el-radio label="top">
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
          <el-radio label="center" :disabled="state.legendForm.hPosition === 'center'">
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
          <el-radio label="bottom">
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
      v-if="showProperty('legendSort')"
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
  border-radius: 4px;
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
