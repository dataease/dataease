<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    :modal-append-to-body="false"
    size="100%"
    ref="drawer"
    v-loading="result.loading">

    <template v-slot:default="scope">
      <el-row :gutter="10">
        <div class="container">
          <el-col :span="17">
            <el-card>
              <el-scrollbar>

                <el-header>

                  <el-row type="flex" class="head-bar">

                    <el-col :span="4">
                      <el-button plain size="mini"
                                 icon="el-icon-back"
                                 @click="cancel">{{ $t('test_track.return') }}
                      </el-button>
                    </el-col>

                    <el-col class="head-right" :span="20">
                      <ms-previous-next-button :index="index" @pre="handlePre" @next="handleNext" :list="testCases"/>
                    </el-col>

                  </el-row>

                  <el-row style="margin-top: 0;">
                    <el-col>
                      <el-divider content-position="left">{{ testCase.name }}</el-divider>
                    </el-col>
                  </el-row>

                </el-header>

                <div class="case_container">
                  <el-row>
                    <el-col :span="4" :offset="1">
                      <span class="cast_label">{{ $t('test_track.case.priority') }}：</span>
                      <span class="cast_item">{{ testCase.priority }}</span>
                    </el-col>
                    <el-col :span="5">
                      <span class="cast_label">{{ $t('test_track.case.case_type') }}：</span>
                      <span class="cast_item" v-if="testCase.type === 'functional'">{{ $t('commons.functional') }}</span>
                      <span class="cast_item" v-if="testCase.type === 'performance'">{{ $t('commons.performance') }}</span>
                      <span class="cast_item" v-if="testCase.type === 'api'">{{ $t('commons.api') }}</span>
                    </el-col>
                    <el-col :span="10">
                      <test-plan-test-case-status-button class="status-button"
                                                         @statusChange="statusChange"
                                                         :is-read-only="isReadOnly"
                                                         :is-failure="isFailure"
                                                         :status="testCase.status"/>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="4" :offset="1">
                      <span class="cast_label">{{ $t('test_track.case.method') }}：</span>
                      <span v-if="testCase.method === 'manual'">{{ $t('test_track.case.manual') }}</span>
                      <span v-if="testCase.method === 'auto'">{{ $t('test_track.case.auto') }}</span>
                    </el-col>
                    <el-col :span="5">
                      <span class="cast_label">{{ $t('test_track.case.module') }}：</span>
                      <span class="cast_item">{{ testCase.nodePath }}</span>
                    </el-col>
                    <el-col :span="4" :offset="1">
                      <span class="cast_label">{{ $t('test_track.plan.plan_project') }}：</span>
                      <span class="cast_item">{{ testCase.projectName }}</span>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="4" :offset="1" v-if="testCase.testId == 'other'">
                      <span class="cast_label">{{ $t('test_track.case.test_name') }}：</span>
                      <span class="cast_item">{{ testCase.otherTestName }}</span>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :offset="1">
                      <span class="cast_label">{{ $t('test_track.case.prerequisite') }}：</span>
                      <span class="cast_item"><p>{{ testCase.prerequisite }}</p></span>
                    </el-col>
                  </el-row>

                  <el-row v-if="testCase.method === 'auto' && testCase.testId && testCase.testId != 'other'">
                    <el-col class="test-detail" :span="20" :offset="1">
                      <el-tabs v-model="activeTab" type="border-card" @tab-click="testTabChange">
                        <el-tab-pane name="detail" :label="$t('test_track.plan_view.test_detail')">
                          <api-test-detail :is-read-only="isReadOnly" v-if="testCase.type === 'api'" @runTest="testRun"
                                           :id="testCase.testId" ref="apiTestDetail"/>
                          <performance-test-detail :is-read-only="isReadOnly" v-if="testCase.type === 'performance'"
                                                   @runTest="testRun" :id="testCase.testId" ref="performanceTestDetail"/>
                        </el-tab-pane>
                        <el-tab-pane name="result" :label="$t('test_track.plan_view.test_result')">
                          <api-test-result :report-id="testCase.reportId" v-if=" testCase.type === 'api'"
                                           ref="apiTestResult"/>
                          <performance-test-result :is-read-only="isReadOnly" :report-id="testCase.reportId"
                                                   v-if="testCase.type === 'performance'" ref="performanceTestResult"/>
                        </el-tab-pane>
                      </el-tabs>
                    </el-col>
                  </el-row>

                  <el-row v-if="testCase.method && testCase.method !== 'auto'">
                    <el-col :span="22" :offset="1">
                      <div>
                        <span class="cast_label">{{ $t('test_track.case.steps') }}：</span>
                      </div>
                      <el-table
                        :data="testCase.steptResults"
                        class="tb-edit"
                        size="mini"
                        :border="true"
                        :default-sort="{prop: 'num', order: 'ascending'}"
                        highlight-current-row>
                        <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="5%"></el-table-column>

                        <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="21%">
                          <template v-slot:default="scope">
                            <el-input
                              size="mini"
                              class="border-hidden"
                              type="textarea"
                              :autosize="{ minRows: 1, maxRows: 4}"
                              :disabled="true"
                              v-model="scope.row.desc"/>
                          </template>
                        </el-table-column>
                        <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="21%">
                          <template v-slot:default="scope">
                            <el-input
                              size="mini"
                              class="border-hidden"
                              type="textarea"
                              :autosize="{ minRows: 1, maxRows: 4}"
                              :disabled="true"
                              v-model="scope.row.result"/>
                          </template>
                        </el-table-column>
                        <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="21%">
                          <template v-slot:default="scope">
                            <el-input
                              class="table-edit-input"
                              size="mini"
                              type="textarea"
                              :autosize="{ minRows: 2, maxRows: 4}"
                              :rows="2"
                              :disabled="isReadOnly"
                              v-model="scope.row.actualResult"
                              :placeholder="$t('commons.input_content')"
                              clearable/>
                          </template>
                        </el-table-column>
                        <el-table-column :label="$t('test_track.plan_view.step_result')" min-width="12%">
                          <template v-slot:default="scope">
                            <el-select
                              :disabled="isReadOnly"
                              v-model="scope.row.executeResult"
                              @change="stepResultChange()"
                              filterable
                              size="mini">
                              <el-option :label="$t('test_track.plan_view.pass')" value="Pass"
                                         style="color: #7ebf50;"></el-option>
                              <el-option :label="$t('test_track.plan_view.failure')" value="Failure"
                                         style="color: #e57471;"></el-option>
                              <el-option :label="$t('test_track.plan_view.blocking')" value="Blocking"
                                         style="color: #dda451;"></el-option>
                              <el-option :label="$t('test_track.plan_view.skip')" value="Skip"
                                         style="color: #919399;"></el-option>
                            </el-select>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="5" :offset="1">
                      <el-switch
                        :disabled="isReadOnly"
                        v-model="issuesSwitch"
                        @change="issuesChange"
                        :active-text="$t('test_track.plan_view.submit_issues')">
                      </el-switch>
                      <el-tooltip class="item" effect="dark"
                                  :content="$t('test_track.issue.platform_tip')"
                                  placement="right">
                        <i class="el-icon-info"/>
                      </el-tooltip>
                    </el-col>
                  </el-row>

                  <el-row v-if="issuesSwitch">
                    <el-col :span="20" :offset="1" class="issues-edit">
                      <el-input
                        type="text"
                        :placeholder="$t('test_track.issue.input_title')"
                        v-model="testCase.issues.title"
                        maxlength="60"
                        show-word-limit
                      />
                      <ckeditor :editor="editor" :disabled="isReadOnly" :config="editorConfig"
                                v-model="testCase.issues.content"/>
                      <el-row v-if="hasTapdId">
                        {{ $t('test_track.issue.tapd_current_owner') }}
                        <el-select v-model="testCase.tapdUsers"
                                   multiple
                                   filterable
                                   style="width: 20%"
                                   :placeholder="$t('test_track.issue.please_choose_current_owner')"
                                   collapse-tags size="small">
                          <el-option v-for="(userInfo, index) in users" :key="index" :label="userInfo.user"
                                     :value="userInfo.user"/>
                        </el-select>
                      </el-row>
                      <el-row v-if="hasZentaoId">
                        {{ $t('test_track.issue.zentao_bug_build') }}
                        <el-select v-model="testCase.zentaoBuilds"
                                   multiple
                                   filterable
                                   style="width: 20%"
                                   :placeholder="$t('test_track.issue.zentao_bug_build')"
                                   collapse-tags size="small">
                          <el-option v-for="(build, index) in Builds" :key="index" :label="build.name"
                                     :value="build.id"/>
                        </el-select>
                        {{ $t('test_track.issue.zentao_bug_assigned') }}
                        <el-select v-model="testCase.zentaoAssigned"
                                   filterable
                                   style="width: 20%"
                                   :placeholder="$t('test_track.issue.please_choose_current_owner')"
                                   collapse-tags size="small">
                          <el-option v-for="(userInfo, index) in zentaoUsers" :key="index" :label="userInfo.name"
                                     :value="userInfo.user"/>
                        </el-select>
                      </el-row>
                      <el-button type="primary" size="small" @click="saveIssues">{{ $t('commons.save') }}</el-button>
                      <el-button size="small" @click="issuesSwitch=false">{{ $t('commons.cancel') }}</el-button>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="20" :offset="1" class="issues-edit">
                      <el-table border class="adjust-table" :data="issues" style="width: 100%">
                        <el-table-column prop="id" :label="$t('test_track.issue.id')" show-overflow-tooltip/>
                        <el-table-column prop="title" :label="$t('test_track.issue.title')" show-overflow-tooltip/>
                        <el-table-column prop="description" :label="$t('test_track.issue.description')">
                          <template v-slot:default="scope">
                            <el-popover
                              placement="right"
                              width="500"
                              trigger="hover"
                              popper-class="issues-popover"
                            >
                              <ckeditor :editor="editor" disabled :config="readConfig"
                                        v-model="scope.row.description"/>
                              <el-button slot="reference" type="text">{{ $t('test_track.issue.preview') }}</el-button>
                            </el-popover>
                          </template>
                        </el-table-column>
                        <el-table-column prop="status" :label="$t('test_track.issue.status')"/>
                        <el-table-column prop="platform" :label="$t('test_track.issue.platform')"/>
                        <el-table-column :label="$t('test_track.issue.operate')">
                          <template v-slot:default="scope">
                            <el-tooltip :content="$t('test_track.issue.close')"
                                        placement="top" :enterable="false">
                              <el-button type="danger" icon="el-icon-circle-close" size="mini"
                                         circle v-if="scope.row.platform === 'Local'"
                                         @click="closeIssue(scope.row)"
                              />
                            </el-tooltip>
                            <el-tooltip :content="$t('test_track.issue.delete')"
                                        placement="top" :enterable="false">
                              <el-button type="danger" icon="el-icon-delete" size="mini"
                                         circle v-if="scope.row.platform === 'Local'"
                                         @click="deleteIssue(scope.row)"
                              />
                            </el-tooltip>
                          </template>
                        </el-table-column>
                      </el-table>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="15" :offset="1">
                      <div>
                        <span class="cast_label">{{ $t('commons.remark') }}：</span>
                        <span v-if="testCase.remark == null || testCase.remark === ''"
                              style="color: darkgrey">{{ $t('commons.not_filled') }}</span>
                      </div>
                      <div>
                        <el-input :rows="3"
                                  type="textarea"
                                  v-if="testCase.remark"
                                  disabled
                                  v-model="testCase.remark"></el-input>
                      </div>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="15" :offset="1">
                      <div>
                        <span class="cast_label">{{ $t('test_track.case.attachment') }}:</span>
                      </div>
                      <div>
                        <test-case-attachment :table-data="tableData"
                                              :read-only="isReadOnly"
                                              :is-delete="false"
                                              @handleDelete="handleDelete"
                        />
                      </div>
                    </el-col>
                  </el-row>
                </div>

              </el-scrollbar>
            </el-card>
          </el-col>
          <el-col :span="7">
            <case-comment :case-id="testCase ? testCase.caseId : ''" class="comment-card"/>
          </el-col>


        </div>
      </el-row>


    </template>

  </el-drawer>


</template>

<script>
import TestPlanTestCaseStatusButton from '../../../common/TestPlanTestCaseStatusButton';
import ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import ApiTestDetail from "../test/ApiTestDetail";
import ApiTestResult from "../test/ApiTestResult";
import PerformanceTestDetail from "../test/PerformanceTestDetail";
import PerformanceTestResult from "../test/PerformanceTestResult";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import CaseComment from "@/business/components/track/case/components/CaseComment";
import MsPreviousNextButton from "../../../../../common/components/MsPreviousNextButton";

export default {
  name: "FunctionalTestCaseEdit",
  components: {
    MsPreviousNextButton,
    CaseComment,
    PerformanceTestResult,
    PerformanceTestDetail,
    ApiTestResult,
    ApiTestDetail,
    TestPlanTestCaseStatusButton,
    TestCaseAttachment
  },
  data() {
    return {
      result: {},
      showDialog: false,
      testCase: {},
      index: 0,
      issuesSwitch: false,
      testCases: [],
      issues: [],
      editor: ClassicEditor,
      editorConfig: {
        toolbar: ['heading', '|', 'bold', 'italic', 'link', 'bulletedList', 'numberedList', 'blockQuote', 'insertTable', '|', 'undo', 'redo'],
      },
      readConfig: {toolbar: []},
      test: {},
      activeTab: 'detail',
      isFailure: true,
      users: [],
      Builds: [],
      zentaoBuilds: [],
      zentaoUsers: [],
      zentaoAssigned: "",
      hasTapdId: false,
      hasZentaoId: false,
      tableData: [],
    };
  },
  props: {
    total: {
      type: Number
    },
    searchParam: {
      type: Object
    },
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    handleClose() {
      removeGoBackListener(this.handleClose);
      this.showDialog = false;
      this.searchParam.status = null;
      this.$emit('update:search-param', this.searchParam);
    },
    cancel() {
      this.handleClose();
      this.$emit('refreshTable');
    },
    statusChange(status) {
      this.testCase.status = status;
      this.saveCase();
    },
    saveCase() {
      let param = {};
      param.id = this.testCase.id;
      param.status = this.testCase.status;
      param.results = [];

      for (let i = 0; i < this.testCase.steptResults.length; i++) {
        let result = {};
        result.actualResult = this.testCase.steptResults[i].actualResult;
        result.executeResult = this.testCase.steptResults[i].executeResult;
        if (result.actualResult && result.actualResult.length > 300) {
          this.$warning(this.$t('test_track.plan_view.actual_result')
            + this.$t('test_track.length_less_than') + '300');
          return;
        }
        param.results.push(result);
      }

      param.results = JSON.stringify(param.results);
      this.$post('/test/plan/case/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.updateTestCases(param);
        this.setPlanStatus(this.testCase.planId);
        if (this.index < this.testCases.length - 1) {
          this.handleNext();
        }
      });
    },
    updateTestCases(param) {
      for (let i = 0; i < this.testCases.length; i++) {
        let testCase = this.testCases[i];
        if (testCase.id === param.id) {
          testCase.results = param.results;
          testCase.issues = param.issues;
          testCase.status = param.status;
          return;
        }
      }
    },
    handleNext() {
      this.index++;
      this.getTestCase(this.index);
    },
    handlePre() {
      this.index--;
      this.getTestCase(this.index);
    },
    getTestCase(index) {
      this.testCase = {};
      let testCase = this.testCases[index];
      // id 为 TestPlanTestCase 的 id
      this.result = this.$get('/test/plan/case/get/' + testCase.id, response => {
        let item = {};
        Object.assign(item, response.data);
        item.results = JSON.parse(item.results);
        item.steps = JSON.parse(item.steps);
        if (item.issues) {
          item.issues = JSON.parse(item.issues);
        } else {
          item.issues = {};
        }
        item.steptResults = [];
        for (let i = 0; i < item.steps.length; i++) {
          if (item.results[i]) {
            item.steps[i].actualResult = item.results[i].actualResult;
            item.steps[i].executeResult = item.results[i].executeResult;
          }
          item.steptResults.push(item.steps[i]);
        }
        this.testCase = item;
        this.getRelatedTest();
        this.initTest();
        this.getIssues(item.caseId);
        this.stepResultChange();
        this.getFileMetaData(item);
      })
    },
    getFileMetaData(testCase) {
      this.tableData = [];
      this.result = this.$get("test/case/file/metadata/" + testCase.caseId, response => {
        let files = response.data;
        if (!files) {
          return;
        }
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = f.size + ' Bytes';
        });
      })
    },
    openTestCaseEdit(testCase) {
      this.showDialog = true;
      this.issuesSwitch = false;
      this.activeTab = 'detail';
      this.hasTapdId = false;
      this.hasZentaoId = false;
      listenGoBack(this.handleClose);
      this.initData(testCase);
    },
    initTest() {
      this.$nextTick(() => {
        if (this.testCase.testId && this.testCase.testId !== 'other') {
          if (this.testCase.method === 'auto') {
            if (this.$refs.apiTestDetail && this.testCase.type === 'api') {
              this.$refs.apiTestDetail.init();
            } else if (this.testCase.type === 'performance') {
              this.$refs.performanceTestDetail.init();
            }
          }
        }
      });
    },
    testRun(reportId) {
      this.testCase.reportId = reportId;
      this.saveReport(reportId);
      this.activeTab = 'result';
    },
    testTabChange(data) {
      if (this.testCase.type === 'performance' && data.paneName === 'result') {
        this.$refs.performanceTestResult.checkReportStatus();
        this.$refs.performanceTestResult.init();
      }
    },
    saveReport(reportId) {
      this.$post('/test/plan/case/edit', {id: this.testCase.id, reportId: reportId});
    },
    initData(testCase) {
      this.result = this.$post('/test/plan/case/list/ids', this.searchParam, response => {
        this.testCases = response.data;
        for (let i = 0; i < this.testCases.length; i++) {
          if (this.testCases[i].id === testCase.id) {
            this.index = i;
            this.getTestCase(i);
          }
        }
      });
    },
    getRelatedTest() {
      if (this.testCase.method === 'auto' && this.testCase.testId && this.testCase.testId !== 'other') {
        this.$get('/' + this.testCase.type + '/get/' + this.testCase.testId, response => {
          let data = response.data;
          if (data) {
            this.test = data;
          } else {
            this.test = {};
            this.$warning(this.$t("test_track.case.relate_test_not_find"));
          }
        });
      } else if (this.testCase.testId === 'other' && this.testCase.method === 'auto') {
        this.$warning(this.$t("test_track.case.other_relate_test_not_find"));
      }
    },
    issuesChange() {
      if (this.issuesSwitch) {
        let desc = this.addPLabel('[' + this.$t('test_track.plan_view.operate_step') + ']');
        let result = this.addPLabel('[' + this.$t('test_track.case.expected_results') + ']');
        let actualResult = this.addPLabel('[' + this.$t('test_track.plan_view.actual_result') + ']');
        this.testCase.steps.forEach(step => {
          let stepPrefix = this.$t('test_track.plan_view.step') + step.num + ':';
          desc += this.addPLabel(stepPrefix + (step.desc === undefined ? '' : step.desc));
          result += this.addPLabel(stepPrefix + (step.result === undefined ? '' : step.result));
          actualResult += this.addPLabel(stepPrefix + (step.actualResult === undefined ? '' : step.actualResult));
        });
        this.testCase.issues.content = desc + this.addPLabel('') + result + this.addPLabel('') + actualResult + this.addPLabel('');

        this.$get("/test/case/project/" + this.testCase.caseId, res => {
          const project = res.data;
          if (project.tapdId) {
            this.hasTapdId = true;
            this.result = this.$get("/issues/tapd/user/" + this.testCase.caseId, response => {
              this.users = response.data;
            })
          }
          if (project.zentaoId) {
            this.hasZentaoId = true;
            this.result = this.$get("/issues/zentao/builds/" + this.testCase.caseId, response => {
              this.Builds = response.data;
            })
            this.result = this.$get("/issues/zentao/user/" + this.testCase.caseId, response => {
              this.zentaoUsers = response.data;
            })
          }
        })
      }
    },
    addPLabel(str) {
      return "<p>" + str + "</p>";
    },
    setPlanStatus(planId) {
      this.$post('/test/plan/edit/status/' + planId);
    },
    stepResultChange() {
      if (this.testCase.method === 'manual') {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      } else {
        this.isFailure = false;
      }
    },
    saveIssues() {
      if (!this.testCase.issues.title || !this.testCase.issues.content) {
        this.$warning(this.$t('test_track.issue.title_description_required'));
        return;
      }

      let param = {};
      param.title = this.testCase.issues.title;
      param.content = this.testCase.issues.content;
      param.testCaseId = this.testCase.caseId;
      param.tapdUsers = this.testCase.tapdUsers;
      param.zentaoBuilds = this.testCase.zentaoBuilds;
      param.zentaoUser = this.testCase.zentaoAssigned;

      this.result = this.$post("/issues/add", param, () => {
        this.$success(this.$t('commons.save_success'));
        this.getIssues(param.testCaseId);
      });

      this.issuesSwitch = false;
      this.testCase.issues.title = "";
      this.testCase.issues.content = "";
      this.testCase.tapdUsers = [];
      this.testCase.zentaoBuilds = [];
      this.testCase.zentaoAssigned = "";
    },
    getIssues(caseId) {
      this.result = this.$get("/issues/get/" + caseId, response => {
        this.issues = response.data;
      })
    },
    closeIssue(row) {
      if (row.status === 'closed') {
        this.$success(this.$t('test_track.issue.close_success'));
      } else {
        this.result = this.$get("/issues/close/" + row.id, () => {
          this.getIssues(this.testCase.caseId);
          this.$success(this.$t('test_track.issue.close_success'));
        });
      }
    },
    deleteIssue(row) {
      this.result = this.$get("/issues/delete/" + row.id, () => {
        this.getIssues(this.testCase.caseId);
        this.$success(this.$t('commons.delete_success'));
      })
    },
    handleDelete() {

    }
  }
}
</script>

<style scoped>

.border-hidden >>> .el-textarea__inner {
  border-style: hidden;
  background-color: white;
  color: #606266;
}

.cast_label {
  color: dimgray;
}

.status-button {
  padding-left: 4%;
}

.head-right {
  text-align: right;
}

.el-col:not(.test-detail) {
  line-height: 50px;
}

.issues-edit >>> p {
  line-height: 16px;
}

.status-button {
  float: right;
}

.el-scrollbar {
  height: 100%;
}

.container {
  height: 100vh;
}

.container >>> .el-card__body {
  height: calc(100vh - 70px);
}

.comment-card >>> .el-card__header {
  padding: 0 20px;
}

.comment-card >>> .el-card__body {
  height: calc(100vh - 120px);
}

.case_container > .el-row {
  margin-top: 1%;
}

.el-switch >>> .el-switch__label {
  color: dimgray;
}

.el-switch >>> .el-switch__label.is-active {
  color: #409EFF;
}

p {
  white-space: pre-line;
  line-height: 20px;
}

.head-bar {
  z-index: 999;
}
</style>

<style>
.issues-popover {
  height: 550px;
  overflow: auto;
}
</style>
