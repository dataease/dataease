<template>
  <div>
    <el-button type="primary" @click="edit">{{ t('commons.edit') }}</el-button>
    <el-button type="primary" @click="validateById">{{ t('commons.validate') }}</el-button>
  </div>
  <BaseInfoContent v-slot="slotProps" :name="t('datasource.base_info')">
    <template v-if="slotProps.active">
      <el-row :gutter="24">
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.type')">{{ typeMap[nodeInfo.type] }}</BaseInfoItem>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.host')">{{
            nodeInfo.configuration.host
          }}</BaseInfoItem>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.port')">{{
            nodeInfo.configuration.port
          }}</BaseInfoItem>
        </el-col>
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.data_base')">{{
            nodeInfo.configuration.dataBase
          }}</BaseInfoItem>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.user_name')">{{
            nodeInfo.configuration.username
          }}</BaseInfoItem>
        </el-col>
        <el-col :span="12">
          <BaseInfoItem :label="t('datasource.extra_params')">{{
            nodeInfo.configuration.extraParams
          }}</BaseInfoItem>
        </el-col>
      </el-row>
      <span class="de-expand" @click="showPriority = !showPriority">
        {{ t('datasource.priority') }}
        <el-icon>
          <Icon :name="showPriority ? 'icon_down_outlined' : 'icon_down_outlined-1'"></Icon>
        </el-icon>
      </span>
      <template v-if="showPriority">
        <el-row :gutter="24">
          <el-col :span="12">
            <BaseInfoItem :label="t('datasource.initial_pool_size')">{{
              nodeInfo.configuration.initialPoolSize || 5
            }}</BaseInfoItem>
          </el-col>
          <el-col :span="12">
            <BaseInfoItem :label="t('datasource.min_pool_size')">{{
              nodeInfo.configuration.minPoolSize || 5
            }}</BaseInfoItem>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <BaseInfoItem :label="t('datasource.max_pool_size')">{{
              nodeInfo.configuration.maxPoolSize || 5
            }}</BaseInfoItem>
          </el-col>
          <el-col :span="12">
            <BaseInfoItem
              :value="nodeInfo.configuration.queryTimeout"
              :label="t('datasource.query_timeout')"
              >{{ nodeInfo.configuration.queryTimeout || '30'
              }}{{ t('common.second') }}</BaseInfoItem
            >
          </el-col>
        </el-row>
      </template>
    </template>
  </BaseInfoContent>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { SettingRecord } from '@/views/system/common/SettingTemplate'
import { reactive } from 'vue'
import BaseInfoItem from '@/views/visualized/data/datasource/BaseInfoItem.vue'
import BaseInfoContent from '@/views/visualized/data/datasource/BaseInfoContent.vue'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
import { dsTypes } from '@/views/visualized/data/datasource/form/option'
import { defineProps } from 'vue/dist/vue'
import request from '@/config/axios'
const editor = ref()
const state = reactive({
  templateList: [] as SettingRecord[]
})
const { t } = useI18n()
const typeMap = dsTypes.reduce((pre, next) => {
  pre[next.type] = next.name
  return pre
}, {})
const showPriority = ref(true)

const props = defineProps({
  nodeInfo: {
    type: Node,
    default: {}
  }
})

const emits = defineEmits(['edit'])
const edit = () => {
  emits('edit')
}

const validateById = async () => {
  request.post({ url: '/engine/validate/' + props.nodeInfo.id }).then(res => {
    if (res !== undefined) {
      ElMessage.success(t('datasource.validate_success'))
    }
  })
}
</script>

<style lang="less" scoped>
@import '@/style/mixin.less';

.datasource-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  position: relative;

  .resource-area {
    position: relative;
    height: 100%;
    width: 279px;
    padding: 0;
    border-right: 1px solid #d7d7d7;
    overflow: visible;
    &.retract {
      display: none;
    }

    .resource-tree {
      padding: 16px 0 0;
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;

      .tree-header {
        padding: 0 16px;
      }

      .icon-methods {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        font-size: 20px;
        font-weight: 500;
        color: var(--TextPrimary, #1f2329);
        padding-bottom: 16px;

        .title {
          margin-right: auto;
          font-size: 16px;
          font-style: normal;
          font-weight: 500;
          line-height: 24px;
        }

        .custom-icon {
          &.btn {
            color: #3370ff;
          }

          &:hover {
            cursor: pointer;
          }
        }
      }

      .search-bar {
        padding-bottom: 10px;
      }
    }
  }

  .update-records {
    position: absolute;
    top: 19px;
    right: 12px;
  }

  .update-info {
    display: inline-flex;
    height: 24px;
    padding: 1px 6px;
    align-items: center;
    border-radius: 2px;

    &.to-be-updated {
      background: rgba(31, 35, 41, 0.1);
      color: #646a73;
    }
    &.updating {
      color: #2b5fd9;
      background: rgba(51, 112, 255, 0.2);
    }
    &.pause {
      background: rgba(31, 35, 41, 0.1);
      color: #646a73;
    }
    &.updated {
      color: #2ca91f;
      background: rgba(52, 199, 36, 0.2);
    }
  }

  .icon-border {
    font-size: 18px;
  }

  .excel-table {
    margin-top: 16px;

    .sheet-table-content {
      height: 400px;
    }
  }

  .api-card-content {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16px;
    margin-left: -16px;
  }

  .api-card {
    width: calc(50% - 16px);
    height: 140px;
    border-radius: 4px;
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-radius: 4px;
    margin: 0 0 16px 16px;
    padding: 16px;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    .name {
      font-size: 16px;
      font-weight: 500;
      margin-right: 8px;
      max-width: 80%;
    }
    .req-title,
    .req-value {
      display: flex;
      font-size: 14px;
      font-weight: 400;
      :nth-child(1) {
        width: 100px;
      }

      :nth-child(2) {
        margin-left: 24px;
        max-width: calc(100% - 124px);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .req-title {
      color: var(--deTextSecondary, #646a73);
      margin: 16px 0 4px 0;
    }
    .req-value {
      color: var(--deTextPrimary, #1f2329);
    }
    .de-copy-icon {
      cursor: pointer;
      margin-right: 20px;
      color: var(--deTextSecondary, #646a73);
    }
    .de-delete-icon:not(.not-allow) {
      cursor: pointer;
      &:hover {
        color: var(--deDanger, #f54a45);
      }
    }
    .de-tag {
      display: inline-flex;
      justify-content: center;
      align-items: center;
      border-radius: 2px;
      padding: 1px 6px;
      height: 24px;
    }

    .error-color {
      color: #646a73;
      background-color: rgba(31, 35, 41, 10%);
    }
    .success-color {
      color: green;
      background-color: rgba(52, 199, 36, 20%);
    }
  }

  .de-expand {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: #3370ff;
    cursor: pointer;
    margin-top: 16px;
    display: inline-flex;
    align-items: center;

    .ed-icon {
      margin-left: 4px;
    }
  }

  .datasource-height,
  .datasource-content {
    height: calc(100vh - 56px);
    overflow: auto;
    position: relative;
    &.h100 {
      .datasource-table {
        height: calc(100% - 140px);
      }
    }
  }

  .datasource-list {
    width: 279px;
    padding: 16px 8px;
  }

  .datasource-content {
    background: #f5f6f7;
    overflow-y: auto;
  }

  .m24 {
    margin: 24px 0;
  }

  .w100 {
    width: 100%;
  }

  .datasource-content {
    flex: 1;
    position: relative;

    .datasource-info {
      background: #fff;
      padding: 0 24px;
      padding-top: 12px;
      height: 90px;
      position: sticky;
      top: 0;
      z-index: 6;
      .info-method {
        width: 100%;
        display: flex;
        align-items: center;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 16px;
        font-weight: 500;

        .ed-icon {
          font-size: 24px;
        }

        .name {
          margin-left: 8px;
          max-width: 200px;
        }

        .create-user {
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          color: #646a73;
          margin-right: 4px;
        }

        .mr8 {
          margin-left: 8px;
        }

        .right-btn {
          margin-left: auto;
          .replace-excel {
            margin: 0 12px;
          }
        }
      }
      .tab-border {
        .border-bottom-tab(24px);
        :deep(.ed-tabs__item) {
          font-size: 14px;
        }
        :deep(.ed-tabs__nav-wrap::after) {
          border-color: rgba(31, 35, 41, 0.15);
        }
        margin-left: 0;
      }
    }

    .datasource-table {
      padding: 24px;
      margin: 24px;
      background: #fff;
      height: calc(100vh - 190px);

      .search-operate {
        width: 280px;
        margin-bottom: 16px;
      }
    }

    .info-table {
      height: calc(100% - 49px);
    }
  }
}

.custom-tree {
  height: calc(100vh - 148px);
  padding: 0 8px;
}

.custom-tree-node {
  width: calc(100% - 30px);
  display: flex;
  align-items: center;
  box-sizing: content-box;
  padding-right: 4px;

  .label-tooltip {
    width: 100%;
    margin-left: 8.75px;
  }

  .icon-more {
    margin-left: auto;
    display: none;
  }

  &:hover {
    .label-tooltip {
      width: calc(100% - 78px);

      &.excel {
        width: calc(100% - 54px);
      }
    }

    .icon-more {
      display: inline-flex;
    }
  }
}
</style>
<style lang="less">
.record-drawer {
  .ed-drawer__body {
    padding: 24px;
  }

  .flex-align-center {
    .ed-icon {
      margin: 0 4px;
    }

    .error-info {
      cursor: pointer;
    }
  }
}
.ds-table-drawer {
  max-height: calc(100% - 120px);
  display: flex;
  flex-direction: column;

  .ed-dialog__body {
    overflow-y: auto;
  }

  .table-value,
  .table-name {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-weight: 400;
    margin: 0;
  }

  .table-name {
    color: var(--deTextSecondary, #646a73);
  }

  .table-value {
    margin: 4px 0 24px 0;
    color: var(--deTextPrimary, #1f2329);
  }
}
</style>
