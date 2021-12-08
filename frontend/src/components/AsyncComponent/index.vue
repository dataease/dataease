<template>
  <component
    :is="mode"
    v-bind="$attrs"
    v-on="$listeners"
  />
</template>

<script>

import { get } from '@/api/system/dynamic'

export default {
  name: 'AsyncComponent',
  inheritAttrs: true,
  props: {
    // 父组件提供请求地址
    url: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      resData: '',
      mode: ''
    }
  },
  watch: {
    url: {
      immediate: true,
      async handler(val, oldVal) {
        if (!this.url) return
        // Cache 缓存 根据 url 参数
        if (!window.SyncComponentCache) {
          window.SyncComponentCache = {}
        }
        let res
        if (!window.SyncComponentCache[this.url]) {
          window.SyncComponentCache[this.url] = get(this.url)

          // window.SyncComponentCache[this.url] = Axios.get(this.url)
          res = await window.SyncComponentCache[this.url]
        } else {
          res = await window.SyncComponentCache[this.url]
        }

        const Fn = Function
        this.mode = new Fn(`return ${res.data || res}`)()
        /* if (res && res.data) {
          const Fn = Function
          this.mode = new Fn(`return ${res.data || res}`)()
        } */
      }
    }
  },
  methods: {

  }
}
</script>
