<template>
  <div v-if="userStore.proxyInfo?.proxyOid" class="proxy-warn-bar">
    <div class="proxy-warn-content">
      <el-icon class="warn-icon"><WarningFilled /></el-icon>
      <span class="warn-text">{{ t('proxy.warn_text') }}</span>
      <el-button class="proxy-warn-btn" type="warning" plain size="mini" @click="exitProxy">{{ t('proxy.exit_proxy') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { useUserStoreWithOut } from '@/store/modules/user'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'

const { t } = useI18n()
const userStore = useUserStoreWithOut()

const exitProxy = () => {
  const original = userStore.proxyInfo
  // 先清代理信息：点击后新请求立即不再携带 X-DE-ADMIN-PROXY header
  userStore.setProxyInfo({
    proxy: false,
    proxyOid: null,
    proxySecret: null
  })
  request.post({url: '/user/proxyClear'}).then(() => {
    userStore.setUid('')
    location.href = location.origin + location.pathname
  }).catch(() => {
    userStore.setProxyInfo(original)
  })
}
</script>

<style lang="less" scoped>
.proxy-warn-bar {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fdf6ec;
  border: 1px solid var(--van-orange-dark);

  .proxy-warn-content {
    display: flex;
    align-items: center;
    gap: 8px;
    .proxy-warn-btn {
      height: 22px;
    }
    .warn-icon {
      font-size: 18px;
      color: var(--van-orange-dark);
    }

    .warn-text {
      font-size: 14px;
      color: var(--van-orange-dark);
      margin-right: 24px;
    }
  }
}
</style>
