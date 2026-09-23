<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const larksuiteForm = ref<FormInstance>()
interface LarksuiteForm {
  appId?: string
  appSecret?: string
  callBack?: string
  enable?: string
  valid?: string
}
const state = reactive({
  form: reactive<LarksuiteForm>({
    appId: null,
    appSecret: null,
    callBack: null,
    enable: 'false',
    valid: 'false'
  })
})
const validateUrl = (rule, value, callback) => {
  const reg = new RegExp(/(http|https):\/\/([\w.]+\/?)\S*/)
  if (!reg.test(value)) {
    callback(new Error(t('system.incorrect_please_re_enter')))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  appId: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'APP Key',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 20,
      message: t('commons.input_limit', [5, 20]),
      trigger: 'blur'
    }
  ],
  appSecret: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'APP Secret',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 50,
      message: t('commons.input_limit', [5, 50]),
      trigger: 'blur'
    }
  ],
  callBack: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.callback_domain_name'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 100,
      message: t('commons.input_limit', [10, 100]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  enable: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'change'
    }
  ],
  valid: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'change'
    }
  ]
})

const edit = row => {
  state.form = {
    appId: row.appId,
    appSecret: row.appSecret,
    callBack: row.callBack,
    enable: row.enable,
    valid: row.valid
  }
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = request.post({ url: '/larksuite/create', data: param })
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
  resetForm(larksuiteForm.value)
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
  const url = '/larksuite/validate'
  const data = state.form
  showLoading()
  request
    .post({ url, data })
    .then(() => {
      state.form.valid = 'true'
      ElMessage.success(t('datasource.validate_success'))
    })
    .catch(() => {
      state.form.enable = 'false'
      state.form.valid = 'false'
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
    :title="t('system.international_feishu_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    destroy-on-close
    direction="rtl"
  >
    <el-form
      ref="larksuiteForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="APP Key" prop="appId">
        <el-input
          v-model="state.form.appId"
          :placeholder="t('common.please_input') + t('common.empty') + 'APP Key'"
        />
      </el-form-item>

      <el-form-item label="APP Secret" prop="appSecret">
        <el-input
          v-model="state.form.appSecret"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + 'APP Secret'"
        />
      </el-form-item>
      <el-form-item :label="t('system.callback_domain_name')" prop="callBack">
        <el-input
          v-model="state.form.callBack"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.callback_domain_name')
          "
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(larksuiteForm)">{{ t('common.cancel') }}</el-button>
        <el-button
          secondary
          :disabled="!state.form.appId || !state.form.appSecret || !state.form.callBack"
          @click="validate"
        >
          {{ t('commons.validate') }}
        </el-button>
        <el-button type="primary" @click="submitForm(larksuiteForm)">
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
