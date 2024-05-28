<script lang="ts" setup>
import noLic from './nolic.vue'
import { ref, useAttrs, nextTick, shallowRef, computed, reactive, watch, onMounted } from 'vue'
import { execute, randomKey, formatArray } from './convert'
import { load } from '@/api/plugin'
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'
import { i18n } from '@/plugins/vue-i18n'
import * as Vue from 'vue'
import axios from 'axios'
import * as Pinia from 'pinia'
import * as vueI18n from 'vue-i18n'
import * as vueRouter from 'vue-router'
import { useEmitt } from '@/hooks/web/useEmitt'

const target = ref()

onMounted(() => {
  window.Vue = Vue
  window.Axios = axios
  window.Pinia = Pinia
  window.vueI18n = vueI18n
  window.vueRouter = vueRouter
  window.MittAll = useEmitt().emitter.all
  window.I18n = i18n
  const xhr = new XMLHttpRequest()
  xhr.onreadystatechange = () => {
    if (xhr.readyState !== xhr.DONE) return
    async function getDEXPack() {
      const xpack = await window.DEXPack.mapping[attrs.jsname]
      target.value = xpack.default
    }

    nextTick(() => {
      getDEXPack()
    })
  }
  xhr.open('get', 'http://192.168.31.47:8000/DEXPack.umd.js')
  xhr.send()
})

const { wsCache } = useCache()

const plugin = ref()

const loading = ref(false)

const attrs = useAttrs()

const showNolic = () => {
  plugin.value = noLic
  loading.value = false
}
const generateRamStr = (len: number) => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let randomStr = ''
  for (var i = 0; i < len; i++) {
    randomStr += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return randomStr
}

const importProxy = (bytesArray: any[]) => {
  const promise = import(
    `../../../../../../${formatArray(bytesArray[6])}/${formatArray(bytesArray[7])}/${formatArray(
      bytesArray[8]
    )}/${formatArray(bytesArray[9])}/${formatArray(bytesArray[10])}.vue`
  )
  promise
    .then((res: any) => {
      plugin.value = res.default
    })
    .catch(e => {
      console.error(e)
      showNolic()
    })
}

const loadComponent = () => {
  loading.value = true
  const byteArray = wsCache.get(`de-plugin-proxy`)
  if (byteArray) {
    importProxy(JSON.parse(byteArray))
    loading.value = false
    return
  }
  const key = generateRamStr(randomKey())
  load(key)
    .then(response => {
      let code = response.data
      const byteArray = execute(code, key)
      storeCacheProxy(byteArray)
      importProxy(byteArray)
    })
    .catch(() => {
      emits('loadFail')
      showNolic()
    })
    .finally(() => {
      loading.value = false
    })
}
const storeCacheProxy = byteArray => {
  const result = []
  byteArray.forEach(item => {
    result.push([...item])
  })
  wsCache.set(`de-plugin-proxy`, JSON.stringify(result))
}
loadComponent()
const pluginProxy = ref(null)
const invokeMethod = param => {
  pluginProxy.value['invokeMethod'](param)
}
const emits = defineEmits(['loadFail'])
defineExpose({
  invokeMethod
})
</script>

<template>
  <component ref="pluginProxy" :is="plugin" v-loading="loading" v-bind="attrs"></component>
</template>

<style lang="less" scoped></style>
