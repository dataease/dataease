<template>
  <home-base-component :title="$t('test_track.review.my_review')" v-loading>
    <template slot="header-area">
      <div style="float: right">
        <ms-table-button :is-tester-permission="true" v-if="!showMyCreator" icon="el-icon-view"
                         :content="$t('test_track.review.my_create')" @click="searchMyCreator"/>
        <ms-table-button :is-tester-permission="true" v-if="showMyCreator" icon="el-icon-view"
                         :content="$t('test_track.review.reviewed_by_me')" @click="searchMyCreator"/>
      </div>

    </template>
    <el-table
      class="adjust-table"
      border
      :data="tableData"
      @row-click="intoPlan"
      v-loading="result.loading" height="300px">
      <el-table-column
        prop="name"
        fixed
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="creator"
        fixed
        :label="$t('test_track.review.creator')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="reviewerName"
        fixed
        :label="$t('test_track.review.reviewer')"
        show-overflow-tooltip>
      </el-table-column>

      <el-table-column
        prop="status"
        :label="$t('test_track.plan.plan_status')">
        <template v-slot:default="scope">
          <plan-status-table-item :value="scope.row.status"/>
        </template>
      </el-table-column>

      <el-table-column
        :label="$t('test_track.review.result_distribution')">
        <template v-slot:default="scope">
          <el-tooltip :content="getResultTip(scope.row.total,scope.row.reviewed,scope.row.pass)"
                      placement="top" :enterable="false" class="item" effect="dark">
            <yan-progress :total="scope.row.total" :done="scope.row.reviewed" :modify="scope.row.pass" :tip="tip"/>
          </el-tooltip>
        </template>
      </el-table-column>

<!--      <el-table-column-->
<!--        prop="projectName"-->
<!--        :label="$t('test_track.plan.plan_project')"-->
<!--        show-overflow-tooltip>-->
<!--      </el-table-column>-->

    </el-table>

  </home-base-component>
</template>

<script>
import MsTableOperator from "../../../common/components/MsTableOperator";
import PlanStageTableItem from "../../common/tableItems/plan/PlanStageTableItem";
import PlanStatusTableItem from "../../common/tableItems/plan/PlanStatusTableItem";
import HomeBaseComponent from "./HomeBaseComponent";
import MsTableButton from "../../../common/components/MsTableButton";
import {getCurrentProjectID} from "../../../../../common/js/utils";

export default {
  name: "ReviewList",
  components: {MsTableOperator, PlanStageTableItem, PlanStatusTableItem, HomeBaseComponent, MsTableButton},
  data() {
    return {
      result: {},
      tableData: [],
      showMyCreator: false,
      tip: [
        {text: "X", fillStyle: '#D3D3D3'},
        {text: "X", fillStyle: '#ee4545'},
        {text: "X", fillStyle: '#4dcf4d'}
      ]
    }
  },
  mounted() {
    this.initTableData('');
  },
  methods: {
    initTableData(type) {
      if (!type) {
        type = 'reviewer'
      }
      if (!getCurrentProjectID()) {
        return;
      }
      this.result = this.$get('/test/case/review/list/all/relate/' + type, response => {
        this.tableData = response.data;
      });
    },
    intoPlan(row) {
      this.$router.push('/track/review/view/' + row.id);
    },
    searchMyCreator() {
      this.showMyCreator = !this.showMyCreator;
      if (this.showMyCreator) {
        this.initTableData("creator");
      } else {
        this.initTableData("reviewer");
      }
    },
    getResultTip(total, reviewed, pass) {
      return '通过: ' + pass + '; ' + '未通过: ' + (reviewed - pass) + '; ' + '未评审: ' + (total - reviewed);
    }
  }
}
</script>

<style scoped>

</style>
