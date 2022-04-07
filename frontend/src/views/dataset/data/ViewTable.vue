<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <el-row style="height: 26px;">
      <span class="title-text" style="line-height: 26px;">
        {{ table.name }}
      </span>
      <el-popover
        placement="right-start"
        width="400"
        trigger="click"
        @show="showTab"
        @hide="hideTab"
      >
        <dataset-chart-detail type="dataset" :data="table" :tab-status="tabStatus"/>
        <!--        <svg-icon slot="reference" class="title-text" icon-class="more_v" style="cursor: pointer;" />-->
        <i slot="reference" class="el-icon-warning icon-class"
           style="margin-left: 4px;cursor: pointer;font-size: 14px;"/>
      </el-popover>
      <el-row v-if="hasDataPermission('manage',param.privileges)" style="float: right">
        <el-dropdown v-if="table.type ==='excel'" style="margin-right: 10px;" size="small" trigger="click"
                     @command="clickEditExcel">
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
        <el-button v-if="table.type ==='union'" size="mini" @click="editUnion">
          {{ $t('dataset.edit_union') }}
        </el-button>
      </el-row>
    </el-row>
    <el-divider/>

    <el-tabs v-model="tabActive" @tab-click="tabClick">
      <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
        <tab-data-preview :param="param" :table="table" :fields="fields" :data="data" :page="page"
                          :form="tableViewRowForm" @reSearch="reSearch"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('dataset.field_manage')" :lazy="true" name="fieldEdit">
        <field-edit v-if="tabActive === 'fieldEdit'" :param="param" :table="table"/>
      </el-tab-pane>
      <el-tab-pane
        v-if="!hideCustomDs && table.type !== 'union' && table.type !== 'custom' && !(table.type === 'sql' && table.mode === 0)"
        :label="$t('dataset.join_view')" name="joinView">
        <union-view :param="param" :table="table"/>
      </el-tab-pane>
      <el-tab-pane
        v-if="table.mode === 1 && (table.type === 'excel' || table.type === 'db' || table.type === 'sql' || table.type === 'api')"
        :label="$t('dataset.update_info')" name="updateInfo">
        <update-info v-if="tabActive=='updateInfo'" :param="param" :table="table"/>
      </el-tab-pane>
      <el-tab-pane v-if="isPluginLoaded && hasDataPermission('manage',param.privileges)" :lazy="true"
                   :label="$t('dataset.row_permissions')" name="rowPermissions">
        <plugin-com v-if="isPluginLoaded && tabActive=='rowPermissions'" ref="RowPermissions"
                    component-name="RowPermissions" :obj="table"/>
      </el-tab-pane>
      <el-tab-pane v-if="isPluginLoaded && hasDataPermission('manage',param.privileges)" :lazy="true"
                   :label="$t('dataset.column_permissions')" name="columnPermissions">
        <plugin-com v-if="isPluginLoaded && tabActive=='columnPermissions'" ref="ColumnPermissions"
                    component-name="ColumnPermissions" :obj="table"/>
      </el-tab-pane>
    </el-tabs>
  </el-row>
</template>

<script>
import {post} from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetChartDetail from '../common/DatasetChartDetail'
import UnionView from './UnionView'
import FieldEdit from './FieldEdit'
import {pluginLoaded} from '@/api/user'
import PluginCom from '@/views/system/plugin/PluginCom'

export default {
  name: 'ViewTable',
  components: {FieldEdit, UnionView, DatasetChartDetail, UpdateInfo, TabDataPreview, PluginCom},
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
        pageSize: 1000,
        show: 1000
      },
      tabActive: 'dataPreview',
      tableViewRowForm: {
        row: 1000
      },
      tabStatus: false,
      isPluginLoaded: false
    }
  },
  computed: {
    hideCustomDs: function () {
      return this.$store.getters.hideCustomDs
    }
  },
  watch: {
    'param': function () {
      this.tabActive = 'dataPreview'
      this.initTable(this.param.id)
    }
  },
  beforeCreate() {
    pluginLoaded().then(res => {
      this.isPluginLoaded = res.success && res.data
    })
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
          this.$emit('switchComponent', {name: ''})
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
            pageSize: 1000,
            show: 0
          }
        })
      }
    },

    edit() {
      this.$emit('switchComponent', {name: 'FieldEdit', param: {table: this.table}})
    },

    editSql() {
      this.$emit('switchComponent', {
        name: 'AddSQL',
        param: {id: this.table.sceneId, tableId: this.table.id, table: this.table}
      })
    },
    editCustom() {
      this.$emit('switchComponent', {
        name: 'AddCustom',
        param: {id: this.table.sceneId, tableId: this.table.id, table: this.table}
      })
    },
    editUnion() {
      this.$emit('switchComponent', {
        name: 'AddUnion',
        param: {id: this.table.sceneId, tableId: this.table.id, table: this.table}
      })
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
          this.$emit('switchComponent', {
            name: 'AddExcel',
            param: {id: this.table.sceneId, tableId: this.table.id, editType: 0, table: this.table}
          })
          break
        case '1':
          this.$emit('switchComponent', {
            name: 'AddExcel',
            param: {id: this.table.sceneId, tableId: this.table.id, editType: 1, table: this.table}
          })
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
        pageSize: 1000,
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

.icon-class {
  color: #6c6c6c;
}

.blackTheme .icon-class {
  color: #cccccc;
}
</style>
