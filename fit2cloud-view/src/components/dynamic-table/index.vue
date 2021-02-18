<template>
  <div class="dynamic-table">
    <div class="dynamic-table__header" v-if="$slots.header || header">
      <slot name="header">{{ header }}</slot>
    </div>
    <div class="dynamic-table__body">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: "DynamicTable",
  props: {
    header: {},
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/common/mixins.scss";

.dynamic-table {

}

.dynamic-table__header {
  @include flex-row(flex-start, center);
  height: 60px;
  font-size: 20px;
}
</style>
