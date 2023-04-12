<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import OrgForm from './OrgForm.vue'
export interface User {
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
const props = defineProps({
  treeData: {
    type: Array as () => User[],
    default: () => []
  }
})
const { t } = useI18n()

const state = reactive({
  maps: new Map()
})
const tree = ref()
const table = ref(null)
const resourceType = ref(3)
const tableData = ref<User[]>([])
const orgId = ref('')
const deptId = ref('')
const loadFirst = ref(false)
const selectDepts = ref([])
const treeDataFormat = ref([])
const treeDataMap = ref([])
const createOrgForm = ref()
const searchNodeLoading = ref(false)
const treeDefaultProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
}
const filterMethod = val => {
  tree.value.filter(val)
}
const filterNode = (value, data) => {
  if (!value) return true
  return data.name.indexOf(value) !== -1
}
const initTree = () => {
  treeDataFormat.value = props.treeData.map(dept => {
    return normalizer(dept)
  })
  if (deptId.value) {
    treeDataFormat.value = treeDataFormat.value.filter(ele => ele.deptId !== deptId.value)
  }
  treeDataMap.value = treeDataFormat.value.map(ele => ele.name)
}
const executeAxios = (url, type, data, callBack) => {
  console.log(url, type, data, callBack)
}
const loadNode = (node, resolve) => {
  if (!treeDataFormat.value.length || !loadFirst.value) {
    loadFirst.value = true
    initTree()
    return
  }
  executeAxios('/plugin/dept/childNodes/' + node.data.deptId, 'post', {}, res => {
    if (deptId.value) {
      res.data = res.data.filter(ele => ele.deptId !== deptId.value)
    }
    resolve(
      res.data.map(dept => {
        return normalizer(dept)
      })
    )
  })
}
const normalizer = node => {
  return {
    ...node,
    leaf: !node.hasChildren
  }
}

const handleNodeClick = (data, node) => {
  selectDepts.value = [data]
  orgId.value = data.deptId
  treeDataMap.value = []
  if (!node) return
  if (!node.isLeaf) {
    if (!node.childNodes.length) {
      searchNodeLoading.value = true
      executeAxios('/plugin/dept/childNodes/' + data.deptId, 'post', {}, res => {
        treeDataMap.value = res.data.map(ele => ele.name)
        searchNodeLoading.value = false
      })
    } else {
      treeDataMap.value = node.childNodes.map(ele => ele.data.name)
    }
  }
}
const timestampFormatDate = value => {
  console.log(value)
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
const reloadByPid = pid => {
  if (pid !== 0 && state.maps.get(pid)) {
    const { row, treeNode, resolve } = state.maps.get(pid)
    table.value.store.states.lazyTreeNodeMap.pid = []
    loadExpandData(row, treeNode, resolve)
  }
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

const edit = row => {
  console.log(row)
  createOrgForm.value.init()
}
// 加载表格数据
const search = () => {
  tableData.value = []
  let conditionExist = false
  const param = { conditions: [] }
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
</script>

<template>
  <el-row class="top-operate">
    <el-col :span="8">
      组织: &nbsp;
      <el-popover placement="bottom" popper-class="dept-popper dept" width="200" trigger="click">
        <template #reference>
          <el-select
            v-model="orgId"
            filterable
            style="width: 200px"
            :filter-method="filterMethod"
            clearable
            popper-class="tree-select"
            :placeholder="t('fu.search_bar.please_input')"
          >
            <el-option
              v-for="item in selectDepts"
              :key="item.name"
              :label="item.name"
              :value="item.deptId"
            />
          </el-select>
        </template>
        <el-tree
          :load="loadNode"
          :lazy="true"
          ref="tree"
          :expand-on-click-node="false"
          :data="treeDataFormat"
          :highlight-current="!!orgId"
          check-on-click-node
          :filter-node-method="filterNode"
          :props="treeDefaultProps"
          @node-click="handleNodeClick"
        ></el-tree>
      </el-popover>
    </el-col>
    <el-col :span="16" class="right-filter">
      资源类型:&nbsp;
      <el-radio-group v-model="resourceType">
        <el-radio :label="3">仪表板</el-radio>
        <el-radio :label="6">数据大屏</el-radio>
        <el-radio :label="9">数据集</el-radio>
        <el-radio :label="9">数据源</el-radio>
      </el-radio-group>
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
      <el-table-column type="selection" width="55" />
      <el-table-column label="资源名称" prop="name">
        <template v-slot="scope">
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
      <el-table-column label="创建人" prop="create" />
      <el-table-column prop="createTime" :label="t('organization.create_time')">
        <template #default="scope">
          <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column :label="t('table.actions')" fixed="right" width="186">
        <template #default="scope">
          <el-button @click="edit(scope.row)" text>{{ t('commons.edit') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <org-form ref="createOrgForm"></org-form>
  </div>
</template>
