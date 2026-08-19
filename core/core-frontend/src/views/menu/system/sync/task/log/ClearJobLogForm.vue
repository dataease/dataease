<script lang="ts" setup>
import { computed, ref } from "vue";
import { find } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
const clearJobLogDialogFormVisible = ref<boolean>(false);
defineExpose({ clearJobLogDialogFormVisible });
const props = withDefaults(
  defineProps<{
    modelValue: Object;
  }>(),
  {
    modelValue: () => {
      return { clearType: "1" };
    },
  }
);

const emits = defineEmits([
  "update:modelValue",
  "clearJobLog",
  "closeClearDialog",
]);
const formLoading = ref<boolean>(false);
const form = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emits("update:modelValue", value);
  },
});
const onSubmit = () => {
  emits(
    "clearJobLog",
    find(clearTypeList, { clearType: form.value.clearType })?.label
  );
};
const closeDialog = () => {
  emits("closeClearDialog");
};
interface ClearTypeOption {
  clearType: string;
  label: string;
}
/**
 * 清理日志方式
 */
const clearTypeList: Array<ClearTypeOption> = [
  { clearType: "1", label: t('sync_task.last_1_days_log') },
  { clearType: "2", label: t('sync_task.last_1_weeks_log') },
  { clearType: "3", label: t('sync_task.last_1_months_log') },
  { clearType: "4", label: t('sync_task.last_3_months_log') },
  { clearType: "5", label: t('sync_task.last_6_months_log') },
  { clearType: "6", label: t('sync_task.last_1_years_log') },
];
</script>
<template>
  <el-dialog
    v-model="clearJobLogDialogFormVisible"
    :title="t('sync_task.clear_log')"
    width="420px"
  >
    <el-form :model="form" label-position="top" v-loading="formLoading">
      <el-form-item :label="t('sync_task.clear')">
        <el-select v-model="form.clearType" :placeholder="t('sync_task.please_choose_clear_method')">
          <el-option
            v-for="item in clearTypeList"
            :label="item.label"
            :value="item.clearType"
            :key="item.clearType"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">{{ t('sync_datasource.cancel') }}</el-button>
        <el-button type="primary" @click="onSubmit">{{ t('sync_task.submit_true') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>
<style lang="less" scoped>
.ed-form-item {
  margin-bottom: 0;
}
.ed-input__wrapper {
  padding-right: 12px;
  padding-left: 12px;
}
.el-button--text {
  margin-right: 15px;
}

.ed-select {
  width: 100%;
}

.el-input {
  width: 100%;
}

.el-dialog__body {
  display: flex;
  justify-content: center;
}
</style>
