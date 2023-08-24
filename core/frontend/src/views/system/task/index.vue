<template>
  <router-view />
</template>

<script>

export default ({
  created() {
    this.$store.dispatch('app/toggleSideBarHide', false)
  }
})
</script>

