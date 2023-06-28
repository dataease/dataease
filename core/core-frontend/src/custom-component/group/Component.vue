<script setup lang="ts">
import { toRefs } from 'vue'
import findComponent from '@/utils/components'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import ComponentWrapper from '@/components/data-visualization/canvas/ComponentWrapper.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasViewInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { propValue, element } = toRefs(props)
</script>

<template>
  <div class="group">
    <div>
      <component-wrapper
        v-for="(item, index) in propValue"
        :id="'group-component' + item.id"
        :view-info="canvasViewInfo[item.id]"
        :key="index"
        :config="item"
        :style="item.groupStyle"
      />
      <!--      -->
      <!--      <component-->
      <!--        :is="findComponent(item.component)"-->
      <!--        v-for="item in propValue"-->
      <!--        :id="'component' + item.id"-->
      <!--        :key="item.id"-->
      <!--        class="component"-->
      <!--        :style="item.groupStyle"-->
      <!--        :prop-value="item.propValue"-->
      <!--        :element="item"-->
      <!--        :request="item.request"-->
      <!--        :view="canvasViewInfo[item.id]"-->
      <!--      />-->
    </div>
  </div>
</template>

<style lang="less" scoped>
.group {
  & > div {
    position: relative;
    width: 100%;
    height: 100%;

    .component {
      position: absolute;
    }
  }
}
</style>
