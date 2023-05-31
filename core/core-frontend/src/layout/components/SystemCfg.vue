<script lang="ts" setup>
import { useRouter } from 'vue-router'
const { push } = useRouter()
const redirectUser = () => {
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
