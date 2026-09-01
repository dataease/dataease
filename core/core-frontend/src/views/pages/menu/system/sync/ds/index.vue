<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import TargetTable from '@/views/menu/system/sync/ds/TargetTable.vue'
import SourceTable from '@/views/menu/system/sync/ds/SourceTable.vue'
import { ref } from 'vue'

const { t } = useI18n()
const activeName = ref('source')
</script>
<template>
  <el-tabs v-model="activeName">
    <el-tab-pane :label="t('sync_datasource.source_ds')" name="source"></el-tab-pane>
    <el-tab-pane :label="t('sync_datasource.target_ds')" name="target"></el-tab-pane>
  </el-tabs>
  <div v-if="activeName === 'source'">
    <source-table :active-name="activeName"></source-table>
  </div>
  <div v-if="activeName === 'target'">
    <target-table :active-name="activeName"></target-table>
  </div>
</template>
<style lang="less">
source-ds-state-switch:not(.is-checked) {
  :deep(.ed-switch__core) {
    background: #bbbfc4;
  }
}

.source-ds-table,
.role-content {
  height: calc(100% - 60px);
  box-sizing: border-box;
  background: white;
  padding: 24px;

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

.bottom-bar {
  position: absolute;
  bottom: 0;
  height: 64px;
  width: calc(100% - 327px);
  padding-left: 24px;
  background: var(--neutral-00, #fff);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);

  .bottom-info {
    color: #646a73;
    margin: 0 16px 0 24px;
  }

  .batch-delete-button {
    color: var(--ed-button-text-color);
    border-color: var(--ed-button-border-color);

    &:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
      outline: none;
    }
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

<style lang="less">
.popper-max-width {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }
}
</style>
