<template>
  <div
    id="dataease-v2-embedded-assistant-sqlbot"
    class="dataease-v2-embedded-assistant-sqlbot"
  ></div>
</template>

<script lang="ts" setup>
import { createApp, onMounted, onUnmounted, reactive, ref } from 'vue'
import request from '@/config/axios'
import { useUserStoreWithOut } from '@/store/modules/user'
import SQDatasetSelect from '@/views/sqlbot/SQDatasetSelect.vue'
import AssistantHead from '@/views/sqlbot/AssistantHead.vue'
const userStore = useUserStoreWithOut()
const loading = ref(true)
const state = reactive({
  domain: '',
  id: '',
  enabled: false,
  valid: false,
  historyShow: false,
  sqlbotScript: null
})
const sqlbotExist = ref(false)
const timer = ref()

const loadSqlbotInfo = () => {
  const url = '/sysParameter/sqlbot'
  request.get({ url }).then(res => {
    if (res && res.data) {
      const { domain, id, enabled, valid } = res.data
      if (!enabled) {
        console.error('sqlbot embedded disabled')
      }
      if (!valid) {
        console.error('sqlbot embedded invalid')
      }
      state.domain = domain
      state.id = id
      state.enabled = enabled
      state.valid = valid
      loadSqlbotPage()
    }
  })
}

const loadSqlbotPage = () => {
  const scriptId = `sqlbot-assistant-float-script-${state.id}`
  const exitsScript = document.getElementById(scriptId)
  if (exitsScript && window['sqlbot_assistant_handler']) {
    mountedEmbeddedPage()
    return
  }
  const script = document.createElement('script')
  script.defer = true
  script.async = true
  script.id = scriptId
  let sqlbotDomain = state.domain
  if (sqlbotDomain.endsWith('/')) {
    sqlbotDomain = sqlbotDomain.slice(0, -1)
  }
  script.src = `${sqlbotDomain}/assistant.js?id=${state.id}&online=true&userFlag=${
    userStore.getUid
  }&t=${new Date().getTime()}`
  script.onload = () => {
    mountedEmbeddedPage()
  }
  document.head.appendChild(script)
  state.sqlbotScript = script
}
const mountedEmbeddedPage = () => {
  if (sqlbotExist.value) {
    return
  }
  const tempTimer = setTimeout(() => {
    if (window['sqlbot_assistant_handler']) {
      const container = document.getElementById('sqlbot-assistant-chat-container')
      if (container) {
        // 数据集选择
        const mountPoint = document.createElement('div')
        mountPoint.id = 'chat-component-mount-point'
        container.appendChild(mountPoint)
        const chatApp = createApp(SQDatasetSelect, {
          // 在这里传递 props
          assistantId: state.id
        })
        chatApp.mount(mountPoint)

        // 头部样式
        const mountPointHead = document.createElement('div')
        mountPointHead.id = 'chat-component-mount-point-head'
        container.appendChild(mountPointHead)
        const chatHeadApp = createApp(AssistantHead)
        chatHeadApp.mount(mountPointHead)
      }
      loading.value = false
      sqlbotExist.value = true
      if (tempTimer) {
        clearTimeout(tempTimer)
      }
    }
  }, 2000)
}

onMounted(() => {
  loadSqlbotInfo()
  timer.value = setInterval(() => {
    loadSqlbotInfo()
  }, 30000)
})

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }

  // 移除 script 标签
  if (state.sqlbotScript && state.sqlbotScript.parentNode) {
    state.sqlbotScript.parentNode.removeChild(state.sqlbotScript)
  }

  // 可选：清理全局变量
  if (window['sqlbot_assistant_handler']) {
    delete window['sqlbot_assistant_handler']
  }
})
</script>

<style lang="less">
#sqlbot-assistant-chat-container {
  z-index: 200;
}
</style>

<style lang="less" scoped>
.dataease-v2-embedded-assistant-sqlbot {
  width: 20px;
  height: 20px;
  overflow: hidden;
  position: absolute;
  display: flex;
}
</style>
