<script setup lang="ts">
import { onMounted, reactive, ref, toRefs, watch, nextTick, computed } from 'vue'
import { copyResource, deleteLogic, ResourceOrFolder } from '@/api/visualization/dataVisualization'
import { ElIcon, ElMessage, ElMessageBox, ElScrollbar } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { useEmitt } from '@/hooks/web/useEmitt'
import { HandleMore } from '@/components/handle-more'
import DeResourceGroupOpt from '@/views/common/DeResourceGroupOpt.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useAppStoreWithOut } from '@/store/modules/app'
import { storeToRefs } from 'pinia'
import DvHandleMore from '@/components/handle-more/src/DvHandleMore.vue'
import { interactiveStoreWithOut } from '@/store/modules/interactive'
const interactiveStore = interactiveStoreWithOut()
import router from '@/router'
import { useI18n } from '@/hooks/web/useI18n'
import _ from 'lodash'
import DeResourceCreateOptV2 from '@/views/common/DeResourceCreateOptV2.vue'
import { useCache } from '@/hooks/web/useCache'
import { findParentIdByChildIdRecursive } from '@/utils/canvasUtils'
import { XpackComponent } from '@/components/plugin'
import treeSort from '@/utils/treeSortUtils'
const { wsCache } = useCache()

const dvMainStore = dvMainStoreWithOut()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
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
const mounted = ref(false)
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
const resourceCreateOpt = ref()
const returnMounted = ref(false)
const state = reactive({
  curSortType: 'time_desc',
  resourceTree: [] as BusiTreeNode[],
  originResourceTree: [] as BusiTreeNode[],
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
  sortType: [
    {
      label: '按时间升序',
      value: 'time_asc'
    },
    {
      label: '按时间降序',
      value: 'time_desc'
    },
    {
      label: '按名称升序',
      value: 'name_asc'
    },
    {
      label: '按名称降序',
      value: 'time_asc'
    }
  ],
  templateCreatePid: 0
})

const dvSvgType = computed(() =>
  curCanvasType.value === 'dashboard' ? 'dv-dashboard-spine' : 'dv-screen-spine'
)

const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)

const resourceTypeList = computed(() => {
  const list = [
    {
      label: '空白新建',
      svgName: dvSvgType.value,
      command: 'newLeaf'
    },
    {
      label: '使用模板新建',
      svgName: 'dv-use-template',
      command: 'newFromTemplate'
    },
    {
      label: '新建文件夹',
      divided: true,
      svgName: 'dv-folder',
      command: 'newFolder'
    }
  ]
  return list
})

const menuList = computed(() => {
  const list = [
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
  ]
  return list
})

const dvId = embeddedStore.dvId || router.currentRoute.value.query.dvId
if (dvId && showPosition.value === 'preview') {
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
  if (dvInfo.value && dvInfo.value.id && !JSON.stringify(nodeData).includes(dvInfo.value.id)) {
    dvMainStore.resetDvInfo()
  }
  if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
    state.originResourceTree = nodeData[0]['children'] || []
    sortTypeChange(state.curSortType)
    afterTreeInit()
    return
  }
  state.originResourceTree = nodeData
  sortTypeChange(state.curSortType)
  afterTreeInit()
}

const flattedTree = computed<BusiTreeNode[]>(() => {
  return _.filter(flatTree(state.resourceTree), node => node.leaf)
})

const hasData = computed<boolean>(() => flattedTree.value.length > 0)

function flatTree(tree: BusiTreeNode[]) {
  let result = _.cloneDeep(tree)
  _.forEach(tree, node => {
    if (node.children && node.children.length > 0) {
      result = _.union(result, flatTree(node.children))
    }
  })
  return result
}

const afterTreeInit = () => {
  mounted.value = true
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
    resourceListTree.value.filter(filterText.value)
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
      deleteLogic(data.id, curCanvasType.value).then(() => {
        ElMessage.success('删除成功')
        getTree()
      })
    })
  } else if (cmd === 'edit') {
    resourceEdit(data.id)
  } else if (cmd === 'copy') {
    const targetPid = findParentIdByChildIdRecursive(state.resourceTree, data.id)
    const params: ResourceOrFolder = {
      nodeType: nodeType as 'folder' | 'leaf',
      name: data.name + '-copy',
      type: curCanvasType.value,
      id: data.id,
      pid: targetPid || '0'
    }
    copyResource(params).then(data => {
      const baseUrl =
        curCanvasType.value === 'dataV'
          ? `#/dvCanvas?opt=copy&pid=${params.pid}&dvId=${data.data}`
          : `#/dashboard?opt=copy&pid=${params.pid}&resourceId=${data.data}`
      if (isEmbedded.value) {
        embeddedStore.clearState()
        embeddedStore.setPid(params.pid as string)
        embeddedStore.setOpt('copy')
        if (curCanvasType.value === 'dataV') {
          embeddedStore.setDvId(data.data)
        } else {
          embeddedStore.setResourceId(data.data)
        }
        useEmitt().emitter.emit(
          'changeCurrentComponent',
          curCanvasType.value === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
        )
        return
      }
      const newWindow = window.open(baseUrl, '_blank')
      initOpenHandler(newWindow)
    })
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
    let newWindow = null
    if (isEmbedded.value) {
      embeddedStore.clearState()
      embeddedStore.setOpt('create')
      if (data?.id) {
        embeddedStore.setPid(data?.id as string)
      }
      useEmitt().emitter.emit(
        'changeCurrentComponent',
        curCanvasType.value === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
      )
      return
    }
    if (data?.id) {
      newWindow = window.open(baseUrl + `&pid=${data.id}`, '_blank')
    } else {
      newWindow = window.open(baseUrl, '_blank')
    }
    initOpenHandler(newWindow)
  } else if (cmd === 'newFromTemplate') {
    const params = {
      curPosition: 'create',
      pid: data?.id,
      templateType: curCanvasType.value === 'dataV' ? 'SCREEN' : 'PANEL'
    }
    resourceCreateOpt.value.optInit(params)
  } else {
    resourceGroupOpt.value.optInit(nodeType, data || {}, cmd, parentSelect)
  }
}

function createNewObject() {
  return addOperation('newLeaf', null, 'leaf', true)
}

const resourceEdit = resourceId => {
  const baseUrl = curCanvasType.value === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  if (isEmbedded.value) {
    embeddedStore.clearState()
    if (curCanvasType.value === 'dataV') {
      embeddedStore.setDvId(resourceId)
    } else {
      embeddedStore.setResourceId(resourceId)
    }
    useEmitt().emitter.emit(
      'changeCurrentComponent',
      curCanvasType.value === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
    )
    return
  }

  const newWindow = window.open(baseUrl + resourceId, '_blank')
  initOpenHandler(newWindow)
}

const resourceOptFinish = () => {
  getTree()
}

const resourceCreateFinish = templateData => {
  // do create
  wsCache.set(`de-template-data`, JSON.stringify(templateData))
  const baseUrl =
    curCanvasType.value === 'dataV'
      ? '#/dvCanvas?opt=create&createType=template'
      : '#/dashboard?opt=create&createType=template'
  let newWindow = null
  if (isEmbedded.value) {
    embeddedStore.clearState()
    embeddedStore.setOpt('create')
    embeddedStore.setCreateType('template')
    if (state.templateCreatePid) {
      embeddedStore.setPid(state.templateCreatePid as unknown as string)
    }
    useEmitt().emitter.emit(
      'changeCurrentComponent',
      curCanvasType.value === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
    )
    return
  }

  if (state.templateCreatePid) {
    newWindow = window.open(baseUrl + `&pid=${state.templateCreatePid}`, '_blank')
  } else {
    newWindow = window.open(baseUrl, '_blank')
  }
  initOpenHandler(newWindow)
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

const sortTypeTip = computed(() => {
  return sortList.find(ele => ele.value === state.curSortType).name
})

const sortTypeChange = sortType => {
  state.resourceTree = treeSort(state.originResourceTree, sortType)
  state.curSortType = sortType
  wsCache.set('TreeSort-' + curCanvasType.value, state.curSortType)
}

watch(filterText, val => {
  resourceListTree.value.filter(val)
})

const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}

const loadInit = () => {
  const historyTreeSort = wsCache.get('TreeSort-' + curCanvasType.value)
  if (historyTreeSort) {
    state.curSortType = historyTreeSort
  }
}
onMounted(() => {
  loadInit()
  getTree()
})

defineExpose({
  rootManage,
  hasData,
  createNewObject,
  mounted
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
            <el-dropdown popper-class="menu-outer-dv_popper" trigger="hover">
              <el-icon class="custom-icon btn" @click="addOperation('newLeaf', null, 'leaf', true)">
                <Icon name="icon_file-add_outlined" />
              </el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="addOperation('newLeaf', null, 'leaf', true)">
                    <el-icon :class="`handle-icon color-${curCanvasType}`">
                      <Icon :name="dvSvgType"></Icon>
                    </el-icon>
                    空白新建
                  </el-dropdown-item>
                  <el-dropdown-item @click="addOperation('newFromTemplate', null, 'leaf', true)">
                    <el-icon class="handle-icon">
                      <Icon name="dv-use-template"></Icon>
                    </el-icon>
                    使用模板新建
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
              <Icon
                :name="data.extraFlag ? 'dv-dashboard-spine-mobile' : 'dv-dashboard-spine'"
              ></Icon>
            </el-icon>
            <el-icon class="icon-screen-new color-dataV" style="font-size: 18px" v-else>
              <Icon name="icon_operation-analysis_outlined"></Icon>
            </el-icon>
            <span :title="node.label" class="label-tooltip">{{ node.label }}</span>

            <div
              class="icon-more flex-align-center"
              v-if="data.weight >= 7 && showPosition === 'preview'"
            >
              <el-icon
                v-on:click.stop
                v-if="data.leaf"
                class="hover-icon"
                @click="resourceEdit(data.id)"
              >
                <Icon name="icon_edit_outlined" />
              </el-icon>
              <handle-more
                @handle-command="
                  cmd => addOperation(cmd, data, cmd === 'newFolder' ? 'folder' : 'leaf')
                "
                :menu-list="resourceTypeList"
                icon-name="icon_add_outlined"
                placement="bottom-start"
                v-if="!data.leaf"
              ></handle-more>
              <dv-handle-more
                @handle-command="cmd => operation(cmd, data, data.leaf ? 'leaf' : 'folder')"
                :node="data"
                :any-manage="anyManage"
                :resource-type="curCanvasType"
                :menu-list="data.leaf ? menuList : state.folderMenuList"
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
      <de-resource-create-opt-v2
        :cur-canvas-type="curCanvasType"
        ref="resourceCreateOpt"
        @finish="resourceCreateFinish"
      ></de-resource-create-opt-v2>
    </el-scrollbar>
  </div>
  <XpackComponent ref="openHandler" jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI=" />
</template>
<style lang="less" scoped>
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
      font-size: 20px;
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

  .icon-screen-new {
    border-radius: 4px;
    color: #fff;
    padding: 3px;
  }
}
</style>

<style lang="less">
.menu-outer-dv_popper {
  width: 140px;
  margin-top: -2px !important;

  .ed-icon {
    border-radius: 4px;
  }
}

.sort-type-normal {
  i {
    display: none;
  }
}

.sort-type-checked {
  color: var(--ed-color-primary);
  i {
    display: block;
  }
}
</style>
