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
        :key="field.originName"
        min-width="200px"
        :prop="field.originName"
        :label="field.name"
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
