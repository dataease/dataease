<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { Icon } from '@/components/icon-custom'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
interface Tree {
  label: string
  children?: Tree[]
}
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const { t } = useI18n()
const handleClick = tab => {
  console.log('tab', tab)
}
const data: Tree[] = [
  {
    label: 'Level one 1',
    children: [
      {
        label: 'Level two 1-1',
        children: [
          {
            label: 'Level three 1-1-1'
          }
        ]
      }
    ]
  },
  {
    label: 'Level one 2',
    children: [
      {
        label: 'Level two 2-1',
        children: [
          {
            label: 'Level three 2-1-1'
          }
        ]
      },
      {
        label: 'Level two 2-2',
        children: [
          {
            label: 'Level three 2-2-1'
          }
        ]
      }
    ]
  },
  {
    label: 'Level one 3',
    children: [
      {
        label: 'Level two 3-1',
        children: [
          {
            label: 'Level three 3-1-1'
          }
        ]
      },
      {
        label: 'Level two 3-2',
        children: [
          {
            label: 'Level three 3-2-1'
          },
          {
            label: 'Level three 3-2-2'
          },
          {
            label: 'Level three 3-2-3'
          },
          {
            label: 'Level three 3-2-4'
          },
          {
            label: 'Level three 3-2-5'
          }
        ]
      }
    ]
  }
]

const resourceList = [
  {
    name: '仪表板'
  },
  {
    name: '数据大屏'
  },
  {
    name: '数据集'
  },
  {
    name: '数据源'
  }
]
const state = reactive({
  userList: [],
  uncommitted: []
})
state.userList = Array(40)
  .fill(1)
  .map((_, index) => ({
    username: 'test' + index,
    nickName: index + 'nickName'
  }))

interface User {
  id: number
  date: string
  name: string
  address: boolean
  hasChildren?: boolean
  children?: User[]
}

const handleNodeClick = (data: Tree) => {
  console.log(data)
}
const load = (row: User, treeNode: unknown, resolve: (date: User[]) => void) => {
  setTimeout(() => {
    resolve([
      {
        id: +new Date() + 1,
        date: '2016-05-01',
        name: 'wangxiaohu',
        address: true
      },
      {
        id: +new Date(),
        date: '2016-05-01',
        hasChildren: true,
        name: 'wangxiaohu',
        address: true
      }
    ])
  }, 1000)
}
const defaultProps = {
  children: 'children',
  label: 'label'
}
const tableData1: User[] = [
  {
    id: 1,
    date: '2016-05-02',
    name: 'wangxiaohu',
    address: true
  },
  {
    id: 2,
    date: '2016-05-04',
    name: 'wangxiaohu',
    address: true
  },
  {
    id: 3,
    date: '2016-05-01',
    name: 'wangxiaohu',
    hasChildren: true,
    address: true
  },
  {
    id: 4,
    date: '2016-05-03',
    name: 'wangxiaohu',
    address: true
  }
]

const save = callback => {
  console.log('save')
  callback && callback()
}

const reset = () => {
  state.uncommitted = []
  console.log('save')
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

defineExpose({
  uncommittedTips
})
</script>

<template>
  <div class="user-role">
    <div class="filter-user-role">
      <el-tabs class="tabs-mr" v-model="activeAuth">
        <el-tab-pane label="资源权限" name="resource"></el-tab-pane>
        <el-tab-pane label="菜单和操作权限" name="menu"></el-tab-pane>
      </el-tabs>
      <el-input class="filter-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <div v-if="activeAuth === 'menu'" class="menu-tree">
      <el-tree menu :data="data" :props="defaultProps" @node-click="handleNodeClick" />
    </div>

    <template v-else>
      <div
        style="margin: 0 24px"
        :key="ele.name"
        v-for="ele in resourceList"
        class="list-item_primary"
      >
        {{ ele.name }}
      </div>
    </template>
  </div>
  <div v-if="activeAuth === 'resource'" class="resource-list">
    仪表板列表
    <el-tree menu :data="data" :props="defaultProps" @node-click="handleNodeClick" />
  </div>
  <div class="resource-panel" :class="[{ 'menu-current': activeAuth === 'menu' }]">
    <div class="tab-search">
      <el-tabs class="tabs-mr" v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="用户" name="user"></el-tab-pane>
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

    <div class="tree-table">
      <el-table
        :data="tableData1"
        style="width: 100%"
        row-key="id"
        header-cell-class-name="header-cell"
        lazy
        :load="load"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="date" label="所有仪表板" />
        <el-table-column align="center" prop="name" label="导出">
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
        </el-table-column>
      </el-table>
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
  overflow-y: auto;
  padding-bottom: 24px;

  .menu-tree {
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
      .border-bottom-tab(30px);
    }
  }

  .tree-table {
    width: 100%;
    float: right;
    height: 100%;
    padding: 24px;
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
}
</style>
