<template>
  <el-col style="height: 100%;">
    <el-row
      style="height: 100%;"
      class="custom-position"
    >
      {{ $t('chart.pls_slc_tbl_left') }}
    </el-row>
  </el-col>
</template>

<script>
export default {
  name: 'ChartHome'
}
</script>

<style scoped>
  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #9ea6b2;
  }
</style>
