<script lang="tsx" setup>
import { computed, onMounted, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { fieldType } from '@/utils/attr'
import { find } from 'lodash-es'

const { t } = useI18n()

const props = defineProps({
  line: {
    type: Array,
    required: true
  },
  quotaFields: {
    type: Array,
    required: true
  },
  quotaExtFields: {
    type: Array,
    required: true
  },
  useQuotaExt: {
    type: Boolean,
    default: false
  }
})

const yAxisTypes = [
  { type: 'left', name: t('chart.drag_block_value_axis_left') },
  { type: 'right', name: t('chart.drag_block_value_axis_right') }
]

const state = reactive({
  lineArr: [],
  lineObj: {
    name: '辅助线',
    field: '0', // 固定值
    fieldId: '',
    summary: 'avg',
    axis: 'y', // 主轴
    yAxisType: 'left',
    value: '0',
    lineType: 'solid',
    color: '#ff0000',
    curField: {},
    fontSize: '10'
  },
  fieldOptions: [
    { label: t('chart.field_fixed'), value: '0' },
    { label: t('chart.field_dynamic'), value: '1' }
  ],
  lineOptions: [
    { label: t('chart.line_type_solid'), value: 'solid' },
    { label: t('chart.line_type_dashed'), value: 'dashed' },
    { label: t('chart.line_type_dotted'), value: 'dotted' }
  ],
  predefineColors: COLOR_PANEL,
  fontSize: []
})

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  return arr
})

const emit = defineEmits(['onAssistLineChange'])

const init = () => {
  state.lineArr = JSON.parse(JSON.stringify(props.line))

  state.lineArr.forEach(line => {
    if (props.useQuotaExt) {
      if (
        line.yAxisType === 'left' &&
        find(props.quotaFields, d => d.id === line.fieldId) == undefined
      ) {
        line.fieldId = undefined
      }
      if (
        line.yAxisType === 'right' &&
        find(props.quotaExtFields, d => d.id === line.fieldId) == undefined
      ) {
        line.fieldId = undefined
      }
    } else {
      if (find(props.quotaFields, d => d.id === line.fieldId) == undefined) {
        line.fieldId = undefined
      }
    }
  })

  changeAssistLine()
}

const addLine = () => {
  const obj = {
    ...state.lineObj,
    curField: props.quotaFields ? props.quotaFields[0] : null,
    fieldId: props.quotaFields ? props.quotaFields[0]?.id : null
  }
  state.lineArr.push(JSON.parse(JSON.stringify(obj)))
  changeAssistLine()
}
const removeLine = index => {
  state.lineArr.splice(index, 1)
  changeAssistLine()
}

const changeYAxisType = item => {
  if (props.useQuotaExt && item.yAxisType === 'right') {
    item.fieldId = props.quotaExtFields ? props.quotaExtFields[0]?.id : null
    item.curField = getQuotaExtField(item.fieldId)
  } else {
    item.fieldId = props.quotaFields ? props.quotaFields[0]?.id : null
    item.curField = getQuotaField(item.fieldId)
  }
  changeAssistLine()
}

const changeAssistLine = () => {
  emit('onAssistLineChange', state.lineArr)
}
const changeAssistLineField = item => {
  if (props.useQuotaExt && item.yAxisType === 'right') {
    item.curField = getQuotaExtField(item.fieldId)
  } else {
    item.curField = getQuotaField(item.fieldId)
  }
  changeAssistLine()
}

const getQuotaField = id => {
  if (!id) {
    return {}
  }
  const fields = props.quotaFields.filter(ele => {
    return ele.id === id
  })
  if (fields.length === 0) {
    return {}
  } else {
    return fields[0]
  }
}

const getQuotaExtField = id => {
  if (!id) {
    return {}
  }
  const fields = props.quotaExtFields.filter(ele => {
    return ele.id === id
  })
  if (fields.length === 0) {
    return {}
  } else {
    return fields[0]
  }
}

onMounted(() => {
  init()
})
</script>

<template>
  <div>
    <div @keydown.stop @keyup.stop style="max-height: 50vh; margin-top: -4px; overflow-y: auto">
      <el-row v-for="(item, index) in state.lineArr" :key="index" class="line-item" :gutter="8">
        <el-col :span="4">
          <el-input
            v-model="item.name"
            class="value-item"
            style="width: 100% !important"
            :placeholder="t('chart.name')"
            clearable
            @change="changeAssistLine"
          />
        </el-col>
        <el-col v-if="useQuotaExt" :span="3">
          <el-select v-model="item.yAxisType" class="select-item" @change="changeYAxisType(item)">
            <el-option
              v-for="opt in yAxisTypes"
              :key="opt.type"
              :label="opt.name"
              :value="opt.type"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="item.field" class="select-item" @change="changeAssistLine">
            <el-option
              v-for="opt in state.fieldOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col v-if="item.field === '0'" :span="7">
          <el-input-number
            v-model="item.value"
            controls-position="right"
            class="value-item"
            :placeholder="t('chart.drag_block_label_value')"
            clearable
            @change="changeAssistLine"
          />
        </el-col>
        <el-col v-if="item.field === '1'" :span="4">
          <el-select
            v-model="item.fieldId"
            class="select-item"
            :placeholder="t('chart.field')"
            @change="changeAssistLineField(item)"
          >
            <el-option
              v-for="quota in useQuotaExt && item.yAxisType === 'right'
                ? quotaExtFields
                : quotaFields"
              :key="quota.id"
              :label="quota.name"
              :value="quota.id"
            >
              <span style="float: left">
                <el-icon>
                  <Icon
                    :className="`field-icon-${fieldType[item.deType]}`"
                    :name="`field_${fieldType[item.deType]}`"
                  />
                </el-icon>
              </span>
              <span :style="{ float: 'left', color: '#8492a6', fontSize: '12px' }">
                {{ quota.name }}
              </span>
            </el-option>
          </el-select>
        </el-col>
        <el-col v-if="item.field === '1'" :span="3">
          <el-select
            v-model="item.summary"
            class="select-item"
            :placeholder="t('chart.aggregation')"
            @change="changeAssistLine"
          >
            <el-option key="avg" value="avg" label="平均值" />
            <el-option key="max" value="max" :label="t('chart.max')" />
            <el-option key="min" value="min" :label="t('chart.min')" />
          </el-select>
        </el-col>
        <el-col :span="useQuotaExt ? 2 : 3">
          <el-tooltip effect="dark" content="字号" placement="top">
            <el-select
              v-model="item.fontSize"
              class="select-item"
              :placeholder="t('chart.text_fontsize')"
              @change="changeAssistLine"
            >
              <el-option
                v-for="option in fontSizeList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-tooltip>
        </el-col>
        <el-col :span="useQuotaExt ? 2 : 4">
          <el-select v-model="item.lineType" class="select-item" @change="changeAssistLine">
            <el-option
              v-for="opt in state.lineOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col :span="2" style="text-align: center">
          <el-color-picker
            is-custom
            size="large"
            v-model="item.color"
            class="color-picker-style"
            :predefine="state.predefineColors"
            @change="changeAssistLine"
          />
        </el-col>
        <el-col :span="1">
          <div class="flex-align-center" style="margin-left: -4px">
            <el-icon
              style="width: 28px !important; height: 28px !important; font-size: 20px !important"
              class="hover-icon"
              @click="removeLine(index)"
            >
              <Icon name="icon_delete-trash_outlined"></Icon>
            </el-icon>
          </div>
        </el-col>
      </el-row>
    </div>
    <el-button class="circle-button" text style="margin-left: 5px" @click="addLine">
      <template #icon>
        <Icon name="icon_add_outlined"></Icon>
      </template>
      {{ t('chart.add_assist_line') }}
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.line-item {
  width: 100%;
  border-radius: 4px;
  padding: 4px;
  display: flex;
  justify-content: left;
  align-items: center;
  &:last-child {
    margin-bottom: 4px;
  }

  :deep(input) {
    font-size: 14px !important;
    line-height: 22px;
  }
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

span {
  font-size: 12px;
}

.value-item {
  position: relative;
  display: inline-block;
  width: 100% !important;
  :deep(.ed-input-number__increase) {
    top: 1.4px;
  }
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100% !important;
}

.ed-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}
</style>
