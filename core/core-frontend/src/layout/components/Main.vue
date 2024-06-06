<script lang="ts" setup>
import { ElMain } from 'element-plus-secondary'
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <el-main>
    <RouterView :key="route.path" />
  </el-main>
</template>
