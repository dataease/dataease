<template>
  <div class="sys-setting-p">
    <div class="mfa-div">
      <mfa-info />
    </div>

    <div class="hmac-div mfa-div">
      <hmac-info />
    </div>
  </div>
</template>

<script lang="ts" setup>
import MfaInfo from '@/views/menu/setting/security/mfa/MfaInfo.vue'
import HmacInfo from '@/views/menu/setting/security/hmac/HmacInfo.vue'

import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
</script>
<style lang="less" scoped>
.sys-setting-p {
  width: 100%;
  overflow-y: auto;
  height: calc(100vh - 108px);
  box-sizing: border-box;
  margin-top: 8px;
  .mfa-div {
    max-height: 100%;
    height: auto;
    overflow-y: auto;
    background: var(--ContentBG, #ffffff);
    border-radius: 12px;
  }
  .hmac-div {
    margin-top: 16px;
  }
}
</style>
