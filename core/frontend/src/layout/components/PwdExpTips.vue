<template>

  <div
    v-if="pwdPeriodWarn && !tipClosed"
    class="lic_tips"
  >
    <el-alert
      class="lic_alert"
      :title="warnMsg"
      type="warning"
      show-icon
      center
      @close="closeTip"
    />

  </div>
</template>

<script>
export default {
  name: 'PwdExpTips',
  components: {

  },
  data() {
    return {
      tipClosed: false
    }
  },
  computed: {
    pwdPeriodWarn() {
      if (localStorage.getItem('pwd-period-warn')) {
        return true
      }
      return this.$store.state.user.validityPeriod > 0 && this.$store.state.user.validityPeriod < 8
    },
    warnMsg() {
      if (localStorage.getItem('pwd-period-warn')) {
        const timeText = localStorage.getItem('pwd-period-warn')
        const temp = this.$t('commons.pwd_will_expired')
        return temp.replace('%s', timeText)
      }
      if (this.$store.state.user.validityPeriod > 0 && this.$store.state.user.validityPeriod < 8) {
        localStorage.setItem('pwd-period-warn', this.$store.state.user.validityPeriod)
        const temp = this.$t('commons.pwd_will_expired')
        return temp.replace('%s', this.$store.state.user.validityPeriod)
      }
      return null
    }
  },

  mounted() {
  },
  methods: {
    closeTip() {
      this.tipClosed = true
    }

  }
}
</script>
  <style lang="scss" scoped>
    .lic_tips {
      position: absolute;
      z-index: 2000;
      position:absolute;
      top: 0;left:0;right:0;
      margin: auto;
    }
    .lic_alert ::v-deep .el-icon-close{
      top: 16px !important;
      right: 10px !important;
    }

  </style>
