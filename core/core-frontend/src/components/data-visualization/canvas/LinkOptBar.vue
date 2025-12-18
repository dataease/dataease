<template>
  <div
    id="fullscreenElement"
    ref="widget-div"
    class="link-bar-main bar-light"
    :class="[{ ['link-bar-main-active']: state.barActive }, functionClass]"
    :style="{
      '--fullWidth': state.fullWidth + 'px',
      '--fullContent': 28 - state.fullWidth + 'px',
      '--firstHoveMove': 32 - state.fullWidth + 'px'
    }"
  >
    <div class="bar-first">
      <el-tooltip
        :disabled="isMobile()"
        :content="state.barActive ? t('visualization.fold') : t('visualization.expand')"
      >
        <el-icon style="width: 16px; height: 16px" @click="firstBarClick">
          <Icon name="icon_down_right_outlined" v-if="state.barActive">
            <icon_down_right_outlined />
          </Icon>
          <Icon name="icon_up_left_outlined" v-else>
            <icon_up_left_outlined />
          </Icon>
        </el-icon>
      </el-tooltip>
    </div>
    <div class="bar-content">
      <div class="bar-diver" />
      <div v-show="fromLink" class="link-icon-active">
        <el-tooltip :disabled="isMobile()" :content="t('visualization.back_parent')">
          <el-icon style="width: 16px; height: 16px" @click="back2Last">
            <Icon name="icon_left_outlined">
              <icon_left_outlined class="svg-icon" />
            </Icon>
          </el-icon>
        </el-tooltip>
      </div>
      <div class="link-icon-active">
        <el-tooltip :disabled="isMobile()" :content="t('visualization.export_pdf')">
          <el-icon style="width: 16px; height: 16px" @click="exportPDF">
            <Icon name="icon_download_outlined">
              <icon_download_outlined class="svg-icon" />
            </Icon>
          </el-icon>
        </el-tooltip>
      </div>
      <div id="fullscreenElement" class="link-icon-active" style="padding-right: 4px">
        <el-tooltip
          :disabled="isMobile()"
          :content="
            fullscreenFlag ? t('visualization.ext_fullscreen') : t('visualization.fullscreen')
          "
        >
          <el-icon style="width: 16px; height: 16px" @click="toggleFullscreen">
            <Icon name="icon_minify_outlined" v-if="fullscreenFlag">
              <icon_minify_outlined class="svg-icon" />
            </Icon>
            <Icon name="icon_magnify_outlined" v-else>
              <icon_magnify_outlined class="svg-icon" />
            </Icon>
          </el-icon>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, toRefs } from 'vue'
import router from '@/router'
import { ElIcon } from 'element-plus-secondary'
import Icon from '../../icon-custom/src/Icon.vue'
import icon_down_right_outlined from '@/assets/svg/icon_down-right_outlined.svg'
import icon_up_left_outlined from '@/assets/svg/icon_up-left_outlined.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import icon_download_outlined from '@/assets/svg/icon_download_outlined.svg'
import icon_minify_outlined from '@/assets/svg/icon_minify_outlined.svg'
import icon_magnify_outlined from '@/assets/svg/icon_magnify_outlined.svg'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { isMobile } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  }
})

const { canvasStyleData } = toRefs(props)
const { fullscreenFlag } = storeToRefs(dvMainStore)
const { t } = useI18n()

const state = reactive({
  fullscreenElement: null,
  barActive: false,
  fullWidth: router.currentRoute.value.query?.fromLink === 'true' ? 126 : 94
})

const functionClass = computed(() => {
  let result = 'link-bar-main-light'
  if (canvasStyleData.value?.dashboard?.themeColor === 'dark') {
    result = 'link-bar-main-dark'
  }
  return result
})

const fromLink = computed(() => {
  return router.currentRoute.value.query?.fromLink === 'true'
})

const firstBarClick = () => {
  state.barActive = !state.barActive
}
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    // 如果当前不是全屏状态，则启动全屏
    useEmitt().emitter.emit('canvasFullscreen')
  } else {
    // 如果当前是全屏状态，则退出全屏
    document.exitFullscreen().catch(error => {
      console.error('Exit fullscreen failed:', error)
    })
  }
}
const back2Last = () => {
  const parentUrl = localStorage.getItem('beforeJumpUrl')
  localStorage.removeItem('beforeJumpUrl')
  window.location.href = parentUrl
  window.location.reload()
}
const exportPDF = () => {
  useEmitt().emitter.emit('canvasDownload', 'pdf')
}
</script>

<style lang="less" scoped>
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
  padding: 4px 0 4px 8px;
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
  transition: 0.1s;
  border-radius: 3px;
  padding-top: 2px;
  padding-left: 2px;
  padding-right: 2px;
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
