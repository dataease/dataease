<template>
  <div class="ds-table de-serach-table">
    <el-row class="top-operate">
      <el-col :span="10">
        <span class="table-name-top">{{ params.name }}</span>
      </el-col>
      <el-col :span="14" class="right-user">
        <el-input
          ref="search"
          v-model="nickName"
          :placeholder="$t('system_parameter_setting.search_keywords')"
          prefix-icon="el-icon-search"
          class="name-email-search"
          size="small"
          clearable
          @blur="initSearch"
          @clear="initSearch"
        />
      </el-col>
    </el-row>
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="filterTable"
        :style="{ width: '100%' }"
      >
        <el-table-column
          key="name"
          prop="name"
          :label="$t('datasource.table_name')"
        />
        <el-table-column
          :label="$t('commons.operating')"
          fixed="right"
          width="108"
        >
          <template slot-scope="scope">
            <el-button
              class="de-text-btn mar3"
              type="text"
              @click="selectDataset(scope.row)"
            >{{ $t("dataset.detail") }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog
      :title="$t('dataset.detail')"
      :visible.sync="userDrawer"
      class="de-dialog-form ds-table-drawer"
      width="840px"
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
        style="width: 100%"
      >
        <el-table-column
          prop="fieldName"
          :label="$t('panel.column_name')"
        />
        <el-table-column
          prop="fieldType"
          :label="$t('dataset.field_type')"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.fieldType === '0' && params.type==='api'">{{
              $t("dataset.text")
            }}</span>
            <span v-if="scope.row.fieldType === '2' && params.type==='api'">{{
              $t("dataset.value")
            }}</span>
            <span v-if="scope.row.fieldType === '3' && params.type==='api'">{{
              $t("dataset.value") + '(' + $t("dataset.float") + ')'
            }}</span>
            <span v-if="params.type !=='api'">
              {{ scope.row.fieldType }}
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="remarks"
          :label="$t('datasource.field_description')"
        />
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import keyEnter from '@/components/msgCfm/keyEnter.js'
import { dsTable, post } from '@/api/dataset/dataset'

export default {
  mixins: [keyEnter],
  props: {
    params: {
      type: Object,
      default: () => {
      }
    }
  },
  data() {
    return {
      userDrawer: false,
      dsTableDetail: {},
      nickName: '',
      loading: false,
      dsTableData: [],
      tableData: [],
      filterTable: []
    }
  },
  created() {
    this.search()
  },
  methods: {
    initSearch() {
      this.filterTable = this.tableData.filter(ele => ele.name.includes(this.nickName))
    },
    selectDataset(row) {
      this.dsTableDetail = row
      this.userDrawer = true
      var table = { dataSourceId: this.params.id }
      table.info = JSON.stringify({ table: row.name })
      post('/dataset/table/getFields', table).then((response) => {
        this.dsTableData = response.data
      })
    },
    search() {
      this.loading = true
      post('/datasource/getTables/' + this.params.id, {}).then((response) => {
        this.tableData = response.data
        this.initSearch()
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
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

  .mar3 {
    margin-left: -5px;
  }

  .table-name-top {
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
