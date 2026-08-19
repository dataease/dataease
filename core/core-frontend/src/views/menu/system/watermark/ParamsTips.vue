<template>
  <div class="tips-class">
    <el-tooltip class="item" effect="dark" placement="bottom">
      <template #content>
        <div>
          <span>{{ t('watermark.support_params') }}</span
          ><br />
          <span>${username}-{{ t('watermark.account') }}</span
          ><br />
          <span>${nickName}-{{ t('watermark.nick_name') }}</span
          ><br />
          <span>${time}-{{ t('watermark.now') }}</span
          ><br />
          <span>${ip}-IP</span><br />
        </div>
      </template>
      <span
        ><el-icon><InfoFilled /></el-icon
      ></span>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
</script>

<style scoped lang="less">
.tips-class {
  position: absolute;
  right: 10px;
  z-index: 10;
  top: 5px;
}
</style>
