<template>
  <layout-content>

    <el-row>
      <el-tabs v-model="tabActive" @tab-click="changeTab">
        <el-tab-pane :label="$t('dataset.task.list')" name="DatasetTaskList">
          <dataset-task-list v-if="tabActive=='DatasetTaskList'" :param="task" :transCondition="transCondition" @jumpTaskRecord="jumpTaskRecord" />
        </el-tab-pane>
        <el-tab-pane :label="$t('dataset.task.record')" name="TaskRecord">
          <task-record v-if="tabActive=='TaskRecord'" ref="task_record" :param="task" :trans-condition="transCondition" @jumpTask="jumpTask" />
        </el-tab-pane>
      </el-tabs>
    </el-row>
  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'

import DatasetTaskList from '@/views/system/task/DatasetTaskList'
import TaskRecord from '@/views/system/task/TaskRecord'

import bus from '@/utils/bus'
import { mapGetters } from 'vuex'
export default {
  components: { LayoutContent, DatasetTaskList, TaskRecord },
  data() {
    return {
      tabActive: 'DatasetTaskList',
      transCondition: {},
      task: null
    }
  },
  computed: {
    ...mapGetters([
      'permission_routes'
    ])
  },
  mounted() {
    bus.$on('to-msg-dataset', params => {
      this.toMsgShare(params)
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', false)
    const routerParam = this.$router.currentRoute.params
    routerParam && this.$nextTick(() => {
      this.toMsgShare(routerParam)
    })
  },
  methods: {
    changeTab() {
      this.task = null
      this.transCondition = {}
    },
    jumpTaskRecord(task) {
      this.transCondition['dataset_table_task.id'] = {
        operator: 'eq',
        value: task.id
      }
      this.tabActive = 'TaskRecord'
    },
    jumpTask(taskRecord) {
      this.transCondition['dataset_table_task.id'] = {
        operator: 'eq',
        value: taskRecord.taskId
      }
      this.tabActive = 'DatasetTaskList'
    },
    toMsgShare(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
        const panelShareTypeIds = [4, 5, 6]
        // 说明是从消息通知跳转过来的
        if (panelShareTypeIds.includes(routerParam.msgType)) { // 是数据集同步
          if (routerParam.sourceParam) {
            this.openSystem()
            try {
              const msgParam = JSON.parse(routerParam.sourceParam)
              // this.param = msgParam.tableId

              this.$nextTick(() => {
                // 目标组件存在定时器 这种方式会被定时器阻塞
                // this.$refs.task_record && this.$refs.task_record.msg2Current && this.$refs.task_record.msg2Current(msgParam)
                this.task = msgParam
                this.tabActive = 'TaskRecord'
              })
            } catch (error) {
              console.error(error)
            }
          }
        }
      }
    },
    openSystem() {
      const path = '/system'
      let route = this.permission_routes.find(
        item => item.path === '/' + path.split('/')[1]
      )
      // 如果找不到这个路由，说明是首页
      if (!route) {
        route = this.permission_routes.find(item => item.path === '/')
      }
      this.$store.commit('permission/SET_CURRENT_ROUTES', route)
      // this.setSidebarHide(route)
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
