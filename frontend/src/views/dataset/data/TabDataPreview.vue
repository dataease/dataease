<template>
  <el-col>
    <el-row>
      <el-col style="width: 300px;">
        <el-form ref="form" :model="form" size="mini" class="row-style">
          <el-form-item>
            <span class="title-text" style="width: 100px;">{{ $t('dataset.showRow') }}</span>
            <el-input v-model="form.row" class="main-area-input">
              <el-button slot="append" size="mini" icon="el-icon-search" @click="reSearch" />
            </el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
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
        :key="field.id"
        min-width="200px"
        :field="field.dataeaseName"
        :resizable="true"
      >
        <template slot="header">
          <svg-icon v-if="field.deType === 0" icon-class="field_text" class="field-icon-text" />
          <svg-icon v-if="field.deType === 1" icon-class="field_time" class="field-icon-time" />
          <svg-icon v-if="field.deType === 2 || field.deType === 3" icon-class="field_value" class="field-icon-value" />
          <svg-icon v-if="field.deType === 5" icon-class="field_location" class="field-icon-location" />
          <span>{{ field.name }}</span>
        </template>
      </ux-table-column>
    </ux-grid>
    <el-row style="margin-top: 4px;">
      <span v-if="table.type === 'excel' || table.type === 'custom'" class="table-count">
        <span v-if="page.total <= currentPage.show">
          {{ $t('dataset.preview_total') }}
          <span class="span-number">{{ page.total }}</span>
          {{ $t('dataset.preview_item') }}
        </span>
        <span v-if="page.total > currentPage.show">
          {{ $t('dataset.preview_show') }}
          <span class="span-number">{{ currentPage.show }}</span>
          {{ $t('dataset.preview_item') }}
          ，{{ $t('dataset.preview_total') }}
          <span class="span-number">{{ page.total }}</span>
          {{ $t('dataset.preview_item') }}
        </span>
      </span>
      <span v-if="table.type === 'db' || table.type === 'sql'" class="table-count">
        {{ $t('dataset.preview_show') }}
        <span class="span-number">{{ page.total }}</span>
        {{ $t('dataset.preview_item') }}
      </span>
      <el-pagination
        :current-page="currentPage.page"
        :page-sizes="[100]"
        :page-size="currentPage.pageSize"
        :pager-count="5"
        layout="sizes, prev, pager, next"
        :total="currentPage.show"
        @current-change="pageChange"
      />
    </el-row>
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
    param: {
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
    },
    form: {
      type: Object,
      required: true
    },
    page: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      height: 500,
      currentPage: {
        page: 1,
        pageSize: 100,
        show: parseInt(this.form.row)
      }
    }
  },
  computed: {
  },
  watch: {
    data() {
      const datas = this.data
      this.$refs.plxTable.reloadData(datas)
    },
    page() {
      if (this.page.total < parseInt(this.form.row)) {
        this.currentPage.show = this.page.total
      } else {
        this.currentPage.show = parseInt(this.form.row)
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      this.calHeight()
    },
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 55 - 38 - 28 - 10
      }, 10)
    },
    reSearch() {
      if (!this.form.row ||
          this.form.row === '' ||
          this.form.row.length > 4 ||
          isNaN(Number(this.form.row)) ||
          String(this.form.row).includes('.') ||
          parseInt(this.form.row) < 1) {
        this.$message({
          message: this.$t('dataset.pls_input_less_5'),
          type: 'error',
          showClose: true
        })
        return
      }
      this.currentPage.show = parseInt(this.form.row)
      this.currentPage.page = 1
      this.$emit('reSearch', { form: this.form, page: this.currentPage })
    },
    pageChange(val) {
      this.currentPage.page = val
      // console.log(this.currentPage)
      this.$emit('reSearch', { form: this.form, page: this.currentPage })
    }
  }
}
</script>

<style scoped>
  .row-style>>>.el-form-item__label{
    font-size: 12px;
  }
  .row-style>>>.el-form-item--mini.el-form-item{
    margin-bottom: 10px;
  }
  .row-style>>>.el-form-item__content{
    display: flex;
    flex-direction: row;
    width: 250px;
  }
  .el-pagination{
    float: right;
  }
  span{
    font-size: 12px;
  }
  .span-number{
    color: #0a7be0;
  }
  .table-count{
    color: #606266;
  }
</style>
