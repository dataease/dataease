<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible" :append-to-body='true'
             @close="close">
    <template>
      <div>
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.edit_timer_task')" name="first">
            <el-form :model="form" :rules="rules" ref="from">
              <el-form-item
                prop="cronValue">
                <el-row>
                  <el-col :span="18">
                    <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                              :placeholder="$t('schedule.please_input_cron_expression')"/>
                    <el-button :disabled="isReadOnly" type="primary" @click="saveCron" v-tester>{{
                        $t('commons.save')
                      }}
                    </el-button>
                  </el-col>
                  <el-col :span="6">
                    <schedule-switch :schedule="schedule" @scheduleChange="scheduleChange"></schedule-switch>
                  </el-col>
                </el-row>



              </el-form-item>
              <el-form-item>
                <el-link :disabled="isReadOnly" type="primary" @click="showCronDialog">
                  {{ $t('schedule.generate_expression') }}
                </el-link>
              </el-form-item>
              <crontab-result :ex="form.cronValue" ref="crontabResult"/>
            </el-form>
            <el-dialog width="60%" :title="$t('schedule.generate_expression')" :visible.sync="showCron"
                       :modal="false">
              <crontab @hide="showCron=false" @fill="crontabFill" :expression="schedule.value"
                       ref="crontab"/>
            </el-dialog>
          </el-tab-pane>
          <el-tab-pane :label="$t('schedule.task_notification')" name="second">
            <ms-schedule-notification :is-tester-permission="isTesterPermission" :test-id="testId"
                                      :schedule-receiver-options="scheduleReceiverOptions"/>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {checkoutTestManagerOrTestUser, getCurrentUser, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import Crontab from "@/business/components/common/cron/Crontab";
import CrontabResult from "@/business/components/common/cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import MsScheduleNotification from "./ScheduleNotification";
import ScheduleSwitch from "@/business/components/api/automation/schedule/ScheduleSwitch";

function defaultCustomValidate() {
  return {pass: true};
}

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const noticeTemplate = requireComponent.keys().length > 0 ? requireComponent("./notice/NoticeTemplate.vue") : {};


export default {
  name: "MsScheduleMaintain",
  components: {CrontabResult, ScheduleSwitch,Crontab, MsScheduleNotification, "NoticeTemplate": noticeTemplate.default},

  props: {
    customValidate: {
      type: Function,
      default: defaultCustomValidate
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },


  watch: {
    'schedule.value'() {
      this.form.cronValue = this.schedule.value;
    }
  },
  data() {
    const validateCron = (rule, cronValue, callback) => {
      let customValidate = this.customValidate(this.getIntervalTime());
      if (!cronValue) {
        callback(new Error(this.$t('commons.input_content')));
      } else if (!cronValidate(cronValue)) {
        callback(new Error(this.$t('schedule.cron_expression_format_error')));
      } else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      scheduleReceiverOptions: [],
      operation: true,
      dialogVisible: false,
      schedule: {
        value: "",
      },
      scheduleTaskType: "",
      testId: String,
      showCron: false,
      form: {
        cronValue: ""
      },
      paramRow:{},
      activeName: 'first',
      rules: {
        cronValue: [{required: true, validator: validateCron, trigger: 'blur'}],
      }
    }
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    scheduleChange(){
      let flag = this.schedule.enable;
      this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        let param = {};
        param.taskID = this.schedule.id;
        param.enable = flag;
        this.updateTask(param);
      }).catch(() => {
      });

    },
    updateTask(param){
      this.result = this.$post('/api/schedule/updateEnableByPrimyKey', param, response => {
        let paramTestId = "";
        if (this.paramRow.redirectFrom == 'testPlan') {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "TEST_PLAN_TEST";
        } else {
          paramTestId = this.paramRow.id;
          this.scheduleTaskType = "API_SCENARIO_TEST";
        }
        this.taskID = paramTestId;
        this.findSchedule(paramTestId);
      });
    },
    initUserList() {
      let param = {
        name: '',
        organizationId: this.currentUser().lastOrganizationId
      };

      if (this.isTesterPermission) {
        this.result = this.$post('user/org/member/list/all', param, response => {
          this.scheduleReceiverOptions = response.data
        });
      }

    },
    buildParam() {
      let param = {};
      param.notices = this.tableData
      param.testId = this.testId
      return param;
    },
    open(row) {
      //测试计划页面跳转来的
      let paramTestId = "";
      this.paramRow = row;
      if (row.redirectFrom == 'testPlan') {
        paramTestId = row.id;
        this.scheduleTaskType = "TEST_PLAN_TEST";
      } else {
        paramTestId = row.id;
        this.scheduleTaskType = "API_SCENARIO_TEST";
      }
      this.testId = paramTestId;
      this.findSchedule(paramTestId);
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = 'first'
    },
    findSchedule() {
      var scheduleResourceID = this.testId;
      var taskType = this.scheduleTaskType;
      this.result = this.$get("/schedule/findOne/" + scheduleResourceID + "/" +taskType, response => {
        if (response.data != null) {
          this.schedule = response.data;
        } else {
          this.schedule = {};
        }
      });
    },
    crontabFill(value, resultList) {
      //确定后回传的值
      this.form.cronValue = value;
      this.$refs.crontabResult.resultList = resultList;
      this.$refs['from'].validate();
    },
    showCronDialog() {
      this.showCron = true;
    },
    saveCron() {
      this.$refs['from'].validate((valid) => {
        if (valid) {
          this.intervalShortValidate();
          let formCronValue = this.form.cronValue
          this.schedule.enable = true;
          this.schedule.value = formCronValue;
          this.saveSchedule();
          this.dialogVisible = false;
        } else {
          return false;
        }
      });
    },
    saveSchedule() {
      this.checkScheduleEdit();
      let param = {};
      param = this.schedule;
      param.resourceId = this.testId;
      let url = '/api/automation/schedule/create';
      if(this.scheduleTaskType === "TEST_PLAN_TEST"){
        param.scheduleFrom = "testPlan";
        //测试计划页面跳转的创建
        url = '/schedule/create';
        if (param.id) {
          url = '/schedule/update';
        }
      }else {
        param.scheduleFrom = "scenario";
        if (param.id) {
          url = '/api/automation/schedule/update';
        }
      }

      this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
        this.$emit("refreshTable");
      });
    },
    checkScheduleEdit() {
      if (this.create) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
    },
    saveNotice() {
      let param = this.buildParam();
      this.result = this.$post("notice/save", param, () => {
        this.$success(this.$t('commons.save_success'));
      })
    },
    close() {
      this.dialogVisible = false;
      this.form.cronValue = '';
      this.$refs['from'].resetFields();
      if (!this.schedule.value) {
        this.$refs.crontabResult.resultList = [];
      }
      removeGoBackListener(this.close);
    },
    intervalShortValidate() {
      if (this.getIntervalTime() < 3 * 60 * 1000) {
        // return false;
        this.$info(this.$t('schedule.cron_expression_interval_short_error'));
      }
      return true;
    },
    resultListChange() {
      this.$refs['from'].validate();
    },
    getIntervalTime() {
      let resultList = this.$refs.crontabResult.resultList;
      let time1 = new Date(resultList[0]);
      let time2 = new Date(resultList[1]);
      return time2 - time1;
    },

  },
  computed: {
    isTesterPermission() {
      return checkoutTestManagerOrTestUser();
    }
  }
}
</script>

<style scoped>

.inp {
  width: 50%;
  margin-right: 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

</style>
