<template>
  <p class="router-title">{{ t('commons.system_parameter_setting') }}</p>
  <el-tabs v-model="activeName">
    <el-tab-pane v-for="item in tabArray" :key="item.name" :label="item.label" :name="item.name" />
  </el-tabs>
  <div class="sys-setting-p" :class="{ 'setting-auto-h': activeName !== 'map' }">
    <div class="container-sys-param">
      <map-setting v-if="activeName === 'map'" />
      <basic-info v-if="activeName === 'basic'" />
      <engine-info v-if="activeName === 'engine'" />
      <xpack-component
        jsname="L21lbnUvc2V0dGluZy9lbWFpbC9pbmRleA=="
        v-if="activeName === 'email'"
      />
    </div>
  </div>
  <xpack-component jsname="L2NvbXBvbmVudC9tZW51LWhhbmRsZXIvRW1haWxIYW5kbGVy" @loaded="addTable" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import MapSetting from './map/MapSetting.vue'
import BasicInfo from './basic/BasicInfo.vue'
import EngineInfo from '@/views/system/parameter/engine/EngineInfo.vue'
import { XpackComponent } from '@/components/plugin'
/* import EmailInfo from './email/EmailInfo.vue' */
const { t } = useI18n()

const tabArray = ref([
  { label: '基础设置', name: 'basic' },
  { label: '地图设置', name: 'map' },
  { label: '引擎设置', name: 'engine' }
])

const activeName = ref('basic')

const addTable = tab => {
  if (!tabArray.value.some(item => item.name === tab['name'])) {
    tabArray.value.splice(1, 0, tab)
  }
}
</script>
<style lang="less">
.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
}
.sys-setting-p {
  width: 100%;
  height: calc(100vh - 176px);
  box-sizing: border-box;
  margin-top: 12px;
}
.setting-auto-h {
  height: auto !important;
}

.container-sys-param {
  height: 100%;
  overflow: hidden;
  background: var(--ContentBG, #ffffff);
  border-radius: 4px;
}
</style>
<style lang="less" scoped></style>
