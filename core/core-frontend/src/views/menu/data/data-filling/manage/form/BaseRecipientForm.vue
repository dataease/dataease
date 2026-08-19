<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag"/>
      <span class="form-title">{{ t("datasource.base_info") }}</span>
    </div>
    <el-form
        ref="reportReciForm"
        class="report-form"
        :model="formState"
        label-width="180px"
        label-position="top"
        :scroll-to-error="true"
    >
      <el-form-item
          :label="t('report.task_name')"
          prop="name"
          :rules="[requiredRule, maxLengthRule(64), minLengthRule(1)]"
      >
        <el-input v-model.trim="formState.name"/>
      </el-form-item>

      <el-form-item :label="t('data_fill.form.form_list_name')">
        <el-input v-model="_formName" disabled/>
      </el-form-item>

      <el-form-item
          :label="t('data_fill.task.commit_type')"
          prop="fillType"
          :rules="[requiredRule]"
      >
        <el-radio-group v-model="formState.fillType" @change="onFillTypeChange">
          <el-radio :label="0">{{
              t("data_fill.form.commit_type_append")
            }}
          </el-radio>
          <el-radio :label="1">{{
              t("data_fill.form.commit_type_update")
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
          :label="t('data_fill.task.receiver')"
          v-if="formState.fillType === 0"
          prop="reciVal"
          :rules="[reciValRule]"
      >
        <reci-select ref="reciSelector" v-model="formState.reciVal" :resource-id="formId" :resource-flag="8" />
      </el-form-item>

      <template v-else-if="formState.fillType === 1">
        <el-form-item
            :label="t('data_fill.task.receive_object')"
            prop="fitType"
            :rules="[requiredRule]"
        >
          <el-select v-model="formState.fitType" style="width: 100%">
            <el-option :label="t('common.account')" :value="0"/>
            <el-option :label="t('user.name')" :value="1"/>
          </el-select>
        </el-form-item>

        <el-form-item
            :label="t('data_fill.task.receive_fit_column')"
            prop="fitColumn"
            :rules="[requiredRule]"
        >
          <el-select v-model="formState.fitColumn" style="width: 100%">
            <el-option
                v-for="o in columns"
                :key="o.props"
                :label="o.props"
                :value="o.props"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('data_fill.task.form_filter_setting')">
          <el-button text @click="openFilterSetting">{{ t('data_fill.task.filter_setting') }}
            <el-icon style="margin-left: 8px">
              <Icon name="icon_edit_outlined">
                <icon_edit_outlined/>
              </Icon>
            </el-icon>
          </el-button>
        </el-form-item>

        <el-form-item :label="t('data_fill.task.form_template_setting')">
          <el-button text @click="openTemplateSetting">{{ t('data_fill.task.template_setting') }}
            <el-icon style="margin-left: 8px">
              <Icon name="icon_edit_outlined">
                <icon_edit_outlined/>
              </Icon>
            </el-icon>
          </el-button>
        </el-form-item>

      </template>
    </el-form>

    <el-dialog v-model="filterVisible" width="840" destroy-on-close class="reci-dialog" align-center
               :close-on-click-modal="false">
      <template #header>
        {{ t('data_fill.task.filter_setting') }}
      </template>
      <div class="reci-dialog-container filter-settings" v-loading="filterLoading">
        <div class="reci-dialog-main template-setting">
          <div style="padding: 12px; background-color: #f5f6f7">
            <el-form
                ref="filterSettingsFormRef"
                :model="filterSettings"
                label-width="180px"
                label-position="top"
                :scroll-to-error="true"
            >
              <el-row :gutter="8" v-for="(rule, _index) in filterSettings" :key="_index">
                <el-col :span="6">
                  <el-form-item class="form-item"
                                :prop="_index +'.column'"
                                :rules="{required: true,message: t('data_fill.required_select'), trigger: ['change', 'blur']}">
                    <el-select v-model="rule.column" style="width: 100%" @change="onFilterColumnChange(rule)">
                      <el-option
                          v-for="opt in columns"
                          :key="opt.props"
                          :label="opt.props"
                          :value="opt.props"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item class="form-item"
                                :prop="_index +'.filterType'"
                                :rules="{required: true,message:t('data_fill.required_select'),trigger: ['change', 'blur']}">
                    <el-select v-model="rule.filterType" style="width: 100%" @change="onFilterTypeChange(rule)">
                      <el-option
                          v-for="opt in rule.filterTypeList"
                          :key="opt.type"
                          :label="opt.name"
                          :value="opt.type"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="13" v-if="rule.filterType === 'enum'">
                  <el-form-item class="form-item"
                                :prop="_index +'.valueList'"
                                :rules="{validator: multiSelectRuleValidator, message:t('data_fill.required_select'),trigger: ['change', 'blur']}">
                    <el-select v-model="rule.valueList" style="width: 100%" multiple filterable>
                      <el-option
                          v-for="opt in rule.asyncOptions ? asyncOptions[rule.id]: rule.options"
                          :key="opt.value"
                          :label="opt.value"
                          :value="opt.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <template v-if="rule.filterType !== 'enum'">
                  <el-col :span="4">
                    <el-form-item class="form-item"
                                  :prop="_index +'.term'"
                                  :rules="{required: true,message:t('data_fill.required_select'),trigger: ['change', 'blur']}">
                      <el-select v-model="rule.term" style="width: 100%">
                        <el-option-group
                            v-for="group in getFilterOptions(rule)"
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
                  <el-col :span="9" v-if="rule.term !== 'between'">
                    <template v-if="rule.term !== 'null' && rule.term !== 'not_null'">
                      <el-form-item class="form-item"
                                    :prop="_index +'.value'"
                                    :rules="{required: true, message:t('common.require'), trigger: ['change', 'blur']}">
                        <el-input clearable v-if="!rule.number && !rule.date" v-model="rule.value"
                                  :placeholder="t('data_fill.condition')"/>
                        <el-input-number clearable style="width: 100%" v-if="rule.number" v-model.number="rule.value"
                                         controls-position="right"
                                         :placeholder="t('data_fill.condition')"/>
                        <el-date-picker
                            style="width: 100%"
                            v-if="rule.date"
                            :type="replace(rule.dateType, 'range', '')"
                            v-model="rule.value"
                            class="between-item"
                            :placeholder="t('data_fill.condition')"
                            value-format="x"
                            clearable
                        />
                      </el-form-item>
                    </template>
                  </el-col>
                  <template v-if="rule.term === 'between'">
                    <el-col :span="4">
                      <el-form-item class="form-item"
                                    :prop="_index +'.min'"
                                    :rules="{required: true, message:t('data_fill.required_select'), trigger: ['change', 'blur']}">
                        <el-input-number
                            v-if="rule.number"
                            v-model="rule.min"
                            controls-position="right"
                            class="between-item"
                            :placeholder="t('chart.axis_value_min')"
                            clearable
                        />
                        <el-date-picker
                            v-if="rule.date"
                            :type="replace(rule.dateType, 'range', '')"
                            v-model="rule.min"
                            class="between-item"
                            :placeholder="t('chart.axis_value_min')"
                            value-format="x"
                            clearable
                        />
                      </el-form-item>
                    </el-col>
                    <el-col :span="1">
                      <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
                        <span>~</span>
                      </div>
                    </el-col>
                    <el-col :span="4">
                      <el-form-item class="form-item"
                                    :prop="_index +'.max'"
                                    :rules="{required: true, message:t('data_fill.required_select'), trigger: ['change', 'blur']}">
                        <el-input-number
                            v-if="rule.number"
                            v-model="rule.max"
                            controls-position="right"
                            class="between-item"
                            :placeholder="t('chart.axis_value_max')"
                            clearable
                        />
                        <el-date-picker
                            v-if="rule.date"
                            :type="replace(rule.dateType, 'range', '')"
                            v-model="rule.max"
                            class="between-item"
                            :placeholder="t('chart.axis_value_max')"
                            value-format="x"
                            clearable
                        />
                      </el-form-item>
                    </el-col>
                  </template>
                </template>
                <el-col :span="1">
                  <div style="display: flex; justify-content: center; align-items: center; height: 100%;">
                    <el-button
                        text
                        @click="deleteFilterRow(_index)"
                    >
                      <template #icon>
                        <Icon name="icon_delete-trash_outlined">
                          <icon_deleteTrash_outlined/>
                        </Icon>
                      </template>
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </el-form>
          </div>
          <div>
            <el-button text @click="addFilter">+{{ t('data_fill.add_condition') }}</el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button secondary @click="closeFilterDialog">{{ t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="saveFilterDialog">{{ t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateVisible" width="840" destroy-on-close class="reci-dialog" align-center
               :close-on-click-modal="false">
      <template #header>
        {{ t('data_fill.task.template_setting') }}
      </template>
      <div class="reci-dialog-container">
        <div class="reci-dialog-main template-setting">

          <div style="padding: 12px; margin-bottom: 12px; background: #d0defd; height: 87px;">
            <div>{{ t('data_fill.task.template_hint_title') }}:</div>
            <div>1. {{ t('data_fill.task.template_hint_1') }}</div>
            <div>2. {{ t('data_fill.task.template_hint_2') }}</div>
          </div>

          <div style="height: calc(100% - 99px)">
            <GridTable
                ref="multipleTableRef"
                :table-data="templateSetting"
                style="width:100%; height: 100%;"
                :show-pagination="false"
            >
              <el-table-column :label="t('data_fill.task.component')">
                <template #default="scope">
                  {{ scope.row.name }}
                </template>
              </el-table-column>
              <el-table-column :label="t('common.operate')">
                <template #default="scope">
                  <div style="display: flex;
                              flex-direction: row;
                              align-items: center;">
                    <el-radio-group v-model="scope.row.disable" size="middle">
                      <el-radio :label="true" size="middle">{{ t('data_fill.disable_edit') }}</el-radio>
                      <el-radio :label="false" size="middle">{{ t('data_fill.enable_edit') }}</el-radio>
                    </el-radio-group>

                    <el-button text @click="openAllowEditDialog(scope.row)"
                               v-if="hasMultiNumberInput && scope.row.inputType === 'number' && scope.row.type === 'input' && !scope.row.disable">
                      <template #icon>
                        <Icon name="icon_params_setting">
                          <icon_params_setting class="svg-icon"/>
                        </Icon>
                      </template>
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </GridTable>
          </div>

        </div>
      </div>
      <template #footer>
        <el-button secondary @click="closeTemplateDialog">{{ t('commons.cancel') }}</el-button>
        <el-button secondary @click="previewTemplateDialog">{{ t('dataset.preview') }}</el-button>
        <el-button type="primary" @click="saveTemplateDialog">{{ t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="allowEditDialogVisible" width="840" destroy-on-close class="reci-dialog" align-center
               :close-on-click-modal="false">
      <template #header>
        {{ t('data_fill.set_condition') }}
      </template>
      <div class="reci-dialog-container">
        <div class="reci-dialog-main template-setting">
          <el-row :gutter="20" style="margin-bottom: 16px">
            <el-col :span="8">
              <div style="font-weight: 500">{{ t('data_fill.set_condition') }}</div>
            </el-col>

            <el-col :span="12">
              <div style="font-weight: 500">{{ t('data_fill.select_component') }}</div>
            </el-col>

          </el-row>
          <el-form
              ref="numberInputFormRef"
              :model="numberRuleForms"
              label-width="180px"
              label-position="top"
              :scroll-to-error="true"
          >
            <el-row v-for="(rule, $index) in numberRuleForms" :key="$index" :gutter="20">
              <el-col :span="8">
                <el-form-item class="form-item"
                              :prop="$index + '.term'"
                              :rules="{
                                        required: true,
                                        message:t('data_fill.required_select'),
                                        trigger: 'change'
                                      }">
                  <el-select v-model="rule.term" style="width: 100%">
                    <el-option-group
                        v-for="(group, idx) in valueOptions"
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
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item class="form-item"
                              :prop="$index + '.column'"
                              :rules="{
                                        required: true,
                                         message:t('data_fill.required_select'),
                                        trigger: 'change'
                                      }">
                  <el-select v-model="rule.column" style="width: 100%">
                    <el-option v-for="(input,index) in filteredInputList"
                               :key="index"
                               :label="input.name"
                               :value="input.id"/>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-button
                    text
                    @click="deleteRow($index)"
                >
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined"
                    >
                      <icon_deleteTrash_outlined
                      />
                    </Icon>
                  </template>
                </el-button>
              </el-col>
            </el-row>
          </el-form>
          <div>
            <el-button text @click="addRuleRow">+{{ t('data_fill.add_condition') }}</el-button>
          </div>
        </div>

      </div>
      <template #footer>
        <el-button secondary @click="closeAllowEditDialog">
          {{ t("commons.cancel") }}
        </el-button>
        <el-button type="primary" @click="confirmRules">
          {{ t("commons.confirm") }}
        </el-button>
      </template>
    </el-dialog>
    <RowDataForm ref="rowDataFormRef"/>
  </div>
</template>

<script lang="ts" setup>
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import icon_params_setting from '@/assets/svg/icon_params_setting.svg'
import {computed, nextTick, onMounted, reactive, ref} from 'vue'
import { ReciOption } from '../../../../system/report/form/formUtil'
import {useI18n} from '@/hooks/web/useI18n'
import {Icon} from '@/components/icon-custom'
import { ElButton, ElDatePicker, ElInput, ElMessage } from 'element-plus-secondary'
import {every, forEach, map, filter, find, replace} from 'lodash-es'
import GridTable from "@/components/grid-table/src/GridTable.vue";
import RowDataForm from "./RowDataForm.vue";
import dayjs from "dayjs";
import {getTableColumnData} from "../../data-filling";
import ReciSelect from '../../../../../component/reci-select/index.vue'

const {t} = useI18n()
const props = withDefaults(defineProps<{
  reciFormData?: any
  isEdit?: boolean
  formName?: string
  columns?: Array<any>,
  forms?: Array<any>,
  formId: string
}>(), {
  reciFormData: () => {
  },
  isEdit: false,
  columns: () => [],
  forms: () => []
})

const rowDataFormRef = ref()
const reciSelector = ref()
const allowEditDialogVisible = ref(false)
const filterVisible = ref(false)
const templateVisible = ref(false)
const reportReciForm = ref(null)

const _formName = computed(() => {
  return props.formName;
});

const checkReciValValidator = (rule, value, callback) => {
  if (formState.value.reciVal?.length > 0) {
    callback();
  } else {
    callback(new Error(t("data_fill.task.receiver_not_null")));
  }
};

const requiredRule = {
  required: true,
  message: t("common.required"),
  trigger: ["blur", "change"],
};
const reciValRule = {
  validator: checkReciValValidator,
  trigger: ["blur", "change"],
};
const maxLengthRule = (max = 50) => {
  return {
    max: max,
    message: t("data_fill.form.input_limit_max", [max]),
    trigger: ["blur", "change"],
  };
};
const minLengthRule = (min = 0) => {
  return {
    min: min,
    message: t("data_fill.form.input_limit_min", [min]),
    trigger: ["blur", "change"],
  };
};


const defaultFormData = {
  reciVal: [] as string[],
  uidList: [] as string[],
  ridList: [] as string[],
  name: "",
  fillType: 0,
  fitType: 0,
  fitColumn: undefined,
  formExtSetting: [],
  formFilterSetting: []
}

const formState = ref(JSON.parse(JSON.stringify(defaultFormData)));

const templateSetting = ref([])


// method area
const setHeight = () => {
  if(reciSelector?.value?.setHeight) {
    reciSelector.value.setHeight()
  }
};

function setOwnSelectHeight() {
  setHeight()
}

function onFillTypeChange() {
  nextTick(() => {
    setOwnSelectHeight()
  })
}

const getFormData = async () => {
  if (!validateCur()) {
    return;
  }
  const p = new Promise((r, e) => {
    reportReciForm?.value?.validate((valid) => {
      r(valid && formatReci2Data());
    });
  });
  return await p;
};

const formatReci2Data = () => {
  const tempUidList = [] as string[];
  const tempRidList = [] as string[];
  formState.value.reciVal.forEach((id) => {
    if (id.startsWith("0")) {
      tempRidList.push(id.substring(1));
    } else {
      tempUidList.push(id.substring(1));
    }
  })
  formState.value.uidList = [...tempUidList]
  formState.value.ridList = [...tempRidList]

  const data = JSON.parse(JSON.stringify(formState.value))
  data.formExtSetting = JSON.stringify(formState.value.formExtSetting)
  data.formFilterSetting = JSON.stringify(formState.value.formFilterSetting)
  return data
}

const numberInputFormRef = ref()

const numberInputList = computed(() => {
  return filter(formState.value.formExtSetting, f => f.inputType === 'number' && f.type === 'input')
})

const filteredInputList = ref<Array<any>>([])
const numberRuleForms = ref<Array<{ term: string, column: string }>>([])

const getOtherNumberInputList = (id) => {
  filteredInputList.value = filter(numberInputList.value, n => n.id !== id)
}

const hasMultiNumberInput = computed(() => {
  return numberInputList.value.length > 1
})

const currentRuleRowId = ref(undefined)

function openAllowEditDialog(row) {
  allowEditDialogVisible.value = true
  getOtherNumberInputList(row.id)
  currentRuleRowId.value = row.id
  numberRuleForms.value = JSON.parse(JSON.stringify(row.numberInputRules ?? []))
}

function closeAllowEditDialog() {
  allowEditDialogVisible.value = false
  currentRuleRowId.value = undefined
  numberRuleForms.value = []
}

function deleteRow(index) {
  numberRuleForms.value.splice(index, 1);
}

function addRuleRow() {
  numberRuleForms.value.push({
    term: 'lt',
    column: undefined
  })
}

function confirmRules() {
  numberInputFormRef.value?.validate((valid) => {
    if (valid) {
      templateSetting.value.forEach(s => {
        if (s.id === currentRuleRowId.value) {
          s.numberInputRules = JSON.parse(JSON.stringify(numberRuleForms.value))
          closeAllowEditDialog()
        }
      })
    }
  })
}

const valueOptions = [
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
]

const formatReci2Form = () => {
  formState.value.formExtSetting = map(props.forms, s => {
    return {
      icon: s.icon,
      id: s.id,
      disable: false,
      name: s.settings.name,
      type: s.type,
      inputType: s.settings.inputType,
      numberInputRules: []
    }
  })

  if (props.reciFormData && props.reciFormData['formExtSetting']) {
    const ss = JSON.parse(props.reciFormData['formExtSetting'])
    if (ss && ss.length > 0) {
      const temp = {}
      const tempRules = {}
      forEach(ss, s => {
        temp[s.id] = s.disable
        tempRules[s.id] = s.numberInputRules
      })

      forEach(formState.value.formExtSetting, s => {
        s.disable = !!temp[s.id]
        s.numberInputRules = tempRules[s.id] ?? []
      })
    }
  }
  if (props.reciFormData && props.reciFormData['formFilterSetting']) {
    const ss = JSON.parse(props.reciFormData['formFilterSetting'])
    if (ss) {
      formState.value.formFilterSetting = ss

      for (let i = 0; i < formState.value.formFilterSetting.length; i++) {
        const rule = formState.value.formFilterSetting[i]
        const _c = find(props.columns, c => c.props === rule.column)
        rule.id = _c.id
        rule.options = _c.options
        rule.asyncOptions = _c.asyncOptions
        rule.asyncOptionSetting = _c.asyncOptionSetting
        rule.type = _c.type
        rule.multiple = _c.multiple
      }

      const queries: Array<any> = []
      const queryIds: Array<string> = []
      for (let i = 0; i < formState.value.formFilterSetting.length; i++) {
        const f = formState.value.formFilterSetting[i]
        if (!f.asyncOptions) {
          continue
        }
        const p = getTableColumnData(props.formId, f.id)
        queries.push(p)
        queryIds.push(f.id)
      }
      if (queries.length > 0) {
        Promise.all(queries).then((val) => {
          for (let i = 0; i < queryIds.length; i++) {
            const id = queryIds[i]
            asyncOptions.value[id] = val[i].data
          }
        }).finally(() => {
        })
      }

    }
  }

  if (props.isEdit && props.reciFormData) {
    for (const key in formState.value) {
      if (key === 'formExtSetting' || key === 'formFilterSetting') {
        continue
      }
      formState.value[key] = props.reciFormData[key]
      if (key === 'reciVal') {
        formState.value[key] = []
      }
    }
    formState.value["fitColumn"] = props.reciFormData["fitColumn"];
  }

  if (formState.value.uidList) {
    formState.value.uidList.forEach((id) => {
      formState.value.reciVal.push(`1${id}`);
    });
  } else {
    formState.value.uidList = [];
  }
  if (formState.value.ridList) {
    formState.value.ridList.forEach((id) => {
      formState.value.reciVal.push(`0${id}`);
    });
  } else {
    formState.value.ridList = [];
  }
};


const validateCur = () => {
  if (formState.fillType !== 0) {
    return true;
  }
  if (formState.value.reciVal?.length > 0) {
    return true;
  } else {
    ElMessage.error(t("data_fill.task.receiver_not_null"));
    return false;
  }
};

function openTemplateSetting() {
  templateSetting.value = JSON.parse(JSON.stringify(formState.value.formExtSetting))
  templateVisible.value = true
}

const closeTemplateDialog = () => {
  templateVisible.value = false
}

const filterSettings = ref([])

const filterSettingsFormRef = ref()

const filterTypes1 = [
  {type: 'rule', name: t('data_fill.task.logic_filter')}
]
const filterTypes2 = [
  {type: 'rule', name: t('data_fill.task.logic_filter')},
  {type: 'enum', name: t('data_fill.task.enum_filter')}
]

const stringFilterOptions = [
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

const selectFilterOptions = [
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

function getFilterOptions(rule) {
  if (rule.date || rule.number) {
    return valueFilterOptions
  }
  if (rule.type === 'select' || rule.type === 'radio' || rule.type === 'checkbox') {
    return selectFilterOptions
  }
  return stringFilterOptions
}

function onFilterColumnChange(rule) {
  const _c = find(props.columns, c => c.props === rule.column)
  const _oldC = rule.oldColumn ? find(props.columns, c => c.props === rule.oldColumn) : undefined

  const hasEnum = _c.type === 'select' || _c.type === 'radio' || _c.type === 'checkbox'
  if (hasEnum) {
    rule.filterTypeList = JSON.parse(JSON.stringify(filterTypes2))
  } else {
    rule.filterTypeList = JSON.parse(JSON.stringify(filterTypes1))
  }
  if (!map(rule.filterTypeList, 'type').includes(rule.filterType)) { //不包括说明是字段从多选的切换为其他的，直接清空值
    rule.value = undefined
    rule.filterType = 'rule'
  }

  if (_c.number !== _oldC?.number) {
    //rule.value = undefined
  }

  rule.id = _c.id
  rule.number = _c.number
  rule.date = _c.date
  rule.dateType = _c.dateType
  rule.oldColumn = rule.column
  rule.options = _c.options
  rule.asyncOptions = _c.asyncOptions
  rule.asyncOptionSetting = _c.asyncOptionSetting
  rule.type = _c.type
  rule.multiple = _c.multiple

  if (rule.date) {
    if (rule.value) {
      const d = dayjs(rule.value)
      if (!d.isValid()) {
        rule.value = undefined
      } else {
        rule.value = d.valueOf()
      }
    }
  }

  const termList = getFilterOptions(rule)
  const termSimpleList = []
  for (let i = 0; i < termList.length; i++) {
    const options = termList[i].options
    for (let j = 0; j < options.length; j++) {
      termSimpleList.push(options[j].value)
    }
  }
  if (!termSimpleList.includes(rule.term)) {
    rule.term = undefined
  }
}

function onFilterTypeChange(rule) {
  if (rule.filterType === 'rule') {
    rule.value = undefined
  } else {
    rule.valueList = []
    getAsyncOption(rule)
  }
}

const asyncOptions = ref({})
const filterLoading = ref(false)

function getAsyncOption(rule) {
  if (rule.filterType === 'rule') {
    return
  }
  if (!rule.asyncOptions) {
    return
  }
  filterLoading.value = true
  getTableColumnData(props.formId, rule.id)
      .then(res => {
        asyncOptions.value[rule.id] = res.data
      })
      .finally(() => {
        filterLoading.value = false
      })
}

function deleteFilterRow(index) {
  filterSettings.value.splice(index, 1);
}

function addFilter() {
  filterSettings.value.push({})
}

function openFilterSetting() {
  filterSettings.value = JSON.parse(JSON.stringify(formState.value.formFilterSetting))
  filterVisible.value = true
  if (filterSettings.value.length === 0) {
    addFilter()
  }
}

const closeFilterDialog = () => {
  filterVisible.value = false
}

const multiSelectRuleValidator = (rule, value, callback) => {
  if (value === undefined || value.length === 0) {
    return callback(new Error(t('common.required')))
  }
  callback()
}

const saveFilterDialog = () => {
  filterSettingsFormRef.value?.validate((valid) => {
    if (valid) {
      formState.value.formFilterSetting = JSON.parse(JSON.stringify(filterSettings.value))
      filterVisible.value = false
    }
  })
}

const previewTemplateDialog = () => {
  rowDataFormRef.value?.preview(props.formId, templateSetting.value)
}
const saveTemplateDialog = () => {
  if (every(templateSetting.value, s => s.disable)) {
    ElMessage.error(t('data_fill.task.cannot_be_all_disabled'))
    return
  }
  formState.value.formExtSetting = JSON.parse(JSON.stringify(templateSetting.value))
  templateVisible.value = false
}

const init = async () => {
  formState.value = JSON.parse(JSON.stringify(defaultFormData));
  formatReci2Form();
};
defineExpose({
  getFormData
});
onMounted(() => {
  init()
      .then(() => {
        setOwnSelectHeight()
      })
});
</script>

<style lang="less">
.reci-custom-email-popper {
  display: none !important;
}
</style>
<style scoped lang="less">
.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}


.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}


.report-form-container {
  height: 100%;
  margin: 0 auto;
  width: 600px;

  .report-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin-top: 24px;
    margin-bottom: 16px;

    .title-flag {
      height: 16px;
      line-height: 16px;
      border-left: 2px solid var(--ed-color-primary, #3370ff);
    }

    .form-title {
      color: #1f2329;
      font-weight: 500;
      font-family: var(--de-custom_font, "PingFang");
      line-height: 24px;
      font-size: 16px;
      padding-left: 8px;
    }
  }

  .report-form {
    width: 600px;
    padding-bottom: 16px;

    .ed-form-item {
      margin-bottom: 16px;
    }

    .is-error {
      margin-bottom: 40px !important;
    }

    :deep(.disabled-platform-option) {
      display: none;
    }
  }

  :deep(.ed-form-item__label) {
    line-height: 22px;
    height: 22px;
  }

}

.dv-selector {
  width: 100%;
}

.label-content-details {
  width: 100%;
  display: flex;
  align-items: center;
}

.reci-custom-email-select {
  width: 100%;

  :deep(.ed-input__suffix) {
    display: none;
  }

  :deep(.ed-select-tags-wrapper) {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }

  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}


.ed-select__tags {
  .ed-select-tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }

  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}

.filter-settings {
  .form-item {
    margin-top: 8px;
    margin-bottom: 8px;

    :deep(.ed-date-editor) {
      .ed-input__wrapper {
        width: 100%;
      }
    }

  }
}
</style>
