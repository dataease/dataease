<template>
  <div
    @click="execute"
    class="item OIDC"
    >
    <el-icon>
        <Icon name="btn_oidc"><btn_oidc class="svg-icon" /></Icon>
    </el-icon>
    <span class="name">
        OIDC
    </span>
  </div>
</template>

<script lang="ts" setup>
import btn_oidc from '@/assets/svg/btn_oidc.svg'
import { Icon } from '@/components/icon-custom'
const emits = defineEmits(['switch-category'])
const execute = () => {
  emits('switch-category', {category: 'oidc', proxy: '/oidcbi/#'})
}
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
