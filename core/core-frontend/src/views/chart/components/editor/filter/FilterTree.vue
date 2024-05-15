<script lang="ts" setup>
import { ElMessage } from 'element-plus-secondary'
import { inject, computed, ref, nextTick, provide } from 'vue'
import RowAuth from '@/views/visualized/data/dataset/auth-tree/RowAuth.vue'

const emits = defineEmits(['filter-data'])
const filedList = inject('filedList', () => [])
const rowAuth = ref()
const dialogVisible = ref(false)
const computedFiledList = computed(() => {
  return filedList().reduce((pre, next) => {
    if (next.id !== '-1') {
      pre[next.id] = next
    }
    return pre
  }, {})
})

provide('filedList', computedFiledList)

const closeFilter = () => {
  dialogVisible.value = false
}

const submit = () => {
  rowAuth.value.submit()
}

const changeFilter = val => {
  const { logic, items, errorMessage } = val
  if (errorMessage) {
    ElMessage.error({
      message: errorMessage,
      type: 'error',
      showClose: true
    })
    return
  }
  dfsTreeDelete(items)
  emits('filter-data', { logic, items })
  dialogVisible.value = false
}

const dfsTreeDelete = arr => {
  arr.forEach(ele => {
    if (ele?.subTree?.items?.length) {
      dfsTreeDelete(ele.subTree.items || [])
    } else {
      if (ele.field) {
        delete ele.field
      }
    }
  })
}

const dfsTree = arr => {
  arr.forEach(ele => {
    if (ele?.subTree?.items?.length) {
      dfsTree(ele.subTree.items)
    } else {
      if (computedFiledList.value[ele.fieldId]) {
        ele.field = computedFiledList.value[ele.fieldId]
      }
    }
  })
}

const init = tree => {
  dialogVisible.value = true
  nextTick(() => {
    dfsTree(tree.items || [])
    rowAuth.value.init(tree || {})
  })
}

defineExpose({
  init
})
</script>
<template>
  <el-dialog
    width="896px"
    append-to-body
    title="添加过滤"
    destroy-on-close
    class="de-dialog-form filter-tree-cont"
    v-model="dialogVisible"
  >
    <div class="tree-cont">
      <div class="content">
        <RowAuth @save="changeFilter" ref="rowAuth" />
      </div>
    </div>
    <template #footer>
      <el-button secondary @click="closeFilter">{{ $t('chart.cancel') }} </el-button>
      <el-button type="primary" @click="submit">{{ $t('chart.confirm') }} </el-button>
    </template>
  </el-dialog>
</template>
<style lang="less">
.filter-tree-cont {
  .tree-cont {
    min-height: 67px;
    width: 100%;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid var(--deBorderBase, #dcdfe6);
    overflow: auto;
    max-height: 500px;
    .content {
      height: 100%;
      width: 100%;
    }
  }
}
</style>
