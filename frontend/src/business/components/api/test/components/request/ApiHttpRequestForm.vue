<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly">

    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input :disabled="isReadOnly" v-model="request.name" maxlength="300" show-word-limit/>
    </el-form-item>

    <el-form-item v-if="!request.useEnvironment" :label="$t('api_test.request.url')" prop="url"
                  class="adjust-margin-bottom">
      <el-input :disabled="isReadOnly" v-model="request.url" maxlength="500"
                :placeholder="$t('api_test.request.url_description')" @change="urlChange" clearable>
        <template v-slot:prepend>
          <ApiRequestMethodSelect :is-read-only="isReadOnly" :request="request" @change="methodChange"/>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item v-if="request.useEnvironment" :label="$t('api_test.request.path')" prop="path">
      <el-input :disabled="isReadOnly" v-model="request.path" maxlength="500"
                :placeholder="$t('api_test.request.path_description')" @change="pathChange" clearable>
        <template v-slot:prepend>
          <ApiRequestMethodSelect :is-read-only="isReadOnly" :request="request" @change="methodChange"/>
        </template>
      </el-input>
    </el-form-item>

    <el-form-item v-if="request.useEnvironment" :label="$t('api_test.request.address')" class="adjust-margin-bottom">
      <el-tag class="environment-display">
        <span class="environment-name">{{ scenario.environment ? scenario.environment.name + ': ' : '' }}</span>
        <span class="environment-url">{{ displayUrl }}</span>
        <span v-if="!displayUrl"
              class="environment-url-tip">{{ $t('api_test.request.please_configure_socket_in_environment') }}</span>
      </el-tag>
    </el-form-item>

    <el-form-item>
      <el-switch
        v-model="request.useEnvironment"
        :active-text="$t('api_test.request.refer_to_environment')" @change="useEnvironmentChange">
      </el-switch>
      <el-checkbox class="follow-redirects-item" v-model="request.followRedirects">{{$t('api_test.request.follow_redirects')}}</el-checkbox>
      <el-checkbox class="do-multipart-post" v-model="request.doMultipartPost">{{$t('api_test.request.do_multipart_post')}}</el-checkbox>
    </el-form-item>

    <el-button :disabled="!request.enable || !scenario.enable || isReadOnly" class="debug-button" size="small"
               type="primary" @click="runDebug">{{ $t('api_test.request.debug') }}
    </el-button>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.request.parameters')" name="parameters">
        <ms-api-variable :is-read-only="isReadOnly"
                         :parameters="request.parameters"
                         :environment="scenario.environment"
                         :scenario="scenario"
                         :extract="request.extract"
                         :description="$t('api_test.request.parameters_desc')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
        <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="true" :suggestions="headerSuggestions" :items="request.headers"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.body')" name="body">
        <ms-api-body :is-read-only="isReadOnly"
                     :body="request.body"
                     :scenario="scenario"
                     :extract="request.extract"
                     :environment="scenario.environment"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions.label')" name="assertions">
        <ms-api-assertions :request="request" :is-read-only="isReadOnly" :assertions="request.assertions"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.extract.label')" name="extract">
        <ms-api-extract :is-read-only="isReadOnly" :extract="request.extract"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.processor.pre_exec_script')" name="jsr223PreProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PreProcessor"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.processor.post_exec_script')" name="jsr223PostProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PostProcessor"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.timeout_config')" name="advancedConfig">
        <ms-api-advanced-config :is-read-only="isReadOnly" :request="request"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
import MsApiKeyValue from "../ApiKeyValue";
import MsApiBody from "../body/ApiBody";
import MsApiAssertions from "../assertion/ApiAssertions";
import {HttpRequest, KeyValue, Scenario} from "../../model/ScenarioModel";
import MsApiExtract from "../extract/ApiExtract";
import ApiRequestMethodSelect from "../collapse/ApiRequestMethodSelect";
import {REQUEST_HEADERS} from "@/common/js/constants";
import MsApiVariable from "@/business/components/api/test/components/ApiVariable";
import MsApiAdvancedConfig from "../ApiAdvancedConfig";
import MsJsr233Processor from "../processor/Jsr233Processor";

export default {
  name: "MsApiHttpRequestForm",
  components: {
    MsJsr233Processor,
    MsApiAdvancedConfig,
    MsApiVariable, ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiBody, MsApiKeyValue},
  props: {
    request: HttpRequest,
    jsonPathList: Array,
    scenario: Scenario,
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
      activeName: "parameters",
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
      headerSuggestions: REQUEST_HEADERS
    }
  },

  methods: {
    urlChange() {
      if (!this.request.url) return;
      let url = this.getURL(this.addProtocol(this.request.url));
      if (url) {
        this.request.url = decodeURIComponent(url.origin + url.pathname);
      }
    },
    pathChange() {
      if (!this.request.path) return;
      let url = this.getURL(this.displayUrl);
      let urlStr = url.origin + url.pathname;
      let envUrl = this.scenario.environment.config.httpConfig.protocol + '://' + this.scenario.environment.config.httpConfig.socket;
      this.request.path = decodeURIComponent(urlStr.substring(envUrl.length, urlStr.length));
    },
    getURL(urlStr) {
      try {
        let url = new URL(urlStr);
        url.searchParams.forEach((value, key) => {
          if (key && value) {
            this.request.parameters.splice(0, 0, new KeyValue({name: key, value: value}));
          }
        });
        return url;
      } catch (e) {
        this.$error(this.$t('api_test.request.url_invalid'), 2000);
      }
    },
    methodChange(value) {
      if (value === 'GET' && this.activeName === 'body') {
        this.activeName = 'parameters';
      }
    },
    useEnvironmentChange(value) {
      if (value && !this.scenario.environment) {
        this.$error(this.$t('api_test.request.please_add_environment_to_scenario'), 2000);
        this.request.useEnvironment = false;
      }
      this.$refs["request"].clearValidate();
    },
    addProtocol(url) {
      if (url) {
        if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
          return "https://" + url;
        }
      }
      return url;
    },
    runDebug() {
      this.$emit('runDebug');
    }
  },

  computed: {
    displayUrl() {
      return (this.scenario.environment && this.scenario.environment.config.httpConfig.socket) ?
        this.scenario.environment.config.httpConfig.protocol + '://' + this.scenario.environment.config.httpConfig.socket + (this.request.path ? this.request.path : '')
        : '';
    }
  }
}
</script>

<style scoped>

  .el-tag {
    width: 100%;
    height: 40px;
    line-height: 40px;
  }

  .environment-display {
    font-size: 14px;
  }

  .environment-name {
    font-weight: bold;
    font-style: italic;
  }

  .adjust-margin-bottom {
    margin-bottom: 10px;
  }

  .environment-url-tip {
    color: #F56C6C;
  }

  .follow-redirects-item {
    margin-left: 30px;
  }

  .do-multipart-post {
    margin-left: 10px;
  }

</style>
