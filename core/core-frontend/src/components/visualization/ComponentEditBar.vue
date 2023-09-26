<template>
  <div
    class="bar-main"
    :class="[
      showEditPosition,
      {
        'bar-main-background': mainBackgroundShow
      }
    ]"
    @mousedown="fieldsAreaDown"
  >
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
      <el-checkbox @change="batchOptChange" />
    </div>

    <el-dropdown trigger="click" placement="right-start" v-if="barShowCheck('setting')">
      <el-icon class="bar-base-icon">
        <el-tooltip :content="t('visualization.more')" effect="dark" placement="bottom">
          <icon name="icon_more_outlined" />
        </el-tooltip>
      </el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 160px">
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
              style="padding-right: 8px"
              v-if="element.innerType !== 'rich-text' && barShowCheck('download')"
              @click.prevent
            >
              <el-dropdown style="width: 100%" trigger="hover" placement="right-start">
                <div style="width: 100%">
                  导出为
                  <el-icon style="float: right"><ArrowRight /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu style="width: 120px">
                    <el-dropdown-item @click="exportAsExcel">Excel</el-dropdown-item>
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
      v-if="element.innerType !== 'rich-text' && barShowCheck('previewDownload')"
    >
      <el-icon @click="downloadClick" class="bar-base-icon">
        <el-tooltip :content="t('chart.export')" effect="dark" placement="bottom">
          <icon name="dv-preview-download" />
        </el-tooltip>
      </el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 160px">
          <el-dropdown-item @click="exportAsExcel">导出为Excel</el-dropdown-item>
          <el-dropdown-item @click="exportAsImage">导出为图片</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-popover v-if="selectFieldShow" width="200" trigger="click" @mousedown="fieldsAreaDown">
      <template #reference>
        <el-icon class="bar-base-icon"> <Icon name="database"></Icon></el-icon>
      </template>
      <fields-list :fields="state.curFields" :element="element" />
    </el-popover>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeUnmount, onMounted, onUnmounted, reactive, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'
import LinkageField from '@/components/visualization/LinkageField.vue'
import { getViewLinkageGather } from '@/api/visualization/linkage'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { exportExcelDownload } from '@/views/chart/components/js/util'
import FieldsList from '@/custom-component/rich-text/FieldsList.vue'
import { ElTooltip } from 'element-plus-secondary'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
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
  default: ['setting', 'delete', 'copy', 'multiplexing']
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

const linkageCheckShowAttach = computed(() => {
  return curLinkageView.value !== element.value
})

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

const { element, active, index, showPosition, canvasId } = toRefs(props)
const {
  pcMatrixCount,
  curComponent,
  componentData,
  canvasStyleData,
  targetLinkageInfo,
  curLinkageView,
  dvInfo,
  canvasViewInfo
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
    if (baseLeft === 0 && baseRight === 0) {
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

const exportAsExcel = () => {
  const viewDataInfo = dvMainStore.getViewDataDetails(element.value.id)
  const chartExtRequest = dvMainStore.getLastViewRequestInfo(element.value.id)
  const viewInfo = dvMainStore.getViewDetails(element.value.id)
  const chart = { ...viewInfo, chartExtRequest, data: viewDataInfo }
  exportExcelDownload(chart)
}
const exportAsImage = () => {
  // do export
  useEmitt().emitter.emit('componentImageDownload-' + element.value.id)
}
const deleteComponent = () => {
  eventBus.emit('removeMatrixItem-' + canvasId.value, index.value)
  dvMainStore.setCurComponent({ component: null, index: null })
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
const batchOptChange = val => {
  if (val) {
    // push
    dvMainStore.addCurBatchComponent(element.value.id)
  } else {
    // remove
    dvMainStore.removeCurBatchComponentWithId(element.value.id)
  }
}
// 批量操作-End

// 联动-Begin
const linkageSetting = () => {
  // sourceViewId 也加入查询
  const targetViewIds = componentData.value
    .filter(item => item.component === 'UserView')
    .map(item => item.id)

  // 获取当前仪表板当前视图联动信息
  const requestInfo = {
    dvId: dvInfo.value.id,
    sourceViewId: curComponent.value.id,
    targetViewIds: targetViewIds,
    linkageInfo: null
  }
  getViewLinkageGather(requestInfo).then(rsp => {
    dvMainStore.setLinkageTargetInfo(rsp.data)
  })
}

const existLinkage = computed(() => {
  let linkageFiltersCount = 0
  componentData.value.forEach(item => {
    if (item.linkageFilters && item.linkageFilters.length > 0) {
      item.linkageFilters.forEach(linkage => {
        if (element.value.id === linkage.sourceViewId) {
          linkageFiltersCount++
        }
      })
    }
  })
  return linkageFiltersCount
})

const linkageInfo = computed(() => {
  return targetLinkageInfo.value[element.value.id]
})

// 清除相同sourceViewId 的 联动条件
const clearLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.id)
}
const linkageSetOpen = () => {
  emits('linkageSetOpen')
}

// 联动-End

// 跳转-Begin
const linkJumpSetOpen = () => {
  emits('linkJumpSetOpen')
}
// 跳转-End

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
  multiplexingCheckOut
})
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  z-index: 10;
  border-radius: 2px;
  cursor: pointer !important;
}
.bar-main-background {
  background-color: var(--primary, #3370ff);
}

.bar-main-right {
  width: 22px;
  right: -25px;
}

.bar-main-preview-right-inner {
  right: 0px;
}

.bar-main-right-inner {
  width: 22px;
  right: 0px;
}

.bar-main-left-outer {
  width: 22px;
  left: -25px;
}

.bar-base-icon {
  height: 22px;
  width: 22px;
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
  height: 22px;
}

.more-menu {
  width: 160px;
}
</style>
