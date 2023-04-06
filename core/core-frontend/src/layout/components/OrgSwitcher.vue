<template>
  <el-tree-select
    v-model="value"
    :data="state.orgOption"
    check-strictly
    :render-after-expand="false"
    :props="props"
    @change="changeVal"
    show-checkbox
  />
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { mountedOrg, switchOrg } from '@/api/user'
import { useUserStoreWithOut } from '@/store/modules/user'
const userStore = useUserStoreWithOut()
const value = ref()
const props = {
  label: 'name'
}
const state = reactive({
  orgOption: []
})

const changeVal = val => {
  switchHandler(val)
}
/* const getById = (id: number) => {
  let item = null
  state.orgOption.forEach(option => {
    if (option.id === id) {
      item = option
    }
  })
  return item
} */

const switchHandler = (id: number) => {
  switchOrg(id)
}

onMounted(() => {
  mountedOrg().then(res => {
    state.orgOption = res.data
  })
  value.value = userStore.getOid
})
</script>

<style lang="less">
.example-showcase .el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>
