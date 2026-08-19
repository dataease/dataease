<script setup lang="ts">
import {
  cloneDeep,
  concat,
  filter,
  find,
  floor,
  forEach,
  includes,
  map,
} from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";
import { ElLoading, FormInstance, FormItemRule } from "element-plus-secondary";
import { EMAIL_REGEX } from "@/utils/validate";
import {
  DfFormItem,
  getDataFilling,
  getDataFillingTemplateSettings,
  getExtraDetailsApi,
  getTableColumnData,
  saveFormRowData,
  searchTable,
} from "../../data-filling";
import { appendTaskRowData, saveTaskRowData } from "../../fill/fill_api";
import MoreDetailColumns from "./MoreDetailColumns.vue";
import dayjs from "dayjs";

const { t } = useI18n();
const loading = ref(false);
const loadingInstance = ref(null);

const showGlobalLoading = () => {
  if (loadingInstance.value) {
    return;
  }
  loadingInstance.value = ElLoading.service({
    target: "body",
  });
};

const closeGlobalLoading = () => {
  loadingInstance.value?.close();
  loadingInstance.value = null;
};

watch(loading, (val) => {
  if (val) {
    showGlobalLoading();
  } else {
    closeGlobalLoading();
  }
});

onBeforeUnmount(() => {
  closeGlobalLoading();
});

const emit = defineEmits(["finish"]);

interface DfFormItemExt extends DfFormItem {}

const formData = ref<Array<DfFormItemExt>>([]);
const baseForm = ref<Array<DfFormItemExt>>([]);
const rowDataKeyName = ref<string | undefined>(undefined);
const title = ref("");
const edit = ref(false);
const formId = ref<string | undefined>(undefined);
const showDialog = ref(false);
const showTable = ref(false);
const previewType = ref(false);
const saveToClose = ref(false); //保存后直接关闭
const noTemplateType = ref(false);

watch(
  () => showDialog.value,
  (val) => {
    if (!val) {
      closeGlobalLoading();
    }
  },
);

const asyncOptions = ref({});

const mForm = ref<FormInstance>();

const rowDataIds = ref<
  Array<{
    rowDataId?: string;
    taskItemId?: string;
  }>
>([]);
const currentRowDataIndex = ref<number | undefined>();

const taskInstanceId = ref<string | undefined>(undefined);

const readonly = computed<boolean>(() => {
  return !edit.value;
});

const templateSettings = ref([]);

function getReadonly(item) {
  return readonly.value;
}

function getDisabled(item) {
  const templateSetting = find(templateSettings.value, (s) => s.id === item.id);
  return !!templateSetting?.disable;
}

const showMoreDetails = ref(false);
const moreDetails = ref([]);

const showAllDetails = (list) => {
  showMoreDetails.value = true;
  moreDetails.value = list;
};

const getExtraDetails = (element: DfFormItem) => {
  element.extraDetails = [];
  if (
    (element.type === "radio" ||
      (element.type === "select" && !element.settings.multiple)) &&
    element.value !== undefined &&
    element.settings?.optionSourceType === 2 &&
    element.settings?.extraColumns &&
    element.settings?.extraColumns.length > 0 &&
    element.settings?.optionDatasource &&
    element.settings?.optionTable
  ) {
    //查询值
    getExtraDetailsApi({
      value: element.value,
      columnId: element.id,
      formId: formId.value,
    })
      .then((res) => {
        element.extraDetails = res.data;
      })
      .catch((e) => {
        element.extraDetails = [];
      });
  }
};

function getSelectOptions(item) {
  let list = [];
  if (item == undefined) {
    return list;
  }
  list = map(
    item.settings.optionSourceType === 1
      ? item.settings.options
      : asyncOptions.value[item.id]
        ? asyncOptions.value[item.id]
        : [],
    (i) => {
      return {
        label: i.name,
        value: i.value,
      };
    },
  );
  tempOptionListMap.value[item.id] = list;
  return list;
}

function preview(_formId: string, settings: Array<any>) {
  previewType.value = true;
  init(_formId, true);
  edit.value = true;
  templateSettings.value = settings;
}

const tempOptionListMap = ref({});

function init(
  _formId: string,
  noTemplate: boolean = false,
  _edit: boolean = false,
  _rowDataIds: Array<{
    rowDataId: number;
    taskItemId?: string;
  }> = [],
  _currentRowDataIndex: number = 0,
  _saveToClose: boolean = false,
  _taskInstanceId: string | undefined = undefined,
) {
  formId.value = _formId;
  edit.value = _edit;
  taskInstanceId.value = _taskInstanceId;
  noTemplateType.value = noTemplate;

  //todo 下一个，上一个
  rowDataIds.value = _rowDataIds;
  currentRowDataIndex.value = _currentRowDataIndex;
  saveToClose.value = _saveToClose;
  loading.value = true;
  showDialog.value = true;
  rowDataKeyName.value = undefined;
  errorMsg.value = undefined;
  tempOptionListMap.value = {};
  getDataFilling(formId.value).then((res) => {
    title.value = res.name;
    const tempForms = filter(JSON.parse(res.forms), (f) => !f.removed);
    forEach(tempForms, (f) => {
      if (
        f.type === "checkbox" ||
        (f.type === "select" && f.settings.multiple)
      ) {
        f.value = []; //防止报错
      }
    });

    if (!noTemplateType.value) {
      getDataFillingTemplateSettings(
        rowDataIds.value[currentRowDataIndex.value].taskItemId,
      ).then((d) => {
        if (d) {
          templateSettings.value = JSON.parse(d);
        }
      });
    }

    initFormOptionsData(tempForms, () => {
      baseForm.value = cloneDeep(tempForms);

      setData(currentRowDataIndex.value, () => {
        nextTick(() => {
          formData.value.forEach((f) => {
            if (f.type === "select") {
              getSelectOptions(f);
            }
          });
        });
      });
    });
  });
}

const errorMsg = ref<string | undefined>(undefined);

function setData(index: number = 0, callback?: (...params: any[]) => any) {
  const _rowDataItem = rowDataIds.value[index];
  if (_rowDataItem?.rowDataId != undefined) {
    //获取数据
    rowDataKeyName.value = undefined;
    searchTable(formId.value, {
      primaryKeyValueList: [_rowDataItem.rowDataId],
      withoutLogs: true,
      currentPage: 1,
      pageSize: 0,
    })
      .then((res) => {
        if (res.data) {
          if (res.data.data.length === 0) {
            // 没找到怎么办？
          }
          const _data = res.data.data[0] ?? undefined;
          if (_data == undefined) {
            errorMsg.value = t("data_fill.data.data_not_exists");
            return;
            //_data[res.data.key] = _rowDataItem.rowDataId
          }
          rowDataKeyName.value = res.data.key;

          const _tempForms = cloneDeep(baseForm.value);
          forEach(_tempForms, (f) => {
            const _value = _data[f.settings.mapping.columnName] ?? undefined;
            //赋值
            if (f.type === "date") {
              if (_value) {
                f.value = new Date(_value);
              }
            } else if (f.type === "dateRange") {
              let _start = _data[f.settings.mapping.columnName1];
              if (_start) {
                _start = new Date(_start);
              }
              let _end = _data[f.settings.mapping.columnName2];
              if (_end) {
                _end = new Date(_end);
              }
              f.value = [_start, _end];
            } else if (
              (f.type === "select" && f.settings.multiple) ||
              f.type === "checkbox"
            ) {
              if (_value) {
                // 过滤一下选项值
                let _list = [];
                try {
                  _list = JSON.parse(_value);
                } catch (e) {
                  console.error(e);
                }
                if (readonly.value) {
                  f.value = _list;
                } else {
                  const tempId = f.id ?? "unset";
                  const options = map(
                    f.settings.optionSourceType === 1
                      ? f.settings.options
                      : asyncOptions.value[tempId]
                        ? asyncOptions.value[tempId]
                        : [],
                    (f) => f.value,
                  );
                  f.value = filter(_list, (v) => includes(options, v));
                }
              } else {
                f.value = [];
              }
            } else if (
              (f.type === "select" && !f.settings.multiple) ||
              f.type === "radio"
            ) {
              if (_value) {
                if (!readonly.value) {
                  const tempId = f.id ?? "unset";
                  const options = map(
                    f.settings.optionSourceType === 1
                      ? f.settings.options
                      : asyncOptions.value[tempId]
                        ? asyncOptions.value[tempId]
                        : [],
                    (f) => f.value,
                  );
                  if (!includes(options, _value)) {
                    f.value = undefined;
                  } else {
                    f.value = _value;
                  }
                } else {
                  f.value = _value;
                }
              } else {
                f.value = _value;
              }
            } else {
              f.value = _value;
            }

            const tempId = f.id ?? "unset";

            if (tempOptionListMap.value[tempId] == undefined) {
              tempOptionListMap.value[tempId] = [];
            }
          });

          formData.value = _tempForms;
        }

        forEach(formData.value, (f) => {
          getExtraDetails(f);
        });

        setDefaultTimes();

        showTable.value = true;
        loading.value = false;
      })
      .finally(() => {
        loading.value = false;

        if (callback) {
          callback();
        }
      });
  } else {
    const _tempForms = cloneDeep(baseForm.value);
    forEach(_tempForms, (f) => {
      const tempId = f.id ?? "unset";
      f.value = undefined;
      if (
        (f.type === "select" && f.settings.multiple) ||
        f.type === "checkbox"
      ) {
        f.value = [];
      } else if (f.type === "dateRange") {
        f.value = [undefined, undefined];
      }
      if (tempOptionListMap.value[tempId] == undefined) {
        tempOptionListMap.value[tempId] = [];
      }
    });
    formData.value = _tempForms;

    setDefaultTimes();

    showTable.value = true;
    loading.value = false;

    if (callback) {
      callback();
    }
  }
}

function setDefaultTimes() {
  //处理默认时间
  for (let i = 0; i < formData.value.length; i++) {
    const _f = formData.value[i];
    if (getReadonly(_f) || getDisabled(_f)) {
      continue;
    }
    if (_f.type === "date") {
      if (_f.settings.enableDefaultTime && _f.value === undefined) {
        if (_f.settings.enableCurrentTime) {
          _f.value = new Date();
        } else {
          if (_f.settings.defaultTime !== undefined) {
            _f.value = new Date(_f.settings.defaultTime);
          }
        }
      }
    }
  }
}

function initFormOptionsData(
  forms: Array<DfFormItem>,
  callback?: (...params: any[]) => any,
) {
  const queries: Array<any> = [];
  const queryIds: Array<string> = [];
  forEach(forms, (f) => {
    if (f.type === "checkbox" || f.type === "select" || f.type === "radio") {
      if (
        f.settings &&
        f.settings.optionSourceType === 2 &&
        f.settings.optionDatasource &&
        f.settings.optionTable &&
        f.settings.optionColumn &&
        f.settings.optionOrder
      ) {
        const id = f.id ?? "unset";

        const p = getTableColumnData(formId.value, f.id);
        queries.push(p);
        queryIds.push(id);
      }
    }
  });

  if (queries.length > 0) {
    Promise.all(queries)
      .then((val) => {
        for (let i = 0; i < queryIds.length; i++) {
          const id = queryIds[i];
          asyncOptions.value[id] = val[i].data;
        }
      })
      .finally(() => {
        if (callback) {
          callback();
        }
      });
  } else {
    if (callback) {
      callback();
    }
  }
}

const checkDateRangeRequireValidator = (rule, value, callback) => {
  if (!value) {
    return callback(new Error(t("common.required")));
  }
  if (value.length < 2) {
    return callback(new Error(t("common.required")));
  }
  if (!value[0]) {
    return callback(new Error(t("common.required")));
  }
  if (!value[1]) {
    return callback(new Error(t("common.required")));
  }
  callback();
};

const requiredRule = {
  required: true,
  message: t("common.required"),
  trigger: ["blur", "change"],
};

const dateRangeRequiredRule = {
  validator: checkDateRangeRequireValidator,
  trigger: ["blur", "change"],
};

const numberRuleValidator = (rule, value, callback) => {
  if (value !== undefined) {
    for (let i = 0; i < rule.custom.length; i++) {
      const r = rule.custom[i];
      const f = find(formData.value, (d) => d.id === r.column);
      if (f?.value !== undefined) {
        switch (r.term) {
          case "lt":
            if (value >= f.value) {
              return callback(
                new Error(
                  t("data_fill.form.lt_check", [f.settings.name, f.value]),
                ),
              );
            }
            break;
          case "gt":
            if (value <= f.value) {
              return callback(
                new Error(
                  t("data_fill.form.gt_check", [f.settings.name, f.value]),
                ),
              );
            }
            break;
          case "le":
            if (value > f.value) {
              return callback(
                new Error(
                  t("data_fill.form.le_check", [f.settings.name, f.value]),
                ),
              );
            }
            break;
          case "ge":
            if (value < f.value) {
              return callback(
                new Error(
                  t("data_fill.form.ge_check", [f.settings.name, f.value]),
                ),
              );
            }
            break;
        }
      }
    }
  }
  callback();
};

const customNumberRule = (obj) => {
  return {
    validator: numberRuleValidator,
    trigger: ["blur", "change"],
    custom: obj ?? [],
  };
};

const pickerOptions = {
  disabledDate: (time) => {
    return time.getTime() < new Date(0).getTime();
  },
};

function onNumberChange(item) {
  let value: number | null = null;
  if (item.value !== null) {
    if (item.settings.mapping.type === "number") {
      value = floor(item.value, 0);
    } else {
      value = floor(item.value, 8);
    }
  }
  nextTick(() => {
    item.value = value;
  });
}

const inputTypes = [
  { type: "text", name: t("data_fill.form.text"), rules: [] },
  { type: "number", name: t("data_fill.form.number"), rules: [] },
  {
    type: "tel",
    name: t("data_fill.form.tel"),
    rules: [],
  },
  {
    type: "email",
    name: t("data_fill.form.email"),
    rules: [
      {
        pattern: EMAIL_REGEX,
        message: t("data_fill.form.email_format_is_incorrect"),
        trigger: ["blur", "change"],
      },
    ],
  },
];

function getRules(item: DfFormItem) {
  let rules: Array<FormItemRule> = [];
  if (item.settings.required) {
    rules.push(requiredRule);
    if (item.type === "dateRange") {
      rules.push(dateRangeRequiredRule);
    }
  }
  if (item.type === "input") {
    const inputRules = find(
      inputTypes,
      (t) => t.type === item.settings.inputType,
    )?.rules;
    if (inputRules) {
      rules = concat(rules, inputRules);
    }
  }
  if (item.type === "input" && item.settings.inputType === "number") {
    const templateSetting = find(
      templateSettings.value,
      (s) => s.id === item.id,
    );
    if (templateSetting?.numberInputRules?.length > 0) {
      rules.push(customNumberRule(templateSetting.numberInputRules));
    }
  }
  return rules;
}

function reset() {
  title.value = "";
  formId.value = undefined;
  rowDataIds.value = [];
  currentRowDataIndex.value = undefined;
  mForm.value?.resetFields();
  showTable.value = false;
  formData.value = [];
  baseForm.value = [];
  showDialog.value = false;
  rowDataKeyName.value = undefined;
  errorMsg.value = undefined;
  templateSettings.value = [];
}

const edited = ref(false);

function closeDialog() {
  reset();
  if (edited.value) {
    emit("finish");
  }
}

function doSave() {
  loading.value = true;
  mForm.value?.validate((valid, invalidFields) => {
    if (valid) {
      const _data = {};
      forEach(formData.value, (f) => {
        if (f.type === "dateRange") {
          const _start = f.settings.mapping.columnName1;
          const _end = f.settings.mapping.columnName2;
          if (f.value) {
            if (f.value[0]) {
              _data[_start] = dayjs(
                dayjs(f.value[0]).format("YYYY-MM-DD HH:mm:ss"),
              ).valueOf();
            }
            if (f.value[1]) {
              _data[_end] = dayjs(
                dayjs(f.value[1]).format("YYYY-MM-DD HH:mm:ss"),
              ).valueOf(); //去除毫秒最后的三位999
            }
          }
        } else {
          const name = f.settings.mapping.columnName;
          if (
            (f.type === "select" && f.settings.multiple) ||
            f.type === "checkbox"
          ) {
            if (f.value) {
              _data[name] = JSON.stringify(f.value);
            }
          } else if (f.type === "date" && f.value) {
            _data[name] = dayjs(
              dayjs(f.value).format("YYYY-MM-DD HH:mm:ss"),
            ).valueOf();
          } else {
            _data[name] = f.value;
          }
        }
      });

      if (rowDataKeyName.value) {
        _data[rowDataKeyName.value] =
          rowDataIds.value[currentRowDataIndex.value]?.rowDataId; //undefined 就是插入新数据
      }

      if (
        taskInstanceId.value &&
        !rowDataIds.value[currentRowDataIndex.value]?.taskItemId
      ) {
        appendTaskRowData(taskInstanceId.value, _data)
          .then((res) => {
            edited.value = true;
            rowDataKeyName.value = res.key;
            if (rowDataIds.value[currentRowDataIndex.value] === undefined) {
              rowDataIds.value[currentRowDataIndex.value] = {};
            }
            if (
              rowDataIds.value[currentRowDataIndex.value].rowDataId ===
              undefined
            ) {
              rowDataIds.value[currentRowDataIndex.value].rowDataId =
                res.data[res.key];
            }
            if (saveToClose.value) {
              closeDialog();
            }
          })
          .finally(() => {
            loading.value = false;
          });
      } else if (rowDataIds.value[currentRowDataIndex.value]?.taskItemId) {
        saveTaskRowData(
          rowDataIds.value[currentRowDataIndex.value]?.taskItemId,
          _data,
        )
          .then((res) => {
            edited.value = true;
            rowDataKeyName.value = res.key;
            if (rowDataIds.value[currentRowDataIndex.value] === undefined) {
              rowDataIds.value[currentRowDataIndex.value] = {};
            }
            if (
              rowDataIds.value[currentRowDataIndex.value].rowDataId ===
              undefined
            ) {
              rowDataIds.value[currentRowDataIndex.value].rowDataId =
                res.data[res.key];
            }
            if (saveToClose.value) {
              closeDialog();
            }
          })
          .finally(() => {
            loading.value = false;
          });
      } else {
        saveFormRowData(formId.value, _data)
          .then((res) => {
            edited.value = true;
            rowDataKeyName.value = res.key;
            if (rowDataIds.value[currentRowDataIndex.value] === undefined) {
              rowDataIds.value[currentRowDataIndex.value] = {};
            }
            if (
              rowDataIds.value[currentRowDataIndex.value].rowDataId ===
              undefined
            ) {
              rowDataIds.value[currentRowDataIndex.value].rowDataId =
                res.data[res.key];
            }
            if (saveToClose.value) {
              closeDialog();
            }
          })
          .finally(() => {
            loading.value = false;
          });
      }
    } else {
      loading.value = false;
    }
  });
}

const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    //doSave()
  }
};
const removeKeyDown = () => {
  window.removeEventListener("keydown", keyFunction);
};
const addKeyDown = () => {
  window.addEventListener("keydown", keyFunction);
};

defineExpose({ init, closeDialog, preview });
</script>

<template>
  <el-dialog
    :before-close="reset"
    v-model="showDialog"
    :title="title"
    :close-on-click-modal="false"
    append-to-body
    destroy-on-close
    width="840px"
    @open="addKeyDown"
    @close="removeKeyDown"
    class="df-data-form"
    modal-class="custom-class"
  >
    <el-main class="row-de-main">
      <el-form
        ref="mForm"
        class="m-form"
        label-position="top"
        hide-required-asterisk
        :model="formData"
        @submit.native.prevent
      >
        <div
          v-if="showTable"
          v-for="(item, $index2) in formData"
          :key="item.id"
          class="m-item m-form-item"
        >
          <div class="m-label-container">
            <span style="width: unset">
              {{ item.settings?.name }}
              <span v-if="item.settings.required" class="df-input-require"
                >*</span
              >
            </span>
          </div>
          <el-form-item
            :prop="'[' + $index2 + '].value'"
            class="form-item"
            :readonly="getReadonly(item)"
            :disabled="getDisabled(item)"
            :rules="getRules(item)"
          >
            <el-input
              v-if="
                item.type === 'input' && item.settings.inputType !== 'number'
              "
              v-model="item.value"
              :type="item.settings.inputType"
              :required="item.settings.required"
              :readonly="getReadonly(item)"
              :disabled="getDisabled(item)"
              :placeholder="item.settings.placeholder"
              :show-word-limit="
                item.value !== undefined &&
                item.value !== null &&
                item.value.length > 250
              "
              maxlength="255"
            />
            <el-input-number
              v-if="
                item.type === 'input' && item.settings.inputType === 'number'
              "
              v-model="item.value"
              :required="item.settings.required"
              :disabled="getReadonly(item) || getDisabled(item)"
              :placeholder="item.settings.placeholder"
              style="width: 100%"
              controls-position="right"
              :precision="
                item.settings.mapping.type === 'number' ? 0 : undefined
              "
              title=""
              :min="-999999999999"
              :max="999999999999"
              @change="onNumberChange(item)"
              @blur="onNumberChange(item)"
              @keyup.enter.native="onNumberChange(item)"
            />
            <el-input
              v-else-if="item.type === 'textarea'"
              v-model="item.value"
              type="textarea"
              :required="item.settings.required"
              :readonly="getReadonly(item)"
              :disabled="getDisabled(item)"
              :placeholder="item.settings.placeholder"
            />
            <el-select-v2
              v-else-if="item.type === 'select'"
              v-model="item.value"
              :required="item.settings.required"
              :disabled="getReadonly(item) || getDisabled(item)"
              :placeholder="item.settings.placeholder"
              style="width: 100%"
              filterable
              :multiple="item.settings.multiple"
              clearable
              :options="tempOptionListMap[item.id]"
              @change="getExtraDetails(item)"
            >
            </el-select-v2>
            <el-radio-group
              v-else-if="item.type === 'radio'"
              v-model="item.value"
              :required="item.settings.required"
              :disabled="getReadonly(item) || getDisabled(item)"
              style="width: 100%"
              @change="getExtraDetails(item)"
            >
              <el-radio
                v-for="(x, $index) in item.settings.optionSourceType === 1
                  ? item.settings.options
                  : asyncOptions[item.id]
                    ? asyncOptions[item.id]
                    : []"
                :key="$index"
                :label="x.value"
                ><span :title="x.name">{{ x.name }}</span>
              </el-radio>
            </el-radio-group>
            <el-checkbox-group
              v-else-if="item.type === 'checkbox'"
              v-model="item.value"
              :required="item.settings.required"
              :disabled="getReadonly(item) || getDisabled(item)"
            >
              <el-checkbox
                v-for="(x, $index) in item.settings.optionSourceType === 1
                  ? item.settings.options
                  : asyncOptions[item.id]
                    ? asyncOptions[item.id]
                    : []"
                :key="$index"
                :label="x.value"
                ><span :title="x.name">{{ x.name }}</span>
              </el-checkbox>
            </el-checkbox-group>
            <el-date-picker
              v-else-if="item.type === 'date'"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="getReadonly(item)"
              :disabled="getDisabled(item)"
              :type="item.settings.dateType"
              :placeholder="item.settings.placeholder"
              style="width: 100%"
              :picker-options="pickerOptions"
            />
            <el-date-picker
              v-else-if="item.type === 'dateRange'"
              v-model="item.value"
              :required="item.settings.required"
              :readonly="getReadonly(item)"
              :disabled="getDisabled(item)"
              :type="item.settings.dateType"
              :range-separator="item.settings.rangeSeparator"
              :start-placeholder="item.settings.startPlaceholder"
              :end-placeholder="item.settings.endPlaceholder"
              style="width: 100%"
              :picker-options="pickerOptions"
            />
          </el-form-item>
          <template
            v-if="
              item.type === 'radio' ||
              (item.type === 'select' && !item.settings.multiple)
            "
          >
            <div
              style="
                width: 100%;
                display: flex;
                margin-top: 8px;
                padding: 8px;
                margin-bottom: 8px;
              "
              v-if="item.extraDetails?.length > 0"
            >
              <div class="df-ex-detail">
                <div class="df-ex-row">
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[0].name +
                      ' : ' +
                      item.extraDetails[0].value
                    "
                    v-if="item.extraDetails[0]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[0].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[0].value
                    }}</span>
                  </div>
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[1].name +
                      ' : ' +
                      item.extraDetails[1].value
                    "
                    v-if="item.extraDetails[1]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[1].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[1].value
                    }}</span>
                  </div>
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[2].name +
                      ' : ' +
                      item.extraDetails[2].value
                    "
                    v-if="item.extraDetails[2]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[2].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[2].value
                    }}</span>
                  </div>
                </div>
                <div class="df-ex-row">
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[3].name +
                      ' : ' +
                      item.extraDetails[3].value
                    "
                    v-if="item.extraDetails[3]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[3].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[3].value
                    }}</span>
                  </div>
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[4].name +
                      ' : ' +
                      item.extraDetails[4].value
                    "
                    v-if="item.extraDetails[4]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[4].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[4].value
                    }}</span>
                  </div>
                  <div
                    class="df-ex-col"
                    :title="
                      item.extraDetails[5].name +
                      ' : ' +
                      item.extraDetails[5].value
                    "
                    v-if="item.extraDetails[5]"
                  >
                    <span class="label-no-warp">{{
                      item.extraDetails[5].name
                    }}</span>
                    <span class="desc-column">{{
                      item.extraDetails[5].value
                    }}</span>
                  </div>
                </div>
              </div>
              <el-button
                text
                type="primary"
                @click="showAllDetails(item.extraDetails)"
              >
                {{ t("data_fill.form.show_more_detail") }}
              </el-button>
            </div>
          </template>
        </div>
        <div v-else-if="errorMsg">{{ errorMsg }}</div>
      </el-form>

      <MoreDetailColumns
        v-model:show="showMoreDetails"
        :details="moreDetails"
      />
    </el-main>
    <template #footer>
      <div class="de-footer">
        <el-button @click="closeDialog"
          >{{
            !readonly && !previewType ? t("common.cancel") : t("commons.close")
          }}
        </el-button>
        <el-button
          v-if="!readonly && !previewType"
          :disabled="loading || formData.length == 0 || errorMsg != undefined"
          type="primary"
          @click="doSave"
        >
          {{ t("commons.save") }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less">
.df-data-form {
  .ed-dialog__body {
    padding: 24px 24px 8px 24px !important;
    max-height: calc(90vh - 48px - 56px);
    min-height: 240px;
    display: flex;
  }
}
</style>

<style scoped lang="less">
.row-de-main {
  padding: 0;
  flex: 1;

  .df-input-require {
    color: red;
    margin-left: 2px;
  }

  .m-form-item {
    border-radius: 4px;

    border: solid 1px transparent;
    background-color: unset;

    padding: 0;

    .ed-form-item {
      margin-bottom: 16px;
    }

    .m-label-container {
      margin-bottom: 8px;
      line-height: 20px;
      font-weight: normal;
    }

    :deep(.ed-date-editor) {
      .ed-input__wrapper {
        width: 100%;
      }
    }

    :deep(.ed-radio) {
      max-width: 100%;
    }

    :deep(.ed-checkbox-group) {
      max-width: 100%;
    }

    :deep(.ed-checkbox) {
      max-width: 100%;
    }

    :deep(.ed-checkbox__label) {
      font-weight: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    :deep(.ed-radio__label) {
      font-weight: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    :deep(.ed-form-item__error) {
      line-height: 14px;
      padding-top: 0;
    }
  }

  .df-ex-detail {
    flex: 1;
    width: 0;
    display: flex;
    flex-direction: column;

    .df-ex-row {
      flex: 1;
      display: flex;
      flex-direction: row;

      .df-ex-col {
        flex: 1;
        width: 0;
        display: flex;
        padding-right: 8px;

        .label-no-warp {
          white-space: nowrap;
          width: unset;
          overflow: hidden;
          text-overflow: ellipsis;
          min-width: unset !important;
          max-width: 50% !important;
          color: var(--ed-color-info);
          padding-right: 4px;
        }

        .desc-column {
          flex: 1;
          max-height: 64px;
          //white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
}
</style>
