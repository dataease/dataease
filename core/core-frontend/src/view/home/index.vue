<template>
  <el-tag closable>Tag 1</el-tag>

  {{ size }}
</template>
<script setup lang="ts">
import { ElTag } from 'element-plus-secondary'
// import { ElTag } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { computed, toRaw } from 'vue'
const app = useAppStore()
const locale = useLocaleStoreWithOut()
const size = computed(() => app.getSize)
const getLocaleMap = computed(() => locale.getLocaleMap)
// console.log('getLocaleMap', yy, cc)
</script>
