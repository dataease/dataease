<template>
  <div>
    <async-component v-if="showAsync" :url="url" @execute-axios="executeAxios" @on-add-languanges="addLanguages" @plugin-call-back="pluginCallBack" />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </div>
</template>

<script>
import AsyncComponent from '@/components/AsyncComponent'
import i18n from '@/lang'
import bus from '@/utils/bus'
import { execute } from '@/api/system/dynamic'
export default {
  name: 'PluginCom',
  components: {
    AsyncComponent
  },
  props: {
    componentName: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      showAsync: false,
      baseUrl: '/api/pluginCommon/component/',
      url: null
    }
  },
  created() {
    if (this.componentName) {
      this.showAsync = true
      this.url = this.baseUrl + this.componentName
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

    pluginCallBack(param) {
      const { eventName, eventParam } = param
      bus.$emit(eventName, eventParam)
    }
  }
}
</script>
