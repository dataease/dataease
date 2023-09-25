<script setup lang="ts">
import { onMounted, reactive, ref, toRefs, watch, nextTick } from 'vue'
import { deleteLogic } from '@/api/visualization/dataVisualization'
import { ElIcon, ElMessage, ElMessageBox, ElScrollbar } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { HandleMore } from '@/components/handle-more'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import DvHandleMore from '@/components/handle-more/src/DvHandleMore.vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
const interactiveStore = interactiveStoreWithOut()
import router from '@/router'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const { t } = useI18n()

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
const anyManage = ref(false)
const { curCanvasType, showPosition } = toRefs(props)
const resourceLabel = curCanvasType.value === 'dataV' ? '数据大屏' : '仪表板'
const newResourceLabel = '新建' + resourceLabel
const selectedNodeKey = ref(null)
const filterText = ref(null)
const expandedArray = ref([])
const resourceListTree = ref()
const resourceGroupOpt = ref()
const returnMounted = ref(false)
const state = reactive({
  resourceTree: [] as BusiTreeNode[],
  menuList: [
    {
      label: '编辑',
      command: 'edit',
      svgName: 'dv-edit'
    },
    {
      label: '复制',
      command: 'copy',
      svgName: 'dv-copy-dark'
    },
    {
      label: '移动到',
      command: 'move',
      svgName: 'dv-move'
    },
    {
      label: '重命名',
      command: 'rename',
      svgName: 'dv-rename'
    },
    {
      label: '删除',
      command: 'delete',
      svgName: 'dv-delete',
      divided: true
    }
  ],
  folderMenuList: [
    {
      label: '移动到',
      command: 'move',
      svgName: 'dv-move'
    },
    {
      label: '重命名',
      command: 'rename',
      svgName: 'dv-rename'
    },
    {
      label: '删除',
      command: 'delete',
      svgName: 'dv-delete',
      divided: true
    }
  ],
  resourceTypeList: []
})

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

const { dvId } = window.DataEaseBi || router.currentRoute.value.query
if (dvId) {
  selectedNodeKey.value = dvId
  returnMounted.value = true
}
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
  selectedNodeKey.value = data.id
  if (data.leaf) {
    emit('nodeClick', data)
  } else {
    resourceListTree.value.setCurrentKey(null)
  }
}

const getTree = async () => {
  const request = { busiFlag: curCanvasType.value } as BusiTreeRequest
  const isDashboard = curCanvasType.value == 'dashboard'
  await interactiveStore.setInteractive(request)
  const interactiveData = isDashboard ? interactiveStore.getPanel : interactiveStore.getScreen
  const nodeData = interactiveData.treeNodes
  rootManage.value = interactiveData.rootManage
  anyManage.value = interactiveData.anyManage
  if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
    state.resourceTree = nodeData[0]['children'] || []
    afterTreeInit()
    return
  }
  state.resourceTree = nodeData
  afterTreeInit()
}

const afterTreeInit = () => {
  if (selectedNodeKey.value && returnMounted.value) {
    expandedArray.value = getDefaultExpandedKeys()
    returnMounted.value = false
  }
  nextTick(() => {
    resourceListTree.value.setCurrentKey(selectedNodeKey.value)
    nextTick(() => {
      if (selectedNodeKey.value) {
        const nodeDom = document.querySelector('.is-current')
        nodeDom && nodeDom.click()
      }
    })
  })
}

const emit = defineEmits(['nodeClick'])

const operation = (cmd: string, data: BusiTreeNode, nodeType: string) => {
  if (cmd === 'delete') {
    const msg = data.leaf ? '' : '删除后，此文件夹下的所有资源都会被删除，请谨慎操作。'
    ElMessageBox.confirm(
      data.leaf ? '确定删除该' + resourceLabel + '吗？' : '确定删除该文件夹吗？',
      {
        confirmButtonType: 'danger',
        type: 'warning',
        tip: msg,
        autofocus: false,
        showClose: false
      }
    ).then(() => {
      deleteLogic(data.id, dvInfo.value.type).then(() => {
        ElMessage.success('删除成功')
        if (dvInfo.value && dvInfo.value.id === data.id) {
          dvMainStore.resetDvInfo()
        }
        getTree()
      })
    })
  } else if (cmd === 'edit') {
    resourceEdit(data.id)
  } else {
    resourceGroupOpt.value.optInit(nodeType, data, cmd, ['copy'].includes(cmd))
  }
}

const addOperation = (
  cmd: string,
  data?: BusiTreeNode,
  nodeType?: string,
  parentSelect?: boolean
) => {
  // 新建子节点的操作流程为先进行创建 后面选择所在目录
  if (cmd === 'newLeaf') {
    const baseUrl =
      curCanvasType.value === 'dataV' ? '#/dvCanvas?opt=create' : '#/dashboard?opt=create'
    window.open(baseUrl, '_blank')
  } else {
    resourceGroupOpt.value.optInit(nodeType, data || {}, cmd, parentSelect)
  }
}

const resourceEdit = resourceId => {
  const baseUrl = curCanvasType.value === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  window.open(baseUrl + resourceId, '_blank')
}

const resourceOptFinish = () => {
  getTree()
}

const getParentKeys = (tree, targetKey, parentKeys = []) => {
  for (const node of tree) {
    if (node.id === targetKey) {
      return parentKeys
    }
    if (node.children) {
      const newParentKeys = [...parentKeys, node.id]
      const result = getParentKeys(node.children, targetKey, newParentKeys)
      if (result) {
        return result
      }
    }
  }
  return null
}

const getDefaultExpandedKeys = () => {
  const parentKeys = getParentKeys(state.resourceTree, selectedNodeKey.value)
  if (parentKeys) {
    return [selectedNodeKey.value, ...parentKeys]
  } else {
    return []
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
    <div class="tree-header">
      <div class="icon-methods" v-show="showPosition === 'preview'">
        <span class="title"> {{ resourceLabel }} </span>
        <div v-if="rootManage" class="flex-align-center">
          <el-tooltip content="新建文件夹" placement="top" effect="dark">
            <el-icon
              class="custom-icon btn"
              style="margin-right: 20px"
              @click="addOperation('newFolder', null, 'folder')"
            >
              <Icon name="dv-new-folder" />
            </el-icon>
          </el-tooltip>
          <el-tooltip :content="newResourceLabel" placement="top" effect="dark">
            <el-icon class="custom-icon btn" @click="addOperation('newLeaf', null, 'leaf', true)">
              <Icon v-if="curCanvasType === 'dashboard'" name="dv-new"></Icon>
              <Icon v-else name="dv-screen-new" />
            </el-icon>
          </el-tooltip>
        </div>
      </div>
      <el-input
        :placeholder="t('commons.search')"
        v-model="filterText"
        clearable
        class="search-bar"
      >
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <el-scrollbar class="custom-tree">
      <el-tree
        menu
        ref="resourceListTree"
        :default-expanded-keys="expandedArray"
        :data="state.resourceTree"
        :props="defaultProps"
        node-key="id"
        highlight-current
        :expand-on-click-node="true"
        :filter-node-method="filterNode"
        @node-expand="nodeExpand"
        @node-collapse="nodeCollapse"
        @node-click="nodeClick"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <el-icon style="font-size: 18px" v-if="!data.leaf">
              <Icon name="dv-folder"></Icon>
            </el-icon>
            <el-icon style="font-size: 18px" v-else-if="curCanvasType === 'dashboard'">
              <Icon name="dv-dashboard-spine"></Icon>
            </el-icon>
            <el-icon style="font-size: 18px" v-else>
              <Icon name="dv-screen-spine"></Icon>
            </el-icon>
            <span :title="node.label" class="label-tooltip">{{ node.label }}</span>

            <div class="icon-more" v-if="data.weight >= 7 && showPosition === 'preview'">
              <span v-on:click.stop>
                <el-icon v-if="data.leaf" class="hover-icon" @click="resourceEdit(data.id)">
                  <Icon name="icon_edit_outlined" />
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
              <dv-handle-more
                @handle-command="cmd => operation(cmd, data, data.leaf ? 'leaf' : 'folder')"
                :node="data"
                :any-manage="anyManage"
                :menu-list="data.leaf ? state.menuList : state.folderMenuList"
              ></dv-handle-more>
            </div>
          </span>
        </template>
      </el-tree>
      <de-resource-group-opt
        :cur-canvas-type="curCanvasType"
        @finish="resourceOptFinish"
        ref="resourceGroupOpt"
      />
    </el-scrollbar>
  </div>
</template>
<style lang="less" scoped>
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
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}

:deep(.ed-input__wrapper) {
  width: 80px;
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
</style>
