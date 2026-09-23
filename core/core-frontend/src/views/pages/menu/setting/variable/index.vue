<template>
  <variable-manage ref="variableManage"></variable-manage>
</template>

<script lang="ts" setup>
import VariableManage from '@/views/menu/setting/variable/VariableManage.vue'
</script>

<style lang="less" scoped>
.right-filter .ed-input {
  width: 240px;
  float: right;
}
.org-table__content {
  padding: 24px;
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100% - 12px) !important;
  box-sizing: border-box;
  overflow: hidden;
  border-radius: 4px;
}
.auth-card-container {
  margin-top: 6px;
  .ed-empty {
    padding-top: 166px !important;
  }
  .authentication-card {
    min-width: 204px;
    width: 24%;
    height: 58px;
    border: 1px solid #dee0e3;
    border-radius: 12px;
    margin: 10px 10px 10px 0;
    float: left;
    padding-left: 16px;
    .inner-card {
      height: 100%;
      display: flex;
      align-items: center;
      .card-span {
        width: calc(100% - 50px);
      }
    }
  }
}
</style>
