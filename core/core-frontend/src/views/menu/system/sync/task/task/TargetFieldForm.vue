<script setup lang="ts">
import { useI18n } from "@/hooks/web/useI18n";
import { computed, reactive, ref } from "vue";
import { ITableField, ITaskInfoRes } from "@/api/sync/syncTask";
import type { FormInstance } from "element-plus-secondary";
import { ElMessage } from "element-plus-secondary";
import { deepCopy } from "@/utils/utils";
import { includes } from "lodash-es";

const { t } = useI18n();
const dialogVisible = ref(false);
const editId = ref("");
const fieldForm = ref<FormInstance>();
const props = withDefaults(
  defineProps<{
    modelValue: ITaskInfoRes;
  }>(),
  {
    modelValue: () => {
      return {} as ITaskInfoRes;
    }
  }
);
const emits = defineEmits(["update:modelValue"]);
const form = computed<ITaskInfoRes>({
  get() {
    return props.modelValue;
  },
  set(value) {
    emits("update:modelValue", value);
  }
});
const state = reactive({
  form: reactive({
    fieldSource: "",
    fieldSourceType: "",
    fieldSourceStandardType: "",
    fieldName: "",
    fieldType: "",
    fieldElementType: "",
    fieldMappingMessage: "",
    remarks: "",
    fieldPk: false,
    fieldIndex: false,
    id: "",
    fieldSize: 0,
    fieldPrecision: 0
  })
});

const spatialSourceTypes = new Set([
  "GEOMETRY",
  "GEOGRAPHY",
  "POINT",
  "LINESTRING",
  "POLYGON",
  "MULTIPOINT",
  "MULTILINESTRING",
  "MULTIPOLYGON",
  "GEOMETRYCOLLECTION",
  "GEO_POINT",
  "GEO_SHAPE"
]);

/**
 * 判断当前字段是否具有空间类型语义
 */
const isSpatialSourceField = computed(() => {
  const sourceType = (state.form.fieldSourceType || "")
    .split("(")[0]
    .trim()
    .replaceAll(" ", "_")
    .toUpperCase();
  return spatialSourceTypes.has(sourceType);
});

/**
 * 普通源字段不展示数组目标类型，避免误选后在同步阶段发生类型不匹配
 */
const availableTargetFieldTypeList = computed(() => {
  const typeList = form.value.source.targetFieldTypeList || [];
  if (isSpatialSourceField.value) {
    return typeList.filter((type) => ["UNKNOWN", "BYTEA", "TEXT"].includes(type));
  }
  if (state.form.fieldElementType) {
    return typeList;
  }
  return typeList.filter((type) => !type.endsWith("_ARRAY"));
});

/**
 * 获取当前字段在源数据库中的原始类型
 * 历史任务未保存原始类型时，从当前源字段列表补充展示
 */
const sourceFieldType = computed(() => {
  if (state.form.fieldSourceType) {
    return state.form.fieldSourceType;
  }
  const sourceField = form.value.source.fieldList?.find(
    item => item.fieldName === state.form.fieldSource
  );
  return sourceField?.fieldSourceType || "-";
});

const fieldMappingMessage = computed(() => {
  return state.form.fieldMappingMessage || "";
});

/**
 * 数组类型使用 PostgreSQL 原生写法展示，保存值仍使用稳定的内部标识
 */
const fieldTypeLabel = (type: string) => {
  if (!type.endsWith("_ARRAY")) {
    return type;
  }
  return `${type.replace(/_ARRAY$/, "").replace(/_/g, " ")}[]`;
};
const rules = reactive({
  fieldSource: {
    required: true,
    message: t("common.required"),
    trigger: "blur"
  },
  fieldName: [
    {
      required: true,
      message: t("common.required"),
      trigger: "change"
    },
    {
      max: 32,
      message: t("sync_task.input_limit", [32]),
      trigger: "change"
    }
  ],
  remarks: [
    {
      max: 255,
      message: t("sync_task.input_limit", [255]),
      trigger: "change"
    }
  ],
  fieldType: {
    required: true,
    message: t("common.required"),
    trigger: "blur"
  },
  fieldSize: {
    required: true,
    message: t("common.required"),
    trigger: "blur"
  },
  fieldPrecision: {
    required: true,
    message: t("common.required"),
    trigger: "blur"
  }
});
const showDialog = (row?: ITableField) => {
  dialogVisible.value = true;
  editId.value = row?.id || "";
  if (editId.value != "") {
    state.form = deepCopy(row);
  }
};
/**
 * 选择源字段事件
 * @param val
 */
const changeFieldSource = (val: string) => {
  const sourceFieldObj = form.value.source.fieldList
    ?.filter((item) => item.fieldName === val)
    .map((item) => item)[0];
  if (sourceFieldObj) {
    state.form = deepCopy(sourceFieldObj);
    if (editId.value != "") {
      state.form.id = editId.value;
    }
    state.form.fieldSource = val;
  }
};

const resetForm = () => {
  fieldForm.value?.resetFields();
  dialogVisible.value = false;
};
const saveForm = () => {
  if (fieldForm.value) {
    const repeat = form.value.target.fieldList
      .filter((item) => {
        // 新增时不校验ID
        if (editId.value === "") {
          return item.fieldName === state.form.fieldName;
        } else {
          return (
            item.id != editId.value && item.fieldName === state.form.fieldName
          );
        }
      })
      .map((item) => item)[0];
    const repeatSourceField = form.value.target.fieldList
      .filter((item) => {
        // 新增时不校验ID
        if (editId.value === "") {
          return item.fieldSource === state.form.fieldSource;
        } else {
          return (
            item.id != editId.value &&
            item.fieldSource === state.form.fieldSource
          );
        }
      })
      .map((item) => item)[0];
    if (/^[0-9]/.test(state.form.fieldName)) {
      ElMessage.warning(t("sync_task.cannot_begin_with_number"));
      return;
    }
    if (repeatSourceField) {
      ElMessage.warning(
        t("sync_task.duplicate_field_tip", [state.form.fieldSource])
      );
      return;
    }
    if (repeat) {
      ElMessage.warning(t("sync_task.duplicate_name_error", [state.form.fieldName]));
      return;
    }
    fieldForm.value.validate((valid) => {
      if (valid) {
        if (editId.value === "") {
          form.value.target.fieldList.push(deepCopy(state.form));
        } else {
          form.value.target.fieldList.forEach((item, index) => {
            if (item.id === editId.value) {
              form.value.target.fieldList[index] = deepCopy(state.form);
              if (!showFieldSize(state.form)) {
                form.value.target.fieldList[index].fieldSize = undefined;
              }
            }
          });
        }
        resetForm();
        dialogVisible.value = false;
      }
    });
  }
};

/**
 * 字符串不能设置长度
 * @param fieldType
 */
const showFieldSize = (field) => {
  if (field.fieldType?.endsWith("_ARRAY")) {
    return false;
  }
  switch (field.fieldType) {
    case "STRING":
    case "BOOLEAN":
    case "MAP":
    case "BITMAP":
    case "HLL":
    case "ARRAY":
    case "STRUCT":
    case "QUANTILE_STATE":
    case "JSON":
    case "JSONB":
    case "AGG_STATE":
    case "DATE":
    case "DATEV2":
    case "DATETIME":
    case "DATETIMEV2":
    case "FLOAT":
    case "DOUBLE":
      return false;
    default:
  }
  return true;
};

/**
 * 需要设置精度的字段类型
 * @param fieldType
 */
const showFieldPrecision = (fieldType: String) => {
  switch (fieldType) {
    case "DATETIME":
    case "DATETIMEV2":
    case "DECIMAL":
    case "DECIMALV3":
      return true;
  }
  return false;
};

/**
 * 支持索引的字段
 */
const isSupport = () => {
  return !includes(
    [
      "STRING",
      "TINYINT",
      "SMALLINT",
      "INT",
      "BIGINT",
      "CHAR",
      "VARCHAR",
      "DATE",
      "DATETIME",
      "DATEV2",
      "DATETIMEV2",
      "LARGEINT",
      "DECIMAL",
      "DECIMALV3",
      "BOOL"
    ],
    state.form.fieldType
  );
};
const targetFieldFormLoading = ref(false);
defineExpose({
  showDialog,
  editId,
  targetFieldFormLoading
});
</script>

<template>
  <el-drawer
    :title="editId === '' ? t('sync_task.add_field') : t('sync_task.edit_field')"
    v-model="dialogVisible"
    modal-class="target-field-drawer"
    size="600px"
    direction="rtl"
  >
    <template #default>
      <div class="target-body">
        <el-form
          ref="fieldForm"
          require-asterisk-position="right"
          :model="state.form"
          :rules="rules"
          label-width="80px"
          label-position="top"
          v-loading="targetFieldFormLoading"
        >
          <el-form-item :label="t('sync_task.source_field')" prop="fieldSource">
            <el-select
              v-model="state.form.fieldSource"
              :filterable="true"
              :placeholder="t('sync_task.please_choose')"
              @change="changeFieldSource"
            >
              <el-option
                v-for="item in form.source.fieldList"
                :key="item.fieldName"
                :label="item.fieldName"
                :value="item.fieldName"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('sync_task.source_field_type')">
            <el-input :model-value="sourceFieldType" readonly disabled />
          </el-form-item>
          <el-form-item :label="t('sync_task.name')" prop="fieldName">
            <el-input
              v-model="state.form.fieldName"
              :placeholder="t('sync_task.please_enter')"
            ></el-input>
          </el-form-item>
          <el-form-item :label="t('sync_task.field_type')" prop="fieldType">
            <el-alert
              v-if="fieldMappingMessage"
              :title="fieldMappingMessage"
              type="warning"
              :closable="false"
              show-icon
              class="field-mapping-alert"
            />
            <el-select
              v-model="state.form.fieldType"
              :filterable="true"
              :placeholder="t('sync_task.please_choose')"
            >
              <el-option
                v-for="(item, index) in availableTargetFieldTypeList"
                :key="index"
                :label="fieldTypeLabel(item)"
                :value="item"
              >
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item :label="t('sync_task.field_comment')" prop="remarks">
            <el-input
              v-model="state.form.remarks"
              :placeholder="t('sync_task.please_enter')"
            ></el-input>
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_length')"
            prop="fieldSize"
            v-if="showFieldSize(state.form)"
          >
            <el-input-number
              v-model="state.form.fieldSize"
              controls-position="right"
              autocomplete="off"
              type="number"
              @mousewheel.prevent
              :min="0"
              :max="100000000"
            />
          </el-form-item>
          <el-form-item
            :label="t('sync_task.field_precision')"
            prop="fieldPrecision"
            v-if="showFieldPrecision(state.form.fieldType)"
          >
            <el-input-number
              v-model="state.form.fieldPrecision"
              controls-position="right"
              autocomplete="off"
              type="number"
              @mousewheel.prevent
              :min="0"
              :max="20"
            />
          </el-form-item>
          <el-form-item prop="fieldPk">
            <el-checkbox v-model="state.form.fieldPk" :label="t('sync_task.field_key')" />
          </el-form-item>
          <el-form-item prop="fieldIndex">
            <el-checkbox
              v-model="state.form.fieldIndex"
              :label="t('sync_task.field_index')"
              :disabled="isSupport()"
            />
          </el-form-item>
        </el-form>
      </div>
    </template>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm"> {{ t("sync_datasource.cancel") }}</el-button>
        <el-button type="primary" @click="saveForm"> {{ t("sync_datasource.save") }} </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style lang="less">
.target-field-drawer {
  .ed-drawer__body {
    padding: 0 24px !important;

    .target-body {
      height: calc(100% - 64px) !important;
      overflow: auto;
      padding: 16px 0;

      .ed-checkbox.ed-checkbox--default {
        height: 20px;
      }

      .field-mapping-alert {
        margin-bottom: 12px;
      }
    }
  }
}
</style>
