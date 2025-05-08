<script lang="ts" setup>
import { computed, nextTick, PropType, ref, toRefs } from 'vue'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import Icon from '../icon-custom/src/Icon.vue'
const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse } = storeToRefs(dvMainStore)
let componentNameEdit = ref(false)
let inputComponentName = ref({ id: null, name: null })
let componentNameInputAttr = ref(null)
import dvInfoSvg from '@/assets/svg/dv-info.svg'
import { useI18n } from '@/hooks/web/useI18n'
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const props = defineProps({
  element: {
    required: false,
    default: {}
  },
  scrollWidth: {
    required: false,
    type: Number,
    default: 6
  },
  width: {
    required: false,
    type: Number,
    default: 200
  },
  themeInfo: {
    required: false,
    type: String,
    default: 'dark'
  },
  asidePosition: {
    required: false,
    type: String,
    default: 'left'
  },
  sideName: {
    required: false,
    type: String,
    default: 'defaultSide'
  },

  view: {
    type: Object as PropType<ChartObj>,
    required: false
  },
  slideIndex: {
    type: Number,
    required: false,
    default: 0
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'light'
  },
  title: String
})

const { width, asidePosition, sideName, themeInfo, view, themes, element } = toRefs(props)
const collapseChange = () => {
  canvasCollapse.value[sideName.value] = !canvasCollapse.value[sideName.value]
}
const widthShow = computed(() => `${canvasCollapse.value[sideName.value] ? 36 : width.value}px`)

const isViewTitle = computed(() => view.value && ['picture-group'].includes(view.value.type))

const slideStyle = computed(() => {
  return { '--de-scroll-width': props.scrollWidth + 'px', width: widthShow.value }
})

const closeEditComponentName = () => {
  componentNameEdit.value = false
  if (props.element.id !== inputComponentName.value.id) {
    return
  }
  if (!inputComponentName.value.name || !inputComponentName.value.name.trim()) {
    return
  }
  if (
    inputComponentName.value.name.trim() === view.value?.title ||
    inputComponentName.value.name.trim() === element.value.name
  ) {
    return
  }
  if (
    inputComponentName.value.name.trim().length > 64 ||
    inputComponentName.value.name.trim().length < 2
  ) {
    ElMessage.warning('名称字段长度2-64个字符')
    editComponentName()
    return
  }
  element.value.label = inputComponentName.value.name
  element.value.name = inputComponentName.value.name
  if (isViewTitle.value) {
    view.value.title = inputComponentName.value.name
  }
  inputComponentName.value.name = ''
}

const editComponentName = () => {
  if (element.value?.id || view.value?.id) {
    componentNameEdit.value = true
    if (isViewTitle.value) {
      inputComponentName.value.name = view.value.title
      inputComponentName.value.id = view.value.id
    } else {
      inputComponentName.value.name = element.value.name
      inputComponentName.value.id = element.value.id
    }
    nextTick(() => {
      componentNameInputAttr.value.focus()
    })
  }
}

const onComponentNameChange = () => {
  snapshotStore.recordSnapshotCache('onComponentNameChange')
}
</script>

<template>
  <div
    @keyup.stop
    @keydown.stop
    class="dv-aside"
    :class="['aside-' + asidePosition + '-' + themeInfo, 'aside-area-' + themeInfo]"
    :style="slideStyle"
  >
    <el-row align="middle" :class="'title-' + themeInfo" justify="space-between">
      <div
        :id="'attr-slide-component-name' + slideIndex"
        v-if="!canvasCollapse[sideName]"
        class="name-area-attr"
        style="max-width: 180px; text-overflow: ellipsis; white-space: nowrap"
        :style="{ width: componentNameEdit ? '300px' : 'auto' }"
        :class="{ 'component-name-dark': themeInfo === 'dark' }"
        @dblclick="editComponentName"
      >
        {{ isViewTitle ? view.title : title }}
        <el-popover
          show-arrow
          :offset="8"
          :effect="themes"
          placement="bottom"
          width="200"
          trigger="click"
        >
          <template #reference>
            <span>
              <el-icon
                v-show="element && element['id']"
                style="margin: 2px 0 0 4px; cursor: pointer"
                ><Icon><dvInfoSvg class="svg-icon" /></Icon
              ></el-icon>
            </span>
          </template>
          <div style="margin-bottom: 4px; font-size: 14px">
            {{ t('visualization.component_id') }}
          </div>
          <div style="font-size: 14px">
            {{ element['id'] }}
          </div>
        </el-popover>
      </div>
      <el-icon
        :title="title"
        :class="['custom-icon-' + themeInfo, 'collapse-icon-' + themeInfo]"
        size="20px"
        @click="collapseChange"
      >
        <Expand
          v-if="
            (canvasCollapse[sideName] && asidePosition === 'left') ||
            (!canvasCollapse[sideName] && asidePosition === 'right')
          "
        />
        <Fold v-else />
      </el-icon>
    </el-row>
    <div class="main-content" v-if="!canvasCollapse[sideName]">
      <el-scrollbar>
        <slot />
      </el-scrollbar>
    </div>
    <div class="collapse-title" v-if="canvasCollapse[sideName]">
      <span>{{ title }}</span>
    </div>
    <Teleport v-if="componentNameEdit" :to="'#attr-slide-component-name' + slideIndex">
      <input
        ref="componentNameInputAttr"
        v-model="inputComponentName.name"
        width="100%"
        :effect="themeInfo"
        @change="onComponentNameChange"
        @blur="closeEditComponentName"
      />
    </Teleport>
  </div>
</template>

<style lang="less" scoped>
.aside-area-dark {
  color: @dv-canvas-main-font-color;
  background-color: @side-area-background;
}

.aside-area-light {
  color: @canvas-main-font-color-light;
  background-color: #fff;
  :deep(.title) {
    border-bottom: #d5d6d8 1px solid !important;
  }
}
.dv-aside {
  position: relative;
  transition: 0.5s;
  .title-dark {
    font-size: 14px;
    font-weight: 500;
    border-bottom: rgba(255, 255, 255, 0.15) 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .title-light {
    font-size: 14px;
    font-weight: 500;
    border-bottom: #d5d6d8 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .collapse-title {
    width: 35px;
    font-size: 14px;
    font-weight: 500;
    text-align: center;
    padding: 5px;
    margin-top: 5px;
    span {
      writing-mode: vertical-rl;
      text-orientation: mixed;
    }
  }
  .main-content {
    height: calc(100% - 45px);
    overflow-y: auto;
  }
  .custom-icon {
    position: absolute;
    right: 5px;
    top: 12px;
    cursor: pointer;
  }

  :deep(.ed-scrollbar__bar) {
    width: var(--de-scroll-width);
  }

  :deep(.ed-scrollbar__thumb) {
    width: var(--de-scroll-width);
  }
}
.aside-left-dark {
  border-right: @side-outline-border-color 1px solid;
}
.aside-right-dark {
  border-left: @side-outline-border-color 1px solid;
}

.aside-left-light {
  border-right: @side-outline-border-color-light 1px solid;
}
.aside-right-light {
  border-left: @side-outline-border-color-light 1px solid;
}
.ed-collapse {
  border-top: 0;
}
:deep(.ed-collapse) {
  border-top: unset;
}

:deep(.ed-collapse-item.ed-collapse--dark .ed-collapse-item__header) {
  border-color: rgba(255, 255, 255, 0.15);
  border-top: unset;
}

.collapse-icon-light {
  color: #646a73;
  cursor: pointer;
}

.collapse-icon-dark {
  color: @canvas-main-font-color-dark;
  cursor: pointer;
}

.ed-scrollbar__bar.is-vertical {
  width: var(--de-scroll-width);
}

.name-area-attr {
  position: relative;
  line-height: 24px;
  height: 24px;
  font-size: 14px !important;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  input {
    position: absolute;
    left: 0;
    width: 100%;
    outline: none;
    border: 1px solid #295acc;
    border-radius: 4px;
    padding: 0 4px;
    height: 100%;
  }
}

.component-name-dark {
  input {
    position: absolute;
    left: 0;
    width: 100%;
    color: @dv-canvas-main-font-color;
    background-color: #050e21;
    outline: none;
    border: 1px solid #295acc;
    border-radius: 4px;
    padding: 0 4px;
    height: 100%;
  }
}
</style>
