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

// ===== 第三方库导入 =====
import { storeToRefs } from 'pinia'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import { useRouter, useRoute } from 'vue-router_2'
import { useDraggable } from '@vueuse/core'

// ===== 组件导入 =====
import DatasetSelect from '@/views/chart/components/editor/dataset-select/DatasetSelect.vue'
import ToggleButton from '@/components/common/ToggleButton.vue'

// ===== 图标组件导入 =====
import { iconChartMap } from '@/components/icon-group/chart-list'
import { iconFieldMap } from '@/components/icon-group/field-list'
import {
  iconFieldCalculatedMap,
  iconFieldCalculatedQMap
} from '@/components/icon-group/field-calculated-list'

// ===== Hooks 导入 =====
import { useI18n } from '@/hooks/web/useI18n'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'

// ===== Store 导入 =====
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

// ===== API 导入 =====
import { Field, getFieldByDQ, copyChartField, deleteChartField } from '@/api/chart'

// ===== 工具函数导入 =====
import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import { fieldType } from '@/utils/attr'

const { t } = useI18n()

// ===== Props 定义 =====
const props = defineProps({
  view: {
    type: Object as PropType<ChartObj>,
    required: false,
    default() {
      return { ...BASE_VIEW_CONFIG }
    }
  },
  // datasetTree: {
  //   type: Array as PropType<Tree[]>,
  //   default: () => []
  // },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

// ===== Store 实例化 =====
const snapshotStore = snapshotStoreWithOut()
const dvMainStore = dvMainStoreWithOut()
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()

// ===== Store 状态解构 =====
const { view } = toRefs(props)
const {
  canvasCollapse,
  curComponent,
  componentData,
  editMode,
  mobileInPc,
  fullscreenFlag,
  dvInfo
} = storeToRefs(dvMainStore)

// ===== 工具实例化 =====
const { wsCache } = useCache('localStorage')
const router = useRouter()

// ===== 响应式状态定义 =====
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

// ===== Ref 定义 =====
const el = ref<HTMLElement | null>(null)
const elDrag = ref<HTMLElement | null>(null)
const openHandler = ref(null)
const previewHeight = ref(0)
const fieldLoading = ref(false)
const datasetSelector = ref(null)
const curDatasetWeight = ref(0)
const activeDimension = ref<Axis[]>([])
const activeQuota = ref<Axis[]>([])

// ===== 动态高度计算 =====
const elDragHeight = ref('auto')

const calculateElDragHeight = () => {
  nextTick(() => {
    const edMainElement = document.querySelector('.dataset-main-top')
    const datasetHeaderElement = document.querySelector('.dataset-header')
    
    if (edMainElement && datasetHeaderElement) {
      const edMainHeight = edMainElement.clientHeight
      const datasetHeaderHeight = datasetHeaderElement.clientHeight
      const remainingHeight = edMainHeight - datasetHeaderHeight
      elDragHeight.value = `${remainingHeight}px`
    }
  })
}

// ===== 拖拽功能 =====
const { y, isDragging } = useDraggable(el, {
  initialValue: { x: 0, y: 400 },
  draggingElement: elDrag
})

// ===== 事件监听器 =====
const onTableColumnWidthChange = val => {
  if (editMode.value !== 'edit') {
    return
  }
  view.value.customAttr.basicStyle.tableFieldWidth = val
  snapshotStore.recordSnapshotCache('renderChart', view.value.id)
}

const { emitter } = useEmitt({
  name: 'set-table-column-width',
  callback: args => onTableColumnWidthChange(args)
})

// ===== 计算属性 =====
const toolTip = computed(() => {
  return props.themes || 'dark'
})

const quotaData = computed(() => {
  let result = JSON.parse(JSON.stringify(state.quota))
  if (view.value?.type === 'table-info') {
    result = result?.filter(item => item.id !== '-1')
  }
  if (state.searchField) {
    result = result.filter(item =>
      item.name.toLowerCase().includes(state.searchField.toLowerCase())
    )
  }
  return result
})

const dimensionData = computed(() => {
  let result = JSON.parse(JSON.stringify(state.dimensionData))
  if (state.searchField) {
    result = result.filter(item =>
      item.name.toLowerCase().includes(state.searchField.toLowerCase())
    )
  }
  return result
})

const fieldDHeight = computed(() => {
  const h = y.value - 200
  if (h < 53) {
    return 53
  }
  return h > previewHeight.value - 50 ? previewHeight.value - 50 : h
})
const dragVerticalTop = computed(() => {
  const h = y.value - 200
  if (h < 50) {
    return 50
  }
  return h > previewHeight.value - 53 ? previewHeight.value - 53 : h
})

// ===== 工具函数 =====
const getIconName = (deType, extField, dimension = false) => {
  if (extField === 2) {
    const iconFieldCalculated = dimension ? iconFieldCalculatedMap : iconFieldCalculatedQMap
    return iconFieldCalculated[deType]
  }
  return iconFieldMap[fieldType[deType]]
}

const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}

const collapseChange = type => {
  canvasCollapse.value[type] = !canvasCollapse.value[type]
}

const recordSnapshotInfo = type => {
  view.value['dataFrom'] = 'calc'
  console.log(view.value.id, view.value.tableId, 'view.value.id')
  snapshotStore.recordSnapshotCache(type, view.value.id)
}

const changeDataset = () => {
  // change dataset, do clear field or other thing
  view.value['calParams'] = []
  recordSnapshotInfo('calcData')
}

const getFields = (id, chartId, type) => {
  if (id && chartId) {
    fieldLoading.value = true
    getFieldByDQ(id, chartId, { type: type })
      .then(res => {
        state.dimension = (res.dimensionList as unknown as Field[]) || []
        state.quota = (res.quotaList as unknown as Field[]) || []
        state.dimensionData = JSON.parse(JSON.stringify(state.dimension))
        state.quotaData = JSON.parse(JSON.stringify(state.quota))

        fieldLoading.value = false
        // emitter.emit('dataset-change')
      })
      .catch(() => {
        state.dimension = []
        state.quota = []
        state.dimensionData = []
        state.quotaData = []

        fieldLoading.value = false
      })
  } else {
    state.dimension = []
    state.quota = []
    state.dimensionData = []
    state.quotaData = []

    fieldLoading.value = false
  }
}

const handleChartFieldEdit = (item, type) => {
  return {
    type: type,
    item: item
  }
}

const initAddValueTableId = (tableId) => {
  if (!view.value['tableId']) {
    view.value['tableId'] = tableId
  }
}

// ===== 拖拽相关方法 =====
const isCtrl = ref(false)
const isDrag = ref(false)
const isDraggingItem = ref(false)

const setActive = (ele, type = 'dimension') => {
  if (isCtrl.value) {
    isCtrl.value = false
  }
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  activeChild.value = activeChild.value.some(item => item.id === ele.id) ? [] : [ele]
}
const dragStartD = () => {
  isDrag.value = true
  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}
const setActiveCtrl = (ele, type = 'dimension') => {
  isCtrl.value = true
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  const index = activeChild.value.findIndex(item => item.id === ele.id)
  if (index !== -1) {
    activeChild.value.splice(index, 1)
    return
  }
  activeChild.value.push(ele)
}
const setActiveShift = (ele, type = 'dimension') => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  const dataArr = type === 'dimension' ? dimensionData : quotaData
  deactivateChild.value = []
  const dimensionDataId = dataArr.value.map(ele => ele.id)
  const dimensionDataActiveChild = activeChild.value.filter(ele => dimensionDataId.includes(ele.id))
  if (!dimensionDataActiveChild.length) {
    const index = activeChild.value.findIndex(item => item.id === ele.id)
    if (index !== -1) {
      activeChild.value.splice(index, 1)
      return
    }
    activeChild.value.push(ele)
  } else {
    const startItx = dataArr.value.findIndex(
      item => item.id === dimensionDataActiveChild[dimensionDataActiveChild.length - 1].id
    )
    const endItx = dataArr.value.findIndex(item => item.id === ele.id)
    if (startItx === endItx) return
    if (startItx > endItx) {
      activeChild.value = [...activeChild.value, ...dataArr.value.slice(endItx, startItx)]
    }
    if (startItx < endItx) {
      activeChild.value = [...activeChild.value, ...dataArr.value.slice(startItx + 1, endItx + 1)]
    }
  }
}
const singleDragStartD = (e: DragEvent, ele, type) => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  if (!activeChild.value.length) {
    activeChild.value = [unref(ele)]
  }
  startToMove(e, unref(activeDimension.value))
}

const dragStart = () => {
  isDrag.value = true
  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}

const singleDragStart = (e: DragEvent, ele, type) => {
  const activeChild = type === 'dimension' ? activeDimension : activeQuota
  const deactivateChild = type === 'quota' ? activeDimension : activeQuota
  deactivateChild.value = []
  if (!activeChild.value.length) {
    activeChild.value = [ele]
  }
  e.dataTransfer.setData(
    'quota',
    JSON.stringify(
      activeQuota.value
        .filter(ele => ele.id)
        .map(ele => ({ ...cloneDeep(unref(ele)), datasetId: view.value.tableId }))
    )
  )
}

const dragEnd = () => {
  isDrag.value = false
  isDraggingItem.value = false
}

const singleDragEnd = () => {
  activeDimension.value = []
  activeQuota.value = []
  dragEnd()
}

const dragEnter = (ev: MouseEvent) => {
  ev.preventDefault()
}

const dragOver = (ev: MouseEvent) => {
  ev.preventDefault()
}

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


watch(
  [() => view.value['tableId']],
  () => {
    nextTick(() => {
      getFields(props.view.tableId, props.view.id, props.view.type)
      const nodeId = view.value['tableId']
      const node = datasetSelector?.value?.getNode(nodeId)
      if (node?.data) {
        curDatasetWeight.value = node.data.weight
      }
    })
  },
  { deep: true, immediate: true }
)

// ===== 生命周期钩子 =====
onMounted(() => {
  calculateElDragHeight()
  // 监听窗口大小变化
  window.addEventListener('resize', calculateElDragHeight)
})

onBeforeUnmount(() => {
  // 清理事件监听器
  window.removeEventListener('resize', calculateElDragHeight)
})
</script>

<template>
  <div class="left-sidebar de-chart-editor" :class="{ collapsed: canvasCollapse.datasetAreaCollapse }">
    <!-- 数据集区域头部 -->
    <div class="sidebar-header">
      <span :class="{'collapsed-header': canvasCollapse.datasetAreaCollapse}">{{ t('visualization.dataset') }}</span>
      <ToggleButton :collapsed="canvasCollapse.datasetAreaCollapse" @toggle="collapseChange('datasetAreaCollapse')" />
    </div>
    <!-- ===== 数据集选择和字段区域 ===== -->
    <el-main class="dataset-main-top" v-if="!canvasCollapse.datasetAreaCollapse">
      <div class="dataset-header">
        <!-- 数据集选择器 -->
        <el-row class="dataset-select">
          <dataset-select
            ref="datasetSelector"
            v-model="view.tableId"
            style="flex: 1"
            :view-id="view.id"
            :state-obj="state"
            :themes="themes"
            :disabled="false"
            @init-add-value-table-id="initAddValueTableId"
            @on-dataset-change="changeDataset"
          />
        </el-row>

        <!-- 字段搜索区域 -->
        <div class="dataset-search-wrapper">
          <div class="search-box-container">
            <el-input
              v-model="state.searchField"
              size="small"
              class="search-input-field"
              :class="{ dark: themes === 'dark' }"
              :placeholder="'字段'+t('chart.search')"
              clearable
            >
            </el-input>
            <el-button class="filter-action-btn" :class="{ dark: themes === 'dark' }" size="small" type="default">
              <el-icon class="filter-btn-icon">
                <Icon name="icon_filter_outlined">
                  <svg viewBox="0 0 1024 1024" class="svg-icon">
                    <path d="M349 838c0 17.7 14.2 32 31.8 32h262.4c17.6 0 31.8-14.3 31.8-32V642H349v196zm531.1-684H143.9c-24.5 0-39.8 26.7-27.5 48l221.3 376h348.8l221.2-376c12.1-21.3-3.2-48-27.6-48z"/>
                  </svg>
                </Icon>
              </el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <!-- 维度和指标区域 -->
      <div ref="elDrag" v-loading="fieldLoading" class="field-sections-container" :style="{ height: elDragHeight }">
        <!-- 维度区域 -->
        <div class="dimension-section">
          <div class="section-header">{{ t('chart.dimension') }}</div>
          <div class="field-list-container">
            <div class="field-list">
              <div
                v-for="element in dimensionData"
                :key="element.id"
                :draggable="true"
                class="field-item dimension-item"
                :class="{ 'field-item-dark': themes === 'dark' }"
                @click.ctrl="setActiveCtrl(element)"
                @click.meta="setActiveCtrl(element)"
                @click.exact="setActive(element)"
                @click.shift="setActiveShift(element)"
                @dragstart="$event => singleDragStartD($event, element, 'dimension')"
                @dragend="singleDragEnd"
              >
                <div class="drag-item">
                  <div
                    class="items flex-align-center"
                    :class="[
                      'item-dimension--' + themes,
                      isDraggingItem && 'is-dragging-item',
                      activeDimension.map(itx => itx.id).includes(element.id) && 'active'
                    ]"
                    :draggable="true"
                    @dragstart="dragStartD"
                    @dragend="dragEnd">
                    <el-icon class="field-icon dimension-icon">
                      <Icon>
                        <component
                          class="svg-icon"
                          :class="`field-icon-${fieldType[[2, 3].includes(element.deType) ? 2 : 0]}`"
                          :is="getIconName(element.deType, element.extField)"
                        ></component>
                      </Icon>
                    </el-icon>
                    <span class="field-name" :title="element.name">{{ element.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      
        <!-- 指标区域 -->
        <div class="metric-section">
          <div class="section-header">{{ t('chart.quota') }}</div>
          <div class="field-list-container">
            <div class="field-list">
              <div
                v-for="element in quotaData"
                :key="element.id"
                :draggable="true"
                class="field-item metric-item"
                :class="{ 'field-item-dark': themes === 'dark' }"
                @click.ctrl="setActiveCtrl(element, 'quota')"
                @click.meta="setActiveCtrl(element, 'quota')"
                @click.exact="setActive(element, 'quota')"
                @click.shift="setActiveShift(element, 'quota')"
                @dragstart="$event => singleDragStart($event, element, 'quota')"
                @dragend="singleDragEnd"
              >
                <div class="drag-item">
                  <div
                    class="items flex-align-center"
                    :class="[
                      'item-dimension--' + themes,
                      isDraggingItem && 'is-dragging-item',
                      activeQuota.map(itx => itx.id).includes(element.id) && 'active'
                    ]">
                    <el-icon class="field-icon metric-icon">
                      <Icon>
                        <component
                          class="svg-icon"
                          :class="`field-icon-${fieldType[element.deType]}`"
                          :is="getIconName(element.deType, element.extField, true)"
                        ></component>
                      </Icon>
                    </el-icon>
                    <span class="field-name" :title="element.name">{{ element.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-space"></div>
      </div>
    </el-main>
  </div>
</template>

<style lang="less" scoped>
.left-sidebar{
  width: 200px;
  height: 100%;
  border-right: 1px solid #f9f9f9;
  background-color: #ffffff;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;

  &.collapsed {
    width: 50px;
  }
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #fafafa;
  font-size: 13px;
  min-height: 50px;
  position: relative;
}
.dataset-main-top{
  padding: 0;
  flex: 1;
  overflow: hidden;
  .dataset-header{
    padding: 10px 10px 0 10px;
  }
  .field-sections-container{
    padding: 0 10px;
    overflow: auto;
  }
}
.de-chart-editor {
  // 搜索区域样式 - 完全按照截图设计
  .dataset-search-wrapper {
    padding: 8px 0;
    background: transparent;
  }
  .search-box-container {
    display: flex;
    align-items: center;
    gap: 6px;
    width: 100%;
  }
  .search-input-field {
    flex: 1;
    min-width: 0;
    :deep(.ed-input__wrapper) {
      background-color: #ffffff;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      box-shadow: none !important;
      padding: 0;
      height: 28px;
      transition: all 0.2s ease;

      &:hover {
        border-color: #40a9ff;
      }

      &:focus-within {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
      }
    }

    :deep(.ed-input__inner) {
      background-color: transparent;
      color: #1f2329;
      border: none;
      padding: 4px 8px;
      font-size: 12px;
      line-height: 20px;
      height: 26px;

      &::placeholder {
        color: #bfbfbf;
        font-size: 12px;
      }
    }
  }
  .filter-action-btn {
    min-width: 28px;
    width: 28px;
    height: 28px;
    padding: 0;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    background-color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;

    .filter-btn-icon {
      color: #8f959e;
      font-size: 14px;
    }

    &:hover {
      border-color: #40a9ff;
      background-color: #f0f8ff;

      .filter-btn-icon {
        color: #1890ff;
      }
    }

    &:active {
      border-color: #1890ff;
      background-color: #e6f7ff;
    }

    &.dark {
      background-color: #2a2d33;
      border-color: #3d4043;

      .filter-btn-icon {
        color: #8f959e;
      }

      &:hover {
        border-color: #40a9ff;
        background-color: #1e2329;

        .filter-btn-icon {
          color: #1890ff;
        }
      }

      &:active {
        border-color: #1890ff;
        background-color: #0f1419;
      }
    }
  }
}
.field-sections-container{
  font-size: 14px;
  .section-header{
    font-weight: 500;
    color: #2e2f32;
    height: 28px;
    line-height: 28px;
    user-select: none;
  }
  .field-name{
    display: inline-block;
    width: 90px;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-left: 4px;
    font-weight: 400;
    color: #646a73
  }
   .items {
      width: 100%;
      height: 28px;
      border-radius: 4px;
      border: 1px solid transparent;
      color: #a6a6a6;
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
      padding: 4px 10px;
      position: relative;
      cursor: pointer;
      &:hover {
        background: #1f23291a;
      }

      &.item-dimension--dark:hover {
        background: #ebebeb1a;
      }

      .ed-icon {
        font-size: 14px;
      }
      &.active {
        border-color: var(--ed-color-primary, #3370ff);
      }
    }
}
.empty-space{
  height: 20px;
}
/* 针对 el-scrollbar 的垂直滚动条宽度设置 */
::-webkit-scrollbar {
  width: 6px!important; /* 垂直滚动条宽度（根据需要调整） */
}
</style>
