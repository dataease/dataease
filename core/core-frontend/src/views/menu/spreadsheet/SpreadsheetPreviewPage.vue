<script setup lang="ts">
import { computed, defineAsyncComponent, ref, watch } from 'vue'
import { useRoute } from 'vue-router_2'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
import { useI18n } from '@/hooks/web/useI18n'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import SpreadsheetPreview from '@/pages/panel/SpreadsheetPreview.vue'

const NewWindowHandler = defineAsyncComponent(
  () => import('@/views/component/embedded-iframe/NewWindowHandler.vue')
)
const Entrances = defineAsyncComponent(
  () => import('@/views/component/embedded-iframe/Entrances.vue')
)
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
const route = useRoute()
const { t } = useI18n()
const windowReady = ref(false)
const iframeReady = ref(false)
const authenticationError = ref('')
const previewError = computed(() =>
  appStore.getXpackValid ? authenticationError.value : t('common.no_auth_tips')
)
const authenticationReady = computed(
  () => appStore.getXpackValid && !previewError.value && windowReady.value && iframeReady.value
)
const authenticationFailed = () => {
  authenticationError.value = t('data_source.verification_failed')
}
// 新窗口认证会带入来源资源的信息，目标始终以当前 URL 为准。
const resourceId = computed(() => (typeof route.query.id === 'string' ? route.query.id : ''))

watch(
  authenticationReady,
  ready => {
    // 认证消息携带的是来源页面参数，不得覆盖新 URL 指定的目标参数。
    if (ready) {
      embeddedStore.setOuterParams('')
      embeddedStore.setJumpInfoParam('')
    }
  },
  { immediate: true, flush: 'sync' }
)
</script>

<template>
  <div class="spreadsheet-preview-page" v-loading="!authenticationReady && !previewError">
    <!-- 认证完成后才读取发布版；同页跳转更换参数时销毁旧实例，重新执行首次取数。 -->
    <SpreadsheetPreview
      v-if="authenticationReady && resourceId"
      :key="route.fullPath"
      :resource-id="resourceId"
    />
    <EmptyBackground v-else-if="previewError" :description="previewError" img-type="none" />
    <EmptyBackground
      v-else-if="authenticationReady"
      :description="t('spreadsheet.no_data')"
      img-type="none"
    />
  </div>
  <NewWindowHandler
    v-if="appStore.getXpackValid"
    @loaded="windowReady = true"
    @authentication-failed="authenticationFailed"
  />
  <Entrances
    v-if="appStore.getXpackValid"
    @init-iframe="iframeReady = true"
    @authentication-failed="authenticationFailed"
  />
</template>

<style lang="less" scoped>
.spreadsheet-preview-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
</style>
