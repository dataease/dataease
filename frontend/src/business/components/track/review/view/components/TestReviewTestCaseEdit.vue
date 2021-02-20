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

                <el-header style="height: 100%;">

                  <el-row type="flex" class="head-bar">

                    <el-col :span="8">
                      <el-button plain size="mini"
                                 icon="el-icon-back"
                                 @click="cancel">{{ $t('test_track.return') }}
                      </el-button>
                    </el-col>

                    <el-col :span="14" class="head-right">

                      <el-button plain size="mini" icon="el-icon-arrow-up"
                                 :disabled="index + 1 <= 1"
                                 @click="handlePre()"/>
                      <span>  {{ index + 1 }}/{{ testCases.length }} </span>
                      <el-button plain size="mini" icon="el-icon-arrow-down"
                                 :disabled="index + 1 >= testCases.length"
                                 @click="handleNext()"/>
                      <el-divider direction="vertical"></el-divider>

                      <el-button type="success" size="mini"
                                 :disabled="isReadOnly" :icon="testCase.reviewStatus === 'Pass' ? 'el-icon-check' : ''" @click="saveCase('Pass')">
                        {{ $t('test_track.review.pass') }}
                      </el-button>
                      <el-button type="danger" size="mini"
                                 :disabled="isReadOnly" :icon="testCase.reviewStatus === 'UnPass' ? 'el-icon-check' : ''" @click="saveCase('UnPass')">
                        {{ $t('test_track.review.un_pass') }}
                      </el-button>
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
                      <span class="cast_item" v-if="testCase.type === 'functional'">{{
                          $t('commons.functional')
                        }}</span>
                      <span class="cast_item"
                            v-if="testCase.type === 'performance'">{{ $t('commons.performance') }}</span>
                      <span class="cast_item" v-if="testCase.type === 'api'">{{ $t('commons.api') }}</span>
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
                    <el-col :offset="1">
                      <span class="cast_label">{{ $t('test_track.case.prerequisite') }}：</span>
                      <span class="cast_item">{{ testCase.prerequisite }}</span>
                    </el-col>
                  </el-row>

                  <el-row v-if="testCase.method === 'auto' && testCase.testId">
                    <el-col class="test-detail" :span="20" :offset="1">
                      <el-tabs v-model="activeTab" type="border-card">
                        <el-tab-pane name="detail" :label="$t('test_track.plan_view.test_detail')">
                          <api-test-detail :is-read-only="true" v-if="testCase.type === 'api'"
                                           :id="testCase.testId" ref="apiTestDetail"/>
                          <performance-test-detail v-if="testCase.type === 'performance'"
                                                   :is-read-only="true"
                                                   :id="testCase.testId"
                                                   ref="performanceTestDetail"/>
                        </el-tab-pane>
                      </el-tabs>
                    </el-col>
                  </el-row>

                  <el-row v-if="testCase.method && testCase.method !== 'auto'">
                    <el-col :span="20" :offset="1">
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
                        <el-table-column :label="$t('test_track.case.number')" prop="num"
                                         min-width="5%"/>

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
                              :disabled="true"
                              v-model="scope.row.actualResult"
                              :placeholder="$t('commons.input_content')"
                              clearable/>
                          </template>
                        </el-table-column>
                        <el-table-column :label="$t('test_track.plan_view.step_result')" min-width="12%">
                          <template v-slot:default="scope">
                            <el-select
                              :disabled="true"
                              v-model="scope.row.executeResult"
                              @change="stepResultChange()"
                              size="mini">
                              <el-option :label="$t('test_track.plan_view.pass')" value="Pass"
                                         style="color: #7ebf50;"/>
                              <el-option :label="$t('test_track.plan_view.failure')" value="Failure"
                                         style="color: #e57471;"/>
                              <el-option :label="$t('test_track.plan_view.blocking')" value="Blocking"
                                         style="color: #dda451;"/>
                              <el-option :label="$t('test_track.plan_view.skip')" value="Skip"
                                         style="color: #919399;"/>
                            </el-select>
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
                                  v-model="testCase.remark"/>
                      </div>
                    </el-col>
                  </el-row>

                  <el-row>
                    <el-col :span="20" :offset="1">
                      <div>
                        <span class="cast_label">{{ $t('test_track.case.attachment') }}:</span>
                      </div>
                      <div>
                        <test-case-attachment :table-data="tableData"
                                              :read-only="false"
                                              :is-delete="false"
                        />
                      </div>
                    </el-col>
                  </el-row>

                </div>

              </el-scrollbar>
            </el-card>
          </el-col>
          <el-col :span="7">
            <el-card class="comment-card">
              <template slot="header">
                <span style="font-size: 15px; color: #1E90FF">{{ $t('test_track.review.comment') }}</span>
                <i class="el-icon-refresh" @click="getComments(testCase)"
                   style="margin-left:10px;font-size: 14px; cursor: pointer"/>
              </template>
              <review-comment :comments="comments" :case-id="testCase.caseId" :review-id="testCase.reviewId" @getComments="getComments"/>
            </el-card>
          </el-col>
        </div>
      </el-row>

    </template>

  </el-drawer>
</template>

<script>


import PerformanceTestResult from "../../../plan/view/comonents/test/PerformanceTestResult";
import PerformanceTestDetail from "../../../plan/view/comonents/test/PerformanceTestDetail";
import ApiTestResult from "../../../plan/view/comonents/test/ApiTestResult";
import ApiTestDetail from "../../../plan/view/comonents/test/ApiTestDetail";
import TestPlanTestCaseStatusButton from "../../../plan/common/TestPlanTestCaseStatusButton";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import ReviewComment from "../../commom/ReviewComment";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";

export default {
  name: "TestReviewTestCaseEdit",
  components: {
    PerformanceTestResult,
    PerformanceTestDetail,
    ApiTestResult,
    ApiTestDetail,
    TestPlanTestCaseStatusButton,
    ReviewComment,
    TestCaseAttachment
  },
  data() {
    return {
      result: {},
      showDialog: false,
      testCase: {},
      index: 0,
      testCases: [],
      readConfig: {toolbar: []},
      test: {},
      activeTab: 'detail',
      isFailure: true,
      users: [],
      activeName: 'comment',
      comments: [],
      tableData: []
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
    },
    cancel() {
      this.handleClose();
      this.$emit('refreshTable');
    },
    saveCase(status) {
      let param = {};
      param.id = this.testCase.id;
      param.caseId = this.testCase.caseId;
      param.reviewId = this.testCase.reviewId;
      param.status = status;
      this.$post('/test/review/case/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.updateTestCases(param);
        this.setReviewStatus(this.testCase.reviewId);
        // 修改当前用例的评审状态
        this.testCase.reviewStatus = status;
        // 修改当前用例在整个用例列表的状态
        this.testCases[this.index].reviewStatus = status;
        if (this.index < this.testCases.length - 1) {
          this.handleNext();
        }
      });
    },
    updateTestCases(param) {
      for (let i = 0; i < this.testCases.length; i++) {
        let testCase = this.testCases[i];
        if (testCase.id === param.id) {
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
      this.result = this.$get("/test/review/case/get/" + testCase.id, response => {
        let item = {};
        let data = response.data;
        Object.assign(item, data);
        item.steps = JSON.parse(item.steps);
        item.steptResults = [];
        for (let i = 0; i < item.steps.length; i++) {
          item.steps[i].actualResult = '';
          item.steps[i].executeResult = '';
          item.steptResults.push(item.steps[i]);
        }
        this.testCase = item;
        this.getRelatedTest();
        this.getComments(item);
        this.initTest();
        this.getFileMetaData(data);
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
      this.activeTab = 'detail';
      listenGoBack(this.handleClose);
      this.initData(testCase);
      this.getComments(testCase);
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
    getComments(testCase) {
      let id = '';
      if (testCase) {
        id = testCase.caseId;
      } else {
        id = this.testCase.caseId;
      }
      this.result = this.$get('/test/case/comment/list/' + id, res => {
        this.comments = res.data;
      })
    },
    initData(testCase) {
      this.result = this.$post('/test/review/case/list/ids', this.searchParam, response => {
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
      } else if (this.testCase.testId === 'other') {
        this.$warning(this.$t("test_track.case.other_relate_test_not_find"));
      }
    },
    setReviewStatus(reviewId) {
      this.$post('/test/case/review/edit/status/' + reviewId);
    },
    stepResultChange() {
      if (this.testCase.method === 'manual') {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      }
    },
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
  margin-top: 30px;
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

.head-right-tip {
  color: darkgrey;
}

.el-scrollbar {
  height: 100%;
}

.container {
  height: 100vh;
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

.container >>> .el-card__body {
  height: calc(100vh - 70px);
}

.comment-card >>> .el-card__header {
  padding: 0 20px;
}

.comment-card >>> .el-card__body {
  height: calc(100vh - 120px);
}
</style>
