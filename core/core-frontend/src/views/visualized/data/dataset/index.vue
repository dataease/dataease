<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, shallowRef, computed, watch, onBeforeMount, nextTick, unref } from 'vue'
import ArrowSide from '@/views/common/DeResourceArrow.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useEmitt } from '@/hooks/web/useEmitt'
import {
  ElIcon,
  ElMessageBox,
  ElMessage,
  type ElMessageBoxOptions,
  ElAside,
  ElScrollbar
} from 'element-plus-secondary'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { useMoveLine } from '@/hooks/web/useMoveLine'
import { useRouter, useRoute } from 'vue-router'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { delDatasetTree, getDatasetPreview, barInfoApi } from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import DatasetDetail from './DatasetDetail.vue'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { save } from '@/api/visualization/dataVisualization'
import { cloneDeep } from 'lodash-es'
import { fieldType } from '@/utils/attr'
import { useAppStoreWithOut } from '@/store/modules/app'
import treeSort from '@/utils/treeSortUtils'

import {
  DEFAULT_CANVAS_STYLE_DATA_LIGHT,
  DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK
} from '@/views/chart/components/editor/util/dataVisualization'
import type { TabPaneName } from 'element-plus-secondary'
import { timestampFormatDate } from './form/util'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { XpackComponent } from '@/components/plugin'
import { useCache } from '@/hooks/web/useCache'
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
}
const appStore = useAppStoreWithOut()
const rootManage = ref(false)
const nickName = ref('')
const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const state = reactive({
  datasetTree: [] as BusiTreeNode[],
  curSortType: 'time_desc'
})

const resourceGroupOpt = ref()
const curCanvasType = ref('')
const mounted = ref(false)

const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)
const createPanel = path => {
  const baseUrl = `#/${path}?opt=create&id=${nodeInfo.id}`
  window.open(baseUrl, '_blank')
}

const resourceOptFinish = param => {
  if (param && param.opt === 'newLeaf') {
    resourceCreate(param.pid, param.name)
  }
}

let originResourceTree = []

const sortTypeChange = sortType => {
  state.datasetTree = treeSort(originResourceTree, sortType)
  state.curSortType = sortType
  wsCache.set('TreeSort-dataset', state.curSortType)
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
    window.open(baseUrl + newResourceId, '_blank')
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
    title: '字段名称',
    width: 250
  },
  {
    key: 'deType',
    dataKey: 'deType',
    title: '字段类型',
    width: 250,
    cellRenderer: ({ cellData: deType }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon
            name={`field_${fieldType[deType]}`}
            className={`field-icon-${fieldType[deType]}`}
          ></Icon>
        </ElIcon>
        {t(`dataset.${fieldType[deType]}`) +
          `${deType === 3 ? '(' + t('dataset.float') + ')' : ''}`}
      </div>
    )
  },
  {
    key: 'description',
    dataKey: 'description',
    title: '字段备注',
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

const dtLoading = ref(false)
const isCreated = ref(false)
const getData = () => {
  dtLoading.value = true
  const request = { busiFlag: 'dataset' } as BusiTreeRequest
  interactiveStore
    .setInteractive(request)
    .then(res => {
      const nodeData = (res as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        rootManage.value = nodeData[0]['weight'] >= 7
        state.datasetTree = nodeData[0]['children'] || []
        originResourceTree = cloneDeep(unref(state.datasetTree))
        sortTypeChange(state.curSortType)
        return
      }
      state.datasetTree = nodeData
      originResourceTree = cloneDeep(unref(state.datasetTree))
      sortTypeChange(state.curSortType)
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
  nodeInfo.id = (route.params.id as string) || ''
  loadInit()
  getData()
})

const columns = shallowRef([])
const tableData = shallowRef([])
const total = ref(0)

const handleNodeClick = (data: BusiTreeNode) => {
  if (!data.leaf) {
    datasetListTree.value.setCurrentKey(null)
    return
  }
  barInfoApi(data.id).then(res => {
    const nodeData = res as unknown as Node[]
    Object.assign(nodeInfo, nodeData)
    nodeInfo.weight = data.weight
    columnsPreview = []
    dataPreview = []
    activeName.value = 'dataPreview'
    handleClick(activeName.value)
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
      getDatasetPreview(nodeInfo.id)
        .then(res => {
          allFields = (res?.allFields as unknown as Field[]) || []
          columnsPreview = generateColumns((res?.data?.fields as Field[]) || [])
          dataPreview = (res?.data?.data as Array<{}>) || []
          columns.value = columnsPreview
          tableData.value = dataPreview
          total.value = res.total
        })
        .finally(() => {
          dataPreviewLoading.value = false
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

const operation = (cmd: string, data: BusiTreeNode, nodeType: string) => {
  if (cmd === 'copy') {
    if (isDataEaseBi.value) {
      embedded.clearState()
      embedded.setDatasetCopyId(data.id as string)
      useEmitt().emitter.emit('changeCurrentComponent', 'DatasetEditor')
      return
    }
    router.push({
      name: 'dataset-form',
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
      options.tip = '删除后，此文件夹下的所有资源都会被删除，请谨慎操作。'
    } else {
      delete options.tip
    }

    ElMessageBox.confirm(
      nodeType === 'folder' ? '确定删除该文件夹吗' : t('datasource.delete_this_dataset'),
      options as ElMessageBoxOptions
    ).then(() => {
      delDatasetTree(data.id).then(() => {
        getData()
        ElMessage.success(t('dataset.delete_success'))
      })
    })
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
    svgName: 'icon_into-item_outlined',
    command: 'move'
  },
  {
    label: t('visualization.rename'),
    svgName: 'icon_rename_outlined',
    command: 'rename'
  },
  {
    label: t('common.delete'),
    divided: true,
    svgName: 'icon_delete-trash_outlined',
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
      label: '新建数据集',
      svgName: 'icon_dataset',
      command: 'dataset'
    },
    {
      label: t('deDataset.new_folder'),
      divided: true,
      svgName: 'dv-folder',
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
    title: '结构预览',
    name: 'structPreview'
  }
]

const sortList = [
  {
    name: '按创建时间升序',
    value: 'time_asc'
  },
  {
    name: '按创建时间降序',
    value: 'time_desc',
    divided: true
  },
  {
    name: '按照名称升序',
    value: 'name_asc'
  },
  {
    name: '按照名称降序',
    value: 'name_desc'
  }
]

const loadInit = () => {
  const historyTreeSort = wsCache.get('TreeSort-dataset')
  if (historyTreeSort) {
    state.curSortType = historyTreeSort
  }
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
          svgName: 'icon_copy_filled',
          command: 'copy'
        }
      ].concat(menuList)
}
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
                  <Icon name="dv-new-folder" />
                </el-icon>
              </el-tooltip>
              <el-tooltip class="box-item" effect="dark" content="新建数据集" placement="top">
                <el-icon class="custom-icon btn" @click="createDataset">
                  <Icon name="icon_file-add_outlined" />
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
                <Icon name="icon_search-outline_outlined" />
              </el-icon>
            </template>
          </el-input>
          <el-dropdown @command="sortTypeChange" trigger="click">
            <el-icon class="filter-icon-span">
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon
                  v-if="state.curSortType.includes('asc')"
                  name="dv-sort-asc"
                  class="opt-icon"
                ></Icon>
              </el-tooltip>
              <el-tooltip :offset="16" effect="dark" :content="sortTypeTip" placement="top">
                <Icon
                  v-show="state.curSortType.includes('desc')"
                  name="dv-sort-desc"
                  class="opt-icon"
                ></Icon>
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
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            :default-expanded-keys="expandedKey"
            :props="defaultProps"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <el-icon v-if="!data.leaf" style="font-size: 18px">
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <el-icon v-if="data.leaf" style="font-size: 18px">
                  <Icon name="icon_dataset"></Icon>
                </el-icon>
                <span :title="node.label" class="label-tooltip ellipsis">{{ node.label }}</span>
                <div class="icon-more" v-if="data.weight >= 7">
                  <handle-more
                    icon-size="24px"
                    @handle-command="cmd => handleDatasetTree(cmd, data)"
                    :menu-list="datasetTypeList"
                    icon-name="icon_add_outlined"
                    placement="bottom-start"
                    v-if="!data.leaf"
                  ></handle-more>
                  <el-icon v-else class="hover-icon" @click.stop="handleEdit(data.id)">
                    <icon name="icon_edit_outlined"></icon>
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
        <empty-background description="暂无数据集" img-type="none">
          <el-button v-if="rootManage" @click="() => createDataset()" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined"></Icon>
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
                  <Icon name="icon_info_outlined"></Icon>
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
                  <Icon name="icon_dashboard_outlined"></Icon>
                </template>
                {{ t('visualization.panelAdd') }}
              </el-button>
              <el-button secondary @click="createPanel('dvCanvas')" v-permission="['screen']">
                <template #icon> <Icon name="icon_operation-analysis_outlined"></Icon> </template
                >新建数据大屏
              </el-button>
              <el-button type="primary" @click="editorDataset" v-if="nodeInfo.weight >= 7">
                <template #icon>
                  <Icon name="icon_edit_outlined"></Icon>
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
            显示 100 条数据，共 {{ total }} 条
          </div>
          <template v-if="['dataPreview', 'structPreview'].includes(activeName)">
            <div class="info-table" :class="[{ 'struct-preview': activeName === 'structPreview' }]">
              <el-auto-resizer v-if="activeName === 'structPreview'">
                <template #default="{ height, width }">
                  <el-table-v2
                    key="structPreview"
                    :columns="columns"
                    v-loading="dataPreviewLoading"
                    header-class="header-cell"
                    :data="tableData"
                    :width="width"
                    :height="height"
                    fixed
                    ><template #empty>
                      <empty-background description="暂无数据" img-type="noneWhite" /> </template
                  ></el-table-v2>
                </template>
              </el-auto-resizer>
              <template v-if="activeName === 'dataPreview'">
                <el-table
                  v-loading="dataPreviewLoading"
                  header-class="header-cell"
                  :data="tableData"
                  key="dataPreview"
                  border
                  style="width: 100%; height: 100%"
                >
                  <el-table-column
                    :key="column.dataKey"
                    v-for="(column, index) in columns"
                    :prop="column.dataKey"
                    :label="column.title"
                    :width="columns.length - 1 === index ? 150 : 'auto'"
                    :fixed="columns.length - 1 === index ? 'right' : false"
                  >
                    <template #header>
                      <div class="flex-align-center">
                        <ElIcon style="margin-right: 6px">
                          <Icon
                            :name="`field_${fieldType[column.deType]}`"
                            :className="`field-icon-${fieldType[column.deType]}`"
                          ></Icon>
                        </ElIcon>
                        <span class="ellipsis" :title="column.title" style="width: 120px">
                          {{ column.title }}
                        </span>
                      </div>
                    </template>
                  </el-table-column>
                  <template #empty>
                    <empty-background description="暂无数据" img-type="noneWhite" />
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
    <de-resource-group-opt
      :cur-canvas-type="curCanvasType"
      @finish="resourceOptFinish"
      ref="resourceGroupOpt"
    ></de-resource-group-opt>
    <creat-ds-group @finish="getData()" ref="creatDsFolder"></creat-ds-group>
  </div>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.ed-table {
  --ed-table-header-bg-color: #f5f6f7;
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
.dataset-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  position: relative;

  &.de-100vh {
    height: 100vh;
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
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
    }

    .icon-more {
      display: inline-flex;
    }
  }
}
</style>

<style lang="less">
.ed-table-v2__header-cell {
  background-color: #f5f6f7;
}
</style>
