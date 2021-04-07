<template>
  <div v-if="show" class="bg">
    <el-button class="close" @click="close">关闭</el-button>
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
import { uuid } from 'vue-uuid'

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
  created() {
    this.restore()
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
    },
    restore() {
      // 用保存的数据恢复画布
      if (localStorage.getItem('canvasData')) {
        this.componentData = this.resetID(JSON.parse(localStorage.getItem('canvasData')))
      }

      if (localStorage.getItem('canvasStyle')) {
        this.canvasStyleData = JSON.parse(localStorage.getItem('canvasStyle'))
        this.$store.commit('setCanvasStyle', JSON.parse(localStorage.getItem('canvasStyle')))
      }
    },
    resetID(data) {
      data.forEach(item => {
        item.id = uuid.v1()
      })

      return data
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
