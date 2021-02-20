<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly">

    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input v-model="request.name" maxlength="300" show-word-limit/>
    </el-form-item>

    <div class="one-row">
      <el-form-item :label="$t('api_test.request.sql.dataSource')" prop="dataSource">
        <el-select v-model="request.dataSource">
          <el-option v-for="(item, index) in databaseConfigsOptions" :key="index" :value="item.id" :label="item.name"/>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('api_test.request.sql.timeout')" prop="queryTimeout">
        <el-input-number :disabled="isReadOnly" size="mini" v-model="request.queryTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </el-form-item>

      <el-form-item>
        <el-switch
          v-model="request.useEnvironment"
          :active-text="$t('api_test.request.refer_to_environment')" @change="getDatabaseConfigsOptions">
        </el-switch>
      </el-form-item>
    </div>

    <el-form-item :label="$t('api_test.request.sql.result_variable')" prop="resultVariable">
      <el-input v-model="request.resultVariable" maxlength="300" show-word-limit/>
    </el-form-item>

    <el-form-item :label="$t('api_test.request.sql.variable_names')" prop="variableNames">
      <el-input v-model="request.variableNames" maxlength="300" show-word-limit/>
    </el-form-item>

    <!--<el-form-item :label="'查询类型'" prop="protocol">-->
      <!--<el-select v-model="request.queryType">-->
        <!--<el-option label="dubbo://" :value="protocols.DUBBO"/>-->
      <!--</el-select>-->
    <!--</el-form-item>-->

    <el-button :disabled="!request.enable || !scenario.enable || isReadOnly" class="debug-button" size="small" type="primary" @click="runDebug">{{$t('api_test.request.debug')}}</el-button>

    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables">
        <ms-api-scenario-variables :is-read-only="isReadOnly" :items="request.variables"
                                   :description="$t('api_test.scenario.kv_description')"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.sql.sql_script')" name="sql">
        <div class="sql-content" >
          <ms-code-edit mode="sql" :read-only="isReadOnly" :modes="['sql']" :data.sync="request.query" theme="eclipse" ref="codeEdit"/>
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.assertions.label')" name="assertions">
        <ms-api-assertions :is-read-only="isReadOnly" :assertions="request.assertions"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.extract.label')" name="extract">
        <ms-api-extract :is-read-only="isReadOnly" :extract="request.extract"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.processor.pre_exec_script')" name="beanShellPreProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PreProcessor"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.processor.post_exec_script')" name="beanShellPostProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PostProcessor"/>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import MsApiAssertions from "../assertion/ApiAssertions";
  import {Scenario, SqlRequest} from "../../model/ScenarioModel";
  import MsApiExtract from "../extract/ApiExtract";
  import ApiRequestMethodSelect from "../collapse/ApiRequestMethodSelect";
  import MsDubboInterface from "@/business/components/api/test/components/request/dubbo/Interface";
  import MsDubboRegistryCenter from "@/business/components/api/test/components/request/dubbo/RegistryCenter";
  import MsDubboConfigCenter from "@/business/components/api/test/components/request/dubbo/ConfigCenter";
  import MsDubboConsumerService from "@/business/components/api/test/components/request/dubbo/ConsumerAndService";
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsApiScenarioVariables from "../ApiScenarioVariables";
  import MsJsr233Processor from "../processor/Jsr233Processor";

  export default {
    name: "MsApiSqlRequestForm",
    components: {
      MsJsr233Processor,
      MsApiScenarioVariables,
      MsCodeEdit,
      MsDubboConsumerService,
      MsDubboConfigCenter,
      MsDubboRegistryCenter,
      MsDubboInterface, ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue
    },
    props: {
      request: SqlRequest,
      scenario: Scenario,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        activeName: "variables",
        databaseConfigsOptions: [],
        rules: {
          name: [
            {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'},
          ],
          dataSource: [
            {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          ],
        }
      }
    },

    methods: {
      getDatabaseConfigsOptions() {
        this.databaseConfigsOptions = [];
        let names = new Set();
        let ids = new Set();
        this.scenario.databaseConfigs.forEach(config => {
          this.databaseConfigsOptions.push(config);
          names.add(config.name);
          ids.add(config.id);
        });
        if (this.request.useEnvironment && this.scenario.environment) {
          this.scenario.environment.config.databaseConfigs.forEach(config => {
            if (!names.has(config.name)) {
              this.databaseConfigsOptions.push(config);
              ids.add(config.id);
            }
          });
        }
        if (!ids.has(this.request.dataSource)) {
          this.request.dataSource = undefined;
        }
      },
      runDebug() {
        this.$emit('runDebug');
      }
    },

    created() {
      this.getDatabaseConfigsOptions();
    },
  }
</script>

<style scoped>

  .sql-content {
    height: calc(100vh - 570px);
  }

  .one-row .el-form-item {
    display: inline-block;
  }

  .one-row .el-form-item:nth-child(2) {
    margin-left: 60px;
  }

</style>
