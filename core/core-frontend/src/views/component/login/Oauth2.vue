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
import { prepareOauth2Login } from '@/utils/oauth2Login'
import logo_oauth from "@/assets/svg/logo_oauth.svg";

const emits = defineEmits(['switch-category'])
const execute = () => {
  emits('switch-category', {category: 'oauth2', proxy: '/#'})
}

const toLoginPage = () => {
  prepareOauth2Login().then(url => window.open(url, '_self'))
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
