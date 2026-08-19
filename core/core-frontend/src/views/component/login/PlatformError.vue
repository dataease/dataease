<template>
  <div class="de-platform-error">
    <span>
      {{ msg }}
    </span>
  </div>
</template>
<script setup lang="ts">
import { onMounted } from 'vue';

defineProps({
  msg: {
    type: String,
    default: 'Error Message'
  }
})

onMounted(() => {
  const maskList = [...document.getElementsByClassName('ed-loading-spinner')]
  maskList?.forEach(mask => {
    mask.style.display = 'none'
  })
})
</script>
<style lang="less" scoped>
.de-platform-error {
  width: 100vw;
  position: absolute;
  height: 100vh;
  right: 0;
  z-index: 9999;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ee0a24;
}
</style>