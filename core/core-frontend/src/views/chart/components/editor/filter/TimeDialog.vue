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
  <div class="time-dialog">
    <div class="setting">
      <div class="setting-label">时间粒度</div>
      <div class="setting-value select">
        <el-select
          @change="timeGranularityChange"
          placeholder="请选择时间粒度"
          v-model="curComponent.timeGranularity"
        >
          <el-option
            v-for="ele in timeList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          />
        </el-select>
      </div>
    </div>
    <div class="setting">
      <div class="setting-label">相对当前</div>
      <div class="setting-value select">
        <el-select v-model="curComponent.relativeToCurrent">
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
    </div>
    <div class="setting">
      <div class="setting-label">预览</div>
      <div class="setting-value" style="width: 325px">
        <DynamicTime style="width: 100%" :config="curComponent" isConfig></DynamicTime>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.time-dialog {
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
      padding-left: 126px;
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
  }
}
</style>
