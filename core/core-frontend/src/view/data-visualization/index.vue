<template>
  <el-row> 数据大屏测试 el-row</el-row>
</template>
<script setup lang="ts">
import { useAppStore } from '@/store/modules/app'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { computed, toRaw } from 'vue'
const app = useAppStore()
const locale = useLocaleStoreWithOut()
const size = computed(() => app.getSize)
const getLocaleMap = computed(() => locale.getLocaleMap)
// console.log('getLocaleMap', yy, cc)
</script>
