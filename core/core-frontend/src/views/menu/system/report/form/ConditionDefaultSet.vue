<template>
  <el-drawer
    v-model="defaultDrawerShow"
    append-to-body
    modal-class="condition-drawer-fullscreen"
    direction="rtl"
    destroy-on-close
    size="600"
    style="height: 100% !important"
  >
    <template #header>
      <div @click="contentClick">
        <h4>{{ t("report.filter.title") }}</h4>
      </div>
    </template>
    <template #default>
      <div class="table-container-condition" @click="contentClick">
        <el-table
          ref="table"
          header-cell-class-name="header-cell"
          :data="filterInfo.defaultFilter"
          @row-click="contentClick"
          @header-click="contentClick"
        >
          <el-table-column
            :label="t('v_query.query_condition')"
            prop="name"
            show-overflow-tooltip
          >
            <template v-slot:default="scope">
              <span>{{ scope.row.name }}</span>
            </template>
          </el-table-column>

          <el-table-column
            :label="`${t('chart.fix')}/${t('chart.dynamic')}`"
            prop="value"
            show-overflow-tooltip
            width="320"
          >
            <template v-slot:default="scope">
              <div v-html="getValueHtml(scope.row)"></div>
            </template>
          </el-table-column>

          <el-table-column
            :label="t('common.operating')"
            fixed="right"
            width="100"
          >
            <template #default="scope">
              <condition-set-popover
                v-if="conditionPopoverShow"
                :cur-component="scope.row"
              ></condition-set-popover>
              <el-button @click="resetCondition(scope.$index)" text>
                <template #icon>
                  <Icon name="icon_reset_outlined"
                    ><icon_reset_outlined class="svg-icon"
                  /></Icon>
                </template>
              </el-button>
            </template>
          </el-table-column>

          <template #empty>
            <empty-background
              :description="t('data_set.no_data')"
              :img-type="'noneWhite'"
            />
          </template>
        </el-table>
      </div>
    </template>
    <template #footer>
      <div style="flex: auto" @click="contentClick">
        <el-button @click="cancel">{{ t("common.cancel") }}</el-button>
        <el-button @click="resetAllCondition">{{
          t("report.filter.reset")
        }}</el-button>
        <el-button type="primary" @click="confirm">{{
          t("commons.confirm")
        }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import icon_reset_outlined from "@/assets/svg/icon_reset_outlined.svg";
import { getCurrentInstance, onMounted, ref, toRefs } from "vue";
import { Icon } from "@/components/icon-custom";
import EmptyBackground from "@/components/empty-background/src/EmptyBackground.vue";
import ConditionSetPopover from "./ConditionSetPopover.vue";
import { deepCopy } from "@/utils/utils";
import { ElMessage, ElMessageBox } from "element-plus-secondary";
import dayjs from "dayjs";
import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
const emits = defineEmits(["reportFilterChange"]);
let currentInstance;

onMounted(() => {
  currentInstance = getCurrentInstance();
});

const defaultDrawerShow = ref(false);
const conditionPopoverShow = ref(true);
const props = defineProps({
  filterInfo: {
    type: Object,
    required: true,
  },
});

const { filterInfo } = toRefs(props);
const relativeToCurrentList = {
  thisYear: t("dynamic_year.current"),
  lastYear: t("dynamic_year.last"),
  thisMonth: t("dynamic_month.current"),
  lastMonth: t("dynamic_month.last"),
  today: t("dynamic_time.today"),
  yesterday: t("dynamic_time.yesterday"),
  monthBeginning: t("dynamic_time.firstOfMonth"),
  yearBeginning: t("dynamic_time.firstOfYear"),
  custom: t("dynamic_time.custom"),
  year: t("dynamic_time.year"),
  month: t("dynamic_time.month"),
  date: t("dynamic_time.date"),
  f: t("dynamic_time.before"),
  b: t("dynamic_time.after"),
  eq: t("v_query.exact_match"),
  like: t("v_query.fuzzy_match"),
};

const typeTimeMap = {
  year: "YYYY",
  yearrange: "DATETIME-YEAR",
  month: "YYYY-MM",
  monthrange: "YYYY-MM",
  date: "YYYY-MM-DD",
  daterange: "YYYY-MM-DD",
  datetime: "YYYY-MM-DD HH:mm:ss",
  datetimerange: "YYYY-MM-DD HH:mm:ss",
};

const contentClick = () => {
  const popoverRefs = currentInstance.proxy.$refs;
  if (popoverRefs) {
    Object.keys(popoverRefs).forEach((refId) => {
      try {
        popoverRefs[refId]?.closePopover();
      } catch (e) {
        // ignore
      }
    });
  }
};

// 校验必填项
const checkData = () => {
  let requiredFilterName = null;
  if (
    filterInfo.value.defaultFilter &&
    filterInfo.value.defaultFilter.length > 0
  ) {
    filterInfo.value.defaultFilter.forEach((filterItem, index) => {
      if (filterItem.required) {
        const value = getValue(filterItem);
        if (!value) {
          if (requiredFilterName) {
            requiredFilterName = "," + filterItem.name;
          } else {
            requiredFilterName = filterItem.name;
          }
        }
      }
    });
  }
  return requiredFilterName;
};

const getValueHtml = (filterItem) => {
  const value = getValue(filterItem);
  return !!value
    ? value
    : `<span class="condition-none">${t("dataset.na")}</span>`;
};

const getValue = (filterItem) => {
  let value = null;
  filterItem.timeType;
  if (filterItem.displayType === "22") {
    // 22 数字区间
    if (filterItem.defaultValueCheck) {
      value =
        filterItem.defaultNumValueStart + "-" + filterItem.defaultNumValueEnd;
    }
    value = !!value ? value : null;
  } else if (filterItem.displayType === "8") {
    // 8 文本搜索
    const pre =
      filterItem.defaultConditionValueOperatorF === "eq"
        ? `${t("v_query.exact_match")}：`
        : `${t("v_query.fuzzy_match")}：`;
    const sx =
      filterItem.defaultConditionValueOperatorS === "eq"
        ? `${t("v_query.exact_match")}：`
        : `${t("v_query.fuzzy_match")}：`;
    value = filterItem.defaultConditionValueF;
    const valueS = filterItem.defaultConditionValueS;
    if ([1, 2].includes(filterItem.conditionType) && valueS) {
      // 与或条件
      value =
        value +
        "<br/>" +
        `<sapn class="condition-type-tip">${filterItem.conditionType === 1 ? t("chart.and") : t("chart.or")}</sapn> ${sx + valueS}`;
    }
    value = !!value ? pre + value : null;
  } else if (filterItem.defaultValueCheck) {
    // 已设置默认值
    if (filterItem.displayType === "1") {
      // 1 时间
      if (filterItem.timeType === "dynamic") {
        // dynamic 动态时间
        if (filterItem.relativeToCurrent === "custom") {
          // custom 自定义
          value =
            `${t("dynamic_time.relative")}: ` +
            filterItem.timeNum +
            relativeToCurrentList[filterItem.relativeToCurrentType] +
            relativeToCurrentList[filterItem.around];
        } else {
          value =
            `${t("dynamic_time.relative")}: ` +
            relativeToCurrentList[filterItem.relativeToCurrent]; // 其他
        }
      } else {
        value = dateFormat(filterItem.defaultValue, filterItem.timeGranularity);
      }
    } else if (filterItem.displayType === "7") {
      // 7 时间范围
      if (filterItem.timeType === "dynamic") {
        // dynamic 动态时间
        value = t("dynamic_time.dynamic") + "<br/>";
        value =
          value +
          `${t("report.start_time")}: ` +
          filterItem.timeNum +
          relativeToCurrentList[filterItem.relativeToCurrentType] +
          relativeToCurrentList[filterItem.around] +
          "<br/>";
        value =
          value +
          `${t("report.end_time")}: ` +
          filterItem.timeNumRange +
          relativeToCurrentList[filterItem.relativeToCurrentTypeRange] +
          relativeToCurrentList[filterItem.aroundRange];
      } else {
        value = dateFormatRange(
          filterItem.defaultValue,
          filterItem.timeGranularityMultiple,
        );
      }
    } else if (filterItem.displayType === "9") {
      // 9 下拉树
      if (filterItem.defaultValue) {
        if (Array.isArray(filterItem.defaultValue)) {
          value = filterItem.defaultValue.map((item) => {
            let parts = item.split("-de-");
            return parts[parts.length - 1];
          });
        } else {
          let parts = filterItem.defaultValue.split("-de-");
          value = parts[parts.length - 1];
        }
      } else {
        value = filterItem.defaultValue;
      }
    } else {
      const emptyText = t("v_query.empty_data");
      if (Array.isArray(filterItem.defaultValue)) {
        value = filterItem.defaultValue.map((item) =>
          item === "_empty_$" ? emptyText : item,
        );
      } else {
        value =
          filterItem.defaultValue === "_empty_$"
            ? emptyText
            : filterItem.defaultValue;
      }
    }
  }
  return value;
};

const dateFormatRange = (value, timeGranularity) => {
  if (value && value.length === 2) {
    const timeBegin = dayjs(new Date(value[0])).format(
      typeTimeMap[timeGranularity],
    );
    const timeEnd = dayjs(new Date(value[1])).format(
      typeTimeMap[timeGranularity],
    );
    return timeBegin + "至" + timeEnd;
  } else {
    return null;
  }
};

const dateFormat = (value, timeGranularity) => {
  if (!!value) {
    const time = dayjs(new Date(value)).format(typeTimeMap[timeGranularity]);
    return time === "Invalid Date" ? null : time;
  } else {
    return null;
  }
};

const open = () => {
  defaultDrawerShow.value = true;
};

//还原单个默认值
const resetCondition = (index) => {
  ElMessageBox.confirm(t("commons.confirm") + t("report.filter.reset") + "?", {
    confirmButtonType: "primary",
    type: "warning",
    autofocus: false,
    showClose: false,
    confirmButtonText: t("dataset.confirm"),
    cancelButtonText: t("dataset.cancel"),
  }).then(() => {
    filterInfo.value.defaultFilter.splice(
      index,
      1,
      filterInfo.value.sourceFilter[index],
    );
  });
};

//还原所有默认值
const resetAllCondition = () => {
  ElMessageBox.confirm(
    t("commons.confirm") + t("report.filter.reset_all") + "?",
    {
      confirmButtonType: "primary",
      type: "warning",
      autofocus: false,
      showClose: false,
      confirmButtonText: t("dataset.confirm"),
      cancelButtonText: t("dataset.cancel"),
    },
  ).then(() => {
    filterInfo.value.defaultFilter = deepCopy(filterInfo.value.sourceFilter);
  });
};
const validateConditionType = ({
  defaultConditionValueF,
  defaultConditionValueS,
  conditionType,
}) => {
  if (conditionType === 0) {
    return defaultConditionValueF === "";
  } else {
    return defaultConditionValueF === "" || defaultConditionValueS === "";
  }
};

const setParams = (ele) => {
  const {
    defaultConditionValueOperatorF,
    defaultConditionValueF,
    defaultConditionValueOperatorS,
    defaultConditionValueS,
  } = ele;
  ele.conditionValueOperatorF = defaultConditionValueOperatorF;
  ele.conditionValueF = defaultConditionValueF;
  ele.conditionValueOperatorS = defaultConditionValueOperatorS;
  ele.conditionValueS = defaultConditionValueS;
};

const validate = () => {
  return filterInfo.value.defaultFilter.some((ele) => {
    if (ele.auto) return false;
    if (
      !ele.checkedFields?.length ||
      ele.checkedFields.some((itx) => !ele.checkedFieldsMap[itx])
    ) {
      ElMessage.error(t("v_query.be_linked_first"));
      return true;
    }

    if (
      ele.displayType === "0" &&
      ele.defaultValueCheck &&
      ((Array.isArray(ele.defaultValue) && !ele.defaultValue.length) ||
        !ele.defaultValue)
    ) {
      if (ele.optionValueSource !== 1) {
        ElMessage.error(t("report.filter.title"));
        return true;
      }

      if (!ele.defaultValueFirstItem) {
        ElMessage.error(t("report.filter.title"));
        return true;
      }
    }

    if (ele.displayType === "9") {
      if (
        ele.defaultValueCheck &&
        ((Array.isArray(ele.defaultValue) && !ele.defaultValue.length) ||
          !ele.defaultValue)
      ) {
        ElMessage.error(t("report.filter.title"));
        return true;
      }

      if (
        ele.treeCheckedList
          ?.slice(0, ele.treeFieldList.length)
          .some(
            (item) =>
              !item.checkedFields?.length ||
              item.checkedFields.some((itx) => !item.checkedFieldsMap[itx]),
          )
      ) {
        ElMessage.error(t("v_query.be_linked_first"));
        return true;
      }
    }

    if (ele.displayType === "22" && ele.defaultValueCheck) {
      ele.numValueEnd = ele.defaultNumValueEnd;
      ele.numValueStart = ele.defaultNumValueStart;
      if (
        (ele.defaultNumValueEnd !== 0 && !ele.defaultNumValueEnd) ||
        (ele.defaultNumValueStart !== 0 && !ele.defaultNumValueStart)
      ) {
        ElMessage.error(t("v_query.cannot_be_empty_de"));
        return true;
      }
      if (
        !isNaN(ele.defaultNumValueEnd) &&
        !isNaN(ele.defaultNumValueStart) &&
        ele.defaultNumValueEnd < ele.defaultNumValueStart
      ) {
        ElMessage.error(t("v_query.the_minimum_value"));
        return true;
      }
    }
    let errorTips = t("v_query.cannot_be_performed");
    let hasParameterNumArrType = 0;
    if (
      ele.checkedFields.some((id) => {
        if (ele.checkedFieldsMapArrNum?.[id]?.length) {
          if (hasParameterNumArrType === 0) {
            hasParameterNumArrType = 1;
          }

          if (hasParameterNumArrType === 2) {
            return true;
          }
        }

        if (
          !ele.checkedFieldsMapArrNum?.[id]?.length &&
          ["22"].includes(ele.displayType) &&
          !!ele.parameters.length
        ) {
          if (hasParameterNumArrType === 0) {
            hasParameterNumArrType = 2;
          }

          if (hasParameterNumArrType === 1) {
            return true;
          }
        }

        if (
          ele.checkedFieldsMapArrNum?.[id]?.length === 1 &&
          ele.displayType === "22"
        ) {
          errorTips = t("v_query.numerical_parameter_configuration");
          return true;
        }

        if (
          ele.checkedFieldsMapArr?.[id]?.length === 1 &&
          ele.displayType === "7"
        ) {
          errorTips = t("v_query.and_end_time");
          return true;
        }
      })
    ) {
      ElMessage.error(errorTips);
      return true;
    }

    if (ele.required) {
      if (ele.displayType === "8") {
        setParams(ele);
        const result = validateConditionType(ele);
        if (result) {
          ElMessage.error(t("v_query.cannot_be_empty_de"));
        }
        return result;
      }

      if (!ele.defaultValueCheck) {
        ElMessage.error(t("v_query.cannot_be_empty_de"));
        return true;
      }

      if (ele.displayType === "22") {
        if (
          (ele.defaultNumValueEnd !== 0 && !ele.defaultNumValueEnd) ||
          (ele.defaultNumValueStart !== 0 && !ele.defaultNumValueStart)
        ) {
          ElMessage.error(t("v_query.cannot_be_empty_de"));
          return true;
        }
        return false;
      }

      if (
        (Array.isArray(ele.defaultValue) && !ele.defaultValue.length) ||
        (ele.defaultValue !== 0 && !ele.defaultValue)
      ) {
        ElMessage.error(t("v_query.cannot_be_empty_de"));
        return true;
      }
    }

    if (ele.displayType === "8") {
      setParams(ele);
      return false;
    }

    if (!ele.defaultValueCheck) {
      const isMultiple = +ele.displayType === 7 || ele.multiple;
      ele.selectValue = isMultiple ? [] : undefined;
      ele.defaultValue = isMultiple ? [] : undefined;
    }

    if (ele.displayType === "1") {
      if (!ele.defaultValueCheck) return false;
      if (ele.timeType === "fixed") {
        if (!ele.defaultValue) {
          ElMessage.error(t("v_query.cannot_be_empty_time"));
          return true;
        }
      }
    }

    if (ele.displayType === "2") {
      if (!ele.defaultValueCheck) return false;
      if (
        (Array.isArray(ele.defaultValue) && !ele.defaultValue.length) ||
        (!Array.isArray(ele.defaultValue) &&
          ["", undefined, null].includes(ele.defaultValue))
      ) {
        ElMessage.error(t("report.filter.title"));
        return true;
      }
    }

    if (
      ele.displayType !== "9" &&
      ele.optionValueSource === 2 &&
      !ele.valueSource?.filter((ele) => !!ele).length
    ) {
      ElMessage.error(t("v_query.cannot_be_empty_input"));
      return true;
    }

    if (
      !["9", "22", "1", "7"].includes(ele.displayType) &&
      ele.optionValueSource === 1 &&
      !ele.field.id
    ) {
      ElMessage.error(
        !ele.dataset?.id
          ? t("v_query.option_value_field")
          : t("v_query.the_data_set"),
      );
      return true;
    }
  });
};

const confirm = () => {
  if (validate()) return;
  const checkResult = checkData();
  if (checkResult) {
    const msg = t("report.filter.empty_tips", [checkResult]);
    ElMessage.warning(msg);
    return;
  }
  // do confirm
  defaultDrawerShow.value = false;
  //
  const reportFilter = [];
  filterInfo.value.defaultFilter.forEach((filter) => {
    reportFilter.push({
      id: filter.id,
      filterId: filter.id,
      filterInfo: JSON.stringify(filter),
    });
  });

  emits("reportFilterChange", reportFilter);
};

const cancel = () => {
  // do cancel
  filterInfo.value.defaultFilter = deepCopy(
    filterInfo.value.sourceDefaultFilter,
  );
  defaultDrawerShow.value = false;
};

defineExpose({
  open,
});
</script>

<style lang="less">
.condition-drawer-fullscreen {
  .ed-drawer {
    height: 100% !important;
  }
  .ed-table__inner-wrapper {
    padding-bottom: 80px;
  }
}
.condition-none {
  font-size: 14px;
  font-weight: 400;
  text-align: left;
  color: rgba(143, 149, 158, 1);
}
.table-container-condition {
  height: 100%;
}

.condition-type-tip {
  font-size: 12px;
  color: #646a73;
  line-height: 26px;
  margin-right: 8px;
}
</style>
