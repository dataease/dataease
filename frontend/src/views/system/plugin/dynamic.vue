<template>
  <layout-content v-if="!noLayout" v-loading="$store.getters.loadingMap[$store.getters.currentPath]" :header="header" :back-name="backName">
    <async-component v-if="showAsync" :url="url" @execute-axios="executeAxios" @on-add-languanges="addLanguages" @on-plugin-layout="setLayoutInfo" @plugin-call-back="pluginCallBack" />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </layout-content>
  <div v-else>
    <async-component v-if="showAsync" :url="url" @execute-axios="executeAxios" @on-add-languanges="addLanguages" @on-plugin-layout="setLayoutInfo" @plugin-call-back="pluginCallBack" />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </div>

</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import AsyncComponent from '@/components/AsyncComponent'
import i18n from '@/lang'
import bus from '@/utils/bus'
import { execute } from '@/api/system/dynamic'
export default {
  name: 'Dynamic',
  components: {
    LayoutContent,
    AsyncComponent
  },
  props: {
    jsname: {
      type: String,
      default: null
    },
    menuid: {
      type: Number,
      default: null
    },
    noLayout: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showAsync: false,
      header: null,
      backName: null,
      baseUrl: '/api/pluginCommon/async/',
      url: null
    }
  },
  created() {
    if (this.jsname && this.menuid) {
      this.showAsync = true
      // console.log(this.jsname)
      this.url = this.baseUrl + this.menuid
      //   this.url = 'http://localhost:8081/PluginDemo.js'
    //   this.url = 'http://localhost:8081/SystemParam.js'
    } else {
      this.showAsync = false
    }
  },

  methods: {
    // hasLicense
    executeAxios(options) {
      execute(options).then(res => {
        if (options.callBack) {
          options.callBack(res)
        }
      }).catch(e => {
        if (options.callBack) {
          options.callBack(e)
        }
      })
    },
    addLanguages(options) {
      for (const key in i18n.messages) {
        if (Object.hasOwnProperty.call(i18n.messages, key)) {
          const element = options[key]
          i18n.mergeLocaleMessage(key, element)
        }
      }
    },
    setLayoutInfo(param) {
      const { header, backName } = param
      this.header = header
      this.backName = backName
    },
    pluginCallBack(param) {
      // console.log(param)

      const { eventName, eventParam } = param
      bus.$emit(eventName, eventParam)
    }
  }
}
</script>

<style scoped>

</style>
