<template>
  <div class="view-table">
    <el-row>
      <el-col class="de-dataset-name" :span="16">
        <span class="title-text" style="line-height: 26px">
          {{ table.name }}
        </span>
        <template v-if="['db', 'sql'].includes(param.modelInnerType)">
          <span v-if="table.mode === 0" class="de-tag primary">{{
            $t('dataset.direct_connect')
          }}</span>
          <span v-if="table.mode === 1" class="de-tag warning">{{
            $t('dataset.sync_data')
          }}</span>
        </template>
        <span
          v-if="sycnStatus === 'Underway'"
          class="blue-color"
          style="line-height: 26px"
        >
          {{ $t('dataset.dataset_sync') }}
        </span>
        <el-divider direction="vertical"></el-divider>
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
          <i slot="reference" class="el-icon-warning-outline detail" />
        </el-popover>
      </el-col>
      <el-col
        v-if="hasDataPermission('manage', param.privileges)"
        style="text-align: right"
        :span="8"
      >
        <el-dropdown
          v-if="table.type === 'excel'"
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
              <svg-icon icon-class="icon_add-entry_outlined"> </svg-icon>
              {{ $t('dataset.excel_replace') + $t('chart.chart_data') }}
            </el-dropdown-item>
            <el-dropdown-item command="1">
              <svg-icon icon-class="icon_doc-replace_outlined"> </svg-icon>
              {{ $t('dataset.excel_add') + $t('chart.chart_data') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <deBtn
          type="primary"
          v-if="['sql', 'union'].includes(table.type)"
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

    <el-tabs class="de-tabs" v-model="tabActive" @tab-click="tabClick">
      <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
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
          table.mode === 1 &&
          (table.type === 'db' || table.type === 'sql' || table.type === 'api')
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
  </div>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'
import DatasetDetail from '../common/DatasetDetail'
import FieldEdit from './FieldEdit'
import { pluginLoaded } from '@/api/user'
import PluginCom from '@/views/system/plugin/PluginCom'

export default {
  name: 'ViewTable',
  components: {
    FieldEdit,
    DatasetDetail,
    UpdateInfo,
    TabDataPreview,
    PluginCom
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
      data: [],
      sycnStatus: '',
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
      isPluginLoaded: false
    }
  },
  computed: {
    hideCustomDs: function () {
      return this.$store.getters.hideCustomDs
    }
  },
  watch: {
    param: function () {
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
      if (this.sycnStatus !== 'Underway') {
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
            this.sycnStatus = response.data.sycnStatus
            if (response.data.status === 'warnning') {
              this.$warning(response.data.msg, 3000)
            }
            if (response.data.status === 'error') {
              this.$error(response.data.msg, 3000)
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
        const reload = localStorage.getItem('reloadDsData')
        if (reload === 'true') {
          localStorage.setItem('reloadDsData', 'false')
          this.initTable(this.param.id)
        }
      }
    }
  }
}
</script>

<style scoped>
.icon-class {
  color: #6c6c6c;
}

.blackTheme .icon-class {
  color: #cccccc;
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
