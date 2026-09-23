<template>
  <p class="threshold-title">{{ t('threshold.grid_title') }}</p>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('threshold.grid')" name="threshold"></el-tab-pane>
    <el-tab-pane :label="t('threshold.record')" name="instance"></el-tab-pane>
  </el-tabs>
  <threshold-grid v-if="activeName === 'threshold'" @open-task-Log="openTaskLog" />
  <threshold-instance :task="taskParam" ref="thresholdInstance" v-else />
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import ThresholdGrid from '@/views/menu/system/threshold/ThresholdGrid.vue'
import ThresholdInstance from '@/views/menu/system/threshold/ThresholdInstance.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { TaskParam } from '@/views/menu/system/threshold/options'

const { t } = useI18n()

const activeName = ref('threshold')
const thresholdInstance = ref(null)
const taskParam = ref<TaskParam>({})
const handleClick = () => {
  taskParam.value = {}
}

const openTaskLog = (task: TaskParam) => {
  taskParam.value = task
  activeName.value = 'instance'
}
</script>

<style lang="less" scoped>
.threshold-title {
  color: #1f2329;
  font-feature-settings: 'clig' off, 'liga' off;
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 20px;
  font-style: normal;
  font-weight: 500;
  line-height: 28px;
}
</style>
