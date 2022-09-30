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
            'system-ukey': 'SystemUkey',
            740: 'XpackThemeForm',
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
        console.log('this.url', this.url);
        // if (this.url.includes('LdapSetting')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/settings/ldap/index.vue');
        //   return
        // }

        // if (this.url.includes('SsoSetting')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/settings/sso/index.vue');
        //   return
        // }

        // if (this.url.includes('CasSetting')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/settings/cas/index.vue');
        //   return
        // }


        // if (this.url.includes('DisplaySetting')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/settings/display/index.vue');
        //   return
        // }

        // if (this.url.includes('ThemeSetting')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/settings/theme/index.vue');
        //   return
        // }

        // if (this.url.includes('740')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/theme/XpackThemeForm.vue');
        //   return
        // }

        // if (this.url.includes('710')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/theme/XpackThemeMenu.vue');
        //   return
        // }

        if (this.$route.name === 'sys-task-email') {
          this.mode = () => import('de-plugins/src/views/xpack/task/TaskList.vue');
          return
        }

        // if (this.url.includes('61')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/task/email.vue');
        //   return
        // }

        // if (this.url.includes('62')) {
        //   this.mode = () => import('de-plugins/src/views/xpack/task/form.vue');
        //   return
        // }

        // const arr = this.url.split('/');
        // let text = arr[arr.length - 1 ]
        // if (urlMap[text]) {
        //   text = urlMap[text]
        // }

        // console.log(1, text);
        

        if (!window.SyncComponentCache[this.url]) {
          // window.SyncComponentCache[this.url] = get(`/static/${text}.js`)


          window.SyncComponentCache[this.url] = get(this.url)

          // window.SyncComponentCache[this.url] = Axios.get(this.url)
          res = await window.SyncComponentCache[this.url]
        } else {
          res = await window.SyncComponentCache[this.url]
        }
        if (res) {
          const Fn = Function
          const dynamicCode = res
          this.mode = new Fn(`return ${dynamicCode}`)()
        }
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





