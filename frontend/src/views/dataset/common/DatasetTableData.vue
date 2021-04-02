<template>
  <el-col>
    <span>{{ table.name }}</span>
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
import { post } from '@/api/dataset/dataset'

export default {
  name: 'DatasetTableData',
  props: {
    table: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      fields: [],
      data: [],
      height: 500
    }
  },
  watch: {
    table() {
      this.initData()
    }
  },
  mounted() {
    window.onresize = () => {
      return (() => {
        this.height = window.innerHeight / 3
      })()
    }
    this.height = window.innerHeight / 3
    this.initData()
  },
  methods: {
    initData() {
      this.resetData()
      if (this.table.id) {
        this.table.row = 10
        post('/dataset/table/getPreviewData', this.table).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
          const datas = this.data
          this.$refs.plxTable.reloadData(datas)
        })
      }
    },

    resetData() {
      this.fields = []
      this.data = []
    }
  }
}
</script>

<style scoped>

</style>
