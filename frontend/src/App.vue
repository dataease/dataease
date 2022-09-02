<template>
  <div id="app">
    <router-view />
    <plugin-com v-show="false" ref="de-theme" component-name="ThemeSetting" />
  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'

export default {
  name: 'App',
  components: { PluginCom },
  beforeCreate() {

  },
  mounted() {
    const attachParams = this.getQueryVariable('attachParams')
    console.log('attachParams,,,',attachParams)
    localStorage.setItem('permissionId',attachParams)
  },
  methods: {
    getQueryVariable(variable) {
      // let query = window.location.search.substring(1)
      let query = window.location.href.split('?')[1]
      console.log('query',query)
      let vars = []
      if (!query) {
        query = document.cookie
        vars = query.split(';')
      } else {
        vars = query.split('&')
      }
      for (var i = 0; i < vars.length; i++) {
        const pair = vars[i].split('=')
        if (pair[0].trim() === variable) {
          return pair[1]
        }
      }
      return (null)
    }
  }
}
</script>

