<template>
  <el-col>
    <div class="table-count">
      <span class="title-text" style="width: 100px"
        >{{ $t('deDataset.display') }} {{ form.row }}
        {{ $t('deDataset.row') }}</span
      >
      <el-popover
        popper-class="de-set-count de-card-dropdown"
        placement="right-start"
        width="306"
        ref="setCount"
        trigger="click"
      >
        {{ $t('deDataset.show_rows') }}
        <el-input size="small" v-model="rowNum"> </el-input>
        <div class="foot">
          <deBtn @click="cancel" secondary>{{ $t('commons.cancel') }} </deBtn>
          <deBtn type="primary" @click="searchRow">
            {{ $t('commons.confirm') }}
          </deBtn>
        </div>
        <i slot="reference" class="el-icon-edit"></i>
      </el-popover>
    </div>
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
        :key="field.id"
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
      rowNum: parseInt(this.form.row),
      currentPage: {
        page: 1,
        pageSize: parseInt(this.form.row),
        show: parseInt(this.form.row)
      }
    }
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
    searchRow() {
      this.form.row = this.rowNum
      this.reSearch()
    },
    init() {
      this.calHeight()
    },
    cancel() {
      this.$refs.setCount.doClose()
    },
    calHeight() {
      const that = this
      setTimeout(function () {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 215
      }, 10)
    },
    reSearch() {
      if (
        !this.form.row ||
        this.form.row === '' ||
        this.form.row.length > 4 ||
        isNaN(Number(this.form.row)) ||
        String(this.form.row).includes('.') ||
        parseInt(this.form.row) < 1
      ) {
        this.$message({
          message: this.$t('dataset.pls_input_less_5'),
          type: 'error',
          showClose: true
        })
        return
      }
      this.currentPage.show = parseInt(this.form.row)
      this.currentPage.pageSize = parseInt(this.form.row)
      this.currentPage.page = 1
      this.$emit('reSearch', { form: this.form, page: this.currentPage })
    },
    pageChange(val) {
      this.currentPage.page = val
      this.$emit('reSearch', { form: this.form, page: this.currentPage })
    }
  }
}
</script>
<style lang="scss" scoped>
.table-count {
  color: var(--deTextSecondary, #606266);
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  margin: 4px 0;
  .title-text {
    margin: 0 5px 0 0;
  }
}
</style>
<style lang="scss">
.de-set-count {
  padding: 20px 24px;
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  color: var(--deTextPrimary, #1f2329);
  text-align: left;

  .el-input {
    margin: 8px 0 20px 0;
  }

  .de-button {
    min-width: 48px;
    height: 28px;
  }
}
</style>
