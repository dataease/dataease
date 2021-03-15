<template>
  <el-col>
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%;"
      :height="500"
      :checkbox-config="{highlight: true}"
    >
      <ux-table-column
        v-for="field in fields"
        :key="field.originName"
        min-width="200px"
        :field="field.originName"
        :title="field.name"
        :resizable="true"
      />
    </ux-grid>
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
  computed: {
  },
  watch: {
    data() {
      const datas = this.data
      this.$refs.plxTable.reloadData(datas)
    }
  },
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
