<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import FunctionCfg from '@/views/chart/components/editor/editor-senior/components/FunctionCfg.vue'
import ScrollCfg from '@/views/chart/components/editor/editor-senior/components/ScrollCfg.vue'
import AssistLine from '@/views/chart/components/editor/editor-senior/components/AssistLine.vue'
import Threshold from '@/views/chart/components/editor/editor-senior/components/Threshold.vue'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { toRefs } from 'vue'

const { t } = useI18n()

const state = {
  attrActiveNames: [],
  styleActiveNames: []
}

const emit = defineEmits([
  'onFunctionCfgChange',
  'onAssistLineChange',
  'onScrollCfgChange',
  'onThresholdChange'
])

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { chart, themes } = toRefs(props)

const onFunctionCfgChange = val => {
  emit('onFunctionCfgChange', val)
}

const onAssistLineChange = val => {
  emit('onAssistLineChange', val)
}

const onScrollCfgChange = val => {
  emit('onScrollCfgChange', val)
}

const onThresholdChange = val => {
  emit('onThresholdChange', val)
}
</script>

<template>
  <el-row class="view-panel" :class="'senior-' + themes">
    <div class="attr-style">
      <el-row class="de-collapse-style">
        <el-collapse
          v-if="
            props.chart.type?.includes('bar') ||
            props.chart.type?.includes('line') ||
            props.chart.type?.includes('area') ||
            props.chart.type?.includes('table') ||
            props.chart.type?.includes('text') ||
            props.chart.type?.includes('label') ||
            props.chart.type?.includes('gauge')
          "
          v-model="state.attrActiveNames"
          class="style-collapse"
        >
          <collapse-switch-item
            v-if="
              props.chart.type?.includes('bar') ||
              props.chart.type?.includes('line') ||
              props.chart.type?.includes('area')
            "
            name="function"
            :title="t('chart.function_cfg')"
            v-model="chart.senior.functionCfg.sliderShow"
            :change-model="chart.senior.functionCfg"
            @modelChange="onFunctionCfgChange"
          >
            <function-cfg
              :themes="themes"
              :chart="props.chart"
              @onFunctionCfgChange="onFunctionCfgChange"
            />
          </collapse-switch-item>

          <el-collapse-item
            v-if="
              props.chart.type?.includes('bar') ||
              props.chart.type?.includes('line') ||
              props.chart.type?.includes('area')
            "
            name="analyse"
            :title="t('chart.assist_line')"
          >
            <assist-line
              :chart="props.chart"
              :themes="themes"
              :quota-data="props.quotaData"
              @onAssistLineChange="onAssistLineChange"
            />
          </el-collapse-item>

          <el-collapse-item
            v-if="props.chart.type?.includes('table')"
            name="scroll"
            :title="t('chart.scroll_cfg')"
          >
            <scroll-cfg
              :themes="themes"
              :chart="props.chart"
              @onScrollCfgChange="onScrollCfgChange"
            />
          </el-collapse-item>

          <el-collapse-item
            v-if="
              props.chart.type?.includes('table') ||
              props.chart.type?.includes('text') ||
              props.chart.type?.includes('label') ||
              props.chart.type?.includes('gauge')
            "
            name="threshold"
            :title="t('chart.threshold')"
          >
            <threshold
              :themes="themes"
              :chart="props.chart"
              @onThresholdChange="onThresholdChange"
            />
          </el-collapse-item>
        </el-collapse>
        <div v-else class="no-senior">
          {{ t('chart.chart_no_senior') }}
        </div>
      </el-row>
    </div>
  </el-row>
</template>

<style lang="less" scoped>
.senior-light {
  border-top: 1px solid @side-outline-border-color-light;
}

.senior-dark {
  border-top: 1px solid @side-outline-border-color;
}
.ed-row {
  display: block;
}

span {
  font-size: 14px;
}

.view-panel {
  display: flex;
  height: 100%;
  width: 100%;
}

.de-collapse-style {
  :deep(.ed-collapse-item__header) {
    height: 34px !important;
    line-height: 34px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }
  :deep(.ed-collapse-item__content) {
    padding: 16px !important;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
  :deep(.ed-checkbox__inner) {
    width: 14px;
    height: 14px;
  }
}
.prop {
  border-bottom: 1px solid @side-outline-border-color;
}
.prop-top {
  border-top: 1px solid @side-outline-border-color;
}
.no-senior {
  width: 100%;
  text-align: center;
  font-size: 12px;
  padding-top: 40px;
  overflow: auto;
  border-right: 1px solid #e6e6e6;
  height: 100%;
}
</style>
