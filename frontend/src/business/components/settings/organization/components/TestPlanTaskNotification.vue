<template>
  <div>
    <el-row>
      <el-col :span="10">
        <h3>{{ $t('organization.message.test_plan_task_notification') }}</h3>
        <el-button icon="el-icon-circle-plus-outline" plain size="mini" @click="handleAddTaskModel">
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
          :data="testCasePlanTask"
          class="tb-edit"
          border
          :cell-style="rowClass"
          :header-cell-style="headClass"
        >
          <el-table-column :label="$t('schedule.event')" min-width="15%" prop="events">
            <template slot-scope="scope">
              <el-select v-model="scope.row.event" :placeholder="$t('organization.message.select_events')"
                         @change="handleTestPlanReceivers(scope.row)" size="mini"
                         prop="events" :disabled="!scope.row.isSet">
                <el-option
                  v-for="item in otherEventOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiver')" prop="userIds" min-width="20%">
            <template v-slot:default="{row}">
              <el-select v-model="row.userIds" filterable multiple size="mini"
                         :placeholder="$t('commons.please_select')" style="width: 100%;" :disabled="!row.isSet">
                <el-option
                  v-for="item in row.testPlanReceiverOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column :label="$t('schedule.receiving_mode')" min-width="20%" prop="type">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" :placeholder="$t('organization.message.select_receiving_method')"
                         size="mini"
                         :disabled="!scope.row.isSet" @change="handleEdit(scope.$index, scope.row)">
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
          <el-table-column :label="$t('commons.operating')" min-width="25%" prop="result">
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
                @click="handleAddTask(scope.$index,scope.row)"
              >{{ $t('commons.add') }}
              </el-button>
              <el-button
                size="mini"
                v-show="scope.row.isSet"
                @click.native.prevent="removeRowTask(scope.$index,testCasePlanTask)"
              >{{ $t('commons.cancel') }}
              </el-button>
              <el-button
                type="primary"
                size="mini"
                v-show="!scope.row.isSet"
                @click="handleEditTask(scope.$index,scope.row)"
              >{{ $t('commons.edit') }}
              </el-button>
              <el-button
                type="danger"
                icon="el-icon-delete"
                size="mini"
                v-show="!scope.row.isSet"
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

const TASK_TYPE = 'TEST_PLAN_TASK';
const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};

export default {
  name: "TestPlanTaskNotification",
  components: {
    "NoticeTemplate": noticeTemplate.default
  },
  props: {
    testPlanReceiverOptions: {
      type: Array
    }
  },
  data() {
    return {
      title: "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>MeterSphere</title>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div>\n" +
        "    <p style=\"text-align: left\">${creator} 创建的:<br>\n" +
        "        ${testPlanName}<br>\n" +
        "        计划开始时间是:${start}<br>\n" +
        "        计划结束时间为:${end}<br>\n" +
        "        请跟进！<br>\n" +
        "        点击下面链接进入测试计划页面</p>\n" +
        "    <a href=\"${url}/#/track/plan/all\">${url}/#/track/plan/all</a>\n" +
        "</div>\n" +
        "</body>\n" +
        "</html>",
      robotTitle:
        "   【任务通知】:${creator} 创建的:${testPlanName}计划开始时间是:${start}计划结束时间是：${end}请跟进！/ ${status}！" +
        "点击下面链接进入测试计划页面${url}/#/track/plan/all",
      testCasePlanTask: [{
        taskType: "testPlanTask",
        event: "",
        userIds: [],
        type: [],
        webhook: "",
        isSet: true,
        identification: "",
        isReadOnly: false,
      }],
      otherEventOptions: [
        {value: 'CREATE', label: this.$t('commons.create')},
        {value: 'UPDATE', label: this.$t('commons.update')},
        {value: 'DELETE', label: this.$t('commons.delete')}
      ],
      receiveTypeOptions: [
        {value: 'EMAIL', label: this.$t('organization.message.mail')},
        {value: 'NAIL_ROBOT', label: this.$t('organization.message.nail_robot')},
        {value: 'WECHAT_ROBOT', label: this.$t('organization.message.enterprise_wechat_robot')}
      ],
    };
  },
  methods: {
    initForm() {
      this.result = this.$get('/notice/search/message/type/' + TASK_TYPE, response => {
        this.testCasePlanTask = response.data;
        this.testCasePlanTask.forEach(planTask => {
          this.handleTestPlanReceivers(planTask);
        });
      })
    },
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly
        data.webhook = '';
      }
    },
    handleEditTask(index, data) {
      data.isSet = true
      if (data.type === 'EMAIL') {
        data.isReadOnly = false;
        data.webhook = '';
      } else {
        data.isReadOnly = true;
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
      Task.taskType = TASK_TYPE
      this.testCasePlanTask.push(Task)
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
      data.isSet = false
      this.result = this.$post("/notice/save/message/task", data, () => {
        this.initForm()
        this.$success(this.$t('commons.save_success'));
      })
    },
    removeRowTask(index, data) { //移除
      if (!data[index].identification) {
        data.splice(index, 1)
      } else {
        data[index].isSet = false
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
    handleTestPlanReceivers(row) {
      let testPlanReceivers = JSON.parse(JSON.stringify(this.testPlanReceiverOptions));
      switch (row.event) {
        case  "CREATE":
          testPlanReceivers.unshift({id: 'EXECUTOR', name: this.$t('test_track.plan_view.executor')})
          break;
        case "UPDATE":
        case "DELETE":
        case "COMMENT":
          testPlanReceivers.unshift({id: 'FOUNDER', name: this.$t('api_test.creator')});
          break;
        default:
          break;
      }
      row.testPlanReceiverOptions = testPlanReceivers;
    },
    handleTemplate(index, row) {
      if (hasLicense()) {
        this.$refs.noticeTemplate.open(row);
      }
    }
  },
  watch: {
    testPlanReceiverOptions(value) {
      if (value && value.length > 0) {
        this.initForm();
      }
    }
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 10px;
}
.el-button {
  margin-left: 10px;
}
</style>
