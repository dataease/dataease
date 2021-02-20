<template>

  <common-component :title="$t('test_track.plan_view.failure_case')">
    <template>
      <el-table
        row-key="id"
        @row-click="goFailureTestCase"
        :data="failureTestCases">
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
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import PriorityTableItem from "../../../../../common/tableItems/planview/PriorityTableItem";
    import TypeTableItem from "../../../../../common/tableItems/planview/TypeTableItem";
    import MethodTableItem from "../../../../../common/tableItems/planview/MethodTableItem";
    import StatusTableItem from "../../../../../common/tableItems/planview/StatusTableItem";
    import {hub} from "@/business/components/track/plan/event-bus";
    export default {
      name: "FailureResultComponent",
      components: {StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent},
      props: {
        failureTestCases: {
          type: Array,
          default() {
            return [
              {
                name: 'testCase1',
                priority: 'P1',
                type: 'api',
                method: 'auto',
                nodePath: '/module1/module2',
                executorName: "Tom",
                status: "Failure",
                updateTime: new Date(),
              },
              {
                name: 'testCase2',
                priority: 'P0',
                type: 'functional',
                method: 'manual',
                nodePath: '/module1',
                executorName: "Micheal",
                status: "Failure",
                updateTime: new Date(),
              }
            ]
          }
        }
      },
      methods: {
        goFailureTestCase(row) {
          hub.$emit("openFailureTestCase", row);
        }
      }
    }
</script>

<style scoped>

</style>
