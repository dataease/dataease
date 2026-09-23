<script setup lang="ts">
import dbMoreWeb from '@/assets/svg/db-more-web.svg'
import dvTabScreen from '@/assets/svg/dv-tab-screen.svg'
import { toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import DragComponent from '@/custom-component/component-group/DragComponent.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'

const props = defineProps({
  dvModel: {
    type: String,
    default: 'dv'
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { dvModel } = toRefs(props)
const newComponent = (componentName: string, innerType: string) => {
  eventBus.emit('handleNew', { componentName: componentName, innerType: innerType })
}

const handleDragStart = e => {
  commonHandleDragStart(e, dvModel.value)
}

const handleDragEnd = e => {
  commonHandleDragEnd(e, dvModel.value)
}
</script>

<template>
  <div class="group" @dragstart="handleDragStart" @dragend="handleDragEnd">
    <drag-component
      :themes="themes"
      :icon="dbMoreWeb"
      :label="$t('visualization.web')"
      drag-info="DeFrame&DeFrame"
      v-on:click="newComponent('DeFrame', 'DeFrame')"
    ></drag-component>
    <!--    <drag-component-->
    <!--      :themes="themes"-->
    <!--      :icon="dvTabScreen"-->
    <!--      :label="$t('visualization.screen_page')"-->
    <!--      drag-info="DeScreen&DeScreen"-->
    <!--      v-on:click="newComponent('DeScreen', 'DeScreen')"-->
    <!--    ></drag-component>-->
  </div>
</template>

<style lang="less" scoped>
.group {
  width: 100%;
  display: flex;
  padding: 12px 8px;
}
</style>
