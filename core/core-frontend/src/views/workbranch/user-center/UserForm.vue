<script lang="ts" setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { groupBy } from './options'
import { useUserStoreWithOut } from '@/store/modules/user'
import {
  userCreateApi,
  userEditApi,
  roleOptionForUserApi,
  queryFormApi,
  defaultPwdApi
} from '@/api/user'
interface UserForm {
  id?: string | number
  account: string
  name: string
  email?: string
  enable: boolean
  phone?: string | number
  phonePrefix: '+86'
  roleIds: string[]
}

const userStore = useUserStoreWithOut()
const curUid = computed(() => userStore.getUid)
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
    roleIds: []
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
  const pattern = '^[a-zA-Z0-9][a-zA-Z0-9\._-]*$'
  const regep = new RegExp(pattern)
  if (!regep.test(value) && formType.value === 'add') {
    const msg = t('user.user_name_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}

const validateNickname = (_, value, callback) => {
  const pattern =
    "[\\u00A0\\s\"`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“'。，、？]"
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
      max: 25,
      message: t('commons.input_limit', [1, 25]),
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
      min: 2,
      max: 50,
      message: t('commons.input_limit', [2, 50]),
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
      required: true,
      pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
      message: t('user.email_format_is_incorrect'),
      trigger: 'blur'
    }
  ],
  roleIds: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
})

const init = () => {
  formType.value = 'add'
  dialogVisible.value = true
}
const edit = uid => {
  formType.value = 'modify'
  dialogVisible.value = true
  queryForm(uid)
}
const queryForm = uid => {
  showLoading()
  queryFormApi(uid).then(res => {
    originName.value = res.name
    state.form = reactive<UserForm>(res.data)
    closeLoading()
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      const param = { ...state.form }
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
    } else {
      console.log('error submit!', fields)
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
    const map = groupBy(roles)
    state.roleList[0].children = map.get(false)
    state.roleList[1].children = map.get(true)
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
defineExpose({
  init,
  edit,
  refreshRole
})
onMounted(() => {
  queryRole()
  loadPwdInfo()
})
</script>

<template>
  <el-drawer
    :title="t('common.personal_info')"
    v-model="dialogVisible"
    custom-class="personal-info-drawer"
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
      <el-form-item :label="$t('user.name')" prop="name">
        <el-input
          v-model="state.form.name"
          :placeholder="$t('common.please_input') + $t('user.name')"
        />
      </el-form-item>

      <el-form-item label="ID" prop="account">
        <el-input
          v-model="state.form.account"
          :placeholder="$t('common.please_input') + $t('common.account')"
          :disabled="formType !== 'add'"
        />
      </el-form-item>

      <el-form-item :label="$t('common.email')" prop="email">
        <el-input
          v-model="state.form.email"
          :placeholder="$t('common.please_input') + $t('common.email')"
        />
      </el-form-item>

      <el-form-item :label="$t('commons.phone')" prop="phone">
        <el-input
          v-model="state.form.phone"
          :placeholder="$t('common.please_input') + $t('common.phone')"
          class="input-with-select"
        >
          <template #prepend> +86 </template>
        </el-input>
      </el-form-item>

      <el-form-item :label="$t('commons.organization')" prop="roleIds">
        <el-tree-select
          style="width: 100%"
          v-model="state.form.roleIds"
          :data="state.roleList"
          :highlight-current="true"
          multiple
          :render-after-expand="false"
          :placeholder="$t('common.please_select') + $t('commons.organization')"
          show-checkbox
          check-on-click-node
        />
      </el-form-item>

      <el-form-item :label="$t('user.role')" prop="roleIds">
        <el-tree-select
          style="width: 100%"
          v-model="state.form.roleIds"
          :data="state.roleList"
          :highlight-current="true"
          multiple
          :render-after-expand="false"
          :placeholder="$t('common.please_select') + $t('user.role')"
          show-checkbox
          check-on-click-node
        />
      </el-form-item>

      <el-form-item :label="$t('user.state')" prop="enabled">
        <el-switch :disabled="state.form.id === curUid" v-model="state.form.enable" />
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
</template>

<style lang="less">
.personal-info-drawer {
  .editer-form-title {
    width: 100%;
    border-radius: 4px;
    background: #e1eaff;
    margin: -8px 0 16px 0;
    height: 40px;
    padding-left: 16px;

    i {
      color: #3370ff;
      font-size: 14.666666030883789px;
    }

    .pwd {
      font-family: PingFang SC;
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
    .ed-input-group__prepend {
      width: 72px;
      background-color: #fff;
      padding: 0 20px;
      color: #1f2329;
      text-align: center;
      font-family: PingFang SC;
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }
  }
}
</style>
