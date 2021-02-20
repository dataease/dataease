<template>
  <div class="failure-cases-list">
    <div class="failure-cases-list-header">
      功能测试用例
    </div>

    <el-table
      row-key="id"
      @row-click="goFailureTestCase"
      :data="functionalTestCases">

      <el-table-column
        prop="num"
        :label="$t('commons.id')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="priority"
        column-key="priority"
        :label="$t('test_track.case.priority')">
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="type"
        column-key="type"
        :label="$t('test_track.case.type')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <type-table-item :value="scope.row.type"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="method"
        column-key="method"
        :label="$t('test_track.case.method')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <method-table-item :value="scope.row.method"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="nodePath"
        :label="$t('test_track.case.module')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="$t('test_track.case.project_name')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="executorName"
        :label="$t('test_track.plan_view.executor')">
      </el-table-column>

      <el-table-column
        prop="status"
        column-key="status"
        :label="$t('test_track.plan_view.execute_result')">
        <template v-slot:default="scope">
          <status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="updateTime"
        :label="$t('api_test.automation.update_time')"
        show-overflow-tooltip>
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
    export default {
      name: "FunctionalFailureCasesList",
      components: {PriorityTableItem, TypeTableItem, MethodTableItem, StatusTableItem},
      props: ['functionalTestCases'],
      methods: {
        goFailureTestCase(row) {
          this.$emit("openFailureTestCase", row);
        }
      }
    }
</script>

<style scoped>

</style>
