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
      v-if="item.filter && item.filter.length > 0"
      style="display: flex; padding: 10px 0; align-items: center"
    >
      <span>{{ t('chart.conform_below') }}</span>
      <el-select
        v-model="state.logic"
        @change="logicChange"
        size="small"
        style="width: 60px; margin: 0 6px"
      >
        <el-option style="min-width: 80px" :label="t('chart.logic_and')" value="and" />
        <el-option style="min-width: 80px" :label="t('chart.logic_or')" value="or" />
      </el-select>
      <span>{{ t('chart.addition') }}</span>
    </div>

    <div
      style="max-height: 50vh; overflow-y: auto"
      v-if="item.filter && item.filter.length > 0"
      class="addition-style"
    >
      <div class="name-title">
        <span>
          {{ item.name }}
          <span v-if="item.summary && item.summary !== ''">
            ({{ t('chart.' + item.summary) }})
          </span>
        </span>
      </div>
      <div v-for="(f, index) in item.filter" :key="index" class="filter-item">
        <div style="width: 164px; margin-right: 8px">
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
        </div>
        <div style="flex: 1">
          <el-input
            v-if="!f.term.includes('null')"
            v-model="f.value"
            class="value-item"
            :placeholder="t('chart.condition')"
            clearable
          />
        </div>
        <el-button class="m-del-icon-btn" text @click="removeFilter(index)">
          <el-icon size="20px">
            <Icon name="icon_delete-trash_outlined" />
          </el-icon>
        </el-button>
      </div>
    </div>
    <el-button
      text
      class="circle-button"
      @click="addFilter"
      :style="{ marginTop: item.filter && item.filter.length > 0 ? '10px' : 0 }"
    >
      <Icon name="icon_add_outlined" style="width: 14px" />
      {{ t('chart.add_addition') }}
    </el-button>
  </div>
</template>

<style lang="less" scoped>
.filter-item {
  display: flex;
  width: 100%;
  margin-bottom: 8px;
  justify-content: left;
  align-items: center;

  &:last-child {
    margin-bottom: unset;
  }

  :deep(input) {
    font-size: 14px !important;
    line-height: 22px;
  }
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
  padding: 16px;
  background: #f5f6f7;

  .name-title {
    margin-bottom: 8px;

    :deep(span) {
      color: var(--N900, #1f2329);
      /* 中文/桌面端/正文 14 22 Regular */
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;

      cursor: default;
    }
  }
}
.btn-delete {
  min-width: auto !important;
}
.m-del-icon-btn {
  color: #646a73;
  margin-left: 4px;

  &:hover {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:focus {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:active {
    background: rgba(31, 35, 41, 0.2) !important;
  }
}
</style>
