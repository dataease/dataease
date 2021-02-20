<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly">

    <el-form-item :label="$t('api_test.request.name')" prop="name">
      <el-input v-model="request.name" maxlength="300" show-word-limit/>
    </el-form-item>

    <el-form-item :label="$t('api_test.request.dubbo.protocol')" prop="protocol">
      <el-select v-model="request.protocol">
        <el-option label="dubbo://" :value="protocols.DUBBO"/>
      </el-select>
    </el-form-item>

    <el-button :disabled="!request.enable || !scenario.enable || isReadOnly" class="debug-button" size="small" type="primary" @click="runDebug">{{$t('api_test.request.debug')}}</el-button>

    <el-tabs v-model="activeName">
      <el-tab-pane label="Interface" name="interface">
        <ms-dubbo-interface :request="request" :is-read-only="isReadOnly"/>
      </el-tab-pane>
      <el-tab-pane label="Config Center" name="config">
        <ms-dubbo-config-center :config="request.configCenter" :is-read-only="isReadOnly"
                                :description="$t('api_test.request.dubbo.form_description')"/>
      </el-tab-pane>
      <el-tab-pane label="Registry Center" name="registry">
        <ms-dubbo-registry-center :registry="request.registryCenter" :is-read-only="isReadOnly"
                                  :description="$t('api_test.request.dubbo.form_description')"/>
      </el-tab-pane>
      <el-tab-pane label="Consumer & Service" name="consumer">
        <ms-dubbo-consumer-service :consumer="request.consumerAndService" :is-read-only="isReadOnly"
                                   :description="$t('api_test.request.dubbo.form_description')"/>
      </el-tab-pane>
    </el-tabs>
    <el-tabs v-model="activeName2">
      <el-tab-pane label="Args" name="args">
        <ms-api-key-value :is-read-only="isReadOnly" :items="request.args"
                          key-placeholder="Param Type" value-placeholder="Param Value"/>
      </el-tab-pane>
      <el-tab-pane label="Attachment Args" name="attachment">
        <ms-api-key-value :is-read-only="isReadOnly" :items="request.attachmentArgs"/>
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
  import MsApiBody from "../body/ApiBody";
  import MsApiAssertions from "../assertion/ApiAssertions";
  import {DubboRequest, Scenario} from "../../model/ScenarioModel";
  import MsApiExtract from "../extract/ApiExtract";
  import ApiRequestMethodSelect from "../collapse/ApiRequestMethodSelect";
  import MsDubboInterface from "@/business/components/api/test/components/request/dubbo/Interface";
  import MsDubboRegistryCenter from "@/business/components/api/test/components/request/dubbo/RegistryCenter";
  import MsDubboConfigCenter from "@/business/components/api/test/components/request/dubbo/ConfigCenter";
  import MsDubboConsumerService from "@/business/components/api/test/components/request/dubbo/ConsumerAndService";
  import MsJsr233Processor from "../processor/Jsr233Processor";

  export default {
    name: "MsApiDubboRequestForm",
    components: {
      MsJsr233Processor,
      MsDubboConsumerService,
      MsDubboConfigCenter,
      MsDubboRegistryCenter,
      MsDubboInterface, ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiBody, MsApiKeyValue
    },
    props: {
      request: DubboRequest,
      scenario: Scenario,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      return {
        activeName: "interface",
        activeName2: "args",
        protocols: DubboRequest.PROTOCOLS,
        rules: {
          name: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
        }
      }
    },

    methods: {
      useEnvironmentChange(value) {
        if (value && !this.request.environment) {
          this.$error(this.$t('api_test.request.please_add_environment_to_scenario'), 2000);
          this.request.useEnvironment = false;
        }
        this.$refs["request"].clearValidate();
      },
      runDebug() {
        this.$emit('runDebug');
      }
    },

    computed: {}
  }
</script>

<style scoped>
  .request-method-select {
    width: 110px;
  }

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



</style>
