<script lang="ts" setup>
import group from '@/assets/svg/group.svg'
import bar from '@/assets/svg/bar.svg'
import dbMoreWeb from '@/assets/svg/db-more-web.svg'
import dvMoreTimeClock from '@/assets/svg/dv-more-time-clock.svg'
import dvPictureReal from '@/assets/svg/dv-picture-real.svg'
import dvTab from '@/assets/svg/dv-tab.svg'
import iconStream from '@/assets/svg/icon-stream.svg'
import iconVideo from '@/assets/svg/icon-video.svg'
import icon_graphical from '@/assets/svg/icon_graphical.svg'
import icon_search from '@/assets/svg/icon_search.svg'
import other_material_board from '@/assets/svg/other_material_board.svg'
import other_material_icon from '@/assets/svg/other_material_icon.svg'
import scrollText from '@/assets/svg/scroll-text.svg'
import areaOrigin from '@/assets/svg/area-origin.svg'
import areaStackOrigin from '@/assets/svg/area-stack-origin.svg'
import barGroupOrigin from '@/assets/svg/bar-group-origin.svg'
import barGroupStackOrigin from '@/assets/svg/bar-group-stack-origin.svg'
import barHorizontalOrigin from '@/assets/svg/bar-horizontal-origin.svg'
import barOrigin from '@/assets/svg/bar-origin.svg'
import barRangeOrigin from '@/assets/svg/bar-range-origin.svg'
import barStackHorizontalOrigin from '@/assets/svg/bar-stack-horizontal-origin.svg'
import barStackOrigin from '@/assets/svg/bar-stack-origin.svg'
import bidirectionalBarOrigin from '@/assets/svg/bidirectional-bar-origin.svg'
import bubbleMapOrigin from '@/assets/svg/bubble-map-origin.svg'
import chartMixGroupOrigin from '@/assets/svg/chart-mix-group-origin.svg'
import chartMixOrigin from '@/assets/svg/chart-mix-origin.svg'
import chartMixStackOrigin from '@/assets/svg/chart-mix-stack-origin.svg'
import chartMixDualLineOrigin from '@/assets/svg/chart-mix-dual-line-origin.svg'
import flowMapOrigin from '@/assets/svg/flow-map-origin.svg'
import funnelOrigin from '@/assets/svg/funnel-origin.svg'
import gaugeOrigin from '@/assets/svg/gauge-origin.svg'
import heatMapOrigin from '@/assets/svg/heat-map-origin.svg'
import indicatorOrigin from '@/assets/svg/indicator-origin.svg'
import lineOrigin from '@/assets/svg/line-origin.svg'
import liquidOrigin from '@/assets/svg/liquid-origin.svg'
import mapOrigin from '@/assets/svg/map-origin.svg'
import percentageBarStackHorizontalOrigin from '@/assets/svg/percentage-bar-stack-horizontal-origin.svg'
import percentageBarStackOrigin from '@/assets/svg/percentage-bar-stack-origin.svg'
import pieDonutOrigin from '@/assets/svg/pie-donut-origin.svg'
import pieDonutRoseOrigin from '@/assets/svg/pie-donut-rose-origin.svg'
import pieOrigin from '@/assets/svg/pie-origin.svg'
import pieRoseOrigin from '@/assets/svg/pie-rose-origin.svg'
import progressBarOrigin from '@/assets/svg/progress-bar-origin.svg'
import quadrantOrigin from '@/assets/svg/quadrant-origin.svg'
import radarOrigin from '@/assets/svg/radar-origin.svg'
import richTextOrigin from '@/assets/svg/rich-text-origin.svg'
import sankeyOrigin from '@/assets/svg/sankey-origin.svg'
import scatterOrigin from '@/assets/svg/scatter-origin.svg'
import stockLineOrigin from '@/assets/svg/stock-line-origin.svg'
import symbolicMapOrigin from '@/assets/svg/symbolic-map-origin.svg'
import tableInfoOrigin from '@/assets/svg/table-info-origin.svg'
import tableNormalOrigin from '@/assets/svg/table-normal-origin.svg'
import tablePivotOrigin from '@/assets/svg/table-pivot-origin.svg'
import treemapOrigin from '@/assets/svg/treemap-origin.svg'
import waterfallOrigin from '@/assets/svg/waterfall-origin.svg'
import wordCloudOrigin from '@/assets/svg/word-cloud-origin.svg'
import tHeatmapOrigin from '@/assets/svg/t-heatmap-origin.svg'
import dvMore from '@/assets/svg/dv-more.svg'
import dvExpandDown from '@/assets/svg/dv-expand-down.svg'
import dvExpandRight from '@/assets/svg/dv-expand-right.svg'
import dvEyeClose from '@/assets/svg/dv-eye-close.svg'
import dvShow from '@/assets/svg/dv-show.svg'
import dvUnlock from '@/assets/svg/dv-unlock.svg'
import dvLock from '@/assets/svg/dv-lock.svg'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { layerStoreWithOut } from '@/store/modules/data-visualization/layer'
import { storeToRefs } from 'pinia'
import { ElIcon, ElRow, ElSwitch } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { computed, nextTick, ref } from 'vue'
import draggable from 'vuedraggable'
import { lockStoreWithOut } from '@/store/modules/data-visualization/lock'
import ContextMenuAsideDetails from '@/components/data-visualization/canvas/ContextMenuAsideDetails.vue'
import ComposeShow from '@/components/data-visualization/canvas/ComposeShow.vue'
import { composeStoreWithOut } from '@/store/modules/data-visualization/compose'
import RealTimeGroup from '@/components/data-visualization/RealTimeGroup.vue'
import { contextmenuStoreWithOut } from '@/store/modules/data-visualization/contextmenu'
const dropdownMore = ref(null)
const lockStore = lockStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const layerStore = layerStoreWithOut()
const composeStore = composeStoreWithOut()
const contextmenuStore = contextmenuStoreWithOut()

const { areaData, isCtrlOrCmdDown, isShiftDown, laterIndex } = storeToRefs(composeStore)

const { componentData, canvasStyleData, curComponent, canvasViewInfo, canvasState } =
  storeToRefs(dvMainStore)
const getComponent = index => {
  return componentData.value[componentData.value.length - 1 - index]
}
const transformIndex = index => {
  return componentData.value.length - 1 - index
}
const areaDataPush = component => {
  if (
    component &&
    !component.isLock &&
    component.isShow &&
    component.canvasId === 'canvas-main' &&
    component.category !== 'hidden' &&
    !['GroupArea', 'DeTabs'].includes(component.component)
  ) {
    areaData.value.components.push(component)
  }
}

const hiddenAreaActive = computed(
  () => canvasState.value.curPointArea === 'hidden' && !curComponent.value
)
const baseAreaActive = computed(
  () => canvasState.value.curPointArea === 'base' && !curComponent.value
)
// shift 选择算法逻辑
// 1.记录上次点击的laterIndex（初始状态laterIndex=0）;
// 2.获取当前index curClickIndex;
// 3.比较laterIndex 和 curClickIndex之间的大小;
// 4.将[laterIndex,curClickIndex] 或者 [curClickIndex,laterIndex]区域的图层加入areaData.value.components(已包含的不再重复加入);
const shiftDataPush = curClickIndex => {
  const areaDataIdArray = areaData.value.components.map(com => com.id)
  let indexBegin, indexEnd
  const laterIndexTrans = laterIndex.value === null ? componentData.value.length : laterIndex.value
  if (laterIndexTrans < curClickIndex) {
    indexBegin = laterIndexTrans
    indexEnd = curClickIndex
  } else {
    indexBegin = curClickIndex
    indexEnd = laterIndexTrans
  }
  const shiftAreaComponents = componentData.value
    .slice(indexBegin, indexEnd + 1)
    .filter(
      component =>
        !areaDataIdArray.includes(component.id) &&
        !component.isLock &&
        component.isShow &&
        component.category !== 'hidden' &&
        !['GroupArea', 'DeTabs'].includes(component.component)
    )
  areaData.value.components.push(...shiftAreaComponents)
  dvMainStore.setCurComponent({ component: null, index: null })
}

const hiddenAreaOnClick = (e, element) => {
  let indexResult
  componentData.value.forEach((component, index) => {
    if (element.id === component.id) {
      indexResult = index
    }
  })
  dvMainStore.setCurComponent({ component: element, index: indexResult })
}

const onClick = (e, index) => {
  contextmenuStore.hideContextMenu()
  // 初始化点击是 laterIndex=0
  if (!curComponent.value) {
    composeStore.setLaterIndex(null)
  }
  // ctrl or command 按下时 鼠标点击为选择需要组合的组件(取消需要组合的组件在ComposeShow组件中)
  if (isCtrlOrCmdDown.value && !areaData.value.components.includes(componentData.value[index])) {
    areaDataPush(componentData.value[index])
    if (curComponent.value && curComponent.value.id !== componentData.value[index].id) {
      areaDataPush(curComponent.value)
    }
    dvMainStore.setCurComponent({ component: null, index: null })
    e.stopPropagation()
    composeStore.setLaterIndex(index)
    return
  }
  //shift操作逻辑
  if (isShiftDown.value) {
    shiftDataPush(index)
    return
  }

  //其他情况点击清理选择区域
  areaData.value.components.splice(0, areaData.value.components.length)
  setCurComponent(index)
  composeStore.setLaterIndex(index)
}
const setCurComponent = index => {
  dvMainStore.setCurComponent({ component: componentData.value[index], index })
}

const expandClick = component => {
  component['expand'] = !component['expand']
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

const popComponentData = computed(() =>
  componentData.value.filter(ele => ele.category && ele.category === 'hidden')
)

const baseComponentData = computed(() =>
  componentData.value.filter(ele => ele?.category !== 'hidden' && ele.component !== 'GroupArea')
)

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
  snapshotStore.recordSnapshotCache()
}
const iconMap = {
  bar: bar,
  'db-more-web': dbMoreWeb,
  'dv-more-time-clock': dvMoreTimeClock,
  'dv-picture-real': dvPictureReal,
  'dv-tab': dvTab,
  'icon-stream': iconStream,
  'icon-video': iconVideo,
  icon_graphical: icon_graphical,
  icon_search: icon_search,
  other_material_board: other_material_board,
  other_material_icon: other_material_icon,
  'scroll-text': scrollText,
  'area-origin': areaOrigin,
  'area-stack-origin': areaStackOrigin,
  'bar-group-origin': barGroupOrigin,
  'bar-group-stack-origin': barGroupStackOrigin,
  'bar-horizontal-origin': barHorizontalOrigin,
  'bar-origin': barOrigin,
  'bar-range-origin': barRangeOrigin,
  'bar-stack-horizontal-origin': barStackHorizontalOrigin,
  'bar-stack-origin': barStackOrigin,
  'bidirectional-bar-origin': bidirectionalBarOrigin,
  'bubble-map-origin': bubbleMapOrigin,
  'chart-mix-group-origin': chartMixGroupOrigin,
  'chart-mix-origin': chartMixOrigin,
  'chart-mix-stack-origin': chartMixStackOrigin,
  'chart-mix-dual-line-origin': chartMixDualLineOrigin,
  'flow-map-origin': flowMapOrigin,
  'funnel-origin': funnelOrigin,
  'gauge-origin': gaugeOrigin,
  'heat-map-origin': heatMapOrigin,
  'indicator-origin': indicatorOrigin,
  'line-origin': lineOrigin,
  'liquid-origin': liquidOrigin,
  'map-origin': mapOrigin,
  'percentage-bar-stack-horizontal-origin': percentageBarStackHorizontalOrigin,
  'percentage-bar-stack-origin': percentageBarStackOrigin,
  'pie-donut-origin': pieDonutOrigin,
  'pie-donut-rose-origin': pieDonutRoseOrigin,
  'pie-origin': pieOrigin,
  'pie-rose-origin': pieRoseOrigin,
  'progress-bar-origin': progressBarOrigin,
  'quadrant-origin': quadrantOrigin,
  'radar-origin': radarOrigin,
  'rich-text-origin': richTextOrigin,
  'sankey-origin': sankeyOrigin,
  'scatter-origin': scatterOrigin,
  'stock-line-origin': stockLineOrigin,
  'symbolic-map-origin': symbolicMapOrigin,
  'table-info-origin': tableInfoOrigin,
  'table-normal-origin': tableNormalOrigin,
  'table-pivot-origin': tablePivotOrigin,
  'treemap-origin': treemapOrigin,
  'waterfall-origin': waterfallOrigin,
  'word-cloud-origin': wordCloudOrigin,
  't-heatmap-origin': tHeatmapOrigin,
  group: group
}
const getIconName = item => {
  if (item.component === 'UserView') {
    const viewInfo = canvasViewInfo.value[item.id]
    return iconMap[`${viewInfo.type}-origin`]
  } else {
    return iconMap[item.icon]
  }
}

const menuAsideHiddenClose = (param, element) => {
  const iconDom = document.getElementById('close-button')
  if (iconDom) {
    iconDom.click()
  }
  if (param?.opt === 'rename') {
    setTimeout(() => {
      editComponentName(element)
    }, 200)
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

const areaClick = area => {
  dvMainStore.setCurComponent({ component: null, index: null })
  dvMainStore.canvasStateChange({ key: 'curPointArea', value: area })
}

const canvasChange = () => {
  snapshotStore.recordSnapshotCache()
}
</script>

<template>
  <!--为了保持图层视觉上的一致性 这里进行数组的倒序排列 相应的展示和移动按照倒序处理-->
  <div class="real-time-component-list">
    <button hidden="true" id="close-button"></button>
    <div class="layer-area" @click="areaClick('hidden')" :class="{ activated: hiddenAreaActive }">
      <span>弹窗区域({{ popComponentData.length }})</span>
      <el-switch v-model="canvasStyleData.popupAvailable" @change="canvasChange" size="small" />
    </div>
    <el-row class="list-wrap">
      <div class="list-container" @contextmenu="handleContextMenu">
        <draggable
          @end="dragOnEnd"
          :list="popComponentData"
          animation="100"
          class="drag-list"
          item-key="id"
        >
          <template #item="{ element, index }">
            <div>
              <div
                :title="element.name"
                class="component-item"
                :class="{
                  'container-item-not-show': !element.isShow || !canvasStyleData.popupAvailable,
                  activated:
                    (curComponent && curComponent?.id === element?.id) ||
                    areaData.components.includes(element)
                }"
                @click="hiddenAreaOnClick($event, element)"
              >
                <div style="width: 22px; padding-left: 3px"></div>
                <el-icon class="component-icon">
                  <Icon><component :is="getIconName(element)"></component></Icon>
                </el-icon>
                <span
                  :id="`component-label-${element?.id}`"
                  class="component-label"
                  @dblclick="editComponentName(element)"
                >
                  {{ element?.name }}
                </span>
                <div
                  v-show="!nameEdit || (nameEdit && curComponent?.id !== element?.id)"
                  class="icon-container"
                  :class="{
                    'icon-container-show': !element?.isShow
                  }"
                >
                  <el-dropdown
                    ref="dropdownMore"
                    trigger="click"
                    placement="bottom-start"
                    effect="dark"
                    :hide-timeout="0"
                  >
                    <span
                      :class="'dropdownMore-' + index"
                      @click="hiddenAreaOnClick($event, element)"
                    >
                      <el-icon class="component-base">
                        <Icon name="dv-more"><dvMore class="svg-icon opt-icon" /></Icon>
                      </el-icon>
                    </span>
                    <template #dropdown>
                      <context-menu-aside-details
                        :element="element"
                        @close="menuAsideHiddenClose($event, element)"
                      ></context-menu-aside-details>
                    </template>
                  </el-dropdown>
                </div>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </el-row>
    <div
      class="layer-area layer-screen"
      @click="areaClick('base')"
      :class="{ activated: baseAreaActive }"
    >
      <span>大屏区域({{ baseComponentData.length }})</span>
    </div>
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
                v-show="
                  getComponent(index)?.component !== 'GroupArea' &&
                  getComponent(index)?.category !== 'hidden'
                "
                :title="getComponent(index)?.name"
                class="component-item"
                :class="{
                  'container-item-not-show': !getComponent(index)?.isShow,
                  activated:
                    (curComponent && curComponent?.id === getComponent(index)?.id) ||
                    areaData.components.includes(getComponent(index))
                }"
                @click="onClick($event, transformIndex(index))"
              >
                <div style="width: 22px; padding-left: 3px">
                  <el-icon
                    v-show="getComponent(index)?.component === 'Group'"
                    class="component-expand"
                    @click="expandClick(getComponent(index))"
                  >
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
                  <Icon><component :is="getIconName(getComponent(index))"></component></Icon>
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
                    'icon-container-lock':
                      getComponent(index)?.isLock && getComponent(index)?.isShow,
                    'icon-container-show': !getComponent(index)?.isShow
                  }"
                >
                  <el-icon
                    class="component-base component-icon-display"
                    v-show="!getComponent(index).isShow"
                    @click="showComponent"
                  >
                    <Icon name="dv-eye-close"><dvEyeClose class="svg-icon opt-icon" /></Icon>
                  </el-icon>
                  <el-icon
                    class="component-base"
                    v-show="getComponent(index)?.isShow"
                    @click="hideComponent"
                  >
                    <Icon name="dv-show"><dvShow class="svg-icon opt-icon" /></Icon>
                  </el-icon>
                  <el-icon
                    v-show="!getComponent(index)?.isLock"
                    class="component-base"
                    @click="lock"
                  >
                    <Icon name="dv-unlock"><dvUnlock class="svg-icon opt-icon" /></Icon>
                  </el-icon>
                  <el-icon
                    class="component-base component-icon-display"
                    v-show="getComponent(index)?.isLock"
                    @click="unlock"
                  >
                    <Icon name="dv-lock"><dvLock class="svg-icon opt-icon" /></Icon>
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
                        <Icon name="dv-more"><dvMore class="svg-icon opt-icon" /></Icon>
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
              <div v-if="getComponent(index)?.component === 'Group' && getComponent(index)?.expand">
                <real-time-group :component-data="getComponent(index).propValue"></real-time-group>
              </div>
            </div>
          </template>
        </draggable>
        <el-row style="width: 100%; height: 150px"></el-row>
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
        padding: 0 2px 0 0px;
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
            width: 66px !important;
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

.layer-area {
  font-size: 12px;
  font-weight: bold;
  height: 36px;
  line-height: 36px;
  padding: 0 8px;
  display: flex;
  cursor: pointer;
  align-items: center;
  justify-content: space-between;
  &:hover {
    background-color: rgba(235, 235, 235, 0.1);
  }
  :deep(.ed-switch.is-checked .ed-switch__core > .ed-switch__action) {
    left: calc(100% - 12px);
  }
  :deep(span.ed-switch__core) {
    min-width: 24px;
    border: none;
    height: 6px;
    border-radius: 3px;
    .ed-switch__action {
      left: 0;
      box-shadow: 0 2px 4px rgba(31, 35, 41, 0.12);
    }
  }
}

.activated {
  background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
  color: var(--ed-color-primary);
}

.layer-screen {
  border-top: rgba(255, 255, 255, 0.15) 1px solid;
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
