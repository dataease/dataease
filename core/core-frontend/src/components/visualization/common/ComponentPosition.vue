<template>
  <div class="position-main">
    <div
      v-for="({ key, label }, index) in positionKeys"
      :key="index"
      :title="label"
      style="display: flex; float: left; margin-top: 10px"
    >
      <div style="max-width: 25px; min-width: 19px; overflow: hidden; text-align: right">
        <span>{{ label }}</span>
      </div>
      <div style="width: 85px">
        <de-input-num
          :disabled="curComponent['isLock']"
          :themes="themes"
          v-model="curComponent.style[key]"
        ></de-input-num>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
import { positionData } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, dvInfo } = storeToRefs(dvMainStore)
const props = defineProps({
  themes: {
    type: String,
    default: 'dark'
  }
})

const { themes } = toRefs(props)
const positionKeys = computed(() => {
  if (curComponent.value) {
    const curComponentStyleKeys = Object.keys(curComponent.value.style)
    return positionData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return null
  }
})
</script>

<style lang="less" scoped>
.position-main {
  width: 100%;
  height: 70px;
  min-width: 220px;
  background-color: #292929 !important;
}
</style>
