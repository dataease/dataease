<template>
  <ms-container>
    <ms-main-container>
      <el-card class="table-card" v-loading="result.loading">
        <template v-slot:header>
          <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="search"
                           :title="$t('commons.test')"
                           @create="create" :createTip="$t('load_test.create')"/>
        </template>

        <el-table border :data="tableData" class="adjust-table test-content"
                  @sort-change="sort"
                  @filter-change="filter"
                  @row-click="link"
        >
          <el-table-column
            prop="num"
            label="ID"
            width="100"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="name"
            :label="$t('commons.name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="projectName"
            :label="$t('load_test.project_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            prop="userName"
            sortable="custom"
            :filters="userFilters"
            column-key="user_id"
            :label="$t('load_test.user_name')"
            width="150"
            show-overflow-tooltip>
          </el-table-column>
          <el-table-column
            width="250"
            sortable
            prop="createTime"
            :label="$t('commons.create_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.createTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            width="250"
            sortable
            prop="updateTime"
            :label="$t('commons.update_time')">
            <template v-slot:default="scope">
              <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            column-key="status"
            :filters="statusFilters"
            :label="$t('commons.status')">
            <template v-slot:default="{row}">
              <ms-performance-test-status :row="row"/>
            </template>
          </el-table-column>
          <el-table-column
            width="150"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <ms-table-operators :buttons="buttons" :row="scope.row"/>
            </template>
          </el-table-column>
        </el-table>
        <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                             :total="total"/>
      </el-card>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsContainer from "../../common/components/MsContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import MsPerformanceTestStatus from "./PerformanceTestStatus";
import MsTableOperators from "../../common/components/MsTableOperators";
import {getCurrentProjectID} from "@/common/js/utils";
import MsTableHeader from "../../common/components/MsTableHeader";
import {TEST_CONFIGS} from "../../common/components/search/search-components";
import {LIST_CHANGE, PerformanceEvent} from "@/business/components/common/head/ListEvent";
import {WORKSPACE_ID} from "@/common/js/constants";
import {_filter, _sort} from "@/common/js/tableUtils";

export default {
  components: {
    MsTableHeader,
    MsPerformanceTestStatus,
    MsTablePagination,
    MsTableOperator,
    MsContainer,
    MsMainContainer,
    MsTableOperators
  },
  data() {
    return {
      result: {},
      queryPath: "/performance/list",
      deletePath: "/performance/delete",
      condition: {
        components: TEST_CONFIGS
      },
      projectId: null,
      tableData: [],
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
      testId: null,
      buttons: [
        {
          tip: this.$t('commons.edit'), icon: "el-icon-edit",
          exec: this.handleEdit
        }, {
          tip: this.$t('commons.copy'), icon: "el-icon-copy-document", type: "success",
          exec: this.handleCopy
        }, {
          tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
          exec: this.handleDelete
        }
      ],
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
      userFilters: [],
    }
  },
  watch: {
    '$route'(to) {
      this.projectId = to.params.projectId;
      this.initTableData();
    }
  },
  created: function () {
    this.projectId = this.$route.params.projectId;
    this.initTableData();
    this.getMaintainerOptions();
  },
  methods: {
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.userFilters = response.data.map(u => {
          return {text: u.name, value: u.id}
        });
      });
    },
    initTableData() {
      if (this.projectId !== 'all') {
        this.condition.projectId = this.projectId;
        if (!this.condition.projectId) {
          return;
        }
      }
      this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
        let data = response.data;
        this.total = data.itemCount;
        this.tableData = data.listObject;
      });
    },
    search(combine) {
      this.initTableData(combine);
    },
    buildPagePath(path) {
      return path + "/" + this.currentPage + "/" + this.pageSize;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleEdit(test) {
      this.$router.push({
        path: '/performance/test/edit/' + test.id,
      })
    },
    handleCopy(test) {
      this.result = this.$post("/performance/copy", {id: test.id}, () => {
        this.$success(this.$t('commons.copy_success'));
        this.search();
      });
    },
    handleDelete(test) {
      this.$alert(this.$t('load_test.delete_confirm') + test.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(test);
          }
        }
      });
    },
    _handleDelete(test) {
      let data = {
        id: test.id
      };

      this.result = this.$post(this.deletePath, data, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initTableData();
        // 发送广播，刷新 head 上的最新列表
        PerformanceEvent.$emit(LIST_CHANGE);
      });
    },
    sort(column) {
      _sort(column, this.condition);
      this.initTableData();
    },
    filter(filters) {
      _filter(filters, this.condition);
      this.initTableData();
    },
    link(row) {
      this.$router.push({
        path: '/performance/test/edit/' + row.id,
      })
    },
    create() {
      if (!getCurrentProjectID()) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      this.$router.push('/performance/test/create');
    }
  }
}
</script>

<style scoped>

.test-content {
  width: 100%;
}

.table-page {
  padding-top: 20px;
  margin-right: -9px;
  float: right;
}

.el-table {
  cursor: pointer;
}
</style>
