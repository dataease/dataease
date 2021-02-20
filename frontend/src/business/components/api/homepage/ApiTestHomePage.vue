<template>
  <ms-container>
    <el-header height="0">
      <div style="float: right">
        <div v-if="tipsType==='1'">
          🤔️ 天凉了，保温杯买了吗？
        </div>
        <div v-else-if="tipsType==='2'">
          😔 觉得MeterSphere不好用就来
          <el-link href="https://github.com/metersphere/metersphere/issues" target="_blank" style="color: black"
                   type="primary">https://github.com/metersphere/metersphere/issues
          </el-link>
          吐个槽吧！
        </div>
        <div v-else-if="tipsType==='3'">
          😄 觉得MeterSphere好用就来
          <el-link href="https://github.com/metersphere/metersphere" target="_blank" style="color: black"
                   type="primary">https://github.com/metersphere/metersphere
          </el-link>
          点个star吧！
        </div>
        <div v-else>
          😊 MeterSphere温馨提醒 —— 多喝热水哟！
        </div>
      </div>
    </el-header>
    <ms-main-container v-loading="result.loading">
      <el-row :gutter="0"></el-row>
      <el-row :gutter="10">
        <el-col :span="6">
          <ms-api-info-card @redirectPage="redirectPage" :api-count-data="apiCountData"/>
        </el-col>
        <el-col :span="6">
          <ms-test-case-info-card @redirectPage="redirectPage" :test-case-count-data="testCaseCountData"/>
        </el-col>
        <el-col :span="6">
          <ms-scene-info-card @redirectPage="redirectPage"  :scene-count-data="sceneCountData"/>
        </el-col>
        <el-col :span="6">
          <ms-schedule-task-info-card :schedule-task-count-data="scheduleTaskCountData"/>
        </el-col>
      </el-row>

      <el-row :gutter="10">
        <el-col :span="12">
          <ms-failure-test-case-list @redirectPage="redirectPage"/>
        </el-col>
        <el-col :span="12">
          <ms-running-task-list @redirectPage="redirectPage"/>
        </el-col>
      </el-row>

    </ms-main-container>
  </ms-container>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsApiInfoCard from "./components/ApiInfoCard";
import MsSceneInfoCard from "./components/SceneInfoCard";
import MsScheduleTaskInfoCard from "./components/ScheduleTaskInfoCard";
import MsTestCaseInfoCard from "./components/TestCaseInfoCard";

import MsFailureTestCaseList from "./components/FailureTestCaseList";
import MsRunningTaskList from "./components/RunningTaskList"
import {getCurrentProjectID,getUUID} from "@/common/js/utils";

export default {
  name: "ApiTestHomePage",

  components: {
    MsApiInfoCard, MsSceneInfoCard, MsScheduleTaskInfoCard, MsTestCaseInfoCard,
    MsFailureTestCaseList, MsRunningTaskList,
    MsMainContainer, MsContainer
  },

  data() {
    return {
      values: [],
      apiCountData: {},
      sceneCountData: {},
      testCaseCountData: {},
      scheduleTaskCountData: {},
      tipsType: "1",
      result: {},
    }
  },
  activated() {
    this.search();
    this.checkTipsType();
  },
  created() {
  },
  methods: {
    checkTipsType() {
      var random = Math.floor(Math.random() * (4 - 1 + 1)) + 1;
      this.tipsType = random + "";
    },

    search() {
      let selectProjectId = getCurrentProjectID();

      this.$get("/api/apiCount/" + selectProjectId, response => {
        this.apiCountData = response.data;
      });

      this.$get("/api/testSceneInfoCount/" + selectProjectId, response => {
        this.sceneCountData = response.data;
      });

      this.$get("/api/testCaseInfoCount/" + selectProjectId, response => {
        this.testCaseCountData = response.data;
      });

      this.$get("/api/scheduleTaskInfoCount/" + selectProjectId, response => {
        this.scheduleTaskCountData = response.data;
      });
    },
    redirectPage(page,dataType,selectType){
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      switch (page){
        case "api":
          this.$router.push({name:'ApiDefinition',params:{redirectID:uuid,dataType:dataType,dataSelectRange:selectType}});
          break;
        case "scenario":
          this.$router.push({name:'ApiAutomation',params:{redirectID:uuid,dataType:dataType,dataSelectRange:selectType}});
          break;
        case "testPlanEdit":
          this.$router.push('/track/plan/view/'+selectType)
          break;
      }
    }
  }
}
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
  margin-left: 20px;
  margin-right: 20px;
}
</style>
