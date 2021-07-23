<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
      <el-tabs v-model="tabActive" @tab-click="changeTab">
        <el-tab-pane :label="$t('dataset.task.list')" name="DatasetTaskList" >
          <dataset-task-list :param="task" @jumpTaskRecord="jumpTaskRecord" v-if="tabActive=='DatasetTaskList'" />
        </el-tab-pane>
        <el-tab-pane  :label="$t('dataset.task.record')" name="TaskRecord">
          <task-record :param="task"  @jumpTask="jumpTask" v-if="tabActive=='TaskRecord'" />
        </el-tab-pane>
      </el-tabs>
    </el-row>
  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import UnionView from "@/views/dataset/data/UnionView";
import UpdateInfo from "@/views/dataset/data/UpdateInfo";
import DatasetTaskList from "@/views/system/task/DatasetTaskList";
import TaskRecord from "@/views/system/task/TaskRecord";
import TabDataPreview from "@/views/dataset/data/TabDataPreview";
import DatasetTableData from "@/views/dataset/common/DatasetTableData";
export default {
  components: {DatasetTableData, LayoutContent, ComplexTable,UnionView, UpdateInfo, TabDataPreview, DatasetTaskList, TaskRecord},
  data() {
    return {
      task: null,
      tabActive: 'DatasetTaskList'
    }
  },

  methods: {
    changeTab(){
      this.task = null
      console.log(this.tabActive)
    },
    jumpTaskRecord(task){
      this.task = task
      this.tabActive = 'TaskRecord'
    },
    jumpTask(task){
      this.task = task
      this.tabActive = 'DatasetTaskList'
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
