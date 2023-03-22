<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElDrawer, ElButton } from 'element-plus-secondary'

import DrawerFilter from '@/components/drawer-filter/src/DrawerFilter.vue'

const state = reactive({
  activeStatus: [],
  statusList: []
})
const userDrawer = ref(false)

const init = () => {
  userDrawer.value = true
  clearFilter(1)
}
// const reset = () => {
//   init()
// }
// const formatCondition = () => {}
// const formatText = () => {}
// const search = () => {}
// const initRoles = () => {}
// const normalizer = () => {}
// const loadNode = () => {}
// const search = () => {}
const clearFilter = (id: string | number) => {
  state.activeStatus = state.activeStatus.filter(ele => ele.id !== id)
}
const statusChange = (id: string | number) => {
  state.activeStatus = state.activeStatus.filter(ele => ele.id !== id)
}

state.statusList = Array(20)
  .fill(1)
  .map((_, index) => ({
    id: 'test' + index + 20,
    name: index + 'nickName'
  }))

defineExpose({
  init,
  clearFilter
})
</script>

<template>
  <el-drawer title="筛选条件" v-model="userDrawer" size="680px" direction="rtl">
    <drawer-filter :status-list="state.statusList" title="状态" @status-change="statusChange">
    </drawer-filter>

    <template #footer>
      <el-button>cancel</el-button>
      <el-button type="primary">confirm</el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped></style>
