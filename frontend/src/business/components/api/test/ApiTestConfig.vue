<template>
  <ms-container>
    <ms-main-container>
      <el-card>
        <el-container class="test-container" v-loading="result.loading">
          <el-header>
            <el-row>
              <el-input :disabled="isReadOnly" class="test-name" v-model="test.name" maxlength="60"
                        :placeholder="$t('api_test.input_name')"
                        show-word-limit>
                <template slot="prepend">测试名称</template>
              </el-input>

              <el-tooltip :content="'Ctrl + S'"
                          placement="top"
                          :enterable="false">
                <el-button type="primary" plain :disabled="isReadOnly" @click="saveTest">
                  {{ $t('commons.save') }}
                </el-button>
              </el-tooltip>

              <el-button type="primary" plain :disabled="isReadOnly"
                         @click="saveRunTest">
                {{ $t('load_test.save_and_run') }}
              </el-button>

              <!--              <el-button :disabled="isReadOnly" type="primary" plain v-if="isShowRun" @click="runTest">-->
              <!--                {{$t('api_test.run')}}-->
              <!--              </el-button>-->

              <el-button :disabled="isReadOnly" type="warning" plain @click="cancel">{{ $t('commons.cancel') }}
              </el-button>

              <el-dropdown trigger="click" @command="handleCommand">
                <el-button class="el-dropdown-link more" icon="el-icon-more" plain/>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="report">
                    {{ $t('api_report.title') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="jar" :disabled="isReadOnly">
                    {{ $t('api_test.jar_config.title') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="performance" :disabled="create || isReadOnly">
                    {{ $t('api_test.create_performance_test') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="export" :disabled="isReadOnly || create">
                    {{ $t('api_test.export_config') }}
                  </el-dropdown-item>
                  <el-dropdown-item command="import" :disabled="isReadOnly">
                    {{ $t('api_test.api_import.label') }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>

              <api-import ref="apiImport"/>

              <ms-api-report-dialog :test-id="id" ref="reportDialog"/>

              <ms-schedule-config :schedule="test.schedule" :is-read-only="isReadOnly" :save="saveCronExpression"
                                  @scheduleChange="saveSchedule" :test-id="id" :check-open="checkScheduleEdit"/>

              <ms-jar-config :is-read-only="isReadOnly" ref="jarConfig"/>

            </el-row>
          </el-header>
          <ms-api-scenario-config :debug-report-id="debugReportId" @runDebug="runDebug" :is-read-only="isReadOnly"
                                  :test="test" :scenarios="test.scenarioDefinition" :project-id="test.projectId"
                                  ref="config"/>
        </el-container>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsApiScenarioConfig from "./components/ApiScenarioConfig";
import {Scenario, Test} from "./model/ScenarioModel"
import MsApiReportStatus from "../report/ApiReportStatus";
import MsApiReportDialog from "./ApiReportDialog";
import {checkoutTestManagerOrTestUser, downloadFile, getCurrentProjectID, getUUID} from "@/common/js/utils";
import MsScheduleConfig from "../../common/components/MsScheduleConfig";
import ApiImport from "./components/import/ApiImport";
import {ApiEvent, LIST_CHANGE} from "@/business/components/common/head/ListEvent";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsJarConfig from "./components/jar/JarConfig";

export default {
  name: "MsApiTestConfig",

  components: {
    MsJarConfig,
    MsMainContainer,
    MsContainer, ApiImport, MsScheduleConfig, MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig
  },

  props: ["id"],

    data() {
      return {
        reportVisible: false,
        create: false,
        result: {},
        projects: [],
        change: false,
        test: new Test(),
        isReadOnly: false,
        debugReportId: ''
      }
    },

    watch: {
      '$route': 'init',
      test: {
        handler: function () {
          this.change = true;
        },
        deep: true
      }
    },

    methods: {
      init() {
        this.isReadOnly = !checkoutTestManagerOrTestUser();

        if (this.id) {
          this.create = false;
          this.getTest(this.id);
        } else {
          this.create = true;
          this.test = new Test();
          if (this.$refs.config) {
            this.$refs.config.reset();
          }
        }
        this.test.projectId = getCurrentProjectID();
      },
      updateReference() {
        let updateIds = [];
        this.test.scenarioDefinition.forEach(scenario => {
          if (scenario.isReference()) {
            updateIds.push(scenario.id.split("#")[0]);
          }
        })

        if (updateIds.length === 0) return;
        // 更新引用场景
        this.result = this.$post("/api/list/ids", {ids: updateIds}, response => {
          let scenarioMap = {};
          if (response.data) {
            response.data.forEach(test => {
              JSON.parse(test.scenarioDefinition).forEach(options => {
                let referenceId = test.id + "#" + options.id;
                scenarioMap[referenceId] = new Scenario(options);
                scenarioMap[referenceId].id = referenceId;
              })
            })
          }

          let scenarios = [];
          this.test.scenarioDefinition.forEach(scenario => {
            if (scenario.isReference()) {
              if (scenarioMap[scenario.id]) {
                let item = scenarioMap[scenario.id];
                item.referenceEnable = scenario.referenceEnable;
                scenarios.push(item);
              }
            } else {
              scenarios.push(scenario);
            }
          })
          this.test.scenarioDefinition = scenarios;
        })
      },
      getTest(id) {
        this.result = this.$get("/api/get/" + id, response => {
          if (response.data) {
            let item = response.data;

            this.test = new Test({
              id: item.id,
              projectId: item.projectId,
              name: item.name,
              status: item.status,
              scenarioDefinition: JSON.parse(item.scenarioDefinition),
              schedule: item.schedule ? item.schedule : {},
            });
            this.updateReference();

            this.$refs.config.reset();
          }
        });
      },
      save(callback) {
        let validator = this.test.isValid();
        if (!validator.isValid) {
          this.$warning(this.$t(validator.info));
          return;
        }
        this.change = false;
        let bodyFiles = this.getBodyUploadFiles();
        let url = this.create ? "/api/create" : "/api/update";
        let jmx = this.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.result = this.$fileUpload(url, file, bodyFiles, this.test, () => {
          if (callback) callback();
          this.create = false;
          this.resetBodyFile();
        });
      },
      saveTest() {
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          if (this.create) {
            this.$router.push({
              path: '/api/test/edit?id=' + this.test.id
            })
          }
          // 发送广播，刷新 head 上的最新列表
          ApiEvent.$emit(LIST_CHANGE);
        })
      },
      runTest() {
        this.result = this.$post("/api/run", {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          this.$success(this.$t('api_test.running'));
          this.$router.push({
            path: '/api/report/view/' + response.data
          })
        });
      },
      saveRunTest() {
        this.change = false;
        if (!this.validateEnableTest()) {
          this.$warning(this.$t('api_test.enable_validate_tip'));
          return;
        }
        this.save(() => {
          this.$success(this.$t('commons.save_success'));
          this.runTest();
          // 发送广播，刷新 head 上的最新列表
          ApiEvent.$emit(LIST_CHANGE);
        })
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.test.bodyUploadIds = [];
        this.test.scenarioDefinition.forEach(scenario => {
          scenario.requests.forEach(request => {
            if (request.body) {
              request.body.kvs.forEach(param => {
                if (param.files) {
                  param.files.forEach(item => {
                    if (item.file) {
                      let fileId = getUUID().substring(0, 8);
                      item.name = item.file.name;
                      item.id = fileId;
                      this.test.bodyUploadIds.push(fileId);
                      bodyUploadFiles.push(item.file);
                    }
                  });
                }
              });
            }
          });
        });
        return bodyUploadFiles;
      },
      validateEnableTest() {
        for (let scenario of this.test.scenarioDefinition) {
          if (scenario.enable) {
            for (let request of scenario.requests) {
              if (request.enable) {
                return true;
              }
            }
          }
        }
        return false;
      },
      resetBodyFile() {
        //下次保存不再上传已传文件
        this.test.scenarioDefinition.forEach(scenario => {
          scenario.requests.forEach(request => {
            if (request.body) {
              request.body.kvs.forEach(param => {
                if (param.files) {
                  param.files.forEach(item => {
                    if (item.file) {
                      item.file = undefined;
                    }
                  });
                }
              });
            }
          });
        });
      },
      cancel() {
        this.$router.push('/api/test/list/all');
        // console.log(this.test.toJMX().xml);
      },
      handleCommand(command) {
        switch (command) {
          case "report":
            this.$refs.reportDialog.open();
            break;
          case "performance":
            this.$store.commit('setTest', {
              name: this.test.name,
              jmx: this.test.toJMX()
            })
            this.$router.push({
              path: "/performance/test/create"
            })
            break;
          case "export":
            downloadFile(this.test.name + ".json", this.test.export());
            break;
          case "jar":
            this.$refs.jarConfig.open();
            break;
          case "import":
            this.$refs.apiImport.open();
            break;
        }
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
        let url = '/api/schedule/create';
        if (param.id) {
          url = '/api/schedule/update';
        }
        this.$post(url, param, () => {
          this.$success(this.$t('commons.save_success'));
          this.getTest(this.test.id);
        });
      },
      checkScheduleEdit() {
        if (this.create) {
          this.$message(this.$t('api_test.environment.please_save_test'));
          return false;
        }
        return true;
      },
      runDebug(scenario) {
        if (this.create) {
          this.$warning(this.$t('api_test.environment.please_save_test'));
          return;
        }

        let url = "/api/run/debug";
        let runningTest = new Test();
        Object.assign(runningTest, this.test);
        let bodyFiles = this.getBodyUploadFiles();
        runningTest.scenarioDefinition = [];
        runningTest.scenarioDefinition.push(scenario);
        let validator = runningTest.isValid();
        if (!validator.isValid) {
          this.$warning(this.$t(validator.info));
          return;
        }

        let jmx = runningTest.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.$fileUpload(url, file, bodyFiles, this.test, response => {
          this.debugReportId = response.data;
          this.resetBodyFile();
        });
      },
      handleEvent(event) {
        if (event.keyCode === 83 && event.ctrlKey) {
          // console.log('拦截到 ctrl + s');//ctrl+s
          this.saveTest();
          event.preventDefault();
          event.returnValue = false;
          return false;
        }
      },
    },

    created() {
      this.init();
      //
      document.addEventListener('keydown', this.handleEvent)
    },
    beforeDestroy() {
      document.removeEventListener('keydown', this.handleEvent);
    }
  }
</script>

<style scoped>
  .test-container {
    height: calc(100vh - 155px);
    min-height: 600px;
  }

  .test-name {
    width: 600px;
    margin-left: -20px;
    margin-right: 20px;
  }

  .test-project {
    min-width: 150px;
  }

  .test-container .more {
    margin-left: 10px;
  }
</style>
