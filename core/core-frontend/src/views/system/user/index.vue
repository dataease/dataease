<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElTabs, ElTabPane } from 'element-plus-secondary'
import { columnNames } from './options'
import { Icon } from '@/components/icon-custom'
import { FilterText } from '@/components/filter-text'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import UserForm from './UserForm.vue'
import DatasetUnion from './DatasetUnion.vue'
import RoleManage from './RoleManage.vue'
import { useI18n } from '@/hooks/web/useI18n'
import ColumnList from '@/components/column-list/src/ColumnList.vue'
import GridTable from '@/components/grid-table/src/GridTable.vue'

import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
const { t } = useI18n()
const activeName = ref('test')
const isPluginLoaded = ref(false)
const drawerMainRef = ref(null)
const nickName = ref('')
const userFormDialog = ref(null)

const handleClick = () => {
  console.log('handleClick')
}

const addUser = () => {
  userFormDialog.value.init()
}

const drawerMainOpen = () => {
  drawerMainRef.value.init()
}

const state = reactive({
  userList: [],
  columnList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})

state.userList = Array(20)
  .fill(1)
  .map((_, index) => ({
    username: 'test' + index,
    nickName: index + 'nickName'
  }))

state.paginationConfig.total = state.userList.length
state.filterTexts = ['12312312', '1231231', '123123']

const columnChange = (columns: string[]) => {
  state.columnList = columns
}

const clearFilter = (index?: number) => {
  if (isNaN(index)) {
    state.filterTexts = []
  } else {
    state.filterTexts.splice(index, 1)
  }
}
</script>
<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
    <el-tab-pane :label="t('system.user')" name="user"></el-tab-pane>
    <el-tab-pane :label="t('system.role')" name="role"></el-tab-pane>
    <el-tab-pane label="test" name="test"></el-tab-pane>
  </el-tabs>
  <div v-if="activeName === 'user'" class="user-table de-search-table">
    <el-row class="user-table__filter top-operate">
      <el-col :span="12">
        <el-button @click="addUser" type="primary">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          {{ t('system.addUser') }}
        </el-button>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-button @click="drawerMainOpen" plain>
          <template #icon>
            <Icon name="icon-filter"></Icon>
          </template>
          筛选
        </el-button>
        <column-list
          @column-change="columnChange"
          :is-plugin-loaded="isPluginLoaded"
          :column-names="columnNames"
        ></column-list>
      </el-col>
    </el-row>
    <filter-text
      @clear-filter="clearFilter"
      :total="state.paginationConfig.total"
      :filter-texts="state.filterTexts"
    ></filter-text>
    <div :class="[state.filterTexts.length ? 'is-in-filter' : 'user-table__content']">
      <GridTable
        :columns="state.columnList"
        :pagination="state.paginationConfig"
        :table-data="state.userList"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="username" key="username" label="ID" />
        <el-table-column
          key="nickName"
          show-overflow-tooltip
          prop="nickName"
          sortable="custom"
          :label="t('commons.nick_name')"
        />
        <el-table-column key="_operation" :label="$t('commons.operating')" fixed="right">
          <template>
            <el-button type="text">{{ t('commons.unlock') }}</el-button>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-else-if="activeName === 'role'" class="role-content">
    <role-manage></role-manage>
  </div>
  <div v-else class="user-table">
    <!-- <EmptyBackground></EmptyBackground> -->
    <dataset-union></dataset-union>
  </div>
  <drawer-main ref="drawerMainRef"></drawer-main>
  <user-form ref="userFormDialog"></user-form>
</template>

<style lang="less" scoped>
.user-table,
.role-content {
  height: calc(100% - 60px);
  box-sizing: border-box;
  margin-top: 12px;
  background: white;
  padding: 24px;

  .user-table__content {
    height: calc(100vh - 260px);
  }

  .is-in-filter {
    height: calc(100vh - 310px);
  }
}

.role-content {
  padding: 0;
}
</style>
