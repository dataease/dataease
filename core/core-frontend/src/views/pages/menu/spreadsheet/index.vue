<script lang="tsx" setup>
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_intoItem_outlined from '@/assets/svg/icon_into-item_outlined.svg'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import dvNewFolder from '@/assets/svg/dv-new-folder.svg'
import icon_fileAdd_outlined from '@/assets/svg/icon_file-add_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import dvSortAsc from '@/assets/svg/dv-sort-asc.svg'
import dvSortDesc from '@/assets/svg/dv-sort-desc.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_spreadsheet from '@/assets/svg/icon_spreadsheet.svg'
import icon_pc_fullscreen from '@/assets/svg/icon_pc_fullscreen.svg'
import dvPreviewOuter from '@/assets/svg/dv-preview-outer.svg'
import dvCancelPublish from '@/assets/svg/icon_undo_outlined.svg'
import icon_shareLabel_outlined from '@/assets/svg/icon_share-label_outlined.svg'
import dvInfoSvg from '@/assets/svg/dv-info.svg'
import { useI18n } from '@/hooks/web/useI18n'
import {
  ref,
  reactive,
  shallowRef,
  computed,
  watch,
  onBeforeMount,
  onMounted,
  onBeforeUnmount,
  nextTick,
  unref
} from 'vue'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import {
  ElIcon,
  ElMessageBox,
  ElMessage,
  ElAside,
  ElScrollbar,
  ElDialog,
  ElInput
} from 'element-plus-secondary'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import router from '@/router'
import type { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import {
  deleteResource,
  move,
  findById,
  updateStatus,
  SpreadsheetPublishStatus
} from '@/views/menu/spreadsheet/api'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import treeSort from '@/utils/treeSortUtils'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { useCache } from '@/hooks/web/useCache'
import UniverSheet from '@/views/menu/spreadsheet/components/UniverSheet.vue'
import PluginRenderIndicator from '@/views/menu/spreadsheet/components/PluginRenderIndicator.vue'
import SpreadsheetDetailInfo from '@/views/menu/spreadsheet/components/SpreadsheetDetailInfo.vue'
import SpreadsheetFolderDialog from '@/views/menu/spreadsheet/components/SpreadsheetFolderDialog.vue'
import {
  createDefaultWorkbookData,
  parseSheetData
} from '@/views/menu/spreadsheet/utils/univerConfig'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { IWorkbookData } from '@univerjs/core'
import { treeWithAuth } from '@/views/menu/spreadsheet/api/auth'

const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()
const { wsCache } = useCache()

const appStore = useAppStoreWithOut()
const localeStore = useLocaleStoreWithOut()
const rootManage = ref(false)
const disabledMove = ref(true)
type SpreadsheetTreeNode = BusiTreeNode & {
  status?: SpreadsheetPublishStatus
}
const state = reactive({
  spreadsheetTree: [] as SpreadsheetTreeNode[],
  curSortType: 'time_desc'
})

const mounted = ref(false)
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)
const currentLang = computed(() => localeStore.getCurrentLocale.lang)

const originResourceTree = shallowRef([])
const previewWorkbookData = shallowRef<Partial<IWorkbookData>>(createDefaultWorkbookData())
const previewDataKey = ref(0)
const previewLoading = ref(false)
const isPreviewFocus = ref(false)
const isFullscreenPreview = ref(false)
// 普通列表需扣除顶部导航，进入预览后直接使用完整视口高度。
const spreadsheetHeightOffset = computed(() => (isPreviewFocus.value ? '0px' : '56px'))
const previewShellRef = ref<HTMLElement>()
let previewFocusBeforeFullscreen = false

const handleSortTypeChange = (sortType: string) => {
  state.spreadsheetTree = treeSort(originResourceTree.value, sortType)
  state.curSortType = sortType
  wsCache.set('TreeSort-spreadsheet', state.curSortType)
}

const sortTypeChange = (sortType: string) => {
  state.spreadsheetTree = treeSort(originResourceTree.value, sortType)
  state.curSortType = sortType
}

const selectedNodeInfo = reactive({
  id: null as number | null,
  name: '',
  nodeType: '',
  weight: 0,
  creator: '',
  createTime: undefined as number | undefined,
  updater: '',
  updateTime: undefined as number | undefined,
  createBy: '',
  status: SpreadsheetPublishStatus.Unpublished
})

const dtLoading = ref(false)
const { width, node } = useMoveLine('SPREADSHEET')

const getSpreadsheetStatus = (data: SpreadsheetTreeNode) =>
  data.status ?? data.extraFlag1 ?? SpreadsheetPublishStatus.Unpublished

const isSpreadsheetUnpublished = (data: SpreadsheetTreeNode) =>
  !!data.leaf && getSpreadsheetStatus(data) === SpreadsheetPublishStatus.Unpublished

const getSpreadsheetTree = () => {
  dtLoading.value = true
  let curSortType = sortList[Number(wsCache.get('TreeSort-backend')) ?? 1].value
  curSortType = wsCache.get('TreeSort-spreadsheet') ?? curSortType
  const request = { busiFlag: 'spreadsheet' } as BusiTreeRequest
  return treeWithAuth(request)
    .then(res => {
      const nodeData = (res?.treeNodes as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        rootManage.value = nodeData[0]['weight'] >= 7
        state.spreadsheetTree = nodeData[0]['children'] || []
        originResourceTree.value = JSON.parse(JSON.stringify(unref(state.spreadsheetTree)))
        sortTypeChange(curSortType)
        return
      }
      state.spreadsheetTree = nodeData
      originResourceTree.value = JSON.parse(JSON.stringify(unref(state.spreadsheetTree)))
      sortTypeChange(curSortType)
    })
    .finally(() => {
      dtLoading.value = false
      mounted.value = true
      nextTick(() => {
        if (!!nickName.value.length) {
          spreadsheetListTree.value.filter(nickName.value)
        }
      })
    })
}

const defaultProps = {
  children: 'children',
  label: 'name',
  // 文件夹没有发布状态，仅禁用未发布的电子表格叶子节点。
  disabled: (data: SpreadsheetTreeNode) => !data.weight || isSpreadsheetUnpublished(data)
}

const sortList = [
  {
    name: t('visualization.time_asc'),
    value: 'time_asc'
  },
  {
    name: t('visualization.time_desc'),
    value: 'time_desc',
    divided: true
  },
  {
    name: t('visualization.name_asc'),
    value: 'name_asc'
  },
  {
    name: t('visualization.name_desc'),
    value: 'name_desc'
  }
]

const loadInit = () => {
  const historyTreeSort = wsCache.get('TreeSort-spreadsheet')
  if (historyTreeSort) {
    state.curSortType = historyTreeSort
  }
}

const sortTypeTip = computed(() => {
  return sortList.find(ele => ele.value === state.curSortType)?.name || ''
})

const spreadsheetListTree = ref()
const spreadsheetFolderDialogRef = ref<InstanceType<typeof SpreadsheetFolderDialog>>()
const nickName = ref('')

watch(nickName, (val: string) => {
  spreadsheetListTree.value.filter(val)
})

const sideTreeStatus = ref(true)
const changeSideTreeStatus = (val: boolean) => {
  sideTreeStatus.value = val
}

const filterNode = (value: string, data: BusiTreeNode) => {
  if (!value) return true
  return data.name?.toLowerCase().includes(value.toLowerCase())
}

const mouseenter = () => {
  appStore.setArrowSide(true)
}

const mouseleave = () => {
  appStore.setArrowSide(false)
}

const cancelPublishMenuItem = {
  label: t('spreadsheet.unpublish'),
  svgName: dvCancelPublish,
  command: 'cancelPublish'
}

const menuList = [
  {
    label: t('visualization.move_to'),
    svgName: icon_intoItem_outlined,
    command: 'move'
  },
  {
    label: t('visualization.rename'),
    svgName: icon_rename_outlined,
    command: 'rename'
  },
  {
    label: t('common.delete'),
    divided: true,
    svgName: icon_deleteTrash_outlined,
    command: 'delete'
  }
]

const getMenuList = (data?: SpreadsheetTreeNode) => {
  const filteredMenu = menuList.filter(item => {
    if (disabledMove.value && item.command === 'move') return false
    if (data?.orgRoot && item.command === 'move') return false
    return true
  })
  const status = data ? getSpreadsheetStatus(data) : SpreadsheetPublishStatus.Unpublished
  const canCancelPublish =
    status === SpreadsheetPublishStatus.Published ||
    status === SpreadsheetPublishStatus.SavedUnpublished
  if (data?.leaf && canCancelPublish) {
    return [cancelPublishMenuItem, ...filteredMenu]
  }
  return filteredMenu
}

const spreadsheetTypeList = computed(() => {
  return [
    {
      label: t('spreadsheet.new_spreadsheet'),
      svgName: icon_spreadsheet,
      command: 'spreadsheet'
    },
    {
      label: t('spreadsheet.new_folder'),
      divided: true,
      svgName: dvFolder,
      command: 'folder'
    }
  ]
})

const expandedKey = ref([])

const nodeExpand = (data: BusiTreeNode) => {
  if (data.id) {
    expandedKey.value.push(data.id)
  }
}

const nodeCollapse = (data: BusiTreeNode) => {
  if (data.id) {
    expandedKey.value.splice(expandedKey.value.indexOf(data.id), 1)
  }
}

const handleNodeClick = (data: SpreadsheetTreeNode, node: { disabled?: boolean }) => {
  if (node.disabled) {
    nextTick(() => spreadsheetListTree.value?.setCurrentKey(null))
    return
  }
  if (!data.leaf) {
    spreadsheetListTree.value.setCurrentKey(null)
    return
  }
  loadSpreadsheetPreview(data.id as number, data.weight)
}

let requestId
const spreadsheetRef = ref()
const loadSpreadsheetPreview = async (id: number, weight?: number) => {
  previewLoading.value = true

  if (requestId) {
    clearTimeout(requestId)
  }
  requestId = setTimeout(async () => {
    try {
      const data = await findById(id)
      const parsedWorkbookData = parseSheetData(data.sheetData) ?? createDefaultWorkbookData()

      previewWorkbookData.value = parsedWorkbookData
      selectedNodeInfo.id = data.id as number
      selectedNodeInfo.weight = weight ?? 0
      selectedNodeInfo.name = data.name || selectedNodeInfo.name
      selectedNodeInfo.creator = data.creator || data.createBy || ''
      selectedNodeInfo.createBy = data.createBy || ''
      selectedNodeInfo.createTime = data.createTime
      selectedNodeInfo.updater = data.updater || data.updateBy || ''
      selectedNodeInfo.updateTime = data.updateTime
      selectedNodeInfo.status = data.status ?? SpreadsheetPublishStatus.Unpublished
      if (spreadsheetRef.value) {
        spreadsheetRef.value.loadSheetData(previewWorkbookData.value)
      }
    } catch (e) {
      console.error(e)
      ElMessage.error(t('spreadsheet.load_error'))
    } finally {
      previewLoading.value = false
    }
  }, 300)
}

const createNewSpreadsheet = (data?: BusiTreeNode) => {
  const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
  const routeObj = {
    path: '/spreadsheet-editor',
    query: { opt: 'create', pid: data?.id || 0 }
  }
  if (openType === '_self') {
    router.push(routeObj)
  } else {
    const routeData = router.resolve(routeObj)
    window.open(routeData.href, openType)
  }
}

const handleEdit = (id: number) => {
  const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
  const routeObj = {
    path: '/spreadsheet-editor',
    query: { id, opt: 'edit' }
  }
  if (openType === '_self') {
    router.push(routeObj)
  } else {
    const routeData = router.resolve(routeObj)
    window.open(routeData.href, openType)
  }
}

const resizePreview = async () => {
  await nextTick()
  spreadsheetRef.value?.resize()
}

const handlePreview = () => {
  isPreviewFocus.value = true
  void resizePreview()
}

const handleFullscreen = async () => {
  previewFocusBeforeFullscreen = isPreviewFocus.value
  isPreviewFocus.value = true
  await resizePreview()
  try {
    await previewShellRef.value?.requestFullscreen()
  } catch (error) {
    isPreviewFocus.value = previewFocusBeforeFullscreen
    await resizePreview()
    console.error('[Spreadsheet] Failed to enter fullscreen preview:', error)
    ElMessage.warning('无法进入全屏预览')
  }
}

const exitFullscreen = async () => {
  if (document.fullscreenElement === previewShellRef.value) {
    await document.exitFullscreen()
  }
}

const exitPreview = async () => {
  await exitFullscreen()
  isPreviewFocus.value = false
  await resizePreview()
}

const resetPreview = async () => {
  if (requestId) {
    clearTimeout(requestId)
  }
  await exitFullscreen()
  isPreviewFocus.value = false
  previewFocusBeforeFullscreen = false
  selectedNodeInfo.id = null
  selectedNodeInfo.name = ''
  selectedNodeInfo.nodeType = ''
  selectedNodeInfo.weight = 0
  selectedNodeInfo.creator = ''
  selectedNodeInfo.createTime = undefined
  selectedNodeInfo.updater = ''
  selectedNodeInfo.updateTime = undefined
  selectedNodeInfo.createBy = ''
  selectedNodeInfo.status = SpreadsheetPublishStatus.Unpublished
  previewWorkbookData.value = createDefaultWorkbookData()
  spreadsheetListTree.value?.setCurrentKey(null)
}

const handleFullscreenChange = () => {
  const wasFullscreenPreview = isFullscreenPreview.value
  const fullscreenPreview = document.fullscreenElement === previewShellRef.value
  isFullscreenPreview.value = fullscreenPreview

  if (wasFullscreenPreview && !fullscreenPreview) {
    // 退出浏览器全屏时恢复入口状态，避免直接全屏退出后停留在铺满页面的普通预览。
    isPreviewFocus.value = previewFocusBeforeFullscreen
  }
  void resizePreview()
}

const handleShare = () => {
  //
}

const operation = async (cmd: string, data: SpreadsheetTreeNode, nodeType: 'folder' | 'sheet') => {
  if (cmd === 'cancelPublish') {
    try {
      await ElMessageBox.confirm(t('spreadsheet.cancel_publish_confirm'), {
        confirmButtonText: t('commons.confirm'),
        cancelButtonText: t('commons.cancel'),
        type: 'warning',
        autofocus: false,
        showClose: false
      })
    } catch {
      return
    }
    try {
      const result = await updateStatus({
        id: data.id,
        status: SpreadsheetPublishStatus.Unpublished
      })
      data.status = result.status
      data.extraFlag1 = result.status ?? SpreadsheetPublishStatus.Unpublished
      if (selectedNodeInfo.id === data.id) {
        await resetPreview()
      }
      await getSpreadsheetTree()
      ElMessage.success(t('spreadsheet.cancel_publish_success'))
    } catch (error) {
      console.error(error)
      ElMessage.error(t('spreadsheet.cancel_publish_error'))
    }
    return
  } else if (cmd === 'delete') {
    let options = {
      confirmButtonText: t('commons.confirm'),
      cancelButtonText: t('commons.cancel'),
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false,
      tip: ''
    }

    if (data?.orgRoot) {
      options.tip = t('common.org_root_delete_tips', [data.name])
    } else if (!!data.children?.length) {
      options.tip = t('spreadsheet.delete_folder_confirm')
    } else {
      delete options.tip
    }

    try {
      await ElMessageBox.confirm(t('spreadsheet.delete_confirm'), options)
      await deleteResource({ id: data.id as number, rootOrgNode: !!data.orgRoot })
      if (selectedNodeInfo.id === data.id) {
        await resetPreview()
      }
      getSpreadsheetTree()
      ElMessage.success(t('common.delete_success'))
    } catch (e) {
      // Cancelled or error
    }
  } else if (cmd === 'rename') {
    spreadsheetFolderDialogRef.value?.rename(data, nodeType)
  } else if (cmd === 'move') {
    openMoveDialog(data)
  }
}

const handleSpreadsheetTree = (cmd: string, data?: BusiTreeNode) => {
  if (cmd === 'spreadsheet') {
    createNewSpreadsheet(data)
  }
  if (cmd === 'folder') {
    spreadsheetFolderDialogRef.value?.open(data)
  }
}

// Dialog state
const moveDialogVisible = ref(false)
const moveForm = reactive({
  pid: 0,
  id: 0
})
const moveTreeData = shallowRef<BusiTreeNode[]>([])
const moveTreeRef = ref()

const openMoveDialog = (data: BusiTreeNode) => {
  moveForm.pid = 0
  moveForm.id = data.id as number
  // Get folder tree for move target selection
  moveTreeData.value = getFolderTree(state.spreadsheetTree)
  moveDialogVisible.value = true
}

const getFolderTree = (tree: BusiTreeNode[]): BusiTreeNode[] => {
  return tree
    .filter(node => !node.leaf)
    .map(node => ({
      ...node,
      children: node.children ? getFolderTree(node.children) : undefined
    }))
}

const submitMoveDialog = async () => {
  try {
    await move({
      id: moveForm.id,
      pid: moveForm.pid || '0'
    })
    ElMessage.success(t('system.update_successful'))
    moveDialogVisible.value = false
    getSpreadsheetTree()
  } catch (e) {
    console.error(e)
  }
}

const moveFilterNode = (value: string, data: BusiTreeNode) => {
  if (!value) return true
  return data.name?.toLowerCase().includes(value.toLowerCase())
}

const handleMoveNodeClick = (data: BusiTreeNode) => {
  if (!data.leaf) {
    moveForm.pid = data.id as number
  }
}

onBeforeMount(() => {
  loadInit()
  getSpreadsheetTree()
})

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onBeforeUnmount(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<template>
  <div
    class="spreadsheet-manage"
    :class="{ 'de-100vh': isIframe, 'preview-focus': isPreviewFocus }"
    v-loading="dtLoading"
  >
    <ArrowSide
      v-if="!isPreviewFocus"
      :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
      @change-side-tree-status="changeSideTreeStatus"
      :isInside="!sideTreeStatus"
    ></ArrowSide>
    <el-aside
      class="resource-area"
      @mouseenter="mouseenter"
      @mouseleave="mouseleave"
      :class="{ retract: !sideTreeStatus }"
      ref="node"
      :style="{ width: width + 'px' }"
    >
      <ArrowSide
        :isInside="!sideTreeStatus"
        :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
        @change-side-tree-status="changeSideTreeStatus"
      ></ArrowSide>
      <div class="resource-tree">
        <div class="tree-header">
          <div class="icon-methods">
            <span class="title"> {{ t('spreadsheet.title') }} </span>
            <div v-if="rootManage" class="flex-align-center">
              <el-tooltip
                class="box-item"
                effect="dark"
                offset="14"
                popper-class="new-folder_tip"
                :content="t('spreadsheet.new_folder')"
                arrow-offset="10"
                placement="top"
              >
                <el-icon
                  class="custom-icon btn"
                  style="margin-right: 20px"
                  @click="handleSpreadsheetTree('folder')"
                >
                  <Icon name="dv-new-folder"><dvNewFolder class="svg-icon" /></Icon>
                </el-icon>
              </el-tooltip>
              <el-tooltip
                class="box-item"
                effect="dark"
                popper-class="new-folder_tip"
                offset="14"
                arrow-offset="10"
                :content="t('spreadsheet.new_spreadsheet')"
                placement="top"
              >
                <el-icon class="custom-icon btn" @click="createNewSpreadsheet()">
                  <Icon name="icon_file-add_outlined"
                    ><icon_fileAdd_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
          </div>
          <el-input
            :placeholder="t('commons.search')"
            v-model="nickName"
            clearable
            class="search-bar"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
          <el-dropdown @command="handleSortTypeChange" trigger="click">
            <el-icon class="filter-icon-span">
              <el-tooltip
                v-if="state.curSortType.includes('asc')"
                :offset="16"
                effect="dark"
                :content="sortTypeTip"
                placement="top"
              >
                <!-- Tooltip 会给触发节点注入 aria 属性，需要使用原生节点承接，避免传给以 slot 为根节点的 Icon。 -->
                <span class="opt-icon">
                  <Icon name="dv-sort-asc"><dvSortAsc class="svg-icon" /></Icon>
                </span>
              </el-tooltip>
              <el-tooltip v-else :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <span class="opt-icon">
                  <Icon name="dv-sort-desc"><dvSortDesc class="svg-icon" /></Icon>
                </span>
              </el-tooltip>
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu style="width: 246px">
                <template :key="ele.value" v-for="ele in sortList">
                  <el-dropdown-item
                    class="ed-select-dropdown__item"
                    :class="ele.value === state.curSortType && 'selected'"
                    :command="ele.value"
                  >
                    {{ ele.name }}
                  </el-dropdown-item>
                  <li v-if="ele.divided" class="ed-dropdown-menu__item--divided"></li>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <el-scrollbar class="custom-tree">
          <el-tree
            menu
            ref="spreadsheetListTree"
            node-key="id"
            :data="state.spreadsheetTree"
            :filter-node-method="filterNode"
            expand-on-click-node
            highlight-current
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            :default-expanded-keys="expandedKey"
            :props="defaultProps"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span
                class="custom-tree-node"
                :class="{
                  'node-disabled-custom': !data.weight || isSpreadsheetUnpublished(data)
                }"
              >
                <el-icon v-if="!data.leaf" style="font-size: 18px">
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>
                <el-icon v-if="data.leaf" style="font-size: 18px">
                  <Icon name="icon_spreadsheet"><icon_spreadsheet class="svg-icon" /></Icon>
                </el-icon>
                <el-tooltip
                  v-if="!data.weight"
                  effect="dark"
                  :content="t('common.no_permission_node')"
                  placement="top-start"
                >
                  <span :title="node.label" class="label-tooltip ellipsis">{{ node.label }}</span>
                </el-tooltip>
                <el-tooltip
                  v-else-if="isSpreadsheetUnpublished(data)"
                  effect="dark"
                  :content="t('visualization.publish_tips1')"
                  placement="top-start"
                >
                  <span :title="node.label" class="label-tooltip ellipsis">{{ node.label }}</span>
                </el-tooltip>
                <span v-else :title="node.label" class="label-tooltip ellipsis">{{
                  node.label
                }}</span>
                <div class="icon-more" v-if="data.weight >= 7">
                  <handle-more
                    @handle-command="cmd => handleSpreadsheetTree(cmd, data)"
                    :menu-list="spreadsheetTypeList"
                    :icon-name="icon_add_outlined"
                    placement="bottom-start"
                    v-if="!data.leaf"
                  ></handle-more>
                  <el-icon v-else class="hover-icon" @click.stop="handleEdit(data.id)">
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </el-icon>
                  <handle-more
                    @handle-command="cmd => operation(cmd, data, data.leaf ? 'sheet' : 'folder')"
                    :menu-list="getMenuList(data)"
                  ></handle-more>
                </div>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </el-aside>

    <div
      class="spreadsheet-content"
      :class="{
        auto: isIframe || isDataEaseBi
      }"
    >
      <template v-if="!state.spreadsheetTree.length && mounted">
        <empty-background :description="t('spreadsheet.no_data')" img-type="none">
          <el-button v-if="rootManage" @click="createNewSpreadsheet()" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
            </template>
            {{ t('commons.create') + t('spreadsheet.title') }}</el-button
          >
        </empty-background>
      </template>
      <template v-else-if="!!selectedNodeInfo.id">
        <div class="spreadsheet-info">
          <div class="info-method">
            <div class="info-left">
              <div class="spreadsheet-title">
                <span :title="selectedNodeInfo.name" class="spreadsheet-name ellipsis">{{
                  selectedNodeInfo.name
                }}</span>
                <span
                  v-if="selectedNodeInfo.status === SpreadsheetPublishStatus.SavedUnpublished"
                  class="spreadsheet-have-update"
                >
                  {{ t('visualization.publish_update_tips') }}
                </span>
              </div>
              <el-divider direction="vertical" class="info-divider" />
              <span class="creator-text">
                {{ t('visualization.creator') }}:{{ selectedNodeInfo.creator || '-' }}
              </span>
              <el-popover show-arrow :offset="8" placement="bottom" width="320" trigger="hover">
                <template #reference>
                  <el-icon class="info-tips">
                    <Icon name="dv-info"><dvInfoSvg class="svg-icon" /></Icon>
                  </el-icon>
                </template>
                <SpreadsheetDetailInfo :info="selectedNodeInfo" />
              </el-popover>
            </div>
            <div class="right-btn">
              <el-button @click="handleFullscreen">
                <template #icon>
                  <Icon name="icon_pc_fullscreen"><icon_pc_fullscreen class="svg-icon" /></Icon>
                </template>
                {{ t('visualization.fullscreen_preview') }}
              </el-button>
              <el-button @click="handlePreview">
                <template #icon>
                  <Icon name="dv-preview-outer"><dvPreviewOuter class="svg-icon" /></Icon>
                </template>
                {{ t('visualization.preview') }}
              </el-button>
              <!-- <el-button
                v-if="selectedNodeInfo.weight >= 7"
                @click="handleShare"
              >
                <template #icon>
                  <Icon name="icon_share-label_outlined"
                    ><icon_shareLabel_outlined class="svg-icon"
                  /></Icon>
                </template>
                {{ t("spreadsheet.share") }}
              </el-button> -->
              <el-button
                v-if="selectedNodeInfo.weight >= 7"
                type="primary"
                @click="handleEdit(selectedNodeInfo.id)"
              >
                <template #icon>
                  <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                </template>
                {{ t('visualization.edit') }}
              </el-button>
            </div>
          </div>
        </div>
        <div ref="previewShellRef" class="spreadsheet-preview-area" v-loading="previewLoading">
          <div v-if="isPreviewFocus && !isFullscreenPreview" class="preview-mode-actions">
            <el-button @click="handleFullscreen">
              {{ t('visualization.fullscreen_preview') }}
            </el-button>
            <el-button @click="exitPreview">关闭预览</el-button>
          </div>
          <div class="preview-sheet-wrapper">
            <UniverSheet
              ref="spreadsheetRef"
              :model-value="previewWorkbookData"
              :data-key="previewDataKey"
              :locale="currentLang"
              mode="preview"
            />
            <PluginRenderIndicator />
          </div>
        </div>
      </template>
      <template v-else-if="mounted">
        <empty-background :description="t('spreadsheet.preview_select_tips')" img-type="select" />
      </template>
    </div>

    <SpreadsheetFolderDialog ref="spreadsheetFolderDialogRef" @success="getSpreadsheetTree" />

    <!-- Dialog for move -->
    <el-dialog
      v-model="moveDialogVisible"
      :title="t('visualization.move_to')"
      width="420px"
      height="204px"
    >
      <el-tree
        ref="moveTreeRef"
        :data="moveTreeData"
        :props="defaultProps"
        node-key="id"
        highlight-current
        :filter-node-method="moveFilterNode"
        @node-click="handleMoveNodeClick"
      >
        <template #default="{ data }">
          <span class="custom-tree-node">
            <el-icon style="font-size: 18px">
              <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
            </el-icon>
            <span class="label-tooltip">{{ data.name }}</span>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="moveDialogVisible = false">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" @click="submitMoveDialog">
            {{ t('common.sure') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.filter-icon-span {
  border: 1px solid #d9dcdf;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #1f2329;
  padding: 8px;
  margin-left: 8px;
  font-size: 16px;
  cursor: pointer;

  .opt-icon:focus {
    outline: none !important;
  }
  &:hover {
    background: #f5f6f7;
  }

  &:active {
    background: #eff0f1;
  }
}
.custom-tree {
  height: calc(100vh - 172px);
  padding: 0 8px;
}
.spreadsheet-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  position: relative;

  &.preview-focus {
    height: calc(100vh - v-bind(spreadsheetHeightOffset));

    .resource-area,
    .spreadsheet-info {
      display: none;
    }

    .spreadsheet-content,
    .spreadsheet-preview-area {
      height: 100%;
    }

    .preview-sheet-wrapper {
      height: calc(100% - 56px) !important;
      min-height: 0 !important;
      margin-top: 56px;
    }
  }

  &.de-100vh {
    height: 100vh;
    .custom-tree {
      height: calc(100vh - 122px);
    }
  }

  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    border-right: 1px solid #d7d7d7;
    overflow: visible;
    &.retract {
      display: none;
    }

    .resource-tree {
      padding: 16px 0 0;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;

      .tree-header {
        padding: 0 16px;
      }

      .icon-methods {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        font-size: 20px;
        font-weight: 500;
        color: var(--TextPrimary, #1f2329);
        padding-bottom: 16px;

        .title {
          margin-right: auto;
          font-size: 16px;
          font-style: normal;
          font-weight: 500;
          line-height: 24px;
        }

        .custom-icon {
          position: relative;
          &.btn {
            color: var(--ed-color-primary);
          }

          &:hover {
            cursor: pointer;
            &::after {
              content: '';
              background-color: var(--ed-color-primary-1a, #3370ff1a);
              width: 28px;
              height: 28px;
              position: absolute;
              top: 50%;
              left: 50%;
              border-radius: 4px;
              transform: translate(-50%, -50%);
            }
          }
        }
      }

      .search-bar {
        padding-bottom: 10px;
        width: calc(100% - 40px);
      }
    }
  }

  .spreadsheet-content {
    height: calc(100vh - v-bind(spreadsheetHeightOffset));
    overflow: auto;
    position: relative;
    background: #f5f6f7;

    &.auto {
      height: auto;
    }
  }

  .spreadsheet-content {
    flex: 1;
    position: relative;

    .spreadsheet-info {
      background: #fff;
      padding: 0 24px;
      padding-top: 12px;
      height: 56px;
      .info-method {
        height: 32px;
        width: 100%;
        display: flex;
        align-items: center;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 16px;
        font-weight: 500;

        .info-left {
          display: flex;
          align-items: center;
          min-width: 0;
          gap: 12px;
        }

        .spreadsheet-title {
          display: flex;
          align-items: center;
          min-width: 0;
          gap: 8px;

          .spreadsheet-name {
            max-width: 200px;
          }

          .spreadsheet-have-update {
            flex-shrink: 0;
            padding: 0 4px;
            background-color: rgba(52, 199, 36, 0.2);
            color: rgba(44, 169, 31, 1);
            font-size: 12px;
            font-weight: 400;
            line-height: 20px;
          }
        }

        .creator-text {
          color: var(--de-text-color-secondary, #646a73);
          font-size: 14px;
          font-weight: 400;
          white-space: nowrap;
        }

        .info-divider {
          margin: 0;
          height: 14px;
          border-color: rgba(31, 35, 41, 0.15);
        }

        .info-tips {
          color: #646a73;
          cursor: pointer;
          font-size: 14px;
          flex-shrink: 0;
        }

        .right-btn {
          margin-left: auto;
          display: flex;
        }
      }
    }

    .spreadsheet-preview-area {
      position: relative;
      background: #fff;
      border-radius: 4px;
      height: calc(100% - 56px);
      overflow: hidden;

      &:fullscreen {
        width: 100%;
        height: 100%;
        border-radius: 0;

        .preview-sheet-wrapper {
          height: 100% !important;
          margin-top: 0;
        }
      }

      .preview-mode-actions {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 56px;
        padding: 0 16px;
        box-sizing: border-box;
        z-index: 300;
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 8px;
        border-bottom: 1px solid #e5e6e8;
        background: #fff;
      }

      .preview-sheet-wrapper {
        position: relative;
        width: 100%;
        height: 100%;
        min-height: 520px;
      }
    }
  }
}

.custom-tree-node {
  width: calc(100% - 30px);
  display: flex;
  align-items: center;
  box-sizing: content-box;
  padding-right: 4px;

  .label-tooltip {
    width: 100%;
    margin-left: 8.75px;
  }

  .icon-more {
    margin-left: auto;
    display: none;
  }

  &:hover {
    .label-tooltip {
      width: calc(100% - 78px);
    }

    .icon-more {
      display: inline-flex;
    }
  }
}

.node-disabled-custom {
  color: rgba(187, 191, 196, 1);
  cursor: not-allowed;
}
</style>
