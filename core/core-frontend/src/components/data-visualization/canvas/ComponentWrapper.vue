<template>
  <div @click="onClick" @mouseenter="onMouseEnter">
    <component
      :is="config?.component"
      v-if="config?.component.startsWith('SVG')"
      ref="component"
      class="component"
      :style="getSVGStyle(config?.style)"
      :prop-value="config?.propValue"
      :element="config"
      :request="config?.request"
      :linkage="config?.linkage"
    />

    <component
      :is="config?.component"
      v-else
      ref="component"
      class="component"
      :style="getStyle(config?.style)"
      :prop-value="config?.propValue"
      :element="config"
      :request="config?.request"
      :linkage="config?.linkage"
    />
  </div>
</template>

<script setup lang="ts">
import { getStyle, getSVGStyle } from '@/utils/style'
import runAnimation from '@/utils/runAnimation'
import eventBus from '@/utils/eventBus'
import { ref, onMounted, toRefs } from 'vue'

const props = defineProps({
  config: {
    type: Object,
    required: true,
    default() {
      return {
        component: null,
        propValue: null,
        request: null,
        linkage: null,
        type: null,
        events: null,
        style: null,
        id: null
      }
    }
  }
})
const component = ref(null)
const { config } = toRefs(props)

onMounted(() => {
  runAnimation(component.value.$el, config.value.type)
})

const onClick = () => {
  const events = config.value.events
  Object.keys(events).forEach(event => {
    this[event](events[event])
  })
  eventBus.emit('v-click', config.value.id)
}

const onMouseEnter = () => {
  eventBus.emit('v-hover', config.value.id)
}
</script>

<style lang="scss" scoped>
.component {
  position: absolute;
}
</style>
