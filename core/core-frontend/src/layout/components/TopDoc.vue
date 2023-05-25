<script lang="ts" setup>
import { ref } from 'vue'
import { Icon } from '@/components/icon-custom'
const helpLink = ref(null)
const openBlank = () => {
  window.open(helpLink.value)
}
</script>

<template>
  <el-icon @click="openBlank">
    <Icon name="docs" />
  </el-icon>
</template>
