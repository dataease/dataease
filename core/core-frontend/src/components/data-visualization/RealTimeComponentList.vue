<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ElCol, ElIcon, ElRow } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { computed, nextTick, ref } from 'vue'
import draggable from 'vuedraggable'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import ContextMenuAsideDetails from '@/components/data-visualization/canvas/ContextMenuAsideDetails.vue'
import ComposeShow from '@/components/data-visualization/canvas/ComposeShow.vue'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
const dropdownMore = ref(null)
const lockStore = lockStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()

const { areaData, isCtrlOrCmdDown } = storeToRefs(composeStore)

const { componentData, curComponent, curComponentIndex, canvasViewInfo } = storeToRefs(dvMainStore)
const getComponent = index => {
  return componentData.value[componentData.value.length - 1 - index]
}
const transformIndex = index => {
  return componentData.value.length - 1 - index
}
const onClick = (e, index) => {
  // ctrl or command 按下时 鼠标点击为选择需要组合的组件(取消需要组合的组件在ComposeShow组件中)
  if (isCtrlOrCmdDown.value && !areaData.value.components.includes(componentData.value[index])) {
    areaData.value.components.push(componentData.value[index])
    if (curComponent.value && curComponent.value.id !== componentData.value[index].id) {
      areaData.value.components.push(curComponent.value)
    }
    dvMainStore.setCurComponent({ component: null, index: null })
    e.stopPropagation()
    return
  }
  //其他情况点击清理选择区域
  areaData.value.components.splice(0, areaData.value.components.length)
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
    snapshotStore.recordSnapshot('realTime-downComponent')
  })
}
const setCurComponent = index => {
  dvMainStore.setCurComponent({ component: componentData.value[index], index })
}

let nameEdit = ref(false)
let editComponentId = ref('')
let inputName = ref('')
let nameInput = ref(null)
const editComponentName = item => {
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
  if (inputName.value.trim() === curComponent.value.name) {
    return
  }
  curComponent.value.name = inputName.value
  inputName.value = ''
}

const toggleComponentVisible = () => {
  // do toggleComponentVisible
}

const lock = () => {
  setTimeout(() => {
    lockStore.lock()
    snapshotStore.recordSnapshot('realTime-lock')
  })
}

const unlock = () => {
  setTimeout(() => {
    lockStore.unlock()
    snapshotStore.recordSnapshot('realTime-unlock')
  })
}

const hideComponent = () => {
  setTimeout(() => {
    layerStore.hideComponent()
    snapshotStore.recordSnapshot('realTime-hideComponent')
  })
}

const showComponent = () => {
  setTimeout(() => {
    layerStore.showComponent()
    snapshotStore.recordSnapshot()
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
    return viewInfo.type
  } else {
    return item.icon
  }
}

const menuAsideClose = (param, index) => {
  const iconDom = document.getElementById('close-button')
  if (iconDom) {
    iconDom.click()
  }
  if (param.opt === 'rename') {
    setTimeout(() => {
      editComponentName(getComponent(index))
    }, 200)
  }
}
const rename = item => {
  editComponentName(item)
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
              :title="getComponent(index).name"
              class="component-item"
              :class="{
                'container-item-not-show': !getComponent(index).isShow,
                activated:
                  (curComponent && curComponent.id === getComponent(index).id) ||
                  areaData.components.includes(getComponent(index))
              }"
              @click="onClick($event, transformIndex(index))"
              @dblclick="editComponentName(getComponent(index))"
            >
              <el-icon class="component-icon">
                <Icon :name="getIconName(getComponent(index))"></Icon>
              </el-icon>
              <span :id="`component-label-${getComponent(index).id}`" class="component-label">
                {{ getComponent(index).name }}
              </span>
              <div
                v-show="!nameEdit || (nameEdit && curComponent.id !== getComponent(index).id)"
                class="icon-container"
                :class="{
                  'icon-container-lock': getComponent(index).isLock && getComponent(index).isShow,
                  'icon-container-show': !getComponent(index).isShow
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
                  v-show="getComponent(index).isShow"
                  @click="hideComponent"
                >
                  <Icon name="dv-show" class="opt-icon"></Icon>
                </el-icon>
                <el-icon v-show="!getComponent(index).isLock" class="component-base" @click="lock">
                  <Icon class="opt-icon" name="dv-unlock"></Icon>
                </el-icon>
                <el-icon
                  class="component-base component-icon-display"
                  v-show="getComponent(index).isLock"
                  @click="unlock"
                >
                  <Icon name="dv-lock" class="opt-icon"></Icon>
                </el-icon>
                <el-dropdown
                  ref="dropdownMore"
                  trigger="click"
                  placement="bottom-start"
                  effect="dark"
                  hide-timeout="0"
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
              <compose-show :show-border="false" :element="getComponent(index)"></compose-show>
            </div>
          </template>
        </draggable>
        <el-row style="width: 100%; height: 150px"></el-row>
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
        padding: 0 2px 0 22px;
        user-select: none;

        .component-icon {
          font-size: 14px;
        }

        > span.component-label {
          font-size: 12px;
          margin-left: 10px;
          position: relative;
          min-width: 65px;
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
        background-color: rgba(51, 112, 255, 0.1) !important;
        :deep(.component-icon) {
          color: #3370ff;
        }
        :deep(.component-label) {
          color: #3370ff;
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
}
</style>
