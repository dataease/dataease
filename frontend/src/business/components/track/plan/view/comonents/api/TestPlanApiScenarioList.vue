<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <test-plan-scenario-list-header
          :condition="condition"
          @refresh="search"
          @relevanceCase="$emit('relevanceCase', 'scenario')"/>
      </template>

      <el-table ref="scenarioTable" border :data="tableData" class="adjust-table" @select-all="handleSelectAll" @select="handleSelect">
        <el-table-column type="selection"/>
        <el-table-column width="40" :resizable="false" align="center">
          <template v-slot:default="{row}">
            <show-more-btn :is-show="isSelect(row)" :buttons="buttons" :size="selectRows.length"/>
          </template>
        </el-table-column>
        <el-table-column prop="num" label="ID"/>
        <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')"
                         show-overflow-tooltip/>
        <el-table-column prop="level" :label="$t('api_test.automation.case_level')"
                         show-overflow-tooltip>
          <template v-slot:default="scope">
            <ms-tag v-if="scope.row.level == 'P0'" type="info" effect="plain" content="P0"/>
            <ms-tag v-if="scope.row.level == 'P1'" type="warning" effect="plain" content="P1"/>
            <ms-tag v-if="scope.row.level == 'P2'" type="success" effect="plain" content="P2"/>
            <ms-tag v-if="scope.row.level == 'P3'" type="danger" effect="plain" content="P3"/>
          </template>

        </el-table-column>
        <el-table-column prop="tagNames" :label="$t('api_test.automation.tag')" width="200px">
          <template v-slot:default="scope">
            <div v-for="(itemName,index) in scope.row.tags" :key="index">
              <ms-tag type="success" effect="plain" :content="itemName"/>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('api_test.automation.update_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip/>
        <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')">
          <template v-slot:default="{row}">
            <el-link type="success" @click="showReport(row)" v-if="row.lastResult === 'Success'">{{ $t('api_test.automation.success') }}</el-link>
            <el-link type="danger" @click="showReport(row)" v-if="row.lastResult === 'Fail'">{{ $t('api_test.automation.fail') }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')"
                         show-overflow-tooltip/>
        <el-table-column :label="$t('commons.operating')" width="200px" v-if="!referenced">
          <template v-slot:default="{row}">
            <ms-table-operator-button class="run-button" :is-tester-permission="true" :tip="$t('api_test.run')" icon="el-icon-video-play"
                                      @exec="execute(row)" v-tester/>
            <ms-table-operator-button :is-tester-permission="true" :tip="$t('test_track.plan_view.cancel_relevance')"
                                      icon="el-icon-unlock" type="danger" @exec="remove(row)" v-tester/>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
      <div>
        <!-- 执行结果 -->
        <el-drawer :visible.sync="runVisible" :destroy-on-close="true" direction="ltr" :withHeader="true" :modal="false" size="90%">
          <ms-api-report-detail @refresh="search" :infoDb="infoDb" :report-id="reportId" :currentProjectId="projectId"/>
        </el-drawer>
      </div>
    </el-card>

  </div>
</template>

<script>
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
  import MsTag from "../../../../../common/components/MsTag";
  import {getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsApiReportDetail from "../../../../../api/automation/report/ApiReportDetail";
  import MsTableMoreBtn from "../../../../../api/automation/scenario/TableMoreBtn";
  import MsScenarioExtendButtons from "@/business/components/api/automation/scenario/ScenarioExtendBtns";
  import MsTestPlanList from "../../../../../api/automation/scenario/testplan/TestPlanList";
  import TestPlanScenarioListHeader from "./TestPlanScenarioListHeader";
  import {_handleSelect, _handleSelectAll} from "../../../../../../../common/js/tableUtils";
  import MsTableOperatorButton from "../../../../../common/components/MsTableOperatorButton";

  export default {
    name: "MsTestPlanApiScenarioList",
    components: {
      MsTableOperatorButton,
      TestPlanScenarioListHeader,
      MsTablePagination, MsTableMoreBtn, ShowMoreBtn, MsTableHeader, MsTag, MsApiReportDetail, MsScenarioExtendButtons, MsTestPlanList},
    props: {
      referenced: {
        type: Boolean,
        default: false,
      },
      selectNodeIds: Array,
      planId: String,
      clickType:String
    },
    data() {
      return {
        loading: false,
        condition: {},
        currentScenario: {},
        schedule: {},
        selectAll: false,
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        reportId: "",
        status:'default',
        infoDb: false,
        runVisible: false,
        projectId: "",
        runData: [],
        buttons: [
          {
            name: this.$t('test_track.case.batch_unlink'), handleClick: this.handleDeleteBatch
          },
          {
            name: this.$t('api_test.automation.batch_execute'), handleClick: this.handleBatchExecute
          }
        ],
        selectRows: new Set()
      }
    },
    created() {
      this.projectId = getCurrentProjectID();
      this.search();
    },
    watch: {
      selectNodeIds() {
        this.search();
      },
      planId() {
        this.search();
      }
    },
    methods: {
      search() {
        this.selectRows = new Set();
        this.loading = true;
        this.condition.moduleIds = this.selectNodeIds;
        this.condition.planId = this.planId;
        if(this.clickType){
          if(this.status =='default'){
            this.condition.status = this.clickType;
          }else{
            this.condition.status = null;
          }
          this.status = 'all';
        }
        let url = "/test/plan/scenario/case/list/" + this.currentPage + "/" + this.pageSize;
        this.$post(url, this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
          this.tableData.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
          this.loading = false;
        });
      },
      reductionApi(row) {
        row.scenarioDefinition = null;
        let rows = [row];
        this.$post("/api/automation/reduction", rows, response => {
          this.$success(this.$t('commons.save_success'));
          this.search();
        })
      },
      handleBatchExecute() {
        this.selectRows.forEach(row => {
          let param = this.buildExecuteParam(row);
          this.$post("/test/plan/scenario/case/run", param, response => {
          });
        });
        this.$message('任务执行中，请稍后刷新查看结果');
        this.search();
      },
      execute(row) {
        this.infoDb = false;
        let param = this.buildExecuteParam(row);
        this.$post("/test/plan/scenario/case/run", param, response => {
          this.runVisible = true;
          this.reportId = response.data;
        });
      },
      buildExecuteParam(row) {
        let param = {};
        // param.id = row.id;
        param.id = getUUID();
        param.planScenarioId = row.id;
        param.projectId = row.projectId;
        param.planCaseIds = [];
        param.planCaseIds.push(row.id);
        return param;
      },
      showReport(row) {
        this.runVisible = true;
        this.infoDb = true;
        this.reportId = row.reportId;
      },
      remove(row) {
        this.$get('/test/plan/scenario/case/delete/' + row.id, () => {
          this.$success(this.$t('test_track.cancel_relevance_success'));
          this.$emit('refresh');
          this.search();
        });
        return;
      },
      isSelect(row) {
        return this.selectRows.has(row);
      },
      handleSelectAll(selection) {
        _handleSelectAll(this, selection, this.tableData, this.selectRows);
      },
      handleSelect(selection, row) {
        _handleSelect(this, selection, row, this.selectRows);
      },
      handleDeleteBatch() {
        this.$alert(this.$t('api_test.definition.request.delete_confirm') + "？", '', {
          confirmButtonText: this.$t('commons.confirm'),
          callback: (action) => {
            if (action === 'confirm') {
              let param = {};
              param.ids = Array.from(this.selectRows).map(row => row.id);
              param.planId = this.planId;
              this.$post('/test/plan/scenario/case/batch/delete', param, () => {
                this.selectRows.clear();
                this.search();
                this.$success(this.$t('test_track.cancel_relevance_success'));
                this.$emit('refresh');
              });
            }
          }
        });
      }
    }
  }
</script>

<style scoped>
  /deep/ .el-drawer__header {
    margin-bottom: 0px;
  }
</style>
