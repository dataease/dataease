<script lang="ts" setup>
import { reactive, ref, nextTick, shallowRef, computed, provide, toRefs, watch } from 'vue'
import { GridTable } from '@/components/grid-table'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import RowAuth from './auth-tree/RowAuth.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import type { Action } from 'element-plus-secondary'
import {
  rowPermissionList,
  rowPermissionTargetObjList,
  listFieldByDatasetGroup,
  saveRowPermission,
  whiteListUsersForPermissions,
  deleteRowPermission
} from '@/api/dataset'

interface Pagination {
  currentPage: number
  pageSize: number
  total: number
}

interface User {
  name: string
  id: string
  email: string
}

interface RowForm {
  authTargetType: 'role' | 'user' | 'sysParams'
  authTargetId: string
  enable: boolean
  datasetId: number
  id?: string
  whiteListUser: User[]
}

const { t } = useI18n()
const defaultForm = {
  authTargetId: '',
  authTargetType: 'role',
  datasetId: 0,
  whiteListUser: [],
  enable: true
}
const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const rowPermissionForm = reactive<RowForm>(
  cloneDeep({
    enable: false,
    authTargetType: 'role',
    whiteListUser: [],
    authTargetId: '',
    datasetId: 0
  })
)

const state = reactive({
  rowList: []
})

const props = defineProps({
  datasetId: {
    required: false,
    default: 0,
    type: Number
  }
})

const { datasetId } = toRefs(props)

const loadingRowPermission = ref(false)
const update_row_permission = ref(false)
const update_row_permission_dialog_title = ref('')
const rowAuth = ref()
const whiteListUsers = shallowRef<User[]>([])
const targetObjs = shallowRef<{ name: string; id: string }[]>([])
const emptyTips = computed(() => {
  return t('auth.select') + t(`auth.${rowPermissionForm.authTargetType}`)
})
const datasetTableFiled = ref([])

const initDatasetTableField = () => {
  listFieldByDatasetGroup(datasetId.value).then(res => {
    datasetTableFiled.value = res.data
  })
}

provide('filedList', datasetTableFiled)
provide('getAuthTargetType', rowPermissionForm)

const typeList = ['role', 'user', 'sysParams']

const formatter = (_, __, cellValue) => {
  return cellValue ? t(`auth.${cellValue}`) : '-'
}
const formatterWhiteListUsers = (_, __, cellValue) => {
  return cellValue ? cellValue.map(ele => ele.name).join('、') : '-'
}

const onAuthTypeChange = () => {
  whiteListUsers.value = []
  rowPermissionForm.authTargetId = ''
  fetchTypeObjsList()
  changeUserList()
}

const authTargetTypeLoading = ref(false)

watch(
  () => rowPermissionForm.authTargetType,
  (newValue, oldValue) => {
    if (authTargetTypeLoading.value) return
    if ([newValue, oldValue].includes('sysParams')) {
      nextTick(() => {
        rowAuth.value.init([])
      })
    }
    if (newValue === 'sysParams') {
      whiteListUsersList()
    }
  }
)

const changeUserList = () => {
  rowPermissionForm.whiteListUser = []
  whiteListUsersList()
}

const confirm = () => {
  rowAuth.value.submit()
}

const whiteListUsersList = () => {
  whiteListUsers.value = []
  const { authTargetType, authTargetId } = rowPermissionForm
  let param = {}
  param = {
    authTargetId: authTargetId,
    section: 1,
    authTargetType: authTargetType,
    datasetId: datasetId.value
  }
  whiteListUsersForPermissions(param).then(res => {
    whiteListUsers.value = [] = res.data
  })
}

const save = ({ logic, items, errorMessage }) => {
  if (errorMessage) {
    ElMessage({
      message: errorMessage,
      type: 'error',
      showClose: true
    })

    return
  }
  loadingRowPermission.value = true
  let params: Omit<RowForm, 'whiteListUser'> & { expressionTree?: string; whiteListUser?: string } =
    {
      ...cloneDeep(rowPermissionForm),
      whiteListUser: JSON.stringify(rowPermissionForm.whiteListUser)
    }
  params.expressionTree = JSON.stringify({ items, logic })
  saveRowPermission(params).then(res => {
    ElMessage.success(t('common.save_success'))
    search()
  })
  clearData()
  loadingRowPermission.value = false
}

const clearData = () => {
  Object.assign(rowPermissionForm, cloneDeep(defaultForm))
  rowAuth.value.relationList = []
  rowAuth.value.logic = 'or'
  update_row_permission.value = false
}

const fetchTypeObjsList = () => {
  const { authTargetType, datasetId } = rowPermissionForm
  const params = {
    authTargetType,
    datasetId
  }
  rowPermissionTargetObjList(datasetId, rowPermissionForm.authTargetType).then(res => {
    targetObjs.value = res.data
  })
}

const search = () => {
  rowPermissionList(paginationConfig.currentPage, paginationConfig.pageSize, datasetId.value).then(
    res => {
      state.rowList = res.data.records
      paginationConfig.total = res.data.total
    }
  )
}

search()

initDatasetTableField()

const create = rowPermissionObj => {
  if (!rowPermissionObj) {
    targetObjs.value = []
    Object.assign(rowPermissionForm, cloneDeep(defaultForm))
    rowPermissionForm.datasetId = datasetId.value
    update_row_permission_dialog_title.value = t('dataset.row_permission.add')
  } else {
    Object.assign(rowPermissionForm, rowPermissionObj)
    update_row_permission_dialog_title.value = t('dataset.row_permission.edit')
    listRowPermissions(rowPermissionObj)
  }
  whiteListUsersList()
  fetchTypeObjsList()
  update_row_permission.value = true
}

const deleteRow = row => {
  ElMessageBox.confirm('确定删除行权限吗?', {
    confirmButtonText: t('dataset.confirm'),
    cancelButtonText: t('dataset.cancel'),
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === 'confirm') {
        deleteRowPermission(row).then(() => {
          ElMessage({
            message: t('dataset.delete_success'),
            type: 'success',
            showClose: true
          })
          search()
        })
      }
    }
  })
}

const listRowPermissions = row => {
  const {
    whiteListUser = '[]',
    enable = false,
    id = '',
    authTargetId,
    authTargetType,
    tree,
    datasetId
  } = row || { enable: true }
  Object.assign(rowPermissionForm, {
    authTargetId,
    authTargetType,
    datasetId,
    id,
    whiteListUser: JSON.parse(whiteListUser),
    enable
  })
  authTargetTypeLoading.value = true
  nextTick(() => {
    rowAuth.value.init(tree || {})
    authTargetTypeLoading.value = false
  })
  loadingRowPermission.value = false
}

const handleSizeChange = (pageSize: number) => {
  paginationConfig.currentPage = 1
  paginationConfig.pageSize = pageSize
  search()
}
const handleCurrentChange = (currentPage: number) => {
  paginationConfig.currentPage = currentPage
  search()
}
</script>

<template>
  <el-button class="add-row-column" secondary @click="create(null)">
    <template #icon>
      <Icon name="icon_add_outlined"></Icon>
    </template>
    {{ t('common.add') }}
  </el-button>
  <GridTable
    @size-change="handleSizeChange"
    @current-page="handleCurrentChange"
    :pagination="paginationConfig"
    :table-data="state.rowList"
  >
    <el-table-column
      :formatter="formatter"
      prop="authTargetType"
      :label="t('dataset.row_permission.type')"
    >
    </el-table-column>
    <el-table-column prop="authTargetName" :label="t('deDataset.restricted_objects')">
    </el-table-column>
    <el-table-column
      :formatter="formatterWhiteListUsers"
      prop="whiteListUsers"
      :label="t('auth.white_list')"
    >
    </el-table-column>
    <el-table-column :label="t('commons.operating')" fixed="right" key="__operation" width="130">
      <template #default="scope">
        <el-button @click="create(scope.row)" text>
          <template #icon> <Icon name="icon_edit_outlined"></Icon> </template
        ></el-button>

        <el-button @click="deleteRow(scope.row)" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"></Icon>
          </template>
        </el-button>
      </template>
    </el-table-column>
  </GridTable>
  <el-drawer
    :title="update_row_permission_dialog_title"
    custom-class="row-column-permissions"
    :wrapperClosable="false"
    :size="896"
    v-loading="loadingRowPermission"
    v-model="update_row_permission"
    direction="rtl"
    :before-close="clearData"
  >
    <div class="title-form_primary between">
      <span>{{ t('auth.row_permission') }}</span>
      <el-switch
        v-model="rowPermissionForm.enable"
        inactive-color="#BBBFC4"
        :active-text="t('auth.enable_row')"
      >
      </el-switch>
    </div>
    <div class="auth-type">
      <p class="type">{{ t('dataset.type') }}</p>
      <el-radio
        v-model="rowPermissionForm.authTargetType"
        @change="onAuthTypeChange"
        v-for="ele in typeList"
        :key="ele"
        :label="ele"
        >{{ t(`auth.${ele}`) }}</el-radio
      >
      <el-select
        v-if="rowPermissionForm.authTargetType !== 'sysParams'"
        class="target-objs"
        :placeholder="emptyTips"
        @change="changeUserList"
        v-model="rowPermissionForm.authTargetId"
      >
        <el-option v-for="item in targetObjs" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <p class="type">{{ t('auth.set_rules') }}</p>
    </div>
    <div class="relation-tree-container">
      <RowAuth ref="rowAuth" @save="save"></RowAuth>
    </div>
    <template v-if="rowPermissionForm.authTargetType !== 'user'">
      <div class="title-form_primary m16">
        <span>{{ t('auth.white_list') }}</span>
        <span class="explain">{{ t('auth.white_user_not') }}</span>
      </div>
      <div>
        <el-select
          popper-class="role-add-name"
          multiple
          clearable
          class="white-list"
          v-model="rowPermissionForm.whiteListUser"
          :placeholder="t('user.select_users')"
        >
          <el-option
            v-for="item in whiteListUsers"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          >
            <p class="name">{{ item.name }}</p>
            <p class="email">{{ item.email }}</p>
          </el-option>
        </el-select>
      </div>
    </template>

    <template #footer>
      <el-button secondary @click="clearData">{{ t('dataset.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ t('dataset.confirm') }}</el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped>
.row-column-permissions {
  .explain {
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
    margin-left: 8px;
    color: #646a73;
  }

  .m16 {
    margin: 34px 0 16px 0;
  }

  .white-list {
    width: 100%;
  }

  .auth-type {
    width: 100%;
    .type {
      margin: 16px 0 11px 0;
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      color: #1f2329;
    }

    .target-objs {
      width: 100%;
      margin: 11px 0 8px 0;
    }
  }

  .between {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .relation-tree-container {
    border: 1px solid #e2e4e7;
    border-radius: 4px;
    max-height: 60vh;
    padding: 16px;
    overflow-x: auto;
  }
}
.role-add-name {
  .ed-select-dropdown__item {
    height: 50px;
    padding: 4px 11px 4px 11px;
    p {
      margin: 0;
      font-family: PingFang SC;
      font-weight: 400;
    }

    .name {
      font-size: 14px;
      line-height: 22px;
      color: #1f2329;
    }

    .email {
      font-size: 12px;
      line-height: 20px;
      color: #8f959e;
    }

    &.selected::after {
      top: 10px;
    }
  }
}
</style>
