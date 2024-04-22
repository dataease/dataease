<template>
  <div
    v-loading="showTips"
    element-loading-custom-class="pwd-tips-loading"
    :class="classObj"
    class="app-wrapper"
  >
    <licbar />
    <pwd-exp-tips />

    <topbar
      v-if="!fullHeightFlag"
      :show-tips="showTips"
    />

    <de-container :style="mainStyle">
      <de-aside-container
        v-if="showSideBar"
        :is-collapse-width="sideWidth"
        type="system"
        class="le-aside-container"
      >
        <sidebar
          class="sidebar-container"
          @changeSideWidth="(side) => sideWidth = side"
        />
      </de-aside-container>

      <de-main-container
        class="la-main-container"
        :class="{'full-height':fullHeightFlag}"
      >
        <app-main />
      </de-main-container>
    </de-container>
    <div
      v-if="showTips"
      class="pwd-tips"
    >
      <span>{{ $t('commons.first_login_tips') }}</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <el-button
          size="mini"
          :disabled="buttonDisable"
          style="padding-right: 65px;"
          type="text"
          @click="doNotNoti"
        >{{ $t('commons.donot_noti') }}</el-button>
        <el-button
          type="primary"
          size="mini"
          @click="showTips = false"
        >{{ $t('commons.roger_that') }}</el-button>
      </div>
      <div class="arrow" />
    </div>

  </div>
</template>

<script>
import { Sidebar, AppMain, Topbar, Licbar, PwdExpTips } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import bus from '@/utils/bus'
import { showMultiLoginMsg } from '@/utils/index'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    Topbar,
    Licbar,
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    PwdExpTips
  },
  mixins: [ResizeMixin],
  data() {
    return {
      componentName: 'PanelMain',
      showTips: false,
      buttonDisable: false,
      sideWidth: ''
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
      const path = this.$route.path
      return (path.indexOf('panel') > -1 && (this.componentName === 'PanelEdit' || this.componentName === 'ChartEdit')) || (path.indexOf('/data-filling/create') > -1)
    },
    showSideBar() {
      return !this.sidebar.hide && this.$route.path.indexOf('data-filling') === -1
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
    this.showTips = false
  },
  mounted() {
    document.addEventListener('click', this.bodyClick)
    bus.$on('PanelSwitchComponent', this.panelSwitchComponent)
    bus.$on('web-seize-topic-call', this.webMsgTopicCall)
  },
  beforeDestroy() {
    this.showTips = false
    document.removeEventListener('click', this.bodyClick)
    bus.$off('PanelSwitchComponent', this.panelSwitchComponent)
    bus.$off('web-seize-topic-call', this.webMsgTopicCall)
  },
  created() {
    showMultiLoginMsg()
  },
  methods: {
    bodyClick(e) {
      const dom = document.querySelector('.pwd-tips')
      if (dom && !dom.contains(e.target)) {
        this.showTips = false
      }
    },
    webMsgTopicCall(param) {
      const msg = this.$t('multi_login_lang.forced_offline')
      // eslint-disable-next-line
      this.$error(eval(msg))
      bus.$emit('sys-logout')
    },
    panelSwitchComponent(c) {
      console.log(c)
      this.componentName = c.name
    },
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
  ::v-deep .pwd-tips-loading {
    z-index: 2024;
    background-color: rgba(255, 255, 255, 0.1);
    .el-loading-spinner {
      display: none !important;
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
      width: 7px;
      height: 7px;
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
