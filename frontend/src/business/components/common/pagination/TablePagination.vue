<template>
  <el-row type="flex" justify="end">
    <div class="table-page">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="pageSizes"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>
  </el-row>
</template>

<script>
  export default {
    name: "MsTablePagination",
    props: {
      page: Object,
      currentPage: {
        type: Number,
        default: 1
      },
      pageSize: {
        type: Number,
        default: 5
      },
      pageSizes: {
        type: Array,
        default: function () {
          return [5, 10, 20, 50, 100]
        }
      },
      total: {
        type: Number,
        default: 0
      },
      change: Function
    },
    methods: {
      handleSizeChange: function (size) {
        this.$emit('update:pageSize', size)
        this.change();
      },
      handleCurrentChange(current) {
        this.$emit('update:currentPage', current)
        this.change();
      }
    }
  }
</script>

<style scoped>
  .table-page {
    padding-top: 20px;
  }
</style>
