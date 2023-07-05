<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_SCROLL, DEFAULT_SIZE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits(['onScrollCfgChange'])

watch(
  () => props.chart,
  () => {
    init()
  }
)

const state = reactive({
  scrollForm: JSON.parse(JSON.stringify(DEFAULT_SCROLL)),
  isAutoBreakLine: false
})

const changeScrollCfg = () => {
  emit('onScrollCfgChange', state.scrollForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    let senior = null
    if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
      senior = JSON.parse(JSON.stringify(chart.senior))
    } else {
      senior = JSON.parse(chart.senior)
    }
    if (senior.scrollCfg) {
      state.scrollForm = senior.scrollCfg
    }
  }
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.size) {
      if (props.chart.render === 'antv') {
        state.isAutoBreakLine = false
      } else {
        state.isAutoBreakLine = customAttr.size.tableAutoBreakLine
          ? customAttr.size.tableAutoBreakLine
          : DEFAULT_SIZE.tableAutoBreakLine
      }
    }
  }
}

init()
</script>

<template>
  <div :style="{ width: '100%', display: 'block' }">
    <el-row class="scroll-style">
      <el-form ref="scrollForm" :model="state.scrollForm" label-width="80px">
        <el-form-item :label="t('chart.scroll')" class="form-item">
          <el-checkbox v-model="state.scrollForm.open" @change="changeScrollCfg">{{
            t('chart.open')
          }}</el-checkbox>
          <!--          <el-tooltip class="item" :effect="props.themes" placement="bottom">-->
          <!--            <div slot="content">-->
          <!--              {{ t('chart.table_scroll_tip') }}-->
          <!--            </div>-->
          <!--            <i class="el-icon-info" :style="{cursor: 'pointer', color: 'gray', fontSize: '12px'" />-->
          <!--          </el-tooltip>-->
        </el-form-item>
        <span v-show="state.scrollForm.open">
          <el-form-item v-show="!state.isAutoBreakLine" :label="t('chart.row')" class="form-item">
            <el-input-number
              :effect="props.themes"
              size="small"
              v-model.number="state.scrollForm.row"
              :min="1"
              :max="1000"
              :precision="0"
              @change="changeScrollCfg"
            />
          </el-form-item>
          <el-form-item v-show="state.isAutoBreakLine" :label="t('chart.step')" class="form-item">
            <el-input-number
              :effect="props.themes"
              size="small"
              v-model="state.scrollForm.step"
              :min="1"
              :max="10000"
              :precision="0"
              @change="changeScrollCfg"
            />
          </el-form-item>
          <el-form-item :label="t('chart.interval') + '(ms)'" class="form-item">
            <el-input-number
              :effect="props.themes"
              size="small"
              v-model="state.scrollForm.interval"
              :min="500"
              :step="1000"
              :precision="0"
              @change="changeScrollCfg"
            />
          </el-form-item>
        </span>
      </el-form>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider :deep(.el-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}
.form-item :deep(.el-form-item__label) {
  font-size: 12px;
}
.el-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}
.el-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.scroll-style :deep(.el-input-number--mini) {
  width: 120px !important;
}
</style>
