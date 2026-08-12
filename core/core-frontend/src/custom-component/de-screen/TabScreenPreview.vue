<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'

const props = defineProps({
  screenId: {
    type: String,
    required: true
  },
  dvType: {
    type: String,
    default: 'dashboard'
  }
})

// 每个 tab 用独立 iframe 承载大屏/仪表板，从而与其它 tab 完全隔离（独立 JS 上下文与 dvMain store）
const frameSrc = computed(
  () =>
    `?${new Date().getTime()}#/preview?dvId=${props.screenId}&dvType=${
      props.dvType
    }&ignoreParams=true`
)

// screenId 变化时重建 iframe，避免复用旧上下文
const frameShow = ref(true)
watch(
  () => props.screenId,
  () => {
    frameShow.value = false
    nextTick(() => {
      frameShow.value = true
    })
  }
)
</script>

<template>
  <iframe
    v-if="frameShow"
    :id="'tab-screen-frame-' + props.screenId"
    :src="frameSrc"
    scrolling="auto"
    frameborder="0"
    class="tab-screen-frame"
  />
</template>

<style lang="less" scoped>
.tab-screen-frame {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
