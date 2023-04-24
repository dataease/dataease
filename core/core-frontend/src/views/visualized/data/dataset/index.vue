<script lang="tsx" setup>
// import type { HeaderCellSlotProps } from 'element-plus-secondary'
import { ref, reactive, shallowRef } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import { useRouter } from 'vue-router'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { Tree } from './form/CreatDsGroup.vue'
import { getDatasetTree, delDatasetTree, getDatasetPreview } from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import type { TabPaneName } from 'element-plus-secondary'
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
}
const nickName = ref('')
const router = useRouter()
const showTable = ref(false)

const state = reactive({
  addedDatasetList: [],
  datasetTree: [] as Tree[],
  menuList: [],
  datasetTypeList: []
})
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}
const creatDsFolder = ref()

const nodeInfo = reactive<Node>({
  name: '',
  createBy: '',
  id: '',
  nodeType: ''
})

let allFields = []
let columnsPreview = []
let dataPreview = []

const allFieldsColumns = [
  {
    key: 'name',
    dataKey: 'name',
    title: '字段名称',
    width: 150
  },
  {
    key: 'deType',
    dataKey: 'deType',
    title: '字段类型',
    width: 150
  },
  {
    key: 'description',
    dataKey: 'description',
    title: '备注',
    width: 150
  }
]

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.dataeaseName,
    deType: ele.deType,
    dataKey: ele.dataeaseName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElIcon>
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
  const { name, createBy, id, nodeType } = data
  Object.assign(nodeInfo, { name, createBy, id, nodeType })
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
      pid: data.id
    }
  })
}

const handleClick = (tabName: TabPaneName) => {
  showTable.value = false
  switch (tabName) {
    case 'dataPreview':
      if (columnsPreview.length) {
        columns.value = columnsPreview
        tableData.value = dataPreview
        break
      }
      getDatasetPreview(nodeInfo.id).then(res => {
        allFields = (res.allFields as unknown as Field[]) || []
        columnsPreview = generateColumns((res.data.fields as Field[]) || [])
        dataPreview = (res.data.data as Array<{}>) || []
        columns.value = columnsPreview
        tableData.value = dataPreview
      })
      break
    case 'structPreview':
      columns.value = allFieldsColumns
      tableData.value = allFields
      break
    default:
      break
  }

  setTimeout(() => {
    showTable.value = true
  }, 500)
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
state.addedDatasetList = Array(40)
  .fill(1)
  .map((_, index) => ({
    datasetname: 'test' + index,
    nickName: index + 'nickName'
  }))

const defaultProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="dataset-manage">
    <div class="dataset-list dataset-height">
      <div class="title">
        <el-button @click="() => handleDatasetTree('folder')" secondary>
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template>文件夹
        </el-button>
        <el-button @click="() => createDataset()" type="primary">
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template>数据集
        </el-button>
        <el-input class="m24 w100" v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
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
            <div>
              <handle-more
                @handle-command="cmd => handleDatasetTree(cmd, data)"
                :menu-list="state.datasetTypeList"
                icon-name="icon_add_outlined"
                placement="bottom-start"
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
          <!-- <el-tag class="mr8" type="warning">定时同步</el-tag> -->
          <el-divider direction="vertical" />
          <span class="create-user"> 创建人：{{ nodeInfo.createBy }} </span>
          <el-icon class="create-user">
            <Icon name="icon_info_outlined"></Icon>
          </el-icon>
          <el-button class="right-btn" type="primary" @click="editorDataset"> 编辑 </el-button>
        </div>
        <el-tabs v-model="activeName" @tab-change="handleClick">
          <el-tab-pane label="数据预览" name="dataPreview"></el-tab-pane>
          <el-tab-pane label="结构预览" name="structPreview"></el-tab-pane>
          <el-tab-pane label="行权限" name="row"></el-tab-pane>
          <el-tab-pane label="列权限" name="column"></el-tab-pane>
        </el-tabs>
        <div class="preview-num">
          预览 <span>100行</span>
          <el-icon>
            <Icon name="icon_edit_outlined"></Icon>
          </el-icon>
        </div>
        <div class="info-table" v-if="showTable">
          <el-auto-resizer>
            <template #default="{ height, width }">
              <el-table-v2
                :columns="columns"
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
      <template v-else>
        <empty-background description="请在左侧选择数据集" img-type="select" />
      </template>
    </div>
    <creat-ds-group @finish="getData()" ref="creatDsFolder"></creat-ds-group>
  </div>
</template>

<style lang="less" scoped>
.dataset-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;

  .dataset-height,
  .dataset-content {
    height: calc(100vh - 110px);
    overflow: auto;
    position: relative;
  }

  .dataset-list {
    width: 269px;
    padding: 24px;
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 24px;
      top: -24px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }

  .m24 {
    margin: 24px 0;
  }
  .w100 {
    width: 100%;
  }

  .dataset-content {
    flex: 1;
    padding: 24px;
    padding-top: 58px;
    border-left: 1px solid rgba(31, 35, 41, 0.15);
    position: relative;

    .info-method {
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      padding: 20px 24px;
      // border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;

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

    .preview-num {
      color: var(--deTextSecondary, #606266);
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      margin: 4px 0;
      margin-top: 16px;
      display: flex;
      align-items: center;
    }

    .info-table {
      height: calc(100% - 100px);
    }
  }
}
</style>

<style lang="less">
.el-table-v2__header-cell {
  background-color: #f5f6f7;
}
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  box-sizing: content-box;

  .label-tooltip {
    width: 60%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
