<template>
  <el-scrollbar
    ref="scrollbarRef"
    :style="{ height: `${props.height}px` }"
    @scroll="handleScroll"
  >
    <div
      class="virtual-list-container"
      :style="{ height: `${totalHeight}px`, position: 'relative' }"
    >
      <div
        class="virtual-list-content"
        :style="{ position: 'absolute', top: `${startOffset}px`, width: '100%' }"
      >
        <div
          v-for="item in visibleData"
          :key="itemKey ? item[itemKey] : item"
          :style="{ height: `${props.itemSize}px` }"
        >
          <slot :item="item" :index="item.__index" />
        </div>
      </div>
    </div>
  </el-scrollbar>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, nextTick } from 'vue'

interface Props {
  data: any[]
  itemSize?: number
  height?: number
  buffer?: number
  itemKey?: string
}

const props = withDefaults(defineProps<Props>(), {
  itemSize: 40,
  height: 400,
  buffer: 5,
  itemKey: ''
})

const emit = defineEmits(['scroll'])

const scrollbarRef = ref<any>(null)
const scrollTop = ref(0)

// 总高度
const totalHeight = computed(() => props.data.length * props.itemSize)

// 可见区域能显示的项目数
const visibleCount = computed(() => Math.ceil(props.height / props.itemSize))

// 起始索引
const startIndex = computed(() =>
  Math.max(0, Math.ceil(scrollTop.value / props.itemSize) - props.buffer)
)

// 结束索引
const endIndex = computed(() =>
  Math.min(
    props.data.length - 1,
    startIndex.value + visibleCount.value + props.buffer * 2
  )
)

// 偏移量（让列表看起来是在滚动）
const startOffset = computed(() => startIndex.value * props.itemSize)

// 当前可见的数据
const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value + 1).map((item, index) => {
    return {
      ...item,
      __index: startIndex.value + index
    }
  })
})

// 处理滚动事件
const handleScroll = (e: { scrollTop: number }) => {
  scrollTop.value = e.scrollTop
  emit('scroll', e)
}

// 滚动到指定位置
const scrollTo = (index: number) => {
  if (scrollbarRef.value) {
    const scrollTop = index * props.itemSize
    scrollbarRef.value.setScrollTop(scrollTop)
  }
}

// 滚动到顶部
const scrollToTop = () => {
  scrollTo(0)
}

// 滚动到底部
const scrollToBottom = () => {
  scrollTo(props.data.length - 1)
}

onMounted(() => {
  nextTick(() => {
    // 确保scrollbar组件已渲染
    if (scrollbarRef.value) {
      // 初始化滚动位置
      scrollbarRef.value.setScrollTop(0)
    }
  })
})

// 暴露方法给父组件
defineExpose({
  scrollTo,
  scrollToTop,
  scrollToBottom
})
</script>

<style scoped>
.virtual-list-container {
  width: 100%;
}
</style>