<template>
  <div style="width: 100%;height: 100vh;">
    <fullscreen style="height:100%;background: #f7f8fa;overflow-y: auto" :fullscreen.sync="fullscreen" @change="fullscreenChange">
      <Preview v-if="fullscreen" :in-screen="!fullscreen" />
    </fullscreen>
  </div>
</template>

<script>

import Preview from './Preview'
import bus from '@/utils/bus'

export default {
  components: { Preview },
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

