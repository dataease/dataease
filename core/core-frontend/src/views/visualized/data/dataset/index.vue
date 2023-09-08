<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, shallowRef, computed, watch, nextTick, onBeforeMount } from 'vue'
import { ElIcon, ElMessageBox, ElMessage, type ElMessageBoxOptions } from 'element-plus-secondary'
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
import RowPermissions from './RowPermissions.vue'
import { guid } from '@/views/visualized/data/dataset/form/util'
import { save } from '@/api/visualization/dataVisualization'
import ColumnPermissions from './ColumnPermissions.vue'
import { cloneDeep } from 'lodash-es'
import { fieldType } from '@/utils/attr'

import {
  DEFAULT_CANVAS_STYLE_DATA_DARK,
  DEFAULT_CANVAS_STYLE_DATA_LIGHT
} from '@/views/chart/components/editor/util/dataVisualiztion'
import type { TabPaneName } from 'element-plus-secondary'
import { timestampFormatDate } from './form/util'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
import { XpackComponent } from '@/components/plugin'
const interactiveStore = interactiveStoreWithOut()
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
const rootManage = ref(false)
const nickName = ref('')
const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const state = reactive({
  datasetTree: [] as BusiTreeNode[]
})

const resourceGroupOpt = ref()
const curCanvasType = ref('')

const createPanel = path => {
  curCanvasType.value = path
  nextTick(() => {
    addOperation('newLeaf', null, 'leaf', true)
  })
}

const addOperation = (
  cmd: string,
  data?: BusiTreeNode,
  nodeType?: string,
  parentSelect?: boolean
) => {
  resourceGroupOpt.value.optInit(nodeType, data || {}, cmd, parentSelect)
}

const resourceOptFinish = param => {
  if (param && param.opt === 'newLeaf') {
    resourceCreate(param.pid, param.name)
  }
}

const tablePaneList = ref([
  { title: t('chart.data_preview'), name: 'dataPreview' },
  { title: '结构预览', name: 'structPreview' }
])

const resourceCreate = (pid, name) => {
  // 新建基础信息
  const newResourceId = guid()
  const bashResourceInfo = {
    id: newResourceId,
    name: name,
    pid: pid,
    type: curCanvasType.value,
    status: 1,
    selfWatermarkStatus: 0
  }
  const canvasStyleDataNew =
    curCanvasType.value === 'dashboard'
      ? DEFAULT_CANVAS_STYLE_DATA_LIGHT
      : DEFAULT_CANVAS_STYLE_DATA_DARK
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
    title: '备注',
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
const getData = () => {
  dtLoading.value = true
  const request = { busiFlag: 'dataset' } as BusiTreeRequest
  /* await interactiveStore.setInteractive(request)
  const interactiveData = interactiveStore.getDataset
  const nodeData = interactiveData.treeNodes
  rootManage.value = interactiveData.rootManage
  if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
    state.datasetTree = nodeData[0]['children'] || []
  } else {
    state.datasetTree = nodeData
  }
  dtLoading.value = false
  const id = nodeInfo.id
  if (!!id) {
    Object.assign(nodeInfo, cloneDeep(defaultNode))
    dfsDatasetTree(state.datasetTree, id)
  } */
  interactiveStore
    .setInteractive(request)
    .then(res => {
      const nodeData = (res as unknown as BusiTreeNode[]) || []
      if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
        rootManage.value = nodeData[0]['weight'] >= 7
        state.datasetTree = nodeData[0]['children'] || []
        return
      }
      state.datasetTree = nodeData
    })
    .finally(() => {
      dtLoading.value = false
      const id = nodeInfo.id
      if (!!id) {
        Object.assign(nodeInfo, cloneDeep(defaultNode))
        dfsDatasetTree(state.datasetTree, id)
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
    if (nodeInfo.weight >= 7) {
      tablePaneList.value.push({
        title: t('dataset.row_permissions'),
        name: 'row'
      })
      tablePaneList.value.push({
        title: t('dataset.column_permissions'),
        name: 'column'
      })
    }
  })
}

const editorDataset = () => {
  router.push({
    path: '/dataset-form',
    query: {
      id: nodeInfo.id
    }
  })
}

const createDataset = (data?: BusiTreeNode) => {
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
  expandedKey.value.push(data.id)
}

const datasetTypeList = [
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

const defaultProps = {
  children: 'children',
  label: 'name'
}

const datasetListTree = ref()

watch(nickName, (val: string) => {
  datasetListTree.value.filter(val)
})

const filterNode = (value: string, data: BusiTreeNode) => {
  if (!value) return true
  return data.name?.toLocaleLowerCase().includes(value.toLocaleLowerCase())
}
</script>

<template>
  <div class="dataset-manage" v-loading="dtLoading">
    <div class="dataset-list dataset-height" ref="node" :style="{ width: width + 'px' }">
      <div class="filter-dataset">
        <div class="icon-methods">
          <span class="title"> {{ t('auth.dataset') }} </span>
          <div v-if="rootManage" class="flex-align-center">
            <el-tooltip
              class="box-item"
              effect="dark"
              :content="t('deDataset.new_folder')"
              placement="top"
            >
              <el-button @click="() => handleDatasetTree('folder')" text>
                <template #icon>
                  <Icon name="dv-new-folder"></Icon>
                </template>
              </el-button>
            </el-tooltip>
            <el-tooltip class="box-item" effect="dark" content="新建数据集" placement="top">
              <el-button @click="() => createDataset()" text>
                <template #icon>
                  <Icon name="icon_dataset_outlined"></Icon>
                </template>
              </el-button>
            </el-tooltip>
          </div>
        </div>
        <div class="search-input">
          <el-input :placeholder="t('commons.search')" v-model="nickName" clearable>
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <div class="data-tree">
        <el-tree
          menu
          ref="datasetListTree"
          node-key="id"
          :data="state.datasetTree"
          :filter-node-method="filterNode"
          @node-expand="nodeExpand"
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
              <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
              <div class="icon-more" v-if="data.weight >= 7">
                <handle-more
                  icon-size="24px"
                  @handle-command="cmd => handleDatasetTree(cmd, data)"
                  :menu-list="datasetTypeList"
                  icon-name="icon_add_outlined"
                  placement="bottom-start"
                  v-if="!data.leaf"
                ></handle-more>
                <handle-more
                  @handle-command="cmd => operation(cmd, data, data.leaf ? 'dataset' : 'folder')"
                  :menu-list="menuList"
                ></handle-more>
              </div>
            </span>
          </template>
        </el-tree>
      </div>
    </div>
    <div class="dataset-content">
      <template v-if="!state.datasetTree.length">
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
            {{ nodeInfo.name }}
            <el-divider direction="vertical" />
            <span class="create-user">
              {{ t('visualization.create_by') }}:{{ nodeInfo.creator }}
            </span>

            <el-popover placement="bottom" width="290" trigger="hover">
              <template #reference>
                <el-icon class="create-user">
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
              <el-button secondary @click="createPanel('dataV')" v-permission="['screen']">
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
                v-for="item in tablePaneList"
                :key="item.name"
                :label="item.title"
                :name="item.name"
              />
            </el-tabs>
          </div>
        </div>
        <div class="dataset-table-info">
          <div v-if="activeName === 'dataPreview'" class="preview-num">
            显示 100 条数据，共 {{ total }} 条
          </div>
          <template v-if="['dataPreview', 'structPreview'].includes(activeName)">
            <div class="info-table" :class="[{ 'struct-preview': activeName === 'structPreview' }]">
              <el-auto-resizer>
                <template #default="{ height, width }">
                  <el-table-v2
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
      <template v-else>
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
.dataset-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;

  .dataset-height,
  .dataset-content {
    height: calc(100vh - 56px);
    overflow: auto;
    position: relative;
  }

  .dataset-content {
    background: #f5f6f7;
  }

  .dataset-list {
    width: 279px;
    padding: 16px 8px;
  }

  .filter-dataset {
    position: sticky;
    top: 0;
    left: 16px;
    padding: 0 8px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 16px;
      top: -16px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }

    .icon-methods {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      font-family: PingFang SC;
      font-size: 20px;
      font-weight: 500;
      color: var(--TextPrimary, #1f2329);

      .title {
        margin-right: auto;
      }
      .ed-button.is-text {
        line-height: 28px;
        font-size: 20px;
        height: 28px;
        width: 28px;
      }
    }

    .search-input {
      margin: 16px 0 8px 0;
      .ed-input {
        width: 100%;
      }
    }
  }

  .dataset-content {
    flex: 1;
    border-left: 1px solid rgba(31, 35, 41, 0.15);
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
        font-family: PingFang SC;
        font-size: 16px;
        font-weight: 500;

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
      font-family: PingFang SC;
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
</style>

<style lang="less">
.ed-table-v2__header-cell {
  background-color: #f5f6f7;
}
.custom-tree-node {
  width: calc(100% - 30px);
  display: flex;
  align-items: center;
  box-sizing: content-box;
  padding-right: 4px;

  .label-tooltip {
    width: calc(100% - 66px);
    margin-left: 8.75px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  .icon-more {
    margin-left: auto;
  }
}
</style>
