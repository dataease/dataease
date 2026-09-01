<script lang="ts" setup>
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import icon_info_colorful from '@/assets/svg/icon_info_colorful.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { ref, reactive, onMounted, computed, onBeforeUnmount, h } from 'vue'
import useClipboard from 'vue-clipboard3'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import { EMAIL_REGEX } from '@/utils/validate'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  userCreateApi,
  userEditApi,
  roleOptionForUserApi,
  queryFormApi,
  defaultPwdApi
} from '@/api/user'
import { searchVariableApi, valueForVariable } from '@/api/variable'
import { searchApi as orgSearchApi } from '@/api/org'

interface UserForm {
  id?: string | number
  account: string
  name: string
  email?: string
  enable: boolean
  phone?: string | number
  phonePrefix: '+86'
  roleIds: string[]
  variables: Item[]
  defaultOid: string | number | null
}
export interface Item {
  variableId: string
  variableValue: string | number
  variableValueIds: []
  valueList: []
  sysVariableDto: SysVariableDto
}

export interface SysVariableDto {
  type: string
  max: number
  min: number
  startTime: string
  endTime: string
}

interface Org {
  id: string | number
  name: string
  children?: Org[]
}

import { Calendar } from '@element-plus/icons-vue'
const userStore = useUserStoreWithOut()
const curUid = computed(() => userStore.getUid)
const { toClipboard } = useClipboard()
const { t } = useI18n()
const dialogVisible = ref(false)
const formType = ref('add')
const defaultPWD = ref(null)
const loadingInstance = ref(null)
const createUserForm = ref<FormInstance>()
const originName = ref(null)
const state = reactive({
  roleList: [],
  form: reactive<UserForm>({
    id: null,
    account: null,
    name: null,
    email: null,
    enable: true,
    phone: null,
    phonePrefix: '+86',
    roleIds: [],
    variables: [],
    defaultOid: null
  }),
  variableList: [],
  orgTreeData: [] as Org[]
})
state.roleList = [
  {
    label: t('role.system_role'),
    options: []
  },
  {
    label: t('role.custom_role'),
    options: []
  }
]

const copyInfo = async () => {
  try {
    await toClipboard(defaultPWD.value)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}

const validateUsername = (_, value, callback) => {
  const pattern = '^[a-zA-Z0-9][a-zA-Z0-9\@._-]*$'
  const regep = new RegExp(pattern)
  if (!regep.test(value) && formType.value === 'add') {
    const msg = t('user.user_name_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}

const validateNickname = (_, value, callback) => {
  if (value.startsWith(' ') || value.endsWith(' ')) {
    const msg = t('user.special_characters_are_not_supported')
    callback(new Error(msg))
    return
  }
  const pattern =
    "[\\u00A0\"`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]"
  const regep = new RegExp(pattern)

  if (regep.test(value)) {
    const msg = t('user.special_characters_are_not_supported')
    callback(new Error(msg))
  } else {
    callback()
  }
}
const phoneRegex = (_, value, callback) => {
  if (!value || !`${value}`.trim()) {
    callback()
    return
  }
  const regep = new RegExp(/^1[3-9]\d{9}$/)

  if (!regep.test(value)) {
    const msg = t('user.phone_format')
    callback(new Error(msg))
  } else {
    callback()
  }
}

const rule = reactive<FormRules>({
  account: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    },
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  name: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    },
    { required: true, validator: validateNickname, trigger: 'blur' }
  ],
  phone: [
    {
      validator: phoneRegex,
      message: t('user.phone_format'),
      trigger: 'blur'
    }
  ],
  email: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    },
    {
      min: 5,
      max: 50,
      message: t('commons.input_limit', [5, 50]),
      trigger: 'blur'
    },
    {
      required: true,
      pattern: EMAIL_REGEX,
      message: t('user.email_format_is_incorrect'),
      trigger: 'blur'
    }
  ]
})

const init = () => {
  formType.value = 'add'
  const defaults: UserForm = {
    id: null,
    account: null,
    name: null,
    email: null,
    enable: true,
    phone: null,
    phonePrefix: '+86',
    roleIds: [],
    variables: [],
    defaultOid: null
  }
  Object.assign(state.form, defaults)
  dialogVisible.value = true
  variableList()
  loadOrgTree()
}
const edit = uid => {
  formType.value = 'modify'
  dialogVisible.value = true
  queryForm(uid)
  variableList()
  loadOrgTree()
}
const queryForm = uid => {
  showLoading()
  queryFormApi(uid).then(res => {
    originName.value = res.name
    const defaults: UserForm = {
      id: null,
      account: null,
      name: null,
      email: null,
      enable: true,
      phone: null,
      phonePrefix: '+86',
      roleIds: [],
      variables: [],
      defaultOid: null
    }
    Object.assign(state.form, defaults, res.data)
    for (let i = 0; i < state.form.variables.length; i++) {
      if (state.form.variables[i].variableType === 'text') {
        valueForVariable(state.form.variables[i].variableId).then(res => {
          state.form.variables[i].valueList = res.data
        })
      }
    }
    if (state.form.defaultOid) {
      queryRole(state.form.defaultOid)
    }
    closeLoading()
  })
}
const isIntegerString = str => {
  var pattern = /^[-+]?\d+$/
  return pattern.test(str)
}
const checkVariables = () => {
  for (let i = 0; i < state.form.variables.length; i++) {
    if (!state.form.variables[i].variableId || state.form.variables[i].variableId === '') {
      ElMessage.warning(t('user.cannot_be_empty'))
      return false
    }

    if (
      state.form.variables[i].sysVariableDto.type !== 'text' &&
      (!state.form.variables[i].variableValue || state.form.variables[i].variableValue === '')
    ) {
      ElMessage.warning(t('user.set_variable_value') + state.form.variables[i].sysVariableDto.name)
      return false
    }
    if (
      state.form.variables[i].sysVariableDto.type === 'text' &&
      (!state.form.variables[i].variableValueIds ||
        state.form.variables[i].variableValueIds.length === 0)
    ) {
      ElMessage.warning(t('user.set_variable_value') + state.form.variables[i].sysVariableDto.name)
      return false
    }
    if (state.form.variables[i].sysVariableDto.type === 'num') {
      if (!isIntegerString(state.form.variables[i].variableValue)) {
        ElMessage.warning(t('user.be_an_integer') + state.form.variables[i].sysVariableDto.name)
        return false
      }
      if (
        state.form.variables[i].sysVariableDto.min != null &&
        Number(state.form.variables[i].variableValue) < state.form.variables[i].sysVariableDto.min
      ) {
        ElMessage.warning(
          state.form.variables[i].sysVariableDto.name +
            t('user.be_less_than') +
            state.form.variables[i].sysVariableDto.min
        )
        return false
      }
      if (
        state.form.variables[i].sysVariableDto.max != null &&
        Number(state.form.variables[i].variableValue) > state.form.variables[i].sysVariableDto.max
      ) {
        ElMessage.warning(
          state.form.variables[i].sysVariableDto.name +
            t('user.be_greater_than') +
            state.form.variables[i].sysVariableDto.max
        )
        return false
      }
    }

    if (state.form.variables[i].sysVariableDto.type === 'time') {
      if (
        state.form.variables[i].sysVariableDto.startTime !== null &&
        state.form.variables[i].sysVariableDto.startTime !== '' &&
        new Date(state.form.variables[i].variableValue).getTime() <
          new Date(state.form.variables[i].sysVariableDto.startTime).getTime()
      ) {
        ElMessage.warning(
          state.form.variables[i].sysVariableDto.name +
            t('user.than_start_time') +
            state.form.variables[i].sysVariableDto.startTime
        )
        return false
      }
      if (
        state.form.variables[i].sysVariableDto.endTime !== null &&
        state.form.variables[i].sysVariableDto.endTime !== '' &&
        new Date(state.form.variables[i].variableValue).getTime() >
          new Date(state.form.variables[i].sysVariableDto.endTime).getTime()
      ) {
        ElMessage.warning(
          state.form.variables[i].sysVariableDto.name +
            t('user.than_end_time') +
            state.form.variables[i].sysVariableDto.endTime
        )
        return false
      }
    }
  }
  return true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      if (!checkVariables()) {
        return
      }
      const method = formType.value === 'modify' ? userEditApi : userCreateApi
      showLoading()
      method(param)
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            if (formType.value === 'modify' && state.form.id === curUid.value) {
              userStore.setUser()
            }
            emits('saved')
            reset()
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(createUserForm.value)
}

const queryRole = (oid?: number | string) => {
  const param: any = {}
  if (oid) {
    param.oid = oid
  }
  state.roleList[0].options = []
  state.roleList[1].options = []
  if (!oid) return
  roleOptionForUserApi(param).then(res => {
    const roles = res.data
    state.roleList[0].options = roles.filter(r => r.root)
    state.roleList[1].options = roles.filter(r => !r.root)
  })
}
const handleOrgChange = () => {
  state.form.roleIds = []
  queryRole(state.form.defaultOid as number)
}
const loadOrgTree = () => {
  orgSearchApi({}).then(res => {
    state.orgTreeData = res.data || []
  })
}
const refreshRole = () => {
  queryRole()
}
const loadPwdInfo = async () => {
  showLoading()
  defaultPwdApi().then(res => {
    defaultPWD.value = res.data
    closeLoading()
  })
}
const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.user-form-dialog' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    submitForm(createUserForm.value)
  }
}
const removeKeyDown = () => {
  window.removeEventListener('keydown', keyFunction)
}
const addKeyDown = () => {
  window.addEventListener('keydown', keyFunction)
}

const variableList = () => {
  searchVariableApi({})
    .then(res => {
      state.variableList = res.data
    })
    .catch(e => {})
}
const disabledOption = item => {
  const ids = state.form.variables.map(variable => {
    return variable.variableId
  })
  return ids.includes(item.id)
}

const variableChange = obj => {
  obj.variableValueId = ''
  obj.variableValueIds = []
  obj.variableValue = null
  obj.valueList = []
  obj.sysVariableDto = {
    type: 'text',
    max: null,
    min: -null,
    startTime: '',
    endTime: ''
  }
  if (obj.variableId != undefined && obj.variableId !== '') {
    state.variableList.forEach(item => {
      if (item.id === obj.variableId) {
        obj.sysVariableDto = item
      }
    })
    valueForVariable(obj.variableId)
      .then(res => {
        obj.valueList = res.data
        if (obj.sysVariableDto.type === 'num') {
          if (res.data.length > 0) {
            if (res.data[0].begin) {
              obj.sysVariableDto.min = res.data[0].begin
            }
            if (res.data[0].end) {
              obj.sysVariableDto.max = res.data[0].end
            }
          } else {
            obj.sysVariableDto.min = null
            obj.sysVariableDto.max = null
          }
        }
        if (obj.sysVariableDto.type === 'time' && res.data.length > 0) {
          if (res.data[0].begin) {
            obj.sysVariableDto.startTime = res.data[0].begin
          }
          if (res.data[0].end) {
            obj.sysVariableDto.endTime = res.data[0].end
          }
        }
      })
      .finally(err => {
        ElMessage.error(err)
      })
  }
}

const buildItemLabel = item => {
  if (item.valueDesc === '' || item.valueDesc === undefined || item.valueDesc === null) {
    return item.value
  } else {
    return item.value + '(' + item.valueDesc + ')'
  }
}

const remove = scope => {
  state.form.variables.splice(scope.$index, 1)
}

const addVariable = () => {
  if (state.form.variables === null || state.form.variables.length === 0) {
    state.form.variables = []
  }
  state.form.variables.push({
    variableId: '',
    variableValueId: '',
    valueList: [],
    sysVariableDto: {
      type: 'text',
      max: null,
      min: null,
      startTime: '',
      endTime: ''
    },
    variableValue: ''
  })
}
defineExpose({
  init,
  edit,
  refreshRole
})
onMounted(() => {
  queryRole()
  loadPwdInfo()
})
onBeforeUnmount(() => {
  removeKeyDown()
})
</script>

<template>
  <el-dialog
    modal-class="user-form-dialog"
    :before-close="reset"
    v-model="dialogVisible"
    :title="formType === 'add' ? t('user.add_title') : t('user.edit_title')"
    :close-on-click-modal="false"
    width="840px"
    @open="addKeyDown"
    @close="removeKeyDown"
  >
    <div v-if="formType === 'add'" class="editor-form-title flex-align-center">
      <el-icon>
        <Icon name="icon_info_colorful"><icon_info_colorful class="svg-icon" /></Icon>
      </el-icon>
      <span class="pwd">{{ $t('user.default_pwd') + '：' + defaultPWD }}</span>
      <el-button @click="copyInfo" text>
        {{ $t('common.copy') }}
      </el-button>
    </div>
    <el-form
      ref="createUserForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      class="user-edit-form"
      label-width="80px"
      label-position="top"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="t('common.account')" prop="account">
            <el-input
              v-model="state.form.account"
              :placeholder="`${$t('common.please_input')} ${$t('common.account')}`"
              :disabled="formType !== 'add'"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item :label="$t('user.name')" prop="name">
            <el-input
              v-model="state.form.name"
              :placeholder="`${$t('common.please_input')} ${$t('user.name')}`"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('common.email')" prop="email">
            <el-input
              v-model="state.form.email"
              :placeholder="$t('common.please_input') + ' ' + $t('common.email')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('common.phone')" prop="phone">
            <el-input
              v-model="state.form.phone"
              :placeholder="$t('common.please_input') + ' ' + $t('common.phone')"
              class="input-with-select"
            >
              <template #prepend> +86 </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item :label="t('auth.sysParams')">
        <div class="system-table_variable">
          <el-table
            v-show="state.form.variables && state.form.variables.length != 0"
            :data="state.form.variables"
            max-height="200"
            style="width: 100%"
          >
            <el-table-column :label="t('user.variable')" width="180">
              <template #default="scope">
                <el-select
                  v-model="scope.row.variableId"
                  :placeholder="t('auth.select')"
                  @change="variableChange(scope.row)"
                >
                  <el-option
                    v-for="item in state.variableList"
                    :key="item.name"
                    :label="item.name"
                    :disabled="disabledOption(item)"
                    :value="item.id"
                  >
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column :label="t('user.variable_value')" width="570">
              <template #default="scope">
                <el-select
                  v-model="scope.row.variableValueIds"
                  multiple
                  collapse-tags
                  v-if="scope.row.sysVariableDto.type === 'text'"
                  :placeholder="t('auth.select')"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in scope.row.valueList"
                    :key="item.value"
                    :label="buildItemLabel(item)"
                    :value="item.id"
                  >
                  </el-option>
                </el-select>
                <el-input
                  v-if="scope.row.sysVariableDto.type === 'num'"
                  v-model="scope.row.variableValue"
                  controls-position="right"
                  autocomplete="off"
                  :placeholder="t('user.enter_a_value')"
                  style="width: 100%"
                />
                <el-date-picker
                  v-if="scope.row.sysVariableDto.type === 'time'"
                  v-model="scope.row.variableValue"
                  class="de-date-picker"
                  :prefix-icon="Calendar"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="t('auth.select')"
                  style="width: 100%"
                />
              </template>
            </el-table-column>

            <el-table-column width="52">
              <template #default="scope">
                <el-icon @click="remove(scope)" class="hover-icon">
                  <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
                </el-icon>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div style="width: 100%">
          <el-button @click="addVariable" text>
            <template #icon>
              <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
            </template>
            {{ t('visualization.add_param') }}
          </el-button>
        </div>
      </el-form-item>

      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('sysuser.org')">
            <el-tree-select
              v-model="state.form.defaultOid"
              :data="state.orgTreeData"
              :props="{ value: 'id', label: 'name' }"
              check-strictly
              clearable
              :render-after-expand="false"
              @change="handleOrgChange"
              :placeholder="$t('common.please_select') + $t('sysuser.org')"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('user.role')">
            <el-select
              v-model="state.form.roleIds"
              multiple
              filterable
              :disabled="!state.form.defaultOid"
              :placeholder="
                state.form.defaultOid
                  ? $t('common.please_select') + $t('user.role')
                  : $t('org.select_org_first')
              "
              style="width: 100%"
            >
              <el-option-group
                v-for="group in state.roleList"
                :key="group.label"
                :label="group.label"
              >
                <el-option
                  v-for="role in group.options"
                  :key="role.id"
                  :value="role.id"
                  :label="role.name"
                >
                  <span>{{ role.name }}</span>
                  <span v-if="role.root" class="role-mark">{{ $t('role.system') }}</span>
                  <span v-else class="role-mark-de">{{
                    role.typeCode === 0
                      ? $t('role.staff')
                      : role.typeCode === 7
                      ? $t('role.analyst')
                      : $t('role.manager')
                  }}</span>
                </el-option>
              </el-option-group>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="switch-label-mask" />
      <el-form-item :label="$t('user.state')" class="user-switch-label" prop="enabled">
        <el-switch :disabled="state.form.id === curUid" v-model="state.form.enable" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(createUserForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(createUserForm)">
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.user-form-dialog {
  .ed-dialog__body {
    padding-bottom: 0px !important;
  }

  .system-table_variable {
    max-height: calc(100vh - 500px);
    .de-date-picker {
      .ed-input__wrapper {
        width: 100%;
        .clear-icon {
          position: absolute !important;
          transform: translateX(-24px);
        }
      }
      .ed-form-item__content {
        display: inline;
      }
    }
  }
}
</style>

<style lang="less" scoped>
.user-edit-form {
  :deep(.ed-form-item) {
    margin-bottom: 16px;
  }
  :deep(.is-error) {
    margin-bottom: 40px !important;
  }
  .user-switch-label {
    :deep(.ed-switch) {
      height: 20px;
    }
  }
}
.switch-label-mask {
  position: absolute;
  width: calc(100% - 48px);
  height: 30px;
}
.editor-form-title {
  width: 100%;
  border-radius: 4px;
  background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  margin-bottom: 16px;
  height: 40px;
  padding-left: 16px;

  i {
    color: var(--ed-color-primary);
    font-size: 14.666666030883789px;
  }

  .pwd {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    text-align: left;
  }

  .pwd {
    margin: 0 8px;
    color: #1f2329;
  }
}
.input-with-select {
  :deep(.ed-input-group__prepend) {
    width: 72px;
    background-color: #fff;
    padding: 0 20px;
    color: #1f2329;
    text-align: center;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
  }
}

.role-mark {
  height: 16px;
  border-radius: 2px;
  margin-left: 8px;
  background-color: var(--ed-color-primary-33, #3370ff33);
  color: var(--ed-menu-active-color);
  font-size: 10px;
  font-weight: 500;
  line-height: 13px;
  padding: 0 4px;
}
.role-mark-de {
  height: 16px;
  border-radius: 2px;
  margin-left: 8px;
  background-color: rgb(232 233 233);
  color: #646a73;
  font-size: 10px;
  font-weight: 500;
  line-height: 13px;
  padding: 0 4px;
}
</style>
