<template>
  <div class="bar-main" :class="showEditPosition" @mousedown="showLabelInfo">
    <input
      id="input"
      ref="files"
      type="file"
      accept="image/*"
      hidden
      @click="
        e => {
          e.target.value = ''
        }
      "
      @change="handleFileChange"
    />
    <div v-if="linkageAreaShow" style="width: 20px; margin-right: -1px">
      <el-checkbox v-model="linkageInfo.linkageActive" size="medium" />
      <linkage-field v-if="linkageInfo.linkageActive" :element="element" />
    </div>
    <div
      v-if="positionCheck('multiplexing') && showMultiplexingCheck"
      style="z-index: 5; width: 18px; margin-right: 1px"
    >
      <el-checkbox
        v-model="state.multiplexingCheckModel"
        size="medium"
        @change="multiplexingCheck"
      />
    </div>
    <div v-if="batchOptAreaShow" style="z-index: 5; width: 20px; margin-right: -1px">
      <el-checkbox size="medium" @change="batchOptChange" />
    </div>
    <div v-if="normalAreaShow">
      <span :title="t('visualization.edit')">
        <el-icon style="height: 22px" @click="edit"><Link /></el-icon>
      </span>
      <span :title="t('visualization.enlarge')">
        <i
          v-if="enlargeShow"
          class="icon iconfont icon-fangda"
          @click.stop="showViewDetails('enlarge')"
        />
      </span>
      <span :title="t('visualization.details')">
        <el-icon style="height: 22px" @click="showViewDetails"><Tickets /></el-icon>
      </span>
      <setting-menu
        v-if="activeModel === 'edit'"
        style="float: right; height: 24px !important"
        @amRemoveItem="amRemoveItem"
        @linkJumpSet="linkJumpSet"
        @boardSet="boardSet"
      >
        <template #icon>
          <span :title="t('visualization.setting')">
            <el-icon><More /></el-icon>
          </span>
        </template>
      </setting-menu>
      <span :title="t('visualization.cancel_linkage')">
        <i
          v-if="curComponent.type === 'view' && existLinkage"
          class="icon iconfont icon-quxiaoliandong"
          @click.stop="clearLinkage"
        />
      </span>
      <span :title="t('visualization.switch_picture')">
        <i
          v-if="activeModel === 'edit' && curComponent && curComponent.type === 'picture-add'"
          class="icon iconfont icon-genghuan"
          @click.stop="goFile"
        />
      </span>
      <el-popover v-if="selectFieldShow" width="200" trigger="click" @mousedown="fieldsAreaDown">
        <fields-list :fields="state.curFields" :element="element" />
        <template #reference>
          ICON
          <i
            :disabled="element.editing"
            :title="t('visualization.select_field')"
            class="icon iconfont icon-datasource-select"
            style="margin-left: 4px; font-size: 14px; cursor: pointer"
          />
        </template>
      </el-popover>
      <span :title="t('visualization.jump')">
        <a
          v-if="showJumpFlag"
          :title="curComponent.hyperlinks.content"
          :target="curComponent.hyperlinks.openMode"
          :href="curComponent.hyperlinks.content"
        >
          <i class="icon iconfont icon-com-jump" />
        </a>
      </span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import SettingMenu from '@/components/visualization/SettingMenu.vue'
import LinkageField from '@/components/visualization/LinkageField.vue'
import FieldsList from '@/components/visualization/FieldsList.vue'
import { computed, onMounted, reactive, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { uploadFileResult } from '@/api/staticResource'
import eventBus from '@/utils/eventBus'
import { ElMessage } from 'element-plus-secondary'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const emits = defineEmits(['closePreview', 'showViewDetails', 'amRemoveItem'])
const files = ref(null)
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()

const props = defineProps({
  canvasId: {
    type: String,
    required: true
  },
  terminal: {
    type: String,
    default: 'pc'
  },
  sourceElement: {
    type: Object,
    default: null
  },
  element: {
    type: Object,
    required: true
  },
  active: {
    type: Boolean,
    required: false,
    default: false
  },
  // 当前模式 preview 预览 edit 编辑，
  activeModel: {
    type: String,
    required: false,
    default: 'preview'
  },
  previewVisible: {
    type: Boolean,
    default: false
  },
  showPosition: {
    type: String,
    required: false,
    default: 'NotProvided'
  },
  chart: {
    type: Object,
    default: null
  },
  seriesIdMap: {
    type: Object,
    default: () => {
      return {
        id: ''
      }
    }
  }
})

const {
  canvasId,
  terminal,
  sourceElement,
  element,
  active,
  activeModel,
  previewVisible,
  showPosition,
  chart,
  seriesIdMap
} = toRefs(props)
const {
  pcMatrixCount,
  curComponent,
  componentData,
  canvasStyleData,
  linkageSettingStatus,
  targetLinkageInfo,
  curLinkageView,
  curCanvasScaleMap,
  batchOptStatus,
  mobileLayoutStatus,
  curBatchOptComponents,
  panelViewDetailsInfo
} = storeToRefs(dvMainStore)

const state = reactive({
  systemOS: 'Mac',
  maxImageSize: 15000000,
  boardSetVisible: false,
  linkJumpSetVisible: false,
  linkJumpSetViewId: null,
  curFields: [],
  multiplexingCheckModel: false,
  barWidth: 24,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom', 'custom-button'],
  timer: null,
  viewXArray: []
})

const yaxis = computed(() => {
  if (!chart.value) return []
  return chart.value.yaxis
})
const showMapLayerController = computed(() => {
  return (
    curComponent.value.type === 'view' &&
    terminal.value === 'pc' &&
    curComponent.value.propValue.innerType &&
    curComponent.value.propValue.innerType === 'map' &&
    yaxis.value.length > 1
  )
})
const detailsShow = computed(() => {
  return (
    curComponent.value.type === 'view' &&
    terminal.value === 'pc' &&
    curComponent.value.propValue.innerType &&
    curComponent.value.propValue.innerType !== 'richTextView'
  )
})

const enlargeShow = computed(() => {
  return (
    curComponent.value.type === 'view' &&
    curComponent.value.propValue.innerType &&
    curComponent.value.propValue.innerType !== 'richTextView' &&
    !curComponent.value.propValue.innerType.includes('table')
  )
})

const selectFieldShow = computed(() => {
  return (
    activeModel.value === 'edit' &&
    curComponent.value.type === 'view' &&
    curComponent.value.propValue.innerType &&
    curComponent.value.propValue.innerType === 'richTextView' &&
    curComponent.value.editing
  )
})

const curComponentTypes = computed(() => {
  const types = []
  componentData.value.forEach(component => {
    types.push(component.type)
  })
  return types
})

const showMultiplexingCheck = computed(() => {
  return (
    element.value.type !== 'custom-button' ||
    (element.value.type === 'custom-button' && !curComponent.value.type.includes('custom-button'))
  )
})
const showEditPosition = computed(() => {
  const baseLeft = element.value.x - 1
  const baseRight = pcMatrixCount.value.x - (element.value.x + element.value.sizeX - 1)
  if (baseLeft === 0 && baseRight === 0) {
    return 'bar-main-right-inner'
  } else if (baseRight === 0) {
    return 'bar-main-left-outer'
  } else {
    return 'bar-main-right'
  }
})

const showJumpFlag = computed(() => {
  return curComponent.value && curComponent.value.hyperlinks && curComponent.value.hyperlinks.enable
})
// batch operation area
const batchOptAreaShow = computed(() => {
  return batchOptStatus.value && element.value.type === 'view' && !element.value.isPlugin
})
// 联动区域按钮显示
const linkageAreaShow = computed(() => {
  return (
    linkageSettingStatus.value &&
    element.value !== curLinkageView.value &&
    element.value.type === 'view'
  )
})
// 编辑或预览区域显示
const normalAreaShow = computed(() => {
  return true
  // return !linkageSettingStatus.value && !batchOptStatus.value && !positionCheck('multiplexing')
})
const existLinkage = computed(() => {
  let linkageFiltersCount = 0
  componentData.value.forEach(item => {
    if (item.linkageFilters && item.linkageFilters.length > 0) {
      item.linkageFilters.forEach(linkage => {
        if (element.value.propValue.viewId === linkage.sourceViewId) {
          linkageFiltersCount++
        }
      })
    }
  })
  return linkageFiltersCount
})
const linkageInfo = computed(() => {
  return targetLinkageInfo.value[element.value.propValue.viewId]
})

const miniHeight = computed(() => {
  return mobileLayoutStatus.value ? 1 : 1
})
const miniWidth = computed(() => {
  return mobileLayoutStatus.value ? 1 : 1
})
const curCanvasScaleSelf = computed(() => {
  return curCanvasScaleMap.value[canvasId.value]
})

const viewEnlarge = () => {
  showViewDetails('enlarge')
}
const backgroundSetClose = () => {
  state.boardSetVisible = false
}
const linkJumpSet = () => {
  state.linkJumpSetViewId = element.value.propValue.viewId
  state.linkJumpSetVisible = true
}
const closeJumpSetDialog = () => {
  state.linkJumpSetVisible = false
}
const fieldsAreaDown = e => {
  // ignore
  e.preventDefault()
}
const initCurFields = () => {
  if (element.value.type === 'view') {
    const chartInfo = panelViewDetailsInfo.value[element.value.propValue.viewId]
    if (chartInfo) {
      state.curFields = []
      const chartDetails = JSON.parse(chartInfo)
      if (chartDetails['type'] === 'richTextView' && chartDetails['data']) {
        state.curFields = chartDetails['data'].fields
      }
    }
  }
}
const positionCheck = position => {
  return showPosition.value.includes(position)
}
const multiplexingCheck = val => {
  if (val) {
    // push
    dvMainStore.addCurMultiplexingComponent({
      component: sourceElement.value,
      componentId: element.value.id
    })
  } else {
    // remove
    dvMainStore.removeCurMultiplexingComponentWithId(element.value.id)
  }
}
const closePreview = () => {
  emits('closePreview')
}
const destroyTimer = () => {
  if (state.timer) {
    clearInterval(state.timer)
    state.timer = null
  }
}
const showViewDetails = (openType = 'details') => {
  emits('showViewDetails', { openType: openType })
}

const edit = () => {
  if (curComponent.value.type === 'custom') {
    eventBus.emit('component-dialog-edit', 'update')
  } else if (curComponent.value.type === 'custom-button') {
    eventBus.emit('button-dialog-edit')
  } else if (
    curComponent.value.type === 'v-text' ||
    curComponent.value.type === 'de-rich-text' ||
    curComponent.value.type === 'rect-shape'
  ) {
    eventBus.emit('component-dialog-style')
  } else {
    eventBus.emit('change_panel_right_draw', true)
  }
}
const linkageEdit = () => {
  return null
}
const amRemoveItem = () => {
  emits('amRemoveItem')
}
// 清除相同sourceViewId 的 联动条件
const clearLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.propValue.viewId)
}
const goFile = () => {
  files.value.click()
}
const showLabelInfo = e => {
  // ignore
  e.preventDefault()
}
const sizeMessage = () => {
  ElMessage.warning(t('visualization.image_size_tips'))
}
const handleFileChange = e => {
  const file = e.target.files[0]
  if (!file.type.includes('image')) {
    ElMessage.warning(t('visualization.image_size_tips'))
    return
  }
  if (file.size > state.maxImageSize) {
    sizeMessage()
  }
  uploadFileResult(file, fileUrl => {
    curComponent.value.propValue = fileUrl
    snapshotStore.recordSnapshot('uploadFileResult')
  })
}

const boardSet = () => {
  state.boardSetVisible = true
}
const batchOptChange = val => {
  if (val) {
    // push
    dvMainStore.addCurBatchComponent(element.value.propValue.viewId)
  } else {
    // remove
    dvMainStore.removeCurBatchComponentWithId(element.value.propValue.viewId)
  }
}

onMounted(() => {
  if (navigator.platform.indexOf('Mac') === -1) {
    state.systemOS = 'Other'
  }
  initCurFields()
  if (element.value.type === 'view') {
    eventBus.on('initCurFields-' + element.value.id, initCurFields)
    eventBus.on('viewEnlarge', viewEnlarge)
  }
})
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  z-index: 2;
  border-radius: 2px;
  padding-left: 3px;
  padding-right: 0px;
  cursor: pointer !important;
  background-color: var(--primary, #3370ff);
}

.bar-main ::v-deep i {
  color: white;
  float: right;
  margin-right: 3px;
}

.bar-main ::v-deep .el-checkbox__inner {
  width: 16px;
  height: 16px;
}

.bar-main ::v-deep .el-checkbox__inner::after {
  width: 4.5px;
}

.bar-main-right {
  width: 22px;
  right: -25px;
}

.bar-main-right-inner {
  width: 22px;
  right: 0px;
}

.bar-main-left-outer {
  width: 22px;
  left: -25px;
}

.bar-main-preview {
  right: 0px;
}
</style>
