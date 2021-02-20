<template>
  <el-menu mode="horizontal" menu-trigger="click"
           background-color="#2c2a48"
           class="header-top-menus"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :default-active="activeIndex"
           @select="handleSelect"
           router>

    <el-menu-item index="/track" v-permission="['test_manager','test_user','test_viewer']">
      {{ $t('test_track.test_track') }}
    </el-menu-item>
    <el-menu-item index="/api" @click="active()" v-permission="['test_manager','test_user','test_viewer']">
      {{ $t('commons.api') }}
    </el-menu-item>
    <el-menu-item index="/performance" onselectstart="return false"
                  v-permission="['test_manager','test_user','test_viewer']">
      {{ $t('commons.performance') }}
    </el-menu-item>
    <el-menu-item index="/setting" onselectstart="return false">
      {{ $t('commons.system_setting') }}
    </el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "MsTopMenus",
    data() {
      return {
        activeIndex: '/'
      }
    },
    watch: {
      '$route'(to) {
        if (to.matched.length > 0) {
          this.activeIndex = to.matched[0].path;
        }
        this.handleSelect(this.activeIndex);
      }
    },
    mounted() {
      if (this.$route.matched.length > 0) {
        this.activeIndex = this.$route.matched[0].path;
      }
    },
    methods: {
      handleSelect(index) {
        this.activeIndex = index
      },
      active() {
        if (this.activeIndex === '/api') {
          if (this.$store.state.switch.value == 'new') {
            window.location.href = "/#/api/home";
          } else if (this.$store.state.switch.value == 'old') {
            window.location.href = "/#/api/home_obsolete";
          }
        }
      }
    }
  }
</script>
<style scoped>

  .el-menu >>> .el-menu-item {
    box-sizing: border-box;
    height: 40px;
  }

</style>
