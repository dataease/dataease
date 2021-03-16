<template>
  <el-col>
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%;"
      :height="height"
      :checkbox-config="{highlight: true}"
      :width-resize="true"
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
    table: {
      type: Object,
      required: true
    },
    fields: {
      type: Array,
      required: true
    },
    data: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      height: 500
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
  mounted() {
    window.onresize = () => {
      return (() => {
        this.height = window.innerHeight / 2
      })()
    }
    this.height = window.innerHeight / 2
  },
  activated() {
  },
  methods: {}
}
</script>

<style scoped>

</style>
