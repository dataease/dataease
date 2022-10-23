<template>
  <el-col>
    <div v-if="item.deType === 0 || item.deType === 5">
      <el-radio-group
        v-model="filterType"
        size="mini"
        style="margin-bottom: 10px;"
        @change="filterTypeChange"
      >
        <el-radio label="logic">{{ $t('chart.logic_exp') }}</el-radio>
        <el-radio label="enum">{{ $t('chart.enum_exp') }}</el-radio>
      </el-radio-group>
    </div>

    <div v-if="((item.deType === 0 || item.deType === 5) && filterType === 'logic') || item.deType === 1 || item.deType === 2 || item.deType === 3">
      <div style="display: inline-block;">
        <el-button
          icon="el-icon-plus"
          circle
          size="mini"
          style="margin-bottom: 10px;"
          @click="addFilter"
        />
        <el-radio-group
          v-show="item.filter && item.filter.length > 1"
          v-model="logic"
          size="mini"
          style="margin-left: 10px;"
          @change="logicChange"
        >
          <el-radio-button label="and">{{ $t('chart.and') }}</el-radio-button>
          <el-radio-button label="or">{{ $t('chart.or') }}</el-radio-button>
        </el-radio-group>
      </div>
      <div style="max-height: 50vh;overflow-y: auto;">
        <el-row
          v-for="(f,index) in item.filter"
          :key="index"
          class="filter-item"
        >
          <el-col :span="4">
            <span>{{ item.name }}</span>
          </el-col>
          <el-col :span="8">
            <el-select
              v-model="f.term"
              size="mini"
            >
              <el-option-group
                v-for="(group,idx) in options"
                :key="idx"
                :label="group.label"
              >
                <el-option
                  v-for="opt in group.options"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-option-group>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-input
              v-show="!f.term.includes('null') && !f.term.includes('empty')"
              v-model="f.value"
              class="value-item"
              :placeholder="$t('chart.condition')"
              size="mini"
              clearable
            />
          </el-col>
          <el-col :span="6">
            <el-button
              type="text"
              icon="el-icon-delete"
              circle
              style="float: right"
              @click="removeFilter(index)"
            />
          </el-col>
        </el-row>
      </div>
    </div>

    <div v-if="(item.deType === 0 || item.deType === 5) && filterType === 'enum'">
      <span style="margin-right: 10px;">{{ $t('chart.filter_exp') }}</span>
      <el-select
        v-model="enumCheckField"
        filterable
        collapse-tags
        multiple
        :placeholder="$t('chart.pls_slc')"
        size="mini"
        @change="enumChange"
      >
        <el-option
          v-for="field in fieldOptions"
          :key="field.id"
          :label="field.text"
          :value="field.id"
        />
      </el-select>
    </div>
  </el-col>
</template>

<script>
import { multFieldValues } from '@/api/dataset/dataset'

export default {
  name: 'ResultFilterEditor',
  props: {
    chart: {
      type: Object,
      required: true
    },
    item: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      textOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'like',
            label: this.$t('chart.filter_like')
          }, {
            value: 'not like',
            label: this.$t('chart.filter_not_like')
          }]
        },
        {
          label: '',
          options: [{
            value: 'null',
            label: this.$t('chart.filter_null')
          }, {
            value: 'not_null',
            label: this.$t('chart.filter_not_null')
          }]
        },
        {
          label: '',
          options: [{
            value: 'empty',
            label: this.$t('chart.filter_empty')
          }, {
            value: 'not_empty',
            label: this.$t('chart.filter_not_empty')
          }]
        }
      ],
      dateOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'lt',
            label: this.$t('chart.filter_lt')
          }, {
            value: 'gt',
            label: this.$t('chart.filter_gt')
          }]
        },
        {
          label: '',
          options: [{
            value: 'le',
            label: this.$t('chart.filter_le')
          }, {
            value: 'ge',
            label: this.$t('chart.filter_ge')
          }]
        }
      ],
      valueOptions: [
        {
          label: '',
          options: [{
            value: 'eq',
            label: this.$t('chart.filter_eq')
          }, {
            value: 'not_eq',
            label: this.$t('chart.filter_not_eq')
          }]
        },
        {
          label: '',
          options: [{
            value: 'lt',
            label: this.$t('chart.filter_lt')
          }, {
            value: 'gt',
            label: this.$t('chart.filter_gt')
          }]
        },
        {
          label: '',
          options: [{
            value: 'le',
            label: this.$t('chart.filter_le')
          }, {
            value: 'ge',
            label: this.$t('chart.filter_ge')
          }]
        }
      ],
      options: [],
      logic: '',
      filterType: '',
      enumCheckField: [],
      fieldOptions: []
    }
  },
  watch: {
    'item': function() {
      this.initOptions()
    }
  },
  mounted() {
    this.initOptions()
    this.init()
    this.initEnumOptions()
  },
  methods: {
    initOptions() {
      if (this.item) {
        if (this.item.deType === 0 || this.item.deType === 5) {
          this.options = JSON.parse(JSON.stringify(this.textOptions))
        } else if (this.item.deType === 1) {
          this.options = JSON.parse(JSON.stringify(this.dateOptions))
        } else {
          this.options = JSON.parse(JSON.stringify(this.valueOptions))
        }
      }
    },
    init() {
      this.logic = this.item.logic
      this.filterType = this.item.filterType
      this.enumCheckField = this.item.enumCheckField
    },
    initEnumOptions() {
      // 查找枚举值
      if (this.item.deType === 0 || this.item.deType === 5) {
        multFieldValues({ fieldIds: [this.item.id] }).then(res => {
          this.fieldOptions = this.optionData(res.data)
        })
      }
    },
    optionData(data) {
      if (!data) return null
      return data.filter(item => !!item).map(item => {
        return {
          id: item,
          text: item
        }
      })
    },
    addFilter() {
      this.item.filter.push({
        fieldId: this.item.id,
        term: 'eq',
        value: ''
      })
    },
    removeFilter(index) {
      this.item.filter.splice(index, 1)
    },
    logicChange(val) {
      this.item.logic = val
    },
    filterTypeChange(val) {
      this.item.filterType = val
    },
    enumChange(val) {
      this.item.enumCheckField = this.enumCheckField
    }
  }
}
</script>

<style scoped>
  .filter-item{
    width: 100%;
    border-radius: 4px;
    border: 1px solid #DCDFE6;
    padding: 4px 14px;
    margin-bottom: 10px;
    display: flex;
    justify-content: left;
    align-items: center;
  }
  .form-item ::v-deep .el-form-item__label{
    font-size: 12px;
  }
  span{
    font-size: 12px;
  }

  .value-item ::v-deep .el-input{
    position: relative;
    display: inline-block;
    width: 80px!important;
  }
</style>
