<script lang="tsx" setup>
// import type { HeaderCellSlotProps } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, shallowRef, computed } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { useRouter } from 'vue-router'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { Tree } from './form/CreatDsGroup.vue'
import { getDatasetTree, delDatasetTree, getDatasetPreview } from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import DatasetDetail from './DatasetDetail.vue'
import RowPermissions from './RowPermissions.vue'
import ColumnPermissions from './ColumnPermissions.vue'
import type { TabPaneName } from 'element-plus-secondary'
import { timestampFormatDate } from './form/util.js'
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
  id: string
  nodeType: string
  createTime: number
}
const nickName = ref('')
const router = useRouter()
const { t } = useI18n()
const state = reactive({
  datasetTree: [] as Tree[],
  menuList: [],
  datasetTypeList: []
})

const fieldMap = ['text', 'time', 'value', 'value', 'location']

const fieldType = (deType: number) => {
  return fieldMap[deType]
}

const creatDsFolder = ref()

const nodeInfo = reactive<Node>({
  name: '',
  createBy: '',
  id: '',
  nodeType: '',
  createTime: 0
})

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
            name={`field_${fieldType(deType)}`}
            className={`field-icon-${fieldType(deType)}`}
          ></Icon>
        </ElIcon>
        {t(`dataset.${fieldMap[deType]}`)}
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

const infoList = computed(() => {
  return ['name', 'createBy', 'nodeType', 'createTime'].map(ele => {
    return {
      name: ele,
      value: ele === 'createTime' ? timestampFormatDate(nodeInfo[ele]) : nodeInfo[ele]
    }
  })
})

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.dataeaseName,
    deType: ele.deType,
    dataKey: ele.dataeaseName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon
            name={`field_${fieldType(column.deType)}`}
            className={`field-icon-${fieldType(column.deType)}`}
          ></Icon>
        </ElIcon>
        {column.title}
      </div>
    )
  }))

const getData = () => {
  getDatasetTree().then(res => {
    state.datasetTree = (res as unknown as Tree[]) || []
  })
}

getData()

const columns = shallowRef([])
const tableData = shallowRef([])

const handleNodeClick = (data: Tree) => {
  if (data.nodeType !== 'dataset') return
  const { name, createBy, id, nodeType, createTime } = data
  Object.assign(nodeInfo, { name, createBy, id, nodeType, createTime })
  columnsPreview = []
  dataPreview = []
  activeName.value = 'dataPreview'
  handleClick(activeName.value)
}

const editorDataset = () => {
  router.push({
    path: '/dataset-form',
    query: {
      id: nodeInfo.id
    }
  })
}

const createDataset = (data?: Tree) => {
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
          allFields = (res.allFields as unknown as Field[]) || []
          columnsPreview = generateColumns((res.data.fields as Field[]) || [])
          dataPreview = (res.data.data as Array<{}>) || []
          columns.value = columnsPreview
          tableData.value = dataPreview
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

const operation = (cmd: string, data: Tree, nodeType: string) => {
  if (cmd === 'delete') {
    delDatasetTree(data.id).then(() => {
      getData()
    })
  } else {
    creatDsFolder.value.createInit(nodeType, data, cmd)
  }
}

const handleDatasetTree = (cmd: string, data?: Tree) => {
  if (cmd === 'dataset') {
    createDataset(data)
  }
  if (cmd === 'folder') {
    creatDsFolder.value.createInit(cmd, data || {})
  }
}

const activeName = ref('dataPreview')

state.menuList = [
  {
    label: '移动到',
    command: 'move'
  },
  {
    label: '重命名',
    command: 'rename'
  },
  {
    label: '删除',
    command: 'delete'
  }
]

state.datasetTypeList = [
  {
    label: '数据集',
    svgName: 'ds-api',
    command: 'dataset'
  },
  {
    label: '新建文件夹',
    divided: true,
    svgName: 'scene',
    command: 'folder'
  }
]

const defaultProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="dataset-manage">
    <div class="dataset-list dataset-height">
      <div class="filter-dataset">
        <div class="icon-methods">
          <span class="title"> 数据集 </span>
          <el-tooltip class="box-item" effect="dark" content="新建文件夹" placement="top">
            <el-button @click="() => handleDatasetTree('folder')" text>
              <template #icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </template>
            </el-button>
          </el-tooltip>
          <el-tooltip class="box-item" effect="dark" content="新建数据集" placement="top">
            <el-button @click="() => createDataset()" text>
              <template #icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </template>
            </el-button>
          </el-tooltip>
        </div>
        <div class="search-input">
          <el-input v-model="nickName" clearable>
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
          <span class="filter-button">
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </span>
        </div>
      </div>

      <el-tree
        :expand-on-click-node="false"
        menu
        :data="state.datasetTree"
        :props="defaultProps"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <el-icon v-if="data.nodeType === 'folder'">
              <Icon name="scene"></Icon>
            </el-icon>
            <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
            <div class="icon-more">
              <handle-more
                @handle-command="cmd => handleDatasetTree(cmd, data)"
                :menu-list="state.datasetTypeList"
                icon-name="icon_add_outlined"
                placement="bottom-start"
                v-if="data.nodeType === 'folder'"
              ></handle-more>
              <handle-more
                @handle-command="cmd => operation(cmd, data, data.nodeType)"
                :menu-list="state.menuList"
              ></handle-more>
            </div>
          </span>
        </template>
      </el-tree>
    </div>
    <div class="dataset-content">
      <template v-if="!!nodeInfo.id">
        <div class="info-method">
          {{ nodeInfo.name }}
          <el-divider direction="vertical" />
          <span class="create-user"> 创建人：{{ nodeInfo.createBy }} </span>

          <el-popover placement="bottom" width="420" trigger="hover">
            <template #reference>
              <el-icon class="create-user">
                <Icon name="icon_info_outlined"></Icon>
              </el-icon>
            </template>
            <dataset-detail :info-list="infoList" title-type="数据集"></dataset-detail>
          </el-popover>
          <div class="right-btn">
            <el-button secondary> 新建数据大屏 </el-button>
            <el-button type="primary" @click="editorDataset">
              <template #icon>
                <Icon name="icon_edit_outlined"></Icon>
              </template>
              编辑
            </el-button>
          </div>
        </div>
        <div class="tab-border">
          <el-tabs v-model="activeName" @tab-change="handleClick">
            <el-tab-pane label="数据预览" name="dataPreview"></el-tab-pane>
            <el-tab-pane label="结构预览" name="structPreview"></el-tab-pane>
            <el-tab-pane label="行权限" name="row"></el-tab-pane>
            <el-tab-pane label="列权限" name="column"></el-tab-pane>
          </el-tabs>
        </div>
        <div v-if="activeName === 'dataPreview'" class="preview-num">
          显示 100 条数据，共 1000 条 &nbsp;
          <el-icon>
            <Icon name="icon_edit_outlined"></Icon>
          </el-icon>
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
                />
              </template>
            </el-auto-resizer>
          </div>
        </template>
        <template v-if="['row', 'column'].includes(activeName)">
          <el-button class="add-row-column" secondary>
            <template #icon>
              <Icon name="icon_add_outlined"></Icon>
            </template>
            {{ t(`dataset.${activeName}`) }}
          </el-button>
          <div class="table-row-column">
            <row-permissions v-if="activeName === 'row'"></row-permissions>
            <column-permissions v-else></column-permissions>
          </div>
        </template>
      </template>
      <template v-else>
        <empty-background description="请在左侧选择数据集" img-type="select" />
      </template>
    </div>
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
    height: calc(100vh - 50px);
    overflow: auto;
    position: relative;
  }

  .dataset-list {
    width: 279px;
    padding: 16px;
  }

  .filter-dataset {
    position: sticky;
    top: 0;
    left: 16px;
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
        height: 28px;
        width: 28px;
      }
    }

    .search-input {
      margin: 16px 8px 8px 8px;
      display: flex;
      justify-content: space-between;
      .ed-input {
        flex: 1;
      }

      .filter-button {
        width: 32px;
        height: 32px;
        margin-left: 8px;
        border: 1px solid #bbbfc4;
        border-radius: 4px;
        line-height: 32px;
        text-align: center;
        cursor: pointer;
      }
    }
  }

  .dataset-content {
    flex: 1;
    padding: 12px 24px 0 24px;
    border-left: 1px solid rgba(31, 35, 41, 0.15);
    position: relative;

    .info-method {
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
      margin-left: 0;
    }

    .preview-num {
      color: var(--deTextSecondary, #606266);
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin: 4px 0;
      margin: 16px 0;
      display: flex;
      align-items: center;
    }

    .info-table {
      height: calc(100% - 130px);
    }

    .struct-preview {
      height: calc(100% - 92px);
      margin-top: 16px;
    }

    .table-row-column {
      height: calc(100% - 155px);
    }

    .add-row-column {
      margin: 16px 0;
    }
  }
}
</style>

<style lang="less">
.ed-table-v2__header-cell {
  background-color: #f5f6f7;
}
.custom-tree-node {
  flex: 1;
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
