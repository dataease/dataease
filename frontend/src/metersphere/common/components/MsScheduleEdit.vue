<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
             @close="close">
    <template>
      <div>
        <el-tabs v-model="activeName">

          <el-tab-pane :label="$t('schedule.edit_timer_task')" name="first">
            <el-form :model="form" :rules="rules" ref="from">
              <el-form-item
                prop="cronValue">
                <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                          :placeholder="$t('schedule.please_input_cron_expression')"/>
                <!--          <el-button type="primary" @click="showCronDialog">{{$t('schedule.generate_expression')}}</el-button>-->
                <el-button :disabled="isReadOnly" type="primary" @click="saveCron">{{
                    $t('commons.save')
                  }}
                </el-button>
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
            <schedule-task-notification :is-tester-permission="isTesterPermission" :test-id="testId"
                                        :schedule-receiver-options="scheduleReceiverOptions"/>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {checkoutTestManagerOrTestUser, getCurrentUser, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import Crontab from "../cron/Crontab";
import CrontabResult from "../cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import ScheduleTaskNotification from "../../settings/organization/components/ScheduleTaskNotification";

function defaultCustomValidate() {
  return {pass: true};
}

export default {
  name: "MsScheduleEdit",
  components: {CrontabResult, Crontab, ScheduleTaskNotification},
  props: {
    testId: String,
    save: Function,
    schedule: {},
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
      }
        // else if(!this.intervalShortValidate()) {
        //   callback(new Error(this.$t('schedule.cron_expression_interval_short_error')));
      // }
      else if (!customValidate.pass) {
        callback(new Error(customValidate.info));
      } else {
        callback();
      }
    };
    return {
      scheduleReceiverOptions: [],
      operation: true,
      dialogVisible: false,
      showCron: false,
      form: {
        cronValue: ""
      },
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
    /* handleClick() {
       if (this.activeName === "second") {
           this.result = this.$get('/notice/search/message/'+this.testId, response => {
             this.scheduleTask = response.data;
           })
       }
     },*/
    buildParam() {
      let param = {};
      param.notices = this.tableData
      param.testId = this.testId
      return param;
    },
    open() {
      this.initUserList();
      this.dialogVisible = true;
      this.form.cronValue = this.schedule.value;
      listenGoBack(this.close);
      this.activeName = 'first'
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
          this.save(this.form.cronValue);
          this.dialogVisible = false;
        } else {
          return false;
        }
      });
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
