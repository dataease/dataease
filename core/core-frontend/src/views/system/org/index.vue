<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { Icon } from '@/components/icon-custom'
import DeptEditer from './DeptEditer.vue'
import OrgResources from './OrgResources.vue'
const { t } = useI18n()

const activeName = ref('manage')
const nickName = ref('')

const timestampFormatDate = value => {
  console.log(value)
}

const table = ref(null)

// const formatter = (_row, _column, cellValue) => {
//   return cellValue || '-'
// }

// const formatterUser = (_row, _column, cellValue) => {
//   return (cellValue || []).length ? cellValue.join(' ') : '-'
// }

const tableData = ref<User[]>([])
const tableDataAll = ref([])
const defaultCondition = {
  field: 'pid',
  operator: 'eq',
  value: ''
}

const state = reactive({
  columns: [],
  orgList: [],
  maps: new Map(),
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})

interface User {
  deptId: number
  pid: number
  subCount: number
  name: string
  deptSort: number
  createBy?: string
  updateBy?: string
  createTime: number
  updateTime: number
  hasChildren: boolean
  leaf: boolean
  top: boolean
}

const load = (_: User, __: unknown, resolve: (date: User[]) => void) => {
  setTimeout(() => {
    resolve([
      {
        deptId: 3,
        pid: 2,
        subCount: 1,
        name: 'wei的二级组织',
        deptSort: null,
        createBy: null,
        updateBy: null,
        createTime: 1667202481457,
        updateTime: 1667202481457,
        hasChildren: true,
        leaf: false,
        top: false
      },
      {
        deptId: 4,
        pid: 2,
        subCount: 0,
        name: 'yyp',
        deptSort: null,
        createBy: null,
        updateBy: null,
        createTime: 1667977447506,
        updateTime: 1667977447506,
        hasChildren: false,
        leaf: true,
        top: false
      }
    ])
  }, 1000)
}

tableData.value = [
  {
    deptId: 2,
    pid: 0,
    subCount: 2,
    name: 'wei的组织',
    deptSort: null,
    createBy: null,
    updateBy: null,
    createTime: 1667202467619,
    updateTime: 1667202467619,
    hasChildren: true,
    leaf: false,
    top: true
  },
  {
    deptId: 5,
    pid: 0,
    subCount: 0,
    name: 'jinlong',
    deptSort: null,
    createBy: null,
    updateBy: null,
    createTime: 1669645057174,
    updateTime: 1669645057174,
    hasChildren: false,
    leaf: true,
    top: true
  },
  {
    deptId: 1,
    pid: 0,
    subCount: 0,
    name: '默认组织1121',
    deptSort: 0,
    createBy: null,
    updateBy: null,
    createTime: 1622533297817,
    updateTime: 1679037885732,
    hasChildren: false,
    leaf: true,
    top: true
  }
]

const handleClick = () => {
  console.log('handleClick')
}

// const handleDelete = () => {
//   console.log('handleClick')
// }
const addOrg = row => {
  console.log(row)
}

const executeAxios = (url, type, data, callBack) => {
  const param = {
    url: url,
    type: type,
    data: data,
    callBack: callBack
  }

  console.log(param)
}

const edit = row => {
  console.log(row)
}
// 加载表格数据
const search = () => {
  tableData.value = []
  let conditionExist = false
  const param = { conditions: [defaultCondition] }
  if (nickName.value) {
    param.conditions.splice(0, 1, {
      field: 'name',
      operator: 'like',
      value: nickName.value
    })
    conditionExist = true
  }
  executeAxios('/plugin/dept/search', 'post', param, res => {
    let data = res.data
    data = data.map(obj => {
      if (obj.subCount > 0) {
        obj.hasChildren = true
      }
      return obj
    })

    if (conditionExist) {
      data = data.map(node => {
        delete node.hasChildren
        return node
      })
      tableData.value = buildTree(data)
      nextTick(() => {
        data.forEach(node => {
          table.value.toggleRowExpansion(node, true)
        })
      })
    } else {
      tableData.value = data
      if (!tableDataAll.value.length) {
        tableDataAll.value = JSON.parse(JSON.stringify(data))
      }
    }
  })
}
const buildTree = arrs => {
  const idMapping = arrs.reduce((acc, el, i) => {
    acc[el.deptId] = i
    return acc
  }, {})
  const roots = []
  arrs.forEach(el => {
    el.children = []
    // 判断根节点
    if (el.pid === null || el.pid === 0) {
      roots.push(el)
      return
    }
    // 用映射表找到父元素
    const parentEl = arrs[idMapping[el.pid]]
    // 把当前元素添加到父元素的`children`数组中
    parentEl.children = [...(parentEl.children || []), el]
  })
  return roots
}

// 加载下一级子节点数据
const loadExpandData = (row, treeNode, resolve) => {
  executeAxios('/plugin/dept/childNodes/' + row.deptId, 'post', {}, res => {
    let data = res.data
    data = data.map(obj => {
      obj.pidName = row.name
      if (obj.subCount > 0) {
        obj.hasChildren = true
      }
      return obj
    })
    state.maps.set(row.deptId, { row, treeNode, resolve })
    resolve && resolve(data)
  })
}
const deptIsEmpty = organization => {
  if (organization.subCount) {
    canNotDelete()
    return
  }
  const param = {
    deptId: organization.deptId,
    section: 1,
    showDept: 1,
    showRole: 1
  }
  const url = '/plugin/dept/userGrid/0/0'
  executeAxios(url, 'post', param, response => {
    if (response.data.itemCount === 0) {
      _handleDeleteZero(organization)
    } else {
      canNotDelete()
    }
  })
}
const canNotDelete = () => {
  ElMessageBox.confirm(t('organization.cannot_delete'), {
    confirmButtonText: t(' commons.roger_that'),
    showCancelButton: false,
    tip: t('organization.remove_user_first'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
}
const _handleDeleteZero = organization => {
  ElMessageBox.confirm(t('organization.sure_delete_organization'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    const requests = [{ deptId: organization.deptId, pid: organization.pid }]
    executeAxios('/plugin/dept/delete', 'post', requests, () => {
      ElMessage({
        message: 'commons.delete_success',
        type: 'success'
      })
      search()
      reloadByPid(organization.pid)
    })
  })
}

const reloadByPid = pid => {
  if (pid !== 0 && state.maps.get(pid)) {
    const { row, treeNode, resolve } = state.maps.get(pid)
    table.value.store.states.lazyTreeNodeMap.pid = []
    loadExpandData(row, treeNode, resolve)
  }
}
// const btnDisabled = row => {
//   return row.deptId === 1 || row.subCount > 0
// }
const startDrag = ev => {
  ev.dataTransfer.setData('sourceId', ev.target.id)
}
const allowDrop = ev => {
  ev.preventDefault()
}
const endDrag = ev => {
  ev.preventDefault()
  const targetId = ev.target.id
  const sourceId = ev.dataTransfer.getData('sourceId')

  nodeMoveHandler(sourceId, targetId)
}
const nodeMoveHandler = (resourceId, targetId) => {
  if (!resourceId || !targetId) return
  resourceId = parseInt(resourceId)
  targetId = parseInt(targetId)
  const parent = getParent(resourceId)
  const pid = parent ? parent.id || parent.deptId : null

  if (isParentNoChange(pid, targetId)) {
    ElMessage({
      message: t('dept.can_not_move_change_sort'),
      type: 'error'
    })
    return
  }

  if (isParent2Children(resourceId, targetId)) {
    ElMessage({
      message: t('dept.can_not_move_parent_to_children'),
      type: 'error'
    })
    return
  }
  const param = {
    resourceId,
    targetId
  }
  executeAxios('/plugin/dept/move', 'post', param, () => {
    ElMessage({
      message: t('dept.move_success'),
      type: 'success'
    })
    search()
    reloadByPid(targetId)
    if (pid) {
      reloadByPid(pid)
    }
  })
}
// 判断是否是父节点移动到子节点
const isParent2Children = (resourceId, targetId) => {
  let currentId = targetId
  while (currentId) {
    if (resourceId === currentId) {
      // 说明 目标节点是 源节点的子集
      return true
    }
    let parent
    if ((parent = getParent(currentId)) === null) {
      // 找到顶都没有 说明就不是子集
      return false
    }
    currentId = parent.id || parent.deptId
  }
}
// 判断是否移动前后父节点没有改变
const isParentNoChange = (pid, targetId) => {
  return pid === targetId
}
// 根据deptId获取parent
const getParent = id => {
  const currentNode = getNodeWithId(id)
  if (!currentNode || !currentNode.pid) {
    return null
  }
  return getNodeWithId(currentNode.pid)
}
const getNodeWithId = id => {
  for (let index = 0; index < tableData.value.length; index++) {
    const element = tableData[index]
    if ((element.id || element.deptId) === id) {
      return element
    }
  }
  let lazyTreeNodeMap = table.value.store.states.lazyTreeNodeMap
  for (const key in lazyTreeNodeMap) {
    if (Object.hasOwnProperty.call(lazyTreeNodeMap, key)) {
      const childNodes = lazyTreeNodeMap[key]
      for (let i = 0; i < childNodes.length; i++) {
        const node = childNodes[i]
        if ((node.id || node.deptId) === id) {
          return node
        }
      }
    }
  }
  return null
}
</script>

<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane label="组织管理" name="manage"></el-tab-pane>
    <el-tab-pane label="组织资源" name="resources"></el-tab-pane>
  </el-tabs>
  <div class="org-table__content de-search-table" v-if="activeName === 'manage'">
    <el-row class="top-operate">
      <el-col :span="12">
        <el-button @click="addOrg" type="primary">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          {{ t('organization.add_organization') }}
        </el-button>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input
          :placeholder="t('organization.search_by_name')"
          v-model="nickName"
          ref="search"
          clearable
          @blur="search"
          @clear="search"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>
    <div class="table-container-org">
      <el-table
        ref="table"
        :data="tableData"
        header-cell-class-name="header-cell"
        lazy
        :load="load"
        :indent="30"
        style="width: 100%"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        row-key="deptId"
      >
        <el-table-column :label="t('organization.name')" prop="name">
          <template #default="scope">
            <span
              class="dept-name"
              :class="
                (scope.row.subCount === 0 && scope.row.pid === 0) ||
                (!scope.row.hasChildren &&
                  (!scope.row.children || (scope.row.children && !scope.row.children.length)))
                  ? 'indent-no-child'
                  : ''
              "
              @drop="endDrag"
              @dragover="allowDrop"
              :data-id="scope.row.deptId"
            >
              <span :id="scope.row.deptId" draggable="true" @dragstart="startDrag">{{
                scope.row.name
              }}</span>
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="t('organization.sub_organizations')" prop="subCount" />
        <el-table-column prop="createTime" :label="t('organization.create_time')">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column :label="t('table.actions')" fixed="right" width="186">
          <template #default="scope">
            <el-button @click="edit(scope.row)" type="text">{{ t('commons.edit') }}</el-button>

            <template v-if="scope.row.deptId === 1">
              <el-tooltip
                class="item"
                effect="dark"
                :content="t('organization.default_organization_cannot_move')"
                placement="left"
              >
                <div class="btn-outer">
                  <el-button disabled type="text">{{ t('commons.delete') }}</el-button>
                </div>
              </el-tooltip>
            </template>
            <el-button v-else @click="deptIsEmpty(scope.row)" type="text">{{
              t('commons.delete')
            }}</el-button>
            <el-button @click="addOrg(scope.row)" type="text">{{
              t('organization.add_child_org')
            }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <dept-editer :treeData="tableData"></dept-editer>
  </div>
  <div v-else class="org-table__content de-search-table">
    <org-resources></org-resources>
  </div>
</template>

<style lang="less">
.org-table__content {
  padding: 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 60px);
  box-sizing: border-box;
  margin-top: 12px;
}
.table-container-org {
  height: calc(100vh - 260px);

  :deep(.el-icon-arrow-right::before) {
    content: '\E791' !important;
  }
}
</style>
