<template>
  <div
    class="bar-main"
    v-if="!mobileInPc"
    :class="[
      showEditPosition,
      {
        'bar-main-background': mainBackgroundShow
      }
    ]"
    @mousedown="fieldsAreaDown"
  >
    <el-tooltip
      effect="dark"
      placement="top"
      :content="'排序'"
      v-if="element.component === 'DeTabs' && showPosition === 'canvas'"
    >
      <el-icon class="bar-base-icon" @click="tabSort">
        <Sort />
      </el-icon>
    </el-tooltip>
    <template v-if="element.component === 'VQuery' && showPosition === 'canvas'">
      <span title="添加查询条件">
        <el-icon class="bar-base-icon" @click="addQueryCriteria">
          <Icon name="icon_add_outlined"></Icon
        ></el-icon>
      </span>
      <span title="编辑查询条件">
        <el-icon class="bar-base-icon" @click="editQueryCriteria">
          <Icon name="icon_edit_outlined"></Icon
        ></el-icon>
      </span>
    </template>
    <el-tooltip
      effect="dark"
      placement="top"
      :content="t('visualization.enlarge')"
      v-if="element.innerType !== 'rich-text' && barShowCheck('enlarge')"
    >
      <span>
        <el-icon class="bar-base-icon" @click="userViewEnlargeOpen($event, 'enlarge')">
          <Icon name="dv-bar-enlarge" />
        </el-icon>
      </span>
    </el-tooltip>
    <el-tooltip
      effect="dark"
      :placement="showBarTooltipPosition"
      content="查看数据"
      v-if="element.innerType !== 'rich-text' && barShowCheck('details')"
    >
      <span>
        <el-icon class="bar-base-icon" @click="userViewEnlargeOpen($event, 'details')">
          <Icon name="dv-details" />
        </el-icon>
      </span>
    </el-tooltip>

    <div v-if="barShowCheck('multiplexing')" class="bar-checkbox-area">
      <el-checkbox
        @click.stop
        style="height: 26px; padding: 5px"
        v-model="state.multiplexingCheckModel"
        @change="multiplexingCheck"
      />
    </div>
    <span
      :title="t('visualization.cancel_linkage')"
      v-if="barShowCheck('unLinkage') && existLinkage"
    >
      <el-icon class="bar-base-icon" @click="clearLinkage">
        <Icon name="dv-bar-unLinkage" />
      </el-icon>
    </span>
    <div v-if="barShowCheck('batchOpt')" class="bar-checkbox-area">
      <el-checkbox v-model="state.batchOptCheckModel" @change="batchOptChange" />
    </div>

    <el-dropdown trigger="click" placement="right-start" v-if="barShowCheck('setting')">
      <el-icon class="bar-base-icon">
        <el-tooltip :content="t('visualization.more')" effect="dark" placement="bottom">
          <icon name="icon_more_outlined" />
        </el-tooltip>
      </el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 158px">
          <el-dropdown-item @click="copyComponent" v-if="barShowCheck('copy')"
            >复制</el-dropdown-item
          >
          <template v-if="element.innerType !== 'rich-text' && barShowCheck('enlarge')">
            <el-dropdown-item
              :divided="showPosition === 'canvas'"
              @click="userViewEnlargeOpen($event, 'enlarge')"
              >放大</el-dropdown-item
            >
            <el-dropdown-item
              @click="userViewEnlargeOpen($event, 'details')"
              v-if="element.innerType !== 'rich-text' && barShowCheck('details')"
              >查看数据</el-dropdown-item
            >
            <el-dropdown-item
              style="padding: 0"
              v-if="element.innerType !== 'rich-text' && barShowCheck('download')"
              @click.prevent
            >
              <el-dropdown style="width: 100%" trigger="hover" placement="right-start">
                <div
                  class="flex-align-center"
                  style="width: 100%; padding: 5px 6px 5px 16px; line-height: 24px"
                >
                  导出为
                  <el-icon size="16px" style="margin-left: auto"><ArrowRight /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu style="width: 120px">
                    <el-dropdown-item @click="exportAsExcel">Excel</el-dropdown-item>
                    <el-dropdown-item
                      v-if="element.innerType === 'table-pivot'"
                      :disabled="!enableFormattedExport"
                      @click="exportAsFormattedExcel"
                    >
                      <span :title="enableFormattedExport ? '' : '树形模式暂不支持导出'"
                        >Excel(带格式)</span
                      >
                    </el-dropdown-item>
                    <el-dropdown-item @click="exportAsImage">图片</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-dropdown-item>
          </template>
          <el-dropdown-item divided @click="deleteComponent" v-if="barShowCheck('delete')"
            >删除</el-dropdown-item
          >
        </el-dropdown-menu>
      </template>
    </el-dropdown>
    <el-dropdown
      trigger="click"
      placement="right-start"
      v-if="element.innerType !== 'rich-text' && barShowCheck('previewDownload') && authShow"
    >
      <el-icon @click="downloadClick" class="bar-base-icon">
        <el-tooltip :content="t('chart.export')" effect="dark" placement="bottom">
          <icon name="dv-preview-download" />
        </el-tooltip>
      </el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 118px">
          <el-dropdown-item @click="exportAsExcel">Excel</el-dropdown-item>
          <el-dropdown-item
            v-if="element.innerType === 'table-pivot'"
            :disabled="!enableFormattedExport"
            @click="exportAsFormattedExcel"
          >
            <span :title="enableFormattedExport ? '' : '树形模式暂不支持导出'">Excel(带格式)</span>
          </el-dropdown-item>
          <el-dropdown-item @click="exportAsImage">图片</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-popover v-if="selectFieldShow" width="200" trigger="click" @mousedown="fieldsAreaDown">
      <template #reference>
        <el-icon class="bar-base-icon"> <Icon name="database"></Icon></el-icon>
      </template>
      <fields-list :fields="state.curFields" :element="element" />
    </el-popover>
    <custom-tabs-sort ref="customTabsSortRef" :element="element"></custom-tabs-sort>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, onBeforeUnmount, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { exportExcelDownload } from '@/views/chart/components/js/util'
import FieldsList from '@/custom-component/rich-text/FieldsList.vue'
import { RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElTooltip, ElButton } from 'element-plus-secondary'
import CustomTabsSort from '@/custom-component/de-tabs/CustomTabsSort.vue'
import { exportPivotExcel } from '@/views/chart/components/js/panel/common/common_table'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
const customTabsSortRef = ref(null)
const authShow = computed(() => !dvInfo.value.weight || dvInfo.value.weight > 3)
const emits = defineEmits([
  'userViewEnlargeOpen',
  'closePreview',
  'showViewDetails',
  'amRemoveItem',
  'linkJumpSetOpen',
  'linkageSetOpen'
])
const { t } = useI18n()
const { emitter } = useEmitt()
// bar所在位置可以显示的功能按钮
const positionBarShow = {
  canvas: [
    'enlarge',
    'details',
    'setting',
    'copy',
    'delete',
    'download',
    'unLinkage',
    'linkageSetting',
    'linkJumpSetting'
  ],
  preview: ['enlarge', 'details', 'download', 'unLinkage', 'previewDownload'],
  multiplexing: ['multiplexing'],
  batchOpt: ['batchOpt'],
  linkage: ['linkage']
}

// bar所属组件类型可以显示的功能按钮
const componentTypeBarShow = {
  UserView: [
    'enlarge',
    'details',
    'setting',
    'copy',
    'download',
    'previewDownload',
    'delete',
    'multiplexing',
    'batchOpt',
    'linkage',
    'unLinkage',
    'linkageSetting',
    'linkJumpSetting'
  ],
  default: ['setting', 'delete', 'copy', 'multiplexing', 'batchOpt']
}

const barShowCheck = barName => {
  return (
    positionBarShow[showPosition.value] &&
    positionBarShow[showPosition.value].includes(barName) &&
    (componentTypeBarShow[element.value.component]
      ? componentTypeBarShow[element.value.component]
      : componentTypeBarShow['default']
    ).includes(barName)
  )
}

const mainBackgroundShow = computed(() => {
  return !['batchOpt', 'multiplexing'].includes(showPosition.value)
})
const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  active: {
    type: Boolean,
    required: false,
    default: false
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  },
  showPosition: {
    required: false,
    type: String,
    default: 'canvas'
  },
  canvasId: {
    type: String,
    default: 'canvas-main'
  }
})

const { element, index, showPosition, canvasId } = toRefs(props)
const {
  batchOptStatus,
  pcMatrixCount,
  curComponent,
  componentData,
  canvasViewInfo,
  mobileInPc,
  dvInfo
} = storeToRefs(dvMainStore)

const state = reactive({
  systemOS: 'Mac',
  maxImageSize: 15000000,
  boardSetVisible: false,
  linkJumpSetVisible: false,
  linkJumpSetViewId: null,
  curFields: [],
  multiplexingCheckModel: false,
  // Currently selected Multiplexing components
  curMultiplexingComponents: {},
  barWidth: 24,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom', 'custom-button'],
  timer: null,
  viewXArray: [],
  batchOptCheckModel: false
})

const tabSort = () => {
  customTabsSortRef.value.sortInit()
}

const downloadClick = () => {
  dvMainStore.setCurComponent({ component: element.value, index: index.value })
}

const addQueryCriteria = () => {
  emitter.emit(`addQueryCriteria${element.value.id}`)
}

const editQueryCriteria = () => {
  emitter.emit(`editQueryCriteria${element.value.id}`)
}

const showEditPosition = computed(() => {
  if (showPosition.value === 'canvas') {
    const baseLeft = element.value.x - 1
    const baseRight = pcMatrixCount.value.x - (element.value.x + element.value.sizeX - 1)
    if ((baseLeft === 0 && baseRight === 0) || baseRight < 0) {
      return 'bar-main-right-inner'
    } else if (baseRight === 0) {
      return 'bar-main-left-outer'
    } else {
      return 'bar-main-right'
    }
  } else if (showPosition.value === 'preview') {
    return 'bar-main-right-inner'
  } else if (showPosition.value === 'canvasDataV') {
    return 'bar-main-right'
  } else {
    return 'bar-main-preview-right-inner'
  }
})

const showBarTooltipPosition = computed(() => {
  if (showEditPosition.value.indexOf('right') >= 0) {
    return 'right'
  } else {
    return 'left'
  }
})

const openMessageLoading = cb => {
  const iconClass = `el-icon-loading`
  const customClass = `de-message-loading de-message-export`
  ElMessage({
    message: h('p', null, [
      '后台导出中,可前往',
      h(
        ElButton,
        {
          text: true,
          size: 'small',
          class: 'btn-text',
          onClick: () => {
            cb()
          }
        },
        t('data_export.export_center')
      ),
      '查看进度，进行下载'
    ]),
    iconClass,
    icon: h(RefreshLeft),
    showClose: true,
    customClass
  })
}

const callbackExport = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'IN_PROGRESS' })
}
const exportAsFormattedExcel = () => {
  const s2Instance = dvMainStore.getViewInstanceInfo(element.value.id)
  if (!s2Instance) {
    return
  }
  const chart = dvMainStore.getViewDetails(element.value.id)
  exportPivotExcel(s2Instance, chart)
}

const enableFormattedExport = computed(() => {
  const chart = dvMainStore.getViewDetails(element.value.id) as ChartObj
  const mode = chart?.customAttr?.basicStyle?.tableLayoutMode
  return mode === 'grid'
})
const exportAsExcel = () => {
  const viewDataInfo = dvMainStore.getViewDataDetails(element.value.id)
  const chartExtRequest = dvMainStore.getLastViewRequestInfo(element.value.id)
  const viewInfo = dvMainStore.getViewDetails(element.value.id)
  const chart = { ...viewInfo, chartExtRequest, data: viewDataInfo }
  exportExcelDownload(chart, () => {
    openMessageLoading(callbackExport)
  })
}
const exportAsImage = () => {
  // do export
  useEmitt().emitter.emit('componentImageDownload-' + element.value.id)
}
const deleteComponent = () => {
  eventBus.emit('removeMatrixItem-' + canvasId.value, index.value)
  dvMainStore.setCurComponent({ component: null, index: null })
  snapshotStore.recordSnapshotCache()
}

const copyComponent = () => {
  copyStore.copy()
  copyStore.paste(false)
}

const userViewEnlargeOpen = (e, opt) => {
  e.preventDefault()
  e.stopPropagation()
  emits('userViewEnlargeOpen', opt)
}

// 复用-Begin

const multiplexingCheckOut = () => {
  state.multiplexingCheckModel = !state.multiplexingCheckModel
  multiplexingCheck(state.multiplexingCheckModel)
}

const multiplexingCheck = val => {
  if (val) {
    // push
    dvMainStore.addCurMultiplexingComponent({
      component: element.value,
      componentId: element.value.id
    })
  } else {
    // remove
    dvMainStore.removeCurMultiplexingComponentWithId(element.value.id)
  }
}
// 复用-End

// 批量操作-Begin

const batchOptCheckOut = () => {
  if (showPosition.value === 'batchOpt') {
    state.batchOptCheckModel = !state.batchOptCheckModel
    batchOptChange(state.batchOptCheckModel)
  }
}

const batchOptChange = val => {
  if (val) {
    // push
    dvMainStore.addCurBatchComponent(element.value)
  } else {
    // remove
    dvMainStore.removeCurBatchComponentWithId(element.value.id)
  }
}
// 批量操作-End

const linkageChange = item => {
  let checkResult = false
  if (item.linkageFilters && item.linkageFilters.length > 0) {
    item.linkageFilters.forEach(linkage => {
      if (element.value.id === linkage.sourceViewId) {
        checkResult = true
      }
    })
  }
  return checkResult
}

const existLinkage = computed(() => {
  let linkageFiltersCount = 0
  componentData.value.forEach(item => {
    if (item.component === 'UserView' && item.innerType != 'VQuery') {
      if (linkageChange(item)) {
        linkageFiltersCount++
      }
    } else if (item.component === 'Group') {
      item.propValue.forEach(groupItem => {
        if (linkageChange(groupItem)) {
          linkageFiltersCount++
        }
      })
    } else if (item.component === 'DeTabs') {
      item.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          if (linkageChange(tabComponent)) {
            linkageFiltersCount++
          }
        })
      })
    }
  })
  return linkageFiltersCount
})

// 清除相同sourceViewId 的 联动条件
const clearLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.id)
  useEmitt().emitter.emit('clearPanelLinkage', { viewId: element.value.id })
}

// 富文本-Begin

const fieldsAreaDown = e => {
  // ignore
  e.stopPropagation()
  e.preventDefault()
}

const selectFieldShow = computed(() => {
  return (
    ['canvasDataV', 'canvas'].includes(showPosition.value) &&
    curComponent.value?.innerType === 'rich-text' &&
    curComponent.value.editing &&
    state.curFields.length > 0
  )
})

const initCurFields = () => {
  if (element.value.component === 'UserView') {
    const chartInfo = canvasViewInfo.value[element.value.id]
    if (chartInfo) {
      state.curFields = []
      if (chartInfo.type === 'rich-text' && chartInfo.curFields) {
        state.curFields = chartInfo.curFields
      }
    }
  }
}
// 富文本-End

onMounted(() => {
  if (element.value.component === 'UserView') {
    eventBus.on('initCurFields-' + element.value.id, initCurFields)
  }
  initCurFields()
})

onBeforeUnmount(() => {
  if (element.value.component === 'UserView') {
    eventBus.off('initCurFields-' + element.value.id, initCurFields)
  }
})

defineExpose({
  multiplexingCheckOut,
  batchOptCheckOut
})

watch(
  () => batchOptStatus.value,
  () => {
    state.batchOptCheckModel = false
  }
)
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  z-index: 10;
  border-radius: 2px;
  cursor: pointer !important;
  font-size: 16px !important;
}
.bar-main-background {
  background-color: var(--ed-color-primary, #3370ff);
}

.bar-main-right {
  width: 24px;
  right: -26px;
}

.bar-main-preview-right-inner {
  right: 0px;
}

.bar-main-right-inner {
  width: 24px;
  right: 0px;
}

.bar-main-left-outer {
  width: 24px;
  left: -26px;
}

.bar-base-icon {
  height: 24px;
  width: 24px;
  font-size: 16px;
  color: #ffffff;
  &:hover {
    color: rgba(255, 255, 255, 0.5);
  }
  &:active {
    color: rgba(255, 255, 255, 0.7);
  }
}

.bar-checkbox-area {
  padding: 0 5px;
  height: 24px;
}
</style>
