<script lang="ts" setup>
import icon_done_outlined from '@/assets/svg/icon_done_outlined.svg'
import { ref, onMounted, reactive } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { switchLangApi } from '@/api/user'
const permissionStore = usePermissionStoreWithOut()
const userStore = useUserStoreWithOut()
const localeStore = useLocaleStoreWithOut()

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

const options = reactive([])

onMounted(async () => {
  language.value = userStore.getLanguage
  const localeMap = await localeStore.getLocaleMap
  localeMap.forEach(item => {
    const option = { value: item['lang'], name: item['name'] }
    options.push(option)
  })
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
        <Icon name="icon_done_outlined"><icon_done_outlined class="svg-icon" /></Icon>
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
