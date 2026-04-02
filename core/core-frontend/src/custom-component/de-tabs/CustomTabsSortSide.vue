<template>
  <div class="drag-list">
    <div style="flex: 1; min-width: 0">
      <draggable :list="config.propValue" animation="300" @end="onSortChange">
        <template #item="{ element }">
          <span
            :key="element.name"
            class="item-dimension"
            :class="{ 'item-dimension-dark': themes === 'dark' }"
            :title="element.title"
          >
            <el-icon size="20px">
              <Icon name="drag"><drag class="svg-icon" /></Icon>
            </el-icon>
            <span class="item-span">
              {{ element.title }}
            </span>
          </span>
        </template>
      </draggable>
    </div>
    <div style="width: 80px">
      <div v-for="item in config.propValue" :key="item" class="item-icon">
        <el-icon class="component-base" @click="onDelete(item)">
          <Icon name="dv-show"><Delete class="svg-icon f16" /></Icon>
        </el-icon>
        <el-icon
          v-show="item.hidden"
          class="component-base component-icon-display"
          @click="onShow(item)"
        >
          <Icon name="dv-eye-close"><dvEyeClose class="svg-icon f16" /></Icon>
        </el-icon>
        <el-icon v-show="!item.hidden" class="component-base" @click="onHidden(item)">
          <Icon name="dv-show"><dvShow class="svg-icon f16" /></Icon>
        </el-icon>

        <el-icon v-show="!item.hidden" class="component-base" @click="onCopy(item)">
          <Icon name="dv-show"><CopyDocument class="svg-icon f16" /></Icon>
        </el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import drag from '@/assets/svg/drag.svg'
import draggable from 'vuedraggable'
import { nextTick, toRefs } from 'vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import dvEyeClose from '@/assets/svg/dv-eye-close.svg'
import dvShow from '@/assets/svg/dv-show.svg'

const snapshotStore = snapshotStoreWithOut()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    config: any
  }>(),
  {
    themes: 'dark',
    config: {
      propValue: []
    }
  }
)

const { config, themes } = toRefs(props)

// 检查并修正当前激活的tab页（传入当前隐藏的tabItem）
const checkAndFixCurrentTab = tabItem => {
  // 如果隐藏的不是当前激活的tab，不需要处理
  if (tabItem.name !== config.value.editableTabsValue) return

  // 过滤出可见的标签页
  const visibleTabs = config.value.propValue.filter(tab => !tab.hidden)

  // 如果没有可见标签页，不做处理
  if (visibleTabs.length === 0) return

  // 切换到第一个可见的tab
  nextTick(() => {
    config.value.editableTabsValue = visibleTabs[0].name
  })
}

const onSortChange = () => {
  snapshotStore.recordSnapshotCache('tab-sort-save')
  eventBus.emit('onTabSortChange-' + config.value.id)
}

const onShow = tabItem => {
  snapshotStore.recordSnapshotCache('tab')
  tabItem['hidden'] = false
  config.value.editableTabsValue = tabItem.name
}

const onHidden = tabItem => {
  snapshotStore.recordSnapshotCache('tab')
  tabItem['hidden'] = true
  checkAndFixCurrentTab(tabItem)
}

const onDelete = item => {
  snapshotStore.recordSnapshotCache('tab')
  eventBus.emit('onTabDelete-' + config.value.id, deepCopy(item))
  checkAndFixCurrentTab(item)
}

const onCopy = item => {
  snapshotStore.recordSnapshotCache('tab')
  eventBus.emit('onTabCopy-' + config.value.id, deepCopy(item))
}
import { ElIcon } from 'element-plus-secondary'
import Icon from '../../components/icon-custom/src/Icon.vue'
import { deepCopy } from '@/utils/utils'
</script>

<style scoped lang="less">
.drag-list {
  overflow: auto;
  max-height: 400px;
  display: flex;
  width: 100%;
}

.item-icon {
  height: 31px;
  padding: 2px;
  margin: 2px !important;
  text-align: left;
  color: #606266;
  display: flex;
  align-items: center;
}

.item-dimension {
  padding: 2px;
  margin: 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: flex;
  align-items: center;
}

.item-icon {
  cursor: move;
  margin: 0 2px;
}

.item-span {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: var(--ed-border-color);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension-dark {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 6px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
  cursor: pointer;
}

.component-base {
  cursor: pointer;
  height: 22px !important;
  width: 22px !important;
  border-radius: 4px;
  padding: 0 4px;
  color: #a6a6a6;

  .f16 {
    font-size: 16px;
  }

  &:hover {
    background: rgba(235, 235, 235, 0.1);
  }

  &:active {
    background: rgba(235, 235, 235, 0.1);
  }
}
</style>
