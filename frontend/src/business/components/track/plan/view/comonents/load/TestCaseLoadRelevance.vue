<template>
  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :plan-id="planId"
    ref="baseRelevance">

    <template v-slot:aside>
      <node-tree class="node-tree"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </template>


    <el-table
      v-loading="result.loading"
      :data="testCases"
      row-key="id"
      @select-all="handleSelectAll"
      @select="handleSelectionChange"
      height="50vh"
      ref="table">
      <el-table-column
        type="selection"/>
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-performance-test-status :row="row"/>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="createTime"
        :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        sortable
        prop="updateTime"
        :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="getTestCases" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
  </test-case-relevance-base>
</template>

<script>

import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
import NodeTree from "@/business/components/track/common/NodeTree";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/components/track/common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "@/business/components/common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "@/business/components/common/components/search/MsTableAdvSearchBar";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";

export default {
  name: "TestCaseLoadRelevance",
  components: {
    TestCaseRelevanceBase,
    NodeTree,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
    MsPerformanceTestStatus,
    MsTablePagination
  },
  data() {
    return {
      result: {},
      testCases: [],
      selectIds: new Set(),
      treeNodes: [],
      selectNodeIds: [],
      selectNodeNames: [],
      projectId: '',
      projectName: '',
      projects: [],
      pageSize: 10,
      currentPage: 1,
      total: 0,
      condition: {},
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ]
    };
  },
  props: {
    planId: {
      type: String
    }
  },
  watch: {
    planId() {
      this.condition.planId = this.planId;
    },
    selectNodeIds() {
      this.search();
    },
    projectId() {
      this.condition.projectId = this.projectId;
      this.getProjectNode();
      this.search();
    }
  },
  methods: {
    open() {
      this.$refs.baseRelevance.open();
    },
    setProject(projectId) {
      this.projectId = projectId;
    },
    saveCaseRelevance() {
      let param = {};
      param.testPlanId = this.planId;
      param.caseIds = [...this.selectIds];
      this.result = this.$post('/test/plan/load/case/relevance', param, () => {
        this.selectIds.clear();
        this.$success(this.$t('commons.save_success'));

        this.$refs.baseRelevance.close();

        this.$emit('refresh');
      });
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    search() {
      this.currentPage = 1;
      this.testCases = [];
      this.getTestCases(true);
    },
    getTestCases() {
      if (this.planId) {
        this.condition.testPlanId = this.planId;
      }
      if (this.projectId) {
        this.condition.projectId = this.projectId;
        this.result = this.$post(this.buildPagePath('/test/plan/load/case/relevance/list'), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.testCases = data.listObject;
        });
      }
    },
    handleSelectAll(selection) {
      if (selection.length > 0) {
        this.testCases.forEach(item => {
          this.selectIds.add(item.id);
        });
      } else {
        this.testCases.forEach(item => {
          if (this.selectIds.has(item.id)) {
            this.selectIds.delete(item.id);
          }
        });
      }
    },
    handleSelectionChange(selection, row) {
      if (this.selectIds.has(row.id)) {
        this.selectIds.delete(row.id);
      } else {
        this.selectIds.add(row.id);
      }
    },
    nodeChange(node, nodeIds, nodeNames) {
      this.selectNodeIds = nodeIds;
      this.selectNodeNames = nodeNames;
    },
    refresh() {
      this.close();
    },
    getAllNodeTreeByPlanId() {
      if (this.planId) {
        let param = {
          testPlanId: this.planId,
          projectId: this.projectId
        };
        this.result = this.$post("/case/node/list/all/plan", param, response => {
          this.treeNodes = response.data;
        });
      }
    },
    close() {
      this.selectIds.clear();
      this.selectNodeIds = [];
      this.selectNodeNames = [];
    },
    getProjectNode(projectId) {
      const index = this.projects.findIndex(project => project.id === projectId);
      if (index !== -1) {
        this.projectName = this.projects[index].name;
      }
      if (projectId) {
        this.projectId = projectId;
      }
      this.treeNodes = [];
      this.selectNodeIds = [];
    }
  }
}
</script>

<style scoped>

</style>
