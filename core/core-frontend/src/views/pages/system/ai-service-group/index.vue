<script setup lang="ts">
import { watch } from 'vue'
import { useRoute, useRouter } from 'vue-router_2'

const route = useRoute()
const router = useRouter()

// 兼容原有 /sys-setting/ai-service 入口，跳转当前身份可访问的首个子菜单。
watch(
  () => route.name,
  name => {
    if (name !== 'ai-service-group') return
    const first = route.matched.at(-1)?.children.find(child => !child.meta?.hidden)
    if (first?.name) router.replace({ name: first.name })
  },
  { immediate: true }
)
</script>

<template><router-view /></template>
