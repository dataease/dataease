<script setup lang="ts">
import {computed, nextTick, onMounted, onUnmounted, ref} from "vue";
import {useI18n} from "@/hooks/web/useI18n";
import {DfFormItem, getTableColumnData} from "../data-filling";
import {filter, find, forEach, get, map, split} from "lodash-es";
import {Icon} from "@/components/icon-custom";
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import {FormInstance} from "element-plus-secondary";

interface DfFormItemExt extends DfFormItem {
  tempId?: string
}

interface FilterItem {
  props: string,
  label: string,
  date: boolean,
  number: boolean,
  dateType: string,
  type: string,
  asyncOptions?: boolean
  options?: Array<{ name: string, value: string }>,
  tempId?: string,
  select?: boolean,
  multiple?: boolean
}

interface FilterOptionItem {
  column: string,
  label: string,
  date: boolean,
  number: boolean,
  dateType: string,
  type: string,
  asyncOptions?: boolean
  options?: Array<{ name: string, value: string }>,
  tempId?: string,
  select?: boolean
  term?: string,
  value?: string | number,
  values?: Array<string | number>,
  multiple?: boolean
}

const props = defineProps<{
  modelValue: boolean
  formId: string
  baseForm: Array<DfFormItemExt>
  conditions: Array<any>
}>()

const emits = defineEmits(['update:modelValue', 'trigger-filter'])

const conditionsData = ref<Array<FilterOptionItem>>([])
const loading = ref(false)

const {t} = useI18n();
const showFilter = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})

function addRow() {
  conditionsData.value.push({values: [], value: undefined})
}

function removeRow(index) {
  conditionsData.value.splice(index, 1)
}

const trigger = () => {
  emits('trigger-filter', conditionsData.value)
  drawerMainClose()
}

const drawerMainClose = () => {
  showFilter.value = false
};

const asyncOptions = ref({})

function initFormOptionsData(forms: Array<DfFormItem>, callback?: (...params: any[]) => any) {
  const queries: Array<any> = []
  const queryIds: Array<string> = []
  forEach(forms, f => {
    if (f.type === 'checkbox' || f.type === 'select' || f.type === 'radio') {
      if (f.settings && f.settings.optionSourceType === 2 && f.settings.optionDatasource && f.settings.optionTable && f.settings.optionColumn && f.settings.optionOrder) {
        const id = f.id ?? "unset"
        console.log(f)
        const p = getTableColumnData(props.formId, f.id)
        queries.push(p)
        queryIds.push(id)
      }
    }
  })

  if (queries.length > 0) {
    Promise.all(queries).then((val) => {
      for (let i = 0; i < queryIds.length; i++) {
        const id = queryIds[i]
        asyncOptions.value[id] = val[i].data
      }
    }).finally(() => {
      if (callback) {
        callback()
      }
    })
  } else {
    if (callback) {
      callback()
    }
  }
}

function getSelectOptions(item) {
  let list = []
  if (item == undefined) {
    return list
  }
  list = map(!item.asyncOptions ? item.options : (asyncOptions.value[item.tempId] ? asyncOptions.value[item.tempId] : []), i => {
    return {
      label: i.name,
      value: i.value
    }
  })
  return list
}

const valueFilterOptions = [
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


const columns = ref<Array<FilterItem>>([])

function onSelectColumn(data: FilterOptionItem, c: string) {
  const item = find(columns.value, f => f.props === c)
  if (item == undefined) {
    return
  }
  data.value = undefined
  data.values = []

  data.label = item.label
  data.date = item.date
  data.number = item.number
  data.dateType = item.dateType?.replace('range', '')
  data.type = item.type
  data.asyncOptions = item.asyncOptions
  data.options = item.options
  data.tempId = item.tempId
  data.select = item.select
  data.multiple = !!item.multiple

  if (data.select) {
    data.term = 'in'
  } else if (data.number || data.date) {
    data.term = data.term && data.term !== 'in' && data.term !== 'like' ? data.term : 'eq'
  } else {
    data.term = 'like'
  }
}

function onTermChange(item) {
  if (item.term === 'between') {
    if (!item.values || item.values.length != 2) {
      item.values = [undefined, undefined]
    }
    item.value = undefined
  } else {
    item.values = []
  }
}

const filterForm = ref<FormInstance>()

const requiredRule = {required: true, message: t('common.required'), trigger: ['blur', 'change']}

const customNumberLtRule = {
  validator: (rule, value, callback) => {
    if (value == undefined) {
      callback(new Error(t('common.required')))
    }
    const f = split(rule.field, '.')[0]
    const _row = get(conditionsData.value, f)
    const checkValue = _row.values[1]
    if (checkValue !== undefined && checkValue <= value) {
      return callback(new Error(t('data_fill.form.lt_check', ['', checkValue])))
    }
    callback()
  },
  trigger: ['blur', 'change'],
}

const customNumberGtRule = {
  validator: (rule, value, callback) => {
    if (value == undefined) {
      callback(new Error(t('common.required')))
    }
    const f = split(rule.field, '.')[0]
    const _row = get(conditionsData.value, f)
    const checkValue = _row.values[0]
    if (checkValue !== undefined && checkValue >= value) {
      return callback(new Error(t('data_fill.form.gt_check', ['', checkValue])))
    }
    callback()
  },
  trigger: ['blur', 'change'],
}

const multiSelectRuleValidator = (rule, value, callback) => {
  if (value === undefined || value.length === 0) {
    return callback(new Error(t('common.required')))
  }
  callback()
}

onMounted(() => {
  conditionsData.value = JSON.parse(JSON.stringify(props.conditions))

  const tempForms = filter(props.baseForm, f => !f.removed)

  const _list: Array<FilterItem> = []
  forEach(tempForms, (f) => {
    if (f.type === "dateRange") {
      _list.push({
        props: f.settings?.mapping?.columnName1,
        label: f.settings?.name + '(' + t("data_fill.data.start") + ')',
        date: true,
        number: false,
        dateType: f.settings?.dateType,
        type: f.type
      } as FilterItem);
      _list.push({
        id: f.id,
        props: f.settings?.mapping?.columnName2,
        label: f.settings?.name + '(' + t("data_fill.data.end") + ')',
        date: true,
        number: false,
        dateType: f.settings?.dateType,
        type: f.type,
      } as FilterItem);
    } else {
      _list.push({
        props: f.settings?.mapping?.columnName,
        label: f.settings?.name,
        date: f.type === "date",
        number: f.settings.inputType === "number",
        dateType: f.settings?.dateType,
        type: f.type,
        asyncOptions: f.settings.optionSourceType === 2,
        select: f.type === "select" || f.type === 'checkbox' || f.type === 'radio',
        multiple: f.type === 'checkbox' || f.settings?.multiple,
        options: f.settings.options,
        tempId: f.id ?? 'unset',
      } as FilterItem);
    }
  });

  columns.value = _list

  initFormOptionsData(tempForms, () => {
  })
})

const pickerOptions = {
  disabledDate: (time) => {
    return time.getTime() < new Date(0).getTime()
  }
}

function reset() {
  conditionsData.value = []
  trigger()
}

function search() {
  filterForm.value?.validate((valid, invalidFields) => {
    if (valid) {
      trigger()
    }
  })
}

onMounted(() => {
  if (conditionsData.value.length == 0) {
    nextTick(() => {
      addRow()
    })
  }
})

onUnmounted(() => {
  loading.value = false
})

</script>

<template>
  <el-drawer
      :title="t('common.filter_condition')"
      v-model="showFilter"
      size="600px"
      modal-class="drawer-main-container"
      direction="rtl"
  >
    <el-form
        ref="filterForm"
        hide-required-asterisk
        :model="conditionsData"
        @submit.native.prevent
    >
      <div v-for="(data, index) in conditionsData" :key="index">
        <el-row :gutter="8">
          <el-col :span="7">
            <el-form-item class="m-form-item form-item" :prop="'['+ index +'].column'" :rules="[requiredRule]">
              <el-select v-model="data.column" @change="onSelectColumn(data, data.column)">
                <el-option v-for="c in columns" :key="c.props" :label="c.label" :value="c.props"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <template v-if="data.number">
              <el-row :gutter="8">
                <el-col :span="6">
                  <el-form-item class="m-form-item form-item" :prop="'['+ index +'].term'" :rules="[requiredRule]">
                    <el-select v-model="data.term" @change="onTermChange(data)">
                      <el-option-group
                          v-for="group in valueFilterOptions"
                          :key="group.label"
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
                  </el-form-item>
                </el-col>
                <el-col :span="18">
                  <template v-if="data.term === 'null' || data.term === 'not_null'">
                    <el-form-item class="m-form-item form-item">
                      <el-input-number disabled controls-position="right" style="width: 100%;"/>
                    </el-form-item>
                  </template>
                  <template v-else-if="data.term === 'between'">
                    <div style="display: flex; flex-direction: row; align-items: center;">
                      <el-form-item class="m-form-item form-item" style="width: 0; flex: 1;"
                                    :prop="'['+ index +'].values[0]'"
                                    :rules="[requiredRule, customNumberLtRule]">
                        <el-input-number title="" v-model="data.values[0]" controls-position="right"
                                         :min="-999999999999"
                                         :max="999999999999"
                                         style="width: 100%"/>
                      </el-form-item>
                      <div style="margin-left: 8px; margin-right: 8px; margin-bottom: 8px;">~</div>
                      <el-form-item class="m-form-item form-item" style="width: 0; flex: 1;"
                                    :prop="'['+ index +'].values[1]'"
                                    :rules="[requiredRule, customNumberGtRule]">
                        <el-input-number title="" v-model="data.values[1]" controls-position="right"
                                         :min="-999999999999"
                                         :max="999999999999"
                                         style="width: 100%"/>
                      </el-form-item>
                    </div>
                  </template>
                  <template v-else>
                    <el-form-item class="m-form-item form-item" :prop="'['+ index +'].value'"
                                  :rules="[requiredRule]">
                      <el-input-number title="" v-model="data.value" controls-position="right"
                                       :min="-999999999999"
                                       :max="999999999999"
                                       style="width: 100%;"/>
                    </el-form-item>
                  </template>
                </el-col>
              </el-row>
            </template>
            <template v-else-if="data.date">
              <el-row :gutter="8">
                <el-col :span="6">
                  <el-form-item class="m-form-item form-item" :prop="'['+ index +'].term'" :rules="[requiredRule]">
                    <el-select v-model="data.term" @change="onTermChange(data)">
                      <el-option-group
                          v-for="group in valueFilterOptions"
                          :key="group.label"
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
                  </el-form-item>
                </el-col>
                <el-col :span="18">
                  <template v-if="data.term === 'null' || data.term === 'not_null'">
                    <el-form-item class="m-form-item form-item">
                      <el-date-picker
                          disabled
                          :type="data.dateType"
                          style="width: 100%"
                          :picker-options="pickerOptions"
                      />
                    </el-form-item>
                  </template>
                  <template v-else-if="data.term === 'between'">
                    <el-form-item class="m-form-item form-item" :prop="'['+ index +'].values'" :rules="[requiredRule]">
                      <el-date-picker
                          v-model="data.values"
                          required
                          :type="data.dateType + 'range'"
                          range-separator="~"
                          style="width: 100%"
                          :picker-options="pickerOptions"
                      />
                    </el-form-item>
                  </template>
                  <template v-else>
                    <el-form-item class="m-form-item form-item" :prop="'['+ index +'].value'" :rules="[requiredRule]">
                      <el-date-picker
                          v-model="data.value"
                          required
                          :type="data.dateType"
                          style="width: 100%"
                          :picker-options="pickerOptions"
                      />
                    </el-form-item>
                  </template>
                </el-col>
              </el-row>
            </template>
            <template v-else-if="data.select">
              <el-form-item class="m-form-item form-item" :prop="'['+ index +'].values'"
                            :rules="{validator: multiSelectRuleValidator, message:t('data_fill.required_select'),trigger: ['change', 'blur']}">
                <el-select-v2
                    v-model="data.values"
                    required="true"
                    style="width: 100%"
                    filterable
                    collapse-tags
                    collapse-tags-tooltip
                    multiple
                    clearable
                    :options="getSelectOptions(data)"
                >
                </el-select-v2>
              </el-form-item>
            </template>
            <template v-else>
              <el-form-item class="m-form-item form-item" :prop="'['+ index +'].value'" :rules="[requiredRule]">
                <el-input v-model="data.value" required/>
              </el-form-item>
            </template>
          </el-col>
          <el-col :span="1" style="display: flex;">
            <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
              <el-button text @click="removeRow(index)" style="margin-bottom: 8px;">
                <template #icon>
                  <Icon name="icon_delete-trash_outlined">
                    <icon_deleteTrash_outlined/>
                  </Icon>
                </template>
              </el-button>
            </el-tooltip>
          </el-col>
        </el-row>
      </div>
    </el-form>
    <el-button @click="addRow" text>
      <template #icon>
        <icon name="icon_add_outlined">
          <icon_add_outlined class="svg-icon"/>
        </icon>
      </template>
      {{ t("data_fill.add_search_condition") }}
    </el-button>

    <template #footer>
      <el-button @click="reset" secondary>{{ t('commons.adv_search.reset') }}</el-button>
      <el-button @click="search" type="primary">{{ t('commons.adv_search.search') }}</el-button>
    </template>
  </el-drawer>
</template>

<style scoped lang="less">
.form-item {
  margin-bottom: 8px;
}

:deep(.ed-date-editor) {
  .ed-input__wrapper {
    width: 100%;
  }
}

:deep(.ed-form-item.is-error) {
  .ed-form-item__error {
    padding-top: 0;
  }

  &.m-form-item {
    margin-bottom: 22px;
  }
}

:deep(.ed-form-item__error) {
  line-height: 14px;
  padding-top: 0;
}
</style>
