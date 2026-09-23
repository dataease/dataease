<script lang="ts" setup>
import { ref, reactive, onMounted, computed, toRefs, h } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type {
  FormInstance,
  FormRules,
  ElMessageBox,
  ElForm,
  ElFormItem,
  ElInput
} from 'element-plus-secondary'
import { groupBy } from './options'
import { EMAIL_REGEX } from '@/utils/validate'
import { useUserStoreWithOut } from '@/store/modules/user'
import { propTypes } from '@/utils/propTypes'
import request from '@/config/axios'
import MfaStep from '../../component/login/MfaStep.vue'
import { userCreateApi, personEditApi, roleOptionForUserApi, personInfoApi } from '@/api/user'
interface UserForm {
  id?: string | number
  account: string
  name: string
  email?: string
  enable: boolean
  phone?: string | number
  phonePrefix: '+86'
  roleIds: string[]
  mfaEnable: boolean
}

const props = defineProps({
  globalMfaStatus: propTypes.number.def(0),
  userMfaBound: propTypes.bool.def(false)
})

const mfaSwitchDisable = computed(() => {
  return (
    state.form.mfaEnable &&
    (props.globalMfaStatus === 1 || (props.globalMfaStatus === 2 && userStore.getUid === '1'))
  )
})
const mfaSwitchTips = computed(() => {
  if (!state.form.mfaEnable || props.globalMfaStatus !== 1) return ''
  return t('setting_mfa.enable_switch_tips')
})
const bindVisible = ref(false)
const { userMfaBound } = toRefs(props)
const userStore = useUserStoreWithOut()
const curUid = computed(() => userStore.getUid)
const mfaData = computed(() => {
  return {
    enabled: state.form.mfaEnable,
    ready: userMfaBound.value,
    uid: curUid.value,
    origin: 0
  }
})
const { t } = useI18n()
const dialogVisible = ref(false)
const formType = ref('add')
const loadingInstance = ref(null)
const createUserForm = ref<FormInstance>()
const originName = ref(null)
const mfaForm = ref()
const unbindMsg = ref('')
const errorMfaCode = ref()
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
    mfaEnable: false
  }),
  mfaForm: reactive({
    code: ''
  })
})
state.roleList = [
  {
    value: 'admin',
    label: t('role.org_admin'),
    children: null,
    disabled: false
  },
  {
    value: 'readonly',
    label: t('role.average_role'),
    children: null,
    disabled: false
  }
]

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
const mfaRule = reactive<FormRules>({
  code: [
    {
      required: true,
      message: t('setting_mfa.code_input_msg', [6]),
      trigger: 'blur'
    },
    { pattern: /^\d{6}$/, message: t('setting_mfa.code_input_msg', [6]), trigger: 'blur' }
  ]
})
const rule = reactive<FormRules>({
  account: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('user.account'),
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
  mfaEnable: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('setting_mfa.user_enable'),
      trigger: 'blur'
    }
  ],
  name: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('user.name'),
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
      message: t('common.please_input') + t('common.empty') + t('common.email'),
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

const edit = () => {
  formType.value = 'modify'
  dialogVisible.value = true
  queryForm()
}
const queryForm = () => {
  showLoading()
  personInfoApi().then(res => {
    originName.value = res.name
    state.form = reactive<UserForm>(res.data)
    if (curUid.value === '1') {
      state.form['roleIds'] = []
    }
    closeLoading()
  })
}
const emits = defineEmits(['saved', 'refreshMfa'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = formType.value === 'modify' ? personEditApi : userCreateApi
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

const queryRole = () => {
  const param = {
    uid: state.form.id
  }
  roleOptionForUserApi(param).then(res => {
    const roles = res.data
    if (roles?.length) {
      const map = groupBy(roles)
      state.roleList[0].children = map.get(false)
      state.roleList[1].children = map.get(true)
    }
  })
}
const refreshRole = () => {
  queryRole()
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.user-form-dialog' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
const bindMfa = () => {
  bindVisible.value = true
  dialogVisible.value = false
}

const beforeMfaUnbindClose = async (action, instance, done) => {
  if (action !== 'confirm') {
    done()
    return
  }
  try {
    await mfaForm.value?.validate()
  } catch {
    return
  }
  const url = `/user/mfaUnbind/${state.mfaForm.code}`
  const res = await request.post({ url })
  const data = res.data
  if (!data || data === '0') {
    unbindMsg.value = ''
  } else {
    unbindMsg.value = data
  }
  if (unbindMsg.value) {
    errorMfaCode.value = state.mfaForm.code
  } else {
    errorMfaCode.value = ''
    refreshBind(false)
  }
  setCodeError()
  if (!unbindMsg.value) {
    done()
  }
}
const setCodeError = () => {
  const ruleArray: any[] = mfaRule.code as any[]
  const len = ruleArray.length
  if (!unbindMsg.value && len > 2) {
    ruleArray.splice(2, 1)
  } else if (unbindMsg.value && len > 2) {
    ruleArray[2]['message'] = unbindMsg.value
  } else if (unbindMsg.value && len === 2) {
    ruleArray.push({
      message: unbindMsg.value,
      validator: (rule, value, callback) => {
        if (value === errorMfaCode.value) {
          callback(new Error(rule.message))
        }
        callback()
      },
      trigger: 'blur'
    })
  }
  mfaForm.value?.validate()
}
const mfaUnbindCb = action => {
  if (action === 'confirm') {
    ElMessage.success(t('userCenter.unbind_success'))
  }
  state.mfaForm.code = ''
}
const updateCode = val => {
  state.mfaForm.code = val
}

const unbindMfa = () => {
  const confirmMsg = t('setting_mfa.unbind_confirm')
  const drawForm = () =>
    h('div', { class: 'unbind-tip-box-div' }, [
      h('p', null, confirmMsg),
      h(ElForm, { model: state.mfaForm, ref: mfaForm, rules: mfaRule, labelPosition: 'top' }, [
        h(ElFormItem, { label: t('setting_mfa.mfa_code'), prop: 'code' }, [
          h(ElInput, {
            modelValue: state.mfaForm.code,
            'onUpdate:modelValue': updateCode,
            placeholder: t('setting_mfa.code_input_msg', [6])
          })
        ])
      ])
    ])
  const boxOption = {
    confirmButtonType: 'danger',
    type: 'warning',
    confirmButtonText: t('commons.unbind'),
    cancelButtonText: t('dataset.cancel'),
    autofocus: false,
    showClose: false,
    dangerouslyUseHTMLString: true,
    beforeClose: beforeMfaUnbindClose,
    callback: mfaUnbindCb,
    message: drawForm
  }
  ElMessageBox.confirm('', boxOption)
}
const stepClose = () => {
  bindVisible.value = false
  dialogVisible.value = true
}
const refreshBind = val => {
  bindVisible.value = false
  dialogVisible.value = true
  userMfaBound.value = val
  emits('refreshMfa', val)
  if (val) {
    ElMessage.success(t('role.bind_success'))
  }
}
defineExpose({
  edit,
  refreshRole
})
onMounted(() => {
  queryRole()
})
</script>

<template>
  <el-drawer
    :title="t('common.personal_info')"
    v-model="dialogVisible"
    modal-class="personal-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="createUserForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('user.name')" prop="name">
        <el-input
          v-model="state.form.name"
          :placeholder="t('common.please_input') + t('common.empty') + t('user.name')"
        />
      </el-form-item>

      <el-form-item :label="t('user.account')" prop="account">
        <el-input
          v-model="state.form.account"
          :placeholder="t('common.please_input') + t('common.empty') + t('common.account')"
          disabled
        />
      </el-form-item>

      <el-form-item :label="t('common.email')" prop="email">
        <el-input
          v-model="state.form.email"
          :placeholder="t('common.please_input') + t('common.empty') + t('common.email')"
        />
      </el-form-item>

      <el-form-item :label="t('commons.phone')" prop="phone">
        <el-input
          v-model="state.form.phone"
          :placeholder="t('common.please_input') + t('common.empty') + t('common.phone')"
          class="input-with-select"
        >
          <template #prepend> +86 </template>
        </el-input>
      </el-form-item>

      <el-form-item :label="t('user.role')" prop="roleIds">
        <el-tree-select
          disabled
          style="width: 100%"
          v-model="state.form.roleIds"
          :data="state.roleList"
          :highlight-current="true"
          multiple
          :render-after-expand="false"
          :placeholder="t('common.please_select') + t('common.empty') + t('user.role')"
          show-checkbox
          check-on-click-node
        />
      </el-form-item>

      <el-form-item :label="t('user.state')" prop="enabled">
        <el-switch :disabled="state.form.id === curUid" v-model="state.form.enable" />
      </el-form-item>

      <el-form-item
        :label="t('setting_mfa.user_enable')"
        prop="mfaEnable"
        class="dynamic-form-label"
      >
        <template v-slot:label>
          <div class="user-form-info-tips">
            <span class="custom-form-item__label">{{ t('setting_mfa.user_enable') }}</span>
            <div class="mfa-bind-label">
              <div v-if="userMfaBound" class="mfa-bind is-active">
                <span>{{ t('setting_mfa.bind_ready') }}</span>
              </div>
              <div v-else class="mfa-bind">
                <span>{{ t('setting_mfa.bind_unready') }}</span>
              </div>
            </div>
            <div class="mfa-bind-btn">
              <el-button v-if="userMfaBound" @click="unbindMfa">{{
                t('commons.unbind')
              }}</el-button>
              <el-button v-else type="primary" @click="bindMfa">{{ t('commons.bind') }}</el-button>
            </div>
          </div>
        </template>

        <el-switch
          v-if="!mfaSwitchTips"
          :disabled="mfaSwitchDisable"
          v-model="state.form.mfaEnable"
        />
        <el-tooltip v-else class="box-item" effect="dark" :content="mfaSwitchTips" placement="top">
          <el-switch :disabled="mfaSwitchDisable" v-model="state.form.mfaEnable" />
        </el-tooltip>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(createUserForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(createUserForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
  <mfa-step
    v-if="bindVisible"
    :mfa-data="mfaData"
    :is-login="false"
    @close="stepClose"
    @refresh-bind="refreshBind"
  />
</template>

<style lang="less">
.unbind-tip-box-div {
  form {
    margin-top: 16px;
  }
  .ed-form-item {
    margin-bottom: 0px;
  }
  .is-error {
    margin-bottom: 24px !important;
  }
}
.personal-info-drawer {
  .input-with-select {
    .ed-input-group__prepend {
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
  .ed-drawer__footer {
    height: 64px;
    padding: 16px 24px;
  }
  .ed-drawer__body {
    padding: 24px;
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;

    .user-form-info-tips {
      width: fit-content;
      display: inline-flex;
      align-items: center;
      column-gap: 4px;
      .mfa-bind-label {
        display: flex;
        line-height: 22px;
        align-items: center;
        .label {
          color: #646a73;
        }
        .mfa-bind {
          margin-left: 4px;
          padding: 0 4px;
          line-height: 16px;
          border-radius: 2px;
          background-color: #1f23291a;
          color: #646a73;
          span {
            font-size: 10px;
          }
        }
        .is-active {
          background-color: #34c72433;
          color: #2ca91f;
        }
      }
      .mfa-bind-btn {
        position: fixed;
        right: 24px;
        button {
          width: 40px;
          height: 28px;
          min-width: 40px;
          span {
            line-height: 20px;
            font-size: 12px;
          }
        }
      }
    }
  }
  .dynamic-form-label {
    &.is-required.asterisk-right {
      .ed-form-item__label:after {
        display: none;
      }
      .user-form-info-tips {
        .custom-form-item__label:after {
          content: '*';
          color: var(--ed-color-danger);
          margin-left: 2px;
          font-family: var(--de-custom_font, 'PingFang');
          font-size: 14px;
          font-style: normal;
          font-weight: 400;
        }
      }
    }
  }
}
</style>
<style lang="less" scoped>
.personal-info-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
}
</style>
