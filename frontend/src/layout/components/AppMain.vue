<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <el-main class="ms-main-container">
        <router-view :key="key" />
      </el-main>
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    key() {
      return this.$route.path
    }
  }
}
</script>

<style lang="scss" scoped>

    .app-main {
        height: 100%;
        /* topbar 56  */
        // min-height: calc(100vh - 56px);
        // width: 100%;
        // height: 100%;
         // position: relative;
        // overflow: hidden;
    }

    .fixed-header + .app-main {
        padding-top: 50px;
    }

    .ms-main-container {
        height: 100%;
        padding: 0 !important;
    }
</style>

<style lang="scss">
    // fix css style bug in open el-dialog
    .el-popup-parent--hidden {
        .fixed-header {
            padding-right: 15px;
        }
    }
</style>
