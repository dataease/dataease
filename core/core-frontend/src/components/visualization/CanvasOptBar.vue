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
  let linkageFiltersCount = 0
  props.componentData.forEach(item => {
    if (item.linkageFilters && item.linkageFilters.length > 0) {
      linkageFiltersCount++
    }
  })
  return linkageFiltersCount
})
</script>

<style lang="less" scoped>
.bar-main-right {
  right: 0px;
  opacity: 0.8;
  z-index: 10000;
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
