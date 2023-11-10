<template>
  <p class="router-title">模版管理</p>
  <div class="sys-setting-p">
    <div class="container-sys-param">
      <template-manage></template-manage>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import TemplateManage from '@/views/template/index.vue'

const { t } = useI18n()

const activeName = ref('basic')
const handleClick = (tab, event: Event) => {
  console.log(tab, event)
}
</script>
<style lang="less">
.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: PingFang SC;
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
}
.sys-setting-p {
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 176px);
  box-sizing: border-box;
  margin-top: 12px;
}
.setting-auto-h {
  height: auto !important;
}

.container-sys-param {
  height: 100%;
  overflow-y: auto;
}
</style>
<style lang="less" scoped></style>
