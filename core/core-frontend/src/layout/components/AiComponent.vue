<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
const aiDialogShow = ref(false)

const closeAi = () => {
  aiDialogShow.value = false
}
defineProps({
  baseUrl: {
    type: String
  }
})

const sizeChange = () => {
  sizeState.value = sizeState.value === 'min' ? 'max' : 'min'
}

const sizeState = ref('min')

onMounted(() => {
  useEmitt({
    name: 'aiComponentChange',
    callback: function () {
      aiDialogShow.value = !aiDialogShow.value
    }
  })
})
</script>
<template>
  <div
    class="ai-main"
    :class="{
      'ai-main-active': aiDialogShow,
      'ai-main-active-max': aiDialogShow && sizeState === 'max',
      'ai-main-active-min': aiDialogShow && sizeState === 'min'
    }"
  >
    <div class="ai-content">
      <el-icon class="close" @click="closeAi"><Close /></el-icon>
      <el-icon class="size-class" @click="sizeChange"
        ><Icon :name="'dv-ai-window-' + sizeState"></Icon
      ></el-icon>
      <iframe :src="baseUrl" style="width: 100%; height: 100%" frameborder="0" allow="microphone">
      </iframe>
    </div>
  </div>
</template>

<style lang="less" scoped>
.ai-main {
  position: fixed;
  border-radius: 5px;
  border-top-right-radius: 0;
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
      top: 16px;
      font-size: 24px;
      color: rgb(100, 106, 115);
      cursor: pointer;
    }
    .size-class {
      position: absolute;
      right: 50px;
      font-size: 16px;
      top: 20px;
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
  min-height: 540px;
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
