<template>
  <div>
    <el-row>
      <el-col :span="10">
        <el-button :disabled="!isTesterPermission" icon="el-icon-circle-plus-outline" plain size="mini"
                   @click="handleAddTaskModel">
          {{ $t('organization.message.create_new_notification') }}
        </el-button>
        <el-popover
          placement="right-end"
          title="示例"
          width="400"
          trigger="click"
          :content="title">
          <el-button icon="el-icon-warning" plain size="mini" slot="reference">
            {{ $t('organization.message.mail_template_example') }}
          </el-button>
        </el-popover>
        <el-popover
          placement="right-end"
          title="示例"
          width="400"
          trigger="click"
          :content="robotTitle">
          <el-button icon="el-icon-warning" plain size="mini" slot="reference">
            {{ $t('organization.message.robot_template') }}
          </el-button>
        </el-popover>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="24">
        <el-table
          :data="scheduleTask"
          class="tb-edit"
          border
          :cell-style="rowClass"
          :header-cell-style="headClass">
          <el-table-column :label="$t('schedule.event')" prop="events" min-width="13%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" size="mini"
                         :placeholder="$t('organization.message.select_events')"
                         prop="events" :disabled="!scope.row.isSet">
                <el-option
                  v-for="item in scheduleEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="userIds" min-width="18%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple size="mini"
                         :placeholder="$t('commons.please_select')" style="width: 100%;" :disabled="!row.isSet">
                <el-option
                  v-for="item in scheduleReceiverOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiving_mode')" prop="type" min-width="15%">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
                         size="mini"
                         :disabled="!scope.row.isSet" @change="handleEdit(scope.$index, scope.row)"
              >
                <el-option
                  v-for="item in receiveTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="webhook" min-width="20%" prop="webhook">
            <template v-slot:default="scope">
              <el-input v-model="scope.row.webhook" placeholder="webhook地址" size="mini"
                        :disabled="!scope.row.isSet||!scope.row.isReadOnly"></el-input>
            </template>
          </el-table-column>
          <el-table-column :label="$t('commons.operating')" prop="result" min-width="25%">
            <template v-slot:default="scope">
              <el-button
                type="success"
                size="mini"
                v-if="scope.row.isSet"
                v-xpack
                @click="handleTemplate(scope.$index,scope.row)"
              >{{ $t('organization.message.template') }}
              </el-button>
              <el-button
                type="primary"
                size="mini"
                v-show="scope.row.isSet"
                :disabled="!isTesterPermission"
                @click="handleAddTask(scope.$index,scope.row)"
              >{{ $t('commons.add') }}
              </el-button>
              <el-button
                size="mini"
                v-show="scope.row.isSet"
                :disabled="!isTesterPermission"
                @click.native.prevent="removeRowTask(scope.$index,scheduleTask)"
              >{{ $t('commons.cancel') }}
              </el-button>
              <el-button
                type="primary"
                size="mini"
                v-show="!scope.row.isSet"
                :disabled="!isTesterPermission"
                @click="handleEditTask(scope.$index,scope.row)"
              >{{ $t('commons.edit') }}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="!scope.row.isSet"
                :disabled="!isTesterPermission"
                @click.native.prevent="deleteRowTask(scope.$index,scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
    <notice-template v-xpack ref="noticeTemplate"/>
  </div>
</template>

<script>
import {hasLicense} from "@/common/js/utils";
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "ScheduleNotification",
  components: {
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    testId: String,
    scheduleReceiverOptions: Array,
    isTesterPermission: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      title: '<!DOCTYPE html>\n' +
        '<html lang="en">\n' +
        '<head>\n' +
        '    <meta charset="UTF-8">\n' +
        '    <title>MeterSphere</title>\n' +
        '</head>\n' +
        '<body>\n' +
        '<div>\n' +
        '    <div style="text-align: left">\n' +
        '        <p>尊敬的用户：</p>\n' +
        '        <p style="margin-left: 60px">您好:\n' +
        '    </div>\n' +
        '    <div style="margin-left: 100px">\n' +
        '        <p>您所执行的 ${testName} 接口测试运行失败<br/>\n' +
        '            请点击下面链接进入测试报告页面</p>\n' +
        '        <a href="${url}/#/${type}/report/view/${id}">${url}/#/${type}/report/view/${id}</a>\n' +
        '        <p>新版接口测试报告路径</p>\n' +
        '        <a href="${url}/#/api/automation">${url}/#/api/automation</a>\n' +
        '    </div>\n' +
        '\n' +
        '</div>\n' +
        '</body>\n' +
        '</html>',
      robotTitle:
        "测试【任务通知】:'您所执行的 ${testName} ${type}测试运行${status}\n" +
        "请点击下面链接进入测试报告页面\n" +
        "${url}/#/${type}/report/view/${id}" +
        "新版接口测试报告路径\n" +
        "${url}/#/api/automation",
      scheduleTask: [{
        taskType: "scheduleTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
        testId: this.testId,
      }],
      scheduleEventOptions: [
        {value: 'EXECUTE_SUCCESSFUL', label: this.$t('schedule.event_success')},
        {value: 'EXECUTE_FAILED', label: this.$t('schedule.event_failed')}
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
    }
  },
  mounted() {
    this.initForm()
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/' + this.testId, response => {
        // console.log(response.data);
        this.scheduleTask = response.data;
      })
    },
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly;
        data.webhook = '';
      }
    },
    handleAddTaskModel() {
      let Task = {};
      Task.event = [];
      Task.userIds = [];
      Task.type = '';
      Task.webhook = '';
      Task.isSet = true;
      Task.identification = '';
      Task.taskType = 'SCHEDULE_TASK';
      Task.testId = this.testId;
      this.scheduleTask.push(Task);
    },
    handleEditTask(index, data) {
      data.isSet = true;
      data.testId = this.testId;
      if (data.type === 'EMAIL') {
        data.isReadOnly = false;
        data.webhook = '';
      } else {
        data.isReadOnly = true;
      }
    },
    handleAddTask(index, data) {
      if (data.event && data.userIds.length > 0 && data.type) {
        // console.log(data.type)
        if (data.type === 'NAIL_ROBOT' || data.type === 'WECHAT_ROBOT') {
          if (!data.webhook) {
            this.$warning(this.$t('organization.message.message_webhook'));
          } else {
            this.addTask(data)
          }
        } else {
          this.addTask(data)
        }
      } else {
        this.$warning(this.$t('organization.message.message'));
      }
    },
    addTask(data) {
      this.result = this.$post("/notice/save/message/task", data, () => {
        this.initForm()
        this.$success(this.$t('commons.save_success'));
      })
    },
    removeRowTask(index, data) { //移除
      if (!data[index].identification) {
        data.splice(index, 1)
      } else {
        data[index].isSet = false;
      }
    },
    deleteRowTask(index, data) { //删除
      this.result = this.$get("/notice/delete/message/" + data.identification, response => {
        this.$success(this.$t('commons.delete_success'));
        this.initForm()
      })
    },
    rowClass() {
      return "text-align:center"
    },
    headClass() {
      return "text-align:center;background:'#ededed'"
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        this.$refs.noticeTemplate.open(row);
      }
    }
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
</style>

