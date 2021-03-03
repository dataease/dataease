<template>
  <el-col>
    <span>{{ table.name }}</span>
    <el-table
      size="mini"
      :data="data"
      height="40vh"
      border
      style="width: 100%;margin-top: 6px;"
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
import { post } from '@/api/dataset/dataset'

export default {
  name: 'DatasetTableData',
  props: {
    // eslint-disable-next-line vue/require-default-prop
    table: Object
  },
  data() {
    return {
      fields: [],
      data: []
    }
  },
  watch: {
    table() {
      this.initData()
    }
  },
  created() {
    this.initData()
  },
  mounted() {
  },
  methods: {
    initData() {
      this.resetData()
      if (this.table.id) {
        post('/dataset/table/getPreviewData', this.table).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
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
