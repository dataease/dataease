<template>
  <ms-container>
    <ms-main-container>
      <span v-if="!reportId">{{$t('commons.not_performed_yet')}}</span>
      <el-card v-loading="result.loading" v-if="reportId">
        <el-row>
          <el-col :span="16">
            <el-row>
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/performance/test/' + this.projectId }">{{projectName}}
                </el-breadcrumb-item>
                <el-breadcrumb-item :to="{ path: '/performance/test/edit/' + this.testId }">{{testName}}
                </el-breadcrumb-item>
                <el-breadcrumb-item>{{reportName}}</el-breadcrumb-item>
              </el-breadcrumb>
            </el-row>
            <!--<el-row class="ms-report-view-btns">-->
              <!--<el-button :disabled="isReadOnly" type="primary" plain size="mini">{{$t('report.test_stop_now')}}</el-button>-->
              <!--<el-button :disabled="isReadOnly"  type="success" plain size="mini">{{$t('report.test_execute_again')}}</el-button>-->
              <!--<el-button :disabled="isReadOnly"  type="info" plain size="mini">{{$t('report.export')}}</el-button>-->
              <!--<el-button :disabled="isReadOnly"  type="warning" plain size="mini">{{$t('report.compare')}}</el-button>-->
            <!--</el-row>-->
          </el-col>
          <el-col :span="8">
            <span class="ms-report-time-desc">
              {{$t('report.test_duration', [this.minutes, this.seconds])}}
            </span>
            <span class="ms-report-time-desc">
              {{$t('report.test_start_time')}}：{{startTime}}
            </span>
            <span class="ms-report-time-desc">
              {{$t('report.test_end_time')}}：{{endTime}}
            </span>
          </el-col>
        </el-row>

        <el-divider></el-divider>

        <el-tabs v-model="active" type="border-card" :stretch="true">
          <el-tab-pane :label="$t('report.test_overview')">
            <test-overview :report="report"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_request_statistics')">
            <request-statistics :report="report"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_error_log')">
            <error-log :report="report"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('report.test_log_details')">
            <log-details :report="report"/>
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsMainContainer from "../../../../../common/components/MsMainContainer";
import MsContainer from "../../../../../common/components/MsContainer";
import LogDetails from "../../../../../performance/report/components/LogDetails";
import ErrorLog from "../../../../../performance/report/components/ErrorLog";
import RequestStatistics from "../../../../../performance/report/components/RequestStatistics";
import TestOverview from "../../../../../performance/report/components/TestOverview";

export default {
  name: "PerformanceTestResult",
  components: {
    TestOverview,
    RequestStatistics,
    ErrorLog,
    LogDetails,
    MsContainer,
    MsMainContainer
  },
  data() {
      return {
        result: {},
        active: '0',
        status: '',
        reportName: '',
        testId: '',
        testName: '',
        projectId: '',
        projectName: '',
        startTime: '0',
        endTime: '0',
        minutes: '0',
        seconds: '0',
        title: 'Logging',
        report: {}
      }
    },
    props: {
      reportId: String,
      isReadOnly: {
        type: Boolean,
        default: false
      },
    },
    mounted() {
      this.init();
    },
    watch: {
      reportId() {
        this.init();
      }
    },
    methods: {
      initBreadcrumb() {
        if (this.reportId) {
          this.result = this.$get("/performance/report/test/pro/info/" + this.reportId, res => {
            let data = res.data;
            if (data) {
              this.reportName = data.name;
              this.testId = data.testId;
              this.testName = data.testName;
              this.projectId = data.projectId;
              this.projectName = data.projectName;
            }
          })
        }
      },
      initReportTimeInfo() {
        if (this.reportId) {
          this.result = this.$get("/performance/report/content/report_time/" + this.reportId)
            .then(res => {
            let data = res.data.data;
            if (data) {
              this.startTime = data.startTime;
              this.endTime = data.endTime;
              let duration = data.duration;
              this.minutes = Math.floor(duration / 60);
              this.seconds = duration % 60;
            }
          }).catch(() => {
            this.clearData();
          })
        }
      },
      checkReportStatus() {
        if (!this.report) {
          return;
        }
        switch (this.report.status) {
          case 'Error':
            // this.$warning(this.$t('report.generation_error'));
            break;
          case 'Starting':
            this.$warning(this.$t('report.start_status'));
            break;
          case 'Reporting':
          case 'Running':
          case 'Completed':
          default:
            break;
        }
      },
      clearData() {
        this.startTime = '0';
        this.endTime = '0';
        this.minutes = '0';
        this.seconds = '0';
      },
      init() {
        this.getReport();
        this.getReportView();
      },
      getReportView() {
        if (this.reportId) {
          this.$get("/performance/report/test/pro/info/" + this.reportId, response => {
            let data = response.data;
            if (data) {
              this.status = data.status;
              this.reportName = data.name;
              this.testName = data.testName;
              this.projectName = data.projectName;

              this.$set(this.report, "id", this.reportId);
              this.$set(this.report, "status", data.status);

              if (this.status === "Completed") {
                this.result = this.$get("/performance/report/content/report_time/" + this.reportId).then(res => {
                  let data = res.data.data;
                  if (data) {
                    this.startTime = data.startTime;
                    this.endTime = data.endTime;
                    let duration = data.duration;
                    this.minutes = Math.floor(duration / 60);
                    this.seconds = duration % 60;
                  }
                }).catch(() => {
                  this.clearData();
                })
              } else {
                this.clearData();
              }
            }
          });

        }
      },
      getReport() {
        if (this.reportId) {
          this.result = this.$get("/performance/report/" + this.reportId, res => {
            let data = res.data;
            if (data) {
              this.status = data.status;
              this.$set(this.report, "id", this.reportId);
              this.$set(this.report, "status", data.status);
              if (this.status === "Completed") {
                this.initReportTimeInfo();
              }
            }
          });
        }
        this.initBreadcrumb();
      }
    }
  }
</script>

<style scoped>

  .ms-report-view-btns {
    margin-top: 15px;
  }

  .ms-report-time-desc {
    text-align: left;
    display: block;
    color: #5C7878;
  }

</style>
