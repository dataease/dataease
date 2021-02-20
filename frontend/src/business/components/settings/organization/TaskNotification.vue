<template>
  <div>
    <el-alert
      title="Notice:"
      type="info"
      show-icon>
      <template v-slot:default>
        {{ $t('organization.message.notes') }}
      </template>
    </el-alert>
    <jenkins-notification :jenkins-receiver-options="jenkinsReceiverOptions"/>
    <test-plan-task-notification :test-plan-receiver-options="testPlanReceiverOptions"/>
    <test-review-notification :review-receiver-options="reviewReceiverOptions"/>
    <defect-task-notification :defect-receiver-options="defectReceiverOptions"/>
  </div>
</template>

<script>
import {getCurrentUser} from "@/common/js/utils";
import JenkinsNotification from "@/business/components/settings/organization/components/JenkinsNotification";
import TestPlanTaskNotification from "@/business/components/settings/organization/components/TestPlanTaskNotification";
import TestReviewNotification from "@/business/components/settings/organization/components/TestReviewNotification";
import DefectTaskNotification from "@/business/components/settings/organization/components/DefectTaskNotification";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "TaskNotification",
  components: {
    DefectTaskNotification, TestReviewNotification, TestPlanTaskNotification, JenkinsNotification, MsContainer,
    MsMainContainer,
  },
  data() {

    return {
      jenkinsReceiverOptions: [],
      //测试计划
      testPlanReceiverOptions: [],
      //评审
      reviewReceiverOptions: [],
      //缺陷
      defectReceiverOptions: [],
    }
  },

  activated() {
    this.initUserList();
  },
  methods: {
    handleEdit(index, data) {
      data.isReadOnly = true;
      if (data.type === 'EMAIL') {
        data.isReadOnly = !data.isReadOnly
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },

    initUserList() {
      let param = {
        name: '',
        organizationId: this.currentUser().lastOrganizationId
      };
      this.result = this.$post('user/org/member/list/all', param, response => {
        this.jenkinsReceiverOptions = response.data
        this.reviewReceiverOptions = response.data
        this.defectReceiverOptions = response.data
        this.testPlanReceiverOptions = response.data
      });
    }
  }
}
</script>

<style scoped>

</style>
