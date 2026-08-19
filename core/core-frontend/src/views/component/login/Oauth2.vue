<template>
  <div
    @click="execute"
    class="item OAUTH"
    >
    <el-icon>
      <Icon name="logo_oauth"><logo_oauth class="svg-icon" /></Icon>
    </el-icon>
    <span class="name">
      OAuth2
    </span>
  </div>
</template>

<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import request from '@/config/axios'
import logo_oauth from "@/assets/svg/logo_oauth.svg";

const emits = defineEmits(['switch-category'])
const execute = () => {
  emits('switch-category', {category: 'oauth2', proxy: '/#'})
}

const toLoginPage = () => {
  const url = '/oauth2/auth'
  request.get({ url }).then(res => {
    const data = res.data
    if (data?.authEndpoint) {
      localStorage.removeItem('DE_OAUTH2_CODE_KEY')
      if (data.codeKey && data.codeKey !== 'null' && typeof data.codeKey !== 'undefined' && data.codeKey !== 'code') {
        localStorage.setItem('DE_OAUTH2_CODE_KEY', data.codeKey)
      }
      const redirectUri = encodeURIComponent(data.redirectUri)
      const result = `${data.authEndpoint}?response_type=code&client_id=${data.clientId}&scope=${data.scope}&state=${data.state}&redirect_uri=${redirectUri}`
      window.open(result, '_self')
    }
  })
}
defineExpose({
  toLoginPage
})
</script>

<style lang="less" scoped>
.item {
  width: 32px;
  cursor: pointer;

  &.qrcode,
  &.account {
    .ed-icon {
      padding: 5px;
    }
  }

  .ed-icon {
    font-size: 32px;
    border: 1px solid #dee0e3;
    border-radius: 50%;
  }
  display: flex;
  align-items: center;
  flex-direction: column;
  justify-content: space-between;

  .name {
    margin-top: 8px;
    color: #000;
    text-align: center;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px; /* 166.667% */
    display: none;
  }
}
</style>
