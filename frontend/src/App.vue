<template>
  <div id="app">
    <keep-alive>
      <router-view v-if="['/dataset/index', '/system/system-settings/appearance'].includes(key)"></router-view>
    </keep-alive>
    <router-view :key="key" v-if="!['/dataset/index', '/system/system-settings/appearance'].includes(key)"></router-view>
    <plugin-com v-show="false" ref="de-theme" component-name="ThemeSetting" />
  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'

export default {
  name: 'App',
  components: { PluginCom },
  computed: {
    key() {
      return this.$route.path
    }
  }
}
</script>
