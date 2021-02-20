<template>
  <div>
    <el-dialog :title="$t('test_track.review_view.relevance_case')" :visible.sync="dialogFormVisible" @close="close"
               width="60%"
               :close-on-click-modal="false"
               top="50px">

      <el-container class="main-content">
        <el-aside class="tree-aside" width="250px">
          <select-menu
            :data="projects"
            width="160px"
            :current-data="currentProject"
            :title="$t('test_track.switch_project')"
            @dataChange="changeProject"/>
          <node-tree class="node-tree"
                     :all-label="$t('commons.all_label.review')"
                     v-loading="result.loading"
                     @nodeSelectEvent="nodeChange"
                     :tree-nodes="treeNodes"
                     ref="nodeTree"/>
        </el-aside>

        <el-container>
          <el-main class="case-content">
            <ms-table-header :condition.sync="condition" @search="search" title="" :show-create="false"/>
            <el-table :data="testReviews" @mouseleave.passive="leave" v-el-table-infinite-scroll="scrollLoading"
                      @filter-change="filter" row-key="id"
                      @select-all="handleSelectAll"
                      @select="handleSelectionChange"
                      v-loading="result.loading"
                      height="50vh"
                      ref="table">

              <el-table-column type="selection"/>
              <el-table-column
                prop="name"
                :label="$t('test_track.case.name')"
                style="width: 100%">
                <template v-slot:default="scope">
                  {{scope.row.name}}
                </template>
              </el-table-column>

              <el-table-column
                prop="priority"
                :filters="priorityFilters"
                column-key="priority"
                :label="$t('test_track.case.priority')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <priority-table-item :value="scope.row.priority"/>
                </template>
              </el-table-column>

              <el-table-column
                prop="type"
                :filters="typeFilters"
                column-key="type"
                :label="$t('test_track.case.type')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <type-table-item :value="scope.row.type"/>
                </template>
              </el-table-column>

              <el-table-column
                :filters="statusFilters"
                column-key="status"
                :label="$t('test_track.case.status')"
                show-overflow-tooltip>
                <template v-slot:default="scope">
                  <review-status :value="scope.row.reviewStatus"/>
                </template>
              </el-table-column>

            </el-table>
            <div v-if="!lineStatus" style="text-align: center">{{$t('test_track.review_view.last_page')}}</div>
            <div style="text-align: center">共 {{total}} 条</div>
          </el-main>
        </el-container>
      </el-container>

      <template v-slot:footer>
        <ms-dialog-footer @cancel="dialogFormVisible = false" @confirm="saveReviewRelevance"/>
      </template>

    </el-dialog>

    <switch-project ref="switchProject" @getProjectNode="getProjectNode"/>
  </div>

</template>

<script>

import NodeTree from "../../../common/NodeTree";
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import PriorityTableItem from "../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../common/tableItems/planview/TypeTableItem";
import MsTableSearchBar from "../../../../common/components/MsTableSearchBar";
import MsTableAdvSearchBar from "../../../../common/components/search/MsTableAdvSearchBar";
import MsTableHeader from "../../../../common/components/MsTableHeader";
import SwitchProject from "../../../case/components/SwitchProject";
import {TEST_CASE_CONFIGS} from "../../../../common/components/search/search-components";
import ReviewStatus from "@/business/components/track/case/components/ReviewStatus";
import elTableInfiniteScroll from 'el-table-infinite-scroll';
import SelectMenu from "../../../common/SelectMenu";
import {_filter} from "@/common/js/tableUtils";


export default {
  name: "TestReviewRelevance",
  components: {
    SelectMenu,
    NodeTree,
    MsDialogFooter,
    PriorityTableItem,
    TypeTableItem,
    MsTableSearchBar,
    MsTableAdvSearchBar,
    MsTableHeader,
      SwitchProject,
      ReviewStatus

    },
    directives: {
      'el-table-infinite-scroll': elTableInfiniteScroll
    },
    data() {
      return {
        result: {},
        currentProject: {},
        dialogFormVisible: false,
        isCheckAll: false,
        testReviews: [],
        selectIds: new Set(),
        treeNodes: [],
        selectNodeIds: [],
        selectNodeNames: [],
        projectId: '',
        projectName: '',
        projects: [],
        pageSize: 50,
        currentPage: 1,
        total: 0,
        lineStatus: true,
        condition: {
          components: TEST_CASE_CONFIGS
        },
        priorityFilters: [
          {text: 'P0', value: 'P0'},
          {text: 'P1', value: 'P1'},
          {text: 'P2', value: 'P2'},
          {text: 'P3', value: 'P3'}
        ],
        typeFilters: [
          {text: this.$t('commons.functional'), value: 'functional'},
          {text: this.$t('commons.performance'), value: 'performance'},
          {text: this.$t('commons.api'), value: 'api'}
        ],
        statusFilters: [
          {text: this.$t('test_track.case.status_prepare'), value: 'Prepare'},
          {text: this.$t('test_track.case.status_pass'), value: 'Pass'},
          {text: this.$t('test_track.case.status_un_pass'), value: 'UnPass'},
        ],
      };
    },
    props: {
      reviewId: {
        type: String
      }
    },
    watch: {
      reviewId() {
        this.condition.reviewId = this.reviewId;
      },
      selectNodeIds() {
        if (this.dialogFormVisible) {
          this.search();
        }
      },
      projectId() {
        this.condition.projectId = this.projectId;
        this.getProjectNode();
      }
    },
    updated() {
      this.toggleSelection(this.testReviews);
    },
    methods: {
      openTestReviewRelevanceDialog() {
        this.getProject();
        this.dialogFormVisible = true;
      },
      saveReviewRelevance() {
        let param = {};
        param.reviewId = this.reviewId;
        param.testCaseIds = [...this.selectIds];
        param.request = this.condition;
        // 选择全选则全部加入到评审，无论是否加载完全部
        if (this.testReviews.length === param.testCaseIds.length) {
          param.testCaseIds = ['all'];
        }
        this.result = this.$post('/test/case/review/relevance', param, () => {
          this.selectIds.clear();
          this.$success(this.$t('commons.save_success'));
          this.dialogFormVisible = false;
          this.$emit('refresh');
        });
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      getReviews(flag) {
        if (this.reviewId) {
          this.condition.reviewId = this.reviewId;
        }
        if (this.selectNodeIds && this.selectNodeIds.length > 0) {
          this.condition.nodeIds = this.selectNodeIds;
        } else {
          this.condition.nodeIds = [];
        }
        if (this.projectId) {
          this.condition.projectId = this.projectId;
          this.result = this.$post(this.buildPagePath('/test/case/reviews/case'), this.condition, response => {
            let data = response.data;
            this.total = data.itemCount;
            let tableData = data.listObject;
            tableData.forEach(item => {
              item.checked = false;
            });
            flag ? this.testReviews = tableData : this.testReviews = this.testReviews.concat(tableData);
            // 去重处理
            let hash = {}
            this.testReviews = this.testReviews.reduce((item, next) => {
              if (!hash[next.id]) {
                hash[next.id] = true
                item.push(next)
              }
              return item
            }, [])
            this.lineStatus = tableData.length === 50 && this.testReviews.length < this.total;

          });
        }

      },
      handleSelectAll(selection) {
        if (selection.length > 0) {
          this.testReviews.forEach(item => {
            this.selectIds.add(item.id);
          });
        } else {
          this.testReviews.forEach(item => {
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
        if (this.reviewId) {
          let param = {
            reviewId: this.reviewId,
            projectId: this.projectId
          };
          this.result = this.$post("/case/node/list/all/review", param, response => {
            this.treeNodes = response.data;
          });
        }
      },
      close() {
        this.lineStatus = false;
        this.selectIds.clear();
        this.selectNodeIds = [];
        this.selectNodeNames = [];
      },
      filter(filters) {
        _filter(filters, this.condition);
        this.search();
      },
      toggleSelection(rows) {
        rows.forEach(row => {
          this.selectIds.forEach(id => {
            if (row.id === id) {
              // true 是为选中
              this.$refs.table.toggleRowSelection(row, true)
            }
          })
        })
      },
      getProject() {
        if (this.reviewId) {
          this.$post("/test/case/review/projects", {reviewId: this.reviewId}, res => {
            let data = res.data;
            if (data) {
              this.currentProject = data[0];
              this.projects = data;
              this.projectId = data[0].id;
              this.projectName = data[0].name;
            }
          })
        }
      },
      switchProject() {
        this.$refs.switchProject.open({id: this.reviewId, url: '/test/case/review/project/', type: 'review'});
      },
      scrollLoading() {
        if (this.dialogFormVisible && this.lineStatus) {
          this.currentPage += 1;
          this.getReviews();
        }
      },
      search() {
        this.currentPage = 1;
        this.testReviews = [];
        this.getReviews(true);
      },
      changeProject(project) {
        this.projectId = project.id;
      },

      getProjectNode(projectId) {
        const index = this.projects.findIndex(project => project.id === projectId);
        if (index !== -1) {
          this.projectName = this.projects[index].name;
          this.currentProject = this.projects[index];
        }
        if (projectId) {
          this.projectId = projectId;
        }
        this.result = this.$post("/case/node/list/all/review",
          {reviewId: this.reviewId, projectId: this.projectId}, response => {
            this.treeNodes = response.data;
          });

        this.selectNodeIds = [];
      }
    }
  }
</script>

<style scoped>

  .tb-edit .el-input {
    display: none;
    color: black;
  }

  .tb-edit .current-row .el-input {
    display: block;

  }

  .tb-edit .current-row .el-input + span {
    display: none;

  }

  .node-tree {
    margin-right: 10px;
  }

  .el-header {
    background-color: darkgrey;
    color: #333;
    line-height: 60px;
  }

  .case-content {
    padding: 0px 20px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .tree-aside {
    min-height: 300px;
    max-height: 100%;
  }

  .main-content {
    min-height: 300px;
    height: 100%;
    /*border: 1px solid #EBEEF5;*/
  }

  .project-link {
    float: right;
    margin-right: 12px;
    margin-bottom: 10px;
  }

</style>
