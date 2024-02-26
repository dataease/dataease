<template>
  <div
    v-if="isPublicLink && isPcTerminal"
    id="fullscreenElement"
    ref="widget-div"
    class="link-bar-main bar-light"
    :class="[{['link-bar-main-active']: barActive},functionClass]"
    :style="{ '--fullWidth': fullWidth+'px','--fullContent': 28-fullWidth+'px','--firstHoveMove': 32-fullWidth+'px' }"
  >
    <div class="bar-first">
      <el-tooltip
        :content="barActive?$t('panel.fold'):$t('panel.expand')"
      >
        <svg-icon
          style="width: 16px;height: 16px"
          :icon-class="barActive?'icon_down-right_outlined':'icon_up-left_outlined'"
          @click="firstBarClick"
        />
      </el-tooltip>
    </div>
    <div class="bar-content">
      <div class="bar-diver" />
      <div
        v-show="fromLink"
        class="link-icon-active"
      >
        <el-tooltip
          :content="$t('pblink.back_parent')"
        >
          <svg-icon
            style="width: 16px;height: 16px"
            icon-class="icon_left_outlined"
            @click="back2Last"
          />
        </el-tooltip>
      </div>
      <div class="link-icon-active">
        <el-tooltip
          :content="$t('panel.export_pdf')"
        >
          <svg-icon
            style="width: 16px;height: 16px"
            icon-class="icon_download_outlined"
            @click="exportPDF"
          />
        </el-tooltip>
      </div>
      <div
        id="fullscreenElement"
        class="link-icon-active"
        style="padding-right: 4px"
      >
        <el-tooltip
          :content="fullscreenState? $t('panel.fullscreen_exit'): $t('panel.fullscreen_preview')"
        >
          <svg-icon
            style="width: 16px;height: 16px"
            :icon-class="fullscreenState?'icon_minify_outlined':'icon_magnify_outlined'"
            @click="toggleFullscreen"
          />
        </el-tooltip>
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
      fullscreenState: false,
      barActive: false,
      fullWidth: this.$route.query.fromLink === 'true' ? 126 : 94
    }
  },
  computed: {
    isPcTerminal() {
      return this.terminal === 'pc'
    },
    functionClass() {
      let result = 'link-bar-main-light'
      if (this.canvasStyleData?.panel?.themeColor === 'dark') {
        result = 'link-bar-main-dark'
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
    firstBarClick() {
      this.barActive = !this.barActive
    },
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
      this.$refs['widget-div'].style.display = 'none'
      this.$emit('link-export-pdf')
    },
    setWidgetStatus() {
      if (!this.isPublicLink || !this.$refs['widget-div']) {
        return
      }
      this.$refs['widget-div'].style.display = 'flex'
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
  width: var(--fullWidth);
  bottom: 24px;
  right: var(--fullContent);
  border-top-left-radius: 50%;
  border-bottom-left-radius: 50%;

  &:hover {
    right: var(--firstHoveMove);
  }

  transition: 0.2s; /* 添加过渡动画 */
}

.link-bar-main-light {
  color: rgba(31, 35, 41, 1);

  .bar-first {
    border-left: 1px solid rgba(222, 224, 227, 1);
    border-top: 1px solid rgba(222, 224, 227, 1);
    border-bottom: 1px solid rgba(222, 224, 227, 1);
    background-color: rgba(255, 255, 255, 1);
  }

  .bar-content {
    border-top: 1px solid rgba(222, 224, 227, 1);
    border-bottom: 1px solid rgba(222, 224, 227, 1);
    background-color: rgba(255, 255, 255, 1);

    .bar-diver {
      background: rgba(187, 191, 196, 1);
    }

  }
}

.link-bar-main-dark {
  color: rgba(255, 255, 255, 1);

  .bar-first {
    border-left: 1px solid rgba(67, 67, 67, 1);
    border-top: 1px solid rgba(67, 67, 67, 1);
    border-bottom: 1px solid rgba(67, 67, 67, 1);
    background-color: rgba(26, 26, 26, 1);
  }

  .bar-content {
    border-top: 1px solid rgba(67, 67, 67, 1);
    border-bottom: 1px solid rgba(67, 67, 67, 1);
    background-color: rgba(26, 26, 26, 1);

    .bar-diver {
      background: rgba(95, 95, 95, 1);
    }
  }
}

.link-bar-main-active {
  right: 0px !important;
  transition: 0.2s; /* 添加过渡动画 */
}

.bar-first {
  width: 36px;
  border-top-left-radius: 50%;
  border-bottom-left-radius: 50%;
  padding: 3px 0px 4px 8px;
  cursor: pointer;

  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}

.bar-diver {
  width: 1px;
  height: 18px;
}

.bar-content {
  display: flex;
  align-items: center;
  padding-right: 4px;
}

.link-icon-active {
  width: 24px;
  height: 24px;
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;
  padding-left: 4px;
  margin-left: 4px;
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
