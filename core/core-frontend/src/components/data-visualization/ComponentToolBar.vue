<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { changeSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { useEmitt } from '@/hooks/web/useEmitt'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, editMode } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const scale = ref(60)

const handleScaleChange = () => {
  snapshotStore.recordSnapshotCache()
  // 画布比例设一个最小值，不能为 0
  scale.value = ~~scale.value || 10
  scale.value = scale.value < 10 ? 10 : scale.value
  scale.value = scale.value > 200 ? 200 : scale.value

  changeSizeWithScale(scale.value)
}

const scaleDecrease = (speed = 1) => {
  if (scale.value > 10) {
    scale.value = scale.value - speed
    handleScaleChange()
  }
}

const scaleIncrease = (speed = 1) => {
  if (scale.value < 200) {
    scale.value = scale.value + speed
    handleScaleChange()
  }
}

const reposition = () => {
  useEmitt().emitter.emit('initScroll')
}

// 记录瞬时wheel值 防止放大操作和滚动操作冲突
let lastWheelNum = 0

// 检查当前页面是否有弹框
const checkDialog = () => {
  let haveDialog = false
  document.querySelectorAll('.ed-overlay').forEach(element => {
    if (window.getComputedStyle(element).getPropertyValue('display') != 'none') {
      haveDialog = true
    }
  })
  document.querySelectorAll('.ed-popover').forEach(element => {
    if (window.getComputedStyle(element).getPropertyValue('display') != 'none') {
      haveDialog = true
    }
  })
  // 富文本单框
  if (document.querySelector('.tox-dialog-wrap')) {
    haveDialog = true
  }

  return haveDialog
}

const handleMouseWheel = e => {
  if (editMode.value === 'preview' || checkDialog()) {
    return
  }
  let dvMain = document.getElementById('dv-main-center')
  let dvMainLeftSlide = document.getElementById('dv-main-left-sidebar')
  let areaLeftWidth = dvMainLeftSlide.clientWidth
  let areaRight = dvMain.clientWidth + areaLeftWidth
  if (areaLeftWidth < e.clientX && e.clientX < areaRight) {
    const delta = e.wheelDelta ? e.wheelDelta : -e.detail
    if ((lastWheelNum === 240 && delta === 240) || delta > 240) {
      //放大
      scaleIncrease(3)
    } else if ((lastWheelNum === -240 && delta === -240) || delta < -240) {
      // 缩小
      scaleDecrease(3)
    }

    if (delta >= 240 || delta <= -240) {
      e.stopPropagation()
      e.preventDefault()
    }
    lastWheelNum = delta
  }
}

onMounted(() => {
  window.addEventListener('mousewheel', handleMouseWheel, { passive: false })
  setTimeout(() => {
    scale.value = canvasStyleData.value.scale
    nextTick(() => {
      useEmitt().emitter.emit('initScroll')
    })
  }, 1000)
})

onUnmounted(() => {
  window.removeEventListener('mousewheel', handleMouseWheel)
})
</script>
<template>
  <el-row class="custom-main">
    <div class="scale-area">
      <el-input
        @keydown.stop
        @keyup.stop
        type="number"
        size="small"
        effect="dark"
        v-model="scale"
        :min="10"
        :max="200"
        :controls="false"
        @change="handleScaleChange()"
        class="scale-input-number"
      >
        <template #suffix> % </template>
      </el-input>
      <el-icon @click="scaleDecrease(1)" class="hover-icon-custom" style="margin-right: 12px">
        <Icon name="dv-min"></Icon
      ></el-icon>
      <el-slider
        style="width: 100px; margin-right: 12px"
        v-model="scale"
        :min="10"
        :max="200"
        tooltip-theme="ndark"
        @change="handleScaleChange()"
        size="small"
      />
      <el-icon @click="scaleIncrease(1)" class="hover-icon-custom">
        <Icon name="dv-max"></Icon
      ></el-icon>
      <el-divider direction="vertical" class="custom-divider" />
      <el-icon @click="reposition" class="hover-icon-custom" style="margin-right: 12px">
        <Icon name="dv-reposition"></Icon
      ></el-icon>
    </div>
  </el-row>
</template>

<style lang="less">
.custom-main {
  display: flex;
  width: 100%;
  justify-content: right;
  height: @component-toolbar-height;
  background-color: @side-area-background;
  border-top: 1px solid @side-outline-border-color;
  color: #fff;
  transition: 0.5s;
  .scale-area {
    display: flex;
    align-items: center;
  }
}

.scale-input-number {
  height: 28px;
  width: 60px !important;
  margin-right: 16px;
  input {
    -webkit-appearance: none;
    -moz-appearance: textfield;

    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
    }
  }
}

.custom-divider {
  border-left: 1px solid rgba(255, 255, 255, 0.15);
  margin: 0 16px;
}

.hover-icon-custom {
  cursor: pointer;
  height: 24px !important;
  width: 24px !important;
  font-size: 16px !important;
  border-radius: 4px;
  color: #646a73 !important;

  &[aria-expanded='true'] {
    background: rgba(31, 35, 41, 1);
  }

  &:hover {
    background: rgba(31, 35, 41, 0.5);
  }

  &:active {
    background: rgba(31, 35, 41, 1);
  }
}
</style>
