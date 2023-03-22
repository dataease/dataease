<script lang="ts" setup>
import { reactive, ref, toRaw } from 'vue'
import { Icon } from '@/components/icon-custom'
import {
  ElDropdown,
  ElDropdownMenu,
  ElCheckbox,
  ElCheckboxGroup,
  ElIcon,
  ElButton
} from 'element-plus-secondary'

import type { CheckboxValueType } from 'element-plus-secondary'
import { propTypes } from '@/utils/propTypes'

const props = defineProps({
  columnNames: propTypes.arrayOf(
    propTypes.shape({
      label: propTypes.string,
      props: propTypes.string
    })
  ),
  isPluginLoaded: propTypes.bool.def(false)
})

const emit = defineEmits(['columnChange'])

const checkAll = ref(true)
const isIndeterminate = ref(false)

const state = reactive({
  checkedColumnNames: props.columnNames.map(ele => ele.props)
})

const handleCheckedColumnNamesChange = (value: string[]) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === props.columnNames.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < props.columnNames.length

  emit('columnChange', toRaw(state.checkedColumnNames))
}
const handleCheckAllChange = (val: CheckboxValueType) => {
  state.checkedColumnNames = val ? props.columnNames.map(ele => ele.props) : []
  if (!props.isPluginLoaded) {
    state.checkedColumnNames = state.checkedColumnNames.filter(
      ele => !['dept', 'roles'].includes(ele)
    )
  }
  isIndeterminate.value = false
  emit('columnChange', toRaw(state.checkedColumnNames))
}
</script>

<template>
  <el-dropdown trigger="click" :hide-on-click="false">
    <el-button plain>
      <el-icon>
        <Icon name="icon-setting"></Icon>
      </el-icon>
      {{ $t('component.columnList') }}
    </el-button>
    <template #dropdown>
      <el-dropdown-menu class="list-columns-select">
        <p class="title">{{ $t('component.selectInfo') }}</p>
        <el-checkbox
          v-model="checkAll"
          :indeterminate="isIndeterminate"
          @change="handleCheckAllChange"
          >{{ $t('component.allSelect') }}</el-checkbox
        >
        <el-checkbox-group
          v-model="state.checkedColumnNames"
          @change="handleCheckedColumnNamesChange"
        >
          <el-checkbox v-for="column in columnNames" :key="column.props" :label="column.props">{{
            $t(column.label)
          }}</el-checkbox>
        </el-checkbox-group>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="less" scoped>
.list-columns-select {
  padding: 8px 11px !important;
  width: 238px;

  .title,
  .el-checkbox {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    padding: 5px 0;
    margin: 0;
    color: #8f959e;
  }

  .el-checkbox {
    width: 100%;
  }
}
</style>
