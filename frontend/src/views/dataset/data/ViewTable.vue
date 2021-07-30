<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <!--    <span v-show="false">{{ tableRefresh }}</span>-->
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
      <el-row v-if="hasDataPermission('manage',param.privileges)" style="float: right">
        <el-dropdown v-if="table.type ==='excel'" style="margin-right: 10px;" size="small" trigger="click" @command="clickEditExcel">
          <el-button size="mini">
            {{ $t('dataset.edit_excel') }}
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item :command="beforeEditExcel('0')">
              {{ $t('dataset.excel_replace') }}
            </el-dropdown-item>
            <el-dropdown-item :command="beforeEditExcel('1')">
              {{ $t('dataset.excel_add') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button v-if="table.type ==='custom'" size="mini" @click="editCustom">
          {{ $t('dataset.edit_custom_table') }}
        </el-button>
        <el-button v-if="table.type ==='sql'" size="mini" @click="editSql">
          {{ $t('dataset.edit_sql') }}
        </el-button>
        <!--        <el-button size="mini" @click="edit">-->
        <!--          {{ $t('dataset.edit_field') }}-->
        <!--        </el-button>-->
        <!--        <el-button size="mini" type="primary" @click="createChart">-->
        <!--          {{$t('dataset.create_view')}}-->
        <!--        </el-button>-->
      </el-row>
    </el-row>
    <el-divider />

    <el-tabs v-model="tabActive" @tab-click="tabClick">
      <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
        <tab-data-preview :param="param" :table="table" :fields="fields" :data="data" :page="page" :form="tableViewRowForm" @reSearch="reSearch" />
      </el-tab-pane>
      <el-tab-pane :label="$t('dataset.field_manage')" name="fieldEdit">
        <field-edit :param="param" />
      </el-tab-pane>
      <el-tab-pane v-if="table.type !== 'custom'" :label="$t('dataset.join_view')" name="joinView">
        <union-view :param="param" :table="table" />
      </el-tab-pane>
      <el-tab-pane v-if="table.mode === 1 && (table.type === 'excel' || table.type === 'db' || table.type === 'sql')" :label="$t('dataset.update_info')" name="updateInfo">
        <update-info :param="param" :table="table" />
      </el-tab-pane>
    </el-tabs>
  </el-row>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetChartDetail from '../common/DatasetChartDetail'
import UnionView from './UnionView'
import FieldEdit from './FieldEdit'

export default {
  name: 'ViewTable',
  components: { FieldEdit, UnionView, DatasetChartDetail, UpdateInfo, TabDataPreview },
  props: {
    param: {
      type: Object,
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
  computed: {
    // tableRefresh() {
    //   this.initTable(this.param)
    //   return this.$store.state.dataset.table
    // }
  },
  watch: {
    'param': function() {
      this.tabActive = 'dataPreview'
      this.initTable(this.param.id)
    }
  },
  created() {

  },
  mounted() {
    this.initTable(this.param.id)
  },
  methods: {
    initTable(id) {
      this.resetPage()
      this.tableViewRowForm.row = 1000
      if (id !== null) {
        this.fields = []
        this.data = []
        post('/dataset/table/getWithPermission/' + id, null).then(response => {
          this.table = response.data
          this.initPreviewData(this.page)
        }).catch(res => {
          this.$emit('switchComponent', { name: '' })
        })
      }
    },

    initPreviewData(page) {
      if (this.table.id) {
        this.table.row = this.tableViewRowForm.row
        post('/dataset/table/getPreviewData/' + page.page + '/' + page.pageSize, this.table, true, 30000).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
          this.page = response.data.page
          if (response.data.status === 'warnning') {
            this.$warning(response.data.msg, 3000)
          }
          if (response.data.status === 'error') {
            this.$error(response.data.msg, 3000)
          }
        }).catch(response => {
          this.fields = []
          this.data = []
          this.page = {
            page: 1,
            pageSize: 100,
            show: 0
          }
        })
      }
    },

    edit() {
      this.$emit('switchComponent', { name: 'FieldEdit', param: { table: this.table }})
    },

    editSql() {
      this.$emit('switchComponent', { name: 'AddSQL', param: { id: this.table.sceneId, tableId: this.table.id, table: this.table }})
    },
    editCustom() {
      this.$emit('switchComponent', { name: 'AddCustom', param: { id: this.table.sceneId, tableId: this.table.id, table: this.table }})
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
    },

    clickEditExcel(param) {
      // console.log(param);
      switch (param.type) {
        case '0':
          this.$emit('switchComponent', { name: 'AddExcel', param: { id: this.table.sceneId, tableId: this.table.id, editType: 0, table: this.table }})
          break
        case '1':
          this.$emit('switchComponent', { name: 'AddExcel', param: { id: this.table.sceneId, tableId: this.table.id, editType: 1, table: this.table }})
          break
      }
    },

    beforeEditExcel(type) {
      return {
        'type': type
      }
    },
    msg2Current(sourceParam) {
      this.tabActive = 'updateInfo'
      this.table.msgTaskId = sourceParam.taskId
    },

    resetPage() {
      this.page = {
        page: 1,
        pageSize: 100,
        show: 1000
      }
    },

    tabClick() {
      if (this.tabActive === 'dataPreview') {
        this.initTable(this.param.id)
      }
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
