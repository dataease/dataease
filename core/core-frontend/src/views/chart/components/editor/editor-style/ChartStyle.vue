<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import ColorSelector from '@/views/chart/components/editor/editor-style/components/ColorSelector.vue'
import SizeSelector from '@/views/chart/components/editor/editor-style/components/SizeSelector.vue'
import LabelSelector from '@/views/chart/components/editor/editor-style/components/LabelSelector.vue'
import TooltipSelector from '@/views/chart/components/editor/editor-style/components/TooltipSelector.vue'
import XAxisSelector from '@/views/chart/components/editor/editor-style/components/XAxisSelector.vue'
import YAxisSelector from '@/views/chart/components/editor/editor-style/components/YAxisSelector.vue'
import TitleSelector from '@/views/chart/components/editor/editor-style/components/TitleSelector.vue'
import LegendSelector from '@/views/chart/components/editor/editor-style/components/LegendSelector.vue'

const { t } = useI18n()

const state = {
  attrActiveNames: [],
  styleActiveNames: []
}

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onColorChange', 'onSizeChange', 'onLabelChange', 'onTooltipChange'])

const onColorChange = val => {
  emit('onColorChange', val)
}

const onSizeChange = val => {
  emit('onSizeChange', val)
}

const onLabelChange = val => {
  emit('onLabelChange', val)
}

const onTooltipChange = val => {
  emit('onTooltipChange', val)
}
</script>

<template>
  <el-row class="view-panel">
    <div
      :style="{
        overflow: 'auto',
        height: '100%',
        width: '100%'
      }"
      class="attr-style"
    >
      <el-row class="de-collapse-style">
        <span class="padding-lr">{{ t('chart.shape_attr') }}</span>

        <el-collapse v-model="state.attrActiveNames" class="style-collapse">
          <el-collapse-item name="color" :title="t('chart.color')">
            <color-selector
              class="attr-selector"
              :chart="props.chart"
              @onColorChange="onColorChange"
            />
          </el-collapse-item>

          <el-collapse-item
            name="size"
            :title="
              props.chart.type && props.chart.type.includes('table')
                ? t('chart.table_config')
                : t('chart.size')
            "
          >
            <size-selector
              class="attr-selector"
              :chart="props.chart"
              @onSizeChange="onSizeChange"
            />
          </el-collapse-item>

          <el-collapse-item name="label" :title="$t('chart.label')">
            <label-selector
              class="attr-selector"
              :chart="props.chart"
              @onLabelChange="onLabelChange"
            />
          </el-collapse-item>

          <el-collapse-item name="tooltip" :title="$t('chart.tooltip')">
            <tooltip-selector
              class="attr-selector"
              :chart="props.chart"
              @onTooltipChange="onTooltipChange"
            />
          </el-collapse-item>
        </el-collapse>
      </el-row>

      <el-row class="de-collapse-style">
        <span class="padding-lr">{{ t('chart.module_style') }}</span>
        <el-collapse v-model="state.styleActiveNames" class="style-collapse">
          <el-collapse-item name="xAxis" :title="t('chart.xAxis')">
            <x-axis-selector />
          </el-collapse-item>

          <el-collapse-item name="yAxis" :title="$t('chart.yAxis')">
            <y-axis-selector />
          </el-collapse-item>

          <el-collapse-item name="title" :title="$t('chart.title')">
            <title-selector />
          </el-collapse-item>

          <el-collapse-item name="legend" :title="$t('chart.legend')">
            <legend-selector />
          </el-collapse-item>
        </el-collapse>
      </el-row>
    </div>
  </el-row>
</template>

<style lang="less" scoped>
.el-row {
  display: block;
}

span {
  font-size: 14px;
}

.view-panel {
  display: flex;
  height: 100%;
  background-color: #f7f8fa;
  width: 100%;
}

.attr-style {
  height: 100%;
}

.de-collapse-style {
  :deep(.el-collapse-item__header) {
    height: 34px !important;
    line-height: 34px !important;
    padding: 0 0 0 6px !important;
    font-size: 12px !important;
    font-weight: 400 !important;
  }
}
</style>
