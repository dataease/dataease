<template>
  <el-dropdown @command="handleCommand" class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
      <el-dropdown-item :disabled="isCaseEdit" command="create_performance">{{ $t('api_test.create_performance_test') }}</el-dropdown-item>
    </el-dropdown-menu>
    <ms-reference-view ref="viewRef"/>
  </el-dropdown>
</template>

<script>
  import MsReferenceView from "./ReferenceView";
  import MsTestPlanList from "../../../automation/scenario/testplan/TestPlanList";
  import {getBodyUploadFiles, getCurrentProjectID, getUUID} from "@/common/js/utils";
  import TestPlan from "@/business/components/api/definition/components/jmeter/components/test-plan";
  import ThreadGroup from "@/business/components/api/definition/components/jmeter/components/thread-group";

  export default {
    name: "MsApiExtendBtns",
    components: {MsReferenceView, MsTestPlanList},
    props: {
      row: Object,
      isCaseEdit: Boolean,
      environment: {},
    },
    data() {
      return {
        planVisible: false,
      }
    },
    methods: {
      handleCommand(cmd) {
        if (this.row.id) {
          switch (cmd) {
            case  "ref":
              this.$refs.viewRef.open(this.row);
              break;
            case "create_performance":
              this.createPerformance(this.row);
              break;
          }
        } else {
          this.$warning(this.$t('api_test.automation.save_case_info'))
        }
      },
      createPerformance(row){
        /**
         * 思路：调用后台创建性能测试的方法，把当前案例的hashTree在后台转化为jmx并文件创建性能测试。
         * 然后跳转到修改性能测试的页面
         *
         * 性能测试保存地址： performance/save
         *
         */
        if (!this.environment || !this.environment) {
          this.$warning(this.$t('api_test.environment.select_environment'));
          return;
        }
        this.runData = [];
        this.singleLoading = true;
        this.row.request.name = this.row.id;
        this.row.request.useEnvironment = this.environment.id;
        this.runData.push(this.row.request);
        /*触发执行操作*/
        let testPlan = new TestPlan();
        let threadGroup = new ThreadGroup();
        threadGroup.hashTree = [];
        testPlan.hashTree = [threadGroup];
        this.runData.forEach(item => {
          threadGroup.hashTree.push(item);
        })
        let reqObj = {id: this.row.id,
          testElement: testPlan,
          type: this.type,
          name:this.row.name,
          projectId:getCurrentProjectID(),
        };

        let bodyFiles = getBodyUploadFiles(reqObj, this.runData);
        reqObj.reportId = "run";
        // let url = "/api/genPerformanceTest";
        let url = "/api/genPerformanceTestXml";

        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          let jmxObj = {};
          jmxObj.name = response.data.name;
          jmxObj.xml = response.data.xml;
          this.$store.commit('setTest', {
            name: row.name,
            jmx: jmxObj
          })
          this.$router.push({
            path: "/performance/test/create"
          })
          // let performanceId = response.data;
          // if(performanceId!=null){
          //   this.$router.push({
          //     path: "/performance/test/edit/"+performanceId,
          //   })
          // }
        }, erro => {
          this.$emit('runRefresh', {});
        });

      }
    }
  }
</script>

<style scoped>
  .scenario-ext-btn {
    margin-left: 10px;
  }
</style>
