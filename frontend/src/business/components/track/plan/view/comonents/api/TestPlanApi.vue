<template>

  <ms-test-plan-common-component>
    <template v-slot:aside>
      <ms-api-module
        v-if="model === 'api'"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refreshTable"
        @setModuleOptions="setModuleOptions"
        :plan-id="planId"
        :is-read-only="true"
        :redirectCharType="redirectCharType"
        ref="apiNodeTree">
        <template v-slot:header>
          <div class="model-change-radio">
            <el-radio v-model="model" label="api">接口用例</el-radio>
            <el-radio v-model="model" label="scenario">场景用例</el-radio>
          </div>
        </template>
      </ms-api-module>

      <ms-api-scenario-module
        v-if="model === 'scenario'"
        @nodeSelectEvent="nodeChange"
        @refreshTable="refreshTable"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        :plan-id="planId"
        ref="scenarioNodeTree">
        <template v-slot:header>
          <div class="model-change-radio">
          <el-radio v-model="model" label="api">接口用例</el-radio>
          <el-radio v-model="model" label="scenario">场景用例</el-radio>
          </div>
        </template>
      </ms-api-scenario-module>
    </template>


    <template v-slot:main>
      <!--测试用例列表-->
      <test-plan-api-case-list
        v-if="model === 'api'"
        :current-protocol="currentProtocol"
        :currentRow="currentRow"
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :is-case-relevance="true"
        :model="'plan'"
        :plan-id="planId"
        :clickType="clickType"
        @refresh="refreshTree"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="apiCaseList"/>

      <ms-test-plan-api-scenario-list
        v-if="model === 'scenario'"
        :select-node-ids="selectNodeIds"
        :trash-enable="trashEnable"
        :plan-id="planId"
        :clickType="clickType"
        @refresh="refreshTree"
        @relevanceCase="openTestCaseRelevanceDialog"
        ref="apiScenarioList"/>

    </template>

    <test-case-api-relevance
      @refresh="refresh"
      :plan-id="planId"
      :model="model"
      ref="apiCaseRelevance"/>

    <test-case-scenario-relevance
      @refresh="refresh"
      :plan-id="planId"
      :model="model"
      ref="scenarioCaseRelevance"/>

  </ms-test-plan-common-component>

</template>

<script>
    import NodeTree from "../../../../common/NodeTree";
    import TestCaseRelevance from "../functional/TestCaseFunctionalRelevance";
    import MsTestPlanCommonComponent from "../base/TestPlanCommonComponent";
    import TestPlanApiCaseList from "./TestPlanApiCaseList";
    import TestCaseApiRelevance from "./TestCaseApiRelevance";
    import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
    import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
    import MsApiScenarioModule from "../../../../../api/automation/scenario/ApiScenarioModule";
    import MsTestPlanApiScenarioList from "./TestPlanApiScenarioList";
    import TestCaseScenarioRelevance from "./TestCaseScenarioRelevance";

    export default {
      name: "TestPlanApi",
      components: {
        TestCaseScenarioRelevance,
        MsTestPlanApiScenarioList,
        MsApiScenarioModule,
        MsApiModule,
        ApiCaseSimpleList,
        TestCaseApiRelevance,
        TestPlanApiCaseList,
        MsTestPlanCommonComponent,
        TestCaseRelevance,
        NodeTree,
      },
      data() {
        return {
          result: {},
          treeNodes: [],
          currentRow: "",
          trashEnable: false,
          currentProtocol: null,
          currentModule: null,
          selectNodeIds: [],
          moduleOptions: {},
          model: 'api'
        }
      },
      props: [
        'planId',
        'redirectCharType',
        'clickType'
      ],
      mounted() {
        this.checkRedirectCharType();
      },
      watch: {
        model() {
          this.selectNodeIds = [];
          this.moduleOptions = {};
        },
        redirectCharType(){
          if(this.redirectCharType=='scenario'){
            this.model = 'scenario';
          }else{
            this.model = 'api';
          }
        }
      },
      methods: {
        checkRedirectCharType(){
          if(this.redirectCharType=='scenario'){
            this.model = 'scenario';
          }else{
            this.model = 'api';
          }
        },
        refresh() {
          this.refreshTree();
          this.refreshTable();
        },
        refreshTable() {
          if (this.$refs.apiCaseList) {
            this.$refs.apiCaseList.initTable();
          }
          if (this.$refs.apiScenarioList) {
            this.$refs.apiScenarioList.search();
          }
        },
        refreshTree() {
          if (this.$refs.apiNodeTree) {
            this.$refs.apiNodeTree.list();
          }
          if (this.$refs.scenarioNodeTree) {
            this.$refs.scenarioNodeTree.list();
          }
        },

        nodeChange(node, nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
        },
        handleProtocolChange(protocol) {
          this.currentProtocol = protocol;
        },
        setModuleOptions(data) {
          this.moduleOptions = data;
        },

        openTestCaseRelevanceDialog(model) {
          if (model === 'scenario') {
            this.$refs.scenarioCaseRelevance.open();
          } else {
            this.$refs.apiCaseRelevance.open();
          }
        },
      }
    }

</script>

<style scoped>

  .model-change-radio {
    height: 25px;
    line-height: 25px;
    margin: 5px 10px;
  }

  /deep/ .run-button {
    background-color: #409EFF;
    border-color: #409EFF;
  }

</style>
