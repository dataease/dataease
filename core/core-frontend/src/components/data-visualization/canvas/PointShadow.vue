<template>
  <div class="point-shadow" :style="styleInfo">
    <div class="point-shadow-content">
      <div id="point-shadow-component" class="point-shadow-component" />
      <div class="point-shadow-tips" :style="tipsStyleInfo">
        <div style="width: 100%; text-align: center">组件将被移出Tab</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'

const dvMainStore = dvMainStoreWithOut()
const { mousePointShadowMap } = storeToRefs(dvMainStore)
const props = defineProps({
  canvasId: {
    type: String,
    required: true
  }
})

const tipsStyleInfo = computed(() => {
  return {
    width: mousePointShadowMap.value.width + 'px',
    height: mousePointShadowMap.value.height + 'px'
  }
})

const styleInfo = computed(() => {
  return {
    left: mousePointShadowMap.value.mouseX - mousePointShadowMap.value.width / 2 + 'px',
    top: mousePointShadowMap.value.mouseY - mousePointShadowMap.value.height / 2 - 60 + 'px',
    width: mousePointShadowMap.value.width + 'px',
    height: mousePointShadowMap.value.height + 'px'
  }
})
</script>
<style lang="less" scoped>
.point-shadow {
  z-index: 1000;
  position: absolute;
  cursor: move;
}

.point-shadow-content {
  position: relative;
}

.point-shadow-component {
  opacity: 0.6;
  background-color: rgba(179, 212, 252);
}

.point-shadow-tips {
  left: 0px;
  top: 0px;
  box-sizing: border-box;
  z-index: 10001;
  display: flex;
  align-items: center;
  position: absolute;
  color: #33ef08;
  font-weight: bold;
  background-color: rgba(179, 212, 252);
}
</style>
