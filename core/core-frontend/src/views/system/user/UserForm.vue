<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import useClipboard from 'vue-clipboard3'
import { ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { userCreateApi, userEditApi, roleOptionForUserApi, queryFormApi } from '@/api/user'
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
interface Tree {
  id: string
  name: string
  readonly: boolean
  children?: Tree[]
  disabled: boolean
  root: boolean
}

const { toClipboard } = useClipboard()
const { t } = useI18n()

const dialogVisible = ref(false)
const loading = ref(false)
const formType = ref('add')
const defaultPWD = ref('DataEase123456')

const createUserForm = ref<FormInstance>()

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
    label: '组织管理员',
    children: null,
    disabled: true
  },
  {
    value: 'readonly',
    label: '普通用户',
    children: null,
    disabled: true
  }
]
const groupBy = (list: Tree[]) => {
  const map = new Map()
  list.forEach(item => {
    const readonly = item.readonly
    let arr = map.get(readonly)
    if (!arr) {
      arr = []
    }
    arr.push({ value: item.id, label: item.name, disabled: false })
    map.set(readonly, arr)
  })
  return map
}
const copyInfo = async () => {
  try {
    await toClipboard(defaultPWD.value)
    ElMessage.success('复制成功')
  } catch (e) {
    ElMessage.warning('您的浏览器不支持复制：', e)
  }
}

const validateUsername = (_, value, callback) => {
  const pattern = '^[a-zA-Z][a-zA-Z0-9\._-]*$'
  const regep = new RegExp(pattern)
  if (!regep.test(value) && formType.value === 'add') {
    const msg = t('user.user_name_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}

/* const repeatValidator = (_, value, callback) => {
  if (value !== form.password) {
    callback(new Error(t('member.inconsistent_passwords')))
  } else {
    callback()
  }
} */
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
      message: t('user.id_mandatory'),
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
      message: t('user.name_mandatory'),
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
      message: t('user.email_mandatory'),
      trigger: 'blur'
    },
    {
      required: true,
      pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
      message: t('user.email_format_is_incorrect'),
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
  queryFormApi(uid).then(res => {
    state.form = reactive<UserForm>(res.data)
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      const param = { ...state.form }
      const method = formType.value === 'modify' ? userEditApi : userCreateApi
      method(param).then(res => {
        if (!res.msg) {
          ElMessage.success(t('common.save_success'))
          emits('saved')
          reset()
        }
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
defineExpose({
  init,
  edit,
  refreshRole
})
onMounted(() => {
  queryRole()
})
</script>

<template>
  <el-dialog
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    :title="formType === 'add' ? t('user.add_title') : t('user.edit_title')"
    width="840px"
  >
    <div v-if="formType === 'add'" class="editer-form-title">
      <el-icon>
        <Icon name="icon_warning_filled"></Icon>
      </el-icon>
      <span class="pwd">{{ $t('user.default_pwd') + '：' + defaultPWD }}</span>
      <el-button @click="copyInfo" class="btn-text" type="text">
        {{ $t('common.copy') }}
      </el-button>
    </div>
    <el-form
      ref="createUserForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('user.name')" prop="name">
            <el-input
              v-model="state.form.name"
              :placeholder="$t('common.please_input') + $t('user.name')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="t('common.account')" prop="account">
            <el-input
              v-model="state.form.account"
              :placeholder="$t('common.please_input') + $t('common.account')"
              :disabled="formType !== 'add'"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('common.email')" prop="email">
            <el-input
              v-model="state.form.email"
              :placeholder="$t('common.please_input') + $t('common.email')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('common.phone')" prop="phone">
            <el-input
              v-model="state.form.phone"
              :placeholder="$t('common.please_input') + $t('common.phone')"
              class="input-with-select"
            >
              <template #prepend>
                <el-select v-model="state.form.phonePrefix">
                  <el-option label="+86" value="+86" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

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
        <el-switch v-model="state.form.enable" />
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

<style lang="less" scoped>
.editer-form-title {
  width: 100%;
  border-radius: 4px;
  background: #e1eaff;
  padding: 9px 16px;
  display: flex;
  align-items: center;
  margin: -8px 0 16px 0;

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
  :deep(.ed-input-group__prepend) {
    background-color: #fff;
  }
  .ed-select {
    :deep(.ed-input__wrapper) {
      width: 72px;
    }
  }
}
</style>
