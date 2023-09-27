<template>
  <el-tag closable>Tag 1</el-tag>
  {{ size }}
  <el-button @click="btn('Auth')">click</el-button>
  <el-button @click="getEyeDropper">click</el-button>
  <component :is="Header"></component>
</template>
<script setup lang="ts">
import { useEyeDropper } from '@vueuse/core'
import { shallowRef } from 'vue'
import { ElTag, ElMessage } from 'element-plus-secondary'
import { ElButton } from 'element-plus-secondary'
import { useAppStore } from '@/store/modules/app'
import { computed } from 'vue'
const app = useAppStore()
const size = computed(() => app.getSize)
const { isSupported, open, sRGBHex } = useEyeDropper()
const getEyeDropper = async () => {
  if (!isSupported) return
  await open()
    .then(res => {
      console.warn(res)
    })
    .catch(cancel => {
      console.warn(cancel)
    })
}
let Header = shallowRef(null)
const btn = (type: string) => {
  import(`../../../../../xpack-sub/${type}.vue`).then((res: any) => {
    Header.value = res.default
  })
  ElMessage({
    duration: 0,
    message: 'Congrats, this is a success message.',
    type: 'success'
  })
}
</script>
