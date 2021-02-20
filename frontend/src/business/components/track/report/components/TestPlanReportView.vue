<template>
  <div>

    <el-drawer
      :visible.sync="showDialog"
      :with-header="false"
      :modal-append-to-body="false"
      size="100%"
      ref="drawer"
      v-loading="result.loading">
      <template v-slot:default="scope">

        <el-row type="flex" class="head-bar">
          <el-col :span="12">
            <div class="name-edit">
              <el-button plain size="mini" icon="el-icon-back" @click="handleClose">{{$t('test_track.return')}}
              </el-button>&nbsp;
              <span class="title">{{report.name}}</span>
            </div>
          </el-col>
          <el-col :span="12" class="head-right">
            <el-button :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExport(report.name)">
              {{$t('test_track.plan_view.export_report')}}
            </el-button>
          </el-col>
        </el-row>

        <div class="container" ref="resume" id="app">
          <el-main>
            <div v-for="(item, index) in previews" :key="item.id">
              <template-component :source="source" :isReportView="true" :metric="metric" :planId="planId" :preview="item" :index="index" ref="templateComponent"/>
            </div>
          </el-main>
        </div>

      </template>
    </el-drawer>
    <ms-test-plan-report-export v-if="reportExportVisible" id="testCaseReportExport" :title="report.name" :metric="metric" :previews="previews"/>
    <test-case-report-template-edit :metric="metric" ref="templateEdit" @refresh="getReport"/>
  </div>
</template>

<script>
  import {checkoutTestManagerOrTestUser, exportPdf, jsonToMap, mapToJson} from "@/common/js/utils";
  import BaseInfoComponent
    from "@/business/components/track/plan/view/comonents/report/TemplateComponent/BaseInfoComponent";
  import TestResultChartComponent
    from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TestResultChartComponent";
  import TestResultComponent
    from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TestResultComponent";
  import RichTextComponent
    from "@/business/components/track/plan/view/comonents/report/TemplateComponent/RichTextComponent";
  import TestCaseReportTemplateEdit
    from "@/business/components/track/plan/view/comonents/report/TestCaseReportTemplateEdit";
  import TemplateComponent
    from "@/business/components/track/plan/view/comonents/report/TemplateComponent/TemplateComponent";
  import html2canvas from "html2canvas";
  import MsTestPlanReportExport from "./TestPlanReportExport";


  export default {
    name: "TestPlanReportView",
    components: {
      MsTestPlanReportExport,
      TemplateComponent,
      TestCaseReportTemplateEdit,
      RichTextComponent, TestResultComponent, TestResultChartComponent, BaseInfoComponent
    },
    data() {
      return {
        result: {},
        imgUrl: "",
        showDialog: false,
        previews: [],
        report: {},
        reportId: '',
        source:"ReportView",
        reportComponents:[1,3,4],
        metric: {},
        planId: '',
        reportExportVisible: false,
        componentMap: new Map(
          [
            [1, {name: this.$t('test_track.plan_view.base_info'), id: 1, type: 'system'}],
            [2, {name: this.$t('test_track.plan_view.test_result'), id: 2, type: 'system'}],
            [3, {name: this.$t('test_track.plan_view.result_distribution'), id: 3, type: 'system'}],
            [4, {name: this.$t('test_track.plan_view.failure_case'), id: 4, type: 'system'}],
            [5, {name: this.$t('test_track.plan_view.defect_list'), id: 5, type: 'system'}],
            [6, {name: this.$t('test_track.plan_view.custom_component'), id: 6, type: 'custom'}]
          ]
        ),
        isTestManagerOrTestUser: false
      }
    },
    mounted() {
      this.isTestManagerOrTestUser = checkoutTestManagerOrTestUser();
    },
    watch: {
      reportComponents() {
        this.initPreviews();
      }
    },
    methods: {
      listenGoBack() {
        //监听浏览器返回操作，关闭该对话框
        if (window.history && window.history.pushState) {
          history.pushState(null, null, document.URL);
          window.addEventListener('popstate', this.goBack, false);
        }
      },
      goBack() {
        this.handleClose();
      },
      open(reportId) {
        this.reportId = reportId;
        this.getReport();
        this.showDialog = true;
        this.listenGoBack();
      },
      getReport() {
        this.getMetric();
        this.initPreviews();
      },
      initPreviews() {
        this.previews = [];
        this.reportComponents.forEach(item => {
          let preview = this.componentMap.get(item);
          if (preview && preview.type != 'custom') {
            this.previews.push(preview);
          } else {
            if (this.report.content.customComponent) {
              let customComponent = this.report.content.customComponent.get(item.toString());
              if (customComponent) {
                this.previews.push({id: item, title: customComponent.title, content: customComponent.content});
              }
            }
          }
        });
      },
      handleClose() {
        window.removeEventListener('popstate', this.goBack, false);
        this.$emit('refresh');
        this.showDialog = false;
      },
      getMetric() {
        this.result = this.$get('/test/plan/report/getMetric/' + this.reportId, response => {
          this.metric = response.data;
          let components = response.data.reportComponents;
          this.planId = response.data.testPlanId;
          this.report.name = response.data.name;
          this.report.startTime = response.data.startTime;
          this.report.endTime = response.data.endTime;
          if(components === null || components === ''){
            this.reportComponents = [1,3,4];
          }else {
            this.reportComponents = JSON.parse(components);
          }

          if (!this.metric.failureTestCases) {
            this.metric.failureTestCases = [];
          }
          if (!this.metric.executeResult) {
            this.metric.executeResult = [];
          }
          if (!this.metric.moduleExecuteResult) {
            this.metric.moduleExecuteResult = [];
          }
          /*缺陷列表*/
          if (!this.metric.issues) {
            this.metric.issues = [];
          }


          if (this.report.startTime) {
            this.metric.startTime = new Date(this.report.startTime);
          }
          if (this.report.endTime) {
            this.metric.endTime = new Date(this.report.endTime);
          }
        });
      },
      handleExport(name) {
        this.result.loading = true;
        this.reportExportVisible = true;
        let reset = this.exportReportReset;

        this.$nextTick(function () {
          setTimeout(() => {
            html2canvas(document.getElementById('testCaseReportExport'), {
              scale: 2
            }).then(function(canvas) {
              exportPdf(name, [canvas]);
              reset();
            });
          }, 1000);
        });
      },
      exportReportReset() {
        this.reportExportVisible = false;
        this.result.loading = false;
      },
    }
  }
</script>cd

<style scoped>

  .el-main {
    height: calc(100vh - 70px);
    width: 100%;
  }

  .head-bar {
    background: white;
    height: 45px;
    line-height: 45px;
    padding: 0 10px;
    border: 1px solid #EBEEF5;
    box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  }

  .container {
    height: 100vh;
    background: #F5F5F5;
  }

  .el-card {
    width: 70%;
    margin: 5px auto;
  }

  .head-right {
    text-align: right;
  }

</style>
