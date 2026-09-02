<script lang="ts" setup>
import { ref, inject, computed, onMounted, onUnmounted, watch, nextTick, type Ref } from 'vue'
import { Search, Refresh, Expand, Fold } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus-secondary'
import Icon from '@/components/icon-custom/src/Icon.vue'
import router from '@/router'
import { useCache } from '@/hooks/web/useCache'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_more_vertical_outlined from '@/assets/svg/icon_more-vertical_outlined.svg'
import icon_sheet_datareference_outlined from '@/assets/svg/icon_sheet-datareference_outlined.svg'
import OpenHandler from '../../../../component/embedded-iframe/OpenHandler.vue'
import DatasetSelect from './dataset-select.vue'
import FieldItem from '../common/field-item.vue'
import type { FieldItemData } from '../../types/plugin'
import { SPREADSHEET_EVENTS } from '../../utils/events'

const { t } = useI18n()
const { wsCache } = useCache()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)
const openHandler = ref<InstanceType<typeof OpenHandler>>()

//事件
const emit = defineEmits<{
  'updateConfig': [key: string, val: any]
}>()

// 注入插件配置
const pluginConfig = inject('pluginConfig') as any
const datasetFields = inject<Ref<{ dimensions: FieldItemData[]; quotas: FieldItemData[] }>>('datasetFields')
const beforeCreateDataset = inject<() => boolean | Promise<boolean>>(
  'beforeCreateSpreadsheetDataset'
)
const datasetSelectRef = ref<InstanceType<typeof DatasetSelect>>()
const collapsed = ref(false)
const curDatasetWeight = ref(0)
const canEditDataset = computed(() => curDatasetWeight.value >= 7 && !isDataEaseBi.value)
const hasDataset = computed(() =>
  Boolean(selectedDataset.value || pluginConfig?.value?.data?.datasetId)
)

// 选中的数据集（本地状态，用于组件内部）
const selectedDataset = ref('')
const lastLoadedDatasetKey = ref('')

// 搜索关键字
const searchKeyword = ref('')

// 拖拽分隔线相关
type ScrollbarExpose = {
  update: () => void
}

type FieldListLayout = {
  contentTop: number
  sectionHeight: number
}

const DEFAULT_DIMENSION_RATIO = 0.6
const MIN_FIELD_SECTION_HEIGHT = 60
const isDragging = ref(false)
const dimensionHeight = ref(200) // 维度区域默认高度
const fieldListRef = ref<HTMLElement>()
const dimensionScrollbarRef = ref<ScrollbarExpose>()
const quotaScrollbarRef = ref<ScrollbarExpose>()
let fieldListResizeObserver: ResizeObserver | undefined
let fieldListInitialized = false

const parsePixel = (value: string) => Number.parseFloat(value) || 0

const getFieldListLayout = (): FieldListLayout | undefined => {
  const fieldList = fieldListRef.value
  if (!fieldList) return

  const fieldListStyle = window.getComputedStyle(fieldList)
  const paddingTop = parsePixel(fieldListStyle.paddingTop)
  const paddingBottom = parsePixel(fieldListStyle.paddingBottom)
  const divider = fieldList.querySelector<HTMLElement>('.divider-drag')
  let dividerOuterHeight = 0

  if (divider) {
    const dividerStyle = window.getComputedStyle(divider)
    dividerOuterHeight =
      divider.offsetHeight +
      parsePixel(dividerStyle.marginTop) +
      parsePixel(dividerStyle.marginBottom)
  }

  // 维度和指标只分配字段区内容高度，不能把 padding 和分隔条重复计入滚动区域。
  const sectionHeight = Math.max(
    0,
    fieldList.clientHeight - paddingTop - paddingBottom - dividerOuterHeight
  )
  const contentTop = fieldList.getBoundingClientRect().top + paddingTop
  return { contentTop, sectionHeight }
}

const clampDimensionHeight = (height: number, sectionHeight: number) => {
  const maxHeight = Math.max(
    MIN_FIELD_SECTION_HEIGHT,
    sectionHeight - MIN_FIELD_SECTION_HEIGHT
  )
  return Math.max(MIN_FIELD_SECTION_HEIGHT, Math.min(maxHeight, height))
}

const updateFieldScrollbars = () => {
  nextTick(() => {
    dimensionScrollbarRef.value?.update()
    quotaScrollbarRef.value?.update()
  })
}

const syncFieldListLayout = () => {
  const layout = getFieldListLayout()
  if (!layout || layout.sectionHeight <= 0) return

  if (!fieldListInitialized) {
    dimensionHeight.value = clampDimensionHeight(
      Math.floor(layout.sectionHeight * DEFAULT_DIMENSION_RATIO),
      layout.sectionHeight
    )
    fieldListInitialized = true
  } else {
    dimensionHeight.value = clampDimensionHeight(dimensionHeight.value, layout.sectionHeight)
  }

  // Element Plus 只监听内容尺寸，父级分区变化后需要主动同步滚动条轨道。
  updateFieldScrollbars()
}

// 开始拖拽
const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  e.preventDefault()
}

// 处理拖拽
const handleDrag = (e: MouseEvent) => {
  if (!isDragging.value) return

  const layout = getFieldListLayout()
  if (!layout) return

  const newHeight = e.clientY - layout.contentTop
  dimensionHeight.value = clampDimensionHeight(newHeight, layout.sectionHeight)
  updateFieldScrollbars()
}

// 停止拖拽
const stopDrag = () => {
  isDragging.value = false
}

// 监听拖拽事件
onMounted(() => {
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)

  fieldListResizeObserver = new ResizeObserver(syncFieldListLayout)
  if (fieldListRef.value) {
    fieldListResizeObserver.observe(fieldListRef.value)
  }
  syncFieldListLayout()
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
  fieldListResizeObserver?.disconnect()
})

watch(
  fieldListRef,
  (fieldList, previousFieldList) => {
    if (previousFieldList) {
      fieldListResizeObserver?.unobserve(previousFieldList)
    }
    if (!fieldList) return

    fieldListResizeObserver?.observe(fieldList)
    nextTick(syncFieldListLayout)
  },
  { flush: 'post' }
)

// 维度和指标数据（从数据集加载）
const dimensionFields = ref<FieldItemData[]>([])
const quotaFields = ref<FieldItemData[]>([])

// 选中的字段
const selectedDimensionFields = ref<FieldItemData[]>([])
const selectedQuotaFields = ref<FieldItemData[]>([])

// 最后点击的字段索引（用于shift范围选择）
const lastClickedDimensionIndex = ref(-1)
const lastClickedQuotaIndex = ref(-1)

// 处理字段加载
const handleFieldsLoaded = (fields: { dimensions: FieldItemData[]; quotas: FieldItemData[] }) => {
  dimensionFields.value = fields.dimensions
  quotaFields.value = fields.quotas
  if (datasetFields) {
    datasetFields.value = fields
  }
  // 清空选中状态
  selectedDimensionFields.value = []
  selectedQuotaFields.value = []
  lastClickedDimensionIndex.value = -1
  lastClickedQuotaIndex.value = -1
}

// 判断是否选中
const isDimensionSelected = (field: FieldItemData) => {
  return selectedDimensionFields.value.some(f => f.id === field.id)
}

const isQuotaSelected = (field: FieldItemData) => {
  return selectedQuotaFields.value.some(f => f.id === field.id)
}

// 普通点击 - 单选（点击已选中的字段则取消选中）
const handleNormalClick = (field: FieldItemData, index: number, type: 'dimension' | 'quota') => {
  if (type === 'dimension') {
    // 如果点击的是已选中的字段，则取消选中
    if (selectedDimensionFields.value.length === 1 && selectedDimensionFields.value[0].id === field.id) {
      selectedDimensionFields.value = []
      lastClickedDimensionIndex.value = -1
    } else {
      selectedDimensionFields.value = [field]
      lastClickedDimensionIndex.value = index
    }
    selectedQuotaFields.value = []
    lastClickedQuotaIndex.value = -1
  } else {
    // 如果点击的是已选中的字段，则取消选中
    if (selectedQuotaFields.value.length === 1 && selectedQuotaFields.value[0].id === field.id) {
      selectedQuotaFields.value = []
      lastClickedQuotaIndex.value = -1
    } else {
      selectedQuotaFields.value = [field]
      lastClickedQuotaIndex.value = index
    }
    selectedDimensionFields.value = []
    lastClickedDimensionIndex.value = -1
  }
}

// Ctrl/Cmd + 点击 - 切换选中状态
const handleCtrlClick = (field: FieldItemData, index: number, type: 'dimension' | 'quota') => {
  if (type === 'dimension') {
    selectedQuotaFields.value = []
    const selectedIndex = selectedDimensionFields.value.findIndex(f => f.id === field.id)
    if (selectedIndex !== -1) {
      selectedDimensionFields.value.splice(selectedIndex, 1)
    } else {
      selectedDimensionFields.value.push(field)
    }
    lastClickedDimensionIndex.value = index
  } else {
    selectedDimensionFields.value = []
    const selectedIndex = selectedQuotaFields.value.findIndex(f => f.id === field.id)
    if (selectedIndex !== -1) {
      selectedQuotaFields.value.splice(selectedIndex, 1)
    } else {
      selectedQuotaFields.value.push(field)
    }
    lastClickedQuotaIndex.value = index
  }
}

// Shift + 点击 - 范围选择
const handleShiftClick = (field: FieldItemData, index: number, type: 'dimension' | 'quota') => {
  if (type === 'dimension') {
    selectedQuotaFields.value = []
    const fields = filteredDimensionFields.value
    const lastIndex = lastClickedDimensionIndex.value

    if (lastIndex === -1 || lastIndex === index) {
      // 没有上次点击或点击同一位置，单选
      selectedDimensionFields.value = [field]
    } else {
      // 范围选择
      const start = Math.min(lastIndex, index)
      const end = Math.max(lastIndex, index)
      selectedDimensionFields.value = fields.slice(start, end + 1)
    }
  } else {
    selectedDimensionFields.value = []
    const fields = filteredQuotaFields.value
    const lastIndex = lastClickedQuotaIndex.value

    if (lastIndex === -1 || lastIndex === index) {
      selectedQuotaFields.value = [field]
    } else {
      const start = Math.min(lastIndex, index)
      const end = Math.max(lastIndex, index)
      selectedQuotaFields.value = fields.slice(start, end + 1)
    }
  }
}

// 字段点击处理
const handleFieldClick = (field: FieldItemData, index: number, event: MouseEvent, type: 'dimension' | 'quota') => {
  if (event.ctrlKey || event.metaKey) {
    handleCtrlClick(field, index, type)
  } else if (event.shiftKey) {
    handleShiftClick(field, index, type)
  } else {
    handleNormalClick(field, index, type)
  }
}

// 拖拽开始 - 批量拖拽选中的字段
const handleDragStart = (field: FieldItemData, index: number, event: DragEvent, type: 'dimension' | 'quota') => {
  // 如果没有选中任何字段，或当前拖拽的字段不在选中列表中，则单选当前字段
  const selectedList = type === 'dimension' ? selectedDimensionFields.value : selectedQuotaFields.value
  const isSelected = selectedList.some(f => f.id === field.id)

  if (!isSelected) {
    handleNormalClick(field, index, type)
  }

  // 设置拖拽数据为所有选中的字段
  if (event.dataTransfer) {
    const finalSelectedList = type === 'dimension' ? selectedDimensionFields.value : selectedQuotaFields.value
    event.dataTransfer.setData('application/json', JSON.stringify(finalSelectedList))
    event.dataTransfer.effectAllowed = 'copy'
  }
}

// 是否正在拖拽
const isDrag = ref(false)
const isDraggingItem = ref(false)

// 创建拖拽图像
const createDragImage = (fields: FieldItemData[], type: 'dimension' | 'quota') => {
  const div = document.createElement('div')
  div.style.cssText = `
    position: fixed;
    left: -9999px;
    top: -9999px;
    background: #fff;
    border: 1px solid ${type === 'dimension' ? '#3370ff' : '#34c724'};
    border-radius: 4px;
    padding: 8px 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    min-width: 160px;
    max-width: 240px;
    z-index: 9999;
  `

  fields.forEach(field => {
    const item = document.createElement('div')
    item.style.cssText = `
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 0;
    `
    item.innerHTML = `
      <span style="font-size: 14px; font-weight: 500; width: 16px; text-align: center; color: ${type === 'dimension' ? '#3370ff' : '#34c724'};">
        ${type === 'dimension' ? 'T' : '#'}
      </span>
      <span style="font-size: 13px; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
        ${field.name}
      </span>
    `
    div.appendChild(item)
  })

  document.body.appendChild(div)
  return div
}

// 拖拽开始
const onDragStart = (type: 'dimension' | 'quota', event: DragEvent) => {
  isDrag.value = true

  // 创建自定义拖拽图像
  const selectedFields = type === 'dimension' ? selectedDimensionFields.value : selectedQuotaFields.value
  if (selectedFields.length > 1) {
    const dragImage = createDragImage(selectedFields, type)
    event.dataTransfer?.setDragImage(dragImage, 10, 10)

    // 拖拽结束后清理
    setTimeout(() => {
      document.body.removeChild(dragImage)
    }, 100)
  }

  setTimeout(() => {
    isDraggingItem.value = true
  }, 0)
}

// 拖拽结束
const onDragEnd = () => {
  isDrag.value = false
  isDraggingItem.value = false
}

// 过滤后的字段
const filteredDimensionFields = computed(() => {
  if (!searchKeyword.value) return dimensionFields.value
  return dimensionFields.value.filter(f =>
    f.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const filteredQuotaFields = computed(() => {
  if (!searchKeyword.value) return quotaFields.value
  return quotaFields.value.filter(f =>
    f.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const handleDatasetChange = (val: string | number) => {
  emit('updateConfig', 'data.datasetId', String(val))
  emit('updateConfig', 'data.customFilter', {})
  // 注意：现在由 dataset-select 组件控制是否重复点击
  // 这里只在真正切换数据集时才会被调用
  // 清空之前的字段，等待新数据集的字段加载
  dimensionFields.value = []
  quotaFields.value = []
}

const handleDatasetNodeChange = (node?: { weight?: number }) => {
  curDatasetWeight.value = Number(node?.weight || 0)
}

// 同步本地状态与全局配置
watch(() => pluginConfig?.value?.data?.datasetId, (newVal) => {
  if (newVal && newVal !== selectedDataset.value) {
    selectedDataset.value = String(newVal)
  }
}, { immediate: true })

const syncDatasetFieldsFromPluginConfig = async () => {
  await nextTick()

  const pluginId = pluginConfig?.value?.id || ''
  const datasetId = pluginConfig?.value?.data?.datasetId
  if (!datasetId) {
    lastLoadedDatasetKey.value = ''
    selectedDataset.value = ''
    handleFieldsLoaded({ dimensions: [], quotas: [] })
    return
  }

  const datasetKey = `${pluginId}:${datasetId}`
  if (datasetKey === lastLoadedDatasetKey.value) {
    return
  }

  selectedDataset.value = String(datasetId)
  if (!datasetSelectRef.value?.loadDatasetFields) {
    return
  }

  lastLoadedDatasetKey.value = datasetKey
  await datasetSelectRef.value.loadDatasetFields(datasetId)
}

watch(
  [
    () => pluginConfig?.value?.id,
    () => pluginConfig?.value?.data?.datasetId,
    () => datasetSelectRef.value
  ],
  syncDatasetFieldsFromPluginConfig,
  { immediate: true, flush: 'post' }
)

const initOpenHandler = (newWindow: Window | null) => {
  if (!newWindow || !openHandler.value) {
    return
  }
  openHandler.value.initOpenHandler(newWindow)
}

const openDatasetForm = (query?: Record<string, string | number>) => {
  const path =
    embeddedStore.getToken && appStore.getIsIframe ? '/dataset-embedded-form' : '/dataset-form'
  const routeObj = {
    path,
    query
  }
  const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
  if (openType === '_self') {
    router.push(routeObj)
    return
  }

  const routeData = router.resolve(routeObj)
  const newWindow = window.open(routeData.href, openType)
  initOpenHandler(newWindow)
}

const openEmbeddedDatasetEditor = (datasetId?: string | number) => {
  embeddedStore.clearState()
  if (datasetId) {
    embeddedStore.setDatasetId(String(datasetId))
  }
  useEmitt().emitter.emit('changeCurrentComponent', 'DatasetEditor')
}

const handleAddDataset = async () => {
  if (beforeCreateDataset && !(await beforeCreateDataset())) {
    return
  }

  if (isDataEaseBi.value) {
    openEmbeddedDatasetEditor()
    return
  }

  openDatasetForm()
}

const handleEditDataset = () => {
  const datasetId = selectedDataset.value || pluginConfig?.value?.data?.datasetId
  if (!datasetId) {
    ElMessage.warning('请先选择数据集')
    return
  }

  if (isDataEaseBi.value) {
    openEmbeddedDatasetEditor(datasetId)
    return
  }

  openDatasetForm({ id: datasetId })
}

const handleReplaceDataset = () => {
  const componentId = pluginConfig?.value?.id
  const datasetId = selectedDataset.value || pluginConfig?.value?.data?.datasetId
  if (!componentId || !datasetId) {
    ElMessage.warning(t('spreadsheet.select_required'))
    return
  }
  useEmitt().emitter.emit(SPREADSHEET_EVENTS.OPEN_COMPONENT_DATASET_REPLACEMENT, {
    componentId: String(componentId)
  })
}

const handleDatasetAction = (command: 'edit' | 'replace') => {
  if (command === 'edit') {
    handleEditDataset()
    return
  }
  handleReplaceDataset()
}

const handleRefresh = () => {
  datasetSelectRef.value?.refresh()
  if (selectedDataset.value) {
    datasetSelectRef.value?.loadDatasetFields(selectedDataset.value)
  }
}

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <div class="dataset-panel" :class="{ 'is-collapsed': collapsed }">
    <div class="panel-header">
      <span v-if="!collapsed" class="title">数据集</span>
      <span v-else class="title-collapsed"></span>
      <el-icon class="header-icon" @click="toggleCollapsed">
        <Fold v-if="collapsed" />
        <Expand v-else />
      </el-icon>
    </div>

    <template v-if="!collapsed">
      <div class="dataset-select-section">
        <div class="dataset-select-wrapper">
          <DatasetSelect
            ref="datasetSelectRef"
            v-model="selectedDataset"
            @dataset-change="handleDatasetChange"
            @fields-loaded="handleFieldsLoaded"
            @add-dataset="handleAddDataset"
            @dataset-node-change="handleDatasetNodeChange"
          />
        </div>
        <el-dropdown
          v-if="hasDataset"
          trigger="click"
          placement="bottom-end"
          popper-class="spreadsheet-dataset-actions"
          @command="handleDatasetAction"
        >
          <button
            type="button"
            class="dataset-more-button"
            :aria-label="t('spreadsheet.dataset_replacement.more')"
          >
            <Icon name="icon_more-vertical_outlined">
              <icon_more_vertical_outlined />
            </Icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="edit" :disabled="!canEditDataset">
                <el-icon>
                  <Icon name="icon_edit_outlined">
                    <icon_edit_outlined />
                  </Icon>
                </el-icon>
                {{ t('deDataset.edit_dataset') }}
              </el-dropdown-item>
              <el-dropdown-item command="replace">
                <el-icon>
                  <Icon name="icon_sheet-datareference_outlined">
                    <icon_sheet_datareference_outlined />
                  </Icon>
                </el-icon>
                {{ t('spreadsheet.dataset_replacement.title') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="field-header">
        <span class="field-title">字段</span>
        <el-icon class="refresh-icon" @click="handleRefresh">
          <Refresh />
        </el-icon>
      </div>

      <div class="field-search">
        <el-icon class="search-icon">
          <Search />
        </el-icon>
        <input
          v-model="searchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索 字段"
        />
      </div>

      <div ref="fieldListRef" class="field-list" :class="{ 'is-dragging': isDragging }">
        <div class="dimension-section" :style="{ height: dimensionHeight + 'px' }">
          <div class="group-title">维度</div>
          <el-scrollbar ref="dimensionScrollbarRef" class="field-items-scrollbar">
            <div class="field-items">
              <div
                v-for="(field, index) in filteredDimensionFields"
                :key="field.id"
                class="field-item-wrapper"
              >
                <FieldItem
                  :field="field"
                  :index="index"
                  :show-remove="false"
                  :show-config="false"
                  :selected="isDimensionSelected(field)"
                  @click="(f, i, e) => handleFieldClick(f, i, e, 'dimension')"
                  @dragstart="(f, i, e) => { handleDragStart(f, i, e, 'dimension'); onDragStart('dimension', e); }"
                  @dragend="onDragEnd"
                />
              </div>
            </div>
          </el-scrollbar>
        </div>

        <div class="divider-drag" @mousedown="startDrag">
          <div class="divider-line"></div>
        </div>

        <div class="quota-section">
          <div class="group-title">指标</div>
          <el-scrollbar ref="quotaScrollbarRef" class="field-items-scrollbar">
            <div class="field-items">
              <div
                v-for="(field, index) in filteredQuotaFields"
                :key="field.id"
                class="field-item-wrapper"
              >
                <FieldItem
                  :field="field"
                  :index="index"
                  :show-remove="false"
                  :show-config="false"
                  :selected="isQuotaSelected(field)"
                  @click="(f, i, e) => handleFieldClick(f, i, e, 'quota')"
                  @dragstart="(f, i, e) => { handleDragStart(f, i, e, 'quota'); onDragStart('quota', e); }"
                  @dragend="onDragEnd"
                />
              </div>
            </div>
          </el-scrollbar>
        </div>
      </div>
    </template>
    <div v-else class="collapsed-placeholder">
      <span class="collapsed-title">数据集</span>
    </div>
  </div>
  <OpenHandler ref="openHandler" />
</template>

<style lang="less" scoped>
.dataset-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 280px;
  background: #fff;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.2s ease;

  &.is-collapsed {
    width: 48px;
  }

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #e5e7eb;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #1f2329;
    }

    .title-collapsed {
      flex: 1;
    }

    .header-icon {
      font-size: 18px;
      color: #8f959e;
      cursor: pointer;
    }
  }

  .dataset-select-section {
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 8px;

    .dataset-select-wrapper {
      flex: 1;

      :deep(.dataset-select-trigger) {
        border-color: #3370ff;

        &:hover {
          border-color: #3370ff;
        }
      }
    }

    .dataset-more-button {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      padding: 4px;
      color: #646a73;
      background: transparent;
      border: 0;
      border-radius: 4px;
      cursor: pointer;

      svg {
        width: 16px;
        height: 16px;
      }

      &:hover,
      &:focus-visible {
        background: rgba(31, 35, 41, 0.1);
        outline: none;
      }
    }
  }

  .collapsed-placeholder {
    flex: 1;
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    padding: 12px 0 12px 10px;
  }

  .collapsed-title {
    writing-mode: vertical-rl;
    text-orientation: upright;
    font-size: 16px;
    line-height: 1;
    letter-spacing: 2px;
    font-weight: 700;
    color: #1f2329;
  }

  .field-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 16px;

    .field-title {
      font-size: 14px;
      font-weight: 500;
      color: #1f2329;
    }

    .refresh-icon {
      font-size: 14px;
      color: #8f959e;
      cursor: pointer;

      &:hover {
        color: #3370ff;
      }
    }
  }

  .field-search {
    display: flex;
    align-items: center;
    padding: 8px 16px;
    border-bottom: 1px solid #e5e7eb;

    .search-icon {
      font-size: 14px;
      color: #8f959e;
      margin-right: 8px;
    }

    .search-input {
      flex: 1;
      border: none;
      outline: none;
      background: transparent;
      font-size: 12px;
      color: #1f2329;

      &::placeholder {
        color: #8f959e;
      }
    }
  }

  .field-list {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
    overflow: hidden;
    padding: 12px 16px;

    &.is-dragging {
      user-select: none;
      cursor: row-resize;
    }

    .dimension-section,
    .quota-section {
      display: flex;
      flex-direction: column;
      overflow: hidden;
      min-height: 60px;

      .group-title {
        font-size: 12px;
        font-weight: 500;
        color: #1f2329;
        margin-bottom: 8px;
        flex-shrink: 0;
      }

      .field-items-scrollbar {
        flex: 1 1 0;
        height: 0;
        min-height: 0;
        overflow: hidden;
      }

      .field-items {
        display: flex;
        flex-direction: column;
        gap: 4px;
      }
    }

    .dimension-section {
      flex: 0 0 auto;
    }

    .quota-section {
      flex: 1 1 0;
    }

    .divider-drag {
      height: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: row-resize;
      flex-shrink: 0;
      margin: 4px 0;

      &:hover {
        .divider-line {
          background: #3370ff;
        }
      }

      .divider-line {
        width: 100%;
        height: 2px;
        background: #e5e7eb;
        border-radius: 1px;
        transition: background 0.2s;
      }
    }
  }
}
</style>

<style lang="less">
.spreadsheet-dataset-actions {
  min-width: 120px;

  .ed-dropdown-menu {
    padding: 4px;
  }

  .ed-dropdown-menu__item {
    min-width: 112px;
    height: 32px;
    padding: 5px 8px;
    border-radius: 4px;
    gap: 8px;

    .ed-icon {
      width: 16px;
      height: 16px;
      margin-right: 0;
      color: #646a73;

      svg {
        width: 16px;
        height: 16px;
      }
    }
  }
}
</style>
