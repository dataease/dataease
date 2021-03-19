<template>
  <el-col>
    <el-row>
      <el-button icon="el-icon-setting" size="mini" @click="showConfig">
        {{ $t('dataset.update_setting') }}
      </el-button>
    </el-row>
    <el-row style="margin-top: 10px;">
      <el-table
        size="mini"
        :data="taskLogData"
        border
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
        <el-table-column
          prop="status"
          :label="$t('dataset.status')"
        />
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
      :title="table.name+' '+$t('dataset.update_setting')"
      :visible="update_setting"
      :show-close="false"
      width="50%"
      class="dialog-css"
    >
      <el-dialog
        :title="$t('dataset.task_update')"
        :visible="update_task"
        :show-close="false"
        width="50%"
        class="dialog-css"
        append-to-body
      >
        <el-col>
          <el-form :form="taskForm" label-width="80px">
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
            <el-form-item :label="$t('dataset.start_time')" prop="startTime">
              <el-date-picker
                v-model="taskForm.startTime"
                type="datetime"
                :placeholder="$t('dataset.select_data_time')"
                size="mini"
              />
            </el-form-item>
            <el-form-item :label="$t('dataset.execute_rate')" prop="rate">
              <el-select v-model="taskForm.rate"  @change="onRateChange">
                <el-option
                  :label="$t('dataset.execute_once')"
                  value="SIMPLE"
                />
                <el-option
                  :label="$t('dataset.cron_config')"
                  value="CRON"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="taskForm.rate === 'CRON'" label="">
              <el-input v-model="taskForm.cron" size="mini" style="width: 50%" />
            </el-form-item>
            <el-form-item :label="$t('dataset.end_time')" prop="end">
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
        <el-button icon="el-icon-download" size="mini">
          {{ $t('dataset.sync_now') }}
        </el-button>
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
          height="300"
        >
          <el-table-column
            prop="name"
            :label="$t('dataset.start_time')"
          />
          <el-table-column
            prop="rate"
            :label="$t('dataset.execute_rate')"
          >
            <template slot-scope="scope">
              <span v-if="scope.row.rate === 'SIMPLE'">{{ $t('dataset.execute_once') }}</span>
              <span v-if="scope.row.rate === 'SIMPLE_COMPLETE'">{{ $t('dataset.execute_once') }}</span>
              <span v-if="scope.row.rate === 'CRON'">{{ $t('dataset.cron_config') }}</span>
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
                :disabled="scope.row.rate === 'SIMPLE_COMPLETE'"
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
          <el-col :span="6"><div>{{ $t('dataset.incremental_update_type') }}</div></el-col>
          <el-col :span="18">
            <el-radio-group v-model="incrementalUpdateType" size="small" @change="incrementalUpdateTypeChange">
              <el-radio label="incrementalAdd"  >{{ $t('dataset.incremental_add') }}</el-radio>
              <el-radio label="incrementalDelete"  >{{ $t('incremental_delete.incremental_update_type') }}</el-radio>
            </el-radio-group>
          </el-col>
        </el-row>
      </el-row>

      <el-row style="height: 26px;">
        <el-row>
          <el-col :span="6" style="height: 26px;"><div style="height: 26px;">参数:</div></el-col>
          <el-col :span="18">
            <el-button type="text">{{ $t('dataset.last_update_time') }}</el-button>
            <el-button type="text">{{ $t('dataset.current_update_time') }}</el-button>
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
        <el-button size="mini" @click="saveIncrementalConfig">{{ $t('dataset.confirm') }}</el-button>
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

export default {
  name: 'UpdateInfo',
  components: { codemirror },
  props: {
    table: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      update_setting: false,
      update_task: false,
      taskForm: {
        name: '',
        type: 'all_scope',
        startTime: '',
        rate: 'SIMPLE',
        cron: '',
        endTime: '',
        end: '0'
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
          { required: true, message: this.$t('dataset.required'), trigger: 'change' }
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
      incrementalConfig: {}
    }
  },
  watch: {
    table() {
      this.listTask()
      this.listTaskLog()
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  mounted() {
    window.onresize = () => {
      return (() => {
        this.height = window.innerHeight / 2
      })()
    }
    this.height = window.innerHeight / 2

  },
  methods: {
    incrementalUpdateTypeChange: function (){

      if(this.incrementalUpdateType === 'incrementalAdd'){
        if(this.sql){
          this.incrementalConfig.incrementalDelete = this.sql
        }
        if(this.incrementalConfig.incrementalAdd){
          this.sql = this.incrementalConfig.incrementalAdd
        }else {
          this.sql = ''
        }
      }

      if(this.incrementalUpdateType === 'incrementalDelete'){
        if(this.sql){
          this.incrementalConfig.incrementalAdd = this.sql
        }
        if(this.incrementalConfig.incrementalDelete){
           this.sql = this.incrementalConfig.incrementalDelete
        }else {
          this.sql = ''
        }
      }
    },
    showConfig() {
      this.update_setting = true
      this.listTask()
      this.getIncrementalConfig()
    },
    addTask(task) {
      if (!task) {
        this.resetTaskForm()
        this.taskForm.name = this.table.name + ' ' + this.$t('dataset.task_update')
        this.taskForm.startTime = new Date()
      } else {
        this.taskForm = JSON.parse(JSON.stringify(task))
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
        if(this.incrementalConfig.incrementalAdd){
          this.sql = this.incrementalConfig.incrementalAdd
        }
      })
    },
    saveIncrementalConfig() {
      this.update_setting = false
      if(this.incrementalUpdateType === 'incrementalAdd'){
        this.incrementalConfig.incrementalAdd = this.sql
      }else {
        this.incrementalConfig.incrementalDelete = this.sql
      }
      this.incrementalConfig.tableId = this.table.id
      post('/dataset/table/save/incrementalConfig', this.incrementalConfig).then(response => {
        this.$message({
          message: this.$t('dataset.save_success'),
          type: 'success',
          showClose: true
        })
      })
    },
    saveTask(task) {
      task.startTime = new Date(task.startTime).getTime()
      task.endTime = new Date(task.endTime).getTime()
      task.tableId = this.table.id
      post('/dataset/task/save', task).then(response => {
        this.$message({
          message: this.$t('dataset.save_success'),
          type: 'success',
          showClose: true
        })
        this.update_task = false
        this.resetTaskForm()
        this.listTask()
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
        })
      }).catch(() => {
      })
    },
    closeTask() {
      this.update_task = false
      this.resetTaskForm()
    },
    onRateChange() {
      if (this.taskForm.rate === 'SIMPLE') {
        this.taskForm.end = '0'
        this.taskForm.endTime = ''
        this.taskForm.cron = ''
      }
    },
    listTaskLog() {
      post('/dataset/taskLog/list/' + this.page.currentPage + '/' + this.page.pageSize, { tableId: this.table.id }).then(response => {
        this.taskLogData = response.data.listObject
        this.page.total = response.data.itemCount
      })
    },
    handleSizeChange(val) {
      this.listTaskLog()
    },
    handleCurrentChange(val) {
      this.listTaskLog()
    },
    resetTaskForm() {
      this.taskForm = {
        name: '',
        type: 'all_scope',
        startTime: '',
        rate: 'SIMPLE',
        endTime: '',
        end: '0'
      }
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
      // console.log('the editor is focus!', cm)
    },
    onCmCodeChange(newCode) {
      console.log(newCode)
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    }

  }
}
</script>

<style scoped>
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
    height: 160px;
    overflow-y: auto;
  }
  .codemirror >>> .CodeMirror-scroll {
    height: 160px;
    overflow-y: auto;
  }
</style>
