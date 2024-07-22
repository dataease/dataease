<script lang="ts" setup>
import { ElSelect } from 'element-plus-secondary'
import { computed, ref, toRefs } from 'vue'
import RangeFilterTime from '@/custom-component/v-query/RangeFilterTime.vue'
import { useI18n } from '@/hooks/web/useI18n'
import DynamicTime from '@/custom-component/v-query/DynamicTime.vue'
import DynamicTimeRange from '@/custom-component/v-query/DynamicTimeRange.vue'
import Time from '@/custom-component/v-query/Time.vue'
import Select from '@/custom-component/v-query/Select.vue'
import Tree from '@/custom-component/v-query/Tree.vue'
const { t } = useI18n()

const visiblePopover = ref(false)

const handleDialogClick = () => {
  visiblePopover.value = false
}

const handleVisiblePopover = ev => {
  ev.stopPropagation()
  visiblePopover.value = !visiblePopover.value
}

const filterTypeCom = computed(() => {
  const { displayType, timeType = 'fixed' } = curComponent.value
  return ['1', '7'].includes(displayType)
    ? timeType === 'dynamic'
      ? displayType === '1'
        ? DynamicTime
        : DynamicTimeRange
      : Time
    : '9' === displayType
    ? Tree
    : Select
})

const props = defineProps({
  curComponent: {
    type: Object,
    required: true
  },
  showPosition: {
    type: String,
    default: 'main'
  }
})

const showFlag = computed(() => props.showPosition === 'main')

const { curComponent } = toRefs(props)

const relativeToCurrentTypeList = computed(() => {
  if (!curComponent.value) return []
  let index = ['year', 'month', 'date', 'datetime'].indexOf(curComponent.value.timeGranularity) + 1
  if (+curComponent.value.displayType === 7) {
    index =
      ['yearrange', 'monthrange', 'daterange', 'datetimerange'].indexOf(
        curComponent.value.timeGranularityMultiple
      ) + 1
  }
  return [
    {
      label: '年',
      value: 'year'
    },
    {
      label: '月',
      value: 'month'
    },
    {
      label: '日',
      value: 'date'
    }
  ].slice(0, index)
})

const relativeToCurrentList = computed(() => {
  let list = []
  if (!curComponent.value) return list
  switch (curComponent.value.timeGranularity) {
    case 'year':
      list = [
        {
          label: '今年',
          value: 'thisYear'
        },
        {
          label: '去年',
          value: 'lastYear'
        }
      ]
      break
    case 'month':
      list = [
        {
          label: '本月',
          value: 'thisMonth'
        },
        {
          label: '上月',
          value: 'lastMonth'
        }
      ]
      break
    case 'date':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
          value: 'yearBeginning'
        }
      ]
      break
    case 'datetime':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
          value: 'yearBeginning'
        }
      ]
      break

    default:
      break
  }

  return [
    ...list,
    {
      label: '自定义',
      value: 'custom'
    }
  ]
})

const aroundList = [
  {
    label: '前',
    value: 'f'
  },
  {
    label: '后',
    value: 'b'
  }
]

const dynamicTime = computed(() => {
  const { displayType, timeType } = curComponent.value
  return timeType === 'dynamic' && [1, 7].includes(+displayType)
})

const operators = [
  {
    label: '精确匹配',
    value: 'eq'
  },
  {
    label: '模糊匹配',
    value: 'like'
  }
]

const multiple = ref(false)

const multipleChange = (val: boolean, isMultipleChange = false) => {
  if (isMultipleChange) {
    curComponent.value.defaultValue = val ? [] : undefined
  }
  const { defaultValue } = curComponent.value
  if (Array.isArray(defaultValue)) {
    curComponent.value.selectValue = val ? defaultValue : undefined
  } else {
    curComponent.value.selectValue = val
      ? defaultValue !== undefined
        ? [defaultValue]
        : []
      : defaultValue
  }

  if (curComponent.value.field.deType === 1) {
    curComponent.value.multiple = val
    return
  }

  curComponent.value.multiple = val
}

const changeMultiple = val => {
  multiple.value = val
}
const inputCom = ref()
const displayTypeChange = () => {
  inputCom.value?.displayTypeChange?.()
}

const mult = () => {
  inputCom.value?.mult?.handleClickOutside?.()
}

const single = () => {
  inputCom.value?.single?.handleClickOutside?.()
}

defineExpose({
  multipleChange,
  handleDialogClick,
  changeMultiple,
  displayTypeChange,
  mult,
  single
})
</script>

<template>
  <div class="list-item top-item" v-if="curComponent.displayType === '8'" @click.stop>
    <div class="label">设置默认值</div>
    <div class="value">
      <div class="condition-type">
        <el-select
          class="condition-value-select"
          popper-class="condition-value-select-popper"
          v-model="curComponent.defaultConditionValueOperatorF"
        >
          <el-option
            v-for="ele in operators"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          >
          </el-option>
        </el-select>
        <el-input class="condition-value-input" v-model="curComponent.defaultConditionValueF" />
        <div class="bottom-line"></div>
      </div>
      <div class="condition-type" v-if="[1, 2].includes(curComponent.conditionType)">
        <sapn class="condition-type-tip">{{ curComponent.conditionType === 1 ? '与' : '或' }}</sapn>
        <el-select
          class="condition-value-select"
          popper-class="condition-value-select-popper"
          v-model="curComponent.defaultConditionValueOperatorS"
        >
          <el-option
            v-for="ele in operators"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          >
          </el-option>
        </el-select>
        <el-input class="condition-value-input" v-model="curComponent.defaultConditionValueS" />
        <div class="bottom-line next-line"></div>
      </div>
    </div>
  </div>
  <div v-if="!['1', '7', '8'].includes(curComponent.displayType) && showFlag" class="list-item">
    <div class="label">选项类型</div>
    <div class="value">
      <el-radio-group
        class="larger-radio"
        @change="val => multipleChange(val as boolean, true)"
        v-model="multiple"
      >
        <el-radio :label="false">{{ t('visualization.single_choice') }}</el-radio>
        <el-radio :label="true">{{ t('visualization.multiple_choice') }}</el-radio>
      </el-radio-group>
    </div>
  </div>
  <div v-if="curComponent.displayType === '7' && showFlag" class="list-item">
    <div class="label">
      <el-checkbox v-model="curComponent.setTimeRange" label="设置时间筛选范围" />
    </div>
    <div class="setting-content">
      <el-popover
        :show-arrow="false"
        popper-class="range-filter-time"
        placement="bottom-start"
        :width="452"
        :visible="visiblePopover"
        :offset="4"
      >
        <template #reference>
          <el-button
            :disabled="!curComponent.setTimeRange"
            @click="handleVisiblePopover($event)"
            text
            style="margin-left: -4px"
          >
            <template #icon>
              <Icon name="icon_admin_outlined"></Icon>
            </template>
            设置
          </el-button>
        </template>
        <RangeFilterTime
          :timeRange="curComponent.timeRange"
          :timeGranularityMultiple="curComponent.timeGranularityMultiple"
        />
      </el-popover>
      <span
        v-if="
          curComponent.timeRange.intervalType !== 'none' || curComponent.timeRange.dynamicWindow
        "
        class="config-flag range-filter-time-flag"
        >已配置</span
      >
    </div>
  </div>
  <div
    class="list-item"
    v-if="+curComponent.displayType === 0 && curComponent.optionValueSource !== 1 && showFlag"
  >
    <div class="label">
      <el-tooltip
        effect="dark"
        content="绑定参数后，不支持传空数据"
        :disabled="!curComponent.parametersCheck"
        placement="top"
      >
        <el-checkbox
          :disabled="curComponent.parametersCheck"
          v-model="curComponent.showEmpty"
          label="选项值包含空数据"
        />
      </el-tooltip>
    </div>
  </div>
  <div v-if="!['8'].includes(curComponent.displayType)" class="list-item">
    <div class="label">
      <el-checkbox v-model="curComponent.defaultValueCheck" label="设置默认值" />
    </div>
    <div
      class="setting-content"
      v-if="curComponent.defaultValueCheck && ['1', '7'].includes(curComponent.displayType)"
    >
      <div class="setting">
        <el-radio-group v-model="curComponent.timeType">
          <el-radio label="fixed">固定时间</el-radio>
          <el-radio label="dynamic">动态时间</el-radio>
        </el-radio-group>
      </div>
      <template v-if="dynamicTime && curComponent.displayType === '1'">
        <div class="setting">
          <div class="setting-label">相对当前</div>
          <div class="setting-value select">
            <el-select @focus="handleDialogClick" v-model="curComponent.relativeToCurrent">
              <el-option
                v-for="item in relativeToCurrentList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
        </div>
        <div class="setting" v-if="curComponent.relativeToCurrent === 'custom'">
          <div
            class="setting-input"
            :class="curComponent.timeGranularity === 'datetime' && 'with-date'"
          >
            <el-input-number v-model="curComponent.timeNum" :min="0" controls-position="right" />
            <el-select @focus="handleDialogClick" v-model="curComponent.relativeToCurrentType">
              <el-option
                v-for="item in relativeToCurrentTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select @focus="handleDialogClick" v-model="curComponent.around">
              <el-option
                v-for="item in aroundList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-time-picker
              v-if="curComponent.timeGranularity === 'datetime'"
              v-model="curComponent.arbitraryTime"
            />
          </div>
        </div>
      </template>
      <template v-else-if="dynamicTime && curComponent.displayType === '7'">
        <div
          class="setting"
          :class="
            ['yearrange', 'monthrange', 'daterange'].includes(
              curComponent.timeGranularityMultiple
            ) && 'is-year-month-range'
          "
        >
          <div class="setting-label">开始时间</div>
          <div class="setting-input with-date range">
            <el-input-number v-model="curComponent.timeNum" :min="0" controls-position="right" />
            <el-select @focus="handleDialogClick" v-model="curComponent.relativeToCurrentType">
              <el-option
                v-for="item in relativeToCurrentTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select @focus="handleDialogClick" v-model="curComponent.around">
              <el-option
                v-for="item in aroundList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-time-picker v-model="curComponent.arbitraryTime" />
          </div>
        </div>
        <div
          class="setting"
          :class="
            ['yearrange', 'monthrange', 'daterange'].includes(
              curComponent.timeGranularityMultiple
            ) && 'is-year-month-range'
          "
        >
          <div class="setting-label">结束时间</div>
          <div class="setting-input with-date range">
            <el-input-number
              v-model="curComponent.timeNumRange"
              :min="0"
              controls-position="right"
            />
            <el-select @focus="handleDialogClick" v-model="curComponent.relativeToCurrentTypeRange">
              <el-option
                v-for="item in relativeToCurrentTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-select @focus="handleDialogClick" v-model="curComponent.aroundRange">
              <el-option
                v-for="item in aroundList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
            <el-time-picker v-model="curComponent.arbitraryTimeRange" />
          </div>
        </div>
      </template>
    </div>
    <div v-if="curComponent.defaultValueCheck" class="parameters" :class="dynamicTime && 'setting'">
      <div class="setting-label" v-if="dynamicTime">预览</div>
      <div :class="dynamicTime ? 'setting-value' : 'w100'">
        <component :config="curComponent" isConfig ref="inputCom" :is="filterTypeCom"></component>
      </div>
    </div>
  </div>
</template>

<style lang="less">
.condition-value-select-popper {
  .ed-select-dropdown__item.selected::after {
    display: none;
  }
}
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10.5px;
  flex-wrap: wrap;

  .setting-content {
    width: 100%;
    padding-left: 24px;
  }

  &.top-item {
    .label {
      margin-bottom: auto;
      padding-top: 5.5px;
    }
  }
  .label {
    width: 100px;
    color: #1f2329;
  }

  .value {
    .ed-select {
      width: 321px;
    }
    width: 321px;
    .value {
      margin-top: 8px;
      &:first-child {
        margin-top: -0.5px;
      }
      .search-field {
        width: 257px;
      }

      .sort-field {
        width: 176px;
      }

      .label {
        line-height: 32px;
        font-size: 14px;
        margin-right: 8px;
      }
    }
  }

  .value {
    width: 321px;
    .condition-type {
      margin-top: 3px !important;
      display: flex;
      position: relative;
      .ed-input__wrapper {
        border: none;
        border-radius: 0;
        box-shadow: none;
        height: 26px;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3', Hiragino Sans GB, Microsoft YaHei,
          sans-serif;
        word-wrap: break-word;
        text-align: left;
        color: rgba(0, 0, 0, 0.65);
        list-style: none;
        user-select: none;
        cursor: pointer;
        line-height: 26px;
        box-sizing: border-box;
        max-width: 100%;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        opacity: 1;
      }

      .ed-select .ed-input.is-focus .ed-input__wrapper,
      .ed-select:hover:not(.ed-select--disabled) .ed-input__wrapper,
      .ed-select .ed-input__wrapper.is-focus {
        box-shadow: none !important;
      }

      .ed-select {
        width: 120px;
        .ed-input__wrapper {
          padding: 0;
        }
      }

      .condition-type-tip {
        font-size: 12px;
        color: #646a73;
        line-height: 26px;
        margin-right: 8px;
      }

      .bottom-line {
        box-sizing: border-box;
        height: 1px;
        background-color: #000;
        opacity: 0.3;
        position: absolute;
        right: 5px;
        bottom: 3px;
        width: 220px;
        z-index: 10;

        &.next-line {
          width: 206px;
        }
      }
      &:first-child {
        margin-top: -0.5px;
      }
    }
  }
  .value {
    .sort-field {
      width: 240px;
    }
    .sort-type {
      width: 73px;
      margin-left: 8px;
    }
  }
  .parameters {
    margin-left: auto;
    margin-top: 8px;

    .w100 {
      width: 100%;
    }
    .ed-select,
    .ed-date-editor,
    .ed-date-editor--datetime .ed-input__wrapper,
    .ed-select-v2 {
      width: 415px;
    }

    .ed-date-editor {
      .ed-input__wrapper {
        width: 100%;
      }
    }
  }
  .parameters-range {
    width: 100%;
    padding-left: 24px;
    display: flex;
    flex-wrap: wrap;

    .range-title,
    .params-start,
    .params-end {
      width: 50%;
    }

    .params-start,
    .params-end {
      margin-top: 8px;
      .ed-select {
        width: 100%;
      }
    }

    .params-end {
      padding-left: 4px;
    }

    .params-start {
      padding-right: 4px;
    }
  }

  .setting {
    &.setting {
      margin-top: 8px;
    }
    &.parameters {
      width: 100%;
      padding-left: 24px;
      .ed-date-editor {
        width: 325px !important;
      }
    }
    margin-left: auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .setting-label {
      width: 80px;
      margin-right: 8px;
    }

    .setting-value {
      margin: 8px 0;
      &.select {
        margin-top: 0;
        .ed-select {
          width: 325px;
        }
      }
    }

    .setting-input {
      display: flex;
      padding-left: 86px;
      justify-content: flex-end;
      align-items: center;
      &.range {
        padding-left: 0px;
      }
      & > div + div {
        margin-left: 8px;
      }

      &.with-date {
        .ed-input-number {
          width: 71px;
        }
        .ed-select {
          width: 62px;
        }

        .ed-date-editor.ed-input {
          width: 106px;
        }
      }
    }

    &.is-year-month-range {
      .setting-input {
        &.with-date {
          .ed-input-number,
          .ed-select {
            width: 103px;
          }
        }
        .ed-date-editor.ed-input {
          display: none;
        }
      }
    }
  }
}
</style>
