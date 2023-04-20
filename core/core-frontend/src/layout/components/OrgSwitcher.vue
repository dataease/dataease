<template>
  <el-tree-select
    v-model="value"
    :data="state.orgOption"
    check-strictly
    :render-after-expand="false"
    :props="props"
    @change="changeVal"
  />
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { mountedOrg, switchOrg } from '@/api/user'
import { useUserStoreWithOut } from '@/store/modules/user'
const userStore = useUserStoreWithOut()
const value = ref()
const props = {
  value: 'id',
  label: 'name',
  disabled: 'readOnly'
}
const state = reactive({
  orgOption: []
})

const changeVal = val => {
  switchHandler(val)
}

const switchHandler = (id: number | string) => {
  switchOrg(id).then(res => {
    const token = res.data
    userStore.setToken(token)
    window.location.reload()
  })
}

onMounted(() => {
  mountedOrg(null).then(res => {
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
