<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { computed, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { formatterItem } from '@/views/chart/components/js/formatter'
import { getItemType, resetValueFormatter } from '@/views/chart/components/editor/drag-item/utils'
import { Filter } from '@element-plus/icons-vue'
import { quotaViews } from '@/views/chart/components/js/util'
import { SUPPORT_Y_M } from '@/views/chart/components/editor/util/chart'
import { fieldType } from '@/utils/attr'

const { t } = useI18n()

const tagType = ref('success')

const state = reactive({
  formatterItem: formatterItem,
  disableEditCompare: false,
  quotaViews: quotaViews
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
  },
  type: {
    type: String,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits([
  'onQuotaItemRemove',
  'onCustomSort',
  'onQuotaItemChange',
  'onNameEdit',
  'editItemFilter',
  'editItemCompare',
  'valueFormatter'
])

const { item, chart } = toRefs(props)
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
watch(
  [() => props.quotaData, () => props.item],
  () => {
    getItemTagType()
  },
  { deep: true }
)

watch(
  () => props.chart,
  () => {
    isEnableCompare()
  },
  { deep: true }
)
const AXIS_FORMAT_VIEW = ['table-normal', 'table-info', 'table-pivot', 'indicator']
const showValueFormatter = computed<boolean>(() => {
  return (
    AXIS_FORMAT_VIEW.includes(props.chart.type) &&
    (props.item.deType === 2 || props.item.deType === 3)
  )
})

const isEnableCompare = () => {
  let xAxis = null
  if (Object.prototype.toString.call(chart.value.xAxis) === '[object Array]') {
    xAxis = JSON.parse(JSON.stringify(chart.value.xAxis))
  } else {
    xAxis = JSON.parse(chart.value.xAxis)
  }
  const t1 = xAxis.filter(ele => {
    return ele.deType === 1 && SUPPORT_Y_M.includes(ele.dateStyle)
  })

  if (chart.value.type === 'table-pivot') {
    let xAxisExt = null
    if (Object.prototype.toString.call(chart.value.xAxisExt) === '[object Array]') {
      xAxisExt = JSON.parse(JSON.stringify(chart.value.xAxisExt))
    } else {
      xAxisExt = JSON.parse(chart.value.xAxisExt)
    }
    const t2 = xAxisExt.filter(ele => {
      return ele.deType === 1 && SUPPORT_Y_M.includes(ele.dateStyle)
    })

    t1.push(...t2)
  }

  // 暂时只支持类别轴/维度的时间类型字段
  if (
    t1.length > 0 &&
    chart.value.type !== 'indicator' &&
    chart.value.type !== 'label' &&
    chart.value.type !== 'gauge' &&
    chart.value.type !== 'liquid'
  ) {
    state.disableEditCompare = false
  } else {
    state.disableEditCompare = true
  }
}

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
      editFilter()
      break
    case 'formatter':
      valueFormatter()
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
    emit('onQuotaItemChange', item.value)
  }
}

const beforeSort = type => {
  return {
    type: type
  }
}

const summary = param => {
  item.value.summary = param.type
  emit('onQuotaItemChange', item.value)
}

const beforeSummary = type => {
  return {
    type: type
  }
}

const showRename = () => {
  item.value.index = props.index
  item.value.renameType = props.type
  // item.value.dsFieldName = getOriginFieldName(props.dimensionData, props.quotaData, item.value)
  emit('onNameEdit', item.value)
}

const removeItem = () => {
  item.value.index = props.index
  item.value.removeType = props.type
  emit('onQuotaItemRemove', item.value)
}

const getItemTagType = () => {
  tagType.value = getItemType(props.dimensionData, props.quotaData, props.item)
}

const editFilter = () => {
  item.value.index = props.index
  item.value.filterType = props.type
  emit('editItemFilter', item.value)
}

const quickCalc = param => {
  switch (param.type) {
    case 'none':
      // 选择占比外，设置自动
      resetValueFormatter(item.value)
      item.value.compareCalc.type = 'none'
      emit('onQuotaItemChange', item.value)
      break
    case 'setting':
      // 选择占比外，设置自动
      resetValueFormatter(item.value)
      editCompare()
      break
    case 'percent':
      // 选择占比，自动将数值格式设置为百分比并保留2位小数
      item.value.formatterCfg.type = 'percent'
      item.value.formatterCfg.decimalCount = 2

      item.value.compareCalc.type = 'percent'
      emit('onQuotaItemChange', item.value)
      break
    default:
      break
  }
}

const beforeQuickCalc = type => {
  return {
    type: type
  }
}

const editCompare = () => {
  item.value.index = props.index
  item.value.calcType = props.type
  emit('editItemCompare', item.value)
}

const valueFormatter = () => {
  item.value.index = props.index
  item.value.formatterType = props.type
  emit('valueFormatter', item.value)
}

onMounted(() => {
  isEnableCompare()
  getItemTagType()
})
</script>

<template>
  <span class="item-style">
    <el-dropdown :effect="themes" trigger="click" @command="clickItem">
      <el-tag
        class="item-axis father"
        :class="['editor-' + props.themes, `${themes}_icon-right`]"
        :style="{ backgroundColor: tagType + '0a', border: '1px solid ' + tagType }"
      >
        <span style="display: flex; color: #646a73">
          <el-icon v-if="'asc' === item.sort">
            <Icon name="icon_sort-a-to-z_outlined" />
          </el-icon>
          <el-icon v-if="'desc' === item.sort">
            <Icon name="icon_sort-z-to-a_outlined" />
          </el-icon>
          <el-icon v-if="'custom_sort' === item.sort">
            <Icon name="icon_sort_outlined" />
          </el-icon>
          <el-icon>
            <Icon
              :className="`field-icon-${fieldType[item.deType]}`"
              :name="`field_${fieldType[item.deType]}`"
            />
          </el-icon>
        </span>
        <el-tooltip
          :effect="toolTip"
          placement="top"
          :content="item.chartShowName ? item.chartShowName : item.name"
        >
          <span class="item-span-style">
            <span class="item-name">{{ item.chartShowName ? item.chartShowName : item.name }}</span>
            <span v-if="item.summary !== ''" class="item-right-summary">
              ({{ t('chart.' + item.summary) }})
            </span>
          </span>
        </el-tooltip>
        <span
          v-if="false && chart.type !== 'table-info' && item.summary && !item.chartId"
          class="summary-span"
        >
          {{ t('chart.' + item.summary) }}
          <span
            v-if="
              item.compareCalc &&
              item.compareCalc.type &&
              item.compareCalc.type !== '' &&
              item.compareCalc.type !== 'none'
            "
          >
            -{{ t('chart.' + item.compareCalc.type) }}
          </span>
        </span>
        <el-tooltip :effect="toolTip" placement="top">
          <template #content>
            <span>{{ t('chart.delete') }}</span>
          </template>
          <el-icon class="child remove-icon">
            <Icon class-name="inner-class" name="icon_delete-trash_outlined" @click="removeItem" />
          </el-icon>
        </el-tooltip>

        <el-icon class="child" style="position: absolute; top: 7px; right: 10px; cursor: pointer">
          <Icon name="icon_down_outlined-1" />
        </el-icon>
      </el-tag>
      <template #dropdown>
        <el-dropdown-menu
          :effect="themes"
          class="drop-style"
          :class="themes === 'dark' ? 'dark-dimension-quota' : ''"
        >
          <el-dropdown-item
            @click.prevent
            v-if="!item.chartId && chart.type !== 'table-info' && item.summary !== ''"
            :divided="chart.type === 'chart-mix'"
          >
            <el-dropdown
              :effect="themes"
              placement="right-start"
              style="width: 100%; height: 100%"
              @command="summary"
            >
              <span class="el-dropdown-link inner-dropdown-menu menu-item-padding">
                <span class="menu-item-content">
                  <el-icon>
                    <Icon name="icon_functions_outlined" />
                  </el-icon>
                  <span>{{ t('chart.summary') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + item.summary) }})</span>
                </span>
                <el-icon>
                  <Icon name="icon_right_outlined"></Icon>
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu
                  :effect="themes"
                  class="drop-style sub"
                  :class="themes === 'dark' ? 'dark-dimension-quota' : ''"
                >
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('sum')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'sum' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.sum') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'sum' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('avg')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'avg' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.avg') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'avg' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('max')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'max' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.max') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'max' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('min')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'min' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.min') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'min' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('stddev_pop')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'stddev_pop' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.stddev_pop') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'stddev_pop' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="
                      item.id !== '-1' &&
                      item.deType !== 0 &&
                      item.deType !== 1 &&
                      item.deType !== 5
                    "
                    :command="beforeSummary('var_pop')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'var_pop' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.var_pop') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'var_pop' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item class="menu-item-padding" :command="beforeSummary('count')">
                    <span
                      class="sub-menu-content"
                      :class="'count' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.count') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'count' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    v-if="item.id !== '-1'"
                    :command="beforeSummary('count_distinct')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'count_distinct' === item.summary ? 'content-active' : ''"
                    >
                      {{ t('chart.count_distinct') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'count_distinct' === item.summary" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>

          <!--同比/环比等快速计算-->
          <el-dropdown-item
            @click.prevent
            v-if="chart.type !== 'table-info' && props.type !== 'extBubble'"
          >
            <el-dropdown
              placement="right-start"
              :effect="themes"
              style="width: 100%; height: 100%"
              @command="quickCalc"
            >
              <span class="el-dropdown-link inner-dropdown-menu menu-item-padding">
                <span class="menu-item-content">
                  <el-icon>
                    <!--                    <Icon name="icon_describe_outlined" />-->
                  </el-icon>
                  <span>{{ t('chart.quick_calc') }}</span>
                  <span class="summary-span-item">
                    ({{
                      !item.compareCalc ? t('chart.none') : t('chart.' + item.compareCalc.type)
                    }})
                  </span>
                </span>
                <el-icon>
                  <Icon name="icon_right_outlined" />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu
                  :effect="themes"
                  class="drop-style sub"
                  :class="themes === 'dark' ? 'dark-dimension-quota' : ''"
                >
                  <el-dropdown-item class="menu-item-padding" :command="beforeQuickCalc('none')">
                    <span
                      class="sub-menu-content"
                      :class="'none' === item.compareCalc.type ? 'content-active' : ''"
                    >
                      {{ t('chart.none') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon name="icon_done_outlined" v-if="'none' === item.compareCalc.type" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    :disabled="state.disableEditCompare"
                    :command="beforeQuickCalc('setting')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'yoy_label' === item.compareCalc.type ? 'content-active' : ''"
                    >
                      {{ t('chart.yoy_label') }}...
                      <el-icon class="sub-menu-content--icon">
                        <Icon
                          name="icon_done_outlined"
                          v-if="'yoy_label' === item.compareCalc.type"
                        />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    class="menu-item-padding"
                    :disabled="state.quotaViews.indexOf(chart.type) > -1"
                    :command="beforeQuickCalc('percent')"
                  >
                    <span
                      class="sub-menu-content"
                      :class="'percent' === item.compareCalc.type ? 'content-active' : ''"
                    >
                      {{ t('chart.percent') }}
                      <el-icon class="sub-menu-content--icon">
                        <Icon
                          name="icon_done_outlined"
                          v-if="'percent' === item.compareCalc.type"
                        />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>

          <el-dropdown-item
            @click.prevent
            v-if="
              props.type !== 'extLabel' && props.type !== 'extTooltip' && props.type !== 'extBubble'
            "
            :divided="chart.type !== 'table-info'"
          >
            <el-dropdown
              :effect="themes"
              placement="right-start"
              style="width: 100%; height: 100%"
              @command="sort"
            >
              <span class="el-dropdown-link inner-dropdown-menu menu-item-padding">
                <span class="menu-item-content">
                  <el-icon>
                    <Icon name="icon_sort_outlined" />
                  </el-icon>
                  <span>{{ t('chart.sort') }}</span>
                  <span class="summary-span-item">({{ t('chart.' + item.sort) }})</span>
                </span>
                <el-icon>
                  <Icon name="icon_right_outlined" />
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
                        <Icon name="icon_done_outlined" v-if="'none' === item.sort" />
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
                        <Icon name="icon_done_outlined" v-if="'asc' === item.sort" />
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
                        <Icon name="icon_done_outlined" v-if="'desc' === item.sort" />
                      </el-icon>
                    </span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-dropdown-item>

          <el-dropdown-item
            class="menu-item-padding"
            v-if="
              props.type !== 'extLabel' && props.type !== 'extTooltip' && props.type !== 'extBubble'
            "
            :icon="Filter"
            :command="beforeClickItem('filter')"
          >
            <span>{{ t('chart.filter') }}...</span>
          </el-dropdown-item>

          <el-dropdown-item
            class="menu-item-padding"
            v-if="item.groupType === 'q' && props.type !== 'extBubble' && showValueFormatter"
            :divided="chart.type !== 'table-info'"
            :command="beforeClickItem('formatter')"
          >
            <el-icon />
            <span>{{ t('chart.value_formatter') }}...</span>
          </el-dropdown-item>

          <el-dropdown-item class="menu-item-padding" :command="beforeClickItem('rename')">
            <el-icon>
              <icon name="icon_edit_outlined"></icon>
            </el-icon>
            <span>{{ t('chart.show_name_set') }}</span>
          </el-dropdown-item>
          <el-dropdown-item class="menu-item-padding" :command="beforeClickItem('remove')">
            <el-icon>
              <icon name="icon_delete-trash_outlined"></icon>
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
  background-color: #04b49c0a;
  border: 1px solid #04b49c;
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
    color: #3370ff;
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
  max-width: 180px;
  color: #1f2329;
  margin-left: 4px;

  .item-name {
    flex: 1;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .item-right-summary {
    flex-shrink: 0;
    margin-left: 4px;
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
    .inner-dropdown-menu {
      color: rgba(235, 235, 235, 1);
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
  right: 26px;
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
  max-width: 150px;
}
</style>
<style lang="less">
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
    color: #3370ff;
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
  span {
    color: #ebebeb;
  }
  .ed-icon {
    color: #a6a6a6;
  }

  .sub-menu-content--icon {
    color: #3370ff;
    margin-right: -7px !important;
  }
}
</style>
