<template>
  <el-scrollbar ref="scrollbarRef" class="virtual-checkbox-list" @scroll="handleScroll">
    <el-checkbox-group v-model="checkedValues">
      <div class="virtual-list-container" :style="{ height: `${totalHeight}px` }">
        <div class="virtual-list-content" :style="{ transform: `translateY(${offsetY}px)` }">
          <div v-for="item in visibleItems" :key="item[keyProp]" class="kid-item">
            <el-checkbox
              :label="item[keyProp]"
              :style="{ height: `${itemHeight}px` }"
            >
              <slot name="item" :item="item"></slot>
            </el-checkbox>
          </div>
        </div>
      </div>
    </el-checkbox-group>
  </el-scrollbar>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted, watch } from 'vue'
import type { ElScrollbar } from 'element-plus-secondary'

const props = defineProps({
  items: {
    type: Array as () => any[],
    required: true,
    default: () => []
  },
  keyProp: {
    type: String,
    default: 'value'
  },
  itemHeight: {
    type: Number,
    default: 40
  },
  buffer: {
    type: Number,
    default: 5
  },
  modelValue: {
    type: Array as () => any[],
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const scrollbarRef = ref<InstanceType<typeof ElScrollbar> | null>(null)
const scrollTop = ref(0)
const containerHeight = ref(0)
const checkedValues = ref<any[]>(props.modelValue)

// 计算总高度
const totalHeight = computed(() => props.items.length * props.itemHeight)

// 计算可见区域
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - props.buffer)
})

const endIndex = computed(() => {
  const visibleCount = Math.ceil(containerHeight.value / props.itemHeight)
  return Math.min(
    props.items.length - 1,
    startIndex.value + visibleCount + props.buffer * 2
  )
})

// 计算偏移量
const offsetY = computed(() => startIndex.value * props.itemHeight)

// 计算可见项
const visibleItems = computed(() => {
  return props.items.slice(startIndex.value, endIndex.value + 1)
})

// 处理滚动事件
const handleScroll = ({ scrollTop: top }: { scrollTop: number }) => {
  scrollTop.value = top
}

// 更新容器高度
const updateContainerHeight = () => {
  if (scrollbarRef.value?.$el) {
    containerHeight.value = scrollbarRef.value.$el.clientHeight
  }
}

// 监听选中值变化
watch(checkedValues, (newVal) => {
  emit('update:modelValue', newVal)
  emit('change', newVal)
})

// 监听外部modelValue变化
watch(() => props.modelValue, (newVal) => {
  checkedValues.value = newVal
})

onMounted(() => {
  updateContainerHeight()
  window.addEventListener('resize', updateContainerHeight)
})
</script>

<style lang="less" scoped>
.virtual-checkbox-list {
  height: 100%;
  width: 100%;
  overflow-y: auto;

  .virtual-list-container {
    position: relative;
    width: 100%;
  }

  .virtual-list-content {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
  }

  /* .virtual-list-item {
    display: flex;
    align-items: center;
    padding: 0 12px;
    box-sizing: border-box;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    
    :deep(> *) {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  } */
}
.kid-item {
  height: 44px;
  line-height: 44px;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding-left: 16px;
  &:hover {
    background-color: #1f23291a;
  }
  :deep(.ed-checkbox__label) {
    display: flex;
    align-items: center;
  }
  .user-item-icon {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background-color: #f78704;
    color: #ffffff;
    margin-right: 8px;
  }
  .item-icon-1 {
    background-color: var(--ed-color-primary, #3370ff) !important;
  }
  .user-item-span {
    width: 300px;
    height: 22px;
    line-height: 22px;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
    -webkit-text-overflow: ellipsis;
    -moz-text-overflow: ellipsis;
    white-space: nowrap;

    :nth-child(1) {
      color: #1f2329 !important;
    }

    :nth-child(2) {
      color: #8d9199;
    }
  }
  .remove-hover-icon {
    width: 16px;
    height: 16px;
    cursor: pointer;
    color: #8f959e;
    margin: 4px;
    &:hover {
      width: 24px;
      margin: 0px;
      height: 24px;
      border-radius: 4px;
      background-color: #1f23291a;
    }
  }
}
</style>