<script setup lang="ts">
import configGlobal from '@/components/config-global/src/ConfigGlobal.vue'
let startX = 0 // 滑动开始时的X坐标
let endX = 0 // 滑动结束时的X坐标

document.addEventListener('touchstart', function (event) {
  const touch = event.touches[0] // 获取第一个触摸点
  startX = touch.pageX // 记录滑动开始时的X坐标
})

document.addEventListener('touchend', function (event) {
  const touch = event.changedTouches[0] // 获取变化的第一个触摸点
  endX = touch.pageX // 记录滑动结束时的X坐标

  // 计算滑动距离
  const diffX = endX - startX

  // 判断左滑还是右滑
  if (diffX > 150) {
    // 设定一个阈值来判断滑动是否有效
    window.history.back()
    // 在这里处理右滑事件
  } else if (diffX < -150) {
    // 在这里处理左滑事件
    window.history.forward()
  }
})
</script>
<template>
  <config-global>
    <router-view />
  </config-global>
</template>
