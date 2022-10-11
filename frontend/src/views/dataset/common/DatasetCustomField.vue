<template>
  <el-col>
    <el-row style="height: 25px;">
      <span class="table-name title-text">{{ table.name }}</span>
    </el-row>
    <el-divider />
    <el-checkbox
      v-model="checkAll"
      :disabled="!(fields.length > 0)"
      :indeterminate="isIndeterminate"
      style="font-weight: 400;"
      @change="handleCheckAllChange"
    >{{ $t('dataset.check_all') }}</el-checkbox>
    <el-checkbox-group
      v-model="checkedFields"
      @change="handleCheckedFieldsChange"
    >
      <el-checkbox
        v-for="f in fields"
        :key="f.id"
        :label="f.id"
        style="display: block;margin-top: 4px;width: 100%;font-weight: 400;"
      >
        <span style="display: flex;flex-direction: row;flex: 1;">
          <span>
            <span v-if="f.deType === 0">
              <svg-icon
                v-if="f.deType === 0"
                icon-class="field_text"
                class="field-icon-text"
              />
            </span>
            <span v-if="f.deType === 1">
              <svg-icon
                v-if="f.deType === 1"
                icon-class="field_time"
                class="field-icon-time"
              />
            </span>
            <span v-if="f.deType === 2 || f.deType === 3">
              <svg-icon
                v-if="f.deType === 2 || f.deType === 3"
                icon-class="field_value"
                class="field-icon-value"
              />
            </span>
            <span v-if="f.deType === 5">
              <svg-icon
                v-if="f.deType === 5"
                icon-class="field_location"
                class="field-icon-location"
              />
            </span>
          </span>
          <span style="display: flex;flex: 1;width: 100%;">
            <span style="text-overflow: ellipsis;white-space: nowrap;overflow: hidden;width: 160px;">{{ f.name }}</span>
          </span>
        </span>
      </el-checkbox>
    </el-checkbox-group>
  </el-col>
</template>

<script>
import { fieldList } from '../../../api/dataset/dataset'

export default {
  name: 'DatasetCustomField',
  props: {
    table: {
      type: Object,
      required: true
    },
    checkedList: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      fields: [],
      checkAll: false,
      checkedFields: [],
      isIndeterminate: false
    }
  },
  watch: {
    'table': function() {
      fieldList(this.table.id).then(response => {
        this.fields = JSON.parse(JSON.stringify(response.data)).filter(ele => ele.extField === 0)

        this.checkedFields = []
        this.checkedList.forEach(ele => {
          if (ele.tableId === this.table.id) {
            this.checkedFields = ele.checkedFields
          }
        })
        const checkedCount = this.checkedFields.length
        this.checkAll = checkedCount === this.fields.length
        this.isIndeterminate = checkedCount > 0 && checkedCount < this.fields.length
      })
    }
  },
  mounted() {
    this.initField()
  },
  methods: {
    initField() {
      if (this.table.id) {
        fieldList(this.table.id).then(response => {
          this.fields = JSON.parse(JSON.stringify(response.data)).filter(ele => ele.extField === 0)
        })
      }
    },

    handleCheckAllChange(val) {
      this.checkedFields = val ? this.fields.map(ele => {
        return ele.id
      }) : []
      this.isIndeterminate = false
      this.getChecked()
    },
    handleCheckedFieldsChange(value) {
      const checkedCount = value.length
      this.checkAll = checkedCount === this.fields.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.fields.length
      this.getChecked()
    },

    getChecked() {
      this.$emit('getChecked', { tableId: this.table.id, checkedFields: this.checkedFields })
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0
  }

  .table-name{
    display: inline-block;
    white-space: nowrap;
    text-overflow: ellipsis;
    width: 100%;
    overflow: hidden;
  }
</style>
