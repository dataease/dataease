<template>
  <div class="failure-cases-list">
    <div class="failure-cases-list-header">
      {{$t('test_track.plan.scenario_case')}}
    </div>

    <el-table
      row-key="id"
      @row-click="goFailureTestCase"
      :data="scenarioTestCases">

      <el-table-column prop="name" :label="$t('api_test.automation.scenario_name')" show-overflow-tooltip/>

      <el-table-column prop="level" :label="$t('api_test.automation.case_level')"
                       show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.level" ref="priority"/>
        </template>
      </el-table-column>

      <el-table-column prop="tags" :label="$t('api_test.automation.tag')" width="200px">
        <template v-slot:default="scope">
          <div v-for="(itemName,index)  in scope.row.tags" :key="index">
            <ms-tag type="success" effect="plain" :content="itemName"/>
          </div>
        </template>
      </el-table-column>

      <el-table-column
        prop="modulePath"
        :label="$t('test_track.case.module')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column prop="stepTotal" :label="$t('api_test.automation.step')" show-overflow-tooltip/>

      <el-table-column prop="passRate" :label="$t('api_test.automation.passing_rate')" show-overflow-tooltip/>

      <el-table-column prop="userId" :label="$t('api_test.automation.creator')" show-overflow-tooltip/>

      <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')">
        <template v-slot:default="{row}">
          <ms-tag type="danger" :content="$t('test_track.plan_view.failure')"/>
        </template>
      </el-table-column>

      <el-table-column prop="updateTime" :label="$t('api_test.automation.update_time')" width="180">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

    </el-table>
  </div>
</template>

<script>
    import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
    import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
    import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
    import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
    import MsTag from "../../../../../../../common/components/MsTag";
    export default {
      name: "ScenarioFailureCasesList",
      components: {MsTag, PriorityTableItem, TypeTableItem, MethodTableItem, StatusTableItem},
      props: ['scenarioTestCases'],
      watch: {
        scenarioTestCases() {
          this.parseTags();
        }
      },
      methods: {
        goFailureTestCase(row) {
          this.$emit("openFailureTestCase", row);
        },
        parseTags() {
          this.scenarioTestCases.forEach(item => {
            if (item.tags && item.tags.length > 0) {
              item.tags = JSON.parse(item.tags);
            }
          });
        }
      }
    }
</script>

<style scoped>

</style>
