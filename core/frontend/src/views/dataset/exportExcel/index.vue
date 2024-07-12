<template>
  <el-drawer
    v-loading="drawerLoading"
    custom-class="de-user-drawer de-export-excel"
    :title="$t('data_export.export_center')"
    :visible.sync="drawer"
    direction="rtl"
    size="1000px"
    append-to-body
    :before-close="handleClose"
  >
    <el-tabs
      v-model="activeName"
      @tab-click="handleClick"
    >
      <el-tab-pane
        v-for="tab in tabList"
        :key="tab.name"
        :label="tab.label"
        :name="tab.name"
      />
    </el-tabs>
    <de-btn
      v-show="activeName === 'SUCCESS' && multipleSelection.length === 0"
      secondary
      icon="el-icon-delete"
      @click="downLoadAll"
    >{{ $t("data_export.download_all") }}
    </de-btn>
    <de-btn
      v-show="activeName === 'SUCCESS' && multipleSelection.length !== 0"
      secondary
      icon="el-icon-delete"
      @click="downLoadAll"
    >{{ $t("data_export.download") }}
    </de-btn>
    <de-btn
      v-show="multipleSelection.length === 0"
      secondary
      icon="el-icon-delete"
      @click="delAll"
    >{{ $t("data_export.del_all") }}
    </de-btn>
    <de-btn
      v-show="multipleSelection.length !== 0"
      secondary
      icon="el-icon-delete"
      @click="delAll"
    >{{ $t("commons.delete") }}
    </de-btn>
    <div
      class="table-container"
      :class="!tableData.length && 'hidden-bottom'"
    >
      <el-table
        ref="multipleTable"
        :data="tableData"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column
          type="selection"
          width="50"
        />
        <el-table-column
          prop="fileName"
          :label="$t('driver.file_name')"
          width="332"
        >
          <template slot-scope="scope">
            <div class="name-excel">
              <svg-icon
                style="font-size: 24px;"
                icon-class="icon_file-excel_colorful"
              />
              <div class="name-content">
                <div class="fileName">{{ scope.row.fileName }}</div>
                <div
                  v-if="scope.row.exportStatus==='FAILED'"
                  class="failed"
                  @click="showMsg(scope.row)"
                >{{ $t("data_export.export_failed") }}</div>
                <div
                  v-if="scope.row.exportStatus==='SUCCESS'"
                  class="success"
                >{{ scope.row.fileSize }}{{ scope.row.fileSizeUnit }}</div>
              </div>
            </div>
            <div
              v-if="scope.row.exportStatus==='FAILED'"
              class="red-line"
            />
            <el-progress
              v-if="scope.row.exportStatus==='IN_PROGRESS'"
              :percentage="+scope.row.exportPogress"
            />
          </template>
        </el-table-column>
        <el-table-column
          prop="exportFromName"
          :label="$t('data_export.export_obj')"
          width="200"
        />
        <el-table-column
          prop="exportFromType"
          width="120"
          :label="$t('data_export.export_from')"
        >
          <template slot-scope="scope">
            <span v-if="scope.row.exportFromType === 'dataset'">{{ $t("dataset.datalist") }}</span>
            <span v-if="scope.row.exportFromType === 'chart'">{{ $t("panel.view") }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="exportTime"
          width="180"
          :label="$t('data_export.export_time')"
        >
          <template slot-scope="scope">
            <span>{{ scope.row.exportTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          prop="operate"
          width="80"
          :label="$t('commons.operating')"
        >
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.exportStatus === 'SUCCESS'"
              type="text"
              size="mini"
              @click="downloadClick(scope.row)"
            >
              <div class="download-export">
                <svg-icon
                  icon-class="icon_download_outlined"
                />
              </div>
            </el-button>
            <el-button
              v-if="scope.row.exportStatus === 'FAILED'"
              type="text"
              size="mini"
              @click="retry(scope.row)"
            >
              <div class="download-export">
                <i class="el-icon-refresh-right" />
              </div>
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="deleteField(scope.row)"
            >
              <div class="download-export">
                <i class="el-icon-delete" />
              </div>
            </el-button>
          </template>
        </el-table-column>
        <el-empty
          v-if="!tableData.length"
          slot="empty"
          style="padding-top: 80px"
          :image-size="125"
          :image="errImg"
          :description="description"
        />
      </el-table>
    </div>
    <el-dialog
      title="失败原因"
      :visible.sync="msgDialogVisible"
      append-to-body
      width="30%"
    >
      <span>{{ msg }}</span>
    </el-dialog>
  </el-drawer>
</template>
<script>
import msgCfm from '@/components/msgCfm/index'
import request from '@/utils/request'
import { downloadFile, post } from '@/api/dataset/dataset'
import bus from '@/utils/bus'
import { Button } from 'element-ui'
import { runInContext as tableData } from 'lodash'
export default {
  mixins: [msgCfm],
  data() {
    return {
      activeName: 'ALL',
      multipleSelection: [],
      errImg: require('@/assets/none-data.png'),
      tableData: [{ name: '附件名称' }],
      drawer: false,
      drawerLoading: false,
      description: this.$t('暂无任务'),
      tabList: [
        {
          label: '导出中(0)',
          name: 'IN_PROGRESS'
        },
        {
          label: '成功(0)',
          name: 'SUCCESS'
        },
        {
          label: '失败(0)',
          name: 'FAILED'
        },
        {
          label: '等待中(0)',
          name: 'PENDING'
        },
        {
          label: '全部(0)',
          name: 'ALL'
        }
      ],
      loading: false,
      msgDialogVisible: false,
      msg: ''
    }
  },
  created() {
  },
  mounted() {
    bus.$on('task-export-topic-call', this.taskExportTopicCall)
  },
  beforeDestroy() {
    bus.$off('task-export-topic-call', this.taskExportTopicCall)
  },
  methods: {
    init() {
      this.drawer = true
      this.handleClick()
      this.timer = setInterval(() => {
        if (this.activeName === 'IN_PROGRESS') {
          post(
            '/exportCenter/exportTasks/' + this.activeName, {}, false
          ).then(
            (res) => {
              this.tabList.forEach(item => {
                if (item.name === 'ALL') {
                  item.label = '全部' + '(' + res.data.length + ')'
                }
                if (item.name === 'IN_PROGRESS') {
                  item.label = '导出中' + '(' + res.data.filter(task => task.exportStatus === 'IN_PROGRESS').length + ')'
                }
                if (item.name === 'SUCCESS') {
                  item.label = '成功' + '(' + res.data.filter(task => task.exportStatus === 'SUCCESS').length + ')'
                }
                if (item.name === 'FAILED') {
                  item.label = '失败' + '(' + res.data.filter(task => task.exportStatus === 'FAILED').length + ')'
                }
                if (item.name === 'PENDING') {
                  item.label = '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
                }
              })
              if (this.activeName === 'ALL') {
                this.tableData = res.data
              } else {
                this.tableData = res.data.filter(task => task.exportStatus === this.activeName)
              }
            }
          )
        }
      }, 5000)
    },
    taskExportTopicCall(task) {
      if (JSON.parse(task).exportStatus === 'SUCCESS') {
        this.openMessageLoading(JSON.parse(task).exportFromName + ' ' + this.$t('excel.export') + this.$t('dataset.completed') + this.$t('dataset.goto'), 'success', this.callbackExport)
      }
      if (JSON.parse(task).exportStatus === 'FAILED') {
        this.openMessageLoading(JSON.parse(task).exportFromName + ' ' + this.$t('excel.export') + this.$t('dataset.error') + this.$t('dataset.goto'), 'error', this.callbackExport)
      }
    },
    openMessageLoading(text, type, cb) {
      const h = this.$createElement
      const iconClass = `el-icon-${type || 'success'}`
      const customClass = `de-message-${type || 'success'} de-message-export`
      this.$message({
        message: h('p', null, [
          this.$t(text),
          h(
            Button,
            {
              props: {
                type: 'text',
              },
              class: 'btn-text',
              on: {
                click: () => {
                  cb()
                }
              }
            },
            this.$t('data_export.export_center')
          )
        ]),
        iconClass,
        showClose: true,
        customClass
      })
    },
    callbackExport() {
      bus.$emit('data-export-center')
    },
    handleClick() {
      if (this.activeName === 'ALL') {
        this.description = this.$t('data_export.no_file')
      } else if (this.activeName === 'FAILED') {
        this.description = this.$t('data_export.no_failed_file')
      } else {
        this.description = this.$t('data_export.no_task')
      }

      this.tableData = []
      this.drawerLoading = true
      post(
        '/exportCenter/exportTasks/' + this.activeName, {}, false
      ).then(
        (res) => {
          this.tabList.forEach(item => {
            if (item.name === 'ALL') {
              item.label = '全部' + '(' + res.data.length + ')'
            }
            if (item.name === 'IN_PROGRESS') {
              item.label = '导出中' + '(' + res.data.filter(task => task.exportStatus === 'IN_PROGRESS').length + ')'
            }
            if (item.name === 'SUCCESS') {
              item.label = '成功' + '(' + res.data.filter(task => task.exportStatus === 'SUCCESS').length + ')'
            }
            if (item.name === 'FAILED') {
              item.label = '失败' + '(' + res.data.filter(task => task.exportStatus === 'FAILED').length + ')'
            }
            if (item.name === 'PENDING') {
              item.label = '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
            }
          })
          if (this.activeName === 'ALL') {
            this.tableData = res.data
          } else {
            this.tableData = res.data.filter(task => task.exportStatus === this.activeName)
          }
        }

      ).finally(() => {
        this.drawerLoading = false
      })
    },
    downLoadAll() {
      if (this.multipleSelection.length === 0) {
        this.tableData.forEach(item => {
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
        })
        return
      }
      this.multipleSelection.map((ele) => {
        downloadFile(ele.id).then((res) => {
          const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
          const link = document.createElement('a')
          link.style.display = 'none'
          link.href = URL.createObjectURL(blob)
          link.download = ele.fileName // 下载的文件名
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        }).finally(() => {
          this.exportDatasetLoading = false
        })
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
        '/exportCenter/retry/' + item.id, {},
        true
      ).then(
        (res) => {
          this.handleClick()
        }
      )
    },
    deleteField(item) {
      this.$confirm(this.$t('data_export.sure_del'), '', {
        confirmButtonText: this.$t('commons.delete'),
        cancelButtonText: this.$t('commons.cancel'),
        cancelButtonClass: 'de-confirm-fail-btn de-confirm-fail-cancel',
        confirmButtonClass: 'de-confirm-fail-btn de-confirm-fail-confirm',
        customClass: 'de-confirm de-confirm-fail',
        iconClass: 'el-icon-warning'
      })
        .then(() => {
          request({
            url: '/exportCenter/delete/' + item.id,
            method: 'get'
          }).then(
            (res) => {
              this.openMessageSuccess('commons.delete_success')
              this.handleClick()
            }
          )
        })
        .catch(() => {
          this.$info(this.$t('commons.delete_cancel'))
        })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    confirmDelete() {
      const options = {
        title: '确定删除该任务吗？',
        type: 'primary',
        cb: this.deleteField
      }
      this.handlerConfirm(options)
    },
    showMsg(item) {
      this.msg = ''
      this.msg = item.msg
      this.msgDialogVisible = true
    },
    delAll() {
      if (this.multipleSelection.length === 0) {
        this.$confirm(this.$t('data_export.sure_del_all'), '', {
          confirmButtonText: this.$t('commons.delete'),
          cancelButtonText: this.$t('commons.cancel'),
          cancelButtonClass: 'de-confirm-fail-btn de-confirm-fail-cancel',
          confirmButtonClass: 'de-confirm-fail-btn de-confirm-fail-confirm',
          customClass: 'de-confirm de-confirm-fail',
          iconClass: 'el-icon-warning'
        })
          .then(() => {
            post(
              '/exportCenter/deleteAll/' + this.activeName,
              this.multipleSelection.map((ele) => ele.id),
              true
            ).then(
              (res) => {
                this.openMessageSuccess('commons.delete_success')
                this.handleClick()
              }
            )
          })
          .catch(() => {
            this.$info(this.$t('commons.delete_cancel'))
          })
        return
      }

      this.$confirm(this.$t('data_export.sure_del'), '', {
        confirmButtonText: this.$t('commons.delete'),
        cancelButtonText: this.$t('commons.cancel'),
        cancelButtonClass: 'de-confirm-fail-btn de-confirm-fail-cancel',
        confirmButtonClass: 'de-confirm-fail-btn de-confirm-fail-confirm',
        customClass: 'de-confirm de-confirm-fail',
        iconClass: 'el-icon-warning'
      })
        .then(() => {
          post(
            '/exportCenter/delete',
            this.multipleSelection.map((ele) => ele.id),
            true
          ).then(
            (res) => {
              this.openMessageSuccess('commons.delete_success')
              this.handleClick()
            }
          )
        })
        .catch(() => {
          this.$info(this.$t('commons.delete_cancel'))
        })
    },

    handleClose() {
      this.drawer = false
      clearInterval(this.timer)
    }
  }
}
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
          cursor: pointer;
        }

        .success {
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
