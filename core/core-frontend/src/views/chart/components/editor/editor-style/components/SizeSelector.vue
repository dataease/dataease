<script lang="tsx" setup>
import { reactive } from 'vue'
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
  sizeForm: JSON.parse(JSON.stringify(DEFAULT_SIZE)),
  fontSize: []
})

const lineSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const pageSizeOptions = [
  { name: '10' + t('chart.table_page_size_unit'), value: '10' },
  { name: '20' + t('chart.table_page_size_unit'), value: '20' },
  { name: '50' + t('chart.table_page_size_unit'), value: '50' },
  { name: '100' + t('chart.table_page_size_unit'), value: '100' }
]

const alignOptions = [
  { name: t('chart.table_align_left'), value: 'left' },
  { name: t('chart.table_align_center'), value: 'center' },
  { name: t('chart.table_align_right'), value: 'right' }
]

const changeBarSizeCase = () => {
  emit('onSizeChange', state.sizeForm)
}

const initFontSize = () => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.size) {
      state.sizeForm = customAttr.size
    }
  }
}

initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="sizeFormBar" :model="state.sizeForm" label-width="80px" size="small">
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
              input-size="small"
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
              input-size="small"
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
              input-size="small"
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
              input-size="small"
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
              input-size="small"
              :min="0"
              :max="100"
              @change="changeBarSizeCase('pieOuterRadius')"
            />
          </el-form-item>
        </div>
        <!--pie-end-->

        <!--table-info-start-->
        <div v-show="props.chart.type.includes('table')">
          <el-form-item label-width="100px" :label="t('chart.table_page_mode')" class="form-item">
            <el-select
              v-model="state.sizeForm.tablePageMode"
              :placeholder="t('chart.table_page_mode')"
              @change="changeBarSizeCase('tablePageMode')"
            >
              <el-option :label="t('chart.page_mode_page')" value="page" />
              <el-option :label="t('chart.page_mode_pull')" value="pull" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.tablePageMode === 'page'"
            label-width="100px"
            :label="t('chart.table_page_size')"
            class="form-item"
          >
            <el-select
              v-model="state.sizeForm.tablePageSize"
              :placeholder="t('chart.table_page_size')"
              @change="changeBarSizeCase('tablePageSize')"
            >
              <el-option
                v-for="item in pageSizeOptions"
                :key="item.value"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_title_fontsize')"
            class="form-item"
          >
            <el-select
              v-model="state.sizeForm.tableTitleFontSize"
              :placeholder="t('chart.table_title_fontsize')"
              @change="changeBarSizeCase('tableTitleFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_item_fontsize')"
            class="form-item"
          >
            <el-select
              v-model="state.sizeForm.tableItemFontSize"
              :placeholder="t('chart.table_item_fontsize')"
              @change="changeBarSizeCase('tableItemFontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_header_align')"
            class="form-item"
          >
            <el-select
              v-model="state.sizeForm.tableHeaderAlign"
              :placeholder="t('chart.table_header_align')"
              @change="changeBarSizeCase('tableHeaderAlign')"
            >
              <el-option
                v-for="option in alignOptions"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label-width="100px" :label="t('chart.table_item_align')" class="form-item">
            <el-select
              v-model="state.sizeForm.tableItemAlign"
              :placeholder="t('chart.table_item_align')"
              @change="changeBarSizeCase('tableItemAlign')"
            >
              <el-option
                v-for="option in alignOptions"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_title_height')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="state.sizeForm.tableTitleHeight"
              :min="20"
              :max="100"
              show-input
              :show-input-controls="false"
              input-size="small"
              @change="changeBarSizeCase('tableTitleHeight')"
            />
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_item_height')"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="state.sizeForm.tableItemHeight"
              :min="20"
              :max="100"
              show-input
              :show-input-controls="false"
              input-size="small"
              @change="changeBarSizeCase('tableItemHeight')"
            />
          </el-form-item>
          <el-form-item
            label-width="100px"
            :label="t('chart.table_column_width_config')"
            class="form-item"
          >
            <el-radio-group
              v-model="state.sizeForm.tableColumnMode"
              @change="changeBarSizeCase('tableColumnMode')"
            >
              <el-radio label="adapt"
                ><span>{{ t('chart.table_column_adapt') }}</span></el-radio
              >
              <el-radio label="custom">
                <span>{{ t('chart.table_column_custom') }}</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.tableColumnMode === 'custom'"
            label=""
            label-width="100px"
            class="form-item form-item-slider"
          >
            <el-slider
              v-model="state.sizeForm.tableColumnWidth"
              :min="10"
              :max="500"
              show-input
              :show-input-controls="false"
              input-size="small"
              @change="changeBarSizeCase('tableColumnWidth')"
            />
          </el-form-item>
          <el-form-item label-width="100px" :label="t('chart.table_show_index')" class="form-item">
            <el-radio-group
              v-model="state.sizeForm.showIndex"
              input-size="small"
              @change="changeBarSizeCase('showIndex')"
            >
              <el-radio :label="true">{{ t('panel.yes') }}</el-radio>
              <el-radio :label="false">{{ t('panel.no') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="state.sizeForm.showIndex"
            label-width="100px"
            :label="t('chart.table_index_desc')"
            class="form-item"
          >
            <el-input
              v-model="state.sizeForm.indexLabel"
              type="text"
              @blur="changeBarSizeCase('indexLabel')"
            />
          </el-form-item>
        </div>
        <!--table-info-end-->
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped></style>
