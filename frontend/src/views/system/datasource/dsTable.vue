<template>
  <div class="ds-table de-serach-table">
    <el-row class="top-operate">
      <el-col :span="10">
        <span class="table-name">{{ params.name }}</span>
      </el-col>
      <el-col :span="14" class="right-user">
        <el-input
          :placeholder="$t('system_parameter_setting.search_keywords')"
          prefix-icon="el-icon-search"
          class="name-email-search"
          size="small"
          clearable
          ref="search"
          v-model="nikeName"
          @blur="initSearch"
          @clear="initSearch"
        >
        </el-input>
      </el-col>
    </el-row>
    <div class="table-container">
      <grid-table
        v-loading="loading"
        :tableData="tableData"
        :columns="[]"
        :pagination="paginationConfig"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column
          key="name"
          prop="name"
          :label="$t('datasource.table_name')"
        />
        <el-table-column
          slot="__operation"
          :label="$t('commons.operating')"
          key="__operation"
          fixed="right"
          width="168"
        >
          <template slot-scope="scope">
            <el-button
              @click="createtDataset(scope.row)"
              class="text-btn mar3 mar6"
              type="text"
              >{{ $t("datasource.create_dataset") }}</el-button
            >
            <el-button
              @click="selectDataset(scope.row)"
              class="text-btn"
              type="text"
              >{{ $t("dataset.detail") }}</el-button
            >
          </template>
        </el-table-column>
      </grid-table>
    </div>
    <el-drawer
      :title="$t('dataset.detail')"
      :visible.sync="userDrawer"
      custom-class="user-drawer-task ds-table-drawer"
      size="840px"
      v-closePress
      direction="rtl"
    >
      <el-row style="margin-top: 12px" :gutter="24">
        <el-col :span="12">
          <p class="table-name">
            {{ $t("datasource.table_name") }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.name }}
          </p>
        </el-col>
        <el-col :span="12">
          <p class="table-name">
            {{ $t("datasource.table_description") }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.remark || "-" }}
          </p>
        </el-col>
      </el-row>
      <el-table
    :data="dsTableData"
    stripe
    style="width: 100%">
    <el-table-column
      prop="date"
      :label="$t('panel.column_name')">
    </el-table-column>
    <el-table-column
      prop="name"
      :label="$t('dataset.field_type')">
    </el-table-column>
    <el-table-column
      prop="name"
      :label="$t('datasource.field_description')">
    </el-table-column>
  </el-table>
    </el-drawer>
  </div>
</template>

<script>
import keyEnter from "@/components/msgCfm/keyEnter.js";
import GridTable from "@/components/gridTable/index.vue";
import { dsTable } from "@/api/dataset/dataset";
export default {
  mixins: [keyEnter],
  components: { GridTable },
  props: {
    params: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      userDrawer: false,
      dsTableDetail: {},
      nikeName: "",
      loading: false,
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      dsTableData: [{date: 1}],
      tableData: [{ name: 1 }],
    };
  },
  created() {
    // this.initSearch();
  },
  methods: {
    createtDataset(row) {},
    selectDataset(row) {
      this.dsTableDetail = row;
      this.userDrawer = true;
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1;
      this.paginationConfig.pageSize = pageSize;
      this.search();
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage;
      this.search();
    },
    initSearch() {
      this.handleCurrentChange(1);
    },
    search() {
      this.loading = true;
      const param = {
        conditions: [],
      };
      if (this.nikeName) {
        param.conditions.push({
          field: `dataset_table_task.name`,
          operator: "like",
          value: this.nikeName,
        });
      }
      const { currentPage, pageSize } = this.paginationConfig;
      dsTable(currentPage, pageSize, this.params.id).then((response) => {
        this.tableData = response.data.listObject;
        this.paginationConfig.total = response.data.itemCount;
        this.loading = false;
      });
    },
  },
};
</script>

<style lang="scss">
.ds-table-drawer {
  .table-value,
  .table-name {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    margin: 0;
  }
  .table-name {
    color: var(--deTextSecondary, #646a73);
  }
  .table-value {
    margin: 4px 0 24px 0;
    color: var(--deTextPrimary, #1f2329);
  }
}
.ds-table {
  height: 100%;
  padding: 10px 14px;
  box-sizing: border-box;
  .table-name {
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: var(--deTextPrimary, #1f2329);
  }
  .table-container {
    height: calc(100% - 50px);
  }
  .el-table__fixed-right::before {
    background: transparent;
  }
}
</style>