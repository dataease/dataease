<script lang="ts" setup>
import { ref, reactive } from 'vue'
import useClipboard from 'vue-clipboard3'
import { ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'

interface UserAssist {
  wecomId: string
  dingtalkId: string
  larkId: string
}
interface UserForm {
  id?: string | number
  username: string
  password?: string
  nickName: string
  gender: '男' | '女'
  email?: string
  enabled: 1 | 0
  phone?: string | number
  phonePrefix: '+86'
  roleIds: number[]
  sysUserAssist: UserAssist
}
const { toClipboard } = useClipboard()
const { t } = useI18n()

const dialogVisible = ref(false)
const loading = ref(false)
const isPluginLoaded = ref(true)
const formType = ref('add')
const defaultPWD = ref('DataEase123..')

const createUserForm = ref<FormInstance>()
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
const roles = Array.from({ length: 10 }).map((_, idx) => ({
  name: `${idx + 1}`,
  id: `${idx + 1}`
}))

const repeatValidator = (_, value, callback) => {
  if (value !== form.password) {
    callback(new Error(t('member.inconsistent_passwords')))
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

const form = reactive<UserForm>({
  id: null,
  username: null,
  nickName: null,
  gender: '男',
  email: null,
  enabled: 1,
  phone: null,
  phonePrefix: '+86',
  roleIds: [2],
  sysUserAssist: {
    wecomId: null,
    dingtalkId: null,
    larkId: null
  }
})

const rule = reactive<FormRules>({
  username: [
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
  nickName: [
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
  ],
  password: [
    {
      required: true,
      message: t('user.input_password'),
      trigger: 'blur'
    },
    {
      required: true,
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
      message: t('member.password_format_is_incorrect'),
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      required: true,
      message: t('user.input_password'),
      trigger: 'blur'
    },
    { required: true, validator: repeatValidator, trigger: 'blur' }
  ],
  newPassword: [
    {
      required: true,
      message: t('user.input_password'),
      trigger: 'blur'
    },
    {
      required: true,
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
      message: t('member.password_format_is_incorrect'),
      trigger: 'blur'
    }
  ],
  gender: [],
  enabled: [{ required: true, trigger: 'change' }]
})

const init = () => {
  dialogVisible.value = true
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      console.log('submit!')
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
defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    title="Tips"
    width="840px"
  >
    <div v-if="formType === 'add'" class="editer-form-title">
      <el-icon>
        <Icon name="icon_warning_filled"></Icon>
      </el-icon>
      <span class="pwd">{{ $t('commons.default_pwd') + '：' + defaultPWD }}</span>
      <el-button @click="copyInfo" class="btn-text" type="text">
        {{ $t('commons.copy') }}
      </el-button>
    </div>
    <el-form
      ref="createUserForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.nick_name')" prop="nickName">
            <el-input v-model="form.nickName" :placeholder="$t('user.input_name')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="ID" prop="username">
            <el-input
              v-model="form.username"
              :placeholder="$t('user.input_id')"
              :disabled="formType !== 'add'"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.email')" prop="email">
            <el-input v-model="form.email" :placeholder="$t('user.input_email')" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('commons.mobile_phone_number')" prop="phone">
            <el-input
              v-model="form.phone"
              :placeholder="$t('commons.mobile_phone')"
              class="input-with-select"
            >
              <template #prepend>
                <el-select
                  v-model="form.phonePrefix"
                  :placeholder="$t('fu.search_bar.please_select')"
                >
                  <el-option label="+86" value="+86" />
                </el-select>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.gender')" prop="gender">
            <el-select
              v-model="form.gender"
              class="de-form-gender-select"
              :placeholder="$t('user.select_gender')"
            >
              <el-option :label="$t('commons.man')" value="男" />
              <el-option :label="$t('commons.woman')" value="女" />
              <el-option :label="$t('commons.keep_secret')" value="保密" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item v-show="isPluginLoaded" :label="$t('commons.role')" prop="roleIds">
        <el-select
          ref="roleSelect"
          v-model="form.roleIds"
          style="width: 100%"
          multiple
          filterable
          :placeholder="$t('user.input_roles')"
        >
          <el-option v-for="item in roles" :key="item.name" :label="item.name" :value="item.id" />
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('commons.status')" prop="enabled">
        <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(createUserForm)">Cancel</el-button>
        <el-button type="primary" @click="submitForm(createUserForm)"> Confirm </el-button>
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

  .pwd,
  .btn-text {
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

  .btn-text {
    padding: 0;
    border: none;
    height: 22px;
  }
}
.input-with-select {
  :deep(.el-input-group__prepend) {
    background-color: #fff;
  }
  .el-select {
    :deep(.el-input__wrapper) {
      width: 72px;
    }
  }
}
</style>
