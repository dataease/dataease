<template>

  <div>
    <ms-test-plan-header-bar>
      <template v-slot:info>
        <select-menu
          :data="testPlans"
          :current-data="currentPlan"
          :title="$t('test_track.plan_view.plan')"
          @dataChange="changePlan"/>
      </template>
      <template v-slot:menu>
        <el-menu v-if="isMenuShow" active-text-color="#6d317c" :default-active="activeIndex"
                 class="el-menu-demo header-menu" mode="horizontal" @select="handleSelect">
          <el-menu-item index="functional">功能测试用例</el-menu-item>
          <el-menu-item index="api">接口测试用例</el-menu-item>
          <el-menu-item index="load">性能测试用例</el-menu-item>
          <el-menu-item index="report">报告统计</el-menu-item>
        </el-menu>
      </template>
    </ms-test-plan-header-bar>
    <test-plan-functional v-if="activeIndex === 'functional'" :redirectCharType="redirectCharType" :clickType="clickType" :plan-id="planId"/>
    <test-plan-api v-if="activeIndex === 'api'" :redirectCharType="redirectCharType" :clickType="clickType" :plan-id="planId"/>
    <test-plan-load v-if="activeIndex === 'load'" :redirectCharType="redirectCharType" :clickType="clickType" :plan-id="planId"/>
    <test-case-statistics-report-view :test-plan="currentPlan" v-if="activeIndex === 'report'"/>

    <test-report-template-list @openReport="openReport" ref="testReportTemplateList"/>

  </div>

</template>

<script>

    import NodeTree from "../../common/NodeTree";
    import TestPlanTestCaseList from "./comonents/functional/FunctionalTestCaseList";
    import TestCaseRelevance from "./comonents/functional/TestCaseFunctionalRelevance";
    import SelectMenu from "../../common/SelectMenu";
    import MsContainer from "../../../common/components/MsContainer";
    import MsAsideContainer from "../../../common/components/MsAsideContainer";
    import MsMainContainer from "../../../common/components/MsMainContainer";
    import MsTestPlanHeaderBar from "./comonents/head/TestPlanHeaderBar";
    import TestPlanFunctional from "./comonents/functional/TestPlanFunctional";
    import TestPlanApi from "./comonents/api/TestPlanApi";
    import TestCaseStatisticsReportView from "./comonents/report/statistics/TestCaseStatisticsReportView";
    import TestReportTemplateList from "./comonents/TestReportTemplateList";
    import TestPlanLoad from "@/business/components/track/plan/view/comonents/load/TestPlanLoad";

    export default {
      name: "TestPlanView",
      components: {
        TestReportTemplateList,
        TestCaseStatisticsReportView,
        TestPlanApi,
        TestPlanFunctional,
        MsTestPlanHeaderBar,
        MsMainContainer,
        MsAsideContainer, MsContainer, NodeTree, TestPlanTestCaseList, TestCaseRelevance, SelectMenu, TestPlanLoad},
      data() {
        return {
          testPlans: [],
          currentPlan: {},
          activeIndex: "functional",
          isMenuShow: true,
          //报表跳转过来的参数-通过哪个图表跳转的
          redirectCharType:'',
          //报表跳转过来的参数-通过哪种数据跳转的
          clickType:'',
        }
      },
      computed: {
        planId: function () {
          return this.$route.params.planId;
        }
      },
      watch: {
        '$route.params.planId'() {
          this.genRedirectParam();
          this.getTestPlans();
        }
      },
      mounted() {
        this.getTestPlans();
      },
      activated() {
        this.genRedirectParam();
      },
      methods: {
        genRedirectParam(){
          this.redirectCharType = this.$route.params.charType;
          this.clickType = this.$route.params.clickType;
          if(this.redirectCharType != ""){
            if(this.redirectCharType=='scenario'){
              this.activeIndex = 'api';
            }else if(this.redirectCharType != null && this.redirectCharType != ''){
              this.activeIndex = this.redirectCharType;
            }
          }else{
            this.activeIndex = "functional";
          }
        },
        getTestPlans() {
          this.$post('/test/plan/list/all', {}, response => {
            this.testPlans = response.data;
            this.testPlans.forEach(plan => {
              if (this.planId && plan.id === this.planId) {
                this.currentPlan = plan;
              }
            });
          });
        },
        changePlan(plan) {
          this.currentPlan = plan;
          this.$router.push('/track/plan/view/' + plan.id);
        },
        handleSelect(key) {
          this.activeIndex = key;
          if (key === 'report' && !this.currentPlan.reportId) {
            this.$refs.testReportTemplateList.open(this.planId);
          }
        },
        openTemplateReport() {
          this.$refs.testReportTemplateList.open(this.planId);
        },
        openReport(planId, id) {
          this.currentPlan.reportId = id;
        },
        reloadMenu() {
          this.isMenuShow = false;
          this.$nextTick(() => {
            this.isMenuShow = true;
          });
        }
      },
    }
</script>

<style scoped>

  .select-menu {
    display: inline-block;
  }

  /deep/ .ms-main-container {
    height: calc(100vh - 80px - 53px);
  }

  /deep/ .ms-aside-container {
    height: calc(100vh - 80px - 53px);
    margin-top: 1px;
  }

  .header-menu.el-menu--horizontal > li {
    height: 49px;
    line-height: 50px;
    color: dimgray;
  }


</style>
