<template>

  <common-component :title="$t('test_track.plan_view.test_result')">
    <template>
      <el-table
        border
        class="adjust-table"
        :data="testResults"
        stripe
        style="width: 100%">
        <el-table-column
          prop="moduleName"
          :label="$t('test_track.module.module')"
         >
        </el-table-column>
        <el-table-column
          prop="projectName"
          :label="$t('test_track.module.project_name')"
          show-overflow-tooltip
         >
        </el-table-column>
        <el-table-column
          prop="caseCount"
          :label="$t('test_track.plan_view.case_count')"
        >
        </el-table-column>
        <el-table-column
          prop="passCount"
          :label="$t('test_track.plan_view.pass')"
        >
        </el-table-column>
        <el-table-column
          prop="failureCount"
          :label="$t('test_track.plan_view.failure')"
        >
        </el-table-column>
        <el-table-column
          prop="blockingCount"
          :label="$t('test_track.plan_view.blocking')"
        >
        </el-table-column>
        <el-table-column
          prop="skipCount"
          :label="$t('test_track.plan_view.skip')"
        >
        </el-table-column>
        <el-table-column
          prop="underwayCount"
          :label="$t('test_track.plan.plan_status_running')"
        >
        </el-table-column>
        <el-table-column
          prop="prepareCount"
          :label="$t('test_track.plan.plan_status_prepare')"
          width="102">
        </el-table-column>
        <el-table-column
          prop="passRate"
          :label="$t('test_track.pass_rate')"
        >
          <template v-slot:default="scope">
            {{scope.row.passRate}}%
          </template>
        </el-table-column>
        <el-table-column
          prop="issuesCount"
          :label="$t('test_track.plan_view.issues_count')"
        >
        </el-table-column>
      </el-table>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    export default {
      name: "TestResultComponent",
      components: {CommonComponent},
      props: {
        testResults: {
          type: Array,
          default() {
            return [
              {
                moduleName: this.$t('test_track.module.module') + '1',
                caseCount: '14',
                passRate: 10.8,
                issuesCount: 3,
                passCount:0,
                failureCount:0,
                blockingCount:0,
                skipCount:0,
                underwayCount:0,
                prepareCount:0
              },
              {
                moduleName: this.$t('test_track.module.module') + '1',
                caseCount: '14',
                passRate: 10.8,
                issuesCount: 3,
                passCount:0,
                failureCount:0,
                blockingCount:0,
                skipCount:0,
                underwayCount:0,
                prepareCount:0
              },
              {
                moduleName: this.$t('test_track.module.module') + '1',
                caseCount: '14',
                passRate: 10.8,
                issuesCount: 3,
                passCount:0,
                failureCount:0,
                blockingCount:0,
                skipCount:0,
                underwayCount:0,
                prepareCount:0
              }
            ]
          }
        }
      }
    }
</script>

<style scoped>

</style>
