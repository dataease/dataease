<script lang="ts" setup>
import { inject, computed } from 'vue'
import { getCSSVariable } from '@/utils/color'

const props = defineProps({
  options: {
    type: Array,
    default: () => []
  },
  activeItems: {
    type: Array,
    default: () => []
  },
  selectStyle: {
    type: Object,
    default: () => ({})
  }
})
const customStyle: any = inject('$custom-style-filter')

const customSelectStyle = computed(() => {
  return customStyle
    ? { ...props.selectStyle, background: customStyle.background }
    : props.selectStyle
})

const customColor = computed(() => {
  return customStyle
    ? { color: customStyle.text, fontSize: customStyle.placeholderSize + 'px' }
    : {}
})

const boxHeight = computed(() => {
  return `${customStyle?.queryConditionHeight || 32}px`
})

const btnColor = computed(() => {
  return customStyle ? customStyle.btnColor : getCSSVariable()
})

const emits = defineEmits(['handleItemClick'])
const handleItemClick = (item: any) => {
  emits('handleItemClick', item.value)
}
</script>

<template>
  <div :style="customSelectStyle" class="flat-select">
    <el-scrollbar>
      <div class="scrollbar-flex-content">
        <p
          @click="handleItemClick(item)"
          v-for="item in options"
          :key="item"
          :style="customColor"
          class="select-item"
          :class="activeItems.includes(item.value) && 'active-select'"
        >
          {{ item.label }}
        </p>
      </div>
    </el-scrollbar>
  </div>
</template>

<style lang="less" scoped>
.flat-select {
  .ed-scrollbar.ed-scrollbar.ed-scrollbar {
    padding: 0;
  }
  .scrollbar-flex-content {
    display: flex;
    width: fit-content;
    .select-item {
      height: v-bind(boxHeight);
      padding: 0 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      cursor: pointer;
      white-space: nowrap;
      &.active-select::after {
        content: '';
        width: 80%;
        height: 2px;
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        background-color: v-bind(btnColor) !important;
      }
    }
  }
}
</style>
