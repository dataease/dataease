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
      <el-table-column property="deType" :label="$t('dataset.field_type')" width="140">
        <template slot-scope="scope">
          <el-select v-model="scope.row.deType" size="mini" style="display: inline-block;width: 26px;">
            <el-option
              v-for="item in fields"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <span style="float: left">
                <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />
                <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />
                <svg-icon v-if="item.value === 2 || item.value === 3" icon-class="field_value" class="field-icon-value" />
                <svg-icon v-if="item.value === 5" icon-class="field_location" class="field-icon-location" />
              </span>
              <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>
            </el-option>
          </el-select>
          <span style="margin-left: 8px;">
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
              <span v-if="scope.row.deType === 2" class="field-class">{{ $t('dataset.value') }}</span>
              <span v-if="scope.row.deType === 3" class="field-class">{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
            </span>
            <span v-if="scope.row.deType === 5">
              <svg-icon v-if="scope.row.deType === 5" icon-class="field_location" class="field-icon-location" />
              <span class="field-class">{{ $t('dataset.location') }}</span>
            </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column property="deExtractType" :label="$t('dataset.origin_field_type')" width="100">
        <template slot-scope="scope">
          <span>
            <span v-if="scope.row.deExtractType === 0">
              <svg-icon v-if="scope.row.deExtractType === 0" icon-class="field_text" class="field-icon-text" />
              <span class="field-class">{{ $t('dataset.text') }}</span>
            </span>
            <span v-if="scope.row.deExtractType === 1">
              <svg-icon v-if="scope.row.deExtractType === 1" icon-class="field_time" class="field-icon-time" />
              <span class="field-class">{{ $t('dataset.time') }}</span>
            </span>
            <span v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4">
              <svg-icon v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4" icon-class="field_value" class="field-icon-value" />
              <span v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 4" class="field-class">{{ $t('dataset.value') }}</span>
              <span v-if="scope.row.deExtractType === 3" class="field-class">{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
            </span>
            <span v-if="scope.row.deExtractType === 5">
              <svg-icon v-if="scope.row.deExtractType === 5" icon-class="field_location" class="field-icon-location" />
              <span class="field-class">{{ $t('dataset.location') }}</span>
            </span>
          </span>
        </template>
      </el-table-column>
      <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
        <template slot-scope="scope">
          <el-input v-model="scope.row.name" size="mini" />
        </template>
      </el-table-column>
      <el-table-column v-if="!(param.table.mode === 0 && param.table.type === 'custom')" property="originName" :label="$t('dataset.field_origin_name')" width="100">
        <template slot-scope="scope">
          <span :title="scope.row.originName" class="field-class" style="width: 100%;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
            <span style="font-size: 12px;">{{ scope.row.originName }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column property="groupType" :label="$t('dataset.field_group_type')" width="180">
        <template slot-scope="scope">
          <el-radio-group v-model="scope.row.groupType" size="mini">
            <el-radio-button label="d">{{ $t('chart.dimension') }}</el-radio-button>
            <el-radio-button label="q">{{ $t('chart.quota') }}</el-radio-button>
          </el-radio-group>
        </template>
      </el-table-column>
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
      tableFields: [],
      fields: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
        { label: this.$t('dataset.location'), value: 5 }
      ]
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
      this.$emit('switchComponent', { name: 'ViewTable', param: this.param.table })
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
  .el-select>>>input{
    padding-right: 10px;
  }
  .el-select>>>.el-input__suffix{
    right: 0;
  }
  .el-radio{
    margin-right: 10px !important;
  }
</style>
