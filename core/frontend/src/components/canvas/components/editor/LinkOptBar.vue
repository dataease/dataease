<template>
  <div
    class="link-bar-main bar-light"
  >
    <div class="bar-first">
      <i class="el-icon-d-arrow-left" />
    </div>
    <div class="bar-content">
      <div class="bar-diver" />
      <div class="link-icon-active">
        <svg-icon
          style="width: 16px;height: 16px"
          icon-class="icon-fanhui"
        />
      </div>
      <div class="link-icon-active">
        <svg-icon
          style="width: 16px;height: 16px"
          icon-class="icon-xiazai"
        />
      </div>
      <div class="link-icon-active">
        <svg-icon
          style="width: 16px;height: 16px"
          :icon-class="fullscreenState?'icon-suoxiao1':'icon_magnify_outlined'"
        />
      </div>
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
    containerClass() {
      return this.isPublicLink && this.isPcTerminal ? 'trans-pc' : 'bar-main'
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
.link-bar-main {
  position: fixed;
  display: flex;
  z-index: 10;
  height: 30px;
  width: 130px;
  bottom: 24px;
  right: 0px;
  border-top-left-radius: 50%;
  border-bottom-left-radius: 50%;
}

.bar-first {
  width: 36px;
  border-left: 1px solid rgba(222, 224, 227, 1);
  border-top: 1px solid rgba(222, 224, 227, 1);
  border-bottom: 1px solid rgba(222, 224, 227, 1);
  background-color: rgba(255, 255, 255, 1);
  border-top-left-radius: 50%;
  border-bottom-left-radius: 50%;
  padding: 3px 0px 4px 8px;
}

.bar-diver {
  width: 1px;
  height: 18px;
  background: rgba(187, 191, 196, 1);
}

.bar-content {
  display: flex;
  align-items: center;
  width: 100px;
  border-top: 1px solid rgba(222, 224, 227, 1);
  border-bottom: 1px solid rgba(222, 224, 227, 1);
  background-color: rgba(255, 255, 255, 1);
}

.link-icon-active {
  width: 24px;
  height: 24px;
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;
  padding-left: 4px;
  text-align: center;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}
</style>
