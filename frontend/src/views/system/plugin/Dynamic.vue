<template>
  <de-layout-content
    v-if="!noLayout && menuid !== 740"
    v-loading="jsname && !innerLoadingNames.includes(jsname) && $store.getters.loadingMap[$store.getters.currentPath]"
    :header="header"
    :back-name="backName"
  >
    <async-component
      v-if="showAsync"
      :url="url"
      @execute-axios="executeAxios"
      @on-add-languages="addLanguages"
      @on-plugin-layout="setLayoutInfo"
      @plugin-call-back="pluginCallBack"
    />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </de-layout-content>
  <div v-else-if="menuid === 740">
    <async-component
      v-if="showAsync"
      :url="url"
      @execute-axios="executeAxios"
      @on-add-languages="addLanguages"
      @on-plugin-layout="setLayoutInfo"
      @plugin-call-back="pluginCallBack"
    />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </div>
  <div v-else>
    <async-component
      v-if="showAsync"
      :url="url"
      @execute-axios="executeAxios"
      @on-add-languages="addLanguages"
      @on-plugin-layout="setLayoutInfo"
      @plugin-call-back="pluginCallBack"
    />
    <div v-else>
      <h1>未知组件无法展示</h1>
    </div>
  </div>

</template>

<script>
import DeLayoutContent from '@/components/business/DeLayoutContent'
import AsyncComponent from '@/components/asyncComponent'
import i18n from '@/lang'
import bus from '@/utils/bus'
import { execute } from '@/api/system/dynamic'

export default {
  name: 'Dynamic',
  components: {
    DeLayoutContent,
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
      url: null,
      innerLoadingNames: ['SystemDept', 'SystemRole', 'SystemAuth']
    }
  },
  watch: {
    'menuid': function() {
      this.init()
    }
  },
  created() {
    this.init()
  },

  methods: {
    init() {
      if (this.jsname && this.menuid) {
        this.showAsync = true
        this.url = this.baseUrl + this.menuid
      } else {
        this.showAsync = false
      }
    },
    // hasLicense
    executeAxios(options) {
      execute(options).then(res => {
        if (options.callBack) {
          options.callBack(res)
        }
      }).catch(e => {
        if (options.error) {
          options.error(e)
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
      const { eventName, eventParam } = param
      bus.$emit(eventName, eventParam)
    }
  }
}
</script>

<style scoped>

</style>
