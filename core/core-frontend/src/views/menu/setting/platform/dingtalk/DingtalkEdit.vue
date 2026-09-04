<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import dvInfo from '@/assets/svg/dv-info.svg'
import {
  config as ddConfig,
  ready as ddReady,
  error as ddError,
  chooseChat as ddChooseChat
} from 'dingtalk-jsapi'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const dingtalkForm = ref<FormInstance>()
interface DingtalkChat {
  name: string
  id: string
}
interface DingtalkForm {
  corpId: string | null
  agentId: string | null
  appKey: string | null
  appSecret: string | null
  callBack: string | null
  enable: string | null
  valid: string | null
  robotCode: string | null
  chatList: DingtalkChat[]
}
const isDingTalkEnv = ref(false)
const dingtalkStatusLoaded = ref(false)
const state = reactive({
  form: reactive<DingtalkForm>({
    corpId: null,
    agentId: null,
    appKey: null,
    appSecret: null,
    callBack: null,
    enable: 'false',
    valid: 'false',
    robotCode: null,
    chatList: [] as DingtalkChat[]
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
  corpId: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'CorpId',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 255,
      message: t('commons.input_limit', [5, 255]),
      trigger: 'blur'
    }
  ],
  agentId: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + 'AgentId',
      trigger: 'blur'
    },
    {
      min: 5,
      max: 20,
      message: t('commons.input_limit', [5, 20]),
      trigger: 'blur'
    }
  ],
  appKey: [
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
      max: 100,
      message: t('commons.input_limit', [5, 100]),
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
    corpId: row.corpId,
    agentId: row.agentId,
    appKey: row.appKey,
    appSecret: row.appSecret,
    callBack: row.callBack,
    enable: row.enable,
    valid: row.valid,
    robotCode: row.robotCode,
    chatList: row.chatList || []
  }
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...state.form }
      const method = request.post({ url: '/dingtalk/create', data: param })
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

const validate = () => {
  const url = '/dingtalk/validate'
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

const DD_READY_STATUS = 'ddReadyStatus' as string

let authPromise: Promise<void> | null = null

const ddJsApiAuth = (): Promise<void> => {
  if (window[DD_READY_STATUS as string]) {
    return Promise.resolve()
  }

  if (authPromise) {
    return authPromise
  }

  authPromise = new Promise<void>(async (resolve, reject) => {
    if (window[DD_READY_STATUS as string]) {
      resolve()
      return
    }

    try {
      const param = {
        currentUrl: window.location.href.split('#')[0]
      }
      const res = await request.post({ url: '/dingtalk/getSignatureInfo', data: param })
      const signatureInfo = res.data
      ddConfig(signatureInfo)

      ddReady(() => {
        window[DD_READY_STATUS as string] = true
        authPromise = null
        resolve()
      })

      ddError((err: any) => {
        console.error('钉钉鉴权失败', err)
        authPromise = null
        reject(err)
      })
    } catch (error) {
      console.error('获取签名信息失败', error)
      authPromise = null
      reject(error)
    }
  })

  return authPromise
}
const addChatRow = async () => {
  if (!dingtalkStatusLoaded.value) {
    showLoading()
    await Promise.race([
      new Promise<void>(resolve => {
        ddReady(() => {
          isDingTalkEnv.value = true
          resolve()
        })
      }),
      new Promise<void>(resolve => setTimeout(resolve, 3000))
    ])
    dingtalkStatusLoaded.value = true
  }

  if (!isDingTalkEnv.value) {
    var corpId = state.form.corpId
    var path = `/?client=dingtalk&corpId=${corpId}#/sys-setting/platform?edit=dingtalk`
    var linkUrl = `https://applink.dingtalk.com/page/h5_app_open?appId=${
      state.form.agentId
    }&corpId=${corpId}&appType=2&target=workbench&path=${encodeURIComponent(path)}`
    window.open(linkUrl, '_blank')
    closeLoading()
    return
  }

  await ddJsApiAuth()
  closeLoading()
  ddChooseChat({
    corpId: state.form.corpId as string,
    isAllowCreateGroup: false,
    filterNotOwnerGroup: true,
    success: (res: any) => {
      const { chatId, openConversationId, title } = res
      if (!chatId || !openConversationId) {
        alert('只能选择当前应用所在的群聊，请重新选择')
        return
      }
      if (
        state.form.chatList?.length &&
        state.form.chatList.some(item => item.id === openConversationId)
      ) {
        alert('当前群聊已选择，请勿重复选择')
        return
      }
      const chat = { name: title, id: openConversationId } as any

      request
        .post({ url: '/dingtalk/checkChat', data: { chatId: openConversationId } })
        .then(() => {
          addRowHandler(chat)
        })
        .catch((e: any) => {
          alert(e)
        })
    },
    fail: e => {
      console.error('ddChooseChat fail--: ' + JSON.stringify(e))
    },
    complete: () => {}
  })
}
const addRowHandler = (row: DingtalkChat) => {
  state.form.chatList.push(row)
}
const delChatRow = (index: number) => {
  state.form.chatList.splice(index, 1)
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="t('system.dingtalk_settings')"
    v-model="dialogVisible"
    modal-class="platform-info-drawer"
    size="600px"
    destroy-on-close
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
      <el-form-item label="CorpId" prop="corpId">
        <el-input
          v-model="state.form.corpId"
          :placeholder="t('common.please_input') + t('common.empty') + 'CorpId'"
        />
      </el-form-item>
      <el-form-item label="AgentId" prop="agentId">
        <el-input
          v-model="state.form.agentId"
          :placeholder="t('common.please_input') + t('common.empty') + 'AgentId'"
        />
      </el-form-item>
      <el-form-item label="APP Key" prop="appKey">
        <el-input
          v-model="state.form.appKey"
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

      <el-form-item label="Robot Code" prop="robotCode">
        <el-input v-model="state.form.robotCode" :placeholder="t('report.robot_code_place')" />
      </el-form-item>

      <el-form-item :label="t('report.dingtalk_groups')" class="last-form-item">
        <template v-slot:label>
          <div class="ticket-form-info-tips">
            <span class="custom-form-item__label">{{ t('report.dingtalk_groups') }}</span>
            <el-tooltip effect="dark" :content="t('report.dingtalk_groups_tips')" placement="top">
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <div class="dingtalk-chat-div">
          <div v-for="(row, index) in state.form.chatList" :key="index">
            <el-tag
              class="dingtalk-chat-tag"
              closable
              type="info"
              :disable-transitions="true"
              @close="delChatRow(index)"
            >
              <span class="dingtalk-chat-tag-span">{{ row['name'] }}</span>
            </el-tag>
          </div>
          <span class="input-placeholder">{{ t('report.click_add_chat') }}</span>
        </div>
      </el-form-item>

      <div class="dingtalk-chat-add">
        <el-button @click.stop="addChatRow" text>
          <template #icon>
            <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
          </template>
          {{ t('commons.add') }}
        </el-button>
        <span class="add-tips">{{ t('report.add_chat_tips') }}</span>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(dingtalkForm)">{{ t('common.cancel') }}</el-button>
        <el-button
          :disabled="!state.form.appKey || !state.form.appSecret || !state.form.callBack"
          @click="validate"
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
  .dingtalk-chat-add {
    display: flex;
    align-items: center;
    column-gap: 12px;
    line-height: 26px;
    height: 26px;
    .add-tips {
      font-size: 12px;
      color: #8f959e;
      font-weight: 300;
    }
  }

  .last-form-item {
    margin-bottom: 8px;
    .ticket-form-info-tips {
      width: fit-content;
      display: inline-flex;
      align-items: center;
      column-gap: 4px;
    }
    .dingtalk-chat-div {
      width: 100%;
      border: 1px solid var(--ed-border-color);
      border-radius: 4px;
      padding: 1px 8px;
      .input-placeholder {
        color: rgba(0, 0, 0, 0.25);
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
        line-height: 22px;
      }
    }
  }
}
.dingtalk-chat-tag {
  max-width: 100%;
  ::v-deep(.ed-tag__content) {
    max-width: calc(100% - 16px);
  }
  .dingtalk-chat-tag-span {
    display: inline-block;
    max-width: calc(100%);
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
