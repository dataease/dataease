<script lang="tsx" setup>
import icon_info_filled from '@/assets/svg/icon_info_filled.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { PropType, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '../../../util/chart'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { cloneDeep } from 'lodash-es'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  threshold: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['onTableThresholdChange'])

const thresholdCondition = {
  term: 'eq',
  field: '0',
  value: '0',
  color: '#ff0000ff',
  backgroundColor: '#ffffff00',
  min: '0',
  max: '1',
  type: 'fixed',
  dynamicField: { summary: 'value' },
  dynamicMinField: { summary: 'value' },
  dynamicMaxField: { summary: 'value' }
}
const textOptions = [
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
        value: 'like',
        label: t('chart.filter_like')
      },
      {
        value: 'not like',
        label: t('chart.filter_not_like')
      }
    ]
  },
  {
    label: '',
    options: [
      {
        value: 'null',
        label: t('chart.filter_null')
      },
      {
        value: 'not_null',
        label: t('chart.filter_not_null')
      }
    ]
  }
]
const dateOptions = [
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
  }
]
const valueOptions = [
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
const predefineColors = COLOR_PANEL

const state = reactive({
  thresholdArr: [] as TableThreshold[],
  fields: [],
  thresholdObj: {
    fieldId: '',
    field: {},
    conditions: []
  } as TableThreshold
})

const init = () => {
  state.thresholdArr = JSON.parse(JSON.stringify(props.threshold)) as TableThreshold[]
  initFields()
}
const initOptions = (item, fieldObj) => {
  if (fieldObj) {
    if ([0, 5, 7].includes(fieldObj.deType)) {
      item.options = JSON.parse(JSON.stringify(textOptions))
    } else if (fieldObj.deType === 1) {
      item.options = JSON.parse(JSON.stringify(dateOptions))
      item.type = 'fixed'
    } else {
      item.options = JSON.parse(JSON.stringify(valueOptions))
    }
    item.conditions &&
      item.conditions.forEach(ele => {
        ele.term = ''
      })
  }
}
const initFields = () => {
  let fields = []
  if (props.chart.type === 'table-info') {
    fields = JSON.parse(JSON.stringify(props.chart.xAxis))
  } else if (props.chart.type === 'table-pivot') {
    const xAxis = JSON.parse(JSON.stringify(props.chart.xAxis))
    const xAxisExt = JSON.parse(JSON.stringify(props.chart.xAxisExt))
    const yAxis = JSON.parse(JSON.stringify(props.chart.yAxis))
    fields = [...xAxis, ...xAxisExt, ...yAxis]
  } else {
    const xAxis = JSON.parse(JSON.stringify(props.chart.xAxis))
    const yAxis = JSON.parse(JSON.stringify(props.chart.yAxis))
    fields = [...xAxis, ...yAxis]
  }
  state.fields.splice(0, state.fields.length, ...fields)
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
  emit('onTableThresholdChange', state.thresholdArr)
}

const addConditions = item => {
  const newCondition = JSON.parse(JSON.stringify(thresholdCondition))
  // 获取单元格默认背景颜色
  const tableCell = props.chart?.customAttr?.tableCell
  if (tableCell) {
    newCondition.backgroundColor = cloneDeep(tableCell.tableItemBgColor)
  }
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
      if (item.dynamicField?.fieldId === ele.id) {
        item.dynamicField.field = JSON.parse(JSON.stringify(ele))
        initOptions(item, item.dynamicField.field)
      }
      if (item.dynamicMinField?.fieldId === ele.id) {
        item.dynamicMinField.field = JSON.parse(JSON.stringify(ele))
        initOptions(item, item.dynamicMinField.field)
      }
      if (item.dynamicMaxField?.fieldId === ele.id) {
        item.dynamicMaxField.field = JSON.parse(JSON.stringify(ele))
        initOptions(item, item.dynamicMaxField.field)
      }
    })
  }
  changeThreshold()
}

const fieldOptions = [
  { label: t('chart.field_fixed'), value: 'fixed' },
  { label: t('chart.field_dynamic'), value: 'dynamic' }
]
const dynamicSummaryOptions = [
  {
    id: 'value',
    name: t('chart.field') + t('chart.drag_block_label_value')
  },
  {
    id: 'avg',
    name: t('chart.avg') + t('chart.drag_block_label_value')
  },
  {
    id: 'max',
    name: t('chart.max')
  },
  {
    id: 'min',
    name: t('chart.min')
  }
]

const getConditionsFields = (fieldItem, conditionItem, conditionItemField) => {
  const fieldItemDeType = state.fields.filter(ele => ele.id === fieldItem.fieldId)?.[0]?.deType
  if (fieldItemDeType === undefined || fieldItemDeType === null) {
    conditionItem.fieldId = null
    conditionItemField.fieldId = null
  }
  const result = state.fields.filter(item => item.deType === fieldItemDeType) ?? []
  if (!result.find(ele => ele.id === conditionItemField.fieldId)) {
    conditionItemField.fieldId = result[0]?.id
    addField(conditionItem)
  }
  return result
}

const getDynamicSummaryOptions = itemId => {
  const deType = state.fields.filter(ele => ele.id === itemId)?.[0]?.deType
  if (deType === 1) {
    // 时间
    return dynamicSummaryOptions.filter(ele => {
      return ele.id !== 'avg'
    })
  } else if (deType === 0 || deType === 5) {
    // 文本、地理位置
    return dynamicSummaryOptions.filter(ele => {
      return ele.id === 'value'
    })
  } else {
    return dynamicSummaryOptions
  }
}

const isNotEmptyAndNull = item => {
  return !item.term.includes('null') && !item.term.includes('empty')
}

const isBetween = item => {
  return item.term === 'between'
}

const isDynamic = item => {
  return item.type === 'dynamic'
}
const changeConditionItemType = item => {
  if (item.type === 'dynamic') {
    item.dynamicField.summary = 'value'
    item.dynamicMinField.summary = 'value'
    item.dynamicMaxField.summary = 'value'
  }
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
            <el-select v-model="fieldItem.fieldId" @change="addField(fieldItem)">
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
            <el-col :span="3">
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
            <el-col :span="2" v-if="isNotEmptyAndNull(item)" style="padding-left: 0 !important">
              <el-form-item class="form-item">
                <el-select
                  v-model="item.type"
                  class="select-item"
                  @change="changeConditionItemType(item)"
                  style="width: 100%"
                >
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
            <!--不是between 是动态值-->
            <!--动态值 字段-->
            <el-col v-if="isNotEmptyAndNull(item) && !isBetween(item) && isDynamic(item)" :span="6">
              <el-form-item class="form-item">
                <el-select
                  v-model="item.dynamicField.fieldId"
                  @change="addField(item)"
                  style="width: 100%"
                >
                  <el-option
                    class="series-select-option"
                    v-for="itemFieldOption in getConditionsFields(
                      fieldItem,
                      item,
                      item.dynamicField
                    )"
                    :key="itemFieldOption.id"
                    :label="itemFieldOption.name"
                    :value="itemFieldOption.id"
                    :disabled="chart.type === 'table-info' && itemFieldOption.deType === 7"
                  >
                    <el-icon style="margin-right: 8px">
                      <Icon
                        ><component
                          :class="`field-icon-${
                            fieldType[[2, 3].includes(itemFieldOption.deType) ? 2 : 0]
                          }`"
                          class="svg-icon"
                          :is="iconFieldMap[fieldType[itemFieldOption.deType]]"
                        ></component
                      ></Icon>
                    </el-icon>
                    {{ itemFieldOption.name }}
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <!--动态值聚合方式-->
            <el-col
              v-if="isNotEmptyAndNull(item) && !isBetween(item) && isDynamic(item)"
              :span="6"
              style="text-align: center"
            >
              <el-form-item class="form-item">
                <el-select
                  :placeholder="t('chart.aggregation')"
                  v-model="item.dynamicField.summary"
                  @change="changeThreshold"
                  style="width: 100%"
                >
                  <el-option
                    v-for="opt in getDynamicSummaryOptions(item.dynamicField.fieldId)"
                    :key="opt.id"
                    :label="opt.name"
                    :value="opt.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <!--是between 不是动态值-->
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

            <!--是between 动态值-->
            <!--开始值 动态值字段-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && isDynamic(item)"
              class="minField"
              :span="3"
            >
              <el-form-item class="form-item">
                <el-select v-model="item.dynamicMinField.fieldId" @change="addField(item)">
                  <el-option
                    class="series-select-option"
                    v-for="itemFieldOption in getConditionsFields(
                      fieldItem,
                      item,
                      item.dynamicMinField
                    )"
                    :key="itemFieldOption.id"
                    :label="itemFieldOption.name"
                    :value="itemFieldOption.id"
                    :disabled="chart.type === 'table-info' && itemFieldOption.deType === 7"
                  >
                    <el-icon style="margin-right: 8px">
                      <Icon
                        ><component
                          :class="`field-icon-${
                            fieldType[[2, 3].includes(itemFieldOption.deType) ? 2 : 0]
                          }`"
                          class="svg-icon"
                          :is="iconFieldMap[fieldType[itemFieldOption.deType]]"
                        ></component
                      ></Icon>
                    </el-icon>
                    {{ itemFieldOption.name }}
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <!--开始值 动态值聚合方式-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && isDynamic(item)"
              class="minValue"
              :span="2"
              style="padding-left: 0 !important"
            >
              <el-form-item class="form-item">
                <el-select v-model="item.dynamicMinField.summary" @change="changeThreshold">
                  <el-option
                    v-for="opt in getDynamicSummaryOptions(item.dynamicMinField.fieldId)"
                    :key="opt.id"
                    :label="opt.name"
                    :value="opt.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col
              v-if="isBetween(item) && isDynamic(item)"
              class="term"
              :span="2"
              style="margin-top: 4px; text-align: center"
            >
              <span style="margin: 0 -5px">
                ≤&nbsp;{{ t('chart.drag_block_label_value') }}&nbsp;≤
              </span>
            </el-col>
            <!--结束值 动态值字段-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && isDynamic(item)"
              class="maxField"
              :span="3"
            >
              <el-form-item class="form-item">
                <el-select v-model="item.dynamicMaxField.fieldId" @change="addField(item)">
                  <el-option
                    class="series-select-option"
                    v-for="itemFieldOption in getConditionsFields(
                      fieldItem,
                      item,
                      item.dynamicMaxField
                    )"
                    :key="itemFieldOption.id"
                    :label="itemFieldOption.name"
                    :value="itemFieldOption.id"
                    :disabled="chart.type === 'table-info' && itemFieldOption.deType === 7"
                  >
                    <el-icon style="margin-right: 8px">
                      <Icon
                        ><component
                          :class="`field-icon-${
                            fieldType[[2, 3].includes(itemFieldOption.deType) ? 2 : 0]
                          }`"
                          class="svg-icon"
                          :is="iconFieldMap[fieldType[itemFieldOption.deType]]"
                        ></component
                      ></Icon>
                    </el-icon>
                    {{ itemFieldOption.name }}
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <!--结束值 动态值聚合方式-->
            <el-col
              v-if="isNotEmptyAndNull(item) && isBetween(item) && isDynamic(item)"
              class="maxValue"
              :span="2"
              style="padding-left: 0 !important"
            >
              <el-form-item class="form-item">
                <el-select v-model="item.dynamicMaxField.summary" @change="changeThreshold">
                  <el-option
                    v-for="opt in getDynamicSummaryOptions(item.dynamicMaxField.fieldId)"
                    :key="opt.id"
                    :label="opt.name"
                    :value="opt.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item class="form-item" :label="t('chart.textColor')">
                <el-color-picker
                  is-custom
                  size="large"
                  v-model="item.color"
                  show-alpha
                  class="color-picker-style"
                  :predefine="predefineColors"
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>
            <el-col :span="3">
              <el-form-item class="form-item" :label="t('chart.backgroundColor')">
                <el-color-picker
                  is-custom
                  size="large"
                  v-model="item.backgroundColor"
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
