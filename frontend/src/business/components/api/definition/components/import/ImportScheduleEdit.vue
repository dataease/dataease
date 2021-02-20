<template>
  <el-dialog :close-on-click-modal="false" width="60%" class="schedule-edit" :visible.sync="dialogVisible"
             append-to-body @close="close">
    <template>
      <div>
        <el-tabs v-model="activeName">
          <el-tab-pane :label="$t('schedule.edit_timer_task')" name="first">
            <el-form :model="form" :rules="rules" ref="from">
              <el-form-item
                prop="cronValue">
                <el-input :disabled="isReadOnly" v-model="form.cronValue" class="inp"
                          :placeholder="$t('schedule.please_input_cron_expression')"/>
                <el-button :disabled="isReadOnly" type="primary" @click="saveCron" v-tester>{{
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
          <el-tab-pane :label="$t('api_test.home_page.running_task_list.title')" name="second">
            <swagger-task-list></swagger-task-list>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {
  checkoutTestManagerOrTestUser,
  getCurrentProjectID,
  getCurrentUser,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
import Crontab from "@/business/components/common/cron/Crontab";
import CrontabResult from "@/business/components/common/cron/CrontabResult";
import {cronValidate} from "@/common/js/cron";
import SwaggerTaskList from "@/business/components/api/definition/components/import/SwaggerTaskList";

function defaultCustomValidate() {
  return {pass: true};
}

export default {
  name: "ImportScheduleEdit",
  components: {SwaggerTaskList, CrontabResult, Crontab},

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
      operation: true,
      dialogVisible: false,
      schedule: {
        value: "",
      },
      showCron: false,
      form: {
        cronValue: ""
      },
      activeName: 'first',
      swaggerUrl: String,
      projectId: String,
      moduleId: String,
      paramSwaggerUrlId: String,
      modulePath: String,
      modeId: String,
      rules: {
        cronValue: [{required: true, validator: validateCron, trigger: 'blur'}],
      }
    }
  },
  methods: {
    currentUser: () => {
      return getCurrentUser();
    },
    open(param) {
      this.$post("/api/definition/getResourceId", param, response => {
        this.paramSwaggerUrlId = response.data
        if (this.paramSwaggerUrlId === "" || this.paramSwaggerUrlId === null || this.paramSwaggerUrlId === undefined) {
          console.log(this.paramSwaggerUrlId)
        } else {
          this.findSchedule(this.paramSwaggerUrlId);

        }
        this.project = param.projectId;
        this.swaggerUrl = param.swaggerUrl;
        this.dialogVisible = true;
        this.form.cronValue = this.schedule.value;
        this.moduleId = param.moduleId;
        this.modulePath = param.modulePath;
        this.modeId = param.modeId;
        listenGoBack(this.close);
      })

      this.activeName = 'first';
    },
    findSchedule(paramSwaggerUrlId) {
      let scheduleResourceID = paramSwaggerUrlId;
      let taskType = "SWAGGER_IMPORT";
      this.result = this.$get("/schedule/findOne/" + scheduleResourceID + "/" + taskType, response => {
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
      param.resourceId = this.swaggerUrl;
      param.projectId = getCurrentProjectID();
      param.moduleId = this.moduleId;
      param.modulePath = this.modulePath;
      param.modeId = this.modeId;
      let url = '';
      if (this.paramSwaggerUrlId) {
        url = '/api/definition/schedule/update';
      } else {
        url = '/api/definition/schedule/create';
      }
      this.$post(url, param, () => {
        this.$success(this.$t('commons.save_success'));
      });
    },
    checkScheduleEdit() {
      if (this.create) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
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
