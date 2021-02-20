<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <el-row>
      <el-col :span="21">
        <!-- HTTP 请求参数 -->
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%" v-loading="isReloadData">
          <el-tabs v-model="activeName" class="request-tabs">
            <!-- 请求头-->
            <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.request.headers')" placement="top-start" slot="label">
              <span>{{$t('api_test.request.headers')}}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="headers.length>1">
                  <div class="el-step__icon-inner">{{headers.length-1}}</div>
                </div>
              </span>
              </el-tooltip>
              <el-row>
                <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{$t("commons.batch_add")}}</el-link>
              </el-row>

              <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :suggestions="headerSuggestions" :items="headers"/>
            </el-tab-pane>

            <!--query 参数-->
            <el-tab-pane :label="$t('api_test.definition.request.query_param')" name="parameters">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.query_info')" placement="top-start" slot="label">
              <span>{{$t('api_test.definition.request.query_param')}}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.arguments.length>1">
                  <div class="el-step__icon-inner">{{request.arguments.length-1}}</div>
                </div></span>
              </el-tooltip>
              <el-row>
                <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{$t("commons.batch_add")}}</el-link>
              </el-row>
              <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.arguments"/>
            </el-tab-pane>

            <!--REST 参数-->
            <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.rest_info')" placement="top-start" slot="label">
              <span>
                {{$t('api_test.definition.request.rest_param')}}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.rest.length>1">
                  <div class="el-step__icon-inner">{{request.rest.length-1}}</div>
                </div>
              </span>
              </el-tooltip>
              <el-row>
                <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{$t("commons.batch_add")}}</el-link>
              </el-row>
              <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.rest"/>
            </el-tab-pane>

            <!--请求体-->
            <el-tab-pane v-if="isBodyShow" :label="$t('api_test.request.body')" name="body" style="overflow: auto">
              <ms-api-body @headersChange="reloadBody" :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :headers="headers" :body="request.body"/>
            </el-tab-pane>

            <!-- 认证配置 -->
            <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')" placement="top-start" slot="label">
                <span>{{$t('api_test.definition.request.auth_config')}}</span>
              </el-tooltip>

              <ms-api-auth-config :is-read-only="isReadOnly" :request="request"/>
            </el-tab-pane>

            <el-tab-pane label="其他设置" name="advancedConfig">
              <ms-api-advanced-config :is-read-only="isReadOnly" :request="request"/>
            </el-tab-pane>

          </el-tabs>
        </div>
      </el-col>
      <!--操作按钮-->
      <api-definition-step-button :request="request" v-if="!referenced && showScript"/>
    </el-row>
    <batch-add-parameter @batchSave="batchSave" ref="batchAddParameter"/>
  </div>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiBody from "../../body/ApiBody";
  import MsApiAuthConfig from "../../auth/ApiAuthConfig";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiVariable from "../../ApiVariable";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import {Body, KeyValue} from "../../../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";
  import BatchAddParameter from "../../basis/BatchAddParameter";
  import MsApiAdvancedConfig from "./ApiAdvancedConfig";
  import MsJsr233Processor from "../../../../automation/scenario/component/Jsr233Processor";
  import ApiDefinitionStepButton from "../components/ApiDefinitionStepButton";

  export default {
    name: "MsApiHttpRequestForm",
    components: {
      ApiDefinitionStepButton,
      MsJsr233Processor,
      MsApiAdvancedConfig,
      BatchAddParameter,
      MsApiVariable,
      ApiRequestMethodSelect,
      MsApiExtract,
      MsApiAuthConfig,
      MsApiBody,
      MsApiKeyValue,
      MsApiAssertions
    },
    props: {
      request: {},
      response: {},
      showScript: {
        type: Boolean,
        default: true,
      },
      headers: {
        type: Array,
        default() {
          return [];
        }
      },
      referenced: {
        type: Boolean,
        default: false,
      },
      isShowEnable: Boolean,
      jsonPathList: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      let validateURL = (rule, value, callback) => {
        try {
          new URL(this.addProtocol(this.request.url));
        } catch (e) {
          callback(this.$t('api_test.request.url_invalid'));
        }
      };
      return {
        activeName: "headers",
        rules: {
          name: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
          url: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
            {validator: validateURL, trigger: 'blur'}
          ],
          path: [
            {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
          ]
        },
        headerSuggestions: REQUEST_HEADERS,
        isReloadData: false,
        isBodyShow: true,
        dialogVisible: false,
      }
    },

    created() {
      this.init();
    },

    methods: {
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      copyRow(row) {
        let obj = JSON.parse(JSON.stringify(row));
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
      init() {
        if (!this.request.body) {
          this.request.body = new Body();
        }
        if (!this.request.body.kvs) {
          this.request.body.kvs = [];
        }
        if (!this.request.rest) {
          this.request.rest = [];
        }
        if (!this.request.arguments) {
          this.request.arguments = [];
        }
      },
      // 解决修改请求头后 body 显示错位
      reloadBody() {
        this.isBodyShow = false;
        this.$nextTick(() => {
          this.isBodyShow = true;
        });
      },
      batchAdd() {
        this.$refs.batchAddParameter.open();
      },
      batchSave(data) {
        if (data) {
          let params = data.split("\n");
          let keyValues = [];
          params.forEach(item => {
            let line = item.split(/，|,/);
            let required = false;
            if (line[1] === '必填' || line[1] === 'true') {
              required = true;
            }
            keyValues.push(new KeyValue({name: line[0], required: required, value: line[2], description: line[3], type: "text", valid: false, file: false, encode: true, enable: true, contentType: "text/plain"}));
          })
          keyValues.forEach(item => {
            switch (this.activeName) {
              case "parameters":
                this.request.arguments.unshift(item);
                break;
              case "rest":
                this.request.rest.unshift(item);
                break;
              case "headers":
                this.request.headers.unshift(item);
                break;
              default:
                break;
            }
          })
        }
      }
    }
  }
</script>

<style scoped>

  .ms-query {
    background: #783887;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .ms-header {
    background: #783887;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .request-tabs {
    margin: 20px;
    min-height: 200px;
  }

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }
</style>
