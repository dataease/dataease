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
  <div class="sys-setting">
    <el-icon @click="redirectUser">
      <Icon class="icon-setting" name="icon-setting" />
    </el-icon>
  </div>
</template>

<style lang="less" scoped>
.sys-setting {
  margin: 0 10px 0 0;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
}
</style>
