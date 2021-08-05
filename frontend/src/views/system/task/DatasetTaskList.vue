<template>
  <el-col>
    <el-row style="margin-top: 10px;" v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
      <complex-table :data="data" :columns="columns" local-key="datasetTask" :transCondition="transCondition" :search-config="searchConfig" :pagination-config="paginationConfig" @select="select" @search="search" @sort-change="sortChange">
        <template #toolbar>
          <el-button icon="el-icon-circle-plus-outline" @click="selectDataset">{{ $t('dataset.task.create') }}</el-button>
        </template>

        <el-table-column prop="name" :label="$t('dataset.task_name')">
          <template slot-scope="scope">
            <span>
              <el-link style="font-size: 12px" @click="jumpTaskRecord(scope.row)">{{ scope.row.name }}</el-link>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="datasetName" :label="$t('dataset.task.dataset')" />
        <el-table-column prop="rate" :label="$t('dataset.execute_rate')">
          <template slot-scope="scope">
            <span v-if="scope.row.rate === 'SIMPLE'">{{ $t('dataset.execute_once') }}</span>
            <span v-if="scope.row.rate === 'CRON'">{{ $t('dataset.cron_config') }}</span>
            <span v-if="scope.row.rate === 'SIMPLE_CRON'">{{ $t('dataset.simple_cron') }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="lastExecTime" :label="$t('dataset.task.last_exec_time')">
          <template slot-scope="scope">
            <span v-if="scope.row.lastExecTime && scope.row.lastExecTime != -1">
              {{ scope.row.lastExecTime | timestampFormatDate }}
            </span>
            <span v-if="scope.row.lastExecTime === -1" />
          </template>
        </el-table-column>

        <el-table-column prop="lastExecStatus" :label="$t('dataset.task.last_exec_status')">
          <template slot-scope="scope">
            <span v-if="scope.row.lastExecStatus === 'Completed'" style="color: green">{{ $t('dataset.completed') }}</span>
            <span v-if="scope.row.lastExecStatus === 'Underway'" style="color: blue">
              <i class="el-icon-loading" />
              {{ $t('dataset.underway') }}
            </span>
            <span v-if="scope.row.lastExecStatus === 'Error'" style="color: red">
              <el-link type="danger" style="font-size: 12px" @click="showErrorMassage(scope.row.msg)">{{ $t('dataset.error') }}</el-link>
            </span>

          </template>
        </el-table-column>

        <el-table-column prop="nextExecTime" :label="$t('dataset.task.next_exec_time')">
          <template slot-scope="scope">
            <span v-if="scope.row.nextExecTime && scope.row.nextExecTime !== -1 && scope.row.rate !== 'SIMPLE'&& scope.row.status !== 'Pending'">
              {{ scope.row.nextExecTime | timestampFormatDate }}
            </span>
            <span v-if="!scope.row.nextExecTime || scope.row.rate === 'SIMPLE'" />
          </template>

        </el-table-column>

        <el-table-column prop="status" :label="$t('dataset.task.task_status')">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'Underway'" style="color: green">
              <el-link type="success" style="font-size: 12px" @click="changeTaskStatus(scope.row)">{{ $t('dataset.task.underway') }}</el-link>
            </span>
            <span v-if="scope.row.status === 'Stopped'" style="color: red">
              <div type="danger" style="font-size: 12px">{{ $t('dataset.task.stopped') }}</div>
            </span>
            <span v-if="scope.row.status === 'Pending'" style="color: blue">
              <el-link type="primary" style="font-size: 12px" @click="changeTaskStatus(scope.row)">{{ $t('dataset.task.pending') }}</el-link>
            </span>
            <span v-if="scope.row.status === 'Exec'" style="color: blue">
              <i class="el-icon-loading" />
              {{ $t('dataset.underway') }}
            </span>
          </template>
        </el-table-column>

        <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />
      </complex-table>
    </el-row>

    <el-dialog v-dialogDrag :title="update_task_dialog_title" :visible="update_task" :show-close="false" width="50%" class="dialog-css" append-to-body>
      <el-col>
        <el-form :form="taskForm"  ref="taskForm" :model="taskForm" label-width="100px" size="mini" :rules="taskFormRules">
          <el-form-item :label="$t('dataset.task_name')" prop="name">
            <el-input v-model="taskForm.name" size="mini" style="width: 50%" :placeholder="$t('dataset.task_name')" />
          </el-form-item>
          <el-form-item :label="$t('dataset.update_type')" prop="type">
            <el-select v-model="taskForm.type" size="mini">
              <el-option :label="$t('dataset.all_scope')" value="all_scope" />
              <el-option :label="$t('dataset.add_scope')" value="add_scope" />
            </el-select>
          </el-form-item>
          <el-form-item :label="$t('dataset.execute_rate')" prop="rate">
            <el-select v-model="taskForm.rate" size="mini" @change="onRateChange">
              <el-option :label="$t('dataset.execute_once')" value="SIMPLE" />
              <el-option :label="$t('dataset.cron_config')" value="CRON" />
              <el-option :label="$t('dataset.simple_cron')" value="SIMPLE_CRON" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="taskForm.rate === 'CRON'" label="">
            <el-popover v-model="cronEdit">
              <cron v-model="taskForm.cron" @close="cronEdit = false" />
              <el-input slot="reference" v-model="taskForm.cron" size="mini" style="width: 50%" @click="cronEdit = true" />
            </el-popover>
          </el-form-item>

          <el-form-item v-if="taskForm.rate === 'SIMPLE_CRON'" label="">
            <el-form :inline="true">
              <el-form-item :label="$t('cron.every')">
                <el-input v-model="taskForm.extraData.simple_cron_value" size="mini" type="number" min="1" @change="onSimpleCronChange()" />
              </el-form-item>

              <el-form-item class="form-item">
                <el-select v-model="taskForm.extraData.simple_cron_type" filterable size="mini" @change="onSimpleCronChange()">
                  <el-option :label="$t('cron.minute_default')" value="minute" />
                  <el-option :label="$t('cron.hour_default')" value="hour" />
                  <el-option :label="$t('cron.day_default')" value="day" />
                </el-select>
              </el-form-item>

              <el-form-item class="form-item" :label="$t('cron.every_exec')" />
            </el-form>
          </el-form-item>

          <el-form-item v-if="taskForm.rate !== 'SIMPLE'" :label="$t('dataset.start_time')" prop="startTime">
            <el-date-picker v-model="taskForm.startTime" type="datetime" :placeholder="$t('dataset.select_data_time')" size="mini" />
          </el-form-item>
          <el-form-item v-if="taskForm.rate !== 'SIMPLE'" :label="$t('dataset.end_time')" prop="end">
            <el-select v-model="taskForm.end" size="mini">
              <el-option :label="$t('dataset.no_limit')" value="0" />
              <el-option :label="$t('dataset.set_end_time')" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="taskForm.end === '1'" label="">
            <el-date-picker v-model="taskForm.endTime" type="datetime" :placeholder="$t('dataset.select_data_time')" size="mini" />
          </el-form-item>

          <el-form-item v-if="taskForm.type === 'add_scope'" :label="$t('dataset.incremental_update_type')">
            <el-radio-group v-model="incrementalUpdateType" size="mini" @change="incrementalUpdateTypeChange">
              <el-radio label="incrementalAdd">{{ $t('dataset.incremental_add') }}</el-radio>
              <el-radio label="incrementalDelete">{{ $t('dataset.incremental_delete') }}</el-radio>
            </el-radio-group>

          </el-form-item>

          <el-form-item v-if="taskForm.type === 'add_scope'" :label="$t('dataset.param')">
            <el-button type="text" size="mini" @click="insertParamToCodeMirror('${__last_update_time__}')">{{ $t('dataset.last_update_time') }}</el-button>
            <el-button type="text" size="mini" @click="insertParamToCodeMirror('${__current_update_time__}')">{{ $t('dataset.current_update_time') }}</el-button>
          </el-form-item>

          <codemirror
            v-if="taskForm.type === 'add_scope'"
            ref="myCm"
            v-model="sql"
            class="codemirror"
            :options="sqlOption"
            @ready="onCmReady"
            @focus="onCmFocus"
            @input="onCmCodeChange"
          />
        </el-form>
      </el-col>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeTask">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveTask(taskForm)">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--添加任务-选择数据集-->
    <el-dialog v-dialogDrag :title="$t('dataset.task.create')" :visible="selectDatasetFlag" :show-close="false" width="70%" class="dialog-css" :destroy-on-close="true">
      <table-selector @getTable="getTable" privileges="manage" :mode="1" :customType=customType  showMode="datasetTask"/>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeCreateTask">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" :disabled="!table.id" @click="create(undefined)">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <el-dialog
      v-dialogDrag
      :title="$t('dataset.detail')"
      :visible="show_error_massage"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <span class="err-msg">{{ error_massage }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="show_error_massage = false">{{ $t('dataset.close') }}</el-button>
      </span>
    </el-dialog>
  </el-col>
</template>

<script>
import ComplexTable from '@/components/business/complex-table'
import { formatCondition, formatQuickCondition, addOrder, formatOrders } from '@/utils/index'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { datasetTaskList, post } from '@/api/dataset/dataset'
import { codemirror } from 'vue-codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/solarized.css'
import 'codemirror/mode/sql/sql.js'
import 'codemirror/addon/selection/active-line.js'
import 'codemirror/addon/edit/closebrackets.js'
import 'codemirror/mode/clike/clike.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/emacs.js'
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/sql-hint'
import 'codemirror/addon/hint/show-hint'
import cron from '@/components/cron/cron'
import TableSelector from '@/views/chart/view/TableSelector'
import { hasDataPermission } from '@/utils/permission'

export default {
  name: 'DatasetTaskList',
  components: { ComplexTable, cron, codemirror, TableSelector },
  props: {
    param: {
      type: Object,
      default: null
    },
    transCondition: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      taskForm: {
        name: '',
        type: 'all_scope',
        startTime: '',
        rate: 'SIMPLE',
        cron: '',
        endTime: '',
        end: '0',
        extraData: {
          simple_cron_type: 'hour',
          simple_cron_value: 1
        }
      },
      update_task: false,
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', type: 'primary', click: this.addTask, disabled: this.disableEdit
        },
        {
          label: this.$t('dataset.task.exec'), icon: 'el-icon-video-play', type: 'success', click: this.execTask, disabled: this.disableExec
        },
        {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.deleteTask, disabled: this.disableDelete
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        useComplexSearch: true,
        quickPlaceholder: this.$t('dataset.task.search_by_name'),
        components: [
          { field: 'dataset_table_task.name', label: this.$t('dataset.task_name'), component: 'DeComplexInput'},
          { field: 'dataset_table_task.id', label: this.$t('dataset.task_id'), component: 'FuComplexInput' },
          { field: 'dataset_table.name', label: this.$t('dataset.name'), component: 'DeComplexInput' },
          { field: 'dataset_table_task.status', label: this.$t('dataset.task.task_status'), component: 'FuComplexSelect',
            options: [{ label: this.$t('dataset.task.stopped'), value: 'Stopped' }, { label: this.$t('dataset.task.underway'), value: 'Underway' }, { label: this.$t('dataset.task.pending'), value: 'Pending' }, { label: this.$t('dataset.underway'), value: 'Exec' }], multiple: false },
          { field: 'dataset_table_task.last_exec_status', label: this.$t('dataset.task.last_exec_status'), component: 'FuComplexSelect', options: [{ label: this.$t('dataset.completed'), value: 'Completed' }, { label: this.$t('dataset.underway'), value: 'Underway' }, { label: this.$t('dataset.error'), value: 'Error' }], multiple: false }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      dialogVisible: false,
      editPasswordVisible: false,
      form: {
        roles: [{
          id: ''
        }]
      },
      checkPasswordForm: {},
      ruleForm: {},
      defaultForm: { id: null, username: null, nickName: null, gender: '男', email: null, enabled: 1, deptId: null, phone: null },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: 'add',
      permission: {
        add: ['user:add'],
        edit: ['user:edit'],
        del: ['user:del'],
        editPwd: ['user:editPwd']
      },
      orderConditions: [],
      last_condition: null,
      update_task_dialog_title: '',
      sqlOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: { // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      incrementalUpdateType: 'incrementalAdd',
      sql: '',
      incrementalConfig: {},
      cronEdit: false,
      lang: this.$store.getters.language === 'en_US' ? 'en' : 'cn',
      selectDatasetFlag: false,
      table: {},
      show_error_massage: false,
      error_massage: '',
      taskFormRules: {
        name: [
          { required: true, message: this.$t('dataset.required'), trigger: 'change' },
          { min: 2, max: 50, message: this.$t('datasource.input_limit_0_50', [2, 50]), trigger: 'blur' }
        ],
        type: [
          { required: true, message: this.$t('dataset.required'), trigger: 'change' }
        ],
        startTime: [
          { required: true, message: this.$t('dataset.required'), trigger: 'change' }
        ],
        rate: [
          { required: true, message: this.$t('dataset.required'), trigger: 'change' }
        ],
        end: [
          { required: true, message: this.$t('dataset.required'), trigger: 'change' }
        ]
      },
      customType: ['db', 'sql']
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  created() {
    this.timer = setInterval(() => {
      this.search(this.last_condition, false)
    }, 10000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.search(this.last_condition)
        return
      }
      if (prop === 'dept') {
        prop = 'u.deptId'
      }
      if (prop === 'status') {
        prop = 'u.enabled'
      }
      this.orderConditions = []
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search(this.last_condition)
    },
    select(selection) {
    },
    search(condition, showLoading = true) {
      this.last_condition = condition
      condition = formatQuickCondition(condition, 'dataset_table_task.name')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      const { currentPage, pageSize } = this.paginationConfig
      datasetTaskList(currentPage, pageSize, param, showLoading).then(response => {
        this.data = response.data.listObject
        this.data.forEach(item => {
          this.taskStatus(item)
        })
        this.paginationConfig.total = response.data.itemCount
      })
    },
    taskStatus(item) {
      post('/dataset/task/lastExecStatus', item, false).then(response => {
        if (!item.lastExecStatus) {
          item.lastExecStatus = response.data.lastExecStatus
        }
        if (!item.lastExecTime) {
          item.lastExecTime = response.data.lastExecTime
        }
        item.msg = response.data.msg
      })
    },
    create(task) {
      if (!task) { // add
        this.selectDatasetFlag = false
        this.resetTaskForm()
        this.taskForm.name = this.table.name + ' ' + this.$t('dataset.task_update')
        this.taskForm.tableId = this.table.id
        this.taskForm.startTime = new Date()
        this.update_task_dialog_title = this.$t('dataset.task_add_title')
      } else { // update
        this.taskForm = JSON.parse(JSON.stringify(task))
        this.update_task_dialog_title = this.$t('dataset.task_edit_title')
      }
      this.update_task = true
    },
    changeTaskStatus(task) {
      let param = JSON.parse(JSON.stringify(task));
      param.status = task.status === 'Underway' ? 'Pending' : 'Underway'
      post('/dataset/task/updateStatus', param).then(response => {
        if(response.success){
          task.status = param.status
          this.$message({
            message: this.$t('dataset.task.change_success'),
            type: 'success',
            showClose: true
          })
        }else {
          this.search(this.last_condition, false)
        }
      }).catch(() => {
        this.search(this.last_condition, false)
      })
    },
    execTask(task) {
      this.$confirm(this.$t('dataset.task.confirm_exec'), this.$t('dataset.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        post('/dataset/task/execTask', task).then(response => {
          this.search(this.last_condition, true)
        })
      }).catch(() => {
      })
    },
    selectDataset() {
      this.selectDatasetFlag = true
    },
    getTable(table) {
      this.table = JSON.parse(JSON.stringify(table))
    },
    closeCreateTask() {
      this.selectDatasetFlag = false
      this.table = {}
    },
    resetTaskForm() {
      this.taskForm = {
        name: '',
        type: 'all_scope',
        startTime: '',
        rate: 'SIMPLE',
        endTime: '',
        end: '0',
        extraData: {
          simple_cron_type: 'hour',
          simple_cron_value: 1
        }
      }
    },
    onSimpleCronChange() {
      if (this.taskForm.extraData.simple_cron_type === 'minute') {
        if (this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 59) {
          this.$message({ message: this.$t('cron.minute_limit'), type: 'warning', showClose: true })
          this.taskForm.extraData.simple_cron_value = 59
        }
        this.taskForm.cron = '0 0/' + this.taskForm.extraData.simple_cron_value + ' * * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'hour') {
        if (this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 23) {
          this.$message({ message: this.$t('cron.hour_limit'), type: 'warning', showClose: true })
          this.taskForm.extraData.simple_cron_value = 23
        }
        this.taskForm.cron = '0 0 0/' + this.taskForm.extraData.simple_cron_value + ' * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'day') {
        if (this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 31) {
          this.$message({ message: this.$t('cron.day_limit'), type: 'warning', showClose: true })
          this.taskForm.extraData.simple_cron_value = 31
        }
        this.taskForm.cron = '0 0 0 1/' + this.taskForm.extraData.simple_cron_value + ' * ? *'
        return
      }
    },
    onRateChange() {
      if (this.taskForm.rate === 'SIMPLE') {
        this.taskForm.end = '0'
        this.taskForm.endTime = ''
        this.taskForm.cron = ''
      }
      if (this.taskForm.rate === 'SIMPLE_CRON') {
        this.taskForm.cron = '0 0 0/1 *  * ? *'
      }
      if (this.taskForm.rate === 'CRON') {
        this.taskForm.cron = '00 00 * ? * * *'
      }
    },
    incrementalUpdateTypeChange: function() {
      if (this.incrementalUpdateType === 'incrementalAdd') {
        if (this.sql) {
          this.incrementalConfig.incrementalDelete = this.sql
        } else {
          this.incrementalConfig.incrementalDelete = ''
        }
        if (this.incrementalConfig.incrementalAdd) {
          this.sql = this.incrementalConfig.incrementalAdd
        } else {
          this.sql = ''
        }
      }

      if (this.incrementalUpdateType === 'incrementalDelete') {
        if (this.sql) {
          this.incrementalConfig.incrementalAdd = this.sql
        } else {
          this.incrementalConfig.incrementalAdd = ''
        }
        if (this.incrementalConfig.incrementalDelete) {
          this.sql = this.incrementalConfig.incrementalDelete
        } else {
          this.sql = ''
        }
      }
    },
    showSQL(val) {
      this.sql = val || ''
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
    },
    onCmCodeChange(newCode) {
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    },
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    },
    cronChange(val) {
      this.taskForm.cron = val
    },
    disableEdit(task) {
      return task.rate === 'SIMPLE' || task.status === 'Stopped' || !hasDataPermission('manage',task.privileges)
    },
    disableExec(task) {
      return task.status === 'Stopped' || task.status === 'Pending' || task.rate === 'SIMPLE' || !hasDataPermission('manage',task.privileges)
    },
    disableDelete(task) {
      return false;
      // !hasDataPermission('manage',task.privileges)
    },
    deleteTask(task) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('dataset.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        post('/dataset/task/delete/' + task.id, null).then(response => {
          this.$message({
            message: this.$t('dataset.delete_success'),
            type: 'success',
            showClose: true
          })
          this.search(this.last_condition, true)
        })
      }).catch(() => {
      })
    },
    addTask(task) {
      if (!task) { // add
        this.resetTaskForm()
        this.taskForm.name = this.table.name + ' ' + this.$t('dataset.task_update')
        this.taskForm.startTime = new Date()
        this.update_task_dialog_title = this.$t('dataset.task_add_title')
      } else { // update
        this.getIncrementalConfig(task.tableId)
        this.taskForm = JSON.parse(JSON.stringify(task))
        this.taskForm.extraData = JSON.parse(this.taskForm.extraData)
        this.update_task_dialog_title = this.$t('dataset.task_edit_title')
      }
      this.update_task = true
    },
    getIncrementalConfig(tableId) {
      post('/dataset/table/incrementalConfig', { tableId: tableId }).then(response => {
        this.incrementalConfig = response.data
        this.incrementalUpdateType = 'incrementalAdd'
        if (this.incrementalConfig.incrementalAdd) {
          this.sql = this.incrementalConfig.incrementalAdd
        } else {
          this.sql = ''
        }
      })
    },
    closeTask() {
      this.update_task = false
      this.resetTaskForm()
    },
    showErrorMassage(massage) {
      this.show_error_massage = true
      this.error_massage = massage
    },
    jumpTaskRecord(item) {
      this.$emit('jumpTaskRecord', item)
    },
    saveTask(task) {
      this.$refs.taskForm.validate(valid => {
        if (valid) {
          if (task.rate !== 'SIMPLE') {
            if (this.incrementalUpdateType === 'incrementalAdd') {
              this.incrementalConfig.incrementalAdd = this.sql
            } else {
              this.incrementalConfig.incrementalDelete = this.sql
            }
            this.incrementalConfig.tableId = task.tableId
          }
          task.startTime = new Date(task.startTime).getTime()
          task.endTime = new Date(task.endTime).getTime()
          const form = JSON.parse(JSON.stringify(task))
          form.extraData = JSON.stringify(form.extraData)
          const dataSetTaskRequest = {
            datasetTableTask: form,
            datasetTableIncrementalConfig: task.type === 'add_scope' ? this.incrementalConfig : undefined
          }
          post('/dataset/task/save', dataSetTaskRequest).then(response => {
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.update_task = false
            this.resetTaskForm()
            this.search(this.last_condition, true)
          })
        }else {
          return false
        }
      })
    },
    handleClose() {
      this.depts = null
      this.formType = 'add'
      this.form = {}
      this.editPasswordVisible = false
      this.dialogVisible = false
    },
    btnDisabled(row) {
      return row.userId === 1
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

.el-radio{
  margin-right: 10px;
}
.el-radio>>>.el-radio__label{
  font-size: 12px;
}

.dialog-css >>> .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css >>> .el-dialog__body {
  padding: 10px 20px 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

.codemirror {
  height: 100px;
  overflow-y: auto;
}
.codemirror >>> .CodeMirror-scroll {
  height: 100px;
  overflow-y: auto;
}

.err-msg{
  font-size: 12px;
  word-break:normal;
  width:auto;
  display:block;
  white-space:pre-wrap;
  word-wrap : break-word ;
  overflow: hidden ;
}

span{
  font-size: 12px;
}
</style>
