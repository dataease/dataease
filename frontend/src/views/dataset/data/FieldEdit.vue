<template>
  <el-row :style="{height: maxHeight,overflow:'auto'}">
    <!--    <el-row style="height: 26px;">-->
    <!--      <span style="line-height: 26px;">-->
    <!--        {{ $t('dataset.field_edit') }}-->
    <!--        <span>{{ param.name }}</span>-->
    <!--      </span>-->
    <!--      <el-row style="float: right">-->
    <!--        <el-button size="mini" @click="closeEdit">{{ $t('dataset.cancel') }}</el-button>-->
    <!--        <el-button type="primary" size="mini" @click="saveEdit">{{ $t('dataset.confirm') }}</el-button>-->
    <!--      </el-row>-->
    <!--    </el-row>-->
    <!--    <el-divider />-->
    <el-row>
      <el-form :inline="true">
        <el-form-item class="form-item">
          <el-button v-if="hasDataPermission('manage',param.privileges)" size="mini" @click="addCalcField">{{ $t('dataset.add_calc_field') }}</el-button>
        </el-form-item>
        <el-form-item class="form-item" style="float: right;margin-right: 0;">
          <el-input
            v-model="searchField"
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            clearable
          />
        </el-form-item>
      </el-form>
    </el-row>

    <el-collapse v-model="fieldActiveNames" class="style-collapse">
      <el-collapse-item name="d" :title="$t('chart.dimension')">
        <el-table :data="tableFields.dimensionListData" size="mini">
          <el-table-column property="checked" :label="$t('dataset.field_check')" width="60">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.checked" :disabled="!hasDataPermission('manage',param.privileges)" @change="saveEdit" />
            </template>
          </el-table-column>
          <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
            <template slot-scope="scope">
              <el-input v-model="scope.row.name" size="mini" :disabled="!hasDataPermission('manage',param.privileges)" @blur="saveEdit" @keyup.enter.native="saveEdit" />
            </template>
          </el-table-column>
          <el-table-column v-if="!(param.mode === 0 && param.type === 'custom')" property="originName" :label="$t('dataset.field_origin_name')" width="100">
            <template slot-scope="scope">
              <span :title="scope.row.originName" class="field-class" style="width: 100%;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                <span style="font-size: 12px;">{{ scope.row.originName }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column property="deType" :label="$t('dataset.field_type')" width="140">
            <template slot-scope="scope">
              <el-select v-model="scope.row.deType" size="mini" style="display: inline-block;width: 26px;" :disabled="!hasDataPermission('manage',param.privileges)" @change="saveEdit">
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
          <!--          <el-table-column property="groupType" :label="$t('dataset.field_group_type')" width="180">-->
          <!--            <template slot-scope="scope">-->
          <!--              <el-radio-group v-model="scope.row.groupType" size="mini">-->
          <!--                <el-radio-button label="d">{{ $t('chart.dimension') }}</el-radio-button>-->
          <!--                <el-radio-button label="q">{{ $t('chart.quota') }}</el-radio-button>-->
          <!--              </el-radio-group>-->
          <!--            </template>-->
          <!--          </el-table-column>-->
          <el-table-column property="groupType" :label="$t('dataset.d_q_trans')" width="120">
            <template slot-scope="scope">
              <el-button icon="el-icon-sort" size="mini" circle :disabled="!hasDataPermission('manage',param.privileges)" @click="dqTrans(scope.row,'d')" />
            </template>
          </el-table-column>
          <el-table-column property="" :label="$t('dataset.operator')">
            <template slot-scope="scope">
              <el-button type="text" size="small">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>

      <el-collapse-item name="q" :title="$t('chart.quota')">
        <el-table :data="tableFields.quotaListData" size="mini">
          <el-table-column property="checked" :label="$t('dataset.field_check')" width="60">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.checked" :disabled="!hasDataPermission('manage',param.privileges)" @change="saveEdit" />
            </template>
          </el-table-column>
          <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
            <template slot-scope="scope">
              <el-input v-model="scope.row.name" size="mini" :disabled="!hasDataPermission('manage',param.privileges)" @blur="saveEdit" @keyup.enter.native="saveEdit" />
            </template>
          </el-table-column>
          <el-table-column v-if="!(param.mode === 0 && param.type === 'custom')" property="originName" :label="$t('dataset.field_origin_name')" width="100">
            <template slot-scope="scope">
              <span :title="scope.row.originName" class="field-class" style="width: 100%;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">
                <span style="font-size: 12px;">{{ scope.row.originName }}</span>
              </span>
            </template>
          </el-table-column>
          <el-table-column property="deType" :label="$t('dataset.field_type')" width="140">
            <template slot-scope="scope">
              <el-select v-model="scope.row.deType" size="mini" style="display: inline-block;width: 26px;" :disabled="!hasDataPermission('manage',param.privileges)" @change="saveEdit">
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
          <!--          <el-table-column property="groupType" :label="$t('dataset.field_group_type')" width="180">-->
          <!--            <template slot-scope="scope">-->
          <!--              <el-radio-group v-model="scope.row.groupType" size="mini">-->
          <!--                <el-radio-button label="d">{{ $t('chart.dimension') }}</el-radio-button>-->
          <!--                <el-radio-button label="q">{{ $t('chart.quota') }}</el-radio-button>-->
          <!--              </el-radio-group>-->
          <!--            </template>-->
          <!--          </el-table-column>-->
          <el-table-column property="groupType" :label="$t('dataset.d_q_trans')" width="120">
            <template slot-scope="scope">
              <el-button icon="el-icon-sort" size="mini" circle :disabled="!hasDataPermission('manage',param.privileges)" @click="dqTrans(scope.row,'q')" />
            </template>
          </el-table-column>
          <el-table-column property="" :label="$t('dataset.operator')">
            <template slot-scope="scope">
              <el-button type="text" size="small">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>

    <el-dialog
      v-dialogDrag
      :visible="editCalcField"
      :show-close="false"
      class="dialog-css"
      :destroy-on-close="true"
      :title="$t('dataset.add_calc_field')"
      append-to-body
    >
      <calc-field-edit :param="param" :table-fields="tableFields" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeCalcField">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import { batchEdit, fieldListDQ } from '@/api/dataset/dataset'
import CalcFieldEdit from './CalcFieldEdit'
export default {
  name: 'FieldEdit',
  components: { CalcFieldEdit },
  props: {
    param: {
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
      editCalcField: false
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
        that.maxHeight = (currentHeight - 56 - 30 - 35 - 26 - 10) + 'px'
      }, 10)
    },
    initField() {
      fieldListDQ(this.param.id).then(response => {
        this.tableFields = response.data
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionList))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList))
        this.filterField(this.searchField)
      })
    },
    saveEdit() {
      // console.log(this.tableFields)
      const list = this.tableFields.dimensionListData.concat(this.tableFields.quotaListData)
      batchEdit(list).then(response => {
        // this.closeEdit()
        this.initField()
      })
    },

    dqTrans(item, val) {
      if (val === 'd') {
        item.groupType = 'q'
      } else if (val === 'q') {
        item.groupType = 'd'
      }
      this.saveEdit()
    },

    addCalcField() {
      this.editCalcField = true
    },

    closeCalcField() {
      this.editCalcField = false
    },

    filterField(val) {
      if (val && val !== '') {
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionListData.filter(ele => { return ele.name.includes(val) })))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList.filter(ele => { return ele.name.includes(val) })))
      } else {
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionList))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList))
      }
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
  .style-collapse>>>.el-collapse-item__header{
    height: 40px;
    line-height: 40px;
    padding: 0 0 0 10px;
  }
  .style-collapse>>>.el-collapse-item__wrap{
    border-bottom: 0 solid #e6ebf5!important;
  }
  .style-collapse{
    border-top: 0 solid #e6ebf5!important;
  }
  .form-item {
    margin-bottom: 6px;
  }

  .dialog-css>>>.el-dialog__title {
    font-size: 14px;
  }
  .dialog-css >>> .el-dialog__header {
    padding: 20px 20px 0;
  }
  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }
  .dialog-css>>>.el-dialog{
    width: 800px!important;
  }
  .dialog-css>>>.el-dialog__footer{
    border-top: 1px solid #eee;
  }
</style>
