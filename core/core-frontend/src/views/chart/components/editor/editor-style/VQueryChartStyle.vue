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
  <el-row class="view-panel">
    <div class="attr-style">
      <el-row class="query-collapse-style">
        <el-collapse v-model="state.styleActiveNames" class="style-collapse">
          <collapse-switch-item
            themes="light"
            v-model="chart.customStyle.component.show"
            name="component"
            :title="t('visualization.module')"
          >
            <el-checkbox v-model="chart.customStyle.component.titleShow">{{
              t('chart.show') + t('chart.title')
            }}</el-checkbox>
            <div class="query-collapse-item">
              <el-input
                :disabled="!chart.customStyle.component.titleShow"
                v-model.lazy="chart.customStyle.component.title"
              />
            </div>
            <el-checkbox v-model="chart.customStyle.component.borderShow">{{
              t('visualization.board')
            }}</el-checkbox>
            <div class="query-collapse-item">
              <el-color-picker
                is-custom
                v-model="chart.customStyle.component.borderColor"
                :disabled="!chart.customStyle.component.borderShow"
                :predefine="predefineColors"
              />
            </div>
            <el-checkbox v-model="chart.customStyle.component.bgColorShow">{{
              t('chart.custom_case') + t('chart.backgroundColor')
            }}</el-checkbox>
            <div class="query-collapse-item">
              <el-color-picker
                is-custom
                v-model="chart.customStyle.component.bgColor"
                :disabled="!chart.customStyle.component.bgColorShow"
                :predefine="predefineColors"
              />
            </div>
            <el-divider />
            <span> 展示按钮 </span>
            <div class="query-collapse-item query-component">
              <el-checkbox-group v-model="chart.customStyle.component.btnList">
                <el-checkbox disabled label="sure">
                  {{ t('commons.adv_search.search') }}</el-checkbox
                >
                <el-checkbox label="clear"> {{ t('commons.clear') }}</el-checkbox>
                <el-checkbox label="reset">{{ t('commons.adv_search.reset') }}</el-checkbox>
              </el-checkbox-group>
            </div>
            <span>
              {{ t('chart.label_position') }}
            </span>
            <div class="query-collapse-item query-component">
              <el-icon
                :class="[
                  'layout-icon',
                  chart.customStyle.component.layout === 'vertical' && 'active'
                ]"
                @click="chart.customStyle.component.layout = 'vertical'"
              >
                <icon name="icon_title-top-align_outlined"></icon>
              </el-icon>
              <el-icon
                :class="[
                  'layout-icon',
                  chart.customStyle.component.layout === 'horizontal' && 'active'
                ]"
                @click="chart.customStyle.component.layout = 'horizontal'"
              >
                <icon name="icon_title-left-align_outlined"></icon>
              </el-icon>
            </div>
          </collapse-switch-item>
        </el-collapse>
      </el-row>
    </div>
  </el-row>
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

  .query-collapse-style {
    :deep(.ed-checkbox__label) {
      color: #1f2329;
      font-family: PingFang SC;
      font-size: 12px !important;
      font-style: normal;
      font-weight: 400;
      line-height: 20px !important;
    }

    :deep(.ed-checkbox__inner) {
      width: 14px;
      height: 14px;
      &::after {
        left: 4px;
      }
    }
    :deep(.ed-collapse-item__content) {
      padding: 16px;
      .query-collapse-item {
        padding: 8px 8px 0 22px;
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        --ed-component-size: 28px;

        .ed-input-number {
          margin-left: 8px;
        }

        .ed-checkbox__input.is-disabled.is-checked .ed-checkbox__inner {
          background-color: var(--ed-color-primary-light-5);
          color: var(--ed-color-primary-light-5);
        }
      }

      .query-component {
        padding: 8px 0 0 0;

        .layout-icon {
          width: 24px;
          height: 24px;
          font-size: 16px;
          color: #1f2329;
          cursor: pointer;

          & + .layout-icon {
            margin-left: 8px;
          }
          &.active {
            color: #3370ff;
            border-radius: 4px;
            background: rgba(51, 112, 255, 0.1);
          }
        }
        .ed-checkbox {
          margin-right: 16px;
        }
      }
    }
  }
}
</style>
