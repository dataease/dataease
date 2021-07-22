<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
      <el-tabs v-model="tabActive">
        <el-tab-pane :label="$t('dataset.task.list')" name="DatasetTaskList">
          <dataset-task-list />
        </el-tab-pane>
        <el-tab-pane :label="$t('dataset.task.record')" name="TaskRecord">
          <task-record ref="task_record" />
        </el-tab-pane>
      </el-tabs>
    </el-row>
  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import UnionView from '@/views/dataset/data/UnionView'
import UpdateInfo from '@/views/dataset/data/UpdateInfo'
import DatasetTaskList from '@/views/system/task/DatasetTaskList'
import TaskRecord from '@/views/system/task/TaskRecord'
import TabDataPreview from '@/views/dataset/data/TabDataPreview'
import DatasetTableData from '@/views/dataset/common/DatasetTableData'
import bus from '@/utils/bus'
export default {
  components: { DatasetTableData, LayoutContent, ComplexTable, UnionView, UpdateInfo, TabDataPreview, DatasetTaskList, TaskRecord },
  data() {
    return {
      tabActive: 'DatasetTaskList'
    }
  },
  mounted() {
    bus.$on('to-msg-dataset', params => {
      this.toMsgShare(params)
    })
  },
  created() {
    const routerParam = this.$router.currentRoute.params
    this.toMsgShare(routerParam)
  },
  methods: {

    toMsgShare(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
        const panelShareTypeIds = [4, 5, 6]
        // 说明是从消息通知跳转过来的
        if (panelShareTypeIds.includes(routerParam.msgType)) { // 是数据集同步
          if (routerParam.sourceParam) {
            try {
              const msgParam = JSON.parse(routerParam.sourceParam)
              this.param = msgParam.tableId
              this.tabActive = 'TaskRecord'
              this.$nextTick(() => {
                this.$refs.task_record && this.$refs.task_record.msg2Current && this.$refs.task_record.msg2Current(routerParam.sourceParam)
              })
            } catch (error) {
              console.error(error)
            }
          }
        }
      }
    }
  }

}
</script>

<style lang="scss" scoped>
.de-msg-radio-class {
  padding: 0 5px;
  >>>.el-radio-button__inner {
    border-radius: 4px 4px 4px 4px !important;
    border-left: 1px solid #dcdfe6 !important;
    padding: 10px 10px;
  }

  >>>.el-radio-button__orig-radio:checked+.el-radio-button__inner {
    color: #fff;
    background-color: #0a7be0;
    border-color: #0a7be0;
    -webkit-box-shadow: 0px 0 0 0 #0a7be0;
    box-shadow: 0px 0 0 0 #0a7be0;
  }
}
.de-msg-a:hover {
    text-decoration: underline !important;
    color: #0a7be0 !important;
    cursor: pointer !important;

}

</style>
