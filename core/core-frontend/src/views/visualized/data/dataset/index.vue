<script lang="tsx" setup>
import icon_copy_filled from '@/assets/svg/icon_copy_filled.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_intoItem_outlined from '@/assets/svg/icon_into-item_outlined.svg'
import { debounce } from 'lodash-es'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import dvNewFolder from '@/assets/svg/dv-new-folder.svg'
import icon_fileAdd_outlined from '@/assets/svg/icon_file-add_outlined.svg'
import { moveDatasetTree } from '@/api/dataset'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import dvSortAsc from '@/assets/svg/dv-sort-asc.svg'
import dvSortDesc from '@/assets/svg/dv-sort-desc.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import { treeDraggble } from '@/utils/treeDraggble'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_dashboard_outlined from '@/assets/svg/icon_dashboard_outlined.svg'
import icon_operationAnalysis_outlined from '@/assets/svg/icon_operation-analysis_outlined.svg'
import icon_download_outlined from '@/assets/svg/icon_download_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import {
  ref,
  reactive,
  shallowRef,
  computed,
  watch,
  onBeforeMount,
  nextTick,
  unref,
  h,
  provide
} from 'vue'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useEmitt } from '@/hooks/web/useEmitt'
import relationChart from '@/components/relation-chart/index.vue'
import {
  ElIcon,
  ElButton,
  ElMessageBox,
  ElMessage,
  type ElMessageBoxOptions,
  ElAside,
  ElScrollbar
} from 'element-plus-secondary'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { useRouter, useRoute } from 'vue-router_2'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import {
  delDatasetTree,
  getDatasetPreview,
  barInfoApi,
  perDelete,
  exportDatasetData,
  exportLimit,
  getDatasetTotal
} from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import DatasetDetail from './DatasetDetail.vue'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { save } from '@/api/visualization/dataVisualization'
import { cloneDeep } from 'lodash-es'
import { fieldType } from '@/utils/attr'
import { useAppStoreWithOut } from '@/store/modules/app'
import treeSort from '@/utils/treeSortUtils'
import RowAuth from '@/views/chart/components/editor/filter/auth-tree/RowAuth.vue'

import {
  DEFAULT_CANVAS_STYLE_DATA_LIGHT,
  DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK
} from '@/views/chart/components/editor/util/dataVisualization'
import type { TabPaneName } from 'element-plus-secondary'
import { timestampFormatDate } from './form/util'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { XpackComponent } from '@/components/plugin'
import { useCache } from '@/hooks/web/useCache'
import { RefreshLeft } from '@element-plus/icons-vue'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { exportPermission, isFreeFolder } from '@/utils/utils'
const { t } = useI18n()
const interactiveStore = interactiveStoreWithOut()
const { wsCache } = useCache()
interface Field {
  fieldShortName: string
  name: string
  dataeaseName: string
  originName: string
  deType: number
}

interface Node {
  name: string
  createBy: string
  creator: string
  id: string
  nodeType: string
  createTime: number
  weight: number
  ext?: number
}
const appStore = useAppStoreWithOut()
const rootManage = ref(false)
const showExport = ref(false)
const rowAuth = ref()
const exportDatasetLoading = ref(false)
const limit = ref(t('data_set.ten_wan'))
const exportForm = ref({})
const table = ref({})
const exportFormRef = ref()
const exportFormRules = {
  name: [
    {
      required: true,
      message: t('commons.input_content'),
      trigger: 'change'
    },
    {
      max: 50,
      message: t('commons.char_can_not_more_50'),
      trigger: 'change'
    }
  ]
}
const datasetTableFiled = ref([])
provide('filedList', datasetTableFiled)

const nickName = ref('')
const router = useRouter()
const route = useRoute()
const state = reactive({
  datasetTree: [] as BusiTreeNode[],
  curSortType: 'time_desc'
})

const resourceGroupOpt = ref()
const curCanvasType = ref('')
const mounted = ref(false)
const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)
const exportPermissions = computed(() => exportPermission(nodeInfo.weight, nodeInfo.ext))
const createPanel = path => {
  const baseUrl = `#/${path}?opt=create&id=${nodeInfo.id}`
  wsCache.set('dataset-info-id', nodeInfo.id)
  window.open(baseUrl, openType)
}

const resourceOptFinish = param => {
  if (param && param.opt === 'newLeaf') {
    resourceCreate(param.pid, param.name)
  }
}

const originResourceTree = shallowRef([])

const handleSortTypeChange = sortType => {
  state.datasetTree = treeSort(originResourceTree.value, sortType)
  state.curSortType = sortType
  wsCache.set('TreeSort-dataset', state.curSortType)
}

const sortTypeChange = sortType => {
  state.datasetTree = treeSort(originResourceTree.value, sortType)
  state.curSortType = sortType
}

const resourceCreate = (pid, name) => {
  // 新建基础信息
  const newResourceId = guid()
  const bashResourceInfo = {
    dataState: 'ready',
    id: newResourceId,
    name: name,
    pid: pid,
    type: curCanvasType.value,
    status: 1,
    selfWatermarkStatus: true
  }
  const canvasStyleDataNew =
    curCanvasType.value === 'dashboard'
      ? DEFAULT_CANVAS_STYLE_DATA_LIGHT
      : DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK
  const canvasInfo = {
    canvasStyleData: JSON.stringify(canvasStyleDataNew),
    componentData: JSON.stringify([]),
    canvasViewInfo: {},
    ...bashResourceInfo
  }
  save(canvasInfo).then(() => {
    const baseUrl = curCanvasType.value === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
    window.open(baseUrl + newResourceId, openType)
  })
}

const creatDsFolder = ref()
const defaultNode = {
  name: '',
  createBy: '',
  creator: '',
  id: '',
  nodeType: '',
  createTime: 0,
  weight: 0
}

const nodeInfo = reactive<Node>(cloneDeep(defaultNode))

let allFields = []
let columnsPreview = []
let dataPreview = []

const allFieldsColumns = [
  {
    key: 'name',
    dataKey: 'name',
    title: t('data_set.field_name'),
    width: 250
  },
  {
    key: 'deType',
    dataKey: 'deType',
    title: t('data_set.field_type'),
    width: 250,
    cellRenderer: ({ cellData: deType }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon>
            {h(iconFieldMap[fieldType[deType]], {
              class: `svg-icon field-icon-${fieldType[deType]}`
            })}
          </Icon>
        </ElIcon>
        {t(`dataset.${fieldType[deType]}`) +
          `${deType === 3 ? '(' + t('dataset.float') + ')' : ''}`}
      </div>
    )
  },
  {
    key: 'description',
    dataKey: 'description',
    title: t('data_set.field_notes'),
    width: 250
  }
]

const dataPreviewLoading = ref(false)
const { width, node } = useMoveLine('DATASOURCE')

const infoList = computed(() => {
  return {
    creator: nodeInfo.creator,
    createTime: nodeInfo.createTime && timestampFormatDate(nodeInfo.createTime)
  }
})

const { handleDrop, allowDrop, handleDragStart } = treeDraggble(
  state,
  'datasetTree',
  moveDatasetTree,
  'dataset',
  originResourceTree
)

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.dataeaseName,
    deType: ele.deType,
    dataKey: ele.dataeaseName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center">
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon>
            {h(iconFieldMap[fieldType[column.deType]], {
              class: `svg-icon field-icon-${fieldType[column.deType]}`
            })}
          </Icon>
        </ElIcon>
        <span class="ellipsis" title={column.title} style={{ width: '120px' }}>
          {column.title}
        </span>
      </div>
    )
  }))

const dtLoading = ref(false)
const isCreated = ref(false)
const getData = () => {
  dtLoading.value = true
  let curSortType = sortList[Number(wsCache.get('TreeSort-backend')) ?? 1].value
  curSortType = wsCache.get('TreeSort-dataset') ?? curSortType
  const request = { busiFlag: 'dataset' } as BusiTreeRequest
  interactiveStore
    .setInteractive(request)
    .then(res => {
      const nodeData = (res as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        rootManage.value = nodeData[0]['weight'] >= 7
        state.datasetTree = nodeData[0]['children'] || []
        originResourceTree.value = cloneDeep(unref(state.datasetTree))
        sortTypeChange(curSortType)
        return
      }
      state.datasetTree = nodeData
      originResourceTree.value = cloneDeep(unref(state.datasetTree))
      sortTypeChange(curSortType)
    })
    .finally(() => {
      dtLoading.value = false
      mounted.value = true
      nextTick(() => {
        if (!!nickName.value.length) {
          datasetListTree.value.filter(nickName.value)
        }
      })
      const id = nodeInfo.id
      if (!!id) {
        Object.assign(nodeInfo, cloneDeep(defaultNode))
        dfsDatasetTree(state.datasetTree, id)
        nextTick(() => {
          if (isCreated.value) return
          isCreated.value = true
          datasetListTree.value.setCurrentKey(id, true)
        })
      }
    })
}

const dfsDatasetTree = (ds, id) => {
  ds.some(ele => {
    if (ele.id === id) {
      handleNodeClick(ele)
      return true
    }
    if (!!ele.children?.length) {
      dfsDatasetTree(ele.children, id)
    }
    return false
  })
}

onBeforeMount(() => {
  const paramId = wsCache.get('dataset-info-id') || route.params.id
  nodeInfo.id = (paramId as string) || (route.query.id as string) || ''
  wsCache.delete('dataset-info-id')
  wsCache.delete('db-info-id')
  wsCache.delete('dv-info-id')
  loadInit()
  getData()
  getLimit()
})

const columns = shallowRef([])
const tableData = shallowRef([])
const total = ref(null)

const handleNodeClick = (data: BusiTreeNode) => {
  if (!data.leaf) {
    datasetListTree.value.setCurrentKey(null)
    return
  }
  barInfoApi(data.id).then(res => {
    const nodeData = res as unknown as Node[]
    Object.assign(nodeInfo, nodeData)
    nodeInfo.weight = data.weight
    nodeInfo.ext = data.ext || 0
    columnsPreview = []
    dataPreview = []
    activeName.value = 'dataPreview'
    handleClick(activeName.value)
  })
}

const exportDataset = () => {
  showExport.value = true
  exportForm.value.name = nodeInfo.name
  exportForm.value.expressionTree = ''
  nextTick(() => {
    rowAuth.value.init({})
    rowAuth.value.relationList = []
    rowAuth.value.logic = 'or'
  })
}

const closeExport = () => {
  showExport.value = false
}

const save = ({ logic, items, errorMessage }) => {
  table.value.id = nodeInfo.id
  table.value.row = 100000
  table.value.filename = exportForm.value.name
  table.value.dataEaseBi = isDataEaseBi.value || appStore.getIsIframe
  if (errorMessage) {
    ElMessage.error(errorMessage)
    return
  }
  table.value.expressionTree = JSON.stringify({ items, logic })
  exportDatasetLoading.value = true
  exportDatasetData(table.value)
    .then(res => {
      if (isDataEaseBi.value || appStore.getIsIframe) {
        const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = table.value.filename + '.xlsx' // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        openMessageLoading(exportData)
      }
    })
    .finally(() => {
      exportDatasetLoading.value = false
      showExport.value = false
    })
}

const exportDatasetRequest = () => {
  exportFormRef.value.validate(valid => {
    if (valid) {
      rowAuth.value.submit()
    } else {
      return false
    }
  })
}

const exportData = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'IN_PROGRESS' })
}

const rowClick = (_, __, event) => {
  const element = event.target.parentNode.parentNode
  if ([...element.classList].includes('no-hide')) {
    element.classList.remove('no-hide')
    return
  }
  element.classList.add('no-hide')
}

const openMessageLoading = cb => {
  const iconClass = `el-icon-loading`
  const customClass = `de-message-loading de-message-export`
  ElMessage({
    message: h('p', null, [
      t('data_set.can_go_to'),
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
      t('data_set.progress_and_download')
    ]),
    iconClass,
    icon: h(RefreshLeft),
    showClose: true,
    customClass
  })
}

const editorDataset = () => {
  handleEdit(nodeInfo.id)
}
const embedded = useEmbedded()

const handleEdit = id => {
  if (isDataEaseBi.value) {
    embedded.clearState()
    embedded.setDatasetId(id as string)
    useEmitt().emitter.emit('changeCurrentComponent', 'DatasetEditor')
    return
  }
  router.push({
    path: '/dataset-form',
    query: {
      id
    }
  })
}

const createDataset = (data?: BusiTreeNode) => {
  if (isDataEaseBi.value) {
    embedded.clearState()
    embedded.setdatasetPid(data?.id as string)
    useEmitt().emitter.emit('changeCurrentComponent', 'DatasetEditor')
    return
  }
  router.push({
    path: '/dataset-form',
    query: {
      pid: data?.id
    }
  })
}

const handleClick = (tabName: TabPaneName) => {
  switch (tabName) {
    case 'dataPreview':
      if (columnsPreview.length) {
        columns.value = columnsPreview
        tableData.value = dataPreview
        break
      }
      dataPreviewLoading.value = true
      total.value = null
      getDatasetPreview(nodeInfo.id)
        .then(res => {
          allFields = (res?.allFields as unknown as Field[]) || []
          datasetTableFiled.value = allFields
          columnsPreview = generateColumns((res?.data?.fields as Field[]) || [])
          dataPreview = (res?.data?.data as Array<{}>) || []
          columns.value = columnsPreview
          tableData.value = dataPreview
        })
        .finally(() => {
          dataPreviewLoading.value = false
        })
      getDatasetTotal(nodeInfo.id).then(res => {
        total.value = res
      })
      break
    case 'structPreview':
      columns.value = allFieldsColumns
      tableData.value = allFields
      break
    case 'row':
      break
    case 'column':
      break
    default:
      break
  }
}
const relationChartRef = ref()
const operation = (cmd: string, data: BusiTreeNode, nodeType: string) => {
  if (cmd === 'copy') {
    if (isDataEaseBi.value) {
      embedded.clearState()
      embedded.setDatasetCopyId(data.id as string)
      useEmitt().emitter.emit('changeCurrentComponent', 'DatasetEditor')
      return
    }
    router.push({
      name: embedded.getToken && appStore.getIsIframe ? 'dataset-embedded-form' : 'dataset-form',
      params: {
        id: data.id
      }
    })
    return
  }
  if (cmd === 'delete') {
    let options = {
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false,
      tip: ''
    }

    if (!!data.children?.length) {
      options.tip = t('data_set.operate_with_caution')
    } else {
      delete options.tip
    }

    if (nodeType !== 'folder') {
      perDelete(data.id).then(res => {
        if (res === true) {
          const onClick = () => {
            relationChartRef.value.getChartData({
              queryType: 'dataset',
              num: data.id,
              label: data.name
            })
          }

          ElMessageBox.confirm('', {
            confirmButtonType: 'danger',
            type: 'warning',
            autofocus: false,
            confirmButtonText: t('userimport.sure'),
            showClose: false,
            dangerouslyUseHTMLString: true,
            message: h('div', null, [
              h('p', { style: 'margin-bottom: 8px;' }, t('data_set.this_data_set')),
              h('p', { class: 'tip' }, t('data_set.to_delete_them')),
              h(
                ElButton,
                { text: true, onClick: onClick, style: 'margin-left: -4px;' },
                t('data_set.check_blood_relationship')
              )
            ])
          }).then(() => {
            delDatasetTree(data.id).then(() => {
              getData()
              ElMessage.success(t('dataset.delete_success'))
            })
          })
        } else {
          ElMessageBox.confirm(
            t('datasource.delete_this_dataset'),
            options as ElMessageBoxOptions
          ).then(() => {
            delDatasetTree(data.id).then(() => {
              getData()
              ElMessage.success(t('dataset.delete_success'))
            })
          })
        }
      })
    } else {
      ElMessageBox.confirm(t('data_set.delete_this_folder'), options as ElMessageBoxOptions).then(
        () => {
          delDatasetTree(data.id).then(() => {
            getData()
            ElMessage.success(t('dataset.delete_success'))
          })
        }
      )
    }
  } else {
    creatDsFolder.value.createInit(nodeType, data, cmd)
  }
}

const handleDatasetTree = (cmd: string, data?: BusiTreeNode) => {
  if (cmd === 'dataset') {
    createDataset(data)
  }
  if (cmd === 'folder') {
    creatDsFolder.value.createInit(cmd, data || {})
  }
}

const activeName = ref('dataPreview')

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
const expandedKey = ref([])

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

const datasetTypeList = computed(() => {
  return [
    {
      label: t('data_set.a_new_dataset'),
      svgName: icon_dataset,
      command: 'dataset'
    },
    {
      label: t('deDataset.new_folder'),
      divided: true,
      svgName: dvFolder,
      command: 'folder'
    }
  ]
})

const defaultProps = {
  children: 'children',
  label: 'name'
}

const defaultTab = [
  {
    title: t('chart.data_preview'),
    name: 'dataPreview'
  },
  {
    title: t('data_set.structure_preview'),
    name: 'structPreview'
  }
]

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
  const historyTreeSort = wsCache.get('TreeSort-dataset')
  if (historyTreeSort) {
    state.curSortType = historyTreeSort
  }
}

const getLimit = () => {
  exportLimit().then(res => {
    limit.value = res
  })
}

const sortTypeTip = computed(() => {
  return sortList.find(ele => ele.value === state.curSortType).name
})

const tablePanes = ref([])
const tablePaneList = computed(() => {
  return nodeInfo.weight >= 7 ? [...defaultTab, ...tablePanes.value] : [...defaultTab]
})
const panelLoad = paneInfo => {
  tablePanes.value = paneInfo
}
const datasetListTree = ref()

watch(nickName, (val: string) => {
  datasetListTree.value.filter(val)
})
const sideTreeStatus = ref(true)
const changeSideTreeStatus = val => {
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

const getMenuList = (val: boolean) => {
  return !val
    ? menuList
    : [
        {
          label: t('common.copy'),
          svgName: icon_copy_filled,
          command: 'copy'
        }
      ].concat(menuList)
}

const proxyAllowDrop = debounce((arg1, arg2) => {
  const flagArray = ['dashboard', 'dataV', 'dataset', 'datasource']
  const flag = flagArray.findIndex(item => item === 'dataset')
  if (flag < 0 || !isFreeFolder(arg2, flag + 1)) {
    return allowDrop(arg1, arg2)
  }
  ElMessage.warning(t('free.save_error'))
  return false
}, 300)
</script>

<template>
  <div class="dataset-manage" :class="isIframe && 'de-100vh'" v-loading="dtLoading">
    <ArrowSide
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
            <span class="title"> {{ t('auth.dataset') }} </span>
            <div v-if="rootManage" class="flex-align-center">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('deDataset.new_folder')"
                placement="top"
              >
                <el-icon
                  class="custom-icon btn"
                  style="margin-right: 20px"
                  @click="handleDatasetTree('folder')"
                >
                  <Icon name="dv-new-folder"><dvNewFolder class="svg-icon" /></Icon>
                </el-icon>
              </el-tooltip>
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('data_set.a_new_dataset')"
                placement="top"
              >
                <el-icon class="custom-icon btn" @click="createDataset">
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
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon name="dv-sort-asc" class="opt-icon"
                  ><dvSortAsc v-if="state.curSortType.includes('asc')" class="svg-icon opt-icon"
                /></Icon>
              </el-tooltip>
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon name="dv-sort-desc" class="opt-icon"
                  ><dvSortDesc v-if="state.curSortType.includes('desc')" class="svg-icon"
                /></Icon>
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
            ref="datasetListTree"
            node-key="id"
            :data="state.datasetTree"
            :filter-node-method="filterNode"
            expand-on-click-node
            highlight-current
            @node-drag-start="handleDragStart"
            :allow-drop="proxyAllowDrop"
            @node-drop="handleDrop"
            draggable
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            :default-expanded-keys="expandedKey"
            :props="defaultProps"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="!data.leaf" style="font-size: 18px">
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>
                <el-icon v-if="data.leaf" style="font-size: 18px">
                  <Icon name="icon_dataset"><icon_dataset class="svg-icon" /></Icon>
                </el-icon>
                <span :title="node.label" class="label-tooltip ellipsis">{{ node.label }}</span>
                <div class="icon-more" v-if="data.weight >= 7">
                  <handle-more
                    icon-size="24px"
                    @handle-command="cmd => handleDatasetTree(cmd, data)"
                    :menu-list="datasetTypeList"
                    :icon-name="icon_add_outlined"
                    placement="bottom-start"
                    v-if="!data.leaf"
                  ></handle-more>
                  <el-icon v-else class="hover-icon" @click.stop="handleEdit(data.id)">
                    <icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></icon>
                  </el-icon>
                  <handle-more
                    @handle-command="cmd => operation(cmd, data, data.leaf ? 'dataset' : 'folder')"
                    :menu-list="getMenuList(data.leaf)"
                  ></handle-more>
                </div>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </el-aside>

    <div
      class="dataset-content"
      :class="{
        auto: isIframe || isDataEaseBi
      }"
    >
      <template v-if="!state.datasetTree.length && mounted">
        <empty-background :description="t('data_set.data_set_yet')" img-type="none">
          <el-button v-if="rootManage" @click="() => createDataset()" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
            </template>
            {{ t('deDataset.create') + t('auth.dataset') }}</el-button
          >
        </empty-background>
      </template>
      <template v-else-if="!!nodeInfo.id">
        <div class="dataset-info">
          <div class="info-method">
            <span :title="nodeInfo.name" class="dataset-name ellipsis">{{ nodeInfo.name }}</span>
            <el-divider style="margin: 0 12px" direction="vertical" />
            <span class="create-user">
              {{ t('visualization.create_by') }}:{{ nodeInfo.creator }}
            </span>

            <el-popover show-arrow :offset="8" placement="bottom" width="290" trigger="hover">
              <template #reference>
                <el-icon size="16px" class="create-user">
                  <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
                </el-icon>
              </template>
              <dataset-detail
                :create-time="infoList.createTime"
                :creator="infoList.creator"
              ></dataset-detail>
            </el-popover>
            <div class="right-btn">
              <el-button secondary @click="createPanel('dashboard')" v-permission="['panel']">
                <template #icon>
                  <Icon name="icon_dashboard_outlined"
                    ><icon_dashboard_outlined class="svg-icon"
                  /></Icon>
                </template>
                {{ t('visualization.panelAdd') }}
              </el-button>
              <el-button secondary @click="createPanel('dvCanvas')" v-permission="['screen']">
                <template #icon>
                  <Icon name="icon_operation-analysis_outlined"
                    ><icon_operationAnalysis_outlined class="svg-icon"
                  /></Icon> </template
                >{{ t('data_set.new_data_screen') }}
              </el-button>
              <el-button v-if="exportPermissions[0]" secondary @click="exportDataset">
                <template #icon>
                  <Icon name="icon_download_outlined"
                    ><icon_download_outlined class="svg-icon"
                  /></Icon>
                </template>
                {{ t('data_set.dataset_export') }}
              </el-button>
              <el-button type="primary" @click="editorDataset" v-if="nodeInfo.weight >= 7">
                <template #icon>
                  <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                </template>
                {{ t('visualization.edit') }}
              </el-button>
            </div>
          </div>
          <div class="tab-border">
            <el-tabs v-model="activeName" @tab-change="handleClick">
              <el-tab-pane
                v-for="ele in tablePaneList"
                :key="ele.name"
                :label="ele.title"
                :name="ele.name"
              ></el-tab-pane>
            </el-tabs>
            <XpackComponent
              jsname="L2NvbXBvbmVudC9yb3ctY29sLXBlcm1pc3Npb24vcGFuZS9pbmRleA=="
              @loaded="panelLoad"
            />
          </div>
        </div>
        <div class="dataset-table-info">
          <div v-if="activeName === 'dataPreview'" class="preview-num">
            {{ t('data_set.pieces_in_total', { msg: total }) }}
          </div>
          <template v-if="['dataPreview', 'structPreview'].includes(activeName)">
            <div class="info-table" :class="[{ 'struct-preview': activeName === 'structPreview' }]">
              <el-auto-resizer v-if="activeName === 'structPreview'">
                <template #default="{ height, width }">
                  <el-table-v2
                    key="structPreview"
                    :columns="columns"
                    v-loading="dataPreviewLoading"
                    :data="tableData"
                    header-class="excel-header-cell"
                    :width="width"
                    :height="height"
                    fixed
                    ><template #empty>
                      <empty-background
                        :description="t('data_set.no_data')"
                        img-type="noneWhite"
                      /> </template
                  ></el-table-v2>
                </template>
              </el-auto-resizer>
              <template v-if="activeName === 'dataPreview'">
                <el-table
                  v-loading="dataPreviewLoading"
                  class="dataset-preview_table"
                  :data="tableData"
                  @row-click="rowClick"
                  key="dataPreview"
                  border
                  style="width: 100%; height: 100%"
                >
                  <el-table-column
                    :key="column.dataKey"
                    v-for="(column, index) in columns"
                    :prop="column.dataKey"
                    :label="column.title"
                    :min-width="150"
                    :fixed="columns.length - 1 === index ? 'right' : false"
                  >
                    <template #header>
                      <div class="flex-align-center">
                        <ElIcon style="margin-right: 6px">
                          <Icon :className="`field-icon-${fieldType[column.deType]}`"
                            ><component
                              class="svg-icon"
                              :class="`field-icon-${fieldType[column.deType]}`"
                              :is="iconFieldMap[fieldType[column.deType]]"
                            ></component
                          ></Icon>
                        </ElIcon>
                        <span class="ellipsis" :title="column.title" style="width: 120px">
                          {{ column.title }}
                        </span>
                      </div>
                    </template>
                  </el-table-column>
                  <template #empty>
                    <empty-background :description="t('data_set.no_data')" img-type="noneWhite" />
                  </template>
                </el-table>
              </template>
            </div>
          </template>
          <template v-if="['row', 'column'].includes(activeName)">
            <div class="table-row-column">
              <XpackComponent
                :active-name="activeName"
                :dataset-id="nodeInfo.id"
                jsname="ZGF0YXNldC1yb3ctcGVybWlzc2lvbnM="
              />
              <XpackComponent
                :active-name="activeName"
                :dataset-id="nodeInfo.id"
                jsname="ZGF0YXNldC1jb2x1bW4tcGVybWlzc2lvbnM="
              />
            </div>
          </template>
        </div>
      </template>
      <template v-else-if="mounted">
        <empty-background :description="t('deDataset.on_the_left')" img-type="select" />
      </template>
    </div>
    <relationChart ref="relationChartRef"></relationChart>
    <de-resource-group-opt
      :cur-canvas-type="curCanvasType"
      @finish="resourceOptFinish"
      ref="resourceGroupOpt"
    ></de-resource-group-opt>
    <creat-ds-group @finish="getData()" ref="creatDsFolder"></creat-ds-group>
  </div>
  <!--导出数据集弹框-->
  <el-dialog
    v-if="showExport"
    v-model="showExport"
    width="800px"
    class="de-dialog-form form-tree-cont"
    :title="$t('dataset.export_dataset')"
    append-to-body
  >
    <el-form
      ref="exportFormRef"
      class="de-form-item"
      @submit.prevent
      :model="exportForm"
      :rules="exportFormRules"
      :before-close="closeExport"
    >
      <el-form-item :label="$t('dataset.filename')" prop="name">
        <el-input v-model.trim="exportForm.name" :placeholder="$t('dataset.pls_input_filename')" />
      </el-form-item>
      <el-form-item :label="$t('dataset.export_filter')" prop="expressionTree">
        <div class="tree-cont">
          <div class="content">
            <RowAuth @save="save" ref="rowAuth" />
          </div>
        </div>
      </el-form-item>
    </el-form>
    <span class="tip">{{ t('data_set.pieces_of_data', { limit: limit }) }}</span>
    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button secondary @click="closeExport">{{ $t('dataset.cancel') }} </el-button>
        <el-button v-loading="exportDatasetLoading" type="primary" @click="exportDatasetRequest"
          >{{ $t('dataset.confirm') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

:deep(.dataset-preview_table) {
  .ed-table__body {
    .ed-table__row:not(.no-hide) {
      .cell {
        white-space: nowrap;
      }
    }
  }
}

.ed-table {
  --ed-table-header-bg-color: #f5f6f7;
}
.form-tree-cont {
  .tree-cont {
    height: 200px;
    width: 100%;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid var(--deBorderBase, #dcdfe6);
    overflow: auto;

    .content {
      height: 100%;
      width: 100%;
    }
  }
}
.filter-icon-span {
  border: 1px solid #bbbfc4;
  width: 32px;
  height: 32px;
  border-radius: 4px;
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
.dataset-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  position: relative;

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
          &.btn {
            color: var(--ed-color-primary);
          }

          &:hover {
            cursor: pointer;
          }
        }
      }

      .search-bar {
        padding-bottom: 10px;
        width: calc(100% - 40px);
      }
    }
  }

  .dataset-height,
  .dataset-content {
    height: calc(100vh - 56px);
    overflow: auto;
    position: relative;
  }

  .dataset-content {
    background: #f5f6f7;
    &.auto {
      height: auto;
    }

    :deep(.ed-table-v2__header-cell) {
      background-color: #f5f6f7 !important;
    }
  }

  .dataset-list {
    width: 279px;
    padding: 16px 8px;
  }

  .dataset-content {
    flex: 1;
    position: relative;

    .dataset-info {
      background: #fff;
      padding: 0 24px;
      padding-top: 12px;
      height: 90px;
      .info-method {
        height: 32px;
        width: 100%;
        display: flex;
        align-items: center;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 16px;
        font-weight: 500;

        .dataset-name {
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

    .dataset-table-info {
      padding: 24px;
      margin: 24px;
      background: #fff;
      border-radius: 4px;
      height: calc(100% - 138px);
    }

    .preview-num {
      color: var(--deTextSecondary, #606266);
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin-bottom: 16px;
    }

    .info-table {
      height: calc(100% - 37px);
    }

    .struct-preview {
      height: 100%;
    }

    .table-row-column {
      height: calc(100% - 50px);
      :deep(.add-row-column) {
        margin-bottom: 16px;
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
</style>
