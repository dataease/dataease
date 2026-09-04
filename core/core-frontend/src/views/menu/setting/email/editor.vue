<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const emailForm = ref<FormInstance>()

const state = reactive({
  form: reactive({
    host: '',
    port: '',
    account: '',
    pwd: '',
    from: '',
    reci: '',
    ssl: '',
    tsl: ''
  }),
  settingList: []
})

const rule = reactive<FormRules>({
  host: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ],
  port: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
  /* account: [
    {
      required: true,
      message: t("common.require"),
      trigger: "blur",
    },
  ], */
})

const buildSettingList = () => {
  return state.settingList.map(item => {
    const pkey = item.pkey.startsWith('email.') ? item.pkey : `email.${item.pkey}`
    const sort = item.sort
    const type = item.type
    let pval = state.form[item.pkey]
    if (Array.isArray(pval)) {
      pval = pval.join(',')
    }
    return { pkey, pval, type, sort }
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = buildSettingList()
      if (param.length < 2) {
        return
      }
      showLoading()
      request
        .post({ url: '/email/setting/save', data: param })
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
  state.settingList = []
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(emailForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.email-param-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const edit = list => {
  resetFormData()
  state.settingList = list.map(item => {
    const pkey = item.pkey
    item['label'] = `setting_${pkey}`
    item['pkey'] = pkey.split('.')[1]
    let pval = item.pval
    state.form[item['pkey']] = pval || state.form[item['pkey']]
    return item
  })
  dialogVisible.value = true
}

const resetFormData = () => {
  state.form = {
    host: '',
    port: '',
    account: '',
    pwd: '',
    from: '',
    reci: '',
    ssl: '',
    tsl: ''
  }
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('setting_email.title')"
    v-model="dialogVisible"
    modal-class="email-param-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="emailForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item
        v-for="item in state.settingList"
        :key="item.pkey"
        :prop="item.pkey"
        :label="t(item.label)"
      >
        <el-switch
          class="de-basic-switch"
          v-if="item.pkey === 'ssl' || item.pkey === 'tsl'"
          active-value="true"
          inactive-value="false"
          v-model="state.form[item.pkey]"
        />

        <el-input
          v-else-if="item.type === 'pwd'"
          v-model="state.form[item.pkey]"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + t(item.label)"
        />
        <el-input
          v-else
          v-model="state.form[item.pkey]"
          :placeholder="t('common.please_input') + t('common.empty') + t(item.label)"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(emailForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(emailForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.basic-param-drawer {
  .ed-drawer__footer {
    box-shadow: 0 -1px 4px #1f232926 !important;
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
<style scoped lang="less">
.email-param-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
  .edit-all-line {
    width: 552px !important;
  }
}
.de-email-switch {
  height: 22px;
}
</style>
