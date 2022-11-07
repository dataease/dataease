<template>

  <async-component
    v-if="showAsync"
    :ref="refId"
    :url="url"
    :obj="obj"
    v-bind="$attrs"
    v-on="$listeners"
    @execute-axios="executeAxios"
    @on-add-languages="addLanguages"
    @plugin-call-back="pluginCallBack"
  />
  <div v-else>
    <h1>未知组件无法展示</h1>
  </div>

</template>

<script>
import AsyncComponent from '@/components/asyncComponent'
import i18n from '@/lang'
import bus from '@/utils/bus'
import { execute } from '@/api/system/dynamic'
import { uuid } from 'vue-uuid'

export default {
  name: 'PluginCom',
  components: {
    AsyncComponent
  },
  inheritAttrs: true,
  props: {
    componentName: {
      type: String,
      default: null
    },
    obj: {
      type: Object,
      default: () => {
      }
    }
  },
  data() {
    return {
      showAsync: false,
      baseUrl: '/api/pluginCommon/component/',
      url: null,
      refId: null
    }
  },
  watch: {
    'componentName': function() {
      this.refId = uuid.v1
      if (this.componentName) {
        this.showAsync = true
        this.url = this.baseUrl + this.componentName
      } else {
        this.showAsync = false
      }
    }
  },
  created() {
    this.refId = uuid.v1
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
    },
    /* chartResize() {
      this.$refs[this.refId] && this.$refs[this.refId].chartResize && this.$refs[this.refId].chartResize()
    }, */
    callPluginInner(param) {
      return this.$refs[this.refId] && this.$refs[this.refId].callPluginInner && this.$refs[this.refId].callPluginInner(param)
    }
  }
}
</script>
