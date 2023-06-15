<script lang="tsx" setup>
import { defineEmits, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '../../../util/chart'

const { t } = useI18n()

const props = defineProps({
  threshold: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['onTextLabelThresholdChange'])

const state = reactive({
  thresholdArr: []
})

const thresholdObj = {
  term: 'eq',
  field: '0',
  value: '',
  color: '#ff0000ff'
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
const predefineColors = COLOR_PANEL

const init = () => {
  state.thresholdArr = JSON.parse(JSON.stringify(props.threshold))
}
const addThreshold = () => {
  state.thresholdArr.push(JSON.parse(JSON.stringify(thresholdObj)))
  changeThreshold()
}
const removeThreshold = index => {
  state.thresholdArr.splice(index, 1)
  changeThreshold()
}

const changeThreshold = () => {
  emit('onTextLabelThresholdChange', state.thresholdArr)
}

init()
</script>

<template>
  <el-col>
    <el-button class="circle-button" circle style="margin-bottom: 10px" @click="addThreshold">
      <template #icon>
        <Icon name="icon_add_outlined"></Icon>
      </template>
    </el-button>
    <div style="max-height: 50vh; overflow-y: auto">
      <el-row v-for="(item, index) in state.thresholdArr" :key="index" class="line-item">
        <el-col :span="6">
          <el-select v-model="item.term" @change="changeThreshold">
            <el-option-group v-for="(group, idx) in textOptions" :key="idx" :label="group.label">
              <el-option
                v-for="opt in group.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-option-group>
          </el-select>
        </el-col>
        <el-col :span="14" style="text-align: center">
          <el-input
            v-if="!item.term.includes('null')"
            v-model="item.value"
            class="value-item"
            :placeholder="t('chart.drag_block_label_value')"
            size="small"
            clearable
            @change="changeThreshold"
          />
        </el-col>
        <el-col :span="2" style="text-align: center">
          <el-color-picker
            v-model="item.color"
            show-alpha
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeThreshold"
          />
        </el-col>
        <el-col :span="2">
          <el-button
            class="circle-button"
            type="text"
            circle
            :style="{ float: 'right' }"
            @click="removeThreshold(index)"
          >
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

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

span {
  font-size: 12px;
}

.value-item {
  position: relative;
  display: inline-block;
  width: 200px !important;
}

.between-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
}

.select-item {
  position: relative;
  display: inline-block;
  width: 100px !important;
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
  margin-top: 6px;
}

.color-picker-style ::v-deep .el-color-picker__trigger {
  width: 28px;
  height: 28px;
}
</style>
