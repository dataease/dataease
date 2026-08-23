<script setup lang="ts">
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {computed, nextTick, onMounted, reactive, Ref, ref, watch} from "vue";
import {ElIcon, ElMessage, ElMessageBox} from "element-plus-secondary";
import {
  getDatasourceListByTypeApi,
  getSinkKeyPolicyApi,
  ITableField,
  ITaskInfoRes
} from "@/api/sync/syncTask";
import type {SinkKeyPolicy} from "@/api/sync/syncTask";
import {includes, map, remove} from "lodash-es";
import FormTitle from "../component/FormTitle.vue";
import {getFieldListApi} from "@/api/sync/syncDatasource";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {Icon} from "@/components/icon-custom";
import {useI18n} from "@/hooks/web/useI18n";
import TargetFieldForm from "./TargetFieldForm.vue";
import {deepCopy} from "@/utils/utils";
import dvInfo from "@/assets/svg/dv-info.svg";
import DorisProperty from "./component/DorisProperty.vue";
import PluginComponent from "@/components/plugin/src/PluginComponent.vue";

const {t} = useI18n();

// region 基础数据
const formLoading = ref<boolean>(false);
const props = withDefaults(
    defineProps<{
      modelValue: ITaskInfoRes;
      dsTypeListData: Object;
      isEdit: boolean;
    }>(),
    {
      modelValue: () => {
        return {} as ITaskInfoRes;
      },
      isEdit: false
    }
);
const emits = defineEmits(["update:modelValue", "changeLoading"]);
const form = computed<ITaskInfoRes>({
  get() {
    return props.modelValue;
  },
  set(value) {
    emits("update:modelValue", value);
  }
});
const databaseList = ref(deepCopy(props.dsTypeListData))
const isPlugin = ref(false)
const sinkKeyPolicy = ref<SinkKeyPolicy>("OPTIONAL")
// 目标数据库自定义属性组件
const targetPropertyFormRef = ref();
// 编辑目标字段表单组件
const targetFieldFormRef = ref();

/**
 * 字段属性
 */
interface FieldAttribute {
  // 输入类型，input,select,checkbox等
  type: string;
  value?: string;
  label: string;
  model: string;
  disabled?: boolean;
  defaultValue?: string;
}

// 分页信息
const pageState = reactive({
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
});

/**
 * 支持增量抽取数据的字段类型
 */
const supportedFieldType = [
  "DATETIME",
  "DATE",
  "DATETIMEV2",
  "DATEV2",
  "INT",
  "BIGINT",
  "LARGEINT",
  "TINYINT",
  "SMALLINT",
  "DECIMAL",
  "DECIMALV3",
  "DOUBLE",
  "FLOAT"
];

const targetSupportedIncrementFieldType = ref([])
/**
 * 支持增量抽取数据的字段
 */
const incrementFieldList = computed(() => {
  const customTypes = (isPlugin.value
      ? targetSupportedIncrementFieldType.value
      : targetPropertyFormRef.value?.getSupportedIncrementFieldType()) || [];
  const allTypes = Array.from(new Set([...supportedFieldType, ...customTypes]));
  return form.value.target.fieldList?.filter(item => allTypes.includes(item.fieldType)) || [];
});

/**
 * 刷新字段列表
 */
const refreshFieldList = () => {
  emits("changeLoading", true);
  buildSearchSourceFieldParams.value.targetDbId = form.value.target.datasourceId;
  getFieldListApi(buildSearchSourceFieldParams.value)
      .then((res) => {
        // 如果源字段不一样，清空目标字段列表
        const newSourceFieldNameListStr = JSON.stringify(map(res.data.sourceFieldList, "fieldName"))
        const formSourceFieldNameListStr = JSON.stringify(map(form.value.source.fieldList, "fieldName"))
        if (newSourceFieldNameListStr !== formSourceFieldNameListStr) {
          form.value.source.fieldList = [];
          form.value.target.fieldList = [];
        }
        emits("changeLoading", false);
      }).catch((err) => {
    console.log(err);
    emits("changeLoading", false);
  })
};

watch(
    () => form.value.target.datasourceId,
    () => {
      form.value.source.fieldList = [];
      form.value.target.fieldList = [];
      void refreshSinkKeyPolicy();
    }
);

watch(
    () => form.value.target.incrementSync,
    () => void refreshSinkKeyPolicy()
);

onMounted(() => {
  if (!props.isEdit && !form.value.target.datasourceId) {
    form.value.target.type = "doris";
  }
  getDatasourceListByTypeApi(form.value.target.type).then((res) => {
    if (res) {
      form.value.target.dsList = res.data;
      if (!form.value.target.datasourceId) {
        form.value.target.datasourceId = res.data[0]?.id as string;
      }
      void refreshSinkKeyPolicy();
      targetPropertyFormRef.value?.initTargetCustomProperty();
    }
  }).catch((err) => {
    ElMessage.error(err.message);
  });
  if (!props.isEdit) {
    refreshFieldList()
  }
});
// endregion

//region 字段表格
/**
 * 构建查询源数据库字段的参数
 */
const buildSearchSourceFieldParams = computed(() => {
  const sourceObj = form.value.source;
  return {
    id: sourceObj.datasourceId,
    query: sourceObj.query,
    table: sourceObj.tables,
    tableExtract: sourceObj.tableExtract === "0",
    type: sourceObj.type,
    targetDbId: ""
  };
});
/**
 * 设置源数据字段以及目标数据库字段类型
 */
const setSourceFieldAndTargetFieldTypeList = (callBack) => {
  if (props.isEdit && form.value.editable === false) {
    if (callBack) callBack();
    return;
  }
  buildSearchSourceFieldParams.value.targetDbId = form.value.target.datasourceId;
  getFieldListApi(buildSearchSourceFieldParams.value)
      .then((res) => {
        emits("changeLoading", false);
        targetFieldFormRef.value.targetFieldFormLoading = false;
        form.value.source.fieldList = res.data.sourceFieldList;
        // 设置ID方便后面映射表格的删除修改
        form.value.source.fieldList?.forEach((item) => {
          item.id = Math.random().toString(16).slice(2);
        });
        form.value.target.fieldList?.forEach((item) => {
          item.id = Math.random().toString(16).slice(2);
        });
        // 目标数据库数据类型列表
        form.value.source.targetFieldTypeList = res.data.targetDbDataTypeList;
        if (callBack) callBack();
      })
      .catch((err) => {
        form.value.source.fieldList = [];
        form.value.target.fieldList = [];
        emits("changeLoading", false);
        targetFieldFormRef.value.targetFieldFormLoading = false;
        console.log(err);
        if (callBack) callBack();
      });
};

/**
 * 字段列表数据
 */
const targetFieldList = computed(() => {
  if (form.value.target.fieldList?.length > 0) {
    pageState.paginationConfig.total = deepCopy(form.value.target.fieldList.length);
    const pageNumber = pageState.paginationConfig.currentPage;
    const startIndex = (pageNumber - 1) * pageState.paginationConfig.pageSize;
    const endIndex = startIndex + pageState.paginationConfig.pageSize;
    // 如果当前页没数据，显示上一页的内容
    const currentPageNoData = form.value.target.fieldList.slice(startIndex, endIndex).length === 0
    if (pageState.paginationConfig.currentPage > 1 && currentPageNoData) {
      pageState.paginationConfig.currentPage--;
      const lastPageNumber = pageState.paginationConfig.currentPage;
      const lastStartIndex = (lastPageNumber - 1) * pageState.paginationConfig.pageSize;
      const lastEndIndex = lastStartIndex + pageState.paginationConfig.pageSize;
      return form.value.target.fieldList.slice(lastStartIndex, lastEndIndex);
    } else {
      return form.value.target.fieldList.slice(startIndex, endIndex);
    }
  }
  return [];
});

const isUnknownField = (field: ITableField) =>
  field.fieldType?.trim().toUpperCase() === "UNKNOWN";

const resolveFieldMappingMessage = (field: ITableField) => {
  if (field.fieldMappingMessage) {
    return field.fieldMappingMessage;
  }
  return isUnknownField(field)
    ? t("sync_task.field_mapping_unknown_detail")
    : "";
};

const mappingWarningFields = computed(() => {
  const targetFields = form.value.target.fieldList || [];
  return targetFields.length > 0
    ? targetFields
    : form.value.source.fieldList || [];
});

const groupFieldMappingWarnings = (fields: ITableField[]) => {
  const warningFields = new Map<string, string>();
  fields.forEach(field => {
    const message = resolveFieldMappingMessage(field);
    if (message) {
      warningFields.set(field.fieldName || field.fieldSource || "-", message);
    }
  });

  const warningGroups = new Map<string, string[]>();
  warningFields.forEach((message, fieldName) => {
    const fieldNames = warningGroups.get(message) || [];
    fieldNames.push(fieldName);
    warningGroups.set(message, fieldNames);
  });
  return Array.from(warningGroups.entries()).map(([message, fieldNames]) => ({
    message,
    fieldNames
  }));
};

/**
 * UNKNOWN 属于阻断项，必须选择目标数据库支持的类型后才能保存
 */
const blockingFieldMappingWarnings = computed(() =>
  groupFieldMappingWarnings(mappingWarningFields.value.filter(isUnknownField))
);

/**
 * 其余映射提示属于非阻断风险，用户确认后仍可保存
 */
const fieldMappingWarnings = computed(() =>
  groupFieldMappingWarnings(
    mappingWarningFields.value.filter(field => !isUnknownField(field))
  )
);

const countWarningFields = (warnings: { fieldNames: string[] }[]) =>
  warnings.reduce((count, warning) => count + warning.fieldNames.length, 0);

const blockingFieldMappingWarningCount = computed(() =>
  countWarningFields(blockingFieldMappingWarnings.value)
);

const fieldMappingWarningCount = computed(() =>
  countWarningFields(fieldMappingWarnings.value)
);

const MAPPING_WARNING_PREVIEW_LIMIT = 3;
const showAllBlockingWarnings = ref(false);
const showAllFieldMappingWarnings = ref(false);
const visibleBlockingFieldMappingWarnings = computed(() =>
  showAllBlockingWarnings.value
    ? blockingFieldMappingWarnings.value
    : blockingFieldMappingWarnings.value.slice(0, MAPPING_WARNING_PREVIEW_LIMIT)
);
const visibleFieldMappingWarnings = computed(() =>
  showAllFieldMappingWarnings.value
    ? fieldMappingWarnings.value
    : fieldMappingWarnings.value.slice(0, MAPPING_WARNING_PREVIEW_LIMIT)
);
const fieldMappingPanelRef = ref<HTMLElement>();

const scrollToFieldMappingWarnings = () => {
  nextTick(() => {
    fieldMappingPanelRef.value?.scrollIntoView({behavior: "smooth", block: "center"});
  });
};

const summarizeFieldNames = (fields: ITableField[]) => {
  const fieldNames = Array.from(new Set(
    fields.map(field => field.fieldName || field.fieldSource || "-")
  ));
  const previewNames = fieldNames.slice(0, 5).join(t("sync_task.field_name_separator"));
  if (fieldNames.length <= 5) {
    return previewNames;
  }
  return `${previewNames}${t("sync_task.field_name_more_suffix", [fieldNames.length - 5])}`;
};

const confirmFieldMappingWarnings = async (loading?: Ref<boolean>) => {
  if (!fieldMappingWarningCount.value) {
    return true;
  }
  try {
    await ElMessageBox.confirm(
      t("sync_task.field_mapping_warning_confirm_message", [fieldMappingWarningCount.value]),
      {
        title: t("sync_task.field_mapping_warning_confirm_title"),
        type: "warning",
        autofocus: false,
        showClose: false,
        confirmButtonText: t("sync_task.field_mapping_save_anyway"),
        cancelButtonText: t("sync_task.field_mapping_back_to_check")
      }
    );
    return true;
  } catch {
    formLoading.value = false;
    if (loading) {
      loading.value = false;
    }
    scrollToFieldMappingWarnings();
    return false;
  }
};

/**
 * 编辑目标字段信息
 * @param row
 */
const edit = (row) => {
  targetFieldFormRef.value.editId = row.id;
  const showTargetFieldFormRef = () => targetFieldFormRef.value.showDialog(row);
  if (!form.value.source.targetFieldTypeList?.length) {
    setSourceFieldAndTargetFieldTypeList(showTargetFieldFormRef);
  } else {
    showTargetFieldFormRef();
  }
};
/**
 * 删除字段
 * @param row
 */
const delMappingField = (row: ITableField) => {
  ElMessageBox.confirm(t("sync_task.msg_confirm_delete_field"), {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t("sync_task.delete"),
    cancelButtonText: t("sync_datasource.cancel"),
    autofocus: false,
    showClose: false
  })
      .then(() => {
        clearIncrementField(row)
        isPlugin.value ? targetPropertyFormRef.value?.invokeMethod({
              methodName: "delMappingFieldHandler",
              args: [row]
            }) :
            targetPropertyFormRef.value?.delMappingFieldHandler(row);
        form.value.target.fieldList = remove(form.value.target.fieldList, (v) => {
          return !includes(map([row], "fieldName"), v["fieldName"]);
        });
      })
      .catch(() => {
        return false;
      });
};

/**
 * 字段列表表头
 */
const tableFields = reactive<Array<FieldAttribute>>([
  {
    label: t("sync_task.source_field"),
    type: "select",
    model: "fieldSource"
  },
  {
    label: t("sync_task.name"),
    type: "input",
    model: "fieldName"
  },
  {
    label: t("sync_task.field_type"),
    type: "select",
    model: "fieldType"
  },
  {
    label: t("sync_task.field_length"),
    type: "number",
    model: "fieldSize"
  },
  {
    label: t("sync_task.field_precision"),
    type: "number",
    model: "fieldPrecision"
  },
  {
    label: t("sync_task.field_key"),
    type: "checkbox",
    model: "fieldPk"
  },
  {
    label: t("sync_task.field_index"),
    type: "checkbox",
    model: "fieldIndex"
  },
  {
    label: t("sync_task.field_comment"),
    type: "input",
    model: "remarks"
  }
]);

const handleSelectionChange = (rows: any) => {
  form.value.target.multipleSelection = rows;
};

const pageChange = (index: any) => {
  if (typeof index !== "number") {
    return;
  }
  pageState.paginationConfig.currentPage = index;
};

const sizeChange = (size) => {
  pageState.paginationConfig.pageSize = size;
};
const changeTargetType = () => {
  const currentDs = props.dsTypeListData?.find(item => item.type === form.value.target.type);
  isPlugin.value = !!currentDs && currentDs.isPlugin;
  getDatasourceListByTypeApi(form.value.target.type).then((res) => {
    if (res) {
      form.value.target.dsList = res.data?.filter(i => i.datasourceRole === 2);
      const id = form.value.target.dsList?.length?.[0]?.id || '';
      form.value.target.datasourceId = id as string;
      if (!isPlugin.value) {
        targetPropertyFormRef.value?.initTargetCustomProperty();
      }
    }
  }).catch((err) => {
    ElMessage.error(err.message);
  });
};
/**
 * 映射所有源库字段
 */
const mappingAllSourceDsField = () => {
  emits("changeLoading", true);
  setSourceFieldAndTargetFieldTypeList(() => {
    const existingSources = new Set(map(form.value.target.fieldList, "fieldSource"));
    form.value.source.fieldList?.forEach((field) => {
      if (!existingSources.has(field.fieldName)) {
        const targetField = {...deepCopy(field), fieldSource: field.fieldName};
        form.value.target.fieldList.push(targetField);
        existingSources.add(field.fieldName);
      }
    });
  });
};

/**
 * 添加字段
 */
const addMappingField = () => {
  targetFieldFormRef.value.targetFieldFormLoading = true;
  setSourceFieldAndTargetFieldTypeList(false);
  targetFieldFormRef.value.editId = "";
  targetFieldFormRef.value.showDialog();
};
/**
 * 清理增量字段
 * 删除字段时，判断是否被用作增量字段，如果被用，则清除掉，重新选择
 */
const clearIncrementField = (row?: any) => {
  if (row) {
    if (existField([row], "fieldName", form.value.target.incrementField)) {
      form.value.target.incrementField = "";
    }
  } else {
    if (existField(form.value.target.multipleSelection, "fieldName", form.value.target.incrementField)) {
      form.value.target.incrementField = "";
    }
  }
};

const existField = (row: any, fieldName: any, formFieldValue: any) => {
  return includes(map(row, fieldName), formFieldValue);
};
/**
 * 批量删除映射字段
 */
const batchDelMappingField = () => {
  const selectionSize = form.value.target.multipleSelection?.length;
  ElMessageBox.confirm(t("sync_task.confirm_delete_field", [selectionSize]), {
    confirmButtonType: "danger",
    type: "warning",
    confirmButtonText: t("sync_task.delete"),
    cancelButtonText: t("sync_datasource.cancel"),
    autofocus: false,
    showClose: false
  }).then(() => {
    clearIncrementField()
    isPlugin.value ? targetPropertyFormRef.value?.invokeMethod({
      methodName: "delMappingFieldHandler",
      args: [{}]
    }) : targetPropertyFormRef.value?.batchDelMappingFieldHandler();
    form.value.target.fieldList = form.value.target.fieldList.filter(
        v => !new Set(map(form.value.target.multipleSelection, "fieldSource")).has(v.fieldSource)
    );
  }).catch(() => {
    return false;
  });
};

/**
 * 验证字段列表输入
 * 字段名称不能为空
 * 字段类型不能为UNKNOWN
 */
const validateFieldList = () => {
  const list = form.value.target.fieldList || [];
  const unresolvedFields = list.filter(isUnknownField);
  if (unresolvedFields.length) {
    ElMessage.warning(t("sync_task.field_mapping_blocking_save_tip", [
      unresolvedFields.length,
      summarizeFieldNames(unresolvedFields)
    ]));
    formLoading.value = false;
    scrollToFieldMappingWarnings();
    return false;
  }
  const result = list.length > 0 && !list.some(field => !field.fieldName?.trim());
  if (!result) {
    ElMessage.warning(t("sync_task.msg_field_list_empty_tip"));
    formLoading.value = false;
  }
  return result;
}

const refreshSinkKeyPolicy = async (showError = false) => {
  if (!form.value.target.datasourceId) {
    sinkKeyPolicy.value = "OPTIONAL";
    return false;
  }
  try {
    const response = await getSinkKeyPolicyApi(form.value.target);
    sinkKeyPolicy.value = response.data || "OPTIONAL";
    return true;
  } catch (error: any) {
    sinkKeyPolicy.value = "OPTIONAL";
    if (showError) {
      ElMessage.error(error?.message || t("sync_task.key_policy_load_failed"));
    }
    return false;
  }
}

const validateSinkKeyPolicy = async () => {
  if (!(await refreshSinkKeyPolicy(true))) {
    return false;
  }
  if (sinkKeyPolicy.value !== "REQUIRED") {
    return true;
  }
  const hasKey = form.value.target.fieldList?.some(field => field.fieldPk);
  if (!hasKey) {
    ElMessage.warning(t("sync_task.key_required_by_target"));
    formLoading.value = false;
  }
  return !!hasKey;
}

function handleValidateFieldList(_, callback) {
  const result = validateFieldList();
  callback(result);
}

//endregion

// region 数据验证
const validate = async (loading?: Ref<boolean>) => {
  if (!validateFieldList()) {
    return false;
  }
  if (!targetPropertyFormRef.value) {
    return false;
  }
  let targetPropertyValid: boolean;
  if (isPlugin.value) {
    targetPropertyValid = await new Promise<boolean>((resolve) => {
      targetPropertyFormRef.value.invokeMethod({
        methodName: "targetPropertyValidate",
        args: [{callback: resolve}]
      });
    });
  } else {
    targetPropertyValid = await targetPropertyFormRef.value.targetPropertyValidate();
  }
  if (!targetPropertyValid) {
    return false;
  }
  if (!(await validateSinkKeyPolicy())) {
    return false;
  }
  return confirmFieldMappingWarnings(loading);
}
const closeLoading = () => {
  formLoading.value = false;
};

const displayText = (row, field) => {
  if (row[field.model] === 0) {
    return "-";
  }
  return row[field.model];
};
/**
 * 支持索引的字段
 */
const isSupport = (row, field) => {
  return field.model === "fieldIndex";
};

const changeTargetDs = () => {
  emits("changeLoading", true);
  setSourceFieldAndTargetFieldTypeList(false);
};
const incrementFieldRef = ref();
/**
 * 增量同步开关
 */
const changeIncrementSyncCheckbox = () => {
  if (form.value.target.incrementSync === "on" && !validateFieldList()) {
    form.value.target.incrementSync = "off";
  } else {
    incrementFieldRef.value?.focus();
  }
};
/**
 * 偏移量时间单位
 */
const incrementOffsetUnitList = ref<Array<option>>([
  {name: t("sync_task.millisecond"), value: "millisecond"},
  {name: t("sync_task.second"), value: "second"},
  {name: t("sync_task.minute"), value: "minute"},
  {name: t("sync_task.hour"), value: "hour"},
  {name: t("sync_task.day"), value: "day"},
  {name: t("sync_task.month"), value: "month"},
  {name: t("sync_task.year"), value: "year"}
]);

const incrementOffsetUnitListFilter = computed(() => {
  const dateTypes = ["DATE", "DATEV2"];
  const allowedUnits = ["year", "month", "day"];
  return dateTypes.includes(form.value.target.incrementFieldType)
      ? incrementOffsetUnitList.value.filter(item => allowedUnits.includes(item.value))
      : incrementOffsetUnitList.value;
});

const blurIncrementOffset = () => {
  if (form.value.target.incrementOffset == null) form.value.target.incrementOffset = 0;
};

/**
 * 增量字段数据类型
 */
const changeIncrementField = () => {
  const field = incrementFieldList.value.find(i => i.fieldName === form.value.target.incrementField);
  if (!field) return;
  const {fieldType} = field;
  form.value.target.incrementFieldType = fieldType;
  form.value.target.incrementOffset = 0;
  form.value.target.incrementOffsetUnit =
      fieldType?.startsWith("DATE")
          ? (fieldType === "DATE" || fieldType === "DATEV2" ? "day" : "millisecond")
          : "";
};
const pluginIndex = ref('')

const getPluginStatic = type => {
  const arr = databaseList.value.filter(ele => {
    return ele.type === type
  })
  return pluginIndex.value
      ? pluginIndex.value
      : arr && arr.length > 0
          ? arr[0].staticMap?.property
          : null
}
const pluginComponentLoadDone = () => {
  targetPropertyFormRef?.value?.invokeMethod({methodName: 'initTargetCustomProperty', args: [{}]})
  targetPropertyFormRef.value?.invokeMethod({
    methodName: "getSupportedIncrementFieldType", args: [{
      callback: (res) => {
        targetSupportedIncrementFieldType.value = res
      }
    }]
  })
}
//将方法暴露出去
defineExpose({validate, closeLoading});
// endregion

</script>
<template>
  <div class="target-body" v-loading="formLoading">
    <FormTitle>
      <template v-slot:title>
        <span> {{ t("sync_task.target_table_info") }}</span>
      </template>
    </FormTitle>
    <el-alert
      v-if="sinkKeyPolicy === 'REQUIRED'"
      type="warning"
      :closable="false"
      show-icon
      class="field-mapping-alert"
      :title="t('sync_task.key_required_by_target')"
    />
    <el-row :gutter="24">
      <el-col :span="6">
        <el-form-item :label="t('sync_task.target_database_type')" prop="target.type">
          <el-select
              v-model="form.target.type"
              :placeholder="t('sync_task.please_choose_database_type')"
              @change="changeTargetType"
          >
            <el-option
                v-for="item in databaseList"
                :key="item.type"
                :label="item.name"
                :value="item.type"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item :label="t('sync_task.database')" prop="target.datasourceId">
          <el-select
              v-model="form.target.datasourceId"
              :filterable="true"
              @change="changeTargetDs"
              :placeholder="t('sync_task.please_choose_database')"
          >
            <el-option
                v-for="item in form.target.dsList"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item :label="t('sync_task.table')+t('sync_task.name')" prop="target.tableName">
          <el-input :disabled="form.editable" v-model="form.target.tableName"
                    :placeholder="t('sync_task.please_enter')"/>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item :label="t('data_set.table_remarks')" prop="target.remarks">
          <el-input v-model="form.target.remarks" :placeholder="t('data_set.table_remarks')"/>
        </el-form-item>
      </el-col>
    </el-row>
    <FormTitle>
      <template v-slot:title>
        <span> {{ t("sync_task.define_mapping_field") }}</span>
      </template>
      <template v-slot:button>
        <el-button
            style="margin-left: 12px"
            @click="batchDelMappingField"
            :disabled="
            !form.target.multipleSelection ||
            form.target.multipleSelection?.length <= 0
          "
        >
          {{ t("sync_task.delete_field") }}
        </el-button>
        <el-button @click="addMappingField">{{ t("sync_task.add_field") }}</el-button>
        <el-button @click="mappingAllSourceDsField" type="primary"
        >{{ t("sync_task.add_all_field") }}
        </el-button
        >
      </template>
    </FormTitle>
    <el-row :gutter="24">
      <el-col style="height: calc(100% - 500px)">
        <div ref="fieldMappingPanelRef">
          <el-alert
            v-if="blockingFieldMappingWarnings.length"
            type="error"
            :closable="false"
            show-icon
            class="field-mapping-alert"
          >
            <template #title>
              {{ t("sync_task.field_mapping_blocking_title", [blockingFieldMappingWarningCount]) }}
            </template>
            <div class="field-mapping-warning-list">
              <div
                v-for="warning in visibleBlockingFieldMappingWarnings"
                :key="warning.message"
                class="field-mapping-warning-item"
              >
                {{ t("sync_task.field_mapping_warning_item", [
                  warning.fieldNames.join(t("sync_task.field_name_separator")),
                  warning.message
                ]) }}
              </div>
              <el-button
                v-if="blockingFieldMappingWarnings.length > MAPPING_WARNING_PREVIEW_LIMIT"
                link
                type="danger"
                class="field-mapping-warning-toggle"
                @click="showAllBlockingWarnings = !showAllBlockingWarnings"
              >
                {{ showAllBlockingWarnings
                  ? t("sync_task.field_mapping_collapse")
                  : t("sync_task.field_mapping_expand_all", [blockingFieldMappingWarningCount]) }}
              </el-button>
            </div>
          </el-alert>
          <el-alert
            v-if="fieldMappingWarnings.length"
            type="warning"
            :closable="false"
            show-icon
            class="field-mapping-alert"
          >
            <template #title>
              {{ t("sync_task.field_mapping_warning_title", [fieldMappingWarningCount]) }}
            </template>
            <div class="field-mapping-warning-list">
              <div
                v-for="warning in visibleFieldMappingWarnings"
                :key="warning.message"
                class="field-mapping-warning-item"
              >
                {{ t("sync_task.field_mapping_warning_item", [
                  warning.fieldNames.join(t("sync_task.field_name_separator")),
                  warning.message
                ]) }}
              </div>
              <el-button
                v-if="fieldMappingWarnings.length > MAPPING_WARNING_PREVIEW_LIMIT"
                link
                type="warning"
                class="field-mapping-warning-toggle"
                @click="showAllFieldMappingWarnings = !showAllFieldMappingWarnings"
              >
                {{ showAllFieldMappingWarnings
                  ? t("sync_task.field_mapping_collapse")
                  : t("sync_task.field_mapping_expand_all", [fieldMappingWarningCount]) }}
              </el-button>
            </div>
          </el-alert>
        </div>
        <div class="field-table-body">
          <GridTable
              ref="multipleTableRef"
              :table-data="targetFieldList"
              class="popper-max-width"
              @selection-change="handleSelectionChange"
              @current-change="pageChange"
              @size-change="sizeChange"
              :pagination="pageState.paginationConfig"
              :showPagination="pageState.paginationConfig.total > 10"
          >
            <el-table-column
                type="selection"
                width="30"
            />
            <el-table-column
                v-for="(field, index) in tableFields"
                :key="index"
                :label="field.label"
                :prop="field.model"
            >
              <template #default="scope">
                <div v-if="field.type === 'checkbox'">
                  <el-checkbox
                      v-model="scope.row[field.model]"
                      :disabled="isSupport(scope.row, field)"
                  ></el-checkbox>
                </div>
                <div v-else>
                  {{ displayText(scope.row, field) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column
                width="80"
                fixed="right"
                key="_operation"
                :label="$t('common.operate')"
            >
              <template #default="scope">
                <el-tooltip
                    effect="dark"
                    :content="t('common.edit')"
                    placement="top"
                >
                  <el-button text @click="edit(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined"
                      >
                        <icon_edit_outlined class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                    effect="dark"
                    :content="t('common.delete')"
                    placement="top"
                >
                  <el-button
                      text
                      @click="delMappingField(scope.row)"
                      class="del-button"
                  >
                    <template #icon>
                      <Icon name="icon_delete-trash_outlined"
                      >
                        <icon_deleteTrash_outlined class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </template>
            </el-table-column>
          </GridTable>
        </div>
      </el-col>
    </el-row>
    <div class="increment-info-body">
      <el-row :gutter="24">
        <el-col :span="6">
          <el-form-item>
            <el-checkbox
                v-model="form.target.incrementSync"
                :label="t('sync_task.incremental_sync')"
                true-label="on"
                false-label="off"
                @change="changeIncrementSyncCheckbox"
            />
            <span class="item-label-class">
              <el-tooltip class="item" effect="dark" placement="right-start">
                <template #content>
                  <p>{{ t("sync_task.incremental_sync_tip_1") }}</p>
                  <p>
                    {{ t("sync_task.incremental_sync_tip_2") }}
                  </p>
                </template>
                <el-icon>
                  <Icon name="dv-info"><dvInfo class="svg-icon"/></Icon>
                </el-icon>
              </el-tooltip>
            </span>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="form.target.incrementSync === 'on'" :gutter="24">
        <el-col :span="6">
          <el-form-item :label="t('sync_task.incremental_field')" prop="target.incrementField">
            <el-select
                ref="incrementFieldRef"
                v-model="form.target.incrementField"
                :placeholder="t('common.selectText')"
                @change="changeIncrementField()"
            >
              <el-option
                  v-for="v of incrementFieldList"
                  :key="v.fieldName"
                  :label="v.fieldName"
                  :value="v.fieldName"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item prop="target.incrementOffset">
            <template #label>
              <span class="item-label-class">
                <span>{{ t("sync_task.offset") }}</span>
              <el-tooltip class="item" effect="dark" placement="right-start">
                <template #content>
                  <p>{{ t("sync_task.offset_tip") }}</p>
                </template>
                <el-icon>
                  <Icon name="dv-info">
                    <dvInfo class="svg-icon"/>
                  </Icon>
                </el-icon>
              </el-tooltip>
            </span>
            </template>
            <el-input-number
                v-model="form.target.incrementOffset"
                controls-position="right"
                autocomplete="off"
                type="number"
                @mousewheel.prevent
                @blur="blurIncrementOffset"
            />
          </el-form-item>
        </el-col>
        <el-col :span="6" v-if="form.target.incrementFieldType?.startsWith('DATE')">
          <el-form-item :label="t('sync_task.units')" prop="target.incrementOffsetUnit">
            <el-select
                v-model="form.target.incrementOffsetUnit"
                :placeholder="t('common.selectText')"
            >
              <el-option
                  v-for="v of incrementOffsetUnitListFilter"
                  :key="v.value"
                  :label="v.name"
                  :value="v.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <doris-property v-if="form.target.type==='doris' && !isPlugin" :ds-type-list-data="dsTypeListData"
                    ref="targetPropertyFormRef"
                    :model-value="form" :is-edit="isEdit" @validateFieldList="handleValidateFieldList">
    </doris-property>
    <plugin-component
        :jsname="getPluginStatic(form.target.type)"
        ref="targetPropertyFormRef"
        :model-value="form"
        :is-edit="isEdit"
        :ds-type-list-data="dsTypeListData"
        @validateFieldList="handleValidateFieldList"
        @pluginComponentLoadDone="pluginComponentLoadDone"
        v-if="isPlugin">
    </plugin-component>
  </div>
  <TargetFieldForm
      ref="targetFieldFormRef"
      :model-value="form"
  ></TargetFieldForm>
</template>
<style scoped lang="less">
.target-body {
  padding: 0 24px 0 24px;

  .field-table-body {
    margin-bottom: 16px;

    .del-button {
      margin: 0 4px;
    }
  }

  .field-mapping-alert {
    margin-bottom: 12px;

    :deep(.el-alert__content) {
      width: 100%;
      min-width: 0;
    }
  }

  .field-mapping-warning-list {
    max-height: 160px;
    overflow-y: auto;
    line-height: 20px;
  }

  .field-mapping-warning-item {
    overflow-wrap: anywhere;
    white-space: normal;
  }

  .field-mapping-warning-item + .field-mapping-warning-item {
    margin-top: 4px;
  }

  .field-mapping-warning-toggle {
    height: auto;
    margin-top: 6px;
    padding: 0;
  }

  .increment-info-body {
  }

  .partition-body {
    .number-input-range {
      display: flex;
      width: 100%;

      .start {
        width: calc(100% - 100px) !important;
      }

      .span {
        line-height: 32px;
        padding: 0 8px 0 8px;
        margin-top: 28px;
      }

      .end {
        width: calc(100% - 100px) !important;
        margin-top: 28px;
      }
    }
  }
}
</style>
