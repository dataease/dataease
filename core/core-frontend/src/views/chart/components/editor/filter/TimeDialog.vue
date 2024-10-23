<script lang="ts" setup>
import { ref, computed } from 'vue'
import DynamicTime from '@/custom-component/v-query/DynamicTimeForViewFilter.vue'
import { type DatePickType } from 'element-plus-secondary'
export interface SelectConfig {
  relativeToCurrent: string
  timeNum: number
  relativeToCurrentType: string
  around: string
  arbitraryTime: Date
  timeGranularity: DatePickType
}

const defaultValue: SelectConfig = {
  relativeToCurrent: 'custom', //相对当前 thisYear ｜ lastYear ｜ thisMonth ｜ lastMonth ｜ today ｜ yesterday ｜ monthBeginning ｜ yearBeginning
  timeGranularity: 'year', //时间粒度 year ｜ month ｜ date ｜ datetime
  timeNum: 0, // 数值
  relativeToCurrentType: 'year', // year ｜ month ｜ date
  around: 'b', // 前 b ｜ 后 f
  arbitraryTime: new Date() //timeGranularity = datetime时 取时分秒
}
const curComponent = ref<SelectConfig>({ ...defaultValue })

const init = (val: SelectConfig) => {
  curComponent.value = {
    ...defaultValue,
    ...val
  }
}

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

const relativeToCurrentTypeList = computed(() => {
  if (!curComponent.value) return []
  let index = ['year', 'month', 'date', 'datetime'].indexOf(curComponent.value.timeGranularity) + 1
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

const timeGranularityChange = (val: string) => {
  if (
    ['year', 'month', 'date', 'datetime'].indexOf(val) <
    ['year', 'month', 'date'].indexOf(curComponent.value.relativeToCurrentType)
  ) {
    curComponent.value.relativeToCurrentType = 'year'
  }
  if (curComponent.value.relativeToCurrent !== 'custom') {
    curComponent.value.relativeToCurrent = relativeToCurrentList.value[0]?.value
  }
}

const timeList = [
  {
    label: '年',
    value: 'year'
  },
  {
    label: '年月',
    value: 'month'
  },
  {
    label: '年月日',
    value: 'date'
  },
  {
    label: '年月日时分秒',
    value: 'datetime'
  }
]

defineExpose({
  init,
  curComponent
})
</script>

<template>
  <el-form label-position="top">
    <el-form-item label="时间粒度">
      <el-select
        @change="timeGranularityChange"
        style="width: 100%"
        placeholder="请选择时间粒度"
        v-model="curComponent.timeGranularity"
      >
        <el-option v-for="ele in timeList" :key="ele.value" :label="ele.label" :value="ele.value" />
      </el-select>
    </el-form-item>
    <el-form-item label="相对当前">
      <el-select style="width: 100%" v-model="curComponent.relativeToCurrent">
        <el-option
          v-for="item in relativeToCurrentList"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item v-if="curComponent.relativeToCurrent === 'custom'">
      <div
        style="width: 100%"
        class="no-with-date"
        :class="curComponent.timeGranularity === 'datetime' && 'with-date'"
      >
        <el-input-number v-model="curComponent.timeNum" :min="0" controls-position="right" />
        <el-select v-model="curComponent.relativeToCurrentType">
          <el-option
            v-for="item in relativeToCurrentTypeList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-select v-model="curComponent.around">
          <el-option
            v-for="item in aroundList"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <el-time-picker
          style="width: 108px; margin-left: 8px"
          v-if="curComponent.timeGranularity === 'datetime'"
          v-model="curComponent.arbitraryTime"
        />
      </div>
    </el-form-item>
    <el-form-item label="预览">
      <DynamicTime style="width: 100%" :config="curComponent" isConfig></DynamicTime>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped>
.no-with-date {
  .ed-input-number {
    width: 120px;
  }
  .ed-select {
    width: 118px;
    margin-left: 8px;
  }
}
.with-date {
  .ed-input-number {
    width: 80px;
  }
  .ed-select {
    width: 80px;
    margin-left: 8px;
  }

  .ed-date-editor.ed-input {
    width: 106px;
  }
}

:deep(.date-editor_granularity .ed-input__wrapper) {
  width: 100%;
}
</style>
