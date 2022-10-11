<template>
  <div
    v-if="!licValidate && licStatus !== 'no_record'"
    class="lic"
  >
    <strong>{{ $t(licMsg) }}</strong>
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
    /* theme() {
      return this.$store.state.settings.theme
    }, */
    licValidate() {
      return this.$store.state.lic.validate
    },
    licStatus() {
      return this.$store.state.lic.licStatus || ''
    },
    licMsg() {
      return this.$store.state.lic.licMsg ? ('license.' + this.$store.state.lic.licMsg) : null
    }

  },

  mounted() {
    // this.validate()
  },
  methods: {
    // validate() {
    //   validateLic().then(res => {
    //     this.lic = true
    //     this.$store.dispatch('lic/setValidate', true)
    //   }).catch((e) => {
    //     this.msg = e.response.data.message
    //     this.lic = false
    //     this.$store.dispatch('lic/setValidate', false)
    //   })
    // }
  }
}
</script>
<style lang="scss" scoped>

    .lic {
        height: 24px;
        background-color: #c92100;
        color: #fff;
        text-align: center;
        /* padding: 6px 11px; */
        position: fixed;
        z-index: 1002;
        top: 0;
        width: 100%;
    }

</style>
