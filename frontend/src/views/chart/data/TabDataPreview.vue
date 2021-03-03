<template>
  <el-col>
    <el-table
      size="mini"
      :data="data"
      border
      style="width: 100%;"
    >
      <el-table-column
        v-for="field in fields"
        :key="field.fieldName"
        width="180px"
        :prop="field.fieldName"
        :label="field.fieldName"
      />
    </el-table>
  </el-col>
</template>

<script>
export default {
  name: 'TabDataPreview',
  props: {
    table: Object,
    fields: Array,
    data: Array
  },
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
  watch: {},
  created() {
  },
  mounted() {
  },
  activated() {
  },
  methods: {}
}
</script>

<style scoped>

</style>
