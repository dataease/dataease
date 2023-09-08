<template>
  <div class="view-table">
    <el-row>
      <el-col
        class="de-dataset-name"
        :span="16"
      >
        <span
          class="title-text"
          style="line-height: 26px"
          :title="table.name"
        >
          {{ table.name }}
        </span>
        <template v-if="['db', 'sql'].includes(param.modelInnerType)">
          <span
            v-if="table.mode === 0"
            class="de-tag primary"
          >{{
              $t('dataset.direct_connect')
            }}</span>
          <span
            v-if="table.mode === 1"
            class="de-tag warning"
          >{{
              $t('dataset.sync_data')
            }}</span>
        </template>
        <span
          v-if="syncStatus === 'Underway'"
          class="blue-color"
          style="line-height: 26px"
        >
          {{ $t('dataset.dataset_sync') }}
        </span>
        <el-divider direction="vertical"/>
        <span class="create-by">{{ $t('dataset.create_by') }}</span>
        <span class="create-by">:{{ table.creatorName || 'N/A' }}</span>
        <el-popover
          placement="bottom"
          width="420"
          trigger="hover"
          @show="showTab"
          @hide="hideTab"
        >
          <dataset-detail
            type="dataset"
            :data="table"
            :tab-status="tabStatus"
          />
          <svg-icon slot="reference" class="detail" icon-class="icon_info_outlined" />
        </el-popover>
      </el-col>
      <el-col
        style="text-align: right"
        :span="8"
      >
        <deBtn
          v-if="hasDataPermission('manage', param.privileges)"
          :disabled="!previewDataSuccess"
          type="primary"
          icon="el-icon-download"
          @click="exportDataset"
        >
          {{ $t('dataset.export_dataset') }}
        </deBtn>
        <el-dropdown
          v-if="table.type === 'excel' && hasDataPermission('manage', param.privileges)"
          style="margin-left: 12px;"
          size="small"
          trigger="click"
          placement="bottom-end"
          @command="(type) => clickEditExcel(type)"
        >
          <deBtn type="primary">
            {{ $t('dataset.edit_excel') }}
          </deBtn>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="0">
              <svg-icon icon-class="icon_add-entry_outlined"/>
              {{ $t('dataset.excel_replace') + $t('chart.chart_data') }}
            </el-dropdown-item>
            <el-dropdown-item command="1">
              <svg-icon icon-class="icon_doc-replace_outlined"/>
              {{ $t('dataset.excel_add') + $t('chart.chart_data') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <deBtn
          v-if="['sql', 'union'].includes(table.type) && hasDataPermission('manage', param.privileges)"
          type="primary"
          @click="editDataset(table.type)"
        >
          {{
            $t(
              table.type === 'union' ? 'dataset.edit_union' : 'dataset.edit_sql'
            )
          }}
        </deBtn>
      </el-col>
    </el-row>

    <el-tabs
      v-model="tabActive"
      class="de-tabs"
      @tab-click="tabClick"
    >
      <el-tab-pane
        :label="$t('dataset.data_preview')"
        name="dataPreview"
      >
        <tab-data-preview
          :param="param"
          :table="table"
          :fields="fields"
          :data="data"
          :page="page"
          :form="tableViewRowForm"
          @reSearch="reSearch"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('dataset.field_manage')"
        :lazy="true"
        name="fieldEdit"
      >
        <field-edit
          v-if="tabActive === 'fieldEdit'"
          :param="param"
          :table="table"
        />
      </el-tab-pane>
      <el-tab-pane
        v-if="
          table.mode === 1 && ['api', 'sql', 'db'].includes(table.type)
        "
        :label="$t('dataset.update_info')"
        name="updateInfo"
      >
        <update-info
          v-if="tabActive == 'updateInfo'"
          :param="param"
          :table="table"
        />
      </el-tab-pane>
      <el-tab-pane
        v-if="
          table.mode === 1 && ['excel'].includes(table.type)
        "
        :label="$t('dataset.update_records')"
        name="updateInfo"
      >
        <update-records
          v-if="tabActive == 'updateInfo'"
          :param="param"
          :table="table"
        />
      </el-tab-pane>
      <el-tab-pane
        v-if="isPluginLoaded && hasDataPermission('manage', param.privileges)"
        :lazy="true"
        :label="$t('dataset.row_permissions')"
        name="rowPermissions"
      >
        <plugin-com
          v-if="isPluginLoaded && tabActive == 'rowPermissions'"
          ref="RowPermissions"
          component-name="RowPermissions"
          :obj="table"
        />
      </el-tab-pane>
      <el-tab-pane
        v-if="isPluginLoaded && hasDataPermission('manage', param.privileges)"
        :lazy="true"
        :label="$t('dataset.column_permissions')"
        name="columnPermissions"
      >
        <plugin-com
          v-if="isPluginLoaded && tabActive == 'columnPermissions'"
          ref="ColumnPermissions"
          component-name="ColumnPermissions"
          :obj="table"
        />
      </el-tab-pane>
    </el-tabs>

    <!--导出数据集弹框-->
    <el-dialog
      v-if="showExport"
      v-dialogDrag
      :visible.sync="showExport"
      width="800px"
      class="de-dialog-form form-tree-cont"
      :title="$t('dataset.export_dataset')"
      append-to-body
    >
      <el-form
        ref="exportForm"
        class="de-form-item"
        :model="exportForm"
        :rules="exportFormRules"
        :before-close="closeExport"
        @submit.native.prevent
        @keypress.enter.native="exportDatasetRequest"
      >
        <el-form-item
          :label="$t('dataset.filename')"
          prop="name"
        >
          <el-input
            v-model.trim="exportForm.name"
            :placeholder="$t('dataset.pls_input_filename')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('dataset.export_filter')"
          prop="expressionTree"
        >
          <div class="tree-cont">
            <div class="content">
              <rowAuth ref="rowAuth"/>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span class="tip">提示：最多支持导出10万条数据</span>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <deBtn
          secondary
          @click="closeExport"
        >{{ $t('dataset.cancel') }}
        </deBtn>
        <deBtn
          type="primary"
          @click="exportDatasetRequest"
        >{{ $t('dataset.confirm') }}
        </deBtn>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { exportDataset, post } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetDetail from '../common/DatasetDetail'
import FieldEdit from './FieldEdit'
import { pluginLoaded } from '@/api/user'
import PluginCom from '@/views/system/plugin/PluginCom'
import UpdateRecords from './UpdateRecords'
import rowAuth from './components/rowAuth.vue'

export default {
  name: 'ViewTable',
  components: {
    FieldEdit,
    DatasetDetail,
    UpdateInfo,
    TabDataPreview,
    UpdateRecords,
    rowAuth,
    PluginCom
  },
  provide() {
    return {
      filedList: () => this.filedList
    }
  },
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
      filedList: [],
      data: [],
      syncStatus: '',
      lastRequestComplete: true,
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
      isPluginLoaded: false,
      showExport: false,
      exportForm: {
        name: ''
      },
      exportFormRules: {
        name: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'change'
          },
          {
            max: 50,
            message: this.$t('commons.char_can_not_more_50'),
            trigger: 'change'
          }
        ]
      },
      previewDataSuccess: false
    }
  },
  computed: {
    hideCustomDs: function() {
      return this.$store.getters.hideCustomDs
    }
  },
  watch: {
    param: function() {
      this.tabActive = 'dataPreview'
      this.initTable(this.param.id)
    }
  },
  beforeCreate() {
    pluginLoaded().then((res) => {
      this.isPluginLoaded = res.success && res.data
    })
  },
  created() {
    this.taskLogTimer = setInterval(() => {
      if (this.syncStatus !== 'Underway') {
        return
      }
      if (!this.lastRequestComplete) {
        return
      } else {
        this.lastRequestComplete = false
      }
      this.initPreviewData(this.page)
    }, 5000)
  },
  beforeDestroy() {
    clearInterval(this.taskLogTimer)
  },
  mounted() {
    this.initTable(this.param.id)
  },
  methods: {
    fetchFiledList() {
      this.filedList = []
      post('dataset/field/listForPermissionSeting/' + this.param.id,
        {}).then((res) => {
        this.filedList = res.data
      })
    },
    initTable(id) {
      this.resetPage()
      this.tableViewRowForm.row = 1000
      if (id !== null) {
        this.fields = []
        this.data = []
        post('/dataset/table/getWithPermission/' + id, null)
          .then((response) => {
            this.table = response.data
            this.initPreviewData(this.page)
          })
          .catch((res) => {
            this.$emit('switchComponent', { name: '' })
          })
      }
    },

    initPreviewData(page) {
      if (this.table.id) {
        this.table.row = this.tableViewRowForm.row
        post(
          '/dataset/table/getPreviewData/' + page.page + '/' + page.pageSize,
          this.table,
          true,
          30000
        )
          .then((response) => {
            this.fields = response.data.fields
            this.data = response.data.data
            this.page = response.data.page
            this.syncStatus = response.data.syncStatus
            this.previewDataSuccess = true
            if (response.data.status === 'warnning') {
              this.$warning(response.data.msg, 3000)
              this.previewDataSuccess = false
            }
            if (response.data.status === 'error') {
              this.$error(response.data.msg, 3000)
              this.previewDataSuccess = false
            }
            this.lastRequestComplete = true
          })
          .catch((response) => {
            this.lastRequestComplete = true
            this.fields = []
            this.data = []
            this.page = {
              page: 1,
              pageSize: 1000,
              show: 0
            }
            this.previewDataSuccess = false
          })
      }
    },
    editDataset(datasetType) {
      this.$router.push({
        path: '/dataset-form',
        query: {
          datasetType,
          id: this.table.id
        }
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
    clickEditExcel(type) {
      this.$router.push({
        path: '/dataset-form',
        query: {
          datasetType: 'excel',
          id: this.table.id,
          editType: type
        }
      })
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
    },

    exportDataset() {
      this.showExport = true
      this.fetchFiledList()
      this.exportForm.name = this.table.name
      this.exportForm.expressionTree = ''
    },
    closeExport() {
      this.showExport = false
    },
    exportDatasetRequest() {
      this.$refs['exportForm'].validate((valid) => {
        if (valid) {
          if (this.table.id) {
            this.table.row = 100000
            this.table.filename = this.exportForm.name
            const { logic, items, errorMessage } = this.$refs.rowAuth.submit()
            if (errorMessage) {
              this.$message({
                message: errorMessage,
                type: 'error',
                showClose: true
              })
              return
            }
            this.table.expressionTree = JSON.stringify({ items, logic })
            exportDataset(this.table).then((res) => {
              const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
              const link = document.createElement('a')
              link.style.display = 'none'
              link.href = URL.createObjectURL(blob)
              link.download = this.exportForm.name + '.xlsx' // 下载的文件名
              document.body.appendChild(link)
              link.click()
              document.body.removeChild(link)
            })
          }
        } else {
          return false
        }
      })
    }
  }
}
</script>

<style lang="scss">
.form-tree-cont {
  .tree-cont {
    height: 200px;
    width: 100%;
    padding: 16px;
    border-radius: 4px;
    border: 1px solid var(--deBorderBase, #DCDFE6);
    overflow: auto;

    .content {
      height: 100%;
      width: 100%;
    }
  }
}

.icon-class {
  color: #6c6c6c;
}

.blackTheme .icon-class {
  color: #cccccc;
}

.tip {
  color: #F56C6C;
  font-size: 12px;
}
</style>
<style lang="scss" scoped>
.view-table {
  padding: 10px 9px;
  height: 100%;
  overflow-y: hidden;
  width: 100%;
  box-sizing: border-box;

  .de-dataset-name {
    display: flex;
    font-family: PingFang SC;
    align-items: center;
    margin-bottom: 20px;

    .title-text {
      font-size: 16px;
      font-weight: 500;
      margin-right: 8px;
      color: var(--deTextPrimary, #1f2329);
      display: inline-block;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      max-width: 50%;
    }

    .de-tag {
      padding: 0 6px;
      border-radius: 2px;
      font-size: 12px;
      font-weight: 400;
    }

    .warning {
      color: var(--deWarning, #ff8800);
      background: rgba(255, 136, 0, 0.2);
    }

    .primary {
      color: var(--primary, #3370ff);
      background: rgba(51, 112, 255, 0.2);
    }

    .el-divider {
      margin: 0 16px;
      height: 18px;
    }

    .create-by {
      font-size: 14px;
      font-weight: 400;
      color: var(--deTextSecondary, #606266);
    }

    .detail {
      cursor: pointer;
      margin-left: 10px;
      color: var(--deTextSecondary, #606266);
    }
  }
}
</style>
