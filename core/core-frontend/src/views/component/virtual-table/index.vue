<template>
  <el-table
    ref="tableRef"
    v-bind="$attrs"
    :data="visibleData"
    class="virtual-table-content"
    v-on="$listeners"
  >
    <slot></slot>
  </el-table>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import type { ElTable } from 'element-plus-secondary'

const props = defineProps({
  data: {
    type: Array as () => any[],
    required: true
  },
  rowHeight: {
    type: Number,
    default: 40
  },
  buffer: {
    type: Number,
    default: 5
  }
})
const id = ref('')
const emit = defineEmits(['scroll'])

const tableRef = ref<InstanceType<typeof ElTable> | null>(null)
const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算总高度
const totalHeight = computed(() => props.data.length * props.rowHeight)

// 计算可见区域
const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.rowHeight) - props.buffer)
})

const endIndex = computed(() => {
  const visibleRowCount = Math.ceil(containerHeight.value / props.rowHeight)
  return Math.min(
    props.data.length - 1,
    startIndex.value + visibleRowCount + props.buffer * 2
  )
})

const offsetY = computed(() => startIndex.value * props.rowHeight)

const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value + 1)
})

// 处理滚动事件
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  scrollTop.value = target.scrollTop
}


// 更新容器高度
const updateContainerHeight = () => {
  if (tableRef.value) {
    containerHeight.value = tableRef.value.$el.clientHeight
    const virtualDom = document.getElementById(id.value)
    if (virtualDom) {
      virtualDom.setAttribute('style',`height: ${totalHeight.value}px;` )
      if (virtualDom.parentElement) {
        virtualDom.parentElement.style.height = `${totalHeight.value}px`
      }
    }
  }
}

// 暴露el-table的方法
const exposeTableMethods = () => {
  if (tableRef.value) {
    return {
      clearSelection: tableRef.value.clearSelection,
      toggleRowSelection: tableRef.value.toggleRowSelection,
      toggleAllSelection: tableRef.value.toggleAllSelection,
      toggleRowExpansion: tableRef.value.toggleRowExpansion,
      setCurrentRow: tableRef.value.setCurrentRow,
      clearSort: tableRef.value.clearSort,
      clearFilter: tableRef.value.clearFilter,
      doLayout: tableRef.value.doLayout,
      sort: tableRef.value.sort
    }
  }
  return {}
}
const generateUUID = () => {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    var r = Math.random() * 16 | 0,
        v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}

const createVirtualDom = () => {
  const el = tableRef.value.$el
  const scollerDom = el.querySelector('.ed-scrollbar__view')
  const virtualDom = document.createElement('div')
  virtualDom.setAttribute('id', id.value)
  virtualDom.setAttribute('style',`height: ${totalHeight.value}px;` )
  virtualDom.classList.add('virtual-table-placeholder')
  scollerDom.appendChild(virtualDom)
  scollerDom.style.height = `${totalHeight.value}px`
  scollerDom.parentElement.addEventListener('scroll', handleScroll)
}

// 暴露组件方法
defineExpose({
  ...exposeTableMethods()
})

onMounted(() => {
  id.value = generateUUID()
  updateContainerHeight()
  window.addEventListener('resize', updateContainerHeight)
  createVirtualDom()
})

watch(() => props.data, () => {
  nextTick(updateContainerHeight)
})
watch(() => offsetY.value, () => {
  const virtualDom = document.getElementById(id.value)
  if (virtualDom) {
    const parent = virtualDom.parentElement
    if (parent) {
      const bodyDom = parent.querySelector('.ed-table__body')
      if (bodyDom) {
        bodyDom.style.transform = `translateY(${offsetY.value}px)`
      }
    }
  }
})
</script>

<style lang="less" scoped>

.virtual-table-content {
  :deep(.ed-scrollbar__view) {
    position: relative;
    // height: 100%;
    .virtual-table-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      z-index: -1;
    }
  }
}
</style>