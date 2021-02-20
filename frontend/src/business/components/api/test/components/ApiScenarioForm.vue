<template>
  <el-form :model="scenario" :rules="rules" ref="scenario" label-width="100px" v-loading="result.loading"
           :disabled="isReadOnly">
    <el-form-item :label="$t('api_test.scenario.name')" prop="name">
      <el-input :disabled="isReadOnly" v-model="scenario.name" maxlength="100" show-word-limit/>
    </el-form-item>

    <el-form-item :label="$t('api_test.environment.environment')">
      <el-select :disabled="isReadOnly" v-model="scenario.environmentId" class="environment-select"
                 @change="environmentChange" clearable>
        <el-option v-for="(environment, index) in environments" :key="index"
                   :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                   :value="environment.id"/>
        <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
          {{ $t('api_test.environment.environment_config') }}
        </el-button>
        <template v-slot:empty>
          <div class="empty-environment">
            <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
              {{ $t('api_test.environment.environment_config') }}
            </el-button>
          </div>
        </template>
      </el-select>
      <el-form-item class="cookie-item">
        <el-checkbox v-model="scenario.enableCookieShare">{{ '共享 Cookie' }}</el-checkbox>
      </el-form-item>
    </el-form-item>

    <el-tabs v-model="activeName" :disabled="isReadOnly">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="parameters">
        <ms-api-scenario-variables :is-read-only="isReadOnly" :items="scenario.variables"
                                   :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
        <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="true" :items="scenario.headers"
                          :suggestions="headerSuggestions"
                          :environment="scenario.environment"
                          :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.environment.database_config')" name="database">
        <ms-database-config :configs="scenario.databaseConfigs" :is-read-only="isReadOnly"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.dubbo')" name="dubbo">
        <div class="dubbo-config-title">Config Center</div>
        <ms-dubbo-config-center :config="scenario.dubboConfig.configCenter" :is-read-only="isReadOnly"/>
        <div class="dubbo-config-title">Registry Center</div>
        <ms-dubbo-registry-center :registry="scenario.dubboConfig.registryCenter" :is-read-only="isReadOnly"/>
        <div class="dubbo-config-title">Consumer & Service</div>
        <ms-dubbo-consumer-service :consumer="scenario.dubboConfig.consumerAndService" :is-read-only="isReadOnly"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.environment.tcp_config')" name="tcp">
        <ms-tcp-config :config="scenario.tcpConfig" :is-read-only="isReadOnly"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions.label')" name="assertions">
        <ms-api-assertions :scenario="scenario" :is-read-only="isReadOnly" :assertions="scenario.assertions"/>
      </el-tab-pane>
    </el-tabs>

    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>

  </el-form>

</template>

<script>
import MsApiKeyValue from "./ApiKeyValue";
import {Scenario} from "../model/ScenarioModel";
import MsApiScenarioVariables from "./ApiScenarioVariables";
import ApiEnvironmentConfig from "./ApiEnvironmentConfig";
import {REQUEST_HEADERS} from "@/common/js/constants";
import MsDubboRegistryCenter from "@/business/components/api/test/components/request/dubbo/RegistryCenter";
import MsDubboConfigCenter from "@/business/components/api/test/components/request/dubbo/ConfigCenter";
import MsDubboConsumerService from "@/business/components/api/test/components/request/dubbo/ConsumerAndService";
import MsDatabaseConfig from "./request/database/DatabaseConfig";
import {parseEnvironment} from "../model/EnvironmentModel";
import MsTcpConfig from "@/business/components/api/test/components/request/tcp/TcpConfig";
import MsApiAssertions from "@/business/components/api/test/components/assertion/ApiAssertions";

export default {
  name: "MsApiScenarioForm",
  components: {
    MsTcpConfig,
    MsDatabaseConfig,
    MsDubboConsumerService,
    MsDubboConfigCenter, MsDubboRegistryCenter, ApiEnvironmentConfig, MsApiScenarioVariables, MsApiKeyValue,
    MsApiAssertions
  },
  props: {
    scenario: Scenario,
    projectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  created() {
    this.getEnvironments();
  },
  data() {
    return {
      result: {},
      activeName: "parameters",
      environments: [],
      rules: {
        name: [
          {max: 100, message: this.$t('commons.input_limit', [1, 100]), trigger: 'blur'}
        ],
        url: [
          {max: 100, message: this.$t('commons.input_limit', [1, 100]), trigger: 'blur'}
        ]
      },
      headerSuggestions: REQUEST_HEADERS
    }
  },
  watch: {
    projectId() {
      this.getEnvironments();
    }
  },
  methods: {
    getEnvironments() {
      if (this.projectId) {
        this.result = this.$get('/api/environment/list/' + this.projectId, response => {
          this.environments = response.data;
          this.environments.forEach(environment => {
            parseEnvironment(environment);
          });
          let hasEnvironment = false;
          for (let i in this.environments) {
            if (this.environments[i].id === this.scenario.environmentId) {
              this.scenario.environment = this.environments[i];
              hasEnvironment = true;
              break;
            }
          }
          if (!hasEnvironment) {
            this.scenario.environmentId = '';
            this.scenario.environment = undefined;
          }
        });
      } else {
        this.scenario.environmentId = '';
        this.scenario.environment = undefined;
      }
    },
    environmentChange(value) {
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          this.scenario.environment = this.environments[i];
          break;
        }
      }
      if (!value) {
        this.scenario.environment = undefined;
        this.scenario.requests.forEach(request => {
          request.environment = undefined;
        });
      }
    },
    openEnvironmentConfig() {
      if (!this.projectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.environmentConfig.open(this.projectId);
    },
    environmentConfigClose() {
      this.getEnvironments();
    }
  }
}
</script>

<style scoped>

.environment-select {
  width: 100%;
}

.environment-button {
  margin-left: 20px;
  padding: 7px;
}

.empty-environment {
  padding: 10px 0;
}

.dubbo-config-title {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
}

.cookie-item {
  margin-top: 15px;
}

</style>
