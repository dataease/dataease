<script setup lang="ts">
import {ITaskInfoRes} from "@/api/sync/syncTask";
import {computed, reactive, ref} from "vue";
import zhCn from "element-plus-secondary/es/locale/lang/zh-cn";
import {ElConfigProvider, ElIcon} from "element-plus-secondary";
import {Icon} from "@/components/icon-custom";
import {useI18n} from "@/hooks/web/useI18n";
import {includes, intersection, map, uniq} from "lodash-es";

const {t} = useI18n();
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
const emits = defineEmits(["update:modelValue", "changeLoading", "validateFieldList"]);
const form = computed<ITaskInfoRes>({
  get() {
    return props.modelValue;
  },
  set(value) {
    emits("update:modelValue", value);
  }
});
const dsForm = ref();
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

const getSupportedIncrementFieldType = () => {
  return supportedFieldType
}
/**
 * 初始化目标端自定义属性
 */
const initTargetCustomProperty = () => {
  if (form.value.target.targetProperty) {
    form.value.target.property = JSON.parse(form.value.target.targetProperty);
    // 兼容旧版本容错率配置
    if (form.value.target.faultToleranceRate > 0 && !form.value.target.property.faultToleranceRate) {
      form.value.target.property.faultToleranceRate = form.value.target.faultToleranceRate;
    }
    if (form.value.target.property.manualPartitionTimeRange != null) {
      form.value.target.property.manualPartitionTimeRange = JSON.parse(
          form.value.target.property.manualPartitionTimeRange
      );
    }
  } else {
    form.value.target.property.faultToleranceRate = 0;
    form.value.target.property.partitionEnable = "off";
    form.value.target.property.partitionType = "";
    form.value.target.property.partitionColumn = "";
    form.value.target.property.dynamicPartitionEnable = "";
  }
}

/**
 * 通用选项接口
 */
interface option {
  name: string;
  value: string;
  disabled?: boolean;
}

/**
 * List分区支持的数据类型
 * @type {string[]}
 */
let listPartitionDataType: string[] = [
  "BOOLEAN",
  "TINYINT",
  "SMALLINT",
  "INT",
  "BIGINT",
  "LARGEINT",
  "DATE",
  "DATETIME",
  "DATEV2",
  "DATETIMEV2",
  "CHAR",
  "VARCHAR"
];
/**
 * Range分区日期支持数据类型
 * @type {string[]}
 */
let dateRangePartitionDataType: string[] = [
  "DATE",
  "DATETIME",
  "DATEV2",
  "DATETIMEV2"
];
/**
 * Range分区数值支持数据类型
 * @type {string[]}
 */
let numberRangePartitionDataType: string[] = [
  "TINYINT",
  "SMALLINT",
  "INT",
  "BIGINT",
  "LARGEINT"
];

/**
 * 日期类型分区，时间单位
 */
const partitionTimeUnitList = ref<Array<option>>([
  {name: t("sync_task.hour"), value: "HOUR"},
  {name: t("sync_task.day"), value: "DAY"},
  {name: t("sync_task.week"), value: "WEEK"},
  {name: t("sync_task.month"), value: "MONTH"},
  {name: t("sync_task.year"), value: "YEAR"}
]);

/**
 * 便捷时间间隔
 */
const shortcuts = [
  {
    text: t("sync_task.next_week"),
    value: () => {
      const end = new Date();
      const start = new Date();
      end.setTime(start.getTime() + 3600 * 1000 * 24 * 7);
      return [start, end];
    }
  },
  {
    text: t("sync_task.next_month"),
    value: () => {
      const end = new Date();
      const start = new Date();
      end.setTime(start.getTime() + 3600 * 1000 * 24 * 30);
      return [start, end];
    }
  },
  {
    text: t("sync_task.next_three_month"),
    value: () => {
      const end = new Date();
      const start = new Date();
      end.setTime(start.getTime() + 3600 * 1000 * 24 * 90);
      return [start, end];
    }
  }
];
const partitionTypeSelectRef = ref()

function parentValidateFieldList(): Promise<boolean> {
  return new Promise((resolve) => {
    emits('validateFieldList', null, (result: boolean) => {
      resolve(result);
    });
  });
}

/**
 * 分区开关
 */
const changePartitionCheckbox = async () => {
  if (form.value.target.property.partitionEnable === "on") {
    const result = await parentValidateFieldList();
    if (!result) {
      form.value.target.property.partitionEnable = "off";
      return
    }
    partitionTypeSelectRef.value?.focus()
    // 选中分区key
    updatePartitionColumnKeyChecked(
        form.value.target.property.partitionColumn,
        true
    );
  } else {
    // 取消分区key
    updatePartitionColumnKeyChecked(
        form.value.target.property.partitionColumn,
        false
    );
  }
};

/**
 * 监听字段表格数据
 */
const changePartitionEnable = () => {
  if (form.value.target.fieldList.length === 0) {
    form.value.target.property.partitionEnable = "off";
    form.value.target.incrementSync = "off";
  }
};
/**
 * 禁用分区类型
 * @param type
 * @param list
 */
const disabledPartitionType = (type: string, list: Array<option>) => {
  list.forEach((v: any) => {
    if (v.value === type) {
      v.disabled = true;
    }
  });
};
/**
 * 分区类型
 */
const partitionTypeList = computed(() => {
  let partitionTypeList: Array<option> = [
    {
      name: t("sync_task.date"),
      value: "DateRange",
      disabled: false
    },
    {
      name: t("sync_task.number"),
      value: "NumberRange",
      disabled: false
    },
    {
      name: t("sync_task.list"),
      value: "List",
      disabled: false
    }
  ];
  if (form.value.target.fieldList && form.value.target.fieldList.length > 0) {
    let fieldTypeList = map(form.value.target.fieldList, (v: any) => {
      return v["fieldType"];
    });
    if (intersection(fieldTypeList, listPartitionDataType).length === 0) {
      disabledPartitionType("List", partitionTypeList);
    }
    if (intersection(fieldTypeList, dateRangePartitionDataType).length === 0) {
      disabledPartitionType("DateRange", partitionTypeList);
    }
    if (
        intersection(fieldTypeList, numberRangePartitionDataType).length === 0
    ) {
      disabledPartitionType("NumberRange", partitionTypeList);
    }
  } else {
    disabledPartitionType("List", partitionTypeList);
    disabledPartitionType("DateRange", partitionTypeList);
    disabledPartitionType("NumberRange", partitionTypeList);
  }
  return partitionTypeList;
});

/**
 * 根据分区类型，获取分区字段
 */
const partitionColumnList = computed(() => {
  let columnList: Array<string> = [];
  if (form.value.target.fieldList && form.value.target.fieldList.length > 0) {
    for (let i = 0; i < form.value.target.fieldList.length; i++) {
      const fieldObj = form.value.target.fieldList[i];
      if (
          "List" === form.value.target.property.partitionType &&
          includes(listPartitionDataType, fieldObj.fieldType)
      ) {
        columnList.push(fieldObj.fieldName);
      }
      if (
          "DateRange" === form.value.target.property.partitionType &&
          includes(dateRangePartitionDataType, fieldObj.fieldType)
      ) {
        columnList.push(fieldObj.fieldName);
      }
      if (
          "NumberRange" === form.value.target.property.partitionType &&
          includes(numberRangePartitionDataType, fieldObj.fieldType)
      ) {
        columnList.push(fieldObj.fieldName);
      }
    }
  }
  return uniq(columnList);
});

const changePartitionType = () => {
  form.value.target.property.partitionColumn = partitionColumnList.value?.[0] ?? "";
  changePartitionColumn(form.value.target.property.partitionColumn);
  form.value.target.property.dynamicPartitionEnable = "off";
};

let lastSelectedPartitionColumn = "";
const updatePartitionColumnKeyChecked = (val, value) => {
  const column = form.value.target.fieldList.filter((item) => {
    return val === item.fieldName;
  });
  if (column.length > 0) {
    column[0].fieldPk = value;
  }
};
const changePartitionColumn = (val) => {
  updatePartitionColumnKeyChecked(lastSelectedPartitionColumn, false);
  updatePartitionColumnKeyChecked(val, true);
  lastSelectedPartitionColumn = val;
};

const filterPartitionTimeUnitList = computed(() => {
  const column = form.value.target.fieldList.filter((item) => {
    return form.value.target.property.partitionColumn === item.fieldName;
  });
  if (column.length > 0) {
    return partitionTimeUnitList.value.filter((item) => {
      if (column[0].fieldType === "DATE" || column[0].fieldType === "DATEV2") {
        if (form.value.target.property.manualPartitionTimeUnit === "HOUR") {
          form.value.target.property.manualPartitionTimeUnit = "";
        }
        return item.value !== "HOUR";
      }
      return true;
    });
  }
});

const targetPropertyValidate = (params) => {
  dsForm.value.validate((valid: boolean) => {
    if (valid) {
      const property = form.value.target.property;
      if (property) {
        const copied = {...property};
        if (copied.manualPartitionTimeRange != null) {
          copied.manualPartitionTimeRange = JSON.stringify(copied.manualPartitionTimeRange);
        }
        form.value.target.targetProperty = JSON.stringify(copied);
      }
    }
    params?.callback?.(valid)
  });
}


const rule = reactive({
  target: {
    property: {
      partitionType: [
        {
          required: true,
          message: t("sync_task.please_choose_partition_type"),
          trigger: "blur",
        },
      ],
      dynamicPartitionEnd: [
        {
          required: true,
          message: t("sync_task.please_enter_end_offset"),
          trigger: "blur",
        },
      ],
      dynamicPartitionTimeUnit: [
        {
          required: true,
          message: t("sync_task.please_choose_partition_interval_unit"),
          trigger: "blur",
        },
      ],
      manualPartitionColumnValue: [
        {
          required: true,
          message: t("sync_task.please_enter_partition_column_value"),
          trigger: "change",
        },
        {
          max: 4096,
          message: t("sync_task.input_limit_4096"),
          trigger: "change",
        },
      ],
      manualPartitionStart: [
        {
          required: true,
          message: t("sync_task.please_enter_starting_value"),
          trigger: "blur",
        },
      ],
      manualPartitionEnd: [
        {
          required: true,
          message: t("sync_task.please_enter_end_value"),
          trigger: "blur",
        },
      ],
      manualPartitionInterval: [
        {
          required: true,
          message: t("sync_task.please_enter_numerical_range_interval"),
          trigger: "blur",
        },
      ],
      manualPartitionTimeRange: [
        {
          required: true,
          message: t("sync_task.please_choose_time_range"),
          trigger: "blur",
        },
      ],
      manualPartitionTimeUnit: [
        {
          required: true,
          message: t("sync_task.please_choose_partition_interval_unit"),
          trigger: "blur",
        },
      ],
      partitionColumn: [
        {
          required: true,
          message: t("sync_task.please_choose_incremental_field"),
          trigger: "blur",
        },
      ],
    }
  }
})
/**
 * 判断字段值是否存在
 * @param row
 * @param fieldName
 * @param formFieldValue
 */
const existField = (row: any, fieldName: any, formFieldValue: any) => {
  return includes(map(row, fieldName), formFieldValue);
};
const delMappingFieldHandler = (row) => {
  if (existField([row], "fieldName", form.value.target.property.partitionColumn)) {
    form.value.target.property.partitionColumn = "";
  }
}

const batchDelMappingFieldHandler = () => {
  if (existField(form.value.target.multipleSelection, "fieldName", form.value.target.property.partitionColumn)) {
    form.value.target.property.partitionColumn = "";
  }
}

defineExpose({
  getSupportedIncrementFieldType,
  initTargetCustomProperty,
  targetPropertyValidate,
  delMappingFieldHandler,
  batchDelMappingFieldHandler
})
// endregion
</script>

<template>
  <el-form
      ref="dsForm"
      :model="form"
      :rules="rule"
      label-width="180px"
      label-position="top"
      require-asterisk-position="right"
      :disabled="isEdit && form.editable === false"
  >
    <div class="fault-tolerance-rate">
      <el-row :gutter="24">
        <el-col :span="6">
          <el-form-item prop="property.faultToleranceRate">
            <template #label>
            <span class="item-label-class">
              <span>{{ t("sync_task.fault_tolerance_rate") }}</span>
              <el-tooltip class="item" effect="dark" placement="right-start">
                <template #content>
                  <p>
                    {{ t("sync_task.fault_tolerance_rate_tip") }}
                  </p>
                </template>
                <el-icon>
                  <Icon name="dv-info"><dvInfo class="svg-icon"/></Icon>
                </el-icon>
              </el-tooltip>
            </span>
            </template>
            <el-input-number
                v-model="form.target.property.faultToleranceRate"
                controls-position="right"
                autocomplete="off"
                type="number"
                @mousewheel.prevent
                :value-on-clear="0"
                :step="0.1"
                :min="0"
                :max="1"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
    <div class="partition-info-body">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item>
            <el-checkbox
                v-model="form.target.property.partitionEnable"
                :label="t('sync_task.enable_partition')"
                true-label="on"
                false-label="off"
                @change="changePartitionCheckbox"
            />
            <span class="item-label-class">
            <el-tooltip class="item" effect="dark" placement="right-start">
              <template #content>
                <p>{{ t("sync_task.enable_partition_tip") }}</p>
              </template>
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon"/></Icon>
              </el-icon>
            </el-tooltip>
          </span>
          </el-form-item>
        </el-col>
      </el-row>
      <div v-if="form.target.property.partitionEnable === 'on'" class="partition-body">
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item :label="t('sync_task.partition_type')" prop="target.property.partitionType">
              <el-select
                  ref="partitionTypeSelectRef"
                  v-model="form.target.property.partitionType"
                  @change="changePartitionType"
                  :placeholder="t('common.selectText')"
              >
                <el-option
                    v-for="v of partitionTypeList"
                    :key="v.value"
                    :label="v.name"
                    :value="v.value"
                    :disabled="v.disabled"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item :label="t('sync_task.partition_field')" prop="target.property.partitionColumn">
              <el-select
                  v-model="form.target.property.partitionColumn"
                  :filterable="true"
                  @change="changePartitionColumn"
                  :placeholder="t('common.selectText')"
              >
                <el-option
                    v-for="v of partitionColumnList"
                    :key="v"
                    :label="v"
                    :value="v"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="form.target.property.partitionType === 'DateRange'" :span="12">
            <el-row :gutter="24">
              <el-col :span="6">
                <el-form-item
                    :label="t('sync_task.dynamic_partition_enable')"
                    prop="target.property.dynamicPartitionEnable"
                >
                  <el-select v-model="form.target.property.dynamicPartitionEnable">
                    <el-option value="on" :label="t('sync_task.on')"/>
                    <el-option value="off" :label="t('sync_task.off')"/>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col v-if="form.target.property.dynamicPartitionEnable === 'on'" :span="12">
                <el-form-item
                    :label="t('sync_task.end_offset')"
                    prop="target.property.dynamicPartitionEnd"
                >
                  <el-input-number
                      v-model="form.target.property.dynamicPartitionEnd"
                      controls-position="right"
                      autocomplete="off"
                      type="number"
                      @mousewheel.prevent
                      :min="1"
                      :max="999999999"
                  />
                </el-form-item>
              </el-col>
              <el-col v-if="form.target.property.dynamicPartitionEnable === 'on'" :span="6">
                <el-form-item
                    :label="t('sync_task.partition_interval_unit')"
                    prop="target.property.dynamicPartitionTimeUnit"
                >
                  <el-select
                      v-model="form.target.property.dynamicPartitionTimeUnit"
                  >
                    <el-option
                        v-for="item in filterPartitionTimeUnitList"
                        :label="item.name"
                        :key="item.value"
                        :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col v-if="form.target.property.dynamicPartitionEnable === 'off'" :span="12">
                <el-form-item
                    :label="t('common.component.dateRange')"
                    prop="target.property.manualPartitionTimeRange"
                >
                  <el-config-provider :locale="zhCn">
                    <el-date-picker
                        v-model="form.target.property.manualPartitionTimeRange"
                        type="daterange"
                        :range-separator="t('sync_task.picker_to')"
                        :start-placeholder="t('sync_task.picker_start')"
                        :end-placeholder="t('sync_task.time_end')"
                        :shortcuts="shortcuts"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        unlink-panels
                        size="default"
                        style="height: 32px"
                    />
                  </el-config-provider>
                </el-form-item>
              </el-col>
              <el-col v-if="form.target.property.dynamicPartitionEnable === 'off'" :span="6">
                <el-form-item
                    :label="t('sync_task.partition_interval_unit')"
                    prop="target.property.manualPartitionTimeUnit"
                >
                  <el-select v-model="form.target.property.manualPartitionTimeUnit">
                    <el-option
                        v-for="item in filterPartitionTimeUnitList"
                        :label="item.name"
                        :key="item.value"
                        :value="item.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-col>
          <el-col
              :span="6"
              style="min-width: 220px"
              v-if="form.target.property.partitionType === 'NumberRange'"
          >
            <div class="number-input-range">
              <div class="start">
                <el-form-item
                    :label="t('sync_task.number_range')"
                    prop="target.property.manualPartitionStart"
                >
                  <el-input-number
                      v-model="form.target.property.manualPartitionStart"
                      controls-position="right"
                      autocomplete="off"
                      type="number"
                      @mousewheel.prevent
                      :min="-999999999"
                      :max="999999999"
                  />
                </el-form-item>
              </div>
              <div class="span">
                <span>-</span>
              </div>
              <div class="end">
                <el-form-item prop="target.property.manualPartitionEnd">
                  <el-input-number
                      v-model="form.target.property.manualPartitionEnd"
                      controls-position="right"
                      autocomplete="off"
                      type="number"
                      @mousewheel.prevent
                      :min="-999999999"
                      :max="999999999"
                  />
                </el-form-item>
              </div>
            </div>
          </el-col>
          <el-col
              :span="6"
              v-if="form.target.property.partitionType === 'NumberRange'"
          >
            <el-form-item
                prop="target.property.manualPartitionInterval"
                :label="t('sync_task.partition_interval')"
            >
              <el-input-number
                  v-model="form.target.property.manualPartitionInterval"
                  controls-position="right"
                  autocomplete="off"
                  type="number"
                  @mousewheel.prevent
                  :min="-999999999"
                  :max="999999999"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6" v-if="form.target.property.partitionType === 'List'">
            <el-form-item
                prop="target.property.manualPartitionColumnValue"
                :label="t('sync_task.partition_column_value')"
            >
              <el-input
                  v-model="form.target.property.manualPartitionColumnValue"
                  :placeholder="t('sync_task.partition_column_value_placeholder')"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </div>
  </el-form>
</template>

<style scoped lang="less">
.partition-info-body {
  padding-bottom: 20px;
}
</style>
