<script lang="ts" setup>
import { ref, computed, PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { propTypes } from '@/utils/propTypes'
import { ElTable } from 'element-plus-secondary'
export interface Field {
  id: number
  deType: number | string
  originName: string
  name: string
}

const { t } = useI18n()
const props = defineProps({
  fieldList: {
    type: Array as PropType<Field[]>,
    default: () => []
  },
  node: propTypes.object.def({})
})
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}
const multipleTableRef = ref<InstanceType<typeof ElTable>>()
const search = ref('')

const fieldSearchList = computed(() => {
  if (search.value.trim() !== '') {
    return props.fieldList.filter(ele =>
      ele.originName.toLocaleLowerCase().includes(search.value.trim().toLocaleLowerCase())
    )
  } else {
    return props.fieldList
  }
})
const multipleSelection = ref<Field[]>([])
const handleSelectionChange = val => {
  multipleSelection.value = val
  emit('checkedFields', multipleSelection.value)
}

const emit = defineEmits(['checkedFields'])
</script>

<template>
  <div class="field-block-style">
    <div class="field-block-option">
      <span class="option-field"
        >{{ $t('dataset.field_select') }}({{ multipleSelection.length }}/{{
          fieldList.length
        }})</span
      >
      <el-input
        v-model="search"
        :placeholder="$t('auth.search_by_field')"
        clearable
        class="option-input"
      >
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
    </div>
    <div class="field-block-body">
      <el-table
        header-cell-class-name="header-cell"
        ref="multipleTableRef"
        :data="fieldSearchList"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column :label="t('panel.column_name')">
          <template #default="scope">
            <el-icon>
              <Icon :name="`field_${fieldType(scope.row.deType)}`"></Icon>
            </el-icon>
            {{ scope.row.originName }}
          </template>
        </el-table-column>
        <el-table-column property="name" :label="t('deDataset.original_name')" />
      </el-table>
    </div>
  </div>
</template>

<style lang="less" scoped>
.field-block-style {
  height: 100%;
  width: 100%;
  font-family: PingFang SC;
  .field-block-body {
    height: 327px;
    overflow-y: auto;
  }
  .field-origin-style {
    display: flex;
    margin-left: 12px;
    width: 140px;
    align-items: center;
    font-size: 14px;
    font-weight: 500;
    color: var(--deTextSecondary, #646a73);
  }
  .field-style {
    width: 140px;
    display: inline-block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: pre;
    font-size: 14px;
    font-weight: 500;
    color: var(--deTextSecondary, #646a73);
  }
  .field-block-option {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
  }
  .option-field {
    font-size: 14px;
    font-weight: 400;
    color: var(--deTextSecondary, #646a73);
  }
  .option-input {
    width: 200px;
  }
}
</style>
