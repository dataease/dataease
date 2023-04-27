<script lang="ts" setup>
import { reactive, ref, nextTick, shallowRef, computed, provide } from 'vue'
import { GridTable } from '@/components/grid-table'
import { clone } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import RowAuth from './auth-tree/RowAuth.vue'
import { ElMessage } from 'element-plus-secondary'
interface Pagination {
  currentPage: number
  pageSize: number
  total: number
}

interface User {
  nickName: string
  userId: string
  email: string
}

interface RowForm {
  authTargetType: 'role' | 'user' | 'sysParams'
  authTargetId: string
  enable: boolean
  datasetId: string
  id?: string
  whiteListUser: User[]
}

const { t } = useI18n()

const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const rowPermissionForm = reactive<RowForm>({
  enable: false,
  authTargetType: 'user',
  whiteListUser: [],
  authTargetId: '',
  datasetId: ''
})

const state = reactive({
  rowList: []
})

const loadingRowPermission = ref(false)
const update_row_permission = ref(false)
const update_row_permission_dialog_title = ref('')
const rowAuth = ref()
const whiteListUsers = shallowRef<User[]>([])
const targetObjs = shallowRef<{ name: string; id: string }[]>([])
const emptyTips = computed(() => {
  return t('auth.select') + t(`auth.${rowPermissionForm.authTargetType}`)
})

provide('filedList', () => [])
provide('getAuthTargetType', () => rowPermissionForm.authTargetType)

const defaultForm = {
  authTargetId: '',
  authTargetType: 'user',
  datasetId: '',
  whiteListUser: [],
  enable: true
}

const typeList = ['role', 'user', 'sysParams']

const formatter = (_, __, cellValue) => {
  return cellValue ? t(`auth.${cellValue}`) : '-'
}
const formatterWhiteListUsers = (_, __, cellValue) => {
  return cellValue ? cellValue.map(ele => ele.nickName).join('、') : '-'
}

const onAuthTypeChange = () => {
  whiteListUsers.value = []
  rowPermissionForm.whiteListUser = []
  rowPermissionForm.authTargetId = ''
  fetchTypeObjsList()
}

const changeUserList = () => {
  rowPermissionForm.whiteListUser = []
  loadUserList()
}

const confirm = () => {
  rowAuth.value.submit()
}

const loadUserList = () => {
  whiteListUsers.value = []
  const { authTargetType, authTargetId } = rowPermissionForm
  if (authTargetType === 'user') return
  let url = `/api/user/userGrid/` + rowPermissionForm.datasetId
  let param = {}
  if (['role', 'dept'].includes(authTargetType)) {
    url = `/plugin/${authTargetType}/userGrid/` + rowPermissionForm.datasetId
    param = {
      [`${authTargetType}Id`]: authTargetId,
      section: 1
    }
  }
  console.log('param', param)
  whiteListUsers.value = []
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
    { ...clone(rowPermissionForm), whiteListUser: JSON.stringify(rowPermissionForm.whiteListUser) }
  params.expressionTree = JSON.stringify({ items, logic })
  clearData()
  search()
  loadingRowPermission.value = false
  console.log('params', params)
}

const clearData = () => {
  Object.assign(rowPermissionForm, clone(defaultForm))
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
  targetObjs.value = []
}

const search = () => {
  state.rowList = [
    {
      id: '5bc10619-d3cb-49dc-9a02-a69672c46393',
      enable: true,
      authTargetType: 'dept',
      authTargetId: 5,
      datasetId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      updateTime: 1682494599723,
      expressionTree:
        '{"items":[{"enumValue":[],"fieldId":"bcd8c5c5-2b66-49ba-93f2-9921f47c2c2d","filterType":"logic","term":"eq","value":"0","type":"item","subTree":null}],"logic":"or"}',
      whiteListUser: '[15]',
      whiteListRole: null,
      whiteListDept: null,
      datasetName: '0wjh4_chart_group',
      authTargetName: 'jinlong',
      tree: {
        logic: 'or',
        items: [
          {
            type: 'item',
            fieldId: 'bcd8c5c5-2b66-49ba-93f2-9921f47c2c2d',
            field: {
              id: 'bcd8c5c5-2b66-49ba-93f2-9921f47c2c2d',
              tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
              originName: 'id',
              name: 'ID',
              dataeaseName: 'C_b80bb7740288fda1f201890375a60c8f',
              groupType: 'd',
              type: 'VARCHAR',
              size: 50,
              deType: 0,
              deTypeFormat: null,
              deExtractType: 0,
              extField: 0,
              checked: true,
              columnIndex: 0,
              lastSyncTime: 1680165907179,
              accuracy: 0,
              dateFormat: null,
              dateFormatType: null
            },
            filterType: 'logic',
            term: 'eq',
            value: '0',
            enumValue: [],
            subTree: null
          }
        ]
      },
      whiteListUsers: [
        {
          userId: 15,
          deptId: 5,
          username: 'jinlong',
          nickName: 'jinlongaaaa',
          gender: '男',
          phone: null,
          email: 'jinlong@fit2cloud.com',
          password: 'ae8000252199d4f2aa00e3b99e6f9934',
          isAdmin: false,
          enabled: 1,
          createBy: null,
          updateBy: null,
          pwdResetTime: null,
          createTime: 1667372203575,
          updateTime: 1669645075432,
          language: 'zh_CN',
          from: 0,
          sub: null,
          phonePrefix: '+86'
        }
      ],
      whiteListRoles: null,
      whiteListDepts: null,
      authTargetIds: null
    }
  ]
}

search()

const create = rowPermissionObj => {
  if (!rowPermissionObj) {
    targetObjs.value = []
    Object.assign(rowPermissionForm, clone(defaultForm))
    update_row_permission_dialog_title.value = t('dataset.row_permission.add')
  } else {
    Object.assign(rowPermissionForm, rowPermissionObj)
    update_row_permission_dialog_title.value = t('dataset.row_permission.edit')
    listRowPermissions(rowPermissionObj)
    loadUserList()
  }
  fetchTypeObjsList()
  update_row_permission.value = true
}

const deleteRowPermission = () => {
  console.log('deleteRowPermission')
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
  nextTick(() => {
    rowAuth.value.init(tree || {})
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
  <GridTable
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
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

        <el-button @click="deleteRowPermission(scope.row)" text>
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
      <p class="type">{{ t('table.type') }}</p>
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
      <div class="title-form_primary m18">
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
            :key="item.userId + item.email"
            :label="item.nickName"
            :value="item.userId"
          >
            <p class="name">{{ item.nickName }}</p>
            <p class="email">{{ item.email }}</p>
          </el-option>
        </el-select>
      </div>
    </template>

    <template #footer>
      <el-button secondary @click="clearData">{{ t('fu.steps.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ t('fu.table.ok') }}</el-button>
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

  .m18 {
    margin: 34px 0 18px 0;
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
  .el-select-dropdown__item {
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
