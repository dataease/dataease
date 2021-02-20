<template>
  <div>
    <el-row>
      <el-col :span="21" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;margin: 20px">

          <el-form :model="request" ref="request" label-width="100px" :disabled="isReadOnly" style="margin: 10px">

            <el-form-item :label="$t('api_test.request.dubbo.protocol')" prop="protocol">
              <el-select v-model="request.protocol" size="small">
                <el-option label="dubbo://" :value="protocols.DUBBO"/>
              </el-select>
            </el-form-item>

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

              <el-tab-pane label="Args" name="args">
                <ms-api-key-value :is-read-only="isReadOnly" :items="request.args"
                                  key-placeholder="Param Type" value-placeholder="Param Value"/>
              </el-tab-pane>
              <el-tab-pane label="Attachment Args" name="attachment">
                <ms-api-key-value :is-read-only="isReadOnly" :items="request.attachmentArgs"/>
              </el-tab-pane>

            </el-tabs>

          </el-form>
        </div>
        <!--<div v-if="showScript">-->
          <!--<div v-for="row in request.hashTree" :key="row.id" v-loading="isReloadData" style="margin-left: 20px;width: 100%">-->
            <!--&lt;!&ndash; 前置脚本 &ndash;&gt;-->
            <!--<ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" @copyRow="copyRow" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.pre_script')" style-type="color: #B8741A;background-color: #F9F1EA"-->
                                 <!--:jsr223-processor="row"/>-->
            <!--&lt;!&ndash;后置脚本&ndash;&gt;-->
            <!--<ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @copyRow="copyRow" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')" style-type="color: #783887;background-color: #F2ECF3"-->
                                 <!--:jsr223-processor="row"/>-->
            <!--&lt;!&ndash;断言规则&ndash;&gt;-->
            <!--<div style="margin-top: 10px">-->
              <!--<ms-api-assertions v-if="row.type==='Assertions'" @copyRow="copyRow" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>-->
            <!--</div>-->
            <!--&lt;!&ndash;提取规则&ndash;&gt;-->
            <!--<div style="margin-top: 10px">-->
              <!--<ms-api-extract :is-read-only="isReadOnly" @copyRow="copyRow" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>-->
            <!--</div>-->
          <!--</div>-->
        <!--</div>-->
      </el-col>

      <el-col :span="3" class="ms-left-cell" v-if="showScript">
        <el-button class="ms-left-buttion" size="small" style="color: #B8741A;background-color: #F9F1EA" @click="addPre">+{{$t('api_test.definition.request.pre_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" style="color: #783887;background-color: #F2ECF3" @click="addPost">+{{$t('api_test.definition.request.post_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" style="color: #A30014;background-color: #F7E6E9" @click="addAssertions">+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" style="color: #015478;background-color: #E6EEF2" @click="addExtract">+{{$t('api_test.definition.request.extract_param')}}</el-button>
      </el-col>
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
  import {Assertions, Extract, DubboRequest} from "../../../model/ApiTestModel";
  import MsDubboInterface from "../../request/dubbo/Interface";
  import MsDubboRegistryCenter from "../../request/dubbo/RegistryCenter";
  import MsDubboConfigCenter from "../../request/dubbo/ConfigCenter";
  import MsDubboConsumerService from "../../request/dubbo/ConsumerAndService";
  import {getUUID} from "@/common/js/utils";
  import MsJsr233Processor from "../../../../automation/scenario/component/Jsr233Processor";

  export default {
    name: "MsDatabaseConfig",
    components: {
      MsJsr233Processor,
      MsApiScenarioVariables,
      MsCodeEdit,
      ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue, MsDubboConsumerService,
      MsDubboConfigCenter,
      MsDubboRegistryCenter,
      MsDubboInterface,
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
      }
    },
    data() {
      return {
        activeName: "interface",
        activeName2: "args",
        protocols: DubboRequest.PROTOCOLS,
        isReloadData: false,
      }
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
      validateApi() {
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

      },
    }
  }
</script>

<style scoped>
  .one-row .el-form-item {
    display: inline-block;
  }

  .one-row .el-form-item:nth-child(2) {
    margin-left: 60px;
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
</style>
