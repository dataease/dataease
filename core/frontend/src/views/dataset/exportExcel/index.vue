<template>
  <el-drawer
    custom-class="de-user-drawer de-export-excel"
    title="数据导出中心"
    :visible.sync="drawer"
    direction="rtl"
    size="1000px"
    append-to-body
    :before-close="handleClose"
  >
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane
        v-for="tab in tabList"
        :key="tab.name"
        :label="tab.label"
        :name="tab.name"
      ></el-tab-pane>
    </el-tabs>
    <de-btn secondary icon="el-icon-delete" @click="delAll"
      >{{ $t("全部删除") }}
    </de-btn>
    <div class="table-container" :class="!tableData.length && 'hidden-bottom'">
      <el-table
        ref="multipleTable"
        :data="tableData"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50"> </el-table-column>
        <el-table-column prop="name" label="文件名" width="320">
          <template slot-scope="scope">
            <div class="name-excel">
              <svg-icon style="font-size: 24px;" icon-class="icon_file-excel_colorful"> </svg-icon>
              <span style="margin-left: 8px">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="object" label="导出对象" width="200">
        </el-table-column>
        <el-table-column prop="origin" width="120" label="导出来源">
        </el-table-column>
        <el-table-column prop="time" width="180" label="导出时间">
        </el-table-column>
        <el-table-column prop="operate" width="80" label="操作">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="deleteField(scope.row)"
              >{{ $t("dataset.delete") }}
            </el-button>
          </template>
        </el-table-column>
        <el-empty
          slot="empty"
          v-if="!tableData.length"
          style="padding-top: 80px"
          :image-size="125"
          :image="errImg"
          :description="description"
        />
      </el-table>
    </div>
  </el-drawer>
</template>
<script>
import msgCfm from "@/components/msgCfm/index";
import { Button } from "element-ui";
export default {
  mixins: [msgCfm],
  data() {
    return {
      activeName: "all",
      multipleSelection: [],
      errImg: require("@/assets/none-data.png"),
      tableData: [{ name: "附件名称" }],
      drawer: false,
      description: this.$t("暂无任务"),
      tabList: [
        {
          label: "导出中(3)",
          name: "ing",
        },
        {
          label: "成功(3)",
          name: "success",
        },
        {
          label: "失败(3)",
          name: "fail",
        },
        {
          label: "全部(3)",
          name: "all",
        },
      ],
    };
  },
  methods: {
    init() {
      this.drawer = true;
    },
    handleClick() {},
    deleteField() {
      this.tableData = [];
      this.openMessageSuccess("commons.delete_success");
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    confirmDelete() {
      const options = {
        title: "确定删除该任务吗？",
        type: "primary",
        cb: this.deleteField,
      };
      this.handlerConfirm(options);
    },
    openMessageLoading(cb) {
      const h = this.$createElement;
      const iconClass = `el-icon-loading`;
      const customClass = `de-message-loading de-message-export`;
      this.$message({
        message: h("p", null, [
          "后台导出中,可前往",
          h(
            Button,
            {
              props: {
                type: "text",
                size: "mini",
              },
              class: "btn-text",
              on: {
                click: () => {
                  cb();
                },
              },
            },
            "数据导出中心",
          ),
          "查看进度,进行下载、暂停等操作",
        ]),
        iconClass,
        showClose: true,
        customClass,
      });
    },
    openMessageSuccess(text, type, cb) {
      const h = this.$createElement;
      const iconClass = `el-icon-${type || "success"}`;
      const customClass = `de-message-${type || "success"} de-message-export`;
      this.$message({
        message: h("p", null, [
          h("span", null, text),
          h(
            Button,
            {
              props: {
                type: "text",
                size: "mini",
              },
              class: "btn-text",
              on: {
                click: () => {
                  cb();
                },
              },
            },
            "数据导出中心",
          ),
        ]),
        iconClass,
        showClose: true,
        customClass,
      });
    },
    delAll() {
      this.openMessageLoading(this.handleClick);
      this.openMessageSuccess( '第二个文件名称 导出失败，前往', 'error',this.handleClick);
      this.openMessageSuccess( '第二个文件名称 导出成功，前往', 'success',this.handleClick);
    },
    handleClose() {
      this.drawer = false;
    },
  },
};
</script>
<style lang="less">
.de-export-excel {
  .el-drawer__header {
    border-bottom: none;
  }
  .el-tabs {
    margin-top: -25px;
    .el-tabs__header {
      margin-bottom: 24px;
    }
  }

  .table-container {
    margin-top: 16px;
    &.hidden-bottom {
      .el-table::before {
        display: none;
      }
    }

    .name-excel {
      display: flex;
      align-items: center;
    }

    .el-table__header {
      border-top: 1px solid #1f232926;
    }

    th.el-table__cell.is-leaf {
      border-color: #1f232926;
    }
  }
}
</style>
