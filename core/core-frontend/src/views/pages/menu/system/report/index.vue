<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('report.report_title')" name="report"></el-tab-pane>
    <el-tab-pane :label="t('report.instance_title')" name="instance"></el-tab-pane>
  </el-tabs>
  <report-grid v-if="activeName === 'report'" @open-task-Log="openTaskLog" />
  <report-instance :task="task" ref="reportInstance" v-else />
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import ReportGrid from '@/views/menu/system/report/ReportGrid.vue'
import ReportInstance from '@/views/menu/system/report/ReportInstance.vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

const activeName = ref('report')
const reportInstance = ref(null)
const task = ref({
  taskId: null,
  name: null
})
const handleClick = () => {
  task.value = {
    taskId: null,
    name: null
  }
}

const openTaskLog = item => {
  task.value = {
    taskId: item.id,
    name: item.name
  }
  activeName.value = 'instance'
}
</script>

<style lang="less" scoped></style>
