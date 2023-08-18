<script setup lang="ts">
import { Search } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, toRefs, watch } from 'vue'
import { deleteLogic, queryTreeApi } from '@/api/visualization/dataVisualization'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { HandleMore } from '@/components/handle-more'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { save } from '@/api/visualization/dataVisualization'
import {
  DEFAULT_CANVAS_STYLE_DATA_DARK,
  DEFAULT_CANVAS_STYLE_DATA_LIGHT
} from '@/views/chart/components/editor/util/dataVisualiztion'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  curCanvasType: {
    type: String,
    required: true
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  }
})
const defaultProps = {
  children: 'children',
  label: 'name'
}
const rootManage = ref(false)
const { curCanvasType, showPosition } = toRefs(props)
const resourceLabel = curCanvasType.value === 'dataV' ? '数据大屏' : '仪表板'
const newResourceLabel = '新建' + resourceLabel

const filterText = ref(null)
const expandedArray = ref([])
const resourceListTree = ref()
const resourceGroupOpt = ref()
const state = reactive({
  resourceTree: [] as BusiTreeNode[],
  menuList: [],
  resourceTypeList: []
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

state.resourceTypeList = [
  {
    label: newResourceLabel,
    svgName: curCanvasType.value === 'dashboard' ? 'dv-dashboard-spine' : 'dv-screen-spine',
    command: 'newLeaf'
  },
  {
    label: '新建文件夹',
    divided: true,
    svgName: 'dv-folder',
    command: 'newFolder'
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

const filterNode = (value: string, data: BusiTreeNode) => {
  if (!value) return true
  return data.name?.toLocaleLowerCase().includes(value.toLocaleLowerCase())
}

const nodeClick = (data: BusiTreeNode) => {
  if (data.leaf) {
    emit('nodeClick', data.id)
  }
}

const getTree = () => {
  const request = { busiFlag: curCanvasType.value } as BusiTreeRequest
  queryTreeApi(request).then(res => {
    const nodeData = (res as unknown as BusiTreeNode[]) || []
    if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
      rootManage.value = nodeData[0]['weight'] >= 3
      state.resourceTree = nodeData[0]['children'] || []
      return
    }
    state.resourceTree = nodeData
  })
}

const emit = defineEmits(['nodeClick'])

const operation = (cmd: string, data: BusiTreeNode, nodeType: string) => {
  if (cmd === 'delete') {
    ElMessageBox.confirm(
      data.leaf ? '确定删除该' + resourceLabel + '吗？' : '确定删除该文件夹吗？',
      {
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        showClose: false
      }
    ).then(() => {
      deleteLogic(data.id).then(() => {
        ElMessage.success('删除成功')
        if (dvInfo.value && dvInfo.value.id === data.id) {
          dvMainStore.resetDvInfo()
        }
        getTree()
      })
    })
  } else {
    resourceGroupOpt.value.optInit(nodeType, data, cmd)
  }
}

const addOperation = (
  cmd: string,
  data?: BusiTreeNode,
  nodeType?: string,
  parentSelect?: boolean
) => {
  resourceGroupOpt.value.optInit(nodeType, data || {}, cmd, parentSelect)
}

const resourceEdit = resourceId => {
  const baseUrl = curCanvasType.value === 'dataV' ? '#/dvCanvas/?dvId=' : '#/dashboard/?resourceId='
  window.open(baseUrl + resourceId, '_blank')
}

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
    const baseUrl =
      curCanvasType.value === 'dataV' ? '#/dvCanvas/?dvId=' : '#/dashboard/?resourceId='
    window.open(baseUrl + newResourceId, '_blank')
    getTree()
  })
}

const resourceOptFinish = param => {
  if (param && param.opt === 'newLeaf') {
    resourceCreate(param.pid, param.name)
  } else {
    getTree()
  }
}

watch(filterText, val => {
  resourceListTree.value.filter(val)
})

onMounted(() => {
  getTree()
})
</script>

<template>
  <div class="resource-tree">
    <div class="icon-methods" v-show="showPosition === 'preview'">
      <span class="title"> {{ resourceLabel }} </span>
      <div v-if="rootManage">
        <el-tooltip content="新建文件夹" placement="top" effect="dark">
          <el-icon
            class="custom-icon"
            style="margin-right: 20px"
            @click="addOperation('newFolder', null, 'folder')"
          >
            <Icon name="dv-new-folder"></Icon>
          </el-icon>
        </el-tooltip>
        <el-tooltip :content="newResourceLabel" placement="top" effect="dark">
          <el-icon class="custom-icon" @click="addOperation('newLeaf', null, 'leaf', true)">
            <Icon v-if="curCanvasType === 'dashboard'" name="dv-new"></Icon>
            <Icon v-else name="dv-screen-new"></Icon>
          </el-icon>
        </el-tooltip>
      </div>
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
      ref="resourceListTree"
      class="custom-tree"
      :default-expanded-keys="expandedArray"
      :data="state.resourceTree"
      :props="defaultProps"
      node-key="id"
      :expand-on-click-node="true"
      :filter-node-method="filterNode"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      @node-click="nodeClick"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node">
          <el-icon v-if="!data.leaf">
            <Icon name="dv-folder"></Icon>
          </el-icon>
          <el-icon v-else-if="curCanvasType === 'dashboard'">
            <Icon name="dv-dashboard-spine"></Icon>
          </el-icon>
          <el-icon v-else>
            <Icon name="dv-screen-spine"></Icon>
          </el-icon>
          <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
          <div class="icon-more" v-if="data.weight >= 3 && showPosition === 'preview'">
            <span v-on:click.stop>
              <el-icon v-if="data.leaf" class="hover-icon" @click="resourceEdit(data.id)">
                <Icon name="edit-in"></Icon>
              </el-icon>
            </span>
            <handle-more
              @handle-command="
                cmd => addOperation(cmd, data, cmd === 'newFolder' ? 'folder' : 'leaf')
              "
              :menu-list="state.resourceTypeList"
              icon-name="icon_add_outlined"
              placement="bottom-start"
              v-if="!data.leaf"
            ></handle-more>
            <handle-more
              @handle-command="cmd => operation(cmd, data, data.leaf ? 'leaf' : 'folder')"
              :menu-list="state.menuList"
            ></handle-more>
          </div>
        </span>
      </template>
    </el-tree>
    <de-resource-group-opt
      :cur-canvas-type="curCanvasType"
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
      font-size: 16px;
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

.custom-tree {
  height: calc(100vh - 160px);
  overflow-y: auto;
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
    visibility: hidden;
  }

  &:hover .icon-more {
    margin-left: auto;
    visibility: visible;
  }
}

.custom-icon {
  &:hover {
    cursor: pointer;
  }
}
</style>
