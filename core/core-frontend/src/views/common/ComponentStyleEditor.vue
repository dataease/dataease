<script setup lang="ts">
import { findComponentAttr } from '@/utils/components'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import ViewEditor from '@/views/chart/components/editor/index.vue'
import { computed } from 'vue'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, batchOptStatus } = storeToRefs(dvMainStore)

defineProps({
  canvasViewInfoMobile: {
    type: Object,
    required: true
  }
})

const otherEditorShow = computed(() => {
  return Boolean(
    curComponent.value &&
      (!['UserView', 'VQuery'].includes(curComponent.value?.component) ||
        (curComponent.value?.component === 'UserView' &&
          curComponent.value?.innerType === 'picture-group')) &&
      !batchOptStatus.value
  )
})

const viewEditorShow = computed(() => {
  return Boolean(
    curComponent.value &&
      ['UserView', 'VQuery'].includes(curComponent.value.component) &&
      curComponent.value.innerType !== 'picture-group' &&
      !batchOptStatus.value
  )
})
</script>

<template>
  <div class="mobile_content">
    <template v-if="otherEditorShow">
      <component :is="findComponentAttr(curComponent)" :themes="'light'" />
    </template>
    <template v-if="viewEditorShow">
      <view-editor
        :themes="'light'"
        :view="canvasViewInfoMobile[curComponent ? curComponent.id : 'default']"
      ></view-editor>
    </template>
  </div>
</template>

<style scoped lang="less">
.mobile_content {
  width: 100%;
  height: 100%;
}
</style>
