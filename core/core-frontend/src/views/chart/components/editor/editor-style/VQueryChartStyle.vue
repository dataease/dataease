<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { toRefs } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
const { t } = useI18n()

const state = {
  styleActiveNames: []
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
                v-model="chart.customStyle.component.borderColor"
                :disabled="!chart.customStyle.component.borderShow"
                :predefine="predefineColors"
              />
              <el-input-number
                v-model="chart.customStyle.component.borderWidth"
                :min="1"
                :max="100"
                :disabled="!chart.customStyle.component.borderShow"
                controls-position="right"
              />
            </div>
            <el-checkbox v-model="chart.customStyle.component.bgColorShow">{{
              t('chart.custom_case') + t('chart.backgroundColor')
            }}</el-checkbox>
            <div class="query-collapse-item">
              <el-color-picker
                v-model="chart.customStyle.component.bgColor"
                :disabled="!chart.customStyle.component.bgColorShow"
                :predefine="predefineColors"
              />
            </div>
            <el-divider />
            <span> 展示按钮 </span>
            <div class="query-collapse-item query-component">
              <el-checkbox-group v-model="chart.customStyle.component.btnList">
                <el-checkbox label="sure"> {{ t('commons.adv_search.search') }}</el-checkbox>
                <el-checkbox label="clear"> {{ t('commons.clear') }}</el-checkbox>
                <el-checkbox label="reset">{{ t('commons.adv_search.reset') }}</el-checkbox>
              </el-checkbox-group>
            </div>
            <span>
              {{ t('chart.label_position') }}
            </span>
            <div class="query-collapse-item query-component">
              <el-radio-group v-model="chart.customStyle.component.layout" size="small">
                <el-radio-button label="horizontal">
                  <el-icon>
                    <icon name="icon_title-left-align_outlined"></icon>
                  </el-icon>
                </el-radio-button>
                <el-radio-button label="vertical">
                  <el-icon>
                    <icon name="icon_title-top-align_outlined"></icon>
                  </el-icon>
                </el-radio-button>
              </el-radio-group>
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
  border-top: 1px solid @side-outline-border-color;
}

.attr-style {
  overflow-y: auto;
  height: 100%;
  width: 100%;

  .query-collapse-style {
    :deep(.ed-collapse-item__content) {
      padding: 8px 16px;
      .query-collapse-item {
        padding: 8px 8px 0 22px;
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .ed-input-number {
          margin-left: 8px;
        }

        .ed-color-picker__mask {
          height: 30px;
          width: 30px;
        }
      }

      .query-component {
        padding: 8px 0 0 0;
        .ed-checkbox {
          margin-right: 16px;
        }
      }
    }
  }
}
</style>
