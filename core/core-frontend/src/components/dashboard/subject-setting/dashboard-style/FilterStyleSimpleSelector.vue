<template>
  <div style="width: 100%; padding-bottom: 8px">
    <el-form label-position="top" style="width: 100%">
      <div style="width: 100%; padding: 16px 8px 0">
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item class="form-item" label="标题水平位置">
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_left') }}
                </template>
                <el-icon
                  :class="filterStyle.titleLayout === 'left' ? 'active' : 'cbd'"
                  @click="handleHorizontalChange('left')"
                  class="hover-icon"
                >
                  <Icon name="icon_left-align_outlined" />
                </el-icon>
              </el-tooltip>
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_center') }}
                </template>
                <el-icon
                  :class="filterStyle.titleLayout === 'center' && 'active'"
                  @click="handleHorizontalChange('center')"
                  class="hover-icon"
                >
                  <Icon name="icon_horizontal-align_outlined" />
                </el-icon>
              </el-tooltip>
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_right') }}
                </template>
                <el-icon
                  :class="filterStyle.titleLayout === 'right' && 'active'"
                  @click="handleHorizontalChange('right')"
                  class="hover-icon"
                >
                  <Icon name="icon_right-align_outlined" />
                </el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item class="form-item" label="标题显示位置">
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_top') }}
                </template>
                <el-icon
                  :class="filterStyle.layout === 'vertical' && 'active'"
                  @click="handleHorizontalChange('vertical', 'layout')"
                  class="hover-icon"
                >
                  <Icon name="icon_title-top-align_outlined" />
                </el-icon>
              </el-tooltip>
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_center') }}
                </template>
                <el-icon
                  :class="filterStyle.layout === 'horizontal' && 'active'"
                  @click="handleHorizontalChange('horizontal', 'layout')"
                  class="hover-icon"
                >
                  <Icon name="icon_title-left-align_outlined" />
                </el-icon>
              </el-tooltip>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item class="form-item" label="标题颜色">
              <el-color-picker
                v-model="filterStyle.color"
                :trigger-width="197"
                is-custom
                :predefine="state.predefineColors"
                @change="themeChange('color')"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider class="m-divider"></el-divider>
      </div>

      <el-collapse-item
        title="输入框样式"
        name="input_style_filter_selector"
        class="inner-collapse"
      >
        <div style="padding: 0 8px 8px">
          <el-row :gutter="8">
            <el-col :span="12">
              <el-form-item class="form-item" :label="t('visualization.board')">
                <el-color-picker
                  v-model="filterStyle.borderColor"
                  :trigger-width="197"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('borderColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item class="form-item" :label="t('visualization.text')">
                <el-color-picker
                  v-model="filterStyle.text"
                  :trigger-width="197"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('text')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item class="form-item" :label="t('visualization.board_background')">
                <el-color-picker
                  v-model="filterStyle.bgColor"
                  :trigger-width="197"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('bgColor')"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-collapse-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, toRefs } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep } from 'lodash-es'
import { adaptCurThemeFilterStyleAll } from '@/utils/canvasStyle'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { ElIcon } from 'element-plus-secondary'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const emits = defineEmits(['onTextChange'])
const { filterStyle } = toRefs(dvMainStore.canvasStyleData.component)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  // do
}
const themeChange = styleKey => {
  dvMainStore.canvasStyleData.component.filterStyle = cloneDeep(filterStyle.value)
  adaptCurThemeFilterStyleAll(styleKey)
  snapshotStore.recordSnapshot('filterStyleSimpleSelector-themeChange')
}

const handleHorizontalChange = (type, horizontal = 'titleLayout') => {
  filterStyle.value[horizontal] = type
  themeChange(horizontal)
}

onMounted(() => {
  eventBus.on('onThemeColorChange', initForm)
})
</script>

<style scoped lang="less">
.hover-icon {
  &.active {
    color: #3370ff !important;
    background-color: rgba(51, 112, 255, 0.1);
  }
  & + & {
    margin-left: 8px;
  }
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 8px;
}
.inner-collapse {
  :deep(.ed-collapse-item__header) {
    background-color: transparent !important;
  }
  :deep(.ed-collapse-item__header) {
    border: none;
  }
  :deep(.ed-collapse-item__wrap) {
    border: none;
  }
}
.ed-form-item {
  margin-bottom: 16px;

  :deep(.ed-form-item__label) {
    color: #646a73;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
  }
}
</style>
