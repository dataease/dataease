<template>
  <div class="bar-main" :class="showEditPosition">
    <span :title="t('visualization.enlarge')" v-if="barShowCheck('enlarge')">
      <el-icon class="base-icon" @click="userViewEnlargeOpen">
        <Icon name="dv-bar-enlarge"></Icon
      ></el-icon>
    </span>

    <el-dropdown trigger="click" v-if="barShowCheck('setting')">
      <el-icon :title="t('visualization.setting')" class="base-icon"><Setting /></el-icon>
      <template #dropdown>
        <el-dropdown-menu style="width: 100px">
          <el-dropdown-item icon="Delete" @click="deleteComponent">删除</el-dropdown-item>
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
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const emits = defineEmits([
  'userViewEnlargeOpen',
  'closePreview',
  'showViewDetails',
  'amRemoveItem'
])
const { t } = useI18n()

const positionBarShow = {
  canvas: ['enlarge', 'setting'],
  preview: ['enlarge']
}

const barShowCheck = barName => {
  return (
    positionBarShow[showPosition.value] && positionBarShow[showPosition.value].includes(barName)
  )
}

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
const { pcMatrixCount, curComponent, componentData, canvasStyleData } = storeToRefs(dvMainStore)

const state = reactive({
  systemOS: 'Mac',
  maxImageSize: 15000000,
  boardSetVisible: false,
  linkJumpSetVisible: false,
  linkJumpSetViewId: null,
  curFields: [],
  multiplexingCheckModel: false,
  barWidth: 24,
  componentType: null,
  linkageActiveStatus: false,
  editFilter: ['view', 'custom', 'custom-button'],
  timer: null,
  viewXArray: []
})

const showEditPosition = computed(() => {
  if (showPosition.value === 'preview') {
    return 'bar-main-preview-right-inner'
  } else {
    const baseLeft = element.value.x - 1
    const baseRight = pcMatrixCount.value.x - (element.value.x + element.value.sizeX - 1)
    if (baseLeft === 0 && baseRight === 0) {
      return 'bar-main-right-inner'
    } else if (baseRight === 0) {
      return 'bar-main-left-outer'
    } else {
      return 'bar-main-right'
    }
  }
})

const deleteComponent = () => {
  eventBus.emit('removeMatrixItem', index.value)
}

const userViewEnlargeOpen = e => {
  e.preventDefault()
  e.stopPropagation()
  emits('userViewEnlargeOpen')
}
</script>

<style lang="less" scoped>
.bar-main {
  position: absolute;
  float: right;
  z-index: 2;
  border-radius: 2px;
  cursor: pointer !important;
  background-color: var(--primary, #3370ff);
}

.bar-main-right {
  width: 22px;
  right: -25px;
}

.bar-main-preview-right-inner {
  height: 22px;
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

.base-icon {
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
</style>
