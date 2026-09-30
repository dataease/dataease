<script lang="tsx" setup>
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { computed, onMounted, PropType, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { find } from 'lodash-es'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
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
  collapsed: new Set<object>(),
  lineObj: {
    name: t('chart.assist_line'),
    field: '0', // 固定值
    fieldId: '',
    boundFieldId: '',
    followField: true,
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
  state.collapsed = new Set(state.lineArr.slice(1))

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
  state.collapsed.delete(state.lineArr[index])
  state.lineArr.splice(index, 1)
  changeAssistLine()
}

const changeYAxisType = item => {
  item.boundFieldId = ''
  if (props.useQuotaExt && item.yAxisType === 'right') {
    item.fieldId = props.quotaExtFields ? props.quotaExtFields[0]?.id : null
    item.curField = getQuotaExtField(item.fieldId)
  } else {
    item.fieldId = props.quotaFields ? props.quotaFields[0]?.id : null
    item.curField = getQuotaField(item.fieldId)
  }
  changeAssistLine()
}

const toggleLine = item => {
  if (state.collapsed.has(item)) state.collapsed.delete(item)
  else state.collapsed.add(item)
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

const getFieldOptions = computed(() => {
  if (['percentage-bar-stack', 'percentage-bar-stack-horizontal'].includes(props.chart.type)) {
    return state.fieldOptions.filter(item => item.value === '0')
  }
  return state.fieldOptions
})

onMounted(() => {
  init()
})
</script>

<template>
  <div class="assist-editor">
    <div class="assist-list" @keydown.stop @keyup.stop>
      <section v-for="(item, index) in state.lineArr" :key="index" class="assist-card">
        <div class="assist-card-header">
          <button
            type="button"
            class="collapse-button"
            :aria-expanded="!state.collapsed.has(item)"
            :aria-label="item.name || t('chart.assist_line')"
            @click="toggleLine(item)"
          >
            <span class="chevron" :class="{ collapsed: state.collapsed.has(item) }" />
          </button>
          <span
            class="line-preview"
            :style="{ borderColor: item.color, borderTopStyle: item.lineType }"
          />
          <el-input
            v-model="item.name"
            class="line-name"
            :aria-label="t('chart.name')"
            :placeholder="t('chart.name')"
            clearable
            @change="changeAssistLine"
          />
          <span class="type-status" :class="{ dynamic: item.field === '1' }">
            {{ t(item.field === '0' ? 'chart.field_fixed' : 'chart.field_dynamic') }}
          </span>
          <span v-if="state.collapsed.has(item)" class="line-summary">
            {{ item.field === '0' ? item.value : item.curField?.name }}
          </span>
          <el-button
            text
            class="delete-button"
            :aria-label="t('commons.delete')"
            @click="removeLine(index)"
          >
            <el-icon
              ><Icon><icon_deleteTrash_outlined class="svg-icon" /></Icon
            ></el-icon>
          </el-button>
        </div>
        <div v-show="!state.collapsed.has(item)" class="assist-card-body">
          <div class="field-grid" :class="{ 'dual-dynamic': useQuotaExt && item.field === '1' }">
            <div class="config-field">
              <label>{{ t('chart.assist_value_type') }}</label>
              <el-select v-model="item.field" @change="changeAssistLine">
                <el-option
                  v-for="opt in getFieldOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </div>
            <div v-if="item.field === '0'" class="config-field">
              <label>{{ t('chart.drag_block_label_value') }}</label>
              <el-input-number
                v-model="item.value"
                controls-position="right"
                @change="changeAssistLine"
              />
            </div>
            <template v-else>
              <div class="config-field">
                <label>{{ t('chart.field') }}</label>
                <el-select
                  v-model="item.fieldId"
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
                  />
                </el-select>
              </div>
              <div class="config-field">
                <label>{{ t('chart.aggregation') }}</label>
                <el-select v-model="item.summary" @change="changeAssistLine">
                  <el-option
                    v-for="summary in ['avg', 'max', 'min', 'last_item']"
                    :key="summary"
                    :value="summary"
                    :label="t('chart.' + summary)"
                  />
                </el-select>
              </div>
            </template>
            <div v-if="useQuotaExt" class="config-field">
              <label>{{ t('chart.assist_axis') }}</label>
              <el-select v-model="item.yAxisType" @change="changeYAxisType(item)">
                <el-option
                  v-for="opt in yAxisTypes"
                  :key="opt.type"
                  :label="opt.name"
                  :value="opt.type"
                />
              </el-select>
            </div>
          </div>
          <div class="field-grid style-grid">
            <div class="config-field">
              <label>{{ t('chart.assist_line_style') }}</label>
              <el-select v-model="item.lineType" @change="changeAssistLine">
                <el-option
                  v-for="opt in state.lineOptions"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </div>
            <div class="config-field">
              <label>{{ t('chart.assist_color') }}</label>
              <el-color-picker
                is-custom
                :trigger-width="80"
                v-model="item.color"
                :predefine="state.predefineColors"
                @change="changeAssistLine"
              />
            </div>
            <div class="config-field">
              <label>{{ t('chart.font_size') }}</label>
              <el-select v-model="item.fontSize" @change="changeAssistLine">
                <el-option
                  v-for="option in fontSizeList"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </div>
          </div>
          <div class="binding-row">
            <template v-if="item.field === '0'">
              <label>{{ t('chart.assist_bind_field') }}</label>
              <el-select
                v-model="item.boundFieldId"
                clearable
                :placeholder="t('chart.assist_bind_field')"
                @change="changeAssistLine"
              >
                <el-option
                  v-for="quota in useQuotaExt && item.yAxisType === 'right'
                    ? quotaExtFields
                    : quotaFields"
                  :key="quota.id"
                  :label="quota.name"
                  :value="quota.id"
                />
              </el-select>
            </template>
            <el-checkbox
              v-model="item.followField"
              :disabled="item.field === '0' && !item.boundFieldId"
              @change="changeAssistLine"
            >
              {{ t('chart.assist_follow_field') }}
            </el-checkbox>
          </div>
        </div>
      </section>
    </div>
    <el-button text class="add-line" @click="addLine">
      <template #icon
        ><Icon><icon_add_outlined class="svg-icon" /></Icon
      ></template>
      {{ t('chart.add_assist_line') }}
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.assist-list {
  max-height: min(60vh, 620px);
  overflow-y: auto;
  padding-right: 4px;
}
.assist-card {
  border: 1px solid var(--ed-border-color, #dcdfe6);
  border-radius: 4px;
  margin-bottom: 12px;
  &:last-child {
    margin-bottom: 0;
  }
}
.assist-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: transparent;
}
.collapse-button {
  border: 0;
  background: transparent;
  color: var(--ed-text-color-primary);
  cursor: pointer;
  padding: 6px;
  display: flex;
}
.chevron {
  width: 7px;
  height: 7px;
  border-right: 1.5px solid currentColor;
  border-bottom: 1.5px solid currentColor;
  transform: rotate(45deg);
  &.collapsed {
    transform: rotate(-45deg);
  }
}
.line-preview {
  width: 32px;
  border-top-width: 2px;
  flex-shrink: 0;
}
.line-name {
  width: 220px;
}
.type-status {
  padding: 1px 6px;
  border-radius: 2px;
  line-height: 22px;
  font-size: 14px;
  white-space: nowrap;
  background: var(--ed-fill-color);
  color: var(--ed-text-color-regular);
  &.dynamic {
    background: var(--ed-color-primary-light-9);
    color: var(--ed-color-primary);
  }
}
.line-summary {
  color: var(--ed-text-color-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.delete-button {
  color: var(--ed-text-color-secondary);
  margin-left: auto;
  padding: 6px;
}
.assist-card-body {
  border-top: 1px solid var(--ed-border-color);
  padding: 16px;
}
.field-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  &.dual-dynamic {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  gap: 16px;
  margin-bottom: 16px;
}
.style-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}
.config-field {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  :deep(.ed-select),
  :deep(.ed-input-number) {
    width: 100%;
  }
}
label {
  font-size: 14px;
  color: var(--ed-text-color-primary);
}
.binding-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  border-top: 1px solid var(--ed-border-color);
  padding-top: 12px;
  .ed-select {
    width: 220px;
  }
}
.add-line {
  margin-top: 12px;
  color: var(--ed-color-primary);
}
</style>
