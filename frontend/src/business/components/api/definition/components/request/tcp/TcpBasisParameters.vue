<template>
  <div>
    <el-row>
      <el-col :span="21" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;margin: 10px">
          <el-form class="tcp" :model="request" :rules="rules" ref="request" :disabled="isReadOnly" style="margin: 20px">

            <el-tabs v-model="activeName" class="request-tabs">

              <!--query 参数-->
              <el-tab-pane name="parameters">
                <template v-slot:label>
                  {{$t('api_test.definition.request.req_param')}}
                  <ms-instructions-icon :content="$t('api_test.definition.request.tcp_parameter_tip')"/>
                </template>
                <ms-api-variable :is-read-only="isReadOnly" :parameters="request.parameters"/>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_test.definition.request.message_template')" name="request">
                <div class="send-request">
                  <ms-code-edit mode="text" :read-only="isReadOnly" :data.sync="request.request"
                                :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
                </div>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_test.definition.request.pre_script')" name="script">
                <jsr233-processor-content
                  :jsr223-processor="request.tcpPreProcessor"
                  :is-pre-processor="true"
                  :is-read-only="isReadOnly"/>
              </el-tab-pane>

              <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="other" class="other-config">
                <el-row>
                  <el-col :span="8">
                    <el-form-item label="TCPClient" prop="classname">
                      <el-select v-model="request.classname" style="width: 100%" size="small">
                        <el-option v-for="c in classes" :key="c" :label="c" :value="c"/>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout">
                      <el-input-number v-model="request.ctimeout" controls-position="right" :min="0" size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout">
                      <el-input-number v-model="request.timeout" controls-position="right" :min="0" size="small"/>
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="10">
                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.so_linger')" prop="soLinger">
                      <el-input v-model="request.soLinger" size="small"/>
                    </el-form-item>
                  </el-col>

                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.eol_byte')" prop="eolByte">
                      <el-input v-model="request.eolByte" size="small"/>
                    </el-form-item>
                  </el-col>

                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.username')" prop="username">
                      <el-input v-model="request.username" maxlength="100" show-word-limit size="small"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.password')" prop="password">
                      <el-input v-model="request.password" maxlength="30" show-word-limit show-password
                                autocomplete="new-password" size="small"/>
                    </el-form-item>
                  </el-col>
                </el-row>


                <el-row :gutter="10" style="margin-left: 30px">
                  <el-col :span="9">
                    <el-form-item :label="$t('api_test.request.tcp.re_use_connection')">
                      <el-checkbox v-model="request.reUseConnection"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="9">
                    <el-form-item :label="$t('api_test.request.tcp.close_connection')">
                      <el-checkbox v-model="request.closeConnection"/>
                    </el-form-item>
                  </el-col>
                  <el-col :span="6">
                    <el-form-item :label="$t('api_test.request.tcp.no_delay')">
                      <el-checkbox v-model="request.nodelay"/>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-tab-pane>

            </el-tabs>


          </el-form>
        </div>

      </el-col>

      <!--操作按钮-->
      <api-definition-step-button :request="request" v-if="!referenced && showScript"/>
    </el-row>
  </div>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import MsCodeEdit from "../../../../../common/components/MsCodeEdit";
  import MsApiScenarioVariables from "../../ApiScenarioVariables";
  import {createComponent} from "../../jmeter/components";
  import {Assertions, Extract} from "../../../model/ApiTestModel";
  import {parseEnvironment} from "../../../model/EnvironmentModel";
  import ApiEnvironmentConfig from "../../environment/ApiEnvironmentConfig";
  import {API_STATUS} from "../../../model/JsonData";
  import TCPSampler from "../../jmeter/components/sampler/tcp-sampler";
  import {getCurrentProjectID, getUUID} from "@/common/js/utils";
  import MsApiVariable from "../../ApiVariable";
  import MsInstructionsIcon from "../../../../../common/components/MsInstructionsIcon";
  import Jsr233ProcessorContent from "../../../../automation/scenario/common/Jsr233ProcessorContent";
  import JSR223PreProcessor from "../../jmeter/components/pre-processors/jsr223-pre-processor";
  import ApiDefinitionStepButton from "../components/ApiDefinitionStepButton";


  export default {
    name: "TcpBasisParameters",
    components: {
      ApiDefinitionStepButton,
      Jsr233ProcessorContent,
      MsInstructionsIcon,
      MsApiVariable,
      MsApiScenarioVariables,
      MsCodeEdit,
      ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue, ApiEnvironmentConfig
    },
    props: {
      request: {},
      basisData: {},
      moduleOptions: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      showScript: {
        type: Boolean,
        default: true,
      },
      referenced: {
        type: Boolean,
        default: false,
      },
    },
    data() {
      return {
        activeName: "parameters",
        classes: TCPSampler.CLASSES,
        isReloadData: false,
        options: API_STATUS,
        currentProjectId: "",
        rules: {
          classname: [{required: true, message: "请选择TCPClient", trigger: 'change'}],
          server: [{required: true, message: this.$t('api_test.request.tcp.server_cannot_be_empty'), trigger: 'blur'}],
          port: [{required: true, message: this.$t('commons.port_cannot_be_empty'), trigger: 'change'}],
        },
      }
    },
    created() {
      this.currentProjectId = getCurrentProjectID();
      if (!this.request.parameters) {
        this.$set(this.request, 'parameters', []);
        this.request.parameters = [];
      }
      if (!this.request.tcpPreProcessor) {
        this.$set(this.request, 'tcpPreProcessor', new JSR223PreProcessor())
      }
      this.getEnvironments();
    },
    methods: {
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let assertions = new Assertions();
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      copyRow(row) {
        let obj =JSON.parse(JSON.stringify(row));
        obj.id = getUUID();
        this.request.hashTree.push(obj);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      validateApi() {
        if (this.currentProjectId === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

      },
      validate() {
        if (this.currentProjectId === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['request'].validate((valid) => {
          if (valid) {
            this.$emit('callback');
          }
        })
      },
      getEnvironments() {
        if (this.currentProjectId) {
          this.environments = [];
          this.$get('/api/environment/list/' + this.currentProjectId, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            this.initDataSource();
          });
        }
      },
      openEnvironmentConfig() {
        if (!this.currentProjectId) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProjectId);
      },
      initDataSource() {
        for (let i in this.environments) {
          if (this.environments[i].id === this.request.environmentId) {
            this.databaseConfigsOptions = [];
            this.environments[i].config.databaseConfigs.forEach(item => {
              this.databaseConfigsOptions.push(item);
            })
            break;
          }
        }
      },
      environmentChange(value) {
        this.request.dataSource = undefined;
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.databaseConfigsOptions = [];
            this.environments[i].config.databaseConfigs.forEach(item => {
              this.databaseConfigsOptions.push(item);
            })
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      }
    }

  }
</script>

<style scoped>
  .tcp >>> .el-input-number {
    width: 100%;
  }

  .send-request {
    padding: 0px 0;
    height: 300px;
    border: 1px #DCDFE6 solid;
    border-radius: 4px;
    width: 100%;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  /deep/ .instructions-icon {
    font-size: 14px !important;
  }

  .request-tabs {
    margin: 20px;
    min-height: 200px;
  }

  .other-config {
    padding: 15px;
  }

</style>
