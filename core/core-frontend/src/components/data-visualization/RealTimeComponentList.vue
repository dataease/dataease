
<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()

const { componentData, curComponent, curComponentIndex } = storeToRefs(dvMainStore)
const getComponent = index => {
  return componentData.value[componentData.value.length - 1 - index]
}
const transformIndex = index => {
  return componentData.value.length - 1 - index
}
const onClick = index => {
  setCurComponent(index)
}
const deleteComponent = (number: number) => {
  setTimeout(() => {
    dvMainStore.deleteComponent()
    snapshotStore.recordSnapshot()
  })
}
const upComponent = (number: number) => {
  setTimeout(() => {
    layerStore.upComponent()
    snapshotStore.recordSnapshot()
  })
}
const downComponent = (number: number) => {
  setTimeout(() => {
    layerStore.downComponent()
    snapshotStore.recordSnapshot()
  })
}
const setCurComponent = index => {
  dvMainStore.setCurComponent({ component: componentData.value[index], index })
}
</script>

<template>
  <div class="real-time-component-list">
    <div
      v-for="(item, index) in componentData"
      :key="index"
      class="list"
      :class="{ actived: transformIndex(index) === curComponentIndex }"
      @click="onClick(transformIndex(index))"
    >
      <span class="iconfont" :class="'icon-' + getComponent(index).icon"></span>
      <span>{{ getComponent(index).label }}</span>
      <div class="icon-container">
        <span class="iconfont icon-shangyi" @click="upComponent(transformIndex(index))"></span>
        <span class="iconfont icon-xiayi" @click="downComponent(transformIndex(index))"></span>
        <span class="iconfont icon-shanchu" @click="deleteComponent(transformIndex(index))"></span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.real-time-component-list {
  height: 35%;

  .list {
    height: 30px;
    cursor: grab;
    text-align: center;
    color: #333;
    display: flex;
    align-items: center;
    font-size: 12px;
    padding: 0 10px;
    position: relative;
    user-select: none;

    &:active {
      cursor: grabbing;
    }

    &:hover {
      background-color: rgba(200, 200, 200, 0.2);

      .icon-container {
        display: block;
      }
    }

    .iconfont {
      margin-right: 4px;
      font-size: 16px;
    }

    .icon-wenben,
    .icon-tupian {
      font-size: 14px;
    }

    .icon-container {
      position: absolute;
      right: 10px;
      display: none;

      .iconfont {
        cursor: pointer;
      }
    }
  }

  .actived {
    background: #ecf5ff;
    color: #409eff;
  }
}
</style>
