<script lang="ts" setup>
import tabTitle from '@/assets/svg/tab-title.svg'
import dvExpandDown from '@/assets/svg/dv-expand-down.svg'
import dvExpandRight from '@/assets/svg/dv-expand-right.svg'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { ElIcon, ElRow } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { nextTick, ref, toRefs } from 'vue'
import draggable from 'vuedraggable'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import RealTimeGroup from '@/components/data-visualization/RealTimeGroup.vue'
import eventBus from '@/utils/eventBus'
import { syncViewTitle } from '@/utils/canvasUtils'

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const composeStore = composeStoreWithOut()

const { areaData } = storeToRefs(composeStore)

const { curTabName } = storeToRefs(dvMainStore)

const props = defineProps({
  tabPosition: {
    type: String,
    required: false,
    default: 'main'
  },
  componentData: [],
  tabElement: {}
})

const { componentData, tabElement } = toRefs(props)

const getComponent = index => {
  return componentData.value[index]
}
const onClick = item => {
  if (item) {
    dvMainStore.setCurTabName(item.name)
  } else {
    dvMainStore.setCurTabName(null)
  }
  //其他情况点击清理选择区域
  areaData.value.components.splice(0, areaData.value.components.length)
}

let nameEdit = ref(false)
let editComponentId = ref('')
let inputName = ref('')
let nameInput = ref(null)
let curEditComponent = null
const editComponentName = item => {
  curEditComponent = item
  editComponentId.value = `#component-label-${item.name}`
  nameEdit.value = true
  inputName.value = item.title
  nextTick(() => {
    nameInput.value.focus()
  })
}
const closeEditComponentName = () => {
  nameEdit.value = false
  if (!inputName.value || !inputName.value.trim()) {
    return
  }
  if (inputName.value.trim() === curEditComponent.title) {
    return
  }
  curEditComponent.title = inputName.value
  syncViewTitle(curEditComponent)
  inputName.value = ''
  curEditComponent = null
}

const dragOnEnd = ({ newIndex }) => {
  const source = componentData.value[newIndex]
  dvMainStore.setCurTabName(source.title)
  eventBus.emit('onTabSortChange-' + tabElement.value?.id)
  snapshotStore.recordSnapshotCache('dragOnEnd')
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

const expandClick = component => {
  component['expand'] = !component['expand']
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
            <div>
              <div
                :title="getComponent(index)?.title"
                class="component-item"
                :class="{
                  activated: curTabName === getComponent(index)?.name,
                  'component-item-group': props.tabPosition === 'groupTab'
                }"
                @click="onClick(getComponent(index))"
              >
                <div style="width: 22px">
                  <el-icon class="component-expand" @click="expandClick(getComponent(index))">
                    <Icon
                      v-if="getComponent(index)?.expand"
                      name="dv-expand-down"
                      class="expand-icon"
                      ><dvExpandDown class="svg-icon expand-icon"
                    /></Icon>
                    <Icon
                      v-if="!getComponent(index)?.expand"
                      name="dv-expand-right"
                      class="expand-icon"
                      ><dvExpandRight class="svg-icon expand-icon"
                    /></Icon>
                  </el-icon>
                </div>
                <el-icon class="component-icon">
                  <Icon><component :is="tabTitle"></component></Icon>
                </el-icon>
                <span
                  :id="`component-label-${getComponent(index)?.name}`"
                  class="component-label"
                  @dblclick="editComponentName(getComponent(index))"
                >
                  {{ getComponent(index)?.title }}
                </span>
              </div>
              <div v-if="getComponent(index)?.expand">
                <real-time-group
                  :tab-position="tabPosition"
                  :component-data="getComponent(index).componentData"
                ></real-time-group>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </el-row>
    <Teleport v-if="editComponentId && nameEdit" :to="editComponentId">
      <input
        class="custom-teleport"
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
.component-item-group {
  padding: 0 2px 0 40px !important;
}
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
        padding: 0 2px 0 15px;
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
            width: 22px !important;
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
        background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.05)) !important;
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

.component-expand {
  cursor: pointer;
  height: 16px !important;
  width: 16px !important;
  border-radius: 2px;
  padding: 0 2px;

  .expand-icon {
    font-size: 10px;
  }

  &:hover {
    background: rgba(235, 235, 235, 0.1);
  }

  &:active {
    background: rgba(235, 235, 235, 0.1);
  }
}
.custom-teleport {
  background: #1a1a1a !important;
}
</style>

<style lang="less">
.compose-dropdown {
  position: initial !important;
}
</style>
