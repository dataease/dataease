<script lang="ts" setup>
import { ref } from 'vue'
import { ElHeader, ElMenu, ElMenuItem } from 'element-plus-secondary'
import { Icon } from '@/components/Icon'
import { useI18n } from '@/hooks/web/useI18n'

const activeIndex = ref('1')
const handleSelect = (key: string, keyPath: string[]) => {
  console.log(key, keyPath)
}
const { t } = useI18n()
</script>

<template>
  <el-header class="header-flex">
    <Icon className="logo" name="area"></Icon>
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
    >
      <el-menu-item index="1">Processing Center1</el-menu-item>
      <el-menu-item index="2">Processing Center2</el-menu-item>
      <el-menu-item index="3">Processing Center3</el-menu-item>
    </el-menu>
    <div class="operate-setting">{{ t('common.inputText') }}</div>
  </el-header>
</template>

<style lang="less" scoped>
.header-flex {
  display: flex;
  align-items: center;
  .operate-setting {
    margin-left: auto;
  }
}
.flex-grow {
  flex-grow: 1;
}
.logo {
  width: 140px;
  height: 45px;
}
</style>
