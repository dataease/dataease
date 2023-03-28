<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
const dvMainStore = dvMainStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const copyStore = copyStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()

const { curComponent } = storeToRefs(dvMainStore)
const { menuTop, menuLeft, menuShow } = storeToRefs(contextmenuStore)
const copyData = ref(null)

const lock = () => {
  lockStore.lock()
}

const unlock = () => {
  lockStore.unlock()
}

// 点击菜单时不取消当前组件的选中状态
const handleMouseUp = () => {
  dvMainStore.setClickComponentStatus(true)
}

const cut = () => {
  copyStore.cut()
}

const copy = () => {
  copyStore.copy()
}

const paste = () => {
  copyStore.paste(true)
  snapshotStore.recordSnapshot()
}

const deleteComponent = () => {
  dvMainStore.deleteComponent()
  snapshotStore.recordSnapshot()
}

const upComponent = () => {
  layerStore.upComponent()
  snapshotStore.recordSnapshot()
}

const downComponent = () => {
  layerStore.downComponent()
  snapshotStore.recordSnapshot()
}

const topComponent = () => {
  layerStore.topComponent()
  snapshotStore.recordSnapshot()
}

const bottomComponent = () => {
  layerStore.bottomComponent()
  snapshotStore.recordSnapshot()
}
</script>

<template>
  <div
    v-show="menuShow"
    class="contextmenu"
    :style="{ top: menuTop + 'px', left: menuLeft + 'px' }"
  >
    <ul @mouseup="handleMouseUp">
      <template v-if="curComponent">
        <template v-if="!curComponent['isLock']">
          <li @click="copy">复制</li>
          <li @click="paste">粘贴</li>
          <li @click="cut">剪切</li>
          <li @click="deleteComponent">删除</li>
          <li @click="lock">锁定</li>
          <li @click="topComponent">置顶</li>
          <li @click="bottomComponent">置底</li>
          <li @click="upComponent">上移</li>
          <li @click="downComponent">下移</li>
        </template>
        <li v-else @click="unlock">解锁</li>
      </template>
      <li v-else @click="paste">粘贴</li>
    </ul>
  </div>
</template>

<style lang="less" scoped>
.contextmenu {
  position: absolute;
  z-index: 1000;

  ul {
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    box-sizing: border-box;
    margin: 5px 0;
    padding: 6px 0;

    li {
      font-size: 14px;
      padding: 0 20px;
      position: relative;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #606266;
      height: 34px;
      line-height: 34px;
      box-sizing: border-box;
      cursor: pointer;

      &:hover {
        background-color: #f5f7fa;
      }
    }
  }
}
</style>
