<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, ref, toRefs, watch } from 'vue'
import { formatterItem } from '../util/formatter'
import { getItemType, getOriginFieldName } from '@/views/chart/components/editor/drag-item/utils'

const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const { t } = useI18n()

const tagType = ref('success')
const showDateExt = ref(false)

const state = reactive({
  formatterItem: formatterItem,
  showDateExt: false
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
  chart: {
    type: Object,
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

const emit = defineEmits([
  'onDimensionItemRemove',
  'onCustomSort',
  'onDimensionItemChange',
  'onNameEdit'
])

const { item } = toRefs(props)

watch(
  [props.dimensionData, props.item],
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
    case 'filter':
      // editFilter()
      break
    case 'formatter':
      // valueFormatter()
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
    emit('onDimensionItemChange', item.value)
  }
}

const beforeSort = type => {
  return {
    type: type
  }
}

const dateStyle = param => {
  item.value.dateStyle = param.type
  emit('onDimensionItemChange', item.value)
}

const beforeDateStyle = type => {
  return {
    type: type
  }
}

const datePattern = param => {
  item.value.datePattern = param.type
  emit('onDimensionItemChange', item.value)
}

const beforeDatePattern = type => {
  return {
    type: type
  }
}

const showRename = () => {
  item.value.index = props.index
  item.value.renameType = 'dimension'
  // item.value.dsFieldName = getOriginFieldName(props.dimensionData, props.quotaData, item.value)
  emit('onNameEdit', item.value)
}

const removeItem = () => {
  item.value.index = props.index
  item.value.removeType = 'dimension'
  emit('onDimensionItemRemove', item.value)
}

const getItemTagType = () => {
  tagType.value = getItemType(props.dimensionData, props.quotaData, props.item)
}

getItemTagType()
</script>

<template>
  <span class="item-style">
    <el-dropdown trigger="click" size="small" @command="clickItem">
      <el-tag class="item-axis">
        <span style="display: flex">
          <el-icon>
            <Icon
              :className="`field-icon-${fieldType(item.deType)}`"
              :name="`field_${fieldType(item.deType)}`"
            ></Icon>
          </el-icon>
          <svg-icon v-if="item.sort === 'asc'" icon-class="sort-asc" class-name="field-icon-sort" />
          <svg-icon
            v-if="item.sort === 'desc'"
            icon-class="sort-desc"
            class-name="field-icon-sort"
          />
          <svg-icon
            v-if="item.sort === 'custom_sort'"
            icon-class="custom_sort"
            class-name="field-icon-sort"
          />
        </span>
        <span class="item-span-style" :title="item.name">{{
          item.chartShowName ? item.chartShowName : item.name
        }}</span>
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
        <el-dropdown-menu>
          <el-dropdown-item>
            <el-dropdown placement="right-start" style="width: 100%" @command="sort">
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-sort" />
                  <span>{{ t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + props.item.sort) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="beforeSort('none')">{{
                    t('chart.none')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeSort('asc')">{{
                    t('chart.asc')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeSort('desc')">{{
                    t('chart.desc')
                  }}</el-dropdown-item>
                  <!--                  <el-dropdown-item-->
                  <!--                    v-show="!item.chartId && (item.deType === 0 || item.deType === 5)"-->
                  <!--                    :command="beforeSort('custom_sort')"-->
                  <!--                    >{{ t('chart.custom_sort') }}...</el-dropdown-item-->
                  <!--                  >-->
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item v-if="item.deType === 1" divided>
            <el-dropdown
              placement="right-start"
              size="small"
              style="width: 100%"
              @command="dateStyle"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-c-scale-to-original" />
                  <span>{{ t('chart.dateStyle') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + item.dateStyle) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="beforeDateStyle('y')">{{
                    t('chart.y')
                  }}</el-dropdown-item>
                  <el-dropdown-item v-if="showDateExt" :command="beforeDateStyle('y_Q')">{{
                    t('chart.y_Q')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeDateStyle('y_M')">{{
                    t('chart.y_M')
                  }}</el-dropdown-item>
                  <el-dropdown-item v-if="showDateExt" :command="beforeDateStyle('y_W')">{{
                    t('chart.y_W')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeDateStyle('y_M_d')">{{
                    t('chart.y_M_d')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeDateStyle('H_m_s')" divided>{{
                    t('chart.H_m_s')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m')">{{
                    t('chart.y_M_d_H_m')
                  }}</el-dropdown-item>
                  <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m_s')">{{
                    t('chart.y_M_d_H_m_s')
                  }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>
          <el-dropdown-item v-if="item.deType === 1">
            <el-dropdown
              placement="right-start"
              size="small"
              style="width: 100%"
              @command="datePattern"
            >
              <span class="el-dropdown-link inner-dropdown-menu">
                <span>
                  <i class="el-icon-timer" />
                  <span>{{ t('chart.datePattern') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + item.datePattern) }})</span>
                </span>
                <i class="el-icon-arrow-right el-icon--right" />
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="beforeDatePattern('date_sub')"
                    >{{ t('chart.date_sub') }}(1990-01-01)</el-dropdown-item
                  >
                  <el-dropdown-item :command="beforeDatePattern('date_split')"
                    >{{ t('chart.date_split') }}(1990/01/01)</el-dropdown-item
                  >
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>

          <!--          <el-dropdown-item-->
          <!--            v-if="chart.render === 'antv' && chart.type.includes('table') && item.groupType === 'q'"-->
          <!--            icon="el-icon-notebook-2"-->
          <!--            divided-->
          <!--            :command="beforeClickItem('formatter')"-->
          <!--          >-->
          <!--            <span>{{ t('chart.value_formatter') }}...</span>-->
          <!--          </el-dropdown-item>-->
          <el-dropdown-item
            icon="el-icon-edit-outline"
            divided
            :command="beforeClickItem('rename')"
          >
            <span>{{ t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item icon="el-icon-delete" divided :command="beforeClickItem('remove')">
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
  background-color: #3370ff20;
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
}

.item-span-style {
  display: inline-block;
  width: 100px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  color: #ffffff;
  margin-left: 4px;
}

.summary-span-item {
  margin-left: 4px;
  color: #878d9f;
}
</style>
