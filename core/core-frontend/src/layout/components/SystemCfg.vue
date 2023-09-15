<script lang="ts" setup>
import { useRouter } from 'vue-router'
const { push, resolve } = useRouter()
const redirectUser = () => {
  const sysMenu = resolve('/system')
  const kidPath = sysMenu.matched[0].children[0].path
  push(`${sysMenu.path}/${kidPath}`)
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
