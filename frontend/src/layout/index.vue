<template>
  <div :class="classObj" class="app-wrapper">
    <licbar />
    <topbar v-if="!fullHeightFlag && finishLoad" :show-tips="showTips" />

    <de-container :style="mainStyle">
      <de-aside-container v-if="!sidebar.hide" type="system" class="le-aside-container">
        <sidebar class="sidebar-container" />
      </de-aside-container>

      <de-main-container class="la-main-container" :class="{'full-height':fullHeightFlag}">
        <app-main />
      </de-main-container>
    </de-container>
    <div v-if="showTips&&oneShow" class="pwd-tips">
      <span>&nbsp;&nbsp;&nbsp;&nbsp;{{ $t('commons.first_login_tips') }}。</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <!-- <el-button type="primary" size="mini" @click="showTips = false">{{ $t('commons.roger_that') }}</el-button> -->
        <span style="margin-right:94px">(1/5)</span>
        <el-button type="primary" size="mini" @click="nextOne()">{{ '下一步' }}</el-button>
      </div>
      <div class="arrow" />
    </div>
    <div v-if="showTips&&twoShow" class="two_nav">
      <div class="arrowTwo" />
      <div class="titel_class">数据连接</div>
      <span>&nbsp;&nbsp;&nbsp;&nbsp;制作看板第一步需要添加数据源，您可以通过数据源对接各种类型数据。</span>
      <div>&nbsp;&nbsp;&nbsp;&nbsp;新建数据源、数据源校验处理、保存数据源。</div>

      <div style="text-align: right; margin-bottom: 10px;">
        <span style="margin-right:94px">(2/5)</span><el-button type="primary" size="mini" @click="nextTwo()">{{ '下一步' }}</el-button>
      </div>
    </div>
    <div v-if="showTips&&threeShow" class="three_nav two_nav">
      <div class="arrowTwo" />
      <div class="titel_class">数据准备</div>
      <div> &nbsp;&nbsp;&nbsp;&nbsp;创建数据库数据集，对数据进行连接，为下一步仪表板制作进行数据准备。</div>
      <span>&nbsp;&nbsp;&nbsp;&nbsp;目前支持创建的数据集类型有数据库数据集、SQL 数据集、Excel 数据集、自定 义数据集、关联数据集、API 数据集等。</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <span style="margin-right:94px">(3/5)</span>
        <el-button type="primary" size="mini" @click="nextThree()">{{ '下一步' }}</el-button>
      </div>
    </div>
    <div v-if="showTips&&foreShow" class="fore_nav two_nav">
      <div class="arrowTwo" />
      <div class="titel_class">分析展现</div>
      <!-- <div>数据准备完成，可制作仪表板</div> -->
      <span>&nbsp;&nbsp;&nbsp;&nbsp;数据准备完成，可制作仪表板,仪表板制作，可新建修改删除查看仪表板，点击编辑按钮对仪表板内容进行编辑操作，可拖动不同的组件来丰富你的仪表板。</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <span style="margin-right:94px">(4/5)</span>
        <el-button type="primary" size="mini" @click="nextFore()">{{ '下一步' }}</el-button>
      </div>
    </div>
    <div v-if="showTips&&fiveShow" class="five_nav two_nav">
      <div class="arrowTwo" />
      <div class="titel_class">资源发布</div>
      <!-- <div>数据准备完成，可制作仪表板</div> -->
      <span>&nbsp;&nbsp;&nbsp;&nbsp;数据看板制作完成以后可以通过数据门户进行组装发布。</span>
      <div style="text-align: right; margin-bottom: 10px;">
        <span style="margin-right:94px">(5/5)</span>
        <el-button type="primary" size="mini" @click="nextFive()">{{ '知道了' }}</el-button>
      </div>
    </div>

  </div>
</template>

<script>
import { Sidebar, AppMain, Topbar, Licbar } from './components'
import ResizeMixin from './mixin/ResizeHandler'
import DeMainContainer from '@/components/datains/DeMainContainer'
import DeContainer from '@/components/datains/DeContainer'
import DeAsideContainer from '@/components/datains/DeAsideContainer'
import bus from '@/utils/bus'

import { needModifyPwd } from '@/api/user'

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
      oneShow: true,
      twoShow: false,
      threeShow: false,
      foreShow: false,
      fiveShow: false
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
    bus.$on('PanelSwitchComponent', (c) => {
      this.componentName = c.name
    })
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    nextOne() {
      this.oneShow = false
      this.twoShow = true
    },
    nextTwo() {
      this.twoShow = false
      this.threeShow = true
    },
    nextThree() {
      this.foreShow = true
      this.threeShow = false
    },
    nextFore() {
      this.foreShow = false
      this.fiveShow = true
    },
    nextFive() {
      this.fiveShow = true
      this.showTips = false
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
  .titel_class{
    font-weight: 600;
  }
  .two_nav{
    position: absolute;
    box-shadow: 0 0 0 1000em rgb(0, 0, 0, 0.3);
    // height: 100px;
    width: 225px;
    top: 66px;
    left: 380px;
    z-index: 9999;
    border-radius: 4px;
    padding: 15px;
    background: #fff;
  }
  .three_nav{
    left: 299px;
  }
  .fore_nav{
   left: 215px;
  }
  .five_nav{
    left: 466px;
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
  .arrowTwo{
    width: 11px;
    height: 12px;
    position: relative;
    top: -21px;
    left: 130px;
    background: #fff;
    transform: rotate(45deg);
  }
  // .arrowThree{

  // }

</style>
