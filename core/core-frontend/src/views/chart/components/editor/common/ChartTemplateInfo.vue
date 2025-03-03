<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
</script>

<template>
  <div class="view-panel-Mask">
    <el-popover
      :visible="true"
      placement="bottom"
      popper-class="template-popper-tips"
      :width="256"
      show-arrow
    >
      <div class="template-popper-tips-content">
        <p class="constant">{{ t('visualization.template_view_tips') }}</p>
      </div>
      <template #reference>
        <div class="view-panel-mask-inner"></div>
      </template>
    </el-popover>
  </div>
</template>

<style lang="less" scoped>
.view-panel-Mask {
  height: 100%;
  width: 100%;
  background-color: rgba(31, 35, 41);
  opacity: 0.4;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  cursor: not-allowed;
  pointer-events: none;
}
.view-panel-mask-inner {
  top: 51px;
  left: 246px;
  height: 34px;
  width: 170px;
  background: white;
  position: relative;
  pointer-events: none;
  border-radius: 5px;
}
</style>

<style lang="less">
.template-popper-tips {
  z-index: 1000 !important;
  padding: 24px !important;
  box-shadow: none !important;
  border: 0px !important;
  background: var(--ed-color-primary) !important;
  .ed-popper__arrow::before {
    border: 1px solid var(--ed-color-primary) !important;
    background: var(--ed-color-primary) !important;
  }
}
.template-popper-tips-content {
  color: rgba(255, 255, 255, 1);
  .content {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    text-align: left;
  }
}
</style>
