<template>
  <div
    v-if="state.tabShow"
    style="width: 100%; height: 100%"
    :class="[
      headClass,
      `ed-tabs-${curThemes}`,
      { 'title-hidde-tab': !showTabTitleFlag },
      { 'title-show-tab': showTabTitleFlag }
    ]"
    class="custom-tabs-head"
    ref="tabComponentRef"
  >
    <de-custom-tab
      v-model="element.editableTabsValue"
      @tab-add="addTab"
      :addable="isEditMode"
      :font-color="fontColor"
      :active-color="activeColor"
      :border-color="noBorderColor"
      :border-active-color="borderActiveColor"
      :hide-title="!showTabTitleFlag"
    >
      <template :key="tabItem.name" v-for="tabItem in element.propValue">
        <el-tab-pane
          class="el-tab-pane-custom"
          :lazy="isEditMode"
          :label="tabItem.title"
          :name="tabItem.name"
        >
          <template #label>
            <div class="custom-tab-title" @mousedown.stop>
              <span class="title-inner" :style="titleStyle(tabItem.name)"
                >{{ tabItem.title }}
                <Board
                  v-show="svgInnerActiveEnable(tabItem.name)"
                  :style="{
                    color: element.titleBackground.active.innerImageColor,
                    pointerEvents: 'none'
                  }"
                  :name="titleBackgroundActiveSvgInner"
                ></Board>

                <Board
                  v-show="svgInnerInActiveEnable(tabItem.name)"
                  :style="{
                    color: element.titleBackground.inActive.innerImageColor,
                    pointerEvents: 'none'
                  }"
                  :name="titleBackgroundInActiveSvgInner"
                ></Board>
                <span v-if="isEditMode">
                  <el-dropdown
                    popper-class="custom-de-tab-dropdown"
                    :effect="curThemes"
                    trigger="click"
                    @command="handleCommand"
                  >
                    <span class="el-dropdown-link">
                      <el-icon v-if="isEdit"><ArrowDown /></el-icon>
                    </span>
                    <template #dropdown>
                      <el-dropdown-menu :style="{ 'font-family': fontFamily }">
                        <el-dropdown-item :command="beforeHandleCommand('editTitle', tabItem)">
                          {{ t('visualization.edit_title') }}
                        </el-dropdown-item>
                        <el-dropdown-item :command="beforeHandleCommand('copyCur', tabItem)">
                          {{ t('visualization.copy') }}
                        </el-dropdown-item>
                        <el-dropdown-item
                          v-if="element.propValue.length > 1"
                          :command="beforeHandleCommand('deleteCur', tabItem)"
                        >
                          {{ t('visualization.delete') }}
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </span>
              </span>
            </div>
          </template>
        </el-tab-pane>
      </template>
      <div
        class="tab-content-custom"
        :key="tabItem.name + '-content'"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave"
        v-for="(tabItem, index) in element.propValue"
        :class="{ 'switch-hidden': element.editableTabsValue !== tabItem.name }"
      >
        <de-canvas
          v-if="isEdit && !mobileInPc"
          :ref="'tabCanvas_' + index"
          :component-data="tabItem.componentData"
          :canvas-style-data="canvasStyleData"
          :canvas-view-info="canvasViewInfo"
          :canvas-id="element.id + '--' + tabItem.name"
          :class="moveActive ? 'canvas-move-in' : ''"
          :canvas-position="'tab'"
          :canvas-active="element.editableTabsValue === tabItem.name"
          :font-family="fontFamily"
        ></de-canvas>
        <de-preview
          v-else
          :ref="'dashboardPreview'"
          :dv-info="dvInfo"
          :cur-gap="curPreviewGap"
          :component-data="tabItem.componentData"
          :canvas-style-data="{}"
          :canvas-view-info="canvasViewInfo"
          :canvas-id="element.id + '--' + tabItem.name"
          :preview-active="element.editableTabsValue === tabItem.name"
          :show-position="showPosition"
          :outer-scale="scale"
          :font-family="fontFamily"
          :outer-search-count="searchCount"
        ></de-preview>
      </div>
    </de-custom-tab>
    <el-dialog
      title="编辑标题"
      :append-to-body="true"
      v-model="state.dialogVisible"
      width="30%"
      :show-close="false"
      :close-on-click-modal="false"
      center
    >
      <el-input
        v-model="state.textarea"
        maxlength="50"
        :placeholder="$t('dataset.input_content')"
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="state.dialogVisible = false">取消</el-button>
          <el-button :disabled="!titleValid" type="primary" @click="sureCurTitle">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  computed,
  getCurrentInstance,
  nextTick,
  onBeforeMount,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  toRefs,
  watch
} from 'vue'
import DeCanvas from '@/views/canvas/DeCanvas.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { guid } from '@/views/visualized/data/dataset/form/util'
import eventBus from '@/utils/eventBus'
import { canvasChangeAdaptor, findComponentIndexById, isDashboard } from '@/utils/canvasUtils'
import DeCustomTab from '@/custom-component/de-tabs/DeCustomTab.vue'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { getPanelAllLinkageInfo } from '@/api/visualization/linkage'
import { dataVTabComponentAdd, groupSizeStyleAdaptor } from '@/utils/style'
import { deepCopyTabItemHelper } from '@/store/modules/data-visualization/copy'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useI18n } from '@/hooks/web/useI18n'
import { imgUrlTrans } from '@/utils/imgUtils'
import Board from '@/components/de-board/Board.vue'
import ChartCarouselTooltip from '@/views/chart/components/js/g2plot_tooltip_carousel'
import { debounce } from 'lodash-es'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const { tabMoveInActiveId, bashMatrixInfo, editMode, mobileInPc } = storeToRefs(dvMainStore)
const tabComponentRef = ref(null)
let carouselTimer = null
const { t } = useI18n()

const props = defineProps({
  canvasStyleData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  dvInfo: {
    type: Object,
    required: true
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: []
      }
    }
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  // 仪表板字体
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  }
})
const {
  element,
  isEdit,
  showPosition,
  canvasStyleData,
  canvasViewInfo,
  dvInfo,
  scale,
  searchCount
} = toRefs(props)

const titleBackgroundActiveSvgInner = computed(() => {
  return element.value.titleBackground.active.innerImage.replace('board/', '').replace('.svg', '')
})

const titleBackgroundInActiveSvgInner = computed(() => {
  return element.value.titleBackground.inActive.innerImage.replace('board/', '').replace('.svg', '')
})

const svgInnerInActiveEnable = itemName => {
  const { backgroundImageEnable, backgroundType, innerImage } =
    element.value.titleBackground.inActive
  return (
    element.value.editableTabsValue !== itemName &&
    !element.value.titleBackground.multiply &&
    element.value.titleBackground?.enable &&
    backgroundImageEnable &&
    backgroundType === 'innerImage' &&
    typeof innerImage === 'string'
  )
}

const svgInnerActiveEnable = itemName => {
  const { backgroundImageEnable, backgroundType, innerImage } = element.value.titleBackground.active
  return (
    (element.value.editableTabsValue === itemName || element.value.titleBackground.multiply) &&
    element.value.titleBackground?.enable &&
    backgroundImageEnable &&
    backgroundType === 'innerImage' &&
    typeof innerImage === 'string'
  )
}

// tooltips 轮播会影响tab 展示
const viewToolTipsChange = () => {
  element.value.propValue?.forEach(tabItem => {
    const tMethod =
      element.value.editableTabsValue === tabItem.name
        ? ChartCarouselTooltip.resume
        : ChartCarouselTooltip.paused
    tabItem.componentData?.forEach(componentItem => {
      tMethod(componentItem.id)
      if (componentItem.component === 'Group')
        componentItem.propValue.forEach(groupItem => {
          tMethod(groupItem.id)
        })
    })
  })
}

const handleMouseEnter = () => {
  state.hoverFlag = true
}

const handleMouseLeave = () => {
  state.hoverFlag = false
}
const state = reactive({
  activeTabName: '',
  curItem: {},
  textarea: '',
  dialogVisible: false,
  tabShow: true,
  hoverFlag: false
})
const tabsAreaScroll = ref(false)

// 无边框
const noBorderColor = ref('none')
let currentInstance

const showTabTitleFlag = computed(() => {
  if (element.value && element.value.style && element.value.style?.showTabTitle === false) {
    return false
  } else {
    return element.value.style?.showTabTitle
  }
})

const isEditMode = computed(() => editMode.value === 'edit' && isEdit.value && !mobileInPc.value)
const curThemes = isDashboard() ? 'light' : 'dark'
const calcTabLength = () => {
  setTimeout(() => {
    if (element.value.propValue.length > 1) {
      const containerDom = document.getElementById(
        'tab-' + element.value.propValue[element.value.propValue.length - 1].name
      )
      tabsAreaScroll.value =
        containerDom.parentNode.clientWidth > tabComponentRef.value.clientWidth - 100
    } else {
      tabsAreaScroll.value = false
    }
  })
}

const beforeHandleCommand = (item, param) => {
  return {
    command: item,
    param: param
  }
}
const curPreviewGap = computed(() =>
  dvInfo.value.type === 'dashboard' && canvasStyleData.value['dashboard'].gap === 'yes'
    ? canvasStyleData.value['dashboard'].gapSize
    : 0
)

function sureCurTitle() {
  state.curItem.title = state.textarea
  state.dialogVisible = false
  snapshotStore.recordSnapshotCache('sureCurTitle')
}

function addTab() {
  const newName = guid()
  const newTab = {
    name: newName,
    title: t('visualization.new_tab'),
    componentData: [],
    closable: true
  }
  element.value.propValue.push(newTab)
  element.value.editableTabsValue = newTab.name
  snapshotStore.recordSnapshotCache('addTab')
}

function deleteCur(param) {
  state.curItem = param
  let len = element.value.propValue.length
  while (len--) {
    if (element.value.propValue[len].name === param.name) {
      element.value.propValue.splice(len, 1)
      const activeIndex =
        (len - 1 + element.value.propValue.length) % element.value.propValue.length
      element.value.editableTabsValue = element.value.propValue[activeIndex].name
      state.tabShow = false
      nextTick(() => {
        state.tabShow = true
      })
    }
  }
}
function copyCur(param) {
  addTab()
  const newTabItem = element.value.propValue[element.value.propValue.length - 1]
  const idMap = {}
  const newCanvasId = element.value.id + '--' + newTabItem.name
  newTabItem.componentData = deepCopyTabItemHelper(newCanvasId, param.componentData, idMap)
  dvMainStore.updateCopyCanvasView(idMap)
}

function editCurTitle(param) {
  state.activeTabName = param.name
  state.curItem = param
  state.textarea = param.title
  state.dialogVisible = true
}

function handleCommand(command) {
  switch (command.command) {
    case 'editTitle':
      editCurTitle(command.param)
      break
    case 'deleteCur':
      deleteCur(command.param)
      snapshotStore.recordSnapshotCache('deleteCur')
      break
    case 'copyCur':
      copyCur(command.param)
      snapshotStore.recordSnapshotCache('copyCur')
      break
  }
}

const reloadLinkage = () => {
  // 刷新联动信息
  if (dvInfo.value.id) {
    const resourceTable = ['canvas', 'canvasDataV', 'edit'].includes(showPosition.value)
      ? 'snapshot'
      : 'core'
    getPanelAllLinkageInfo(dvInfo.value.id, resourceTable).then(rsp => {
      dvMainStore.setNowPanelTrackInfo(rsp.data)
    })
  }
}

const componentMoveIn = component => {
  element.value.propValue.forEach((tabItem, index) => {
    if (element.value.editableTabsValue === tabItem.name) {
      //获取主画布当前组件的index
      if (isDashboard()) {
        eventBus.emit('removeMatrixItemById-canvas-main', component.id)
        dvMainStore.setCurComponent({ component: null, index: null })
        component.canvasId = element.value.id + '--' + tabItem.name
        const refInstance = currentInstance.refs['tabCanvas_' + index][0]
        if (refInstance) {
          const matrixBase = refInstance.getBaseMatrixSize() //矩阵基础大小
          canvasChangeAdaptor(component, matrixBase)
          component.x = 1
          component.y = 200
          component.style.left = 0
          component.style.top = 0
          tabItem.componentData.push(component)
          refInstance.addItemBox(component) //在适当的时候初始化布局组件
          nextTick(() => {
            refInstance.canvasInitImmediately()
          })
        }
      } else {
        const curIndex = findComponentIndexById(component.id)
        // 从主画布删除
        dvMainStore.deleteComponent(curIndex)
        dvMainStore.setCurComponent({ component: null, index: null })
        component.canvasId = element.value.id + '--' + tabItem.name
        dataVTabComponentAdd(component, element.value)
        tabItem.componentData.push(component)
      }
    }
  })

  reloadLinkage()
}

const componentMoveOut = component => {
  if (isDashboard()) {
    canvasChangeAdaptor(component, bashMatrixInfo.value, true)
  }
  // 从Tab画布中移除
  eventBus.emit('removeMatrixItemById-' + component.canvasId, component.id)
  dvMainStore.setCurComponent({ component: null, index: null })
  // 主画布中添加
  if (isDashboard()) {
    eventBus.emit('moveOutFromTab-canvas-main', component)
  } else {
    addToMain(component)
  }
  reloadLinkage()
}

const addToMain = component => {
  const { left, top } = element.value.style
  component.style.left = component.style.left + left
  component.style.top = component.style.top + top
  component.canvasId = 'canvas-main'
  dvMainStore.addComponent({
    component,
    index: undefined,
    isFromGroup: true
  })
}

const moveActive = computed(() => {
  return tabMoveInActiveId.value && tabMoveInActiveId.value === element.value.id
})

const headClass = computed(() => {
  if (tabsAreaScroll.value) {
    return 'tab-head-left'
  } else {
    return 'tab-head-' + element.value.style.headHorizontalPosition
  }
})

const backgroundStyle = backgroundParams => {
  if (backgroundParams) {
    const {
      backdropFilterEnable,
      backdropFilter,
      backgroundColorSelect,
      backgroundColor,
      backgroundImageEnable,
      backgroundType,
      outerImage,
      innerPadding,
      borderRadius
    } = backgroundParams
    let style = {
      padding: innerPadding * scale.value + 'px',
      borderRadius: borderRadius + 'px'
    }
    let colorRGBA = ''
    if (backgroundColorSelect && backgroundColor) {
      colorRGBA = backgroundColor
    }
    if (backgroundImageEnable) {
      if (backgroundType === 'outerImage' && typeof outerImage === 'string') {
        style['background'] = `url(${imgUrlTrans(outerImage)}) no-repeat ${colorRGBA}`
      } else {
        style['background-color'] = colorRGBA
      }
    } else {
      style['background-color'] = colorRGBA
    }
    if (backdropFilterEnable) {
      style['backdrop-filter'] = 'blur(' + backdropFilter + 'px)'
    }
    return style
  }
  return {}
}

const titleStyle = itemName => {
  let style = {}
  if (element.value.editableTabsValue === itemName) {
    style = {
      textDecoration: element.value.style.textDecoration,
      fontStyle: element.value.style.fontStyle,
      fontWeight: element.value.style.fontWeight,
      fontSize: (element.value.style.activeFontSize || 18) + 'px',
      lineHeight: (element.value.style.activeFontSize || 18) + 'px'
    }
    if (element.value.titleBackground?.enable) {
      style = {
        ...style,
        ...backgroundStyle(element.value.titleBackground.active)
      }
    }
  } else {
    style = {
      textDecoration: element.value.style.textDecoration,
      fontStyle: element.value.style.fontStyle,
      fontWeight: element.value.style.fontWeight,
      fontSize: (element.value.style.fontSize || 16) + 'px',
      lineHeight: (element.value.style.fontSize || 16) + 'px'
    }
    if (element.value.titleBackground?.enable) {
      style = {
        ...style,
        ...backgroundStyle(
          element.value.titleBackground.multiply
            ? element.value.titleBackground.active
            : element.value.titleBackground.inActive
        )
      }
    }
  }
  return style
}

const fontColor = computed(() => {
  if (
    element.value &&
    element.value.style &&
    element.value.style.headFontColor &&
    typeof element.value.style.headFontColor === 'string'
  ) {
    return element.value.style.headFontColor
  } else {
    return 'none'
  }
})

const activeColor = computed(() => {
  if (
    element.value &&
    element.value.style &&
    element.value.style.headFontActiveColor &&
    typeof element.value.style.headFontActiveColor === 'string'
  ) {
    return element.value.style.headFontActiveColor
  } else {
    return 'none'
  }
})

const borderActiveColor = computed(() => {
  if (
    element.value &&
    element.value.style &&
    element.value.style.headBorderActiveColor &&
    typeof element.value.style.headBorderActiveColor === 'string'
  ) {
    return element.value.style.headBorderActiveColor
  } else {
    return 'none'
  }
})

const titleValid = computed(() => {
  return !!state.textarea && !!state.textarea.trim()
})

const viewToolTipsChangeDebounce = debounce(() => {
  viewToolTipsChange()
}, 500)

watch(
  () => scale.value,
  () => {
    viewToolTipsChangeDebounce()
  }
)

watch(
  () => element.value,
  () => {
    calcTabLength()
    viewToolTipsChangeDebounce()
  },
  { deep: true }
)

const reShow = () => {
  state.tabShow = false
  nextTick(() => {
    state.tabShow = true
  })
}

watch(
  () => isEditMode.value,
  () => {
    initCarousel()
  }
)

const initCarousel = () => {
  carouselTimer && clearInterval(carouselTimer)
  carouselTimer = null
  if (!isEditMode.value) {
    if (element.value.carousel?.enable) {
      const switchTime = (element.value.carousel.time || 5) * 1000
      let switchCount = 1
      // 轮播定时器
      carouselTimer = setInterval(() => {
        // 鼠标移入时 停止轮播
        if (!state.hoverFlag) {
          const nowIndex = switchCount % element.value.propValue.length
          switchCount++
          nextTick(() => {
            element.value.editableTabsValue = element.value.propValue[nowIndex].name
          })
        }
      }, switchTime)
    }
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', viewToolTipsChange)
  if (element.value.propValue.length > 0) {
    element.value.editableTabsValue = element.value.propValue[0].name
  }
  calcTabLength()
  if (['canvas', 'canvasDataV', 'edit'].includes(showPosition.value) && !mobileInPc.value) {
    eventBus.on('onTabMoveIn-' + element.value.id, componentMoveIn)
    eventBus.on('onTabMoveOut-' + element.value.id, componentMoveOut)
    eventBus.on('onTabSortChange-' + element.value.id, reShow)
  }
  currentInstance = getCurrentInstance()
  initCarousel()
  nextTick(() => {
    groupSizeStyleAdaptor(element.value)
  })
  setTimeout(() => {
    viewToolTipsChange()
  }, 1000)
})
onBeforeUnmount(() => {
  document.removeEventListener('visibilitychange', viewToolTipsChange)
  if (['canvas', 'canvasDataV', 'edit'].includes(showPosition.value) && !mobileInPc.value) {
    eventBus.off('onTabMoveIn-' + element.value.id, componentMoveIn)
    eventBus.off('onTabMoveOut-' + element.value.id, componentMoveOut)
    eventBus.off('onTabSortChange-' + element.value.id, reShow)
  }
})
onBeforeMount(() => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
    carouselTimer = null
  }
})
</script>
<style lang="less" scoped>
.title-hidde-tab {
  :deep(.ed-tabs__content) {
    height: 100% !important;
  }
}

.title-show-tab {
  :deep(.ed-tabs__content) {
    height: calc(100% - 46px) !important;
  }
  :deep(.ed-tabs__item) {
    font-family: inherit;
    padding-right: 0 !important;
  }
}

.ed-tabs-dark {
  :deep(.ed-tabs__new-tab) {
    margin-right: 25px;
    color: #fff;
  }
  :deep(.el-dropdown-link) {
    color: #fff;
  }
}
.ed-tabs-light {
  :deep(.ed-tabs__new-tab) {
    margin-right: 25px;
    background-color: #fff;
  }
}
.el-tab-pane-custom {
  width: 100%;
}
.canvas-move-in {
  border: 2px dotted transparent;
  border-color: blueviolet;
}

.tab-head-left :deep(.ed-tabs__nav-scroll) {
  display: flex;
  justify-content: flex-start;
}

.tab-head-right :deep(.ed-tabs__nav-scroll) {
  display: flex;
  justify-content: flex-end;
}

.tab-head-center :deep(.ed-tabs__nav-scroll) {
  display: flex;
  justify-content: center;
}

.switch-hidden {
  opacity: 0;
  z-index: -1;
}

.tab-content-custom {
  position: absolute;
  width: 100%;
  height: 100%;
}
.custom-tab-title {
  .title-inner {
    position: relative;
    background-size: 100% 100% !important;
  }
  :deep(.ed-dropdown) {
    vertical-align: middle !important;
  }
}
</style>
