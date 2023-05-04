<script lang="ts" setup>
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ElCol, ElIcon, ElRow } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { nextTick, ref } from 'vue'

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
const toggleComponentLock = () => {
  console.log(1)
}
</script>

<template>
  <el-col class="real-time-component-list">
    <el-row align="middle" class="banner" justify="space-between">
      <span>图层管理</span>
      <el-icon size="20px" @click="toggleComponentList">
        <Expand />
      </el-icon>
    </el-row>
    <el-row class="list-wrap">
      <div class="list-container">
        <div
          v-for="(item, index) in componentData"
          :key="index"
          :class="{ activated: transformIndex(index) === curComponentIndex }"
          class="component-item"
          @click="onClick(transformIndex(index))"
          @dblclick="editComponentName(getComponent(index))"
        >
          <el-icon class="component-icon">
            <Icon :name="getComponent(index).icon"></Icon>
          </el-icon>
          <span
            :id="`component-label-${getComponent(index).id}`"
            :title="getComponent(index).name"
            class="component-label"
          >
            {{ getComponent(index).name }}
          </span>
          <div
            v-show="!nameEdit || (nameEdit && curComponent.id !== getComponent(index).id)"
            class="icon-container"
          >
            <el-icon v-show="getComponent(index).show" @click="toggleComponentVisible">
              <Hide />
            </el-icon>
            <el-icon v-show="!getComponent(index).show" @click="toggleComponentVisible">
              <View />
            </el-icon>
            <el-icon v-show="!getComponent(index).isLock" @click="toggleComponentLock">
              <Lock />
            </el-icon>
            <el-icon v-show="getComponent(index).isLock" @click="toggleComponentLock">
              <Unlock />
            </el-icon>
            <el-dropdown trigger="click">
              <el-icon @click="onClick(transformIndex(index))">
                <MoreFilled />
              </el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>Action 1</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </el-row>
    <Teleport v-if="editComponentId && nameEdit" :to="editComponentId">
      <input ref="nameInput" v-model="inputName" @blur="closeEditComponentName" />
    </Teleport>
  </el-col>
</template>

<style lang="less" scoped>
.real-time-component-list {
  color: white;
  background-color: #232c31;
  border-right: #525552 1px solid;
  border-bottom: #525552 1px solid;
  white-space: nowrap;
  height: 100%;

  .banner {
    border-bottom: 1px solid #7b797b;
    height: @component-toolbar-height;
    padding: 8px 10px 8px 8px;
  }

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
</style>
