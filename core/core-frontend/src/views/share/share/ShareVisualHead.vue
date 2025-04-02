<template>
  <el-popover
    :visible="popoverVisible"
    title=""
    width="480"
    placement="bottom-end"
    :show-arrow="false"
    popper-class="share-popover"
    @show="share"
  >
    <template #reference>
      <el-button
        secondary
        v-if="props.weight >= 7"
        :disabled="disabled"
        @click="openPopover"
        v-click-outside="clickOutPopover"
      >
        <template #icon>
          <icon name="icon_share-label_outlined"
            ><icon_shareLabel_outlined class="svg-icon"
          /></icon>
        </template>
        {{ t('visualization.share') }}
      </el-button>
    </template>
    <div v-if="!shareDisable" class="share-container">
      <div class="share-title share-padding">{{ t('work_branch.public_link_share') }}</div>
      <div class="open-share flex-align-center share-padding">
        <el-switch size="small" v-model="shareEnable" @change="enableSwitcher" />
        {{ shareTips }}
      </div>
      <div v-if="shareEnable" class="custom-link-line share-padding">
        <el-input
          ref="linkUuidRef"
          placeholder=""
          :class="!linkCustom ? 'link-input-readlonly' : ''"
          v-model="state.detailInfo.uuid"
          :readonly="!linkCustom"
          @blur="validateUuid"
        >
          <template v-if="!linkCustom" #prefix>
            {{ formatLinkBase() }}
          </template>

          <template #suffix>
            <div class="share-input-suffix">
              <span class="suffix-split" />
              <div
                class="input-suffix-btn edit-uuid-icon"
                v-if="!linkCustom"
                @click.stop="editUuid"
              >
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('commons.edit') + t('chart.indicator_suffix')"
                  placement="top"
                >
                  <el-icon class="edit-uuid-icon" style="cursor: pointer">
                    <Icon class="edit-uuid-icon">
                      <icon_edit_outlined class="svg-icon edit-uuid-icon" />
                    </Icon>
                  </el-icon>
                </el-tooltip>
              </div>
              <div class="input-suffix-btn" v-if="linkCustom" @click.stop="resetUuid">
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('commons.cancel')"
                  placement="top"
                >
                  <el-icon style="cursor: pointer">
                    <Icon><icon_close_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </el-tooltip>
              </div>
              <div
                class="input-suffix-btn done-finish"
                v-if="linkCustom"
                @click.stop="finishEditUuid"
              >
                <el-tooltip class="item" effect="dark" :content="t('commons.save')" placement="top">
                  <el-icon style="cursor: pointer">
                    <Icon><icon_done_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </el-tooltip>
              </div>
            </div>
          </template>
        </el-input>
      </div>
      <div v-if="shareEnable" class="exp-container share-padding">
        <el-checkbox
          ref="expCheckbox"
          :disabled="!shareEnable"
          v-model="overTimeEnable"
          @change="expEnableSwitcher"
        >
          <div class="checkbox-span">
            <span>{{ t('visualization.over_time') }}</span>
            <span class="pe-require" :class="{ 'pe-tips-hidden': !sharePeRequire }">
              <span>*</span>
            </span>
          </div>
        </el-checkbox>
        <div class="inline-share-item-picker">
          <el-date-picker
            :clearable="false"
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
          <span v-if="expError" class="exp-error">{{ t('work_branch.share_time_limit') }}</span>
        </div>
      </div>
      <div v-if="shareEnable" class="pwd-container share-padding">
        <el-checkbox
          ref="pwdCheckbox"
          :disabled="!shareEnable"
          v-model="passwdEnable"
          @change="pwdEnableSwitcher"
        >
          <div class="checkbox-span">
            <span>{{ t('visualization.passwd_protect') }}</span>
            <span class="pe-require" :class="{ 'pe-tips-hidden': !sharePeRequire }">
              <span>*</span>
            </span>
          </div>
        </el-checkbox>
        <div class="auto-pwd-container" v-if="passwdEnable">
          <el-checkbox
            v-show="false"
            :disabled="!shareEnable"
            v-model="state.detailInfo.autoPwd"
            @change="autoEnableSwitcher"
            :label="t('visualization.auto_pwd')"
          />
        </div>
        <div class="inline-share-item" v-if="passwdEnable">
          <el-input
            ref="pwdRef"
            style="flex: 1"
            class="link-input-readlonly"
            v-model="state.detailInfo.pwd"
            :readonly="state.detailInfo.autoPwd"
          >
            <template #suffix>
              <div class="share-input-suffix">
                <span class="suffix-split" />
                <div class="input-suffix-btn" @click="copyPwd">
                  <el-tooltip
                    class="item"
                    effect="dark"
                    :content="t('commons.copy')"
                    placement="top"
                  >
                    <el-icon style="cursor: pointer">
                      <Icon><deCopy class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </div>
                <div class="input-suffix-btn" @click="resetPwd">
                  <el-tooltip
                    class="item"
                    effect="dark"
                    :content="t('commons.reset')"
                    placement="top"
                  >
                    <el-icon style="cursor: pointer">
                      <Icon><icon_refresh_outlined class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </div>
              </div>
            </template>
          </el-input>

          <el-button secondary @click="openPwdDialog">{{ t('user.change_password') }}</el-button>
        </div>
      </div>

      <el-divider v-if="shareEnable" class="share-divider" />
      <div v-if="shareEnable" class="share-foot share-padding">
        <el-button secondary @click="openTicket">{{ t('work_branch.ticket_setting') }}</el-button>
        <el-button :disabled="!shareEnable || expError" type="primary" @click="copyInfo">
          {{ passwdEnable ? t('visualization.copy_link_passwd') : t('visualization.copy_link') }}
        </el-button>
      </div>
    </div>
    <div v-else class="share-container">
      <div class="share-title share-padding">{{ t('work_branch.public_link_share') }}</div>
      <div class="open-share flex-align-center share-padding">
        <span>{{ t('work_branch.cannot_share_link') }}</span>
      </div>
    </div>
  </el-popover>
  <custom-link-pwd ref="customPwdRef" @pwd-change="customPwdChange" />
  <ticket-dialog v-if="showTicket" ref="ticketDialogRef">
    <div v-if="!shareDisable && shareEnable && showTicket">
      <share-ticket
        :uuid="state.detailInfo.uuid"
        :resource-id="props.resourceId"
        :ticket-require="state.detailInfo.ticketRequire"
        @require-change="updateRequireTicket"
        @close="closeTicket"
      />
    </div>
  </ticket-dialog>
</template>

<script lang="ts" setup>
import icon_shareLabel_outlined from '@/assets/svg/icon_share-label_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import icon_done_outlined from '@/assets/svg/icon_done_outlined.svg'
import deCopy from '@/assets/svg/de-copy.svg'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, reactive, computed, nextTick, watch } from 'vue'
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'
import { ShareInfo, SHARE_BASE, shortcuts } from './option'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import ShareTicket from './ShareTicket.vue'
import { useEmbedded } from '@/store/modules/embedded'
import { useShareStoreWithOut } from '@/store/modules/share'
import CustomLinkPwd from './CustomLinkPwd.vue'
import TicketDialog from './TicketDialog.vue'
const shareStore = useShareStoreWithOut()
const embeddedStore = useEmbedded()
const { toClipboard } = useClipboard()
const { t } = useI18n()
const props = defineProps({
  resourceId: propTypes.string.def(''),
  resourceType: propTypes.string.def(''),
  weight: propTypes.number.def(0),
  disabled: propTypes.bool.def(false)
})
const popoverVisible = ref(false)
const pwdRef = ref(null)
const expCheckbox = ref()
const pwdCheckbox = ref()
const loadingInstance = ref<any>(null)
const overTimeEnable = ref(false)
const passwdEnable = ref(false)
const shareEnable = ref(false)
const linkAddr = ref('')
const expError = ref(false)
const linkCustom = ref(false)
const linkUuidRef = ref(null)
const showTicket = ref(false)
const originUuid = ref('')
const customPwdRef = ref()
const ticketDialogRef = ref()
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
const hideShare = async () => {
  if (!shareEnable.value) {
    popoverVisible.value = false
    return
  }
  if (sharePeRequire.value) {
    const peRequireValid = validatePeRequire()
    if (!peRequireValid) {
      return
    }
  }
  const uuidValid = await validateUuid()
  if (uuidValid) {
    linkCustom.value = false
    popoverVisible.value = false
    return
  }
}
const clickOutPopover = e => {
  if (
    !popoverVisible.value ||
    e.target.closest('[class*="share-popover"]') ||
    e.target.closest('[class*="ed-overlay-dialog"]') ||
    e.target.classList?.toString()?.includes('edit-uuid-icon')
  ) {
    return
  }
  hideShare()
}
const openPopover = () => {
  if (!popoverVisible.value) {
    showTicket.value = false
    popoverVisible.value = true
  }
}
const shareTips = computed(
  () =>
    `${t('work_branch.open_link_hint')}${
      props.resourceType === 'dashboard'
        ? t('work_branch.dashboard')
        : t('work_branch.big_data_screen')
    }`
)
const shareDisable = computed(() => shareStore.getShareDisable)
const sharePeRequire = computed(() => shareStore.getSharePeRequire)

const copyInfo = async () => {
  if (shareEnable.value) {
    try {
      if (existErrorMsg('link-uuid-error-msg')) {
        ElMessage.warning(t('work_branch.error_link_hint'))
        return
      }
      if (passwdEnable.value && !state.detailInfo.autoPwd && existErrorMsg('link-pwd-error-msg')) {
        ElMessage.warning(t('work_branch.error_password_hint'))
        return
      }
      if (sharePeRequire.value) {
        const peRequireValid = validatePeRequire()
        if (!peRequireValid) {
          return
        }
      }
      formatLinkAddr()
      let info = linkAddr.value
      if (passwdEnable.value) {
        info += `,${state.detailInfo.pwd}`
      }
      await toClipboard(info)
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
  loadShareInfo(validatePeRequire)
}

const loadShareInfo = cb => {
  showLoading()
  const resourceId = props.resourceId
  const url = `/share/detail/${resourceId}`
  request
    .get({ url })
    .then(res => {
      state.detailInfo = { ...res.data }
      if (res.data?.uuid) {
        originUuid.value = res.data.uuid
      }
      setPageInfo()
    })
    .finally(() => {
      closeLoading()
      cb && cb()
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
    loadShareInfo(null)
  })
}

const formatLinkAddr = () => {
  linkAddr.value = formatLinkBase() + state.detailInfo.uuid
}
const formatLinkBase = () => {
  let prefix = '/'
  if (embeddedStore.baseUrl) {
    prefix = embeddedStore.baseUrl + '#'
  } else {
    prefix = window.location.origin + window.location.pathname + '#'
  }
  if (prefix.includes('oidcbi/') || prefix.includes('casbi/')) {
    prefix = prefix.replace('oidcbi/', '')
    prefix = prefix.replace('casbi/', '')
  }
  return prefix + SHARE_BASE
}

const expEnableSwitcher = val => {
  let exp = 0
  if (val) {
    const now = new Date()
    now.setTime(now.getTime() + 3600 * 1000)
    exp = now.getTime()
    state.detailInfo.exp = exp
  }
  validateExpRequire()
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
    loadShareInfo(null)
  })
}

const pwdEnableSwitcher = val => {
  let pwd = ''
  if (val) {
    pwd = getUuid()
  }
  validatePwdRequire()
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
    loadShareInfo(null)
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
const validatePeRequire = () => {
  if (shareEnable.value && sharePeRequire.value) {
    const expRequireValid = validateExpRequire()
    const pwdRequireValid = validatePwdRequire()
    return expRequireValid && pwdRequireValid
  }
  return true
}

const validateExpRequire = () => {
  if (!sharePeRequire.value || overTimeEnable.value) {
    showCheckboxError(null, expCheckbox)
    return true
  }
  showCheckboxError(t('common.required'), expCheckbox)
  return false
}

const validatePwdRequire = () => {
  if (!sharePeRequire.value || passwdEnable.value) {
    showCheckboxError(null, pwdCheckbox)
    return true
  }
  showCheckboxError(t('common.required'), pwdCheckbox)
  return false
}
const showCheckboxError = (msg, target, className?: string) => {
  if (!target.value) {
    return
  }
  className = className || 'checkbox-span-require'
  const fullClassName = `.${className}`
  const e = target.value.$el
  if (!msg) {
    e.style = null
    e.children[0].children[1].style.borderColor = null
    const child = e.children[1].children[0].querySelector(fullClassName)
    if (child) {
      e.children[1].children[0].removeChild(child)
    }
  } else {
    e.style.color = 'red'
    e.children[0].children[1].style.borderColor = 'red'
    const child = e.children[1].children[0].querySelector(fullClassName)
    if (!child) {
      const errorDom = document.createElement('span')
      errorDom.className = className
      errorDom.innerText = msg
      e.children[1].children[0].appendChild(errorDom)
    } else {
      child.innerText = msg
    }
  }
}
const showPageError = (msg, target, className?: string) => {
  className = className || 'link-pwd-error-msg'
  const fullClassName = `.${className}`
  const domRef = target || pwdRef
  if (!domRef.value) {
    return
  }
  const e = domRef.value.input
  if (!msg) {
    e.style = null
    e.style.borderColor = null
    const child = e.parentElement.querySelector(fullClassName)
    if (child) {
      e.parentElement['style'] = null
      e.parentElement.removeChild(child)
    }
  } else {
    e.style.color = 'red'
    e.style.borderColor = 'red'
    e.parentElement['style']['box-shadow'] = '0 0 0 1px red inset'
    const child = e.parentElement.querySelector(fullClassName)
    if (!child) {
      const errorDom = document.createElement('div')
      errorDom.className = className
      errorDom.innerText = msg
      e.parentElement.appendChild(errorDom)
    } else {
      child.innerText = msg
    }
  }
}
const existErrorMsg = (className: string) => {
  return document.getElementsByClassName(className)?.length
}
const autoEnableSwitcher = val => {
  if (val) {
    showPageError(null, pwdRef)
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
    if (!state.detailInfo.autoPwd && existErrorMsg('link-pwd-error-msg')) {
      ElMessage.warning(t('work_branch.error_password_hint'))
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
const editUuid = () => {
  linkCustom.value = true
  nextTick(() => {
    if (linkUuidRef?.value) {
      linkUuidRef.value.input.focus()
    }
  })
}
const validateUuid = async () => {
  const val = state.detailInfo.uuid
  const className = 'link-uuid-error-msg'
  if (!val) {
    showPageError(t('commons.cannot_be_null'), linkUuidRef, className)
    return false
  }
  const regex = /^[a-zA-Z0-9]{8,16}$/
  const result = regex.test(val)
  if (!result) {
    showPageError(t('work_branch.uuid_checker'), linkUuidRef, className)
  } else {
    const msg = await uuidValidateApi(val)
    showPageError(msg, linkUuidRef, className)
    return !msg
  }
  return result
}

const uuidValidateApi = async val => {
  const url = '/share/editUuid'
  const data = { resourceId: props.resourceId, uuid: val }
  const res = await request.post({ url, data })
  return res.data
}
const finishEditUuid = async () => {
  const uuidValid = await validateUuid()
  linkCustom.value = !uuidValid
}
const resetUuid = event => {
  event.stopPropagation()
  state.detailInfo.uuid = originUuid.value
  finishEditUuid()
}
const openTicket = () => {
  showTicket.value = true
  nextTick(() => {
    ticketDialogRef.value.open()
  })
}
const closeTicket = () => {
  ticketDialogRef.value.close()
  nextTick(() => {
    showTicket.value = false
  })
}
const updateRequireTicket = val => {
  state.detailInfo.ticketRequire = val
}

const execute = () => {
  share()
}

const openPwdDialog = () => {
  customPwdRef.value.open(state.detailInfo.pwd)
}
const customPwdChange = val => {
  state.detailInfo.pwd = val
  resetPwdHandler(val, false)
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
  .custom-link-line {
    display: flex;
    margin-bottom: 16px;
    align-items: center;
    .maxW380 {
      :deep(.ed-input__prefix) {
        overflow: hidden;
        max-width: 380px;
      }
    }
    button {
      width: 40px;
      min-width: 40px;
      margin-left: 8px;
      height: 100%;
    }
    :deep(.link-uuid-error-msg) {
      color: red;
      position: absolute;
      z-index: 9;
      font-size: 10px;
      height: 10px;
      top: 25px;
      width: 350px;
      left: 0px;
    }
  }
}
:deep(.checkbox-span) {
  display: flex;
  align-items: center;
  .pe-require {
    color: red;
    font-size: 10px;
    line-height: 32px;
    margin: 0 4px;
  }
  .checkbox-span-require {
    font-size: 10px;
  }
  .pe-tips-hidden {
    display: none;
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
  width: 220px;
  display: inline-flex;
  column-gap: 12px;
  margin-left: 25px;
  width: 332px;

  :deep(.ed-input-group__append) {
    width: initial !important;
    background: none;
    color: #1f2329;
    padding: 0px 0px !important;
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

.share-input-suffix {
  display: flex;
  height: 30px;
  line-height: 30px;
  column-gap: 4px;
  align-items: center;
  .suffix-split {
    height: 30px;
    width: 1px;
    display: inline-block;
    background-color: #bbbfc4;
    margin-right: 4px;
  }
  .done-finish {
    color: #3370ff;
    &:hover {
      background-color: #3370ff1a !important;
    }
  }
  .input-suffix-btn {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    display: flex;
    align-items: center;
    justify-content: center;
    &:hover {
      background-color: #1f23291a;
    }
    svg {
      width: 16px;
      height: 16px;
    }
  }
}
.link-input-readlonly {
  :deep(.ed-input__wrapper) {
    background-color: rgba(0, 0, 0, 0.1);
    color: #8f959e;
    &:hover {
      box-shadow: 0 0 0 1px var(--ed-input-border-color, var(--ed-border-color)) inset;
    }
    input {
      color: #646a73;
    }
  }
}
</style>
