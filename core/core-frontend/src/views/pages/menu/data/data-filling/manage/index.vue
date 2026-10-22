<script lang="tsx" setup>
import icon_intoItem_outlined from '@/assets/svg/icon_into-item_outlined.svg'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_download_outlined from '@/assets/svg/icon_download_outlined.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import dvNewFolder from '@/assets/svg/dv-new-folder.svg'
import icon_fileAdd_outlined from '@/assets/svg/icon_file-add_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_file_doc_colorful from '@/assets/svg/icon_file-doc_colorful.svg'
import dvSortAsc from '@/assets/svg/dv-sort-asc.svg'
import dvSortDesc from '@/assets/svg/dv-sort-desc.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_upload_outlined from '@/assets/svg/icon_upload_outlined.svg'
import icon_describe_outlined from '@/assets/svg/icon_describe_outlined.svg'
import icon_copy_filled from '@/assets/svg/icon_copy_filled.svg'
import { computed, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, unref, watch } from 'vue'
import {
  ElButton,
  ElDrawer,
  ElIcon,
  ElMessage,
  ElMessageBox,
  ElMessageBoxOptions,
  ElScrollbar,
  TabPaneName
} from 'element-plus-secondary'
import CreatDsGroup from '@/views/menu/data/data-filling/manage/CreatDsGroup.vue'
import RowDataForm from '@/views/menu/data/data-filling/manage/form/RowDataForm.vue'
import FilterSearchDrawer from '@/views/menu/data/data-filling/manage/FilterSearchDrawer.vue'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import {
  batchDeleteRowData,
  ColumnItem,
  deleteById,
  deleteRowData,
  DfFormItem,
  DfFormSetting,
  formatDate,
  getDataFilling,
  innerExport,
  searchTable,
  Tree,
  truncateRowData
} from '@/views/menu/data/data-filling/data-filling'
import { useI18n } from '@/hooks/web/useI18n'
import router from '@/router'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import type { BusiTreeNode } from '@/models/tree/TreeNode'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { cloneDeep, filter, forEach, forIn, includes, join, map, some } from 'lodash-es'
import treeSort from '@/utils/treeSortUtils'
import { useCache } from '@/hooks/web/useCache'
import InfoDetail from '@/views/menu/data/data-filling/manage/InfoDetail.vue'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import TaskGrid from '@/views/menu/data/data-filling/manage/TaskGrid.vue'
import LogGrid from '@/views/menu/data/data-filling/manage/LogGrid.vue'
import ExcelBatchUpload from '@/views/menu/data/data-filling/manage/ExcelBatchUpload.vue'
import {
  initModuleAuth,
  InnerInteractive
} from '@/views/menu/data/data-filling/manage/DataFillAuth'
import { RefreshLeft } from '@element-plus/icons-vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useEmbedded } from '@/store/modules/embedded'
import iconFilter from '@/assets/svg/icon-filter.svg'
import dayjs from 'dayjs'

const { wsCache } = useCache()
const { t } = useI18n()
const appStore = useAppStoreWithOut()
const state = reactive({
  datasourceTree: [] as BusiTreeNode[],
  dfTableData: [],
  multipleSelection: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
    key: ''
  },
  curSortType: 'time_desc',
  filterTable: []
})
const moduleAuthData = ref<InnerInteractive>({
  rootManage: false,
  anyManage: false,
  treeNodes: [],
  leafNodeCount: 0,
  menuAuth: true
})
const emptyDesc = ref('')
const imgType = ref('noneWhite')

const embeddedStore = useEmbedded()
const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)
const embeddedSyncExport = computed(() => wsCache.get('embeddedExportMode-backend') !== 'async')

const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)
const datasourceManageRef = ref<HTMLElement | null>(null)
const datasourceContentHeight = ref<number | undefined>()
let datasourceManageResizeObserver: ResizeObserver | undefined

const updateDatasourceContentHeight = () => {
  if (!isIframe.value && !isDataEaseBi.value) {
    datasourceContentHeight.value = undefined
    return
  }

  const containerHeight = datasourceManageRef.value?.clientHeight
  if (!containerHeight) {
    return
  }

  datasourceContentHeight.value = Math.max(containerHeight, 0)
}

const datasourceContentStyle = computed(() => {
  if (!isIframe.value && !isDataEaseBi.value) {
    return undefined
  }

  if (typeof datasourceContentHeight.value !== 'number') {
    return undefined
  }

  return {
    height: `${datasourceContentHeight.value}px`
  }
})

const customTreeStyle = computed(() => {
  if (!isIframe.value && !isDataEaseBi.value) {
    return undefined
  }

  if (typeof datasourceContentHeight.value !== 'number') {
    return undefined
  }

  return {
    height: `${datasourceContentHeight.value - 105}px`
  }
})

const showDownloadDrawer = ref(false)

const { width, node } = useMoveLine('DATA-FILLING')

const rootManage = ref(false)
const disabledMove = ref(true)
const dsName = ref('')
const rawDatasourceList = ref([])

const menuList = [
  {
    label: t('data_fill.move_to'),
    svgName: icon_intoItem_outlined,
    command: 'move'
  },
  {
    label: t('data_fill.rename'),
    svgName: icon_rename_outlined,
    command: 'rename'
  },
  {
    label: t('data_fill.delete'),
    divided: true,
    svgName: icon_deleteTrash_outlined,
    command: 'delete'
  }
]

const timestampFormatDate = (timestamp, showMs?: boolean) => {
  if (!timestamp || timestamp === -1) {
    return '-'
  }

  const date = new Date(timestamp)

  const y = date.getFullYear()

  let MM = date.getMonth() + 1
  MM = (MM < 10 ? '0' + MM : MM) as number

  let d = date.getDate()
  d = (d < 10 ? '0' + d : d) as number

  let h = date.getHours()
  h = (h < 10 ? '0' + h : h) as number

  let m = date.getMinutes()
  m = (m < 10 ? '0' + m : m) as number

  let s = date.getSeconds()
  s = (s < 10 ? '0' + s : s) as number

  let format = y + '-' + MM + '-' + d + ' ' + h + ':' + m + ':' + s

  if (showMs === true) {
    const ms = date.getMilliseconds()
    format += ':' + ms
  }

  return format
}

const addOptionTypeList = computed(() => {
  return [
    {
      label: t('data_fill.create_form'),
      svgName: icon_dataset,
      command: 'data-filling'
    },
    {
      label: t('data_fill.create_folder'),
      divided: true,
      svgName: dvFolder,
      command: 'folder'
    }
  ]
})

let originResourceTree = []
const handleSortTypeChange = sortType => {
  state.datasourceTree = treeSort(originResourceTree, sortType)
  state.curSortType = sortType
  wsCache.set('TreeSort-dataFillingForm', state.curSortType)
}

const sortTypeChange = sortType => {
  state.datasourceTree = treeSort(originResourceTree, sortType)
  state.curSortType = sortType
}

let listScrollTop = 0
const handleScroll = val => {
  listScrollTop = val.scrollTop
}

const scrollbarRef = ref()

const getDsIconName = data => {
  if (!data.leaf) return dvFolder
  return icon_file_doc_colorful
}

const selectedItemId = ref<string | undefined>()

const selectedItem = ref<DfFormSetting | undefined>()

const dsLoading = ref(false)
const mounted = ref(false)

const listDf = () => {
  rawDatasourceList.value = []
  dsLoading.value = true
  initModuleAuth({ busiFlag: 'data_filling' })
    .then(res => {
      moduleAuthData.value = res

      rootManage.value = moduleAuthData.value.rootManage
      const nodeData = (moduleAuthData.value.treeNodes as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        state.datasourceTree = nodeData[0]['children'] || []
      } else {
        state.datasourceTree = moduleAuthData.value.treeNodes
      }
      originResourceTree = cloneDeep(unref(state.datasourceTree))
      let curSortType = sortList[Number(wsCache.get('TreeSort-backend')) ?? 1].value
      curSortType = wsCache.get('TreeSort-dataFillingForm') ?? curSortType
      sortTypeChange(curSortType)
    })
    .finally(() => {
      mounted.value = true
      dsLoading.value = false
      updateTreeExpand()
      const id = selectedItemId.value
      if (!!id) {
        const pid = selectedItem.value?.pid
        dfsDatasourceTree(state.datasourceTree, id)
        setTimeout(() => {
          if (pid) {
            const node = dsListTree.value.getNode(pid)
            if (node && !node.expanded) {
              node.expand()
            }
          }
          dsListTree.value.setCurrentKey(id, true)
        }, 100)
      }
    })
}

const dfsDatasourceTree = (ds, id) => {
  const has = findNode(ds, id)
  if (has) {
    selectedItemId.value = id
  } else {
    selectedItemId.value = undefined
  }
}

function findNode(ds, id) {
  return some(ds, ele => {
    if (ele.id === id) {
      handleNodeClick(ele)
      return true
    } else if (ele.children?.length > 0) {
      return findNode(ele.children, id)
    }
    return false
  })
}

const creatDsFolder = ref()
const rowDataFormRef = ref()

function onCloseToRefresh() {
  search(searchConditions.value)
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

const sortTypeTip = computed(() => {
  return sortList.find(ele => ele.value === state.curSortType)?.name
})

const mainLoading = ref(false)

const handleNodeClick = data => {
  try {
    if (router.currentRoute.value.query?.id) {
      //清理url路径上的id
      router.replace({ name: 'data-filling-manage' })
    }
  } catch (e) {}

  if (!data.leaf) {
    dsListTree.value.setCurrentKey(null)
    return
  }

  if (selectedItemId.value !== data.id) {
    state.dfTableData = []
    searchConditions.value = []
  }

  selectedItemId.value = data.id
  selectedItem.value = undefined

  mainLoading.value = true
  return getDataFilling(data.id)
    .then(res => {
      const {
        id,
        name,
        pid,
        datasource,
        datasourceName,
        tableName,
        forms,
        creator,
        updater,
        createTime,
        updateTime
      } = res

      const obj = {
        id,
        name,
        pid,
        datasource,
        datasourceName,
        tableName,
        forms: JSON.parse(forms),
        creator,
        updater,
        createTime,
        updateTime,
        weight: data.weight
      }

      selectedItem.value = obj

      state.paginationConfig.currentPage = 1

      activeName.value = 'dataPreview'
      state.multipleSelection = []
      handleClick('dataPreview')
    })
    .finally(() => {
      mainLoading.value = false
    })
}

const dsListTree = ref()
const expandedKey = ref([])
const dsListTreeShow = ref(true)
watch(dsName, (val: string) => {
  dsListTree.value.filter(val)
})
const updateTreeExpand = () => {
  dsListTreeShow.value = false
  nextTick(() => {
    dsListTreeShow.value = true
    nextTick(() => {
      scrollbarRef.value?.setScrollTop(listScrollTop)
    })
  })
}

const nodeExpand = data => {
  if (data.id) {
    expandedKey.value.push(data.id)
  }
}

const nodeCollapse = data => {
  if (data.id) {
    expandedKey.value.splice(expandedKey.value.indexOf(data.id), 1)
  }
}

const filterNode = (value: string, data: BusiTreeNode) => {
  if (!value) return true
  return data.name?.toLowerCase().includes(value.toLowerCase())
}

const handleEdit = async data => {
  await handleNodeClick(data)
  editForm(data.id)
}

const createForm = (data?: Tree) => {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setOpt('create')
    embeddedStore.setDfId(undefined)
    embeddedStore.setPid(data?.id)
    useEmitt().emitter.emit('changeCurrentComponent', 'DataFillingEditor')
    return
  }
  router.push({
    path: '/df-form',
    query: {
      pid: data?.id
    }
  })
}

function editForm(id) {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setOpt('edit')
    embeddedStore.setDfId(id)
    useEmitt().emitter.emit('changeCurrentComponent', 'DataFillingEditor')
    return
  }
  router.push({
    path: '/df-form',
    query: {
      id: id
    }
  })
}

function copyForm(id) {
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setOpt('copy')
    embeddedStore.setDfId(id)
    useEmitt().emitter.emit('changeCurrentComponent', 'DataFillingEditor')
    return
  }
  router.push({
    path: '/df-form',
    query: {
      copyId: id
    }
  })
}

const handleCopy = async data => {
  await handleNodeClick(data)
  copyForm(data.id)
}

const handleFormTree = (cmd: string, data?: Tree) => {
  if (cmd === 'data-filling') {
    createForm(data)
  }
  if (cmd === 'folder') {
    creatDsFolder.value.createInit(cmd, data || {})
  }
}
const operation = (cmd: string, data: Tree, nodeType: string) => {
  if (cmd === 'copy') {
    handleCopy(data)
  } else if (cmd === 'delete') {
    let options = {
      confirmButtonText: t('common.sure'),
      cancelButtonText: t('common.cancel'),
      confirmButtonType: 'danger',
      type: 'warning',
      tip: '',
      autofocus: false,
      showClose: false
    }
    if (data?.orgRoot) {
      options.tip = t('common.org_root_delete_tips', [data.name])
    } else if (!!data.children?.length) {
      options.tip = t('data_fill.delete_folder_hint')
    } else {
      delete options.tip
    }
    ElMessageBox.confirm(
      nodeType === 'folder'
        ? t('data_fill.confirm_delete_folder')
        : t('data_fill.confirm_delete_form'),
      options as ElMessageBoxOptions
    ).then(() => {
      deleteById({ id: data.id as string, rootOrgNode: !!data.orgRoot }).then(() => {
        listDf()
        ElMessage.success(t('dataset.delete_success'))
      })
    })
  } else {
    creatDsFolder.value.createInit(nodeType, data, cmd)
  }
}

const defaultProps = {
  children: 'children',
  label: 'name',
  disabled: data => !data.weight
}

const sortInit = () => {
  const historyTreeSort = wsCache.get('TreeSort-dataFillingForm')
  if (historyTreeSort) {
    state.curSortType = historyTreeSort
  }
}

onMounted(async () => {
  sortInit()
  listDf()
  selectedItemId.value = router.currentRoute.value.query?.id

  nextTick(() => {
    updateDatasourceContentHeight()
    if (typeof ResizeObserver !== 'undefined' && datasourceManageRef.value) {
      datasourceManageResizeObserver = new ResizeObserver(() => {
        updateDatasourceContentHeight()
      })
      datasourceManageResizeObserver.observe(datasourceManageRef.value)
    }
  })
})

onBeforeUnmount(() => {
  datasourceManageResizeObserver?.disconnect()
})

watch([isIframe, isDataEaseBi], () => {
  nextTick(() => {
    updateDatasourceContentHeight()
  })
})

const sideTreeStatus = ref(true)
const changeSideTreeStatus = val => {
  sideTreeStatus.value = val
}

const mouseenter = () => {
  appStore.setArrowSide(true)
}

const mouseleave = () => {
  appStore.setArrowSide(false)
}

const getMenuList = (val: boolean, data?: any) => {
  let list = !val
    ? menuList
    : [
        {
          label: t('common.copy'),
          svgName: icon_copy_filled,
          command: 'copy'
        }
      ].concat(menuList)
  return list.filter(item => {
    if (disabledMove.value && item.command === 'move') return false
    if (data?.orgRoot && item.command === 'move') return false
    return true
  })
}

const activeName = ref('dataPreview')

const handleClick = (tabName: TabPaneName) => {
  switch (tabName) {
    case 'dataPreview':
      search(searchConditions.value)
      break
    case 'record':
      state.multipleSelection = []
      break
    case 'task':
      state.multipleSelection = []
      break
  }
}

const existsForms = computed<Array<DfFormItem>>(() => {
  return filter(selectedItem.value?.forms, f => !f.removed)
})

const columns = computed<Array<ColumnItem>>(() => {
  const _list: Array<ColumnItem> = []
  forEach(existsForms.value, f => {
    if (f.type === 'dateRange') {
      _list.push({
        id: f.id,
        props: f.settings?.mapping?.columnName1,
        label: f.settings?.name,
        date: true,
        number: false,
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        rangeIndex: 0
      } as ColumnItem)
      _list.push({
        id: f.id,
        props: f.settings?.mapping?.columnName2,
        label: f.settings?.name,
        date: true,
        number: false,
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        rangeIndex: 1
      } as ColumnItem)
    } else {
      _list.push({
        id: f.id,
        props: f.settings?.mapping?.columnName,
        label: f.settings?.name,
        date: f.type === 'date',
        number: f.settings.inputType === 'number',
        dateType: f.settings?.dateType,
        type: f.type,
        multiple: !!f.settings.multiple,
        asyncOptions: f.settings.optionSourceType === 2,
        asyncOptionSetting: {
          optionDatasource: f.settings.optionDatasource,
          optionTable: f.settings.optionTable,
          optionColumn: f.settings.optionColumn,
          optionOrder: f.settings.optionOrder
        },
        options: f.settings.options,
        tempId:
          f.settings.optionDatasource +
          '_' +
          f.settings.optionTable +
          '_' +
          f.settings.optionColumn +
          '_' +
          f.settings.optionOrder
      } as ColumnItem)
    }
  })
  return _list
})

const dateFormatColumns = computed(() => {
  return map(
    filter(columns.value, c => c.date),
    'props'
  )
})

function getCondition() {
  const _condition: Array<any> = []
  for (let i = 0; i < searchConditions.value.length; i++) {
    const c = searchConditions.value[i]
    if (c.date) {
      if (c.term === 'between') {
        _condition.push({
          field: c.column,
          value: undefined,
          values: [dayjs(c.values[0]).toDate().getTime(), dayjs(c.values[1]).toDate().getTime()],
          term: c.term,
          multiple: c.multiple
        })
      } else {
        _condition.push({
          field: c.column,
          value: dayjs(c.value).toDate().getTime(),
          values: [],
          term: c.term,
          multiple: c.multiple
        })
      }
    } else {
      _condition.push({
        field: c.column,
        value: c.value,
        values: c.values,
        term: c.term,
        multiple: c.multiple
      })
    }
  }
  return _condition
}

const search = (condition = []) => {
  searchConditions.value = condition
  if (!selectedItem.value?.id) {
    return
  }
  dsLoading.value = true

  const _condition: Array<any> = getCondition()

  searchTable(selectedItem.value.id, {
    currentPage: state.paginationConfig.currentPage,
    pageSize: state.paginationConfig.pageSize,
    searchParams: _condition
  })
    .then(res => {
      if (res.data) {
        state.paginationConfig.key = res.data.key
        state.paginationConfig.total = res.data.total
        state.paginationConfig.currentPage = res.data.currentPage
        const _data = []
        forEach(res.data.data, d => {
          const obj = {}
          forIn(d.data, (value, key) => {
            if (includes(dateFormatColumns.value, key)) {
              if (value) {
                obj[key] = new Date(value)
              } else {
                obj[key] = undefined
              }
            } else {
              obj[key] = value === null ? undefined : value
            }
          })
          _data.push({
            data: obj,
            logInfo: d.logInfo ? JSON.parse(d.logInfo) : undefined
          })
        })
        state.dfTableData = _data
      }
    })
    .finally(() => {
      dsLoading.value = false
    })
}

const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search(searchConditions.value)
}

const sizeChange = size => {
  state.paginationConfig.pageSize = size
  state.paginationConfig.currentPage = 1
  search(searchConditions.value)
}
const sortChange = param => {
  state.orders = []
  if (param.order && param.prop === 'createTime') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    state.orders.push('create_time ' + type)
    search(searchConditions.value)
  }
}

const multipleTableRef = ref()

const handleSelectionChange = rows => {
  state.multipleSelection = rows
}

const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
}

const batchDelHandler = () => {
  ElMessageBox.confirm(
    t('data_fill.confirm_delete_multiple_data', [state.multipleSelection.length]),
    {
      confirmButtonType: 'danger',
      type: 'warning',
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false
    }
  )
    .then(res => {
      batchDel()
    })
    .catch(() => {
      clearSelection()
    })
}

const batchDel = () => {
  if (selectedItemId.value) {
    const key = state.paginationConfig.key
    const ids = state.multipleSelection.map(item => item.data[key])
    dsLoading.value = true

    batchDeleteRowData(selectedItemId.value, ids)
      .then(res => {
        ElMessage.success(t('common.delete_success'))
        search(searchConditions.value)
      })
      .catch(e => {
        dsLoading.value = false
      })
  }
}

const addRowData = () => {
  rowDataFormRef.value?.init(selectedItemId.value, true, true, undefined, undefined, true)
}

function openRow(item) {
  const id = item[state.paginationConfig.key]
  rowDataFormRef.value?.init(selectedItemId.value, true, false, [{ rowDataId: id }])
}

function updateRow(item) {
  const id = item[state.paginationConfig.key]
  rowDataFormRef.value?.init(selectedItemId.value, true, true, [{ rowDataId: id }], undefined, true)
}

function deleteRow(id) {
  ElMessageBox.confirm(t('data_fill.confirm_delete_data'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  }).then(() => {
    dsLoading.value = true
    deleteRowData(selectedItemId.value, id)
      .then(res => {
        ElMessage.success(t('common.delete_success'))
        search(searchConditions.value)
      })
      .catch(e => {
        dsLoading.value = false
      })
  })
}

function truncateTable() {
  ElMessageBox.confirm(t('data_fill.confirm_truncate_table'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('data_fill.truncate'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  }).then(() => {
    dsLoading.value = true
    truncateRowData(selectedItemId.value)
      .then(res => {
        ElMessage.success(t('common.delete_success'))
        search(searchConditions.value)
      })
      .catch(e => {
        dsLoading.value = false
      })
  })
}

function openUploadData() {
  showDownloadDrawer.value = true
}

function closeUpload() {
  showDownloadDrawer.value = false
}

function downloadData() {
  const _condition: Array<any> = getCondition()
  innerExport(selectedItemId.value, isEmbedded.value, { searchParams: _condition })
    .then(res => {
      if (isEmbedded.value && embeddedSyncExport.value) {
        const blobData = res.data
        const temp = res.headers['content-disposition']
          ?.split(';')[1]
          ?.split("filename*=utf-8''")[1]
        const fileName = temp ? decodeURIComponent(temp) : `${selectedItem.value.name}.xlsx`
        const blob = new Blob([blobData], {
          type:
            res.headers['content-type'] ??
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8'
        })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = fileName // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        openMessageLoading(exportData)
      }
    })
    .catch(() => {
      console.error('Excel download error')
    })
}

const exportData = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'IN_PROGRESS' })
}

const openMessageLoading = cb => {
  const iconClass = `el-icon-loading`
  const customClass = `de-message-loading de-message-export`
  ElMessage({
    message: h('p', null, [
      t('data_fill.exporting'),
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
      t('data_fill.progress_to_download')
    ]),
    iconClass,
    icon: h(RefreshLeft),
    showClose: true,
    customClass
  })
}

function finishUpload() {
  closeUpload()
  search(searchConditions.value)
}

const showFilter = ref(false)
const searchConditions = ref([])
const drawerMainOpen = async () => {
  //todo
  showFilter.value = true
}
const drawerMainClose = () => {
  showFilter.value = false
}

function getMultipleValueList(data) {
  try {
    return join(JSON.parse(data), '; ')
  } catch (e) {
    console.error(e)
  }
  return ''
}
</script>

<template>
  <div class="datasource-manage" ref="datasourceManageRef" v-loading="dsLoading">
    <ArrowSide
      :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
      @change-side-tree-status="changeSideTreeStatus"
      :isInside="!sideTreeStatus"
    />
    <el-aside
      @mouseenter="mouseenter"
      @mouseleave="mouseleave"
      class="resource-area"
      :class="{ retract: !sideTreeStatus }"
      ref="node"
      :style="{ width: width + 'px' }"
    >
      <ArrowSide
        :isInside="!sideTreeStatus"
        :style="{ left: (sideTreeStatus ? width - 12 : 0) + 'px' }"
        @change-side-tree-status="changeSideTreeStatus"
      />
      <div class="resource-tree">
        <div class="tree-header">
          <div class="icon-methods">
            <span class="title"> {{ t('data_fill.data_fill') }} </span>
            <div v-if="rootManage || moduleAuthData.anyManage" class="flex-align-center">
              <el-tooltip
                effect="dark"
                offset="14"
                popper-class="new-folder_tip"
                :content="t('data_fill.create_folder')"
                placement="top"
                v-if="rootManage"
              >
                <el-icon
                  class="custom-icon btn"
                  :style="{ marginRight: '20px' }"
                  @click="handleFormTree('folder')"
                >
                  <Icon name="dv-new-folder">
                    <dvNewFolder class="svg-icon" />
                  </Icon>
                </el-icon>
              </el-tooltip>
              <el-tooltip
                effect="dark"
                :content="t('data_fill.create_form')"
                offset="14"
                popper-class="new-data_fill"
                placement="top"
                v-if="rootManage"
              >
                <el-icon class="custom-icon btn" @click="createForm">
                  <Icon name="icon_file-add_outlined">
                    <icon_fileAdd_outlined />
                  </Icon>
                </el-icon>
              </el-tooltip>
            </div>
          </div>

          <el-input
            :placeholder="t('commons.search')"
            v-model="dsName"
            clearable
            class="search-bar"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined">
                  <icon_searchOutline_outlined />
                </Icon>
              </el-icon>
            </template>
          </el-input>
          <el-dropdown @command="handleSortTypeChange" trigger="click">
            <el-icon class="filter-icon-span">
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon v-if="state.curSortType.includes('asc')" name="dv-sort-asc" class="opt-icon">
                  <dvSortAsc />
                </Icon>
              </el-tooltip>
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon
                  v-if="state.curSortType.includes('desc')"
                  name="dv-sort-desc"
                  class="opt-icon"
                >
                  <dvSortDesc class="svg-icon opt-icon" />
                </Icon>
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
        <el-scrollbar
          @scroll="handleScroll"
          ref="scrollbarRef"
          :style="customTreeStyle"
          class="custom-tree"
        >
          <el-tree
            menu
            v-if="dsListTreeShow"
            ref="dsListTree"
            node-key="id"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            :filter-node-method="filterNode"
            :default-expanded-keys="expandedKey"
            :data="state.datasourceTree"
            :props="defaultProps"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node" :class="{ 'node-disabled-custom': !data.weight }">
                <el-icon :class="data.leaf && 'icon-border'" style="font-size: 18px">
                  <Icon>
                    <component :is="getDsIconName(data)"></component>
                  </Icon>
                </el-icon>
                <el-tooltip
                  v-if="!data.weight"
                  effect="dark"
                  :content="t('common.no_permission_node')"
                  placement="top-start"
                >
                  <span
                    :title="node.label"
                    class="label-tooltip ellipsis"
                    :class="data.type === 'Excel' && 'excel'"
                    >{{ node.label }}</span
                  >
                </el-tooltip>
                <span
                  v-else
                  :title="node.label"
                  class="label-tooltip ellipsis"
                  :class="data.type === 'Excel' && 'excel'"
                  >{{ node.label }}</span
                >
                <div class="icon-more" v-if="data.weight >= 7">
                  <handle-more
                    icon-size="24px"
                    @handle-command="cmd => handleFormTree(cmd, data)"
                    :menu-list="addOptionTypeList"
                    :icon-name="icon_add_outlined"
                    placement="bottom-start"
                    v-if="!data.leaf"
                  ></handle-more>
                  <el-icon
                    class="hover-icon"
                    @click.stop="handleEdit(data)"
                    v-else-if="data.type !== 'Excel'"
                  >
                    <icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></icon>
                  </el-icon>
                  <handle-more
                    @handle-command="
                      cmd => operation(cmd, data, data.leaf ? 'data-filling' : 'folder')
                    "
                    :menu-list="
                      getMenuList(!['Excel', 'API'].includes(data.type) && data.leaf, data)
                    "
                  ></handle-more>
                </div>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </el-aside>

    <div
      class="datasource-content"
      :class="{
        auto: isEmbedded,
        h100: isIframe,
        h90: isDataEaseBi
      }"
      :style="datasourceContentStyle"
      v-loading="mainLoading"
    >
      <template v-if="!state.datasourceTree.length && mounted">
        <empty-background :description="t('data_fill.no_form')" img-type="none">
          <el-button v-if="rootManage" @click="() => createForm()" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined">
                <icon_add_outlined class="svg-icon" />
              </Icon>
            </template>
            {{ t('data_fill.create_form') }}
          </el-button>
        </empty-background>
      </template>
      <template v-else-if="!!selectedItemId && selectedItem">
        <div class="datasource-info">
          <div class="info-method">
            <span :title="selectedItem.name" class="name ellipsis">
              {{ selectedItem.name }}
            </span>
            <el-divider style="margin: 0 12px" direction="vertical" />
            <span class="create-user">
              {{ t('visualization.create_by') }}:{{ selectedItem.creator }}
            </span>
            <el-popover :offset="8" show-arrow placement="bottom" width="290" trigger="hover">
              <template #reference>
                <el-icon size="16px" class="create-user">
                  <Icon name="icon_info_outlined">
                    <icon_info_outlined class="svg-icon" />
                  </Icon>
                </el-icon>
              </template>
              <InfoDetail
                :id="selectedItem.id"
                :creator="selectedItem.creator"
                :updater="selectedItem.updater"
                :create-time="timestampFormatDate(selectedItem.createTime)"
                :update-time="timestampFormatDate(selectedItem.updateTime)"
                :data-source-name="selectedItem.datasourceName"
                :table-name="selectedItem.tableName"
              />
            </el-popover>
            <div class="right-btn flex-align-center" style="height: 32px">
              <el-button
                v-if="selectedItem?.weight >= 7"
                @click="editForm(selectedItem.id)"
                type="primary"
              >
                <template #icon>
                  <Icon name="icon_edit_outlined">
                    <icon_edit_outlined class="svg-icon" />
                  </Icon>
                </template>
                {{ t('common.edit') }}
              </el-button>
            </div>
          </div>
          <div class="tab-border">
            <el-tabs v-model="activeName" @tab-change="handleClick">
              <el-tab-pane :label="t('data_fill.form.list')" name="dataPreview" />
              <el-tab-pane :label="t('data_fill.form.record')" name="record" />
              <el-tab-pane
                :label="t('data_fill.form.task_manage')"
                name="task"
                v-if="selectedItem?.weight >= 7"
              />
            </el-tabs>
          </div>
        </div>

        <div
          v-if="activeName === 'dataPreview'"
          class="df-table-container"
          :class="{
            'df-table-container-no-bottom': state.multipleSelection.length
          }"
        >
          <div style="display: flex; width: 100%; height: 100%; flex-direction: column">
            <div
              class="df-table"
              :class="{
                'df-table-bottom': state.multipleSelection.length
              }"
            >
              <div style="display: flex; width: 100%; flex-direction: row; margin-bottom: 16px">
                <div class="search-operate" v-if="selectedItem?.weight >= 7" style="flex: 1">
                  <el-button secondary @click="addRowData">
                    <template #icon>
                      <Icon name="icon_add_outlined">
                        <icon_add_outlined class="svg-icon" />
                      </Icon>
                    </template>
                    {{ t('data_fill.data.add_data') }}
                  </el-button>
                  <el-button secondary @click="openUploadData">
                    <template #icon>
                      <Icon name="icon_upload_outlined">
                        <icon_upload_outlined />
                      </Icon>
                    </template>
                    {{ t('data_fill.data.batch_upload') }}
                  </el-button>
                  <el-button secondary @click="downloadData">
                    <template #icon>
                      <Icon name="icon_download_outlined">
                        <icon_download_outlined />
                      </Icon>
                    </template>
                    {{ t('data_fill.data.download') }}
                  </el-button>
                  <el-button secondary @click="truncateTable">
                    {{ t('data_fill.truncate_table') }}
                  </el-button>
                </div>
                <el-button
                  @click="drawerMainOpen"
                  :plain="!!searchConditions.length"
                  :class="searchConditions.length ? 'filter-condition-button' : 'filter-button'"
                >
                  <template #icon>
                    <Icon name="icon-filter">
                      <iconFilter class="svg-icon" />
                    </Icon>
                  </template>
                  {{
                    t('common.filter') +
                    (searchConditions.length ? `(${searchConditions?.length})` : '')
                  }}
                </el-button>
              </div>
              <div class="info-table-full" :class="{ 'info-table': selectedItem?.weight >= 7 }">
                <GridTable
                  ref="multipleTableRef"
                  :pagination="state.paginationConfig"
                  :table-data="state.dfTableData"
                  :empty-desc="emptyDesc"
                  :empty-img="imgType"
                  class="popper-max-width df-data-form-table"
                  @current-change="pageChange"
                  @size-change="sizeChange"
                  @sort-change="sortChange"
                  @selection-change="handleSelectionChange"
                  border
                >
                  <el-table-column type="selection" width="36" v-if="selectedItem?.weight >= 7" />
                  <el-table-column v-for="c in columns" :key="c.props" :prop="c.props">
                    <template #header>
                      {{ c.label }}
                      <span v-if="c.rangeIndex === 0">({{ t('data_fill.data.start') }})</span>
                      <span v-if="c.rangeIndex === 1">({{ t('data_fill.data.end') }})</span>
                    </template>
                    <template #default="scope">
                      <span
                        v-if="c.date && scope.row.data[c.props]"
                        style="width: fit-content; white-space: nowrap"
                        :title="formatDate(scope.row.data[c.props], c.dateType)"
                      >
                        {{ formatDate(scope.row.data[c.props], c.dateType) }}
                      </span>
                      <template
                        v-else-if="
                          ((c.type === 'select' && c.multiple) || c.type === 'checkbox') &&
                          scope.row.data[c.props]
                        "
                      >
                        <span
                          style="width: fit-content; white-space: nowrap"
                          :title="getMultipleValueList(scope.row.data[c.props])"
                        >
                          {{ getMultipleValueList(scope.row.data[c.props]) }}
                        </span>
                      </template>
                      <span
                        v-else
                        style="width: fit-content; white-space: nowrap"
                        :title="scope.row.data[c.props]"
                      >
                        {{ scope.row.data[c.props] }}
                      </span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="t('data_fill.data.recent_committer')"
                    fixed="right"
                    width="100"
                  >
                    <template #default="scope">
                      {{ scope.row.logInfo?.committer }}
                    </template>
                  </el-table-column>
                  <el-table-column
                    :label="t('data_fill.data.recent_commit_time')"
                    fixed="right"
                    width="180"
                  >
                    <template #default="scope">
                      {{ formatDate(scope.row.logInfo?.commitTime, 'datetime') }}
                    </template>
                  </el-table-column>
                  <el-table-column :label="t('data_fill.form.operation')" width="160" fixed="right">
                    <template #default="scope">
                      <div style="display: flex; flex-direction: row">
                        <el-button text @click="openRow(scope.row.data)">
                          <template #icon>
                            <Icon name="icon_describe_outlined">
                              <icon_describe_outlined />
                            </Icon>
                          </template>
                        </el-button>
                        <el-button
                          text
                          v-if="selectedItem?.weight >= 7"
                          @click="updateRow(scope.row.data)"
                        >
                          <template #icon>
                            <Icon name="icon_edit_outlined">
                              <icon_edit_outlined />
                            </Icon>
                          </template>
                        </el-button>
                        <el-button
                          text
                          v-if="selectedItem?.weight >= 7"
                          @click="deleteRow(scope.row.data[state.paginationConfig.key])"
                        >
                          <template #icon>
                            <Icon name="icon_delete-trash_outlined">
                              <icon_deleteTrash_outlined />
                            </Icon>
                          </template>
                        </el-button>
                      </div>
                    </template>
                  </el-table-column>
                </GridTable>
              </div>
            </div>
            <div
              v-if="state.multipleSelection.length && selectedItem?.weight >= 7"
              class="df-bottom-bar flex-align-center"
            >
              <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
                {{ t('user.batch_del') }}
              </el-button>
              <span class="bottom-info">
                {{ t('user.selection_info', [state.multipleSelection.length]) }}
              </span>
              <el-button text @click="clearSelection">
                {{ t('data_fill.clear_selection') }}
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="activeName === 'record'" class="df-table-container">
          <LogGrid :form-id="selectedItemId" :has-manage-permission="selectedItem?.weight >= 7" />
        </div>

        <div v-if="activeName === 'task'" class="df-table-container" style="padding: 0">
          <TaskGrid
            :form-id="selectedItemId"
            :form-name="selectedItem?.name"
            :columns="columns"
            :forms="existsForms"
          />
        </div>
      </template>
      <template v-else-if="mounted">
        <empty-background :description="t('data_fill.on_the_left')" img-type="select" />
      </template>
    </div>
  </div>

  <FilterSearchDrawer
    v-if="showFilter"
    @trigger-filter="search"
    :form-id="selectedItem?.id"
    :base-form="selectedItem?.forms"
    v-model:conditions="searchConditions"
    v-model:model-value="showFilter"
  />

  <creat-ds-group @finish="listDf" ref="creatDsFolder" />

  <RowDataForm ref="rowDataFormRef" @finish="onCloseToRefresh" />

  <el-drawer
    :title="t('data_fill.data.batch_upload')"
    :close-on-click-modal="false"
    size="calc(100% - 100px)"
    v-model="showDownloadDrawer"
    direction="btt"
    append-to-body
    modal-class="df-upload-drawer"
  >
    <ExcelBatchUpload
      v-if="showDownloadDrawer"
      :formId="selectedItemId"
      :form-name="selectedItem.name"
      :columns="columns"
      @close="closeUpload"
      @finish="finishUpload"
    />
  </el-drawer>
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

.datasource-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  position: relative;

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

  .update-records {
    position: absolute;
    top: 19px;
    right: 12px;
  }

  .update-info {
    display: inline-flex;
    height: 24px;
    padding: 1px 6px;
    align-items: center;
    border-radius: 2px;

    &.to-be-updated {
      background: rgba(31, 35, 41, 0.1);
      color: #646a73;
    }

    &.updating {
      color: var(--ed-color-primary, rgba(51, 112, 255, 1));
      background: var(--ed-color-primary-33, rgba(51, 112, 255, 0.2));
    }

    &.pause {
      background: rgba(31, 35, 41, 0.1);
      color: #646a73;
    }

    &.updated {
      color: #2ca91f;
      background: rgba(52, 199, 36, 0.2);
    }
  }

  .icon-border {
    font-size: 18px;
  }

  .excel-table {
    margin-top: 16px;

    .sheet-table-content {
      height: 400px;
    }
  }

  .api-card-content {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16px;
    margin-left: -16px;
  }

  .api-card {
    width: calc(50% - 16px);
    height: 140px;
    border-radius: 4px;
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-radius: 4px;
    margin: 0 0 16px 16px;
    padding: 16px;
    font-family: var(--de-custom_font, 'PingFang');

    .name {
      font-size: 16px;
      font-weight: 500;
      margin-right: 8px;
      max-width: 70%;
      display: inline-flex;
    }

    .req-title,
    .req-value {
      display: flex;
      font-size: 14px;
      font-weight: 400;

      :nth-child(1) {
        width: 100px;
      }

      :nth-child(2) {
        margin-left: 24px;
        max-width: calc(100% - 124px);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .req-title {
      color: var(--deTextSecondary, #646a73);
      margin: 16px 0 4px 0;
    }

    .req-value {
      color: var(--deTextPrimary, #1f2329);
    }

    .de-copy-icon {
      cursor: pointer;
      margin-right: 20px;
      color: var(--deTextSecondary, #646a73);
    }

    .de-delete-icon:not(.not-allow) {
      cursor: pointer;

      &:hover {
        color: var(--deDanger, #f54a45);
      }
    }

    .de-tag {
      display: inline-flex;
      justify-content: center;
      align-items: center;
      border-radius: 2px;
      padding: 1px 6px;
      height: 24px;
      font-size: 14px;
    }

    .error-color {
      color: #646a73;
      background-color: rgba(31, 35, 41, 10%);
    }

    .success-color {
      color: green;
      background-color: rgba(52, 199, 36, 20%);
    }
  }

  .de-expand {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: var(--ed-color-primary);
    cursor: pointer;
    margin-top: 16px;
    display: inline-flex;
    align-items: center;

    .ed-icon {
      margin-left: 4px;
    }
  }

  .datasource-height,
  .datasource-content {
    height: calc(100vh - 56px);
    overflow: auto;
    position: relative;

    &.h100 {
      .df-table-container {
        height: calc(100% - 90px);
      }
    }

    &.h90 {
      .df-table-container {
        height: calc(100% - 90px);
      }
    }
  }

  .datasource-list {
    width: 279px;
    padding: 16px 8px;
  }

  .datasource-content {
    background: #f5f6f7;
    overflow-y: auto;

    &.auto {
      height: auto;
    }
  }

  .m24 {
    margin: 24px 0;
  }

  .w100 {
    width: 100%;
  }

  .datasource-content {
    flex: 1;
    position: relative;

    .datasource-info {
      background: #fff;
      padding: 0 24px;
      padding-top: 12px;
      height: 90px;
      position: sticky;
      top: 0;
      z-index: 6;

      .info-method {
        width: 100%;
        display: flex;
        align-items: center;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 16px;
        font-weight: 500;

        .ed-icon {
          font-size: 24px;
        }

        .name {
          margin-left: 8px;
          max-width: 200px;
        }

        .create-user {
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          color: #646a73;
          margin-right: 4px;
        }

        .mr8 {
          margin-left: 8px;
        }

        .right-btn {
          margin-left: auto;

          .replace-excel {
            margin: 0 12px;
          }
        }
      }

      .tab-border {
        .border-bottom-tab(24px);

        :deep(.ed-tabs__item) {
          font-size: 14px;
        }

        :deep(.ed-tabs__nav-wrap::after) {
          border-color: rgba(31, 35, 41, 0.15);
        }

        margin-left: 0;
      }
    }

    .df-table-container {
      padding: 24px;
      height: calc(100vh - 148px);
      display: flex;
      flex-direction: column;
    }

    .df-table-container-no-bottom {
      padding: 24px 24px 0;
    }

    .df-table {
      padding: 24px;
      background: #fff;
      height: 100%;

      .search-operate {
        width: 500px;
      }
    }

    .df-table-bottom {
      height: calc(100% - 64px);
    }

    .info-table-full {
      height: 100%;
    }

    .info-table {
      height: calc(100% - 49px);
    }

    .info-table-no-bottom {
      height: 100%;
    }
  }
}

.custom-tree {
  height: calc(100vh - 148px);
  padding: 0 8px;
}

.custom-tree-node {
  width: calc(100% - 30px);
  display: flex;
  align-items: center;
  box-sizing: content-box;
  padding-right: 4px;
  position: relative;

  .label-tooltip {
    width: calc(100% - 40px);
    margin-left: 8.75px;
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    left: 18px;
  }

  .icon-more {
    margin-left: auto;
    opacity: 0;
  }

  &:hover {
    .label-tooltip {
      width: calc(100% - 78px);

      &.excel {
        width: calc(100% - 54px);
      }
    }

    .icon-more {
      opacity: 1;
    }
  }
}

.node-disabled-custom {
  color: rgba(187, 191, 196, 1);
  cursor: not-allowed;
}

.df-bottom-bar {
  z-index: 800;
  position: unset !important;
  height: 64px;
  min-height: 64px !important;
  width: 100%;
  padding-left: 24px;
  background: var(--neutral-00, #fff);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);

  .bottom-info {
    color: #646a73;
    margin: 0 16px 0 24px;
  }

  .batch-delete-button {
    color: var(--ed-button-text-color);
    border-color: var(--ed-button-border-color);

    &:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
      outline: none;
    }
  }
}

.df-data-form-table {
  :deep(.ed-table--border) {
    border-bottom: var(--ed-table-border);
  }
}
</style>
<style lang="less">
.new-data_fill {
  .ed-popper__arrow {
    transform: translate(39px, 0px) !important;
  }
}
.record-drawer {
  .ed-drawer__body {
    padding: 24px;
  }

  .flex-align-center {
    .ed-icon {
      margin: 0 4px;
    }

    .error-info {
      cursor: pointer;
    }
  }
}

.ds-table-drawer {
  max-height: calc(100% - 120px);
  display: flex;
  flex-direction: column;

  .ed-dialog__body {
    overflow-y: auto;
  }

  .table-value,
  .table-name {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
    margin: 0;
  }

  .table-name {
    color: var(--deTextSecondary, #646a73);
  }

  .table-value {
    margin: 4px 0 24px 0;
    color: var(--deTextPrimary, #1f2329);
  }
}

.df-upload-drawer {
  .ed-drawer__body {
    padding: 0;
  }
}
</style>
