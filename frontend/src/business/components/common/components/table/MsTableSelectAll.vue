<template>
  <el-table-column width="1" :resizable="false" align="center">
    <el-dropdown slot="header">
            <span class="el-dropdown-link">
              <i class="el-icon-arrow-down"></i>
            </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item @click.native.stop="$emit('selectAll')">
          {{$t('api_test.batch_menus.select_all_data',[total])}}
        </el-dropdown-item>
        <el-dropdown-item @click.native.stop="$emit('selectPageAll')">
          {{$t('api_test.batch_menus.select_show_data',[pageSize])}}
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </el-table-column>
</template>

<script>
    export default {
      name: "MsTableSelectAll",
      props: ['total', 'pageSize']
    }
</script>

<style scoped>

</style>
