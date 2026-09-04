<script lang="ts" setup>
import field_text from '@/assets/svg/field_text.svg'
import field_time from '@/assets/svg/field_time.svg'
import field_value from '@/assets/svg/field_value.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Icon } from '@/components/icon-custom'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import {
  batchDelApi,
  searchVariableApi,
  valueForVariable,
  valueSelectedForVariableApi,
  variableDeletelApi,
  variableValueCreateApi,
  variableValueDeletelApi,
  variableValueEditApi
} from '@/api/variable'
import VariableForm from './VariableForm.vue'
import {
  ElCol,
  ElDatePicker,
  ElDialog,
  ElFormItem,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElRow,
  type FormInstance,
  type FormRules
} from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { setColorName } from '@/utils/utils'
import { fieldEnums } from '../../../component/row-col-permission/dataset-row-permissions/options.js'
import { fieldType } from '@/utils/attr'

interface ValueForm {
  id?: string | number
  sysVariableId: string | number
  value: string
  valueDesc: string
  begin: string
  end: string
}
const selectedRoleId = ref('')
const selectedVariableType = ref('')
const selectedRoleName = ref('')
const selectedRoleRoot = ref(false)
const dialogVisible = ref(false)
const multipleTableRef = ref(null)
const valueForm = ref<FormInstance>()
const formType = ref('add')
const roleKeyword = ref('')
const keyword = ref('')
const roleFormRef = ref(null)
const { t } = useI18n()
const loading = ref(false)
const valueLoading = ref(false)
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root?: boolean
}

const valueRule = reactive<FormRules>({
  value: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.variable_value'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ],
  valueDesc: [
    {
      required: false,
      message: t('common.please_input') + t('common.empty') + t('system.variable_desc'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ]
})

const iconName = (data: Tree) => {
  if (data.type === 'text') {
    return field_text
  }
  if (data.type === 'num') {
    return field_value
  }
  if (data.type === 'time') {
    return field_time
  }
}

const iconClassName = (data: Tree) => {
  if (data.type === 'text') {
    return 'field-icon-text'
  }
  if (data.type === 'num') {
    return 'field-icon-value'
  }
  if (data.type === 'time') {
    return 'field-icon-time'
  }
}

const handleNodeClick = (data: Tree) => {
  if (data.disabled) {
    return
  }
  if (data.root) {
    return
  }
  selectedRoleId.value = data.id
  selectedVariableType.value = data.type
  selectedRoleName.value = data.name
  selectedRoleRoot.value = data.root
  if (data.type === 'text') {
    state.paginationConfig.currentPage = 1
    state.paginationConfig.pageSize = 10
    selectedSearch(data.id)
  } else {
    selectedVariableValue(data.id)
  }
}

const triggerFilterRole = () => {
  const value = roleKeyword.value
  state.roleData.forEach(roleGroup => {
    roleGroup.children?.forEach(data => {
      setColorName(data, value)
      data['hidden'] = value && !data.name.includes(value)
    })
  })
}

const initialValue = {
  id: null,
  sysVariableId: null,
  value: '',
  end: '',
  begin: ''
}

const state = reactive({
  form: reactive<ValueForm>(initialValue),
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  optionUserList: [],
  addedUserList: [],
  roleData: [],
  checkList: [],
  multipleSelection: []
})
const order = ref(null)
state.roleData = [
  {
    id: 'system',
    name: t('system.system_built_in_variable'),
    children: null,
    disabled: true,
    root: true
  },
  {
    id: 'custom',
    name: t('system.custom_variable'),
    children: null,
    disabled: true,
    root: false
  }
]

const selectedSearch = (sysVariableId?: string) => {
  const param = { sysVariableId, value: keyword.value }
  if (sysVariableId) {
    loading.value = true
    const page = state.paginationConfig.currentPage
    const limit = state.paginationConfig.pageSize
    valueSelectedForVariableApi(page, limit, param)
      .then(res => {
        if (res?.data?.total) {
          const records = res.data.records
          records.forEach(item => {
            setColorName(item, keyword.value, 'value', 'colorValue')
          })
          state.addedUserList = records
          state.paginationConfig.total = res.data.total
        } else {
          state.addedUserList = []
          state.paginationConfig.total = 0
        }
      })
      .finally(() => {
        loading.value = false
      })
  }
}

const selectedVariableValue = (sysVariableId?: string) => {
  if (sysVariableId) {
    valueForVariable(sysVariableId)
      .then(res => {
        if (res.data.length > 0) {
          Object.assign(state.form, res.data[0])
        } else {
          state.form.id = null
          state.form.begin = ''
          state.form.end = ''
          state.form.sysVariableId = selectedRoleId.value
        }
      })
      .finally(err => {
        ElMessage.error(err)
      })
  }
}
const variableSearch = (resolve, reject, type, data) => {
  loading.value = true
  state.roleData[0].children = [
    {
      name: t('system.account'),
      id: 'userId',
      root: true
    },
    {
      name: t('auth.sysParams_type.user_name'),
      id: 'userName',
      root: true
    },
    {
      name: t('commons.email'),
      id: 'userEmail',
      root: true
    },
    {
      name: t('auth.sysParams_type.user_phone'),
      id: 'userPhone',
      root: true
    }
  ]
  searchVariableApi({})
    .then(res => {
      const roles = res.data
      const map = groupBy(roles)
      state.roleData[1].children = map.get(false)
      loading.value = false
      if (selectedRoleId.value) {
        selectedRoleName.value = getNode()?.name
      }
      nextTick(() => {
        if (data !== undefined) {
          for (let i = 0; i < state.roleData[1].children.length; i++) {
            if (data.id === state.roleData[1].children[i].id) {
              handleNodeClick(state.roleData[1].children[i])
            }
          }
        }
      })

      resolve && resolve(res)
    })
    .catch(e => {
      loading.value = false
      reject && reject(e)
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
  if (row.root) {
    return
  }
  roleFormRef.value.edit(row.id)
}

const delHandler = row => {
  if (row.root) {
    return
  }
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' + t('system.delete_this_variable') + '</strong></br>',
    showClose: false
  }).then(() => {
    loading.value = true
    variableDeletelApi(row.id).then(() => {
      ElMessage.success(t('common.delete_success'))
      roleSaved('modify')
      if (selectedRoleId.value === row.id) {
        selectedRoleId.value = ''
        selectedRoleName.value = ''
        selectedRoleRoot.value = false
      }
    })
  })
}
const emits = defineEmits(['refresh', 'refresh-grid'])
const roleSaved = (type: string, data) => {
  variableSearch(null, null, type, data)
  emits('refresh')
  if (type === 'modify') {
    emits('refresh-grid')
  }
}

const getNode = () => {
  let result = null
  state.roleData.forEach(group => {
    const nodes = group['children']
    nodes?.forEach(node => {
      if (node.id === selectedRoleId.value) {
        result = node
      }
    })
  })
  return result
}

const saveValue = () => {
  const param = { ...state.form }
  const method = param.id === null ? variableValueCreateApi : variableValueEditApi
  valueLoading.value = true
  method(param)
    .then(res => {
      if (!res.msg) {
        state.form.id = res.data.id
        ElMessage.success(t('common.save_success'))
      }
      valueLoading.value = false
    })
    .catch(() => {
      valueLoading.value = false
    })
}

const filterSelected = () => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = 10
  selectedSearch(selectedRoleId.value)
}
const batchDelHandler = () => {
  ElMessageBox.confirm(
    t('components.to_delete_variable', [state.multipleSelection.map(ele => ele.value).join(',')]),
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
    selectedSearch(selectedRoleId.value)
  })
}

const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
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
  state.paginationConfig.currentPage = 1
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

const addValue = () => {
  dialogVisible.value = true
  formType.value = 'add'
  Object.assign(state.form, {})
  state.form.sysVariableId = selectedRoleId.value
}
const reset = () => {
  resetForm(valueForm.value)
}

const valueEdit = async item => {
  dialogVisible.value = true
  formType.value = 'edit'
  Object.assign(state.form, item)
}

const valueDelete = row => {
  ElMessageBox.confirm(t('role.confirm_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('dataset.cancel'),
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' + t('system.this_variable_value') + '</strong></br>',
    showClose: false
  }).then(() => {
    loading.value = true
    variableValueDeletelApi(row.id)
      .then(() => {
        ElMessage.success(t('common.delete_success'))
        loading.value = false
        state.paginationConfig.currentPage = 1
        state.paginationConfig.pageSize = 10
        selectedSearch(selectedRoleId.value)
      })
      .catch(() => {
        loading.value = false
      })
  })
}

const handleSelectionChange = rows => {
  state.multipleSelection = rows
}

const rowCheckStatus = row => {
  return row.id
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = formType.value === 'add' ? variableValueCreateApi : variableValueEditApi
      valueLoading.value = true
      method(param)
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            reset()
            selectedSearch(selectedRoleId.value)
          }
          valueLoading.value = false
        })
        .catch(() => {
          valueLoading.value = false
        })
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  state.form.id = null
  state.form.sysVariableId = null
  state.form.value = ''
  state.form.valueDesc = ''
  state.form.begin = ''
  state.form.end = ''
  dialogVisible.value = false
}

onMounted(() => {
  const p = new Promise((resolve, reject) => {
    variableSearch(resolve, reject)
  })
  p.then(() => {
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
})
</script>

<template>
  <div class="variable-manage" v-loading="loading">
    <div class="role-list role-height">
      <div class="title">
        <div class="text w100 flex-align-center">
          <span>{{ t('system.variable_list') }}</span>
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
              <Icon name="icon_search-outline_outlined"><icon_searchOutline_outlined /></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <el-scrollbar class="role-tree-container">
        <div v-for="(roleGroup, index) in state.roleData" :key="roleGroup.id">
          <div class="role-title text flex-align-center">
            <span>{{ roleGroup.name }}</span>
            <span class="icon-span">
              <el-tooltip
                effect="dark"
                v-if="roleGroup.id === 'custom' && !roleGroup.root"
                :content="t('system.add_variable')"
                placement="top"
              >
                <el-icon
                  @click="roleAdd"
                  style="color: #3370ff !important; font-size: 20px !important"
                  class="hover-icon_primary"
                >
                  <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
                </el-icon>
              </el-tooltip>
              <el-icon
                v-else-if="!roleGroup.root"
                @click="roleAdd"
                style="color: #3370ff !important; font-size: 20px !important"
                class="hover-icon_primary"
              >
                <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
              </el-icon>
            </span>
          </div>
          <div
            class="list-item_primary"
            :class="{
              'de-role-hidden': role.hidden,
              'de-is-active': selectedRoleId === role.id
            }"
            v-for="role in roleGroup.children"
            :key="role.id"
            @click.stop="handleNodeClick(role)"
          >
            <span class="flex-align-center label">
              <el-icon v-if="iconName(role)" :style="{ marginRight: '4px', fontSize: '16px' }">
                <Icon
                  ><component
                    class="svg-icon"
                    :class="iconClassName(role)"
                    :is="iconName(role)"
                  ></component
                ></Icon>
              </el-icon>
              <span v-if="role.colorName" v-html="role.colorName"> </span>
              <span v-else>
                {{ role.name }}
              </span>
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="t('system.system_built_in_variable')"
                placement="top"
                v-if="role.root"
              >
                <span class="mark flex-center">{{ t('role.system') }}</span>
              </el-tooltip>
            </span>
            <span v-if="!role.root" class="btn-list" :class="{ 'de-disabled-btn': role.root }">
              <el-tooltip
                class="box-item"
                effect="dark"
                :content="role.root ? t('role.system_role_edit_tips') : t('common.edit')"
                placement="top"
              >
                <el-icon @click.stop="roleEdit(role)" class="hover-icon">
                  <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                </el-icon>
              </el-tooltip>

              <el-tooltip
                class="box-item"
                effect="dark"
                :content="role.root ? t('role.system_role_del_tips') : t('common.delete')"
                placement="top"
              >
                <el-icon @click.stop="delHandler(role)" class="hover-icon">
                  <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
                </el-icon>
              </el-tooltip>
            </span>
          </div>
          <el-divider v-if="!index" />
        </div>
      </el-scrollbar>
    </div>
    <div
      class="added-user-list role-height"
      v-if="selectedRoleId && selectedVariableType == 'text'"
    >
      <div
        class="flex-align-center"
        style="font-size: 16px; font-weight: 500; line-height: 24px; margin-bottom: 16px"
      >
        <el-icon
          v-if="iconName({ type: selectedVariableType })"
          :style="{ marginRight: '4px', fontSize: '16px' }"
        >
          <Icon
            ><component
              class="svg-icon"
              :class="iconClassName({ type: selectedVariableType })"
              :is="iconName({ type: selectedVariableType })"
            ></component
          ></Icon> </el-icon
        >{{ selectedRoleName }}
      </div>
      <el-row>
        <el-col :span="12">
          <el-button @click="addValue" type="primary">
            {{ t('system.add_variable_value') }}
          </el-button>
        </el-col>
        <el-col :span="12" style="margin-bottom: 16px; text-align: right">
          <el-input
            style="width: 240px"
            v-model="keyword"
            clearable
            @change="filterSelected"
            :placeholder="t('system.search_variable_value')"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"><icon_searchOutline_outlined /></Icon>
              </el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <div class="user-table" :class="!!state.multipleSelection.length && 'user-table_multiple'">
        <GridTable
          ref="multipleTableRef"
          :pagination="state.paginationConfig"
          :table-data="state.addedUserList"
          @current-change="pageChange"
          @size-change="sizeChange"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="30" :selectable="rowCheckStatus" />
          <el-table-column
            key="value"
            show-overflow-tooltip
            prop="value"
            :label="t('system.variable_value')"
          >
            <template v-slot:default="scope">
              <span v-if="scope.row.colorValue" v-html="scope.row.colorValue" />
              <span v-else>{{ scope.row.value }}</span>
            </template>
          </el-table-column>
          <el-table-column
            key="valueDesc"
            show-overflow-tooltip
            prop="valueDesc"
            :label="t('system.variable_desc')"
          >
            <template v-slot:default="scope">
              {{ scope.row.valueDesc }}
            </template>
          </el-table-column>

          <el-table-column width="124" key="_operation" fixed="right" :label="$t('common.operate')">
            <template #default="scope">
              <div class="operate-icon-container">
                <el-tooltip effect="dark" :content="t('common.edit')" placement="top">
                  <el-button text @click="valueEdit(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined"><icon_edit_outlined /></Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
                  <el-button text @click="valueDelete(scope.row)">
                    <template #icon>
                      <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
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
      </div>
    </div>

    <div
      class="added-user-list role-height"
      v-if="selectedRoleId && selectedVariableType === 'num'"
    >
      <div
        class="flex-align-center"
        style="font-size: 16px; font-weight: 500; line-height: 24px; margin-bottom: 16px"
      >
        <el-icon
          v-if="iconName({ type: selectedVariableType })"
          :style="{ marginRight: '4px', fontSize: '16px' }"
        >
          <Icon
            ><component
              class="svg-icon"
              :class="iconClassName({ type: selectedVariableType })"
              :is="iconName({ type: selectedVariableType })"
            ></component></Icon></el-icon
        >{{ selectedRoleName }}
      </div>
      <el-row>
        <el-col style="font-size: 14px; line-height: 22px" :span="12">
          {{ t('system.set_variable_value') }}
        </el-col>
      </el-row>
      <div class="user-table">
        <el-form
          :v-loading="valueLoading"
          ref="numForm"
          require-asterisk-position="right"
          :model="state.form"
          class="variable-form"
          inline
          label-width="80px"
          label-position="top"
          :rules="valueRule"
          @keydown.stop.prevent.enter
        >
          <el-form-item prop="account">
            <el-input
              @change="saveValue"
              v-model="state.form.begin"
              :placeholder="t('system.the_minimum_value')"
            />
          </el-form-item>
          <el-form-item>
            <div class="line"></div>
          </el-form-item>
          <el-form-item prop="name">
            <el-input
              @change="saveValue"
              v-model="state.form.end"
              :placeholder="t('system.the_maximum_value')"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div
      :v-loading="valueLoading"
      class="added-user-list role-height"
      v-if="selectedRoleId && selectedVariableType == 'time'"
    >
      <div
        class="flex-align-center"
        style="font-size: 16px; font-weight: 500; line-height: 24px; margin-bottom: 16px"
      >
        <el-icon
          v-if="iconName({ type: selectedVariableType })"
          :style="{ marginRight: '4px', fontSize: '16px' }"
        >
          <Icon
            ><component
              class="svg-icon"
              :class="iconClassName({ type: selectedVariableType })"
              :is="iconName({ type: selectedVariableType })"
            ></component
          ></Icon> </el-icon
        >{{ selectedRoleName }}
      </div>
      <el-row>
        <el-col style="font-size: 14px; line-height: 22px" :span="12">
          {{ t('system.set_variable_value') }}
        </el-col>
      </el-row>
      <div class="user-table">
        <el-form
          ref="valueForm"
          require-asterisk-position="right"
          :model="state.form"
          class="variable-form"
          label-width="80px"
          inline
          label-position="top"
          :rules="valueRule"
        >
          <el-form-item prop="account">
            <el-date-picker
              @change="saveValue"
              v-model="state.form.begin"
              class="de-date-picker"
              :prefix-icon="calendar"
              type="datetime"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :placeholder="t('system.the_minimum_date')"
            />
          </el-form-item>
          <el-form-item>
            <div class="line"></div>
          </el-form-item>
          <el-form-item prop="name">
            <el-date-picker
              @change="saveValue"
              v-model="state.form.end"
              class="de-date-picker"
              :prefix-icon="calendar"
              type="datetime"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
              :placeholder="t('system.the_maximum_date')"
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <el-empty
      v-if="!selectedRoleId"
      class="added-user-list role-height"
      :description="t('system.on_the_left_p')"
    />
  </div>
  <variable-form ref="roleFormRef" @saved="roleSaved" />
  <el-dialog
    custom-class="value-form-dialog"
    :before-close="reset"
    v-model="dialogVisible"
    ref="valueForm"
    :title="formType === 'add' ? t('system.add_variable_value') : t('system.edit_variable_value')"
    :close-on-click-modal="false"
    width="840px"
    :v-loading="valueLoading"
  >
    <el-form
      ref="valueForm"
      require-asterisk-position="right"
      :model="state.form"
      class="user-edit-form"
      label-width="80px"
      label-position="top"
      :rules="valueRule"
      @keydown.stop.prevent.enter
    >
      <el-form-item :label="t('system.variable_value')" prop="value">
        <el-input v-model="state.form.value" :placeholder="t('data_set.enter_1_50_characters')" />
      </el-form-item>
      <el-form-item :label="t('system.variable_desc')" prop="valueDesc">
        <el-input
          v-model="state.form.valueDesc"
          :placeholder="t('data_set.enter_1_50_characters')"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(valueForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(valueForm)">
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.variable-manage {
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 8px) !important;
  box-sizing: border-box;
  margin-top: 8px;
  display: flex;
  border-radius: 12px;

  .variable-form {
    margin-top: 16px;

    :deep(.ed-form-item) {
      margin: 0;
    }

    :deep(.ed-input__wrapper),
    :deep(.ed-input) {
      width: 240px !important;
    }
  }

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

    .title {
      padding: 0 24px;
      padding-top: 24px;
      border-top-left-radius: 12px;
    }

    .m16 {
      margin: 16px 0;
    }
    .role-tree-container {
      height: calc(100% - 112px);
      .text {
        .icon-span {
          color: var(--ed-color-primary);
          margin: 0 16px 0 auto;
          font-size: 16px;

          .hover-icon_primary {
            font-weight: 500;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            cursor: pointer;
            height: 24px !important;
            width: 24px !important;
            border-radius: 4px;
            line-height: 1em;
            display: inline-flex;
            justify-content: center;
            align-items: center;
            color: rgb(51, 112, 255) !important;
            font-size: 20px !important;
            &:hover,
            &:active {
              background: #3370ff1a;
            }
          }
        }
      }
      .role-title {
        color: #8d9199;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        height: 40px;
        padding-left: 24px;
      }
      .de-role-hidden {
        display: none !important;
      }
      .de-is-active {
        background-color: var(--ed-menu-hover-bg-color) !important;
        color: var(--ed-menu-active-color) !important;
      }
      .list-item_primary {
        padding: 0 16px;
        margin: 0 7px;

        .label {
          line-height: 22px;
        }
        .mark {
          height: 16px;
          border-radius: 2px;
          margin-left: 8px;
          background-color: var(--ed-color-primary-light-7);
          color: var(--ed-menu-active-color);
          font-family: var(--de-custom_font, 'PingFang');
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
          font-family: var(--de-custom_font, 'PingFang');
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
          color: #1f2329;
          background: #1f23291a;
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
        margin: 4px 24px;
        width: calc(100% - 48px);
      }
    }
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: var(--de-custom_font, 'PingFang');
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

  .operate-icon-container {
    font-size: 16px;
    display: flex;

    .ed-button {
      width: 24px;
      height: 24px;
      line-height: 24px;
    }
  }
  .popper-max-width {
    .ed-popper.is-dark {
      white-space: pre-wrap;
      max-width: 300px;
    }
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
      font-family: var(--de-custom_font, 'PingFang');
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
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 10px;
        font-weight: 500;
        line-height: 13px;
        padding: 0 4px;
      }
    }

    .user-table {
      height: calc(100% - 64px);

      &.user-table_multiple {
        height: calc(100% - 135px);
      }
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

.line {
  width: 12px;
  background: #8f959e;
  height: 1px;
  margin: 15px 8px 0 8px;
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  height: 64px;
  width: calc(100% - 308px);
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
</style>

<style lang="less">
.menu-user-add_popper {
  margin-top: -10px !important;
  .ed-popper__arrow {
    display: none;
  }
}
</style>
