<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  ArrowRight,
  Check,
  Delete,
  Edit,
  Hide,
  Sort,
  SortDown,
  SortUp,
  View
} from '@element-plus/icons-vue'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'
import type {
  FieldDatePattern,
  FieldDateStyle,
  FieldFormatterConfig,
  FieldItemData,
  PluginDataConfig,
  FieldSortType
} from '../../../../types/plugin'
import SpreadsheetCustomSortDialog from '../../../../components/common/spreadsheet-custom-sort-dialog.vue'
import ValueFormatterDialog from './ValueFormatterDialog.vue'
import {
  DEFAULT_DATE_PATTERN,
  DEFAULT_DATE_STYLE,
  normalizeFormatterConfig
} from '../../utils/field-format'

const props = defineProps<{
  field: FieldItemData
  index: number
  pluginType?: string
  dataConfig?: PluginDataConfig
  enableSummaryMenu?: boolean
  enableQuickCalcMenu?: boolean
}>()

const emit = defineEmits<{
  remove: [index: number]
  rename: [field: FieldItemData, index: number]
  updateField: [index: number, field: FieldItemData]
}>()

const customSortDialogVisible = ref(false)
const valueFormatterDialogVisible = ref(false)

const dateStyleOptions: Array<{ label: string; value: FieldDateStyle }> = [
  { label: '年', value: 'y' },
  { label: '年-季度', value: 'y_Q' },
  { label: '年-月', value: 'y_M' },
  { label: '年-周', value: 'y_W' },
  { label: '年-月-日', value: 'y_M_d' },
  { label: '时:分:秒', value: 'H_m_s' },
  { label: '年-月-日 时', value: 'y_M_d_H' },
  { label: '年-月-日 时:分', value: 'y_M_d_H_m' },
  { label: '年-月-日 时:分:秒', value: 'y_M_d_H_m_s' }
]

const datePatternOptions: Array<{ label: string; example: string; value: FieldDatePattern }> = [
  { label: 'yyyy-MM-dd', example: '1990-01-01', value: 'date_sub' },
  { label: 'yyyy/MM/dd', example: '1990/01/01', value: 'date_split' }
]

const sortOptions = computed<Array<{ label: string; value: FieldSortType }>>(() => {
  const baseOptions: Array<{ label: string; value: FieldSortType }> = [
    { label: '无', value: 'none' },
    { label: '升序', value: 'asc' },
    { label: '降序', value: 'desc' }
  ]

  if (props.field.groupType === 'd') {
    return [...baseOptions, { label: '自定义', value: 'custom_sort' }]
  }

  return baseOptions
})

const currentSort = computed<FieldSortType>(() => {
  if (props.field.groupType === 'q' && props.field.sort === 'custom_sort') {
    return 'none'
  }

  return props.field.sort || 'none'
})

const currentSortLabel = computed(() => {
  return sortOptions.value.find(option => option.value === currentSort.value)?.label || '无'
})

const showSortIcon = computed(() => currentSort.value !== 'none')
const isDateField = computed(() => props.field.deType === 1)
const isQuotaField = computed(() => props.field.groupType === 'q')
const visibleFieldCount = computed(() => {
  if (!props.dataConfig) {
    return Number.POSITIVE_INFINITY
  }
  const fields = props.dataConfig?.zones?.fields || []
  return fields.filter(field => field.hidden !== true).length
})
const disableHideField = computed(() => !props.field.hidden && visibleFieldCount.value <= 1)
const currentDateStyle = computed(() => props.field.dateStyle ?? DEFAULT_DATE_STYLE)
const currentDatePattern = computed(() => props.field.datePattern ?? DEFAULT_DATE_PATTERN)
const currentDateStyleLabel = computed(
  () => dateStyleOptions.find(option => option.value === currentDateStyle.value)?.label
)
const currentDatePatternLabel = computed(
  () => datePatternOptions.find(option => option.value === currentDatePattern.value)?.label
)
const summaryOptions = [
  { label: '求和', value: 'sum' },
  { label: '平均值', value: 'avg' },
  { label: '最大值', value: 'max' },
  { label: '最小值', value: 'min' },
  { label: '计数', value: 'count' },
  { label: '去重计数', value: 'count_distinct' }
] as const
const quickCalcOptions = [
  { label: '无', value: 'none' },
  { label: '占比', value: 'percent' },
  { label: '累加', value: 'accumulate' }
] as const
const currentSummary = computed(() => props.field.summary || 'sum')
const currentSummaryLabel = computed(
  () => summaryOptions.find(option => option.value === currentSummary.value)?.label || '求和'
)
const currentQuickCalc = computed(() => props.field.compareCalc?.type || 'none')
const currentQuickCalcLabel = computed(
  () => quickCalcOptions.find(option => option.value === currentQuickCalc.value)?.label || '无'
)

const handleCommand = (command: string) => {
  if (command === 'rename') {
    emit('rename', props.field, props.index)
  } else if (command.startsWith('sort:')) {
    handleSort(command.replace('sort:', '') as FieldSortType)
  } else if (command === 'toggleHidden') {
    emit('updateField', props.index, {
      ...props.field,
      hidden: !props.field.hidden
    })
  } else if (command === 'remove') {
    emit('remove', props.index)
  } else if (command === 'valueFormatter') {
    valueFormatterDialogVisible.value = true
  }
}

const handleSort = (sort: FieldSortType) => {
  if (sort === 'custom_sort') {
    if (props.field.groupType !== 'd') {
      return
    }
    customSortDialogVisible.value = true
    return
  }

  emit('updateField', props.index, {
    ...props.field,
    sort,
    customSort: undefined
  })
}

const handleCustomSortConfirm = (customSort: Array<string | number>) => {
  emit('updateField', props.index, {
    ...props.field,
    sort: 'custom_sort',
    customSort
  })
}

const handleDateStyle = (dateStyle: FieldDateStyle) => {
  emit('updateField', props.index, {
    ...props.field,
    dateStyle,
    datePattern: props.field.datePattern ?? DEFAULT_DATE_PATTERN
  })
}

const handleDatePattern = (datePattern: FieldDatePattern) => {
  emit('updateField', props.index, {
    ...props.field,
    dateStyle: props.field.dateStyle ?? DEFAULT_DATE_STYLE,
    datePattern
  })
}

const handleFormatterConfirm = (formatterCfg: FieldFormatterConfig) => {
  emit('updateField', props.index, {
    ...props.field,
    formatterCfg
  })
}

const handleSummary = (summary: string) => {
  emit('updateField', props.index, {
    ...props.field,
    summary
  })
}

const handleQuickCalc = (type: 'none' | 'percent' | 'accumulate') => {
  emit('updateField', props.index, {
    ...props.field,
    compareCalc: {
      ...props.field.compareCalc,
      type,
      resultData: props.field.compareCalc?.resultData || 'percent'
    }
  })
}

const fieldIconType = computed(() => {
  return fieldType[props.field.deType ?? 0] || 'text'
})

const fieldTypeIcon = computed(() => {
  return iconFieldMap[fieldIconType.value] || iconFieldMap.text
})

const getFieldColor = (groupType: string) => {
  return groupType === 'd' ? '#3370ff' : '#34c724'
}
</script>

<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="detail-table-field-item" :class="{ hidden: field.hidden }">
      <div class="field-content">
        <el-icon v-if="showSortIcon" class="sort-status-icon">
          <SortUp v-if="currentSort === 'asc'" />
          <SortDown v-else-if="currentSort === 'desc'" />
          <Sort v-else />
        </el-icon>
        <el-icon class="field-icon" :style="{ color: getFieldColor(field.groupType) }">
          <Icon :class-name="`field-icon-${fieldIconType}`">
            <component
              class="svg-icon"
              :class="`field-icon-${fieldIconType}`"
              :is="fieldTypeIcon"
            />
          </Icon>
        </el-icon>
        <el-tooltip placement="top">
          <template #content>
            <div>字段名: {{ field.name }}</div>
            <div>显示名称: {{ field.chartShowName || field.name }}</div>
            <div v-if="field.hidden">状态: 已隐藏</div>
          </template>
          <span class="field-name" :title="field.chartShowName || field.name">
            {{ field.chartShowName || field.name }}
          </span>
        </el-tooltip>
        <el-icon v-if="field.hidden" class="hidden-icon">
          <Hide />
        </el-icon>
      </div>
      <div class="field-actions child">
        <span class="action-btn delete-btn" @click.stop="emit('remove', index)">
          <el-icon><Delete /></el-icon>
        </span>
      </div>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-if="isQuotaField && enableSummaryMenu" @click.prevent>
          <el-dropdown
            trigger="hover"
            placement="right-start"
            popper-class="detail-table-summary-dropdown"
            class="sort-sub-dropdown"
            @command="handleSummary"
          >
            <span class="inner-dropdown-menu">
              <span class="menu-item-content">
                <span>汇总方式</span>
                <span class="current-sort-label">({{ currentSummaryLabel }})</span>
              </span>
              <el-icon class="submenu-arrow"><ArrowRight /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in summaryOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  <span
                    class="sub-menu-content"
                    :class="{ active: currentSummary === option.value }"
                  >
                    <span>{{ option.label }}</span>
                    <el-icon v-if="currentSummary === option.value" class="sort-check-icon">
                      <Check />
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item v-if="isQuotaField && enableQuickCalcMenu" @click.prevent>
          <el-dropdown
            trigger="hover"
            placement="right-start"
            popper-class="detail-table-quick-calc-dropdown"
            class="sort-sub-dropdown"
            @command="handleQuickCalc"
          >
            <span class="inner-dropdown-menu">
              <span class="menu-item-content">
                <span>快速计算</span>
                <span class="current-sort-label">({{ currentQuickCalcLabel }})</span>
              </span>
              <el-icon class="submenu-arrow"><ArrowRight /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in quickCalcOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  <span
                    class="sub-menu-content"
                    :class="{ active: currentQuickCalc === option.value }"
                  >
                    <span>{{ option.label }}</span>
                    <el-icon v-if="currentQuickCalc === option.value" class="sort-check-icon">
                      <Check />
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item
          @click.prevent
          :divided="isQuotaField && (enableSummaryMenu || enableQuickCalcMenu)"
        >
          <el-dropdown
            trigger="hover"
            placement="right-start"
            popper-class="detail-table-sort-dropdown"
            class="sort-sub-dropdown"
            @command="handleSort"
          >
            <span class="inner-dropdown-menu">
              <span class="menu-item-content">
                <el-icon><Sort /></el-icon>
                <span>排序</span>
                <span class="current-sort-label">({{ currentSortLabel }})</span>
              </span>
              <el-icon class="submenu-arrow"><ArrowRight /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in sortOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  <span
                    class="sub-menu-content"
                    :class="{ active: currentSort === option.value }"
                  >
                    <span>{{ option.label }}</span>
                    <el-icon v-if="currentSort === option.value" class="sort-check-icon">
                      <Check />
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item v-if="isDateField" divided @click.prevent>
          <el-dropdown
            trigger="hover"
            placement="right-start"
            popper-class="detail-table-date-style-dropdown"
            class="sort-sub-dropdown"
            @command="handleDateStyle"
          >
            <span class="inner-dropdown-menu">
              <span class="menu-item-content">
                <span>日期显示</span>
                <span class="current-sort-label">({{ currentDateStyleLabel }})</span>
              </span>
              <el-icon class="submenu-arrow"><ArrowRight /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in dateStyleOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  <span
                    class="sub-menu-content date-style-menu-content"
                    :class="{ active: currentDateStyle === option.value }"
                  >
                    <span>{{ option.label }}</span>
                    <el-icon v-if="currentDateStyle === option.value" class="sort-check-icon">
                      <Check />
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item v-if="isDateField" @click.prevent>
          <el-dropdown
            trigger="hover"
            placement="right-start"
            popper-class="detail-table-date-pattern-dropdown"
            class="sort-sub-dropdown"
            @command="handleDatePattern"
          >
            <span class="inner-dropdown-menu">
              <span class="menu-item-content">
                <span>日期格式</span>
                <span class="current-sort-label">({{ currentDatePatternLabel }})</span>
              </span>
              <el-icon class="submenu-arrow"><ArrowRight /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in datePatternOptions"
                  :key="option.value"
                  :command="option.value"
                >
                  <span
                    class="sub-menu-content date-pattern-menu-content"
                    :class="{ active: currentDatePattern === option.value }"
                  >
                    <span>{{ option.label }}（{{ option.example }}）</span>
                    <el-icon v-if="currentDatePattern === option.value" class="sort-check-icon">
                      <Check />
                    </el-icon>
                  </span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-dropdown-item>
        <el-dropdown-item command="rename" divided>
          <el-icon><Edit /></el-icon>
          <span>编辑显示名称</span>
        </el-dropdown-item>
        <el-dropdown-item command="toggleHidden" :disabled="disableHideField">
          <el-icon>
            <View v-if="field.hidden" />
            <Hide v-else />
          </el-icon>
          <span>{{ field.hidden ? '取消隐藏字段' : '隐藏字段' }}</span>
        </el-dropdown-item>
        <el-dropdown-item v-if="isQuotaField" command="valueFormatter">
          <span>数值格式...</span>
        </el-dropdown-item>
        <el-dropdown-item command="remove" divided>
          <el-icon class="delete-icon"><Delete /></el-icon>
          <span class="delete-text">删除</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <SpreadsheetCustomSortDialog
    v-model="customSortDialogVisible"
    :plugin-type="pluginType"
    :data-config="dataConfig"
    :field="field"
    @confirm="handleCustomSortConfirm"
  />
  <ValueFormatterDialog
    v-model="valueFormatterDialogVisible"
    :field-name="field.chartShowName || field.name"
    :formatter-cfg="normalizeFormatterConfig(field.formatterCfg)"
    @confirm="handleFormatterConfirm"
  />
</template>

<style scoped lang="less">
.detail-table-field-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;

  &.hidden {
    opacity: 0.72;
  }

  .field-content {
    display: flex;
    align-items: center;
    flex: 1;
    overflow: hidden;

    .field-icon {
      font-size: 14px;
      font-weight: 500;
      flex-shrink: 0;
      width: 16px;
      text-align: center;
    }

    .sort-status-icon {
      flex-shrink: 0;
      color: #646a73;
      font-size: 14px;
    }

    .field-name {
      font-size: 13px;
      color: #1f2329;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-left: 4px;
    }

    .hidden-icon {
      flex-shrink: 0;
      color: #8f959e;
      font-size: 14px;
    }
  }

  .field-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    visibility: hidden;

    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      cursor: pointer;
      color: #8f959e;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        background: rgba(0, 0, 0, 0.05);
        color: #f54a45;
      }

      .ed-icon {
        font-size: 14px;
      }
    }
  }

  &:hover .field-actions {
    visibility: visible;
  }
}

.sort-sub-dropdown {
  width: 100%;
}

.inner-dropdown-menu {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .menu-item-content {
    display: flex;
    align-items: center;
    gap: 6px;
    min-width: 0;
  }

  .current-sort-label {
    color: #8f959e;
  }

  .submenu-arrow {
    margin-left: 12px;
    color: #8f959e;
  }
}

.sub-menu-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 80px;
  width: 100%;

  &.active {
    color: #3370ff;
  }

  .sort-check-icon {
    margin-left: 16px;
  }
}

.date-style-menu-content {
  min-width: 150px;
}

.date-pattern-menu-content {
  min-width: 190px;
}
</style>
