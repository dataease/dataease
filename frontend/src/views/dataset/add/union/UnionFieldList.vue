<template>
  <div>
    <div class="field-block-style">
      <div class="field-block-option">
        <span class="option-field">{{ $t('dataset.field_select') }}({{ checkedFields.length }}/{{ fieldList.length }})</span>
        <el-input
          v-model="search"
          size="mini"
          :placeholder="$t('dataset.search')"
          prefix-icon="el-icon-search"
          clearable
          class="option-input"
        />
      </div>
      <div class="field-block-head">
        <el-checkbox v-model="checkAll" :indeterminate="isIndeterminate" class="check-style" @change="handleCheckAllChange">&nbsp;</el-checkbox>
        <span class="label-style">
          <span class="field-origin-style">{{ $t('dataset.field_origin_name') }}</span>
          <span class="field-style">{{ $t('dataset.field_name') }}</span>
        </span>
      </div>
      <div class="field-block-body">
        <el-checkbox-group v-model="checkedFields" @change="handleCheckedCitiesChange">
          <div v-for="field in fieldSearchList" :key="field.id" class="field-item-style">
            <el-checkbox :label="field.id" class="check-style">&nbsp;</el-checkbox>
            <span class="label-style">
              <span class="field-origin-style" :title="field.originName">
                <span>
                  <svg-icon v-if="field.deType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="field.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon v-if="field.deType === 2 || field.deType === 3" icon-class="field_value" class="field-icon-value" />
                  <svg-icon v-if="field.deType === 5" icon-class="field_location" class="field-icon-location" />
                </span>
                <span class="origin-style">
                  {{ field.originName }}
                </span>
              </span>
              <span class="field-style" :title="field.name">{{ field.name }}</span>
            </span>
          </div>
        </el-checkbox-group>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UnionFieldList',
  props: {
    fieldList: {
      type: Array,
      required: true
    },
    node: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      checkAll: false,
      isIndeterminate: false,
      checkedFields: [],
      search: '',
      fieldSearchList: []
    }
  },
  watch: {
    fieldList: function() {
      this.fieldSearchList = JSON.parse(JSON.stringify(this.fieldList))
      this.init()
    },
    search: function(val) {
      if (val && val !== '') {
        this.fieldSearchList = JSON.parse(JSON.stringify(this.fieldList)).filter(ele => ele.originName.toLocaleLowerCase().includes(val.toLocaleLowerCase()))
      } else {
        this.fieldSearchList = JSON.parse(JSON.stringify(this.fieldList))
      }
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      this.checkedFields = this.node.currentDsField
      this.handleCheckedCitiesChange(this.checkedFields)
    },
    handleCheckAllChange(val) {
      this.checkedFields = val ? this.fieldList.map(ele => ele.id) : []
      this.isIndeterminate = false
      this.returnCheckedFields()
    },
    handleCheckedCitiesChange(value) {
      const checkedCount = value.length
      this.checkAll = checkedCount === this.fieldList.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.fieldList.length
      this.returnCheckedFields()
    },

    returnCheckedFields() {
      this.$emit('checkedFields', this.checkedFields)
    }
  }
}
</script>

<style scoped>
span{
  font-size: 12px;
}
.field-block-style{
  height: 300px;
  width: 100%;
}
.field-block-head{
  height: 30px;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.05);
  padding: 0 0 0 10px;
}
.field-block-body{
  height: 210px;
  overflow-y: auto;
}
.field-origin-style{
  width: 100px;
  display: flex;
  align-items: center;
}
.field-style{
  margin-left: 10px;
  width: 100px;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: pre;
}
.label-style{
  display: flex;
  align-items: center;
}
.field-item-style{
  width: 100%;
  height: 30px;
  display: flex;
  align-items: center;
  border-top:1px solid #dcdfe6;
  padding: 0 0 0 10px;
}
.check-style{
  width: 30px;
  text-align: center;
}
.origin-style{
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
.field-block-option{
  width: 100%;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.option-field{
  color: #C0C4CC;
}
.option-input{
  width: 120px;
}
</style>
