<template>
  <el-row :style="{height: maxHeight,overflow:'auto'}">
    <el-row>
      <el-form :inline="true">
        <el-form-item class="form-item">
          <el-button size="mini" icon="el-icon-circle-plus-outline" @click="addCalcField">
            {{ $t('dataset.add_calc_field') }}
          </el-button>
        </el-form-item>
        <el-form-item class="form-item" style="float: right;margin-right: 0;">
          <el-input
            v-model="searchField"
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            clearable
            class="main-area-input"
          />
        </el-form-item>
      </el-form>
    </el-row>

    <el-collapse v-model="fieldActiveNames" class="style-collapse">
      <el-collapse-item name="d" :title="$t('chart.dimension')">
        <el-table :data="tableFields.dimensionListData" size="mini">
          <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.name"
                size="mini"
                @blur="saveEdit(scope.row)"
                @keyup.enter.native="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column property="deType" :label="$t('dataset.field_type')" width="140">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.deType"
                size="mini"
                style="display: inline-block;width: 26px;"
                @change="saveEdit(scope.row)"
              >
                <el-option
                  v-for="item in fields"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <span style="float: left">
                    <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />
                    <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />
                    <svg-icon
                      v-if="item.value === 2 || item.value === 3"
                      icon-class="field_value"
                      class="field-icon-value"
                    />
                    <svg-icon v-if="item.value === 5" icon-class="field_location" class="field-icon-location" />
                  </span>
                  <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>
                </el-option>
              </el-select>
              <span style="margin-left: 8px;">
                <span v-if="scope.row.deType === 0 || scope.row.deType === 6">
                  <svg-icon
                    v-if="scope.row.deType === 0 || scope.row.deType === 6"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <span class="field-class">{{ $t('dataset.text') }}</span>
                </span>
                <span v-if="scope.row.deType === 1">
                  <svg-icon v-if="scope.row.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <span class="field-class">{{ $t('dataset.time') }}</span>
                </span>
                <span v-if="scope.row.deType === 2 || scope.row.deType === 3">
                  <svg-icon
                    v-if="scope.row.deType === 2 || scope.row.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <span v-if="scope.row.deType === 2" class="field-class">{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deType === 3"
                    class="field-class"
                  >{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
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
                <span v-if="scope.row.deExtractType === 0 || scope.row.deExtractType === 6">
                  <svg-icon
                    v-if="scope.row.deExtractType === 0 || scope.row.deExtractType === 6"
                    icon-class="field_text"
                    class="field-icon-text"
                  />
                  <span class="field-class">{{ $t('dataset.text') }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 1">
                  <svg-icon v-if="scope.row.deExtractType === 1" icon-class="field_time" class="field-icon-time" />
                  <span class="field-class">{{ $t('dataset.time') }}</span>
                </span>
                <span
                  v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4"
                >
                  <svg-icon
                    v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <span
                    v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 4"
                    class="field-class"
                  >{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deExtractType === 3"
                    class="field-class"
                  >{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 5">
                  <svg-icon
                    v-if="scope.row.deExtractType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                  <span class="field-class">{{ $t('dataset.location') }}</span>
                </span>
              </span>
            </template>
          </el-table-column>
          <el-table-column property="fromType" :label="$t('chart.form_type')" width="120">
            <template slot-scope="scope">
              <span v-if="scope.row.extField === 1" class="from-type-span">{{ $t('chart.copy_field') }}</span>
              <span v-if="scope.row.extField === 2" class="from-type-span">{{ $t('chart.calc_field') }}</span>
            </template>
          </el-table-column>
          <el-table-column property="groupType" width="120">
            <template slot="header">
              <span style="font-size: 12px;">
                {{ $t('dataset.d_q_trans') }}
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    若字段表达式中使用聚合函数，则字段不能设置为维度使用。
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </template>
            <template slot-scope="scope">
              <el-button
                icon="el-icon-sort"
                size="mini"
                circle
                @click="dqTrans(scope.row,'d')"
              />
            </template>
          </el-table-column>
          <el-table-column property="" :label="$t('dataset.operator')">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.extField !== 0"
                type="text"
                size="mini"
                @click="editField(scope.row)"
              >{{ $t('dataset.edit') }}
              </el-button>
              <el-button
                v-if="scope.row.extField !== 0"
                type="text"
                size="mini"
                @click="deleteField(scope.row)"
              >{{ $t('dataset.delete') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item name="q" :title="$t('chart.quota')">
        <el-table :data="tableFields.quotaListData" size="mini">
          <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
            <template slot-scope="scope">
              <el-input
                v-model="scope.row.name"
                size="mini"
                @blur="saveEdit(scope.row)"
                @keyup.enter.native="saveEdit(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column property="deType" :label="$t('dataset.field_type')" width="140">
            <template slot-scope="scope">
              <el-select
                v-model="scope.row.deType"
                size="mini"
                style="display: inline-block;width: 26px;"
                @change="saveEdit(scope.row)"
              >
                <el-option
                  v-for="item in fields"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                >
                  <span style="float: left">
                    <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />
                    <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />
                    <svg-icon
                      v-if="item.value === 2 || item.value === 3"
                      icon-class="field_value"
                      class="field-icon-value"
                    />
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
                  <svg-icon
                    v-if="scope.row.deType === 2 || scope.row.deType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <span v-if="scope.row.deType === 2" class="field-class">{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deType === 3"
                    class="field-class"
                  >{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
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
                <span
                  v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4"
                >
                  <svg-icon
                    v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3 || scope.row.deExtractType === 4"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <span
                    v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 4"
                    class="field-class"
                  >{{ $t('dataset.value') }}</span>
                  <span
                    v-if="scope.row.deExtractType === 3"
                    class="field-class"
                  >{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>
                </span>
                <span v-if="scope.row.deExtractType === 5">
                  <svg-icon
                    v-if="scope.row.deExtractType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  />
                  <span class="field-class">{{ $t('dataset.location') }}</span>
                </span>
              </span>
            </template>
          </el-table-column>
          <el-table-column property="fromType" :label="$t('chart.form_type')" width="120">
            <template slot-scope="scope">
              <span v-if="scope.row.extField === 1" class="from-type-span">{{ $t('chart.copy_field') }}</span>
              <span v-if="scope.row.extField === 2" class="from-type-span">{{ $t('chart.calc_field') }}</span>
            </template>
          </el-table-column>
          <el-table-column property="groupType" width="120">
            <template slot="header">
              <span style="font-size: 12px;">
                {{ $t('dataset.d_q_trans') }}
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <div slot="content">
                    若字段表达式中使用聚合函数，则字段不能设置为维度使用。
                  </div>
                  <i class="el-icon-info" style="cursor: pointer;" />
                </el-tooltip>
              </span>
            </template>
            <template slot-scope="scope">
              <el-button
                icon="el-icon-sort"
                size="mini"
                circle
                @click="dqTrans(scope.row,'q')"
              />
            </template>
          </el-table-column>
          <el-table-column property="" :label="$t('dataset.operator')">
            <template slot-scope="scope">
              <el-button
                v-if="scope.row.extField !== 0"
                type="text"
                size="mini"
                @click="editField(scope.row)"
              >{{ $t('dataset.edit') }}
              </el-button>
              <el-button
                v-if="scope.row.extField !== 0"
                type="text"
                size="mini"
                @click="deleteField(scope.row)"
              >{{ $t('dataset.delete') }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog
      v-if="editCalcField"
      v-dialogDrag
      :visible="editCalcField"
      :show-close="false"
      class="dialog-css"
      :title="currEditField.id?$t('dataset.edit_calc_field'):$t('dataset.add_calc_field')"
      append-to-body
    >
      <calc-chart-field-edit
        :param="param"
        :field="currEditField"
        :mode="currEditField.extField === 1 ? 'copy' : 'normal'"
        @onEditClose="closeCalcField"
      />
    </el-dialog>
  </el-row>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import CalcChartFieldEdit from '@/views/chart/view/CalcChartFieldEdit'

export default {
  name: 'ChartFieldEdit',
  components: { CalcChartFieldEdit },
  props: {
    param: {
      type: Object,
      required: true
    },
    table: {
      type: Object,
      required: true
    },
    chart: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      maxHeight: 'auto',
      tableFields: {
        dimensionList: [],
        quotaList: [],
        dimensionListData: [],
        quotaListData: []
      },
      fields: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      fieldActiveNames: ['d', 'q'],
      searchField: '',
      editCalcField: false,
      currEditField: {},
      isSyncField: false
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    'param': function() {
      this.initField()
    },
    searchField(val) {
      this.filterField(val)
    }
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
        that.maxHeight = (currentHeight - 56 - 30 - 35 - 26 - 10 - 10) + 'px'
      }, 10)
    },
    initField() {
      post('/chart/field/listByDQ/' + this.param.id + '/' + this.panelInfo.id, null).then(response => {
        this.tableFields = response.data
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionList))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList))
        this.filterField(this.searchField)
      })
    },
    saveEdit(item) {
      if (item.name && item.name.length > 50) {
        this.$message.error(this.$t('dataset.field_name_less_50'))
        return
      }

      post('/chart/field/save/' + this.panelInfo.id, item).then(response => {
        this.initField()
      }).catch(res => {
        this.initField()
      })
    },

    dqTrans(item, val) {
      if (val === 'd') {
        item.groupType = 'q'
      } else if (val === 'q') {
        item.groupType = 'd'
      }
      this.saveEdit(item)
    },

    addCalcField() {
      this.currEditField = {}
      this.editCalcField = true
    },

    closeCalcField() {
      this.editCalcField = false
      this.initField()
    },

    filterField(val) {
      if (val && val !== '') {
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionListData.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
      } else {
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionList))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList))
      }
    },

    editField(item) {
      this.currEditField = item
      this.editCalcField = true
    },

    deleteField(item) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('chart.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        post('/chart/field/delete/' + item.id + '/' + this.panelInfo.id, null).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('chart.delete_success'),
            showClose: true
          })
          this.initField()
        })
      }).catch(() => {
      })
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

span {
  font-size: 14px;
}

.field-class {
  font-size: 12px !important;
}

.el-select ::v-deep input {
  padding-right: 10px;
}

.el-select ::v-deep .el-input__suffix {
  right: 0;
}

.el-radio {
  margin-right: 10px !important;
}

.style-collapse ::v-deep .el-collapse-item__header {
  height: 40px;
  line-height: 40px;
  padding: 0 0 0 10px;
}

.style-collapse ::v-deep .el-collapse-item__wrap {
  border-bottom: 0 solid #e6ebf5 !important;
}

.style-collapse {
  border-top: 0 solid #e6ebf5 !important;
}

.form-item {
  margin-bottom: 6px;
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.dialog-css ::v-deep .el-dialog {
  width: 800px !important;
}

.from-type-span{
  font-size: 12px;
  color: #c0c0c0;
}
</style>
