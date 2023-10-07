<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, toRefs } from 'vue'

const { t } = useI18n()

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const { item } = toRefs(props)

const state = reactive({
  options: [
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
          value: 'not_null',
          label: t('chart.filter_not_null')
        }
      ]
    }
  ],
  logic: ''
})

const init = () => {
  state.logic = props.item.logic
}
const addFilter = () => {
  item.value.filter.push({
    term: 'eq',
    value: ''
  })
}
const removeFilter = index => {
  item.value.filter.splice(index, 1)
}
const logicChange = val => {
  item.value.logic = val
}
init()
</script>

<template>
  <div @keydown.stop @keyup.stop>
    <div
      v-show="item.filter && item.filter.length > 0"
      style="display: flex; padding: 10px 0; align-items: center"
    >
      <span>{{ t('chart.conform_below') }}</span>
      <el-select
        v-model="state.logic"
        @change="logicChange"
        size="small"
        style="width: 60px; margin: 0 6px"
      >
        <el-option :label="t('chart.logic_and')" value="and" />
        <el-option :label="t('chart.logic_or')" value="or" />
      </el-select>
      <span>{{ t('chart.addition') }}</span>
    </div>

    <div
      style="max-height: 50vh; overflow-y: auto"
      v-show="item.filter && item.filter.length > 0"
      class="addition-style"
    >
      <el-col style="margin: 0 10px">
        <span>
          {{ item.name }}
          <span v-show="item.summary && item.summary !== ''">
            ({{ t('chart.' + item.summary) }})
          </span>
        </span>
      </el-col>
      <el-row v-for="(f, index) in item.filter" :key="index" class="filter-item">
        <el-col :span="9">
          <el-select v-model="f.term">
            <el-option-group v-for="(group, idx) in state.options" :key="idx" :label="group.label">
              <el-option
                v-for="opt in group.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-option-group>
          </el-select>
        </el-col>
        <el-col :span="14">
          <el-input
            v-show="!f.term.includes('null')"
            v-model="f.value"
            class="value-item"
            :placeholder="t('chart.condition')"
            clearable
          />
        </el-col>
        <el-col :span="1">
          <el-button class="btn-delete" type="text" circle @click="removeFilter(index)">
            <template #icon>
              <Icon name="icon_delete-trash_outlined"></Icon>
            </template>
          </el-button>
        </el-col>
      </el-row>
    </div>
    <el-button
      text
      class="circle-button"
      @click="addFilter"
      :style="{ marginTop: item.filter && item.filter.length > 0 ? '10px' : 0 }"
    >
      <Icon name="icon_add_outlined" style="width: 14px"></Icon>
      {{ t('chart.add_addition') }}
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.filter-item {
  width: 100%;
  padding: 4px 10px;
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

.value-item :deep(.el-input) {
  position: relative;
  display: inline-block;
  width: 80px !important;
}
.el-select-dropdown__item {
  padding: 0 20px;
  font-size: 12px;
}

.addition-style {
  padding: 10px;
  background: #f5f6f7;
}
.btn-delete {
  min-width: auto !important;
}
</style>
