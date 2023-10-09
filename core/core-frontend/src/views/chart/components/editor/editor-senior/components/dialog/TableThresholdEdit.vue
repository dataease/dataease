<script lang="tsx" setup>
import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '../../../util/chart'
import { fieldType } from '@/utils/attr'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
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
  max: '1'
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
  thresholdArr: [],
  fields: [],
  thresholdObj: {
    fieldId: '',
    field: {},
    conditions: []
  }
})

const init = () => {
  state.thresholdArr = JSON.parse(JSON.stringify(props.threshold))
  initFields()
}
const initOptions = item => {
  if (item.field) {
    if (item.field.deType === 0 || item.field.deType === 5) {
      item.options = JSON.parse(JSON.stringify(textOptions))
    } else if (item.field.deType === 1) {
      item.options = JSON.parse(JSON.stringify(dateOptions))
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
  // 暂时支持指标
  if (props.chart.type === 'table-info') {
    if (Object.prototype.toString.call(props.chart.xAxis) === '[object Array]') {
      state.fields = JSON.parse(JSON.stringify(props.chart.xAxis))
    } else {
      state.fields = JSON.parse(props.chart.xAxis)
    }
  } else if (props.chart.type === 'table-pivot') {
    if (Object.prototype.toString.call(props.chart.yAxis) === '[object Array]') {
      state.fields = JSON.parse(JSON.stringify(props.chart.yAxis))
    } else {
      state.fields = JSON.parse(props.chart.yAxis)
    }
  } else {
    if (Object.prototype.toString.call(props.chart.xAxis) === '[object Array]') {
      state.fields = state.fields.concat(JSON.parse(JSON.stringify(props.chart.xAxis)))
    } else {
      state.fields = state.fields.concat(JSON.parse(props.chart.xAxis))
    }
    if (Object.prototype.toString.call(props.chart.yAxis) === '[object Array]') {
      state.fields = state.fields.concat(JSON.parse(JSON.stringify(props.chart.yAxis)))
    } else {
      state.fields = state.fields.concat(JSON.parse(props.chart.yAxis))
    }
  }
  // 暂不支持时间
  // this.fields = this.fields.filter(ele => ele.deType !== 1)
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
  item.conditions.push(JSON.parse(JSON.stringify(thresholdCondition)))
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
        initOptions(item)
      }
    })
  }
  changeThreshold()
}

init()
</script>

<template>
  <el-col>
    <div class="tip">
      <Icon name="icon_info_filled" class="icon-style"></Icon>
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
                v-for="fieldOption in state.fields"
                :key="fieldOption.id"
                :label="fieldOption.name"
                :value="fieldOption.id"
              >
                <span style="float: left">
                  <el-icon>
                    <Icon
                      :className="`field-icon-${fieldType[fieldOption.deType]}`"
                      :name="`field_${fieldType[fieldOption.deType]}`"
                    ></Icon>
                  </el-icon>
                </span>
                <span :style="{ float: 'left', color: '#8492a6', fontSize: '12px' }">{{
                  fieldOption.name
                }}</span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-button
            class="circle-button"
            link
            :style="{ float: 'right' }"
            @click="removeThreshold(fieldIndex)"
          >
            <template #icon>
              <Icon name="icon_delete-trash_outlined"></Icon>
            </template>
          </el-button>
        </el-row>

        <el-row :style="{ marginTop: '16px', borderTop: '1px solid #d5d6d8' }">
          <el-row
            v-for="(item, index) in fieldItem.conditions"
            :key="index"
            class="line-item"
            :gutter="10"
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
            <el-col
              v-if="
                !item.term.includes('null') &&
                !item.term.includes('empty') &&
                item.term !== 'between'
              "
              :span="10"
              style="text-align: center"
            >
              <el-form-item class="form-item">
                <el-input-number
                  controls-position="right"
                  v-model="item.value"
                  class="value-item"
                  :placeholder="t('chart.drag_block_label_value')"
                  clearable
                  @change="changeThreshold"
                />
              </el-form-item>
            </el-col>

            <el-col v-if="item.term === 'between'" :span="4" style="text-align: center">
              <el-input-number
                v-model="item.min"
                controls-position="right"
                class="between-item"
                :placeholder="t('chart.axis_value_min')"
                clearable
                @change="changeThreshold"
              />
            </el-col>

            <el-col v-if="item.term === 'between'" :span="2" style="text-align: center">
              <span style="margin: 0 4px">
                ≤&nbsp;&nbsp;{{ t('chart.drag_block_label_value') }}&nbsp;&nbsp;≤
              </span>
            </el-col>

            <el-col v-if="item.term === 'between'" :span="4" style="text-align: center">
              <el-input-number
                v-model="item.max"
                controls-position="right"
                class="between-item"
                :placeholder="t('chart.axis_value_max')"
                clearable
                @change="changeThreshold"
              />
            </el-col>

            <el-col :span="3" style="display: flex; align-items: center; justify-content: center">
              <span class="color-title">{{ t('chart.textColor') }}</span>
              <el-color-picker
                is-custom
                v-model="item.color"
                show-alpha
                class="color-picker-style"
                :predefine="predefineColors"
                @change="changeThreshold"
              />
            </el-col>
            <el-col :span="3" style="display: flex; align-items: center; justify-content: center">
              <span class="color-title">{{ t('chart.backgroundColor') }}</span>
              <el-color-picker
                is-custom
                v-model="item.backgroundColor"
                show-alpha
                class="color-picker-style"
                :predefine="predefineColors"
                @change="changeThreshold"
              />
            </el-col>
            <el-col :span="2">
              <el-button class="circle-button" link @click="removeCondition(fieldItem, index)">
                <template #icon>
                  <Icon name="icon_delete-trash_outlined"></Icon>
                </template>
              </el-button>
            </el-col>
          </el-row>
        </el-row>

        <el-button
          style="margin-top: 10px"
          class="circle-button"
          type="primary"
          link
          @click="addConditions(fieldItem)"
        >
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          {{ t('chart.add_condition') }}
        </el-button>
      </div>
    </div>

    <el-button
      class="circle-button"
      link
      type="primary"
      style="margin-top: 10px"
      @click="addThreshold"
    >
      <template #icon>
        <Icon name="icon_add_outlined"></Icon>
      </template>
      {{ t('chart.add_threshold') }}
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
  margin-right: 6px;
  color: #909399;
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
  color: #3370ff;
}
</style>
