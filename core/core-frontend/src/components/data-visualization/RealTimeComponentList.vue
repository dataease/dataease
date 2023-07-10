<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ElCol, ElIcon, ElRow } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { nextTick, ref } from 'vue'
import draggable from 'vuedraggable'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import ContextMenu from '@/components/data-visualization/canvas/ContextMenu.vue'
const lockStore = lockStoreWithOut()

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
    snapshotStore.recordSnapshot('realTime-deleteComponent')
  })
}
const upComponent = (number: number) => {
  setTimeout(() => {
    layerStore.upComponent()
    snapshotStore.recordSnapshot('realTime-upComponent')
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

const toggleComponentList = () => {
  console.log(111)
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
  console.log(111)
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
    snapshotStore.recordSnapshot('realTime-showComponent')
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
</script>

<template>
  <!--为了保持图层视觉上的一致性 这里进行数组的倒序排列 相应的展示和移动按照倒序处理-->
  <div class="real-time-component-list">
    <el-row class="list-wrap">
      <div class="list-container">
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
              :class="{ activated: transformIndex(index) === curComponentIndex }"
              class="component-item"
              @click="onClick(transformIndex(index))"
              @dblclick="editComponentName(getComponent(index))"
            >
              <el-icon class="component-icon">
                <Icon :name="getComponent(index).icon"></Icon>
              </el-icon>
              <span :id="`component-label-${getComponent(index).id}`" class="component-label">
                {{ getComponent(index).name }}
              </span>
              <div
                v-show="!nameEdit || (nameEdit && curComponent.id !== getComponent(index).id)"
                class="icon-container"
              >
                <el-icon v-show="!getComponent(index).isShow" @click="showComponent">
                  <Hide />
                </el-icon>
                <el-icon v-show="getComponent(index).isShow" @click="hideComponent">
                  <View />
                </el-icon>
                <el-icon v-show="!getComponent(index).isLock" @click="lock">
                  <Unlock />
                </el-icon>
                <el-icon v-show="getComponent(index).isLock" @click="unlock">
                  <Lock />
                </el-icon>
                <el-dropdown trigger="click">
                  <el-icon @click="onClick(transformIndex(index))">
                    <MoreFilled />
                  </el-icon>
                  <template #dropdown>
                    <div>More Action</div>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </el-row>
    <Teleport v-if="editComponentId && nameEdit" :to="editComponentId">
      <input ref="nameInput" v-model="inputName" @blur="closeEditComponentName" />
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
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: flex-start;
        font-size: 12px;
        padding: 0 10px;
        user-select: none;

        .component-icon {
          color: #4772f1;
          font-size: 20px;
        }

        > span.component-label {
          font-size: 14px;
          margin-left: 10px;
          position: relative;
          width: 60%;
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
          background-color: rgba(200, 200, 200, 0.4);

          .icon-container {
            display: flex;
          }
        }

        .icon-container {
          display: none;
          justify-content: space-around;
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
        background-color: rgba(200, 200, 200, 0.2);
      }
    }
  }
}

.real-time-component-list :deep(.ed-popper) {
  background: #303133 !important;
}
</style>
