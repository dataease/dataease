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
  },
  direction: {
    type: String,
    default: 'horizontal' // 尺子方向
  }
})

const labelInterval = 5

const { canvasStyleData } = storeToRefs(dvMainStore)

const ticks = computed(() => {
  const result = []
  let currentValue = 0
  while (currentValue <= canvasStyleData.value.height) {
    const isLong = currentValue % (labelInterval * tickSize.value) === 0
    const label = isLong ? props.tickLabelFormatter(currentValue) : ''
    result.push({ position: (currentValue * canvasStyleData.value.scale) / 100, label, isLong })
    currentValue += tickSize.value
  }
  return result
})

const hStyle = computed(() => {
  return {
    height: canvasStyleData.value.height * 1.5 + 'px'
  }
})
const tickSize = computed(
  () =>
    10 *
    Math.max(Math.floor(200000 / (canvasStyleData.value.height * canvasStyleData.value.scale)), 1)
)

const scaleHeight = computed(
  () => (canvasStyleData.value.height * canvasStyleData.value.scale) / 100
)

const rulerScroll = e => {
  wRuleRef.value.scrollTo(0, e.scrollHeight)
}

defineExpose({
  rulerScroll
})
</script>

<template>
  <div class="ruler-outer-vertical" ref="wRuleRef">
    testtest
    <!--覆盖着尺子上方防止鼠标移到尺子位置滑动-->
    <div class="ruler-shadow-vertical"></div>
    <div :style="hStyle" class="ruler-outer-vertical-scroll">
      <div class="ruler" :style="{ height: `${scaleHeight}px` }">
        <div class="ruler-line" :style="{ height: `${scaleHeight}px` }"></div>
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
.ruler-shadow-vertical {
  position: absolute;
  width: 30px;
  height: 100%;
  z-index: 10;
  overflow: hidden;
}

.ruler-outer-vertical {
  position: absolute;
  width: 30px;
  height: 100%;
  overflow-y: auto;
  background-color: #2c2c2c;
}

.ruler-outer-vertical-scroll {
  display: flex;
  align-items: center;
  justify-content: center;
}
.ruler {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  height: 100%;
  border-left: 1px solid #974e4e;
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
