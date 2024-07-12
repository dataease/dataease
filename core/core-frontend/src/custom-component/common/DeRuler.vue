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
  size: {
    type: Number,
    default: 300 // 尺子方向
  },
  direction: {
    type: String,
    default: 'horizontal' // 尺子方向
  }
})

const labelInterval = 5

const { canvasStyleData, curComponent } = storeToRefs(dvMainStore)

const rulerSize = computed(() =>
  props.direction === 'horizontal' ? canvasStyleData.value.width : canvasStyleData.value.height
)

const curComponentSize = computed(() => {
  if (curComponent.value) {
    return (
      ((props.direction === 'horizontal'
        ? curComponent.value.style.width
        : curComponent.value.style.height) *
        canvasStyleData.value.scale) /
      100
    )
  } else {
    return 0
  }
})

const curComponentShadow = computed(() => {
  if (curComponent.value) {
    return {
      left:
        (props.direction === 'horizontal'
          ? curComponent.value.style.left
          : curComponent.value.style.top) + 'px',
      width:
        (props.direction === 'horizontal'
          ? curComponent.value.style.width
          : curComponent.value.style.height) + 'px'
    }
  } else {
    return {}
  }
})

const ticks = computed(() => {
  const result = []
  let currentValue = 0
  while (currentValue <= rulerSize.value) {
    const isLong = currentValue % (labelInterval * tickSize.value) === 0
    const label = isLong ? props.tickLabelFormatter(currentValue) : ''
    result.push({ position: (currentValue * canvasStyleData.value.scale) / 100, label, isLong })
    currentValue += tickSize.value
  }
  return result
})

const wStyle = computed(() => {
  return {
    width: rulerSize.value * 1.5 + 'px'
  }
})

const radio = computed(() => rulerSize.value / canvasStyleData.value.width)
const tickSize = computed(
  () =>
    10 *
    Math.max(
      Math.floor((200000 * radio.value) / (rulerSize.value * canvasStyleData.value.scale)),
      1
    )
)

const scaleWidth = computed(() => (rulerSize.value * canvasStyleData.value.scale) / 100)

const rulerScroll = e => {
  const left = props.direction === 'vertical' ? e.scrollTop : e.scrollLeft
  wRuleRef.value.scrollTo(left, 0)
}

const outerStyle = computed(() => {
  return {
    width: props.direction === 'vertical' ? props.size - 30 + 'px' : '100%'
  }
})

const curShadowShow = computed(() => curComponent.value && curComponent.value.category !== 'hidden')

defineExpose({
  rulerScroll
})
</script>

<template>
  <div
    class="ruler-outer"
    :style="outerStyle"
    :class="{ 'ruler-vertical': direction === 'vertical' }"
    ref="wRuleRef"
  >
    <!--覆盖着尺子上方防止鼠标移到尺子位置滑动-->
    <div class="ruler-shadow" :style="outerStyle"></div>
    <div :style="wStyle" class="ruler-outer-scroll">
      <div class="ruler" :style="{ width: `${scaleWidth}px` }">
        <div v-if="curShadowShow" :style="curComponentShadow" class="cur-shadow"></div>
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
.ruler-vertical {
  position: absolute;
  left: 30px;
  top: 30px;
  transform-origin: top left;
  transform: rotate(90deg);
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 2;
  .ruler {
    .ruler-line {
      top: 0;
    }
    .ruler-tick {
      top: 0;
      .tick-label {
        transform: rotate(180deg);
      }
    }
  }
}

.ruler-shadow {
  position: absolute;
  height: 30px;
  z-index: 10;
  overflow: hidden;
}

.ruler-outer {
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

.cur-shadow {
  background: rgba(10, 123, 224, 0.3);
  height: 30px;
  position: absolute;
}
</style>
