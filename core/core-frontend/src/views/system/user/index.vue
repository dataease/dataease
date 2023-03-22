<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { columnNames } from './options'
import {
  ElTableColumn,
  ElTabs,
  ElTabPane,
  ElButton,
  ElCol,
  ElRow,
  ElInput
} from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import ColumnList from '@/components/column-list/src/column-list.vue'
import GridTable from '@/components/grid-table/src/grid-table.vue'
const { t } = useI18n()
const activeName = ref('user')
const isPluginLoaded = ref(false)
const nickName = ref('')
const handleClick = () => {
  console.log('handleClick')
}

const state = reactive({
  userList: [],
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
</script>
<template>
  <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
    <el-tab-pane :label="t('system.user')" name="user"></el-tab-pane>
    <el-tab-pane :label="t('system.role')" name="role"></el-tab-pane>
  </el-tabs>
  <div class="user-table de-search-table">
    <el-row class="user-table__filter top-operate">
      <el-col :span="12">
        <el-button type="primary">
          <el-icon class="el-icon--right"></el-icon>
          {{ t('system.addUser') }}
        </el-button>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input v-model="nickName" clearable />
        <el-button plain>
          {{ t('system.addUser') }}
        </el-button>
        <column-list :is-plugin-loaded="isPluginLoaded" :column-names="columnNames"></column-list>
      </el-col>
    </el-row>
    <div class="user-table__content">
      <GridTable :columns="[]" :pagination="state.paginationConfig" :table-data="state.userList">
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
</template>

<style lang="less" scoped>
.user-table {
  height: calc(100% - 61px);
  box-sizing: border-box;

  .user-table__content {
    height: 500px;
  }
}
</style>
