<template>
  <span>
    <el-select :disabled="isReadOnly" v-model="environmentId" size="small" class="environment-select"
               :placeholder="$t('api_test.definition.request.run_env')" clearable>
      <el-option v-for="(environment, key) in environments" :key="key"
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
    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
  </span>
</template>

<script>
    import ApiEnvironmentConfig from "../../../test/components/ApiEnvironmentConfig";
    import {parseEnvironment} from "../../../test/model/EnvironmentModel";
    export default {
      name: "MsEnvironmentSelect",
      components: {ApiEnvironmentConfig},
      data() {
        return {
          environments: [],
          environment: undefined,
          isShow: true,
          environmentId: ""
        }
      },
      props:['projectId','isReadOnly'],
      created() {
        this.getEnvironments();
      },
      watch: {
        projectId() {
          this.getEnvironments();
        },
        environment() {
          this.$emit('setEnvironment', this.environment);
        },
        environmentId() {
          this.environmentChange(this.environmentId);
        },
        // planEnvironmentId() {
        //   this.environmentId = this.planEnvironmentId;
        // }
      },
      methods: {
        getEnvironments() {
          if (this.projectId) {
            this.$get('/api/environment/list/' + this.projectId, response => {
              this.environments = response.data;
              this.environments.forEach(environment => {
                parseEnvironment(environment);
                // if (this.planEnvironmentId && environment.id === this.planEnvironmentId) {
                //   this.planEnvironmentId = environment.id;
                // }
              });
            });
          } else {
            this.environmentId = undefined;
          }
        },
        openEnvironmentConfig() {
          if (!this.projectId) {
            this.$error(this.$t('api_test.select_project'));
            return;
          }
          this.$refs.environmentConfig.open(this.projectId);
        },
        environmentChange(value) {
          for (let i in this.environments) {
            if (this.environments[i].id === value) {
              this.environment = this.environments[i];
              break;
            }
          }
        },
        environmentConfigClose() {
          this.getEnvironments();
        },
      }
    }
</script>

<style scoped>


  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

  .ms-api-header-select {
    margin-left: 20px;
    min-width: 100px;
  }

</style>
