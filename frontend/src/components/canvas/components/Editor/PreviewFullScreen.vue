<template>
  <div style="width: 100%;height: 100vh;">
    <fullscreen style="height:100%;background: #f7f8fa;overflow-y: auto" :fullscreen.sync="fullscreen" @change="fullscreenChange">
      <Preview
        v-if="fullscreen"
        :component-data="componentData"
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

export default {
  components: { Preview },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'canvasStyleData',
      'componentData'
    ])
  },
  data() {
    return {
      fullscreen: false
    }
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

