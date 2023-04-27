<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import {
  queryUserApi,
  queryRoleApi,
  resourceTreeApi,
  menuTreeApi,
  resourcePerApi,
  menuPerApi,
  busiPerSaveApi,
  menuPerSaveApi
} from '@/api/auth'
const { t } = useI18n()
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const selectedTarget = ref('')
const selectedResourceType = ref('panel')
const emptyDescription = ref('')
const authTable = ref(null)
const roleChecked = ref(true)
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root: boolean
}
interface PermissionRequest {
  id: string
  type: number
  flag: string
}
const activeNameChange = tabName => {
  nickName.value = ''
  selectedTarget.value = ''
  if (activeAuth.value === 'menu' && tabName === 'user') {
    activeAuth.value = 'resource'
  }
}

const authActiveChange = tabName => {
  if (tabName === 'menu') {
    const id = 'menu'
    if (state.treeMap[id]) {
      getColumn(id)
      state.tableData = state.treeMap[id]
    } else {
      menuTreeApi().then(res => {
        getColumn('menu')
        state.tableData = res.data
        state.treeMap['menu'] = res.data
      })
    }
  }

  if (tabName === 'resource') {
    const id = selectedResourceType.value
    getColumn(id)
    state.tableData = state.treeMap[id]
  }
}

const resourceTypeList = [
  {
    id: 'panel',
    name: t('auth.panel')
  },
  { id: 'screen', name: t('auth.screen') },
  { id: 'dataset', name: t('auth.dataset') },
  { id: 'datasource', name: t('auth.datasource') }
]
const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id',
  disabled: 'disabled'
}
const state = reactive({
  userList: [],
  roleList: [],
  tableData: [],
  tableColumn: [],
  globalColumn: [],
  treeMap: {},
  uncommitted: [],
  sourceData: {},
  expandedKeys: []
})
state.roleList = [
  {
    id: 'admin',
    name: '组织管理员',
    children: null,
    disabled: true
  },
  {
    id: 'readonly',
    name: '普通用户',
    children: null,
    disabled: true
  }
]
state.globalColumn = [
  { type: 'datasource, dataset, menu', label: '使用', weightLevel: 1 },
  { type: 'panel, screen', label: '查看', weightLevel: 1 },
  { type: 'panel, screen', label: '导出', weightLevel: 4 },
  { type: 'datasource, dataset, panel, screen', label: '管理', weightLevel: 7 },
  { label: '授权', weightLevel: 9 }
]
// 选中角色事件
const roleNodeClick = (data: Tree) => {
  if (data.disabled || data.root) {
    return
  }
  const change = (data: Tree) => {
    if (selectedTarget.value === data.id) {
      return
    }
    selectedTarget.value = data.id
    loadPermission(1)
  }

  if (uncommittedTips(() => change(data))) {
    change(data)
  }
}
// 选中用户事件
const targetClick = (id: string) => {
  const change = (id: string) => {
    if (selectedTarget.value === id) {
      return
    }
    selectedTarget.value = id
    loadPermission(0)
  }
  if (uncommittedTips(() => change(id))) {
    change(id)
  }
}

const resourceTypeClick = async (id: string) => {
  const change = async (id: string) => {
    if (selectedResourceType.value === id) {
      return
    }
    selectedResourceType.value = id
    if (state.treeMap[id]) {
      state.tableData = state.treeMap[id]
    } else {
      const res = await resourceTreeApi(id)
      state.tableData = res.data
    }
    getColumn(id)
    // 如果有selectedTarget 再查权限
    if (selectedTarget.value) {
      const type = activeName.value === 'user' ? 0 : 1
      loadPermission(type)
    }
  }
  if (uncommittedTips(() => change(id))) {
    change(id)
  }
}

const getColumn = (type: string) => {
  const array = state.globalColumn.filter(item => !item.type || item.type.includes(type))
  state.tableColumn = array
}

const loadResourceTree = () => {
  const id = selectedResourceType.value
  resourceTreeApi(id).then(res => {
    getColumn(id)
    state.tableData = res.data
    state.treeMap[id] = res.data
  })
}

const loadUser = () => {
  const param = { keyword: nickName.value }
  queryUserApi(param).then(res => {
    if (res?.data?.length) {
      state.userList = res.data
    } else {
      state.userList = []
    }
  })
}

const loadRole = () => {
  const param = { keyword: nickName.value }
  queryRoleApi(param).then(res => {
    if (res?.data?.length) {
      const roles = res.data
      const map = groupBy(roles)
      state.roleList[0].children = map.get(false)
      state.roleList[1].children = map.get(true)
    }
  })
}
const groupBy = (list: Tree[]) => {
  const map = new Map()
  list.forEach(item => {
    const readonly = item.readonly
    let arr = map.get(readonly)
    if (!arr) {
      arr = []
    }
    item.disabled = false
    arr.push(item)
    map.set(readonly, arr)
  })
  return map
}
const loadPermission = (type: number) => {
  emptyDescription.value = ''
  resetTableData(state.tableData)
  state.expandedKeys = []
  if (activeAuth.value === 'menu') {
    menuPerApi({ id: selectedTarget.value }).then(res => {
      const vo = res.data
      if (isOrgAdminPer(vo, type)) {
        return
      }
      const permissionMap = groupPermission(vo)

      fillTableData(state.tableData, permissionMap)
    })
    return
  }
  const param: PermissionRequest = {
    id: selectedTarget.value,
    flag: selectedResourceType.value.toUpperCase(),
    type
  }
  resourcePerApi(param).then(res => {
    const vo = res.data
    if (isOrgAdminPer(vo, type)) {
      return
    }
    const permissionMap = groupPermission(vo)
    fillTableData(state.tableData, permissionMap)
  })
}

const isOrgAdminPer = (vo, type) => {
  if (vo?.root && !vo.readonly) {
    emptyDescription.value = type
      ? '组织管理员已拥有所有资源的权限，无需再授权'
      : '该用户是组织管理员，已拥有所有资源的权限，无需再授权'
    return true
  }
  return false
}

const groupPermission = vo => {
  const map = new Map()
  const expandedKeys = new Set<string>()
  const origins = vo.permissionOrigins
  const permissions = vo.permissions
  const cols = state.tableColumn

  const buildPermissionMap = (type, list, rname) => {
    list?.length &&
      list.forEach(item => {
        const { id, weight } = item
        const originLevelobj = buildCallback(type, item, rname)
        const obj = Object.assign({ id }, originLevelobj)
        map.set(id, obj)
        if (weight) {
          expandedKeys.add(id)
        }
      })
  }
  const buildCallback = (type: number, item, rname: string) => {
    if (type === 0) {
      const originLevelobj = {}
      cols.forEach(col => {
        originLevelobj['level' + col.weightLevel] = { show: false, roles: new Set<string>() }
      })
      originLevelobj['weight'] = item['weight']
      return originLevelobj
    } else {
      const { id, weight } = item
      const originLevelobj = map.get(id) || { id }

      originLevelobj['weight'] = originLevelobj['weight'] || 0
      const userWeight = originLevelobj['weight']
      cols.forEach(col => {
        const weightLevel = col.weightLevel
        const temp = originLevelobj['level' + weightLevel] || {}
        const roleMatch = weight >= weightLevel
        temp['show'] = userWeight < weightLevel && roleMatch
        if (roleMatch) {
          const roles = temp['roles'] || new Set<string>()
          roles.add(rname)
          temp['roles'] = roles
        }
        originLevelobj['level' + weightLevel] = temp
      })
      return originLevelobj
    }
  }
  buildPermissionMap(0, permissions, null)

  origins?.length &&
    origins.forEach(item => {
      const pers = item.permissions
      buildPermissionMap(1, pers, item.name)
    })
  state.uncommitted = []
  state.sourceData = map
  expandNodes(Array.from(expandedKeys))
  return map
}

const expandNodes = (ids: string[]) => {
  const datalist = state.tableData
  let result = []
  const match = (list, targetids, parentlist) => {
    if (!targetids?.length) return
    for (let i = 0; i < list.length; i++) {
      const item = list[i]
      if (targetids.includes(item.id)) {
        targetids = targetids.filter(id => id !== item.id)
        result = [...result, ...parentlist]
      }

      if (item.children?.length) {
        parentlist.push(item.id)
        match(item.children, targetids, parentlist)
        const len = parentlist.length
        len && parentlist.splice(len - 1, 1)
      }
    }
  }
  match(datalist, ids, [])
  state.expandedKeys = Array.from(new Set([...result]))
}

const fillTableData = (rows, maps) => {
  rows?.forEach(row => {
    const temp = (maps?.get && maps.get(row.id)) || {}
    state.tableColumn?.forEach(col => {
      const weight = temp['weight'] || 0
      const weightLevel = col.weightLevel
      temp['value' + weightLevel] = false
      if (weight >= weightLevel) {
        temp['value' + weightLevel] = true
      }
    })
    Object.assign(row, temp)
    if (row.children?.length) {
      fillTableData(row.children, maps)
    }
  })
}
const independentAuth = (row, level) => {
  row['independent' + level] = true
  nextTick(() => {
    row['value' + level] = true
    rowWeightChanged(row, level)
    row['independent' + level] = false
  })
}
const rowWeightChanged = (row, level) => {
  const check = row['value' + level]
  if (check) {
    state.tableColumn.forEach(col => {
      if (level >= col.weightLevel) {
        row['value' + col.weightLevel] = true
      }
    })
    row['weight'] = level
  } else {
    let finalWeight = 0
    let index = state.tableColumn.indexOf(level)
    if (index--) {
      finalWeight = state.tableColumn[index]
    }
    row['weight'] = finalWeight
    state.tableColumn.forEach(col => {
      const curLevel = col.weightLevel
      if (curLevel >= level) {
        row['value' + curLevel] = false
        // 下面3行用作取消用户授权之后 恢复角色覆盖效果
        const levelObj = row['level' + curLevel]
        if (levelObj && levelObj['roles'] && levelObj['roles'].size) {
          row['level' + curLevel]['show'] = true
        }
      }
    })
  }
  const item = state.sourceData['get'](row.id)

  if (item?.weight !== row['weight'] || (!item?.weight && row['weight'])) {
    add2Uncommitted(row.id, row['weight'])
  } else {
    removeFromUncommitted(row.id)
  }

  if (row.children?.length) {
    row.children.forEach(item => {
      item['value' + level] = check
      rowWeightChanged(item, level)
    })
  }
}
const add2Uncommitted = (id: string, weight: number) => {
  let match = false
  state.uncommitted.forEach(item => {
    if (item.id === id) {
      item.weight = weight
      match = true
      return false
    }
  })
  match || state.uncommitted.push({ id, weight })
}
const removeFromUncommitted = id => {
  let len = state.uncommitted.length
  if (!len) {
    return
  }
  while (len--) {
    const item = state.uncommitted[len]
    if (item.id === id) {
      state.uncommitted.splice(len, 1)
    }
  }
}
const save = callback => {
  const param = {
    permissions: state.uncommitted,
    id: selectedTarget.value
  }
  let method = menuPerSaveApi
  if (activeAuth.value !== 'menu') {
    method = busiPerSaveApi
    param['type'] = activeName.value === 'user' ? 0 : 1
    param['flag'] = selectedResourceType.value
  }
  method(param).then(() => {
    ElMessage.success(t('common.save_success'))
    loadPermission(param['type'] || 0)
    callback && callback instanceof Function && callback()
  })
}

const reset = () => {
  state.uncommitted = []
  resetTableData(state.tableData)
  fillTableData(state.tableData, state.sourceData)
}

const uncommittedTips = callback => {
  if (!state.uncommitted.length) {
    callback && callback()
    return true
  }
  ElMessageBox.confirm(t('auth.uncommitted_tips'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      save(callback)
    })
    .catch(() => {
      reset()
      callback && callback()
    })
  return false
}

const beforeActiveNameChange = (newName, oldName) => {
  if (newName !== oldName) {
    return uncommittedTips(() => {
      activeName.value = newName
    })
  }
  return true
}
const beforeActiveAuthChange = (newName, oldName) => {
  if (newName !== oldName) {
    return uncommittedTips(() => {
      activeAuth.value = newName
    })
  }
  return true
}

const resetTableData = rows => {
  const keys: string[] = ['id', 'name', 'children']
  rows.forEach(item => {
    for (const key in item) {
      if (Object.prototype.hasOwnProperty.call(item, key) && !keys.includes(key)) {
        delete item[key]
      }
    }
    if (item.children?.length) {
      resetTableData(item.children)
    }
  })
}
onMounted(() => {
  loadUser()
  loadRole()
  loadResourceTree()
})

defineExpose({
  uncommittedTips
})
</script>

<template>
  <div class="user-role">
    <div class="filter-user-role">
      <el-tabs
        class="tabs-mr"
        v-model="activeName"
        @tab-change="activeNameChange"
        :before-leave="beforeActiveNameChange"
      >
        <el-tab-pane :label="t('auth.user')" name="user"></el-tab-pane>
        <el-tab-pane :label="t('auth.role')" name="role"></el-tab-pane>
      </el-tabs>
      <el-input class="filter-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <el-scrollbar class="role-tree-container" v-if="activeName === 'user'">
      <div
        :key="ele.id"
        v-for="ele in state.userList"
        class="list-item_primary user-role-container"
        :class="{ 'is-active': selectedTarget === ele.id }"
        @click="targetClick(ele.id)"
      >
        {{ ele.name }}
      </div>
    </el-scrollbar>
    <el-scrollbar class="role-tree-container" v-else>
      <el-tree
        class="user-role-container"
        menu
        :data="state.roleList"
        :props="defaultProps"
        @node-click="roleNodeClick"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node" :class="{ 'is-disabled': node.disabled || data.root }">
            <span :title="data.name">{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
  <div class="resource-panel">
    <div class="tab-search">
      <el-tabs
        class="tabs-mr"
        v-model="activeAuth"
        @tab-change="authActiveChange"
        :before-leave="beforeActiveAuthChange"
      >
        <el-tab-pane label="资源权限" name="resource"></el-tab-pane>
        <el-tab-pane v-if="activeName === 'role'" label="菜单和操作权限" name="menu"></el-tab-pane>
      </el-tabs>
      <el-input class="search-table-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
      <div class="search-table-bt">
        <el-button :disabled="!state.uncommitted.length" @click="save" type="primary">{{
          t('common.sure')
        }}</el-button>
        <el-button :disabled="!state.uncommitted.length" @click="reset">{{
          t('common.cancel')
        }}</el-button>
      </div>
    </div>
    <div class="resource-table">
      <div class="resource-type" v-if="activeAuth === 'resource'">
        <div
          :key="ele.name"
          v-for="ele in resourceTypeList"
          class="list-item_primary user-role-container"
          :class="{ 'is-active': selectedResourceType === ele.id }"
          @click="resourceTypeClick(ele.id)"
        >
          {{ ele.name }}
        </div>
      </div>
      <div class="tree-table" :class="activeAuth === 'menu' ? 'full-tree-table' : ''">
        <el-empty v-if="emptyDescription" :description="emptyDescription" />
        <el-table
          v-else
          ref="authTable"
          :data="state.tableData"
          style="width: 100%"
          row-key="id"
          height="100%"
          header-cell-class-name="header-cell"
          :expand-row-keys="state.expandedKeys"
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" show-overflow-tooltip label="资源名称" />
          <el-table-column
            v-for="item in state.tableColumn"
            :key="item.label"
            align="center"
            prop="name"
            width="70"
            :label="item.label"
          >
            <template #default="scope">
              <el-popover
                v-if="
                  scope.row['level' + item.weightLevel] &&
                  scope.row['level' + item.weightLevel]['show'] &&
                  !scope.row['value' + item.weightLevel]
                "
                placement="top-start"
                title=""
                :width="200"
                trigger="hover"
              >
                <template #reference>
                  <el-checkbox
                    class="user-role-per-checked"
                    disabled
                    v-model="roleChecked"
                  ></el-checkbox>
                </template>
                <div class="role-auth-tips">
                  <span>继承自以下角色：</span>
                  <span
                    :key="rname"
                    v-for="(rname, index) in scope.row['level' + item.weightLevel]['roles']"
                    >{{ index + 1 + '、' + rname }}</span
                  >
                  <span
                    >单独授权<el-switch
                      class="independent-auth"
                      size="small"
                      v-model="scope.row['independent' + item.weightLevel]"
                      @change="independentAuth(scope.row, item.weightLevel)"
                  /></span>
                </div>
              </el-popover>
              <el-checkbox
                v-show="
                  !(
                    scope.row['level' + item.weightLevel] &&
                    scope.row['level' + item.weightLevel]['show'] &&
                    !scope.row['value' + item.weightLevel]
                  )
                "
                v-model="scope.row['value' + item.weightLevel]"
                @change="rowWeightChanged(scope.row, item.weightLevel)"
              ></el-checkbox>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
@width: 30px;
@width_table: 30px;
.user-role {
  width: 250px;
  float: left;
  height: 100%;
  overflow-y: auto;
  padding-bottom: 24px;

  .filter-user-role {
    position: sticky;
    top: 0;
    left: 0;
    background: #fff;

    .filter-input {
      margin: 8px 24px 16px;
      width: 200px;
    }
    .tabs-mr {
      margin-left: @width;
      ::before {
        content: '';
        position: absolute;
        left: -@width;
        bottom: 0;
        width: @width;
        height: 1px;
        background-color: rgba(31, 35, 41, 0.15);
      }
    }
  }
  .role-tree-container {
    height: calc(100% - 101px);
  }
}
.resource-panel {
  width: calc(100% - 250px);
  float: right;
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  height: 100%;
  overflow-y: auto;

  .tab-search {
    height: 50px;
    position: relative;

    .search-table-input {
      position: absolute;
      right: 24px;
      top: 7px;
      width: 240px;
    }
    .search-table-bt {
      position: absolute;
      right: 280px;
      top: 7px;
      width: 190px;
    }
    .tabs-mr {
      margin-left: @width_table;
      ::before {
        content: '';
        position: absolute;
        left: -@width_table;
        bottom: 0;
        width: @width_table;
        height: 1px;
        background-color: rgba(31, 35, 41, 0.15);
      }
    }
  }

  .resource-table {
    width: 100%;
    height: calc(100% - 50px);
    .resource-type {
      float: left;
      width: 140px;
      height: 100%;
      padding-top: 12px;
    }

    .tree-table {
      width: calc(100% - 140px);
      float: right;
      height: 100%;
      border-left: 1px solid rgba(31, 35, 41, 0.15);
      padding: 24px;
    }
    .full-tree-table {
      width: calc(100%) !important;
    }
  }
}
.is-active {
  color: var(--el-menu-active-color);
}
.is-disabled {
  opacity: 0.25;
  cursor: not-allowed;
  background: none !important;
}
.user-role-container {
  margin: 0 24px;
}
.role-auth-tips {
  span {
    display: block;
  }
}
.independent-auth {
  margin-left: 5px;
}
.user-role-per-checked {
  margin-right: 0;
}

.custom-tree-node {
  display: flex;
  span {
    width: 120px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
</style>
