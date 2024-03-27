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
  <div class="ai-main" :class="{ 'ai-main-active': aiDialogShow }">
    <div class="ai-content">
      <el-icon class="close" @click="closeAi"><Close /></el-icon>
      <iframe :src="baseUrl" style="width: 100%; height: 100%" frameborder="0" allow="microphone">
      </iframe>
    </div>
  </div>
</template>

<style lang="less" scoped>
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
