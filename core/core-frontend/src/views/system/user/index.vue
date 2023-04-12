<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElTabs, ElTabPane } from 'element-plus-secondary'
import { columnNames } from './options'
import { Icon } from '@/components/icon-custom'
import { FilterText } from '@/components/filter-text'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import UserForm from './UserForm.vue'
// import DatasetUnion from './DatasetUnion.vue'
import RoleManage from './RoleManage.vue'
import { useI18n } from '@/hooks/web/useI18n'
import ColumnList from '@/components/column-list/src/ColumnList.vue'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { userPageApi, userDelApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
// import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
const { t } = useI18n()
const activeName = ref('user')
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

state.filterTexts = []

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

const search = () => {
  userPageApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, {}).then(res => {
    state.userList = res.data.records
    state.paginationConfig.total = res.data.total
  })
}
const filterRoles = cellValue => {
  const roleNames = cellValue.map(ele => ele?.name)
  return roleNames.length ? roleNames.join() : '-'
}
const changeSwitch = row => {
  console.log(row.id)
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value).format()
}
const edit = row => {
  userFormDialog.value.edit(row.id)
}
const unlock = row => {
  console.log(row.id)
}
const delHandler = row => {
  ElMessageBox.confirm(t('user.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    userDelApi(row.id).then(() => {
      ElMessage.success(t('common.delete_success'))
      search()
    })
  })
}
onMounted(() => {
  search()
})
const saveHandler = () => {
  search()
}
</script>
<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('system.user')" name="user"></el-tab-pane>
    <el-tab-pane :label="t('system.role')" name="role"></el-tab-pane>
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
        <el-table-column type="selection" width="30" />
        <el-table-column prop="account" key="account" label="ID" width="100" />
        <el-table-column
          key="name"
          show-overflow-tooltip
          prop="name"
          sortable="custom"
          :label="t('user.name')"
          width="150"
        />
        <el-table-column prop="roleItems" key="roleItems" :label="t('user.role')" width="200">
          <template #default="scope">
            <el-tooltip popper-class="de-table-tooltips" class="item" effect="dark" placement="top">
              <template #content>
                <div v-html="filterRoles(scope.row.roleItems)" />
              </template>

              <div class="de-one-line">{{ filterRoles(scope.row.roleItems) }}</div>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="email" key="email" :label="t('common.email')" width="200" />

        <el-table-column prop="enable" key="enable" :label="t('user.state')" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.enable"
              inactive-color="#DCDFE6"
              @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('common.create_time')" width="170">
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column key="_operation" :label="$t('common.operate')">
          <template #default="scope">
            <div class="operate-icon-container">
              <div><Icon name="edit" @click="edit(scope.row)"></Icon></div>
              <div><Icon name="unlock" @click="unlock(scope.row)"></Icon></div>
              <div><Icon name="delete" @click="delHandler(scope.row)"></Icon></div>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-else-if="activeName === 'role'" class="role-content">
    <role-manage></role-manage>
  </div>
  <!-- <div v-else class="user-table">
    <EmptyBackground></EmptyBackground>
    <dataset-union></dataset-union>
  </div> -->
  <drawer-main ref="drawerMainRef"></drawer-main>
  <user-form @saved="saveHandler" ref="userFormDialog"></user-form>
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
.operate-icon-container {
  font-size: 16px;
  display: flex;
  div {
    width: 24px;
    height: 20px;
    padding: 0 3px;
    svg {
      width: 16px;
      height: 16px;
      color: var(--el-text-color-regular);
      background-color: var(--el-color-white);
    }
  }

  div:hover {
    cursor: pointer;
    svg {
      color: var(--el-color-primary) !important;
      background: var(--el-color-primary-light-7) !important;
    }
  }
}
</style>
