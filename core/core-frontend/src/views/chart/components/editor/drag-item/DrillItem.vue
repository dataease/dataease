<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted, ref, toRefs, watch } from 'vue'
import { getItemType } from '@/views/chart/components/editor/drag-item/utils'
import { Delete } from '@element-plus/icons-vue'
import { fieldType } from '@/utils/attr'

const { t } = useI18n()

const tagType = ref('success')

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

const emit = defineEmits(['onDimensionItemRemove'])

const { item } = toRefs(props)

watch(
  [() => props.dimensionData, () => props.item],
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
    default:
      break
  }
}
const beforeClickItem = type => {
  return {
    type: type
  }
}
const removeItem = () => {
  item.value.index = props.index
  emit('onDimensionItemRemove', item.value)
}
const getItemTagType = () => {
  tagType.value = getItemType(props.dimensionData, props.quotaData, props.item)
}
onMounted(() => {
  getItemTagType()
})
</script>

<template>
  <span class="item-style">
    <el-dropdown :effect="themes" trigger="click" size="mini" @command="clickItem">
      <el-tag
        class="item-axis father"
        :class="'editor-' + props.themes"
        :style="{ backgroundColor: tagType + '0a', border: '1px solid ' + tagType }"
      >
        <span style="display: flex">
          <el-icon>
            <Icon
              :className="`field-icon-${fieldType[item.deType]}`"
              :name="`field_${fieldType[item.deType]}`"
            ></Icon>
          </el-icon>
        </span>
        <span class="item-span-style" :title="item.name">{{ item.name }}</span>
        <el-icon
          class="child"
          style="position: absolute; top: 7px; right: 24px; color: #a6a6a6; cursor: pointer"
        >
          <Icon
            name="icon_delete-trash_outlined"
            class="el-icon-arrow-down el-icon-delete"
            @click="removeItem"
          ></Icon>
        </el-icon>
        <el-icon
          class="child"
          style="position: absolute; top: 7px; right: 8px; color: #a6a6a6; cursor: pointer"
        >
          <Icon name="icon_down_outlined-1" class="el-icon-arrow-down el-icon-delete"></Icon>
        </el-icon>
      </el-tag>
      <template #dropdown>
        <el-dropdown-menu :effect="themes" class="drop-style">
          <el-dropdown-item :icon="Delete" :command="beforeClickItem('remove')">
            <span>{{ t('chart.delete') }}</span>
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
  text-align: left;
  height: 28px;
  line-height: 28px;
  display: flex;
  border-radius: 4px;
  box-sizing: border-box;
  white-space: nowrap;
  width: 100%;
  justify-content: space-between;
  align-items: center;
  background-color: #3370ff0a;
  border: 1px solid #3370ff;
}

.item-axis:hover {
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

.father .child {
  visibility: hidden;
}

.father:hover .child {
  visibility: visible;
}
</style>
