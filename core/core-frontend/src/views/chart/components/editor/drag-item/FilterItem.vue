<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, ref, toRefs, watch } from 'vue'
import { formatterItem } from '../util/formatter'
import { getItemType } from './utils'
import { Delete, Edit, Filter } from '@element-plus/icons-vue'

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const { t } = useI18n()

const showDateExt = ref(false)

const state = reactive({
  formatterItem: formatterItem,
  tagColor: '#3370ff'
})

const props = defineProps({
  param: {
    type: Object,
    required: false
  },
  item: {
    type: Object,
    required: true
  },
  index: {
    type: Number,
    required: true
  },
  dimensionData: {
    type: Array,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits(['onFilterItemRemove', 'editItemFilter'])

const { item } = toRefs(props)

watch(
  [props.dimensionData, props.quotaData, props.item, item],
  () => {
    getItemTagColor()
  },
  { deep: true }
)

const clickItem = param => {
  if (!param) {
    return
  }
  switch (param.type) {
    case 'remove':
      removeItem()
      break
    case 'filter':
      editFilter()
      break
    default:
      break
  }
}

const beforeClickItem = type => {
  return {
    type: type
  }
}

const editFilter = () => {
  item.value.index = props.index
  emit('editItemFilter', item.value)
}

const removeItem = () => {
  item.value.index = props.index
  emit('onFilterItemRemove', item.value)
}

const getItemTagColor = () => {
  state.tagColor = getItemType(props.dimensionData, props.quotaData, props.item)
}

getItemTagColor()
</script>

<template>
  <span class="item-style">
    <el-dropdown effect="dark" trigger="click" @command="clickItem">
      <el-tag
        class="item-axis"
        :class="'editor-' + props.themes"
        :style="{ backgroundColor: state.tagColor + '0a', border: '1px solid ' + state.tagColor }"
      >
        <span style="display: flex">
          <el-icon>
            <Icon
              :className="`field-icon-${fieldType(item.deType)}`"
              :name="`field_${fieldType(item.deType)}`"
            ></Icon>
          </el-icon>
        </span>
        <span class="item-span-style" :title="item.name">{{ item.name }}</span>
        <el-icon style="position: absolute; top: 7px; right: 24px; color: #a6a6a6; cursor: pointer">
          <Icon
            name="icon_delete-trash_outlined"
            class="el-icon-arrow-down el-icon-delete"
            @click="removeItem"
          ></Icon>
        </el-icon>
        <el-icon style="position: absolute; top: 7px; right: 8px; color: #a6a6a6; cursor: pointer">
          <Icon name="icon_down_outlined-1" class="el-icon-arrow-down el-icon-delete"></Icon>
        </el-icon>
      </el-tag>
      <template #dropdown>
        <el-dropdown-menu effect="dark" class="drop-style">
          <el-dropdown-item :icon="Filter" :command="beforeClickItem('filter')">
            <span>{{ $t('chart.filter') }}...</span>
          </el-dropdown-item>
          <el-dropdown-item :icon="Delete" divided :command="beforeClickItem('remove')">
            <span>{{ $t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </span>
</template>

<style lang="less" scoped>
.item-style {
  position: relative;
  width: 100%;
  display: block;
  .ed-dropdown {
    display: flex;
  }
  :deep(.ed-tag__content) {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
}

.item-axis {
  padding: 1px 6px;
  margin: 0 3px 2px 3px;
  height: 28px;
  line-height: 28px;
  display: flex;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
  width: 100%;
  justify-content: space-between;
  align-items: center;
}

.item-axis:hover {
  background-color: #3370ff20;
  cursor: pointer;
}

span {
  font-size: 12px;
}

.summary-span {
  margin-left: 4px;
  color: #a6a6a6;
}

.inner-dropdown-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.item-span-drop {
  color: #a6a6a6;
  display: flex;
}

.item-span-style {
  display: inline-block;
  width: 115px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  color: #1f2329;
  margin-left: 4px;
}

.editor-dark {
  .item-span-style {
    color: #ffffff !important;
  }
}

.drop-style {
  :deep(.ed-dropdown-menu__item) {
    height: 32px;
  }
}
</style>
