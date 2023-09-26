<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { toRefs } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
const { t } = useI18n()

const state = {
  styleActiveNames: ['component']
}

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})
const predefineColors = COLOR_PANEL

const { chart } = toRefs(props)
</script>

<template>
  <div class="attr-style">
    <el-row class="de-collapse-style">
      <el-collapse v-model="state.styleActiveNames" class="style-collapse">
        <collapse-switch-item
          themes="light"
          v-model="chart.customStyle.component.show"
          name="component"
          :title="t('visualization.module')"
        >
          <el-form label-position="top" :disabled="!chart.customStyle.component.show">
            <el-form-item class="form-item margin-bottom-8">
              <el-checkbox size="small" v-model="chart.customStyle.component.titleShow">
                {{ t('chart.show') + t('chart.title') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item class="form-item" style="padding-left: 20px">
              <el-input
                :disabled="!chart.customStyle.component.titleShow"
                v-model.lazy="chart.customStyle.component.title"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8">
              <el-checkbox size="small" v-model="chart.customStyle.component.borderShow">
                {{ t('visualization.board') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item class="form-item" style="padding-left: 20px">
              <el-color-picker
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.borderColor"
                :disabled="!chart.customStyle.component.borderShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8">
              <el-checkbox size="small" v-model="chart.customStyle.component.bgColorShow">
                {{ t('chart.custom_case') + t('chart.backgroundColor') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item class="form-item" style="padding-left: 20px">
              <el-color-picker
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.bgColor"
                :disabled="!chart.customStyle.component.bgColorShow"
                :predefine="predefineColors"
              />
            </el-form-item>

            <el-divider class="m-divider" />
            <el-form-item class="form-item" label="展示按钮">
              <el-checkbox-group v-model="chart.customStyle.component.btnList">
                <el-checkbox size="small" disabled label="sure">
                  {{ t('commons.adv_search.search') }}
                </el-checkbox>
                <el-checkbox size="small" label="clear">
                  {{ t('commons.clear') }}
                </el-checkbox>
                <el-checkbox size="small" label="reset">
                  {{ t('commons.adv_search.reset') }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item class="form-item" :label="t('chart.label_position')">
              <el-radio-group class="icon-radio-group" v-model="chart.customStyle.component.layout">
                <el-radio label="vertical">
                  <el-tooltip effect="dark" placement="top">
                    <template #content>
                      {{ t('chart.text_pos_top') }}
                    </template>
                    <div
                      class="icon-btn"
                      :class="{ active: chart.customStyle.component.layout === 'vertical' }"
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
                      :class="{ active: chart.customStyle.component.layout === 'horizontal' }"
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
        </collapse-switch-item>
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
      color: #3370ff;
      background-color: rgba(51, 112, 255, 0.1);
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }

  &.active {
    color: #3370ff;
    background-color: rgba(51, 112, 255, 0.1);
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
