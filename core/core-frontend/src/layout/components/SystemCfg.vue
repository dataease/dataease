<script lang="ts" setup>
import { useRouter, useRoute } from 'vue-router'
const { push } = useRouter()
const route = useRoute()
const redirectUser = () => {
  console.log('route', route)
  push('/system/user')
}
</script>

<template>
  <el-icon @click="redirectUser">
    <Icon class="icon-setting" name="icon-setting" />
  </el-icon>
</template>

<style lang="less" scoped>
.icon-setting {
  cursor: pointer;
}
</style>
