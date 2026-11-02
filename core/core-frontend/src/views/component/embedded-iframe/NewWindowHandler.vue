<template>
  <div class="de-embedded-new-window" />
</template>

<script lang="ts" setup>
import { onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useEmbedded } from '@/store/modules/embedded'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useI18n } from '@/hooks/web/useI18n'
import { ensureDekey, rsaEncryp } from '@/utils/encryption'
import { config } from '@/config/axios/config'
import { initApi } from './embedded'

const embeddedStore = useEmbedded()
const appStore = useAppStoreWithOut()
const { t } = useI18n()
const emits = defineEmits(['loaded'])
let promiseResolve: ((result: boolean | null) => void) | undefined
let openerWindow: Window | null = null
let verifying = false
let finished = false
let disposed = false

const finish = (result: boolean | null) => {
  finished = true
  window.removeEventListener('message', windowMsg)
  promiseResolve?.(result)
}

const windowMsg = async (event: MessageEvent) => {
  const data = event.data
  if (
    disposed ||
    finished ||
    verifying ||
    !openerWindow ||
    event.source !== openerWindow ||
    !event.origin ||
    event.origin === 'null' ||
    !data ||
    typeof data !== 'object' ||
    Array.isArray(data) ||
    data['de-inner-embedded'] !== true ||
    window['uuid'] == null ||
    window['uuid'] !== data.uuid ||
    typeof data.embeddedToken !== 'string' ||
    !data.embeddedToken.trim() ||
    (data.baseUrl != null && typeof data.baseUrl !== 'string')
  ) {
    return
  }

  verifying = true
  try {
    // DIV 嵌入的发送方属于第三方域名，复用应用域名校验，不能仅允许同源。
    // 校验前不写入 token/baseUrl，确保请求使用当前 DataEase 的服务地址。
    if (event.origin !== window.location.origin) {
      await ensureDekey()
      if (disposed) return
      const origin = rsaEncryp(window.btoa(event.origin))
      if (!origin) throw new Error('Origin encryption failed')
      const response = await initApi(data.embeddedToken, origin.toString())
      if (response?.code !== config.result_code) {
        throw new Error('Embedded origin verification failed')
      }
    }
    if (disposed) return

    embeddedStore.setIframeData(data)
    const params = new URL(window.location.href).searchParams
    if (params.get('de-embedded-window-args')) {
      appStore.setIsDataEaseBi(true)
      if (data.baseUrl) embeddedStore.setBaseUrl(data.baseUrl)
    } else {
      appStore.setIsIframe(true)
    }
    finish(true)
  } catch {
    if (!disposed) {
      ElMessage.error(t('data_source.verification_failed'))
      // 失败时不通知调用页面继续初始化，避免回退到当前登录身份。
      finish(false)
    }
  } finally {
    verifying = false
  }
}

const newWindowReady = async (): Promise<boolean | null> => {
  const params = new URL(window.location.href).searchParams
  const json = params.get('de-embedded-window-args')
  if (json) {
    try {
      const param = JSON.parse(decodeURIComponent(window.atob(json)))
      window['uuid'] = param['uuid']
      window.name = param['name']
    } catch {
      ElMessage.error(t('data_source.verification_failed'))
      return false
    }
  }

  const opener = window.opener as Window | null
  openerWindow = opener
  if (!opener || window.name !== 'de-new-resource-window') return null

  return new Promise(resolve => {
    promiseResolve = resolve
    window.addEventListener('message', windowMsg)
    const readyData = {
      ready: true,
      msgOrigin: 'de-inner-fit2cloud',
      uuid: window['uuid']
    }
    opener.postMessage(readyData, '*')
  })
}
onMounted(async () => {
  const result = await newWindowReady()
  if (!disposed && result !== false) emits('loaded')
})
onUnmounted(() => {
  disposed = true
  finish(false)
  openerWindow = null
})
</script>
