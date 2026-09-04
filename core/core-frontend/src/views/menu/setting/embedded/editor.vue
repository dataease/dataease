<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const embeddedForm = ref<FormInstance>()
interface EmbeddedForm {
  id?: string
  name?: string
  domain?: string
  secretLength: number
}
const state = reactive({
  form: reactive<EmbeddedForm>({
    id: null,
    name: null,
    domain: null,
    secretLength: 16
  })
})
const formType = ref('add')
const originSecretLen = ref()
const validateUrl = (rule, value, callback) => {
  const reg = new RegExp(/(http|https):\/\/([\w.]+\/?)\S*/)
  if (!reg.test(value)) {
    callback(new Error(t('system.wrong_please_re_enter')))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  name: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.application_name'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ],
  domain: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.cross_domain_settings'),
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
  secretLength: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.secret_length'),
      trigger: 'blur'
    }
  ]
})

const edit = data => {
  if (!data?.id) {
    add()
    return
  }
  state.form = {
    id: data.id,
    name: data.name,
    domain: data.domain,
    secretLength: data.secretLength || 16
  }
  originSecretLen.value = data.secretLength
  formType.value = 'edit'
  dialogVisible.value = true
}
const add = () => {
  state.form = {
    secretLength: 16
  }
  formType.value = 'add'
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      if (checkSecretLenUpdate()) {
        ElMessageBox.confirm(t('system.embedded_secret_len_change'), {
          confirmButtonType: 'primary',
          type: 'warning',
          confirmButtonText: t('common.sure'),
          cancelButtonText: t('dataset.cancel'),
          autofocus: false,
          showClose: false
        }).then(() => {
          saveHandler()
        })
        return
      }
      saveHandler()
    }
  })
}

const saveHandler = () => {
  const param = { ...state.form }
  const method = request.post({
    url: formType.value === 'add' ? '/embedded/create' : '/embedded/edit',
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
    })
    .finally(() => {
      closeLoading()
    })
}
const checkSecretLenUpdate = () => {
  return formType.value === 'edit' && originSecretLen.value !== state.form.secretLength
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(embeddedForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({
    target: '.embedded-info-drawer'
  })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="
      formType === 'add'
        ? t('system.create_embedded_application')
        : t('system.edit_embedded_application')
    "
    v-model="dialogVisible"
    modal-class="embedded-info-drawer"
    size="600px"
    destroy-on-close
    direction="rtl"
  >
    <el-form
      ref="embeddedForm"
      class="embedded-form"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('system.application_name')" prop="name">
        <el-input
          v-model="state.form.name"
          :placeholder="
            t('common.please_input') + $t('common.empty') + $t('system.application_name')
          "
        />
      </el-form-item>

      <el-form-item :label="t('system.cross_domain_settings')" prop="domain">
        <el-input
          v-model="state.form.domain"
          :placeholder="
            t('common.please_input') + t('common.empty') + t('system.cross_domain_settings')
          "
        />
      </el-form-item>

      <el-form-item :label="t('system.secret_length')" prop="secretLength">
        <el-input-number
          v-model="state.form.secretLength"
          autocomplete="off"
          step-strictly
          class="text-left edit-all-line"
          :min="4"
          :max="32"
          :placeholder="t('common.please_input') + t('common.empty') + t('system.secret_length')"
          controls-position="right"
          type="number"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(embeddedForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(embeddedForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.embedded-info-drawer {
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
.embedded-info-drawer {
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
.edit-all-line {
  width: 100%;
}
</style>
