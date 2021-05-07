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
        <union-view :table="table" />
      </el-tab-pane>
      <el-tab-pane v-if="table.mode === 1 && (table.type === 'db' || table.type === 'sql')" :label="$t('dataset.update_info')" name="updateInfo">
        <update-info :table="table" />
      </el-tab-pane>
    </el-tabs>
  </el-row>
</template>

<script>
import { getTable, post } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetChartDetail from '../common/DatasetChartDetail'
import UnionView from './UnionView'

export default {
  name: 'ViewTable',
  components: { UnionView, DatasetChartDetail, UpdateInfo, TabDataPreview },
  props: {
    param: {
      type: String,
      required: true
    }
  },
  data() {
    return {
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
      tableViewRowForm: {
        row: 1000
      },
      tabStatus: false
    }
  },
  watch: {
    'param': function() {
      this.initTable(this.param)
    }
  },
  mounted() {
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

    edit() {
      this.$emit('switchComponent', { name: 'FieldEdit', param: { table: this.table }})
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
</style>
