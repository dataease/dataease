<script setup lang="ts">
import iconVideo from '@/assets/svg/icon-video.svg'
import dvPictureShow from '@/assets/svg/dv-picture-show.svg'
import iconStream from '@/assets/svg/icon-stream.svg'
import pictureGroupOrigin from '@/assets/svg/picture-group-origin.svg'
import { toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import DragComponent from '@/custom-component/component-group/DragComponent.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  dvModel: {
    type: String,
    default: 'dv'
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { dvModel } = toRefs(props)
const newComponent = (componentName, innerType) => {
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
      class="media-component"
      :themes="themes"
      :icon="dvPictureShow"
      label="图片"
      drag-info="Picture&Picture"
      v-on:click="newComponent('Picture', 'Picture')"
    ></drag-component>
    <drag-component
      class="media-component"
      :themes="themes"
      :icon="iconVideo"
      label="视频"
      drag-info="DeVideo&DeVideo"
      v-on:click="newComponent('DeVideo', 'DeVideo')"
    ></drag-component>
    <drag-component
      class="media-component"
      :themes="themes"
      :icon="iconStream"
      label="流媒体"
      drag-info="DeStreamMedia&DeStreamMedia"
      v-on:click="newComponent('DeStreamMedia', 'DeStreamMedia')"
    ></drag-component>
    <drag-component
      class="media-component"
      :themes="themes"
      :icon="pictureGroupOrigin"
      label="图片组"
      drag-info="UserView&picture-group"
      v-on:click="newComponent('UserView', 'picture-group')"
    ></drag-component>
  </div>
</template>

<style lang="less" scoped>
.group {
  padding: 12px 8px;
}
.media-component {
  float: left;
  margin: 0 6px !important;
}
</style>
