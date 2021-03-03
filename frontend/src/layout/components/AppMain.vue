<template>
<section class="app-main">
    <transition name="fade-transform" mode="out-in">
        <el-main class="ms-main-container">
            <keep-alive>
                <router-view :key="key"/>
            </keep-alive>
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
    @import "~@/styles/variables.scss";

    .app-main {
        /*107 = navbar 50 + topbar 57  */
        min-height: calc(100vh - 107px);
        width: 100%;
        position: relative;
        overflow: hidden;
    }

    .fixed-header + .app-main {
        padding-top: 50px;
    }

    .ms-main-container {
        padding: 0;
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
