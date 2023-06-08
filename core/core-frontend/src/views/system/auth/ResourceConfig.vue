<script lang="ts" setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Icon } from '@/components/icon-custom'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  queryUserApi,
  queryRoleApi,
  resourceTreeApi,
  menuTreeApi,
  resourceTargetPerApi,
  menuTargetPerApi,
  busiTargetPerSaveApi,
  menuTargetPerSaveApi
} from '@/api/auth'
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
const { t } = useI18n()
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const roleChecked = ref(true)
const selectedResourceType = ref('panel')
const selectedResourceId = ref('')
const selectedMenuId = ref('')
const resourceTreeRef = ref(null)
const menuTreeRef = ref(null)
const loading = ref(false)
const resourceList = [
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
  tableData: [],
  tableColumn: [],
  globalColumn: [],
  treeMap: {},
  uncommitted: [],
  sourceData: {},
  resourceTreeData: [],
  resourceBaseMap: {}
})
state.globalColumn = [
  { type: 'datasource, dataset, menu', label: t('auth.use'), weightLevel: 1 },
  { type: 'panel, screen', label: t('auth.check'), weightLevel: 1 },
  { type: 'panel, screen', label: t('auth.export'), weightLevel: 4 },
  { type: 'datasource, dataset, panel, screen', label: t('auth.manage'), weightLevel: 7 },
  { label: t('auth.auth'), weightLevel: 9 }
]

const baseRoles = [
  {
    id: 'admin',
    name: t('role.org_admin'),
    children: null,
    readonly: false,
    disabled: true
  },
  {
    id: 'readonly',
    name: t('role.average_role'),
    children: null,
    readonly: true,
    disabled: true
  }
]

const activeNameChange = async tabName => {
  if (tabName === 'role' && !state.treeMap[tabName]) {
    const param = { keyword: nickName.value }
    const res = await queryRoleApi(param)
    const roles = res.data || []
    const map = groupBy(roles)
    const root = JSON.parse(JSON.stringify(baseRoles))
    root[0].children = map.get(false)
    root[1].children = map.get(true)
    state.treeMap[tabName] = root
  }
  state.tableData = state.treeMap[tabName]
  const type = tabName === 'user' ? 0 : 1
  if (
    (selectedMenuId.value && activeAuth.value === 'menu') ||
    (selectedResourceId.value && activeAuth.value === 'resource')
  ) {
    loadPermission(type)
  }
}

const activeAuthChange = tabName => {
  if (tabName === 'menu') {
    const id = 'menu'
    if (state.resourceBaseMap[id]) {
      getColumn(id)
      state.resourceTreeData = state.resourceBaseMap[id]
    } else {
      menuTreeApi().then(res => {
        getColumn(id)
        state.resourceTreeData = res.data
        state.resourceBaseMap[id] = res.data
      })
    }
    activeName.value = 'role'
  }

  if (tabName === 'resource') {
    const id = selectedResourceType.value
    getColumn(id)
    state.resourceTreeData = state.resourceBaseMap[id]
    activeName.value = 'user'
  }
}
const menuIdChange = data => {
  const change = (id: string) => {
    if (id === selectedMenuId.value) {
      return
    }
    selectedMenuId.value = id
    const type = activeName.value === 'user' ? 0 : 1
    loadPermission(type)
  }

  if (uncommittedTips(() => change(data.id))) {
    change(data.id)
  }
}
const resourceIdChange = data => {
  const change = (id: string) => {
    if (id === selectedResourceId.value) {
      return
    }
    selectedResourceId.value = id
    const type = activeName.value === 'user' ? 0 : 1
    loadPermission(type)
  }

  if (uncommittedTips(() => change(data.id))) {
    change(data.id)
  }
}
const resourceTypeClick = async (id: string) => {
  const change = async (id: string) => {
    if (selectedResourceType.value === id) {
      return
    }
    selectedResourceType.value = id
    if (state.resourceBaseMap[id]) {
      state.resourceTreeData = state.resourceBaseMap[id]
    } else {
      const res = await resourceTreeApi(id)
      state.resourceTreeData = res.data
      state.resourceBaseMap[id] = res.data
    }
    getColumn(id)
  }
  if (uncommittedTips(() => change(id))) {
    change(id)
  }
}

const loadPermission = (type: number) => {
  resetTableData(state.tableData)
  loading.value = true
  if (activeAuth.value === 'menu') {
    menuTargetPerApi({ id: selectedMenuId.value }).then(res => {
      const vo = res.data

      const permissionMap = groupPermission(vo)

      fillTableData(state.tableData, permissionMap, null)
      loading.value = false
    })
    return
  }
  const param: PermissionRequest = {
    id: selectedResourceId.value,
    flag: selectedResourceType.value.toUpperCase(),
    type
  }
  resourceTargetPerApi(param).then(res => {
    const vo = res.data

    const permissionMap = groupPermission(vo)
    fillTableData(state.tableData, permissionMap, null)
    loading.value = false
  })
}
const groupPermission = vo => {
  const map = new Map()
  const origins = vo.permissionOrigins
  const permissions = vo.permissions
  const cols = state.tableColumn

  const buildPermissionMap = (type, list, rname) => {
    list?.length &&
      list.forEach(item => {
        const { id } = item
        const originLevelobj = buildCallback(type, item, rname)
        const obj = Object.assign({ id }, originLevelobj)
        map.set(id, obj)
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
  return map
}

const fillTableData = (rows, maps, pmap) => {
  rows?.forEach(row => {
    const temp = (maps?.get && maps.get(row.id)) || {}
    state.tableColumn?.forEach(col => {
      const weight = temp['weight'] || 0
      const weightLevel = col.weightLevel
      temp['value' + weightLevel] = false

      if (weight >= weightLevel) {
        temp['value' + weightLevel] = true
        if (pmap) {
          pmap[weightLevel] = pmap[weightLevel] || 0
          pmap[weightLevel]++
        }
      }
    })
    Object.assign(row, temp)
    if (row.children?.length) {
      const parentMap = {}
      fillTableData(row.children, maps, parentMap)

      const len = row.children.length
      for (const key in parentMap) {
        if (Object.prototype.hasOwnProperty.call(parentMap, key)) {
          const element = parentMap[key]
          if (element && element === len) {
            row['value' + key] = true
          }
        }
      }
    }
  })
}
const resetTableData = rows => {
  const keys: string[] = ['id', 'name', 'children', 'readonly']
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

const save = callback => {
  const param = {
    permissions: state.uncommitted
  }
  let method = menuTargetPerSaveApi
  let treeRef = menuTreeRef.value
  if (activeAuth.value !== 'menu') {
    method = busiTargetPerSaveApi
    param['type'] = activeName.value === 'user' ? 0 : 1
    param['flag'] = selectedResourceType.value
    treeRef = resourceTreeRef.value
  }
  const ids = getChildrenIds(treeRef)
  if (!ids?.size) {
    ElMessage.error('未获取到资源节点')
    return
  }
  param['ids'] = Array.from(ids)
  loading.value = true
  method(param).then(() => {
    ElMessage.success(t('common.save_success'))
    loadPermission(param['type'] || 0)
    callback && callback instanceof Function && callback()
    loading.value = false
  })
}

const getChildrenIds = treeRef => {
  const node = treeRef.getCurrentNode()
  if (!node) {
    return null
  }
  const stack = [node]
  const ids = new Set()
  while (stack.length) {
    const item = stack.pop()
    ids.add(item.id)
    if (item.children?.length) {
      item.children.forEach(kid => stack.push(kid))
    }
  }
  return ids
}

const reset = () => {
  state.uncommitted = []
  resetTableData(state.tableData)
  fillTableData(state.tableData, state.sourceData, null)
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

  if (row.children?.length) {
    row.children.forEach(item => {
      item['value' + level] = check
      rowWeightChanged(item, level)
    })
  }
}
const add2Uncommitted = (id: string, weight: number) => {
  const baseIdList = ['admin', 'readonly']
  if (baseIdList.includes(id)) {
    return
  }
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
  const baseIdList = ['admin', 'readonly']
  if (baseIdList.includes(id)) {
    return
  }
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
const getColumn = (type: string) => {
  const array = state.globalColumn.filter(item => !item.type || item.type.includes(type))
  state.tableColumn = array
}
const loadResourceTree = () => {
  loading.value = true
  const id = selectedResourceType.value
  resourceTreeApi(id).then(res => {
    getColumn(id)
    state.resourceTreeData = res.data
    state.resourceBaseMap[id] = res.data
    loading.value = false
  })
}
const loadUser = () => {
  loading.value = true
  const param = { keyword: nickName.value }
  queryUserApi(param).then(res => {
    if (res?.data?.length) {
      state.tableData = res.data
      state.treeMap['user'] = res.data
      loading.value = false
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

onMounted(() => {
  loadResourceTree()
  loadUser()
  // loadRole()
})
defineExpose({
  uncommittedTips
})
</script>

<template>
  <div class="user-role" v-loading="loading">
    <div class="filter-user-role">
      <el-tabs class="tabs-mr" v-model="activeAuth" @tab-change="activeAuthChange">
        <el-tab-pane :label="t('auth.resource')" name="resource"></el-tab-pane>
        <el-tab-pane :label="t('auth.menu')" name="menu"></el-tab-pane>
      </el-tabs>
      <el-input v-if="activeAuth === 'menu'" class="filter-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <el-scrollbar v-if="activeAuth === 'menu'" class="menu-tree">
      <el-tree
        menu
        ref="menuTreeRef"
        :data="state.resourceTreeData"
        :props="defaultProps"
        @node-click="menuIdChange"
        :highlight-current="true"
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node" :class="{ 'is-disabled': node.disabled || data.root }">
            <span :title="data.name">{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </el-scrollbar>

    <template v-else>
      <div class="resource-type-container">
        <div
          :key="ele.name"
          v-for="ele in resourceList"
          class="list-item_primary"
          :class="{ 'is-active': selectedResourceType === ele.id }"
          @click="resourceTypeClick(ele.id)"
        >
          {{ ele.name }}
        </div>
      </div>
    </template>
  </div>
  <div v-if="activeAuth === 'resource'" class="resource-list">
    <el-input class="filter-input" v-model="nickName" clearable>
      <template #prefix>
        <el-icon>
          <Icon name="icon_search-outline_outlined"></Icon>
        </el-icon>
      </template>
    </el-input>
    <el-divider class="resource-divider" />
    <el-tree
      menu
      ref="resourceTreeRef"
      :data="state.resourceTreeData"
      :props="defaultProps"
      :expand-on-click-node="false"
      :highlight-current="true"
      @node-click="resourceIdChange"
    >
      <template #default="{ node, data }">
        <span class="custom-tree-node" :class="{ 'is-disabled': node.disabled || data.root }">
          <span :title="data.name">{{ node.label }}</span>
        </span>
      </template>
    </el-tree>
  </div>
  <div class="resource-panel" :class="[{ 'menu-current': activeAuth === 'menu' }]">
    <div class="tab-search">
      <el-tabs class="tabs-mr" v-model="activeName" @tab-change="activeNameChange">
        <el-tab-pane
          v-if="activeAuth === 'resource'"
          :label="t('auth.user')"
          name="user"
        ></el-tab-pane>
        <el-tab-pane :label="t('auth.role')" name="role"></el-tab-pane>
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
      <div class="tree-table">
        <el-table
          :data="state.tableData"
          style="width: 100%"
          height="100%"
          row-key="id"
          header-cell-class-name="header-cell"
          default-expand-all
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" :label="t('common.name')" />
          <el-table-column
            v-for="item in state.tableColumn"
            :key="item.label"
            align="center"
            prop="name"
            width="70"
            :label="item.label"
          >
            <template #default="scope">
              <div v-if="activeName === 'role' && item.weightLevel > 1 && scope.row.readonly" />
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

@width: 20px;
@width_table: 30px;
.user-role {
  width: 250px;
  float: left;
  height: 100%;
  padding-bottom: 24px;

  .menu-tree {
    height: calc(100% - 100px);
    width: 100%;
    padding: 0 24px;
  }

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
      .border-bottom-tab(20px);
    }
  }
}
.resource-panel {
  width: calc(100% - 500px);
  float: right;
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  height: 100%;
  overflow-y: auto;

  .tab-search {
    height: 50px;
    position: relative;
    // padding-top: 18px;

    .search-table-input {
      position: absolute;
      right: 24px;
      top: 7px;
      width: 240px;
    }
    .search-table-bt {
      position: absolute;
      right: 250px;
      top: 7px;
      width: 190px;
    }
    .tabs-mr {
      .border-bottom-tab(30px);
    }
  }
  .resource-table {
    width: 100%;
    height: calc(100% - 50px);
    .tree-table {
      width: 100%;
      float: right;
      height: 100%;
      padding: 24px;
      overflow-y: auto;
    }
  }
}

.menu-current {
  width: calc(100% - 250px);
}

.resource-list {
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  float: left;
  width: 250px;
  height: 100%;
  padding: 24px;
  overflow-y: auto;
  padding-top: 6px;
  .resource-divider {
    margin: 7px 0;
  }
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
.resource-type-container {
  margin-top: 10px;
  div {
    margin: 0 24px;
  }
}
.is-active {
  background-color: var(--ed-color-primary-light-9);
  // color: var(--ed-color-primary);
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
</style>
