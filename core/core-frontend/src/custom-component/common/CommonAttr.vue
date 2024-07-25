<script setup lang="ts">
import { computed, nextTick, onMounted, ref, toRefs } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { styleData, selectKey, optionMap, horizontalPosition } from '@/utils/attr'
import ComponentPosition from '@/components/visualization/common/ComponentPosition.vue'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { useI18n } from '@/hooks/web/useI18n'
import elementResizeDetectorMaker from 'element-resize-detector'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import CommonStyleSet from '@/custom-component/common/CommonStyleSet.vue'
import CommonEvent from '@/custom-component/common/CommonEvent.vue'
const snapshotStore = snapshotStoreWithOut()

const { t } = useI18n()
const emits = defineEmits(['onAttrChange'])

const props = withDefaults(
  defineProps<{
    type?: 'light' | 'dark'
    themes?: EditorTheme
    element: any
    showStyle: boolean
    backgroundColorPickerWidth?: number
    backgroundBorderSelectWidth?: number
  }>(),
  {
    showStyle: true,
    themes: 'dark',
    backgroundColorPickerWidth: 50,
    backgroundBorderSelectWidth: 108
  }
)

const { themes, element, showStyle } = toRefs(props)
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, batchOptStatus } = storeToRefs(dvMainStore)
const activeName = ref(element.value.collapseName)

const styleKeys = computed(() => {
  if (element.value) {
    const curComponentStyleKeys = Object.keys(element.value.style)
    return styleData.filter(item => curComponentStyleKeys.includes(item.key))
  } else {
    return []
  }
})

const onChange = () => {
  element.value.collapseName = activeName
}

const isIncludesColor = str => {
  return str.toLowerCase().includes('color')
}

const positionComponentShow = computed(() => {
  return !batchOptStatus.value && !dashboardActive.value
})

const dashboardActive = computed(() => {
  return dvInfo.value.type === 'dashboard'
})

const onBackgroundChange = val => {
  element.value.commonBackground = val
  emits('onAttrChange', { custom: 'commonBackground' })
}

const onStyleAttrChange = ({ key, value }) => {
  snapshotStore.recordSnapshotCache()
  emits('onAttrChange', { custom: 'style', property: key, value: value })
}

const containerRef = ref()
const containerWidth = ref()

const colSpan = computed(() => {
  if (containerWidth.value <= 240) {
    return 24
  } else {
    return 12
  }
})

const colorPickerWidth = computed(() => {
  if (containerWidth.value <= 280) {
    return 125
  } else if (containerWidth.value <= 240) {
    return 108
  } else {
    return 197
  }
})

// 暂时关闭
const eventsShow = computed(() => {
  return false
  // return !dashboardActive.value && ['Picture'].includes(element.value.component)
})

const backgroundCustomShow = computed(() => {
  return (
    dashboardActive.value ||
    (!dashboardActive.value &&
      !['CanvasBoard', 'CanvasIcon', 'Picture', 'CircleShape', 'RectShape'].includes(
        element.value.component
      ))
  )
})
onMounted(() => {
  const erd = elementResizeDetectorMaker()
  containerWidth.value = containerRef.value?.offsetWidth
  erd.listenTo(containerRef.value, () => {
    nextTick(() => {
      containerWidth.value = containerRef.value?.offsetWidth
    })
  })
})
const stopEvent = e => {
  if (e && e.code === 'Enter') {
    e.stopPropagation()
    e.preventDefault()
  }
}
</script>

<template>
  <div class="v-common-attr" ref="containerRef">
    <el-collapse v-model="activeName" @change="onChange()">
      <el-collapse-item :effect="themes" title="位置" name="position" v-if="positionComponentShow">
        <component-position :themes="themes" />
      </el-collapse-item>

      <el-collapse-item
        :effect="themes"
        title="背景"
        name="background"
        v-if="element && backgroundCustomShow"
      >
        <background-overall-common
          :themes="themes"
          :common-background-pop="element.commonBackground"
          component-position="component"
          @onBackgroundChange="onBackgroundChange"
          :background-color-picker-width="backgroundColorPickerWidth"
          :background-border-select-width="backgroundBorderSelectWidth"
        />
      </el-collapse-item>
      <slot></slot>
      <el-collapse-item
        v-if="element && element.style"
        :effect="themes"
        title="样式"
        name="style"
        class="common-style-area"
      >
        <common-style-set
          @onStyleAttrChange="onStyleAttrChange"
          :themes="themes"
          :element="element"
        ></common-style-set>
      </el-collapse-item>
      <el-collapse-item
        v-if="element && element.events && eventsShow"
        :effect="themes"
        title="事件"
        name="style"
        class="common-style-area"
      >
        <common-event :themes="themes" :element="element"></common-event>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<style lang="less" scoped>
.v-common-attr {
  .ed-input-group__prepend {
    padding: 0 10px;
  }
  :deep(.ed-collapse-item__content) {
    border-top: none;
  }

  :deep(.ed-collapse-item__header) {
    height: 36px !important;
    line-height: 36px !important;
    font-size: 12px !important;
    padding: 0 !important;
    font-weight: 500 !important;

    .ed-collapse-item__arrow {
      margin: 0 6px 0 8px;
    }
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px 8px 0;
    border: none;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}

:deep(.ed-collapse-item) {
  &:first-child {
    .ed-collapse-item__header {
      border-top: none;
    }
  }
}

:deep(.ed-collapse) {
  width: 100%;
}

.attr-custom-icon-main {
  padding-top: 4px;
  width: 30px;
  overflow: hidden;
  text-align: right;
}

.attr-custom-icon {
  font-size: 16px;
  color: #646a73;
  margin-right: 5px;
}

.common-style-inner {
  width: 100%;
  min-width: 230px;
  margin-left: -12px;
}

.bash-icon {
  width: 24px;
  height: 24px;
}

.custom-color {
  margin-left: 4px;
}

:deep(.ed-color-picker.is-custom .ed-color-picker__mask) {
  height: 26px;
  width: 48px;
}

:deep(.ed-form-item) {
  .ed-radio.ed-radio--small .ed-radio__inner {
    width: 14px;
    height: 14px;
  }
  .ed-input__inner {
    font-size: 12px;
    font-weight: 400;
  }
  .ed-input {
    --ed-input-height: 28px;

    .ed-input__suffix {
      height: 26px;
    }
  }
  .ed-input-number {
    width: 100%;

    .ed-input-number__decrease {
      --ed-input-number-controls-height: 13px;
    }
    .ed-input-number__increase {
      --ed-input-number-controls-height: 13px;
    }

    .ed-input__inner {
      text-align: start;
    }
  }
  .ed-select {
    width: 100%;
    .ed-input__inner {
      height: 26px !important;
    }
  }
  .ed-checkbox {
    .ed-checkbox__label {
      font-size: 12px;
    }
  }
  .ed-color-picker {
    .ed-color-picker__mask {
      height: 26px;
      width: calc(100% - 2px) !important;
    }
  }
  .ed-radio {
    height: 20px;
    .ed-radio__label {
      font-size: 12px;
      font-style: normal;
      font-weight: 400;
      line-height: 20px;
    }
  }
}
:deep(.ed-checkbox__label) {
  color: #1f2329;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
}
:deep(.ed-checkbox--dark) {
  .ed-checkbox__label {
    color: @dv-canvas-main-font-color;
  }
}

:deep(.ed-form-item__label) {
  color: @canvas-main-font-color;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
}
:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @canvas-main-font-color-dark;
  }
}

.icon-radio-group {
  :deep(.ed-radio) {
    margin-right: 8px;
    height: 28px;

    &:last-child {
      margin-right: 0;
    }
  }
  :deep(.ed-radio__input) {
    display: none;
  }
  :deep(.ed-radio__label) {
    padding: 0;
  }
}
.icon-btn {
  font-size: 16px;
  line-height: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 4px;
  padding-top: 4px;

  color: #1f2329;

  cursor: pointer;

  &.dark {
    color: #a6a6a6;
    &.active {
      color: var(--ed-color-primary);
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }

  &.active {
    color: var(--ed-color-primary);
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
  }
}
</style>
