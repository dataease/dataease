<template>
  <relevance-dialog :title="$t('api_test.automation.scenario_import')" ref="relevanceDialog">

    <template v-slot:aside>
        <ms-api-scenario-module
          @nodeSelectEvent="nodeChange"
          @refreshTable="refresh"
          @setModuleOptions="setModuleOptions"
          @enableTrash="false"
          :is-read-only="true"
          ref="nodeTree"/>
    </template>

    <ms-api-scenario-list
      :select-node-ids="selectNodeIds"
      :referenced="true"
      :trash-enable="false"
      @selection="setData"
      ref="apiScenarioList"/>

    <template v-slot:footer>
      <el-button type="primary" @click="copy" @keydown.enter.native.prevent>{{$t('commons.copy')}}</el-button>
      <el-button type="primary" @click="reference" @keydown.enter.native.prevent> {{ $t('api_test.scenario.reference') }}</el-button>
    </template>

  </relevance-dialog>
</template>

<script>
    import ScenarioRelevanceCaseList from "./RelevanceCaseList";
    import MsApiModule from "../../../definition/components/module/ApiModule";
    import MsContainer from "../../../../common/components/MsContainer";
    import MsAsideContainer from "../../../../common/components/MsAsideContainer";
    import MsMainContainer from "../../../../common/components/MsMainContainer";
    import ScenarioRelevanceApiList from "./RelevanceApiList";
    import MsApiScenarioModule from "../ApiScenarioModule";
    import MsApiScenarioList from "../ApiScenarioList";
    import {getUUID} from "../../../../../../common/js/utils";
    import RelevanceDialog from "../../../../track/plan/view/comonents/base/RelevanceDialog";
    export default {
      name: "ScenarioRelevance",
      components: {
        RelevanceDialog,
        MsApiScenarioList,
        MsApiScenarioModule,
        MsMainContainer, MsAsideContainer, MsContainer},
      data() {
          return {
            result: {},
            currentProtocol: null,
            selectNodeIds: [],
            moduleOptions: {},
            isApiListEnable: true,
            currentScenario: [],
            currentScenarioIds: [],
          }
      },
      methods: {
        reference() {
          let scenarios = [];
          if (!this.currentScenario || this.currentScenario.length < 1) {
            this.$emit('请选择场景');
            return;
          }
          this.currentScenario.forEach(item => {
            let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'REF', resourceId: getUUID()};
            scenarios.push(obj);
          });
          this.$emit('save', scenarios);
          this.close();
        },
        copy() {
          let scenarios = [];
          if (!this.currentScenarioIds || this.currentScenarioIds.length < 1) {
            this.$emit('请选择场景');
            return;
          }
          this.result = this.$post("/api/automation/getApiScenarios/", this.currentScenarioIds, response => {
            if (response.data) {
              response.data.forEach(item => {
                let scenarioDefinition = JSON.parse(item.scenarioDefinition);
                let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'Copy', resourceId: getUUID(), hashTree: scenarioDefinition.hashTree};
                scenarios.push(obj);
              });
              this.$emit('save', scenarios);
              this.close();
            }
          })
        },
        close() {
          this.refresh();
          this.$refs.relevanceDialog.close();
        },
        open() {
          this.$refs.relevanceDialog.open();
          if (this.$refs.apiScenarioList) {
            this.$refs.apiScenarioList.search();
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
        refresh() {
            this.$refs.apiScenarioList.search();
        },
        setData(data) {
          this.currentScenario = Array.from(data).map(row => row);
          this.currentScenarioIds = Array.from(data).map(row => row.id);
        },
      }
    }
</script>

<style scoped>

</style>
