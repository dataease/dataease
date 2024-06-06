<script lang="tsx" setup>
import { computed, onMounted, PropType, reactive, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '@/views/chart/components/editor/util/chart'
import { formatterType, unitType } from '@/views/chart/components/js/formatter'
import { ElMessage } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'
import DualYAxisSelectorInner from './DualYAxisSelectorInner.vue'

const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  chart: {
    type: Object,
    required: true
  },
  propertyInner: {
    type: Array<string>
  }
})

const activeName = ref('left')

const state = reactive<any>({
  axisForm: JSON.parse(JSON.stringify(DEFAULT_YAXIS_STYLE)),
  subAxisForm: JSON.parse(JSON.stringify(DEFAULT_YAXIS_EXT_STYLE))
})

const emit = defineEmits(['onChangeYAxisForm', 'onChangeYAxisExtForm'])

watch(
  () => props.chart.customStyle.yAxis,
  () => {
    init()
  },
  { deep: true }
)

const changeAxisStyle = (val, prop) => {
  emit('onChangeYAxisForm', val, prop)
}

const changeSubAxisStyle = (val, prop) => {
  emit('onChangeYAxisExtForm', val, prop)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    if (customStyle.yAxis) {
      state.axisForm = cloneDeep(customStyle.yAxis)
      state.axisForm.position = 'left'
    }

    if (customStyle.yAxisExt) {
      state.subAxisForm = cloneDeep(customStyle.yAxisExt)
    }
    state.subAxisForm.position = 'right'
    state.subAxisForm.show = state.axisForm.show
    if (chart.type === 'bidirectional-bar') {
      state.axisForm.position = customStyle.yAxis.position
      state.subAxisForm.position = customStyle.yAxisExt.position
    }
  }
}

onMounted(() => {
  init()
})
</script>

<template>
  <el-tabs v-model="activeName" id="axis-tabs" stretch>
    <el-tab-pane
      :label="
        chart.type === 'bidirectional-bar'
          ? t('chart.text_pos_left') + t('chart.xAxis')
          : t('chart.yAxisLeft')
      "
      name="left"
    >
      <dual-y-axis-selector-inner
        style="margin-top: 8px"
        v-if="state.axisForm"
        :form="state.axisForm"
        :property-inner="propertyInner"
        :themes="themes"
        type="left"
        :chart-type="chart.type"
        :layout="chart.customAttr.basicStyle.layout"
        @on-change-y-axis-form="changeAxisStyle"
      />
    </el-tab-pane>
    <el-tab-pane
      :label="
        chart.type === 'bidirectional-bar'
          ? t('chart.text_pos_right') + t('chart.xAxis')
          : t('chart.yAxisRight')
      "
      name="right"
    >
      <dual-y-axis-selector-inner
        style="margin-top: 8px"
        v-if="state.subAxisForm"
        :form="state.subAxisForm"
        :property-inner="propertyInner"
        :themes="themes"
        type="right"
        :chart-type="chart.type"
        :layout="chart.customAttr.basicStyle.layout"
        @on-change-y-axis-form="changeSubAxisStyle"
      />
    </el-tab-pane>
  </el-tabs>
</template>

<style lang="less" scoped>
#axis-tabs {
  margin-top: -16px;
  --ed-tabs-header-height: 34px;

  :deep(.ed-tabs__header) {
    border-top: none !important;
  }
}

.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #646a73;
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  padding: 2px 12px 0 0;

  &.custom-form-item-label--dark {
    color: #a6a6a6;
  }
}
.form-item-checkbox {
  margin-bottom: 10px !important;
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 16px;

  &.m-divider--dark {
    border-color: rgba(235, 235, 235, 0.15);
  }
}
</style>
