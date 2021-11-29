<template>
  <div :class="classObj" class="app-wrapper">
    <licbar />
    <topbar v-if="!fullHeightFlag" />

    <de-container :style="mainStyle">
      <de-aside-container v-if="!sidebar.hide" class="le-aside-container">
        <sidebar class="sidebar-container" />
      </de-aside-container>

      <de-main-container class="la-main-container" :class="{'full-height':fullHeightFlag}">
        <app-main />
      </de-main-container>
    </de-container>
  </div>
</template>

<script>
import { Sidebar, AppMain, Topbar, Licbar } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import bus from '@/utils/bus'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    Topbar,
    Licbar,
    DeMainContainer,
    DeContainer,
    DeAsideContainer
  },
  mixins: [ResizeMixin],
  data() {
    return {
      componentName: 'PanelMain'
    }
  },
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    },
    device() {
      return this.$store.state.app.device
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader
    },
    showSettings() {
      return this.$store.state.settings.showSettings
    },
    fullHeightFlag() {
      return this.$route.path.indexOf('panel') > -1 && (this.componentName === 'PanelEdit' || this.componentName === 'ChartEdit')
    },
    mainStyle() {
      if (this.fullHeightFlag) {
        return {
          'height': '100vh!important'
        }
      } else {
        return {
          'paddingTop': '56px'
        }
      }
    },
    classObj() {
      return {
        // hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  mounted() {
    bus.$on('PanelSwitchComponent', (c) => {
      this.componentName = c.name
    })
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/styles/mixin.scss";
  @import "~@/styles/variables.scss";

  .app-wrapper {
    @include clearfix;
    position: relative;
    height: 100%;
    // height: $contentHeight;
    background-color: var(--ContentBG);
    width: 100%;
    &.mobile.openSidebar{
      position: fixed;
      top: 0;
    }
  }
  .drawer-bg {
    background: #000;
    opacity: 0.3;
    width: 100%;
    top: 0;
    height: 100%;
    position: absolute;
    z-index: 999;
  }

  .fixed-header {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9;
    width: calc(100% - #{$sideBarWidth});
    transition: width 0.28s;
  }

  .hideSidebar .fixed-header {
    width: calc(100% - 56px)
  }

  .mobile .fixed-header {
    width: 100%;
  }

  .la-main-container {
      padding: 0px !important;
  }
  .le-aside-container {
      .sidebar-container {
        width: 100% !important;
        position: initial !important;
        height: calc(100vh - 80px) !important;
        overflow-x: hidden !important;
        overflow-y: auto !important;
        background-color: var(--SiderBG) !important;
      }
  }
  .full-height {
    height: 100vh !important;
  }
</style>
