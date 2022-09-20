<template>
  <div class="table-preview">
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%"
      height="100%"
      :checkbox-config="{ highlight: true }"
      :width-resize="true"
    >
      <ux-table-column
        v-for="field in fields"
        :key="field.fieldName"
        min-width="200px"
        :field="field.fieldName"
        :title="field.remarks"
        :resizable="true"
      />
    </ux-grid>
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'

export default {
  name: 'UnionPreview',
  props: {
    table: {
      type: Object,
      required: true
    },
    dataset: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      height: 'auto',
      fields: [],
      data: []
    }
  },
  watch: {
    table: function () {
      this.initPreview()
    }
  },
  mounted() {
    this.initPreview()
  },
  methods: {
    initPreview() {
      if (this.dataset && this.dataset.length > 0) {
        post('/dataset/table/unionPreview', this.table).then((response) => {
          this.fields = response.data.fields
          this.data = response.data.data
          const datas = this.data
          this.$refs.plxTable.reloadData(datas)
        })
      } else {
        this.fields = []
        this.data = []
        const datas = this.data
        this.$refs.plxTable.reloadData(datas)
      }
    }
  }
}
</script>

<style scoped>
.table-preview {
  height: calc(100% - 64px);
  padding: 18px 25px;
  overflow-y: auto;
  box-sizing: border-box;
}
</style>
