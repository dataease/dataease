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
      aiDialogShow: false,
      sizeState: 'min'
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
    },
    sizeChange() {
      this.sizeState = this.sizeState === 'min' ? 'max' : 'min'
    }
  }
}
</script>
<template>
  <div
    class="ai-main"
    :class="{'ai-main-active': aiDialogShow,
             'ai-main-active-max' : aiDialogShow && sizeState=== 'max',
             'ai-main-active-min' : aiDialogShow && sizeState=== 'min'}"
  >
    <div class="ai-content">
      <div class="close"><i
        class="el-icon-close"
        @click="closeAi"
      /></div>
      <div class="size-class"> <svg-icon
        :icon-class="'dv-ai-window-'+sizeState"
        @click="sizeChange"
      /></div>
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
  height: 0;
  bottom: 48px;
  right: 36px;
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
      color: rgb(100, 106, 115);
      cursor: pointer;
    }
    .size-class{
      position: absolute;
      right: 48px;
      font-size: 16px;
      top: 17px;
      color: rgb(100, 106, 115);
      cursor: pointer;
    }
  }
}
.ai-main-active {
  border: 1px solid rgba(239, 240, 241, 1);
  box-shadow: 0px 6px 24px 0px #1f232914;
}
.ai-main-active-min {
  min-width: 350px;
  max-width: 420px;
  height: 50%;
  width: 25%;
  min-height: 450px;
  max-height: 600px;
  bottom: 48px;
  right: 36px;
}

.ai-main-active-max {
  height: 100%;
  width: 50%;
  bottom: 0px;
  right: 0px;
}
</style>
