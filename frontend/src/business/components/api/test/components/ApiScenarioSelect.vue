<template>
  <el-dialog :title="$t('api_test.scenario.select_scenario')" :visible.sync="visible" width="70%">
    <el-table stripe :data="tableData" size="small" @expand-change="expand">
      <el-table-column type="expand" width="50">
        <template v-slot:default="{row}">
          <ms-api-scenario-select-sub-table :row="row" v-model="row.selected"/>
        </template>
      </el-table-column>
      <el-table-column prop="name" :label="$t('api_test.scenario.test_name')" width="400" show-overflow-tooltip/>
      <el-table-column prop="sr" :label="$t('api_test.scenario.scenario_request')" width="150" show-overflow-tooltip/>
      <el-table-column prop="userName" :label="$t('api_test.creator')" width="150" show-overflow-tooltip/>
      <el-table-column prop="enable" :label="$t('api_test.scenario.enable_disable')" width="150"/>
      <el-table-column>
        <template v-slot:header>
          <div class="search-header">
            <ms-table-search-bar :condition.sync="condition" @change="search" class="search-bar"
                                 :tip="$t('commons.search_by_name')"/>
            <ms-table-adv-search-bar :condition.sync="condition" @search="search"/>
          </div>
        </template>
        <template v-slot:default="{row}">
          {{ row.reference }}
          <el-button type="text" size="small" @click="reference(row)">
            {{ $t('api_test.scenario.reference') }}
          </el-button>
          <el-button type="text" size="small" @click="clone(row)">{{ $t('api_test.scenario.clone') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>

    <div class="dialog-footer">
      <el-button @click="close">{{ $t('commons.cancel') }}</el-button>
    </div>
  </el-dialog>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import {TEST_CONFIGS} from "@/business/components/common/components/search/search-components";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import MsApiScenarioSelectSubTable from "@/business/components/api/test/components/ApiScenarioSelectSubTable";
import {Scenario} from "@/business/components/api/test/model/ScenarioModel";
import {parseEnvironment} from "../model/EnvironmentModel";

export default {
  name: "MsApiScenarioSelect",
  components: {
    MsApiScenarioSelectSubTable,
    MsTableAdvSearchBar, MsTableSearchBar, MsTablePagination, MsTableHeader
  },
  props: {
    excludeId: String,
    projectId: String
  },
  data() {
    return {
      visible: false,
      condition: {
        components: TEST_CONFIGS
      },
      tableData: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      selection: false,
      environmentMap: new Map()
    }
  },
  methods: {
    search() {
      this.condition.excludeId = this.excludeId;
      this.condition.projectId = this.projectId;
      let url = "/api/list/" + this.currentPage + "/" + this.pageSize;
      this.result = this.$post(url, this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
        this.tableData.forEach(row => {
          row.selected = [];
        })
      });
    },
    reference(row) {
      let scenarios = [];
      for (let options of row.selected) {
        if (!options.id) {
          this.$warning(this.$t('api_test.scenario.cant_reference'));
          return;
        }
        let scenario = new Scenario(options);
        if (!scenario.isReference()) {
          scenario.id = row.id + "#" + options.id;
        } else {
          scenario.id = options.id;
        }
        scenarios.push(scenario);
      }
      this.initScenarioEnvironment(scenarios);
      this.$emit('select', scenarios);
    },
    getEnvironment() {
      if (this.projectId) {
        this.result = this.$get('/api/environment/list/' + this.projectId, response => {
          let environments = response.data;
          this.environmentMap = new Map();
          environments.forEach(environment => {
            parseEnvironment(environment);
            this.environmentMap.set(environment.id, environment);
          });
        });
      }
    },
    initScenarioEnvironment(scenarios) {
      scenarios.forEach(scenario => {
        if (scenario.environmentId) {
          let env = this.environmentMap.get(scenario.environmentId);
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
    },
    clone(row) {
      let scenarios = [];
      row.selected.forEach(options => {
        // 去掉ID，创建新的ID
        options.id = undefined;
        scenarios.push(new Scenario(options));
      });
      this.initScenarioEnvironment(scenarios);
      this.$emit('select', scenarios);
    },
    open() {
      this.search();
      this.getEnvironment();
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    expand(row) {
      row.selected = [];
    }
  }
}
</script>

<style scoped>
.search-header {
  text-align: right;
}

.search-bar {
  width: 200px
}
</style>
