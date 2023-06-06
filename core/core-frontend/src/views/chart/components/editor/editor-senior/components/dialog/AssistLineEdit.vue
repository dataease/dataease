<script lang="tsx" setup>
import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  line: {
    type: Array,
    required: true
  },
  quotaFields: {
    type: Array,
    required: true
  }
})

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const state = reactive({
  lineArr: [],
  lineObj: {
    name: '辅助线',
    field: '0', // 固定值
    fieldId: '',
    summary: 'avg',
    axis: 'y', // 主轴
    value: '0',
    lineType: 'solid',
    color: '#ff0000',
    curField: {},
    fontSize: '10'
  },
  fieldOptions: [
    { label: t('chart.field_fixed'), value: '0' }
    // { label: t('chart.field_dynamic'), value: '1' }
  ],
  lineOptions: [
    { label: t('chart.line_type_solid'), value: 'solid' },
    { label: t('chart.line_type_dashed'), value: 'dashed' },
    { label: t('chart.line_type_dotted'), value: 'dotted' }
  ],
  predefineColors: COLOR_PANEL,
  quotaData: [],
  fontSize: []
})

const emit = defineEmits(['onAssistLineChange'])

const initField = () => {
  state.quotaData = props.quotaFields.filter(ele => !ele.chartId && ele.id !== 'count')
}
const init = () => {
  state.lineArr = JSON.parse(JSON.stringify(props.line))

  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}

const addLine = () => {
  const obj = {
    ...state.lineObj,
    curField: state.quotaData ? state.quotaData[0] : null,
    fieldId: state.quotaData ? state.quotaData[0]?.id : null
  }
  state.lineArr.push(JSON.parse(JSON.stringify(obj)))
  changeAssistLine()
}
const removeLine = index => {
  state.lineArr.splice(index, 1)
  changeAssistLine()
}

const changeAssistLine = () => {
  emit('onAssistLineChange', state.lineArr)
}
const changeAssistLineField = item => {
  item.curField = getQuotaField(item.fieldId)
  changeAssistLine()
}
const getQuotaField = id => {
  if (!id) {
    return {}
  }
  const fields = state.quotaData.filter(ele => {
    return ele.id === id
  })
  if (fields.length === 0) {
    return {}
  } else {
    return fields[0]
  }
}

initField()
init()
</script>

<template>
  <el-col>
    <el-button circle size="small" style="margin-bottom: 10px" @click="addLine">
      <template #icon>
        <Icon name="icon_add_outlined"></Icon>
      </template>
    </el-button>
    <div style="max-height: 50vh; overflow-y: auto">
      <el-row v-for="(item, index) in state.lineArr" :key="index" class="line-item">
        <el-col :span="4">
          <el-input
            v-model="item.name"
            class="value-item"
            style="width: 90% !important"
            :placeholder="t('chart.name')"
            size="mini"
            clearable
            @change="changeAssistLine"
          />
        </el-col>
        <el-col :span="4">
          <el-select
            v-model="item.field"
            size="mini"
            class="select-item"
            @change="changeAssistLine"
          >
            <el-option
              v-for="opt in state.fieldOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col v-if="item.field === '0'" :span="6">
          <el-input
            v-model="item.value"
            class="value-item"
            :placeholder="t('chart.drag_block_label_value')"
            size="mini"
            clearable
            @change="changeAssistLine"
          />
        </el-col>
        <el-col v-if="item.field === '1'" :span="6">
          <el-select
            v-model="item.fieldId"
            size="mini"
            class="select-item"
            :placeholder="t('chart.field')"
            @change="changeAssistLineField(item)"
          >
            <el-option
              v-for="quota in state.quotaData"
              :key="quota.id"
              :label="quota.name"
              :value="quota.id"
            >
              <span style="float: left">
                <el-icon>
                  <Icon
                    :className="`field-icon-${fieldType(item.deType)}`"
                    :name="`field_${fieldType(item.deType)}`"
                  ></Icon>
                </el-icon>
              </span>
              <span :style="{ float: 'left', color: '#8492a6', fontSize: '12px' }">{{
                quota.name
              }}</span>
            </el-option>
          </el-select>
          <el-select
            v-model="item.summary"
            size="mini"
            class="select-item"
            style="margin-left: 10px"
            :placeholder="t('chart.aggregation')"
            @change="changeAssistLine"
          >
            <el-option key="avg" value="avg" :label="t('chart.avg')" />
            <el-option key="max" value="max" :label="t('chart.max')" />
            <el-option key="min" value="min" :label="t('chart.min')" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select
            v-model="item.fontSize"
            size="mini"
            class="select-item"
            style="margin-left: 10px"
            :placeholder="t('chart.text_fontsize')"
            @change="changeAssistLine"
          >
            <el-option
              v-for="option in state.fontSize"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select
            v-model="item.lineType"
            size="mini"
            class="select-item"
            @change="changeAssistLine"
          >
            <el-option
              v-for="opt in state.lineOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-col>
        <el-col :span="1" style="text-align: center">
          <el-color-picker
            v-model="item.color"
            class="color-picker-style"
            :predefine="state.predefineColors"
            @change="changeAssistLine"
          />
        </el-col>
        <el-col :span="1">
          <el-button type="text" circle style="float: right" @click="removeLine(index)">
            <template #icon>
              <Icon name="icon_delete-trash_outlined"></Icon>
            </template>
          </el-button>
        </el-col>
      </el-row>
    </div>
  </el-col>
</template>

<style lang="less" scoped>
.line-item {
  width: 100%;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  padding: 4px 14px;
  margin-bottom: 10px;
  display: flex;
  justify-content: left;
  align-items: center;
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
  width: 100px !important;
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
}

.ed-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
  width: 28px;
  height: 28px;
  margin-top: 6px;
}

.color-picker-style :deep(.ed-color-picker__trigger) {
  width: 28px;
  height: 28px;
}
</style>
