<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { deleteLogic, findById, findTree } from '@/api/dataVisualization'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { HandleMore } from '@/components/handle-more'
import DeResourceGroupOpt, { ResourceTree } from '@/views/common/DeResourceGroupOpt.vue'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { DEFAULT_CANVAS_STYLE_DATA } from '@/store/modules/data-visualization/dvMain'
import { save } from '@/api/dataVisualization'
const searchMap = {
  all: '全部',
  folder: '文件夹'
}

let searchPids = [] // 查询命中的pid
const filterText = ref(null)
const searchType = ref('all')
const expandedArray = ref([])
const dvListTree = ref()
const resourceGroupOpt = ref()

const state = reactive({
  dvTree: [] as ResourceTree[],
  menuList: [],
  dvTypeList: []
})

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

state.dvTypeList = [
  {
    label: '新建数据大屏',
    svgName: 'dashboard',
    command: 'dv'
  },
  {
    label: '新建文件夹',
    divided: true,
    svgName: 'scene',
    command: 'folder'
  }
]

const nodeExpand = data => {
  if (data.id) {
    expandedArray.value.push(data.id)
  }
}

const nodeCollapse = data => {
  if (data.id) {
    expandedArray.value.splice(expandedArray.value.indexOf(data.id), 1)
  }
}

const filterNode = (value, data) => {
  if (!value) return true
  const result = data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1
  return result
}

const nodeClick = (data: ResourceTree, node) => {
  if (data.nodeType !== 'folder') {
    emit('nodeClick', data.id)
  }
}

const getTree = () => {
  const param = {}
  // 从数据库中获取
  findTree(param).then(res => {
    state.dvTree = res.data as unknown as ResourceTree[]
  })
}

const emit = defineEmits(['nodeClick'])

const clickMore = () => {
  //do something
}

const beforeClickEdit = (optType, data, node) => {
  return {
    data: data,
    node: node,
    optType: optType
  }
}

const operation = (cmd: string, data: ResourceTree, nodeType: string) => {
  if (cmd === 'delete') {
    deleteLogic(data.id).then(res => {
      ElMessage.success('删除成功')
      getTree()
    })
  } else {
    resourceGroupOpt.value.optInit(nodeType, data, cmd)
  }
}

const addOperation = (cmd: string, data?: ResourceTree) => {
  if (cmd === 'dv') {
    dvCreate(data.id)
  }
  if (cmd === 'folder') {
    resourceGroupOpt.value.optInit(cmd, data || {})
  }
}

const dvEdit = dvId => {
  const url = '#/dvCanvas/?dvId=' + dvId
  window.open(url, '_blank')
}

const dvCreate = pid => {
  // 新建基础信息
  const newDvId = guid()
  const bashDvInfo = {
    id: newDvId,
    name: '新建仪表板',
    pid: pid,
    type: 'dataV',
    status: 1,
    selfWatermarkStatus: 0
  }
  const canvasInfo = {
    canvasStyleData: JSON.stringify(DEFAULT_CANVAS_STYLE_DATA),
    componentData: JSON.stringify([]),
    canvasViewInfo: {},
    ...bashDvInfo
  }
  save(canvasInfo).then(res => {
    const url = '#/dvCanvas/?dvId=' + newDvId
    window.open(url, '_blank')
    getTree()
  })
}

const handleDvTree = (cmd, data) => {
  //do handleDvTree
}

const resourceOptFinish = () => {
  getTree()
}

watch(filterText, val => {
  searchPids = []
  dvListTree.value.filter(val)
})

onMounted(() => {
  getTree()
})
</script>

<template>
  <div class="resource-tree">
    <div class="icon-methods">
      <span class="title"> 数据大屏 </span>
      <el-tooltip class="box-item" effect="dark" content="新建文件夹" placement="top">
        <el-button type="primary" @click="addOperation('folder')"> 新建文件夹 </el-button>
      </el-tooltip>
    </div>

    <el-input
      v-model="filterText"
      size="small"
      :placeholder="'搜索'"
      prefix-icon="el-icon-search"
      clearable
      class="search-bar"
    >
      <template #append>
        <el-button :icon="Search" />
      </template>
    </el-input>
    <el-tree
      menu
      ref="dvListTree"
      :default-expanded-keys="expandedArray"
      :data="state.dvTree"
      node-key="id"
      :expand-on-click-node="true"
      :filter-node-method="filterNode"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      @node-click="nodeClick"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <el-icon v-if="data.nodeType === 'folder'">
            <Icon name="scene"></Icon>
          </el-icon>
          <el-icon v-else>
            <Icon name="dashboard"></Icon>
          </el-icon>
          <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
          <div class="icon-more">
            <span v-on:click.stop>
              <el-icon
                v-if="data.nodeType !== 'folder'"
                class="hover-icon"
                @click="dvEdit(data.id)"
              >
                <Icon name="edit-in"></Icon>
              </el-icon>
            </span>
            <handle-more
              @handle-command="cmd => addOperation(cmd, data)"
              :menu-list="state.dvTypeList"
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
    <de-resource-group-opt
      @finish="resourceOptFinish"
      ref="resourceGroupOpt"
    ></de-resource-group-opt>
  </div>
</template>
<style lang="less" scoped>
.resource-tree {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  .icon-methods {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    padding-bottom: 10px;
    .title {
      margin-right: auto;
    }
  }
  .search-bar {
    padding-bottom: 10px;
  }
}
.title-area {
  margin-left: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.title-area-outer {
  display: flex;
  flex: 1 1 0%;
  width: 0px;
}
.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}
.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}

:deep(.ed-input__wrapper) {
  width: 80px;
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
