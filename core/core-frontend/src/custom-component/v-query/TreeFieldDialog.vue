<script lang="ts" setup>
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { ElMessage } from 'element-plus-secondary'
import { ref, shallowRef, computed } from 'vue'
import { cloneDeep } from 'lodash-es'

const dialogVisible = ref(false)
const treeList = ref([])
const datasetMap = shallowRef([])
const emits = defineEmits(['saveTree'])

const addCascadeItem = () => {
  treeList.value.push({
    field: null,
    id: guid()
  })
}
const handleBeforeClose = () => {
  dialogVisible.value = false
}
const init = (arr, treeArr) => {
  datasetMap.value = cloneDeep(arr)
  treeList.value = cloneDeep(treeArr).map(ele => ({ field: ele, id: ele.id }))
  dialogVisible.value = true
}

const disableFieldArr = computed(() => {
  return treeList.value.map(ele => ele.field).map(ele => ele.id)
})

const cancelClick = () => {
  handleBeforeClose()
}
const setCascadeArrBack = () => {
  let isError = false
  const arr = cloneDeep(treeList.value).map(item => {
    if (!item.field) {
      isError = true
    }
    return item.field
  })

  return {
    arr,
    isError
  }
}
const confirmClick = () => {
  const { isError, arr } = setCascadeArrBack()
  if (isError) {
    ElMessage.error('层级字段不能为空,请选择字段!')
    return
  }
  emits('saveTree', arr)
  handleBeforeClose()
}
const indexCascade = ' 一二三四五'

const deleteCascade = idx => {
  treeList.value.splice(idx, 1)
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    width="600"
    custom-class="tree-field_dialog"
    :before-close="handleBeforeClose"
    @mousedown.stop
    @mousedup.stop
    title="下拉树结构设计"
  >
    <div class="cascade-content">
      <div style="display: flex; align-items: center; justify-content: space-between">
        <el-button :disabled="treeList.length === 5" text @click="addCascadeItem">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          添加层级
        </el-button>
      </div>
      <div class="cascade-item">
        <div class="label">层级</div>
        <div class="item-name">下拉树查询字段</div>
      </div>
      <div class="cascade-item" v-for="(ele, idx) in treeList" :key="ele.id">
        <div class="label">层级{{ indexCascade[idx + 1] }}</div>
        <div class="item-name">
          <el-select
            :disabled="idx === 0 && ele.field"
            value-key="id"
            v-model="ele.field"
            style="width: 300px"
          >
            <el-option
              :disabled="disableFieldArr.includes(item.id)"
              v-for="item in datasetMap"
              :key="item.id"
              :label="item.name"
              :value="item"
            />
          </el-select>
        </div>
        <el-button v-show="idx !== 0" @click="deleteCascade(idx)" class="cascade-delete" text>
          <template #icon>
            <Icon name="icon_delete-trash_outlined"></Icon>
          </template>
        </el-button>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button secondary @click="cancelClick">取消</el-button>
        <el-button type="primary" @click="confirmClick"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less">
.tree-field_dialog {
  .cascade-content {
    box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    border: 1px solid #e4e7ed;
    padding: 24px;
    padding-top: 8px;
    margin-top: 8px;

    .cascade-item {
      display: flex;
      align-items: center;
      width: 100%;
      height: 40px;
      .label {
        width: 100px;
      }

      .item-name {
        width: 300px;
      }
    }

    .cascade-delete-block,
    .cascade-delete {
      width: 40px;
      font-size: 20px;
      color: #646a73;
      margin-left: 20px;
    }
  }
}
</style>
