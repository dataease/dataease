<template>
  <div style="width: 100%; height: 100%" :class="headClass">
    <de-custom-tab
      v-model="editableTabsValue"
      @tab-add="addTab"
      :addable="isEdit"
      :font-color="fontColor"
      :active-color="activeColor"
      :border-color="noBorderColor"
      :border-active-color="borderActiveColor"
    >
      <el-tab-pane
        class="el-tab-pane-custom"
        :lazy="true"
        :key="tabItem.name"
        v-for="(tabItem, index) in element.propValue"
        :label="tabItem.title"
        :name="tabItem.name"
      >
        <template #label>
          <span :style="titleStyle(tabItem.name)">{{ tabItem.title }}</span>
          <el-dropdown
            v-if="dropdownShow"
            style="line-height: 4 !important"
            trigger="click"
            @command="handleCommand"
          >
            <span class="el-dropdown-link">
              <el-icon v-if="isEdit"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="beforeHandleCommand('editTitle', tabItem)">
                  编辑标题
                </el-dropdown-item>

                <el-dropdown-item
                  v-if="element.propValue.length > 1"
                  :command="beforeHandleCommand('deleteCur', tabItem)"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <de-canvas
          v-if="isEdit"
          :ref="'tabCanvas_' + index"
          :component-data="tabItem.componentData"
          :canvas-style-data="canvasStyleData"
          :canvas-view-info="canvasViewInfo"
          :canvas-id="element.id + '--' + tabItem.name"
          :class="moveActive ? 'canvas-move-in' : ''"
          :canvas-active="editableTabsValue === tabItem.name"
        ></de-canvas>
        <de-preview
          v-else
          :ref="'dashboardPreview'"
          :dv-info="dvInfo"
          :cur-gap="curPreviewGap"
          :component-data="tabItem.componentData"
          :canvas-style-data="canvasStyleData"
          :canvas-view-info="canvasViewInfo"
          :canvas-id="element.id + '--' + tabItem.name"
          :preview-active="editableTabsValue === tabItem.name"
          :show-position="showPosition"
        ></de-preview>
      </el-tab-pane>
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
        maxlength="10"
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
import { canvasChangeAdaptor, findComponentIndexById } from '@/utils/canvasUtils'
import DeCustomTab from '@/custom-component/de-tabs/DeCustomTab.vue'
import { useI18n } from '@/hooks/web/useI18n'
import DePreview from '@/components/data-visualization/canvas/DePreview.vue'
import { useEmitt } from '@/hooks/web/useEmitt'
const dvMainStore = dvMainStoreWithOut()
const { curComponent, tabMoveInActiveId, bashMatrixInfo } = storeToRefs(dvMainStore)
const tabCanvas = ref(null)
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
  }
})
const { element, isEdit, showPosition, canvasStyleData, canvasViewInfo, dvInfo } = toRefs(props)

const state = reactive({
  activeTabName: '',
  curItem: {},
  textarea: '',
  dialogVisible: false
})
const curItem = ref(null)
const tabsAreaScroll = ref(false)
const editableTabsValue = ref(null)

// 无边框
const noBorderColor = ref('none')
let currentInstance

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
}

function addTab() {
  const newName = guid()
  const newTab = {
    name: newName,
    title: '新建Tab',
    componentData: [],
    closable: true
  }
  element.value.propValue.push(newTab)
  editableTabsValue.value = newTab.name
}

function deleteCur(param) {
  state.curItem = param
  let len = element.value.propValue.length
  while (len--) {
    if (element.value.propValue[len].name === param.name) {
      element.value.propValue.splice(len, 1)
      const activeIndex =
        (len - 1 + element.value.propValue.length) % element.value.propValue.length
      editableTabsValue.value = element.value.propValue[activeIndex].name
    }
  }
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
      break
  }
}
function removeTab(targetName: string) {
  let tabs = element.value.propValue
  let activeName = editableTabsValue.value
  if (activeName === targetName) {
    tabs.forEach((tab: any, index: number) => {
      if (tab.name === targetName) {
        let nextTab = tabs[index + 1] || tabs[index - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
    })
  }

  editableTabsValue.value = activeName
  element.value.propValue = tabs.filter((tab: any) => tab.name !== targetName)
}

const componentMoveIn = component => {
  element.value.propValue.forEach((tabItem, index) => {
    if (editableTabsValue.value === tabItem.name) {
      //获取主画布当前组件的index
      const curIndex = findComponentIndexById(component.id)
      // 从主画布中移除
      eventBus.emit('removeMatrixItem-canvas-main', curIndex)
      dvMainStore.setCurComponent({ component: null, index: null })
      component.canvasId = element.value.id + '--' + tabItem.name
      const refInstance = currentInstance.refs['tabCanvas_' + index][0]
      const matrixBase = refInstance.getBaseMatrixSize() //矩阵基础大小
      canvasChangeAdaptor(component, matrixBase)
      tabItem.componentData.push(component)
      nextTick(() => {
        component.x = 1
        component.y = 1
        component.style.left = 0
        component.style.top = 0
        refInstance.addItemBox(component) //在适当的时候初始化布局组件
      })
    }
  })
}

const componentMoveOut = component => {
  canvasChangeAdaptor(component, bashMatrixInfo.value, true)
  // 从Tab画布中移除
  eventBus.emit('removeMatrixItemById-' + component.canvasId, component.id)
  dvMainStore.setCurComponent({ component: null, index: null })
  // 主画布中添加
  eventBus.emit('moveOutFromTab-canvas-main', component)
}

const moveActive = computed(() => {
  return tabMoveInActiveId.value && tabMoveInActiveId.value === element.value.id
})

const dropdownShow = computed(() => {
  return isEdit.value
})

const headClass = computed(() => {
  if (tabsAreaScroll.value) {
    return 'tab-head-left'
  } else {
    return 'tab-head-' + element.value.style.headHorizontalPosition
  }
})

const calcTabLength = () => {
  console.log('calcTabLength')
}

const titleStyle = itemName => {
  if (editableTabsValue.value === itemName) {
    return {
      fontSize: (element.value.style.activeFontSize || 18) + 'px'
    }
  } else {
    return {
      fontSize: (element.value.style.fontSize || 16) + 'px'
    }
  }
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

const borderColor = computed(() => {
  if (
    element.value &&
    element.value.style &&
    element.value.style.headBorderColor &&
    typeof element.value.style.headBorderColor === 'string'
  ) {
    return element.value.style.headBorderColor
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

const isCurrentEdit = computed(() => {
  return isEdit.value && curComponent.value && curComponent.value.id === element.value.id
})

watch(
  () => element.value,
  () => {
    calcTabLength()
  },
  { deep: true }
)

watch(
  () => editableTabsValue.value,
  () => {
    nextTick(() => {
      useEmitt().emitter.emit('tabCanvasChange-' + activeCanvasId.value)
    })
  },
  { deep: true }
)

const activeCanvasId = computed(() => {
  return element.value.id + '--' + editableTabsValue.value
})

onMounted(() => {
  if (element.value.propValue.length > 0) {
    editableTabsValue.value = element.value.propValue[0].name
  }
  calcTabLength()
  eventBus.on('onTabMoveIn-' + element.value.id, componentMoveIn)
  eventBus.on('onTabMoveOut-' + element.value.id, componentMoveOut)
  currentInstance = getCurrentInstance()
})
</script>
<style lang="less" scoped>
:deep(.ed-tabs__content) {
  height: calc(100% - 46px) !important;
}
:deep(.ed-tabs__new-tab) {
  margin-right: 25px;
  background-color: #fff;
}
.el-tab-pane-custom {
  width: 100%;
  height: 100%;
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
</style>
