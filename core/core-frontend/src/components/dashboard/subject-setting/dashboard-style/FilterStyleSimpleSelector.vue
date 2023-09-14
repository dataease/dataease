<template>
  <div style="width: 100%; padding-bottom: 8px">
    <el-form size="small" style="width: 100%; padding-bottom: 8px">
      <div style="width: 100%; padding: 16px 8px 0">
        <el-row :gutter="8">
          <el-col :span="12">
            <el-form-item label="标题水平位置" class="form-item">
              <el-radio-group v-model="filterForm.horizontal" @change="themeChange('horizontal')">
                <el-radio label="left">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_left') }}
                    </template>
                    <el-icon class="bash-icon">
                      <Icon name="icon_left-align_outlined" />
                    </el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio :disabled="filterForm.vertical === 'center'" label="center">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_center') }}
                    </template>
                    <el-icon class="bash-icon">
                      <Icon name="icon_horizontal-align_outlined" />
                    </el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio label="right">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_right') }}
                    </template>
                    <el-icon class="bash-icon">
                      <Icon name="icon_right-align_outlined" />
                    </el-icon>
                  </el-tooltip>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标题显示位置" class="form-item">
              <el-radio-group v-model="filterForm.vertical" @change="themeChange('vertical')">
                <el-radio label="top">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_top') }}
                    </template>
                    <el-icon class="bash-icon">
                      <Icon name="icon_title-top-align_outlined" />
                    </el-icon>
                  </el-tooltip>
                </el-radio>
                <el-radio :disabled="filterForm.horizontal === 'center'" label="center">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_center') }}
                    </template>
                    <el-icon class="bash-icon">
                      <Icon name="icon_title-left-align_outlined" />
                    </el-icon>
                  </el-tooltip>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标题颜色" class="form-item">
              <el-color-picker
                v-model="filterForm.color"
                class="color-picker-style"
                size="small"
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
              <el-form-item :label="t('visualization.board')" class="form-item">
                <el-color-picker
                  v-model="filterForm.brColor"
                  class="color-picker-style"
                  size="small"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('brColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('visualization.text')" class="form-item">
                <el-color-picker
                  v-model="filterForm.wordColor"
                  class="color-picker-style"
                  size="small"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('wordColor')"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('visualization.board_background')" class="form-item">
                <el-color-picker
                  v-model="filterForm.innerBgColor"
                  class="color-picker-style"
                  size="small"
                  is-custom
                  :predefine="state.predefineColors"
                  @change="themeChange('innerBgColor')"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { adaptCurThemeFilterStyleAll } from '@/utils/canvasStyle'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { ElIcon } from 'element-plus-secondary'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const emits = defineEmits(['onTextChange'])
const filterForm = computed(() => dvMainStore.canvasStyleData.component.filterStyle)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  // do
}
const themeChange = styleKey => {
  adaptCurThemeFilterStyleAll(styleKey)
  snapshotStore.recordSnapshot('filterStyleSimpleSelector-themeChange')
}

onMounted(() => {
  eventBus.on('onThemeColorChange', initForm)
})
</script>

<style scoped lang="less">
.custom-item-text {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 20px;
  color: #646a73 !important;
}

:deep(.ed-radio) {
  margin-right: 0;
  .ed-radio__label {
    padding: 0 8px 0 0;
  }
}
:deep(.ed-radio-group) {
  padding-top: 2px;
}

:deep(.ed-radio.is-checked) {
  .ed-radio__label {
    .bash-icon {
      background: rgba(51, 112, 255, 0.1);
      border-radius: 4px;
    }
  }
}

:deep(.ed-radio__input) {
  display: none;
}

:deep(.ed-radio__input.is-checked) {
  .ed-radio__inner {
    padding: 4px;
    background-color: green;
    background-clip: content-box;
  }
}

.bash-icon {
  width: 24px;
  height: 24px;
}

.custom-divider {
  margin: 5px 0 0 8px;
  height: 20px;
  width: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}

.inner-type-text {
  font-weight: 500;
  font-size: 12px;
  margin-bottom: 8px;
}

.bash-icon {
  font-size: 16px;
}
:deep(.ed-radio.is-checked .ed-radio__label .bash-icon) {
  background: rgba(51, 112, 255, 0.1);
  color: #3370ff;
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 16px 0 8px;
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
  flex-direction: column;
  margin-bottom: 8px;
}
:deep(.ed-form-item__label) {
  color: #646a73;
}
</style>
