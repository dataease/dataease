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
const userStore = useUserStoreWithOut()
const loading = ref(true)
const state = reactive({
  domain: '',
  id: '',
  enabled: false,
  valid: false,
  historyShow: false
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
  console.log('==test==0=')
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
    console.log('==test==00=')
    mountedEmbeddedPage()
  }
  document.head.appendChild(script)
}
const mountedEmbeddedPage = () => {
  if (sqlbotExist.value) {
    return
  }
  const tempTimer = setTimeout(() => {
    console.log('==test==1=' + window['sqlbot_assistant_handler'])
    if (window['sqlbot_assistant_handler']) {
      console.log('==test==2=')

      // window['sqlbot_assistant_handler'].mounted('#dataease-v2-embedded-assistant-sqlbot', {
      //   embeddedId: state.id,
      //   online: true,
      //   userFlag: userStore.getUid
      // })

      const container = document.getElementById('sqlbot-assistant-chat-container')
      if (container) {
        const mountPoint = document.createElement('div')
        mountPoint.id = 'chat-component-mount-point'
        container.appendChild(mountPoint)
        const chatApp = createApp(SQDatasetSelect)
        chatApp.mount(mountPoint)
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
