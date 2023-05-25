<script lang="tsx" setup>
import FunctionCfg from '@/views/chart/components/editor/editor-senior/components/FunctionCfg.vue'
import ScrollCfg from '@/views/chart/components/editor/editor-senior/components/ScrollCfg.vue'
import AssistLine from '@/views/chart/components/editor/editor-senior/components/AssistLine.vue'
import Threshold from '@/views/chart/components/editor/editor-senior/components/Threshold.vue'

const state = {
  attrActiveNames: []
}

const emit = defineEmits(['onFunctionCfgChange', 'onAssistLineChange'])

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  }
})

const onFunctionCfgChange = val => {
  emit('onFunctionCfgChange', val)
}

const onAssistLineChange = val => {
  emit('onAssistLineChange', val)
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
        <span class="padding-lr">{{ $t('chart.senior_cfg') }}</span>
        <el-collapse v-model="state.attrActiveNames" class="style-collapse">
          <el-collapse-item name="function" :title="$t('chart.function_cfg')">
            <function-cfg :chart="props.chart" @onFunctionCfgChange="onFunctionCfgChange" />
          </el-collapse-item>

          <!--          <el-collapse-item name="scroll" :title="$t('chart.scroll_cfg')">-->
          <!--            <scroll-cfg />-->
          <!--          </el-collapse-item>-->
        </el-collapse>
      </el-row>

      <el-row class="de-collapse-style">
        <span class="padding-lr">{{ $t('chart.analyse_cfg') }}</span>
        <el-collapse v-model="styleActiveNames" class="style-collapse">
          <el-collapse-item name="analyse" :title="$t('chart.assist_line')">
            <assist-line
              :chart="props.chart"
              :quota-data="props.quotaData"
              @onAssistLineChange="onAssistLineChange"
            />
          </el-collapse-item>

          <!--          <el-collapse-item name="threshold" :title="$t('chart.threshold')">-->
          <!--            <threshold />-->
          <!--          </el-collapse-item>-->
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
