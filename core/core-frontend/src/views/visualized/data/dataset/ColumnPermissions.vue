<script lang="ts" setup>
import { reactive, ref, nextTick, shallowRef, computed, onBeforeMount, watch, toRefs } from 'vue'
import { GridTable } from '@/components/grid-table'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { fieldEnums } from './options.js'
import type { Action } from 'element-plus-secondary'
import {
  listFieldByDatasetGroup,
  rowPermissionTargetObjList,
  whiteListUsersForPermissions,
  saveColumnPermission,
  columnPermissionList,
  deleteColumnPermission
} from '@/api/dataset'
interface Pagination {
  currentPage: number
  pageSize: number
  total: number
}

interface CurCol {
  id?: string
  name: string
  deType: number
  selected?: boolean
  opt: 'Desensitization' | 'Prohibit'
  desensitizationRule: {
    m: number
    n: number
    customBuiltInRule: 'RetainMToN' | 'RetainBeforeMAndAfterN'
    builtInRule:
      | 'custom'
      | 'KeepMiddleThreeCharacters'
      | 'KeepFirstAndLastThreeCharacters'
      | 'CompleteDesensitization'
  }
}

interface User {
  nickName: string
  userId: string
  email: string
}

interface Column {
  id: string
  authTargetType: string
  authTargetId: number
  datasetId: string
  updateTime: number
  permissions: CurCol[]
  whiteListUser: number[]
  datasetName: string
  authTargetName: string
  authTargetIds: string
  whiteListUsers: {
    userId: number
    deptId: number
    username: string
    nickName: string
    email: string
  }[]
}

interface ColumnForm {
  authTargetType: 'role' | 'user' | 'sysParams'
  authTargetId: string
  permissions: {
    enable: boolean
    columns: (CurCol & { selected?: boolean })[]
  }
  datasetId: number
  id?: string
  whiteListUser: User[]
}

const props = defineProps({
  datasetId: {
    required: false,
    default: 0,
    type: Number
  }
})

const { datasetId } = toRefs(props)

const { t } = useI18n()

const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const defaultForm: ColumnForm = {
  authTargetId: null,
  authTargetType: 'user',
  datasetId: 0,
  permissions: {
    enable: true,
    columns: []
  },
  whiteListUser: []
}

const defaultCol: CurCol = {
  opt: 'Prohibit',
  name: '',
  deType: 0,
  desensitizationRule: {
    m: 1,
    n: 1,
    customBuiltInRule: 'RetainMToN',
    builtInRule: 'custom'
  }
}
const columnPermissionForm = reactive<ColumnForm>(cloneDeep(defaultForm))

const state = reactive<{
  columnList: Column[]
  tableData: CurCol[]
}>({
  columnList: [],
  tableData: []
})

const loadingRowPermission = ref(false)
const update_column_permission = ref(false)
const update_column_permission_dialog_title = ref('')
const colKeywords = ref('')
const whiteListUsers = shallowRef<User[]>([])
const filedList = shallowRef([])
const checkAll = ref(false)
const numberM = ref()
const authTarge = ref(false)
const isIndeterminate = ref(false)
const selectedId = shallowRef([])
const curCol = reactive<CurCol>(cloneDeep(defaultCol))
const targetObjs = shallowRef<{ name: string; id: string }[]>([])
const emptyTips = computed(() => {
  return t('auth.select') + t(`auth.${columnPermissionForm.authTargetType}`)
})

const preview = computed(() => {
  const { customBuiltInRule = '', m = 1, n = 1 } = curCol?.desensitizationRule || {}
  if (customBuiltInRule === 'RetainMToN') {
    return [...Array(3).fill('*'), ...Array(n + 1 - m).fill('X'), '***'].join('')
  }
  if (customBuiltInRule === 'RetainBeforeMAndAfterN') {
    return [...Array(m).fill('X'), '***', ...Array(n).fill('X')].join('')
  }
  return ''
})

watch([columnPermissionForm.authTargetId], ([val]) => {
  if (val) {
    authTarge.value = false
  }
})

watch(colKeywords, val => {
  const tableData = columnPermissionForm.permissions.columns || []
  if (!val) {
    state.tableData = [...tableData]
  } else {
    state.tableData = tableData.filter(ele => ele.name.includes(val))
  }
})

onBeforeMount(() => {
  defaultForm.datasetId = props.datasetId
  initFieldLists()
  search()
})

const typeList = ['role', 'user']
const optRules = [
  {
    label: '******',
    value: 'CompleteDesensitization'
  },
  {
    label: 'XXX***XXX',
    value: 'KeepFirstAndLastThreeCharacters'
  },
  {
    label: '***XXX***',
    value: 'KeepMiddleThreeCharacters'
  },
  {
    label: t('commons.custom'),
    value: 'custom'
  }
]
const regionList = [
  {
    label: '保留第M至N位',
    value: 'RetainMToN'
  },
  {
    label: '保留前M位,后N位',
    value: 'RetainBeforeMAndAfterN'
  }
]

const formatter = (_, __, cellValue) => {
  return cellValue ? t(`auth.${cellValue}`) : '-'
}
const formatterWhiteListUsers = (_, __, cellValue) => {
  return cellValue ? cellValue.map(ele => ele.name).join('、') : '-'
}

const initFieldLists = () => {
  listFieldByDatasetGroup(datasetId.value).then(res => {
    filedList.value = res.data
  })
}

const validateAuthTarge = () => {
  authTarge.value = !columnPermissionForm.authTargetId
  return authTarge.value
}
const onTypeChange = () => {
  authTarge.value = false
  fetchTypeObjsList()
  whiteListUsers.value = []
  columnPermissionForm.whiteListUser = []
  columnPermissionForm.authTargetId = ''
}

const changeWhiteListUsers = () => {
  columnPermissionForm.whiteListUser = []
  loadWhiteUserList()
}

const loadWhiteUserList = () => {
  whiteListUsers.value = []
  const { authTargetType, authTargetId } = columnPermissionForm
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

const permissionInfo = item => {
  const params = {
    authTargetId: item.authTargetId,
    authTargetType: item.authTargetType
  }
}

const search = () => {
  columnPermissionList(
    paginationConfig.currentPage,
    paginationConfig.pageSize,
    datasetId.value
  ).then(res => {
    const columnList = res.data.records
    paginationConfig.total = res.data.total
    state.columnList = columnList.map(ele => {
      const item: Column = {
        ...ele,
        permissions: JSON.parse(ele.permissions),
        whiteListUser: JSON.parse(ele.whiteListUser)
      }
      permissionInfo(item)
      return item
    })
    paginationConfig.total = 1
  })
}

const create = permissionObj => {
  selectedId.value = []
  Object.assign(curCol, cloneDeep(defaultCol))
  if (!permissionObj) {
    targetObjs.value = []
    Object.assign(columnPermissionForm, cloneDeep(defaultForm))
    columnPermissionForm.datasetId = datasetId.value
    filedList.value.forEach(filed => {
      columnPermissionForm.permissions.columns.push({
        id: filed.id,
        name: filed.name,
        deType: filed.deType,
        opt: 'Prohibit',
        desensitizationRule: {
          builtInRule: 'CompleteDesensitization',
          customBuiltInRule: 'RetainMToN',
          m: 1,
          n: 1
        }
      })
    })
    update_column_permission_dialog_title.value = t('dataset.column_permission.add')
  } else {
    update_column_permission_dialog_title.value = t('dataset.column_permission.edit')
    Object.assign(columnPermissionForm, cloneDeep(permissionObj))

    let columnsPermissions = columnPermissionForm.permissions.columns
    columnPermissionForm.permissions.columns = []

    for (let i = 0; i < filedList.value.length; i++) {
      let item: CurCol & { selected?: boolean } = {
        id: filedList.value[i].id,
        name: filedList.value[i].name,
        deType: filedList.value[i].deType,
        opt: 'Prohibit',
        desensitizationRule: {
          builtInRule: 'CompleteDesensitization',
          customBuiltInRule: 'RetainMToN',
          m: 1,
          n: 1
        }
      }
      for (let j = 0; j < columnsPermissions.length; j++) {
        if (item.id === columnsPermissions[j].id) {
          item.selected = columnsPermissions[j].selected
          item.opt = columnsPermissions[j].opt
          if (item.selected) {
            selectedId.value.push(item.id)
          }
          if (columnsPermissions[j].desensitizationRule !== undefined) {
            item.desensitizationRule = columnsPermissions[j].desensitizationRule
          }
        }
      }
      columnPermissionForm.permissions.columns.push(item)
    }
  }
  fetchTypeObjsList()
  state.tableData = [...columnPermissionForm.permissions.columns]
  update_column_permission.value = true
  initSelect()
}

const initSelect = () => {
  handleSelectionChange(selectedId.value)
  const [fir] = selectedId.value
  const obj = state.tableData.find(ele => fir === ele.id)
  if (obj?.id) {
    selectCur(obj)
  }
}

const fetchTypeObjsList = () => {
  targetObjs.value = []
  const params = {
    authTargetId: columnPermissionForm.authTargetId,
    authTargetType: columnPermissionForm.authTargetType,
    datasetId: columnPermissionForm.datasetId
  }

  rowPermissionTargetObjList(
    columnPermissionForm.datasetId,
    columnPermissionForm.authTargetType
  ).then(res => {
    targetObjs.value = res.data
  })

  changeWhiteListUsers()
}
const closeDialog = () => {
  update_column_permission.value = false
  resetTaskForm()
}

const resetTaskForm = () => {
  Object.assign(columnPermissionForm, cloneDeep(defaultForm))
  Object.assign(curCol, cloneDeep(defaultCol))
  selectedId.value = []
  isIndeterminate.value = false
}

const deletePermission = item => {
  ElMessageBox.confirm(t('dataset.confirm_delete'), {
    confirmButtonText: t('commons.confirm'),
    tip: t('dataset.tips'),
    cancelButtonText: t('dataset.cancel'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === 'confirm') {
        deleteColumnPermission({ id: item.id }).then(res => {
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

const save = () => {
  if (validateAuthTarge()) return
  let params: Omit<ColumnForm, 'whiteListUser' | 'permissions'> & {
    permissions?: string
    whiteListUser?: string
  } = {
    ...cloneDeep(columnPermissionForm),
    whiteListUser: JSON.stringify(columnPermissionForm.whiteListUser),
    permissions: JSON.stringify(columnPermissionForm.permissions)
  }
  saveColumnPermission(params).then(res => {
    ElMessage({
      message: t('dataset.save_success'),
      type: 'success',
      showClose: true
    })
    search()
  })
  update_column_permission.value = false
  resetTaskForm()
}
const selectCur = ele => {
  Object.assign(curCol, ele)
  const { m = 1, n = 1 } = curCol.desensitizationRule
  curCol.desensitizationRule.m = m || 1
  curCol.desensitizationRule.n = n || 1
}
const handleCheckAllChange = val => {
  selectedId.value = val ? state.tableData.map(ele => ele.id) : []
  handleSelectionChange(selectedId.value)
  isIndeterminate.value = false
}
const handleSelectionChange = val => {
  state.tableData.forEach(filed => {
    filed.selected = val.includes(filed.id)
  })
  checkAll.value = val.length === state.tableData.length
  isIndeterminate.value = val.length > 0 && val.length < state.tableData.length
}
const regionChange = () => {
  const { customBuiltInRule, m, n } = curCol.desensitizationRule
  if (customBuiltInRule === 'RetainMToN' && m > n) {
    ElMessage({
      message: t('dataset.column_permission.mgtn'),
      type: 'error',
      showClose: true
    })
    curCol.desensitizationRule.m = n

    nextTick(() => {
      numberM.value.currentValue = n
    })
  }
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
    :table-data="state.columnList"
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

        <el-button @click="deletePermission(scope.row)" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"></Icon>
          </template>
        </el-button>
      </template>
    </el-table-column>
  </GridTable>
  <el-drawer
    :title="update_column_permission_dialog_title"
    custom-class="column-permissions"
    :wrapperClosable="false"
    :size="896"
    v-loading="loadingRowPermission"
    v-model="update_column_permission"
    direction="rtl"
    :before-close="closeDialog"
  >
    <div class="title-form_primary between">
      <span>{{ t('auth.column_permission') }}</span>
      <el-switch
        v-model="columnPermissionForm.permissions.enable"
        inactive-color="#BBBFC4"
        :active-text="t('auth.enable_column')"
      >
      </el-switch>
    </div>
    <div class="auth-type">
      <p class="type">{{ t('dataset.type') }}</p>
      <el-radio
        v-model="columnPermissionForm.authTargetType"
        @change="onTypeChange"
        v-for="ele in typeList"
        :key="ele"
        :label="ele"
        >{{ t(`auth.${ele}`) }}
      </el-radio>
      <el-select
        v-model="columnPermissionForm.authTargetId"
        @change="changeUserList"
        :placeholder="emptyTips"
        class="select-item target-objs"
      >
        <el-option v-for="item in targetObjs" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <div v-if="authTarge" style="top: 100px; left: 0" class="el-form-item__error">
        {{ emptyTips }}
      </div>
      <p class="type">{{ t('auth.set_rules') }}</p>
    </div>
    <div class="mrbt40">
      <div class="border-left" :class="[{ 'border-none': !curCol.id }]">
        <el-input
          clearable
          style="margin-bottom: 12px"
          v-model="colKeywords"
          :placeholder="t('auth.search_by_field')"
        ></el-input>
        <el-checkbox
          :indeterminate="isIndeterminate"
          v-model="checkAll"
          @change="handleCheckAllChange"
        >
          {{ t('dataset.check_all') }}
        </el-checkbox>
        <div style="margin-top: 12px" />
        <el-checkbox-group v-model="selectedId" @change="handleSelectionChange">
          <template :key="ele.id" v-for="ele in state.tableData">
            <div :class="[{ 'is-active': curCol.id === ele.id }]" @click="selectCur(ele)">
              <el-checkbox :label="ele.id">
                <el-icon>
                  <Icon
                    :name="`field_${fieldEnums[ele.deType]}`"
                    :class="`field-icon-${fieldEnums[ele.deType]}`"
                  ></Icon>
                </el-icon>

                {{ ele.name }}
              </el-checkbox>
            </div>
            <div style="margin-top: 12px" />
          </template>
        </el-checkbox-group>
      </div>
      <div class="border-right" v-if="!!curCol.id">
        <el-form label-width="80px">
          <el-form-item :label="t('commons.please_select')">
            <el-radio-group v-model="curCol.opt">
              <el-radio label="Prohibit">{{ t('dataset.column_permission.prohibit') }}</el-radio>
              <el-radio label="Desensitization">{{
                t('dataset.column_permission.desensitization')
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <template v-if="curCol.opt === 'Desensitization'">
            <el-form-item :label="t('dataset.column_permission.desensitization_rule')">
              <el-radio
                v-for="ele in optRules"
                :key="ele.label"
                v-model="curCol.desensitizationRule.builtInRule"
                :label="ele.value"
              >
                {{ ele.label }}
              </el-radio>
            </el-form-item>
            <template v-if="curCol.desensitizationRule.builtInRule === 'custom'">
              <el-form-item>
                <el-select
                  @change="regionChange"
                  v-model="curCol.desensitizationRule.customBuiltInRule"
                >
                  <el-option
                    :key="ele.value"
                    v-for="ele in regionList"
                    :label="ele.label"
                    :value="ele.value"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                {{ t('dataset.column_permission.m') }}
                <el-input-number
                  ref="numberM"
                  @change="regionChange"
                  v-model="curCol.desensitizationRule.m"
                  controls-position="right"
                  :min="1"
                  :max="20"
                ></el-input-number>
              </el-form-item>
              <el-form-item>
                {{ t('dataset.column_permission.n') }}
                <el-input-number
                  @change="regionChange"
                  v-model="curCol.desensitizationRule.n"
                  controls-position="right"
                  :min="1"
                  :max="20"
                ></el-input-number>
              </el-form-item>
              <el-form-item> {{ t('dataset.preview') }} {{ preview }} </el-form-item>
            </template>
          </template>
        </el-form>
      </div>
    </div>
    <template v-if="columnPermissionForm.authTargetType !== 'user'">
      <div class="title-form_primary">
        <span>{{ t('auth.white_list') }}</span>
        <span class="explain">{{ t('auth.white_user_not') }}</span>
      </div>
      <div>
        <el-select
          popper-class="role-add-name"
          multiple
          clearable
          style="width: 100%"
          v-model="columnPermissionForm.whiteListUser"
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
      <el-button secondary @click="closeDialog">{{ t('dataset.cancel') }}</el-button>
      <el-button type="primary" @click="save()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped>
.column-permissions {
  .white-list {
    width: 100%;
  }

  .auth-type {
    position: relative;
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

  .mrbt40 {
    margin-bottom: 40px;
    border: 1px solid var(--deBorderBase, #dcdfe6);
    border-radius: 4px;
    display: flex;
    justify-content: space-between;

    .border-left {
      flex: 1;
      padding: 24px;
      max-height: 490px;
      border-right: 1px solid var(--deBorderBase, #dcdfe6);
      overflow-y: auto;
    }

    .border-none {
      border: none !important;
    }

    .border-right {
      padding: 24px;
      width: 490px;
      min-width: 300px;
      border-left: 1px solid var(--deBorderBase, #dcdfe6);
      margin-left: -1px;
      overflow-y: auto;
    }

    .is-active {
      background-color: #f5f7fa;
    }
  }

  .between {
    justify-content: space-between;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
