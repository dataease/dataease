<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const mappingTips = ref(t('system.for_example'))
const ldapForm = ref<FormInstance>()
interface LdapForm {
  addr?: string
  dn?: string
  pwd?: string
  ou?: string
  filter?: string
  mapping?: string
}
const state = reactive({
  form: reactive<LdapForm>({
    addr: '',
    dn: '',
    pwd: '',
    ou: '',
    filter: '',
    mapping: ''
  })
})
const validateMapping = (rule, value, callback) => {
  if (!value) {
    callback()
  }
  try {
    JSON.parse(value)
  } catch (e) {
    callback(new Error(t('system.in_json_format')))
  }
  callback()
}
const rule = reactive<FormRules>({
  addr: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.ldap_address'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    }
  ],
  dn: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.bind_dn'),
      trigger: ['blur', 'change']
    }
  ],
  pwd: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('common.pwd'),
      trigger: ['blur', 'change']
    }
  ],
  ou: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.user_ou'),
      trigger: ['blur', 'change']
    }
  ],
  filter: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.user_filter'),
      trigger: ['blur', 'change']
    }
  ],
  mapping: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.ldap_attribute_mapping'),
      trigger: ['blur', 'change']
    },
    { required: true, validator: validateMapping, trigger: 'blur' }
  ]
})

const edit = () => {
  showLoading()
  request
    .get({ url: '/setting/authentication/info/ldap' })
    .then(res => {
      const resData = res.data
      for (const key in resData) {
        state.form[key] = resData[key]
      }
    })
    .finally(() => {
      closeLoading()
    })
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = request.post({
        url: '/setting/authentication/save/ldap',
        data: param
      })
      showLoading()
      method
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
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
  resetForm(ldapForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({
    target: '.platform-info-drawer'
  })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const validate = () => {
  const url = '/setting/authentication/validate/ldap'
  const data = state.form
  showLoading()
  request
    .post({ url, data })
    .then(res => {
      if (res?.data === 'true') {
        ElMessage.success(t('commons.test_connect') + t('report.last_status_success'))
      } else {
        ElMessage.error(t('commons.test_connect') + t('report.last_status_fail') + ': ' + res.data)
      }
    })
    .finally(() => {
      closeLoading()
      emits('saved')
    })
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('system.ldap_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="ldapForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('system.ldap_address')" prop="addr">
        <el-input
          v-model="state.form.addr"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.such_as_ldap')"
        />
      </el-form-item>

      <el-form-item :label="t('system.bind_dn')" prop="dn">
        <el-input
          v-model="state.form.dn"
          :placeholder="t('common.please_input') + t('common.empty') + 'DN'"
        />
      </el-form-item>

      <el-form-item :label="t('common.pwd')" prop="pwd">
        <el-input
          v-model="state.form.pwd"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + t('common.pwd')"
        />
      </el-form-item>

      <el-form-item :label="t('system.user_ou')" prop="ou">
        <el-input
          v-model="state.form.ou"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.separate_each_ou')"
        />
      </el-form-item>

      <el-form-item :label="t('system.user_filter')" prop="filter">
        <el-input
          v-model="state.form.filter"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.such_as_uid')"
        />
      </el-form-item>

      <el-form-item :label="t('system.ldap_attribute_mapping')" prop="mapping">
        <el-input v-model="state.form.mapping" :placeholder="mappingTips" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(ldapForm)">{{ t('common.cancel') }}</el-button>
        <el-button secondary :disabled="!state.form.addr" @click="validate">
          {{ t('commons.test_connect') }}
        </el-button>
        <el-button type="primary" @click="submitForm(ldapForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style lang="less">
.platform-info-drawer {
  .ed-drawer__footer {
    height: 64px !important;
    padding: 16px 24px !important;
    .dialog-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;
  }
}
</style>
<style lang="less" scoped>
.platform-info-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
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
}
</style>
