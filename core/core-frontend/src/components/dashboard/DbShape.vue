<template>
  <div class="shape" :class="{ active }">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
import eventBus from '@/utils/eventBus'
import { nextTick, onMounted, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const composeStore = composeStoreWithOut()

const { curComponent } = storeToRefs(dvMainStore)
const { editor } = storeToRefs(composeStore)

const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  element: {
    required: true,
    type: Object,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  }
})

const { active, element, index } = toRefs(props)

const selectCurComponent = e => {
  // 阻止向父组件冒泡
  e.stopPropagation()
  e.preventDefault()
}

const handleMouseDownOnShape = e => {
  // 将当前点击组件的事件传播出去
  nextTick(() => eventBus.emit('componentClick'))
  dvMainStore.setInEditorStatus(true)
  dvMainStore.setClickComponentStatus(true)
  e.stopPropagation()
}
</script>

<style lang="less" scoped>
.shape {
  width: 100%;
  height: 100%;

  &:hover {
    cursor: move;
  }
}

.active {
  outline: 1px solid #70c0ff;
  user-select: none;
}
</style>
