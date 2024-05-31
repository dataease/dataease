<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { PropType, toRefs } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
const { t } = useI18n()

const state = {
  styleActiveNames: ['component']
}

const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  chart: {
    type: Object,
    required: true
  },
  commonBackgroundPop: {
    type: Object,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})
const predefineColors = COLOR_PANEL

const { element, chart, commonBackgroundPop } = toRefs(props)
</script>

<template>
  <div class="attr-style">
    <el-row class="de-collapse-style">
      <el-collapse v-model="state.styleActiveNames" class="style-collapse">
        <el-collapse-item :effect="themes" name="component" :title="t('visualization.module')">
          <el-form label-position="top">
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.titleShow"
              >
                {{ t('chart.show') + t('chart.title') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-input
                :effect="themes"
                :disabled="!chart.customStyle.component.titleShow"
                v-model.lazy="chart.customStyle.component.title"
              />
            </el-form-item>
            <el-form-item
              label="标题颜色"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                v-model="chart.customStyle.component.titleColor"
                :trigger-width="204"
                :disabled="!chart.customStyle.component.titleShow"
                is-custom
                :predefine="COLOR_PANEL"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="commonBackgroundPop.backgroundColorSelect"
              >
                自定义组件背景
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="commonBackgroundPop.backgroundColor"
                :disabled="!commonBackgroundPop.backgroundColorSelect"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.labelColorShow"
              >
                标签颜色
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.labelColor"
                :disabled="!chart.customStyle.component.labelColorShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.borderShow"
              >
                {{ t('visualization.board') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.borderColor"
                :disabled="!chart.customStyle.component.borderShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.textColorShow"
              >
                提示文字颜色
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.text"
                :disabled="!chart.customStyle.component.textColorShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.bgColorShow"
              >
                自定义查询条件背景
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.bgColor"
                :disabled="!chart.customStyle.component.bgColorShow"
                :predefine="predefineColors"
              />
            </el-form-item>

            <el-divider :effect="themes" class="m-divider" />
            <el-form-item
              :effect="themes"
              class="form-item"
              label="展示按钮"
              :class="'form-item-' + themes"
            >
              <el-checkbox-group :effect="themes" v-model="chart.customStyle.component.btnList">
                <el-checkbox :effect="themes" size="small" disabled label="sure">
                  {{ t('commons.adv_search.search') }}
                </el-checkbox>
                <el-checkbox :effect="themes" size="small" label="clear">
                  {{ t('commons.clear') }}
                </el-checkbox>
                <el-checkbox :effect="themes" size="small" label="reset">
                  {{ t('commons.adv_search.reset') }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('chart.label_position')"
              :class="'form-item-' + themes"
            >
              <el-radio-group
                :effect="themes"
                class="icon-radio-group"
                v-model="chart.customStyle.component.layout"
              >
                <el-radio :effect="themes" label="vertical">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_top') }}
                    </template>
                    <div
                      class="icon-btn"
                      :class="{
                        dark: themes === 'dark',
                        active: chart.customStyle.component.layout === 'vertical'
                      }"
                    >
                      <el-icon>
                        <Icon name="icon_title-top-align_outlined" />
                      </el-icon>
                    </div>
                  </el-tooltip>
                </el-radio>

                <el-radio label="horizontal">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_left') }}
                    </template>
                    <div
                      class="icon-btn"
                      :class="{
                        dark: themes === 'dark',
                        active: chart.customStyle.component.layout === 'horizontal'
                      }"
                    >
                      <el-icon>
                        <Icon name="icon_title-left-align_outlined" />
                      </el-icon>
                    </div>
                  </el-tooltip>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.view-panel {
  display: flex;
  height: 100%;
  width: 100%;
}

.attr-style {
  overflow-y: auto;
  height: 100%;
  width: 100%;
}

.form-item {
  margin-bottom: 16px;

  &.margin-bottom-8 {
    margin-bottom: 8px !important;
  }
  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 8px;
}

:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @dv-canvas-main-font-color !important;
  }

  &.select-append {
    .ed-input-group__append {
      background-color: transparent;
    }
    .dv-dark {
      & > .ed-input__wrapper {
        background-color: #1a1a1a;
      }
      .ed-input-group__append .ed-select {
        margin: 0 -20px;
      }
    }
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

.is-disabled {
  .icon-btn {
    color: #8f959e;
    cursor: not-allowed;

    &:hover {
      background-color: inherit;
    }

    &.active {
      background-color: #f5f7fa;
      &:hover {
        background-color: #f5f7fa;
      }
    }
    &.dark {
      color: #5f5f5f;
      &.active {
        background-color: #373737;
        &:hover {
          background-color: #373737;
        }
      }
    }
  }
}
.icon-radio-group {
  :deep(.ed-radio) {
    margin-right: 8px;

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
</style>
