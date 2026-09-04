<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import dvInfo from '@/assets/svg/dv-info.svg'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const hmacForm = ref<FormInstance>()

const state = reactive({
  form: reactive({
    enable: '',
    secretKey: '',
    clockSkew: '300'
  }),
  settingList: [] as any[]
})
const tooltipItem = ref({
  secretKey: t('commons.refresh'),
  clockSkew: t('setting_hmac.clock_skew_tips')
})
const rule = reactive<FormRules>({
  secretKey: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ],
  clockSkew: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    },
    {
      validator: (_rule, value, callback) => {
        const num = Number(value)
        if (isNaN(num) || num < 1 || num > 600) {
          callback(new Error(t('setting_hmac.clock_skew_range', [600])))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const buildSettingList = () => {
  return state.settingList.map((item: any) => {
    const pkey = item.pkey.startsWith('hmac.') ? item.pkey : `hmac.${item.pkey}`
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
        .post({ url: '/perSetting/hmac/save', data: param })
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            window['de_secret_key'] = null
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
  resetForm(hmacForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.hmac-param-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const edit = list => {
  resetFormData()
  state.settingList = list.map(item => {
    const pkey = item.pkey
    if (pkey.includes('enable')) {
      item['label'] = `setting_${pkey}`
    } else if (pkey.includes('clockSkew')) {
      item['label'] = 'setting_hmac.clock_skew'
    } else if (pkey.includes('secretKey')) {
      item['label'] = 'Secret Key'
    }
    item['pkey'] = pkey.split('.')[1]
    let pval = item.pval
    state.form[item['pkey']] = pval || state.form[item['pkey']]
    return item
  })
  dialogVisible.value = true
}

const resetFormData = () => {
  state.form = {
    enable: '',
    secretKey: '',
    clockSkew: '300'
  }
}

const enableChange = (val: string) => {
  if (val === 'true' && !state.form.secretKey) {
    refreshSecret(false)
  }
}
const refreshSecret = (showMsg: boolean) => {
  request.post({ url: '/perSetting/hmac/refresh' }).then(res => {
    if (!res.msg) {
      state.form.secretKey = res.data
      if (showMsg) {
        ElMessage.success(t('common.refresh_success'))
      }
    }
  })
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('setting_hmac.title')"
    v-model="dialogVisible"
    modal-class="hmac-param-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="hmacForm"
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
        <template v-slot:label>
          <div class="hmac-form-info-tips">
            <span class="custom-form-item__label">{{ t(item.label) }}</span>
            <el-tooltip
              v-if="tooltipItem[`${item.pkey}`]"
              effect="dark"
              :content="tooltipItem[`${item.pkey}`]"
              placement="top"
            >
              <el-icon v-if="item.pkey === 'clockSkew'">
                <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
              </el-icon>

              <el-button v-else text @click="refreshSecret(true)">
                <template #icon>
                  <Icon name="icon_refresh_outlined"
                    ><icon_refresh_outlined class="svg-icon"
                  /></Icon>
                </template>
              </el-button>
            </el-tooltip>
          </div>
        </template>
        <el-switch
          class="de-basic-switch"
          v-if="item.pkey === 'enable'"
          active-value="true"
          inactive-value="false"
          v-model="state.form[item.pkey]"
          @change="enableChange"
        />

        <el-input
          v-else-if="item.pkey === 'secretKey'"
          v-model="state.form[item.pkey]"
          type="password"
          show-password
          :placeholder="t('common.please_input') + t('common.empty') + t(item.label)"
        />

        <el-input-number
          v-else-if="item.pkey === 'clockSkew'"
          v-model="state.form.clockSkew"
          autocomplete="off"
          step-strictly
          class="text-left edit-all-line"
          :min="1"
          :max="600"
          :placeholder="t('common.inputText')"
          controls-position="right"
          type="number"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(hmacForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(hmacForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.hmac-param-drawer {
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
    .hmac-form-info-tips {
      width: fit-content;
      display: inline-flex;
      align-items: center;
      column-gap: 4px;
    }
  }
  .ed-form-item {
    &.is-required.asterisk-right {
      .ed-form-item__label:after {
        display: none;
      }
      .hmac-form-info-tips {
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
<style scoped lang="less">
.hmac-param-drawer {
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
.de-hmac-switch {
  height: 22px;
}
</style>
