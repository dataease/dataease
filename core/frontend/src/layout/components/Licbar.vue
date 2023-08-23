<template>

  <div
    v-if="!licValidate && licStatus !== 'no_record' && !tipClosed"
    class="lic_tips"
  >
    <el-alert
      class="lic_alert"
      :title="$t(licMsg)"
      type="warning"
      show-icon
      center
      @close="closeTip"
    />

  </div>
</template>

<script>
export default {
  name: 'Licbar',
  components: {

  },
  data() {
    return {
      lic: true,
      msg: 'Cannot run program "/usr/local/bin/validator_darwin_amd64": error=2, No such file or directory'
    }
  },
  computed: {

    licValidate() {
      return this.$store.state.lic.validate
    },
    licStatus() {
      return this.$store.state.lic.licStatus || ''
    },
    licMsg() {
      if (this.$store.state.lic?.licMsg?.includes('expired')) {
        const message = this.$store.state.lic.licMsg
        const exp = message.substring(message.indexOf('since ') + 6, message.indexOf(','))
        return this.$t('license.expired_msg').replace('{0}', exp)
      }
      return this.$store.state.lic.licMsg ? ('license.' + this.$store.state.lic.licMsg) : null
    },
    tipClosed() {
      return localStorage.getItem('lic_closed')
    }

  },

  mounted() {
  },
  methods: {
    closeTip() {
      localStorage.setItem('lic_closed', true)
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
