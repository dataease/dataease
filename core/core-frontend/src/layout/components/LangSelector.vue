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
const options = [
  { value: 'zh-CN', name: '简体中文' },
  { value: 'tw', name: '繁體中文' },
  { value: 'en', name: 'English' }
]
onMounted(() => {
  language.value = userStore.getLanguage
})
</script>
<template>
  <div class="language-container">
    <div
      v-for="item in options"
      :key="item.value"
      class="language-item"
      :class="{ active: language === item.value }"
      @click="handleSetLanguage(item.value)"
    >
      <span>{{ item.name }}</span>
      <el-icon v-if="language === item.value">
        <Icon name="icon_done_outlined"></Icon>
      </el-icon>
    </div>
  </div>
</template>

<style lang="less" scoped>
.language-item {
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  &:hover {
    cursor: pointer;
    background-color: #f2f2f2;
  }
}
.active {
  color: var(--ed-color-primary) !important;
}
</style>
