<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, ref, toRefs, watch } from 'vue'
import { formatterItem } from '../util/formatter'
import { getItemType } from './utils'

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const { t } = useI18n()

const tagType = ref('success')
const showDateExt = ref(false)

const state = reactive({
  formatterItem: formatterItem
})

const props = defineProps({
  param: {
    type: Object,
    required: true
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
  }
})

const emit = defineEmits(['onFilterItemRemove', 'editItemFilter'])

const { item } = toRefs(props)

watch(
  [props.dimensionData, props.quotaData, props.item],
  () => {
    getItemTagType()
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

const getItemTagType = () => {
  tagType.value = getItemType(props.dimensionData, props.quotaData, props.item)
}

getItemTagType()
</script>

<template>
  <div>
    <span style="position: relative; display: inline-block">
      <i
        class="el-icon-arrow-down el-icon-delete"
        style="position: absolute; top: 6px; right: 24px; color: #878d9f; cursor: pointer"
        @click="removeItem"
      />
      <el-dropdown trigger="click" size="small" @command="clickItem">
        <el-tag size="small" class="item-axis" :type="tagType">
          <span style="float: left">
            <el-icon>
              <Icon
                :className="`field-icon-${fieldType(item.deType)}`"
                :name="`field_${fieldType(item.deType)}`"
              ></Icon>
            </el-icon>
          </span>
          <span class="item-span-style" :title="item.name">{{ item.name }}</span>
        </el-tag>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item icon="el-icon-files" :command="beforeClickItem('filter')">
              <span>{{ $t('chart.filter') }}...</span>
            </el-dropdown-item>
            <el-dropdown-item icon="el-icon-delete" divided :command="beforeClickItem('remove')">
              <span>{{ $t('chart.delete') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </span>
  </div>
</template>

<style lang="less" scoped>
.item-axis {
  padding: 1px 6px;
  margin: 0 3px 2px 3px;
  text-align: left;
  height: 24px;
  line-height: 22px;
  display: flex;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
  width: 159px;
}

.item-axis:hover {
  background-color: #fdfdfd;
  cursor: pointer;
}

span {
  font-size: 12px;
}

.summary-span {
  margin-left: 4px;
  color: #878d9f;
}

.inner-dropdown-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.item-span-style {
  display: inline-block;
  width: 115px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}
</style>
