<script lang="tsx" setup>
import { computed, reactive, ref, shallowRef, nextTick, watch, onMounted } from 'vue'
import { dsTypes } from '@/views/visualized/data/datasource/form/option'
import type { TabPaneName, ElMessageBoxOptions } from 'element-plus-secondary'
import { ElIcon, ElMessageBox, ElMessage, ElScrollbar, ElAside } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { fieldType } from '@/utils/attr'
import { listSyncRecord, uploadFile } from '@/api/datasource'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { Tree } from '../dataset/form/CreatDsGroup.vue'
import { previewData, getById } from '@/api/datasource'
import { useI18n } from '@/hooks/web/useI18n'
import { useRouter } from 'vue-router'
import DatasetDetail from '@/views/visualized/data/dataset/DatasetDetail.vue'
import { timestampFormatDate } from '@/views/visualized/data/dataset/form/util'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import dayjs from 'dayjs'
import { useAppStoreWithOut } from '@/store/modules/app'
import {
  getTableField,
  listDatasourceTables,
  deleteById,
  save,
  validateById,
  syncApiDs,
  syncApiTable
} from '@/api/datasource'
import { Base64 } from 'js-base64'
import type { Configuration, ApiConfiguration, SyncSetting } from './form/index.vue'
import EditorDatasource from './form/index.vue'
import ExcelInfo from './ExcelInfo.vue'
import SheetTabs from './SheetTabs.vue'
import BaseInfoItem from './BaseInfoItem.vue'
import BaseInfoContent from './BaseInfoContent.vue'
import type { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { cloneDeep } from 'lodash-es'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
const interactiveStore = interactiveStoreWithOut()
interface Field {
  fieldShortName: string
  name: string
  dataeaseName: string
  originName: string
  deType: number
}

export interface Node {
  name: string
  createBy: string
  creator: string
  createTime: string
  id: number | string
  size: number
  description: string
  type: string
  nodeType: string
  fileName: string
  syncSetting?: SyncSetting
  editType?: number
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
  weight?: number
  lastSyncTime?: number | string
}

const { t } = useI18n()
const router = useRouter()
const appStore = useAppStoreWithOut()
const state = reactive({
  datasourceTree: [] as BusiTreeNode[],
  dsTableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  filterTable: []
})

const recordState = reactive({
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

const createDataset = (tableName?: string) => {
  router.push({
    path: '/dataset-form',
    query: {
      datasourceId: nodeInfo.id,
      tableName
    }
  })
}

const { width, node } = useMoveLine('DATASOURCE')

const dsTableDetail = reactive({
  tableName: '',
  remark: ''
})
const rootManage = ref(false)
const nickName = ref('')
const dsName = ref('')
const userDrawer = ref(false)
const rawDatasourceList = ref([])
const showPriority = ref(true)
const datasourceEditor = ref()
const activeTab = ref('')
const menuList = [
  {
    label: '移动到',
    svgName: 'icon_into-item_outlined',
    command: 'move'
  },
  {
    label: '重命名',
    svgName: 'icon_rename_outlined',
    command: 'rename'
  },
  {
    label: '删除',
    divided: true,
    svgName: 'icon_delete-trash_outlined',
    command: 'delete'
  }
]

const typeMap = dsTypes.reduce((pre, next) => {
  pre[next.type] = next.name
  return pre
}, {})

const datasetTypeList = computed(() => {
  const list = [
    {
      label: '新建数据源',
      svgName: 'icon_dataset',
      command: 'datasource'
    },
    {
      label: '新建文件夹',
      divided: true,
      svgName: 'dv-folder',
      command: 'folder'
    }
  ]
  if (isDataEaseBi.value) {
    list.shift()
    list[0].divided = false
  }
  return list
})

const dsTableDataLoading = ref(false)
const selectDataset = row => {
  Object.assign(dsTableDetail, row)
  userDrawer.value = true
  dsTableDataLoading.value = true
  getTableField({ tableName: row.tableName, datasourceId: nodeInfo.id })
    .then(res => {
      state.dsTableData = res.data
    })
    .finally(() => {
      dsTableDataLoading.value = false
    })
}

const handleSizeChange = pageSize => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = pageSize
}
const handleCurrentChange = currentPage => {
  state.paginationConfig.currentPage = currentPage
}

const handleRecordSizeChange = pageSize => {
  recordState.paginationConfig.currentPage = 1
  recordState.paginationConfig.pageSize = pageSize
  getRecord()
}
const handleRecordCurrentChange = currentPage => {
  recordState.paginationConfig.currentPage = currentPage
  getRecord()
}

let listScrollTop = 0
const handleScroll = val => {
  listScrollTop = val.scrollTop
}

const scrollbarRef = ref()

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    deType: ele.deType,
    dataKey: ele.originName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center">
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon
            name={`field_${fieldType[column.deType]}`}
            className={`field-icon-${fieldType[column.deType]}`}
          ></Icon>
        </ElIcon>
        <span class="ellipsis" title={column.title} style={{ width: '120px' }}>
          {column.title}
        </span>
      </div>
    )
  }))

const dataPreviewLoading = ref(false)
const columns = ref([])
const handleLoadExcel = data => {
  dataPreviewLoading.value = true
  previewData(data)
    .then(res => {
      columns.value = generateColumns((res?.data?.fields as Field[]) || [])
      tabData.value = (res?.data?.data as Array<{}>) || []
    })
    .finally(() => {
      dataPreviewLoading.value = false
    })
}

const validateDS = () => {
  validateById(nodeInfo.id as number)
    .then(res => {
      if (res.data.type === 'API') {
        let error = 0
        const status = JSON.parse(res.data.status)
        for (let i = 0; i < status.length; i++) {
          if (status[i].status === 'Error') {
            error++
          }
        }
        if (error === 0) {
          ElMessage.success('校验成功')
        } else {
          ElMessage.error('校验失败')
        }
      } else {
        ElMessage.success('校验成功')
      }
    })
    .catch(() => {
      ElMessage.error('校验失败')
    })
}

const dialogErrorInfo = ref(false)
const dialogMsg = ref('')

const formatSimpleCron = (info?: SyncSetting) => {
  const { syncRate, simpleCronValue, simpleCronType, startTime, endTime, cron, endLimit } = info
  let start = '-'
  let end = '-'
  if (startTime) {
    start = dayjs(new Date(startTime)).format('YYYY-MM-DD HH:mm:ss')
  }
  if (endLimit === '1' && endTime) {
    end = dayjs(new Date(endTime)).format('YYYY-MM-DD HH:mm:ss')
  }
  if (endLimit === '0') {
    end = '无限制'
  }
  let strArr = []
  switch (syncRate) {
    case 'RIGHTNOW':
      strArr.push(t('dataset.execute_once'))
      break
    case 'CRON':
      strArr.push(`${t('dataset.cron_config')}: ${cron}`)
      strArr.push(`${t('dataset.start_time')}: ${start}`)
      strArr.push(`${t('dataset.end_time')}: ${end}`)
      break
    case 'SIMPLE_CRON':
      const type = t(`common.${simpleCronType}`)
      strArr.push(
        `${t('dataset.simple_cron')}: ${t('common.every')}${simpleCronValue}${type}更新一次`
      )
      strArr.push(`${t('dataset.start_time')}: ${start}`)
      strArr.push(`${t('dataset.end_time')}: ${end}`)
      break
    default:
      break
  }

  return strArr
}

const showErrorInfo = info => {
  dialogMsg.value = info
  dialogErrorInfo.value = true
}

const getDsIconName = data => {
  if (!data.leaf) return 'dv-folder'
  return `${data.type}-ds`
}

const handleTabClick = tab => {
  activeTab.value = tab.value
  handleLoadExcel({ table: tab.value, id: nodeInfo.id })
}

const tabList = shallowRef([])

const initSearch = () => {
  handleCurrentChange(1)
  state.filterTable = tableData.value.filter(ele => ele.tableName.includes(nickName.value))
  state.paginationConfig.total = state.filterTable.length
}

const pagingTable = computed(() => {
  const { currentPage, pageSize } = state.paginationConfig
  return state.filterTable.slice((currentPage - 1) * pageSize, currentPage * pageSize)
})

const defaultInfo = {
  name: '',
  createBy: '',
  creator: '',
  createTime: '',
  description: '',
  id: 0,
  size: 0,
  nodeType: '',
  type: '',
  fileName: '',
  configuration: null,
  syncSetting: null,
  apiConfiguration: [],
  weight: 0
}
const nodeInfo = reactive<Node>(cloneDeep(defaultInfo))
const infoList = computed(() => {
  return {
    creator: nodeInfo.creator,
    createTime: timestampFormatDate(nodeInfo.createTime)
  }
})
const saveDsFolder = (params, successCb, finallyCb, cmd) => {
  save(params)
    .then(res => {
      if (res !== undefined) {
        successCb()
        switch (cmd) {
          case 'move':
            ElMessage.success('移动成功')
            break
          case 'rename':
            ElMessage.success('重命名成功')
            break
          default:
            ElMessage.success('新建成功')
            break
        }
        listDs()
      }
    })
    .finally(() => {
      finallyCb()
    })
}

const dsLoading = ref(false)

const listDs = () => {
  rawDatasourceList.value = []
  dsLoading.value = true
  const request = { busiFlag: 'datasource' } as BusiTreeRequest
  interactiveStore
    .setInteractive(request)
    .then(res => {
      const nodeData = (res as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        rootManage.value = nodeData[0]['weight'] >= 7
        state.datasourceTree = nodeData[0]['children'] || []
        return
      }
      state.datasourceTree = nodeData
    })
    .finally(() => {
      dsLoading.value = false
      updateTreeExpand()
      const id = nodeInfo.id
      if (!!id) {
        Object.assign(nodeInfo, cloneDeep(defaultInfo))
        dfsDatasourceTree(state.datasourceTree, id)
      }
    })
}

const dfsDatasourceTree = (ds, id) => {
  ds.some(ele => {
    if (ele.id === id) {
      handleNodeClick(ele)
      return true
    }
    if (!!ele.children?.length) {
      dfsDatasourceTree(ele.children, id)
    }
    return false
  })
}

listDs()

const creatDsFolder = ref()

const tableData = shallowRef([])
const tabData = shallowRef([])
const handleNodeClick = data => {
  if (!data.leaf) {
    dsListTree.value.setCurrentKey(null)
    return
  }
  return getById(data.id).then(res => {
    let {
      name,
      createBy,
      id,
      createTime,
      creator,
      type,
      pid,
      configuration,
      syncSetting,
      apiConfigurationStr,
      fileName,
      size,
      description,
      lastSyncTime
    } = res.data
    if (configuration) {
      configuration = JSON.parse(Base64.decode(configuration))
    }
    if (apiConfigurationStr) {
      apiConfigurationStr = JSON.parse(Base64.decode(apiConfigurationStr))
    }
    Object.assign(nodeInfo, {
      name,
      pid,
      description,
      fileName,
      size,
      createTime,
      creator,
      createBy,
      id,
      type,
      configuration,
      syncSetting,
      apiConfiguration: apiConfigurationStr,
      weight: data.weight,
      lastSyncTime
    })
    activeTab.value = ''
    activeName.value = 'config'
    nickName.value = ''
    handleCurrentChange(1)
    handleClick(activeName.value)
  })
}
const createDatasource = (data?: Tree) => {
  datasourceEditor.value.init(null, data?.id)
}
const showRecord = ref(false)
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

const recordData = ref([])

const getRecord = () => {
  showRecord.value = true
  listSyncRecord(
    recordState.paginationConfig.currentPage,
    recordState.paginationConfig.pageSize,
    nodeInfo.id
  ).then(res => {
    recordData.value = res.data.records
    recordState.paginationConfig.total = res.data.total
  })
}

const updateApiTable = api => {
  syncApiTable({ name: api.name, tableName: api.deTableName, datasourceId: nodeInfo.id }).then(
    () => {
      ElMessage.success(t('datasource.req_completed'))
    }
  )
}

const updateApiDs = () => {
  syncApiDs({ datasourceId: nodeInfo.id }).then(() => {
    ElMessage.success(t('datasource.req_completed'))
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
  return data.name?.includes(value)
}

const editDatasource = (editType?: number) => {
  if (nodeInfo.type === 'Excel') {
    nodeInfo.editType = editType
  }
  datasourceEditor.value.init(nodeInfo)
}

const handleEdit = async data => {
  await handleNodeClick(data)
  datasourceEditor.value.init(nodeInfo)
}

const handleCopy = async data => {
  await handleNodeClick(data)
  nodeInfo.id = ''
  nodeInfo.name = '复制数据源'
  datasourceEditor.value.init(nodeInfo)
}

const handleDatasourceTree = (cmd: string, data?: Tree) => {
  if (cmd === 'datasource') {
    createDatasource(data)
  }
  if (cmd === 'folder') {
    creatDsFolder.value.createInit(cmd, data || {})
  }
}
const operation = (cmd: string, data: Tree, nodeType: string) => {
  if (cmd === 'copy') {
    handleCopy(data)
    return
  }
  if (cmd === 'delete') {
    let options = {
      confirmButtonText: t('common.sure'),
      cancelButtonText: t('common.cancel'),
      confirmButtonType: 'danger',
      type: 'warning',
      tip: '',
      autofocus: false,
      showClose: false
    }
    if (!!data.children?.length) {
      options.tip = '删除后，此文件夹下的所有资源都会被删除，请谨慎操作。'
    } else {
      delete options.tip
    }
    ElMessageBox.confirm(
      nodeType === 'folder' ? '确定删除该文件夹吗' : t('datasource.this_data_source'),
      options as ElMessageBoxOptions
    ).then(() => {
      deleteById(data.id as number).then(() => {
        if (data.id === nodeInfo.id) {
          Object.assign(nodeInfo, cloneDeep(defaultInfo))
        }
        listDs()
        ElMessage.success(t('dataset.delete_success'))
      })
    })
  } else {
    creatDsFolder.value.createInit(nodeType, data, cmd)
  }
}

const handleClick = (tabName: TabPaneName) => {
  switch (tabName) {
    case 'config':
      listDatasourceTables({ datasourceId: nodeInfo.id }).then(res => {
        tabList.value = res.data.map(ele => {
          const { name, tableName } = ele
          return {
            value: name,
            label: tableName
          }
        })
        if (!!tabList.value.length && !activeTab.value) {
          activeTab.value = tabList.value[0].value
          if (nodeInfo.type === 'Excel') {
            handleTabClick(activeTab)
          }
        }
        tableData.value = res.data
      })
      break
    case 'table':
      initSearch()
      break
    default:
      break
  }
}
const refresh = () => {
  listDs()
}

let fileList = null
const onChange = file => {
  fileList = file
}

const replaceLoading = ref(false)
const addLoading = ref(false)

const uploadExcel = editType => {
  const formData = new FormData()
  formData.append('file', fileList.raw)
  formData.append('type', '')
  formData.append('editType', editType)
  formData.append('id', (nodeInfo.id || 0) as string)
  replaceLoading.value = editType === 0
  addLoading.value = editType === 1
  return uploadFile(formData)
    .then(res => {
      nodeInfo.editType = editType
      datasourceEditor.value.init(nodeInfo, nodeInfo.id, res)
    })
    .finally(() => {
      replaceLoading.value = false
      addLoading.value = false
    })
}
const activeName = ref('table')
const defaultProps = {
  children: 'children',
  label: 'name'
}
onMounted(() => {
  const { opt } = router.currentRoute.value.query
  if (opt && opt === 'create') {
    datasourceEditor.value.init(null, null)
  }
})

const getMenuList = (val: boolean) => {
  return !val || isDataEaseBi.value
    ? menuList
    : [
        {
          label: t('common.copy'),
          svgName: 'icon_copy_filled',
          command: 'copy'
        }
      ].concat(menuList)
}
</script>

<template>
  <div class="datasource-manage" v-loading="dsLoading">
    <el-aside class="resource-area" ref="node" :style="{ width: width + 'px' }">
      <div class="resource-tree">
        <div class="tree-header">
          <div class="icon-methods">
            <span class="title"> {{ t('datasource.datasource') }} </span>
            <div v-if="rootManage" class="flex-align-center">
              <el-tooltip effect="dark" content="新建文件夹" placement="top">
                <el-icon
                  class="custom-icon btn"
                  :style="{ marginRight: isDataEaseBi ? 0 : '20px' }"
                  @click="handleDatasourceTree('folder')"
                >
                  <Icon name="dv-new-folder" />
                </el-icon>
              </el-tooltip>
              <el-tooltip
                v-if="!isDataEaseBi"
                effect="dark"
                :content="t('datasource.create')"
                placement="top"
              >
                <el-icon class="custom-icon btn" @click="createDatasource">
                  <Icon name="icon_file-add_outlined" />
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
                <Icon name="icon_search-outline_outlined" />
              </el-icon>
            </template>
          </el-input>
        </div>
        <el-scrollbar @scroll="handleScroll" ref="scrollbarRef" class="custom-tree">
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
              <span class="custom-tree-node">
                <el-icon :class="data.leaf && 'icon-border'" style="font-size: 18px">
                  <Icon :name="getDsIconName(data)"></Icon>
                </el-icon>
                <span
                  :title="node.label"
                  class="label-tooltip ellipsis"
                  :class="data.type === 'Excel' && 'excel'"
                  >{{ node.label }}</span
                >
                <div class="icon-more" v-if="data.weight >= 7">
                  <handle-more
                    icon-size="24px"
                    @handle-command="cmd => handleDatasourceTree(cmd, data)"
                    :menu-list="datasetTypeList"
                    icon-name="icon_add_outlined"
                    placement="bottom-start"
                    v-if="!data.leaf"
                  ></handle-more>
                  <el-icon
                    class="hover-icon"
                    @click.stop="handleEdit(data)"
                    v-else-if="data.type !== 'Excel' && !isDataEaseBi"
                  >
                    <icon name="icon_edit_outlined"></icon>
                  </el-icon>
                  <handle-more
                    @handle-command="
                      cmd => operation(cmd, data, data.leaf ? 'datasource' : 'folder')
                    "
                    :menu-list="getMenuList(!['Excel', 'API'].includes(data.type) && data.leaf)"
                  ></handle-more>
                </div>
              </span>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </el-aside>

    <div class="datasource-content" :class="isDataEaseBi && 'h100'">
      <template v-if="!state.datasourceTree.length">
        <empty-background description="暂无数据源" img-type="none">
          <el-button
            v-if="rootManage && !isDataEaseBi"
            @click="() => createDatasource()"
            type="primary"
          >
            <template #icon>
              <Icon name="icon_add_outlined"></Icon>
            </template>
            {{ t('datasource.create') }}</el-button
          >
        </empty-background>
      </template>
      <template v-else-if="!!nodeInfo.id">
        <div class="datasource-info">
          <div class="info-method">
            <el-icon class="icon-border">
              <Icon :name="`${nodeInfo.type}-ds`"></Icon>
            </el-icon>
            <span :title="nodeInfo.name" class="name ellipsis">
              {{ nodeInfo.name }}
            </span>
            <el-divider style="margin: 0 12px" direction="vertical" />
            <span class="create-user">
              {{ t('visualization.create_by') }}:{{ nodeInfo.creator }}
            </span>
            <el-popover :offset="8" show-arrow placement="bottom" width="290" trigger="hover">
              <template #reference>
                <el-icon size="16px" class="create-user">
                  <Icon name="icon_info_outlined"></Icon>
                </el-icon>
              </template>
              <dataset-detail
                :create-time="infoList.createTime"
                :creator="infoList.creator"
              ></dataset-detail>
            </el-popover>
            <div class="right-btn flex-align-center" v-if="!isDataEaseBi">
              <el-button secondary @click="createDataset(null)" v-permission="['dataset']">
                <template #icon>
                  <Icon name="icon_dataset_outlined"></Icon>
                </template>
                新建数据集
              </el-button>
              <el-button
                v-if="nodeInfo.type !== 'Excel' && nodeInfo.weight >= 7"
                secondary
                @click="validateDS"
              >
                {{ t('datasource.validate') }}</el-button
              >

              <template v-if="nodeInfo.type === 'Excel'">
                <el-upload
                  v-if="nodeInfo.weight >= 7"
                  action=""
                  :multiple="false"
                  ref="uploadAgain"
                  :show-file-list="false"
                  accept=".xls,.xlsx,.csv"
                  :on-change="onChange"
                  :http-request="() => uploadExcel(0)"
                  name="file"
                >
                  <template #trigger>
                    <el-button v-loading="replaceLoading" class="replace-excel" type="primary">
                      <template #icon>
                        <Icon name="icon_edit_outlined"></Icon>
                      </template>
                      替换数据
                    </el-button>
                  </template>
                </el-upload>

                <el-upload
                  v-if="nodeInfo.weight >= 7"
                  action=""
                  :multiple="false"
                  ref="uploadAgain"
                  :show-file-list="false"
                  accept=".xls,.xlsx,.csv"
                  :on-change="onChange"
                  :http-request="() => uploadExcel(1)"
                  name="file"
                >
                  <template #trigger>
                    <el-button v-loading="addLoading" type="primary">
                      <template #icon>
                        <Icon name="icon_new-item_outlined"></Icon>
                      </template>
                      追加数据
                    </el-button>
                  </template>
                </el-upload>
              </template>
              <el-button v-else-if="nodeInfo.weight >= 7" @click="editDatasource()" type="primary">
                <template #icon>
                  <Icon name="icon_edit_outlined"></Icon>
                </template>
                编辑
              </el-button>
            </div>
          </div>
          <div class="tab-border">
            <el-tabs v-model="activeName" @tab-change="handleClick">
              <el-tab-pane :label="t('datasource.config')" name="config"></el-tab-pane>
              <el-tab-pane :label="t('datasource.table')" name="table"></el-tab-pane>
            </el-tabs>
          </div>
        </div>
        <div v-if="activeName === 'table'" class="datasource-table">
          <div class="search-operate">
            <el-input
              ref="search"
              v-model="nickName"
              :placeholder="t('commons.search')"
              clearable
              @input="initSearch"
              style="width: 240px"
            >
              <template #prefix>
                <el-icon>
                  <Icon name="icon_search-outline_outlined"></Icon>
                </el-icon>
              </template>
            </el-input>
          </div>
          <div class="info-table">
            <grid-table
              :pagination="state.paginationConfig"
              :table-data="pagingTable"
              :is-search="!!nickName.trim()"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            >
              <el-table-column
                key="tableName"
                prop="tableName"
                :label="t('datasource.table_name')"
              />
              <el-table-column
                key="status"
                prop="status"
                v-if="['api'].includes(nodeInfo.type.toLowerCase())"
                label="最近更新状态"
              >
                <template #default="scope">
                  <div class="flex-align-center">
                    <template v-if="scope.row.status === 'Completed'">
                      <el-icon>
                        <icon name="icon_succeed_filled"></icon>
                      </el-icon>
                      {{ t('dataset.completed') }}
                    </template>
                    <template v-if="scope.row.status === 'UnderExecution'">
                      {{ t('dataset.underway') }}
                    </template>
                    <template v-if="scope.row.status === 'Error' || scope.row.status === 'Warning'">
                      <el-icon>
                        <icon class="field-icon-red" name="icon_close_filled"></icon>
                      </el-icon>
                      {{ t('dataset.error') }}
                    </template>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                key="lastUpdateTime"
                prop="lastUpdateTime"
                v-if="['excel', 'api'].includes(nodeInfo.type.toLowerCase())"
                label="最近更新时间"
              >
                <template v-slot:default="scope">
                  <span>{{ timestampFormatDate(scope.row.lastUpdateTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column
                key="__operation"
                :label="t('commons.operating')"
                fixed="right"
                width="108"
              >
                <template #default="scope">
                  <el-tooltip
                    v-if="!isDataEaseBi"
                    effect="dark"
                    content="新建数据集"
                    placement="top"
                  >
                    <el-button
                      @click.stop="createDataset(scope.row.name)"
                      text
                      v-permission="['dataset']"
                    >
                      <template #icon>
                        <Icon name="icon_dataset_outlined"></Icon>
                      </template>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip effect="dark" :content="t('visualization.details')" placement="top">
                    <el-button @click.stop="selectDataset(scope.row)" text>
                      <template #icon>
                        <Icon name="icon_describe_outlined"></Icon>
                      </template>
                    </el-button>
                  </el-tooltip>
                </template>
              </el-table-column>
            </grid-table>
          </div>
        </div>
        <template v-else>
          <BaseInfoContent v-slot="slotProps" :name="t('datasource.base_info')">
            <template v-if="slotProps.active">
              <el-row :gutter="24">
                <el-col :span="12">
                  <BaseInfoItem :label="t('auth.datasource') + t('common.name')">{{
                    nodeInfo.name
                  }}</BaseInfoItem>
                </el-col>
                <el-col :span="12">
                  <BaseInfoItem :label="t('datasource.type')">{{
                    typeMap[nodeInfo.type]
                  }}</BaseInfoItem>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col v-if="nodeInfo.type === 'Excel'" :span="12">
                  <BaseInfoItem label="文件">
                    <ExcelInfo :name="nodeInfo.fileName" :size="nodeInfo.size"></ExcelInfo>
                  </BaseInfoItem>
                </el-col>
                <el-col v-else :span="24">
                  <BaseInfoItem :label="t('common.description')">{{
                    nodeInfo.description
                  }}</BaseInfoItem>
                </el-col>
              </el-row>
              <template v-if="!['Excel', 'API'].includes(nodeInfo.type)">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.host')">{{
                      nodeInfo.configuration.host
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.port')">{{
                      nodeInfo.configuration.port
                    }}</BaseInfoItem>
                  </el-col>
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.data_base')">{{
                      nodeInfo.configuration.dataBase
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.user_name')">{{
                      nodeInfo.configuration.username
                    }}</BaseInfoItem>
                  </el-col>
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.extra_params')">{{
                      nodeInfo.configuration.extraParams
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <span
                  v-if="!['es', 'api'].includes(nodeInfo.type.toLowerCase())"
                  class="de-expand"
                  @click="showPriority = !showPriority"
                  >{{ t('datasource.priority') }}
                  <el-icon>
                    <Icon
                      :name="showPriority ? 'icon_down_outlined' : 'icon_down_outlined-1'"
                    ></Icon>
                  </el-icon>
                </span>
                <template v-if="showPriority">
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.initial_pool_size')">{{
                        nodeInfo.configuration.initialPoolSize || 5
                      }}</BaseInfoItem>
                    </el-col>
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.min_pool_size')">{{
                        nodeInfo.configuration.minPoolSize || 5
                      }}</BaseInfoItem>
                    </el-col>
                  </el-row>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.max_pool_size')">{{
                        nodeInfo.configuration.maxPoolSize || 5
                      }}</BaseInfoItem>
                    </el-col>
                    <el-col :span="12">
                      <BaseInfoItem
                        :value="nodeInfo.configuration.queryTimeout"
                        :label="t('datasource.query_timeout')"
                        >{{ nodeInfo.configuration.queryTimeout || '30'
                        }}{{ t('common.second') }}</BaseInfoItem
                      >
                    </el-col>
                  </el-row>
                </template>
              </template>
            </template>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'API'"
            v-slot="slotProps"
            :name="t('datasource.data_table')"
          >
            <div class="api-card-content" v-if="slotProps.active">
              <div v-for="api in nodeInfo.apiConfiguration" :key="api.id" class="api-card">
                <el-row>
                  <el-col :span="19">
                    <span class="name ellipsis">{{ api.name }}</span>
                    <span v-if="api.status === 'Error'" class="de-tag error-color">{{
                      t('datasource.invalid')
                    }}</span>
                    <span v-if="api.status === 'Success'" class="de-tag success-color">{{
                      t('datasource.valid')
                    }}</span>
                  </el-col>
                  <el-col style="text-align: right" :span="5">
                    <el-button @click="updateApiTable(api)" text>
                      <template #icon>
                        <icon name="icon_replace_outlined"></icon>
                      </template>
                    </el-button>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="19">
                    <span>数据时间： {{ timestampFormatDate(api['updateTime']) }}</span>
                  </el-col>
                </el-row>

                <div class="req-title">
                  <span>{{ t('datasource.method') }}</span>
                  <span>{{ t('datasource.url') }}</span>
                </div>
                <div class="req-value">
                  <span>{{ api.method }}</span>
                  <el-tooltip w effect="dark" :content="api.url" placement="top">
                    <span>{{ api.url }}</span>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <el-button @click="updateApiDs" class="update-records" text>
              <template #icon>
                <icon name="icon_replace_outlined"></icon>
              </template>
              全部更新
            </el-button>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'API'"
            v-slot="slotProps"
            :name="t('dataset.update_setting')"
            :time="(nodeInfo.lastSyncTime as string)"
          >
            <template v-if="slotProps.active">
              <el-row :gutter="24">
                <el-col :span="12">
                  <BaseInfoItem :label="t('dataset.update_type')">{{
                    t(`dataset.${nodeInfo.syncSetting.updateType}`)
                  }}</BaseInfoItem>
                </el-col>
                <el-col :span="12">
                  <BaseInfoItem :label="t('dataset.execute_rate')">
                    <p
                      class="value"
                      :key="ele"
                      v-for="ele in formatSimpleCron(nodeInfo.syncSetting)"
                    >
                      {{ ele }}
                    </p>
                  </BaseInfoItem>
                </el-col>
              </el-row>
            </template>
            <el-button @click="getRecord" class="update-records" text>
              <template #icon>
                <icon name="icon_describe_outlined"></icon>
              </template>
              {{ t('dataset.update_records') }}
            </el-button>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'Excel'"
            v-slot="slotProps"
            :name="t('dataset.data_preview')"
          >
            <template v-if="slotProps.active">
              <div class="excel-table">
                <SheetTabs
                  :active-tab="activeTab"
                  @tab-click="handleTabClick"
                  :tab-list="tabList"
                ></SheetTabs>
                <div class="sheet-table-content">
                  <el-auto-resizer>
                    <template #default="{ height, width }">
                      <el-table-v2
                        :columns="columns"
                        v-loading="dataPreviewLoading"
                        header-class="excel-header-cell"
                        :data="tabData"
                        :width="width"
                        :height="height"
                        fixed
                        ><template #empty>
                          <empty-background
                            description="暂无数据"
                            img-type="noneWhite"
                          /> </template
                      ></el-table-v2>
                    </template>
                  </el-auto-resizer>
                </div>
              </div>
            </template>
          </BaseInfoContent>
        </template>
      </template>
      <template v-else>
        <empty-background description="请在左侧选择数据源" img-type="select" />
      </template>
    </div>
    <EditorDatasource @refresh="refresh" ref="datasourceEditor"></EditorDatasource>
    <el-dialog
      :title="t('common.detail')"
      v-model="userDrawer"
      class="ds-table-drawer"
      width="840px"
      top="60px"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <p class="table-name">
            {{ t('datasource.table_name') }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.tableName }}
          </p>
        </el-col>
        <el-col :span="12">
          <p class="table-name">
            {{ t('datasource.table_description') }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.remark || '-' }}
          </p>
        </el-col>
      </el-row>
      <el-scrollbar>
        <el-table
          v-loading="dsTableDataLoading"
          header-cell-class-name="header-cell"
          :data="state.dsTableData"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="originName" :label="t('datasource.column_name')" />
          <el-table-column prop="type" :label="t('datasource.field_type')">
            <template #default="scope">
              <div class="flex-align-center icon">
                <el-icon>
                  <icon
                    :name="`field_${fieldType[scope.row.deType]}`"
                    :class="`field-icon-${fieldType[scope.row.deType]}`"
                  ></icon>
                </el-icon>
                {{
                  t(`dataset.${fieldType[scope.row.deType]}`) +
                  `${scope.row.deType === 3 ? '(' + t('dataset.float') + ')' : ''}`
                }}
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="remarks"
            show-overflow-tooltip
            :label="t('datasource.field_description')"
          />
        </el-table>
      </el-scrollbar>
    </el-dialog>
    <creat-ds-group @finish="saveDsFolder" ref="creatDsFolder"></creat-ds-group>
    <el-drawer
      v-model="showRecord"
      :title="t('dataset.update_records')"
      :close-on-press-escape="false"
      :close-on-click-modal="false"
      modal-class="record-drawer"
      direction="rtl"
      size="840px"
    >
      <grid-table
        :pagination="recordState.paginationConfig"
        :table-data="recordData"
        @size-change="handleRecordSizeChange"
        @current-change="handleRecordCurrentChange"
      >
        <el-table-column prop="name" :label="t('datasource.data_table')"></el-table-column>
        <el-table-column prop="triggerType" :label="t('datasource.sync_rate')">
          <template #default="scope">
            <div class="flex-align-center">
              <template v-if="scope.row.triggerType === 'CRON'">
                {{ t('datasource.cron_config') }}
              </template>
              <template v-if="scope.row.triggerType === 'RIGHTNOW'">
                {{ t('datasource.execute_once') }}
              </template>
              <template v-if="scope.row.triggerType === 'SIMPLE_CRON'">
                {{ t('datasource.simple_cron') }}
              </template>
              <template v-if="scope.row.triggerType === 'MANUAL'">
                {{ t('datasource.manual') }}
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="t('datasource.start_time')">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" :label="t('datasource.end_time')">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.endTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="taskStatus" label="更新结果">
          <template #default="scope">
            <div class="flex-align-center">
              <template v-if="scope.row.taskStatus === 'Completed'">
                <el-icon>
                  <icon name="icon_succeed_filled"></icon>
                </el-icon>
                {{ t('dataset.completed') }}
              </template>
              <template v-if="scope.row.taskStatus === 'UnderExecution'">
                {{ t('dataset.underway') }}
              </template>

              <template
                v-if="scope.row.taskStatus === 'Error' || scope.row.taskStatus === 'Warning'"
              >
                <el-icon>
                  <icon class="field-icon-red" name="icon_close_filled"></icon>
                </el-icon>
                {{ t('dataset.error') }}
                <el-icon @click="showErrorInfo(scope.row.info)" class="error-info">
                  <icon name="icon-maybe_outlined"></icon>
                </el-icon>
              </template>
            </div>
          </template>
        </el-table-column>
      </grid-table>
    </el-drawer>
    <el-dialog
      v-model="dialogErrorInfo"
      :close-on-press-escape="false"
      :close-on-click-modal="false"
      title="失败详情"
      width="600px"
    >
      <span>{{ dialogMsg }}</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button secondary @click="dialogErrorInfo = false">
            {{ t('chart.close') }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.datasource-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;

  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    border-right: 1px solid #d7d7d7;
    overflow: visible;

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
        padding-bottom: 10px;

        .title {
          margin-right: auto;
          font-size: 16px;
          font-style: normal;
          font-weight: 500;
          line-height: 24px;
        }

        .custom-icon {
          &.btn {
            color: #3370ff;
          }

          &:hover {
            cursor: pointer;
          }
        }
      }

      .search-bar {
        padding-bottom: 10px;
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
      color: #2b5fd9;
      background: rgba(51, 112, 255, 0.2);
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
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    .name {
      font-size: 16px;
      font-weight: 500;
      margin-right: 8px;
      max-width: 80%;
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
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: #3370ff;
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
      .datasource-table {
        height: calc(100% - 140px);
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
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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

    .datasource-table {
      padding: 24px;
      margin: 24px;
      background: #fff;
      height: calc(100vh - 190px);

      .search-operate {
        width: 280px;
        margin-bottom: 16px;
      }
    }

    .info-table {
      height: calc(100% - 49px);
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

      &.excel {
        width: calc(100% - 54px);
      }
    }

    .icon-more {
      display: inline-flex;
    }
  }
}
</style>
<style lang="less">
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
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
</style>
