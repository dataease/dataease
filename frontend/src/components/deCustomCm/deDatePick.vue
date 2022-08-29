<template>
  <div class="de-date-pick">
    <el-date-picker
      :value="value"
      size="small"
      :disabled="disabled"
      type="daterange"
      @input="handleChange"
      range-separator="-"
      :start-placeholder="$t('commons.date.start_date')"
      :end-placeholder="$t('commons.date.end_date')"
    >
    </el-date-picker>
    <svg-icon icon-class="icon_calendar_outlined" class="calendar-outlined" />
  </div>
</template>

<script>
export default {
  name: "DeDatePick",
  props: {
    disabled: Boolean,
    value: Array,
  },
  methods: {
    handleChange(val) {
      this.$emit("input", val);
    },
  },
};
</script>

<style lang="scss">
.de-date-pick {
  width: 256px;
  position: relative;
  .el-input__icon {
    display: none;
  }
  .el-range-input {
    text-align: left;
  }

  .el-date-editor {
    padding-left: 12px;
    width: 100%;
  }

  .calendar-outlined {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    right: 13px;
  }
}
</style>