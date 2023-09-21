<script setup lang="ts">
import { toRefs } from 'vue'
import findComponent from '@/utils/components'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  dvInfo: {
    type: Object,
    required: true
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  }
})

const { propValue, element, dvInfo, searchCount } = toRefs(props)
</script>

<template>
  <div class="group">
    <div>
      <component-wrapper
        v-for="(item, index) in propValue"
        :id="'component' + item.id"
        :view-info="canvasViewInfo[item.id]"
        :key="index"
        :config="item"
        :index="index"
        :dv-info="dvInfo"
        :style="item.groupStyle"
        :show-position="showPosition"
        :search-count="searchCount"
      />
    </div>
  </div>
</template>

<style lang="less" scoped>
.group {
  & > div {
    position: relative;
    width: 100%;
    height: 100%;

    .component {
      position: absolute;
    }
  }
}
</style>
