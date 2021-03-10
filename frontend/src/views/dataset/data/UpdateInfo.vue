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
                  value="0"
                />
                <el-option
                  :label="$t('dataset.add_scope')"
                  value="1"
                  :disabled="true"
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
              <el-select v-model="taskForm.rate" size="mini" @change="onRateChange">
                <el-option
                  :label="$t('dataset.execute_once')"
                  value="0"
                />
                <el-option
                  :label="$t('dataset.cron_config')"
                  value="1"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="taskForm.rate === '1'" label="">
              <el-input v-model="taskForm.cron" size="mini" style="width: 50%" />
            </el-form-item>
            <el-form-item :label="$t('dataset.end_time')" prop="end">
              <el-select v-model="taskForm.end" size="mini" :disabled="taskForm.rate === '0'">
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
              <span v-if="scope.row.rate === '0'">{{ $t('dataset.execute_once') }}</span>
              <span v-if="scope.row.rate === '1'">{{ $t('dataset.cron_config') }}</span>
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

      <div slot="footer" class="dialog-footer">
        <!--        <el-button size="mini" @click="update_setting = false">{{ $t('dataset.cancel') }}</el-button>-->
        <!--        <el-button type="primary" size="mini" @click="update_setting = false">{{ $t('dataset.confirm') }}</el-button>-->
        <el-button size="mini" @click="update_setting = false">{{ $t('dataset.close') }}</el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
import { post } from '@/api/dataset/dataset'

export default {
  name: 'UpdateInfo',
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
        type: '0',
        startTime: '',
        rate: '0',
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
      }
    }
  },
  watch: {
    table() {
      this.listTask()
      this.listTaskLog()
    }
  },
  methods: {
    showConfig() {
      this.update_setting = true
      this.listTask()
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
      // this.$refs['taskForm'].validate((valid) => {
      //   if (valid) {
      //
      //   } else {
      //     this.$message({
      //       message: this.$t('dataset.input_content'),
      //       type: 'error',
      //       showClose: true
      //     })
      //     return false
      //   }
      // })
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
      if (this.taskForm.rate === '0') {
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
        type: '0',
        startTime: '',
        rate: '0',
        endTime: '',
        end: '0'
      }
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
</style>
