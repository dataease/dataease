<script lang="tsx" setup>
import { reactive, onMounted, watch } from 'vue'
import { COLOR_PANEL, DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  },
  propertyInner: {
    type: Array
  },
  selectorType: {
    type: String
  }
})

const predefineColors = COLOR_PANEL

const state = reactive({
  bulletRangeForm: {
    bar: {
      ranges: {
        fill: 'rgba(0,128,255,0.5)',
        size: 20,
        showType: 'dynamic',
        fixedRange: [],
        fixedRangeNumber: 3,
        symbol: 'circle',
        symbolSize: 10,
        name: ''
      }
    }
  },
  rangeList: []
})

const emit = defineEmits(['onBasicStyleChange', 'onMiscChange'])

watch(
  () => props.chart.customAttr.misc,
  () => {
    init()
  },
  { deep: true }
)

const changeStyle = (prop?) => {
  if (state.bulletRangeForm.bar.ranges.showType === 'fixed' && state.rangeList.length) {
    state.bulletRangeForm.bar.ranges.fixedRange = cloneDeep(state.rangeList)
  }
  emit('onMiscChange', { data: { bullet: { ...state.bulletRangeForm } }, requestData: true }, prop)
}
const changeRangeNumber = () => {
  if (state.bulletRangeForm.bar.ranges.fixedRangeNumber === null) {
    state.bulletRangeForm.bar.ranges.fixedRangeNumber = 1
  }
  if (state.rangeList.length > state.bulletRangeForm.bar.ranges.fixedRangeNumber) {
    state.rangeList = state.rangeList.slice(0, state.bulletRangeForm.bar.ranges.fixedRangeNumber)
  } else {
    for (
      let i = state.rangeList.length;
      i < state.bulletRangeForm.bar.ranges.fixedRangeNumber;
      i++
    ) {
      state.rangeList.push({
        name: t('chart.symbolic_range') + (i + 1),
        fixedRangeValue: undefined,
        fill: 'rgba(0,128,255,0.44)'
      })
    }
  }
  changeRangeItem()
}

const changeRangeItem = () => {
  validateRangeList() && changeStyle()
}

const validateRangeList = () => {
  return state.rangeList.every(
    item => item.name && item.fixedRangeValue !== null && item.fixedRangeValue !== undefined
  )
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
    state.bulletRangeForm = defaultsDeep(customAttr.misc.bullet, cloneDeep(DEFAULT_MISC.bullet))
    getRangeList()
  }
}

const getRangeList = () => {
  const range = []
  if (state.bulletRangeForm.bar.ranges?.fixedRange?.length) {
    state.rangeList = state.bulletRangeForm.bar.ranges.fixedRange
  } else {
    for (let i = 0; i < state.bulletRangeForm.bar.ranges.fixedRangeNumber; i++) {
      range.push({
        name: '区间' + (i + 1),
        fixedRangeValue: undefined,
        fill: 'rgba(0,128,255,0)'
      })
    }
    state.rangeList = cloneDeep(range)
  }
}

const changeShowType = () => {
  if (state.bulletRangeForm.bar.ranges.showType === 'dynamic') {
    changeStyle()
  } else {
    changeRangeItem()
  }
}

onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="bulletRangeForm"
    :model="state.bulletRangeForm"
    size="small"
    label-position="top"
    @submit.prevent
  >
    <div v-if="selectorType === 'range'">
      <el-form-item
        :label="t('chart.radar_size')"
        class="form-item"
        :class="'form-item-' + themes"
        style="padding-left: 4px; width: 100%"
      >
        <el-input-number
          :effect="props.themes"
          v-model="state.bulletRangeForm.bar.ranges.size"
          :min="1"
          size="small"
          controls-position="right"
          @change="changeStyle('bar.ranges.size')"
        />
      </el-form-item>
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-radio-group
          :effect="themes"
          v-model="state.bulletRangeForm.bar.ranges.showType"
          @change="changeShowType()"
        >
          <el-radio :effect="themes" label="dynamic">{{ t('chart.dynamic') }}</el-radio>
          <el-radio :effect="themes" label="fixed">{{ t('chart.fix') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <div v-if="state.bulletRangeForm.bar.ranges.showType === 'dynamic'">
        <div style="flex: 1; display: flex">
          <el-form-item
            :label="t('visualization.backgroundColor')"
            class="form-item"
            :class="'form-item-' + themes"
            style="padding-right: 4px"
          >
            <el-color-picker
              v-model="state.bulletRangeForm.bar.ranges.fill"
              :predefine="predefineColors"
              :effect="themes"
              @change="changeStyle('bar.ranges.fill')"
              show-alpha
              is-custom
            />
          </el-form-item>
        </div>
      </div>
      <div v-if="state.bulletRangeForm.bar.ranges.showType === 'fixed'">
        <div style="flex: 1; display: flex">
          <el-form-item
            style="width: 100%"
            class="form-item"
            :class="'form-item-' + themes"
            :label="t('chart.range_num')"
          >
            <el-input-number
              :effect="themes"
              v-model="state.bulletRangeForm.bar.ranges.fixedRangeNumber"
              :precision="0"
              :min="1"
              :max="9"
              :step="1"
              :controls="true"
              controls-position="right"
              @change="changeRangeNumber()"
            />
          </el-form-item>
        </div>
        <div style="flex: 1; display: flex" v-for="(item, index) in state.rangeList" :key="index">
          <el-form-item
            :label="index === 0 ? t('chart.threshold_value') : ' '"
            style="width: 170px"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-input-number
              :effect="themes"
              v-model="item.fixedRangeValue"
              :min="0"
              size="small"
              controls-position="right"
              @change="changeRangeItem()"
            />
          </el-form-item>
          <el-form-item
            :label="index === 0 ? t('chart.show_name') : ' '"
            class="form-item"
            :class="'form-item-' + themes"
            style="padding-left: 4px"
          >
            <el-input
              :effect="themes"
              v-model="item.name"
              size="small"
              @change="changeRangeItem()"
            />
          </el-form-item>
          <el-form-item
            :label="index === 0 ? t('visualization.backgroundColor') : ' '"
            class="form-item"
            :class="'form-item-' + themes"
            style="padding-left: 4px"
          >
            <el-color-picker
              v-model="item.fill"
              :predefine="predefineColors"
              :effect="themes"
              @change="changeRangeItem()"
              show-alpha
              is-custom
            />
          </el-form-item>
        </div>
      </div>
    </div>
  </el-form>
</template>

<style lang="less" scoped></style>
