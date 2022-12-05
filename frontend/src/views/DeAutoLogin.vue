<template>
  <div
    v-loading="loading"
    class="de-auto-login"
  >
    <plugin-com
      v-if="dingtalkOpen && corpId"
      ref="DTWithoutLogin"
      :corp-id="corpId"
      component-name="DTWithoutLogin"
    />

    <div
      v-if="!loading && !dingtalkOpen"
      class="auto-login-missing"
    >
      <span>未开启钉钉</span>
    </div>

    <div
      v-else-if="!loading && !corpId"
      class="auto-login-missing"
    >
      <span>缺失企业ID参数</span>
    </div>
  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'
import { dingtalkStatus } from '@/api/user'
export default {
  name: 'DeAutoLogin',
  components: { PluginCom },
  data() {
    return {
      corpId: null,
      dingtalkOpen: false,
      loading: true
    }
  },

  created() {
    this.corpId = this.$route.query.corpId
    dingtalkStatus().then(res => {
      if (res.success && res.data) {
        this.dingtalkOpen = true
      }
      this.loading = false
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
