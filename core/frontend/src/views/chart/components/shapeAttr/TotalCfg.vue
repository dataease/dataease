<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="totalForm"
        :model="totalForm"
        label-width="80px"
        size="mini"
      >
        <el-divider
          v-if="showProperty('row')"
          content-position="center"
          class="divider-style"
        >{{ $t('chart.row_cfg') }}</el-divider>
        <el-form-item
          v-show="showProperty('row')"
          :label="$t('chart.total_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="totalForm.row.showGrandTotals"
            @change="changeTotalCfg('row')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="showProperty('row') && totalForm.row.showGrandTotals">
          <el-form-item
            :label="$t('chart.total_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.row.reverseLayout"
              @change="changeTotalCfg('row')"
            >
              <el-radio :label="true">{{ $t('chart.total_pos_top') }}</el-radio>
              <el-radio :label="false">{{ $t('chart.total_pos_bottom') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('chart.total_label')"
            class="form-item"
          >
            <el-input
              v-model="totalForm.row.label"
              style="width: 160px;"
              :placeholder="$t('chart.total_label')"
              size="mini"
              clearable
              @change="changeTotalCfg('row')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.aggregation')"
            class="form-item"
          >
            <el-col :span="10">
              <el-select
                v-model="rowTotalItem.dataeaseName"
                :placeholder="$t('chart.aggregation')"
                popper-class="total-select"
                size="mini"
                @change="changeTotal(rowTotalItem, totalForm.row.calcTotals.cfg)"
              >
                <el-option
                  v-for="option in totalSortFields"
                  :key="option.dataeaseName"
                  :label="option.name"
                  :value="option.dataeaseName"
                />
              </el-select>
            </el-col>
            <el-col
              :span="10"
              :offset="1"
            >
              <el-select
                v-model="rowTotalItem.aggregation"
                :placeholder="$t('chart.aggregation')"
                popper-class="total-select"
                size="mini"
                @change="changeTotalAggr(rowTotalItem, totalForm.row.calcTotals.cfg, 'row')"
              >
                <el-option
                  v-for="option in aggregations"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item
            v-if="chart.type === 'table-pivot'"
            :label="$t('chart.total_sort')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.row.totalSort"
              @change="changeTotalCfg('row')"
            >
              <el-radio label="none">{{ $t('chart.total_sort_none') }}</el-radio>
              <el-radio label="asc">{{ $t('chart.total_sort_asc') }}</el-radio>
              <el-radio label="desc">{{ $t('chart.total_sort_desc') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-if="chart.type === 'table-pivot' && totalForm.row.totalSort !== 'none'"
            :label="$t('chart.total_sort_field')"
            class="form-item"
          >
            <el-select
              v-model="totalForm.row.totalSortField"
              class="form-item-select"
              popper-class="total-select"
              :placeholder="$t('chart.total_sort_field')"
              size="mini"
              @change="changeTotalCfg('row')"
            >
              <el-option
                v-for="option in totalSortFields"
                :key="option.dataeaseName"
                :label="option.name"
                :value="option.dataeaseName"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item
          v-show="showProperty('row')"
          :label="$t('chart.sub_total_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="totalForm.row.showSubTotals"
            :disabled="rowNum < 2"
            @change="changeTotalCfg('row')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="showProperty('row') && totalForm.row.showSubTotals">
          <el-form-item
            :label="$t('chart.total_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.row.reverseSubLayout"
              :disabled="rowNum < 2"
              @change="changeTotalCfg('row')"
            >
              <el-radio :label="true">{{ $t('chart.total_pos_top') }}</el-radio>
              <el-radio :label="false">{{ $t('chart.total_pos_bottom') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('chart.total_label')"
            class="form-item"
          >
            <el-input
              v-model="totalForm.row.subLabel"
              :disabled="rowNum < 2"
              style="width: 160px;"
              :placeholder="$t('chart.total_label')"
              size="mini"
              clearable
              @change="changeTotalCfg"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.aggregation')"
            class="form-item"
          >
            <el-col :span="10">
              <el-select
                v-model="rowSubTotalItem.dataeaseName"
                :disabled="rowNum < 2"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotal(rowSubTotalItem, totalForm.row.calcSubTotals.cfg)"
              >
                <el-option
                  v-for="option in totalSortFields"
                  :key="option.dataeaseName"
                  :label="option.name"
                  :value="option.dataeaseName"
                />
              </el-select>
            </el-col>
            <el-col
              :span="10"
              :offset="1"
            >
              <el-select
                v-model="rowSubTotalItem.aggregation"
                :disabled="rowNum < 2"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotalAggr(rowSubTotalItem, totalForm.row.calcSubTotals.cfg, 'row')"
              >
                <el-option
                  v-for="option in aggregations"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-col>
          </el-form-item>
        </div>

        <el-divider
          v-if="showProperty('col')"
          content-position="center"
          class="divider-style"
        >{{ $t('chart.col_cfg') }}</el-divider>
        <el-form-item
          v-show="showProperty('col')"
          :label="$t('chart.total_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="totalForm.col.showGrandTotals"
            @change="changeTotalCfg('col')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="showProperty('col') && totalForm.col.showGrandTotals">
          <el-form-item
            :label="$t('chart.total_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.col.reverseLayout"
              @change="changeTotalCfg('col')"
            >
              <el-radio :label="true">{{ $t('chart.total_pos_left') }}</el-radio>
              <el-radio :label="false">{{ $t('chart.total_pos_right') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('chart.total_label')"
            class="form-item"
          >
            <el-input
              v-model="totalForm.col.label"
              style="width: 160px;"
              :placeholder="$t('chart.total_label')"
              size="mini"
              clearable
              @change="changeTotalCfg('col')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.aggregation')"
            class="form-item"
          >
            <el-col :span="10">
              <el-select
                v-model="colTotalItem.dataeaseName"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotal(colTotalItem, totalForm.col.calcTotals.cfg)"
              >
                <el-option
                  v-for="option in totalSortFields"
                  :key="option.dataeaseName"
                  :label="option.name"
                  :value="option.dataeaseName"
                />
              </el-select>
            </el-col>
            <el-col
              :span="10"
              :offset="1"
            >
              <el-select
                v-model="colTotalItem.aggregation"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotalAggr(colTotalItem, totalForm.col.calcTotals.cfg, 'col')"
              >
                <el-option
                  v-for="option in aggregations"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-col>
          </el-form-item>
          <el-form-item
            v-if="chart.type === 'table-pivot'"
            :label="$t('chart.total_sort')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.col.totalSort"
              @change="changeTotalCfg('col')"
            >
              <el-radio label="none">{{ $t('chart.total_sort_none') }}</el-radio>
              <el-radio label="asc">{{ $t('chart.total_sort_asc') }}</el-radio>
              <el-radio label="desc">{{ $t('chart.total_sort_desc') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            v-show="false && chart.type === 'table-pivot' && totalForm.col.totalSort !== 'none'"
            :label="$t('chart.total_sort_field')"
            class="form-item"
          >
            <el-select
              v-model="totalForm.col.totalSortField"
              class="form-item-select"
              popper-class="total-select"
              :placeholder="$t('chart.total_sort_field')"
              size="mini"
              @change="changeTotalCfg('col')"
            >
              <el-option
                v-for="option in totalSortFields"
                :key="option.dataeaseName"
                :label="option.name"
                :value="option.dataeaseName"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item
          v-show="showProperty('col')"
          :label="$t('chart.sub_total_show')"
          class="form-item"
        >
          <el-checkbox
            v-model="totalForm.col.showSubTotals"
            :disabled="colNum < 2"
            @change="changeTotalCfg('col')"
          >{{ $t('chart.show') }}</el-checkbox>
        </el-form-item>
        <div v-show="showProperty('col') && totalForm.col.showSubTotals">
          <el-form-item
            :label="$t('chart.total_position')"
            class="form-item"
          >
            <el-radio-group
              v-model="totalForm.col.reverseSubLayout"
              :disabled="colNum < 2"
              @change="changeTotalCfg('col')"
            >
              <el-radio :label="true">{{ $t('chart.total_pos_left') }}</el-radio>
              <el-radio :label="false">{{ $t('chart.total_pos_right') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            :label="$t('chart.total_label')"
            class="form-item"
          >
            <el-input
              v-model="totalForm.col.subLabel"
              :disabled="colNum < 2"
              style="width: 160px;"
              :placeholder="$t('chart.total_label')"
              size="mini"
              clearable
              @change="changeTotalCfg('col')"
            />
          </el-form-item>
          <el-form-item
            :label="$t('chart.aggregation')"
            class="form-item"
          >
            <el-col :span="10">
              <el-select
                v-model="colSubTotalItem.dataeaseName"
                :disabled="colNum < 2"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotal(colSubTotalItem, totalForm.col.calcSubTotals.cfg)"
              >
                <el-option
                  v-for="option in totalSortFields"
                  :key="option.dataeaseName"
                  :label="option.name"
                  :value="option.dataeaseName"
                />
              </el-select>
            </el-col>
            <el-col
              :span="10"
              :offset="1"
            >
              <el-select
                v-model="colSubTotalItem.aggregation"
                :disabled="colNum < 2"
                :placeholder="$t('chart.aggregation')"
                size="mini"
                popper-class="total-select"
                @change="changeTotalAggr(colSubTotalItem, totalForm.col.calcSubTotals.cfg, 'col')"
              >
                <el-option
                  v-for="option in aggregations"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-col>
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script>
import { DEFAULT_TOTAL } from '@/views/chart/chart/chart'

export default {
  name: 'TotalCfg',
  props: {
    chart: {
      type: Object,
      required: true
    },
    propertyInner: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      totalForm: JSON.parse(JSON.stringify(DEFAULT_TOTAL)),
      aggregations: [
        { name: this.$t('chart.sum'), value: 'SUM' },
        { name: this.$t('chart.avg'), value: 'AVG' },
        { name: this.$t('chart.max'), value: 'MAX' },
        { name: this.$t('chart.min'), value: 'MIN' }
      ],
      totalSortFields: [],
      rowSubTotalItem: {
        dataeaseName: '',
        aggregation: ''
      },
      rowTotalItem: {
        dataeaseName: '',
        aggregation: ''
      },
      colSubTotalItem: {
        dataeaseName: '',
        aggregation: ''
      },
      colTotalItem: {
        dataeaseName: '',
        aggregation: ''
      }
    }
  },
  computed: {
    rowNum() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.xaxisExt) {
        let arr = null
        if (Object.prototype.toString.call(chart.xaxisExt) === '[object Array]') {
          arr = JSON.parse(JSON.stringify(chart.xaxisExt))
        } else {
          arr = JSON.parse(chart.xaxisExt)
        }
        return arr.length
      }
      return 0
    },
    colNum() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.xaxis) {
        let arr = null
        if (Object.prototype.toString.call(chart.xaxis) === '[object Array]') {
          arr = JSON.parse(JSON.stringify(chart.xaxis))
        } else {
          arr = JSON.parse(chart.xaxis)
        }
        return arr.length
      }
      return 0
    }
  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.customAttr) {
        let customAttr = null
        if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
          customAttr = JSON.parse(JSON.stringify(chart.customAttr))
        } else {
          customAttr = JSON.parse(chart.customAttr)
        }
        if (customAttr.totalCfg) {
          this.totalForm = customAttr.totalCfg
        } else {
          this.totalForm = JSON.parse(JSON.stringify(DEFAULT_TOTAL))
        }

        this.totalForm.row.totalSort = this.totalForm.row.totalSort ? this.totalForm.row.totalSort : DEFAULT_TOTAL.row.totalSort
        this.totalForm.row.totalSortField = this.totalForm.row.totalSortField ? this.totalForm.row.totalSortField : DEFAULT_TOTAL.row.totalSortField
        this.totalForm.col.totalSort = this.totalForm.col.totalSort ? this.totalForm.col.totalSort : DEFAULT_TOTAL.col.totalSort
        this.totalForm.col.totalSortField = this.totalForm.col.totalSortField ? this.totalForm.col.totalSortField : DEFAULT_TOTAL.col.totalSortField
      }
      // 解析表格的指标
      if (chart.yaxis) {
        if (Object.prototype.toString.call(chart.yaxis) === '[object Array]') {
          this.totalSortFields = JSON.parse(JSON.stringify(chart.yaxis))
        } else {
          this.totalSortFields = JSON.parse(chart.yaxis)
        }
        if (this.totalSortFields.length > 0) {
          if (this.resetTotalSort(this.totalForm.row.totalSortField)) {
            this.totalForm.row.totalSortField = this.totalSortFields[0].dataeaseName
          }
          this.totalForm.col.totalSortField = this.totalSortFields[0].dataeaseName
        } else {
          this.totalForm.row.totalSortField = ''
          this.totalForm.col.totalSortField = ''
        }
        let needCompatible = false
        if (!this.totalForm.row.calcTotals.cfg) {
          needCompatible = true
          this.$set(this.totalForm.row.calcTotals, 'cfg', [])
          this.$set(this.totalForm.row.calcSubTotals, 'cfg', [])
          this.$set(this.totalForm.col.calcTotals, 'cfg', [])
          this.$set(this.totalForm.col.calcSubTotals, 'cfg', [])
        }
        const totals = [
          { ...this.totalForm.row.calcTotals },
          { ...this.totalForm.row.calcSubTotals },
          { ...this.totalForm.col.calcTotals },
          { ...this.totalForm.col.calcSubTotals }
        ]
        totals.forEach(total => {
          // 兼容原有的聚合方式
          const aggregation = needCompatible ? total.aggregation : 'SUM'
          this.setupTotalCfg(total.cfg, this.totalSortFields, aggregation)
        })
        const totalTupleArr = [
          [this.rowTotalItem, this.totalForm.row.calcTotals.cfg],
          [this.rowSubTotalItem, this.totalForm.row.calcSubTotals.cfg],
          [this.colTotalItem, this.totalForm.col.calcTotals.cfg],
          [this.colSubTotalItem, this.totalForm.col.calcSubTotals.cfg]
        ]
        totalTupleArr.forEach(tuple => {
          const [total, totalCfg] = tuple
          if (!totalCfg.length) {
            total.dataeaseName = ''
            total.aggregation = ''
            return
          }
          const totalIndex = totalCfg.findIndex(i => i.dataeaseName === total.dataeaseName)
          if (totalIndex !== -1) {
            total.aggregation = totalCfg[totalIndex].aggregation
          } else {
            total.dataeaseName = totalCfg[0].dataeaseName
            total.aggregation = totalCfg[0].aggregation
          }
        })
      }
    },
    changeTotal(totalItem, totals) {
      for (let i = 0; i < totals.length; i++) {
        const item = totals[i]
        if (item.dataeaseName === totalItem.dataeaseName) {
          totalItem.aggregation = item.aggregation
          return
        }
      }
    },
    changeTotalAggr(totalItem, totals, colOrNum) {
      for (let i = 0; i < totals.length; i++) {
        const item = totals[i]
        if (item.dataeaseName === totalItem.dataeaseName) {
          item.aggregation = totalItem.aggregation
          break
        }
      }
      this.changeTotalCfg(colOrNum)
    },
    changeTotalCfg(modifyName) {
      this.totalForm['modifyName'] = modifyName
      this.$emit('onTotalCfgChange', this.totalForm)
    },
    showProperty(property) {
      return this.propertyInner.includes(property)
    },
    resetTotalSort(field) {
      if (field === '') {
        return true
      }
      const sortFieldList = []
      this.totalSortFields.forEach(ele => {
        sortFieldList.push(ele.dataeaseName)
      })
      return sortFieldList.indexOf(field) === -1
    },
    setupTotalCfg(totalCfg, axis, aggregation) {
      if (!totalCfg.length) {
        axis.forEach(i => {
          totalCfg.push({
            dataeaseName: i.dataeaseName,
            aggregation
          })
        })
        return
      }
      if (!axis.length) {
        totalCfg.splice(0, totalCfg.length)
        return
      }
      const cfgMap = totalCfg.reduce((p, n) => {
        p[n.dataeaseName] = n
        return p
      }, {})
      totalCfg.splice(0, totalCfg.length)
      axis.forEach(i => {
        totalCfg.push({
          dataeaseName: i.dataeaseName,
          aggregation: cfgMap[i.dataeaseName] ? cfgMap[i.dataeaseName].aggregation : 'SUM'
        })
      })
    }
  }
}
</script>
<style>
.total-select .el-select-dropdown__item {
  font-size: 12px;
}
</style>
<style scoped>
.shape-item{
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider ::v-deep .el-form-item__label{
  font-size: 12px;
  line-height: 38px;
}
.form-item ::v-deep .el-form-item__label{
  font-size: 12px;
}

.form-item ::v-deep .el-checkbox__label {
  font-size: 12px;
}
.form-item ::v-deep .el-radio__label {
  font-size: 12px;
}

.el-select-dropdown__item{
  padding: 0 20px;
}
span{font-size: 12px}

.el-form-item{
  margin-bottom: 6px;
}
.el-divider--horizontal {
  margin: 10px 0
}
.divider-style ::v-deep .el-divider__text{
  color: #606266;
  font-size: 12px;
  font-weight: 400;
  padding: 0 10px;
}

.form-item-select{
  width:160px!important;
}
</style>
