<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">{{$t('schedule.running_task')}}</span>
    </template>
    <el-table height="289" border :data="tableData" class="adjust-table table-content" @row-click="link">
      <el-table-column prop="resourceName" :label="$t('schedule.test_name')" width="150" show-overflow-tooltip/>
      <el-table-column prop="value" :label="$t('schedule.running_rule')" width="150" show-overflow-tooltip/>
      <el-table-column width="100px" :label="$t('schedule.job_status')">
        <template v-slot:default="scope">
          <el-switch @click.stop.native v-model="scope.row.enable" @change="update(scope.row)"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('schedule.next_execution_time')">
        <template v-slot:default="scope">
          <crontab-result :enable-simple-mode="true" :ex="scope.row.value" ref="crontabResult"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('report.user_name')" prop="userName"/>
      <el-table-column :label="$t('commons.update_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script>
  import {getCurrentWorkspaceId} from "../../../../common/js/utils";
  import CrontabResult from "../../common/cron/CrontabResult";
  import {SCHEDULE_TYPE} from "../../../../common/js/constants";

  export default {
    name: "MsScheduleList",
    components: {CrontabResult},
    data() {
      return {
        result: {},
        tableData: [],
        loading: false,
        operators: {}
      }
    },
    props: {
      group: String
    },
    watch: {
      group() {
        this.init();
      }
    },
    methods: {
      search() {
        if (this.operators.listUrl) {
          this.result = this.$post(this.operators.listUrl, {
            workspaceId: getCurrentWorkspaceId(),
            group: this.group
          }, response => {
            this.tableData = response.data;
          });
        }
      },
      link(row) {
        this.$router.push({
          path: this.operators.linkUrl + row.resourceId,
        })
      },
      update(schedule) {
        this.result = this.$post(this.operators.updateUrl, schedule, response => {
          this.search();
        });
      },
      init() {
        switch (this.group) {
          case SCHEDULE_TYPE.API_TEST:
            this.operators.listUrl = '/api/list/schedule';
            this.operators.updateUrl = '/api/schedule/update';
            this.operators.linkUrl = '/api/test/edit?id=';
            break;
          case SCHEDULE_TYPE.PERFORMANCE_TEST:
            this.operators.listUrl = '/performance/list/schedule';
            this.operators.updateUrl = '/performance/schedule/update';
            this.operators.linkUrl = '/performance/test/edit/';
            break;
          default:
            break;
        }
        this.search();
      }
    },

    created() {
      this.init();
    },
    activated() {
      this.init();
    }
  }
</script>

<style scoped>
  .el-table {
    cursor: pointer;
  }
</style>
