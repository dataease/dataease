<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { storeToRefs } from 'pinia'
import { computed, toRefs } from 'vue'
import { ElDivider } from 'element-plus-secondary'
import eventBus from '@/utils/eventBus'
import { componentArraySort, getCurInfo } from '@/store/modules/data-visualization/common'
import { useEmitt } from '@/hooks/web/useEmitt'
import { XpackComponent } from '@/components/plugin'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const copyStore = copyStoreWithOut()
const lockStore = lockStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()
const composeStore = composeStoreWithOut()

const { areaData } = storeToRefs(composeStore)
const { curComponent, componentData } = storeToRefs(dvMainStore)
const emit = defineEmits(['close', 'rename'])
const { emitter } = useEmitt()
const props = defineProps({
  activePosition: {
    type: String,
    default: 'canvas'
  }
})

const { activePosition } = toRefs(props)
const { t } = useI18n()
const popComponentDataLength = computed(
  () => componentData.value.filter(ele => ele.category === 'hidden').length
)

const lock = () => {
  if (curComponent.value && !isGroupArea.value) {
    lockStore.lock()
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      lockStore.lock(component)
    })
  }
  snapshotStore.recordSnapshotCache('lock')
  menuOpt('lock')
}

const unlock = () => {
  if (curComponent.value && !isGroupArea.value) {
    lockStore.unlock()
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      lockStore.unlock(component)
    })
  }
  menuOpt('unlock')
}

// 点击菜单时不取消当前组件的选中状态
const handleMouseUp = () => {
  dvMainStore.setClickComponentStatus(true)
  activePosition.value === 'aside' && emit('close')
}

const menuOpt = optName => {
  const param = { opt: optName }
  activePosition.value === 'aside' && emit('close', param)
}

const cut = () => {
  if (curComponent.value) {
    const curInfo = getCurInfo()
    copyStore.cut(curInfo.componentData)
  } else if (areaData.value.components.length) {
    copyStore.cut()
  }
  menuOpt('cut')
}

const copy = () => {
  copyStore.copy()
  menuOpt('copy')
}

const hide = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.hideComponentWithComponent()
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      layerStore.hideComponentWithComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('hide')
  menuOpt('hide')
}

const show = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.showComponent()
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      layerStore.showComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('show')
  menuOpt('show')
}
const showMoveMenu = computed(
  () =>
    curComponent?.value?.canvasId === 'canvas-main' &&
    curComponent?.value['category'] === 'base' &&
    curComponent.value?.component === 'VQuery' &&
    popComponentDataLength.value === 0
)
const categoryChange = type => {
  if (curComponent.value) {
    snapshotStore.recordSnapshotCache('categoryChange')
    curComponent.value['category'] = type
    if (type === 'hidden') {
      dvMainStore.canvasStateChange({ key: 'curPointArea', value: 'hidden' })
    }
  }
}
const rename = () => {
  emit('rename')
  menuOpt('rename')
}
const paste = () => {
  copyStore.paste(true)
  snapshotStore.recordSnapshotCache('renderChart')
  menuOpt('paste')
}

const deleteComponent = () => {
  if (curComponent.value && !isGroupArea.value) {
    const curInfo = getCurInfo()
    dvMainStore.deleteComponentById(curComponent.value?.id, curInfo.componentData)
  } else if (areaData.value.components.length) {
    areaData.value.components.forEach(component => {
      dvMainStore.deleteComponentById(component.id)
    })
  }
  eventBus.emit('hideArea-canvas-main')
  snapshotStore.recordSnapshotCache('deleteComponent')
  menuOpt('deleteComponent')
}

const upComponent = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.upComponent(curComponent.value.id)
  } else if (areaData.value.components.length) {
    componentArraySort(areaData.value.components)
    areaData.value.components.forEach(component => {
      layerStore.upComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('upComponent')
  menuOpt('upComponent')
}

const downComponent = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.downComponent(curComponent.value.id)
  } else if (areaData.value.components.length) {
    componentArraySort(areaData.value.components, 'top')
    areaData.value.components.forEach(component => {
      layerStore.downComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('downComponent')
  menuOpt('downComponent')
}

const topComponent = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.topComponent(curComponent.value.id)
  } else if (areaData.value.components.length) {
    componentArraySort(areaData.value.components, 'top')
    areaData.value.components.forEach(component => {
      layerStore.topComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('topComponent')
  menuOpt('topComponent')
}

const bottomComponent = () => {
  if (curComponent.value && !isGroupArea.value) {
    layerStore.bottomComponent(curComponent.value.id)
  } else if (areaData.value.components.length) {
    componentArraySort(areaData.value.components)
    areaData.value.components.forEach(component => {
      layerStore.bottomComponent(component.id)
    })
  }
  snapshotStore.recordSnapshotCache('bottomComponent')
  menuOpt('bottomComponent')
}

const customSort = () => {
  // do customSort
  eventBus.emit('tabSort')
}

const componentCompose = () => {
  composeStore.compose()
  snapshotStore.recordSnapshotCache('componentCompose')
  menuOpt('componentCompose')
}

const decompose = () => {
  composeStore.decompose()
  snapshotStore.recordSnapshotCache('decompose')
  menuOpt('decompose')
}

const alignment = params => {
  composeStore.alignment(params)
  snapshotStore.recordSnapshotCache('decompose')
}

// 阻止事件向父级组件传播调用父级的handleMouseDown 导致areaData 被隐藏
const handleComposeMouseDown = e => {
  e.preventDefault()
  e.stopPropagation()
}

const composeDivider = computed(() => {
  return !(
    !curComponent ||
    curComponent['isLock'] ||
    curComponent['component'] != 'Group' ||
    curComponent.category === 'hidden'
  )
})

const isGroupArea = computed(() => {
  return curComponent.value?.component === 'GroupArea'
})

const editQueryCriteria = () => {
  emitter.emit(`editQueryCriteria${curComponent.value.id}`)
}
</script>

<template>
  <div class="context-menu-base context-menu-details" @mousedown="handleComposeMouseDown">
    <ul @mouseup="handleMouseUp">
      <template v-if="areaData.components.length">
        <li @mousedown="handleComposeMouseDown" @click="componentCompose">
          {{ t('visualization.view_group') }}
        </li>
        <el-dropdown
          style="width: 100%"
          trigger="hover"
          effect="dark"
          placement="right-start"
          :teleported="false"
          popper-class="context-menu-details"
        >
          <li>
            <div>
              <span>{{ t('visualization.alignment') }}</span
              ><el-icon><ArrowRight /></el-icon>
            </div>
          </li>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item style="width: 118px" @click="alignment('left')">{{
                t('visualization.left_justifying')
              }}</el-dropdown-item>
              <el-dropdown-item style="width: 118px" @click="alignment('right')">{{
                t('visualization.right_justifying')
              }}</el-dropdown-item>
              <el-dropdown-item @click="alignment('top')">{{
                t('visualization.top_justifying')
              }}</el-dropdown-item>
              <el-dropdown-item @click="alignment('bottom')">{{
                t('visualization.bottom_justifying')
              }}</el-dropdown-item>
              <el-dropdown-item @click="alignment('transverse')">{{
                t('visualization.horizontally_centered')
              }}</el-dropdown-item>
              <el-dropdown-item @click="alignment('direction')">{{
                t('visualization.vertically_centered')
              }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-divider class="custom-divider" />
        <li @click="copy">{{ t('visualization.copy') }}</li>
        <li @click="paste">{{ t('visualization.paste') }}</li>
        <li @click="cut">{{ t('visualization.cut') }}</li>
        <el-divider class="custom-divider" />
        <li @click="deleteComponent">{{ t('visualization.delete') }}</li>
      </template>
      <template v-else>
        <li
          v-show="
            !(!curComponent || curComponent['isLock'] || curComponent['component'] != 'Group')
          "
          @click="decompose()"
        >
          {{ t('visualization.cancel_group') }}
        </li>
        <el-divider class="custom-divider" v-show="composeDivider" />
        <template v-if="curComponent">
          <template v-if="!curComponent['isLock'] && curComponent.category === 'hidden'">
            <li @click="categoryChange('base')">{{ t('visualization.move_to_screen_show') }}</li>
            <li @click="editQueryCriteria">{{ t('visualization.edit') }}</li>
            <li v-if="activePosition === 'aside'" @click="rename">
              {{ t('visualization.rename') }}
            </li>
            <li @click="copy">{{ t('visualization.copy') }}</li>
            <li @click="paste">{{ t('visualization.paste') }}</li>
            <el-divider class="custom-divider" />
            <li @click="deleteComponent">{{ t('visualization.delete') }}</li>
          </template>
          <template v-if="!curComponent['isLock'] && curComponent.category !== 'hidden'">
            <li v-if="curComponent.component === 'VQuery'" @click="editQueryCriteria">
              {{ t('visualization.edit') }}
            </li>
            <li @click="upComponent">{{ t('visualization.up_component') }}</li>
            <li @click="downComponent">{{ t('visualization.down_component') }}</li>
            <li @click="topComponent">{{ t('visualization.top_component') }}</li>
            <li @click="bottomComponent">{{ t('visualization.bottom_component') }}</li>
            <li @click="customSort" v-if="curComponent.component === 'DeTabs'">
              {{ t('visualization.sort') }}
            </li>
            <xpack-component
              :chart="curComponent"
              is-screen
              resource-table="snapshot"
              jsname="L2NvbXBvbmVudC90aHJlc2hvbGQtd2FybmluZy9FZGl0QmFySGFuZGxlcg=="
            />
            <li @click="categoryChange('hidden')" v-show="showMoveMenu">
              {{ t('visualization.move_to_pop_area') }}
            </li>
            <el-divider class="custom-divider" />
            <li @click="hide" v-show="curComponent['isShow']">{{ t('visualization.hidden') }}</li>
            <li @click="show" v-show="!curComponent['isShow'] || isGroupArea">
              {{ t('visualization.cancel_hidden') }}
            </li>
            <li @click="lock">{{ t('visualization.lock') }}</li>
            <li v-if="curComponent['isLock'] || isGroupArea" @click="unlock">
              {{ t('visualization.unlock') }}
            </li>
            <el-divider class="custom-divider" />
            <li v-if="activePosition === 'aside'" @click="rename">
              {{ t('visualization.rename') }}
            </li>
            <li @click="copy">{{ t('visualization.copy') }}</li>
            <li @click="paste">{{ t('visualization.paste') }}</li>
            <li @click="cut">{{ t('visualization.cut') }}</li>
            <el-divider class="custom-divider" />
            <li @click="deleteComponent">{{ t('visualization.delete') }}</li>
          </template>
          <li v-if="curComponent['isLock']" @click="unlock">{{ t('visualization.unlock') }}</li>
        </template>
        <li v-else-if="!curComponent && !areaData.components.length" @click="paste">
          {{ t('visualization.paste') }}
        </li>
      </template>
    </ul>
  </div>
</template>

<style lang="less">
.context-menu-base {
  width: 220px;
}
.context-menu-details {
  z-index: 1000;
  border: #434343 1px solid;
  ul {
    padding: 4px 0;
    background-color: #292929;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    box-sizing: border-box;
    :deep(.ed-divider) {
      margin: 8px 0;
    }

    li {
      width: 100%;
      font-size: 14px;
      padding: 0 12px;
      position: relative;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      color: #ebebeb;
      height: 34px;
      line-height: 34px;
      box-sizing: border-box;
      cursor: pointer;

      i {
        position: absolute;
        right: 0px;
        top: 50%;
        transform: translate(-50%, -50%);
      }

      &:hover {
        background-color: #333 !important;
      }
    }
  }
}

.custom-divider {
  border-color: #434343 !important;
  margin: 0 !important;
}
</style>
