<script lang="ts" setup>
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import field_text from "@/assets/svg/field_text.svg";
import field_time from "@/assets/svg/field_time.svg";
import field_value from "@/assets/svg/field_value.svg";
import field_location from "@/assets/svg/field_location.svg";
import {
  ref,
  inject,
  computed,
  onBeforeMount,
  toRefs,
  Ref,
  reactive,
} from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import { multFieldValuesForPermissions } from "@/api/dataset";
import {
  textOptions,
  dateOptions,
  valueOptions,
  fieldEnums,
  timeOptions,
  timeShortOptionMap,
  timeUnitOptionMap,
  timeSuffixOptions,
  customPreviewTime,
  simplePreviewTime,
  dateTypeMap,
  getSureTime,
} from "./options.js";
import TimeSetDialog from "@/components/time-set-dialog/index.vue";

export interface Item {
  term: string;
  fieldId: string;
  filterType: string;
  valueType: string;
  deType: number;
  enumValue: string[];
  name: string;
  value: any;
  timeType?: string;
}

type Props = {
  index: number;
  fieldConfiguration: Item;
};

const props = withDefaults(defineProps<Props>(), {
  index: 0,
  fieldConfiguration: () => ({
    term: "",
    fieldId: "",
    filterType: "",
    valueType: "",
    deType: 0,
    timeType: "year",
    enumValue: [],
    name: "",
    value: null,
  }),
});
const dynamicPopover = ref();
const dynamicTimeForm = ref();
const sureTimePreview = ref();
const dynamicTimePreview = ref();
const state = reactive({
  form: reactive({
    format: "YYYY-MM-DD",
    timeFlag: 1,
    count: 1,
    unit: 1,
    suffix: 1,
    time: "09:00:00",
  }),
});
const { t } = useI18n();
const showDel = ref(false);
const keywords = ref("");
const activeName = ref("");
const enumList = ref<string[]>([]);
const filterList = ref<{ value: string; label: string }[]>([]);

const { fieldConfiguration } = toRefs(props);
const iconMap = {
  text: field_text,
  value: field_value,
  location: field_location,
  time: field_time,
};
const filedList =
  inject<
    Ref<{ name: string; id: string; deType: number; dateStyle: string }[]>
  >("filedList")!;

const operators = computed(() => {
  const { deType } = fieldConfiguration.value;
  if ([0, 5].includes(deType)) {
    return textOptions;
  } else if (deType === 1) {
    return dateOptions;
  } else {
    return valueOptions;
  }
});

const valueList = [
  {
    label: t("chart.field_dynamic"),
    value: "dynamic",
  },
  {
    label: t("chart.field_fixed"),
    value: "fixed",
  },
];
const dynamicValueList = [
  {
    label: t("chart.axis_value_max"),
    value: "max",
  },
  {
    label: t("chart.axis_value_min"),
    value: "min",
  },
  {
    label: t("threshold.average"),
    value: "average",
  },
];
const valueEnum = computed(() => {
  const { deType } = fieldConfiguration.value;
  if (
    [2, 3].includes(deType) ||
    (1 === deType && timeFormat.value?.includes("YYYY"))
  ) {
    return [...valueList];
  } else {
    return [valueList[1]];
  }
});
const dimensions = computed(() => {
  if (!keywords.value) return computedFiledList.value;
  return computedFiledList.value.filter((ele) =>
    ele.name.includes(keywords.value)
  );
});
const computedFiledList = computed(() => {
  return filedList.value || [];
});

const timeFormat = computed(() => {
  let format = "yyyy-MM-dd HH:mm:ss";
  const { fieldId, deType } = fieldConfiguration.value;
  if (!deType || deType !== 1) {
    return format;
  }
  const { dateStyle } = filedList.value.find((ele) => ele.id === fieldId) || {};

  switch (dateStyle) {
    case "y":
      format = "YYYY";
      break;

    case "y_M":
      format = "YYYY-MM";
      break;

    case "y_M_d":
      format = "YYYY-MM-DD";
      break;

    case "H_m_s":
      format = "HH:mm:ss";
      break;

    case "y_M_d_H_m":
      format = "YYYY-MM-DD HH:mm";
      break;

    case "y_M_d_H_m_s":
      format = "YYYY-MM-DD HH:mm:ss";
      break;
    default:
      break;
  }
  return format;
});
const dateType = computed(() => {
  return dateTypeMap[timeFormat.value] || "datetime";
});
const timePickerFormat = computed(() => {
  return timeFormat.value.replace("YYYY-MM-DD ", "");
});
const timeFlagOptions = computed(() => {
  const { deType } = fieldConfiguration.value;
  if (!deType || deType !== 1) {
    return [];
  }
  return timeShortOptionMap[timeFormat.value];
});

const timeUnitOptions = computed(() => {
  const { deType } = fieldConfiguration.value;
  if (
    !deType ||
    deType !== 1 ||
    state.form.timeFlag !== 9 ||
    timeFormat.value === "HH:mm:ss"
  ) {
    return [];
  }
  return timeUnitOptionMap[timeFormat.value];
});

onBeforeMount(() => {
  initNameEnumName();
  filterListInit(fieldConfiguration.value.deType);
  fillDynamicInputVal();
});

const initNameEnumName = () => {
  const { name, fieldId } = fieldConfiguration.value;
  if (!name && !fieldId) return;
  initEnumOptions();
  activeName.value = fieldConfiguration.value.name;
};

const filterTypeChange = () => {
  fieldConfiguration.value.term = "";
  fieldConfiguration.value.value = null;
  initEnumOptions();
};

const normalizeNumericValue = (value: string | number) => {
  const source = String(value ?? "");
  let normalized = source.replace(/[^\d.-]/g, "");
  normalized = normalized.replace(/(?!^)-/g, "");
  const firstDot = normalized.indexOf(".");
  if (firstDot !== -1) {
    normalized =
      normalized.slice(0, firstDot + 1) +
      normalized.slice(firstDot + 1).replace(/\./g, "");
  }
  fieldConfiguration.value.value = normalized;
};
const initEnumOptions = () => {
  const { deType, filterType, fieldId } = fieldConfiguration.value;
  // 查找枚举值
  if (filterType === "enum" && [0, 5].includes(deType)) {
    multFieldValuesForPermissions({ fieldIds: [fieldId] }).then((res) => {
      enumList.value = optionData(res.data);
    });
  }
};

const optionData = (data) => {
  if (!data) return null;
  return data.filter((item) => !!item);
};
const cancel = () => {
  fieldConfiguration.value.name = activeName.value || "";
};

const selectItem = ({ name, id, deType }) => {
  activeName.value = name;
  Object.assign(fieldConfiguration.value, {
    fieldId: id,
    name,
    deType,
    filterType: "logic",
    valueType: "fixed",
    enumValue: [],
    value: "",
    term: "eq",
  });
  filterListInit(deType);
};
const filterListInit = (deType) => {
  filterList.value = [
    {
      value: "logic",
      label: t("deDataset.logic_filter"),
    },
    {
      value: "enum",
      label: t("deDataset.enum_filter"),
    },
  ];
  if ([1, 2, 3].includes(deType)) {
    filterList.value = [filterList.value[0]];
  }
};
const handleValueTypeChange = () => {
  fieldConfiguration.value.enumValue = [];
  fieldConfiguration.value.value = null;
};
const emits = defineEmits(["update:item", "del"]);

const cancelTimePopover = () => {
  dynamicPopover.value?.hide();
};
const saveTimePopover = () => {
  const value = JSON.stringify(state.form);
  fieldConfiguration.value.value = value;
  sureTimePreview.value = getSureTime(state.form);
  cancelTimePopover();
};

const beforeDynamicShow = () => {
  initDynamicForm();
};
const beforeDynamicHide = () => {
  resetForm();
};
const defaultForm = {
  format: "YYYY-MM-DD",
  timeFlag: 1,
  count: 1,
  unit: 1,
  suffix: 1,
  time: "09:00:00",
};
const resetForm = () => {
  for (const key in defaultForm) {
    state.form[key] = defaultForm[key];
  }
  dynamicTimePreview.value = null;
};
const initDynamicForm = () => {
  if (fieldConfiguration.value.value) {
    const json = JSON.parse(fieldConfiguration.value.value);
    for (const key in defaultForm) {
      if (json.hasOwnProperty(key)) {
        state.form[key] = json[key];
      } else {
        state.form[key] = defaultForm[key];
      }
    }
  } else {
    resetForm();
  }
  state.form["format"] = timeFormat.value;
  getPreviewTime();
};
const getPreviewTime = () => {
  if (state.form.timeFlag !== 9) {
    dynamicTimePreview.value = simplePreviewTime(
      timeFormat.value,
      state.form.timeFlag
    );
  } else {
    dynamicTimePreview.value = customPreviewTime(timeFormat.value, state.form);
  }
};
const countChange = (val) => {
  if (val === null) {
    state.form.count = 0;
  }
  getPreviewTime();
};

const fillDynamicInputVal = () => {
  const { deType, valueType } = fieldConfiguration.value;
  if (
    deType === 1 &&
    valueType === "dynamic" &&
    timeFormat.value?.includes("YYYY")
  ) {
    initDynamicForm();
    sureTimePreview.value = getSureTime(state.form);
  }
};

const timeDialog = ref();
const showTimeDialog = (obj: any) => {
  if (obj.deType !== 1) return;
  timeDialog.value.init(obj.timeType, obj.value);
};
const saveTime = (type, value) => {
  fieldConfiguration.value.timeType = type;
  fieldConfiguration.value.value = value;
};
</script>

<template>
  <div class="white-nowrap">
    <div
      class="filed"
      @mouseover="showDel = true"
      @mouseleave="showDel = false"
    >
      <span class="filed-title">{{ t("auth.filter_fields") }}</span>

      <el-dropdown trigger="click" :hide-on-click="false">
        <el-input
          :placeholder="t('auth.select_filter_fields')"
          v-model="fieldConfiguration.name"
          size="small"
          @input="cancel"
        >
        </el-input>
        <template #dropdown>
          <el-dropdown-menu class="de-el-dropdown-menu">
            <el-input
              :placeholder="t('auth.enter_keywords')"
              @keydown.stop
              size="small"
              v-model="keywords"
            >
              <template #prefix>
                <el-icon>
                  <Icon name="icon_search-outline_outlined"
                    ><icon_searchOutline_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </template>
            </el-input>
            <ul class="dimension">
              <li
                @click="selectItem(ele)"
                :style="{
                  backgroundColor: activeName === ele.name ? '#f0f7ff' : '',
                }"
                :key="ele.id"
                v-for="ele in dimensions"
              >
                <el-icon>
                  <Icon :className="`field-icon-${fieldEnums[ele.deType]}`">
                    <component
                      :is="iconMap[fieldEnums[ele.deType]]"
                      :class="`field-icon-${fieldEnums[ele.deType]}`"
                      class="svg-icon"
                    ></component>
                  </Icon>
                </el-icon>
                <span>{{ ele.name }}</span>
              </li>
            </ul>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <div
        class="white-nowrap flex-align-center"
        style="position: relative"
        v-if="fieldConfiguration.fieldId"
      >
        <span
          class="filed-title"
          :class="{ 'hidden-item': filterList?.length === 1 }"
          >{{ t("auth.screen_method") }}</span
        >
        <el-select
          size="small"
          class="threshold-filter-type w181"
          :class="{ 'hidden-item': filterList?.length === 1 }"
          @change="filterTypeChange"
          v-model="fieldConfiguration.filterType"
          :placeholder="t('auth.select')"
        >
          <el-option
            v-for="ele in filterList"
            :key="ele.value"
            :label="ele.label"
            :value="ele.value"
          >
          </el-option>
        </el-select>
        <template v-if="fieldConfiguration.filterType === 'logic'">
          <span
            class="filed-title"
            :class="{ 'hidden-item': valueEnum?.length === 1 }"
            style="min-width: 8px"
          ></span>
          <el-select
            class="w100"
            @change="handleValueTypeChange"
            size="small"
            :class="{ 'hidden-item': valueEnum?.length === 1 }"
            v-model="fieldConfiguration.valueType"
          >
            <el-option
              v-for="ele in valueEnum"
              :key="ele.value"
              :label="t(ele.label)"
              :value="ele.value"
            >
            </el-option>
          </el-select>
          <el-select
            class="w100"
            size="small"
            v-model="fieldConfiguration.term"
            :placeholder="t('auth.default_method')"
          >
            <el-option
              v-for="ele in operators"
              :key="ele.value"
              :label="t(ele.label)"
              :value="ele.value"
            >
            </el-option>
          </el-select>

          <template v-if="fieldConfiguration.valueType === 'dynamic'">
            <template v-if="fieldConfiguration.deType === 1">
              <el-popover
                ref="dynamicPopover"
                width="420"
                placement="bottom-start"
                trigger="click"
                popper-class="dynamic-time-popover"
                @show="beforeDynamicShow"
                @hide="beforeDynamicHide"
              >
                <template #reference>
                  <el-input
                    v-model="sureTimePreview"
                    size="small"
                    class="dynamic-time-input"
                    :class="
                      timeFormat.includes('YYYY')
                        ? 'threshold-rule-date'
                        : 'threshold-rule-time'
                    "
                    :placeholder="t('auth.select')"
                    readonly
                  >
                    <template #suffix>
                      <el-icon class="el-input__icon">
                        <Icon name="icon_edit_outlined"
                          ><icon_edit_outlined class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </template>
                  </el-input>
                </template>
                <div class="threshold-dynamic-time">
                  <div class="time-header">
                    <span class="title">时间设置</span>
                  </div>
                  <div class="time-form">
                    <el-form
                      ref="dynamicTimeForm"
                      require-asterisk-position="right"
                      :model="state.form"
                      label-width="80px"
                      label-position="top"
                    >
                      <el-form-item label="时间粒度">
                        <el-select v-model="timeFormat" disabled>
                          <el-option
                            v-for="ele in timeOptions"
                            :key="ele.value"
                            :label="t(ele.label)"
                            :value="ele.value"
                          >
                          </el-option>
                        </el-select>
                      </el-form-item>

                      <el-form-item label="相对当前" prop="timeFlag">
                        <el-select
                          v-model="state.form.timeFlag"
                          :teleported="false"
                          @change="getPreviewTime"
                        >
                          <el-option
                            v-for="ele in timeFlagOptions"
                            :key="ele.value"
                            :label="t(ele.label)"
                            :value="ele.value"
                          >
                          </el-option>
                        </el-select>

                        <div
                          class="dynamic-custom-line"
                          v-if="state.form.timeFlag === 9"
                        >
                          <el-input-number
                            v-if="timeFormat.includes('YYYY')"
                            :min="0"
                            :max="100"
                            effect="plain"
                            step-strictly
                            v-model="state.form.count"
                            controls-position="right"
                            @change="countChange"
                          />
                          <el-select
                            v-if="timeFormat.includes('YYYY')"
                            v-model="state.form.unit"
                            :teleported="false"
                            @change="getPreviewTime"
                          >
                            <el-option
                              v-for="ele in timeUnitOptions"
                              :key="ele.value"
                              :label="t(ele.label)"
                              :value="ele.value"
                            ></el-option>
                          </el-select>

                          <el-select
                            v-if="timeFormat.includes('YYYY')"
                            v-model="state.form.suffix"
                            :teleported="false"
                            @change="getPreviewTime"
                          >
                            <el-option
                              v-for="ele in timeSuffixOptions"
                              :key="ele.value"
                              :label="t(ele.label)"
                              :value="ele.value"
                            ></el-option>
                          </el-select>

                          <el-time-picker
                            class="dynamic-time-picker"
                            v-if="
                              timeFormat.includes('HH') && state.form.unit !== 4
                            "
                            v-model="state.form.time"
                            :teleported="false"
                            :clearable="false"
                            :format="timePickerFormat"
                            :value-format="timePickerFormat"
                            @change="getPreviewTime"
                            placeholder=""
                          />
                        </div>
                      </el-form-item>

                      <el-form-item :label="t('template_manage.preview')">
                        <el-date-picker
                          v-if="timeFormat.includes('YYYY')"
                          v-model="dynamicTimePreview"
                          :type="
                            timeFormat.includes('HH') ? 'datetime' : 'date'
                          "
                          :clearable="false"
                          :format="timeFormat"
                          class="threshold-preview-date"
                          :value-format="timeFormat"
                          placeholder=""
                          disabled
                        />
                        <el-time-picker
                          v-else
                          v-model="dynamicTimePreview"
                          class="threshold-preview-date"
                          :clearable="false"
                          :format="timePickerFormat"
                          :value-format="timePickerFormat"
                          placeholder=""
                          disabled
                        />
                      </el-form-item>
                    </el-form>
                  </div>
                  <div class="time-btn">
                    <el-button secondary @click="cancelTimePopover">{{
                      t("common.cancel")
                    }}</el-button>
                    <el-button type="primary" @click="saveTimePopover">
                      {{ t("common.sure") }}</el-button
                    >
                  </div>
                </div>
              </el-popover>
            </template>
            <template v-else>
              <el-select
                class="w100"
                v-if="fieldConfiguration.valueType === 'dynamic'"
                size="small"
                filterable
                v-model="fieldConfiguration.value"
              >
                <el-option
                  v-for="ele in dynamicValueList"
                  :key="ele.value"
                  :label="t(ele.label)"
                  :value="ele.value"
                >
                </el-option>
              </el-select>
            </template>
          </template>

          <template v-else>
            <template v-if="fieldConfiguration.deType === 1">
              <el-date-picker
                v-if="timeFormat.includes('YYYY')"
                v-model="fieldConfiguration.value"
                :type="timeFormat.includes('HH') ? 'datetime' : dateType"
                class="threshold-rule-date"
                size="small"
                :clearable="false"
                :format="timeFormat"
                :value-format="timeFormat"
                placeholder=""
              />
              <el-time-picker
                v-else
                v-model="fieldConfiguration.value"
                class="threshold-rule-time"
                size="small"
                :clearable="false"
                :format="timeFormat"
                :value-format="timeFormat"
                placeholder=""
              />
              <div class="time-bottom-line"></div>
            </template>
            <template
              v-else-if="
                [2, 3].includes(fieldConfiguration.deType) &&
                !['null', 'empty', 'not_null', 'not_empty'].includes(
                  fieldConfiguration.term
                )
              "
            >
              <el-input
                class="w70 mar5"
                size="small"
                v-model="fieldConfiguration.value"
                inputmode="decimal"
                @input="normalizeNumericValue"
              />
              <div class="bottom-line"></div>
            </template>
            <template
              v-else-if="
                !['null', 'empty', 'not_null', 'not_empty'].includes(
                  fieldConfiguration.term
                )
              "
            >
              <el-input
                class="w70 mar5"
                size="small"
                :readonly="fieldConfiguration.deType === 1"
                v-model="fieldConfiguration.value"
                @click="showTimeDialog(fieldConfiguration)"
              />
              <div class="bottom-line"></div>
            </template>
          </template>
        </template>
        <template v-else>
          <span class="filed-title" style="min-width: 8px"></span>
          <el-select
            style="width: 220px"
            size="small"
            collapse-tags
            multiple
            filterable
            collapse-tags-tooltip
            v-model="fieldConfiguration.enumValue"
          >
            <el-option
              v-for="ele in enumList"
              :key="ele"
              :label="ele"
              :value="ele"
            >
            </el-option>
          </el-select>
        </template>
      </div>
      <el-icon v-if="showDel" class="delete-icon" @click="emits('del')">
        <Icon name="icon_delete-trash_outlined"
          ><icon_deleteTrash_outlined class="svg-icon"
        /></Icon>
      </el-icon>
    </div>
  </div>
  <TimeSetDialog @saveTime="saveTime" ref="timeDialog"></TimeSetDialog>
</template>
<style lang="less">
.dynamic-time-popover {
  padding: 0 !important;
}
</style>

<style lang="less" scoped>
.threshold-dynamic-time {
  width: 420px;
  padding: 24px;
  div {
    width: 100%;
  }
  .time-header {
    height: 24px;
    display: flex;
    align-items: center;
    .title {
      width: 64px;
    }
  }
  .time-form {
    margin: 24px 0;
    :deep(.dynamic-custom-line) {
      margin-top: 8px;
      width: 100%;
      column-gap: 8px;
      display: flex;
      .dynamic-time-picker {
        min-width: 95px;
      }
    }
    :deep(.threshold-preview-date) {
      width: 100%;
      .ed-input__wrapper {
        width: 100%;
      }
    }
  }

  .time-btn {
    height: 32px;
    display: flex;
    justify-content: end;
  }
}
.white-nowrap {
  white-space: nowrap;
}
.hidden-item {
  display: none !important;
}
.threshold-filter-type {
  width: 85px;
}
.filed {
  width: fit-content;
  height: 41.4px;
  padding: 1px 3px 1px 10px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  margin-left: 20px;
  min-width: 200px;
  padding-right: 40px;
  justify-content: left;
  position: relative;
  white-space: nowrap;
  :deep(.ed-input-number.is-controls-right .ed-input__wrapper) {
    padding-left: 1px !important;
    padding-right: 1px !important;
  }
  :deep(.threshold-rule-date) {
    width: 160px;
    margin-top: -2px;
  }
  :deep(.threshold-rule-time) {
    width: 100px;
    margin-top: -2px;
  }
  :deep(.dynamic-time-input) {
    margin-top: 0px !important;
  }

  .filed-title {
    word-wrap: break-word;
    line-height: 28px;
    color: #7e7e7e;
    font-size: 14px;
    white-space: nowrap;
    box-sizing: border-box;
    margin-right: 5px;
    display: inline-block;
    min-width: 50px;
    text-align: right;
  }

  .delete-icon {
    font-size: 14px;
    cursor: pointer;
    position: absolute;
    top: 50%;
    right: 16px;
    transform: translateY(-50%);
  }

  .ed-input {
    width: 170px;
  }

  .w100.ed-select {
    width: 100px !important;
  }

  .w181.ed-select {
    width: 181px !important;
  }

  .w70 {
    width: 70px !important;
  }

  .w70 {
    width: 70px;
  }
  .w120 {
    width: 120px;
  }

  .mar5 {
    margin-left: -5px;
  }

  :deep(.ed-input-number__decrease:not(.is-disabled)),
  :deep(.ed-input-number__increase:not(.is-disabled)) {
    &:hover {
      z-index: 10;
      &::after {
        display: block;
      }
      & ~ .ed-input:not(.is-disabled) .ed-input__wrapper {
        box-shadow: 0 0 0 0 #000 inset !important;
      }
    }
  }
  :deep(.ed-input-number__decrease),
  :deep(.ed-input-number__increase) {
    width: 20px;
    height: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: height 0.3s cubic-bezier(0.645, 0.045, 0.355, 1);
    display: none;
  }

  :deep(.ed-input-number__decrease:hover) {
    height: 16px;
    & + .ed-input-number__increase {
      height: 8px;
    }
  }
  :deep(.ed-input-number__increase:hover) {
    height: 16px;
    & + .ed-input-number__decrease {
      height: 8px;
    }
  }

  .bottom-line {
    font-family: var(--de-custom_font, "PingFang");
    font-variant: tabular-nums;
    font-feature-settings: "tnum";
    word-wrap: break-word;
    text-align: left;
    line-height: 28px;
    color: #7e7e7e;
    font-size: 14px;
    white-space: pre;
    box-sizing: border-box;
    height: 1px;
    background-color: #000;
    opacity: 0.3;
    position: absolute;
    right: 5px;
    bottom: 5px;
    width: 70px;
    z-index: 10;
  }

  :deep(.ed-input-number.is-controls-right .ed-input__inner) {
    padding-right: 20px;
  }

  .ed-input-number {
    line-height: 26px;
    height: 26px;
  }

  :deep(.ed-select) {
    :deep(.ed-input__suffix-inner) {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;

      .ed-input__icon {
        height: auto;
      }
    }
  }

  :deep(.ed-input__wrapper),
  :deep(.ed-select__wrapper) {
    background-color: #f8f8fa;
    border: none;
    border-radius: 0;
    box-shadow: none !important;
    height: 26px;
    font-family: var(--de-custom_font, "PingFang");
    word-wrap: break-word;
    text-align: left;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    list-style: none;
    user-select: none;
    cursor: pointer;
    line-height: 26px;
    box-sizing: border-box;
    max-width: 100%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    opacity: 1;
  }
  :deep(.ed-select .ed-input.is-focus .ed-input__wrapper),
  :deep(.ed-select:hover:not(.ed-select--disabled) .ed-input__wrapper),
  :deep(.ed-select .ed-input__wrapper.is-focus) {
    box-shadow: none !important;
  }

  i {
    margin-left: 5px;
    color: #7e7e7e;
  }
}
.filed:hover {
  background-color: #e9eaef;
  :deep(.ed-input-number__decrease),
  :deep(.ed-input-number__increase) {
    display: flex;
  }
}
</style>

<style lang="less">
.de-el-dropdown-menu {
  .dimension {
    max-height: 200px;
    padding: 0;
    overflow-y: auto;
    li {
      list-style: none;
      box-sizing: border-box;
      display: flex;
      align-items: center;
      white-space: nowrap;
      cursor: pointer;
      transition: color 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        border-color 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        background 0.3s cubic-bezier(0.645, 0.045, 0.355, 1),
        padding 0.15s cubic-bezier(0.645, 0.045, 0.355, 1);
      position: relative;
      overflow: hidden;
      font-size: 14px;
      text-overflow: ellipsis;
      padding: 0 16px 0 28px;
      line-height: 32px;
      height: 32px;
      margin: 0;
      padding-left: 16px;
      color: rgba(0, 0, 0, 0.65);
      .ed-icon {
        margin-right: 5px;
      }
    }
    li:hover {
      color: #2e74ff;
      background-color: #f0f7ff;
    }
  }

  .ed-input {
    font-family: inherit;
    overflow: visible;
    box-sizing: border-box;
    margin: 0;
    font-variant: tabular-nums;
    list-style: none;
    font-feature-settings: "tnum";
    display: inline-block;
    width: 100%;
    height: 28px;
    padding: 4px 7px;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    line-height: 28px;
    background-color: #fff;
    background-image: none;
    transition: all 0.3s;
    touch-action: manipulation;
    text-overflow: ellipsis;
    position: relative;
    text-align: inherit;
    min-height: 100%;
    border: 0;
    border-radius: 0;
    padding-left: 26px;

    .ed-input__wrapper {
      box-shadow: none;
      border-bottom: 1px solid #e5e5e5;
      &:focus {
        box-shadow: 0 0 0 2px rgb(46 116 255 / 20%);
        border-right-width: 1px !important;
        outline: 0;
        border-color: none;
      }
    }
  }

  .ed-input {
    font-family: var(--de-custom_font, "PingFang");
    box-sizing: border-box;
    margin: 0;
    color: rgba(0, 0, 0, 0.65);
    font-size: 14px;
    font-variant: tabular-nums;
    line-height: 1.5;
    list-style: none;
    font-feature-settings: "tnum";
    position: relative;
    display: inline-block;
    width: 100%;
    text-align: start;
    padding: 0 6px;
  }
}
</style>
