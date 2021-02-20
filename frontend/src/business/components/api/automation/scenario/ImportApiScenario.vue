<template>
  <ms-container>
    <ms-aside-container>
      <ms-api-scenario-module @selectModule="selectModule" @getApiModuleTree="initTree"
                              @refresh="refresh" @saveAsEdit="editScenario"/>
    </ms-aside-container>
    <ms-main-container>
      <ms-api-scenario-list
        :current-module="currentModule"
        @edit="editScenario"
        @selection="setData"
        :referenced="true"
        ref="apiScenarioList"/>

      <el-button style="float: right;margin: 10px" @click="importApiScenario" type="primary">{{ $t('api_test.scenario.reference') }}</el-button>
      <el-button style="float: right;margin: 10px" @click="copyApiScenario">{{ $t('commons.copy') }}</el-button>
    </ms-main-container>
  </ms-container>
</template>

<script>

  import MsContainer from "@/business/components/common/components/MsContainer";
  import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
  import MsMainContainer from "@/business/components/common/components/MsMainContainer";
  import MsApiScenarioList from "@/business/components/api/automation/scenario/ApiScenarioList";
  import {getUUID} from "@/common/js/utils";
  import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
  import MsEditApiScenario from "../scenario/EditApiScenario";

  export default {
    name: "ImportApiScenario",
    components: {MsApiScenarioModule, MsApiScenarioList, MsMainContainer, MsAsideContainer, MsContainer, MsEditApiScenario},
    comments: {},
    data() {
      return {
        isHide: true,
        activeName: 'default',
        currentModule: null,
        currentScenario: [],
        currentScenarioIds: [],
        moduleOptions: {},
        scenarioDefinition: Object,
      }
    },
    watch: {},
    methods: {
      setData(data) {
        this.currentScenario = Array.from(data).map(row => row);
        this.currentScenarioIds = Array.from(data).map(row => row.id);
      },
      selectModule(data) {
        this.currentModule = data;
      },
      importApiScenario() {
        let scenarios = [];
        if (this.currentScenario) {
          this.currentScenario.forEach(item => {
            let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'REF', resourceId: getUUID()};
            scenarios.push(obj);
          })
        }
        this.$emit('addScenario', scenarios);
      },
      getApiScenario() {
        let scenarios = [];
        this.result = this.$post("/api/automation/getApiScenarios/", this.currentScenarioIds, response => {
          if (response.data) {
            response.data.forEach(item => {
              let scenarioDefinition = JSON.parse(item.scenarioDefinition);
              let obj = {id: item.id, name: item.name, type: "scenario", referenced: 'Copy', resourceId: getUUID(), hashTree: scenarioDefinition && scenarioDefinition.hashTree ? scenarioDefinition.hashTree : []};
              scenarios.push(obj);
            })
            this.$emit('addScenario', scenarios);
          }
        })
      },
      copyApiScenario() {
        if (this.currentScenarioIds) {
          this.getApiScenario();
        }
      },
      initTree(data) {
        this.moduleOptions = data;
      },
      refresh(data) {
        this.$refs.apiScenarioList.search(data);
      },
      editScenario(row) {
        this.currentScenario = row;
        this.addTab({name: 'add'});
      },

    }
  }
</script>

<style scoped>

</style>
