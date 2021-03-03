<template>
  <el-table-column width="1" :resizable="false" align="center">
    <el-popover slot="header" placement="right" trigger="click" style="margin-right: 0px;">
      <el-link @click.native.stop="$emit('selectAll')">{{$t('api_test.batch_menus.select_all_data',[total])}}</el-link>
      <br/>
      <el-link @click.native.stop="$emit('selectPageAll')">{{$t('api_test.batch_menus.select_show_data',[pageSize])}}</el-link>
      <i class="el-icon-arrow-down" slot="reference"></i>
    </el-popover>
  </el-table-column>
</template>

<script>
    export default {
      name: "MsTableSelectAll",
      props: ['total', 'pageSize'],
      data() {
        return {
        };
      }
    }
</script>

<style scoped>

</style>
