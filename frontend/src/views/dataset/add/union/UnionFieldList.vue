<template>
  <div class="field-block-style">
    <div class="field-block-option">
      <span class="option-field"
        >{{ $t('dataset.field_select') }}({{ checkedFields.length }}/{{
          fieldList.length
        }})</span
      >
      <el-input
        v-model="search"
        size="small"
        :placeholder="$t('auth.search_by_field')"
        prefix-icon="el-icon-search"
        clearable
        class="option-input"
      />
    </div>
    <div class="field-block-head">
      <el-checkbox
        v-model="checkAll"
        :indeterminate="isIndeterminate"
        class="check-style"
        @change="handleCheckAllChange"
        >&nbsp;</el-checkbox
      >
      <span class="label-style">
        <span class="field-origin-style">{{ $t('panel.column_name') }}</span>
        <span class="field-style">{{ $t('deDataset.original_name') }}</span>
      </span>
    </div>
    <div class="field-block-body">
      <el-checkbox-group
        v-model="checkedFields"
        @change="handleCheckedCitiesChange"
      >
        <div
          v-for="field in fieldSearchList"
          :key="field.id"
          class="field-item-style"
        >
          <el-checkbox :label="field.id" class="check-style"
            >&nbsp;</el-checkbox
          >
          <span class="label-style">
            <span class="field-origin-style value" :title="field.originName">
              <span>
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
              </span>
              <span class="origin-style">
                {{ field.originName }}
              </span>
            </span>
            <span class="field-style value" :title="field.name">{{
              field.name
            }}</span>
          </span>
        </div>
      </el-checkbox-group>
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
    fieldList: function () {
      this.fieldSearchList = JSON.parse(JSON.stringify(this.fieldList))
      this.init()
    },
    search: function (val) {
      if (val && val !== '') {
        this.fieldSearchList = JSON.parse(
          JSON.stringify(this.fieldList)
        ).filter((ele) =>
          ele.originName.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        )
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
      this.checkedFields = val ? this.fieldList.map((ele) => ele.id) : []
      this.isIndeterminate = false
      this.returnCheckedFields()
    },
    handleCheckedCitiesChange(value) {
      const checkedCount = value.length
      this.checkAll = checkedCount === this.fieldList.length
      this.isIndeterminate =
        checkedCount > 0 && checkedCount < this.fieldList.length
      this.returnCheckedFields()
    },

    returnCheckedFields() {
      this.$emit('checkedFields', this.checkedFields)
    }
  }
}
</script>

<style lang="scss" scoped>
.field-block-style {
  height: 100%;
  width: 100%;
  font-family: PingFang SC;
  .field-block-head {
    height: 46px;
    display: flex;
    align-items: center;
    background: rgba(0, 0, 0, 0.05);
    border-top: 1px solid rgba(31, 35, 41, 0.15);
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }
  .field-block-body {
    height: 281px;
    overflow-y: auto;
  }
  .field-origin-style {
    display: flex;
    margin-left: 12px;
    width: 140px;
    align-items: center;
    font-size: 14px;
    font-weight: 500;
    color: var(--deTextSecondary, #646a73);
  }
  .field-style {
    width: 140px;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: pre;
    font-size: 14px;
    font-weight: 500;
    color: var(--deTextSecondary, #646a73);
  }
  .label-style {
    display: flex;
    align-items: center;
  }
  .field-item-style {
    width: 100%;
    height: 46px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }
  .check-style {
    width: 52px;
    text-align: center;
    ::v-deep.el-checkbox__label {
      display: none;
    }
  }
  .origin-style {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: inline-block;
  }
  .field-block-option {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }
  .option-field {
    font-size: 14px;
    font-weight: 400;
    color: var(--deTextSecondary, #646a73);
  }
  .option-input {
    width: 200px;
  }

  .value {
    font-weight: 400;
    color: var(--deTextPrimary, #1f2329);
  }
}
</style>
