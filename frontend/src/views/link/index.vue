<template>
  <div>
    <link-error v-if="showIndex===0" />
    <link-pwd v-if="showIndex===1" />
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
      validate(this.buildParam()).then(res => {
        const { valid, enablePwd, passPwd } = res.data
        // 如果链接无效 直接显示无效页面
        !valid && this.showError()

        // 如果未启用密码 直接显示视图页面
        !enablePwd && this.showView()

        // 如果启用密码 但是未通过密码验证 显示密码框
        !passPwd && this.showPwd()

        this.showView()
      })
      console.log(this.link)
    },
    buildParam() {
      return {}
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
