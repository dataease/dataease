<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElTabs, ElTabPane } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { FilterText, convertFilterText } from '@/components/filter-text'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import UserForm from './UserForm.vue'
import RoleManage from './RoleManage.vue'
import { useI18n } from '@/hooks/web/useI18n'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import {
  userPageApi,
  userDelApi,
  batchDelApi,
  defaultPwdApi,
  resetPwdApi,
  switchEnableApi,
  searchRoleApi
} from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { setColorName } from '@/utils/utils'
import UserImport from './UserImport/index.vue'
import useClipboard from 'vue-clipboard3'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutHandler } from '@/utils/logout'
import { filterOption, groupBy } from './options'

const userStore = useUserStoreWithOut()
const curUid = computed(() => userStore.getUid)
const { toClipboard } = useClipboard()
const { t } = useI18n()
const activeName = ref('user')
const drawerMainRef = ref(null)
const userFormDialog = ref(null)
const loading = ref(false)
const multipleTableRef = ref(null)
const defaultPwd = ref(null)
const handleClick = () => {
  console.log('handleClick')
}

const addUser = () => {
  userFormDialog.value.init()
}

const roleLoad = ref(false)
const drawerMainOpen = async () => {
  if (!roleLoad.value) {
    const res = await searchRoleApi('')
    const map = groupBy(res.data)
    filterOption[1].option[0]['children'] = map.get(false)
    filterOption[1].option[1]['children'] = map.get(true)
    roleLoad.value = true
  }
  drawerMainRef.value.init()
}
const drawerMainClose = () => {
  drawerMainRef.value.close()
}

const state = reactive({
  userList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  conditions: [],
  orders: [],
  multipleSelection: []
})
const keyword = ref(null)
state.filterTexts = []

const clearFilter = (index?: number) => {
  if (isNaN(index)) {
    state.filterTexts = []
  } else {
    state.filterTexts.splice(index, 1)
  }
  drawerMainRef.value.clearFilter(index)
}

const search = () => {
  loading.value = true
  userPageApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, {
    orders: state.orders,
    conditions: state.conditions,
    keyword: keyword.value
  }).then(res => {
    const records = res.data.records
    records.forEach(item => {
      setColorName(item, keyword.value)
      setColorName(item, keyword.value, 'account', 'colorAccount')
      setColorName(item, keyword.value, 'email', 'colorEmail')
    })
    state.userList = records
    state.paginationConfig.total = res.data.total
    loading.value = false
  })
}
const filterRoles = cellValue => {
  const roleNames = cellValue.map(ele => ele?.name)
  return roleNames.length ? roleNames.join() : '-'
}
const changeSwitch = row => {
  const param = { id: row.id, enable: row.enable }
  loading.value = true
  switchEnableApi(param).then(() => {
    ElMessage.success(t('user.switch_success'))
    loading.value = false
  })
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value)['format']()
}
const edit = row => {
  userFormDialog.value.edit(row.id)
}

const delHandler = row => {
  ElMessageBox.confirm(t('user.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      loading.value = true
      userDelApi(row.id).then(() => {
        ElMessage.success(t('common.delete_success'))
        search()
      })
    })
    .catch(() => {
      console.log('cancel del')
    })
}
const refreshRole = () => {
  userFormDialog.value.refreshRole()
}

const refreshGrid = () => {
  search()
}
onMounted(() => {
  search()
})
const saveHandler = () => {
  search()
}
const searchCondition = conditions => {
  state.conditions = conditions
  search()
  fillFilterText()
  drawerMainClose()
}
const fillFilterText = () => {
  const textArray = state.conditions?.length
    ? convertFilterText(state.conditions, filterOption)
    : []
  Object.assign(state.filterTexts, textArray)
}
const pageChange = index => {
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  search()
}
const sortChange = param => {
  state.orders = []
  if (param.order && param.prop === 'createTime') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    state.orders.push('create_time ' + type)
    search()
  }
}

const handleSelectionChange = rows => {
  state.multipleSelection = rows
}
const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
}

const batchDelHandler = () => {
  ElMessageBox.confirm(t('user.confirm_batch_delete', [state.multipleSelection.length]), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      batchDel()
    })
    .catch(() => {
      clearSelection()
    })
}
const batchDel = () => {
  const ids = state.multipleSelection.map(item => item.id)
  loading.value = true
  batchDelApi(ids).then(() => {
    loading.value = false
    ElMessage.success(t('common.delete_success'))
    search()
  })
}

const loadRestInfo = async () => {
  if (defaultPwd.value) {
    return
  }
  const res = await defaultPwdApi()
  defaultPwd.value = res.data
}
const resetPwd = row => {
  resetPwdApi(row.id).then(() => {
    ElMessage.success(t('user.reset_success'))
    closeResetInfo(row)
    if (row.id === curUid.value) {
      logoutHandler()
    }
  })
}
const closeResetInfo = row => {
  row.resetInfoShow = false
}
const copyPwd = async () => {
  try {
    await toClipboard(defaultPwd.value)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}
</script>
<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('system.user')" name="user"></el-tab-pane>
    <el-tab-pane :label="t('system.role')" name="role"></el-tab-pane>
  </el-tabs>
  <div v-if="activeName === 'user'" v-loading="loading" class="user-table de-search-table">
    <el-row class="user-table__filter top-operate">
      <el-col :span="12">
        <el-button @click="addUser" type="primary">
          {{ t('system.addUser') }}
        </el-button>

        <user-import @refresh-grid="refreshGrid" />
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('user.search_placeholder')"
          @change="search"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-button
          @click="drawerMainOpen"
          :plain="!!state.conditions.length"
          :class="state.conditions.length ? 'filter-condition-button' : 'filter-button'"
        >
          <template #icon>
            <Icon name="icon-filter"></Icon>
          </template>
          {{
            t('common.filter') + (state.conditions.length ? `(${state.conditions?.length})` : '')
          }}
        </el-button>
      </el-col>
    </el-row>
    <filter-text
      @clear-filter="clearFilter"
      :total="state.paginationConfig.total"
      :filter-texts="state.filterTexts"
    ></filter-text>
    <div :class="[state.filterTexts.length ? 'is-in-filter' : 'user-table__content']">
      <GridTable
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.userList"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="30" />
        <el-table-column
          key="name"
          show-overflow-tooltip
          prop="name"
          :label="t('user.name')"
          width="150"
        >
          <template v-slot:default="scope">
            <span v-if="scope.row.colorName" v-html="scope.row.colorName" />
            <span v-else>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="account"
          key="account"
          :label="t('user.account')"
          show-overflow-tooltip
          width="120"
        >
          <template v-slot:default="scope">
            <span v-html="scope.row.colorAccount || scope.row.account" />
          </template>
        </el-table-column>

        <el-table-column
          prop="roleItems"
          key="roleItems"
          :label="t('user.role')"
          width="200"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="de-one-line">{{ filterRoles(scope.row.roleItems) }}</div>
          </template>
        </el-table-column>
        <el-table-column
          prop="email"
          show-overflow-tooltip
          key="email"
          :label="t('common.email')"
          width="200"
        >
          <template v-slot:default="scope">
            <span v-html="scope.row.colorEmail || scope.row.email" />
          </template>
        </el-table-column>

        <el-table-column prop="enable" key="enable" :label="t('user.state')" width="80">
          <template #default="scope">
            <el-switch
              :disabled="scope.row.id === '1' || scope.row.id === curUid"
              v-model="scope.row.enable"
              inactive-color="#DCDFE6"
              @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          :label="t('common.create_time')"
          sortable="custom"
          width="170"
        >
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column key="_operation" :label="$t('common.operate')">
          <template #default="scope">
            <div class="operate-icon-container" v-if="scope.row.id !== '1'">
              <div><Icon name="icon_edit_outlined" @click="edit(scope.row)"></Icon></div>
              <div @click="scope.row.resetInfoShow = true">
                <el-popover
                  placement="right"
                  :width="300"
                  trigger="click"
                  @show="loadRestInfo"
                  :visible="scope.row.resetInfoShow"
                >
                  <template #reference>
                    <Icon name="unlock" />
                  </template>
                  <div class="reset-pwd-confirm">
                    <div class="confirm-header">
                      <span class="icon-span">
                        <el-icon>
                          <Icon name="icon_warning_filled"></Icon>
                        </el-icon>
                      </span>
                      <span class="header-span">{{ t('user.reset_confirm') }}</span>
                    </div>
                    <div class="confirm-content">
                      <span>{{ defaultPwd }}</span>
                      <el-button text @click="copyPwd">{{ t('common.copy') }}</el-button>
                    </div>
                    <div v-if="scope.row.id === curUid" class="confirm-warning">
                      <span>{{ t('user.modify_cur_pwd') }}</span>
                    </div>
                    <div class="confirm-foot">
                      <el-button @click="closeResetInfo(scope.row)">{{
                        t('common.cancel')
                      }}</el-button>
                      <el-button type="primary" @click="resetPwd(scope.row)">
                        {{ t('common.sure') }}
                      </el-button>
                    </div>
                  </div>
                </el-popover>
              </div>
              <div><Icon name="delete" @click="delHandler(scope.row)"></Icon></div>
            </div>
          </template>
        </el-table-column>
      </GridTable>
      <div v-if="state.multipleSelection.length" class="bottom-bar">
        <el-button
          size="small"
          type="danger"
          class="batch-delete-button"
          plain
          @click="batchDelHandler"
        >
          {{ t('user.batch_del') }}
        </el-button>
        <span class="bottom-info">{{
          t('user.selection_info', [state.multipleSelection.length])
        }}</span>
        <el-button size="small" class="clear-selection" @click="clearSelection" text>
          {{ t('user.clear_button') }}
        </el-button>
      </div>
    </div>
  </div>
  <div v-else-if="activeName === 'role'" class="role-content">
    <role-manage @refresh="refreshRole" @refresh-grid="refreshGrid"></role-manage>
  </div>
  <drawer-main
    :filter-options="filterOption"
    @trigger-filter="searchCondition"
    ref="drawerMainRef"
  ></drawer-main>
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
      color: var(--ed-color-primary);
      background-color: var(--ed-color-white);
      &:focus {
        outline: none;
        color: var(--ed-color-primary) !important;
        background: var(--ed-color-primary-light-9) !important;
      }
    }
  }

  div:hover {
    cursor: pointer;
    svg {
      color: var(--ed-color-primary) !important;
      background: var(--ed-color-primary-light-9) !important;
    }
  }
}

.bottom-bar {
  position: absolute;
  bottom: 45px;
  display: flex;
  .bottom-info {
    font-size: 12px;
    color: var(--ed-color-info);
    line-height: 26px;
    padding: 0 15px;
  }
  .batch-delete-button {
    color: var(--ed-button-text-color);
    border-color: var(--ed-button-border-color);
    &:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
      outline: none;
    }
  }
  .clear-selection {
    font-size: 12px;
  }
}
.reset-pwd-confirm {
  // height: 115px;
  padding: 5px 15px;
  .confirm-header {
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: flex;
    flex-direction: row;
    .icon-span {
      color: var(--ed-color-warning);
      font-size: 22px;
      i {
        top: 3px;
      }
    }
    .header-span {
      font-size: 16px;
      font-weight: bold;
      margin-left: 10px;
    }
  }
  .confirm-foot {
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    margin-top: 15px;
  }
  .confirm-warning {
    font-size: 12px;
    color: var(--ed-color-danger);
    margin-left: 33px;
  }
  .confirm-content {
    margin-left: 33px;
  }
}
.right-filter {
  .filter-button {
    &:hover {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #f5f6f7;
      outline: 0;
    }
    &:focus {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #eff0f1;
      outline: 0;
    }
  }
  .filter-condition-button {
  }
}
</style>
