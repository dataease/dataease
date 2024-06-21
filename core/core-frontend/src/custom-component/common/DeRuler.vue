<script setup lang="ts">
import { computed, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const wRuleRef = ref(null)
const props = defineProps({
  tickLabelFormatter: {
    type: Function,
    default: value => value.toString() // 刻度标签格式化函数，默认直接转为字符串
  }
})

const labelInterval = 5

const { canvasStyleData } = storeToRefs(dvMainStore)

const ticks = computed(() => {
  const result = []
  let currentValue = 0
  while (currentValue <= canvasStyleData.value.width) {
    const isLong = currentValue % (labelInterval * tickSize.value) === 0
    const label = isLong ? props.tickLabelFormatter(currentValue) : ''
    result.push({ position: (currentValue * canvasStyleData.value.scale) / 100, label, isLong })
    currentValue += tickSize.value
  }
  return result
})

const wStyle = computed(() => {
  return {
    width: canvasStyleData.value.width * 1.5 + 'px'
  }
})
const tickSize = computed(
  () =>
    10 *
    Math.max(Math.floor(200000 / (canvasStyleData.value.width * canvasStyleData.value.scale)), 1)
)

const scaleWidth = computed(() => (canvasStyleData.value.width * canvasStyleData.value.scale) / 100)

const rulerScroll = e => {
  wRuleRef.value.scrollTo(e.scrollLeft, 0)
}

defineExpose({
  rulerScroll
})
</script>

<template>
  <div class="ruler-outer" ref="wRuleRef">
    <div :style="wStyle" class="ruler-outer-scroll">
      <div class="ruler" :style="{ width: `${scaleWidth}px` }">
        <div class="ruler-line" :style="{ width: `${scaleWidth}px` }"></div>
        <div
          v-for="(tick, index) in ticks"
          :key="index"
          class="ruler-tick"
          :class="{ 'long-tick': tick.isLong }"
          :style="{ left: `${tick.position}px` }"
        >
          <span v-if="tick.isLong" class="tick-label">{{ tick.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}
.ruler-outer {
  width: 100%;
  overflow-x: auto;
  background-color: #2c2c2c;
}

.ruler-outer-scroll {
  display: flex;
  justify-content: center;
}
.ruler {
  position: relative;
  height: 30px;
  display: flex;
  align-items: center;
  background-color: #2c2c2c;
}

.ruler-line {
  position: absolute;
  bottom: 0;
  height: 1px;
  background-color: #ac2a2a;
}

.ruler-tick {
  position: absolute;
  bottom: 1px;
  height: 3px;
  width: 1px;
  background-color: #e38a8a;
}

.long-tick {
  width: 1px;
  height: 15px;
}

.tick-label {
  position: absolute;
  bottom: 2px;
  font-size: 8px;
  left: 50%;
  transform: translateX(2%);
  white-space: nowrap;
}
</style>
