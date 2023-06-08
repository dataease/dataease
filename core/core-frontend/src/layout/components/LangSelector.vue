<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { switchLangApi } from '@/api/user'
const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()
const language = ref(null)
const handleSetLanguage = lang => {
  const param = { lang }
  switchLangApi(param).then(res => {
    if (!res.msg) {
      language.value = lang
      userStore.setLanguage(lang)
      permissionStore.$reset()
      window.location.reload()
    }
  })
}
onMounted(() => {
  language.value = userStore.getLanguage
})
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
        <el-dropdown-item :disabled="language === 'zh-CN'" command="zh-CN"
          >简体中文</el-dropdown-item
        >
        <el-dropdown-item :disabled="language === 'tw'" command="tw"> 繁體中文 </el-dropdown-item>
        <el-dropdown-item :disabled="language === 'en'" command="en"> English </el-dropdown-item>
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
