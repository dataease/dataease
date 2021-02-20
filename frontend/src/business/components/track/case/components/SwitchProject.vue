<template>
  <el-dialog v-loading="result.loading"
             :visible.sync="dialogVisible"
             :close-on-click-modal="false"
             class="ms-switch-project"
  >
    <ms-table-header :condition.sync="condition" @search="initData" :title="$t('test_track.switch_project')" :show-create="false"/>
    <el-table
            :data="tableData"
            highlight-current-row
            @current-change="handleCurrentChange"
            style="width: 100%">
      <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
      <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip>
        <template v-slot:default="scope">
          {{ scope.row.description }}
        </template>
      </el-table-column>
      <el-table-column
              prop="createTime"
              :label="$t('commons.create_time')"
              show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
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
    <ms-table-pagination :change="initData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <template v-slot:footer>
      <div class="dialog-footer">
        <ms-dialog-footer
                @cancel="dialogVisible = false"
                @confirm="submit()"/>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "../../../common/components/MsDialogFooter";
import MsTableHeader from "../../../common/components/MsTableHeader";
import MsTablePagination from "../../../common/pagination/TablePagination";

export default {
  name: "SwitchProject",
  components: {MsDialogFooter, MsTableHeader, MsTablePagination},
  data() {
    return {
      tableData: [],
      result: {},
      dialogVisible: false,
      projectId: '',
      id: '',
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      url: '',
      type: ''
    }
  },
  methods: {
    open(obj) {
      this.dialogVisible = true;
      this.id = obj.id;
      this.url = obj.url;
      this.type = obj.type;
      this.initData();
    },
    initData() {
      if (this.type === 'plan') {
        this.condition.planId = this.id;
      } else {
        this.condition.reviewId = this.id;
      }
      this.result = this.$post(this.url + this.currentPage + "/" + this.pageSize, this.condition, res => {
        const data = res.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      })
    },
    handleCurrentChange(currentRow) {
      // initData 改变表格数据会触发此方法
      if (currentRow) {
        this.projectId = currentRow.id;
      }
    },
    submit() {
      this.$emit('getProjectNode', this.projectId);
      this.dialogVisible = false;
    }
  }
}
</script>

<style scoped>
  .ms-switch-project >>> .el-dialog__body {
    padding: 0 15px !important;
  }
</style>
