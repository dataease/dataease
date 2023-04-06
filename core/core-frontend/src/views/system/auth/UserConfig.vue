<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { Icon } from '@/components/icon-custom'
const activeName = ref('user')
const activeAuth = ref('resource')
const nickName = ref('')
const handleClick = tab => {
  console.log('tab', tab)
}

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
  userList: []
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
</script>

<template>
  <div class="user-role">
    <div class="filter-user-role">
      <el-tabs class="tabs-mr" v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="用户" name="user"></el-tab-pane>
        <el-tab-pane label="角色" name="role"></el-tab-pane>
      </el-tabs>
      <el-input class="filter-input" v-model="nickName" clearable>
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>

    <div
      style="margin: 0 24px"
      :key="ele.username"
      v-for="ele in state.userList"
      class="list-item_primary"
    >
      {{ ele.username }}
    </div>
  </div>
  <div class="resource-panel">
    <div class="tab-search">
      <el-tabs class="tabs-mr" v-model="activeAuth">
        <el-tab-pane label="资源权限" name="resource"></el-tab-pane>
        <el-tab-pane label="菜单和操作权限" name="menu"></el-tab-pane>
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
      <div class="resource-type">
        <div
          style="margin: 0 24px"
          :key="ele.name"
          v-for="ele in resourceList"
          class="list-item_primary"
        >
          {{ ele.name }}
        </div>
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
  }
}
</style>
