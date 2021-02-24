<template>
<el-col>
  <el-table
    size="mini"
    :data="data"
    border
    style="width: 100%;">
    <el-table-column
      min-width="200px"
      v-for="field in fields"
      :key="field.originName"
      :prop="field.originName"
      :label="field.name">
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
