<template>
  <el-drawer
    custom-class="de-user-drawer de-export-excel"
    title="数据导出中心"
    v-loading="drawerLoading"
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
    <de-btn secondary icon="el-icon-delete" @click="delAll" :disabled="multipleSelection.length === 0"
      >{{ $t("删除") }}
    </de-btn>
    <div class="table-container" :class="!tableData.length && 'hidden-bottom'">
      <el-table
        ref="multipleTable"
        :data="tableData"
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50"> </el-table-column>
        <el-table-column prop="fileName" label="文件名" width="332">
          <template slot-scope="scope">
            <div class="name-excel">
              <svg-icon style="font-size: 24px;" icon-class="icon_file-excel_colorful"> </svg-icon>
              <div class="name-content">
                <div class="fileName">{{ scope.row.fileName }}</div>
                <div class="failed" v-if="activeName==='FAILED'">导出失败</div>
                <div class="sucess" v-if="scope.row.exportStatus==='SUCCESS'">{{scope.row.fileSize}}{{scope.row.fileSizeUnit}}</div>
              </div>
            </div>
            <div v-if="activeName==='FAILED'" class="red-line" />
            <el-progress v-if="activeName==='IN_PROGRESS'" :percentage="+scope.row.exportPogress"></el-progress>
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
        <el-table-column fixed="right" prop="operate" width="80" label="操作">
          <template slot-scope="scope">
            <el-button v-if="scope.row.exportStatus === 'SUCCESS'" type="text" size="mini" @click="downloadClick(scope.row)">
              <div class="download-export">
                <svg-icon
                  icon-class="icon_download_outlined"
                />
              </div>
            </el-button>
            <el-button v-if="scope.row.exportStatus === 'FAILED'" type="text" size="mini" @click="retry(scope.row)">
              <div class="download-export">
                <i class="el-icon-refresh-right"></i>
              </div>
            </el-button>
            <el-button type="text" size="mini" @click="deleteField(scope.row)">
              <div class="download-export">
                <i class="el-icon-delete"></i>
              </div>
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
import request from "@/utils/request";
import {downloadFile, post} from '@/api/dataset/dataset'
export default {
  mixins: [msgCfm],
  data() {
    return {
      activeName: "ALL",
      multipleSelection: [],
      errImg: require("@/assets/none-data.png"),
      tableData: [{ name: "附件名称" }],
      drawer: false,
      drawerLoading: false,
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
          label: "等待中(0)",
          name: "PENDING",
        },
        {
          label: "全部(0)",
          name: "ALL",
        },
      ],
      loading : false
    };
  },
  created() {
    this.handleClick()
  },
  methods: {
    init() {
      this.drawer = true;
      this.handleClick()
      this.timer = setInterval(() => {
        if(this.activeName === 'IN_PROGRESS'){
          post(
            '/exportCenter/exportTasks/' + this.activeName,{}, true
          ).then(
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
                if(item.name === 'PENDING'){
                  item.label = '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
                }
              })
              if(this.activeName === 'ALL'){
                this.tableData = res.data
              }else {
                this.tableData = res.data.filter(task => task.exportStatus === this.activeName)
              }
            },
          )
        }
      }, 5000);
    },
    format(percentage) {
      return '';
    },
    handleClick() {
      this.tableData = []
      this.drawerLoading = true
      post(
        '/exportCenter/exportTasks/' + this.activeName,{}, false
      ).then(
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
            if(item.name === 'PENDING'){
              item.label = '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
            }
          })
          if(this.activeName === 'ALL'){
            this.tableData = res.data
          }else {
            this.tableData = res.data.filter(task => task.exportStatus === this.activeName)
          }
        },

      ).finally(() => {
        this.drawerLoading = false
      })
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

    retry(item) {
      post(
        '/exportCenter/retry/' + item.id,{},
        true
      ).then(
        (res) => {
          this.handleClick()
        }
      )
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
    delAll() {
      if(this.multipleSelection.length === 0 ){
        this.openMessageSuccess("请选择", 'error')
        return
      }
      post(
        '/exportCenter/delete',
        this.multipleSelection.map((ele) => ele.id),
        true
      ).then(
        (res) => {
          this.handleClick()
        }
      )
      this.openMessageSuccess("commons.delete_success");
    },

    handleClose() {
      this.drawer = false;
      clearInterval(this.timer);
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

  .download-export {
    font-size: 16px;
  }

  .table-container {
    margin-top: 16px;

    .el-table .cell {
      padding-left: 12px;
      padding-right: 12px;
    }

    &.hidden-bottom {
      .el-table::before {
        display: none;
      }
    }

    .name-excel {
      display: flex;
      align-items: center;
      .name-content {
        max-width: 280px;
        margin-left: 4px;
        .fileName {
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          width: 100%;
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
        }

        .failed {
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #F54A45;
        }

        .sucess {
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #8F959E;
        }
      }
    }

    .el-table__header {
      border-top: 1px solid #1f232926;
    }

    th.el-table__cell.is-leaf {
      border-color: #1f232926;
    }

    .red-line {
      width: 100%;
      height: 4px;
      background: #F54A45;
      position: absolute;
      left: 0;
      bottom: 0;
    }
  }
}
</style>
