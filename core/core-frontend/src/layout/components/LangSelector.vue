<script lang="ts" setup>
import { ref } from 'vue'
import { Icon } from '@/components/icon-custom'
const language = ref(null)
const handleSetLanguage = lang => {
  language.value = lang
}
</script>
<template>
  <el-dropdown
    style="display: flex; align-items: center"
    trigger="click"
    class="international"
    @command="handleSetLanguage"
  >
    <el-icon>
      <Icon name="language" />
    </el-icon>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item :disabled="language === 'zh_CN'" command="zh_CN">
          简体中文
        </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'zh_TW'" command="zh_TW">
          繁体中文
        </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'en_US'" command="en_US">
          English
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="less">
.right-menu-item {
  display: inline-block;
  padding: 10px 8px;
  height: 100%;

  vertical-align: text-bottom;

  &.hover-effect {
    cursor: pointer;
    transition: background 0.3s;

    &:hover {
      background-color: rgba(0, 0, 0, 0.025);
    }
  }
}
</style>
