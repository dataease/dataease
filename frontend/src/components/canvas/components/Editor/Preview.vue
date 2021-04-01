<template>
  <div class="bg">
    <div class="canvas-container">
      <div
        class="canvas"
        :style="{
          width: changeStyleWithScale(canvasStyleData.width) + 'px',
          height: changeStyleWithScale(canvasStyleData.height) + 'px',
        }"
      >
        <ComponentWrapper
          v-for="(item, index) in componentData"
          :key="index"
          :config="item"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import { mapState } from 'vuex'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'

export default {
  components: { ComponentWrapper },
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  computed: mapState([
    'componentData',
    'canvasStyleData'
  ]),
  methods: {
    changeStyleWithScale,

    getStyle,

    close() {
      this.$emit('change', false)
    }
  }
}
</script>

<style lang="scss" scoped>
.bg {
    width: 100%;
    height: 100%;
    .canvas-container {
        width: 100%;
        height: 100%;
        overflow: auto;
      .canvas {
            position: relative;
            margin: auto;
        }
    }
}
</style>
