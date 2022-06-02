<template>
  <div class="portal-container">
    <div class="header">
      <el-button
        plain
        icon="el-icon-circle-plus-outline"
        @click="handleAddPortalDrawer"
        >新建数据门户</el-button
      >
    </div>
    <div class="content">
      <el-table :data="list">
        <el-table-column label="名称" prop="portalName"></el-table-column>
        <el-table-column label="创建者" prop="userName"></el-table-column>
        <el-table-column label="修改人" prop="updateBy"></el-table-column>
        <el-table-column label="修改时间">
          <template slot-scope="{ row }">
            {{ row.createTime || row.updateTime }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="{ row }">
            <div class="table-option">
              <i class="el-icon-edit"></i>
              <i class="el-icon-table-lamp"></i>
              <i class="el-icon-delete" @click="handleDeleteRow(row)"></i>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :current-page="pageValue.page_number"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageValue.page_size"
        layout="prev, pager, next"
        :total="total"
        @current-change="handlePageNumberChange"
        @size-change="handlePageSizeChange"
      />
    </div>
    <div class="bottom"></div>
    <div class="other">
      <PortalDrawerComponent
        :open-type="openType"
        :visible.sync="showPortalDrawer"
      ></PortalDrawerComponent>
    </div>
  </div>
</template>

<script>
import PortalDrawerComponent from "./components/PortalDrawerComponent.vue";
import { getPortalList, deletePortal } from "@/api/panel/portal";
export default {
  components: {
    PortalDrawerComponent,
  },
  data() {
    return {
      list: [],
      total: 0,
      showPortalDrawer: false,
      openType: "add", // edit
      pageValue: {
        page_number: 1,
        page_size: 10,
      },
    };
  },

  mounted() {
    this.handleGetPortalList()
  },

  methods: {
    handleAddPortalDrawer() {
      this.openType = "add";
      this.showPortalDrawer = true;
    },

    // 获取站点列表
    handleGetPortalList() {
      getPortalList({ ...this.pageValue })
        .then((res) => {
          if (res.success) {
            this.list = res.data.portalDataList.map((item) => {
              return {
                ...item,
                portalName: JSON.parse(item.positionJson).portalName,
              };
            });
            this.total = res.data.total;
          }
        })
        .catch((err) => {
          console.log("err: ", err);
        });
    },

    // 删除一个站点
    handleDeleteRow(row) {
      deletePortal(row.id)
        .then((res) => {
          if (res.success) {
            this.$message.success("操作成功");
            this.handleGetPortalList()
          }
        })
        .catch((err) => {
          console.log("deletePortal err ", err);
        });
    },

    handlePageNumberChange() {},

    handlePageSizeChange() {},
  },
};
</script>

<style lang="scss" scoped>
.portal-container {
  padding: 10px;
  box-sizing: border-box;
  .header {
    margin-bottom: 10px;
  }
  .content {
    .table-option {
      i {
        cursor: pointer;
        margin-right: 10px;
        font-size: 20px;
      }
      i:last-child {
        margin-right: 0;
      }
    }
  }
}
</style>
