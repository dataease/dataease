<template>
  <p class="router-title">{{ t('commons.system_parameter_setting') }}</p>
  <el-tabs v-model="activeName">
    <el-tab-pane v-for="item in tabArray" :key="item.name" :label="item.label" :name="item.name" />
  </el-tabs>
  <div class="sys-setting-p" :class="{ 'setting-auto-h': activeName !== 'map' }">
    <div class="container-sys-param">
      <map-setting v-if="activeName === 'map'" />
      <basic-info v-if="activeName === 'basic'" />
      <!-- <email-info v-if="activeName === 'email'" /> -->
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import MapSetting from './map/MapSetting.vue'
import BasicInfo from './basic/BasicInfo.vue'
/* import EmailInfo from './email/EmailInfo.vue' */
const { t } = useI18n()

const tabArray = [
  { label: '基础设置', name: 'basic' },
  /* { label: '邮件设置', name: 'email' }, */
  { label: '地图设置', name: 'map' }
  /* {label: '引擎设置', name: 'engine'}, */
]
const activeName = ref('basic')
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
