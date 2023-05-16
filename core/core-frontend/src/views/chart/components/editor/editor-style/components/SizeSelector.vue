<script lang="tsx" setup>
import { reactive, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_SIZE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onSizeChange'])

const state = reactive({
  sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE))
})

const lineSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const changeBarSizeCase = () => {
  emit('onSizeChange', state.sizeForm)
}
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="sizeFormBar" :model="state.sizeForm" label-width="80px" size="mini">
        <!--bar-begin-->
        <div v-show="props.chart.type.includes('bar')">
          <el-form-item :label="t('chart.adapt')" class="form-item">
            <el-checkbox
              v-model="state.sizeForm.barDefault"
              @change="changeBarSizeCase('barDefault')"
              >{{ t('chart.adapt') }}</el-checkbox
            >
          </el-form-item>
          <el-form-item :label="t('chart.bar_gap')" class="form-item form-item-slider">
            <el-slider
              v-model="state.sizeForm.barGap"
              :disabled="state.sizeForm.barDefault"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="5"
              :step="0.1"
              @change="changeBarSizeCase('barGap')"
            />
          </el-form-item>
        </div>
        <!--bar-end-->

        <!--line-begin-->
        <div v-show="props.chart.type.includes('line')">
          <el-form-item :label="t('chart.line_width')" class="form-item form-item-slider">
            <el-slider
              v-model="state.sizeForm.lineWidth"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="10"
              @change="changeBarSizeCase('lineWidth')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.line_symbol')" class="form-item">
            <el-select
              v-model="state.sizeForm.lineSymbol"
              :placeholder="t('chart.line_symbol')"
              @change="changeBarSizeCase('lineSymbol')"
            >
              <el-option
                v-for="item in lineSymbolOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.line_symbol_size')" class="form-item form-item-slider">
            <el-slider
              v-model="state.sizeForm.lineSymbolSize"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="20"
              @change="changeBarSizeCase('lineSymbolSize')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.line_smooth')" class="form-item">
            <el-checkbox
              v-model="state.sizeForm.lineSmooth"
              @change="changeBarSizeCase('lineSmooth')"
              >{{ t('chart.line_smooth') }}
            </el-checkbox>
          </el-form-item>
        </div>
        <!--line-end-->

        <!--pie-begin-->
        <div v-show="props.chart.type.includes('pie')">
          <el-form-item
            :label="t('chart.pie_inner_radius_percent')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="state.sizeForm.pieInnerRadius"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="100"
              @change="changeBarSizeCase('pieInnerRadius')"
            />
          </el-form-item>
          <el-form-item
            :label="t('chart.pie_outer_radius_size')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="state.sizeForm.pieOuterRadius"
              show-input
              :show-input-controls="false"
              input-size="mini"
              :min="0"
              :max="100"
              @change="changeBarSizeCase('pieOuterRadius')"
            />
          </el-form-item>
        </div>
        <!--pie-end-->
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
