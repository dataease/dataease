<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.failed_case_list.title')}}
      </span>
    </template>
    <el-table border :data="tableData" class="adjust-table table-content" height="300px">
      <el-table-column prop="sortIndex"  :label="$t('api_test.home_page.failed_case_list.table_coloum.index')" width="100" show-overflow-tooltip/>
      <el-table-column prop="caseName"  :label="$t('api_test.home_page.failed_case_list.table_coloum.case_name')" width="150">
        <template v-slot:default="{row}">
            <el-link type="info" @click="redirect(row.caseType,row.caseID)">
              {{ row.caseName }}
            </el-link>
        </template>
      </el-table-column>
      <el-table-column
        prop="caseType"
        column-key="caseType"
        :label="$t('api_test.home_page.failed_case_list.table_coloum.case_type')"
        width="150"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <ms-tag v-if="scope.row.caseType == 'apiCase'" type="success" effect="plain" :content="$t('api_test.home_page.failed_case_list.table_value.case_type.api')"/>
          <ms-tag v-if="scope.row.caseType == 'scenario'" type="warning" effect="plain" :content="$t('api_test.home_page.failed_case_list.table_value.case_type.scene')"/>
        </template>
      </el-table-column>
      <el-table-column prop="testPlan"  :label="$t('api_test.home_page.failed_case_list.table_coloum.test_plan')">
        <template v-slot:default="{row}">
          <div v-for="(testPlan, index) in row.testPlanDTOList" :key="index">
            <el-link type="info" @click="redirect('testPlanEdit',testPlan.id)">
              {{ testPlan.name }};
            </el-link>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="failureTimes"  :label="$t('api_test.home_page.failed_case_list.table_coloum.failure_times')" width="100" show-overflow-tooltip/>
    </el-table>
  </el-card>
</template>

<script>
import {getCurrentProjectID} from "@/common/js/utils";
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "MsFailureTestCaseList",

  components: {
    MsTag
  },

  data() {
    return {
      result: {},
      tableData: [],
      loading: false
    }
  },

  methods: {
    search() {
      let projectID = getCurrentProjectID();
      this.result = this.$get("/api/faliureCaseAboutTestPlan/"+projectID+"/10", response => {
        this.tableData = response.data;
      });
    },
    redirect(pageType,param){
      switch (pageType){
        case "testPlanEdit":
          this.$emit('redirectPage','testPlanEdit',null, param);
          break;
        case "apiCase":
          this.$emit('redirectPage','api','apiTestCase', 'single:'+param);
          break;
        case "scenario":
          this.$emit('redirectPage','scenario','scenario', 'edit:'+param);
          break;
      }
    }
  },


  created() {
    this.search();
  },
  activated() {
    this.search();
  }
}
</script>

<style scoped>

.el-table {
  cursor:pointer;
}
.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

</style>
