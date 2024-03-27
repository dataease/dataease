<script>
import bus from '@/utils/bus'
export default {
  name: 'AiComponent',
  props: {
    baseUrl: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      aiDialogShow: false
    }
  },
  mounted() {
    bus.$on('aiComponentChange', this.showChange)
  },
  methods: {
    showChange() {
      this.aiDialogShow = !this.aiDialogShow
    },
    closeAi() {
      this.aiDialogShow = false
    }
  }
}
</script>
<template>
  <div
    class="ai-main"
    :class="{ 'ai-main-active': aiDialogShow }"
  >
    <div class="ai-content">
      <div class="close"><i @click="closeAi" class="el-icon-close" /></div>
      <iframe
        :src="baseUrl"
        style="width: 100%; height: 100%"
        frameborder="0"
        allow="microphone"
      />
    </div>
  </div>
</template>

<style lang="scss">
.ai-main {
  position: fixed;
  border-radius: 5px;
  overflow: hidden;
  bottom: 48px;
  right: 36px;
  height: 0;
  width: 25%;
  min-width: 350px;
  max-width: 420px;
  transition: 0.2s;
  z-index: 10;
  .ai-content {
    position: relative;
    width: 100%;
    height: 100%;
    .close {
      position: absolute;
      right: 12px;
      top: 12px;
      font-size: 24px;
      color: #1a1a1a;
      cursor: pointer;
    }
  }
}
.ai-main-active {
  border: 1px solid #d9d9d9;
  height: 50%;
  min-height: 450px;
  max-height: 600px;
}
</style>
