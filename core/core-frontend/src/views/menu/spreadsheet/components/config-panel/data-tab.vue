<script lang="ts" setup>
import { ref, inject, computed, onMounted, type Ref } from 'vue'
import { Delete, InfoFilled, Filter } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import FieldDropZone from '../common/field-drop-zone.vue'
import FilterTree from './filter/FilterTree.vue'
import type { FieldItemData, FilterTree as FilterTreeData, PluginConfig, TablePluginConfig } from '../../types/plugin'
import { PluginAdapterManager, TablePluginAdapter } from '../../types/adapter'
import {
  clampSpreadsheetResultLimit,
  DEFAULT_SPREADSHEET_QUERY_LIMIT,
  getSpreadsheetQueryLimit
} from '../../utils/query-limit'

const emit = defineEmits<{
  'queryData': [config: PluginConfig]
  'updateConfig': [key: string, value: any]
}>()

const pluginConfig = inject<Ref<TablePluginConfig>>('pluginConfig')
if (!pluginConfig) {
  throw new Error('data-tab: missing required injections')
}

const { t } = useI18n()

const loading = ref(false)
const filterTree = ref<InstanceType<typeof FilterTree>>()
const currentConfig = computed(() => pluginConfig.value)
const resultLimitInput = ref<string | number>(currentConfig.value.data.resultLimit ?? 1000)
const resultLimitMax = ref(DEFAULT_SPREADSHEET_QUERY_LIMIT)


const zoneSchemas = computed(() => {
  if (!currentConfig.value?.type) {
    return []
  }
  const adapter = PluginAdapterManager.getAdapter(currentConfig.value.type) as TablePluginAdapter
  if (!adapter) {
    return []
  }
  return adapter.getZonesSchema()
})

const handleZoneUpdate = (zoneId: string, val: FieldItemData[]) => {
  emit('updateConfig', `data.zones.${zoneId}`, val)
}

const validateZoneUpdate = (zoneId: string) => (fields: FieldItemData[]) => {
  const adapter = PluginAdapterManager.getAdapter(currentConfig.value.type) as TablePluginAdapter
  return adapter?.validateZoneUpdate?.(currentConfig.value, zoneId, fields)
}

const customFilter = computed({
  get: () => currentConfig.value.data.customFilter || {},
  set: (val: FilterTreeData) => emit('updateConfig', 'data.customFilter', val)
})

const filterCount = computed(() => countFilterItems(customFilter.value?.items || []))

const isFilterActive = computed(() => filterCount.value > 0)

const updateResultLimit = (val: string | number) => {
  const normalizedLimit = clampSpreadsheetResultLimit(val, resultLimitMax.value)
  resultLimitInput.value = normalizedLimit
  emit('updateConfig', 'data.resultLimit', normalizedLimit)
}

onMounted(async () => {
  resultLimitMax.value = await getSpreadsheetQueryLimit()
  const normalizedLimit = clampSpreadsheetResultLimit(
    currentConfig.value.data.resultLimit,
    resultLimitMax.value
  )
  resultLimitInput.value = normalizedLimit
  if (normalizedLimit !== currentConfig.value.data.resultLimit) {
    emit('updateConfig', 'data.resultLimit', normalizedLimit)
  }
})

const handleZoneFieldAdd = (zoneId: string) => (field: FieldItemData) => {
}

const handleZoneFieldRemove = (zoneId: string) => (field: FieldItemData, index: number) => {
}

const handleZoneFieldConfig = (zoneId: string) => (field: FieldItemData, index: number) => {
}

const handleClearFilters = () => {
  customFilter.value = {}
}

const handleOpenFilter = () => {
  filterTree.value?.init(cloneDeep(customFilter.value))
}

const handleFilterDataChange = (val: FilterTreeData) => {
  customFilter.value = cloneDeep(val)
}

const countFilterItems = (items: any[] = []) => {
  return items.reduce((count, item) => {
    if (item?.subTree?.items?.length) {
      return count + countFilterItems(item.subTree.items)
    }
    return count + (item?.type === 'item' ? 1 : 0)
  }, 0)
}

const hasFields = computed(() => {
  return Object.values(currentConfig.value.data.zones).some(
    fields => fields && fields.length > 0
  )
})

const queryData = async () => {
  if (loading.value) return

  if (!hasFields.value) {
    ElMessage.warning(t('spreadsheet.field_required'))
    return
  }

  if (!currentConfig.value.data.datasetId) {
    ElMessage.warning(t('spreadsheet.select_dataset_first'))
    return
  }

  loading.value = true

  try {
    const normalizedLimit = clampSpreadsheetResultLimit(
      currentConfig.value.data.resultLimit,
      resultLimitMax.value
    )
    if (normalizedLimit !== currentConfig.value.data.resultLimit) {
      resultLimitInput.value = normalizedLimit
      emit('updateConfig', 'data.resultLimit', normalizedLimit)
    }
    emit('queryData', {
      ...currentConfig.value,
      data: {
        ...currentConfig.value.data,
        resultLimit: normalizedLimit
      }
    })
  } catch (error) {
    ElMessage.error(t('spreadsheet.query_data_error'))
  } finally {
    loading.value = false
  }
}

</script>

<template>
  <div class="data-tab">
    <div class="data-tab-content">
      <div
        v-for="schema in zoneSchemas"
        :key="schema.id"
        class="config-section"
      >
        <div class="section-header">
          <div class="header-left">
            <span class="section-title">{{ schema.name }}</span>
            <span v-if="schema.minFields" class="required">*</span>
            <el-tooltip :content="schema.placeholder">
              <el-icon class="info-icon"><InfoFilled /></el-icon>
            </el-tooltip>
          </div>
          <el-icon
            v-if="(currentConfig?.data.zones[schema.id] || []).length > 0"
            class="delete-icon"
            @click="handleZoneUpdate(schema.id, [])"
          >
            <Delete />
          </el-icon>
        </div>
        <FieldDropZone
          :model-value="currentConfig?.data.zones[schema.id] || []"
          :zone-schema="schema"
          :plugin-type="currentConfig.type"
          :data-config="currentConfig.data"
          :placeholder="schema.placeholder"
          :accept-types="schema.acceptTypes"
          :validate-update="validateZoneUpdate(schema.id)"
          @update:model-value="(val) => handleZoneUpdate(schema.id, val)"
          @field-add="handleZoneFieldAdd(schema.id)"
          @field-remove="handleZoneFieldRemove(schema.id)"
          @field-config="handleZoneFieldConfig(schema.id)"
        />
      </div>

      <div class="config-section">
        <div class="section-header">
          <span class="section-title">过滤器</span>
          <el-icon
            v-if="isFilterActive"
            class="delete-icon"
            @click="handleClearFilters"
          >
            <Delete />
          </el-icon>
        </div>
        <div class="filter-content">
          <div class="filter-btn" :class="{ active: isFilterActive }" @click="handleOpenFilter">
            <el-icon><Filter /></el-icon>
            <span>过滤</span>
          </div>
        </div>
      </div>
    </div>

    <div class="data-tab-footer">
      <div class="footer-bar">
        <div class="footer-label">结果展示</div>
        <div class="footer-content">
          <el-input-number
            v-model="resultLimitInput"
            :min="1"
            :max="resultLimitMax"
            :precision="0"
            :controls="false"
            class="limit-input"
            @change="updateResultLimit"
          />
        </div>
      </div>

      <div class="footer-actions">
        <el-button
          type="primary"
          class="refresh-btn"
          :loading="loading"
          :disabled="loading"
          @click="queryData"
        >
          <span>更新图表数据</span>
        </el-button>
      </div>
    </div>
    <FilterTree ref="filterTree" @filter-data="handleFilterDataChange" />
  </div>
</template>

<style lang="less" scoped>
.data-tab {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;

  .data-tab-content {
    flex: 1;
    overflow: auto;
    padding: 0 0 16px;

    .config-section {
      padding: 14px 16px 18px;
      border-bottom: 1px solid #e8eaef;

      &:last-child {
        border-bottom: none;
      }

      .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;

        .header-left {
          display: flex;
          align-items: center;
          gap: 3px;
        }

        .section-title {
          font-size: 15px;
          line-height: 22px;
          font-weight: 700;
          color: #1d2129;
        }

        .required {
          color: #f54a45;
          font-size: 14px;
          margin-left: 2px;
        }

        .info-icon {
          font-size: 14px;
          color: #9ca3af;
          cursor: help;
          margin-left: 2px;
        }

        .delete-icon {
          font-size: 18px;
          color: #6b7280;
          cursor: pointer;

          &:hover {
            color: #f54a45;
          }
        }
      }

      .filter-content {
        .filter-btn {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 8px;
          height: 44px;
          padding: 0 16px;
          border: 1px solid #d8dde6;
          border-radius: 10px;
          cursor: pointer;
          font-size: 15px;
          font-weight: 600;
          color: #1f2329;
          background: #fff;
          transition: all 0.2s;

          &:hover {
            border-color: #3370ff;
            color: #3370ff;
          }

          &.active {
            border-color: #3370ff;
            color: #3370ff;
          }
        }
      }
    }
  }

  .data-tab-footer {
    flex-shrink: 0;
    background: #fff;
    border-top: 1px solid #e8eaef;

    .footer-bar {
      display: flex;
      align-items: center;
      padding: 12px 12px 12px 10px;

      .footer-label {
        width: 88px;
        flex-shrink: 0;
        font-size: 15px;
        color: #1d2129;
        font-weight: 700;
      }

      .footer-content {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: flex-end;

        .limit-input {
          width: 104px;

          :deep(.el-input__wrapper) {
            height: 40px;
            border-radius: 10px;
            box-shadow: 0 0 0 1px #d8dde6 inset;
            padding: 0 10px;
          }

          :deep(.el-input__inner) {
            text-align: center;
            font-size: 15px;
            color: #1f2329;
          }
        }
      }
    }

    .footer-actions {
      padding: 0;

      :deep(.refresh-btn) {
        width: 100%;
        height: 52px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 0;
        margin: 0;
        background: #3370ff;
        border-color: #3370ff;
        color: #fff;

        &:hover {
          background: #285fdb;
          border-color: #285fdb;
          color: #fff;
        }

        &:active {
          background: #1e4dc8;
          border-color: #1e4dc8;
        }
      }
    }
  }
}
</style>
