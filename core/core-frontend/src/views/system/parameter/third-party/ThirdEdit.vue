<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const dingtalkForm = ref<FormInstance>()
interface DingtalkForm {
  id?: string
  domain?: string
  valid?: boolean
  enabled?: boolean
}
const state = reactive({
  form: reactive<DingtalkForm>({
    id: null,
    domain: null
  })
})
const validateUrl = (_, value, callback) => {
  const reg = new RegExp(/(http|https):\/\/([\w.]+\/?)\S*/)
  if (!reg.test(value)) {
    callback(new Error(t('system.incorrect_please_re_enter')))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  id: [
    {
      required: true,
      message: t('common.the_application_id'),
      trigger: 'blur'
    }
  ],
  domain: [
    {
      required: true,
      message: t('common.enter_the_url'),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ]
})

const edit = ({ id, domain, valid, enabled }) => {
  state.form = {
    id,
    domain,
    valid,
    enabled
  }
  dialogVisible.value = true
}

const save = () => {
  const param = { ...state.form }
  const method = request.post({ url: '/sysParameter/sqlbot', data: param })
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

const saveClose = () => {
  const param = { ...state.form }
  const method = request.post({ url: '/sysParameter/sqlbot', data: param })
  showLoading()
  method
    .then(res => {
      if (!res.msg) {
        ElMessage.success(t('common.save_success'))
        emits('saved')
      }
      closeLoading()
    })
    .catch(() => {
      closeLoading()
    })
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      let url = `${
        state.form.domain.endsWith('/') ? state.form.domain : state.form.domain + '/'
      }api/v1/system/assistant/info/${state.form.id}`
      fetch(url)
        .then(response => response.json())
        .finally(() => {
          save()
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
  resetForm(dingtalkForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({
    target: '.platform-info-drawer'
  })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const validateHandlerOnly = () => {
  let url = `${
    state.form.domain.endsWith('/') ? state.form.domain : state.form.domain + '/'
  }api/v1/system/assistant/info/${state.form.id}`
  fetch(url)
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok')
      }
      return response.json()
    })
    .then(() => {
      state.form.valid = true
      ElMessage.success(t('datasource.validate_success'))
    })
    .catch(() => {
      ElMessage.error(t('data_source.verification_failed'))
      state.form.enabled = false
      state.form.valid = false
    })
    .finally(() => {
      saveClose()
    })
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('common.sqlbot_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="dingtalkForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="$t('common.sqlbot_server_url')" prop="domain">
        <el-input v-model="state.form.domain" :placeholder="t('common.enter_the_url')" />
      </el-form-item>
      <el-form-item :label="$t('common.application_id')" prop="id">
        <el-input v-model="state.form.id" :placeholder="t('common.the_application_id')" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(dingtalkForm)">{{ t('common.cancel') }}</el-button>
        <el-button
          secondary
          :disabled="!state.form.id || !state.form.domain"
          @click="validateHandlerOnly"
        >
          {{ t('commons.validate') }}
        </el-button>
        <el-button type="primary" @click="submitForm(dingtalkForm)">
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
