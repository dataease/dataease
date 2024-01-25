<script lang="ts" setup>
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DEFAULT_TABLE_TOTAL } from '@/views/chart/components/editor/util/chart'
import { cloneDeep, defaultsDeep } from 'lodash-es'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})
watch(
  [props.chart.customAttr.tableTotal, props.chart.yAxis],
  () => {
    init()
  },
  { deep: true }
)

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})
const aggregations = [
  { name: t('chart.sum'), value: 'SUM' },
  { name: t('chart.avg'), value: 'AVG' },
  { name: t('chart.max'), value: 'MAX' },
  { name: t('chart.min'), value: 'MIN' }
]
const state = reactive({
  tableTotalForm: cloneDeep(DEFAULT_TABLE_TOTAL) as ChartTableTotalAttr,
  rowSubTotalItem: {
    dataeaseName: '',
    aggregation: ''
  } as CalcTotalCfg,
  rowTotalItem: {
    dataeaseName: '',
    aggregation: ''
  } as CalcTotalCfg,
  colSubTotalItem: {
    dataeaseName: '',
    aggregation: ''
  } as CalcTotalCfg,
  colTotalItem: {
    dataeaseName: '',
    aggregation: ''
  } as CalcTotalCfg
})

const emit = defineEmits(['onTableTotalChange'])

const changeTableTotal = prop => {
  emit('onTableTotalChange', state.tableTotalForm, prop)
}

const init = () => {
  const tableTotal = props.chart?.customAttr?.tableTotal
  if (tableTotal) {
    state.tableTotalForm = defaultsDeep(cloneDeep(tableTotal), cloneDeep(DEFAULT_TABLE_TOTAL))
  }
  const yAxis = props.chart.yAxis
  if (yAxis?.length > 0) {
    const axisArr = yAxis.map(i => i.dataeaseName)
    if (axisArr.indexOf(state.tableTotalForm.row.totalSortField) != -1) {
      state.tableTotalForm.row.totalSortField = yAxis[0].dataeaseName
    }
    state.tableTotalForm.col.totalSortField = yAxis[0].dataeaseName
  } else {
    state.tableTotalForm.row.totalSortField = ''
    state.tableTotalForm.col.totalSortField = ''
  }
  const totals = [
    { ...state.tableTotalForm.row.calcTotals },
    { ...state.tableTotalForm.row.calcSubTotals },
    { ...state.tableTotalForm.col.calcTotals },
    { ...state.tableTotalForm.col.calcSubTotals }
  ]
  totals.forEach(total => {
    setupTotalCfg(total.cfg, yAxis)
  })
  const totalTupleArr: [CalcTotalCfg, CalcTotalCfg[]][] = [
    [state.rowTotalItem, state.tableTotalForm.row.calcTotals.cfg],
    [state.rowSubTotalItem, state.tableTotalForm.row.calcSubTotals.cfg],
    [state.colTotalItem, state.tableTotalForm.col.calcTotals.cfg],
    [state.colSubTotalItem, state.tableTotalForm.col.calcSubTotals.cfg]
  ]
  totalTupleArr.forEach(tuple => {
    const [total, totalCfg] = tuple
    if (!totalCfg.length) {
      total.dataeaseName = ''
      total.aggregation = ''
      return
    }
    const totalIndex = totalCfg.findIndex(i => i.dataeaseName === total.dataeaseName)
    if (totalIndex !== -1) {
      total.aggregation = totalCfg[totalIndex].aggregation
    } else {
      total.dataeaseName = totalCfg[0].dataeaseName
      total.aggregation = totalCfg[0].aggregation
    }
  })
}
const showProperty = prop => props.propertyInner?.includes(prop)
const changeTotal = (totalItem, totals) => {
  for (let i = 0; i < totals.length; i++) {
    const item = totals[i]
    if (item.dataeaseName === totalItem.dataeaseName) {
      totalItem.aggregation = item.aggregation
      return
    }
  }
}
const changeTotalAggr = (totalItem, totals, colOrNum) => {
  for (let i = 0; i < totals.length; i++) {
    const item = totals[i]
    if (item.dataeaseName === totalItem.dataeaseName) {
      item.aggregation = totalItem.aggregation
      break
    }
  }
  changeTableTotal(colOrNum)
}
const setupTotalCfg = (totalCfg, axis) => {
  if (!totalCfg.length) {
    axis.forEach(i => {
      totalCfg.push({
        dataeaseName: i.dataeaseName,
        aggregation: 'SUM'
      })
    })
    return
  }
  if (!axis.length) {
    totalCfg.splice(0, totalCfg.length)
    return
  }
  const cfgMap = totalCfg.reduce((p, n) => {
    p[n.dataeaseName] = n
    return p
  }, {})
  totalCfg.splice(0, totalCfg.length)
  axis.forEach(i => {
    totalCfg.push({
      dataeaseName: i.dataeaseName,
      aggregation: cfgMap[i.dataeaseName] ? cfgMap[i.dataeaseName].aggregation : 'SUM'
    })
  })
}
onMounted(() => {
  init()
})
</script>

<template>
  <el-form ref="tableTotalForm" :model="state.tableTotalForm" label-position="top">
    <el-divider
      v-if="showProperty('row')"
      content-position="center"
      :class="'divider-style-' + themes"
    >
      {{ t('chart.row_cfg') }}
    </el-divider>
    <el-form-item
      v-show="showProperty('row')"
      :label="t('chart.total_show')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        :effect="themes"
        v-model="state.tableTotalForm.row.showGrandTotals"
        @change="changeTableTotal('row.showGrandTotals')"
      >
        {{ t('chart.show') }}
      </el-checkbox>
    </el-form-item>
    <div v-show="showProperty('row') && state.tableTotalForm.row.showGrandTotals">
      <el-form-item
        :label="t('chart.total_position')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.row.reverseLayout"
          @change="changeTableTotal('row.reverseLayout')"
        >
          <el-radio :effect="themes" :label="true">{{ t('chart.total_pos_top') }}</el-radio>
          <el-radio :effect="themes" :label="false">{{ t('chart.total_pos_bottom') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        :label="t('chart.total_label')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input
          :effect="themes"
          :placeholder="t('chart.total_label')"
          size="small"
          maxlength="20"
          v-model="state.tableTotalForm.row.label"
          clearable
          @blur="changeTableTotal('row.label')"
        />
      </el-form-item>
      <el-form-item
        :label="t('chart.aggregation')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-col :span="11">
          <el-select
            :effect="themes"
            v-model="state.rowTotalItem.dataeaseName"
            :placeholder="t('chart.aggregation')"
            @change="changeTotal(state.rowTotalItem, state.tableTotalForm.row.calcTotals.cfg)"
          >
            <el-option
              v-for="option in chart.yAxis"
              :key="option.dataeaseName"
              :label="option.name"
              :value="option.dataeaseName"
            />
          </el-select>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-select
            :effect="themes"
            v-model="state.rowTotalItem.aggregation"
            :placeholder="t('chart.aggregation')"
            @change="
              changeTotalAggr(
                state.rowTotalItem,
                state.tableTotalForm.row.calcTotals.cfg,
                'row.calcTotals.cfg'
              )
            "
          >
            <el-option
              v-for="option in aggregations"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
      </el-form-item>
      <el-form-item
        v-if="chart.type === 'table-pivot'"
        :label="t('chart.total_sort')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.row.totalSort"
          @change="changeTableTotal('row.totalSort')"
        >
          <el-radio :effect="themes" label="none">{{ t('chart.total_sort_none') }}</el-radio>
          <el-radio :effect="themes" label="asc">{{ t('chart.total_sort_asc') }}</el-radio>
          <el-radio :effect="themes" label="desc">{{ t('chart.total_sort_desc') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-if="chart.type === 'table-pivot' && state.tableTotalForm.row.totalSort !== 'none'"
        :label="t('chart.total_sort_field')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-select
          :effect="themes"
          v-model="state.tableTotalForm.row.totalSortField"
          class="form-item-select"
          :placeholder="t('chart.total_sort_field')"
          @change="changeTableTotal('row')"
        >
          <el-option
            v-for="option in chart.yAxis"
            :key="option.dataeaseName"
            :label="option.name"
            :value="option.dataeaseName"
          />
        </el-select>
      </el-form-item>
    </div>

    <el-form-item
      v-show="showProperty('row')"
      :label="t('chart.sub_total_show')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        :effect="themes"
        v-model="state.tableTotalForm.row.showSubTotals"
        :disabled="chart.xAxisExt.length < 2"
        @change="changeTableTotal('row')"
        >{{ t('chart.show') }}</el-checkbox
      >
    </el-form-item>
    <div v-show="showProperty('row') && state.tableTotalForm.row.showSubTotals">
      <el-form-item
        :label="t('chart.total_position')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.row.reverseSubLayout"
          :disabled="chart.xAxisExt.length < 2"
          @change="changeTableTotal('row')"
        >
          <el-radio :effect="themes" :label="true">{{ t('chart.total_pos_top') }}</el-radio>
          <el-radio :effect="themes" :label="false">{{ t('chart.total_pos_bottom') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        :label="t('chart.total_label')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input
          :effect="themes"
          :disabled="chart.xAxisExt.length < 2"
          :placeholder="t('chart.total_label')"
          v-model="state.tableTotalForm.row.subLabel"
          size="small"
          maxlength="20"
          clearable
          @blur="changeTableTotal"
        />
      </el-form-item>
      <el-form-item
        :label="t('chart.aggregation')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-col :span="11">
          <el-select
            :effect="themes"
            v-model="state.rowSubTotalItem.dataeaseName"
            :disabled="chart.xAxisExt.length < 2"
            :placeholder="t('chart.aggregation')"
            @change="changeTotal(state.rowSubTotalItem, state.tableTotalForm.row.calcSubTotals.cfg)"
          >
            <el-option
              v-for="option in chart.yAxis"
              :key="option.dataeaseName"
              :label="option.name"
              :value="option.dataeaseName"
            />
          </el-select>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-select
            :effect="themes"
            v-model="state.rowSubTotalItem.aggregation"
            :disabled="chart.xAxisExt.length < 2"
            :placeholder="t('chart.aggregation')"
            @change="
              changeTotalAggr(
                state.rowSubTotalItem,
                state.tableTotalForm.row.calcSubTotals.cfg,
                'row'
              )
            "
          >
            <el-option
              v-for="option in aggregations"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
      </el-form-item>
    </div>

    <el-divider
      v-if="showProperty('col')"
      content-position="center"
      :class="'divider-style-' + themes"
    >
      {{ t('chart.col_cfg') }}
    </el-divider>
    <el-form-item
      v-show="showProperty('col')"
      :label="t('chart.total_show')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        :effect="themes"
        v-model="state.tableTotalForm.col.showGrandTotals"
        @change="changeTableTotal('col')"
        >{{ t('chart.show') }}</el-checkbox
      >
    </el-form-item>
    <div v-show="showProperty('col') && state.tableTotalForm.col.showGrandTotals">
      <el-form-item
        :label="t('chart.total_position')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.col.reverseLayout"
          @change="changeTableTotal('col')"
        >
          <el-radio :effect="themes" :label="true">{{ t('chart.total_pos_left') }}</el-radio>
          <el-radio :effect="themes" :label="false">{{ t('chart.total_pos_right') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        :label="t('chart.total_label')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input
          :effect="themes"
          :placeholder="t('chart.total_label')"
          size="small"
          maxlength="20"
          v-model="state.tableTotalForm.col.label"
          clearable
          @blur="changeTableTotal('col')"
        />
      </el-form-item>
      <el-form-item
        :label="t('chart.aggregation')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-col :span="11">
          <el-select
            :effect="themes"
            v-model="state.colTotalItem.dataeaseName"
            :placeholder="t('chart.aggregation')"
            @change="changeTotal(state.colTotalItem, state.tableTotalForm.col.calcTotals.cfg)"
          >
            <el-option
              v-for="option in chart.yAxis"
              :key="option.dataeaseName"
              :label="option.name"
              :value="option.dataeaseName"
            />
          </el-select>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-select
            :effect="themes"
            v-model="state.colTotalItem.aggregation"
            :placeholder="t('chart.aggregation')"
            @change="
              changeTotalAggr(
                state.colTotalItem,
                state.tableTotalForm.col.calcTotals.cfg,
                'col.calcTotals.cfg'
              )
            "
          >
            <el-option
              v-for="option in aggregations"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
      </el-form-item>
      <el-form-item
        v-if="chart.type === 'table-pivot'"
        :label="t('chart.total_sort')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.col.totalSort"
          @change="changeTableTotal('col')"
        >
          <el-radio :effect="themes" label="none">{{ t('chart.total_sort_none') }}</el-radio>
          <el-radio :effect="themes" label="asc">{{ t('chart.total_sort_asc') }}</el-radio>
          <el-radio :effect="themes" label="desc">{{ t('chart.total_sort_desc') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        v-show="
          false && chart.type === 'table-pivot' && state.tableTotalForm.col?.totalSort !== 'none'
        "
        :label="t('chart.total_sort_field')"
        class="form-item"
      >
        <el-select
          :effect="themes"
          v-model="state.tableTotalForm.col.totalSortField"
          class="form-item-select"
          :placeholder="t('chart.total_sort_field')"
          @change="changeTableTotal('col')"
        >
          <el-option
            v-for="option in chart.yAxis"
            :key="option.dataeaseName"
            :label="option.name"
            :value="option.dataeaseName"
          />
        </el-select>
      </el-form-item>
    </div>

    <el-form-item
      v-show="showProperty('col')"
      :label="t('chart.sub_total_show')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        :effect="themes"
        v-model="state.tableTotalForm.col.showSubTotals"
        :disabled="chart.xAxis.length < 2"
        @change="changeTableTotal('col')"
        >{{ t('chart.show') }}</el-checkbox
      >
    </el-form-item>
    <div v-show="showProperty('col') && state.tableTotalForm.col.showSubTotals">
      <el-form-item
        :label="t('chart.total_position')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-radio-group
          :effect="themes"
          v-model="state.tableTotalForm.col.reverseSubLayout"
          :disabled="chart.xAxis?.length < 2"
          @change="changeTableTotal('col')"
        >
          <el-radio :effect="themes" :label="true">{{ t('chart.total_pos_left') }}</el-radio>
          <el-radio :effect="themes" :label="false">{{ t('chart.total_pos_right') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        :label="t('chart.total_label')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-input
          :effect="themes"
          :disabled="chart.xAxis?.length < 2"
          :placeholder="t('chart.total_label')"
          v-model="state.tableTotalForm.col.subLabel"
          size="small"
          maxlength="20"
          clearable
          @change="changeTableTotal('col')"
        />
      </el-form-item>
      <el-form-item
        :label="t('chart.aggregation')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-col :span="11">
          <el-select
            :effect="themes"
            v-model="state.colSubTotalItem.dataeaseName"
            :disabled="chart.xAxis?.length < 2"
            :placeholder="t('chart.aggregation')"
            @change="changeTotal(state.colSubTotalItem, state.tableTotalForm.col.calcSubTotals.cfg)"
          >
            <el-option
              v-for="option in chart.yAxis"
              :key="option.dataeaseName"
              :label="option.name"
              :value="option.dataeaseName"
            />
          </el-select>
        </el-col>
        <el-col :span="11" :offset="2">
          <el-select
            :effect="themes"
            v-model="state.colSubTotalItem.aggregation"
            :disabled="chart.xAxis?.length < 2"
            :placeholder="t('chart.aggregation')"
            @change="
              changeTotalAggr(
                state.colSubTotalItem,
                state.tableTotalForm.col.calcSubTotals.cfg,
                'col.calcSubTotals.cfg'
              )
            "
          >
            <el-option
              v-for="option in aggregations"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
      </el-form-item>
    </div>
  </el-form>
</template>

<style lang="less" scoped>
.divider-style-dark {
  ::v-deep(.ed-divider__text) {
    color: #fff;
    background: @side-content-background;
  }
}
</style>
