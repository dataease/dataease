<template>
  <el-col>
    <el-row>
      <el-button v-if="hasDataPermission('manage',param.privileges) && table.type !== 'excel'" icon="el-icon-setting" size="mini" @click="showConfig">
        {{ $t('dataset.update_setting') }}
      </el-button>
      <el-button icon="el-icon-refresh" size="mini" @click="refreshLog">
        {{ $t('commons.refresh') }}
      </el-button>
    </el-row>
    <el-row style="margin-top: 10px;">
      <el-table
        size="mini"
        :data="taskLogData"
        border
        :height="height"
        style="width: 100%"
      >
        <el-table-column
          prop="name"
          :label="$t('dataset.task_name')"
        />
        <el-table-column
          prop="startTime"
          :label="$t('dataset.start_time')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.startTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="endTime"
          :label="$t('dataset.end_time')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.endTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" :label="$t('dataset.status')">
          <template slot-scope="scope">
            <span v-if="scope.row.status === 'Completed'" style="color: green">{{ $t('dataset.completed') }}</span>
            <span v-if="scope.row.status === 'Underway'" style="color: blue">
              <i class="el-icon-loading" />
              {{ $t('dataset.underway') }}
            </span>
            <span v-if="scope.row.status === 'Error'" style="color: red">
              <el-link type="danger" style="font-size: 12px" @click="showErrorMassage(scope.row.info)">{{ $t('dataset.error') }}</el-link>
            </span>
          </template>
        </el-table-column>
      </el-table>
      <el-row style="margin-top: 10px;text-align: right;">
        <el-pagination
          :current-page="page.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="page.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="page.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </el-row>
    </el-row>

    <el-dialog
      v-dialogDrag
      :title="$t('dataset.detail')"
      :visible="show_error_massage"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <span class="err-msg">{{ error_massage }}
      </span>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="show_error_massage = false">{{ $t('dataset.close') }}</el-button>
      </span>
    </el-dialog>

    <el-dialog
      v-dialogDrag
      :title="table.name+' '+$t('dataset.update_setting')"
      :visible="update_setting"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <el-dialog
        v-dialogDrag
        :title="update_task_dialog_title"
        :visible="update_task"
        :show-close="false"
        width="50%"
        class="dialog-css"
        append-to-body
      >
        <el-col>
          <el-form :form="taskForm" :model="taskForm" label-width="100px" size="mini" ref="taskForm" :rules="taskFormRules">
            <el-form-item :label="$t('dataset.task_name')" prop="name">
              <el-input
                v-model="taskForm.name"
                size="mini"
                style="width: 50%"
                :placeholder="$t('dataset.task_name')"
              />
            </el-form-item>
            <el-form-item :label="$t('dataset.update_type')" prop="type">
              <el-select v-model="taskForm.type" size="mini">
                <el-option
                  :label="$t('dataset.all_scope')"
                  value="all_scope"
                />
                <el-option
                  :label="$t('dataset.add_scope')"
                  value="add_scope"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('dataset.execute_rate')" prop="rate">
              <el-select v-model="taskForm.rate" size="mini" @change="onRateChange">
                <el-option
                  :label="$t('dataset.execute_once')"
                  value="SIMPLE"
                />
                <el-option
                  :label="$t('dataset.simple_cron')"
                  value="SIMPLE_CRON"
                />
                <el-option
                  :label="$t('dataset.cron_config')"
                  value="CRON"
                />
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
                <el-form-item :label="$t('cron.every')" >
                  <el-input v-model="taskForm.extraData.simple_cron_value" size="mini" type="number"  min="1" @change="onSimpleCronChange()" />
                </el-form-item>

                <el-form-item class="form-item">
                  <el-select v-model="taskForm.extraData.simple_cron_type"  filterable size="mini" @change="onSimpleCronChange()" >
                    <el-option :label="$t('cron.minute_default')" value="minute" />
                    <el-option :label="$t('cron.hour_default')" value="hour" />
                    <el-option :label="$t('cron.day_default')" value="day" />
                  </el-select>
                </el-form-item>
                <el-form-item class="form-item" :label="$t('cron.every_exec')" />
              </el-form>
            </el-form-item>

            <el-form-item v-if="taskForm.rate !== 'SIMPLE'" :label="$t('dataset.start_time')" prop="startTime">
              <el-date-picker
                v-model="taskForm.startTime"
                type="datetime"
                :placeholder="$t('dataset.select_data_time')"
                size="mini"
              />
            </el-form-item>
            <el-form-item v-if="taskForm.rate !== 'SIMPLE'" :label="$t('dataset.end_time')" prop="end">
              <el-select v-model="taskForm.end" size="mini">
                <el-option
                  :label="$t('dataset.no_limit')"
                  value="0"
                />
                <el-option
                  :label="$t('dataset.set_end_time')"
                  value="1"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="taskForm.end === '1'" label="">
              <el-date-picker
                v-model="taskForm.endTime"
                type="datetime"
                :placeholder="$t('dataset.select_data_time')"
                size="mini"
              />
            </el-form-item>
          </el-form>
        </el-col>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="closeTask">{{ $t('dataset.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveTask(taskForm)">{{ $t('dataset.confirm') }}</el-button>
        </div>
      </el-dialog>
      <el-row>
        <el-button icon="el-icon-plus" size="mini" @click="addTask(undefined)">
          {{ $t('dataset.add_task') }}
        </el-button>
      </el-row>
      <el-row style="margin-top: 10px;">
        <el-table
          border
          size="mini"
          :data="taskData"
          style="width: 100%"
          height="240"
        >
          <el-table-column
            prop="name"
            :label="$t('dataset.task_name')"
          />
          <el-table-column
            prop="rate"
            :label="$t('dataset.execute_rate')"
          >
            <template slot-scope="scope">
              <span v-if="scope.row.rate === 'SIMPLE'">{{ $t('dataset.execute_once') }}</span>
              <span v-if="scope.row.rate === 'SIMPLE_CRON'">{{ $t('dataset.simple_cron') }}</span>
              <span v-if="scope.row.rate === 'CRON'">{{ $t('dataset.cron_config') }}</span>
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
          <el-table-column
            :label="$t('dataset.operate')"
          >
            <template slot-scope="scope" style="float: right">
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-edit"
                circle
                :disabled="scope.row.rate === 'SIMPLE' || scope.row.status === 'Stopped'"
                @click="addTask(scope.row)"
              />
              <el-button
                size="mini"
                type="danger"
                icon="el-icon-delete"
                circle
                @click="deleteTask(scope.row)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-row>

      <el-divider />

      <el-row style="height: 26px;">
        <el-row>
          <el-col :span="4"><span>{{ $t('dataset.incremental_update_type') }}:</span></el-col>
          <el-col :span="18">
            <el-radio-group v-model="incrementalUpdateType" size="mini" @change="incrementalUpdateTypeChange">
              <el-radio label="incrementalAdd">{{ $t('dataset.incremental_add') }}</el-radio>
              <el-radio label="incrementalDelete">{{ $t('dataset.incremental_delete') }}</el-radio>
            </el-radio-group>
          </el-col>
        </el-row>
      </el-row>

      <el-row style="height: 26px;">
        <el-row>
          <el-col :span="4" style="height: 26px;"><span style="display: inline-block;height: 26px;line-height: 26px;">{{ $t('dataset.param') }}:</span></el-col>
          <el-col :span="18">
            <el-button type="text" size="mini" @click="insertParamToCodeMirror('${__last_update_time__}')">{{ $t('dataset.last_update_time') }}</el-button>
            <el-button type="text" size="mini" @click="insertParamToCodeMirror('${__current_update_time__}')">{{ $t('dataset.current_update_time') }}</el-button>
          </el-col>
        </el-row>
      </el-row>

      <el-row>
        <el-col style="min-width: 200px;">
          <codemirror
            ref="myCm"
            v-model="sql"
            class="codemirror"
            :options="sqlOption"
            @ready="onCmReady"
            @focus="onCmFocus"
            @input="onCmCodeChange"
          />
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="update_setting = false">{{ $t('dataset.close') }}</el-button>
        <el-button size="mini" type="primary" @click="saveIncrementalConfig">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>
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

export default {
  name: 'UpdateInfo',
  components: { codemirror, cron },
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
      height: 500,
      update_setting: false,
      update_task: false,
      show_error_massage: false,
      update_task_dialog_title: '',
      error_massage: '',
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
      taskLogData: [],
      taskData: [],
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
      lang: this.$store.getters.language === 'en_US' ? 'en' : 'cn'
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {
    table: {
      handler() {
        this.listTask()
        this.listTaskLog()
      },
      immediate: true
    }
  },
  mounted() {
    this.calHeight()
  },
  created() {
    this.timer = setInterval(() => {
      this.listTaskLog(false)
    }, 5000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 55 - 38 - 28 - 10
      }, 10)
    },
    cellStyle({ row, column }) {
      // 状态列字体颜色
      if (row.status === 'Underway' && column === 'status') {
        return 'color: blue'
      } else if (row.status === 'Completed' && column === 'status') {
        return 'color: green'
      } else if (row.status === 'Error' && column === 'status') {
        return 'color: red'
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
    showConfig() {
      this.update_setting = true
      this.listTask()
      this.getIncrementalConfig()
    },
    refreshLog() {
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
        this.taskForm.name = this.table.name + ' ' + this.$t('dataset.task_update')
        this.taskForm.startTime = new Date()
        this.update_task_dialog_title = this.$t('dataset.task_add_title')
      } else {
        // update
        this.taskForm = JSON.parse(JSON.stringify(task))
        this.taskForm.extraData = JSON.parse(this.taskForm.extraData)
        this.update_task_dialog_title = this.$t('dataset.task_edit_title')
      }
      this.update_task = true
    },
    listTask() {
      post('/dataset/task/list', { tableId: this.table.id }).then(response => {
        this.taskData = response.data
      })
    },
    getIncrementalConfig() {
      post('/dataset/table/incrementalConfig', { tableId: this.table.id }).then(response => {
        this.incrementalConfig = response.data
        this.incrementalUpdateType = 'incrementalAdd'
        if (this.incrementalConfig.incrementalAdd) {
          this.sql = this.incrementalConfig.incrementalAdd
        } else {
          this.sql = ''
        }
      })
    },
    saveIncrementalConfig() {
      if (this.incrementalUpdateType === 'incrementalAdd') {
        this.incrementalConfig.incrementalAdd = this.sql
      } else {
        this.incrementalConfig.incrementalDelete = this.sql
      }
      this.incrementalConfig.tableId = this.table.id
      post('/dataset/table/save/incrementalConfig', this.incrementalConfig).then(response => {
        this.$message({
          message: this.$t('dataset.save_success'),
          type: 'success',
          showClose: true
        })
        this.update_setting = false
      })
    },
    saveTask(task) {
      this.$refs.taskForm.validate(valid => {
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
          post('/dataset/task/save', dataSetTaskRequest).then(response => {
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
        }else {
          return false
        }

      })
    },
    changeTaskStatus(task) {
      const param = task
      param.status = task.status === 'Underway' ? 'Pending' : 'Underway'
      post('/dataset/task/updateStatus', task).then(response => {
        task.status = param.status
        this.$message({
          message: this.$t('dataset.task.change_success'),
          type: 'success',
          showClose: true
        })
      })
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
          this.resetTaskForm()
          this.listTask()
          this.listTaskLog()
        })
      }).catch(() => {
      })
    },
    closeTask() {
      this.update_task = false
      this.resetTaskForm()
    },
    onSimpleCronChange() {
      if (this.taskForm.extraData.simple_cron_type === 'minute') {
        if(this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 59){
          this.$message({message: this.$t('cron.minute_limit'), type: 'warning', showClose: true})
          this.taskForm.extraData.simple_cron_value = 59
        }
        this.taskForm.cron = '0 0/'+ this.taskForm.extraData.simple_cron_value + ' * * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'hour') {
        if(this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 23){
          this.$message({message: this.$t('cron.hour_limit'), type: 'warning', showClose: true})
          this.taskForm.extraData.simple_cron_value = 23
        }
        this.taskForm.cron = '0 0 0/'+ this.taskForm.extraData.simple_cron_value + ' * * ? *'
        return
      }
      if (this.taskForm.extraData.simple_cron_type === 'day') {
        if(this.taskForm.extraData.simple_cron_value < 1 || this.taskForm.extraData.simple_cron_value > 31){
          this.$message({message: this.$t('cron.day_limit'), type: 'warning', showClose: true})
          this.taskForm.extraData.simple_cron_value = 31
        }
        this.taskForm.cron = '0 0 0 1/'+ this.taskForm.extraData.simple_cron_value + ' * ? *'
        return
      }
    },
    onRateChange() {
      if (this.taskForm.rate === 'SIMPLE') {
        this.taskForm.end = '0'
        this.taskForm.endTime = ''
        this.taskForm.cron = ''
      }
      if (this.taskForm.rate === 'SIMPLE_CRON'){
        this.taskForm.cron = '0 0 0/1 *  * ? *'
      }
      if (this.taskForm.rate === 'CRON'){
        this.taskForm.cron = '00 00 * ? * * *'
      }
    },
    listTaskLog(loading = true) {
      const params = {"conditions":[{"field":"dataset_table_task_log.table_id","operator":"eq","value": this.table.id}],"orders":[]}
      post('/dataset/taskLog/list/' +  this.table.type + '/' + this.page.currentPage + '/' + this.page.pageSize, params, loading).then(response => {
        this.taskLogData = response.data.listObject
        this.page.total = response.data.itemCount
      })
    },
    handleSizeChange(val) {
      this.page.pageSize = val
      this.listTaskLog()
    },
    handleCurrentChange(val) {
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
    onCmFocus(cm) {
      // console.log('the editor is focus!', cm)
    },
    onCmCodeChange(newCode) {
      // console.log(newCode)
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

  .span{
    font-size: 12px;
  }
</style>
