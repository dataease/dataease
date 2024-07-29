<script lang="ts" setup>
import { ref, h, onUnmounted, computed } from 'vue'
import { EmptyBackground } from '@/components/empty-background'
import { ElButton, ElMessage, ElMessageBox, ElTabPane, ElTabs } from 'element-plus-secondary'
import { RefreshLeft } from '@element-plus/icons-vue'
import {
  exportTasks,
  exportRetry,
  downloadFile,
  exportDelete,
  exportDeleteAll,
  exportDeletePost
} from '@/api/dataset'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { useCache } from '@/hooks/web/useCache'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useAppStoreWithOut } from '@/store/modules/app'

const { t } = useI18n()
const tableData = ref([])
const drawerLoading = ref(false)
const drawer = ref(false)
const msgDialogVisible = ref(false)
const msg = ref('')
const exportDatasetLoading = ref(false)
const activeName = ref('ALL')
const multipleSelection = ref([])
const description = ref('暂无任务')
const tabList = ref([
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
])
let timer
const handleClose = () => {
  drawer.value = false
  clearInterval(timer)
}
const { wsCache } = useCache()
const xpack = wsCache.get('xpack-model-distributed')

onUnmounted(() => {
  clearInterval(timer)
})
const handleClick = tab => {
  if (tab) {
    activeName.value = tab.paneName
  }
  if (activeName.value === 'ALL') {
    description.value = t('data_export.no_file')
  } else if (activeName.value === 'FAILED') {
    description.value = t('data_export.no_failed_file')
  } else {
    description.value = t('data_export.no_task')
  }
  drawerLoading.value = true
  exportTasks(activeName.value)
    .then(res => {
      tabList.value.forEach(item => {
        if (item.name === 'ALL') {
          item.label = '全部' + '(' + res.data.length + ')'
        }
        if (item.name === 'IN_PROGRESS') {
          item.label =
            '导出中' +
            '(' +
            res.data.filter(task => task.exportStatus === 'IN_PROGRESS').length +
            ')'
        }
        if (item.name === 'SUCCESS') {
          item.label =
            '成功' + '(' + res.data.filter(task => task.exportStatus === 'SUCCESS').length + ')'
        }
        if (item.name === 'FAILED') {
          item.label =
            '失败' + '(' + res.data.filter(task => task.exportStatus === 'FAILED').length + ')'
        }
        if (item.name === 'PENDING') {
          item.label =
            '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
        }
      })
      if (activeName.value === 'ALL') {
        tableData.value = res.data
      } else {
        tableData.value = res.data.filter(task => task.exportStatus === activeName.value)
      }
    })
    .finally(() => {
      drawerLoading.value = false
    })
}

const init = params => {
  drawer.value = true
  if (params && params.activeName !== undefined) {
    activeName.value = params.activeName
  }
  handleClick()
  timer = setInterval(() => {
    if (activeName.value === 'IN_PROGRESS') {
      exportTasks(activeName.value).then(res => {
        tabList.value.forEach(item => {
          if (item.name === 'ALL') {
            item.label = '全部' + '(' + res.data.length + ')'
          }
          if (item.name === 'IN_PROGRESS') {
            item.label =
              '导出中' +
              '(' +
              res.data.filter(task => task.exportStatus === 'IN_PROGRESS').length +
              ')'
          }
          if (item.name === 'SUCCESS') {
            item.label =
              '成功' + '(' + res.data.filter(task => task.exportStatus === 'SUCCESS').length + ')'
          }
          if (item.name === 'FAILED') {
            item.label =
              '失败' + '(' + res.data.filter(task => task.exportStatus === 'FAILED').length + ')'
          }
          if (item.name === 'PENDING') {
            item.label =
              '等待中' + '(' + res.data.filter(task => task.exportStatus === 'PENDING').length + ')'
          }
        })
        if (activeName.value === 'ALL') {
          tableData.value = res.data
        } else {
          tableData.value = res.data.filter(task => task.exportStatus === activeName.value)
        }
      })
    }
  }, 5000)
}
const linkStore = useLinkStoreWithOut()
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

const taskExportTopicCall = task => {
  if (!linkStore.getLinkToken && !isDataEaseBi.value && !appStore.getIsIframe) {
    if (JSON.parse(task).exportStatus === 'SUCCESS') {
      openMessageLoading(
        JSON.parse(task).exportFromName + ' 导出成功，前往',
        'success',
        callbackExportSuc
      )
      return
    }
    if (JSON.parse(task).exportStatus === 'FAILED') {
      openMessageLoading(
        JSON.parse(task).exportFromName + ' 导出失败，前往',
        'error',
        callbackExportError
      )
    }
  }
}

const openMessageLoading = (text, type = 'success', cb) => {
  // success error loading
  const customClass = `de-message-${type || 'success'} de-message-export`
  ElMessage({
    message: h('p', null, [
      h(
        'span',
        {
          title: t(text),
          class: 'ellipsis m50-export'
        },
        t(text)
      ),
      h(
        ElButton,
        {
          text: true,
          size: 'small',
          class: 'btn-text',
          onClick: () => {
            cb()
          }
        },
        t('data_export.export_center')
      )
    ]),
    icon: type === 'loading' ? h(RefreshLeft) : '',
    type,
    showClose: true,
    customClass
  })
}

const callbackExportError = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'FAILED' })
}

const callbackExportSuc = () => {
  useEmitt().emitter.emit('data-export-center', { activeName: 'SUCCESS' })
}

const downLoadAll = () => {
  if (multipleSelection.value.length === 0) {
    tableData.value.forEach(item => {
      downloadFile(item.id)
        .then(res => {
          const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
          const link = document.createElement('a')
          link.style.display = 'none'
          link.href = URL.createObjectURL(blob)
          link.download = item.fileName // 下载的文件名
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        })
        .finally(() => {
          exportDatasetLoading.value = false
        })
    })
    return
  }
  multipleSelection.value.map(ele => {
    downloadFile(ele.id)
      .then(res => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = ele.fileName // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      })
      .finally(() => {
        exportDatasetLoading.value = false
      })
  })
}
const showMsg = item => {
  msg.value = ''
  msg.value = item.msg
  msgDialogVisible.value = true
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}
const downloadClick = item => {
  downloadFile(item.id)
    .then(res => {
      const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = URL.createObjectURL(blob)
      link.download = item.fileName // 下载的文件名
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    })
    .finally(() => {
      exportDatasetLoading.value = false
    })
}

const retry = item => {
  exportRetry(item.id).then(() => {
    handleClick()
  })
}

const deleteField = item => {
  ElMessageBox.confirm(t('data_export.sure_del'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      exportDelete(item.id).then(() => {
        ElMessage.success(t('commons.delete_success'))
        handleClick()
      })
    })
    .catch(() => {
      //   info(t('commons.delete_cancel'))
    })
}

const handleSelectionChange = val => {
  multipleSelection.value = val
}

const confirmDelete = () => {
  const options = {
    title: '确定删除该任务吗？',
    type: 'primary',
    cb: deleteField
  }
  //   handlerConfirm(options)
}

const delAll = () => {
  if (multipleSelection.value.length === 0) {
    ElMessageBox.confirm(t('data_export.sure_del_all'), {
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false
    })
      .then(() => {
        exportDeleteAll(
          activeName.value,
          multipleSelection.value.map(ele => ele.id)
        ).then(() => {
          ElMessage.success(t('commons.delete_success'))
          handleClick()
        })
      })
      .catch(() => {
        // info(t('commons.delete_cancel'))
      })
    return
  }

  ElMessageBox.confirm(t('data_export.sure_del'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  })
    .then(() => {
      exportDeletePost(multipleSelection.value.map(ele => ele.id)).then(() => {
        ElMessage.success(t('commons.delete_success'))
        handleClick()
      })
    })
    .catch(() => {
      //   info(t('commons.delete_cancel'))
    })
}

useEmitt({ name: 'task-export-topic-call', callback: taskExportTopicCall })

defineExpose({
  init
})
</script>

<template>
  <el-drawer
    v-loading="drawerLoading"
    custom-class="de-export-excel"
    :title="$t('data_export.export_center')"
    v-model="drawer"
    direction="rtl"
    size="1000px"
    append-to-body
    :before-close="handleClose"
  >
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane v-for="tab in tabList" :key="tab.name" :label="tab.label" :name="tab.name" />
    </el-tabs>
    <el-button
      v-if="activeName === 'SUCCESS' && multipleSelection.length === 0"
      secondary
      @click="downLoadAll"
    >
      <template #icon>
        <Icon name="dv-preview-download"></Icon>
      </template>
      {{ $t('data_export.download_all') }}
    </el-button>
    <el-button
      v-if="activeName === 'SUCCESS' && multipleSelection.length !== 0"
      secondary
      @click="downLoadAll"
      ><template #icon> <Icon name="de-delete"></Icon> </template>{{ $t('data_export.download') }}
    </el-button>
    <el-button v-if="multipleSelection.length === 0" secondary @click="delAll"
      ><template #icon> <Icon name="de-delete"></Icon> </template>{{ $t('data_export.del_all') }}
    </el-button>
    <el-button v-if="multipleSelection.length !== 0" secondary @click="delAll"
      ><template #icon> <Icon name="de-delete"></Icon> </template>{{ $t('commons.delete') }}
    </el-button>
    <div class="table-container" :class="!tableData.length && 'hidden-bottom'">
      <el-table
        ref="multipleTable"
        :data="tableData"
        height="100%"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="fileName" :label="$t('driver.file_name')" width="332">
          <template #default="scope">
            <div class="name-excel">
              <el-icon style="font-size: 24px">
                <Icon name="icon_file-excel_colorful"></Icon>
              </el-icon>
              <div class="name-content">
                <div class="fileName">{{ scope.row.fileName }}</div>
                <div
                  v-if="scope.row.exportStatus === 'FAILED'"
                  class="failed"
                  @click="showMsg(scope.row)"
                >
                  {{ $t('data_export.export_failed') }}
                </div>
                <div v-if="scope.row.exportStatus === 'SUCCESS'" class="success">
                  {{ scope.row.fileSize }}{{ scope.row.fileSizeUnit }}
                </div>
              </div>
            </div>
            <div v-if="scope.row.exportStatus === 'FAILED'" class="red-line" />
            <el-progress
              v-if="scope.row.exportStatus === 'IN_PROGRESS'"
              :percentage="+scope.row.exportProgress"
            />
          </template>
        </el-table-column>
        <el-table-column prop="exportFromName" :label="$t('data_export.export_obj')" width="200" />
        <el-table-column prop="exportFromType" width="120" :label="$t('data_export.export_from')">
          <template #default="scope">
            <span v-if="scope.row.exportFromType === 'dataset'">数据集</span>
            <span v-if="scope.row.exportFromType === 'chart'">视图</span>
          </template>
        </el-table-column>
        <el-table-column v-show="xpack" prop="orgName" label="所属组织" width="200" />
        <el-table-column prop="exportTime" width="180" :label="$t('data_export.export_time')">
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.exportTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" prop="operate" width="90" :label="$t('commons.operating')">
          <template #default="scope">
            <el-tooltip effect="dark" content="下载" placement="top">
              <el-button
                v-if="scope.row.exportStatus === 'SUCCESS'"
                text
                @click="downloadClick(scope.row)"
              >
                <template #icon>
                  <el-icon>
                    <Icon name="dv-preview-download"></Icon>
                  </el-icon>
                </template>
              </el-button>
            </el-tooltip>

            <el-tooltip effect="dark" content="重新导出" placement="top">
              <el-button v-if="scope.row.exportStatus === 'FAILED'" text @click="retry(scope.row)">
                <template #icon>
                  <Icon name="icon_refresh_outlined"></Icon>
                </template>
              </el-button>
            </el-tooltip>

            <el-tooltip effect="dark" content="删除" placement="top">
              <el-button text @click="deleteField(scope.row)">
                <template #icon>
                  <Icon name="de-delete"></Icon>
                </template>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
        <template #empty>
          <empty-background :description="description" img-type="noneWhite" />
        </template>
      </el-table>
    </div>
  </el-drawer>

  <el-dialog title="失败原因" v-model="msgDialogVisible" width="30%">
    <span>{{ msg }}</span>
    <template v-slot:footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="msgDialogVisible = false">关闭</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.de-export-excel {
  .ed-drawer__body {
    padding-bottom: 24px;
  }
  .ed-drawer__header {
    border-bottom: none;
  }
  .ed-tabs {
    margin-top: -25px;
    .ed-tabs__header {
      margin-bottom: 24px;
    }
  }

  .table-container {
    margin-top: 16px;
    height: calc(100vh - 190px);

    .ed-table .cell {
      padding-left: 12px;
      padding-right: 12px;
    }

    &.hidden-bottom {
      .ed-table::before {
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
          color: #f54a45;
          cursor: pointer;
        }

        .success {
          font-size: 12px;
          font-weight: 400;
          line-height: 20px;
          color: #8f959e;
        }
      }
    }

    .ed-table__header {
      border-top: 1px solid #1f232926;
    }

    th.ed-table__cell.is-leaf {
      border-color: #1f232926;
    }

    .red-line {
      width: 100%;
      height: 4px;
      background: #f54a45;
      position: absolute;
      left: 0;
      bottom: 0;
    }
  }
}
</style>
