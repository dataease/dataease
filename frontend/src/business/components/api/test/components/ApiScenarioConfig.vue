<template>
  <el-container>
    <el-aside class="scenario-aside">
      <div class="scenario-list">
        <ms-api-collapse v-model="activeName" @change="handleChange">
          <draggable :list="scenarios" group="Scenario" class="scenario-draggable" ghost-class="scenario-ghost">
            <ms-api-collapse-item v-for="(scenario, index) in scenarios" :key="index"
                                  :title="scenario.name" :name="index" :class="{'disable-scenario': !scenario.isEnable()}">
              <template slot="title">
                <div class="scenario-name">
                  <el-tag type="info" size="small" v-if="scenario.isReference()">{{
                      $t('api_test.scenario.reference')
                    }}
                  </el-tag>
                  {{ scenario.name }}
                  <span id="hint" v-if="!scenario.name">
                    {{ $t('api_test.scenario.config') }}
                  </span>
                </div>
                <el-dropdown trigger="click" @command="handleCommand">
                  <span class="el-dropdown-link el-icon-more scenario-btn"/>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item :disabled="isReadOnly" :command="{type: 'copy', index: index}">
                      {{ $t('api_test.scenario.copy') }}
                    </el-dropdown-item>
                    <el-dropdown-item :disabled="isReadOnly" :command="{type:'delete', index:index}">
                      {{ $t('api_test.scenario.delete') }}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="scenario.isEnable()" :disabled="isReadOnly"
                                      :command="{type:'disable', index:index}">
                      {{ $t('api_test.scenario.disable') }}
                    </el-dropdown-item>
                    <el-dropdown-item v-if="!scenario.isEnable()" :disabled="isReadOnly"
                                      :command="{type:'enable', index:index}">
                      {{ $t('api_test.scenario.enable') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
              <ms-api-request-config :is-read-only="disable" :scenario="scenario" @select="select"/>
            </ms-api-collapse-item>
          </draggable>
        </ms-api-collapse>
      </div>
      <el-popover placement="top" v-model="visible">
        <el-radio-group v-model="type" @change="createScenario">
          <el-radio :label="types.CREATE">{{ $t('api_test.scenario.create_scenario') }}</el-radio>
          <el-radio :label="types.SELECT">{{ $t('api_test.scenario.select_scenario') }}</el-radio>
        </el-radio-group>
        <el-button slot="reference" :disabled="isReadOnly" class="scenario-create" type="primary" size="mini"
                   icon="el-icon-plus" plain/>
      </el-popover>
      <ms-horizontal-drag-bar/>
    </el-aside>

    <el-main class="scenario-main">
      <div class="scenario-form">
        <ms-api-scenario-form :is-read-only="disable" :scenario="selected" :project-id="projectId"
                              v-if="isScenario"/>
        <ms-api-request-form :debug-report-id="debugReportId" @runDebug="runDebug"
                             :is-read-only="disable"
                             :request="selected" :scenario="currentScenario" v-if="isRequest"/>
      </div>
    </el-main>
    <ms-api-scenario-select :exclude-id="test.id" :project-id="test.projectId" @select="selectScenario" ref="selectDialog"/>
  </el-container>
</template>

<script>

import MsApiCollapseItem from "./collapse/ApiCollapseItem";
import MsApiCollapse from "./collapse/ApiCollapse";
import MsApiRequestConfig from "./request/ApiRequestConfig";
import MsApiRequestForm from "./request/ApiRequestForm";
import MsApiScenarioForm from "./ApiScenarioForm";
import {Request, Scenario} from "../model/ScenarioModel";
import draggable from 'vuedraggable';
import MsApiScenarioSelect from "@/business/components/api/test/components/ApiScenarioSelect";
import {parseEnvironment} from "../model/EnvironmentModel";
import MsHorizontalDragBar from "../../../common/components/dragbar/MsLeft2RightDragBar";

export default {
  name: "MsApiScenarioConfig",

  components: {
    MsHorizontalDragBar,
    MsApiScenarioSelect,
    MsApiRequestConfig,
    MsApiScenarioForm,
    MsApiRequestForm,
    MsApiCollapse,
    MsApiCollapseItem,
    draggable
  },

  props: {
    test: Object,
    scenarios: Array,
    projectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    debugReportId: String,
  },

  data() {
    return {
      visible: false,
      types: {CREATE: "create", SELECT: "select"},
      type: "",
      activeName: 0,
      selected: [Scenario, Request],
      currentScenario: {}
    }
  },
  watch: {
    test() {
      this.initScenarioEnvironment();
    },
    projectId() {
      this.initScenarioEnvironment();
    }
  },
  methods: {
    notContainsScenario(item) {
      for (let scenario of this.scenarios) {
        if (item.id === scenario.id) {
          return false;
        }
      }
      return true;
    },
    selectScenario(selection) {
      selection.filter(this.notContainsScenario).forEach(item => {
        this.scenarios.push(item);
      })
    },
    createScenario() {
      if (this.type === this.types.CREATE) {
        this.scenarios.push(new Scenario());
      } else {
        this.$refs.selectDialog.open();
      }
      this.visible = false;
      this.type = "";
    },
    copyScenario(index) {
      let scenario = this.scenarios[index];
      this.scenarios.push(scenario.clone());
    },
    deleteScenario(index) {
      this.scenarios.splice(index, 1);
      if (this.scenarios.length === 0) {
        this.type = this.types.CREATE;
        this.createScenario();
        this.select(this.scenarios[0]);
      }
    },
    disableScenario(index) {
      let scenario = this.scenarios[index];
      if (scenario.isReference()) {
        scenario.referenceEnable = false;
      } else {
        scenario.enable = false;
      }
    },
    enableScenario(index) {
      let scenario = this.scenarios[index];
      if (scenario.isReference()) {
        scenario.referenceEnable = true;
      } else {
        scenario.enable = true;
      }
    },
    handleChange(index) {
      this.select(this.scenarios[index]);
    },
    handleCommand(command) {
      switch (command.type) {
        case "copy":
          this.copyScenario(command.index);
          break;
        case "delete":
          this.deleteScenario(command.index);
          break;
        case "disable":
          this.disableScenario(command.index);
          break;
        case "enable":
          this.enableScenario(command.index);
          break;
      }
    },
    select(obj, scenario) {
      this.selected = null;
      this.$nextTick(function () {
        if (obj instanceof Scenario) {
          this.currentScenario = obj;
        } else {
          this.currentScenario = scenario;
        }
        this.selected = obj;
      });
    },
    reset() {
      this.$nextTick(function () {
        this.activeName = 0;
        this.select(this.scenarios[0]);
      });
    },
    initScenarioEnvironment() {
      if (this.projectId) {
        this.result = this.$get('/api/environment/list/' + this.projectId, response => {
          let environments = response.data;
          let environmentMap = new Map();
          environments.forEach(environment => {
            parseEnvironment(environment);
            environmentMap.set(environment.id, environment);
          });
          this.scenarios.forEach(scenario => {
            if (scenario.environmentId) {
              let env = environmentMap.get(scenario.environmentId);
              if (!env) {
                scenario.environmentId = undefined;
                scenario.environment = undefined;
              } else {
                scenario.environment = env;
              }
            } else {
              scenario.environmentId = undefined;
              scenario.environment = undefined;
            }
          });
        });
      }
    },
    runDebug(request) {
      let scenario = new Scenario();
      Object.assign(scenario, this.currentScenario);
      scenario.requests = [];
      scenario.requests.push(request);
      this.$emit('runDebug', scenario);
    }
  },

  computed: {
    isScenario() {
      return this.selected instanceof Scenario;
    },
    isRequest() {
      return this.selected instanceof Request;
    },
    isReference() {
      if (this.selected instanceof Scenario) {
        return this.selected.isReference();
      }
      if (this.selected instanceof Request) {
        return this.currentScenario.isReference();
      }
      return false;
    },
    disable() {
      return this.isReadOnly || this.isReference;
    },
  },

  created() {
    this.select(this.scenarios[0]);
  },
}
</script>

<style scoped>
.scenario-aside {
  position: relative;
  border-radius: 4px;
  border: 1px solid #EBEEF5;
  box-sizing: border-box;
}

.scenario-list {
  overflow-y: auto;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 28px;
}

.scenario-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  width: 100%;
}

/*.scenario-name > #hint {*/
/*color: #8a8b8d;*/
/*}*/

.scenario-btn {
  text-align: center;
  padding: 13px;
}

.scenario-create {
  position: absolute;
  bottom: 0;
  width: 100%;
}

.scenario-main {
  position: relative;
  margin-left: 20px;
  border: 1px solid #EBEEF5;
}

.scenario-form {
  padding: 20px;
  overflow-y: auto;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.scenario-ghost {
  opacity: 0.5;
}

.scenario-draggable {
  background-color: #909399;
}

.disable-scenario >>> .el-collapse-item__header {
  border-right: 2px solid #909399;
  color: #8a8b8d;
}

</style>
