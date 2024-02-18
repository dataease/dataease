<template>
  <el-button ref="shareButtonRef" v-if="props.weight >= 7" v-click-outside="openShare">
    <template #icon>
      <icon name="icon_share-label_outlined"></icon>
    </template>
    {{ t('visualization.share') }}
  </el-button>
  <el-popover
    ref="sharePopoverRef"
    :virtual-ref="shareButtonRef"
    trigger="click"
    title=""
    virtual-triggering
    width="480"
    placement="bottom-start"
    :show-arrow="false"
    popper-class="share-popover"
    @show="share"
  >
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
        <div class="inline-share-item" v-if="state.detailInfo.pwd">
          <el-input v-model="state.detailInfo.pwd" readonly size="small">
            <template #append>
              <div @click="resetPwd" class="share-reset-container">
                <span>{{ t('commons.reset') }}</span>
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
import { ref, reactive, unref, computed } from 'vue'
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
const shareButtonRef = ref()
const sharePopoverRef = ref()

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
    exp: 0
  } as ShareInfo
})

const openShare = () => {
  unref(sharePopoverRef).popperRef?.delayHide?.()
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
  dialogVisible.value = false
  openShare()
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
  resetPwdHandler(pwd)
}
const resetPwd = () => {
  const pwd = getUuid()
  resetPwdHandler(pwd)
}
const resetPwdHandler = (pwd?: string) => {
  const resourceId = props.resourceId
  const url = '/share/editPwd'
  const data = { resourceId, pwd }
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
    width: 45px !important;
    background: none;
    color: #1f2329;
    padding: 0px 0px !important;
    .share-reset-container {
      width: 100%;
      display: flex;
      justify-content: center;
    }
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
</style>
