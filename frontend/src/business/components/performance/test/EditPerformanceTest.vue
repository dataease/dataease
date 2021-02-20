<template>
  <ms-container>
    <ms-main-container>
      <el-card v-loading="result.loading">
        <el-row>
          <el-col :span="10">
            <el-input :disabled="isReadOnly" :placeholder="$t('load_test.input_name')" v-model="test.name"
                      class="input-with-select"
                      maxlength="30" show-word-limit
            >
              <template slot="prepend">测试名称</template>
            </el-input>
          </el-col>
          <el-col :span="12" :offset="2">
            <el-button :disabled="isReadOnly" type="primary" plain @click="save">{{ $t('commons.save') }}</el-button>
            <el-button :disabled="isReadOnly" type="primary" plain @click="saveAndRun">
              {{ $t('load_test.save_and_run') }}
            </el-button>
            <el-button :disabled="isReadOnly" type="warning" plain @click="cancel">{{ $t('commons.cancel') }}
            </el-button>

            <ms-schedule-config :schedule="test.schedule" :save="saveCronExpression" @scheduleChange="saveSchedule"
                                :check-open="checkScheduleEdit" :test-id="testId" :custom-validate="durationValidate"/>
          </el-col>
        </el-row>


        <el-tabs class="testplan-config" v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('load_test.basic_config')">
            <performance-basic-config :is-read-only="isReadOnly" :test="test" ref="basicConfig"
                                      @fileChange="fileChange"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.pressure_config')">
            <performance-pressure-config :is-read-only="isReadOnly" :test="test" :test-id="testId"
                                         ref="pressureConfig" @changeActive="changeTabActive"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('load_test.advanced_config')" class="advanced-config">
            <performance-advanced-config :read-only="isReadOnly" :test-id="testId" ref="advancedConfig"/>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import PerformanceBasicConfig from "./components/PerformanceBasicConfig";
import PerformancePressureConfig from "./components/PerformancePressureConfig";
import PerformanceAdvancedConfig from "./components/PerformanceAdvancedConfig";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {checkoutTestManagerOrTestUser, getCurrentProjectID} from "@/common/js/utils";
import MsScheduleConfig from "../../common/components/MsScheduleConfig";
import {LIST_CHANGE, PerformanceEvent} from "@/business/components/common/head/ListEvent";

export default {
  name: "EditPerformanceTest",
  components: {
    MsScheduleConfig,
    PerformancePressureConfig,
    PerformanceBasicConfig,
    PerformanceAdvancedConfig,
    MsContainer,
    MsMainContainer
  },
  data() {
    return {
      result: {},
      test: {schedule: {}},
      savePath: "/performance/save",
      editPath: "/performance/edit",
      runPath: "/performance/run",
      projects: [],
      active: '0',
      testId: '',
      isReadOnly: false,
      tabs: [{
        title: this.$t('load_test.basic_config'),
        id: '0',
        component: 'PerformanceBasicConfig'
      }, {
        title: this.$t('load_test.pressure_config'),
        id: '1',
        component: 'PerformancePressureConfig'
      }, {
        title: this.$t('load_test.advanced_config'),
        id: '2',
        component: 'PerformanceAdvancedConfig'
      }]
    }
  },
  watch: {
    '$route'(to) {
      // 如果是创建测试
      if (to.name === 'createPerTest') {
        window.location.reload();
        return;
      }

      if (to.name !== 'editPerTest') {
        return;
      }

      this.isReadOnly = false;
      if (!checkoutTestManagerOrTestUser()) {
        this.isReadOnly = true;
      }
      this.getTest(to.params.testId);
    }

  },
  created() {
    this.isReadOnly = false;
    if (!checkoutTestManagerOrTestUser()) {
      this.isReadOnly = true;
    }
    this.getTest(this.$route.params.testId);
  },
  mounted() {
    this.importAPITest();
  },
  methods: {
    importAPITest() {
      let apiTest = this.$store.state.api.test;
      if (apiTest && apiTest.name) {
        this.$set(this.test, "name", apiTest.name);
        let blob = new Blob([apiTest.jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], apiTest.jmx.name);
        this.$refs.basicConfig.beforeUpload(file);
        this.$refs.basicConfig.handleUpload({file: file});
        this.active = '1';
        this.$store.commit("clearTest");
      }
    },
    getTest(testId) {
      if (testId) {
        this.testId = testId;
        this.result = this.$get('/performance/get/' + testId, response => {
          if (response.data) {
            this.test = response.data;
            if (!this.test.schedule) {
              this.test.schedule = {};
            }
          }
        });
      }
    },
    save() {
      if (!this.validTest()) {
        return;
      }

      let options = this.getSaveOption();

      this.result = this.$request(options, () => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.advancedConfig.cancelAllEdit();
        this.$router.push({path: '/performance/test/all'})
        // 发送广播，刷新 head 上的最新列表
        PerformanceEvent.$emit(LIST_CHANGE);
      });
    },
    saveAndRun() {
      if (!this.validTest()) {
        return;
      }

      let options = this.getSaveOption();

      this.result = this.$request(options, (response) => {
        this.test.id = response.data;
        this.$success(this.$t('commons.save_success'));
        this.result = this.$post(this.runPath, {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          let reportId = response.data;
          this.$router.push({path: '/performance/report/view/' + reportId})
          // 发送广播，刷新 head 上的最新列表
          PerformanceEvent.$emit(LIST_CHANGE);
        })
      });
    },
    getSaveOption() {
      let formData = new FormData();
      let url = this.test.id ? this.editPath : this.savePath;

      if (this.$refs.basicConfig.uploadList.length > 0) {
        this.$refs.basicConfig.uploadList.forEach(f => {
          formData.append("file", f);
        });
      }
      // 基本配置
      this.test.updatedFileList = this.$refs.basicConfig.updatedFileList();
      // 压力配置
      this.test.loadConfiguration = JSON.stringify(this.$refs.pressureConfig.convertProperty());
      this.test.testResourcePoolId = this.$refs.pressureConfig.resourcePool;
      // 高级配置
      this.test.advancedConfiguration = JSON.stringify(this.$refs.advancedConfig.configurations());

      // file属性不需要json化
      let requestJson = JSON.stringify(this.test, function (key, value) {
        return key === "file" ? undefined : value
      });

      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));

      return {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
    },
    cancel() {
      this.$router.push({path: '/performance/test/all'})
    },
    validTest() {
      let currentProjectId = getCurrentProjectID();
      this.test.projectId = currentProjectId;

      if (!this.test.name) {
        this.$error(this.$t('load_test.test_name_is_null'));
        return false;
      }

      if (!this.test.projectId) {
        this.$error(this.$t('load_test.project_is_null'));
        return false;
      }

      if (!this.$refs.basicConfig.validConfig()) {
        return false;
      }

      if (!this.$refs.pressureConfig.validConfig()) {
        return false;
      }

      if (!this.$refs.advancedConfig.validConfig()) {
        return false;
      }

      /// todo: 其他校验

      return true;
    },
    changeTabActive(activeName) {
      this.$nextTick(() => {
        this.active = activeName;
      });
    },
    saveCronExpression(cronExpression) {
      this.test.schedule.enable = true;
      this.test.schedule.value = cronExpression;
      this.saveSchedule();
    },
    saveSchedule() {
      this.checkScheduleEdit();
      let param = {};
      param = this.test.schedule;
      param.resourceId = this.test.id;
      let url = '/performance/schedule/create';
      if (param.id) {
        url = '/performance/schedule/update';
      }
      this.$post(url, param, response => {
        this.$success(this.$t('commons.save_success'));
        this.getTest(this.test.id);
      });
    },
    checkScheduleEdit() {
      if (!this.test.id) {
        this.$message(this.$t('api_test.environment.please_save_test'));
        return false;
      }
      return true;
    },
    durationValidate(intervalTime) {
      let duration = this.$refs.pressureConfig.duration * 60 * 1000;
      if (intervalTime < duration) {
        return {
          pass: false,
          info: this.$t('load_test.schedule_tip')
        }
      }
      return {
        pass: true
      }
    },
    fileChange(threadGroups) {
      let handler = this.$refs.pressureConfig;

      threadGroups.forEach(tg => {
        tg.threadNumber = tg.threadNumber || 10;
        tg.duration = tg.duration || 10;
        tg.rampUpTime = tg.rampUpTime || 5;
        tg.step = tg.step || 5;
        tg.rpsLimit = tg.rpsLimit || 10;
        tg.threadType = tg.threadType || 'DURATION';
        tg.iterateNum = tg.iterateNum || 1;
        tg.iterateRampUp = tg.iterateRampUp || 10;
      });

      this.$set(handler, "threadGroups", threadGroups);

      threadGroups.forEach(tg => {
        handler.calculateChart(tg);
      })
    }
  }
}
</script>

<style scoped>

.testplan-config {
  margin-top: 15px;
  text-align: center;
}

.el-select {
  min-width: 130px;
}

.edit-testplan-container .input-with-select .el-input-group__prepend {
  background-color: #fff;
}

.advanced-config {
  height: calc(100vh - 280px);
  overflow: auto;
}
</style>
