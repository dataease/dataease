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
  <div @keydown.stop @keyup.stop>
    <el-col>
      <div v-if="item.deType === 0 || item.deType === 5">
        <div>{{ t('chart.slc_logic') }}:</div>
        <el-radio-group
          v-model="state.filterType"
          size="small"
          :style="{ margin: '6px 0 10px 0' }"
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
            <span>{{ item.name }}</span>
          </el-col>
          <el-row v-for="(f, index) in item.filter" :key="index" class="filter-item">
            <el-col :span="9">
              <el-select v-model="f.term">
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
            <el-col :span="14">
              <el-input
                v-show="!f.term.includes('null') && !f.term.includes('empty')"
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
        <el-button text class="circle-button" @click="addFilter" style="margin-top: 10px">
          <Icon name="icon_add_outlined" style="width: 14px"></Icon>
          {{ t('chart.add_addition') }}
        </el-button>
      </div>

      <div v-if="(item.deType === 0 || item.deType === 5) && state.filterType === 'enum'">
        <span style="margin-right: 10px">{{ t('chart.filter_exp') }}</span>
        <el-select
          v-model="state.enumCheckField"
          filterable
          collapse-tags
          multiple
          :placeholder="t('chart.pls_slc')"
          @change="enumChange"
          :clearable="true"
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

.addition-style {
  padding: 10px;
  background: #f5f6f7;
}
.btn-delete {
  min-width: auto !important;
}
</style>
