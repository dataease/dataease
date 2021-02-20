<template>
  <el-table border :data="tableData" class="adjust-table table-content" height="300px">
    <el-table-column prop="index" :label="$t('api_test.home_page.running_task_list.table_coloum.index')"
                     width="50"
                     show-overflow-tooltip/>
    <el-table-column prop="SwaggerUrlId">
    </el-table-column>
    <el-table-column prop="swaggerUrl" :label="$t('swaggerUrl')" width="100" show-overflow-tooltip>
    </el-table-column>
    <el-table-column prop="modulePath" :label="$t('导入模块')"
                     width="100" show-overflow-tooltip/>
    <el-table-column prop="rule" label="同步规则" width="120"
                     show-overflow-tooltip/>
    <el-table-column width="100" :label="$t('api_test.home_page.running_task_list.table_coloum.task_status')">
      <template v-slot:default="scope">
        <div>
          <el-switch
            v-model="scope.row.taskStatus"
            class="captcha-img"
            @click.native="closeTaskConfirm(scope.row)"
          ></el-switch>
        </div>
      </template>
    </el-table-column>
    <el-table-column width="170" :label="$t('api_test.home_page.running_task_list.table_coloum.next_execution_time')">
      <template v-slot:default="scope">
        <span>{{ scope.row.nextExecutionTime | timestampFormatDate }}</span>
      </template>
    </el-table-column>
    <el-table-column width="100" label="操作">
      <template v-slot:default="scope">
        <el-button
          type="danger"
          icon="el-icon-delete"
          size="mini"
          @click.native.prevent="deleteRowTask(scope.row)"
        ></el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
import {getCurrentProjectID} from "../../../../../../common/js/utils";

export default {
  name: "SwaggerTaskList",
  data() {
    return {
      tableData: [],
    }
  },
  methods: {
    search() {
      let projectId = getCurrentProjectID();
      this.result = this.$get("/api/definition/scheduleTask/" + projectId, response => {
        this.tableData = response.data;
      })
    },
    closeTaskConfirm(row) {
      let flag = row.taskStatus;
      row.taskStatus = !flag;
      if (row.taskStatus) {
        this.$confirm(this.$t('api_test.home_page.running_task_list.confirm.close_title'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          row.taskStatus = !row.taskStatus
          this.updateTask(row);
        }).catch(() => {
        });
      } else {
        row.taskStatus = !row.taskStatus
        this.updateTask(row);
      }

    },
    updateTask(taskRow) {
      this.result = this.$post('/api/definition/schedule/updateByPrimyKey', taskRow, response => {
        this.search();
      });
    },
    deleteRowTask(row) {
      this.result = this.$post('/api/definition/schedule/deleteByPrimyKey', row, response => {
        this.search();
      });
    }

  },
  mounted() {
    this.search()
  },
}
</script>


<style scoped>

</style>
