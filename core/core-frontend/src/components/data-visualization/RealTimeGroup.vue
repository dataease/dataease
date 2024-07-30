<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ElIcon, ElRow } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { nextTick, ref, toRefs } from 'vue'
import draggable from 'vuedraggable'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import ContextMenuAsideDetails from '@/components/data-visualization/canvas/ContextMenuAsideDetails.vue'
import ComposeShow from '@/components/data-visualization/canvas/ComposeShow.vue'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
const dropdownMore = ref(null)
const lockStore = lockStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()
const composeStore = composeStoreWithOut()

const { areaData } = storeToRefs(composeStore)

const { curComponent, canvasViewInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  componentData: []
})

const { componentData } = toRefs(props)

const getComponent = index => {
  return componentData.value[componentData.value.length - 1 - index]
}
const transformIndex = index => {
  return componentData.value.length - 1 - index
}

const onClick = index => {
  setCurComponent(index)
  //其他情况点击清理选择区域
  areaData.value.components.splice(0, areaData.value.components.length)
}

const setCurComponent = index => {
  dvMainStore.setCurComponent({ component: componentData.value[index], index })
}

let nameEdit = ref(false)
let editComponentId = ref('')
let inputName = ref('')
let nameInput = ref(null)
let curEditComponent = null
const editComponentName = item => {
  curEditComponent = curComponent.value
  editComponentId.value = `#component-label-${item.id}`
  nameEdit.value = true
  inputName.value = item.name
  nextTick(() => {
    nameInput.value.focus()
  })
}
const closeEditComponentName = () => {
  nameEdit.value = false
  if (!inputName.value || !inputName.value.trim()) {
    return
  }
  if (inputName.value.trim() === curEditComponent.name) {
    return
  }
  curEditComponent.name = inputName.value
  inputName.value = ''
  curEditComponent = null
}

const lock = () => {
  setTimeout(() => {
    lockStore.lock()
    snapshotStore.recordSnapshotCache('realTime-lock')
  })
}

const unlock = () => {
  setTimeout(() => {
    lockStore.unlock()
    snapshotStore.recordSnapshotCache('realTime-unlock')
  })
}

const hideComponent = () => {
  setTimeout(() => {
    layerStore.hideComponent()
    snapshotStore.recordSnapshotCache('realTime-hideComponent')
  })
}

const showComponent = () => {
  setTimeout(() => {
    layerStore.showComponent()
    snapshotStore.recordSnapshotCache()
  })
}

const dragOnEnd = ({ oldIndex, newIndex }) => {
  const source = componentData.value[newIndex]
  const comLength = componentData.value.length
  // 还原数组
  componentData.value.splice(newIndex, 1)
  componentData.value.splice(oldIndex, 0, source)
  const target = componentData.value[comLength - 1 - oldIndex]
  // 反向移动数组
  componentData.value.splice(comLength - 1 - oldIndex, 1)
  componentData.value.splice(comLength - 1 - newIndex, 0, target)
  dvMainStore.setCurComponent({ component: target, index: transformIndex(comLength - oldIndex) })
}

const getIconName = item => {
  if (item.component === 'UserView') {
    const viewInfo = canvasViewInfo.value[item.id]
    return `${viewInfo.type}-origin`
  } else {
    return item.icon
  }
}

const menuAsideClose = (param, index) => {
  const iconDom = document.getElementById('close-button')
  if (iconDom) {
    iconDom.click()
  }
  if (param?.opt === 'rename') {
    setTimeout(() => {
      editComponentName(getComponent(index))
    }, 200)
  }
}

const handleContextMenu = e => {
  e.preventDefault()
  // 获取鼠标点击位置
  const x = e.clientX
  const y = e.clientY
  const customContextMenu = document.createElement('div')
  customContextMenu.style.position = 'fixed'
  customContextMenu.style.left = x + 'px'
  customContextMenu.style.top = y + 'px'

  // 将自定义菜单添加到页面
  document.body.appendChild(customContextMenu)

  // 为自定义菜单添加事件监听器，例如，点击菜单项后执行的操作
  customContextMenu.addEventListener('click', () => {
    // 在这里执行菜单项点击后的操作
    // 例如，关闭菜单
    document.body.removeChild(customContextMenu)
  })
}
</script>

<template>
  <!--为了保持图层视觉上的一致性 这里进行数组的倒序排列 相应的展示和移动按照倒序处理-->
  <div class="real-time-component-list">
    <button hidden="true" id="close-button"></button>
    <el-row class="list-wrap">
      <div class="list-container" @contextmenu="handleContextMenu">
        <draggable
          @end="dragOnEnd"
          :list="componentData"
          animation="100"
          class="drag-list"
          item-key="id"
        >
          <template #item="{ index }">
            <div
              :title="getComponent(index)?.name"
              class="component-item"
              :class="{
                'container-item-not-show': !getComponent(index)?.isShow,
                activated:
                  (curComponent && curComponent?.id === getComponent(index)?.id) ||
                  areaData.components.includes(getComponent(index))
              }"
              @click="onClick(transformIndex(index))"
            >
              <el-icon class="component-icon">
                <Icon :name="getIconName(getComponent(index))"></Icon>
              </el-icon>
              <span
                :id="`component-label-${getComponent(index)?.id}`"
                class="component-label"
                @dblclick="editComponentName(getComponent(index))"
              >
                {{ getComponent(index)?.name }}
              </span>
              <div
                v-show="!nameEdit || (nameEdit && curComponent?.id !== getComponent(index)?.id)"
                class="icon-container"
                :class="{
                  'icon-container-lock': getComponent(index)?.isLock && getComponent(index)?.isShow,
                  'icon-container-show': !getComponent(index)?.isShow
                }"
              >
                <el-icon
                  class="component-base component-icon-display"
                  v-show="!getComponent(index).isShow"
                  @click="showComponent"
                >
                  <Icon name="dv-eye-close" class="opt-icon"></Icon>
                </el-icon>
                <el-icon
                  class="component-base"
                  v-show="getComponent(index)?.isShow"
                  @click="hideComponent"
                >
                  <Icon name="dv-show" class="opt-icon"></Icon>
                </el-icon>
                <el-icon v-show="!getComponent(index)?.isLock" class="component-base" @click="lock">
                  <Icon class="opt-icon" name="dv-unlock"></Icon>
                </el-icon>
                <el-icon
                  class="component-base component-icon-display"
                  v-show="getComponent(index)?.isLock"
                  @click="unlock"
                >
                  <Icon name="dv-lock" class="opt-icon"></Icon>
                </el-icon>
                <el-dropdown
                  ref="dropdownMore"
                  trigger="click"
                  placement="bottom-start"
                  effect="dark"
                  :hide-timeout="0"
                >
                  <span :class="'dropdownMore-' + index" @click="onClick(transformIndex(index))">
                    <el-icon class="component-base">
                      <Icon name="dv-more" class="opt-icon"></Icon>
                    </el-icon>
                  </span>
                  <template #dropdown>
                    <context-menu-aside-details
                      :element="getComponent(index)"
                      @close="menuAsideClose($event, index)"
                    ></context-menu-aside-details>
                  </template>
                </el-dropdown>
              </div>
              <el-dropdown
                class="compose-dropdown"
                trigger="contextmenu"
                placement="bottom-start"
                effect="dark"
                :hide-timeout="0"
              >
                <compose-show
                  :show-border="false"
                  :element-index="transformIndex(index)"
                  :element="getComponent(index)"
                ></compose-show>
                <template #dropdown>
                  <context-menu-aside-details
                    :element="getComponent(index)"
                    @close="menuAsideClose($event, index)"
                  ></context-menu-aside-details>
                </template>
              </el-dropdown>
            </div>
          </template>
        </draggable>
      </div>
    </el-row>
    <Teleport v-if="editComponentId && nameEdit" :to="editComponentId">
      <input
        @keydown.stop
        @keyup.stop
        ref="nameInput"
        v-model="inputName"
        @blur="closeEditComponentName"
      />
    </Teleport>
  </div>
</template>

<style lang="less" scoped>
.real-time-component-list {
  white-space: nowrap;
  .list-wrap {
    max-height: calc(100% - @component-toolbar-height);
    overflow-y: auto;
    width: 100%;
    .list-container {
      width: 100%;
      .component-item {
        position: relative;
        height: 30px;
        width: 100%;
        cursor: grab;
        color: @dv-canvas-main-font-color;
        display: flex;
        align-items: center;
        justify-content: flex-start;
        font-size: 12px;
        padding: 0 2px 0 44px;
        user-select: none;

        .component-icon {
          color: #a6a6a6;
          font-size: 14px;
        }
        .component-label {
          color: #ebebeb;
        }

        > span.component-label {
          font-size: 12px;
          margin-left: 10px;
          position: relative;
          min-width: 43px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          input {
            position: absolute;
            left: 0;
            width: 100%;
            background-color: white;
            outline: none;
            border: none;
            border-radius: 2px;
            padding: 0 4px;
            height: 100%;
          }
        }

        &:active {
          cursor: grabbing;
        }

        &:hover {
          background-color: rgba(235, 235, 235, 0.1);

          .icon-container {
            .component-base {
              opacity: 1;
            }
            width: 70px !important;
          }
        }

        .icon-container {
          .component-base {
            opacity: 0;
          }
          width: 0px;
          display: flex;
          justify-content: flex-end;
          align-items: center;
          flex-grow: 1;
          cursor: none;
          i {
            font-size: 16px;
            cursor: pointer;
          }
        }
      }
      .activated {
        background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
        :deep(.component-icon) {
          color: var(--ed-color-primary);
        }
        :deep(.component-label) {
          color: var(--ed-color-primary);
        }
      }
    }
  }
}

.real-time-component-list :deep(.ed-popper) {
  background: #303133 !important;
}

.component-base {
  cursor: pointer;
  height: 22px !important;
  width: 22px !important;
  border-radius: 4px;
  padding: 0 4px;

  .opt-icon {
    font-size: 14px;
  }

  &:hover {
    background: rgba(235, 235, 235, 0.1);
  }

  &:active {
    background: rgba(235, 235, 235, 0.1);
  }
}

.component-icon-display {
  opacity: 1 !important;
}

.icon-container-show {
  width: 70px !important;
}

.icon-container-lock {
  width: 45px !important;
}

.container-item-not-show {
  color: #5f5f5f !important;
  :deep(.component-icon) {
    color: #5f5f5f !important;
  }
  :deep(.component-label) {
    color: #5f5f5f !important;
  }
}
</style>

<style lang="less">
.compose-dropdown {
  position: initial !important;
}
</style>
