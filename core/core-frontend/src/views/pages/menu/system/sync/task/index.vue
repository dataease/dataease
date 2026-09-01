<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import Log from '@/views/menu/system/sync/task/log/index.vue'
import Task from '@/views/menu/system/sync/task/task/index.vue'
import { ref } from 'vue'

const { t } = useI18n()
const activeName = ref('task')
const taskId = ref('')
const handleClick = () => {
  taskId.value = ''
}
const showTaskLog = tId => {
  taskId.value = tId
  activeName.value = 'log'
}
</script>
<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane :label="t('sync_task.task_list')" name="task"></el-tab-pane>
    <el-tab-pane :label="t('sync_task.log_list')" name="log"></el-tab-pane>
  </el-tabs>
  <div v-if="activeName === 'task'">
    <task
      :active-name="activeName"
      v-if="activeName === 'task'"
      @open-task-log="showTaskLog"
    ></task>
  </div>
  <div v-if="activeName === 'log'">
    <log :active-name="activeName" :job-id="taskId"></log>
  </div>
</template>
<style lang="less">
.source-ds-table {
  height: calc(100% - 60px);
  box-sizing: border-box;
  background: white;
  padding: 24px;

  .ed-input__wrapper {
    padding-left: 12px;
    padding-right: 12px;
  }

  .source-ds-table__content {
    height: calc(100vh - 246px);
  }

  .is-in-filter {
    height: calc(100vh - 296px);
  }

  .popper-max-width {
    .ed-popper.is-dark {
      white-space: pre-wrap;
      max-width: 300px;
    }

    .ed-loading-mask {
      top: 43px;

      .ed-loading-spinner {
        top: 30%;
        margin-top: calc((43px - var(--ed-loading-spinner-size)) / 2);
      }
    }
  }
}

.source-ds-table-selection {
  height: calc(100% - 98px);

  .source-ds-table__content {
    height: calc(100vh - 285px);
  }

  .is-in-filter {
    height: calc(100vh - 335px);
  }
}

.role-content {
  padding: 0;
}

.operate-icon-container {
  font-size: 16px;
  display: flex;

  .ed-button {
    width: 24px;
    height: 24px;
    line-height: 24px;
  }
}

.right-filter {
  .filter-button {
    &:hover {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #f5f6f7;
      outline: 0;
    }

    &:focus {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #eff0f1;
      outline: 0;
    }
  }
}
</style>
<style scoped lang="less">
.popper-max-width {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }
}
</style>
