<template>
  <el-col
    ref="container"
    v-loading="dataLoading"
    style="width: 100%; height: 100%"
  >
    <span>{{ table.name }}</span>
    <ux-grid
      id="dsData"
      ref="plxTable"
      size="mini"
      style="width: 100%"
      :height="height"
      :checkbox-config="{ highlight: true }"
      :width-resize="true"
    >
      <ux-table-column
        v-for="field in fields"
        :key="field.dataeaseName"
        min-width="200px"
        :field="field.dataeaseName"
        :resizable="true"
      >
        <template slot="header">
          <svg-icon
            v-if="field.deType === 0"
            icon-class="field_text"
            class="field-icon-text"
          />
          <svg-icon
            v-if="field.deType === 1"
            icon-class="field_time"
            class="field-icon-time"
          />
          <svg-icon
            v-if="field.deType === 2 || field.deType === 3"
            icon-class="field_value"
            class="field-icon-value"
          />
          <svg-icon
            v-if="field.deType === 5"
            icon-class="field_location"
            class="field-icon-location"
          />
          <span>{{ field.name }}</span>
        </template>
      </ux-table-column>
    </ux-grid>
    <span v-if="table.name" style="font-size: 12px">{{
      $t('chart.preview_100_data')
    }}</span>
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
      height: 'auto',
      dataLoading: false
    }
  },
  watch: {
    table() {
      this.initData()
    }
  },
  mounted() {
    // window.onresize = () => {
    //   return (() => {
    //     this.height = window.innerHeight / 3
    //   })()
    // }
    // this.height = window.innerHeight / 3
    this.$nextTick(() => {
      this.height =
        document.getElementById('dsData').parentNode.offsetHeight - 16 - 14 - 5
      this.initData()
    })
  },
  methods: {
    initData() {
      this.resetData()
      if (this.table.id) {
        this.dataLoading = true
        this.table.row = 100
        post('/dataset/table/getPreviewData/1/100', this.table, false, 30000)
          .then((response) => {
            this.fields = response.data.fields
            this.data = response.data.data
            const datas = this.data
            if (response.data.status === 'warnning') {
              this.$warning(response.data.msg, 3000)
            }
            if (response.data.status === 'error') {
              this.$error(response.data.msg, 3000)
            }
            this.$refs.plxTable.reloadData(datas)
            this.dataLoading = false
          })
          .catch((res) => {
            this.dataLoading = false
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
