<template>
  <div
    id="dataease-v2-embedded-sqlbot"
    v-loading="loading"
    class="dataease-v2-embedded-sqlbot"
  ></div>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/config/axios'
const loading = ref(true)
const state = reactive({
  domain: '',
  id: '',
  enabled: false,
  valid: false
})

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
  const scriptId = 'dataease-v2-embedded-sqlbot-script'
  const exitsScript = document.getElementById(scriptId)
  if (exitsScript && window['sqlbot_embedded_handler']) {
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
  script.src = `${sqlbotDomain}/xpack_static/sqlbot-embedded-dynamic.umd.js?t=${new Date().getTime()}`
  script.onload = () => {
    mountedEmbeddedPage()
  }
  document.head.appendChild(script)
}

const mountedEmbeddedPage = () => {
  setTimeout(() => {
    if (window['sqlbot_embedded_handler']) {
      window['sqlbot_embedded_handler'].mounted('#dataease-v2-embedded-sqlbot', {
        embeddedId: state.id,
        online: true
      })
      loading.value = false
    }
  }, 2000)
}

onMounted(() => {
  loadSqlbotInfo()
})
</script>

<style lang="less" scoped>
.dataease-v2-embedded-sqlbot {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  display: flex;
}
</style>
