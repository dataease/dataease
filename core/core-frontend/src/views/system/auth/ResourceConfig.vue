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
  resourcePerApi,
  menuPerApi,
  busiPerSaveApi,
  menuPerSaveApi
} from '@/api/auth'
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root: boolean
}

const { t } = useI18n()
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const roleChecked = ref(true)
const selectedResourceType = ref('panel')

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
  expandedKeys: [],
  resourceTreeData: [],
  resourceBaseMap: {}
})
state.globalColumn = [
  { type: 'datasource, dataset, menu', label: '使用', weightLevel: 1 },
  { type: 'panel, screen', label: '查看', weightLevel: 1 },
  { type: 'panel, screen', label: '导出', weightLevel: 4 },
  { type: 'datasource, dataset, panel, screen', label: '管理', weightLevel: 7 },
  { label: '授权', weightLevel: 9 }
]

const baseRoles = [
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

const activeNameChange = tabName => {
  nickName.value = ''
  console.log(tabName)
}

const activeAuthChange = tabName => {
  if (tabName === 'menu') {
    const id = 'menu'
    if (state.resourceBaseMap[id]) {
      getColumn(id)
      state.resourceTreeData = state.treeMap[id]
    } else {
      menuTreeApi().then(res => {
        getColumn('menu')
        state.resourceTreeData = res.data
        state.resourceBaseMap['menu'] = res.data
      })
    }
  }

  if (tabName === 'resource') {
    const id = selectedResourceType.value
    getColumn(id)
    state.resourceTreeData = state.resourceBaseMap[id]
  }
}
const menuIdChange = data => {
  console.log(data)
}
const resourceIdChange = data => {
  console.log(data)
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
    // 如果有selectedTarget 再查权限
    /* if (selectedTarget.value) {
      const type = activeName.value === 'user' ? 0 : 1
      loadPermission(type)
    } */
  }
  if (uncommittedTips(() => change(id))) {
    change(id)
  }
}

const save = callback => {
  console.log('save')
  callback && callback()
}

const reset = () => {
  state.uncommitted = []
  console.log('save')
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
  const id = selectedResourceType.value
  resourceTreeApi(id).then(res => {
    getColumn(id)
    state.resourceTreeData = res.data
    state.resourceBaseMap[id] = res.data
  })
}
const loadUser = () => {
  const param = { keyword: nickName.value }
  queryUserApi(param).then(res => {
    if (res?.data?.length) {
      state.tableData = res.data
      state.treeMap['user'] = res.data
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
const loadRole = () => {
  const param = { keyword: nickName.value }
  queryRoleApi(param).then(res => {
    if (res?.data?.length) {
      const roles = res.data
      const map = groupBy(roles)
      const root = JSON.parse(JSON.stringify(baseRoles))
      root[0].children = map.get(false)
      root[1].children = map.get(true)
      state.tableData = root
      state.treeMap['role'] = root
    }
  })
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
  <div class="user-role">
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
      <el-tree menu :data="state.resourceTreeData" :props="defaultProps" @node-click="menuIdChange">
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
      :data="state.resourceTreeData"
      :props="defaultProps"
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
        <el-tab-pane v-if="activeAuth === 'resource'" label="用户" name="user"></el-tab-pane>
        <el-tab-pane label="角色" name="role"></el-tab-pane>
      </el-tabs>
      <el-input class="search-table-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <div class="resource-table">
      <div class="tree-table">
        <el-table
          :data="state.tableData"
          style="width: 100%"
          row-key="id"
          header-cell-class-name="header-cell"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        >
          <el-table-column prop="name" label="名称" />
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
  color: var(--el-menu-active-color);
}
</style>
