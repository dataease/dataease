<template>
  <div style="width: 100%;height: 100vh;">
    <fullscreen
      style="height:100%;background: #f7f8fa;overflow-y: auto"
      :fullscreen.sync="fullscreen"
      @change="fullscreenChange"
    >
      <Preview
        v-if="fullscreen"
        :component-data="mainCanvasComponentData"
        :canvas-style-data="canvasStyleData"
        :panel-info="panelInfo"
        :in-screen="!fullscreen"
      />
    </fullscreen>
  </div>
</template>

<script>
import Preview from './Preview'
import bus from '@/utils/bus'
import { mapState } from 'vuex'
import { getNowCanvasComponentData } from '@/components/canvas/utils/utils'

export default {
  components: { Preview },
  data() {
    return {
      canvasId: 'canvas-main',
      fullscreen: false
    }
  },
  computed: {
    mainCanvasComponentData() {
      return getNowCanvasComponentData(this.canvasId)
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'canvasStyleData'
    ])
  },
  mounted() {
    this.fullscreen = false
    this.$nextTick(() => (this.fullscreen = true))
  },

  methods: {
    fullscreenChange(fullscreen) {
      if (!fullscreen) {
        bus.$emit('previewFullScreenClose')
      }
    }
  }
}
</script>

