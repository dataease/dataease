<template>
  <div id="app">
    <router-view />
    <plugin-com
      v-show="false"
      ref="de-theme"
      component-name="ThemeSetting"
    />
  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'

export default {
  name: 'App',
  components: { PluginCom },
  beforeCreate() {

  }
}
</script>
