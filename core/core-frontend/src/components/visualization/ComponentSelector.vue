<template>
  <div class="bar-main">
    <el-checkbox v-model="checked" @change="checkChange" />
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useViewSelectorStoreWithOut } from '@/store/modules/data-visualization/viewSelector'
const viewSelectorStore = useViewSelectorStoreWithOut()

const checked = ref(false)

const props = defineProps({
  resourceId: {
    type: String,
    required: false
  }
})
onMounted(() => {
  checked.value = viewSelectorStore.getViewIdList.includes(props.resourceId)
})

const checkChange = val => {
  if (val) {
    viewSelectorStore.add(props.resourceId)
  } else {
    viewSelectorStore.remove(props.resourceId)
  }
}
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  right: 10px;
  z-index: 10;
  border-radius: 2px;
  cursor: pointer !important;
  font-size: 16px !important;
}
</style>
