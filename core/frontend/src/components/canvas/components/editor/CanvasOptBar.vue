<template>

  <div
    class="bar-main"
  >
    <div
      v-if="existLinkage || backToTopBtn"
      class="bar-main-right"
    >
      <el-button
        v-if="existLinkage"
        size="mini"
        type="warning"
        @click="clearAllLinkage"
      ><i class="icon iconfont icon-quxiaoliandong" />{{ $t('panel.remove_all_linkage') }}</el-button>

      <el-button
        v-if="backToTopBtn"
        size="mini"
        type="warning"
        @click="backToTop"
      ><i class="icon iconfont icon-back-top" />{{ $t('panel.back_to_top') }}</el-button>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import { isMobile } from '@/utils'
import bus from '@/utils/bus'

export default {
  props: {
    canvasStyleData: {
      type: Object,
      default: null
    },
    backToTopBtn: {
      type: Boolean,
      default: false
    },
    terminal: {
      type: String,
      default: 'pc'
    }
  },
  data() {
    return {
      fullscreenElement: null,
      fullscreenState: false
    }
  },
  computed: {
    isPcTerminal() {
      return this.terminal === 'pc'
    },
    functionClass() {
      let result = 'function-light'
      if (this.canvasStyleData?.panel?.themeColor === 'dark') {
        result = 'function-dark'
      }
      return result
    },
    existLinkage() {
      let linkageFiltersCount = 0
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          linkageFiltersCount++
        }
      })
      return linkageFiltersCount
    },
    isPublicLink() {
      return this.$router.currentRoute.path === '/delink'
    },
    fromLink() {
      return this.$route.query.fromLink === 'true'
    },
    ...mapState([
      'componentData'
    ])
  },

  mounted() {
    this.fullscreenElement = document.getElementById('fullscreenElement')
    document.addEventListener('fullscreenchange', this.handleFullscreenChange)
  },
  beforeDestroy() {
    // 在组件销毁前移除事件监听器
    document.removeEventListener('fullscreenchange', this.handleFullscreenChange)
  },
  methods: {
    handleFullscreenChange() {
      // 在全屏状态变化时触发此方法
      if (document.fullscreenElement) {
        this.fullscreenState = true
      } else {
        this.fullscreenState = false
      }
    },
    toggleFullscreen() {
      if (!document.fullscreenElement) {
        // 如果当前不是全屏状态，则启动全屏
        document.documentElement.requestFullscreen().catch(error => {
          console.error('Request fullscreen failed:', error)
        })
      } else {
        // 如果当前是全屏状态，则退出全屏
        document.exitFullscreen().catch(error => {
          console.error('Exit fullscreen failed:', error)
        })
      }
    },
    clearAllLinkage() {
      this.$store.commit('clearPanelLinkageInfo')
      bus.$emit('clear_panel_linkage', { viewId: 'all' })
    },
    back2Last() {
      if (isMobile()) {
        let parentUrl = window.location.href
        parentUrl = localStorage.getItem('beforeJumpUrl')
        localStorage.removeItem('beforeJumpUrl')
        window.location.href = parentUrl
        window.location.reload()
        return false
      } else {
        const parentUrl = localStorage.getItem('beforeJumpUrl')
        localStorage.removeItem('beforeJumpUrl')
        window.location.href = parentUrl
        window.location.reload()
      }
    },
    exportPDF() {
      this.$refs['widget-div'].style.display = ''
      this.$emit('link-export-pdf')
    },
    setWidgetStatus() {
      if (!this.isPublicLink || !this.$refs['widget-div']) {
        return
      }
      const val = this.$refs['widget-div'].style.display

      this.$refs['widget-div'].style.display = val ? '' : 'block'
    },
    backToTop() {
      this.$emit('back-to-top')
    }
  }
}
</script>

<style lang="scss" scoped>
  .bar-main {
    display: flex;
    div {
      position: absolute;
      z-index: 10;
      height: 20px;
      border-radius:2px;
      padding-left: 3px;
      padding-right: 0px;
      cursor:pointer!important;
    }
  }
  .bar-main-right{
    right: 0px;
    opacity: 0.8;
  }

  .bar-main-left {
    left: 0px;
    opacity: 0;
    height: fit-content;
    &:hover {
      opacity: 0.8;
    }
  }

  .trans-pc {
    position: absolute;
    width: 60px;
    right: 0;
    top: 0;
    border-top: 10px solid rgba(245, 74, 69, 0);
    border-left: 60px solid transparent;
    cursor: pointer;
    z-index: 999;
    .function-div {
      display: none;
      position: absolute;
      right: 10px;
      top: -50px;
      width: max-content;
      text-align: end;
      z-index: 999;
      border-radius: 4px;
      ::v-deep button {
        border-radius: 0px;
      }
    }
    .function-light {
      background: #FFFFFF;
      border: 1px solid #DEE0E3;
      box-shadow: 0px 4px 8px rgb(31 35 41 / 10%);
      ::v-deep button {
        background-color: #FFFFFF;
        box-shadow: 0px 4px 8px rgba(31, 35, 41, 0.1);
        border: 1px solid #DEE0E3;
        &:hover {
          background-color: rgba(31, 35, 41, 0.1);
          color: #1F2329;
          font-weight: bold;
          border-color: rgba(31, 35, 41, 0.1)
        }
      }
    }
    .function-dark {
      background: #1A1A1A;
      border: 1px solid #434343;
      box-shadow: 0px 4px 8px rgba(26, 26, 26, 0.1);
      ::v-deep button {
        background-color: #1A1A1A;
        border: 1px solid #434343;
        box-shadow: 0px 4px 8px rgba(26, 26, 26, 0.1);
        color: #FFFFFF;
        &:hover {
          background-color: rgba(235, 235, 235, 0.1);
          color: #EBEBEB;
          font-weight: bold;
          border-color: rgba(235, 235, 235, 0.1);
        }
      }
    }
    &:hover {
      border-top: 60px solid rgba(245, 74, 69, 0);;
      .function-div {
        display: block;
      }
    }
  }

  .link-public {
    top: -49px;
    right: 8px;
    opacity: 0.8;
    position: absolute;
  }

  .function-back-div {
    right: 100px!important;
  }
</style>
