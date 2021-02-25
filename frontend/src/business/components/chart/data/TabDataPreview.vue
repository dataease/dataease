<template>
<el-col>
  <el-table
    size="mini"
    :data="data"
    border
    style="width: 100%;">
    <el-table-column
      width="180px"
      v-for="field in fields"
      :key="field.fieldName"
      :prop="field.fieldName"
      :label="field.fieldName">
    </el-table-column>
  </el-table>
</el-col>
</template>

<script>
export default {
  props: {
    table: Object,
    fields: Array,
    data: Array
  },
  name: "TabDataPreview",
  data() {
    return {
      tableData: [{
        date: '2016-05-02',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1518 弄'
      }, {
        date: '2016-05-04',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1517 弄'
      }, {
        date: '2016-05-01',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1519 弄'
      }, {
        date: '2016-05-03',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1516 弄'
      }]
    }
  },
  computed: {},
  created() {
  },
  mounted() {
  },
  activated() {
  },
  methods: {},
  watch: {}
}
</script>

<style scoped>

</style>
