<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  width: {
    type: Number,
    required: true
  },
  tickSize: {
    type: Number,
    default: 10 // 刻度大小，可以根据需要调整
  },
  labelInterval: {
    type: Number,
    default: 5 // 长刻度线的间隔（多少个小刻度线之后）
  },
  tickLabelFormatter: {
    type: Function,
    default: value => value.toString() // 刻度标签格式化函数，默认直接转为字符串
  }
})

const ticks = computed(() => {
  const result = []
  let currentValue = 0
  while (currentValue <= props.width) {
    const isLong = currentValue % (props.labelInterval * props.tickSize) === 0
    const label = isLong ? props.tickLabelFormatter(currentValue) : ''
    result.push({ position: currentValue, label, isLong })
    currentValue += props.tickSize
  }
  return result
})
</script>

<template>
  <div class="ruler-outer">
    <div class="ruler">
      <div class="ruler-line" :style="{ width: `${width}px` }"></div>
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
</template>

<style scoped lang="less">
.ruler-outer {
  width: 100%;
  overflow-x: auto;
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
