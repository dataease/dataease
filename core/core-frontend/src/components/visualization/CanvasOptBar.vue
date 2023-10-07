<template>
  <div v-if="existLinkage" class="bar-main-right">
    <el-button size="mini" type="warning" @click="clearAllLinkage"
      ><el-icon class="bar-base-icon"> <Icon name="dv-bar-unLinkage"></Icon></el-icon
      >{{ $t('visualization.remove_all_linkage') }}</el-button
    >
  </div>
</template>

<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { isMainCanvas } from '@/utils/canvasUtils'
const { t } = useI18n()

const dvMainStore = dvMainStoreWithOut()

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
  }
})

const clearAllLinkage = () => {
  dvMainStore.clearPanelLinkageInfo()
}

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
  z-index: 20;
  position: absolute;
}

.bar-main-left {
  left: 0px;
  opacity: 0;
  height: fit-content;
  &:hover {
    opacity: 0.8;
  }
}
</style>
