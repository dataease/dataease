<script lang="tsx" setup>
import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '../../../util/chart'
import { ElSpace } from 'element-plus-secondary'

const { t } = useI18n()

const props = defineProps({
  threshold: {
    type: Array,
    required: true
  }
})

const emit = defineEmits(['onLabelThresholdChange'])

const state = reactive({
  thresholdArr: []
})

const thresholdObj = {
  term: 'eq',
  field: '0',
  value: '0',
  color: '#ff0000ff',
  min: '0',
  max: '1'
}
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
  emit('onLabelThresholdChange', state.thresholdArr)
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
    <div @keydown.stop @keyup.stop style="max-height: 50vh; overflow-y: auto">
      <el-row
        v-for="(item, index) in state.thresholdArr"
        :key="index"
        class="line-item"
        :gutter="8"
      >
        <el-col :span="5">
          <el-form-item class="form-item">
            <el-select v-model="item.term" @change="changeThreshold">
              <el-option-group v-for="(group, idx) in valueOptions" :key="idx" :label="group.label">
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
        <el-col :span="9" style="text-align: center">
          <el-form-item class="form-item" v-if="item.term !== 'between'">
            <el-input-number
              controls-position="right"
              v-model="item.value"
              class="value-item"
              :placeholder="t('chart.drag_block_label_value')"
              clearable
              @change="changeThreshold"
            />
          </el-form-item>
          <el-space v-if="item.term === 'between'">
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
            <div style="display: flex; justify-content: center; min-width: 40px">
              <span>≤{{ t('chart.drag_block_label_value') }}≤</span>
            </div>
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
          </el-space>
        </el-col>
        <div style="display: flex; align-items: center; justify-content: center; margin-left: 8px">
          <div class="color-title">{{ t('chart.textColor') }}</div>
          <el-color-picker
            is-custom
            size="large"
            v-model="item.color"
            show-alpha
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeThreshold"
          />
        </div>
        <!--        <div style="display: flex; align-items: center; justify-content: center; margin-left: 8px">
          <div class="color-title">{{ t('chart.backgroundColor') }}</div>
          <el-color-picker
            is-custom
            size="large"
            v-model="item.backgroundColor"
            show-alpha
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeThreshold"
          />
        </div>-->
        <div style="display: flex; align-items: center; justify-content: center; margin-left: 8px">
          <el-button
            class="circle-button"
            type="text"
            circle
            style="float: right"
            @click="removeThreshold(index)"
          >
            <template #icon>
              <Icon name="icon_delete-trash_outlined"></Icon>
            </template>
          </el-button>
        </div>
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

.form-item :deep(.el-form-item__label) {
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

.form-item {
  height: 28px !important;
  margin-bottom: 0 !important;
  :deep(.el-form-item__label) {
    font-size: 12px;
  }
}
</style>
