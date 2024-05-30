<template>
  <div
    v-if="hasDataPermission('use', param.privileges)"
    class="view-table"
  >
    <el-row>
      <el-col
        class="de-dataset-name"
        :span="16"
      >
        <span
          class="title-text"
          style="line-height: 26px"
          :title="param.name"
        >
          {{ param.name }}
        </span>
        <el-divider direction="vertical" />
        <span class="create-by">{{ $t('data_fill.form.creator') }}</span>
        <span class="create-by">:{{ param.creatorName || 'N/A' }}</span>
        <el-popover
          placement="bottom"
          width="420"
          trigger="hover"
          @show="showTab"
          @hide="hideTab"
        >
          <div class="info-tab">
            <div class="info-card">
              <div class="info-item">
                <p class="info-title">
                  {{ $t('data_fill.form.form_name') }}
                </p>
                <p class="info-content">{{ param.name }}</p>
              </div>
              <div class="info-item">
                <p class="info-title">
                  {{ $t('data_fill.form.datasource') }}
                </p>
                <p class="info-content">{{ param.datasourceName }}</p>
              </div>
              <div class="info-item">
                <p class="info-title">
                  {{ $t('data_fill.form.table') }}
                </p>
                <p class="info-content">{{ param.tableName }}</p>
              </div>
              <div class="info-item">
                <p class="info-title">
                  {{ $t('data_fill.form.creator') }}
                </p>
                <p class="info-content">{{ param.creatorName }}</p>
              </div>
              <div class="info-item">
                <p class="info-title">
                  {{ $t('data_fill.form.createTime') }}
                </p>
                <p class="info-content">
                  {{ param.createTime ? (new Date(param.createTime)).format("yyyy-MM-dd hh:mm:ss") : '' }}</p>
              </div>
            </div>
          </div>
          <svg-icon
            slot="reference"
            class="detail"
            icon-class="icon_info_outlined"
          />
        </el-popover>
      </el-col>
      <el-col
        style="text-align: right"
        :span="8"
      >
        <!--  编辑 todo      -->
        <el-button
          v-if="hasDataPermission('manage', param.privileges)"
          type="primary"
          @click="editForm(param)"
        >{{ $t('panel.edit') }}</el-button>
      </el-col>
    </el-row>

    <el-tabs
      v-model="tabActive"
      class="de-tabs"
      @tab-click="tabClick"
    >
      <el-tab-pane
        :label="$t('data_fill.form.list')"
        name="dataPreview"
      />
      <el-tab-pane
        :label="$t('data_fill.form.record')"
        :lazy="true"
        name="record"
      />
      <el-tab-pane
        v-if="hasDataPermission('manage', param.privileges)"
        :label="$t('data_fill.form.task_manage')"
        :lazy="true"
        name="task"
      />

    </el-tabs>

    <el-container
      v-if="tabActive === 'dataPreview'"
      direction="vertical"
      style="display: flex; flex-direction: column"
    >

      <template>
        <div
          v-if="hasDataPermission('write', param.privileges)"
          style="margin-bottom: 12px; height: 32px; display: flex; flex-direction: row;"
        >
          <el-button
            v-if="hasDataPermission('write', param.privileges)"
            icon="el-icon-plus"
            size="small"
            @click="addData"
          >{{ $t('data_fill.data.add_data') }}</el-button>
          <el-button
            v-if="hasDataPermission('write', param.privileges)"
            icon="el-icon-download"
            size="small"
            @click="downloadTemplate"
          >{{ $t('data_fill.data.download_template') }}</el-button>
          <el-upload
            v-if="hasDataPermission('write', param.privileges)"
            :action="`${baseUrl}dataFilling/form/${param.id}/excel/upload`"
            :multiple="false"
            :show-file-list="false"
            :file-list="fileList"
            :data="{}"
            accept=".xlsx"
            :before-upload="beforeUpload"
            :on-success="uploadSuccess"
            :on-error="uploadFail"
            name="file"
            :headers="headers"
          >
            <el-button
              style="margin-left: 10px"
              icon="el-icon-upload2"
              size="small"
              :disabled="uploading"
            >{{ $t('deDataset.upload_data') }}
            </el-button>
          </el-upload>
        </div>
        <div style="flex: 1">
          <grid-table
            v-if="columns.length > 0 && dataTableShow"
            ref="dataTable"
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            border
            stripe
            :table-data="data"
            :columns="[]"
            :current-row-key="paginationConfig.key"
            :pagination="paginationConfig"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          >
            <el-table-column
              v-for="c in columns"
              :key="c.props"
              :prop="c.props"
            >
              <template
                slot="header"
              >
                {{ c.label }}
                <span v-if="c.rangeIndex === 0">({{ $t('data_fill.data.start') }})</span>
                <span v-if="c.rangeIndex === 1">({{ $t('data_fill.data.end') }})</span>
              </template>
              <template slot-scope="scope">
                <span
                  v-if="c.date && scope.row.data[c.props]"
                  style="white-space:nowrap; width: fit-content"
                  :title="formatDate(scope.row.data[c.props], c.dateType)"
                >
                  {{ formatDate(scope.row.data[c.props], c.dateType) }}
                </span>
                <template v-else-if="(c.type === 'select' && c.multiple || c.type === 'checkbox') && scope.row.data[c.props]">
                  <div
                    v-for="(x, $index) in JSON.parse(scope.row.data[c.props])"
                    :key="$index"
                    style="white-space:nowrap; width: fit-content"
                    :title="x"
                  >
                    {{ x }}
                  </div>
                </template>
                <span
                  v-else
                  style="white-space:nowrap; width: fit-content"
                  :title="scope.row.data[c.props]"
                >
                  {{ scope.row.data[c.props] }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('data_fill.data.recent_committer')"
              fixed="right"
              width="100"
            >
              <template slot-scope="scope">
                {{ scope.row.logInfo && scope.row.logInfo.commitByName ? scope.row.logInfo.commitByName : (scope.row.logInfo && scope.row.logInfo.commitBy ? scope.row.logInfo.commitBy : '-' ) }}
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('data_fill.data.recent_commit_time')"
              fixed="right"
              width="160"
            >
              <template slot-scope="scope">
                {{ scope.row.logInfo && scope.row.logInfo.commitTime ? (new Date(scope.row.logInfo.commitTime)).format("yyyy-MM-dd hh:mm:ss") : '-' }}
              </template>
            </el-table-column>
            <el-table-column
              :label="$t('data_fill.form.operation')"
              width="160"
              fixed="right"
            >
              <template slot-scope="scope">
                <el-button
                  v-if="hasDataPermission('write', param.privileges)"
                  type="text"
                  @click="updateRow(scope.row.data)"
                >
                  {{ $t('data_fill.form.modify') }}
                </el-button>
                <el-button
                  type="text"
                  @click="openRow(scope.row.data)"
                >
                  {{ $t('data_fill.form.show') }}
                </el-button>
                <el-button
                  v-if="hasDataPermission('write', param.privileges)"
                  type="text"
                  @click="deleteRow(scope.row.data[paginationConfig.key])"
                >
                  {{ $t('data_fill.form.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </template>
    </el-container>
    <el-container
      v-if="tabActive === 'record'"
      direction="vertical"
      style="display: flex; flex-direction: column"
    >
      <template>
        <div style="margin-bottom: 12px; height: 32px;">
          <el-row>
            <el-col
              :span="8"
              :offset="16"
            >
              <el-input
                ref="search2"
                v-model="operateName"
                :placeholder="$t('data_fill.form.operator')"
                prefix-icon="el-icon-search"
                class="name-email-search"
                size="small"
                clearable
                @blur="searchTableRecordData"
                @clear="entryKey('record')"
                @keyup.enter.native="entryKey('record')"
              />
            </el-col>
          </el-row>
        </div>
        <div style="flex: 1">
          <grid-table
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            stripe
            :table-data="records"
            :columns="[]"
            current-row-key="id"
            :pagination="recordPaginationConfig"
            @size-change="recordHandleSizeChange"
            @current-change="recordHandleCurrentChange"
          >
            <el-table-column
              key="operate"
              prop="operate"
              :label="$t('data_fill.form.operation')"
            >
              <template slot-scope="scope">
                <template v-if="scope.row.operate === 'INSERT'">
                  {{ $t('data_fill.data.insert_data') }}
                </template>
                <template v-else-if="scope.row.operate === 'UPDATE'">
                  {{ $t('data_fill.data.update_data') }}
                </template>
                <template v-else-if="scope.row.operate === 'DELETE'">
                  {{ $t('data_fill.data.delete_data') }}
                </template>
                <template v-else>
                  {{ scope.row.operate }}
                </template>
              </template>
            </el-table-column>
            <el-table-column
              key="commitByName"
              prop="commitByName"
              :label="$t('data_fill.form.operator')"
            >
              <template slot-scope="scope">
                {{ scope.row.commitByName ? scope.row.commitByName: scope.row.commitBy }}
              </template>
            </el-table-column>
            <el-table-column
              key="commitTime"
              prop="commitTime"
              :label="$t('data_fill.form.operate_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.commitTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>
            <el-table-column
              key="id"
              :label="$t('data_fill.form.operation')"
              prop="id"
            >
              <template slot-scope="scope">
                <el-button
                  v-if="scope.row.operate !== 'DELETE'"
                  type="text"
                  @click="showData(scope.row)"
                >
                  {{ $t('data_fill.form.show_data') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </template>
    </el-container>
    <el-container
      v-if="tabActive === 'task'"
      direction="vertical"
      style="display: flex; flex-direction: column"
    >
      <template>
        <div style="margin-bottom: 12px; height: 32px;">
          <el-row>
            <el-col :span="8">
              <el-button
                icon="el-icon-plus"
                size="small"
                @click="addTask"
              >{{ $t('data_fill.task.add_task') }}</el-button>
            </el-col>
            <el-col
              :span="8"
              :offset="8"
            >
              <el-input
                ref="search3"
                v-model="taskName"
                :placeholder="$t('data_fill.task.name')"
                prefix-icon="el-icon-search"
                class="name-email-search"
                size="small"
                clearable
                @blur="searchFormTaskData"
                @clear="entryKey('task')"
                @keyup.enter.native="entryKey('task')"
              />
            </el-col>
          </el-row>
        </div>
        <div style="flex: 1">
          <grid-table
            v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
            style="width: 100%; height: 100%"
            stripe
            :table-data="tasks"
            :columns="[]"
            current-row-key="id"
            :pagination="taskPaginationConfig"
            @size-change="taskHandleSizeChange"
            @current-change="taskHandleCurrentChange"
          >
            <el-table-column
              key="name"
              prop="name"
              :label="$t('data_fill.task.name')"
            >
              <template slot-scope="scope">
                {{ scope.row.name }}
              </template>
            </el-table-column>
            <el-table-column
              key="rateType"
              prop="rateType"
              :label="$t('data_fill.task.rate_type')"
            >
              <template slot-scope="scope">
                {{ scope.row.rateType === -1 ? $t('emailtask.single_task') : $t('emailtask.simple_repeat') }}
              </template>
            </el-table-column>
            <el-table-column
              key="status"
              prop="status"
              :label="$t('data_fill.task.task_status')"
            >
              <template slot-scope="scope">
                <span :class="['de-status', `de-${taskStatusFormatter(scope.row.status)}`]">{{
                  $t(`xpacktask.${taskStatusFormatter(scope.row.status)}`)
                }}
                </span>
              </template>
            </el-table-column>
            <el-table-column
              key="creatorName"
              prop="creatorName"
              :label="$t('data_fill.task.creator')"
            >
              <template slot-scope="scope">
                {{ scope.row.creatorName }}
              </template>
            </el-table-column>
            <el-table-column
              key="createTime"
              prop="createTime"
              :label="$t('data_fill.task.create_time')"
            >
              <template slot-scope="scope">
                {{ new Date(scope.row.createTime).format("yyyy-MM-dd hh:mm:ss") }}
              </template>
            </el-table-column>
            <el-table-column
              key="id"
              :label="$t('data_fill.form.operation')"
              prop="id"
            >
              <template slot-scope="scope">
                <el-button
                  type="text"
                  @click="editTask(scope.row)"
                >
                  {{ $t('data_fill.task.edit') }}
                </el-button>
                <el-button
                  v-if="!scope.row.status"
                  type="text"
                  @click="enableTask(scope.row)"
                >
                  {{ $t('data_fill.task.start') }}
                </el-button>
                <el-button
                  v-if="scope.row.status"
                  type="text"
                  @click="disableTask(scope.row)"
                >
                  {{ $t('data_fill.task.stop') }}
                </el-button>
                <el-button
                  type="text"
                  @click="deleteTask(scope.row)"
                >
                  {{ $t('data_fill.task.delete') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </template>

    </el-container>

    <el-drawer
      :visible.sync="showDrawer"
      direction="btt"
      size="100%"
      append-to-body
      :with-header="false"
    >
      <edit-form-data
        v-if="showDrawer"
        :id="selectedDataId"
        :data="selectedData"
        :form-title.sync="param.name"
        :form-id="param.id"
        :forms.sync="forms"
        :title.sync="createTitle"
        :show-drawer.sync="showDrawer"
        :readonly.sync="drawerReadonly"
        @save-success="onSaveSuccess"
      />
    </el-drawer>

    <el-drawer
      :visible.sync="showTaskDrawer"
      direction="btt"
      size="100%"
      append-to-body
      :with-header="false"
    >
      <create-task
        v-if="showTaskDrawer"
        :id="selectedTaskId"
        :form-id="param.id"
        :show-drawer.sync="showTaskDrawer"
        @save-success="onSaveTaskSuccess"
      />
    </el-drawer>

  </div>
</template>

<script>
import {
  deleteData,
  deleteFormTasks, disableFormTasks, downloadTemplate, enableFormTasks,
  searchCommitLogs,
  searchFormTasks,
  searchTable
} from '@/views/dataFilling/form/dataFilling'
import GridTable from '@/components/gridTable/index.vue'
import { forEach, forIn, includes, filter, map } from 'lodash-es'
import EditFormData from '@/views/dataFilling/form/EditFormData.vue'
import CreateTask from '@/views/dataFilling/form/CreateTask.vue'
import i18n from '@/lang'
import { getToken, setToken } from '@/utils/auth'
import { $alert } from '@/utils/message'
import store from '@/store'
import Config from '@/settings'

const token = getToken()
const RefreshTokenKey = Config.RefreshTokenKey

export default {
  name: 'ViewTable',
  components: { EditFormData, GridTable, CreateTask },
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
      baseUrl: process.env.VUE_APP_BASE_API,
      headers: {
        Authorization: token,
        'Accept-Language': i18n.locale.replace('_', '-')
      },
      dataTableShow: true,
      fileList: [],
      uploading: false,
      operateName: '',
      taskName: '',
      showDrawer: false,
      showTaskDrawer: false,
      filedList: [],
      data: [],
      records: [],
      tasks: [],
      selectedDataId: undefined,
      selectedTaskId: undefined,
      selectedData: undefined,
      createTitle: undefined,
      drawerReadonly: true,
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
        key: ''
      },
      recordPaginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      taskPaginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      tabActive: 'dataPreview',
      tableViewRowForm: {
        row: 1000
      },
      tabStatus: false

    }
  },
  computed: {
    hideCustomDs: function() {
      return this.$store.getters.hideCustomDs
    },
    forms: function() {
      if (this.param?.forms) {
        return JSON.parse(this.param.forms)
      }
      return []
    },
    columns: function() {
      const _list = []
      forEach(filter(this.forms, f => !f.removed), f => {
        if (f.type === 'dateRange') {
          _list.push({
            props: f.settings?.mapping?.columnName1,
            label: f.settings?.name,
            date: true,
            dateType: f.settings?.dateType ? f.settings?.dateType : (f.settings?.enableTime ? 'datetimerange' : 'daterange'),
            type: f.type,
            multiple: !!f.settings.multiple,
            rangeIndex: 0
          })
          _list.push({
            props: f.settings?.mapping?.columnName2,
            label: f.settings?.name,
            date: true,
            dateType: f.settings?.dateType ? f.settings?.dateType : (f.settings?.enableTime ? 'datetimerange' : 'daterange'),
            type: f.type,
            multiple: !!f.settings.multiple,
            rangeIndex: 1
          })
        } else {
          _list.push({
            props: f.settings?.mapping?.columnName,
            label: f.settings?.name,
            date: f.type === 'date',
            dateType: f.type === 'date' ? (f.settings?.dateType ? f.settings?.dateType : (f.settings?.enableTime ? 'datetime' : 'date')) : undefined,
            type: f.type,
            multiple: !!f.settings.multiple
          })
        }
      })
      return _list
    },
    dateFormatColumns: function() {
      return map(filter(this.columns, c => c.date), 'props')
    }
  },
  watch: {
    param: function(newValue, oldValue) {
      this.tabActive = 'dataPreview'
      if (newValue.id !== oldValue.id) {
        this.data = []
        this.records = []
        this.tasks = []
        this.dataTableShow = false
        this.$nextTick(() => {
          this.dataTableShow = true
        })
      }
      this.initTable(this.param.id)
    },
    tabActive: function(newVal, oldVal) {
      this.resetPage()
      if (newVal === 'record') {
        this.searchTableRecordData()
      } else if (newVal === 'task') {
        this.searchFormTaskData()
      } else {
        this.searchTableData()
      }
    }
  },
  mounted() {
    this.initTable(this.param.id)
  },
  methods: {
    beforeUpload() {
      this.uploading = true
    },
    uploadFail(response, file, fileList) {
      let myError = response.toString()
      myError = myError.replace('Error: ', '')

      if (myError.indexOf('AuthenticationException') >= 0) {
        const message = i18n.t('login.tokenError')
        $alert(
          message,
          () => {
            store.dispatch('user/logout').then(() => {
              location.reload()
            })
          },
          {
            confirmButtonText: i18n.t('login.re_login'),
            showClose: false
          }
        )
        return
      }

      const errorMessage = JSON.parse(myError).message

      this.fileList = []
      this.uploading = false
      this.$message({
        type: 'error',
        message: errorMessage,
        showClose: true
      })
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.fileList = fileList

      if (response.headers && response.headers[RefreshTokenKey]) {
        const refreshToken = response.headers[RefreshTokenKey]
        setToken(refreshToken)
        store.dispatch('user/refreshToken', refreshToken)
      }

      this.initTable()
    },
    entryKey(type) {
      if (type === 'record') {
        this.$refs.search2.focus()
        this.$refs.search2.blur()
      } else if (type === 'task') {
        this.$refs.search3.focus()
        this.$refs.search3.blur()
      }
    },
    initTable(id) {
      this.resetPage()
      this.searchTableData()
    },
    taskStatusFormatter(status) {
      if (status) return 'running'
      return 'stopped'
    },
    searchTableData() {
      searchTable(this.param.id, {
        currentPage: this.paginationConfig.currentPage,
        pageSize: this.paginationConfig.pageSize
      }).then(res => {
        if (res.data) {
          this.paginationConfig.key = res.data.key
          this.paginationConfig.total = res.data.total
          this.paginationConfig.currentPage = res.data.currentPage
          const _data = []
          forEach(res.data.data, d => {
            const obj = {}
            forIn(d.data, (value, key) => {
              if (includes(this.dateFormatColumns, key)) {
                if (value) {
                  obj[key] = new Date(value)
                } else {
                  obj[key] = undefined
                }
              } else {
                obj[key] = value === null ? undefined : value
              }
            })
            _data.push({
              data: obj,
              logInfo: d.logInfo
            })
          })
          this.data = _data
        }
      })
    },

    searchTableRecordData() {
      searchCommitLogs(this.param.id, { commitByName: this.operateName }, this.recordPaginationConfig.currentPage, this.recordPaginationConfig.pageSize).then(res => {
        if (res.data) {
          this.recordPaginationConfig.total = res.data.itemCount
          this.records = res.data.listObject
        }
      })
    },

    searchFormTaskData() {
      searchFormTasks(this.param.id, { name: this.taskName }, this.taskPaginationConfig.currentPage, this.taskPaginationConfig.pageSize).then(res => {
        if (res.data) {
          this.taskPaginationConfig.total = res.data.itemCount
          this.tasks = res.data.listObject
        }
      })
    },

    columnsChange() {
      const arr = this.data
      this.data = []
      this.$nextTick(() => {
        this.data = arr
      })
    },

    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.searchTableData()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.searchTableData()
    },
    recordHandleSizeChange(pageSize) {
      this.recordPaginationConfig.currentPage = 1
      this.recordPaginationConfig.pageSize = pageSize
      this.searchTableRecordData()
    },
    recordHandleCurrentChange(currentPage) {
      this.recordPaginationConfig.currentPage = currentPage
      this.searchTableRecordData()
    },
    taskHandleSizeChange(pageSize) {
      this.taskPaginationConfig.currentPage = 1
      this.taskPaginationConfig.pageSize = pageSize
      this.searchFormTaskData()
    },
    taskHandleCurrentChange(currentPage) {
      this.taskPaginationConfig.currentPage = currentPage
      this.searchFormTaskData()
    },

    showTab() {
      this.tabStatus = true
    },
    hideTab() {
      this.tabStatus = false
    },

    resetPage() {
      this.operateName = ''
      this.taskName = ''
      this.paginationConfig = {
        currentPage: 1,
        pageSize: 10,
        total: 0,
        key: ''
      }
      this.recordPaginationConfig = {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
      this.taskPaginationConfig = {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    },

    tabClick() {
      /* if (this.tabActive === 'dataPreview') {
        this.initTable(this.param.id)
      }*/
      /* if (this.tabActive === 'record') {
        this.searchTableRecordData()
      }
      if (this.tabActive === 'task') {
        this.searchFormTaskData()
      }*/
    },

    editForm(param) {
      this.$emit('editForm', param)
    },

    showData(row) {
      searchTable(this.param.id, {
        primaryKeyValue: row.dataId,
        currentPage: 1,
        pageSize: 10
      }).then(res => {
        if (res.data) {
          const obj = {}
          if (res.data.data.length === 0) {
            this.$message({
              message: this.$t('data_fill.data.id_is') + row.dataId + this.$t('data_fill.data.data_not_found'),
              type: 'warning',
              showClose: true
            })
            return
          }
          forIn(res.data.data[0].data, (value, key) => {
            if (includes(this.dateFormatColumns, key)) {
              if (value) {
                obj[key] = new Date(value)
              } else {
                obj[key] = undefined
              }
            } else {
              obj[key] = value
            }
          })
          this.openRow(obj)
        }
      })
    },

    openRow(data) {
      this.selectedDataId = data.id
      this.selectedData = data

      this.showDrawer = true
      this.drawerReadonly = true
      this.createTitle = undefined
    },
    updateRow(data) {
      this.selectedDataId = data.id
      this.selectedData = data

      this.showDrawer = true
      this.drawerReadonly = false
      this.createTitle = undefined
    },
    addData() {
      this.selectedDataId = undefined
      this.selectedData = {}

      this.showDrawer = true
      this.drawerReadonly = false
      this.createTitle = this.$t('data_fill.data.add_data')
    },
    downloadTemplate() {
      downloadTemplate(this.param.id).then(res => {
        const blob = new Blob([res])
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = this.param.name + '.xlsx' // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      })
    },
    deleteRow(id) {
      this.$confirm(
        this.$t('data_fill.data.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteData(this.param.id, id).then((response) => {
          this.searchTableData()
        })
      }).catch(() => {
      })
    },
    formatDate(value, dateType) {
      if (!value) {
        return value
      }
      switch (dateType) {
        case 'year':
          return value.format('yyyy')
        case 'month':
        case 'monthrange':
          return value.format('yyyy-MM')
        case 'datetime':
        case 'datetimerange':
          return value.format('yyyy-MM-dd hh:mm:ss')
        default:
          return value.format('yyyy-MM-dd')
      }
    },

    onSaveSuccess() {
      this.showDrawer = false
      this.searchTableData()
    },

    addTask() {
      this.selectedTaskId = undefined
      this.showTaskDrawer = true
    },
    editTask(row) {
      this.selectedTaskId = row.id
      this.showTaskDrawer = true
    },
    deleteTask(row) {
      this.$confirm(
        this.$t('data_fill.data.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        deleteFormTasks(row.id).then(res => {
          this.searchFormTaskData()
        })
      })
    },
    enableTask(row) {
      this.$confirm(
        this.$t('data_fill.task.confirm_enable'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        enableFormTasks(row.id).then(res => {
          setTimeout(() => {
            this.searchFormTaskData()
          }, 1000)
        })
      })
    },
    disableTask(row) {
      this.$confirm(
        this.$t('data_fill.task.confirm_disable'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      ).then(() => {
        disableFormTasks(row.id).then(res => {
          setTimeout(() => {
            this.searchFormTaskData()
          }, 1000)
        })
      })
    },

    onSaveTaskSuccess() {
      this.showTaskDrawer = false
      this.searchFormTaskData()
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
  background: rgba(255, 255, 255, 1);
  padding: 10px 24px 48px;
  height: 100%;
  overflow-y: hidden;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;

  .de-dataset-name {
    display: flex;
    font-family: AlibabaPuHuiTi;
    align-items: center;
    margin-bottom: 12px;

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

.info-tab {
  width: 100%;
  padding: 0 4px;
  font-family: AlibabaPuHuiTi;
  box-sizing: border-box;

  .title-type {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 8px;
    color: var(--deTextPrimary, #1f2329);
  }

  .info-card {
    padding-bottom: 4px;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
    margin-bottom: 12px;

    .info-item {
      font-family: AlibabaPuHuiTi;
      font-weight: 400;
      margin: 6px 0 12px 0;
    }

    .info-title {
      margin: 0 !important;
      font-weight: 600;
      font-size: 12px;
    }

    .info-content {
      font-size: 12px;
      margin: 0 !important;
    }
  }

  :last-child {
    border: none;
  }

}

.de-status {
  position: relative;
  margin-left: 15px;
  &::before {
    content: "";
    position: absolute;
    top: 50%;
    left: -13px;
    transform: translateY(-50%);
    width: 5px;
    height: 5px;
    border-radius: 50%;
  }
}

.de-running {
  &::before {
    background: var(--deSuccess, #67C23A);
  }
}

.de-stopped {
  &::before {
    background: var(--deInfo, #909399);
  }
}

</style>
