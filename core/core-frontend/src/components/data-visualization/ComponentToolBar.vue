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
  if (
    editMode.value === 'preview' ||
    checkDialog() ||
    (Math.abs(e.deltaX) !== 0 && Math.abs(e.deltaY) !== 0)
  ) {
    return
  }
  if (e.ctrlKey) {
    if (e.deltaY > 0) {
      //向内 缩小
      scaleDecrease(3)
      e.stopPropagation()
      e.preventDefault()
    }
    if (e.deltaY < 0) {
      //向外 放大
      scaleIncrease(3)
      e.stopPropagation()
      e.preventDefault()
    }
  }
}

onMounted(() => {
  window.addEventListener('wheel', handleMouseWheel, { passive: false })
  setTimeout(() => {
    scale.value = canvasStyleData.value.scale
    nextTick(() => {
      useEmitt().emitter.emit('initScroll')
    })
  }, 1000)
  useEmitt({
    name: 'canvasScrollRestore',
    callback: function () {
      // 用于全屏后还原编辑状态大小
      changeSizeWithScale(scale.value)
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('wheel', handleMouseWheel)
})
</script>
<template>
  <el-row class="custom-main">
    <div class="scale-area">
      <el-input-number
        @keydown.stop
        @keyup.stop
        v-model="scale"
        effect="dark"
        :min="10"
        :max="200"
        size="small"
        controls-position="right"
        @change="handleScaleChange()"
        class="scale-input-number"
      />

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
      <el-divider direction="vertical" class="custom-divider_scale" />
      <el-tooltip effect="ndark" content="定位到中心点" placement="top">
        <el-icon @click="reposition" class="hover-icon-custom" style="margin-right: 12px">
          <Icon name="dv-reposition"></Icon
        ></el-icon>
      </el-tooltip>
    </div>
  </el-row>
</template>

<style scoped lang="less">
.custom-main {
  display: flex;
  width: 100%;
  justify-content: right;
  height: @component-toolbar-height;
  background-color: @side-area-background;
  border-top: 1px solid @side-outline-border-color;
  color: #fff;
  z-index: 2;
  transition: 0.5s;
  .scale-area {
    display: flex;
    align-items: center;
  }
}
:deep(.ed-input--dark .ed-input__wrapper),
:deep(.ed-input-number--dark:not(.is-disabled) .ed-input-number__decrease:not(.is-disabled)),
:deep(.ed-input-number--dark:not(.is-disabled) .ed-input-number__increase:not(.is-disabled)) {
  background-color: #1a1a1a;
}

.custom-divider_scale {
  height: 18px;
  border-color: #ffffff26;
}

.scale-input-number {
  height: 24px;
  line-height: 24px;
  width: 80px;
  margin-right: 16px;

  :deep(.ed-input__wrapper) {
    position: relative;
    padding: 0 38px 0 8px;
    &::after {
      position: absolute;
      content: '%';
      right: 35px;
      top: 1px;
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
