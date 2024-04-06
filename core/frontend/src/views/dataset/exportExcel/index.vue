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
        <el-table-column prop="fileName" label="文件名" width="320">
          <template slot-scope="scope">
            <div class="name-excel">
              <svg-icon style="font-size: 24px;" icon-class="icon_file-excel_colorful"> </svg-icon>
              <span style="margin-left: 8px">{{ scope.row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="exportFromName" label="导出对象" width="200">
        </el-table-column>
        <el-table-column prop="exportFromType" width="120" label="导出来源">
          <template slot-scope="scope">
            <span v-if="scope.row.exportFromType === 'dataset'">数据集</span>
            <span v-if="scope.row.exportFromType === 'chart'">视图</span>
          </template>
        </el-table-column>
        <el-table-column prop="exportTime" width="180" label="导出时间">
          <template slot-scope="scope">
            <span>{{ scope.row.exportTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operate" width="80" label="操作">
          <template slot-scope="scope">
            <el-button v-if="activeName === 'SUCCESS'" type="text" size="mini" @click="downloadClick(scope.row)">
              <div class="download-export">
                <svg-icon
                  icon-class="icon_download_outlined"
                />
              </div>
            </el-button>
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
import request from "@/utils/request";
import {downloadFile, post} from '@/api/dataset/dataset'
export default {
  mixins: [msgCfm],
  data() {
    return {
      activeName: "IN_PROGRESS",
      multipleSelection: [],
      errImg: require("@/assets/none-data.png"),
      tableData: [{ name: "附件名称" }],
      drawer: false,
      description: this.$t("暂无任务"),
      tabList: [
        {
          label: "导出中(0)",
          name: "IN_PROGRESS",
        },
        {
          label: "成功(0)",
          name: "SUCCESS",
        },
        {
          label: "失败(0)",
          name: "FAILED",
        },
        {
          label: "全部(0)",
          name: "ALL",
        },
      ],
    };
  },
  created() {
    this.handleClick()
  },
  methods: {
    init() {
      this.drawer = true;
    },
    handleClick() {
      request({
        url: '/exportCenter/exportTasks/' + this.activeName,
        method: 'post'
      }).then(
        (res) => {
          this.tabList.forEach( item => {
            if(item.name === 'ALL'){
              item.label = '全部' + '(' + res.data.length + ')'
            }
            if(item.name === 'IN_PROGRESS'){
              item.label = '导出中' + '(' + res.data.filter(task => task.exportStatus === 'IN_PROGRESS').length + ')'
            }
            if(item.name === 'SUCCESS'){
              item.label = '成功' + '(' + res.data.filter(task => task.exportStatus === 'SUCCESS').length + ')'
            }
            if(item.name === 'FAILED'){
              item.label = '失败' + '(' + res.data.filter(task => task.exportStatus === 'FAILED').length + ')'
            }
          })
          if(this.activeName === 'ALL'){
            this.tableData = res.data
          }else {
            this.tableData = res.data.filter(task => task.exportStatus === this.activeName)
          }
        }
      )
    },
    downloadClick(item) {
      downloadFile(item.id).then((res) => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = item.fileName // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      }).finally(() => {
        this.exportDatasetLoading = false
      })
    },

    deleteField(item) {
      request({
        url: '/exportCenter/delete/' + item.id,
        method: 'get'
      }).then(
        (res) => {
         this.handleClick()
        }
      )
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
    delAll() {
      post(
        '/exportCenter/delete',
        this.multipleSelection.map((ele) => ele.id),
        false
      ).then(
        (res) => {
          this.handleClick()
        }
      )
      this.openMessageSuccess("commons.delete_success");
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
