<script lang="tsx" setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, COLOR_CASES } from '@/views/chart/components/editor/util/chart'
import { ElPopover } from 'element-plus-secondary'
import { getMapColorCases } from '@/views/chart/components/js/util'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    modelValue: {
      basicStyleForm: ChartBasicStyle
      customColor: any
      colorIndex: number
    }
    propertyInner: Array<string>
  }>(),
  {
    themes: 'light'
  }
)
const colorCases = JSON.parse(JSON.stringify(COLOR_CASES))
const predefineColors = JSON.parse(JSON.stringify(COLOR_PANEL))

const emits = defineEmits(['update:modelValue', 'selectColorCase'])
const state = computed({
  get() {
    return props.modelValue
  },
  set(v) {
    emits('update:modelValue', v)
  }
})

const form = reactive({
  value: null,
  activeName: 'simple',
  enableCustom: false,
  tabPanes: [
    {
      label: t('chart.page_pager_general'),
      name: 'simple',
      data: JSON.parse(JSON.stringify(colorCases))
    },
    {
      label: t('chart.gradient'),
      name: 'split_gradient',
      data: JSON.parse(JSON.stringify(getMapColorCases(colorCases)))
    }
  ]
})
const scrollToSelected = () => {
  const index = form.activeName === 'simple' ? 0 : 1
  const parents = document.getElementById('color-tab-content-' + index)
  if (!parents) return
  const items = parents.getElementsByClassName('color-div-base selected')
  if (items && items.length) {
    const top = items[0].offsetTop || 0
    parents.scrollTo(0, top)
  }
}

const handleClick = () => {
  form.enableCustom = false
  nextTick(() => {
    scrollToSelected()
  })
  const widget = ref['de-color-picker']
  if (!widget) return
  if (Array.isArray(widget)) {
    widget.forEach(item => {
      item.triggerHide && item.triggerHide()
    })
    return
  }
  widget.triggerHide && widget.triggerHide()
}

const selectNode = option => {
  state.value.basicStyleForm.colors = option.colors
  state.value.basicStyleForm.colorScheme = option.value
  emits('selectColorCase', option)
}

const colorCaseSelectorRef = ref<InstanceType<typeof ElPopover>>()

const _popoverShow = ref(false)
function onPopoverShow() {
  _popoverShow.value = true
}
function onPopoverHide() {
  _popoverShow.value = false
}
onMounted(() => {
  form.activeName = state.value.basicStyleForm.colorScheme.endsWith('_split_gradient')
    ? 'split_gradient'
    : 'simple'
})
</script>

<template>
  <el-popover
    placement="bottom-start"
    ref="colorCaseSelectorRef"
    width="268"
    :offset="4"
    trigger="click"
    :persistent="false"
    :show-arrow="false"
    @show="onPopoverShow"
    @hide="onPopoverHide"
    :popper-style="{ padding: 0 }"
    :effect="themes"
  >
    <template #reference>
      <el-input :effect="themes" readonly class="custom-color-selector">
        <template #prefix>
          <div class="custom-color-selector-container">
            <div
              v-for="(c, index) in state.basicStyleForm.colors"
              :key="index"
              :style="{
                flex: 1,
                height: '100%',
                backgroundColor: c
              }"
            ></div>
          </div>
        </template>
        <template #suffix>
          <el-icon class="input-arrow-icon" :class="{ reverse: _popoverShow }">
            <ArrowDown />
          </el-icon>
        </template>
      </el-input>
    </template>
    <template #default>
      <el-tabs v-model="form.activeName" class="tab-header" @tab-click="handleClick">
        <el-tab-pane
          class="padding-tab"
          v-for="(pane, i) in form.tabPanes"
          :key="i"
          :label="pane.label"
          :name="pane.name"
        >
          <div class="pane_content">
            <el-scrollbar
              max-height="274px"
              class="cases-list"
              :class="{ dark: 'dark' === themes }"
            >
              <div
                v-for="option in pane.data"
                :key="option.value"
                class="select-color-item"
                :class="{ active: state.basicStyleForm.colorScheme === option.value }"
                @click="selectNode(option)"
              >
                <div style="float: left">
                  <span
                    v-for="(c, index) in option.colors"
                    :key="index"
                    :style="{
                      width: '20px',
                      height: '20px',
                      float: 'left',
                      backgroundColor: c
                    }"
                  />
                </div>
                <span class="cases-list__text">{{ option.name }}</span>
              </div>
            </el-scrollbar>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </el-popover>
</template>

<style scoped lang="less">
.custom-color-selector {
  :deep(.ed-input__prefix) {
    width: calc(100% - 22px);
    .ed-input__prefix-inner {
      width: 100%;
    }
  }
  :deep(.ed-input__wrapper) {
    cursor: pointer;
  }
  .custom-color-selector-container {
    border-radius: 2px;
    overflow: hidden;
    width: 100%;
    height: 16px;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-items: center;
    justify-content: space-evenly;
  }
}
.cases-list {
  margin: 6px 0;

  .select-color-item {
    width: 100%;

    font-size: var(--ed-font-size-base);
    padding: 0 32px 0 20px;
    position: relative;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: var(--ed-text-color-regular);
    height: 34px;
    line-height: 34px;
    box-sizing: border-box;
    cursor: pointer;

    display: flex;
    align-items: center;

    &:hover {
      background-color: var(--ed-fill-color-light);
    }

    &.active {
      color: var(--ed-color-primary);
      font-weight: 500;
    }
  }

  &.dark {
    .select-color-item {
      color: #ebebeb;
      &:hover {
        background-color: rgba(235, 235, 235, 0.1);
      }
    }
  }

  .cases-list__text {
    margin-left: 4px;
  }
}
.tab-header {
  --ed-tabs-header-height: 34px;
  --custom-tab-color: #646a73;

  :deep(.ed-tabs__nav-wrap::after) {
    background-color: unset;
  }

  &.dark {
    --custom-tab-color: #a6a6a6;
  }

  height: 100%;
  :deep(.ed-tabs__item) {
    font-weight: 400;
    font-size: 12px;
    padding: 0 8px !important;
    margin-right: 12px;
    color: var(--custom-tab-color);
  }
  :deep(.is-active) {
    font-weight: 500;
    color: var(--ed-color-primary, #3370ff);
  }

  :deep(.ed-tabs__nav-scroll) {
    padding-left: 0 !important;
  }

  :deep(.ed-tabs__header) {
    margin: 0 !important;
  }

  :deep(.ed-tabs__content) {
    height: calc(100% - 35px);
    overflow-y: auto;
    overflow-x: hidden;
  }
}
.padding-tab {
  padding: 0;
  height: 100%;
  width: 100%;
  display: flex;

  :deep(.ed-scrollbar) {
    &.has-footer {
      height: calc(100% - 81px);
    }
  }

  :deep(.ed-footer) {
    padding: 0;
    height: 114px;
  }
}
</style>
