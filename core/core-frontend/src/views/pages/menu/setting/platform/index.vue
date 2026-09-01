<template>
  <div class="sys-setting-p" v-loading="loading">
    <div class="container-sys-platform">
      <lark-info />
    </div>
    <div class="container-sys-platform not-first">
      <dingtalk-info ref="dingtalkEditor" />
    </div>
    <div class="container-sys-platform not-first">
      <wecom-info />
    </div>
    <div class="container-sys-platform not-first">
      <larksuite-info />
    </div>
  </div>
</template>

<script lang="ts" setup>
import LarkInfo from '@/views/menu/setting/platform/lark/LarkInfo.vue'
import DingtalkInfo from '@/views/menu/setting/platform/dingtalk/DingtalkInfo.vue'
import WecomInfo from '@/views/menu/setting/platform/wecom/WecomInfo.vue'
import LarksuiteInfo from '@/views/menu/setting/platform/larksuite/LarksuiteInfo.vue'
import { useI18n } from '@/hooks/web/useI18n'
import router from '@/router'
import { onMounted, ref } from 'vue'
const { t } = useI18n()

const editor = router.currentRoute.value.query.edit as string | null

const dingtalkEditor = ref(null)

const loading = ref(true)
onMounted(() => {
  if (editor === 'dingtalk') {
    loading.value = true
    setTimeout(() => {
      dingtalkEditor.value?.edit()
      loading.value = false
    }, 1000)
    return
  }
  loading.value = false
})
</script>
<style lang="less" scoped>
.sys-setting-p {
  width: 100%;
  overflow-y: auto;
  height: calc(100vh - 108px);
  box-sizing: border-box;
  margin-top: 8px;
}
.container-sys-platform {
  padding: 24px;
  overflow: hidden;
  border-radius: 12px;
  background: var(--ContentBG, #ffffff);
}
.not-first {
  margin-top: 16px;
}
</style>
