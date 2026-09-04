<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const mfaForm = ref<FormInstance>()

const state = reactive({
  form: reactive({
    status: '',
    platformEnable: '',
    otpName: '',
    rate: ''
  }),
  settingList: [],
  statusOptions: [
    { value: '0', label: t('setting_mfa.status_0') },
    { value: '1', label: t('setting_mfa.status_1') },
    { value: '2', label: t('setting_mfa.status_2') }
  ]
})

const rule = reactive<FormRules>({
  rate: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
})

const buildSettingList = () => {
  return state.settingList.map(item => {
    const pkey = item.pkey.startsWith('mfa.') ? item.pkey : `mfa.${item.pkey}`
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
        .post({ url: '/perSetting/mfa/save', data: param })
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
  resetForm(mfaForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.mfa-param-drawer' })
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
    status: '',
    platformEnable: '',
    otpName: '',
    rate: ''
  }
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('setting_mfa.title')"
    v-model="dialogVisible"
    modal-class="mfa-param-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="mfaForm"
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
          v-if="item.pkey === 'platformEnable'"
          active-value="true"
          inactive-value="false"
          v-model="state.form[item.pkey]"
        />
        <el-radio-group v-else-if="item.pkey === 'status'" v-model="state.form[item.pkey]">
          <el-radio v-for="item in state.statusOptions" :key="item.value" :label="item.value">
            {{ item.label }}
          </el-radio>
        </el-radio-group>
        <el-input-number
          v-else-if="item.pkey === 'rate'"
          v-model="state.form[item.pkey]"
          autocomplete="off"
          step-strictly
          class="text-left edit-all-line"
          :min="1"
          :max="8"
          :placeholder="`${t('common.inputText')}${t('data_fill.form.number')} 1-8`"
          controls-position="right"
          type="number"
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
        <el-button secondary @click="resetForm(mfaForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(mfaForm)">
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
.mfa-param-drawer {
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
.de-mfa-switch {
  height: 22px;
}
</style>
