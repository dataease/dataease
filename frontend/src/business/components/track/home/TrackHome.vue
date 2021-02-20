<template>
  <ms-container>
    <ms-main-container>
      <el-row :gutter="20">
        <el-col :span="15">
          <el-row>
            <related-test-plan-list ref="relatedTestPlanList"/>
          </el-row>
          <el-row>
            <review-list :title="$t('test_track.review.my_review')" ref="caseReviewList"/>
          </el-row>
        </el-col>
        <el-col :span="9">
          <test-case-side-list :title="$t('test_track.home.recent_test')" ref="testCaseRecentList"/>
        </el-col>
      </el-row>
    </ms-main-container>
  </ms-container>
</template>

<script>
import RelatedTestPlanList from "./components/RelatedTestPlanList";
import TestCaseSideList from "./components/TestCaseSideList";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import ReviewList from "./components/ReviewList";

export default {
  name: "TrackHome",
  components: {MsMainContainer, MsContainer, TestCaseSideList, RelatedTestPlanList, ReviewList},
  watch: {
    '$route'(to, from) {
      if (to.path.indexOf('/track/home') > -1) {
        this.innitData();
      }
    }
  },
  methods: {
    innitData() {
      this.$refs.relatedTestPlanList.initTableData();
      this.$refs.testCaseRecentList.initTableData();
      this.$refs.caseReviewList.initTableData();
    }
  }
}
</script>

<style scoped>

.ms-main-container >>> .el-table {
  cursor: pointer;
}

.el-row {
  padding-bottom: 20px;
}
</style>
