<template>
  <h2>this is sso page</h2>
</template>

<script>

export default ({
  data() {
    return {

    }
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', false)
  },
  method: {

  }
})
</script>

