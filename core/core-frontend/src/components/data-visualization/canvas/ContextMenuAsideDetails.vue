<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { computed, ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import ContextMenuDetails from '@/components/data-visualization/canvas/ContextMenuDetails.vue'
const dvMainStore = dvMainStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()
const copyStore = copyStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()
const composeStore = composeStoreWithOut()

const { areaData } = storeToRefs(composeStore)
const { curComponent } = storeToRefs(dvMainStore)
const copyData = ref(null)
const emit = defineEmits(['close'])
const props = defineProps({
  element: {
    type: Object
  },
  index: {
    type: Number
  }
})
const { element, index } = toRefs(props)

const lock = () => {
  lockStore.lock()
}

const unlock = () => {
  lockStore.unlock()
}

// 点击菜单时不取消当前组件的选中状态
const handleMouseUp = e => {
  dvMainStore.setClickComponentStatus(true)
}

const cut = () => {
  copyStore.cut()
}

const copy = () => {
  copyStore.copy()
}

const hide = () => {
  layerStore.hideComponent()
}

const paste = () => {
  copyStore.paste(true)
  snapshotStore.recordSnapshot('paste')
}

const deleteComponent = () => {
  if (curComponent.value) {
    dvMainStore.deleteComponent()
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      dvMainStore.deleteComponentById(component.id)
    })
  }
  eventBus.emit('hideArea-canvas-main')
  snapshotStore.recordSnapshot('deleteComponent')
}

const upComponent = () => {
  layerStore.upComponent()
  snapshotStore.recordSnapshot('upComponent')
}

const downComponent = () => {
  layerStore.downComponent()
  snapshotStore.recordSnapshot('downComponent')
}

const topComponent = () => {
  layerStore.topComponent()
  snapshotStore.recordSnapshot('topComponent')
}

const bottomComponent = () => {
  layerStore.bottomComponent()
  snapshotStore.recordSnapshot('bottomComponent')
}

const componentCompose = () => {
  composeStore.compose()
  snapshotStore.recordSnapshot('componentCompose')
}

const decompose = () => {
  composeStore.decompose()
  snapshotStore.recordSnapshot('decompose')
}

// 阻止事件向父级组件传播调用父级的handleMouseDown 导致areaData 被隐藏
const handleComposeMouseDown = e => {
  e.preventDefault()
  e.stopPropagation()
}

const composeDivider = computed(() => {
  return (
    areaData.value.components.length ||
    !(!curComponent || curComponent['isLock'] || curComponent['component'] != 'Group')
  )
})

const onClick = () => {
  dvMainStore.setCurComponent({ component: element.value, index })
}

const close = param => {
  emit('close', param)
}
</script>

<template>
  <context-menu-details active-position="aside" @close="close"></context-menu-details>
</template>

<style lang="less" scoped>
.context-menu-details {
  z-index: 1000;
  border: #bbb 1px solid;
  ul {
    padding: 4px 0;
    background-color: #000;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    box-sizing: border-box;
    ::v-deep(.ed-divider) {
      margin: 8px 0;
    }

    li {
      font-size: 14px;
      padding: 0 60px;
      position: relative;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #fff;
      height: 34px;
      line-height: 34px;
      box-sizing: border-box;
      cursor: pointer;

      i {
        position: absolute;
        left: 30px;
        top: 50%;
        transform: translate(-50%, -50%);
      }

      &:hover {
        background-color: #333;
      }
    }
  }
}
</style>
