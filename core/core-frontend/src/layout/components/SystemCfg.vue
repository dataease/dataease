<script lang="ts" setup>
import { useRouter } from 'vue-router'
const { push } = useRouter()
</script>

<template>
  <el-icon @click="push('/system/user')">
    <Icon class="icon-setting" name="icon-setting" />
  </el-icon>
</template>

<style lang="less" scoped>
.icon-setting {
  cursor: pointer;
}
</style>
