<script lang="ts" setup>
import { reactive, ref, nextTick, shallowRef, computed, onBeforeMount, watch } from 'vue'
import { GridTable } from '@/components/grid-table'
import { clone } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { fieldEnums } from './options.js'
import type { Action } from 'element-plus-secondary'
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
  datasetId: string
  id?: string
  whiteListUser: User[]
}

const props = defineProps({
  id: {
    type: String,
    default: ''
  }
})

const { t } = useI18n()

const paginationConfig = reactive<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const defaultForm: ColumnForm = {
  authTargetId: null,
  authTargetType: 'user',
  datasetId: '',
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
const columnPermissionForm = reactive<ColumnForm>(clone(defaultForm))

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
const curCol = reactive<CurCol>(clone(defaultCol))
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
  defaultForm.datasetId = props.id
  initFieldLists()
  search()
})

const typeList = ['role', 'user', 'sysParams']
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
  return cellValue ? cellValue.map(ele => ele.nickName).join('、') : '-'
}

const initFieldLists = () => {
  filedList.value = [
    {
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
    {
      id: '5456ec09-292f-494d-a319-19fcc854f786',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'name',
      name: '名称',
      dataeaseName: 'C_b068931cc450442b63f5b3d276ea4297',
      groupType: 'd',
      type: 'VARCHAR',
      size: 64,
      deType: 0,
      deTypeFormat: null,
      deExtractType: 0,
      extField: 0,
      checked: true,
      columnIndex: 1,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    },
    {
      id: '4e7c4677-5c4b-493e-91c1-de4bf1c82128',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'pid',
      name: '父级ID',
      dataeaseName: 'C_0db3209e1adc6d67be435a81baf9a66e',
      groupType: 'd',
      type: 'VARCHAR',
      size: 50,
      deType: 0,
      deTypeFormat: null,
      deExtractType: 0,
      extField: 0,
      checked: true,
      columnIndex: 2,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    },
    {
      id: 'cf3deae9-f6bd-45f0-8900-2f3983d389b8',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'level',
      name: '当前分组处于第几级',
      dataeaseName: 'C_c9e9a848920877e76685b2e4e76de38d',
      groupType: 'q',
      type: 'INT',
      size: 10,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 3,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    },
    {
      id: '290b6a7d-5e87-4946-b248-6f4bf566e055',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'type',
      name: 'group or scene',
      dataeaseName: 'C_599dcce2998a6b40b1e38e8c6006cb0a',
      groupType: 'd',
      type: 'VARCHAR',
      size: 50,
      deType: 0,
      deTypeFormat: null,
      deExtractType: 0,
      extField: 0,
      checked: true,
      columnIndex: 4,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    },
    {
      id: '7a0957dd-2eaf-4af2-b42a-134bdb6736ab',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'create_by',
      name: '创建人ID',
      dataeaseName: 'C_2293ea954bc29b88e7d4b512250b0a44',
      groupType: 'd',
      type: 'VARCHAR',
      size: 50,
      deType: 0,
      deTypeFormat: null,
      deExtractType: 0,
      extField: 0,
      checked: true,
      columnIndex: 5,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    },
    {
      id: '6092698f-1c2b-4d74-9056-d23ed97db076',
      tableId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      originName: 'create_time',
      name: '创建时间',
      dataeaseName: 'C_b16a626598674ad426e4b885017906d7',
      groupType: 'q',
      type: 'BIGINT',
      size: 19,
      deType: 2,
      deTypeFormat: null,
      deExtractType: 2,
      extField: 0,
      checked: true,
      columnIndex: 6,
      lastSyncTime: 1680165907179,
      accuracy: 0,
      dateFormat: null,
      dateFormatType: null
    }
  ]
}

const validateAuthTarge = () => {
  authTarge.value = !columnPermissionForm.authTargetId
  return authTarge.value
}
const onTypeChange = () => {
  authTarge.value = false
  fetchTypeList()
  whiteListUsers.value = []
  columnPermissionForm.whiteListUser = []
  columnPermissionForm.authTargetId = ''
}

const changeUserList = () => {
  columnPermissionForm.whiteListUser = []
  loadUserList()
}

const loadUserList = () => {
  whiteListUsers.value = []
  const { authTargetType, authTargetId } = columnPermissionForm
  if (authTargetType === 'user') return
  let url = `/api/user/userGrid/` + columnPermissionForm.datasetId
  let param = {}
  if (['role', 'dept'].includes(authTargetType)) {
    url = `/plugin/${authTargetType}/userGrid/` + columnPermissionForm.datasetId
    param = {
      [`${authTargetType}Id`]: authTargetId,
      section: 1
    }
  }
  console.log('param', param)
  whiteListUsers.value = []
}

const permissionInfo = item => {
  const params = {
    authTargetId: item.authTargetId,
    authTargetType: item.authTargetType
  }
  item.authTargetName = ''
}

const search = () => {
  const columnList = [
    {
      id: '19db717b-ec53-47ba-84d7-be8e3437bbba',
      authTargetType: 'role',
      authTargetId: 2,
      datasetId: '91b6555d-0c06-4ff5-881b-4a6c0cf710fe',
      updateTime: 1682647270408,
      permissions:
        '{"enable":true,"columns":[{"id":"bcd8c5c5-2b66-49ba-93f2-9921f47c2c2d","name":"ID","deType":0,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":false},{"id":"5456ec09-292f-494d-a319-19fcc854f786","name":"名称","deType":0,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":true},{"id":"4e7c4677-5c4b-493e-91c1-de4bf1c82128","name":"父级ID","deType":0,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":false},{"id":"cf3deae9-f6bd-45f0-8900-2f3983d389b8","name":"当前分组处于第几级","deType":2,"opt":"Desensitization","desensitizationRule":{"builtInRule":"custom","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":false},{"id":"290b6a7d-5e87-4946-b248-6f4bf566e055","name":"group or scene","deType":0,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":false},{"id":"7a0957dd-2eaf-4af2-b42a-134bdb6736ab","name":"创建人ID","deType":0,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":true},{"id":"6092698f-1c2b-4d74-9056-d23ed97db076","name":"创建时间","deType":2,"opt":"Prohibit","desensitizationRule":{"builtInRule":"CompleteDesensitization","customBuiltInRule":"RetainMToN","m":1,"n":1},"selected":true}]}',
      whiteListUser: '[18]',
      datasetName: '0wjh4_chart_group',
      authTargetName: null,
      authTargetIds: null,
      whiteListUsers: [
        {
          userId: 18,
          deptId: 1,
          username: 'zhourunfa',
          nickName: '周润发',
          email: 'zhourunfa@qq.ciom'
        }
      ]
    }
  ]
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
}

search()

const create = permissionObj => {
  selectedId.value = []
  Object.assign(curCol, clone(defaultCol))
  if (!permissionObj) {
    targetObjs.value = []
    Object.assign(columnPermissionForm, clone(defaultForm))
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
    Object.assign(columnPermissionForm, clone(permissionObj))

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
  fetchTypeList()
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

const fetchTypeList = () => {
  targetObjs.value = []
  const params = {
    authTargetId: columnPermissionForm.authTargetId,
    authTargetType: columnPermissionForm.authTargetType,
    datasetId: columnPermissionForm.datasetId
  }
  targetObjs.value = []
  loadUserList()
}
const closeDialog = () => {
  update_column_permission.value = false
  resetTaskForm()
}

const resetTaskForm = () => {
  Object.assign(columnPermissionForm, clone(defaultForm))
  Object.assign(curCol, clone(defaultCol))
  selectedId.value = []
  isIndeterminate.value = false
}

const deletePermission = item => {
  console.log(item)
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
        ElMessage({
          message: t('dataset.delete_success'),
          type: 'success',
          showClose: true
        })
        search()
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
    ...clone(columnPermissionForm),
    whiteListUser: JSON.stringify(columnPermissionForm.whiteListUser),
    permissions: JSON.stringify(columnPermissionForm.permissions)
  }
  console.log(params)
  ElMessage({
    message: t('dataset.save_success'),
    type: 'success',
    showClose: true
  })
  update_column_permission.value = false
  resetTaskForm()
  search()
}
const selectCur = ele => {
  console.log('ele', ele)

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
  <GridTable
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
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
      <p class="type">{{ t('table.type') }}</p>
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
