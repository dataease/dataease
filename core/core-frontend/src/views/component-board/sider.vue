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
const { t } = useI18n()
import { storeToRefs } from 'pinia'
import { iconChartMap } from '@/components/icon-group/chart-list'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { ElMessage, ElTreeSelect } from 'element-plus-secondary'
import { useRouter, useRoute } from 'vue-router_2'
import { useDraggable } from '@vueuse/core'
import DatasetSelect from '@/views/chart/components/editor/dataset-select/DatasetSelect.vue'

import { useAppStoreWithOut } from '@/store/modules/app'
import { useI18n } from '@/hooks/web/useI18n'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useEmbedded } from '@/store/modules/embedded'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'

import { Field, getFieldByDQ, copyChartField, deleteChartField } from '@/api/chart'

import { BASE_VIEW_CONFIG, getViewConfig } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'


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
const toolTip = computed(() => {
  return props.themes || 'dark'
})
const snapshotStore = snapshotStoreWithOut()
const { wsCache } = useCache('localStorage')
const router = useRouter()
const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()
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
import {
  iconFieldCalculatedMap,
  iconFieldCalculatedQMap
} from '@/components/icon-group/field-calculated-list'
import { fieldType } from '@/utils/attr'
const getIconName = (deType, extField, dimension = false) => {
  if (extField === 2) {
    const iconFieldCalculated = dimension ? iconFieldCalculatedMap : iconFieldCalculatedQMap
    return iconFieldCalculated[deType]
  }
  return iconFieldMap[fieldType[deType]]
}
const { view } = toRefs(props)
const dvMainStore = dvMainStoreWithOut()
const {
  canvasCollapse,
  curComponent,
  componentData,
  editMode,
  mobileInPc,
  fullscreenFlag,
  dvInfo
} = storeToRefs(dvMainStore)

const el = ref<HTMLElement | null>(null)
const elDrag = ref<HTMLElement | null>(null)
const { y, isDragging } = useDraggable(el, {
  initialValue: { x: 0, y: 400 },
  draggingElement: elDrag
})
const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}
const addDsWindow = () => {
  if (!dvInfo.value.id) {
    ElMessage.warning(t('visualization.save_page_tips'))
    return
  }
  const path =
    embeddedStore.getToken && appStore.getIsIframe ? 'dataset-embedded-form' : '/dataset-form'
  let routeData = router.resolve(path)
  const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
  const newWindow = window.open(routeData.href, openType)
  initOpenHandler(newWindow)
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

const previewHeight = ref(0)
const fieldDHeight = computed(() => {
  const h = y.value - 200
  if (h < 53) {
    return 53
  }
  return h > previewHeight.value - 50 ? previewHeight.value - 50 : h
})
const fieldLoading = ref(false)
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
        emitter.emit('dataset-change')
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

// 拖动相关
const activeQuota = ref<Axis[]>([])
const isDrag = ref(false)
const isDraggingItem = ref(false)
const dragStartD = () => {
  isDrag.value = true
  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}
const handleChartFieldEdit = (item, type) => {
  return {
    type: type,
    item: item
  }
}

watch(
  [() => view.value['tableId']],
  () => {
    nextTick(() => {
      if ('picture-group' === props.view.type) {
        return
      }
      getFields(props.view.tableId, props.view.id, props.view.type)
      const nodeId = view.value['tableId']
      if (!!nodeId) {
        cacheId = nodeId as unknown as string
      }
      const node = datasetSelector?.value?.getNode(nodeId)
      if (node?.data) {
        curDatasetWeight.value = node.data.weight
      }
    })
  },
  { deep: true, immediate: true }
)
// const singleDragStartD = (e: DragEvent, ele, type) => {
//   const activeChild = type === 'dimension' ? activeDimension : activeQuota
//   const deactivateChild = type === 'quota' ? activeDimension : activeQuota
//   deactivateChild.value = []
//   if (!activeChild.value.length) {
//     activeChild.value = [unref(ele)]
//   }
//   startToMove(e, unref(activeDimension.value))
// }

// const dragStart = () => {
//   isDrag.value = true
//   setTimeout(() => {
//     isDraggingItem.value = true
//   }, 0)
// }

// const singleDragStart = (e: DragEvent, ele, type) => {
//   const activeChild = type === 'dimension' ? activeDimension : activeQuota
//   const deactivateChild = type === 'quota' ? activeDimension : activeQuota
//   deactivateChild.value = []
//   if (!activeChild.value.length) {
//     activeChild.value = [ele]
//   }
//   e.dataTransfer.setData(
//     'quota',
//     JSON.stringify(
//       activeQuota.value
//         .filter(ele => ele.id)
//         .map(ele => ({ ...cloneDeep(unref(ele)), datasetId: view.value.tableId }))
//     )
//   )
// }

// const dragEnd = () => {
//   isDrag.value = false
//   isDraggingItem.value = false
// }

// const singleDragEnd = () => {
//   activeDimension.value = []
//   activeQuota.value = []
//   dragEnd()
// }

// const dragEnter = (ev: MouseEvent) => {
//   ev.preventDefault()
// }

// const dragOver = (ev: MouseEvent) => {
//   ev.preventDefault()
// }

// const drop = (ev: MouseEvent, type = 'xAxis') => {
//   ev.preventDefault()
//   const arr = activeDimension.value.length ? activeDimension.value : activeQuota.value
//   for (let i = 0; i < arr.length; i++) {
//     const obj = cloneDeep(arr[i])
//     state.moveId = obj.id as unknown as number
//     view.value[type] ??= []
//     view.value[type].push(obj)
//     const e = { newDraggableIndex: view.value[type].length - 1 }

//     if ('drillFields' === type) {
//       addDrill(e)
//     } else {
//       addAxis(e, type as AxisType)
//     }
//   }
// }
</script>

<template>
  <div class="sidebar-container">
    <div class="left-sidebar de-chart-editor" :class="{ collapsed: canvasCollapse.datasetAreaCollapse }">
      <div class="sidebar-header">
        <span :class="{'collapsed-header': canvasCollapse.datasetAreaCollapse}">{{ t('visualization.dataset') }}</span>
        <button class="toggle-btn" @click="collapseChange('datasetAreaCollapse')" :title="canvasCollapse.datasetAreaCollapse ? '展开' : '收起'">
          <svg class="toggle-icon" :class="{ rotated: canvasCollapse.datasetAreaCollapse }" viewBox="0 0 24 24"  fill="none" stroke="currentColor">
            <polyline points="15,18 9,12 15,6"></polyline>
          </svg>
        </button>
      </div>

      <!-- 数据集选择区域 -->
      <el-main class="dataset-main-top" v-if="!canvasCollapse.datasetAreaCollapse">
        <el-row class="dataset-select">
          <DatasetSelect
            ref="datasetSelector"
            v-model="view.tableId"
            style="flex: 1"
            :view-id="view.id"
            :state-obj="state"
            :themes="themes"
            :disabled="false"
            @on-dataset-change="changeDataset"
          />
        </el-row>
        <el-row class="dataset-search padding-lr">
          <div class="dataset-search-label" :class="{ dark: themes === 'dark' }">
            <span>{{ t('chart.field') }}</span>
            <span>
              <el-tooltip
                :effect="toolTip"
                :content="$t('visualization.refresh')"
                placement="top"
              >
                <el-icon
                  class="field-search-icon-btn"
                  :class="{ dark: themes === 'dark' }"
                  @click="getFields(view.tableId, view.id, view.type)"
                >
                  <Icon name="icon_refresh_outlined" class="el-icon-arrow-down el-icon-delete"
                    ><icon_refresh_outlined
                      class="svg-icon el-icon-arrow-down el-icon-delete"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </span>
          </div>
          <el-input
            v-model="state.searchField"
            size="default"
            :effect="themes"
            class="dataset-search-input"
            :class="{ dark: themes === 'dark' }"
            :placeholder="t('chart.search') + ' ' + t('chart.field')"
            clearable
          >
            <template #prefix>
              <el-icon class="el-input__icon">
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
        </el-row>
        <div
          ref="elDrag"
          v-loading="fieldLoading && !fullscreenFlag"
          style="height: calc(100% - 137px); min-height: 120px"
        >
          <div
            class="padding-lr field-height first right-dimension"
            :class="{ dark: themes === 'dark', 'user-select': isDragging }"
            :style="{
              height: fieldDHeight + 'px'
            }"
          >
            <div style="margin-top: 12px" class="label-top">
              {{ t('chart.dimension') }}
            </div>
            <el-scrollbar class="drag-list">
              <div
                v-for="element in dimensionData"
                :key="element.id"
                :draggable="true"
                class="item father"
              >
                <div
                  class="items flex-align-center"
                  :class="[
                    'item-dimension--' + themes,
                    isDraggingItem && 'is-dragging-item',
                  ]"
                >
                  <el-icon>
                    <Icon
                      ><component
                        class="svg-icon"
                        :class="`field-icon-${
                          fieldType[[2, 3].includes(element.deType) ? 2 : 0]
                        }`"
                        :is="getIconName(element.deType, element.extField)"
                      ></component
                    ></Icon>
                  </el-icon>
                  <span
                    class="field-name ellipsis"
                    :class="{ dark: themes === 'dark' }"
                    :title="element.name"
                    >{{ element.name }}</span
                  >
                  <el-dropdown
                    v-if="element.id !== '-1' && false"
                    :effect="props.themes"
                    placement="right-start"
                    trigger="click"
                    size="small"
                    class="field-setting child"
                    @command="chartFieldEdit"
                  >
                    <span class="el-dropdown-link">
                      <el-icon class="icon-setting"><Setting /></el-icon>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu :effect="props.themes">
                        <el-dropdown-item :command="handleChartFieldEdit(element, 'copy')">
                          {{ t('common.copy') }}
                        </el-dropdown-item>
                        <span v-if="element.chartId">
                          <el-dropdown-item :command="handleChartFieldEdit(element, 'edit')">
                            {{ t('common.edit') }}
                          </el-dropdown-item>
                          <el-dropdown-item
                            :command="handleChartFieldEdit(element, 'delete')"
                          >
                            {{ t('common.delete') }}
                          </el-dropdown-item>
                        </span>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <div
                  v-if="activeDimension.map(itx => itx.id).includes(element.id)"
                  :draggable="true"
                  class="shadow"
                  :class="isDraggingItem && 'is-dragging'"
                  @dragstart="dragStartD"
                  @dragend="dragEnd"
                >
                  <template v-if="isDrag">
                    <div
                      v-for="ele in activeDimension"
                      :key="ele.id"
                      class="items flex-align-center"
                    >
                      <el-icon>
                        <Icon
                          ><component
                            class="svg-icon"
                            :class="`field-icon-${
                              fieldType[[2, 3].includes(ele.deType) ? 2 : 0]
                            }`"
                            :is="iconFieldMap[fieldType[ele.deType]]"
                          ></component
                        ></Icon>
                      </el-icon>
                      <span
                        class="field-name ellipsis"
                        :class="{ dark: themes === 'dark' }"
                        >{{ ele.name }}</span
                      >
                      <el-dropdown
                        v-if="ele.id !== '-1' && false"
                        :effect="props.themes"
                        placement="right-start"
                        trigger="click"
                        size="small"
                        class="field-setting child"
                        @command="chartFieldEdit"
                      >
                        <span class="el-dropdown-link">
                          <el-icon class="icon-setting"><Setting /></el-icon>
                        </span>
                        <template #dropdown>
                          <el-dropdown-menu :effect="props.themes">
                            <el-dropdown-item :command="handleChartFieldEdit(ele, 'copy')">
                              {{ t('common.copy') }}
                            </el-dropdown-item>
                            <span v-if="ele.chartId">
                              <el-dropdown-item :command="handleChartFieldEdit(ele, 'edit')">
                                {{ t('common.edit') }}
                              </el-dropdown-item>
                              <el-dropdown-item
                                :command="handleChartFieldEdit(ele, 'delete')"
                              >
                                {{ t('common.delete') }}
                              </el-dropdown-item>
                            </span>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </template>
                </div>
              </div>
            </el-scrollbar>
            <div
              ref="el"
              :style="{
                top: dragVerticalTop + 'px'
              }"
              :class="['drag-vertical', isDragging && 'is-hovering']"
            ></div>
          </div>
          <div
            class="padding-lr field-height right-dimension"
            :class="{ dark: themes === 'dark' }"
          >
            <div class="divider"></div>
            <div style="margin-top: 8px" class="label-top">{{ t('chart.quota') }}</div>
            <el-scrollbar class="drag-list">
              <div
                v-for="element in quotaData"
                :key="element.id"
                class="item father"
                :draggable="true"
              >
                <div
                  class="items flex-align-center"
                  :class="[
                    'item-dimension--' + themes,
                    isDraggingItem && 'is-dragging-item',
                  ]"
                >
                  <el-icon>
                    <Icon :class-name="`field-icon-${fieldType[element.deType]}`"
                      ><component
                        class="svg-icon"
                        :class="`field-icon-${fieldType[element.deType]}`"
                        :is="getIconName(element.deType, element.extField, true)"
                      ></component
                    ></Icon>
                  </el-icon>
                  <span
                    class="field-name ellipsis"
                    :class="{ dark: themes === 'dark' }"
                    :title="element.name"
                    >{{ element.name }}</span
                  >
                </div>
                <div
                  v-if="activeQuota.map(itx => itx.id).includes(element.id)"
                  :draggable="true"
                  class="shadow"
                  :class="isDraggingItem && 'is-dragging'"
                >
                  <template v-if="isDrag">
                    <div
                      v-for="ele in activeQuota"
                      :key="ele.id"
                      class="items flex-align-center"
                    >
                      <el-icon>
                        <Icon :class-name="`field-icon-${fieldType[ele.deType]}`"
                          ><component
                            class="svg-icon"
                            :class-name="`field-icon-${fieldType[ele.deType]}`"
                            :is="iconFieldMap[fieldType[ele.deType]]"
                          ></component
                        ></Icon>
                      </el-icon>
                      <span
                        class="field-name ellipsis"
                        :class="{ dark: themes === 'dark' }"
                        >{{ ele.name }}</span
                      >
                    </div>
                  </template>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </div>
      </el-main>
    </div>

    <!-- 第二个左侧边栏 -->
    <div class="left-sidebar" :class="{ collapsed: canvasCollapse.chartAreaCollapse }">
      <div class="sidebar-header">
        <span :class="{'collapsed-header': canvasCollapse.chartAreaCollapse}">图表</span>
        <button class="toggle-btn" @click="collapseChange('chartAreaCollapse')" :title="canvasCollapse.chartAreaCollapse ? '展开' : '收起'">
          <svg class="toggle-icon" :class="{ rotated: canvasCollapse.chartAreaCollapse }" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <polyline points="15,18 9,12 15,6"></polyline>
          </svg>
        </button>
      </div>
      <div class="sidebar-content" v-if="!canvasCollapse.chartAreaCollapse">
       内容
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.sidebar-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.left-sidebar{
  width: 200px;
  height: 100%;
  border-right: 1px solid #e0e0e0;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;

  overflow-y: hidden;
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
  min-height: 50px;
}

.toggle-btn {
  position: absolute;
  top: 15px;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  cursor: pointer;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
  right: -9px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

  &:hover {
    background-color: #e6f7ff;
    border-color: #1890ff;
    color: #1890ff;
    box-shadow: 0 2px 6px rgba(24, 144, 255, 0.2);
  }

  &:active {
    transform: scale(0.95);
  }
}


.toggle-icon {
  width: 12px;
  height: 12px;
  transition: transform 0.3s ease;

  &.rotated {
    transform: rotate(180deg);
  }
}

.de-chart-editor {
  .padding-lr {
    padding: 0 16px;

    &.no-top-border {
      border-top: none !important;
    }
    &.no-top-padding {
      padding-top: 0 !important;

      :deep(.drag-placeholder-style) {
        top: calc(50% - 8px);
      }
    }
  }
  .view-title-name {
    display: -moz-inline-box;
    display: inline-block;
    width: 130px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
    margin-left: 38px;
  }

  .view-panel-row {
    overflow-y: auto;
    overflow-x: hidden;
    height: 100%;

    :deep(.ed-collapse-item__content) {
      padding: 16px 10px 0;
    }
  }

  .tab-header {
    --ed-tabs-header-height: 34px;
    --custom-tab-color: #646a73;

    :deep(.ed-tabs__nav-wrap::after) {
      background-color: unset;
    }

    &.dark {
      --custom-tab-color: #a6a6a6;
    }

    height: 100%;
    :deep(.ed-tabs__header) {
      border-top: solid 1px @side-outline-border-color;
    }
    :deep(.ed-tabs__item) {
      font-weight: 400;
      font-size: 12px;
      padding: 0 8px !important;
      margin-right: 12px;
    }

    :deep(.ed-tabs__item:not(.is-active)) {
      color: var(--custom-tab-color);
    }

    :deep(.ed-tabs__item.is-active) {
      font-weight: 500;
    }

    :deep(.ed-tabs__nav-scroll) {
      padding-left: 0 !important;
    }

    :deep(.ed-tabs__header) {
      margin: 0 !important;
    }

    :deep(.ed-tabs__content) {
      height: calc(100% - 35px);
      overflow-y: auto;
      overflow-x: hidden;
    }
  }

  .field-height {
    height: 50%;

    .label-top {
      color: #646a73;
      font-size: 12px;
      font-style: normal;
      font-weight: 500;
      line-height: 20px;
    }

    &.first {
      border-top: none !important;
      position: relative;
    }

    .drag-vertical {
      width: 100%;
      height: 4px;
      position: absolute;
      left: 0;
      cursor: row-resize;

      &.is-hovering::after,
      &:hover::after {
        width: calc(100% - 32px);
        height: 1px;
        content: '';
        position: absolute;
        left: 16px;
        top: 0;
        background: var(--ed-color-primary, #3370ff);
      }
    }

    &.dark {
      label {
        color: #a6a6a6;
      }
    }

    .divider {
      width: 100%;
      height: 1px;
      padding: 0 16px;
      background-color: rgba(255, 255, 255, 0.15);
    }
  }

  .drag-list {
    height: calc(100% - 26px);
    min-height: 24px;
    //overflow: auto;
    padding: 2px 0;
  }

  .item-dimension {
    padding: 4px 10px;
    margin: 0 2px;
    text-align: left;
    color: #606266;
    display: block;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    position: relative;
    border-radius: 4px;
    border: 1px solid transparent;

    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    vertical-align: middle;

    height: 28px;

    cursor: pointer;

    &:hover {
      background: rgba(31, 35, 41, 0.1);
    }

    &.item-dimension--dark {
      &:hover {
        background: rgba(235, 235, 235, 0.1);
      }
    }

    &.sortable-chosen {
      border: 1px solid var(--ed-color-primary, #3370ff);
      background: #fff;

      &:hover {
        background: #fff;
      }

      &.item-dimension--dark {
        background: #1a1a1a;
        &:hover {
          background: #1a1a1a;
        }
      }
    }
  }

  .father .child {
    visibility: hidden;
  }

  .father:hover .child {
    visibility: visible;
  }

  .field-name {
    display: inline-block;
    width: 90px;
    word-break: break-all;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding-left: 4px;
    font-weight: 400;
    color: #646a73;

    &.dark {
      color: #a6a6a6;
    }
  }

  .padding-tab {
    padding: 0;
    height: 100%;
    width: 100%;
    display: flex;

    :deep(.ed-scrollbar) {
      &.has-footer {
        height: calc(100% - 81px);
      }
    }

    :deep(.ed-footer) {
      padding: 0;
      height: 114px;
    }
  }

  .result-count {
    width: 60px;

    :deep(.ed-input__wrapper) {
      padding: 1px 2px;
    }
  }

  .result-count :deep(input) {
    padding: 0 4px;
  }

  .data-area-label {
    text-align: left;
    position: relative;
    width: 100%;
    display: flex;
    flex-direction: row;
    align-items: center;
    :deep(.required::after) {
      content: '*';
      color: var(--ed-color-danger);
      margin-left: 4px;
      font-family: var(--de-custom_font, 'PingFang');
      font-style: normal;
      font-weight: 400;
    }
  }

  .form-draggable-title {
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    justify-content: space-between;

    span {
      cursor: default;
    }

    :deep(.required::after) {
      content: '*';
      color: var(--ed-color-danger);
      margin-left: 2px;
      font-family: var(--de-custom_font, 'PingFang');
      font-style: normal;
      font-weight: 400;
    }

    .remove-icon {
      color: #646a73;
      cursor: pointer;
      margin-top: 2px;
      margin-right: 2px;

      &.remove-icon--dark {
        color: #a6a6a6;
      }

      .inner-class {
        font-size: 14px;
      }
    }
  }

  .drag-block-style {
    padding: 2px 0 0 0;
    width: 100%;
    min-height: 32px;
    border-radius: 4px;
    overflow-x: hidden;
    overflow-y: hidden;
    display: block;
    align-items: center;
    border: 1px dashed #bbbfc4;
    background-color: rgba(31, 35, 41, 0.05);
    margin-top: 8px;

    &.dark {
      border: 1px dashed #5f5f5f;
      background-color: rgba(235, 235, 235, 0.05);
    }

    &:has(span) {
      background-color: transparent !important;
    }
  }

  .draggable-group {
    display: block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .ed-input-refresh-time {
    width: calc(50% - 4px) !important;
  }

  .ed-input-refresh-unit {
    margin-left: 8px;
    width: calc(50% - 4px) !important;
  }

  .ed-input-refresh-loading {
    margin-left: 4px;
    font-size: 12px !important;
  }

  .drag-data {
    padding-top: 8px;
    padding-bottom: 16px;

    .tree-btn {
      width: 100%;
      margin-top: 8px;
      background: #fff;
      height: 28px;
      border-radius: 4px;
      border: 1px solid #dcdfe6;
      display: flex;
      color: #cccccc;
      align-items: center;
      cursor: pointer;
      justify-content: center;
      font-size: 12px;
      &.tree-btn--dark {
        background: rgba(235, 235, 235, 0.05);
        border-color: #5f5f5f;
      }

      &.active {
        color: #3370ff;
        border-color: #3370ff;
      }

      &.invalid {
        color: red !important;
        border-color: red !important;
      }
    }

    :deep(.tree-btn_secondary) {
      width: 100%;
      margin-top: 8px;
      line-height: 28px;
      height: 28px;
      font-size: 12px;

      & > [class*='ed-icon'] + span {
        margin-left: 2px !important;
      }
    }

    &.no-top-border {
      border-top: none !important;
    }
    &.no-top-padding {
      padding-top: 0 !important;
    }
    &:nth-child(n + 2) {
      border-top: 1px solid @side-outline-border-color;
    }
    &:first-child {
      border-top: none !important;
    }
  }

  .editor-title {
    color: @dv-canvas-main-font-color;
    font-weight: 500;
    height: @component-toolbar-height;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 8px;
    line-height: 22px;

    span {
      width: calc(100% - 24px);
      overflow-x: hidden;
      text-overflow: ellipsis;
      word-break: break-all;
      white-space: nowrap;
    }
  }

  .ed-tabs {
    --el-tabs-header-height: 38px !important;
  }

  .switch-chart {
    display: flex;
    align-items: center;
    height: 100%;
    justify-content: space-between;
  }

  .dataset-selector :deep(.ed-input__inner) {
    height: 24px;
    width: 110px;
  }

  .result-style {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-top: 1px solid @side-outline-border-color;
  }
  .result-style-dark {
    :deep(.ed-button) {
      color: #ffffff;
      background-color: var(--ed-color-primary, #3370ff);
      border: none;
      border-radius: 0;
    }
    :deep(.ed-button:hover) {
      background-color: var(--ed-color-primary-light-3);
    }
    :deep(.ed-button:active) {
      background-color: var(--ed-color-primary);
    }
  }
  .result-style-input {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 40px;
    padding: 0 6px;

    .margin20-radio {
      margin-right: 20px;
    }

    .result-count-label {
      color: #1f2329;
      font-size: 12px;
      font-weight: 400;

      &.dark {
        color: #fff;
      }
    }
  }
  .result-style-button {
    height: 40px;
    width: 100%;
    border-radius: 0;
  }

  .switch-chart-dark {
    :deep(.ed-button) {
      color: #ffffff;
      background-color: #1a1a1a;
      border: 1px solid hsla(0, 0%, 100%, 0.15);
      border-radius: 2px;
    }
    :deep(.ed-button:hover) {
      border: 1px solid var(--ed-color-primary, #3370ff);
    }
  }

  .dataset-search {
    height: 51px;
    width: 100%;
  }
  .dataset-search-label {
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1f2329;
    font-weight: 500;

    &.dark {
      color: #ebebeb;
    }
  }
  .field-search-icon-btn {
    font-size: 16px;
    color: #646a73;
    cursor: pointer;
    position: relative;
    &:hover {
      &::after {
        content: '';
        position: absolute;
        width: 24px;
        height: 24px;
        border-radius: 4px;
        top: -4px;
        left: -4px;
        background: rgba(31, 35, 41, 0.1);
      }
    }

    &.dark {
      color: #a6a6a6;
      &:hover {
        &::after {
          background: rgba(235, 235, 235, 0.1);
        }
      }
    }
  }

  .dataset-search-input {
    font-size: 12px;

    :deep(.ed-input__inner) {
      background-color: @side-area-background-light;
      color: @canvas-main-font-color-light;
    }
    :deep(.ed-input__wrapper) {
      box-shadow: none !important;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      background-color: @side-area-background-light;
      border-radius: 0;
      padding: 1px 4px;
    }

    &.dark {
      :deep(.ed-input__inner) {
        background-color: @side-area-background;
        color: #ffffff;
      }
      :deep(.ed-input__wrapper) {
        border-bottom: 1px solid hsla(0, 0%, 100%, 0.15);
        background-color: @side-area-background;
      }
    }
  }
}
</style>
