<template>
  <el-tag closable>Tag 1</el-tag>
  {{ size }}
  <el-button @click="btn('Auth')">click</el-button>
  <component :is="Header"></component>
</template>
<script setup lang="ts">
import { shallowRef } from 'vue'
import { ElTag } from 'element-plus-secondary'
import { ElButton } from 'element-plus-secondary'
import { useAppStore } from '@/store/modules/app'
import { computed } from 'vue'
const app = useAppStore()
const size = computed(() => app.getSize)

let Header = shallowRef(null)
const btn = (type: string) => {
  import(`../../../../../xpack-sub/${type}.vue`).then((res: any) => {
    Header.value = res.default
  })
}
</script>
