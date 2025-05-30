<script lang="tsx" setup>
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_down_outlined1 from '@/assets/svg/icon_down_outlined-1.svg'
import icon_sortAToZ_outlined from '@/assets/svg/icon_sort-a-to-z_outlined.svg'
import icon_sortZToA_outlined from '@/assets/svg/icon_sort-z-to-a_outlined.svg'
import icon_sort_outlined from '@/assets/svg/icon_sort_outlined.svg'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import icon_done_outlined from '@/assets/svg/icon_done_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { onMounted, ref, toRefs, watch } from 'vue'
import { getItemType } from '@/views/chart/components/editor/drag-item/utils'
import { fieldType } from '@/utils/attr'
import { iconFieldMap } from '@/components/icon-group/field-list'

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
  chart: {
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

const emit = defineEmits([
  'onDimensionItemRemove',
  'onCustomSort',
  'onDimensionItemChange',
  'onNameEdit',
  'editSortPriority'
])

const { item } = toRefs(props)

watch(
  [() => props.dimensionData, () => props.item, () => props.chart.type],
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
    case 'rename':
      showRename()
      break
    case 'remove':
      removeItem()
      break
    case 'sortPriority':
      emit('editSortPriority')
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

const showRename = () => {
  item.value.index = props.index
  item.value.renameType = 'drillFields'
  emit('onNameEdit', item.value)
}

const sort = param => {
  if (param.type === 'custom_sort') {
    const item = {
      index: props.index,
      sort: param.type
    }
    emit('onCustomSort', item)
  } else {
    item.value.index = props.index
    item.value.sort = param.type
    item.value.customSort = []
    delete item.value.axisType
    emit('onDimensionItemChange', item.value)
  }
}

const beforeSort = type => {
  return {
    type: type
  }
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
        :class="['editor-' + props.themes, `${themes}_icon-right`]"
        :style="{ backgroundColor: tagType + '0a', border: '1px solid ' + tagType }"
      >
        <span style="display: flex; color: #646a73">
          <template v-if="index !== 0">
            <el-icon v-if="'asc' === item.sort">
              <Icon name="icon_sort-a-to-z_outlined"
                ><icon_sortAToZ_outlined class="svg-icon"
              /></Icon>
            </el-icon>
            <el-icon v-if="'desc' === item.sort">
              <Icon name="icon_sort-z-to-a_outlined"
                ><icon_sortZToA_outlined class="svg-icon"
              /></Icon>
            </el-icon>
            <el-icon v-if="'custom_sort' === item.sort">
              <Icon name="icon_sort_outlined"><icon_sort_outlined class="svg-icon" /></Icon>
            </el-icon>
          </template>
          <el-icon>
            <Icon :className="`field-icon-${fieldType[[2, 3].includes(item.deType) ? 2 : 0]}`"
              ><component
                class="svg-icon"
                :class="`field-icon-${fieldType[[2, 3].includes(item.deType) ? 2 : 0]}`"
                :is="iconFieldMap[fieldType[item.deType]]"
              ></component
            ></Icon>
          </el-icon>
        </span>
        <el-tooltip :effect="themes || 'dark'" placement="top">
          <template #content>
            <table>
              <tbody>
                <tr>
                  <td>{{ t('dataset.field_origin_name') }}</td>
                  <td>:</td>
                  <td>{{ item.name }}</td>
                </tr>
                <tr>
                  <td>{{ t('chart.show_name') }}</td>
                  <td>:</td>
                  <td>{{ item.chartShowName ? item.chartShowName : item.name }}</td>
                </tr>
              </tbody>
            </table>
          </template>
          <span
            class="item-span-style"
            :class="{ 'sort-status': index !== 0 && item.sort !== 'none' }"
          >
            <span class="item-name">{{ item.chartShowName ? item.chartShowName : item.name }}</span>
          </span>
        </el-tooltip>
        <el-icon class="child remove-icon" size="14px">
          <Icon name="icon_delete-trash_outlined" class-name="inner-class"
            ><icon_deleteTrash_outlined @click="removeItem" class="svg-icon inner-class"
          /></Icon>
        </el-icon>
        <el-icon
          class="child"
          style="position: absolute; top: 7px; right: 10px; color: #a6a6a6; cursor: pointer"
        >
          <Icon name="icon_down_outlined-1"><icon_down_outlined1 class="svg-icon" /></Icon>
        </el-icon>
      </el-tag>
      <template #dropdown>
        <el-dropdown-menu
          :effect="themes"
          class="drop-style"
          :class="themes === 'dark' ? 'dark-dimension-quota' : ''"
        >
          <el-dropdown-item v-if="index !== 0" @click.prevent>
            <el-dropdown
              :effect="themes"
              popper-class="data-dropdown_popper_mr9"
              placement="right-start"
              style="width: 100%; height: 100%"
              @command="sort"
            >
              <span class="inner-dropdown-menu menu-item-padding">
                <span class="menu-item-content">
                  <el-icon>
                    <Icon name="icon_sort_outlined"><icon_sort_outlined class="svg-icon" /></Icon>
                  </el-icon>
                  <span>{{ t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + props.item.sort) }})</span>
                </span>
                <el-icon>
                  <Icon name="icon_right_outlined"><icon_right_outlined class="svg-icon" /></Icon>
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu
                  :effect="themes"
                  class="drop-style sub"
                  :class="themes === 'dark' ? 'dark-dimension-quota' : ''"
                >
                  <el-dropdown-item class="menu-item-padding" :command="beforeSort('none')">
                    <span
                      class="sub-menu-content"
                      :class="'none' === item.sort ? 'content-active' : ''"
                    >
                      {{ t('chart.none') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'none' === item.sort"
                          ><icon_done_outlined class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item class="menu-item-padding" :command="beforeSort('asc')">
                    <span
                      class="sub-menu-content"
                      :class="'asc' === item.sort ? 'content-active' : ''"
                    >
                      {{ t('chart.asc') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'asc' === item.sort"
                          ><icon_done_outlined class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item class="menu-item-padding" :command="beforeSort('desc')">
                    <span
                      class="sub-menu-content"
                      :class="'desc' === item.sort ? 'content-active' : ''"
                    >
                      {{ t('chart.desc') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'desc' === item.sort"
                          ><icon_done_outlined class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item class="menu-item-padding" :command="beforeSort('custom_sort')">
                    <span
                      class="sub-menu-content"
                      :class="'custom_sort' === item.sort ? 'content-active' : ''"
                    >
                      {{ t('chart.custom_sort') }}{{ t('chart.sort') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'custom_sort' === item.sort"
                          ><icon_done_outlined class="svg-icon"
                        /></Icon>
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>
          <!-- <el-dropdown-item
            v-if="index !== 0"
            :command="beforeClickItem('sortPriority')"
            class="menu-item-padding"
          >
            <el-icon />
            <span>{{ t('chart.sort_priority') }}</span>
          </el-dropdown-item> -->
          <el-dropdown-item class="menu-item-padding" :command="beforeClickItem('rename')">
            <el-icon>
              <icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></icon>
            </el-icon>
            <span>{{ t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item class="menu-item-padding" divided :command="beforeClickItem('remove')">
            <el-icon>
              <icon name="icon_delete-trash_outlined"
                ><icon_deleteTrash_outlined class="svg-icon"
              /></icon>
            </el-icon>
            <span>{{ t('chart.delete') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </span>
</template>

<style lang="less" scoped>
:deep(.ed-dropdown-menu__item) {
  padding: 0;
}
:deep(.ed-dropdown-menu__item.menu-item-padding) {
  padding: 5px 16px;
}

.menu-item-padding {
  padding: 5px 16px;
}
.item-style {
  position: relative;
  width: 100%;
  display: block;
  overflow: hidden;
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
  padding: 1px 8px;
  margin-bottom: 3px;
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
  border: 1px solid var(--ed-color-primary);
}

.item-axis:hover {
  cursor: pointer;
}

span {
  font-size: 12px;
}

.summary-span {
  margin-left: 4px;
  color: #878d9f;
  position: absolute;
  right: 25px;
}

.inner-dropdown-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .menu-item-content {
    display: flex;
    flex-direction: row;
    align-items: center;
  }
}

.sub-menu-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  &.content-active {
    color: var(--ed-color-primary);
  }

  .sub-menu-content--icon {
    margin-left: 8px;
  }
}

.item-span-drop {
  color: #a6a6a6;
  display: flex;
}

.item-span-style {
  display: flex;
  max-width: 170px;
  color: #1f2329;
  margin-left: 4px;

  &.sort-status {
    max-width: 150px;
  }

  .item-name {
    flex: 1;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}

.editor-dark {
  .item-span-style {
    color: #ffffff !important;
  }
}

.summary-span-item {
  margin-left: 4px;
}

.drop-style {
  :deep(.ed-dropdown-menu__item) {
    height: 32px;
    min-width: 218px;
  }
  &.sub {
    :deep(.ed-dropdown-menu__item) {
      min-width: 118px;
    }
  }
  :deep(.ed-dropdown-menu__item:not(.is_disabled):focus) {
    color: inherit;
    background-color: rgba(31, 35, 41, 0.1);
  }
  &.dark-dimension-quota {
    background-color: #292929;
    border: 1px solid #434343;
    :deep(.ed-dropdown-menu__item--divided) {
      border-color: #ebebeb26;
    }
    .inner-dropdown-menu {
      color: rgba(235, 235, 235, 1);
    }
    :deep(.ed-dropdown-menu__item:not(.is-disabled):hover) {
      background-color: #ebebeb1a;
    }

    :deep(.ed-dropdown-menu__item) {
      color: rgba(235, 235, 235, 1);
    }
    :deep(.ed-dropdown-menu__item.is-disabled) {
      color: #a6a6a6;
    }
    :deep(.ed-dropdown-menu__item:not(.is_disabled):focus) {
      background-color: rgba(235, 235, 235, 0.1);
    }
  }
}
.remove-icon {
  position: absolute;
  top: 7px;
  right: 24px;
  cursor: pointer;
  .inner-class {
    font-size: 14px;
  }
}

.father {
  &.dark_icon-right {
    .child {
      color: #a6a6a6;
    }
  }

  &.light_icon-right {
    .child {
      color: #646a73;
    }
  }
  .child {
    font-size: 14px;
    visibility: hidden;
  }
}

.father:hover .child {
  visibility: visible;
}

.father:hover .item-span-style {
  max-width: 140px;
  &.sort-status {
    max-width: 120px;
  }
}
</style>
<style lang="less">
.data-dropdown_popper_mr9 {
  margin-left: -9px !important;
}
.menu-item-padding {
  span {
    font-size: 14px;
    color: #1f2329;
  }
  .ed-icon {
    color: #646a73;
    font-size: 16px !important;
  }

  .sub-menu-content--icon {
    color: var(--ed-color-primary);
    margin-right: -7px;
  }
  :nth-child(1).ed-icon {
    margin-right: 8px;
  }
  .menu-item-content {
    :nth-child(1).ed-icon {
      margin-right: 8px;
    }
  }
}
.dark-dimension-quota {
  background-color: #292929;
  span {
    color: #ebebeb;
  }
  .ed-icon {
    color: #a6a6a6;
  }

  .sub-menu-content--icon {
    color: var(--ed-color-primary);
    margin-right: -7px !important;
  }
}
</style>
