<template>
  <p class="router-title">{{ t('commons.system_parameter_setting') }}</p>
  <el-tabs v-model="activeName">
    <el-tab-pane v-for="item in tabArray" :key="item.name" :label="item.label" :name="item.name" />
  </el-tabs>
  <div class="sys-setting-p">
    <div
      class="container-sys-param"
      :class="{ 'setting-max-h': activeName === 'map', 'basic-info_bg': activeName === 'basic' }"
    >
      <map-setting v-if="activeName === 'map'" />
      <basic-info v-if="activeName === 'basic'" />
      <engine-info v-if="activeName === 'engine'" />
      <xpack-component
        jsname="L21lbnUvc2V0dGluZy9lbWFpbC9pbmRleA=="
        v-if="activeName === 'email'"
      />
      <third-party v-if="activeName === 'third_party'" />
    </div>
  </div>
  <xpack-component jsname="L2NvbXBvbmVudC9tZW51LWhhbmRsZXIvRW1haWxIYW5kbGVy" @loaded="addTable" />
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import MapSetting from './map/MapSetting.vue'
import BasicInfo from './basic/BasicInfo.vue'
import ThirdParty from './third-party/index.vue'
import EngineInfo from '@/views/system/parameter/engine/EngineInfo.vue'
import { XpackComponent } from '@/components/plugin'
import { isDesktop } from '@/utils/ModelUtil'
/* import EmailInfo from './email/EmailInfo.vue' */
const { t } = useI18n()

const desktop = isDesktop()
const tabArray = ref([
  { label: t('system.basic_settings'), name: 'basic' },
  { label: t('system.map_settings'), name: 'map' },
  { label: t('system.engine_settings'), name: 'engine' },
  {
    label: t('common.third_party_embed'),
    name: 'third_party'
  }
])

const activeName = ref('basic')

const addTable = tab => {
  if (!tabArray.value.some(item => item.name === tab['name'])) {
    tabArray.value.splice(1, 0, tab)
  }
}

onMounted(() => {
  if (desktop) {
    let len = tabArray.value.length
    while (len--) {
      if (tabArray.value[len]['name'] === 'third_party') {
        tabArray.value.splice(len, 1)
        break
      }
    }
  }
})
</script>
<style lang="less">
.router-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: var(--de-custom_font, 'PingFang');
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

.container-sys-param {
  max-height: 100%;
  height: auto;
  overflow-y: auto;
  background: var(--ContentBG, #ffffff);
  border-radius: 4px;
  &.basic-info_bg {
    background: none;
  }
}
.setting-max-h {
  height: 100% !important;
}
</style>
