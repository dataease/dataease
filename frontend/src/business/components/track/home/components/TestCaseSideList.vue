<template>

  <home-base-component :title="title">

    <el-table
      row-key="id"
      border
      class="adjust-table"
      @row-click="editTestCase"
      :data="tableData"
      v-loading="result.loading" height="300px">

      <el-table-column
        prop="name"
        fixed
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="priority"
        :label="$t('test_track.case.priority')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="type"
        :label="$t('test_track.case.type')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <type-table-item :value="scope.row.type"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="status"
        :label="$t('test_track.plan_view.execute_result')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="planName"
        :label="$t('test_track.plan.test_plan')"
        show-overflow-tooltip>
      </el-table-column>

    </el-table>

  </home-base-component>

</template>

<script>
  import HomeBaseComponent from "./HomeBaseComponent";
  import StatusTableItem from "../../common/tableItems/planview/StatusTableItem";
  import TypeTableItem from "../../common/tableItems/planview/TypeTableItem";
  import PriorityTableItem from "../../common/tableItems/planview/PriorityTableItem";
  import {getCurrentProjectID} from "../../../../../common/js/utils";
  export default {
    name: "TestCaseSideList",
    components: {PriorityTableItem, TypeTableItem, StatusTableItem, HomeBaseComponent},
    data() {
      return {
        result: {},
        tableData: [],
      }
    },
    props: {
      title: {
        type: String
      },
    },
    mounted() {
      this.initTableData();
    },
    methods: {
      initTableData() {
        if (!getCurrentProjectID()) {
          return;
        }
        this.result = this.$post('/test/plan/case/recent/10', {}, response => {
          this.tableData = response.data;
        });
      },
      editTestCase(row, event, column) {
        this.$router.push('/track/plan/view/edit/' + row.id)
      }
    }
  }
</script>

<style scoped>

</style>
