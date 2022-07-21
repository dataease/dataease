<template>
  <component
    :is="mode"
    :ref="refId"
    :obj="obj"
    v-bind="$attrs"
    v-on="$listeners"
  />
</template>

<script>
import { uuid } from 'vue-uuid'
import { get } from '@/api/system/dynamic'
export default {
  name: 'AsyncComponent',
  inheritAttrs: true,
  props: {
    // 父组件提供请求地址
    url: {
      type: String,
      default: ''
    },
    obj: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      resData: '',
      mode: '',
      refId: null
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
        console.log(1, this.$route);
        const urlMap = {
            'system-role': 'SystemRole',
            'system-dept': 'SystemDept',
            'system-auth': 'SystemAuth',
            'email-task': 'TaskEmail',
            'system-dept-form': 'SystemDeptFormn',
            'system-role-form': 'SystemRoleForm',
            'email-task-form': 'TaskEmailForm',
            'system-ukey': 'SystemUkey'
        }

        // if (this.$route.name === 'system-role') {
        //   this.mode = () => import('de-plugins/src/views/xpack/role/index.vue');
        //   return
        // }

        // if (this.$route.name === 'system-role-form') {
        //   this.mode = () => import('de-plugins/src/views/xpack/role/form.vue');
        //   return
        // }

        // if (this.$route.name === 'dataset') {
        //   this.mode = () => import('de-plugins/src/views/xpack/dataset/rowpermissions/rowPermissions.vue');
        //   return
        // }

        // if (this.$route.name === 'system-dept') {
        //   this.mode = () => import('de-plugins/src/views/xpack/dept/index.vue');
        //   return
        // }

        // if (this.$route.name === 'system-auth') {
        //   this.mode = () => import('de-plugins/src/views/xpack/auth/index.vue');
        //   return
        // }

        if (!window.SyncComponentCache[this.url]) {
          if (urlMap[this.$route.name]) {
            window.SyncComponentCache[this.url] = get(`/static/${urlMap[this.$route.name]}.js`)
          }

          // window.SyncComponentCache[this.url] = get(this.url)

          // window.SyncComponentCache[this.url] = Axios.get(this.url)
          res = await window.SyncComponentCache[this.url]
        } else {
          res = await window.SyncComponentCache[this.url]
        }

        console.log(1, res)

        const Fn = Function
        this.mode = new Fn(`return ${res}`)()
        /* if (res && res.data) {
          const Fn = Function
          this.mode = new Fn(`return ${res.data || res}`)()
        } */
      }
    }
  },
  created() {
    this.refId = uuid.v1
  },
  methods: {
    /* chartResize() {
      this.$refs[this.refId] && this.$refs[this.refId].chartResize && this.$refs[this.refId].chartResize()
    }, */
    callPluginInner(param) {
      const { methodName, methodParam } = param
      return this.$refs[this.refId] && this.$refs[this.refId][methodName] && this.$refs[this.refId][methodName](methodParam)
    }
  }
}
</script>
