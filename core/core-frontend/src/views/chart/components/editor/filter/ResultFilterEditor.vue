<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, toRefs, watch, ref } from 'vue'
import { multFieldValuesForPermissions } from '@/api/dataset'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  item: {
    type: Object,
    required: true
  }
})

const { item } = toRefs(props)

const needRequestEnum = ref(true)

const state = reactive({
  textOptions: [
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
    },
    {
      label: '',
      options: [
        {
          value: 'empty',
          label: t('chart.filter_empty')
        },
        {
          value: 'not_empty',
          label: t('chart.filter_not_empty')
        }
      ]
    }
  ],
  dateOptions: [
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
    }
  ],
  valueOptions: [
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
    }
  ],
  options: [],
  logic: '',
  filterType: '',
  enumCheckField: [],
  fieldOptions: []
})

watch(
  [props.item],
  () => {
    initOptions()
  },
  { deep: true }
)

const initOptions = () => {
  if (item?.value) {
    if (item.value.deType === 0 || item.value.deType === 5) {
      state.options = JSON.parse(JSON.stringify(state.textOptions))
    } else if (item.value.deType === 1) {
      state.options = JSON.parse(JSON.stringify(state.dateOptions))
    } else {
      state.options = JSON.parse(JSON.stringify(state.valueOptions))
    }
  }
}
const init = () => {
  state.logic = item.value.logic
  state.filterType = item.value.filterType
  state.enumCheckField = item.value.enumCheckField
  if (item.value.filterType === 'enum') {
    initEnumOptions()
  }
}
const initEnumOptions = () => {
  // 查找枚举值
  if (item.value.deType === 0 || item.value.deType === 5) {
    multFieldValuesForPermissions({ fieldIds: [item.value.id] }).then(res => {
      state.fieldOptions = optionData(res.data)
      needRequestEnum.value = false
    })
  }
}
const optionData = data => {
  if (!data) return null
  return data
    .filter(item => !!item)
    .map(item => {
      return {
        id: item,
        text: item
      }
    })
}
const addFilter = () => {
  item.value.filter.push({
    fieldId: item.value.id,
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
const filterTypeChange = val => {
  item.value.filterType = val
  if (val === 'enum' && needRequestEnum.value) {
    initEnumOptions()
  }
}
const enumChange = val => {
  item.value.enumCheckField = state.enumCheckField
}

initOptions()
init()
</script>

<template>
  <el-col>
    <div v-if="item.deType === 0 || item.deType === 5">
      <el-radio-group
        v-model="state.filterType"
        size="small"
        style="margin-bottom: 10px"
        @change="filterTypeChange"
      >
        <el-radio label="logic">{{ t('chart.logic_exp') }}</el-radio>
        <el-radio label="enum">{{ t('chart.enum_exp') }}</el-radio>
      </el-radio-group>
    </div>

    <div
      v-if="
        ((item.deType === 0 || item.deType === 5) && state.filterType === 'logic') ||
        item.deType === 1 ||
        item.deType === 2 ||
        item.deType === 3
      "
    >
      <div style="display: flex; padding: 10px 0">
        <el-button circle size="small" class="circle-button" @click="addFilter">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
        </el-button>
        <el-radio-group
          v-show="item.filter && item.filter.length > 1"
          v-model="state.logic"
          size="small"
          style="margin-left: 10px"
          @change="logicChange"
        >
          <el-radio-button label="and">{{ t('chart.and') }}</el-radio-button>
          <el-radio-button label="or">{{ t('chart.or') }}</el-radio-button>
        </el-radio-group>
      </div>
      <div style="max-height: 50vh; overflow-y: auto">
        <el-row v-for="(f, index) in item.filter" :key="index" class="filter-item">
          <el-col :span="4">
            <span>{{ item.name }}</span>
          </el-col>
          <el-col :span="8">
            <el-select v-model="f.term" size="small">
              <el-option-group
                v-for="(group, idx) in state.options"
                :key="idx"
                :label="group.label"
              >
                <el-option
                  v-for="opt in group.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-option-group>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-show="!f.term.includes('null') && !f.term.includes('empty')"
              v-model="f.value"
              class="value-item"
              :placeholder="t('chart.condition')"
              size="small"
              clearable
            />
          </el-col>
          <el-col :span="6">
            <el-button type="text" circle style="float: right" @click="removeFilter(index)">
              <template #icon>
                <Icon name="icon_delete-trash_outlined"></Icon>
              </template>
            </el-button>
          </el-col>
        </el-row>
      </div>
    </div>

    <div v-if="(item.deType === 0 || item.deType === 5) && state.filterType === 'enum'">
      <span style="margin-right: 10px">{{ t('chart.filter_exp') }}</span>
      <el-select
        v-model="state.enumCheckField"
        filterable
        collapse-tags
        multiple
        :placeholder="t('chart.pls_slc')"
        size="small"
        @change="enumChange"
      >
        <el-option
          v-for="field in state.fieldOptions"
          :key="field.id"
          :label="field.text"
          :value="field.id"
        />
      </el-select>
    </div>
  </el-col>
</template>

<style lang="less" scoped>
.filter-item {
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

.value-item :deep(.el-input) {
  position: relative;
  display: inline-block;
  width: 80px !important;
}
</style>
