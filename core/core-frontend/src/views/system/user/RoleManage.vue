<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { Icon } from '@/components/icon-custom'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import OrgUser from './OrgUser.vue'
import {
  searchRoleApi,
  userSelectedForRoleApi,
  roleDelApi,
  beforeUnmountInfoApi,
  unMountUserApi
} from '@/api/user'
import RoleForm from './RoleForm.vue'
import OutUserForm from './OutUserForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { setColorName } from '@/utils/utils'
const selectedRoleId = ref('')
const selectedRoleName = ref('')
const selectedRoleRoot = ref(false)
const roleKeyword = ref('')
const selectedFilterkey = ref('')
const roleFormRef = ref(null)
const outUserFormRef = ref(null)
const { t } = useI18n()
const loading = ref(false)
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root?: boolean
}

const handleNodeClick = (data: Tree) => {
  if (data.disabled) {
    return
  }
  selectedRoleId.value = data.id
  selectedRoleName.value = data.name
  selectedRoleRoot.value = data.root
  selectedSearch(data.id)
}

const state = reactive({
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  optionUserList: [],
  addedUserList: [],
  roleData: [],
  checkList: []
})
const order = ref(null)
state.roleData = [
  {
    id: 'system',
    name: t('role.system_role'),
    children: null,
    disabled: true,
    root: true
  },
  {
    id: 'custom',
    name: t('role.custom_role'),
    children: null,
    disabled: true,
    root: false
  }
]

const selectedSearch = (rid?: string) => {
  const param = { rid, order: order.value, keyword: selectedFilterkey.value }
  if (rid) {
    loading.value = true
    const page = state.paginationConfig.currentPage
    const limit = state.paginationConfig.pageSize
    userSelectedForRoleApi(page, limit, param).then(res => {
      if (res?.data?.total) {
        const records = res.data.records
        records.forEach(item => {
          setColorName(item, selectedFilterkey.value)
          setColorName(item, selectedFilterkey.value, 'account', 'colorAccount')
        })
        state.addedUserList = records
        state.paginationConfig.total = res.data.total
      } else {
        state.addedUserList = []
        state.paginationConfig.total = 0
      }
      loading.value = false
    })
  }
}

const roleSearch = () => {
  loading.value = true
  searchRoleApi(null).then(res => {
    const roles = res.data
    const map = groupBy(roles)
    state.roleData[0].children = map.get(true)
    state.roleData[1].children = map.get(false)
    loading.value = false
  })
}

const groupBy = (list: Tree[]) => {
  const map = new Map()
  list.forEach(item => {
    const root = item.root
    let arr = map.get(root)
    if (!arr) {
      arr = []
    }
    item.disabled = false
    arr.push(item)
    map.set(root, arr)
  })
  return map
}

const roleAdd = () => {
  roleFormRef.value.init()
}

const roleEdit = row => {
  roleFormRef.value.edit(row.id)
}

const delHandler = row => {
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      t('role.confirm_delete') +
      '</strong></br>' +
      t('role.delete_tips'),
    showClose: false
  }).then(() => {
    loading.value = true
    roleDelApi(row.id).then(() => {
      ElMessage.success(t('common.delete_success'))
      roleSearch()
    })
  })
}
const emits = defineEmits(['refresh', 'refresh-grid'])
const roleSaved = () => {
  roleSearch()
  emits('refresh')
}
const outUserSaved = () => {
  selectedSearch(selectedRoleId.value)
  emits('refresh-grid')
}

const unBindUser = (uid: string) => {
  const param = { uid, rid: selectedRoleId.value }
  loading.value = true
  beforeUnmountInfoApi(param).then(res => {
    if (res.data) {
      const msg = res.data === 2 ? t('role.clear_in_system') : t('role.clear_in_org')
      ElMessageBox.confirm(t('role.confirm_unbind_user'), {
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        tip: msg,
        showClose: false
      })
        .then(() => {
          unMountUserHandler(param, () => {
            moveSelected([uid])
          })
        })
        .catch(() => {
          loading.value = false
        })
    } else {
      // 删除用户角色映射
      ElMessageBox.confirm(t('role.confirm_unbind_user'), {
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        tip: '',
        showClose: false
      })
        .then(() => {
          unMountUserHandler(param, () => {
            moveSelected2Option([uid])
          })
        })
        .catch(() => {
          loading.value = false
        })
    }
  })
}

const unMountUserHandler = (param: any, callback?) => {
  loading.value = true
  unMountUserApi(param).then(() => {
    ElMessage({
      message: t('role.unbind_success'),
      type: 'success'
    })
    emits('refresh-grid')
    callback && callback()
    loading.value = false
  })
}

const moveSelected = (uids: string[]): any[] => {
  const result = []
  let len = state.addedUserList.length
  while (len--) {
    const item = state.addedUserList[len]
    if (uids.includes(item.id)) {
      state.addedUserList.splice(len, 1)
      result.push({ ...item })
    }
  }
  return result
}
const moveSelected2Option = (uids: string[]) => {
  const result = moveSelected(uids)
  if (result?.length) {
    result.forEach(item => {
      state.optionUserList.push({ ...item })
    })
  }
}
const openOutUser = () => {
  outUserFormRef.value.init(selectedRoleId.value)
}

const triggerFilterRole = () => {
  const value = roleKeyword.value
  state.roleData.forEach(roleGroup => {
    roleGroup.children?.forEach(data => {
      setColorName(data, value)
      data['hidden'] = value && !data.name.includes(value)
    })
  })
  console.log(state.roleData)
}
const filterSelected = () => {
  selectedSearch(selectedRoleId.value)
}
const addOrgUserDialog = ref()
const handleCommand = (command: string) => {
  if ('org' === command) {
    addOrgUserDialog.value.init()
    return
  }
  openOutUser()
}

const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  selectedSearch(selectedRoleId.value)
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  selectedSearch(selectedRoleId.value)
}
const sortChange = param => {
  order.value = null
  if (param.order && param.prop === 'name') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    order.value = 'name ' + type
  } else {
    order.value = null
  }
  selectedSearch(selectedRoleId.value)
}
const userAddPopper = ref(false)

const handleVisibleChange = (val: boolean) => {
  userAddPopper.value = val
}
const refreshGrid = () => {
  selectedSearch(selectedRoleId.value)
  emits('refresh-grid')
}
onMounted(() => {
  roleSearch()
})
</script>

<template>
  <div class="role-manage" v-loading="loading">
    <div class="role-list role-height">
      <div class="title">
        <div class="text w100 flex-align-center">
          <span>{{ t('role.role_title') }}</span>
        </div>
        <el-input
          class="m16 w100"
          v-model="roleKeyword"
          clearable
          @change="triggerFilterRole"
          :placeholder="t('commons.search')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-scrollbar class="role-tree-container">
        <div v-for="(roleGroup, index) in state.roleData" :key="roleGroup.id">
          <div class="role-title text flex-align-center">
            <span>{{ roleGroup.name }}</span>
            <span class="icon-span">
              <el-icon v-if="!roleGroup.root" @click="roleAdd" class="hover-icon">
                <Icon name="icon_add_outlined"></Icon>
              </el-icon>
            </span>
          </div>
          <div
            class="list-item_primary"
            :class="{ 'de-role-hidden': role.hidden, 'de-is-active': selectedRoleId === role.id }"
            v-for="role in roleGroup.children"
            :key="role.id"
            @click.stop="handleNodeClick(role)"
          >
            <span class="flex-align-center label">
              <span v-if="role.colorName" v-html="role.colorName" />
              <span v-else>{{ role.name }}</span>
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('role.system_role')"
                placement="top"
                v-if="role.root"
              >
                <span class="mark flex-center">{{ t('role.system') }}</span>
              </el-tooltip>
              <span v-else class="de-mark flex-center">{{
                role.readonly ? t('role.manager') : t('role.staff')
              }}</span>
            </span>
            <span class="btn-list" :class="{ 'de-disabled-btn': role.root }">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="role.root ? t('role.system_role_edit_tips') : t('common.edit')"
                placement="top"
              >
                <el-icon @click.stop="roleEdit(role)" class="hover-icon">
                  <Icon name="icon_edit_outlined"></Icon>
                </el-icon>
              </el-tooltip>

              <el-tooltip
                class="box-item"
                effect="dark"
                :content="role.root ? t('role.system_role_del_tips') : t('common.delete')"
                placement="top"
              >
                <el-icon @click.stop="delHandler(role)" class="hover-icon">
                  <Icon name="icon_delete-trash_outlined"></Icon>
                </el-icon>
              </el-tooltip>
            </span>
          </div>
          <el-divider v-if="!index" />
        </div>
      </el-scrollbar>
    </div>
    <div class="added-user-list role-height" v-if="selectedRoleId">
      <div class="user-info flex-align-center">
        <span class="text">{{ selectedRoleName }}</span>
        <span v-if="selectedRoleRoot" class="mark flex-center">{{ t('role.system') }}</span>
        <el-divider direction="vertical" />
        <el-icon>
          <Icon name="icon_member_filled"></Icon>
        </el-icon>
        <span class="user-num">{{ state.paginationConfig.total }}</span>
      </div>
      <el-row>
        <el-col :span="12">
          <el-dropdown
            placement="bottom-start"
            @visible-change="handleVisibleChange"
            popper-class="menu-user-add_popper"
            @command="handleCommand"
            trigger="click"
          >
            <el-button type="primary">
              {{ t('system.addUser') }}
              <el-icon style="margin-left: 4px">
                <arrow-up v-if="userAddPopper" />
                <arrow-down v-else />
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="org">
                  <el-icon>
                    <Icon name="icon_team-add_outlined"></Icon>
                  </el-icon>
                  {{ t('role.org_user_title') }}
                </el-dropdown-item>
                <el-dropdown-item command="external">
                  <el-icon>
                    <Icon name="icon_member-add_outlined"></Icon>
                  </el-icon>
                  {{ t('role.out_user_title') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
        <el-col :span="12" style="margin-bottom: 16px; text-align: right">
          <el-input
            style="width: 240px"
            v-model="selectedFilterkey"
            clearable
            @change="filterSelected"
            :placeholder="t('role.user_search_placeholder')"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <div class="user-table">
        <GridTable
          :pagination="state.paginationConfig"
          :table-data="state.addedUserList"
          @current-change="pageChange"
          @size-change="sizeChange"
          @sort-change="sortChange"
        >
          <!-- <el-table-column type="selection" width="30" /> -->
          <el-table-column
            key="name"
            show-overflow-tooltip
            prop="name"
            sortable="custom"
            :label="t('user.name')"
          >
            <template v-slot:default="scope">
              <span v-if="scope.row.colorName" v-html="scope.row.colorName" />
              <span v-else>{{ scope.row.name }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="account"
            show-overflow-tooltip
            key="user_name"
            :label="t('datasource.user_name')"
          >
            <template v-slot:default="scope">
              <span v-if="scope.row.colorAccount" v-html="scope.row.colorAccount" />
              <span v-else>{{ scope.row.account }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="email"
            show-overflow-tooltip
            key="email"
            :label="t('common.email')"
          />

          <el-table-column key="_operation" fixed="right" :label="$t('common.operate')">
            <template #default="scope">
              <el-button @click="unBindUser(scope.row.id)" text>
                <template #icon>
                  <Icon name="icon_assigned_outlined"></Icon>
                </template>
              </el-button>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
    <el-empty
      v-else
      class="added-user-list role-height"
      :description="t('role.empty_description')"
    />
  </div>
  <role-form ref="roleFormRef" @saved="roleSaved" />
  <out-user-form ref="outUserFormRef" @saved="outUserSaved" />
  <OrgUser ref="addOrgUserDialog" :rid="selectedRoleId" @refresh-grid="refreshGrid"></OrgUser>
</template>

<style lang="less" scoped>
.role-manage {
  display: flex;
  width: 100%;
  height: 100%;

  .role-height {
    height: calc(100vh - 165px);
    overflow: auto;
    position: relative;
  }
  .role-height-option {
    height: calc(100vh - 170px);
    position: relative;
    overflow: hidden;
    .content {
      height: calc(100% - 140px);
      overflow: auto;
    }
  }

  .role-list {
    width: 269px;
    padding: 24px 7px;
    .title {
      padding: 0 17px;
    }

    .m16 {
      margin: 16px 0;
    }
    .role-tree-container {
      height: calc(100% - 112px);
      .text {
        .icon-span {
          color: #3370ff;
          margin: 0 16px 0 auto;
          font-size: 16px;
        }
      }
      .role-title {
        color: #8d9199;
        font-family: PingFang SC;
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        height: 40px;
        padding-left: 16px;
      }
      .de-role-hidden {
        display: none !important;
      }
      .de-is-active {
        background-color: var(--ed-menu-hover-bg-color);
        color: var(--ed-menu-active-color);
      }
      .list-item_primary {
        padding: 0 16px;
        .mark {
          height: 16px;
          border-radius: 2px;
          margin-left: 8px;
          background-color: var(--ed-color-primary-light-7);
          color: var(--ed-menu-active-color);
          font-family: PingFang SC;
          font-size: 10px;
          font-weight: 500;
          line-height: 13px;
          padding: 0 4px;
        }
        .de-mark {
          height: 16px;
          border-radius: 2px;
          margin-left: 8px;
          background-color: rgb(232 233 233);
          color: #646a73;
          font-family: PingFang SC;
          font-size: 10px;
          font-weight: 500;
          line-height: 13px;
          padding: 0 4px;
        }
        .btn-list {
          display: none;
        }
        &:hover {
          cursor: pointer;
          .btn-list {
            display: flex;
            align-items: center;
          }
          .de-disabled-btn {
            i {
              color: #bbbfc4;
              cursor: not-allowed;
              background-image: none;
              background-color: var(--el-button-disabled-bg-color);
              border-color: var(--el-button-disabled-border-color);
            }
          }
        }
      }

      .ed-divider--horizontal {
        margin: 4px 13px;
        width: calc(100% - 26px);
      }
    }
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 24px;
      top: -24px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }
  .foot1 {
    display: flex;
    margin-top: 10px;
    button {
      width: 100%;
    }
  }

  .m24 {
    margin: 24px 0;
  }
  .w100 {
    width: 100%;
  }

  .added-user-list {
    flex: 1;
    border-left: 2px solid var(--MainBG, #f5f6f7);
    padding: 24px;

    .ed-input {
      width: 120px;
      height: 32px;
    }
    .user-info {
      margin-bottom: 16px;
      font-style: normal;
      font-family: PingFang SC;
      font-size: 16px;
      .text {
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
      }
      .user-num {
        color: #8d9199;
        font-weight: 400;
        line-height: 16px;
      }
      .mark {
        height: 16px;
        border-radius: 2px;
        margin-left: 8px;
        background-color: var(--ed-color-primary-light-7);
        color: var(--ed-menu-active-color);
        font-family: PingFang SC;
        font-size: 10px;
        font-weight: 500;
        line-height: 13px;
        padding: 0 4px;
      }
    }

    .user-table {
      height: calc(100% - 90px);
    }

    .user-list-item {
      float: left;
      width: 150px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 24px 0 0 24px;
      border: 1px solid #ccc;
      .role-remove-icon {
        display: none;
        top: 0;
        right: 0;
        width: 12px;
        height: 12px;
        color: var(--ed-color-primary);
        background: var(--ed-color-primary-light-7);
      }
      &:hover {
        cursor: pointer;
        border-color: var(--ed-color-primary-light-7);
        .role-remove-icon {
          display: block;
        }
      }
    }
  }

  .add-user-list {
    width: 269px;
    padding: 24px;

    .user-list-item {
      width: 100%;
      height: 30px;
      margin-bottom: 24px;
      padding-left: 24px;
      border: 1px solid #ccc;
    }
  }
}

.add-out-icon {
  cursor: pointer;
  color: var(--ed-text-color-regular);
  background-color: var(--ed-color-white);
  :hover {
    color: var(--ed-color-primary) !important;
    background: var(--ed-color-primary-light-7) !important;
  }
}
</style>

<style lang="less">
.menu-user-add_popper {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}
</style>
