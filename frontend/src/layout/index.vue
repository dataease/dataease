<template>
  <div :class="classObj" class="app-wrapper">
    <licbar />
    <topbar v-if="!fullHeightFlag && finishLoad" :show-tips="showTips" />

    <de-container :style="mainStyle">
      <de-aside-container v-if="!sidebar.hide" :isCollapseWidth="sideWidth" type="system" class="le-aside-container">
        <sidebar @changeSideWidth="(side) => sideWidth = side"  class="sidebar-container" />
      </de-aside-container>

      <de-main-container class="la-main-container" :class="{'full-height':fullHeightFlag}">
        <app-main />
      </de-main-container>
    </de-container>
    <div v-if="showTips" class="pwd-tips">
      <span>{{ $t('commons.first_login_tips') }}</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <el-button size="mini" :disabled="buttonDisable" style="padding-right: 65px;" type="text" @click="doNotNoti">{{ $t('commons.donot_noti') }}</el-button>
        <el-button type="primary" size="mini" @click="showTips = false">{{ $t('commons.roger_that') }}</el-button>
      </div>
      <div class="arrow" />
    </div>

  </div>
</template>

<script>
import { Sidebar, AppMain, Topbar, Licbar } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import bus from '@/utils/bus'

import { needModifyPwd, removePwdTips } from '@/api/user'

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
      componentName: 'PanelMain',
      showTips: false,
      finishLoad: false,
      buttonDisable: false,
      sideWidth: "",
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
  beforeCreate() {
    needModifyPwd().then(res => {
      this.showTips = res.success && res.data
      this.finishLoad = true
    }).catch(e => {
      this.finishLoad = true
    })
  },
  mounted() {
    bus.$on('PanelSwitchComponent', this.panelSwitchComponent)
  },
  beforeDestroy() {
    bus.$off('PanelSwitchComponent', this.panelSwitchComponent)
  },
  methods: {
    panelSwitchComponent(c) {
      this.componentName = c.name
    },
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    doNotNoti() {
      this.buttonDisable = true
      removePwdTips().then(res => {
        this.showTips = false
        this.buttonDisable = false
      }).catch(e => {
        this.buttonDisable = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
  @import "~@/styles/mixin.scss";
  @import "~@/styles/variables.scss";

  .app-wrapper {
    min-width: 1000px!important;
    min-height: 600px!important;
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
        width: 100%;
        position: relative !important;
        height: 100%;
        top: 0 !important;
        background-color: var(--SiderBG) !important;
      }
      overflow: hidden;
  }
  .full-height {
    height: 100vh !important;
    ::-webkit-scrollbar {
      width: 5px;
      height: 5px;
    }

  }
  .pwd-tips {
    position: absolute;
    box-shadow: 0 0 0 1000em rgb(0, 0, 0, 0.3);
    height: 100px;
    width: 225px;
    top: 105px;
    right: 115px;
    z-index: 9999;
    border-radius: 4px;
    padding: 15px;
    background: #fff;
  }
  .arrow{
    border-bottom: 7px solid #fff;
    border-right: 7px solid #b5b5b7;
    border-left: 7px solid #b5b5b7;
    border-top: 7px solid #b5b5b7;
    width: 0px;
    height: 0px;
    position: relative;
    top:-60px;
    left:210px;
    transform: rotate(90deg);
}

</style>
