<template>
  <div>
    <link-error v-if="showIndex===0" />
    <link-pwd v-if="showIndex===1" :resource-id="resourceId" />
    <link-view v-if="showIndex===2" />
  </div>
</template>
<script>
import { getQueryVariable } from '@/utils/index'
import { validate } from '@/api/link'
import LinkView from './view'
import LinkError from './error'
import LinkPwd from './pwd'
export default {
  components: {
    LinkError, LinkPwd, LinkView
  },
  data() {
    return {
      PARAMKEY: 'link',
      link: null,
      showIndex: -1
    }
  },
  created() {
    this.loadInit()
  },
  methods: {

    loadInit() {
      this.link = getQueryVariable(this.PARAMKEY)
      validate({ link: this.link }).then(res => {
        const { valid, enablePwd, passPwd } = res.data
        // 如果链接无效 直接显示无效页面
        if (!valid) {
          this.showError()
          return
        }

        if (enablePwd && !passPwd) {
          this.showPwd()
          return
        }

        this.showView()
      })
    },

    // 显示无效链接
    showError() {
      this.showIndex = 0
    },
    // 显示密码框
    showPwd() {
      this.showIndex = 1
    },
    // 显示仪表板
    showView() {
      this.showIndex = 2
    }
  }
}
</script>
