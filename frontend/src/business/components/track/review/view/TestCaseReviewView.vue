<template>
  <ms-container>

    <ms-aside-container>
      <select-menu
        :data="testReviews"
        :current-data="currentReview"
        :title="$t('test_track.review_view.review')"
        @dataChange="changeReview"/>
      <node-tree class="node-tree"
                 :all-label="$t('commons.all_label.review')"
                 v-loading="result.loading"
                 @nodeSelectEvent="nodeChange"
                 :tree-nodes="treeNodes"
                 ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <test-review-test-case-list
        class="table-list"
        @openTestReviewRelevanceDialog="openTestReviewRelevanceDialog"
        @refresh="refresh"
        :review-id="reviewId"
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        ref="testPlanTestCaseList"/>
    </ms-main-container>

    <test-review-relevance
      @refresh="refresh"
      :review-id="reviewId"
      ref="testReviewRelevance"/>

  </ms-container>
</template>

<script>


import MsMainContainer from "../../../common/components/MsMainContainer";
import MsAsideContainer from "../../../common/components/MsAsideContainer";
import MsContainer from "../../../common/components/MsContainer";
import NodeTree from "../../common/NodeTree";
import TestReviewTestCaseList from "./components/TestReviewTestCaseList";
import SelectMenu from "../../common/SelectMenu";
import TestReviewRelevance from "./components/TestReviewRelevance";

export default {
  name: "TestCaseReviewView",
  components: {
    MsMainContainer,
    MsAsideContainer,
    MsContainer,
    NodeTree,
    TestReviewTestCaseList,
    TestReviewRelevance,
    SelectMenu
  },
  data() {
    return {
      result: {},
      testReviews: [],
      currentReview: {},
      selectNodeIds: [],
      selectParentNodes: [],
      treeNodes: []
    }
  },
  computed: {
    reviewId: function () {
      return this.$route.params.reviewId;
    }
  },
  mounted() {
    this.initData();
    this.openTestCaseEdit(this.$route.path);
  },
  watch: {
    '$route'(to, from) {
      this.openTestCaseEdit(to.path);
    },
    reviewId() {
      this.initData();
    }
  },
  methods: {
    refresh() {
      this.selectNodeIds = [];
      this.selectParentNodes = [];
      this.$refs.testReviewRelevance.search();
      this.getNodeTreeByReviewId();
    },
    initData() {
      this.getTestReviews();
      this.getNodeTreeByReviewId();
    },
    openTestReviewRelevanceDialog() {
      this.$refs.testReviewRelevance.openTestReviewRelevanceDialog();
    },
    getTestReviews() {
      this.result = this.$post('/test/case/review/list/all', {}, response => {
        this.testReviews = response.data;
        this.testReviews.forEach(review => {
          if (this.reviewId && review.id === this.reviewId) {
            this.currentReview = review;
          }
        });
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.selectParentNodes = pNodes;
    },
    changeReview(review) {
      this.currentReview = review;
      this.$router.push('/track/review/view/' + review.id);
    },
    getNodeTreeByReviewId() {
      if (this.reviewId) {
        this.result = this.$get("/case/node/list/review/" + this.reviewId, response => {
          this.treeNodes = response.data;
        });
      }
    },
    openTestCaseEdit(path) {
      if (path.indexOf("/review/view/edit") >= 0) {
        let caseId = this.$route.params.caseId;
        this.$get('/test/review/case/get/' + caseId, response => {
          let testCase = response.data;
          if (testCase) {
            this.$refs.testPlanTestCaseList.handleEdit(testCase);
            this.$router.push('/track/review/view/' + testCase.reviewId);
          }
        });
      }
    }
  }
}
</script>

<style scoped>

</style>
