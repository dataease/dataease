<script setup lang="ts">
import { computed, onMounted, PropType, reactive, watch, ref } from 'vue'
import {
  COLOR_PANEL,
  DEFAULT_BASIC_STYLE,
  DEFAULT_MISC
} from '@/views/chart/components/editor/util/chart'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import CustomColorStyleSelect from '@/views/chart/components/editor/editor-style/components/CustomColorStyleSelect.vue'
import { cloneDeep, debounce, defaultsDeep } from 'lodash-es'
import { SERIES_NUMBER_FIELD } from '@antv/s2'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { isNumber } from 'lodash-es'
import { ElFormItem, ElInputNumber, ElMessage } from 'element-plus-secondary'
import { svgStrToUrl } from '../../../js/util'
import { numberToChineseUnderHundred } from '../../../js/panel/common/common_antv'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { useMapStoreWithOut } from '@/store/modules/map'
import { queryMapKeyApi } from '@/api/setting/sysParameter'
import {
  gaodeMapStyleOptions,
  qqMapStyleOptions,
  tdtMapStyleOptions
} from '@/views/chart/components/js/panel/charts/map/common'
import { useEmitt } from '@/hooks/web/useEmitt'

const dvMainStore = dvMainStoreWithOut()
const localeStore = useLocaleStoreWithOut()
const { batchOptStatus, mobileInPc } = storeToRefs(dvMainStore)
const { t } = useI18n()
const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})
const showProperty = prop => {
  const has = props.propertyInner?.includes(prop)
  if (!has) {
    return false
  }
  if (props.chart.type.includes('map') && mapType.value === 'tianditu' && prop === 'showLabel') {
    return false
  }
  return has
}
const tableExpandLevelOptions = reactive([{ name: t('chart.expand_all'), value: 'all' }])
const predefineColors = COLOR_PANEL
const state = reactive({
  basicStyleForm: JSON.parse(JSON.stringify(DEFAULT_BASIC_STYLE)) as ChartBasicStyle,
  miscForm: JSON.parse(JSON.stringify(DEFAULT_MISC)) as ChartMiscAttr,
  customColor: null,
  colorIndex: 0,
  fieldColumnWidth: {
    fieldId: '',
    width: 0
  },
  fileList: []
})
const emit = defineEmits(['onBasicStyleChange', 'onMiscChange'])
const changeBasicStyle = (prop?: string, requestData = false, render = true) => {
  emit('onBasicStyleChange', { data: state.basicStyleForm, requestData, render }, prop)
}
const onAlphaChange = v => {
  const _v = parseInt(v)
  if (_v >= 0 && _v <= 100) {
    state.basicStyleForm.alpha = _v
  } else if (_v < 0) {
    state.basicStyleForm.alpha = 0
  } else if (_v > 100) {
    state.basicStyleForm.alpha = 100
  } else {
    const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
    const oldForm = defaultsDeep(basicStyle, cloneDeep(DEFAULT_BASIC_STYLE)) as ChartBasicStyle
    state.basicStyleForm.alpha = oldForm.alpha
  }
  changeBasicStyle('alpha')
}

const onColumnWidthRatioChange = v => {
  const _v = parseInt(v)
  if (_v >= 1 && _v <= 100) {
    state.basicStyleForm.columnWidthRatio = _v
  } else if (_v < 1) {
    state.basicStyleForm.columnWidthRatio = 1
  } else if (_v > 100) {
    state.basicStyleForm.columnWidthRatio = 100
  } else {
    const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
    const oldForm = defaultsDeep(basicStyle, cloneDeep(DEFAULT_BASIC_STYLE)) as ChartBasicStyle
    state.basicStyleForm.columnWidthRatio = oldForm.columnWidthRatio
  }
  changeBasicStyle('columnWidthRatio')
}

const changeMisc = prop => {
  emit('onMiscChange', { data: state.miscForm, requestData: true }, prop)
}
const init = () => {
  const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
  const miscStyle = cloneDeep(props.chart.customAttr.misc)
  configCompat(basicStyle)
  if (
    basicStyle.mapSymbol === 'custom' &&
    state.basicStyleForm.customIcon !== basicStyle.customIcon
  ) {
    let file
    if (basicStyle.customIcon?.startsWith('data')) {
      file = basicStyle.customIcon
    } else {
      file = svgStrToUrl(basicStyle.customIcon)
    }
    file && (state.fileList[0] = { url: file })
  }
  state.basicStyleForm = defaultsDeep(basicStyle, cloneDeep(DEFAULT_BASIC_STYLE)) as ChartBasicStyle
  state.miscForm = defaultsDeep(miscStyle, cloneDeep(DEFAULT_MISC)) as ChartMiscAttr
  if (!state.customColor) {
    state.customColor = state.basicStyleForm.colors[0]
    state.colorIndex = 0
  }
  if (basicStyle.tableLayoutMode === 'tree') {
    tableExpandLevelOptions.splice(1)
    let maxLevel = props.chart.xAxis?.length
    if (isNumber(basicStyle.defaultExpandLevel)) {
      maxLevel = Math.max(maxLevel, basicStyle.defaultExpandLevel)
    }
    for (let i = 1; i <= maxLevel; i++) {
      let name = t('chart.level_label', { num: i })
      if (localeStore.getCurrentLocale.lang !== 'en') {
        name = t('chart.level_label', { num: numberToChineseUnderHundred(i) })
      }
      tableExpandLevelOptions.push({ name, value: i })
    }
  }
  const lastPageInfo = dvMainStore.getViewPageInfo(props.chart.id)
  if (lastPageInfo) {
    if (lastPageInfo.pageSize && lastPageInfo.pageSize !== state.basicStyleForm.tablePageSize) {
      state.basicStyleForm.tablePageSize = lastPageInfo.pageSize
      changeBasicStyle('tablePageSize', false, false)
      return
    }
  }
  initTableColumnWidth()
}
const debouncedInit = debounce(init, 500)
watch(
  [
    () => props.chart.customAttr.basicStyle,
    () => props.chart.customAttr.misc,
    () => props.chart.customAttr.tableHeader,
    () => props.chart.xAxis,
    () => props.chart.yAxis
  ],
  debouncedInit,
  { deep: true }
)
const configCompat = (basicStyle: ChartBasicStyle) => {
  // 悬浮改为图例和缩放按钮
  if (basicStyle.suspension === false && basicStyle.showZoom === undefined) {
    basicStyle.showZoom = false
  }
}
const COLUMN_WIDTH_TYPE = ['table-info', 'table-normal']
const initTableColumnWidth = () => {
  if (!COLUMN_WIDTH_TYPE.includes(props.chart.type)) {
    return
  }
  let { xAxis, yAxis, customAttr } = JSON.parse(JSON.stringify(props.chart))
  let allAxis = xAxis
  if (props.chart.type === 'table-normal') {
    allAxis = allAxis.concat(yAxis)
  }
  const { tableHeader } = customAttr
  if (allAxis.length && tableHeader.showIndex) {
    const indexColumn = {
      dataeaseName: SERIES_NUMBER_FIELD,
      name: tableHeader.indexLabel
    } as unknown as Axis
    allAxis.unshift(indexColumn)
  }
  if (!allAxis.length) {
    state.basicStyleForm.tableFieldWidth?.splice(0)
    state.fieldColumnWidth.fieldId = ''
    state.fieldColumnWidth.width = 0
  } else {
    if (!state.basicStyleForm.tableFieldWidth.length) {
      state.basicStyleForm.tableFieldWidth.splice(0)
      const defaultWidth = parseFloat((100 / allAxis.length).toFixed(2))
      allAxis.forEach(item => {
        state.basicStyleForm.tableFieldWidth.push({
          fieldId: item.dataeaseName,
          name: item.name,
          width: defaultWidth
        })
      })
    } else {
      const fieldMap = state.basicStyleForm.tableFieldWidth.reduce((p, n) => {
        p[n.fieldId] = n
        return p
      }, {})
      state.basicStyleForm.tableFieldWidth.splice(0)
      allAxis.forEach(item => {
        let width = 10
        if (fieldMap[item.dataeaseName]) {
          width = fieldMap[item.dataeaseName].width
        }
        state.basicStyleForm.tableFieldWidth.push({
          fieldId: item.dataeaseName,
          name: item.name,
          width
        })
      })
    }
    let selectedField = state.basicStyleForm.tableFieldWidth[0]
    const curFieldIndex = state.basicStyleForm.tableFieldWidth.findIndex(
      i => i.fieldId === state.fieldColumnWidth.fieldId
    )
    if (curFieldIndex !== -1) {
      selectedField = state.basicStyleForm.tableFieldWidth[curFieldIndex]
    }
    state.fieldColumnWidth.fieldId = selectedField.fieldId
    state.fieldColumnWidth.width = selectedField.width
  }
}
const changeFieldColumn = () => {
  const { basicStyleForm, fieldColumnWidth } = state
  const fieldWidth = basicStyleForm.tableFieldWidth?.find(
    i => i.fieldId === fieldColumnWidth.fieldId
  )
  if (fieldWidth) {
    fieldColumnWidth.width = fieldWidth.width
  }
}
const changeFieldColumnWidth = () => {
  const { basicStyleForm, fieldColumnWidth } = state
  let { width } = fieldColumnWidth
  let validate = true
  width = parseFloat(width)
  if (isNaN(width) || !isNumber(width)) {
    validate = false
  }
  if (width < 0 || width > 200) {
    validate = false
  }
  const fieldWidth = basicStyleForm.tableFieldWidth?.find(
    i => i.fieldId === fieldColumnWidth.fieldId
  )
  if (!validate) {
    ElMessage.warning('宽度需要在 0-200 之间')
    if (fieldWidth) {
      fieldColumnWidth.width = fieldWidth.width
    }
    return
  }
  if (fieldWidth) {
    fieldWidth.width = fieldColumnWidth.width
    changeBasicStyle('tableFieldWidth')
  }
}
const pageSizeOptions = [
  { name: '10' + t('chart.table_page_size_unit'), value: 10 },
  { name: '20' + t('chart.table_page_size_unit'), value: 20 },
  { name: '30' + t('chart.table_page_size_unit'), value: 30 },
  { name: '40' + t('chart.table_page_size_unit'), value: 40 },
  { name: '50' + t('chart.table_page_size_unit'), value: 50 },
  { name: '100' + t('chart.table_page_size_unit'), value: 100 }
]

const gaugeStyleOptions = [{ name: '默认', value: 'default' }]

const symbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const mapStore = useMapStoreWithOut()

const getMapKey = async () => {
  if (!mapStore.mapKey.key) {
    await queryMapKeyApi().then(res => mapStore.setKey(res.data))
  }
  if (mapStore.mapKey.securityCode) {
    window._AMapSecurityConfig = {
      securityJsCode: mapStore.mapKey.securityCode
    }
  }
  return mapStore.mapKey
}

const mapType = ref<string>(undefined)

const mapStyleOptions = computed(() => {
  switch (mapType.value) {
    case 'tianditu':
      return tdtMapStyleOptions
    case 'qq':
      return qqMapStyleOptions
    default:
      return gaodeMapStyleOptions
  }
})

const heatMapTypeOptions = [
  { name: t('chart.heatmap_classics'), value: 'heatmap' },
  { name: t('chart.heatmap3D'), value: 'heatmap3D' }
]

/**
 * 表格是否合并单元格
 */
const mergeCell = computed(() => {
  if (COLUMN_WIDTH_TYPE.includes(props.chart.type)) {
    let { customAttr } = JSON.parse(JSON.stringify(props.chart))
    const { tableCell } = customAttr
    return tableCell.mergeCells
  }
  return false
})

const preventInvalidKeydown = event => {
  const invalidKeys = ['e', 'E', '+', '-', '.']
  if (invalidKeys.includes(event.key)) {
    event.preventDefault()
  }
}
// 验证输入值
const validateInput = (value, field) => {
  if (value === '') {
    state.basicStyleForm[field] = 1
    return
  }

  let num = parseInt(value, 10)

  if (isNaN(num)) {
    num = 1
  } else if (num < 1) {
    num = 1
  } else if (num > 100) {
    num = 100
  }
  state.basicStyleForm[field] = num
}
onMounted(() => {
  init()
  getMapKey().then(res => {
    if (res) {
      mapType.value = res.mapType
    }
  })
  useEmitt({
    name: 'chart-type-change',
    callback: () => {
      if (['topRoundAngle', 'roundAngle'].includes(state.basicStyleForm.radiusColumnBar)) {
        state.basicStyleForm.radiusColumnBar = 'roundAngle'
        changeBasicStyle('radiusColumnBar')
      }
    }
  })
})
</script>
<template>
  <el-form size="small" style="width: 100%">
    <template v-if="showProperty('colors')">
      <custom-color-style-select
        v-model="state"
        :chart="chart"
        :themes="themes"
        :property-inner="propertyInner"
        @change-basic-style="prop => changeBasicStyle(prop)"
      />
    </template>

    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('gradient')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.gradient"
        @change="changeBasicStyle('gradient')"
      >
        {{ $t('chart.gradient') }}{{ $t('chart.color') }}
      </el-checkbox>
    </el-form-item>

    <el-form-item
      class="form-item"
      v-if="showProperty('tableLayoutMode')"
      :label="t('chart.table_layout_mode')"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.tableLayoutMode"
        @change="changeBasicStyle('tableLayoutMode')"
      >
        <el-radio label="grid" :effect="themes">{{ t('chart.table_layout_grid') }}</el-radio>
        <el-radio label="tree" :effect="themes">{{ t('chart.table_layout_tree') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      class="form-item"
      v-if="showProperty('tableLayoutMode') && state.basicStyleForm.tableLayoutMode === 'tree'"
      :label="t('chart.default_expand_level')"
      :class="'form-item-' + themes"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.defaultExpandLevel"
        @change="changeBasicStyle('defaultExpandLevel')"
      >
        <el-option
          v-for="item in tableExpandLevelOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      class="form-item"
      v-if="showProperty('quotaPosition')"
      :label="t('chart.quota_position')"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.quotaPosition"
        @change="changeBasicStyle('quotaPosition')"
      >
        <el-radio label="col" :effect="themes">{{ t('chart.quota_position_col') }}</el-radio>
        <el-radio label="row" :effect="themes">{{ t('chart.quota_position_row') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="showProperty('quotaColLabel') && state.basicStyleForm.quotaPosition === 'row'"
      class="form-item"
      :label="t('chart.quota_col_label')"
      :class="'form-item-' + themes"
    >
      <el-input
        :effect="themes"
        v-model="state.basicStyleForm.quotaColLabel"
        @change="changeBasicStyle('quotaColLabel')"
      />
    </el-form-item>
    <div class="alpha-setting" v-if="showProperty('alpha')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.not_alpha') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="13">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              v-model="state.basicStyleForm.alpha"
              @change="changeBasicStyle('alpha')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="11" style="padding-top: 2px">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-input
              type="number"
              :effect="themes"
              v-model="state.basicStyleForm.alpha"
              :min="0"
              :max="100"
              class="basic-input-number"
              :controls="false"
              @change="onAlphaChange"
            >
              <template #suffix> % </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <el-form-item
      class="form-item"
      v-if="showProperty('radiusColumnBar')"
      :label="t('chart.radiusColumnBar')"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.radiusColumnBar"
        @change="changeBasicStyle('radiusColumnBar')"
        class="radius-class"
      >
        <el-radio label="rightAngle" :effect="themes">{{ t('chart.rightAngle') }}</el-radio>
        <el-radio label="roundAngle" :effect="themes">{{ t('chart.roundAngle') }}</el-radio>
        <el-radio
          v-if="!props.chart.type.includes('-stack')"
          label="topRoundAngle"
          :effect="themes"
          >{{ t('chart.topRoundAngle') }}</el-radio
        >
      </el-radio-group>
    </el-form-item>

    <el-form-item
      :label="t('chart.orient')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('layout')"
    >
      <el-radio-group
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.layout"
        @change="changeBasicStyle('layout')"
      >
        <el-radio :effect="themes" label="horizontal">{{ t('chart.horizontal') }}</el-radio>
        <el-radio :effect="themes" label="vertical">{{ t('chart.vertical') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <!--flow map begin-->
    <el-form-item
      v-if="showProperty('heatMapStyle')"
      :label="t('chart.type')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.heatMapType"
        @change="changeBasicStyle('heatMapType')"
      >
        <el-option
          v-for="item in heatMapTypeOptions"
          :key="item.name"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <div class="map-style" v-if="showProperty('mapBaseStyle') || showProperty('heatMapStyle')">
      <el-row style="flex: 1">
        <el-col>
          <el-form-item
            :label="t('chart.map_style')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              :effect="themes"
              v-model="state.basicStyleForm.mapStyle"
              @change="changeBasicStyle('mapStyle')"
            >
              <el-option
                v-for="item in mapStyleOptions"
                :key="item.name"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row style="flex: 1" v-if="state.basicStyleForm.mapStyle === 'custom'">
        <el-col>
          <el-form-item
            :label="t('chart.map_style_url')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-input
              :effect="themes"
              v-model="state.basicStyleForm.mapStyleUrl"
              maxlength="50"
              @change="changeBasicStyle('mapStyleUrl')"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <div class="alpha-setting" v-if="mapType !== 'tianditu'">
        <label class="alpha-label" :class="{ dark: 'dark' === themes }">
          {{ t('chart.chart_map') + ' ' + t('chart.map_pitch') }}
        </label>
        <el-row style="flex: 1" :gutter="8">
          <el-col>
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="0"
                :max="90"
                v-model="state.miscForm.mapPitch"
                @change="changeMisc('mapPitch')"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </div>
    <div class="alpha-setting" v-if="showProperty('heatMapStyle')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.heatMapIntensity') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col>
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              :min="1"
              :max="20"
              v-model="state.basicStyleForm.heatMapIntensity"
              @change="changeBasicStyle('heatMapIntensity')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <div class="alpha-setting" v-if="showProperty('heatMapStyle')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.heatMapRadius') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col>
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              :min="1"
              :max="40"
              v-model="state.basicStyleForm.heatMapRadius"
              @change="changeBasicStyle('heatMapRadius')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <!--flow map end-->
    <!--map start-->
    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('areaBorderColor')">
        <el-form-item
          :label="t('chart.area_border_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.areaBorderColor"
            :effect="themes"
            is-custom
            show-alpha
            :trigger-width="108"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeBasicStyle('areaBorderColor')"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('areaBaseColor')">
        <el-form-item
          :label="t('chart.area_base_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.areaBaseColor"
            is-custom
            show-alpha
            :effect="themes"
            :trigger-width="108"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeBasicStyle('areaBaseColor')"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('showLabel')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.showLabel"
        @change="changeBasicStyle('showLabel')"
      >
        {{ t('chart.show_label') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('autoFit')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.autoFit"
        @change="changeBasicStyle('autoFit')"
      >
        {{ t('chart.auto_fit') }}
      </el-checkbox>
    </el-form-item>
    <div
      class="alpha-setting"
      v-if="showProperty('zoomLevel') && state.basicStyleForm.autoFit === false"
    >
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.zoom_level') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col>
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              :min="1"
              :max="18"
              :step="0.1"
              v-model="state.basicStyleForm.zoomLevel"
              @change="changeBasicStyle('zoomLevel')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <template v-if="showProperty('mapCenter') && state.basicStyleForm.autoFit === false">
      <el-row :gutter="8">
        <el-col :span="12">
          <el-form-item
            class="form-item"
            :class="'form-item-' + themes"
            :label="t('chart.central_point') + ' ' + t('chart.longitude')"
          >
            <el-input-number
              controls-position="right"
              :min="-180"
              :max="180"
              :effect="props.themes"
              v-model.number="state.basicStyleForm.mapCenter.longitude"
              @change="changeBasicStyle('mapCenter.longitude')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            class="form-item"
            :class="'form-item-' + themes"
            :label="t('chart.central_point') + ' ' + t('chart.latitude')"
          >
            <el-input-number
              controls-position="right"
              :min="-90"
              :max="90"
              :effect="props.themes"
              v-model.number="state.basicStyleForm.mapCenter.latitude"
              @change="changeBasicStyle('mapCenter.latitude')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </template>
    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('zoom')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.showZoom"
        @change="changeBasicStyle('showZoom')"
      >
        {{ t('chart.show_zoom') }}
      </el-checkbox>
    </el-form-item>
    <div v-if="showProperty('zoom') && state.basicStyleForm.showZoom">
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.button_color')"
      >
        <el-color-picker
          is-custom
          class="color-picker-style"
          v-model="state.basicStyleForm.zoomButtonColor"
          :persistent="false"
          :effect="themes"
          :trigger-width="108"
          :predefine="predefineColors"
          @change="changeBasicStyle('zoomButtonColor')"
        />
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.button_background_color')"
      >
        <el-color-picker
          is-custom
          class="color-picker-style"
          v-model="state.basicStyleForm.zoomBackground"
          :persistent="false"
          :effect="themes"
          :trigger-width="108"
          :predefine="predefineColors"
          @change="changeBasicStyle('zoomBackground')"
        />
      </el-form-item>
    </div>

    <!--map end-->

    <!--table start-->
    <el-row :gutter="8">
      <el-col :span="12" v-if="showProperty('tableBorderColor')">
        <el-form-item
          :label="t('chart.table_border_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.tableBorderColor"
            :effect="themes"
            is-custom
            :trigger-width="mobileInPc ? 197 : 108"
            color-format="rgb"
            :predefine="predefineColors"
            show-alpha
            @change="changeBasicStyle('tableBorderColor')"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12" v-if="showProperty('tableScrollBarColor')">
        <el-form-item
          :label="t('chart.table_scroll_bar_color')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            :persistent="false"
            v-model="state.basicStyleForm.tableScrollBarColor"
            class="color-picker-style"
            :predefine="predefineColors"
            :effect="themes"
            is-custom
            :trigger-width="mobileInPc ? 197 : 108"
            color-format="rgb"
            show-alpha
            @change="changeBasicStyle('tableScrollBarColor')"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item
      v-if="showProperty('tablePageMode')"
      :label="t('chart.table_page_mode')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        :effect="themes"
        v-model="state.basicStyleForm.tablePageMode"
        @change="changeBasicStyle('tablePageMode', true)"
      >
        <el-radio :effect="themes" label="page">{{ t('chart.page_mode_page') }}</el-radio>
        <el-radio :effect="themes" label="pull">{{ t('chart.page_mode_pull') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="showProperty('tablePageMode') && state.basicStyleForm.tablePageMode !== 'pull'"
      :label="t('chart.table_pager_style')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-radio-group
        :effect="themes"
        v-model="state.basicStyleForm.tablePageStyle"
        @change="changeBasicStyle('tablePageStyle', false)"
      >
        <el-radio :effect="themes" label="simple">{{ t('chart.page_pager_simple') }}</el-radio>
        <el-radio :effect="themes" label="general">{{ t('chart.page_pager_general') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="
        showProperty('tablePageMode') &&
        state.basicStyleForm.tablePageMode === 'page' &&
        state.basicStyleForm.tablePageStyle === 'simple'
      "
      :label="t('chart.table_page_size')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.tablePageSize"
        :placeholder="t('chart.table_page_size')"
        @change="changeBasicStyle('tablePageSize', true)"
      >
        <el-option
          v-for="item in pageSizeOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <!--table end-->

    <!--table2 start-->
    <el-form-item
      :label="t('chart.table_column_width_config')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('tableColumnMode')"
    >
      <el-radio-group
        v-model="state.basicStyleForm.tableColumnMode"
        @change="changeBasicStyle('tableColumnMode')"
        class="table-column-mode"
      >
        <el-radio label="adapt" :effect="themes">
          {{ t('chart.table_column_adapt') }}
        </el-radio>
        <el-radio label="custom" :effect="themes">
          {{ t('chart.table_column_fixed') }}
        </el-radio>
        <el-radio v-show="chart.type !== 'table-pivot'" label="field" :effect="themes">
          {{ t('chart.table_column_custom') }}
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="showProperty('tableColumnMode') && state.basicStyleForm.tableColumnMode === 'custom'"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        v-model.number="state.basicStyleForm.tableColumnWidth"
        :min="10"
        controls-position="right"
        @change="changeBasicStyle('tableColumnWidth')"
      />
    </el-form-item>
    <el-form-item
      v-if="showProperty('tableColumnMode') && state.basicStyleForm.tableColumnMode === 'field'"
      label=""
      class="form-item table-field-width-config"
    >
      <el-select
        v-model="state.fieldColumnWidth.fieldId"
        :effect="themes"
        :disabled="batchOptStatus"
        @change="changeFieldColumn()"
      >
        <el-option
          v-for="item in state.basicStyleForm.tableFieldWidth"
          :key="item.fieldId"
          :label="item.name"
          :value="item.fieldId"
        />
      </el-select>
      <el-input
        v-model.number="state.fieldColumnWidth.width"
        type="number"
        class="basic-input-number"
        :effect="themes"
        :disabled="batchOptStatus"
        @change="changeFieldColumnWidth()"
      >
        <template #append>%</template>
      </el-input>
    </el-form-item>
    <el-form-item v-if="showProperty('autoWrap')" class="form-item" :class="'form-item-' + themes">
      <el-checkbox
        size="small"
        :effect="themes"
        :disabled="mergeCell"
        v-model="state.basicStyleForm.autoWrap"
        @change="changeBasicStyle('autoWrap')"
      >
        <span class="data-area-label">
          <span style="margin-right: 4px">{{ t('chart.table_auto_break_line') }}</span>
          <el-tooltip class="item" effect="dark" placement="bottom" v-if="mergeCell">
            <template #content>
              <div>{{ t('chart.merge_cells_break_line_tip') }}</div>
            </template>
            <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
          <el-tooltip class="item" effect="dark" placement="bottom" v-else>
            <template #content>
              <div>{{ t('chart.table_break_line_tip') }}</div>
            </template>
            <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
        </span>
      </el-checkbox>
    </el-form-item>
    <el-form-item
      v-if="showProperty('autoWrap') && state.basicStyleForm.autoWrap"
      :label="t('chart.table_break_line_max_lines')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        v-model="state.basicStyleForm.maxLines"
        controls-position="right"
        :show-input-controls="false"
        :min="1"
        :step="1"
        :disabled="mergeCell"
        :precision="0"
        @change="changeBasicStyle('maxLines')"
      />
    </el-form-item>
    <el-form-item
      v-if="showProperty('showHoverStyle')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.showHoverStyle"
        @change="changeBasicStyle('showHoverStyle')"
      >
        {{ t('chart.show_hover_style') }}
      </el-checkbox>
    </el-form-item>
    <!--table2 end-->
    <!--gauge start-->
    <el-form-item
      :label="t('chart.chart_style')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('gaugeStyle')"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.gaugeStyle"
        @change="changeBasicStyle('gaugeStyle')"
      >
        <el-option
          v-for="item in gaugeStyleOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      v-if="showProperty('gaugeAxisLine')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        v-model="state.basicStyleForm.gaugeAxisLine"
        :effect="themes"
        size="small"
        @change="changeBasicStyle('gaugeAxisLine')"
      >
        {{ t('chart.gauge_axis_label') }}</el-checkbox
      >
    </el-form-item>
    <el-form-item
      v-if="showProperty('gaugePercentLabel') && state.basicStyleForm.gaugeAxisLine"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        v-model="state.basicStyleForm.gaugePercentLabel"
        :effect="themes"
        size="small"
        @change="changeBasicStyle('gaugePercentLabel')"
      >
        {{ t('chart.gauge_percentage_tick') }}</el-checkbox
      >
    </el-form-item>
    <!--gauge end-->
    <!--bar start-->
    <el-form-item
      v-if="showProperty('barDefault')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.barDefault"
        @change="changeBasicStyle('barDefault')"
      >
        {{ t('chart.adapt') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      v-if="showProperty('barDefault') && !state.basicStyleForm.barDefault"
      :label="t('chart.bar_gap')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        v-model="state.basicStyleForm.barGap"
        controls-position="right"
        :show-input-controls="false"
        :min="0"
        :max="5"
        :step="0.1"
        @change="changeBasicStyle('barGap')"
      />
    </el-form-item>
    <div class="alpha-setting" v-if="showProperty('columnWidthRatio')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.column_width_ratio') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="13">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              :min="1"
              :max="100"
              v-model="state.basicStyleForm.columnWidthRatio"
              @change="changeBasicStyle('columnWidthRatio')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="11" style="padding-top: 2px">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-input
              type="number"
              :effect="themes"
              v-model="state.basicStyleForm.columnWidthRatio"
              :min="1"
              :max="100"
              class="basic-input-number"
              :controls="false"
              @change="onColumnWidthRatioChange"
            >
              <template #suffix> % </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <!--bar end-->
    <!--line area start-->
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          :label="t('chart.line_width')"
          class="form-item form-item-slider"
          :class="'form-item-' + themes"
          v-if="showProperty('lineWidth')"
        >
          <el-input-number
            :effect="themes"
            v-model="state.basicStyleForm.lineWidth"
            :min="0"
            :max="10"
            controls-position="right"
            @change="changeBasicStyle('lineWidth')"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          :label="t('chart.line_symbol')"
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('lineSymbol')"
        >
          <el-select
            :effect="themes"
            v-model="state.basicStyleForm.lineSymbol"
            :placeholder="t('chart.line_symbol')"
            @change="changeBasicStyle('lineSymbol')"
          >
            <el-option
              v-for="item in symbolOptions"
              :key="item.value"
              :label="item.name"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item
          :label="t('chart.line_symbol_size')"
          class="form-item form-item-slider"
          :class="'form-item-' + themes"
          v-if="showProperty('lineSymbolSize')"
        >
          <el-input-number
            :effect="themes"
            v-model="state.basicStyleForm.lineSymbolSize"
            :min="0"
            :max="20"
            controls-position="right"
            @change="changeBasicStyle('lineSymbolSize')"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('lineSmooth')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.lineSmooth"
        @change="changeBasicStyle('lineSmooth')"
      >
        {{ t('chart.line_smooth') }}
      </el-checkbox>
    </el-form-item>
    <!--line area end-->
    <!--radar begin-->
    <el-form-item
      :label="t('chart.shape')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('radarShape')"
    >
      <el-radio-group
        :effect="themes"
        v-model="state.basicStyleForm.radarShape"
        @change="changeBasicStyle('radarShape')"
      >
        <el-radio :effect="themes" label="polygon">{{ t('chart.polygon') }}</el-radio>
        <el-radio :effect="themes" label="circle">{{ t('chart.circle') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      class="form-item margin-bottom-8"
      :class="'form-item-' + themes"
      v-if="showProperty('radarShowPoint')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.radarShowPoint"
        @change="changeBasicStyle('radarShowPoint')"
      >
        {{ $t('chart.radar_point') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      style="padding-left: 20px"
      class="form-item margin-bottom-8"
      :class="'form-item-' + themes"
      :label="t('chart.radar_point_size')"
      v-if="showProperty('radarPointSize')"
    >
      <el-input-number
        style="width: 100%"
        :effect="themes"
        controls-position="right"
        size="middle"
        :min="0"
        :max="30"
        :disabled="!state.basicStyleForm.radarShowPoint"
        v-model="state.basicStyleForm.radarPointSize"
        @change="changeBasicStyle('radarPointSize')"
      />
    </el-form-item>
    <el-form-item
      class="form-item margin-bottom-8"
      :class="'form-item-' + themes"
      v-if="showProperty('radarAreaColor')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.basicStyleForm.radarAreaColor"
        @change="changeBasicStyle('radarAreaColor')"
      >
        {{ $t('chart.radar_area_color') }}
      </el-checkbox>
    </el-form-item>
    <!--radar end-->
    <!--scatter start-->
    <el-form-item
      :label="t('chart.bubble_symbol')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('scatterSymbol')"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.scatterSymbol"
        :placeholder="t('chart.line_symbol')"
        @change="changeBasicStyle('scatterSymbol')"
      >
        <el-option
          v-for="item in symbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      :label="t('chart.bubble_size')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
      v-if="showProperty('scatterSymbolSize')"
    >
      <el-input-number
        :effect="themes"
        v-model="state.basicStyleForm.scatterSymbolSize"
        controls-position="right"
        :min="1"
        :max="40"
        @change="changeBasicStyle('scatterSymbolSize')"
      />
    </el-form-item>
    <!--scatter end-->

    <!--symbol map start-->
    <el-form-item
      :label="t('chart.bubble_symbol')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('mapSymbol')"
    >
      <el-select
        :effect="themes"
        v-model="state.basicStyleForm.mapSymbol"
        :placeholder="t('chart.line_symbol')"
        @change="changeBasicStyle('mapSymbol')"
      >
        <el-option
          v-for="item in symbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item
      :label="t('chart.bubble_size')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
      v-if="showProperty('mapSymbolSize')"
    >
      <el-input-number
        :effect="themes"
        controls-position="right"
        v-model="state.basicStyleForm.mapSymbolSize"
        :min="1"
        :max="40"
        @change="changeBasicStyle('mapSymbolSize')"
      />
    </el-form-item>
    <el-form-item
      :label="t('chart.not_alpha')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
      v-if="showProperty('mapSymbolOpacity')"
    >
      <el-input-number
        :effect="themes"
        controls-position="right"
        v-model="state.basicStyleForm.mapSymbolOpacity"
        :min="1"
        :max="100"
        @change="changeBasicStyle('mapSymbolOpacity')"
      />
    </el-form-item>
    <el-form-item
      v-if="showProperty('mapSymbol') && state.basicStyleForm.mapSymbol !== 'marker'"
      :label="t('visualization.border_color')"
      class="form-item form-item-slider"
      :class="'form-item-' + themes"
    >
      <el-input-number
        :effect="themes"
        controls-position="right"
        v-model="state.basicStyleForm.mapSymbolStrokeWidth"
        :min="0"
        :max="5"
        @change="changeBasicStyle('mapSymbolStrokeWidth')"
      />
    </el-form-item>
    <!-- pie/rose start -->

    <div v-show="showProperty('topN')" class="top-n-setting">
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          v-model="state.basicStyleForm.calcTopN"
          @change="changeBasicStyle('calcTopN')"
        >
          {{ $t('chart.top_n_desc') }}
        </el-checkbox>
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-show="state.basicStyleForm.calcTopN"
      >
        <span>{{ $t('chart.top_n_input_1') }}</span>
        <el-input-number
          v-model="state.basicStyleForm.topN"
          controls-position="right"
          size="small"
          :min="1"
          :max="100"
          :precision="0"
          :step-strictly="true"
          :value-on-clear="5"
          :effect="themes"
          @change="changeBasicStyle('topN')"
        />
        <span>{{ $t('chart.top_n_input_2') }}</span>
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        :label="t('chart.top_n_label')"
        v-show="state.basicStyleForm.calcTopN"
      >
        <el-input
          :effect="themes"
          v-model="state.basicStyleForm.topNLabel"
          size="small"
          :maxlength="50"
          @change="changeBasicStyle('topNLabel')"
        />
      </el-form-item>
    </div>
    <div class="alpha-setting" v-if="showProperty('innerRadius')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.pie_inner_radius_percent') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="13">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              v-model="state.basicStyleForm.innerRadius"
              :min="1"
              :max="100"
              @change="changeBasicStyle('innerRadius')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-input
              type="number"
              :effect="themes"
              v-model="state.basicStyleForm.innerRadius"
              :min="1"
              :max="100"
              class="basic-input-number"
              :controls="false"
              @input="validateInput($event, 'innerRadius')"
              @change="changeBasicStyle('innerRadius')"
              @keydown="preventInvalidKeydown"
            >
              <template #suffix> % </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </div>

    <div class="alpha-setting" v-if="showProperty('radius')">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }">
        {{ t('chart.pie_outer_radius') }}
      </label>
      <el-row style="flex: 1" :gutter="8">
        <el-col :span="13">
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              v-model="state.basicStyleForm.radius"
              :min="1"
              :max="100"
              @change="changeBasicStyle('radius')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="11">
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <el-input
              type="number"
              :effect="themes"
              v-model="state.basicStyleForm.radius"
              :min="1"
              :max="100"
              class="basic-input-number"
              :controls="false"
              @input="validateInput($event, 'radius')"
              @change="changeBasicStyle('radius')"
              @keydown="preventInvalidKeydown"
            >
              <template #suffix> % </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <!-- pie/rose end -->
    <!-- circle-packing start -->
    <div v-if="showProperty('circleBorderStyle')">
      <div class="alpha-setting">
        <el-row style="display: flex; width: 100%">
          <el-col :span="10">
            <el-form-item
              :label="t('chart.circle_packing_border_color')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                v-model="state.basicStyleForm.circleBorderColor"
                class="color-picker-style"
                :triggerWidth="65"
                is-custom
                show-alpha
                :predefine="state.predefineColors"
                @change="changeBasicStyle('circleBorderColor')"
              >
              </el-color-picker>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item
              :label="t('chart.circle_packing_border_width')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-input-number
                :min="0"
                :max="50"
                :effect="themes"
                controls-position="right"
                v-model="state.basicStyleForm.circleBorderWidth"
                class="color-picker-style"
                @change="changeBasicStyle('circleBorderWidth')"
              >
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <el-row>
        <el-form-item
          style="width: 150px"
          :label="t('chart.circle_packing_padding')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input-number
            :min="0"
            :max="10"
            :effect="themes"
            controls-position="right"
            v-model="state.basicStyleForm.circlePadding"
            class="color-picker-style"
            @change="changeBasicStyle('circlePadding')"
          >
          </el-input-number>
        </el-form-item>
      </el-row>
    </div>
    <!-- circle-packing end -->
  </el-form>
</template>
<style scoped lang="less">
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
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
.table-field-width-config {
  .ed-select {
    width: 100px !important;
    :deep(.ed-input__wrapper) {
      border-radius: 4px 0 0 4px !important;
    }
  }
  .ed-input-group {
    width: 120px;
    :deep(.ed-input__wrapper) {
      border-radius: 0 !important;
    }
    :deep(.ed-input-group__append) {
      padding: 0 8px;
    }
  }
}
.table-column-mode {
  :deep(.ed-radio) {
    margin-right: 10px !important;
  }
}
.basic-input-number {
  :deep(input) {
    -webkit-appearance: none;
    -moz-appearance: textfield;

    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
    }
  }
}
.top-n-setting {
  .ed-input-number {
    width: 80px !important;
    margin: 0 2px;
  }
  :deep(span) {
    font-size: 12px;
  }
}
.avatar-uploader-container {
  :deep(.ed-upload--picture-card) {
    background: #eff0f1;
    border: 1px dashed #dee0e3;
    border-radius: 4px;

    .ed-icon {
      color: #1f2329;
    }

    &:hover {
      .ed-icon {
        color: var(--ed-color-primary);
      }
    }
  }

  &.img-area_dark {
    :deep(.ed-upload-list__item).is-ready {
      border-color: #434343;
    }
    :deep(.ed-upload--picture-card) {
      background: #373737;
      border-color: #434343;
      .ed-icon {
        color: #ebebeb;
      }
    }
  }

  &.img-area_light {
    :deep(.ed-upload-list__item).is-ready {
      border-color: #dee0e3;
    }
  }
  :deep(.ed-upload-list__item-preview) {
    display: none !important;
  }
  :deep(.ed-upload-list__item-delete) {
    margin-left: 0 !important;
  }
  :deep(.ed-upload-list__item-status-label) {
    display: none !important;
  }
  :deep(.ed-icon--close-tip) {
    display: none !important;
  }
}
.avatar-uploader {
  width: 90px;
  height: 80px;
  overflow: hidden;
}
.avatar-uploader {
  width: 90px;
  :deep(.ed-upload) {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }

  :deep(.ed-upload-list li) {
    width: 80px !important;
    height: 80px !important;
  }

  :deep(.ed-upload--picture-card) {
    background: #eff0f1;
    border: 1px dashed #dee0e3;
    border-radius: 4px;

    .ed-icon {
      color: #1f2329;
    }

    &:hover {
      .ed-icon {
        color: var(--ed-color-primary);
      }
    }
  }
}
.uploader {
  :deep(.ed-form-item__content) {
    justify-content: center;
  }
}
.data-area-label {
  text-align: left;
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
}
.radius-class {
  :deep(.ed-radio) {
    margin-right: 30px !important;
  }
  .ed-radio:last-child {
    margin-right: 0px !important;
  }
}
</style>
