<script setup lang="ts">
import configGlobal from '@/components/config-global/src/ConfigGlobal.vue'
import { useRoute } from 'vue-router'
const route = useRoute()
</script>
<template>
  <config-global>
    <router-view :key="route.path" />
  </config-global>
</template>
