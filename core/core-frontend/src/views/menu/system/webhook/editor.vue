<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import CodeEdit from '@/components/CodeEdit/CodeEdit.vue'
import dvInfo from '@/assets/svg/dv-info.svg'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const webhookForm = ref<FormInstance>()
interface WebhookForm {
  id?: string
  name?: string
  url?: string
  contentType?: string
  secret?: string
  ssl: boolean
  msgTemplate?: string
}
const state = reactive({
  form: reactive<WebhookForm>({
    id: '',
    name: '',
    url: '',
    ssl: true,
    contentType: '',
    secret: '',
    msgTemplate: ''
  })
})
const formType = ref('add')
const options = [
  { value: 'application/json', label: 'application/json' },
  { value: 'application/x-www-form-urlencoded', label: 'application/x-www-form-urlencoded' }
]
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
      message: `${t('common.please_input')} webhook`,
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ],
  url: [
    {
      required: true,
      message: `${t('common.please_input')} url`,
      trigger: 'blur'
    },
    {
      min: 10,
      max: 255,
      message: t('commons.input_limit', [10, 255]),
      trigger: 'blur'
    },
    { required: true, validator: validateUrl, trigger: 'blur' }
  ],
  contentType: [
    {
      required: true,
      message: `${t('common.please_select')} ${t('webhook.content_type')}`,
      trigger: 'change'
    }
  ],
  ssl: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'SSL',
      trigger: 'change'
    }
  ]
})

const edit = id => {
  if (!id) {
    add()
    return
  }
  request.get({ url: `/webhook/get/${id}` }).then(res => {
    const data = res.data
    state.form = {
      id: data.id,
      name: data.name,
      url: data.url,
      ssl: data.ssl,
      contentType: data.contentType,
      secret: data.secret,
      msgTemplate: data.msgTemplate
    }
    formType.value = 'edit'
    dialogVisible.value = true
  })
}
const add = () => {
  state.form = {
    ssl: true,
    msgTemplate: ''
  }
  formType.value = 'add'
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      saveHandler()
    }
  })
}

const saveHandler = () => {
  const param = { ...state.form }
  const method = request.post({
    url: '/webhook/save',
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

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(webhookForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({
    target: '.webhook-info-drawer'
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
    :title="t('webhook.add')"
    v-model="dialogVisible"
    modal-class="webhook-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="webhookForm"
      class="webhook-form"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('common.name')" prop="name">
        <el-input
          v-model="state.form.name"
          :placeholder="t('common.please_input') + t('common.empty') + t('common.name')"
        />
      </el-form-item>

      <el-form-item label="URL" prop="url">
        <el-input v-model="state.form.url" :placeholder="`${t('common.please_input')}URL`" />
      </el-form-item>

      <el-form-item :label="t('webhook.content_type')" prop="contentType">
        <el-select
          v-model="state.form.contentType"
          :placeholder="t('common.please_select') + t('common.empty') + t('webhook.content_type')"
          style="width: 100%"
        >
          <el-option
            v-for="item in options"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="Secret" prop="secret">
        <el-input
          v-model="state.form.secret"
          :placeholder="t('common.please_input') + t('common.empty') + 'Secret'"
        />
      </el-form-item>

      <el-form-item
        v-if="state.form.contentType === 'application/json'"
        :label="t('webhook.msg_template')"
        prop="msgTemplate"
      >
        <template v-slot:label>
          <div class="basic-form-info-tips">
            <span class="custom-form-item__label">{{ t('webhook.msg_template') }}</span>
            <el-tooltip
              effect="dark"
              :content="
                t('webhook.msg_template_tips', {
                  t0: '${title}',
                  t1: '${content}',
                  t2: '${messageId}'
                })
              "
              placement="top"
            >
              <el-icon
                ><Icon name="dv-info"><dvInfo class="svg-icon" /></Icon
              ></el-icon>
            </el-tooltip>
          </div>
        </template>
        <div style="width: 100%; border: 1px solid #dcdfe6; border-radius: 4px">
          <CodeEdit
            v-model="state.form.msgTemplate"
            :data="state.form.msgTemplate"
            mode="json"
            height="300px"
            :read-only="false"
            :enable-format="true"
          />
        </div>
      </el-form-item>

      <el-form-item label="SSL" prop="ssl">
        <el-switch v-model="state.form.ssl" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(webhookForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(webhookForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.webhook-info-drawer {
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
  .basic-form-info-tips {
    width: fit-content;
    display: inline-flex;
    align-items: center;
    column-gap: 4px;
  }
}
</style>
<style lang="less" scoped>
.webhook-info-drawer {
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
