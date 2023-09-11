<template>
  <div class="position-main">
    <div
      v-for="({ key, label, min, max, step }, index) in positionKeys"
      :key="index"
      :title="label"
      style="display: flex; float: left; margin-bottom: 8px"
    >
      <div style="max-width: 25px; min-width: 19px; overflow: hidden; text-align: right">
        <span>{{ label }}</span>
      </div>
      <div style="width: 85px">
        <de-input-num
          :disabled="curComponent['isLock']"
          :min="min"
          :max="max"
          :step="step"
          :effect="themes"
          v-model="curComponent.style[key]"
        ></de-input-num>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs, PropType } from 'vue'
import { positionData } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import DeInputNum from '@/custom-component/common/DeInputNum.vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, dvInfo } = storeToRefs(dvMainStore)
const props = defineProps({
  themes: {
    type: String as PropType<'light' | 'dark'>,
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
  min-width: 220px;
  height: 64px;
  background-color: #292929 !important;
}
</style>
