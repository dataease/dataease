<template>
  <div
    v-loading="loading"
    class="de-auto-login"
  >
    <plugin-com
      v-if="isDingTalkLink && dingtalkOpen && corpId"
      ref="DTWithoutLogin"
      :corp-id="corpId"
      component-name="DTWithoutLogin"
    />

    <plugin-com
      v-else-if="isLarkLink && larkOpen && appId"
      ref="LKWithoutLogin"
      :app-id="appId"
      component-name="LKWithoutLogin"
    />

    <plugin-com
      v-else-if="isLarksuiteLink && larksuiteOpen"
      ref="LKSWithoutLogin"
      component-name="LKSWithoutLogin"
    />

    <plugin-com
      v-else-if="isWecomLink && wecomOpen"
      ref="WecomWithoutLogin"
      :app-id="appId"
      :agent-id="agentId"
      :redirect-uri="redirectUri"
      component-name="WecomWithoutLogin"
    />

    <div
      v-if="isDingTalkLink && !loading && !dingtalkOpen"
      class="auto-login-missing"
    >
      <span>未开启钉钉</span>
    </div>

    <div
      v-else-if="isDingTalkLink && !loading && !corpId"
      class="auto-login-missing"
    >
      <span>缺失企业ID参数</span>
    </div>

    <div
      v-if="isLarkLink && !loading && !larkOpen"
      class="auto-login-missing"
    >
      <span>未开启飞书</span>
    </div>

    <div
      v-else-if="isLarkLink && !loading && !appId"
      class="auto-login-missing"
    >
      <span>缺失应用ID参数</span>
    </div>

    <div
      v-if="isLarksuiteLink && !loading && !larksuiteOpen"
      class="auto-login-missing"
    >
      <span>未开启国际飞书</span>
    </div>

    <div
      v-if="isWecomLink && !loading && !wecomOpen"
      class="auto-login-missing"
    >
      <span>未开启企业微信</span>
    </div>

    <div
      v-if="isWecomLink && !loading && !appId"
      class="auto-login-missing"
    >
      <span>缺失企业ID</span>
    </div>

    <div
      v-if="isWecomLink && !loading && !agentId"
      class="auto-login-missing"
    >
      <span>缺失应用ID</span>
    </div>

  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'
import { dingtalkStatus, larkStatus, larksuiteStatus, larkAppId, wecomStatus, wecomQrParams } from '@/api/user'
export default {
  name: 'DeAutoLogin',
  components: { PluginCom },
  data() {
    return {
      type: 'dingtalk',
      corpId: null,
      appId: null,
      dingtalkOpen: false,
      larkOpen: false,
      larksuiteOpen: false,
      loading: true,
      wecomOpen: false,
      agentId: null,
      redirectUri: null
    }
  },
  computed: {
    isDingTalkLink() {
      return this.type === 'dingtalk' || !this.type
    },
    isLarkLink() {
      return this.type === 'lark'
    },
    isLarksuiteLink() {
      return this.type === 'larksuite'
    },
    isWecomLink() {
      return this.type === 'wecom'
    }
  },
  created() {
    this.corpId = this.$route.query.corpId
    if (this.$route.query.type) {
      this.type = this.$route.query.type
    }

    this.isDingTalkLink && dingtalkStatus().then(res => {
      if (res.success && res.data) {
        this.dingtalkOpen = true
      }
      this.loading = false
    }).catch(e => {
      this.loading = false
    })

    this.isLarkLink && larkStatus().then(res => {
      if (res.success && res.data) {
        larkAppId().then(resp => {
          this.appId = resp.data
          this.larkOpen = true
          this.loading = false
        })
      }
    }).catch(e => {
      this.loading = false
    })

    this.isLarksuiteLink && larksuiteStatus().then(res => {
      if (res.success && res.data) {
        this.larksuiteOpen = true
      }
      this.loading = false
    }).catch(e => {
      this.loading = false
    })

    this.isWecomLink && wecomStatus().then(res => {
      if (res.success && res.data) {
        wecomQrParams().then(resp => {
          this.appId = resp.data.appid
          this.agentId = resp.data.agentid
          this.redirectUri = resp.data.redirectUri
          this.wecomOpen = true
          this.loading = false
        })
      }
    }).catch(e => {
      this.loading = false
    })
  },
  methods: {

  }
}
</script>

<style lang="scss" scoped>
.de-auto-login {
  width: 100%;
  height: 100vh;
  .auto-login-missing {
    text-align: center;
  }
}
</style>
