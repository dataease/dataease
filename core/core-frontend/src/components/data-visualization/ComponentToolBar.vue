<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref, onMounted } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { changeSizeWithScale } from '@/utils/changeComponentsSizeWithScale'
import { useEmitt } from '@/hooks/web/useEmitt'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const scale = ref(60)

let timer = null

const handleScaleChange = () => {
  snapshotStore.recordSnapshotCache()
  // 画布比例设一个最小值，不能为 0
  scale.value = ~~scale.value || 10
  changeSizeWithScale(scale.value)
}

const scaleDecrease = () => {
  scale.value = --scale.value
  handleScaleChange()
}

const scaleIncrease = () => {
  scale.value = ++scale.value
  handleScaleChange()
}

const reposition = () => {
  useEmitt().emitter.emit('initScroll')
}

onMounted(() => {
  setTimeout(() => {
    scale.value = canvasStyleData.value.scale
  }, 1000)
})
</script>
<template>
  <el-row class="custom-main">
    <div class="scale-area">
      <el-input
        type="number"
        size="small"
        effect="dark"
        v-model="scale"
        :min="10"
        :max="100"
        :controls="false"
        class="scale-input-number"
      >
        <template #suffix> % </template>
      </el-input>
      <el-icon @click="scaleDecrease" class="hover-icon-custom" style="margin-right: 12px">
        <Icon name="dv-min"></Icon
      ></el-icon>
      <el-slider
        style="width: 100px; margin-right: 12px"
        v-model="scale"
        @change="handleScaleChange()"
        size="small"
      />
      <el-icon @click="scaleIncrease" class="hover-icon-custom">
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
