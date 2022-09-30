<template>
  <el-col>
    <el-row style="margin: 6px 0 16px 0">
      <el-col :span="12">
        <deBtn
          secondary
          icon="el-icon-plus"
          @click="() => addTask()"
        >{{ $t('dataset.add_task') }}</deBtn>
      </el-col>
      <el-col style="text-align: right" :span="12">
        <el-button type="text" icon="el-icon-document" @click="showConfig">
          {{ $t('dataset.task.record') }}
        </el-button>
      </el-col>
    </el-row>
    <div class="table-task-container">
      <grid-table
        v-loading="loading"
        :table-data="taskData"
        :columns="[]"
        :pagination="paginationConfig"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column
          key="name"
          min-width="178"
          prop="name"
          :label="$t('dataset.task_name')"
        />
        <el-table-column
          key="rate"
          min-width="100"
          prop="rate"
          :label="$t('dataset.execute_rate')"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.rate === 'SIMPLE'">{{
              $t('dataset.execute_once')
            }}</span>
            <span v-if="scope.row.rate === 'CRON'">{{
              $t('dataset.cron_config')
            }}</span>
            <span v-if="scope.row.rate === 'SIMPLE_CRON'">{{
              $t('dataset.simple_cron')
            }}</span>
          </template>
        </el-table-column>

        <el-table-column
          key="lastExecStatus"
          prop="lastExecStatus"
          min-width="140"
          :label="$t('dataset.task.last_exec_status')"
        >
          <template slot-scope="scope">
            <span
              v-if="scope.row.lastExecStatus"
              :class="[`de-${scope.row.lastExecStatus}-pre`, 'de-status']"
            >{{
              $t(`dataset.${scope.row.lastExecStatus.toLocaleLowerCase()}`)
            }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <el-table-column
          key="lastExecTime"
          prop="lastExecTime"
          min-width="178"
          :label="$t('dataset.task.last_exec_time')"
        >
          <template slot-scope="scope">
            <span>
              {{ scope.row.lastExecTime | timestampFormatDate }}
            </span>
          </template>
        </el-table-column>

        <el-table-column
          key="nextExecTime"
          prop="nextExecTime"
          min-width="178"
          :label="$t('dataset.task.next_exec_time')"
        >
          <template slot-scope="scope">
            <span
              v-if="
                scope.row.nextExecTime &&
                  scope.row.nextExecTime !== -1 &&
                  scope.row.rate !== 'SIMPLE' &&
                  scope.row.status !== 'Pending'
              "
            >
              {{ scope.row.nextExecTime | timestampFormatDate }}
            </span>
            <span
              v-if="!scope.row.nextExecTime || scope.row.rate === 'SIMPLE'"
            >-</span>
          </template>
        </el-table-column>

        <el-table-column
          key="status"
          min-width="120"
          prop="status"
          :label="$t('dataset.task.task_status')"
        >
          <template slot-scope="scope">
            <span
              :class="[`de-${scope.row.status}-result`, 'de-status']"
            >{{ $t(`dataset.task.${scope.row.status.toLocaleLowerCase()}`) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          slot="__operation"
          key="__operation"
          :label="$t('commons.operating')"
          fixed="right"
          min-width="100"
        >
          <template slot-scope="scope">
            <el-button
              class="de-text-btn"
              style="margin-left: -4px"
              type="text"
              @click="selectDataset(scope.row)"
            >{{
              $t(disableEdit(scope.row) ? 'auth.view' : 'commons.edit')
            }}</el-button>
            <el-dropdown
              size="medium"
              trigger="click"
              @command="(type) => handleCommand(type, scope.row)"
            >
              <el-button
                style="margin-left: 8px"
                class="el-icon-more de-text-btn"
                type="text"
              />
              <el-dropdown-menu slot="dropdown" class="de-card-dropdown">
                <template
                  v-if="!['Stopped', 'Exec'].includes(scope.row.status)"
                >
                  <el-dropdown-item
                    :disabled="disableExec(scope.row)"
                    command="exec"
                  >
                    {{ $t('components.run_once') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="scope.row.status === 'Pending'"
                    command="contine"
                  >
                    {{ $t('components.continue') }}
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="scope.row.status === 'Underway'"
                    command="pause"
                  >
                    {{ $t('dataset.task.pending') }}
                  </el-dropdown-item>
                </template>
                <el-dropdown-item
                  :disabled="disableDelete(scope.row)"
                  command="delete"
                >
                  {{ $t('commons.delete') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </grid-table>
    </div>

    <el-dialog
      v-dialogDrag
      :title="$t('dataset.detail')"
      :visible="show_error_massage"
      :show-close="false"
      width="50%"
      class="de-dialog-form"
    >
      <span class="err-msg">{{ error_massage }} </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="show_error_massage = false">{{
          $t('dataset.close')
        }}</el-button>
      </span>
    </el-dialog>

    <el-drawer
      v-closePress
      :title="$t('dataset.task.record')"
      :visible.sync="userDrawer"
      custom-class="user-drawer-task"
      size="840px"
      direction="rtl"
    >
      <el-row style="margin-top: 12px">
        <el-table
          :data="taskLogData"
          class="top-border"
          :height="height"
          style="width: 100%"
        >
          <el-table-column prop="name" :label="$t('dataset.task_name')" />
          <el-table-column prop="startTime" :label="$t('dataset.start_time')">
            <template slot-scope="scope">
              <span>{{ scope.row.startTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="endTime" :label="$t('dataset.end_time')">
            <template slot-scope="scope">
              <span>{{ scope.row.endTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="status" :label="$t('dataset.status')">
            <template slot-scope="scope">
              <span
                v-if="scope.row.status"
                :class="[`de-${scope.row.status}-pre`, 'de-status']"
              >{{
                 $t(`dataset.${scope.row.status.toLocaleLowerCase()}`)
               }}
                <svg-icon v-if="scope.row.status === 'Error'" style="cursor: pointer;" icon-class="icon-maybe" class="field-icon-location" @click="showErrorMassage(scope.row.info)" />
              </span>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-row style="margin-top: 10px; text-align: right">
          <el-pagination
            :current-page="page.currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
            @size-change="handleSizeChangeLog"
            @current-change="handleCurrentChangeLog"
          />
        </el-row>
      </el-row>
    </el-drawer>
    <el-drawer
      v-closePress
      :title="header"
      :visible.sync="update_task"
      custom-class="user-drawer update-drawer-task"
      size="680px"
      direction="rtl"
    >
      <el-form
        ref="taskForm"
        :form="taskForm"
        :model="taskForm"
        label-width="100px"
        class="de-form-item"
        :disabled="disableForm"
        :rules="taskFormRules"
      >
        <el-form-item :label="$t('dataset.task_name')" prop="name">
          <el-input
            v-model="taskForm.name"
            size="small"
            :placeholder="$t('fu.search_bar.please_input') + $t('dataset.task_name')"
          />
        </el-form-item>
        <el-form-item :label="$t('dataset.update_type')" prop="type">
          <el-radio-group v-model="taskForm.type">
            <el-radio label="all_scope">{{ $t('dataset.all_scope') }}</el-radio>
            <el-radio label="add_scope">
              {{ $t('dataset.add_scope') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="taskForm.type === 'add_scope' && table.type !== 'api'" class="add-scope-cont">
          <el-form-item
            prop="type"
            :label="$t('dataset.incremental_update_type')"
          >
            <el-radio-group
              v-model="incrementalUpdateType"
              size="small"
              @change="incrementalUpdateTypeChange"
            >
              <el-radio label="incrementalAdd">{{
                $t('dataset.incremental_add')
              }}</el-radio>
              <el-radio label="incrementalDelete">{{
                $t('dataset.incremental_delete')
              }}</el-radio>
            </el-radio-group>
            <div class="param-title">
              <span>{{ $t('dataset.param') }}</span>
              <div class="param-title-btn">
                <el-button
                  type="text"
                  size="small"
                  @click="insertParamToCodeMirror('${__last_update_time__}')"
                >{{ $t('dataset.last_update_time') }}</el-button>
                <el-button
                  type="text"
                  size="small"
                  @click="insertParamToCodeMirror('${__current_update_time__}')"
                >{{ $t('dataset.current_update_time') }}</el-button>
              </div>
            </div>
            <div class="codemirror-cont">
              <codemirror
                ref="myCm"
                v-model="sql"
                class="codemirror"
                :options="sqlOption"
                @ready="onCmReady"
                @focus="onCmFocus"
                @input="onCmCodeChange"
              />
            </div>
          </el-form-item>
        </div>

        <el-form-item :label="$t('dataset.execute_rate')" prop="rate">
          <el-radio-group v-model="taskForm.rate" @change="onRateChange">
            <el-radio label="SIMPLE">{{ $t('dataset.execute_once') }}</el-radio>
            <el-radio label="CRON">{{ $t('dataset.cron_config') }}</el-radio>
            <el-radio label="SIMPLE_CRON">{{
              $t('dataset.simple_cron')
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-if="taskForm.rate !== 'SIMPLE'" class="execute-rate-cont">
          <el-form-item
            v-if="taskForm.rate === 'SIMPLE_CRON'"
            :label="$t('dataset.execute_rate')"
            prop="rate"
          >
            <div class="simple-cron">
              {{ $t('cron.every') }}
              <el-input-number
                v-model="taskForm.extraData.simple_cron_value"
                controls-position="right"
                :min="1"
                size="small"
                @change="onSimpleCronChange()"
              />
              <el-select
                v-model="taskForm.extraData.simple_cron_type"
                filterable
                size="small"
                @change="onSimpleCronChange()"
              >
                <el-option :label="$t('components.minute')" value="minute" />
                <el-option :label="$t('components.hour')" value="hour" />
                <el-option :label="$t('components.day')" value="day" />
              </el-select>
              {{ $t('cron.every_exec') }}
            </div>
          </el-form-item>
          <el-form-item
            v-if="taskForm.rate === 'CRON' && showCron"
            prop="cron"
            :label="$t('emailtask.cron_exp')"
          >
            <el-popover v-model="cronEdit">
              <cron
                v-model="taskForm.cron"
                :is-rate="taskForm.rate === 'CRON'"
                @close="cronEdit = false"
              />
              <el-input
                slot="reference"
                v-model="taskForm.cron"
                size="small"
                style="width: 50%"
                @click="cronEdit = true"
              />
            </el-popover>
          </el-form-item>
          <el-form-item
            v-if="taskForm.rate !== 'SIMPLE'"
            :label="$t('dataset.start_time')"
            prop="startTime"
          >
            <el-date-picker
              v-model="taskForm.startTime"
              class="de-date-picker"
              type="datetime"
              :placeholder="$t('dataset.start_time')"
              size="small"
            />
            <svg-icon
              icon-class="icon_calendar_outlined"
              class="icon-calendar-outlined"
            />
          </el-form-item>
          <el-form-item
            v-if="taskForm.rate !== 'SIMPLE'"
            :label="$t('dataset.end_time')"
            prop="end"
          >
            <el-radio-group v-model="taskForm.end">
              <el-radio label="0">{{ $t('dataset.no_limit') }}</el-radio>
              <el-radio label="1"> {{ $t('dataset.set_end_time') }}</el-radio>
            </el-radio-group>
            <el-date-picker
              v-if="taskForm.end === '1'"
              v-model="taskForm.endTime"
              class="de-date-picker"
              type="datetime"
              :placeholder="$t('dataset.end_time')"
              size="small"
            />
            <svg-icon
              v-if="taskForm.end === '1'"
              icon-class="icon_calendar_outlined"
              class="icon-calendar-outlined"
            />
          </el-form-item>
        </div>
      </el-form>
      <div class="de-foot">
        <deBtn secondary @click="closeTask">{{ $t('dataset.cancel') }}</deBtn>
        <deBtn v-if="!disableForm" type="primary" @click="saveTask(taskForm)">{{
          $t('dataset.confirm')
        }}</deBtn>
      </div>
    </el-drawer>
  </el-col>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import { codemirror } from 'vue-codemirror'
// 核心样式
import 'codemirror/lib/codemirror.css'
// 引入主题后还需要在 options 中指定主题才会生效
import 'codemirror/theme/solarized.css'
import 'codemirror/mode/sql/sql.js'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// closebrackets
import 'codemirror/addon/edit/closebrackets.js'
// keyMap
import 'codemirror/mode/clike/clike.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/emacs.js'
// 引入代码自动提示插件
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/sql-hint'
import 'codemirror/addon/hint/show-hint'
// vue-cron
import cron from '@/components/cron/cron'
import { hasDataPermission } from '@/utils/permission'
import { engineMode } from '@/api/system/engine'
import GridTable from '@/components/gridTable/index.vue'
export default {
  name: 'UpdateInfo',
  components: { codemirror, cron, GridTable },
  props: {
    table: {
      type: Object,
      default: null
    },
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      userDrawer: false,
      showCron: false,
      taskDrawer: false,
      height: 500,
      update_setting: false,
      update_task: false,
      show_error_massage: false,
      update_task_dialog_title: '',
      error_massage: '',
      disableForm: false,
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
      page: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      taskLogData: [],
      taskData: [],
      taskFormRules: {
        name: [
          {
            required: true,
            message: this.$t('dataset.required'),
            trigger: 'change'
          },
          {
            min: 2,
            max: 50,
            message: this.$t('datasource.input_limit_2_50', [2, 50]),
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: this.$t('dataset.required'),
            trigger: 'change'
          }
        ],
        startTime: [
          {
            required: true,
            message: this.$t('components.time_is_required'),
            trigger: 'change'
          }
        ],
        rate: [
          {
            required: true,
            message: this.$t('dataset.required'),
            trigger: 'change'
          }
        ],
        end: [
          {
            required: true,
            message: this.$t('dataset.required'),
            trigger: 'change'
          }
        ]
      },
      sqlOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: {
          // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      incrementalUpdateType: 'incrementalAdd',
      sql: '',
      incrementalConfig: {},
      cronEdit: false,
      lang: this.$store.getters.language === 'en_US' ? 'en' : 'cn',
      taskLastRequestComplete: true,
      taskLogLastRequestComplete: true,
      enableUpdate: true,
      engineMode: 'local'
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    },
    header() {
      return this.disableForm
        ? '查看任务'
        : this.taskForm.id
          ? '编辑任务'
          : this.$t('dataset.add_task')
    }
  },
  watch: {
    table: {
      handler() {
        if (hasDataPermission('manage', this.param.privileges)) {
          this.listTask()
          this.getIncrementalConfig()
        }
        this.listTaskLog()
      },
      immediate: true
    }
  },
  mounted() {
    this.calHeight()
  },
  created() {
    this.taskLogTimer = setInterval(() => {
      if (!this.taskLogLastRequestComplete) {
        return
      } else {
        this.taskLogLastRequestComplete = false
      }
      this.listTaskLog(false)
    }, 10000)

    this.taskTimer = setInterval(() => {
      if (!this.taskLastRequestComplete) {
        return
      } else {
        this.taskLastRequestComplete = false
      }
      if (hasDataPermission('manage', this.param.privileges)) {
        this.listTask(false)
      }
    }, 10000)

    engineMode().then((res) => {
      this.engineMode = res.data
      if (this.engineMode === 'simple') {
        if (this.table.type === 'api') {
          this.enableUpdate = true
        } else {
          this.enableUpdate = false
        }
      } else {
        if (this.table.type === 'excel') {
          this.enableUpdate = false
        } else {
          this.enableUpdate = true
        }
      }
    })
  },
  beforeDestroy() {
    clearInterval(this.taskTimer)
    clearInterval(this.taskLogTimer)
  },
  methods: {
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 55 - 38 - 28 - 10
      }, 10)
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
    showConfig() {
      this.userDrawer = true
      this.listTaskLog()
    },
    showErrorMassage(massage) {
      this.show_error_massage = true
      this.error_massage = massage
    },
    addTask(task) {
      if (!task) {
        // add
        this.resetTaskForm()
        this.disableForm = false
        this.taskForm.name =
          this.table.name + ' ' + this.$t('dataset.task_update')
        this.taskForm.startTime = new Date()
        this.update_task_dialog_title = this.$t('dataset.task_add_title')
      } else {
        // update
        this.taskForm = JSON.parse(JSON.stringify(task))
        this.taskForm.extraData = JSON.parse(this.taskForm.extraData) || {
          simple_cron_type: 'hour',
          simple_cron_value: 1
        }
        this.showCron = this.taskForm.rate === 'CRON'
        this.update_task_dialog_title = this.$t('dataset.task_edit_title')
      }
      this.update_task = true
    },
    listTask(loading = true) {
      this.loading = loading
      const { currentPage, pageSize } = this.paginationConfig
      post(
        `/dataset/task/list/${currentPage}/${pageSize}`,
        { tableId: this.table.id },
        loading
      )
        .then((response) => {
          const { itemCount, listObject } = response.data
          this.taskData = listObject
          this.paginationConfig.total = itemCount
          this.loading = false
          this.taskLastRequestComplete = true
        })
        .catch(() => {
          this.taskLastRequestComplete = true
        })
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.listTask()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.listTask()
    },
    initSearch() {
      this.handleCurrentChange(1)
    },
    handleCommand(key, row) {
      switch (key) {
        case 'exec':
          this.execTask(row)
          return
          break
        case 'delete':
          this.deleteTask(row)
          return
          break
        default:
          break
      }
      this.changeTaskStatus(row)
    },
    execTask(task) {
      this.$confirm(
        this.$t('dataset.task.confirm_exec'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          post('/dataset/task/execTask', task).then((response) => {
            this.initSearch()
          })
        })
        .catch(() => {})
    },
    disableEdit(task) {
      return (
        task.rate === 'SIMPLE' ||
        !hasDataPermission('manage', task.privileges)
      )
    },
    disableExec(task) {
      return (
        task.status === 'Stopped' ||
        task.status === 'Pending' ||
        task.rate === 'SIMPLE' ||
        !hasDataPermission('manage', task.privileges)
      )
    },
    disableDelete(task) {
      return false
      // !hasDataPermission('manage',task.privileges)
    },
    selectDataset(row) {
      this.disableForm = this.disableEdit(row)
      this.addTask(row)
    },
    changeTaskStatus(task) {
      const { status } = task
      if (!['Pending', 'Underway'].includes(status)) {
        return
      }
      const param = {
        ...task,
        status: status === 'Underway' ? 'Pending' : 'Underway'
      }
      post('/dataset/task/updateStatus', param)
        .then((response) => {
          if (response.success) {
            task.status = param.status
            this.$message({
              message: this.$t('dataset.task.change_success'),
              type: 'success',
              showClose: true
            })
          } else {
            this.initSearch(false)
          }
        })
        .catch(() => {
          this.initSearch(false)
        })
    },
    saveTask(task) {
      this.$refs.taskForm.validate((valid) => {
        if (valid) {
          if (this.incrementalUpdateType === 'incrementalAdd') {
            this.incrementalConfig.incrementalAdd = this.sql
          } else {
            this.incrementalConfig.incrementalDelete = this.sql
          }
          this.incrementalConfig.tableId = this.table.id
          task.startTime = new Date(task.startTime).getTime()
          task.endTime = new Date(task.endTime).getTime()
          task.tableId = this.table.id
          const form = JSON.parse(JSON.stringify(task))
          form.extraData = JSON.stringify(form.extraData)
          const dataSetTaskRequest = {
            datasetTableTask: form,
            datasetTableIncrementalConfig: this.incrementalConfig
          }
          post('/dataset/task/save', dataSetTaskRequest).then((response) => {
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.update_task = false
            this.resetTaskForm()
            this.listTask()
            this.listTaskLog()
          })
        } else {
          return false
        }
      })
    },
    getIncrementalConfig() {
      post('/dataset/table/incrementalConfig', { tableId: this.table.id }).then(response => {
        this.incrementalConfig = response.data
        if (this.incrementalConfig.incrementalAdd.length === 0 && this.incrementalConfig.incrementalDelete.length === 0) {
          this.incrementalUpdateType = 'incrementalAdd'
          this.sql = ''
          return
        }
        if (this.incrementalConfig.incrementalAdd.length > 0) {
          this.incrementalUpdateType = 'incrementalAdd'
          this.sql = this.incrementalConfig.incrementalAdd
        } else {
          this.incrementalUpdateType = 'incrementalDelete'
          this.sql = this.incrementalConfig.incrementalDelete
        }
      })
    },
    deleteTask(task) {
      this.$confirm(
        this.$t('dataset.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          post('/dataset/task/delete/' + task.id, null).then((response) => {
            this.$message({
              message: this.$t('dataset.delete_success'),
              type: 'success',
              showClose: true
            })
            this.resetTaskForm()
            this.listTask()
            this.listTaskLog()
          })
        })
        .catch(() => {})
    },
    closeTask() {
      this.update_task = false
      this.resetTaskForm()
    },
    onSimpleCronChange() {
      if (this.taskForm.extraData.simple_cron_type === 'minute') {
        if (
          this.taskForm.extraData.simple_cron_value < 1 ||
          this.taskForm.extraData.simple_cron_value > 59
        ) {
          this.$message({
            message: this.$t('cron.minute_limit'),
            type: 'warning',
            showClose: true
          })
          this.taskForm.extraData.simple_cron_value = 59
        }
        this.taskForm.cron =
          '0 0/' + this.taskForm.extraData.simple_cron_value + ' * * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'hour') {
        if (
          this.taskForm.extraData.simple_cron_value < 1 ||
          this.taskForm.extraData.simple_cron_value > 23
        ) {
          this.$message({
            message: this.$t('cron.hour_limit'),
            type: 'warning',
            showClose: true
          })
          this.taskForm.extraData.simple_cron_value = 23
        }
        this.taskForm.cron =
          '0 0 0/' + this.taskForm.extraData.simple_cron_value + ' * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'day') {
        if (
          this.taskForm.extraData.simple_cron_value < 1 ||
          this.taskForm.extraData.simple_cron_value > 31
        ) {
          this.$message({
            message: this.$t('cron.day_limit'),
            type: 'warning',
            showClose: true
          })
          this.taskForm.extraData.simple_cron_value = 31
        }
        this.taskForm.cron =
          '0 0 0 1/' + this.taskForm.extraData.simple_cron_value + ' * ? *'
        return
      }
    },
    onRateChange() {
      if (this.taskForm.rate === 'SIMPLE') {
        this.taskForm.end = '0'
        this.taskForm.endTime = ''
        this.taskForm.cron = ''
        this.showCron = false
      }
      if (this.taskForm.rate === 'SIMPLE_CRON') {
        this.taskForm.cron = '0 0 0/1 *  * ? *'
        this.showCron = false
      }
      if (this.taskForm.rate === 'CRON') {
        this.taskForm.cron = '00 00 * ? * * *'
        this.$nextTick(() => {
          this.showCron = true
        })
      }
    },
    listTaskLog(loading = true) {
      const params = {
        conditions: [
          {
            field: 'dataset_table_task_log.table_id',
            operator: 'eq',
            value: this.table.id
          }
        ],
        orders: []
      }
      post(
        '/dataset/taskLog/listForDataset/' +
          this.table.type +
          '/' +
          this.page.currentPage +
          '/' +
          this.page.pageSize,
        params,
        loading
      )
        .then((response) => {
          this.taskLogData = response.data.listObject
          this.page.total = response.data.itemCount
          this.taskLogLastRequestComplete = true
        })
        .catch(() => {
          this.taskLogLastRequestComplete = true
        })
    },
    handleSizeChangeLog(val) {
      this.page.pageSize = val
      this.listTaskLog()
    },
    handleCurrentChangeLog(val) {
      this.page.currentPage = val
      this.listTaskLog()
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
    showSQL(val) {
      this.sql = val || ''
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {},
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
    }
  }
}
</script>

<style scoped>
.err-msg {
  font-size: 12px;
  word-break: normal;
  width: auto;
  display: block;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow: hidden;
}

.blackTheme .dialog_cancel_button {
  background-color: #171b22 !important;
  color: #2681ff !important;
}

.span {
  font-size: 12px;
}
</style>
<style lang="scss" scoped>
.table-task-container {
  height: calc(100vh - 250px);
}
</style>
<style lang="scss">
.user-drawer-task {
  .el-table::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    z-index: 10;
    border-top: 1px solid rgba(31, 35, 41, 0.15);
  }
}
.update-drawer-task {
  .el-drawer__body {
    padding: 24px;
    padding-bottom: 80px;
    position: unset;
  }
  .simple-cron {
    display: flex;
    align-items: center;
    width: 100%;
    .el-input-number,
    .el-select {
      width: 140px;
      margin-left: 8px;

      .el-input__inner {
        text-align: left;
      }
    }

    .el-select {
      margin-right: 8px;
    }
  }

  .execute-rate-cont {
    box-sizing: border-box;
    padding: 20px;
    width: 100%;
    background-color: var(--MainBG, #f5f6f7);
    border-radius: 4px;

    .el-input__inner {
      background: var(--ContentBG, #ffffff) !important;
    }

    .el-date-editor {
      width: 100%;
    }
  }

  .add-scope-cont {
    height: 350px;
    width: 100%;
    border-radius: 4px;
    padding: 20px;
    background-color: var(--MainBG, #f5f6f7);

    .param-title {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-bottom: 9px;
      &:nth-child(1) {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
      }
    }

    .codemirror-cont {
      box-sizing: border-box;
      width: 100%;
      height: 200px;
      background: #ffffff;
      border: 1px solid #bbbfc4;
      border-radius: 4px;
      overflow: auto;
    }
  }
}
</style>
