<script lang="tsx" setup>
import icon_info_filled from '@/assets/svg/icon_info_filled.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { computed, PropType, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '../../../util/chart'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  threshold: {
    type: Array,
    required: true,
    default: () => []
  }
})

const emit = defineEmits(['onLineThresholdChange'])

const thresholdCondition = {
  term: 'lt',
  field: '0',
  value: '0',
  color: '#ff0000ff',
  backgroundColor: '#ffffff00',
  min: '0',
  max: '1',
  type: 'fixed'
}
const expressionList = [
  {
    label: '',
    options: [
      {
        value: 'eq',
        label: t('chart.filter_eq')
      },
      {
        value: 'not_eq',
        label: t('chart.filter_not_eq')
      }
    ]
  },
  {
    label: '',
    options: [
      {
        value: 'lt',
        label: t('chart.filter_lt')
      },
      {
        value: 'gt',
        label: t('chart.filter_gt')
      }
    ]
  },
  {
    label: '',
    options: [
      {
        value: 'le',
        label: t('chart.filter_le')
      },
      {
        value: 'ge',
        label: t('chart.filter_ge')
      }
    ]
  },
  {
    label: '',
    options: [
      {
        value: 'between',
        label: t('chart.filter_between')
      }
    ]
  }
]
const filterExpressionListByValue = (list, values) => {
  return list
    .map(group => ({
      ...group,
      options: group.options.filter(option => values.includes(option.value))
    }))
    .filter(group => group.options.length > 0)
}
const valueOptions = computed(() => {
  if (props.chart.type === 'symbolic-map') {
    return filterExpressionListByValue(expressionList, [
      'eq',
      'not_eq',
      'lt',
      'gt',
      'le',
      'ge',
      'between'
    ])
  }
  return filterExpressionListByValue(expressionList, ['lt', 'gt', 'le', 'ge', 'between'])
})
const predefineColors = COLOR_PANEL

const state = reactive({
  thresholdArr: [] as LineThreshold[],
  fields: [],
  thresholdObj: {
    fieldId: '',
    field: {},
    conditions: []
  } as LineThreshold
})

const init = () => {
  state.thresholdArr = JSON.parse(JSON.stringify(props.threshold)) as LineThreshold[]
  initFields()
}
const initOptions = (item, fieldObj) => {
  if (fieldObj) {
    item.options = JSON.parse(JSON.stringify(valueOptions.value))
    item.conditions &&
      item.conditions.forEach(ele => {
        ele.term = ''
      })
  }
}

const isSymbolicMap = computed(() => {
  return props.chart.type === 'symbolic-map'
})

const isProgressBar = computed(() => {
  return props.chart.type === 'progress-bar'
})

const isBidirectionalBar = computed(() => {
  return props.chart.type === 'bidirectional-bar'
})

const isRangeBar = computed(() => {
  return props.chart.type === 'bar-range'
})

const isBarOrHorizontal = computed(() => {
  return [
    'bar',
    'bar-group',
    'waterfall',
    'bar-horizontal',
    'bidirectional-bar',
    'progress-bar'
  ].includes(props.chart.type)
})

const initFields = () => {
  let fields = []
  if (isSymbolicMap.value) {
    const extBubble = JSON.parse(JSON.stringify(props.chart.extBubble))
    fields = [...extBubble]
  } else if (isProgressBar.value) {
    const yAxisExt = JSON.parse(JSON.stringify(props.chart.yAxisExt))
    fields = [...yAxisExt]
  } else if (isBidirectionalBar.value || isRangeBar.value) {
    const yAxis = JSON.parse(JSON.stringify(props.chart.yAxis))
    const yAxisExt = JSON.parse(JSON.stringify(props.chart.yAxisExt))
    fields = [...yAxis, ...yAxisExt]
  } else {
    const yAxis = JSON.parse(JSON.stringify(props.chart.yAxis))
    fields = [...yAxis]
  }
  state.fields.splice(0, state.fields.length, ...fields)
  // 字段不存在时
  let change = false
  state.thresholdArr.forEach(item => {
    item.options = JSON.parse(JSON.stringify(valueOptions.value))
    const fieldItemObj = state.fields.filter(ele => ele.id === item.fieldId)
    if (fieldItemObj.length === 0) {
      change = true
      item.fieldId = null
    }
  })
  if (change) {
    changeThreshold()
  }
}
const addThreshold = () => {
  state.thresholdArr.push(JSON.parse(JSON.stringify(state.thresholdObj)))
  changeThreshold()
}
const removeThreshold = index => {
  state.thresholdArr.splice(index, 1)
  changeThreshold()
}

const changeThreshold = () => {
  emit('onLineThresholdChange', state.thresholdArr)
}

const addConditions = item => {
  const newCondition = JSON.parse(JSON.stringify(thresholdCondition))
  item.conditions.push(newCondition)
  changeThreshold()
}
const removeCondition = (item, index) => {
  item.conditions.splice(index, 1)
  changeThreshold()
}

const addField = item => {
  // get field
  if (state.fields && state.fields.length > 0) {
    state.fields.forEach(ele => {
      if (item.fieldId === ele.id) {
        item.field = JSON.parse(JSON.stringify(ele))
        initOptions(item, item.field)
      }
    })
  }
  changeThreshold()
}

const fieldOptions = [{ label: t('chart.field_fixed'), value: 'fixed' }]

const isNotEmptyAndNull = item => {
  return !item.term.includes('null') && !item.term.includes('empty')
}

const isBetween = item => {
  return item.term === 'between'
}

const isDynamic = item => {
  return item.type === 'dynamic'
}

const getFieldOptions = fieldItem => {
  const deType = state.fields.filter(ele => ele.id === fieldItem.fieldId)?.[0]?.deType
  if (deType === 1) {
    return fieldOptions.filter(ele => ele.value === 'fixed')
  } else {
    return fieldOptions
  }
}

init()
</script>

<template>
  <el-col>
    <div class="tip">
      <Icon name="icon_info_filled" class="icon-style"
        ><icon_info_filled class="svg-icon icon-style"
      /></Icon>
      <span style="padding-left: 10px">{{ t('chart.table_threshold_tip') }}</span>
    </div>

    <div @keydown.stop @keyup.stop style="max-height: 50vh; overflow-y: auto">
      <div
        v-for="(fieldItem, fieldIndex) in state.thresholdArr"
        :key="fieldIndex"
        class="field-item"
      >
        <el-row style="margin-top: 6px; align-items: center; justify-content: space-between">
          <el-form-item class="form-item">
            <el-select
              style="width: 181px"
              v-model="fieldItem.fieldId"
              @change="addField(fieldItem)"
            >
              <el-option
                class="series-select-option"
                v-for="fieldOption in state.fields"
                :key="fieldOption.id"
                :label="fieldOption.name"
                :value="fieldOption.id"
                :disabled="chart.type === 'table-info' && fieldOption.deType === 7"
              >
                <el-icon style="margin-right: 8px">
                  <Icon
                    ><component
                      :class="`field-icon-${
                        fieldType[[2, 3].includes(fieldOption.deType) ? 2 : 0]
                      }`"
                      class="svg-icon"
                      :is="iconFieldMap[fieldType[fieldOption.deType]]"
                    ></component
                  ></Icon>
                </el-icon>
                {{ fieldOption.name }}
              </el-option>
            </el-select>
          </el-form-item>

          <el-button
            class="circle-button m-icon-btn"
            text
            :style="{ float: 'right' }"
            @click="removeThreshold(fieldIndex)"
          >
            <el-icon size="20px" style="color: #646a73">
              <Icon name="icon_delete-trash_outlined"
                ><icon_deleteTrash_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </el-button>
        </el-row>

        <el-row :style="{ marginTop: '16px', borderTop: '1px solid #d5d6d8' }">
          <el-row
            v-for="(item, index) in fieldItem.conditions"
            :key="index"
            class="line-item"
            :gutter="12"
          >
            <el-col :span="4">
              <el-form-item class="form-item">
                <el-select v-model="item.term" @change="changeThreshold">
                  <el-option-group
                    v-for="(group, idx) in fieldItem.options"
                    :key="idx"
                    :label="group.label"
                  >
                    <el-option
                      v-for="opt in group.options"
                      :key="opt.value"
                      :label="opt.label"
                      :value="opt.value"
                    />
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="4" v-if="isNotEmptyAndNull(item)" style="padding-left: 0 !important">
              <el-form-item class="form-item">
                <el-select v-model="item.type" class="select-item" style="width: 100%">
                  <el-option
                    v-for="opt in getFieldOptions(fieldItem)"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <!--不是between 不是动态值-->
            <el-col
              v-if="isNotEmptyAndNull(item) && !isBetween(item) && !isDynamic(item)"
              :span="12"
              style="text-align: center"
            >
              <el-form-item class="form-item">
                <el-input-number
                  v-model="item.value"
                  v-if="[2, 3].includes(fieldItem.field.deType)"
                  :placeholder="t('chart.drag_block_label_value')"
                  controls-position="right"
                  class="value-item"
                  clearable
                  @change="changeThreshold"
                />
                <el-input
                  v-model="item.value"
                  v-else
                  :placeholder="t('chart.drag_block_label_value')"
                  controls-position="right"
                  clearable
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>
            <!--between 开始值-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && !isDynamic(item)"
              :span="5"
              style="text-align: center"
            >
              <el-form-item class="form-item">
                <el-input-number
                  v-model="item.min"
                  controls-position="right"
                  class="between-item"
                  :placeholder="t('chart.axis_value_min')"
                  clearable
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>
            <el-col
              v-if="isBetween(item) && !isDynamic(item)"
              :span="2"
              style="margin-top: 4px; text-align: center"
            >
              <span style="margin: 0 -5px">
                ≤&nbsp;{{ t('chart.drag_block_label_value') }}&nbsp;≤
              </span>
            </el-col>
            <!--between 结束值-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && !isDynamic(item)"
              :span="5"
              style="text-align: center"
            >
              <el-form-item class="form-item">
                <el-input-number
                  v-model="item.max"
                  controls-position="right"
                  class="between-item"
                  :placeholder="t('chart.axis_value_max')"
                  clearable
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>

            <el-col :span="3">
              <el-form-item
                class="form-item"
                :label="
                  isSymbolicMap || isBarOrHorizontal ? t('chart.color') : t('chart.textColor')
                "
              >
                <el-color-picker
                  is-custom
                  :trigger-width="60"
                  v-model="item.color"
                  show-alpha
                  class="color-picker-style"
                  :predefine="predefineColors"
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>
            <el-col :span="1">
              <div style="display: flex; align-items: center; justify-content: center">
                <el-button
                  class="circle-button m-icon-btn"
                  text
                  @click="removeCondition(fieldItem, index)"
                >
                  <el-icon size="20px" style="color: #646a73">
                    <Icon name="icon_delete-trash_outlined"
                      ><icon_deleteTrash_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </el-button>
              </div>
            </el-col>
          </el-row>
        </el-row>

        <el-button
          style="margin-top: 10px"
          class="circle-button"
          type="primary"
          text
          @click="addConditions(fieldItem)"
        >
          <template #icon>
            <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
          </template>
          {{ t('chart.add_style') }}
        </el-button>
      </div>
    </div>

    <el-button
      class="circle-button"
      text
      type="primary"
      style="margin-top: 10px"
      @click="addThreshold"
    >
      <template #icon>
        <Icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></Icon>
      </template>
      {{ t('chart.add_condition') }}
    </el-button>
  </el-col>
</template>

<style lang="less" scoped>
.field-item {
  width: 100%;
  border-radius: 4px;
  padding: 10px 16px;
  margin-top: 10px;
  background: #f5f6f7;
}

.line-item {
  width: 100%;
  display: flex;
  justify-content: left;
  align-items: center;
  margin-top: 16px;
}

.form-item {
  height: 28px !important;
  :deep(.el-form-item__label) {
    font-size: 12px;
  }
}

span {
  font-size: 12px;
}

.value-item {
  position: relative;
  display: inline-block;
  width: 100% !important;
}

.between-item {
  position: relative;
  display: inline-block;
  width: 100% !important;
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100% !important;
}

.el-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
  width: 28px;
  height: 28px;
}

.color-picker-style :deep(.el-color-picker__trigger) {
  width: 28px;
  height: 28px;
}

.color-title {
  color: #646a73;
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
  padding: 0 8px;
}

.tip {
  font-size: 12px;
  background: #d6e2ff;
  border-radius: 4px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
}

:deep(.ed-form-item) {
  margin-bottom: 0 !important;
}

.icon-style {
  width: 14px;
  height: 14px;
  color: var(--ed-color-primary);
}

.m-icon-btn {
  &:hover {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:focus {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:active {
    background: rgba(31, 35, 41, 0.2) !important;
  }
}

.series-select-option {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0 11px;
}
</style>
