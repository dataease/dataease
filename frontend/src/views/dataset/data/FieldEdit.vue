<template>
  <el-row>
    <el-row style="height: 26px;">
      <span style="line-height: 26px;">
        {{ $t('dataset.field_edit') }}
        <span>{{ param.table.name }}</span>
      </span>
      <el-row style="float: right">
        <el-button size="mini" @click="closeEdit">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveEdit">{{ $t('dataset.confirm') }}</el-button>
      </el-row>
    </el-row>
    <el-divider />
    <el-table :data="tableFields" size="mini" :max-height="maxHeight">
      <el-table-column property="type" :label="$t('dataset.field_type')" width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.deType === 0">
            <svg-icon v-if="scope.row.deType === 0" icon-class="field_text" class="field-icon-text" />
            <span class="field-class">{{ $t('dataset.text') }}</span>
          </span>
          <span v-if="scope.row.deType === 1">
            <svg-icon v-if="scope.row.deType === 1" icon-class="field_time" class="field-icon-time" />
            <span class="field-class">{{ $t('dataset.time') }}</span>
          </span>
          <span v-if="scope.row.deType === 2 || scope.row.deType === 3">
            <svg-icon v-if="scope.row.deType === 2 || scope.row.deType === 3" icon-class="field_value" class="field-icon-value" />
            <span class="field-class">{{ $t('dataset.value') }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
        <template slot-scope="scope">
          <el-input v-model="scope.row.name" size="mini" />
        </template>
      </el-table-column>
      <el-table-column property="originName" :label="$t('dataset.field_origin_name')" width="180" />
      <el-table-column property="checked" :label="$t('dataset.field_check')" width="80">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.checked" />
        </template>
      </el-table-column>
      <!--下面这一列占位-->
      <el-table-column property="" />
    </el-table>
  </el-row>
</template>

<script>
import { fieldList, batchEdit } from '@/api/dataset/dataset'
export default {
  name: 'FieldEdit',
  props: {
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      maxHeight: 'auto',
      tableFields: []
    }
  },
  watch: {

  },
  mounted() {
    window.onresize = () => {
      this.calcHeight()
    }
    this.calcHeight()
    this.initField()
  },
  methods: {
    calcHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.maxHeight = (currentHeight - 56 - 30 - 35 - 26 - 10) + 'px'
      }, 10)
    },
    initField() {
      fieldList(this.param.table.id).then(response => {
        this.tableFields = response.data
      })
    },
    saveEdit() {
      // console.log(this.tableFields)
      batchEdit(this.tableFields).then(response => {
        this.closeEdit()
      })
    },

    closeEdit() {
      this.$emit('switchComponent', { name: 'ViewTable', param: this.param.table.id })
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0;
  }
  span{
    font-size: 14px;
  }
  .field-class{
    font-size: 12px !important;
  }
</style>
