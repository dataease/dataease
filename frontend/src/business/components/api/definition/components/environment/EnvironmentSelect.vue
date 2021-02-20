<template>
    <span>
      <el-select v-model="currentData.environmentId" size="small" class="ms-htt-width"
                 :placeholder="$t('api_test.definition.request.run_env')"
                 @change="environmentChange" clearable>
        <el-option v-for="(environment, index) in environments" :key="index"
                   :label="getLabel(environment)"
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

      <!-- 环境 -->
      <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
    </span>
</template>

<script>
    import {parseEnvironment} from "../../model/EnvironmentModel";
    import ApiEnvironmentConfig from "../../../definition/components/environment/ApiEnvironmentConfig";

    export default {
      name: "EnvironmentSelect",
      components: {ApiEnvironmentConfig},
      data() {
        return {
          environments: []
        }
      },
      props: {
        projectId: String,
        currentData: {},
        type: String
      },
      created() {
        this.getEnvironments();
      },
      methods: {
        getEnvironments() {
          if (this.projectId) {
            this.$get('/api/environment/list/' + this.projectId, response => {
              this.environments = response.data;
              this.environments.forEach(environment => {
                parseEnvironment(environment);
              });
              let hasEnvironment = false;
              for (let i in this.environments) {
                if (this.environments[i].id === this.currentData.environmentId) {
                  this.currentData.environmentId = this.environments[i].id;
                  hasEnvironment = true;
                  break;
                }
              }
              if (!hasEnvironment) {
                this.currentData.environmentId = '';
                this.currentData.environment = undefined;
              }
            });
          } else {
            this.currentData.environmentId = '';
            this.currentData.environment = undefined;
          }
        },
        getLabel(environment) {
          if (environment) {
            if (this.type === 'TCP') {
              if (environment.config.tcpConfig && environment.config.tcpConfig.server) {
                return environment.name + ": " + environment.config.tcpConfig.server + ":" +
                  (environment.config.tcpConfig.port ? environment.config.tcpConfig.port : "");
              } else {
                return environment.name;
              }
            }
            return environment.name + (environment.config.httpConfig.socket ?
              (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '');
          }
          return "";
        },
        environmentConfigClose() {
          this.getEnvironments();
        },
        environmentChange(value) {
          for (let i in this.environments) {
            if (this.environments[i].id === value) {
              this.currentData.environmentId = value;
              if (this.currentData.request) {
                this.currentData.request.useEnvironment = value;
              }
              break;
            }
          }
        },
        openEnvironmentConfig() {
          if (!this.projectId) {
            this.$error(this.$t('api_test.select_project'));
            return;
          }
          this.$refs.environmentConfig.open(this.projectId);
        },
      }
    }
</script>

<style scoped>

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

</style>
