<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <el-row style="height: 26px;">
      <el-popover
        placement="right-start"
        width="400"
        trigger="click"
        @show="showTab"
        @hide="hideTab"
      >
        <dataset-chart-detail type="dataset" :data="table" :tab-status="tabStatus" />
        <span slot="reference" style="line-height: 26px;cursor: pointer;">
          {{ table.name }}
        </span>
      </el-popover>
      <el-row style="float: right">
        <el-button v-if="table.type ==='sql'" size="mini" @click="editSql">
          {{ $t('dataset.edit_sql') }}
        </el-button>
        <el-button size="mini" @click="edit">
          {{ $t('dataset.edit') }}
        </el-button>
        <!--        <el-button size="mini" type="primary" @click="createChart">-->
        <!--          {{$t('dataset.create_view')}}-->
        <!--        </el-button>-->
      </el-row>
    </el-row>
    <el-divider />

    <el-tabs v-model="tabActive">
      <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
        <tab-data-preview :table="table" :fields="fields" :data="data" :page="page" :form="tableViewRowForm" @reSearch="reSearch" />
      </el-tab-pane>
      <el-tab-pane :label="$t('dataset.join_view')" name="joinView">
        关联视图 TODO
      </el-tab-pane>
      <el-tab-pane v-if="table.mode === 1 && (table.type === 'db' || table.type === 'sql')" :label="$t('dataset.update_info')" name="updateInfo">
        <update-info :table="table" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="table.name" :visible.sync="editField" :fullscreen="true" :show-close="false" class="dialog-css">
      <el-table :data="tableFields" size="mini" :max-height="maxHeight">
        <el-table-column property="type" :label="$t('dataset.field_type')" width="100">
          <template slot-scope="scope">
            <span v-if="scope.row.deType === 0">
              <svg-icon v-if="scope.row.deType === 0" icon-class="field_text" class="field-icon-text" />
              {{ $t('dataset.text') }}
            </span>
            <span v-if="scope.row.deType === 1">
              <svg-icon v-if="scope.row.deType === 1" icon-class="field_time" class="field-icon-time" />
              {{ $t('dataset.time') }}
            </span>
            <span v-if="scope.row.deType === 2 || scope.row.deType === 3">
              <svg-icon v-if="scope.row.deType === 2 || scope.row.deType === 3" icon-class="field_value" class="field-icon-value" />
              {{ $t('dataset.value') }}
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
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeEdit">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveEdit">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--    <el-dialog title="view" :visible.sync="createViewDialog" :fullscreen="true">-->
    <!--      <chart-edit/>-->
    <!--    </el-dialog>-->
  </el-row>
</template>

<script>
import { getTable, post, fieldList, batchEdit } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetChartDetail from '../common/DatasetChartDetail'

export default {
  name: 'ViewTable',
  components: { DatasetChartDetail, UpdateInfo, TabDataPreview },
  props: {
    param: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      createViewDialog: false,
      editField: false,
      table: {
        name: ''
      },
      fields: [],
      data: [],
      page: {
        page: 1,
        pageSize: 100,
        show: 1000
      },
      tabActive: 'dataPreview',
      tableFields: [],
      tableViewRowForm: {
        row: 1000
      },
      tabStatus: false,
      maxHeight: 'auto'
    }
  },
  // computed: {
  //   tableId() {
  //     this.initTable(this.$store.state.dataset.table)
  //     return this.$store.state.dataset.table
  //   }
  // },
  watch: {
    'param': function() {
      this.initTable(this.param)
    }
  },
  mounted() {
    this.maxHeight = (document.documentElement.clientHeight - 45 - 78) + 'px'
    this.initTable(this.param)
  },
  methods: {
    initTable(id) {
      console.log(id)
      this.tabActive = 'dataPreview'
      this.tableViewRowForm.row = 1000
      if (id !== null) {
        this.fields = []
        this.data = []
        getTable(id).then(response => {
          this.table = response.data
          this.initPreviewData(this.page)
        })
      }
    },

    initPreviewData(page) {
      if (this.table.id) {
        this.table.row = this.tableViewRowForm.row
        post('/dataset/table/getPreviewData/' + page.page + '/' + page.pageSize, this.table).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
          this.page = response.data.page
        })
      }
    },

    initTableFields() {
      fieldList(this.table.id).then(response => {
        this.tableFields = response.data
      })
    },

    edit() {
      this.editField = true
      // 请求当前表的所有字段，进行编辑
      this.initTableFields()
    },

    // createChart() {
    //   console.log(this.table);
    //   this.createViewDialog = true;
    // },

    saveEdit() {
      console.log(this.tableFields)
      batchEdit(this.tableFields).then(response => {
        this.closeEdit()
        this.initTable(this.table.id)
      })
    },

    closeEdit() {
      this.editField = false
      this.tableFields = []
    },

    editSql() {
      this.$emit('switchComponent', { name: 'AddSQL', param: { id: this.table.sceneId, tableId: this.table.id }})
    },

    reSearch(val) {
      this.tableViewRowForm = val.form
      this.initPreviewData(val.page)
    },

    showTab() {
      this.tabStatus = true
    },
    hideTab() {
      this.tabStatus = false
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0;
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
    padding: 10px 20px;
  }
  .dialog-css >>> .el-dialog__footer {
    padding-top: 10px;
  }
</style>
