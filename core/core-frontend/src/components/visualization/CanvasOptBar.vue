<template>
  <div
    v-if="existLinkage && (!dvMainStore.mobileInPc || isMobile())"
    class="bar-main-right"
    :class="{
      'bar-main-preview-fixed': dvPreviewMode,
      'bar-main-preview-fixed-fullscreen': fullscreenFlag
    }"
    @mousedown="handOptBarMousedown"
  >
    <el-button type="warning" @click="clearAllLinkage"
      ><el-icon class="bar-base-icon">
        <Icon name="dv-bar-unLinkage"><dvBarUnLinkage class="svg-icon" /></Icon></el-icon
      >{{ $t('visualization.remove_all_linkage') }}</el-button
    >
  </div>
</template>

<script lang="ts" setup>
import dvBarUnLinkage from '@/assets/svg/dv-bar-unLinkage.svg'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed } from 'vue'
import { isMainCanvas } from '@/utils/canvasUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { isMobile } from '@/utils/utils'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { fullscreenFlag } = storeToRefs(dvMainStore)

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  componentData: {
    type: Object,
    required: true
  },
  canvasId: {
    type: String,
    required: false,
    default: 'canvas-main'
  },
  isFixed: {
    type: Boolean,
    default: false
  }
})

const handOptBarMousedown = e => {
  e.preventDefault()
  e.stopPropagation()
}

const clearAllLinkage = () => {
  dvMainStore.clearPanelLinkageInfo()
  useEmitt().emitter.emit('clearPanelLinkage', { viewId: 'all' })
}

const dvPreviewMode = computed(() => {
  return dvMainStore.dvInfo.type === 'dataV' && props.isFixed
})

const existLinkage = computed(() => {
  if (isMainCanvas(props.canvasId)) {
    let linkageFiltersCount = 0
    props.componentData.forEach(item => {
      if (item.component === 'UserView') {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          linkageFiltersCount++
        }
      } else if (item.component === 'Group') {
        item.propValue.forEach(groupItem => {
          if (groupItem.linkageFilters && groupItem.linkageFilters.length > 0) {
            linkageFiltersCount++
          }
        })
      } else if (item.component === 'DeTabs') {
        item.propValue.forEach(tabItem => {
          tabItem.componentData.forEach(tabComponent => {
            if (tabComponent.linkageFilters && tabComponent.linkageFilters.length > 0) {
              linkageFiltersCount++
            }
          })
        })
      }
    })
    return linkageFiltersCount
  } else {
    return false
  }
})
</script>

<style lang="less" scoped>
.bar-main-right {
  top: 2px;
  right: 2px;
  opacity: 0.8;
  z-index: 2;
  position: absolute;
}

.bar-main-edit-right {
  top: 8px;
  right: 102px !important;
}

.bar-main-left {
  left: 0px;
  opacity: 0;
  height: fit-content;
  &:hover {
    opacity: 0.8;
  }
}

.bar-main-preview-fixed {
  position: fixed;
  top: 120px;
  right: 5px;
}

.bar-main-preview-fixed-fullscreen {
  top: 5px !important;
}
</style>
