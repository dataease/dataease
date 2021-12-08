<template>
  <div>
    <div class="text item">
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
          :key="field.fieldName"
          min-width="200px"
          :field="field.fieldName"
          :title="field.remarks"
          :resizable="true"
        />
      </ux-grid>
    </div>
    <span class="table-count">
      {{ $t('dataset.preview_show') }}
      <span class="span-number">1000</span>
      {{ $t('dataset.preview_item') }}
    </span>
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
    'table': function() {
      this.initPreview()
    }
  },
  mounted() {
    this.initHeight()
    this.initPreview()
  },
  methods: {
    initHeight() {
      this.height = (document.getElementsByClassName('el-drawer__body')[0].clientHeight - 40) + 'px'
    },
    initPreview() {
      if (this.dataset && this.dataset.length > 0) {
        post('/dataset/table/unionPreview', this.table).then(response => {
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
.span-number{
  color: #0a7be0;
}
.table-count{
  color: #606266;
}
span{
  font-size: 12px;
}
</style>
