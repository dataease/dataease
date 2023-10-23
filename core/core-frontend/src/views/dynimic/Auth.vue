<script lang="ts" setup>
import { computed } from 'vue'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
const locale = useLocaleStoreWithOut()
const getLocaleMap = computed(() => locale.getLocaleMap)
</script>

<template>
  <div>auth123</div>
</template>

<style lang="less" scoped></style>
