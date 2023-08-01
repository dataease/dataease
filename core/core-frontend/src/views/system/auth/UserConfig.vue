<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { setColorName } from '@/utils/utils'
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
const userKeyword = ref('')
const resourceKeyword = ref('')
const selectedTarget = ref('')
const selectedResourceType = ref('panel')
const emptyDescription = ref('')
const authTable = ref(null)
const roleChecked = ref(true)
const selectedRoleRootReadonly = ref(false)
const loading = ref(false)
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

const authActiveChange = async tabName => {
  if (tabName === 'menu') {
    const id = 'menu'
    if (state.treeMap[id]) {
      getColumn(id)
      state.tableData = state.treeMap[id]
    } else {
      const res = await menuTreeApi()
      getColumn('menu')
      state.tableData = res.data
      state.treeMap['menu'] = res.data
    }
    selectedTarget.value && loadPermission(1)
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
  filterTableData: [],
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
    name: t('role.org_admin'),
    children: null,
    disabled: true
  },
  {
    id: 'readonly',
    name: t('role.average_role'),
    children: null,
    disabled: true
  }
]
state.globalColumn = [
  { type: 'datasource, dataset, menu', label: t('auth.use'), weightLevel: 1 },
  { type: 'panel, screen', label: t('auth.check'), weightLevel: 1 },
  { type: 'panel, screen', label: t('auth.export'), weightLevel: 4 },
  { type: 'datasource, dataset, panel, screen', label: t('auth.manage'), weightLevel: 7 },
  { label: t('auth.auth'), weightLevel: 9 }
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
    selectedRoleRootReadonly.value = data.readonly
    selectedTarget.value = data.id
    getColumn(activeAuth.value === 'resource' ? selectedResourceType.value : 'menu')
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
    selectedRoleRootReadonly.value = false
    getColumn(activeAuth.value === 'resource' ? selectedResourceType.value : 'menu')
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
  let array = state.globalColumn.filter(item => !item.type || item.type.includes(type))
  if (selectedRoleRootReadonly.value && array?.length) {
    array = array.filter(item => selectedRoleRootReadonly.value >= item.weightLevel)
  }
  state.tableColumn = array
}

const loadResourceTree = () => {
  loading.value = true
  const id = selectedResourceType.value
  resourceTreeApi(id).then(res => {
    getColumn(id)
    state.tableData = res.data
    state.treeMap[id] = res.data
    loading.value = false
  })
}

const loadUser = () => {
  const param = {}
  loading.value = true
  queryUserApi(param).then(res => {
    if (res?.data?.length) {
      state.userList = res.data
    } else {
      state.userList = []
    }
    loading.value = false
  })
}

const loadRole = () => {
  const param = {}
  loading.value = true
  queryRoleApi(param).then(res => {
    if (res?.data?.length) {
      const roles = res.data
      const map = groupBy(roles)
      state.roleList[0].children = map.get(false)
      state.roleList[1].children = map.get(true)
    }
    loading.value = false
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
  loading.value = true
  emptyDescription.value = ''
  resetTableData(state.tableData)
  state.expandedKeys = []
  if (activeAuth.value === 'menu') {
    menuPerApi({ id: selectedTarget.value }).then(res => {
      const vo = res.data
      loading.value = false
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
    loading.value = false
    if (isOrgAdminPer(vo, type)) {
      return
    }
    const permissionMap = groupPermission(vo)
    fillTableData(state.tableData, permissionMap)
  })
}

const isOrgAdminPer = (vo, type) => {
  if (vo?.root && !vo.readonly) {
    emptyDescription.value = type ? t('auth.org_role_empty') : t('auth.user_role_empty')
    return true
  }
  fillOrgReadonly(vo)
  return false
}

const fillOrgReadonly = vo => {
  const result = vo?.root && vo.readonly
  if (result) {
    const id = activeAuth.value === 'menu' ? 'menu' : selectedResourceType.value
    const data = state.treeMap[id]
    if (data) {
      const origin = { name: t('role.average_role'), permissions: [] }
      const stack = [...data]
      while (stack.length > 0) {
        const node = stack.pop()
        origin.permissions.push({ id: node.id, weight: 1 })
        if (node.children?.length) {
          node.children.forEach(item => stack.push(item))
        }
      }
      vo?.permissionOrigins?.push(origin)
    }
  }
  return result
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
        temp['show'] = temp['show'] || (userWeight < weightLevel && roleMatch)
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
    row['weight'] = Math.max(row?.weight || 0, level)
  } else {
    let finalWeight = 0
    let index = state.tableColumn.findIndex(col => level === col.weightLevel)
    if (index--) {
      finalWeight = state.tableColumn[index]['weightLevel']
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

  if (row.children?.length && row.id !== '0') {
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
  loading.value = true
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
    loading.value = false
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
  rows?.length &&
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
const roleTree = ref(null)
const filterTarget = val => {
  if (activeName.value === 'role') {
    roleTree.value?.filter(val)
  } else {
    state.userList.forEach(item => setColorName(item, val))
    userKeyword.value = val ? val.toLocaleLowerCase() : val
  }
}
const filterRoleNode = (value: string, data) => {
  setColorName(data, value)
  if (!value) return true
  return data.name.includes(value)
}
const dynamicResourceClass = param => {
  const row = param.row
  return row.hidden ? 'dynamic-resource-hidden' : ''
}
const matchFilter = (row, val): boolean => {
  let match = !val || row.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
  setColorName(row, val)
  if (row.children?.length) {
    for (let index = 0; index < row.children.length; index++) {
      const kid = row.children[index]
      const kidMatch = matchFilter(kid, val)
      if (kidMatch && !match) {
        match = kidMatch
      }
    }
  }
  row.hidden = !match
  return match
}
const resourceFilter = val => {
  state.tableData.forEach(item => {
    matchFilter(item, val)
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
  <div class="user-role" v-loading="loading">
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
      <el-input class="filter-input" v-model="nickName" clearable @change="filterTarget">
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
        v-for="ele in state.userList.filter(
          item => !userKeyword || item.name.toLocaleLowerCase().includes(userKeyword)
        )"
        class="list-item_primary user-role-container"
        :class="{ 'is-active': selectedTarget === ele.id }"
        @click="targetClick(ele.id)"
      >
        <span :title="ele.name" v-html="ele.colorName ? ele.colorName : ele.name" />
      </div>
    </el-scrollbar>
    <el-scrollbar class="role-tree-container" v-else>
      <el-tree
        class="user-role-container"
        menu
        ref="roleTree"
        :data="state.roleList"
        :props="defaultProps"
        :highlight-current="true"
        :expand-on-click-node="false"
        @node-click="roleNodeClick"
        :default-expand-all="true"
        :filter-node-method="filterRoleNode"
      >
        <template #default="{ node, data }">
          <span
            class="custom-tree-node"
            :class="{ 'span-is-disabled': node.disabled || data.root }"
          >
            <span :title="data.name" v-html="data.colorName ? data.colorName : node.label" />
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
        <el-tab-pane :label="t('auth.resource')" name="resource"></el-tab-pane>
        <el-tab-pane v-if="activeName === 'role'" :label="t('auth.menu')" name="menu"></el-tab-pane>
      </el-tabs>
      <!-- <el-input
        class="search-table-input"
        v-model="resourceKeyword"
        clearable
        @change="resourceFilter"
      >
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input> -->
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

        <el-input
          v-else
          class="search-table-input"
          v-model="resourceKeyword"
          clearable
          @change="resourceFilter"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>

        <el-table
          class="table-container"
          v-if="!emptyDescription"
          ref="authTable"
          :data="state.tableData"
          style="width: 100%"
          row-key="id"
          :row-class-name="dynamicResourceClass"
          header-cell-class-name="header-cell"
          :expand-row-keys="state.expandedKeys"
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" show-overflow-tooltip :label="t('auth.resource_name')">
            <template v-slot:default="scope">
              <span v-html="scope.row.colorName ? scope.row.colorName : scope.row.name" />
            </template>
          </el-table-column>
          <el-table-column
            v-for="item in state.tableColumn"
            :key="item.label"
            align="center"
            prop="name"
            width="70"
            :label="item.label"
          >
            <template #default="scope">
              <div
                v-if="activeAuth === 'resource' && item.weightLevel < 7 && scope.row.id === '0'"
              />
              <div v-else>
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
                    <span>{{ t('auth.from_role') }}</span>
                    <span
                      :key="rname"
                      v-for="(rname, index) in scope.row['level' + item.weightLevel]['roles']"
                      >{{ index + 1 + '、' + rname }}</span
                    >
                    <span
                      >{{ t('auth.auth_alone')
                      }}<el-switch
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
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

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
      .border-bottom-tab(30px);
      margin: 0 30px;
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

    .search-table-bt {
      position: absolute;
      right: 5px;
      top: 7px;
      width: 190px;
    }
    .tabs-mr {
      .border-bottom-tab(30px);
      margin: 0 30px;
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
      .search-table-input {
        margin-bottom: 16px;
      }
      .table-container {
        height: calc(100% - 48px);
      }
    }
    .full-tree-table {
      width: calc(100%) !important;
      .table-container {
        height: calc(100% - 25px);
      }
    }
  }
}
.is-active {
  background-color: var(--ed-color-primary-light-9);
  // color: var(--ed-color-primary);
}
.span-is-disabled {
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
    width: 140px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
::v-deep(.dynamic-resource-hidden) {
  display: none !important;
}
</style>
