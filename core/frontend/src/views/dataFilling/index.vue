<template>
  <div
    element-loading-custom-class="pwd-tips-loading"
    :class="classObj"
    class="app-wrapper"
  >
    <de-container>
      <de-main-container
        class="la-main-container full-height"
      >
        <app-main />
      </de-main-container>
    </de-container>

  </div>
</template>

<script>
import { AppMain } from '../../layout/components'
import ResizeMixin from '../../layout/mixin/ResizeHandler'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import bus from '@/utils/bus'
import { showMultiLoginMsg } from '@/utils/index'

export default {
  name: 'DataFilling',
  components: {
    AppMain,
    DeMainContainer,
    DeContainer
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
