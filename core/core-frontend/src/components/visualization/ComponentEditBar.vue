<template>
  <div
    class="bar-main"
    :class="[
      showEditPosition,
      {
        'bar-main-background': showPosition !== 'multiplexing'
      }
    ]"
    @click.stop
  >
    <span :title="t('visualization.enlarge')" v-if="barShowCheck('enlarge')">
      <el-icon class="bar-base-icon" @click="userViewEnlargeOpen">
        <Icon name="dv-bar-enlarge"></Icon
      ></el-icon>
    </span>
    <div v-if="barShowCheck('multiplexing')" class="bar-checkbox-area">
      <el-checkbox
        size="medium"
        v-model="state.multiplexingCheckModel"
        @change="multiplexingCheck"
      />
    </div>
    <div v-if="barShowCheck('linkage') && linkageCheckShowAttach" class="bar-checkbox-area">
      <el-checkbox size="medium" v-model="linkageInfo.linkageActive" />
    </div>
    <linkage-field
      v-if="linkageInfo && linkageInfo.linkageActive"
      :element="element"
    ></linkage-field>
    <span
      :title="t('visualization.cancel_linkage')"
      v-if="barShowCheck('unLinkage') && existLinkage"
    >
      <el-icon class="bar-base-icon" @click="clearLinkage">
        <Icon name="dv-bar-unLinkage"></Icon
      ></el-icon>
    </span>
    <div v-if="barShowCheck('batchOpt')" class="bar-checkbox-area">
      <el-checkbox size="medium" @change="batchOptChange" />
    </div>

    <el-dropdown trigger="click" v-if="barShowCheck('setting')">
      <el-icon :title="t('visualization.setting')" class="bar-base-icon"><Setting /></el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 100px">
          <el-dropdown-item icon="Delete" @click="deleteComponent">删除</el-dropdown-item>
          <el-dropdown-item icon="DocumentCopy" @click="copyComponent">复制</el-dropdown-item>
          <el-dropdown-item
            v-if="barShowCheck('linkageSetting')"
            icon="Link"
            @click="linkageSetting"
            >联动设置</el-dropdown-item
          >
          <el-dropdown-item
            v-if="barShowCheck('linkJumpSetting')"
            icon="Connection"
            @click="linkJumpSetOpen"
            >跳转设置</el-dropdown-item
          >
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts" setup>
import { computed, reactive, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { useI18n } from '@/hooks/web/useI18n'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import LinkageField from '@/components/visualization/LinkageField.vue'
import { getViewLinkageGather } from '@/api/visualization/linkage'
import { copyStoreWithOut } from '@/store/modules/data-visualization/copy'
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const copyStore = copyStoreWithOut()
const emits = defineEmits([
  'userViewEnlargeOpen',
  'closePreview',
  'showViewDetails',
  'amRemoveItem',
  'linkJumpSetOpen'
])
const { t } = useI18n()

// bar所在位置可以显示的功能按钮
const positionBarShow = {
  canvas: ['enlarge', 'setting', 'unLinkage', 'linkageSetting', 'linkJumpSetting'],
  preview: ['enlarge', 'unLinkage'],
  multiplexing: ['multiplexing'],
  batchOpt: ['batchOpt'],
  linkage: ['linkage']
}

// bar所属组件类型可以显示的功能按钮
const componentTypeBarShow = {
  UserView: [
    'enlarge',
    'setting',
    'multiplexing',
    'batchOpt',
    'linkage',
    'unLinkage',
    'linkageSetting',
    'linkJumpSetting'
  ],
  default: ['setting', 'multiplexing']
}

const barShowCheck = barName => {
  return (
    positionBarShow[showPosition.value] &&
    positionBarShow[showPosition.value].includes(barName) &&
    (componentTypeBarShow[element.value.component]
      ? componentTypeBarShow[element.value.component]
      : componentTypeBarShow['default']
    ).includes(barName)
  )
}

const linkageCheckShowAttach = computed(() => {
  return curLinkageView.value !== element.value
})

const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  active: {
    type: Boolean,
    required: false,
    default: false
  },
  index: {
    required: true,
    type: [Number, String],
    default: 0
  },
  showPosition: {
    required: false,
    type: String,
    default: 'canvas'
  }
})

const { element, active, index, showPosition } = toRefs(props)
const {
  pcMatrixCount,
  curComponent,
  componentData,
  canvasStyleData,
  linkageSettingStatus,
  targetLinkageInfo,
  curLinkageView,
  dvInfo
} = storeToRefs(dvMainStore)

const state = reactive({
  systemOS: 'Mac',
  maxImageSize: 15000000,
  boardSetVisible: false,
  linkJumpSetVisible: false,
  linkJumpSetViewId: null,
  curFields: [],
  multiplexingCheckModel: false,
  // Currently selected Multiplexing components
  curMultiplexingComponents: {},
  barWidth: 24,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom', 'custom-button'],
  timer: null,
  viewXArray: [],
  batchOptCheckModel: false
})

const showEditPosition = computed(() => {
  if (showPosition.value === 'canvas') {
    const baseLeft = element.value.x - 1
    const baseRight = pcMatrixCount.value.x - (element.value.x + element.value.sizeX - 1)
    if (baseLeft === 0 && baseRight === 0) {
      return 'bar-main-right-inner'
    } else if (baseRight === 0) {
      return 'bar-main-left-outer'
    } else {
      return 'bar-main-right'
    }
  } else {
    return 'bar-main-preview-right-inner'
  }
})

const deleteComponent = () => {
  eventBus.emit('removeMatrixItem', index.value)
  dvMainStore.setCurComponent({ component: null, index: null })
}

const copyComponent = () => {
  copyStore.copy()
  copyStore.paste(false)
}

const userViewEnlargeOpen = e => {
  e.preventDefault()
  e.stopPropagation()
  emits('userViewEnlargeOpen')
}

// 复用-Begin
const multiplexingCheck = val => {
  if (val) {
    // push
    dvMainStore.addCurMultiplexingComponent({
      component: element.value,
      componentId: element.value.id
    })
  } else {
    // remove
    dvMainStore.removeCurMultiplexingComponentWithId(element.value.id)
  }
}
// 复用-End

// 批量操作-Begin
const batchOptChange = val => {
  if (val) {
    // push
    dvMainStore.addCurBatchComponent(element.value.id)
  } else {
    // remove
    dvMainStore.removeCurBatchComponentWithId(element.value.id)
  }
}
// 批量操作-End

// 联动-Begin
const linkageSetting = () => {
  // sourceViewId 也加入查询
  const targetViewIds = componentData.value
    .filter(item => item.component === 'UserView')
    .map(item => item.id)

  // 获取当前仪表板当前视图联动信息
  const requestInfo = {
    dvId: dvInfo.value.id,
    sourceViewId: curComponent.value.id,
    targetViewIds: targetViewIds,
    linkageInfo: null
  }
  getViewLinkageGather(requestInfo).then(rsp => {
    dvMainStore.setLinkageInfo(rsp.data)
  })
}

const existLinkage = computed(() => {
  let linkageFiltersCount = 0
  componentData.value.forEach(item => {
    if (item.linkageFilters && item.linkageFilters.length > 0) {
      item.linkageFilters.forEach(linkage => {
        if (element.value.id === linkage.sourceViewId) {
          linkageFiltersCount++
        }
      })
    }
  })
  return linkageFiltersCount
})

const linkageInfo = computed(() => {
  return targetLinkageInfo.value[element.value.id]
})

// 清除相同sourceViewId 的 联动条件
const clearLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.id)
}

// 联动-End

// 跳转-Begin
const linkJumpSetOpen = () => {
  emits('linkJumpSetOpen')
}

// 跳转-End
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  z-index: 2;
  border-radius: 2px;
  cursor: pointer !important;
}
.bar-main-background {
  background-color: var(--primary, #3370ff);
}

.bar-main-right {
  width: 22px;
  right: -25px;
}

.bar-main-preview-right-inner {
  right: 0px;
}

.bar-main-right-inner {
  width: 22px;
  right: 0px;
}

.bar-main-left-outer {
  width: 22px;
  left: -25px;
}

.bar-base-icon {
  height: 22px;
  width: 22px;
  color: #ffffff;
  &:hover {
    color: rgba(255, 255, 255, 0.5);
  }
  &:active {
    color: rgba(255, 255, 255, 0.7);
  }
}

.bar-checkbox-area {
  padding: 0 3px;
  height: 22px;
  :deep(.ed-checkbox) {
    height: 22px;
  }
}
</style>
