<script setup lang="ts">
import {
  PropType,
  reactive,
  ref,
  watch,
  toRefs,
  computed,
  nextTick,
  onBeforeMount,
  provide,
  unref,
  onBeforeUnmount,
  onMounted
} from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import { cloneDeep, forEach, get, debounce, set, concat, keys } from 'lodash-es'

import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import DimensionItem from '@/views/chart/components/editor/drag-item/DimensionItem.vue'

import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'

import chartViewManager from '@/views/chart/components/js/panel'
import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import findComponent from '@/utils/components'

// store
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const dvMainStore = dvMainStoreWithOut()

const {
  canvasCollapse,
  curComponent,
  componentData,
  editMode,
  canvasViewInfo,
  fullscreenFlag,
  dvInfo
} = storeToRefs(dvMainStore)
import {
  getStyle,
  getComponentRotatedStyle,
  getShapeItemStyle,
  getCanvasStyle,
  syncShapeItemStyle
} from '@/utils/style'
// hook
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'

const { t } = useI18n()

const showAxis = (axis: AxisType) => chartViewInstance.value?.axis?.includes(axis)

const props = defineProps({
  view: {
    type: Object as PropType<ChartObj>,
    required: false,
    default() {
      return { ...BASE_VIEW_CONFIG }
    }
  },
  datasetTree: {
    type: Array as PropType<Tree[]>,
    default: () => []
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  draggable: {
    required: false,
    default: true,
    type: Boolean
  },
  dragStart: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  dragging: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  dragEnd: {
    required: false,
    type: Function,
    default() {
      return {}
    }
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  }
})
const commonFilterAttrs = ['width', 'height', 'top', 'left', 'rotate']
const commonFilterAttrsFilterBorder = [
  'width',
  'height',
  'top',
  'left',
  'rotate',
  'borderActive',
  'borderWidth',
  'borderRadius',
  'borderStyle',
  'borderColor'
]
const { view } = toRefs(props)
const customSortAxis = ref<AxisType>('xAxis')
const state = reactive({
  extData: '',
  moveId: -1,
  dimension: [],
  quota: [],
  dimensionData: [],
  quotaData: [],
  renameItem: false,
  itemForm: {
    name: '',
    chartShowName: '',
    index: 0,
    renameType: ''
  },
  quotaFilterEdit: false,
  quotaItem: {},
  resultFilterEdit: false,
  filterItem: {},
  chartForFilter: {},
  searchField: '',
  quotaItemCompare: {},
  showEditQuotaCompare: false,
  showValueFormatter: false,
  valueFormatterItem: {},
  showCustomSort: false,
  showSortPriority: false,
  sortPriority: [],
  customSortList: [],
  customSortField: {},
  currEditField: {},
  worldTree: [],
  areaId: '',
  chartTypeOptions: [],
  useless: null
})
const snapshotStore = snapshotStoreWithOut()
const { emitter } = useEmitt({
  name: 'set-table-column-width',
  callback: args => onTableColumnWidthChange(args)
})
const recordSnapshotInfo = type => {
  view.value['dataFrom'] = 'calc'
  snapshotStore.recordSnapshotCache(type, view.value.id)
}
const removeItems = (
  _type:
    | 'xAxis'
    | 'xAxisExt'
    | 'extStack'
    | 'yAxis'
    | 'yAxisExt'
    | 'extBubble'
    | 'customFilter'
    | 'drillFields'
    | 'flowMapStartName'
    | 'flowMapEndName'
    | 'extColor'
) => {
  recordSnapshotInfo('calcData')
  let axis = []
  switch (_type) {
    case 'xAxis':
      axis = view.value.xAxis?.splice(0)
      break
    case 'xAxisExt':
      axis = view.value.xAxisExt?.splice(0)
      break
    case 'extStack':
      axis = view.value.extStack?.splice(0)
      break
    case 'yAxis':
      axis = view.value.yAxis?.splice(0)
      break
    case 'yAxisExt':
      axis = view.value.yAxisExt?.splice(0)
      break
    case 'extBubble':
      axis = view.value.extBubble?.splice(0)
      break
    case 'customFilter':
      view.value.customFilter = {}
      return
      break
    case 'drillFields':
      axis = view.value.drillFields?.splice(0)
      break
    case 'flowMapStartName':
      axis = view.value.flowMapStartName?.splice(0)
      break
    case 'flowMapEndName':
      axis = view.value.flowMapEndName?.splice(0)
      break
    case 'extColor':
      axis = view.value.extColor?.splice(0)
      break
  }
  axis?.length && emitter.emit('removeAxis', { axisType: _type, axis, editType: 'remove' })
}
const activeDimension = ref<Axis[]>([])
const activeQuota = ref<Axis[]>([])

const drop = (ev: MouseEvent, type = 'xAxis') => {
  ev.preventDefault()
  const arr = activeDimension.value.length ? activeDimension.value : activeQuota.value
  for (let i = 0; i < arr.length; i++) {
    const obj = cloneDeep(arr[i])
    state.moveId = obj.id as unknown as number
    view.value[type] ??= []
    view.value[type].push(obj)
    const e = { newDraggableIndex: view.value[type].length - 1 }

    if ('drillFields' === type) {
      addDrill(e)
    } else {
      addAxis(e, type as AxisType)
    }
  }
}

const dragCheckMapType = list => {
  if (list && list.length > 0) {
    let valid = true
    for (let i = 0; i < list.length; i++) {
      if (list[i].deType !== 5) {
        list.splice(i, 1)
        valid = false
      }
    }
    if (!valid) {
      ElMessage({
        message: t('chart.error_d_not_coordinates'),
        type: 'warning'
      })
    }
    return valid
  }
}
const onMove = e => {
  recordSnapshotInfo('calcData')
  state.moveId = e.draggedContext.element.id
  return true
}
const addAxis = (e, axis: AxisType) => {
  recordSnapshotInfo('calcData')
  const axisSpec = chartViewInstance.value?.axisConfig[axis]
  if (!axisSpec) {
    return
  }
  const { type, limit, duplicate } = axisSpec
  let typeValid, dup

  if (view.value.type === 'bar-range' && (axis === 'yAxis' || axis === 'yAxisExt')) {
    //区间条形图先排除非时间纬度或者指标的情况
    const list = view.value[axis]
    if (list && list.length > 0) {
      let valid = true
      for (let i = 0; i < list.length; i++) {
        if (list[i].groupType === 'd' && list[i].deType === 1) {
          list[i].sort = 'asc'
        }
        if (!(list[i].groupType === 'q' || (list[i].groupType === 'd' && list[i].deType === 1))) {
          list.splice(i, 1)
          valid = false
        }
      }
      if (!valid) {
        ElMessage({
          message: t('chart.error_d_not_time_2_q'),
          type: 'warning'
        })
      }
      typeValid = valid
    }
  } else if (
    ((view.value.type === 'symbolic-map' || view.value.type === 'heat-map') && axis === 'xAxis') ||
    (view.value.type === 'flow-map' && (axis === 'xAxis' || axis === 'xAxisExt'))
  ) {
    typeValid = dragCheckMapType(view.value[axis])
  } else if (type) {
    typeValid = dragCheckType(view.value[axis], type)
  }

  // 针对指标卡进行数值类型判断
  if (typeValid && type === 'q' && view.value.type === 'indicator') {
    const list = view.value[axis]
    if (list && list.length > 0) {
      let valid = true
      for (let i = 0; i < list.length; i++) {
        if (list[i].deType !== 2 && list[i].deType !== 3) {
          list.splice(i, 1)
          valid = false
        }
      }
      typeValid = valid
      if (!typeValid) {
        ElMessage({
          message: t('chart.error_not_number'),
          type: 'warning'
        })
      }
    }
  }

  if (!duplicate) {
    dup = dragMoveDuplicate(view.value[axis], e, 'chart')
  }
  if (view.value[axis].length > limit) {
    const removedAxis = view.value[axis].splice(limit)
    if (e.newDraggableIndex + 1 <= limit) {
      emitter.emit('removeAxis', { axisType: axis, axis: removedAxis, editType: 'remove' })
      emitter.emit('addAxis', {
        axisType: axis,
        axis: [view.value[axis][e.newDraggableIndex]],
        editType: 'add'
      })
    }
  } else {
    if (!dup && typeValid) {
      const isGaugeOrLiquid = view.value.type === 'gauge' || view.value.type === 'liquid'
      const quotaData = cloneDeep(state.quotaData)
      emitter.emit('addAxis', {
        axisType: axis,
        axis: [view.value[axis][e.newDraggableIndex]],
        editType: 'add',
        ...(isGaugeOrLiquid ? { quotaData: quotaData } : {})
      })
    }
  }
  if (view.value.type === 'line') {
    if (view.value?.xAxisExt?.length && view.value?.yAxis?.length > 1) {
      const axis = view.value.yAxis.splice(1)
      emitter.emit('removeAxis', { axisType: 'yAxis', axis, editType: 'remove' })
    }
  }
  if (view.value.type.includes('chart-mix')) {
    if (axis === 'yAxis') {
      if (view.value.yAxisExt.length > 0) {
        const chartType = view.value.yAxisExt[0].chartType
        forEach(view.value.yAxis, axis => {
          if (chartType === 'bar') {
            axis.chartType = 'line'
          } else if (chartType === 'line') {
            axis.chartType = 'bar'
          }
        })
      }
    } else if (axis === 'yAxisExt') {
      if (view.value.yAxis.length > 0) {
        const chartType = view.value.yAxis[0].chartType
        forEach(view.value.yAxisExt, axis => {
          if (chartType === 'bar') {
            axis.chartType = 'line'
          } else if (chartType === 'line') {
            axis.chartType = 'bar'
          }
        })
      }
    }
  }

  if (typeValid && view.value.type === 'bar-range') {
    //处理某一个轴有数据的情况
    let tempType = null
    let tempDeType = null
    if (axis === 'yAxis' && view.value.yAxisExt[0]) {
      tempType = view.value.yAxisExt[0].groupType
      tempDeType = view.value.yAxisExt[0].deType
    } else if (axis === 'yAxisExt' && view.value.yAxis[0]) {
      tempType = view.value.yAxis[0].groupType
      tempDeType = view.value.yAxis[0].deType
    }
    if (tempType !== null) {
      const list = view.value[axis]
      if (list && list.length > 0) {
        let valid = true
        for (let i = 0; i < list.length; i++) {
          if (
            !(
              list[i].groupType === tempType &&
              (tempType === 'q' || (tempType === 'd' && list[i].deType === tempDeType))
            )
          ) {
            list.splice(i, 1)
            valid = false
          }
        }
        if (!valid) {
          ElMessage({
            message: t('chart.error_bar_range_axis_type_not_equal'),
            type: 'warning'
          })
        }
        typeValid = valid
      }
    }
  }
}
const addXaxis = e => {
  addAxis(e, 'xAxis')
}
const addDrill = e => {
  recordSnapshotInfo('calcData')
  dragCheckType(view.value.drillFields, 'd')
  dragMoveDuplicate(view.value.drillFields, e, '')
  dragRemoveAggField(view.value.drillFields, e)
}
const onTableColumnWidthChange = val => {
  if (editMode.value !== 'edit') {
    return
  }
  view.value.customAttr.basicStyle.tableFieldWidth = val
  snapshotStore.recordSnapshotCache('renderChart', view.value.id)
}

const dimensionItemChange = item => {
  recordSnapshotInfo('calcData')
  // do dimensionItemChange
  if (view.value.type === 'bar-range') {
    if (item.axisType === 'quota') {
      view.value.yAxisExt?.forEach(y => {
        y.dateStyle = item.dateStyle
        y.datePattern = item.datePattern
      })
    } else if (item.axisType === 'quotaExt') {
      view.value.yAxis?.forEach(y => {
        y.dateStyle = item.dateStyle
        y.datePattern = item.datePattern
      })
    }
  }
}
const dimensionItemRemove = item => {
  recordSnapshotInfo('calcData')
  if (item.removeType === 'dimension') {
    view.value.xAxis.splice(item.index, 1)
  } else if (item.removeType === 'dimensionExt') {
    view.value.xAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'dimensionStack') {
    view.value.extStack.splice(item.index, 1)
  } else if (item.removeType === 'quota') {
    view.value.yAxis.splice(item.index, 1)
  } else if (item.removeType === 'quotaExt') {
    view.value.yAxisExt.splice(item.index, 1)
  } else if (item.removeType === 'xAxisExtRight') {
    view.value.extBubble.splice(item.index, 1)
  } else if (item.removeType === 'flowMapStartName') {
    view.value.flowMapStartName.splice(item.index, 1)
  } else if (item.removeType === 'flowMapEndName') {
    view.value.flowMapEndName.splice(item.index, 1)
  } else if (item.removeType === 'extColor') {
    view.value.extColor.splice(item.index, 1)
  }
}
const getComponentStyle = style => {
  return getStyle(style, style.borderActive ? commonFilterAttrs : commonFilterAttrsFilterBorder)
}

// computed
const chartViewInstance = computed(() => {
  return chartViewManager.getChartView(view.value.render, view.value.type)
})

const toolTip = computed(() => {
  return props.themes || 'dark'
})
const curComponentId = computed(() => {
  return curComponent.value?.id || ''
})
const onCustomSort = item => {
  recordSnapshotInfo('render')
  state.customSortField = view.value.xAxis[item.index]
  customSortAxis.value = 'xAxis'
  // customSort()
}

</script>

<template>
    <div class="main-content">
      <div class="config-area">
        <!--area-->
        <el-row v-if="showAxis('area')" class="line-style drag-data">
          <span class="data-area-label">
            {{ t('chart.area') }}
            <i class="required"></i>
          </span>
          <div class="area-tree-select">
            <el-tree-select
              ref="areaSelect"
              v-model="state.areaId"
              :effect="themes"
              :data="state.worldTree"
              :props="treeProps"
              :filterNodeMethod="filterNode"
              :current-node-key="state.areaId"
              :teleported="false"
              :default-expanded-keys="expandKeys"
              empty-text="请选择区域"
              node-key="id"
              check-strictly
              filterable
              @node-click="onAreaChange"
            />
          </div>
        </el-row>
        <!--xAxis-->
        <el-row v-if="showAxis('xAxis')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.xAxis.name }}
              <i
                v-if="!chartViewInstance.axisConfig.xAxis?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('xAxis')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            class="qw"
            @drop="$event => drop($event)"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.xAxis"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addXaxis"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="dimension"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onCustomSort="onCustomSort"
                  @valueFormatter="valueFormatter"
                  @onToggleHide="onToggleHide"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :themes="themes" :drag-list="view.xAxis" />
          </div>
        </el-row>

        <!--xAxisExt-->
        <el-row v-if="showAxis('xAxisExt')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.xAxisExt.name }}
              <i
                v-if="!chartViewInstance.axisConfig.xAxisExt?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('xAxisExt')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            @drop="$event => drop($event, 'xAxisExt')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.xAxisExt"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addXaxisExt"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="dimensionExt"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onExtCustomSort"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :drag-list="view.xAxisExt" />
          </div>
        </el-row>

        <!--flowMapStartName-->
        <el-row v-if="showAxis('flowMapStartName')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.flowMapStartName.name }}
              <i
                v-if="!chartViewInstance.axisConfig.flowMapStartName?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('flowMapStartName')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            class="qw"
            @drop="$event => drop($event, 'flowMapStartName')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.flowMapStartName"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addFlowMapStartName"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="flowMapStartName"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onCustomFlowMapStartNameSort"
                  @valueFormatter="valueFormatter"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :themes="themes" :drag-list="view.flowMapStartName" />
          </div>
        </el-row>

        <!--flowMapEndName-->
        <el-row v-if="showAxis('flowMapEndName')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.flowMapEndName.name }}
              <i
                v-if="!chartViewInstance.axisConfig.flowMapEndName?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('flowMapEndName')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            class="qw"
            @drop="$event => drop($event, 'flowMapEndName')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.flowMapEndName"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addFlowMapEndName"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="flowMapEndName"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onCustomFlowMapEndNameSort"
                  @valueFormatter="valueFormatter"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :themes="themes" :drag-list="view.flowMapEndName" />
          </div>
        </el-row>

        <!--extStack-->
        <el-row v-if="showAxis('extStack')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.extStack.name }}
              <i
                v-if="!chartViewInstance.axisConfig.extStack?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('extStack')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            @drop="$event => drop($event, 'extStack')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.extStack"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addExtStack"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="dimensionStack"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onStackCustomSort"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :drag-list="view.extStack" />
          </div>
        </el-row>

        <el-row v-if="showAxis('extColor')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span>
              {{ chartViewInstance.axisConfig.extColor.name }}
              <i
                v-if="!chartViewInstance.axisConfig.extColor?.allowEmpty"
                class="required"
              ></i>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('extColor')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            class="qw"
            @drop="$event => drop($event, 'extColor')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.extColor"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addExtColor"
              @change="e => onAxisChange(e, 'extColor')"
            >
              <template #item="{ element, index }">
                <dimension-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  :themes="props.themes"
                  type="extColor"
                  @onDimensionItemChange="dimensionItemChange"
                  @onDimensionItemRemove="dimensionItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onCustomExtColorSort"
                  @valueFormatter="valueFormatter"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :themes="themes" :drag-list="view.extColor" />
          </div>
        </el-row>

        <template v-if="view.type !== 'bar-range'">
          <!--yAxis-->
          <el-row v-if="showAxis('yAxis')" class="line-style drag-data">
            <div class="form-draggable-title">
              <span class="data-area-label">
                <span style="margin-right: 4px">
                  {{ chartViewInstance.axisConfig.yAxis.name }}
                  <i
                    v-if="!chartViewInstance.axisConfig.yAxis?.allowEmpty"
                    class="required"
                  ></i>
                </span>
                <el-tooltip
                  v-if="chartViewInstance.axisConfig.yAxis.tooltip"
                  class="item"
                  :effect="toolTip"
                  placement="top"
                >
                  <template #content>
                    <span> {{ chartViewInstance.axisConfig.yAxis.tooltip }}</span>
                  </template>
                  <el-icon
                    class="hint-icon"
                    :class="{ 'hint-icon--dark': themes === 'dark' }"
                  >
                    <Icon name="icon_info_outlined"
                      ><icon_info_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </el-tooltip>
              </span>
              <el-tooltip
                :effect="toolTip"
                placement="top"
                :content="t('common.delete')"
              >
                <el-icon
                  class="remove-icon"
                  :class="{ 'remove-icon--dark': themes === 'dark' }"
                  size="14px"
                  @click="removeItems('yAxis')"
                >
                  <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon inner-class"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
            <div
              @drop="$event => drop($event, 'yAxis')"
              @dragenter="dragEnter"
              @dragover="$event => dragOver($event)"
            >
              <draggable
                :list="view.yAxis"
                :move="onMove"
                item-key="id"
                group="drag"
                animation="300"
                class="drag-block-style"
                :class="{ dark: themes === 'dark' }"
                @add="addYaxis"
                @change="e => onAxisChange(e, 'yAxis')"
              >
                <template #item="{ element, index }">
                  <quota-item
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    type="quota"
                    :themes="props.themes"
                    @onQuotaItemChange="item => quotaItemChange(item, 'yAxis')"
                    @onQuotaItemRemove="quotaItemRemove"
                    @onNameEdit="showRename"
                    @editItemFilter="showQuotaEditFilter"
                    @editItemCompare="showQuotaEditCompare"
                    @valueFormatter="valueFormatter"
                    @onToggleHide="onToggleHide"
                    @editSortPriority="editSortPriority"
                  />
                </template>
              </draggable>
              <drag-placeholder
                :margin-top="view.type === 'stock-line' ? '9px' : '0'"
                :drag-list="view.yAxis"
              />
            </div>
          </el-row>
          <!-- xAxisExtRight -->
          <el-row v-if="showAxis('xAxisExtRight')" class="line-style drag-data">
            <div class="form-draggable-title">
              <span>
                {{ chartViewInstance.axisConfig.extBubble.name }}
                <i
                  v-if="!chartViewInstance.axisConfig.extBubble?.allowEmpty"
                  class="required"
                ></i>
              </span>
              <el-tooltip
                :effect="toolTip"
                placement="top"
                :content="t('common.delete')"
              >
                <el-icon
                  class="remove-icon"
                  :class="{ 'remove-icon--dark': themes === 'dark' }"
                  size="14px"
                  @click="removeItems('extBubble')"
                >
                  <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon inner-class"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
            <div
              @drop="$event => drop($event, 'extBubble')"
              @dragenter="dragEnter"
              @dragover="$event => dragOver($event)"
            >
              <draggable
                :list="view.extBubble"
                :move="onMove"
                item-key="id"
                group="drag"
                animation="300"
                class="drag-block-style"
                :class="{ dark: themes === 'dark' }"
                @add="addExtBubble"
                @change="e => onAxisChange(e, 'extBubble')"
              >
                <template #item="{ element, index }">
                  <dimension-item
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    :themes="props.themes"
                    type="xAxisExtRight"
                    @onDimensionItemChange="dimensionItemChange"
                    @onDimensionItemRemove="dimensionItemRemove"
                    @onNameEdit="showRename"
                    @onCustomSort="onExtCustomRightSort"
                    @editSortPriority="editSortPriority"
                  />
                </template>
              </draggable>
              <drag-placeholder :drag-list="view.extBubble" />
            </div>
          </el-row>
          <!--yAxisExt-->
          <el-row v-if="showAxis('yAxisExt')" class="line-style drag-data">
            <div class="form-draggable-title">
              <span>
                {{ chartViewInstance.axisConfig.yAxisExt.name }}
                <i
                  v-if="!chartViewInstance.axisConfig.yAxisExt?.allowEmpty"
                  class="required"
                ></i>
              </span>
              <el-tooltip
                :effect="toolTip"
                placement="top"
                :content="t('common.delete')"
              >
                <el-icon
                  class="remove-icon"
                  :class="{ 'remove-icon--dark': themes === 'dark' }"
                  size="14px"
                  @click="removeItems('yAxisExt')"
                >
                  <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon inner-class"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
            <div
              @drop="$event => drop($event, 'yAxisExt')"
              @dragenter="dragEnter"
              @dragover="$event => dragOver($event)"
            >
              <draggable
                :list="view.yAxisExt"
                :move="onMove"
                item-key="id"
                group="drag"
                animation="300"
                class="drag-block-style"
                :class="{ dark: themes === 'dark' }"
                @add="addYaxisExt"
                @change="e => onAxisChange(e, 'yAxisExt')"
              >
                <template #item="{ element, index }">
                  <quota-item
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    type="quotaExt"
                    :themes="props.themes"
                    @onQuotaItemChange="item => quotaItemChange(item, 'yAxisExt')"
                    @onQuotaItemRemove="quotaItemRemove"
                    @onNameEdit="showRename"
                    @editItemFilter="showQuotaEditFilter"
                    @editItemCompare="showQuotaEditCompare"
                    @valueFormatter="valueFormatter"
                    @editSortPriority="editSortPriority"
                  />
                </template>
              </draggable>
              <drag-placeholder :drag-list="view.yAxisExt" />
            </div>
          </el-row>
        </template>
        <template v-else-if="view.type === 'bar-range'">
          <!--yAxis-->
          <el-row v-if="showAxis('yAxis')" class="line-style drag-data">
            <div class="form-draggable-title">
              <span>
                {{ chartViewInstance.axisConfig.yAxis.name }}
                <i
                  v-if="!chartViewInstance.axisConfig.yAxis?.allowEmpty"
                  class="required"
                ></i>
              </span>
              <el-tooltip
                :effect="toolTip"
                placement="top"
                :content="t('common.delete')"
              >
                <el-icon
                  class="remove-icon"
                  :class="{ 'remove-icon--dark': themes === 'dark' }"
                  size="14px"
                  @click="removeItems('yAxis')"
                >
                  <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon inner-class"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
            <div
              @drop="$event => drop($event, 'yAxis')"
              @dragenter="dragEnter"
              @dragover="$event => dragOver($event)"
            >
              <draggable
                :list="view.yAxis"
                :move="onMove"
                item-key="id"
                group="drag"
                animation="300"
                class="drag-block-style"
                :class="{ dark: themes === 'dark' }"
                @add="addYaxis"
                @change="e => onAxisChange(e, 'yAxis')"
              >
                <template #item="{ element, index }">
                  <dimension-item
                    v-if="element.groupType === 'd'"
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    :themes="props.themes"
                    type="quota"
                    @onDimensionItemChange="dimensionItemChange"
                    @onDimensionItemRemove="dimensionItemRemove"
                    @onNameEdit="showRename"
                    @onCustomSort="onExtCustomSort"
                    @editSortPriority="editSortPriority"
                  />
                  <quota-item
                    v-else-if="element.groupType === 'q'"
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    type="quota"
                    :themes="props.themes"
                    @onQuotaItemChange="item => quotaItemChange(item, 'yAxis')"
                    @onQuotaItemRemove="quotaItemRemove"
                    @onNameEdit="showRename"
                    @editItemFilter="showQuotaEditFilter"
                    @editItemCompare="showQuotaEditCompare"
                    @valueFormatter="valueFormatter"
                    @editSortPriority="editSortPriority"
                  />
                </template>
              </draggable>
              <drag-placeholder :drag-list="view.yAxis" />
            </div>
          </el-row>
          <!--yAxisExt-->
          <el-row v-if="showAxis('yAxisExt')" class="line-style drag-data">
            <div class="form-draggable-title">
              <span>
                {{ chartViewInstance.axisConfig.yAxisExt.name }}
                <i
                  v-if="!chartViewInstance.axisConfig.yAxisExt?.allowEmpty"
                  class="required"
                ></i>
              </span>
              <el-tooltip
                :effect="toolTip"
                placement="top"
                :content="t('common.delete')"
              >
                <el-icon
                  class="remove-icon"
                  :class="{ 'remove-icon--dark': themes === 'dark' }"
                  size="14px"
                  @click="removeItems('yAxisExt')"
                >
                  <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon inner-class"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
            <div
              @drop="$event => drop($event, 'yAxisExt')"
              @dragenter="dragEnter"
              @dragover="$event => dragOver($event)"
            >
              <draggable
                :list="view.yAxisExt"
                :move="onMove"
                item-key="id"
                group="drag"
                animation="300"
                class="drag-block-style"
                :class="{ dark: themes === 'dark' }"
                @add="addYaxisExt"
                @change="e => onAxisChange(e, 'yAxisExt')"
              >
                <template #item="{ element, index }">
                  <dimension-item
                    v-if="element.groupType === 'd'"
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    :themes="props.themes"
                    type="quotaExt"
                    @onDimensionItemChange="dimensionItemChange"
                    @onDimensionItemRemove="dimensionItemRemove"
                    @onNameEdit="showRename"
                    @onCustomSort="onExtCustomSort"
                    @editSortPriority="editSortPriority"
                  />
                  <quota-item
                    v-else-if="element.groupType === 'q'"
                    :dimension-data="state.dimension"
                    :quota-data="state.quota"
                    :chart="view"
                    :item="element"
                    :index="index"
                    type="quotaExt"
                    :themes="props.themes"
                    @onQuotaItemChange="item => quotaItemChange(item, 'yAxisExt')"
                    @onQuotaItemRemove="quotaItemRemove"
                    @onNameEdit="showRename"
                    @editItemFilter="showQuotaEditFilter"
                    @editItemCompare="showQuotaEditCompare"
                    @valueFormatter="valueFormatter"
                    @editSortPriority="editSortPriority"
                  />
                </template>
              </draggable>
              <drag-placeholder :drag-list="view.yAxisExt" />
            </div>
          </el-row>
        </template>
        <!-- extBubble -->
        <el-row v-if="showAxis('extBubble')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span class="data-area-label">
              <span style="margin-right: 4px">
                {{ chartViewInstance.axisConfig.extBubble.name }}
                <i
                  v-if="!chartViewInstance.axisConfig.extBubble?.allowEmpty"
                  class="required"
                ></i>
              </span>
              <el-tooltip
                v-if="chartViewInstance.axisConfig.extBubble.tooltip"
                class="item"
                :effect="toolTip"
                placement="top"
              >
                <template #content>
                  <span> {{ chartViewInstance.axisConfig.extBubble.tooltip }}</span>
                </template>
                <el-icon
                  class="hint-icon"
                  :class="{ 'hint-icon--dark': themes === 'dark' }"
                >
                  <Icon name="icon_info_outlined"
                    ><icon_info_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('extBubble')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            @drop="$event => drop($event, 'extBubble')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.extBubble"
              :move="onMove"
              item-key="id"
              group="drag"
              animation="300"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addExtBubble"
              @change="e => onAxisChange(e, 'extBubble')"
            >
              <template #item="{ element, index }">
                <quota-item
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :chart="view"
                  :item="element"
                  :index="index"
                  type="extBubble"
                  :themes="props.themes"
                  @onQuotaItemChange="item => quotaItemChange(item, 'extBubble')"
                  @onQuotaItemRemove="quotaItemRemove"
                  @onNameEdit="showRename"
                  @editItemFilter="showQuotaEditFilter"
                  @editItemCompare="showQuotaEditCompare"
                  @valueFormatter="valueFormatter"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :drag-list="view.extBubble" />
          </div>
        </el-row>

        <!--drill-->
        <el-row v-if="showAxis('drill')" class="line-style drag-data">
          <div class="form-draggable-title">
            <span class="data-area-label">
              <span style="margin-right: 4px">
                {{ t('chart.drill') }} / {{ t('chart.dimension') }}
              </span>
              <el-tooltip class="item" :effect="toolTip" placement="top">
                <template #content>
                  <span> {{ t('chart.drill_dimension_tip') }}</span>
                </template>
                <el-icon
                  class="hint-icon"
                  :class="{ 'hint-icon--dark': themes === 'dark' }"
                >
                  <Icon name="icon_info_outlined"
                    ><icon_info_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('drillFields')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
          <div
            @drop="$event => drop($event, 'drillFields')"
            @dragenter="dragEnter"
            @dragover="$event => dragOver($event)"
          >
            <draggable
              :list="view.drillFields"
              item-key="id"
              group="drag"
              animation="300"
              :move="onMove"
              class="drag-block-style"
              :class="{ dark: themes === 'dark' }"
              @add="addDrill"
            >
              <template #item="{ element, index }">
                <drill-item
                  :key="element.id"
                  :index="index"
                  :chart="view"
                  :item="element"
                  :dimension-data="state.dimension"
                  :quota-data="state.quota"
                  :themes="props.themes"
                  @onDimensionItemChange="drillItemChange"
                  @onDimensionItemRemove="drillItemRemove"
                  @onNameEdit="showRename"
                  @onCustomSort="onDrillCustomSort"
                  @editSortPriority="editSortPriority"
                />
              </template>
            </draggable>
            <drag-placeholder :drag-list="view.drillFields" />
          </div>
        </el-row>

        <!--filter-->
        <el-row class="line-style drag-data no-top-border no-top-padding">
          <div class="form-draggable-title">
            <span>
              {{ t('chart.result_filter') }}
            </span>
            <el-tooltip
              :effect="toolTip"
              placement="top"
              :content="t('common.delete')"
            >
              <el-icon
                class="remove-icon"
                :class="{ 'remove-icon--dark': themes === 'dark' }"
                size="14px"
                @click="removeItems('customFilter')"
              >
                <Icon class-name="inner-class" name="icon_delete-trash_outlined"
                  ><icon_deleteTrash_outlined class="svg-icon inner-class"
                /></Icon>
              </el-icon>
            </el-tooltip>
          </div>

          <div
            class="tree-btn"
            v-if="isFilterActive || themes === 'dark'"
            :class="{
              'tree-btn--dark': themes === 'dark',
              active: isFilterActive,
              invalid: isFilterInvalid
            }"
            @click="openTreeFilter"
          >
            <el-icon style="margin-right: 2px; font-size: 12px">
              <Icon class="svg-background" name="icon-filter"
                ><iconFilter class="svg-icon svg-background"
              /></Icon>
            </el-icon>

            <span>{{ $t('chart.filter') }}</span>
          </div>
          <el-button
            v-else
            class="tree-btn_secondary"
            secondary
            @click="openTreeFilter"
          >
            <template #icon>
              <Icon><iconFilter class="svg-icon svg-background" /></Icon>
            </template>
            <span>{{ $t('chart.filter') }}</span>
          </el-button>
        </el-row>

        <el-row v-if="showAggregate" class="refresh-area">
          <el-form-item
            class="form-item no-margin-bottom"
            :class="'form-item-' + themes"
          >
            <el-checkbox
              v-model="view.aggregate"
              :effect="themes"
              size="small"
              @change="aggregateChange"
            >
              {{ t('chart.aggregate_time') }}
            </el-checkbox>
          </el-form-item>
        </el-row>
      </div>
      <div class="data-area">
        <div class="button-area">
          <el-button size="small" class="arco-btn fullscreen-btn">全屏</el-button>
          <el-button size="small" class="arco-btn data-view-btn">查询</el-button>
        </div>
        <div v-for="item in componentData" :key="item.id">
          <component
            :is="findComponent(item.component)"
            v-if="item.component === 'UserView' || item['isPlugin']"
            class="component"
            :id="'component' + item.id"
            :dv-type="dvInfo.type"
            :style="getComponentStyle(item.style)"
            :prop-value="item.propValue"
            :view="canvasViewInfo[item.id]"
            :element="item"
            :request="item.request"
            :dv-info="dvInfo"
            :font-family="fontFamily"
            />
            <!-- :active="item.id === curComponentId" -->
            <!-- @input="handleInput" -->
            <!-- :is-edit="true" -->
            <!-- :scale="curBaseScale" -->
            <!-- :show-position="'canvas'" -->
            <!-- :canvas-active="canvasActive" -->
        </div>
      </div>
    </div>
</template>

<style lang="less" scoped>
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding-left: 8px;
  font-size: 13px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  background-color: #f5f6f7;
  .config-area{
    padding-top: 12px;
    background-color: #fff;
  }
  .data-area-label{
    line-height: 24px;
  }
  .data-area{
    flex: 1;
    margin-top: 12px;
    background-color: #fff;
  }
}
.line-style{
  border-bottom: 1px solid #e3e5eb;
  min-height: 40px;
  padding: 8px 8px;
  // padding: 0px 16px 0px 8px;
}
.data-area{
  padding: 8px 8px;
}
/* 全屏按钮样式 */
.fullscreen-btn,.data-view-btn {
  font-size: 13px;
  // height: 24px;
  border: 1px solid #dee0e3;
  cursor: pointer;
  margin-right: 8px;
}

.arco-btn.fullscreen-btn::before {
  content: '';
  width: 11px;
  height: 11px;
  margin-right: 2px;
  background: url('@/assets/svg/fullscreen.svg') no-repeat center;
  background-size: contain;
}

.fullscreen-btn:hover,.fullscreen-btn:active,.data-view-btn:hover,.data-view-btn:active {
  border-color: #3370ff;
  background-color: #f2f5ff;
}

.arco-btn.data-view-btn::before {
  content: '';
  width: 14px;
  height: 14px;
  margin-right: 2px;
  background: url('@/assets/svg/reference-play.svg') center no-repeat;
  background-size: contain;
}
</style>
