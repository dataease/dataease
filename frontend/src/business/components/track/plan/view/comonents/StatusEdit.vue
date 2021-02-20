<template>

  <el-dialog :title="$t('test_track.plan_view.change_execution_results')"
             :visible.sync="statusEditVisible"
             width="30%">

    <test-plan-test-case-status-button
      @statusChange="statusChange"
      :status="status"/>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="statusEditVisible = false"
        @confirm="saveStatus"/>
    </template>

  </el-dialog>

</template>

<script>
  import TestPlanTestCaseStatusButton from '../../common/TestPlanTestCaseStatusButton';
  import MsDialogFooter from '../../../../common/components/MsDialogFooter'

    export default {
      name: "statusEdit",
      components: {TestPlanTestCaseStatusButton, MsDialogFooter},
      data() {
        return {
          statusEditVisible: false,
          status: ''
        }
      },
      props: {
        selectIds: {
          type: Set
        },
        planId: {
          type: String
        }
      },
      methods: {
        statusChange(status) {
          this.status = status;
        },
        saveStatus() {
          let param = {};
          if (this.status === '') {
            this.$warning(this.$t('test_track.plan_view.select_execute_result'));
            return;
          }
          param.status = this.status;
          param.ids = [...this.selectIds];
          this.$post('/test/plan/case/batch/edit' , param, () => {
            this.selectIds.clear();
            this.status = '';
            this.$post('/test/plan/edit/status/' + this.planId);
            this.$success(this.$t('commons.save_success'));
            this.statusEditVisible = false;
            this.$emit('refresh');
          });
        },
        openStatusEdit() {
          this.statusEditVisible = true;
        }
      }
    }
</script>

<style scoped>

</style>
