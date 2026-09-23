<template>
  <el-tabs v-model="activeName">
    <el-tab-pane v-for="item in tabArray" :key="item.name" :label="item.label" :name="item.name" />
  </el-tabs>
  <div class="sys-setting-p">
    <div
      class="container-sys_param"
      :class="{ 'setting-max-h': activeName === 'map', 'engine-bg': activeName === 'basic' }"
    >
      <map-setting v-if="activeName === 'map'" />
      <basic-info v-if="activeName === 'basic'" />
      <engine-info v-if="activeName === 'engine'" />
      <Email v-if="appStore.getXpackValid && activeName === 'email'" />
      <!-- <third-party v-if="activeName === 'third_party'" /> -->
    </div>
  </div>
  <EmailHandler v-if="appStore.getXpackValid" @loaded="addTable" />
</template>

<script lang="ts" setup>
import { ref, defineAsyncComponent } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import MapSetting from '@/views/system/parameter/map/MapSetting.vue'
import BasicInfo from '@/views/system/parameter/basic/BasicInfo.vue'
/* import ThirdParty from '@/views/system/parameter/third-party/index.vue' */
import EngineInfo from '@/views/system/parameter/engine/EngineInfo.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
const EmailHandler = defineAsyncComponent(
  () => import('@/views/component/menu-handler/EmailHandler.vue')
)
const Email = defineAsyncComponent(() => import('@/views/menu/setting/email/index.vue'))
/* import EmailInfo from '@/views/system/parameter/email/EmailInfo.vue' */
const { t } = useI18n()
const appStore = useAppStoreWithOut()

const tabArray = ref([
  { label: t('system.basic_settings'), name: 'basic' },
  { label: t('system.map_settings'), name: 'map' },
  { label: t('system.engine_settings'), name: 'engine' }
  /*{
    label: t('common.third_party_embed'),
    name: 'third_party'
  }*/
])

const activeName = ref('basic')

const addTable = tab => {
  if (!tabArray.value.some(item => item.name === tab['name'])) {
    tabArray.value.splice(1, 0, tab)
  }
}
</script>
<style lang="less" scoped>
.sys-setting-p {
  width: 100%;
  height: calc(100vh - 155px);
  box-sizing: border-box;
  margin-top: 12px;
}

.container-sys_param {
  max-height: 100%;
  height: auto;
  overflow-y: auto;
  border-radius: 12px;
  background: #ffffff;

  &.engine-bg {
    background: transparent;
  }
}
.setting-max-h {
  height: 100% !important;
}
</style>
