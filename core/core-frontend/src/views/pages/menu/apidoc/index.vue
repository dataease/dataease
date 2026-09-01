<template>
  <div v-if="!loading" class="doc-container">
    <iframe src="" id="de2-api-iframe" scrolling="no" width="100%" height="100%" frameborder="0" />
  </div>
  <div v-else class="doc-container" />
</template>

<script lang="ts" setup>
import { ref, nextTick } from 'vue'
import { userInfo } from '@/api/user'
import router from '@/router'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache()
const loading = ref(true)
const init = () => {
  fetchUserInfo()
}

const fetchUserInfo = () => {
  if (!wsCache.get('user.token')) {
    loading.value = false
    toLogin()
  }
  userInfo()
    .then(res => {
      if (res?.data?.id) {
        loadIframe()
      } else {
        toLogin()
      }
    })
    .catch(() => {
      toLogin()
    })
    .finally(() => {
      loading.value = false
    })
}

const loadIframe = () => {
  loading.value = false
  nextTick(() => {
    const cIframeArray = document.getElementById('de2-api-iframe')
    if (!cIframeArray) {
      return
    }
    const pathname = getPathname()
    const time = new Date().getTime()
    const salt = window.btoa('fit2cloud-dataease-v2-api')
    const url = `${pathname}/doc.html?time=${time}&salt=${salt}`
    cIframeArray.setAttribute('src', url)
    cIframeArray.onload = () => {
      const innerDoc = cIframeArray.contentDocument
      if (innerDoc) {
        const token = wsCache.get('user.token')
        const scriptTag = innerDoc.createElement('script')
        scriptTag.type = 'text/javascript'
        scriptTag.id = 'fit2cloud-de2-api'
        const scriptText = `var deToken = "${token}";var target = XMLHttpRequest.prototype.send;XMLHttpRequest.prototype.send = function(...args) { console.log(this);this.setRequestHeader("X-DE-TOKEN", deToken);return target.apply(this, args); }`
        scriptTag.innerHTML = scriptText
        innerDoc.head.appendChild(scriptTag)
      } else {
        toLogin()
      }
    }
  })
}

const getPathname = () => {
  let pathname = window.location.pathname
  if (pathname) {
    if (pathname.includes('oidcbi/') || pathname.includes('casbi/')) {
      pathname = pathname.replace('oidcbi/', '')
      pathname = pathname.replace('casbi/', '')
    }
    pathname = pathname.substring(0, pathname.length - 1)
  }
  return pathname
}

const toLogin = () => {
  let queryRedirectPath = '/workbranch/index'
  // 如果redirect参数中有值
  if (router.currentRoute.value.fullPath) {
    queryRedirectPath = router.currentRoute.value.fullPath as string
  }
  router.push(`/login?redirect=${queryRedirectPath}`)
}

init()
</script>

<style lang="less" scoped>
.doc-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
</style>
