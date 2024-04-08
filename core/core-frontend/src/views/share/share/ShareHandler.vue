<template>
  <el-tooltip
    v-if="props.weight >= 7 && props.inGrid"
    effect="dark"
    :content="t('visualization.share')"
    placement="top"
  >
    <el-icon class="hover-icon hover-icon-in-table share-button-icon" @click.stop="share">
      <Icon name="icon_share-label_outlined"></Icon>
    </el-icon>
  </el-tooltip>
  <el-button v-if="props.weight >= 7 && props.isButton" @click.stop="share" icon="Share">{{
    t('visualization.share')
  }}</el-button>

  <el-dialog
    v-if="dialogVisible && props.weight >= 7"
    class="copy-link_dialog"
    :class="{ 'hidden-footer': !shareEnable }"
    v-model="dialogVisible"
    :close-on-click-modal="true"
    :append-to-body="true"
    :before-close="beforeClose"
    title="公共链接分享"
    width="480px"
    :show-close="false"
  >
    <div class="share-dialog-container">
      <div class="copy-link">
        <div class="open-share flex-align-center">
          <el-switch size="small" v-model="shareEnable" @change="enableSwitcher" />
          {{ shareTips }}
        </div>
        <div v-if="shareEnable" class="text">{{ linkAddr }}</div>
        <div v-if="shareEnable" class="exp-container">
          <el-checkbox
            :disabled="!shareEnable"
            v-model="overTimeEnable"
            @change="expEnableSwitcher"
            :label="t('visualization.over_time')"
          />
          <div class="inline-share-item-picker">
            <el-date-picker
              :clearable="false"
              size="small"
              v-if="state.detailInfo.exp"
              class="share-exp-picker"
              v-model="state.detailInfo.exp"
              type="datetime"
              placeholder=""
              :shortcuts="shortcuts"
              @change="expChangeHandler"
              :disabled-date="disabledDate"
              value-format="x"
            />
            <span v-if="expError" class="exp-error">必须大于当前时间</span>
          </div>
        </div>
        <div v-if="shareEnable" class="pwd-container">
          <el-checkbox
            :disabled="!shareEnable"
            v-model="passwdEnable"
            @change="pwdEnableSwitcher"
            :label="t('visualization.passwd_protect')"
          />
          <div class="auto-pwd-container" v-if="passwdEnable">
            <el-checkbox
              :disabled="!shareEnable"
              v-model="state.detailInfo.autoPwd"
              @change="autoEnableSwitcher"
              :label="t('visualization.auto_pwd')"
            />
          </div>
          <div class="inline-share-item" v-if="passwdEnable">
            <el-input
              ref="pwdRef"
              v-model="state.detailInfo.pwd"
              :readonly="state.detailInfo.autoPwd"
              size="small"
              @blur="validatePwdFormat"
            >
              <template #append>
                <div class="share-pwd-opt">
                  <div
                    v-if="state.detailInfo.autoPwd"
                    @click.stop="resetPwd"
                    class="share-reset-container"
                  >
                    <span>{{ t('commons.reset') }}</span>
                  </div>
                  <div @click.stop="copyPwd" class="share-reset-container">
                    <span>{{ t('commons.copy') }}</span>
                  </div>
                </div>
              </template>
            </el-input>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button :disabled="!shareEnable || expError" type="primary" @click.stop="copyInfo">
          {{ t('visualization.copy_link') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
import { ShareInfo, SHARE_BASE, shortcuts } from './option'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
const { toClipboard } = useClipboard()
const { t } = useI18n()
const props = defineProps({
  inGrid: propTypes.bool.def(false),
  resourceId: propTypes.string.def(''),
  resourceType: propTypes.string.def(''),
  weight: propTypes.number.def(0),
  isButton: propTypes.bool.def(false)
})
const pwdRef = ref(null)
const loadingInstance = ref<any>(null)
const dialogVisible = ref(false)
const overTimeEnable = ref(false)
const passwdEnable = ref(false)
const shareEnable = ref(false)
const linkAddr = ref('')
const expError = ref(false)
const state = reactive({
  detailInfo: {
    id: '',
    uuid: '',
    pwd: '',
    exp: 0,
    autoPwd: true
  } as ShareInfo
})
const emits = defineEmits(['loaded'])
const shareTips = computed(
  () =>
    `开启后，用户可以通过该链接访问${props.resourceType === 'dashboard' ? '仪表板' : '数据大屏'}`
)
const copyPwd = async () => {
  if (shareEnable.value && passwdEnable.value) {
    if (!state.detailInfo.autoPwd && existErrorMsg()) {
      ElMessage.warning('密码格式错误，请重新填写！')
      return
    }
    try {
      await toClipboard(state.detailInfo.pwd)
      ElMessage.success(t('common.copy_success'))
    } catch (e) {
      ElMessage.warning(t('common.copy_unsupported'))
    }
  } else {
    ElMessage.warning(t('common.copy_unsupported'))
  }
}
const copyInfo = async () => {
  if (shareEnable.value) {
    try {
      await toClipboard(linkAddr.value)
      ElMessage.success(t('common.copy_success'))
    } catch (e) {
      ElMessage.warning(t('common.copy_unsupported'))
    }
  } else {
    ElMessage.warning(t('common.copy_unsupported'))
  }
  dialogVisible.value = false
}

const disabledDate = date => {
  return date.getTime() < new Date().getTime()
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.share-dialog-container' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const share = () => {
  dialogVisible.value = true
  loadShareInfo()
}

const loadShareInfo = () => {
  showLoading()
  const resourceId = props.resourceId
  const url = `/share/detail/${resourceId}`
  request
    .get({ url })
    .then(res => {
      state.detailInfo = { ...res.data }
      setPageInfo()
    })
    .finally(() => {
      closeLoading()
    })
}

const setPageInfo = () => {
  if (state.detailInfo.id && state.detailInfo.uuid) {
    shareEnable.value = true
    formatLinkAddr()
  }
  passwdEnable.value = !!state.detailInfo.pwd
  overTimeEnable.value = !!state.detailInfo.exp
}

const enableSwitcher = () => {
  const resourceId = props.resourceId
  const url = `/share/switcher/${resourceId}`
  request.post({ url }).then(() => {
    loadShareInfo()
  })
}

const formatLinkAddr = () => {
  let prefix = '/'
  if (window.DataEaseBi?.baseUrl) {
    prefix = window.DataEaseBi.baseUrl + '#'
  } else {
    const href = window.location.href
    prefix = href.substring(0, href.indexOf('#') + 1)
  }
  linkAddr.value = prefix + SHARE_BASE + state.detailInfo.uuid
}

const expEnableSwitcher = val => {
  let exp = 0
  if (val) {
    const now = new Date()
    now.setTime(now.getTime() + 3600 * 1000)
    exp = now.getTime()
    state.detailInfo.exp = exp
  }
  expChangeHandler(exp)
}

const expChangeHandler = exp => {
  if (overTimeEnable.value && exp < new Date().getTime()) {
    expError.value = true
    return
  }
  expError.value = false
  const resourceId = props.resourceId
  const url = '/share/editExp'
  const data = { resourceId, exp }
  request.post({ url, data }).then(() => {
    loadShareInfo()
  })
}
const beforeClose = done => {
  if (validatePwdFormat()) {
    done()
  }
}
const validatePwdFormat = () => {
  if (!shareEnable.value || state.detailInfo.autoPwd) {
    showPageError(null)
    return true
  }
  const val = state.detailInfo.pwd
  if (!val) {
    showPageError('密码不能为空，请重新输入！')
    return false
  }
  const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()_+])[A-Za-z\d!@#$%^&*()_+]{4,10}$/
  if (!regex.test(val)) {
    showPageError('密码必须是包含数字、字母、特殊字符[!@#$%^&*()_+]的4-10位字符串')
    return false
  }
  showPageError(null)
  resetPwdHandler(val, false)
  return true
}
const showPageError = msg => {
  const domRef = pwdRef
  if (!domRef.value) {
    return
  }
  const e = domRef.value.input
  if (!msg) {
    e.style = null
    e.style.borderColor = null
    const child = e.parentElement.querySelector('.link-pwd-error-msg')
    if (child) {
      e.parentElement['style'] = null
      e.parentElement.removeChild(child)
    }
  } else {
    e.style.color = 'red'
    e.style.borderColor = 'red'
    e.parentElement['style']['box-shadow'] = '0 0 0 1px red inset'
    const child = e.parentElement.querySelector('.link-pwd-error-msg')
    if (!child) {
      const errorDom = document.createElement('div')
      errorDom.className = 'link-pwd-error-msg'
      errorDom.innerText = msg
      e.parentElement.appendChild(errorDom)
    } else {
      child.innerText = msg
    }
  }
}
const existErrorMsg = () => {
  return document.getElementsByClassName('link-pwd-error-msg')?.length
}
const autoEnableSwitcher = val => {
  if (val) {
    showPageError(null)
    resetPwd()
  } else {
    state.detailInfo.pwd = ''
    nextTick(() => {
      pwdRef.value.input.focus()
    })
  }
}
const pwdEnableSwitcher = val => {
  let pwd = ''
  if (val) {
    pwd = getUuid()
  }
  resetPwdHandler(pwd, true)
}
const resetPwd = () => {
  const pwd = getUuid()
  resetPwdHandler(pwd, true)
}
const resetPwdHandler = (pwd?: string, autoPwd?: boolean) => {
  const resourceId = props.resourceId
  const url = '/share/editPwd'
  const data = { resourceId, pwd, autoPwd }
  request.post({ url, data }).then(() => {
    loadShareInfo()
  })
}

const getUuid = () => {
  return 'xyxy'.replace(/[xy]/g, function (c) {
    var r = (Math.random() * 16) | 0,
      v = c == 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

const execute = () => {
  share()
}
defineExpose({
  execute
})

onMounted(() => {
  if (!props.inGrid && props.weight >= 7) {
    const commandInfo = {
      label: '分享',
      command: 'share',
      svgName: 'dv-share'
    }
    emits('loaded', commandInfo)
  }
})
</script>
<style lang="less">
.copy-link_dialog {
  .ed-dialog__header {
    padding: 16px 16px 10px !important;
    .ed-dialog__title {
      font-size: 14px !important;
    }
  }
  .ed-dialog__body {
    padding: 16px !important;
  }
  .ed-dialog__footer {
    border-top: 1px solid #1f232926;
    padding: 12px 16px 16px;
  }
}
.hidden-footer {
  .ed-dialog__footer {
    display: none !important;
  }
}
</style>
<style lang="less" scoped>
.share-button-icon {
  margin-left: 4px;
}
.copy-link_dialog {
  .exp-container {
    .ed-checkbox {
      margin-right: 10px;
    }
    .inline-share-item-picker {
      display: flex;
      align-items: center;
      :deep(.share-exp-picker) {
        margin-left: 25px !important;
        .ed-input__wrapper {
          width: 200px !important;
        }
      }
      .exp-error {
        color: var(--ed-color-danger);
        font-size: 12px;
      }
    }
  }
  .pwd-container {
    .auto-pwd-container {
      padding: 0 25px 6px;
    }
    .ed-checkbox {
      margin-right: 10px;
    }
    .inline-share-item {
      margin-left: 25px;
      width: 220px;

      :deep(.ed-input-group__append) {
        width: initial !important;
        background: none;
        color: #1f2329;
        padding: 0px 0px !important;
        .share-pwd-opt {
          display: flex;
          padding: 1px;
          .share-reset-container {
            &:not(:first-child) {
              border-left: 1px solid var(--ed-input-border-color) !important;
            }
            width: 45px;
            display: flex;
            justify-content: center;
            &:hover {
              cursor: pointer;
              background-color: #f5f6f7;
            }
            &:active {
              cursor: pointer;
              background-color: #eff0f1;
            }
          }
        }
      }
      :deep(.link-pwd-error-msg) {
        color: red;
        position: absolute;
        z-index: 9;
        font-size: 10px;
        height: 10px;
        top: 21px;
        width: 350px;
        left: 0px;
      }
    }
  }

  .copy-link {
    font-weight: 400;
    font-family: PingFang SC;

    .open-share {
      margin: -18px 0 8px 0;
      color: #646a73;
      font-size: 12px;
      font-style: normal;
      line-height: 20px;
      .ed-switch {
        margin-right: 8px;
      }
    }

    .text {
      border-radius: 4px;
      border: 1px solid #bbbfc4;
      background: #eff0f1;
      margin-bottom: 16px;
      height: 32px;
      padding: 5px 12px;
      color: #8f959e;
      font-size: 14px;
      font-style: normal;
      line-height: 22px;
    }
  }
}
</style>
