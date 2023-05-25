<template>
  <div class="table-preview">
    <ux-grid
      ref="plxTable"
      size="mini"
      style="width: 100%"
      :height="height"
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
import _ from 'lodash'

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
    },
    unionHeight: {
      type: Number,
      default: 298
    }
  },
  data() {
    return {
      height: 200,
      fields: [],
      data: []
    }
  },
  watch: {
    table: function() {
      this.initPreview()
    },
    unionHeight: {
      handler: function() {
        this.calHeight()
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calHeight)
  },
  mounted() {
    this.initPreview()
    this.calHeight()
    window.addEventListener('resize', this.calHeight)
  },
  methods: {
    calHeight: _.debounce(function() {
      const unionHeight = Math.max(this.unionHeight, 298)
      const currentHeight = document.documentElement.clientHeight
      this.height = currentHeight - unionHeight - 56 - 54 - 36
    }, 200),
    initPreview() {
      if (this.dataset && this.dataset.length > 0) {
        post('/dataset/table/unionPreview', this.table).then((response) => {
          this.fields = response.data.fields
          this.data = response.data.data
          const data = this.data
          this.$refs.plxTable.reloadData(data)
        })
      } else {
        this.fields = []
        this.data = []
        const data = this.data
        this.$refs.plxTable.reloadData(data)
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
