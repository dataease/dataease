<script lang="ts" setup>
import { PropType, toRefs } from 'vue'
import draggable from 'vuedraggable'
export interface DragItem {
  id: string
  name: string
}

const props = defineProps({
  dragItems: {
    type: Array as PropType<DragItem[]>,
    default: () => []
  }
})
const { dragItems } = toRefs(props)
const handleClose = index => {
  dragItems.value.splice(index, 1)
}
</script>

<template>
  <div class="filter-header">
    <draggable :list="dragItems" group="dimension" class="drag-content" item-key="id">
      <template #item="{ element, index }">
        <el-tag @close="handleClose(index)" closable>{{ element.name }}</el-tag>
      </template>
      <template #footer>
        <span class="footer">拖动维度至此处</span>
      </template>
    </draggable>
  </div>
</template>

<style lang="less" scoped>
.filter-header {
  width: 100%;
  height: 100%;
  position: relative;
  .drag-content {
    width: 100%;
    height: 100%;
    border-radius: 4px;
    overflow-x: auto;
    display: flex;
    align-items: center;
    .el-tag + .el-tag {
      margin-left: 8px;
    }
    :deep(.filter-db-row) {
      background-color: #3685f2;
      color: #fff;
      margin: 5px 0;
      white-space: nowrap;
      span {
        margin-left: 6px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .footer {
    white-space: nowrap;
    margin-left: 8px;
  }
}
</style>
