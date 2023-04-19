<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import { queryUserApi, queryRoleApi, resourceTreeApi, menuTreeApi } from '@/api/auth'
const { t } = useI18n()
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const selectedTarget = ref('')
const selectedResourceType = ref('panel')
const tableLoading = ref(false)
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root: boolean
}
const activeNameChange = tabName => {
  nickName.value = ''
  selectedTarget.value = ''
  if (activeAuth.value === 'menu' && tabName === 'user') {
    activeAuth.value = 'resource'
  }
  /* if (tabName === 'role') {
    selectedResourceType.value = ''
  } */
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
  treeMap: {}
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
  { type: 'datasource, dataset, menu', label: '使用', weight: 1 },
  { type: 'panel, screen', label: '查看', weight: 1 },
  { type: 'panel, screen', label: '导出', weight: 3 },
  { type: 'datasource, dataset, panel, screen', label: '管理', weight: 5 },
  { label: '授权', weight: 7 }
]
// 选中角色事件
const roleNodeClick = (data: Tree) => {
  if (data.disabled || data.root) {
    return
  }
  selectedTarget.value = data.id
}
// 选中用户事件
const targetClick = (id: string) => {
  selectedTarget.value = id
}

const resourceTypeClick = async (id: string) => {
  selectedResourceType.value = id
  if (state.treeMap[id]) {
    state.tableData = state.treeMap[id]
  } else {
    const res = await resourceTreeApi(id)
    state.tableData = res.data
  }
  getColumn(id)
  // 如果有selectedTarget 再查权限
}

const getColumn = (type: string) => {
  tableLoading.value = true
  const array = state.globalColumn.filter(item => !item.type || item.type.includes(type))
  state.tableColumn = array
  setTimeout(() => {
    tableLoading.value = false
  }, 500)
}

const loadResourceTree = () => {
  const id = selectedResourceType.value
  if (activeAuth.value === 'resource' && id) {
    resourceTreeApi(id).then(res => {
      getColumn(id)
      state.tableData = res.data
      state.treeMap[id] = res.data
    })
  }
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
onMounted(() => {
  loadUser()
  loadRole()
  loadResourceTree()
})
</script>

<template>
  <div class="user-role">
    <div class="filter-user-role">
      <el-tabs class="tabs-mr" v-model="activeName" @tab-change="activeNameChange">
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
    <div v-if="activeName === 'user'">
      <div
        :key="ele.id"
        v-for="ele in state.userList"
        class="list-item_primary user-role-container"
        :class="{ 'is-active': selectedTarget === ele.id }"
        @click="targetClick(ele.id)"
      >
        {{ ele.name }}
      </div>
    </div>
    <div v-else>
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
    </div>
  </div>
  <div class="resource-panel">
    <div class="tab-search">
      <el-tabs class="tabs-mr" v-model="activeAuth" @tab-change="authActiveChange">
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
        <!-- <el-empty
          v-if="!state.tableData || !state.tableData.length"
          :description="t('auth.empty_desc')"
        /> -->
        <el-table
          v-if="!tableLoading"
          :data="state.tableData"
          style="width: 100%"
          row-key="id"
          header-cell-class-name="header-cell"
          :tree-props="{ children: 'children' }"
        >
          <el-table-column prop="name" label="资源名称" />
          <!-- <el-table-column width="1" v-show="false"></el-table-column> -->
          <el-table-column
            v-for="item in state.tableColumn"
            :key="item.label"
            align="center"
            prop="name"
            :label="item.label"
          >
            <template #default="scope">
              <el-checkbox v-model="scope.row.name"></el-checkbox>
            </template>
          </el-table-column>
          <!-- <el-table-column align="center" prop="name" label="导出">
            <template #default="scope">
              <el-checkbox v-model="scope.row.address"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="name" label="管理">
            <template #default="scope">
              <el-checkbox v-model="scope.row.address"></el-checkbox>
            </template>
          </el-table-column>
          <el-table-column align="center" prop="address" label="授权">
            <template #default="scope">
              <el-checkbox v-model="scope.row.address"></el-checkbox>
            </template>
          </el-table-column> -->
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
}
.resource-panel {
  width: calc(100% - 250px);
  float: right;
  border-left: 1px solid rgba(31, 35, 41, 0.15);
  height: 100%;
  overflow-y: auto;

  .tab-search {
    height: 65px;
    position: relative;
    padding-top: 18px;

    .search-table-input {
      position: absolute;
      right: 24px;
      top: 16px;
      width: 240px;
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
    height: calc(100% - 65px);
    .resource-type {
      float: left;
      width: 140px;
      height: 100%;
      padding-top: 24px;
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
</style>
