<template>
  <div class="page-not-found">
    <Icon className="login-logo-icon" name="403"></Icon>
    <span class="span-403">{{ t('common.no_auth_tips') }}</span>
  </div>
</template>

<script lang="ts" setup>
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
</script>
<style lang="less">
.page-not-found {
  width: 100%;
  top: calc(50% - 100px);
  position: absolute;
  text-align: center;
}
.span-403 {
  display: block;
  margin: 0;
  font-size: var(--ed-font-size-base);
  color: var(--ed-text-color-secondary);
}
</style>
