<template>
  <de-layout-content :header="header" back-path="/system/system-task/dataset">
    <div class="dataset-editer-form">
      <div class="w600">
        <el-form
          ref="taskForm"
          :form="taskForm"
          :model="taskForm"
          :disabled="disableForm"
          label-width="100px"
          class="de-form-item"
          :rules="taskFormRules"
        >
          <el-form-item
            :label="$t('chart.select_dataset')"
            prop="datasetName"
            @click.native="selectDataset"
          >
            <el-input
              v-model="taskForm.datasetName"
              size="small"
              :disabled="!!taskForm.id"
              readonly
              :placeholder="$t('dataset.task_name')"
            />
          </el-form-item>
          <el-form-item :label="$t('dataset.task_name')" prop="name">
            <el-input
              v-model="taskForm.name"
              size="small"
              :placeholder="$t('dataset.task_name')"
            />
          </el-form-item>
          <el-form-item :label="$t('dataset.update_type')" prop="type">
            <el-radio-group v-model="taskForm.type">
              <el-radio label="all_scope">{{
                $t("dataset.all_scope")
              }}</el-radio>
              <el-radio label="add_scope">
                {{ $t("dataset.add_scope") }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <div v-if="taskForm.type === 'add_scope'" class="add-scope-cont">
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
                  $t("dataset.incremental_add")
                }}</el-radio>
                <el-radio label="incrementalDelete">{{
                  $t("dataset.incremental_delete")
                }}</el-radio>
              </el-radio-group>
              <div class="param-title">
                <span>{{ $t("dataset.param") }}</span>
                <div class="param-title-btn">
                  <el-button
                    type="text"
                    size="small"
                    @click="insertParamToCodeMirror('${__last_update_time__}')"
                  >{{ $t("dataset.last_update_time") }}</el-button>
                  <el-button
                    type="text"
                    size="small"
                    @click="
                      insertParamToCodeMirror('${__current_update_time__}')
                    "
                  >{{ $t("dataset.current_update_time") }}</el-button>
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
              <el-radio label="SIMPLE">{{
                $t("dataset.execute_once")
              }}</el-radio>
              <el-radio label="CRON">{{ $t("dataset.cron_config") }}</el-radio>
              <el-radio label="SIMPLE_CRON">{{
                $t("dataset.simple_cron")
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
                {{ $t("cron.every") }}
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
                {{ $t("cron.every_exec") }}
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
              <svg-icon icon-class="icon_calendar_outlined" class="icon-calendar-outlined" />
            </el-form-item>
            <el-form-item
              v-if="taskForm.rate !== 'SIMPLE'"
              :label="$t('dataset.end_time')"
              prop="end"
            >
              <el-radio-group v-model="taskForm.end">
                <el-radio label="0">{{ $t("dataset.no_limit") }}</el-radio>
                <el-radio label="1"> {{ $t("dataset.set_end_time") }}</el-radio>
              </el-radio-group>
              <el-date-picker
                v-if="taskForm.end === '1'"
                v-model="taskForm.endTime"
                class="de-date-picker"
                type="datetime"
                :placeholder="$t('dataset.end_time')"
                size="small"
              />
              <svg-icon v-if="taskForm.end === '1'" icon-class="icon_calendar_outlined" class="icon-calendar-outlined" />
            </el-form-item>
          </div>
        </el-form>
        <table-selector
          ref="tableSelector"
          preview-for-task="true"
          privileges="manage"
          :mode="1"
          :clear-empty-dir="true"
          :custom-type="['db', 'sql', 'api']"
          show-mode="datasetTask"
          @getTableId="getTableId"
        />
      </div>
      <div v-if="!disableForm" class="de-foot-layout">
        <div class="cont">
          <deBtn secondary @click="closeTask">{{ $t("dataset.cancel") }}</deBtn>
          <deBtn type="primary" @click="saveTask(taskForm)">{{
            $t("dataset.confirm")
          }}</deBtn>
        </div>
      </div>
    </div>
  </de-layout-content>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import DeLayoutContent from '@/components/business/DeLayoutContent'
import { hasDataPermission } from '@/utils/permission'
import msgCfm from '@/components/msgCfm/index'

import cron from '@/components/cron/cron'
import { codemirror } from 'vue-codemirror'
import 'codemirror/lib/codemirror.css'
import 'codemirror/theme/eclipse.css'
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
import TableSelector from './TableSelector'

export default {
  components: { cron, codemirror, TableSelector, DeLayoutContent },
  mixins: [msgCfm],
  data() {
    return {
      disableForm: false,
      table: {
        name: '',
        id: ''
      },
      showCron: false,
      taskForm: {
        name: '',
        type: 'all_scope',
        startTime: '',
        tableId: '',
        rate: 'SIMPLE',
        cron: '',
        endTime: '',
        end: '0',
        extraData: {
          simple_cron_type: 'hour',
          simple_cron_value: 1
        }
      },
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
        ],
        cron: [
          {
            required: true,
            message: this.$t('dataset.required'),
            trigger: 'change'
          }
        ],
        datasetName: [
          {
            required: true,
            message: this.$t('components.data_set_required'),
            trigger: 'change'
          }
        ]
      },
      cronEdit: false,
      sqlOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'eclipse',
        hintOptions: {
          // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      incrementalConfig: {},
      sql: '',
      incrementalUpdateType: 'incrementalAdd'
    }
  },
  computed: {
    header() {
      return this.disableForm
        ? '查看任务'
        : this.taskDetail.id
          ? '编辑任务'
          : '添加任务'
    }
  },
  created() {
    const { datasetName, id, tableId } = this.$route.query
    this.taskDetail = { datasetName, id, tableId }
    if (!id) {
      this.taskForm.startTime = new Date()
      return
    }
    this.getTaskDetail(id)
    this.getIncrementalConfig(tableId)
  },
  methods: {
    getTaskDetail(id) {
      post(`/dataset/task/detail/${id}`, {}).then((res) => {
        if (res.data.extraData) {
          res.data.extraData = JSON.parse(res.data.extraData)
        }
        this.taskForm = res.data
        this.showCron = this.taskForm.rate === 'CRON'
        this.disableForm = this.disableEdit()
      })
    },
    selectDataset() {
      if (this.taskForm.id) return
      this.$refs.tableSelector.init()
    },
    getTableId(id, name) {
      this.taskForm.tableId = id
      this.$set(this.taskForm, 'datasetName', name)
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
    disableEdit() {
      const { privileges, rate, status } = this.taskForm
      return (
        rate === 'SIMPLE' ||
        status === 'Stopped' ||
        !hasDataPermission('manage', privileges)
      )
    },
    onCmReady(cm) {
      //   this.codemirror.setSize("-webkit-fill-available", "auto");
    },
    onCmFocus(cm) {},
    onCmCodeChange(newCode) {
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    },
    closeTask() {
      this.$router.back()
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
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    },
    saveTask(task) {
      this.$refs.taskForm.validate((valid) => {
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
            datasetTableIncrementalConfig:
              task.type === 'add_scope' ? this.incrementalConfig : undefined
          }
          post('/dataset/task/save', dataSetTaskRequest).then((response) => {
            this.openMessageSuccess('dataset.save_success')
            this.closeTask()
          })
        } else {
          return false
        }
      })
    },
    getIncrementalConfig(tableId) {
      post('/dataset/table/incrementalConfig', { tableId: tableId }).then(
        (response) => {
          this.incrementalConfig = response.data

          if (
            this.incrementalConfig.incrementalAdd.length === 0 &&
            this.incrementalConfig.incrementalDelete.length === 0
          ) {
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
        }
      )
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
    }
  }
}
</script>

<style lang="scss">
.dataset-editer-form {
  display: flex;
  align-items: center;
  justify-content: center;
  .w600 {
    width: 600px;
    padding-top: 24px;
    padding-bottom: 24px;
    .el-radio:not(:last-child) {
      margin-right: 0;
      width: 156px;
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
        width: 560px;
        height: 200px;
        background: #ffffff;
        border: 1px solid #bbbfc4;
        border-radius: 4px;
        overflow: auto;
      }
    }
  }
}
.de-foot-layout {
  position: absolute;
  width: calc(100% - 48px);
  height: 80px;
  bottom: 0;
  right: 24px;
  background-color: var(--MainBG, #fff);
  box-shadow: 0px -2px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
  .cont {
    width: 600px;
    text-align: right;
  }
}
</style>
