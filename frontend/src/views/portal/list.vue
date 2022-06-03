<template>
  <div class="portal-container">
    <div class="portal-container-table">
      <div class="header">
        <el-button
          plain
          icon="el-icon-circle-plus-outline"
          @click="handleAddPortalDrawer"
          >新建数据门户</el-button
        >
      </div>
      <div class="content">
        <el-table :data="list" v-loading="previewLoading">
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
                <i class="el-icon-edit" @click="handleEditPortal(row)"></i>
                <i
                  class="el-icon-table-lamp"
                  @click="handleUpdateTrend(row)"
                ></i>
                <i class="el-icon-delete" @click="handleDeleteRow(row)"></i>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="bottom">
        <el-pagination
          background
          :current-page="pageValue.page_number"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageValue.page_size"
          layout="prev, pager, next"
          :total="total"
          @current-change="handlePageNumberChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
    <div class="other">
      <PortalDrawerComponent
        :item="currentItem"
        :open-type="openType"
        :visible.sync="showPortalDrawer"
      ></PortalDrawerComponent>
    </div>
    <div class="portal-panel-view-show">
      <PanelViewShow ref="panelViewShow"></PanelViewShow>
    </div>
  </div>
</template>

<script>
import PortalDrawerComponent from "./components/PortalDrawerComponent.vue";
import PanelViewShow from "@/views/panel/list/PanelViewShow.vue";
import {
  getPortalList,
  deletePortal,
  savePortal,
  updatePortal,
} from "@/api/panel/portal";
import { initPanelData } from "@/api/panel/panel";
import bus from "@/utils/bus";
export default {
  components: {
    PortalDrawerComponent,
    PanelViewShow,
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
      currentItem: {},
      previewLoading: false,
    };
  },

  mounted() {
    this.handleGetPortalList();
    bus.$on("savePortal", this.handleSavePortal);
    bus.$on("updatePortal", this.handleUpdatePortal);
  },

  destroyed() {
    bus.$off("savePortal", this.handleSavePortal);
    bus.$off("updatePortal", this.handleUpdatePortal);
  },

  methods: {
    // 新建站点
    handleAddPortalDrawer() {
      this.openType = "add";
      this.showPortalDrawer = true;
    },

    // 点击站点
    handleEditPortal(row) {
      this.openType = "edit";
      this.currentItem = {
        ...JSON.parse(row.positionJson),
        id: row.id,
      };
      this.showPortalDrawer = true;
    },

    // 站点预览
    handleUpdateTrend(row) {
      this.previewLoading = true;
      // this.showPanelView = true;
      // this.$store.commit("setComponentDataCache", null);
      let trendId = "";
      function getTreedDataFirstTrendId(treeData) {
        for (let i = 0; i < treeData.length; i++) {
          const item = treeData[i];
          if (item.trendId) {
            trendId = item.trendId;
          } else {
            getTreedDataFirstTrendId(item.children);
          }
        }
      }

      const treeData = JSON.parse(row.positionJson).config.treeData;
      getTreedDataFirstTrendId(treeData);
      if (trendId) {
        if (Object.prototype.toString.call(trendId) == "[object Array]") {
          trendId = trendId[trendId.length - 1];
        }
        const that = this;
        initPanelData(trendId, function (response) {
          bus.$emit("set-panel-show-type", 0);
          if (that.$refs.panelViewShow) {
            that.$refs.panelViewShow.clickFullscreen();
            console.log("this.$refs.panelViewShow", that.$refs.panelViewShow);
            that.previewLoading = false;
            // that.$watch(
            //   () => that.$refs.panelViewShow.showMain,
            //   (val) => {
            //     console.log("this.$refs.panelViewShow val", val)
            //     if (val) that.$refs.panelViewShow.clickFullscreen();
            //   }
            // );
          }
        });
      } else {
        this.previewLoading = false;
        this.$message.warning("该站点下还没有配置仪表板");
      }
    },

    // 获取站点列表
    async handleGetPortalList() {
      this.previewLoading = true
      const res = await getPortalList({ ...this.pageValue });
      this.previewLoading = false
      if (res.success) {
        this.total = res.data.total;
        this.list = res.data.portalDataList.map((item) => {
          return {
            ...item,
            portalName: JSON.parse(item.positionJson).portalName,
          };
        });
      }
    },

    // 删除一个站点
    async handleDeleteRow(row) {
      this.$confirm("删除该站点", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          const res = await deletePortal(row.id);
          if (res.success) {
            this.$message.success("操作成功");
            this.handleGetPortalList();
          }
        })
        .catch(() => {});
    },

    handlePageNumberChange(page_number) {
      this.pageValue.page_number = page_number
      this.handleGetPortalList()
    },

    handlePageSizeChange(evt) {
      console.log("handlePageSizeChange evt", evt)
    },

    async handleSavePortal(params) {
      const res = await savePortal(params);
      if (res.success) {
        this.$message.success("操作成功");
      } else {
        this.$message.error(res.message);
      }
      this.handleGetPortalList();
    },
    async handleUpdatePortal(params) {
      const res = await updatePortal(params);
      if (res.success) {
        this.$message.success("操作成功");
      } else {
        this.$message.error(res.message);
      }
      this.handleGetPortalList();
    },
  },
};
</script>

<style lang="scss" scoped>
.portal-container {
  position: relative;
  .portal-container-table {
    padding: 10px;
    box-sizing: border-box;
    position: absolute;
    left: 0;
    right: 0;
    z-index: 2;
  }
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
  .bottom {
    display: flex;
    justify-content: flex-end;
    padding: 10px 0;
    box-sizing: border-box;
  }
  .portal-panel-view-show {
    position: absolute;
    z-index: 1;
    opacity: 0;
    visibility: visible;
  }
}
</style>
