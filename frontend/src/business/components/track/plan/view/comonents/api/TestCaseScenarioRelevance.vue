<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :relevance-project-id="projectId"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <relevance-scenario-list
      :select-node-ids="selectNodeIds"
      :trash-enable="trashEnable"
      :plan-id="planId"
      :project-id="projectId"
      ref="apiScenarioList"/>

  </test-case-relevance-base>

</template>

<script>

  import TestCaseRelevanceBase from "../base/TestCaseRelevanceBase";
  import MsApiModule from "../../../../../api/definition/components/module/ApiModule";
  import {getCurrentProjectID} from "../../../../../../../common/js/utils";
  import ApiList from "../../../../../api/definition/components/list/ApiList";
  import ApiCaseSimpleList from "../../../../../api/definition/components/list/ApiCaseSimpleList";
  import MsApiScenarioList from "../../../../../api/automation/scenario/ApiScenarioList";
  import MsApiScenarioModule from "../../../../../api/automation/scenario/ApiScenarioModule";
  import RelevanceScenarioList from "./RelevanceScenarioList";

  export default {
    name: "TestCaseScenarioRelevance",
    components: {
      RelevanceScenarioList,
      MsApiScenarioModule,
      MsApiScenarioList,
      ApiCaseSimpleList,
      ApiList,
      MsApiModule,
      TestCaseRelevanceBase,
    },
    data() {
      return {
        showCasePage: true,
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        moduleOptions: {},
        trashEnable: false,
        condition: {},
        currentRow: {},
        projectId: ""
      };
    },
    props: {
      planId: {
        type: String
      },
    },
    watch: {
      planId() {
        this.condition.planId = this.planId;
      },
    },
    methods: {
      open() {
        this.$refs.baseRelevance.open();
        if (this.$refs.apiScenarioList) {
          this.$refs.apiScenarioList.search();
        }
      },
      setProject(projectId) {
        this.projectId = projectId;
      },

      refresh(data) {
          this.$refs.apiScenarioList.search(data);
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

      saveCaseRelevance() {
        let param = {};
        let url = '';
        let selectIds = [];
        url = '/api/automation/relevance';
        selectIds = Array.from(this.$refs.apiScenarioList.selectRows).map(row => row.id);
        param.planId = this.planId;
        param.selectIds = selectIds;

        this.result = this.$post(url, param, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refresh');
          this.refresh();
          this.$refs.baseRelevance.close();
        });
      },
    }
  }
</script>

<style scoped>

  /deep/ .select-menu {
    margin-bottom: 15px;
  }

  /deep/ .environment-select {
    float: right;
    margin-right: 10px;
  }

  /deep/ .module-input {
    width: 243px;
  }

</style>
