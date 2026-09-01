<script lang="ts" setup>
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_resetpassword from '@/assets/svg/icon_resetpassword.svg'
import icon_warning_filled from '@/assets/svg/icon_warning_filled.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_mfa_reset from '@/assets/svg/icon_mfa_reset.svg'
import { ref, reactive, onMounted, computed, unref } from 'vue'
import { Icon } from '@/components/icon-custom'
import { FilterText, convertFilterText } from '@/components/filter-text'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import UserForm from '@/views/menu/system/sysuser/UserForm.vue'
import { useI18n } from '@/hooks/web/useI18n'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import request from '@/config/axios'
import { HandleMore } from '@/components/handle-more'
import {
  userPageApi,
  userDelApi,
  batchDelApi,
  defaultPwdApi,
  resetPwdApi,
  switchEnableApi,
  roleOptionForUserApi,
  userUnlockApi
} from '@/api/user'
import { searchApi as orgSearchApi } from '@/api/org'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { setColorName } from '@/utils/utils'
import UserImport from '@/views/menu/system/sysuser/UserImport/index.vue'
import useClipboard from 'vue-clipboard3'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutHandler } from '@/utils/logout'
import { filterOption, groupBy } from '@/views/menu/system/sysuser/options'

const userStore = useUserStoreWithOut()
const curUid = computed(() => userStore.getUid)
const { toClipboard } = useClipboard()
const { t } = useI18n()
const drawerMainRef = ref(null)
const userFormDialog = ref(null)
const loading = ref(false)
const multipleTableRef = ref(null)
const defaultPwd = ref(null)
interface FieldSort {
  field: string
  type: boolean
}
const addUser = () => {
  userFormDialog.value.init()
}

const orgLoad = ref(false)
const transformOrgTree = (nodes: any[]): any[] =>
  nodes.map(n => ({
    value: n.id,
    label: n.name,
    disabled: n.disabled ?? false,
    children: n.children ? transformOrgTree(n.children) : []
  }))
const drawerMainOpen = async () => {
  // Load org tree (once)
  if (!orgLoad.value) {
    const orgRes = await orgSearchApi({})
    filterOption[2].option = transformOrgTree(orgRes.data || [])
    orgLoad.value = true
  }
  // Get current org selection
  const oidCond = state.conditions?.find(c => c.field === 'oid')
  const currentOid = oidCond?.value
  const currentOidVal = Array.isArray(currentOid) ? currentOid?.[0] : currentOid
  // Set role filter disabled state
  filterOption[3].disabled = !currentOidVal
  filterOption[3].property.placeholder = currentOidVal
    ? t('commons.role')
    : t('org.select_org_first')
  // Load roles (org-scoped if org selected, empty otherwise)
  await loadRolesForOrg(currentOidVal ? [currentOidVal] : undefined)
  drawerMainRef.value.init()
}
const drawerMainClose = () => {
  drawerMainRef.value.close()
}

const moreList = [
  {
    label: t('setting_mfa.reset_key_tips'),
    divided: false,
    svgName: icon_mfa_reset,
    command: 'resetMfa'
  },
  {
    label: t('user.unlock_user'),
    divided: false,
    svgName: icon_mfa_reset,
    command: 'unlock'
  },
  {
    label: t('common.delete'),
    divided: true,
    svgName: icon_deleteTrash_outlined,
    command: 'delete'
  }
]
const selfMoreList = [
  {
    label: t('setting_mfa.reset_key_tips'),
    divided: false,
    svgName: icon_mfa_reset,
    command: 'resetMfa'
  }
]

const moreHandler = (cmd: string, row: Org) => {
  if (cmd === 'delete') {
    delHandler(row)
    return
  }
  if (cmd === 'resetMfa') {
    resetMfaHandler(row)
    return
  }
  if (cmd === 'unlock') {
    unlockHandler(row)
  }
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
  orders: [] as FieldSort[],
  multipleSelection: [],
  originMap: {
    0: 'LOCAL',
    1: 'LDAP',
    2: 'OIDC',
    3: 'CAS',
    4: t('user.feishu'),
    5: t('user.dingtalk'),
    6: t('user.wechat_for_business'),
    7: t('user.international_feishu'),
    9: 'OAuth2',
    10: 'Saml2'
  }
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
const buildParam = () => {
  const param = {}
  if (state.conditions?.length) {
    state.conditions.forEach(condition => {
      if (condition['value']) {
        // For org filter (single-select), send single value instead of array
        if (condition['field'] === 'oid' && Array.isArray(condition['value'])) {
          param[condition['field']] = condition['value'][0]
        } else {
          param[condition['field']] = condition['value']
        }
      }
    })
  }
  if (keyword.value) {
    param['keyword'] = keyword.value
  }
  if (state.orders?.length) {
    state.orders.forEach(item => {
      param[item['field']] = item.type
    })
  }
  return param
}
const search = () => {
  loading.value = true
  userPageApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    buildParam()
  ).then(res => {
    const records = res.data.records
    records.forEach(item => {
      setColorName(item, keyword.value)
      setColorName(item, keyword.value, 'account', 'colorAccount')
      setColorName(item, keyword.value, 'email', 'colorEmail')
    })
    imgType.value = getEmptyImg()
    emptyDesc.value = getEmptyDesc()
    state.userList = records
    state.paginationConfig.total = res.data.total
    if (
      state.paginationConfig.currentPage > 1 &&
      res.data?.pages < state.paginationConfig.currentPage &&
      !records?.length
    ) {
      pageChange(1)
    }
    loading.value = false
  })
}
const filterRoles = cellValue => {
  if (!cellValue) return '-'
  const roleNames = cellValue.map(ele => ele?.name)
  return roleNames.length ? roleNames.join() : '-'
}
const changeSwitch = row => {
  const param = { id: row.id, enable: row.enable }
  loading.value = true
  switchEnableApi(param).then(() => {
    ElMessage.success(t(row.enable ? 'user.enable_success' : 'user.disable_success'))
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
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
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
    .catch(err => {
      console.error(err)
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
const treeFilterChangeHandler = ({
  value,
  field
}: {
  value: any
  field: string
  operator: string
}) => {
  if (field === 'oid') {
    const newOid = value?.[0]
    const oldOid = state.conditions?.find(c => c.field === 'oid')?.value
    const oldOidVal = Array.isArray(oldOid) ? oldOid?.[0] : oldOid
    if (newOid !== oldOidVal || (newOid == null && oldOidVal == null)) {
      drawerMainRef.value?.cleanrInnerValue(3)
      filterOption[3].disabled = !newOid
      filterOption[3].property.placeholder = newOid ? t('commons.role') : t('org.select_org_first')
      filterOption[3].property.customPlaceholder = newOid ? '' : t('org.select_org_first_top')
      loadRolesForOrg(newOid ? [newOid] : undefined)
    }
  }
}
const searchCondition = conditions => {
  // Cascade: if org changed, clear role filter and reload roles
  const oidCond = conditions?.find(c => c.field === 'oid')
  const newOid = oidCond?.value
  const newOidVal = Array.isArray(newOid) ? newOid?.[0] : newOid
  const oldOidCond = state.conditions?.find(c => c.field === 'oid')
  const oldOid = oldOidCond?.value
  const oldOidVal = Array.isArray(oldOid) ? oldOid?.[0] : oldOid
  if (newOidVal !== oldOidVal) {
    // Toggle role filter disabled state
    filterOption[3].disabled = !newOidVal
    filterOption[3].property.placeholder = newOidVal ? t('commons.role') : t('org.select_org_first')
    // Clear role condition
    conditions = conditions.filter(c => c.field !== 'roleIdList')
    // Reload roles for new org
    loadRolesForOrg(newOidVal ? [newOidVal] : undefined)
  }
  state.conditions = conditions
  search()
  fillFilterText()
  drawerMainClose()
}
const loadRolesForOrg = (oidList?: number[] | string[]) => {
  if (!oidList || oidList.length === 0) {
    filterOption[3].option = []
    return Promise.resolve()
  }
  const param: any = { oidList }
  return roleOptionForUserApi(param).then(res => {
    const roles = res.data || []
    // Format as { id, name } for select dropdown
    filterOption[3].option = roles.map(r => ({ id: r.id, name: r.name }))
  })
}
const fillFilterText = () => {
  const textArray = state.conditions?.length
    ? convertFilterText(state.conditions, filterOption)
    : []
  state.filterTexts = [...textArray]
  // Object.assign(state.filterTexts, textArray)
}
const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = size
  search()
}
const sortChange = param => {
  state.orders = []
  if (param.order && param.prop === 'createTime') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    state.orders.push({
      field: 'timeDesc',
      type: type !== 'asc'
    })
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
  ElMessageBox.confirm(
    t('user.confirm_batch_delete', [' ' + state.multipleSelection.length + ' ']),
    {
      confirmButtonType: 'danger',
      type: 'warning',
      confirmButtonText: t('common.delete'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false
    }
  )
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
  row.popoverRef?.hide()
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
const rowCheckStatus = row => {
  return row.id !== curUid.value && row.id !== '1'
}

const imgType = ref()
const emptyDesc = ref('')
const getEmptyImg = (): string => {
  if (keyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t('work_branch.relevant_content_found')
  }

  return ''
}

const setPopoverRef = (el, row) => {
  row.popoverRef = el
}
const setButtonRef = (el, row) => {
  row.buttonRef = el
}
const onClickOutside = row => {
  if (row.popoverRef) {
    unref(row.popoverRef).popperRef?.delayHide?.()
  }
}
const resetMfaHandler = row => {
  const url = `/user/mfaRest/${row.id}`
  request.post({ url }).then(() => {
    ElMessage.success(t('user.reset_success'))
  })
}
const unlockHandler = row => {
  ElMessageBox.confirm(t('user.confirm_unlock'), {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('user.unlock_user'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false
  })
    .then(() => {
      userUnlockApi(row.id).then(() => {
        ElMessage.success(t('user.unlock_user_success'))
        search()
      })
    })
    .catch(err => {
      console.error(err)
    })
}
</script>
<template>
  <div
    :class="!!state.multipleSelection.length && 'user-table-selection'"
    class="user-table de-search-table"
  >
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
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
        <el-button
          @click="drawerMainOpen"
          :plain="!!state.conditions.length"
          :class="state.conditions.length ? 'filter-condition-button' : 'filter-button'"
        >
          <template #icon>
            <Icon name="icon-filter"><iconFilter class="svg-icon" /></Icon>
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
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        @selection-change="handleSelectionChange"
        :show-empty-img="!loading"
      >
        <el-table-column type="selection" width="30" :selectable="rowCheckStatus" />
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
        >
          <template v-slot:default="scope">
            <span v-html="scope.row.colorAccount || scope.row.account" />
          </template>
        </el-table-column>

        <el-table-column
          prop="orgItem"
          key="orgItem"
          :label="t('commons.organization')"
          width="150"
          show-overflow-tooltip
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.orgItem?.name || '' }}</span>
          </template>
        </el-table-column>

        <el-table-column
          prop="roleItems"
          key="roleItems"
          :label="t('user.role')"
          min-width="200"
          show-overflow-tooltip
        >
          <template #default="scope">
            <div class="de-one-line">
              {{ filterRoles(scope.row.roleItems) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="origin"
          key="origin"
          :label="t('auth.sysParams_type.user_source')"
          width="90"
        >
          <template v-slot:default="scope">
            <span>{{ state.originMap[scope.row.origin] || 'LOCAL' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="sysVariable"
          show-overflow-tooltip
          key="sysVariable"
          :label="t('auth.sysParams')"
          width="200"
        />
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
            <el-tooltip
              effect="dark"
              :content="
                scope.row.id === '1'
                  ? t('user.cannot_be_modified')
                  : scope.row.id === curUid
                  ? t('user.cannot_be_modified_de')
                  : t('user.has_been_disabled')
              "
              placement="top"
              v-if="scope.row.id === '1' || scope.row.id === curUid || !scope.row.enable"
            >
              <el-switch
                class="user-state-switch"
                :disabled="scope.row.id === '1' || scope.row.id === curUid"
                v-model="scope.row.enable"
                inactive-color="#DCDFE6"
                @change="changeSwitch(scope.row)"
              />
            </el-tooltip>
            <el-switch
              v-else
              class="user-state-switch"
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
          width="180"
        >
          <template v-slot:default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column width="124" fixed="right" key="_operation" :label="t('common.operate')">
          <template #default="scope">
            <div class="operate-icon-container" v-if="scope.row.id !== '1'">
              <el-tooltip effect="dark" :content="t('common.edit')" placement="top">
                <el-button text @click="edit(scope.row)">
                  <template #icon>
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <div @click="scope.row.origin === 0 && (scope.row.resetInfoShow = true)">
                <el-tooltip effect="dark" :content="t('user.reset_pwd')" placement="top">
                  <el-button
                    text
                    :disabled="scope.row.origin !== 0"
                    :ref="
                      el => {
                        setButtonRef(el, scope.row)
                      }
                    "
                    v-click-outside="onClickOutside(scope.row)"
                  >
                    <template #icon>
                      <Icon name="icon_resetpassword"><icon_resetpassword class="svg-icon" /></Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-popover
                  placement="right"
                  :width="300"
                  :virtual-ref="scope.row.buttonRef"
                  trigger="click"
                  :ref="
                    el => {
                      setPopoverRef(el, scope.row)
                    }
                  "
                  @show="loadRestInfo"
                  :show-arrow="true"
                >
                  <!-- <template #reference>

                  </template> -->
                  <div class="reset-pwd-confirm">
                    <div class="confirm-header">
                      <span class="icon-span">
                        <el-icon>
                          <Icon name="icon_warning_filled"
                            ><icon_warning_filled class="svg-icon"
                          /></Icon>
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
              <div class="user-icon-more">
                <handle-more
                  class="user-handle-more"
                  @handle-command="cmd => moreHandler(cmd, scope.row)"
                  :menu-list="
                    scope.row.id === curUid
                      ? selfMoreList
                      : moreList.filter(item => item.command !== 'unlock' || scope.row.locked)
                  "
                />
              </div>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-if="state.multipleSelection.length" class="bottom-bar flex-align-center">
    <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
      {{ t('user.batch_del') }}
    </el-button>
    <span class="bottom-info">{{
      t('user.selection_info', [state.multipleSelection.length])
    }}</span>
    <el-button text @click="clearSelection">
      {{ t('user.clear_button') }}
    </el-button>
  </div>

  <drawer-main
    :filter-options="filterOption"
    @trigger-filter="searchCondition"
    @tree-filter-change="treeFilterChangeHandler"
    ref="drawerMainRef"
  ></drawer-main>
  <user-form @saved="saveHandler" ref="userFormDialog"></user-form>
</template>

<style lang="less" scoped>
.user-state-switch:not(.is-checked) {
  :deep(.ed-switch__core) {
    background: #bbbfc4;
  }
}

.user-table {
  height: calc(100% - 24px);
  box-sizing: border-box;
  background: white;
  padding: 24px;
  margin-top: 8px;
  border-radius: 4px;

  .user-table__content {
    height: calc(100vh - 186px);
  }

  .is-in-filter {
    height: calc(100vh - 236px);
  }
}

.user-table-selection {
  height: calc(100% - 38px);
  .user-table__content {
    height: calc(100vh - 225px);
  }

  .is-in-filter {
    height: calc(100vh - 275px);
  }
}

.operate-icon-container {
  font-size: 16px;
  display: flex;

  .ed-button {
    width: 24px;
    height: 24px;
    line-height: 24px;
  }
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  height: 64px;
  width: calc(100% - 328px);
  padding-left: 24px;
  background: var(--neutral-00, #fff);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);
  .bottom-info {
    color: #646a73;
    margin: 0 16px 0 24px;
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
}
.reset-pwd-confirm {
  // height: 115px;
  padding: 5px 15px;
  .confirm-header {
    width: 100%;
    min-height: 40px;
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
      white-space: pre-wrap;
      word-break: keep-all;
    }
  }
  .confirm-foot {
    padding: 0;
    display: flex;
    flex-wrap: wrap;
    justify-content: flex-end;
    align-items: center;
    margin-top: 15px;
    .ed-button {
      min-width: 48px;
      height: 28px;
      line-height: 28px;
      font-size: 12px;
    }
  }
  .confirm-warning {
    font-size: 12px;
    color: var(--ed-color-danger);
    margin-left: 33px;
  }
  .confirm-content {
    margin-left: 33px;
    display: flex;
    align-items: center;
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
}
</style>
<style lang="less">
.ed-message-box__message {
  .tip {
    margin-top: 8px;
  }
}
.popper-max-width {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }
}
.operate-icon-container {
  .user-icon-more {
    color: var(--ed-color-primary) !important;
    border: 0 solid transparent;
    background-color: transparent;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
    line-height: 24px;
    height: 24px;
    letter-spacing: 0;
    text-align: center;
    padding: 0px 0;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    white-space: nowrap;
    cursor: pointer;
    box-sizing: border-box;
    outline: 0;
    transition: 0.1s;

    user-select: none;
    vertical-align: middle;

    border-radius: var(--ed-border-radius-base);
    .ed-dropdown {
      color: var(--ed-color-primary) !important;
      i {
        color: var(--ed-color-primary) !important;
      }
      &:hover {
        i {
          background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
        }
      }
    }
    .user-handle-more {
      .hover-icon {
        color: inherit !important;
      }
    }
  }
}
</style>
