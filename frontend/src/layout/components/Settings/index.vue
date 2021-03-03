<template>
  <div class="drawer-container">
    <div>
      <h3 class="drawer-title">系统布局设置</h3>

      <div class="drawer-item">
        <span>主题颜色</span>
        <theme-picker style="float: right;height: 26px;margin: -3px 8px 0 0;" @change="themeChange" />
      </div>

      <!-- <div class="drawer-item">
        <span>显示标签</span>
        <el-switch v-model="tagsView" class="drawer-switch" />
      </div>

      <div class="drawer-item">
        <span>固定头部</span>
        <el-switch v-model="fixedHeader" class="drawer-switch" />
      </div> -->

      <div class="drawer-item">
        <span>显示LOGO</span>
        <el-switch v-model="sidebarLogo" class="drawer-switch" />
      </div>

      <!-- <div class="drawer-item">
        <span>菜单UniqueOpened</span>
        <el-switch v-model="uniqueOpened" class="drawer-switch" />
      </div> -->

      <div class="drawer-item">
        <span>展开菜单</span>
        <el-switch v-model="toggleSideBar" class="drawer-switch" />
      </div>

    </div>
  </div>
</template>

<script>
import ThemePicker from '@/components/ThemePicker'

export default {
  components: { ThemePicker },
  data() {
    return {
      showMenu: true
    }
  },
  computed: {
    // fixedHeader: {
    //   get() {
    //     return this.$store.state.settings.fixedHeader
    //   },
    //   set(val) {
    //     this.$store.dispatch('settings/changeSetting', {
    //       key: 'fixedHeader',
    //       value: val
    //     })
    //   }
    // },
    // tagsView: {
    //   get() {
    //     return this.$store.state.settings.tagsView
    //   },
    //   set(val) {
    //     this.$store.dispatch('settings/changeSetting', {
    //       key: 'tagsView',
    //       value: val
    //     })
    //   }
    // },
    sidebarLogo: {
      get() {
        return this.$store.state.settings.sidebarLogo
      },
      set(val) {
        this.$store.dispatch('settings/changeSetting', {
          key: 'sidebarLogo',
          value: val
        })
      }
    },
    toggleSideBar: {
      get() {
        return this.showMenu
      },
      set(val) {
        this.showMenu = !this.showMenu
        this.$store.dispatch('app/toggleSideBar')
      }
    }
    // uniqueOpened: {
    //   get() {
    //     return this.$store.state.settings.uniqueOpened
    //   },
    //   set(val) {
    //     this.$store.dispatch('settings/changeSetting', {
    //       key: 'uniqueOpened',
    //       value: val
    //     })
    //   }
    // }
  },
  methods: {
    themeChange(val) {
      this.$store.dispatch('settings/changeSetting', {
        key: 'theme',
        value: val
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.drawer-container {
  padding: 24px;
  font-size: 14px;
  line-height: 1.5;
  word-wrap: break-word;

  .drawer-title {
    margin-bottom: 12px;
    color: rgba(0, 0, 0, .85);
    font-size: 14px;
    line-height: 22px;
  }

  .drawer-item {
    color: rgba(0, 0, 0, .65);
    font-size: 14px;
    padding: 12px 0;
  }

  .drawer-switch {
    float: right
  }
}
</style>
