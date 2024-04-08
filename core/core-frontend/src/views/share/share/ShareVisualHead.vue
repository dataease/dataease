<template>
  <el-popover
    :visible="popoverVisible"
    title=""
    width="480"
    placement="bottom-start"
    :show-arrow="false"
    popper-class="share-popover"
    @show="share"
  >
    <template #reference>
      <el-button
        secondary
        v-if="props.weight >= 7"
        @click="openPopover"
        v-click-outside="clickOutPopover"
      >
        <template #icon>
          <icon name="icon_share-label_outlined"></icon>
        </template>
        {{ t('visualization.share') }}
      </el-button>
    </template>
    <div class="share-container">
      <div class="share-title share-padding">公共链接分享</div>
      <div class="open-share flex-align-center share-padding">
        <el-switch size="small" v-model="shareEnable" @change="enableSwitcher" />
        {{ shareTips }}
      </div>
      <div v-if="shareEnable" class="text share-padding">
        <el-input v-model="linkAddr" disabled />
      </div>
      <div v-if="shareEnable" class="exp-container share-padding">
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
            class="share-exp-picker"
            v-if="state.detailInfo.exp"
            v-model="state.detailInfo.exp"
            type="datetime"
            :teleported="false"
            placeholder=""
            :shortcuts="shortcuts"
            @change="expChangeHandler"
            :disabled-date="disabledDate"
            value-format="x"
          />
          <span v-if="expError" class="exp-error">必须大于当前时间</span>
        </div>
      </div>
      <div v-if="shareEnable" class="pwd-container share-padding">
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

      <el-divider v-if="shareEnable" class="share-divider" />
      <div v-if="shareEnable" class="share-foot share-padding">
        <el-button :disabled="!shareEnable || expError" type="primary" @click="copyInfo">
          {{ t('visualization.copy_link') }}
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, computed, nextTick, watch } from 'vue'
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
import { ShareInfo, SHARE_BASE, shortcuts } from './option'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
const { toClipboard } = useClipboard()
const { t } = useI18n()
const props = defineProps({
  resourceId: propTypes.string.def(''),
  resourceType: propTypes.string.def(''),
  weight: propTypes.number.def(0)
})
const popoverVisible = ref(false)
const pwdRef = ref(null)
const loadingInstance = ref<any>(null)
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

watch(
  () => props.resourceId,
  () => {
    popoverVisible.value = false
  }
)
const hideShare = () => {
  if (validatePwdFormat()) {
    popoverVisible.value = false
    return
  }
}
const clickOutPopover = e => {
  if (!popoverVisible.value || e.target.closest('[class*="share-popover"]')) {
    return
  }
  hideShare()
}
const openPopover = () => {
  if (!popoverVisible.value) {
    popoverVisible.value = true
  }
}
const shareTips = computed(
  () =>
    `开启后，用户可以通过该链接访问${props.resourceType === 'dashboard' ? '仪表板' : '数据大屏'}`
)

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
  hideShare()
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
    passwdEnable.value = !!state.detailInfo.pwd
    overTimeEnable.value = !!state.detailInfo.exp
  } else {
    shareEnable.value = false
    passwdEnable.value = false
    overTimeEnable.value = false
  }
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
  const length = 10
  const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+'
  let result = ''
  const specialChars = '!@#$%^&*()_+'
  let hasSpecialChar = false

  for (let i = 0; i < length; i++) {
    if (i === 0) {
      result += characters.charAt(Math.floor(Math.random() * characters.length))
    } else {
      if (!hasSpecialChar && i < length - 2) {
        result += specialChars.charAt(Math.floor(Math.random() * specialChars.length))
        hasSpecialChar = true
      } else {
        result += characters.charAt(Math.floor(Math.random() * characters.length))
      }
    }
  }
  result = result
    .split('')
    .sort(() => 0.5 - Math.random())
    .join('')
  return result
}

const validatePwdFormat = () => {
  if (!shareEnable.value || !passwdEnable.value || state.detailInfo.autoPwd) {
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

const execute = () => {
  share()
}
defineExpose({
  execute
})
</script>

<style lang="less">
.share-popover {
  padding: 16px 0px !important;
}
</style>

<style lang="less" scoped>
.share-container {
  .share-title {
    font-weight: 500;
    color: #1f2329;
    padding-bottom: 5px !important;
  }
  .share-padding {
    padding: 0px 16px;
  }
  .share-divider {
    margin-bottom: 10px !important;
    border-top: 1px #1f232926 solid;
  }
  .share-foot {
    display: flex;
    justify-content: flex-end;
  }
  .open-share {
    font-size: 12px;
    color: #646a73;
    .ed-switch {
      margin-right: 8px;
    }
  }
  .text {
    padding-bottom: 5px !important;
  }
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
</style>
