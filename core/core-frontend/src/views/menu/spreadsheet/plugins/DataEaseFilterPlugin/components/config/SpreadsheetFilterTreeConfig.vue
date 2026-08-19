<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { EditPen, InfoFilled, Plus } from '@element-plus/icons-vue'
import { getDsDetailsWithPerm } from '@/api/dataset'
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'
import type {
  SpreadsheetFilterCondition,
  SpreadsheetFilterTreeField
} from '../../../../types/plugin'
import type { SpreadsheetFilterAvailableField } from '../../utils/events'
import SpreadsheetFilterRenderer from '../renderers/SpreadsheetFilterRenderer.vue'
import SpreadsheetFilterTreeFieldDialog from '../tree/SpreadsheetFilterTreeFieldDialog.vue'

const props = defineProps<{
  condition: SpreadsheetFilterCondition
  datasetTree: any[]
  primaryColor: string
}>()

const emit = defineEmits<{
  'activate-level': [index: number]
  'tree-fields-change': [newTreeFieldIds: Array<string | number>]
}>()

const fields = ref<SpreadsheetFilterAvailableField[]>([])
const fieldDialogVisible = ref(false)
const levelNames = ['一', '二', '三', '四', '五']
const datasetTreeProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: (node: any) => !!node.leaf || !node.children?.length
}
const selectableDatasetTree = computed(() => {
  const normalize = (nodes: any[]): any[] => (nodes || []).map(node => ({
    ...node,
    disabled: !!node.children?.length,
    children: normalize(node.children || [])
  }))
  return normalize(props.datasetTree)
})
const previewStyle = computed(() => ({
  '--dataease-filter-primary-color': props.primaryColor
}))

const findDatasetNode = (nodes: any[], datasetId: string | number): any => {
  for (const node of nodes) {
    if (String(node.id) === String(datasetId)) return node
    const child = findDatasetNode(node.children || [], datasetId)
    if (child) return child
  }
}

const loadFields = async (datasetId?: string | number) => {
  if (datasetId === undefined || datasetId === null || datasetId === '') {
    fields.value = []
    return
  }
  try {
    const details = await getDsDetailsWithPerm([datasetId])
    const datasetFields = details?.[0]?.fields
    fields.value = [
      ...(datasetFields?.dimensionList || []).map((field: any) => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'd' as const,
        deType: field.deType,
        desensitized: field.desensitized
      })),
      ...(datasetFields?.quotaList || []).map((field: any) => ({
        fieldId: field.id,
        fieldName: field.name || field.dataeaseName,
        groupType: 'q' as const,
        deType: field.deType,
        desensitized: field.desensitized
      }))
    ]
  } catch {
    fields.value = []
  }
}

const handleDatasetChange = async (datasetId: string | number) => {
  props.condition.treeDatasetName = findDatasetNode(props.datasetTree, datasetId)?.name
  props.condition.treeFields = []
  props.condition.treeLevelMappings = []
  props.condition.defaultValue = props.condition.multiple ? [] : ''
  props.condition.defaultValueFirstItem = false
  emit('activate-level', 0)
  await loadFields(datasetId)
}

const handleTreeFieldsConfirm = (treeFields: SpreadsheetFilterTreeField[]) => {
  const oldMappings = new Map(
    props.condition.treeLevelMappings.map(mapping => [String(mapping.treeFieldId), mapping])
  )
  const newTreeFieldIds = treeFields
    .filter(field => !oldMappings.has(String(field.fieldId)))
    .map(field => field.fieldId)
  props.condition.treeFields = treeFields
  props.condition.treeLevelMappings = treeFields.map((field, index) => ({
    treeFieldId: field.fieldId,
    linkedFields: index === 0
      ? props.condition.linkedFields
      : oldMappings.get(String(field.fieldId))?.linkedFields || []
  }))
  props.condition.defaultValue = props.condition.multiple ? [] : ''
  props.condition.defaultValueFirstItem = false
  emit('tree-fields-change', newTreeFieldIds)
  emit('activate-level', 0)
}

const handleMultipleChange = () => {
  props.condition.defaultValue = []
  props.condition.defaultValueFirstItem = false
}

const getFieldIcon = (field: SpreadsheetFilterTreeField) =>
  iconFieldMap[fieldType[field.deType] || 'text']

watch(
  () => props.condition.treeDatasetId,
  datasetId => loadFields(datasetId),
  { immediate: true }
)
</script>

<template>
  <el-form-item label="选项值数量">
    <el-radio-group v-model="condition.optionCountMode">
      <el-radio label="default">
        默认
        <el-tooltip content="最多展示1000个选项" placement="top">
          <el-icon><InfoFilled /></el-icon>
        </el-tooltip>
      </el-radio>
      <el-radio label="all">全部</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="请选择数据集" label-width="112px">
    <el-tree-select
      v-model="condition.treeDatasetId"
      :data="selectableDatasetTree"
      :props="datasetTreeProps"
      node-key="id"
      check-strictly
      :render-after-expand="false"
      placeholder="请选择数据集"
      @change="handleDatasetChange"
    >
      <template #default="{ data, node }">
        <div class="spreadsheet-filter-tree-config__dataset-node">
          <el-icon>
            <Icon :name="data.leaf ? 'icon_dataset' : 'dv-folder'">
              <icon_dataset v-if="data.leaf" class="svg-icon" />
              <dvFolder v-else class="svg-icon" />
            </Icon>
          </el-icon>
          <span :title="data.name || node.label">{{ data.name || node.label }}</span>
        </div>
      </template>
    </el-tree-select>
  </el-form-item>
  <el-form-item label="" class="spreadsheet-filter-tree-config__design-item">
    <div class="spreadsheet-filter-tree-config__design">
      <div class="spreadsheet-filter-tree-config__design-title">
        <span>下拉树结构设计</span>
        <el-button
          v-if="condition.treeFields.length"
          text
          type="primary"
          :disabled="!condition.treeDatasetId"
          @click="fieldDialogVisible = true"
        >
          <el-icon><EditPen /></el-icon>
        </el-button>
      </div>
      <template v-if="condition.treeFields.length">
        <div
          v-for="(field, index) in condition.treeFields"
          :key="field.fieldId"
          class="spreadsheet-filter-tree-config__level"
        >
          <span>层级{{ levelNames[index] }}</span>
          <el-icon><Icon><component :is="getFieldIcon(field)" class="svg-icon" /></Icon></el-icon>
          <span class="spreadsheet-filter-tree-config__field-name">{{ field.fieldName }}</span>
          <span v-if="index === 0" class="spreadsheet-filter-tree-config__linked-text">
            第一层级已关联图表
          </span>
          <el-button v-else text type="primary" @click="emit('activate-level', index)">
            关联图表
          </el-button>
        </div>
      </template>
      <el-button
        v-else
        text
        type="primary"
        :disabled="!condition.treeDatasetId"
        @click="fieldDialogVisible = true"
      >
        <el-icon><Plus /></el-icon>
        设计树结构
      </el-button>
    </div>
  </el-form-item>
  <el-form-item label="选项类型">
    <el-radio-group v-model="condition.multiple" @change="handleMultipleChange">
      <el-radio :label="false">单选</el-radio>
      <el-radio :label="true">多选</el-radio>
    </el-radio-group>
  </el-form-item>
  <el-form-item label="">
    <el-checkbox v-model="condition.defaultValueEnabled">设置默认值</el-checkbox>
  </el-form-item>
  <template v-if="condition.defaultValueEnabled">
    <el-form-item label="">
      <el-checkbox v-model="condition.defaultValueFirstItem">首项</el-checkbox>
    </el-form-item>
    <el-form-item v-if="condition.treeFields.length" label="">
      <SpreadsheetFilterRenderer
        v-model="condition.defaultValue"
        :condition="condition"
        :style="previewStyle"
        is-config
        :disabled="condition.defaultValueFirstItem"
      />
    </el-form-item>
  </template>
  <SpreadsheetFilterTreeFieldDialog
    v-model:visible="fieldDialogVisible"
    :fields="fields"
    :model-value="condition.treeFields"
    :dataset-id="condition.treeDatasetId"
    @confirm="handleTreeFieldsConfirm"
  />
</template>

<style scoped lang="less">
.spreadsheet-filter-tree-config {
  &__dataset-node {
    display: flex;
    align-items: center;
    gap: 8px;

    .ed-icon {
      width: 16px;
      height: 16px;
      flex: 0 0 16px;
    }
  }

  &__design-item {
    :deep(.ed-form-item__content) {
      margin-left: 0 !important;
    }
  }

  &__design {
    width: 100%;
  }

  &__design-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 32px;
    border-bottom: 1px solid #e5e6eb;
  }

  &__level {
    display: grid;
    grid-template-columns: 64px 20px minmax(0, 1fr) auto;
    align-items: center;
    gap: 8px;
    min-height: 40px;
  }

  &__field-name {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__linked-text {
    color: #646a73;
    font-size: 12px;
  }
}
</style>
