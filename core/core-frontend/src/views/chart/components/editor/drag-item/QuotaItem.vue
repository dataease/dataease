<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { reactive, ref, toRefs, watch } from 'vue'
import { formatterItem } from '../util/formatter'
import { getItemType } from '@/views/chart/components/editor/drag-item/utils'

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

const emit = defineEmits(['onQuotaItemRemove', 'onCustomSort', 'onQuotaItemChange'])

const { item } = toRefs(props)

watch(
  [props.quotaData, props.item],
  () => {
    getItemTagType()
  },
  { deep: true }
)

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

const removeItem = () => {
  item.value.index = props.index
  item.value.removeType = 'dimension'
  emit('onQuotaItemRemove', item.value)
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
            <svg-icon
              v-if="item.sort === 'asc'"
              icon-class="sort-asc"
              class-name="field-icon-sort"
            />
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
          <span class="item-span-style" :title="item.name">{{ item.name }}</span>
        </el-tag>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-show="!item.chartId && chart.type !== 'table-info'"
              :divided="chart.type === 'chart-mix'"
            >
              <el-dropdown
                placement="right-start"
                size="small"
                style="width: 100%"
                @command="summary"
              >
                <span class="el-dropdown-link inner-dropdown-menu">
                  <span>
                    <i class="el-icon-notebook-2" />
                    <span>{{ $t('chart.summary') }}</span>
                    <span class="summary-span-item">({{ $t('chart.' + item.summary) }})</span>
                  </span>
                  <i class="el-icon-arrow-right el-icon--right" />
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('sum')"
                      >{{ $t('chart.sum') }}</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('avg')"
                      >{{ $t('chart.avg') }}</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('max')"
                      >{{ $t('chart.max') }}</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('min')"
                      >{{ $t('chart.min') }}</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('stddev_pop')"
                      >{{ $t('chart.stddev_pop') }}</el-dropdown-item
                    >
                    <el-dropdown-item
                      v-if="
                        item.id !== 'count' &&
                        item.deType !== 0 &&
                        item.deType !== 1 &&
                        item.deType !== 5
                      "
                      :command="beforeSummary('var_pop')"
                      >{{ $t('chart.var_pop') }}</el-dropdown-item
                    >
                    <el-dropdown-item :command="beforeSummary('count')">{{
                      $t('chart.count')
                    }}</el-dropdown-item>
                    <el-dropdown-item
                      v-if="item.id !== 'count'"
                      :command="beforeSummary('count_distinct')"
                      >{{ $t('chart.count_distinct') }}</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-dropdown-item>

            <el-dropdown-item :divided="!item.chartId && chart.type !== 'table-info'">
              <el-dropdown placement="right-start" size="small" style="width: 100%" @command="sort">
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
                    <el-dropdown-item
                      v-show="!item.chartId && (item.deType === 0 || item.deType === 5)"
                      :command="beforeSort('custom_sort')"
                      >{{ t('chart.custom_sort') }}...</el-dropdown-item
                    >
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-dropdown-item>
            <!--          <el-dropdown-item v-show="item.deType === 1" divided>-->
            <!--            <el-dropdown-->
            <!--              placement="right-start"-->
            <!--              size="small"-->
            <!--              style="width: 100%"-->
            <!--              @command="dateStyle"-->
            <!--            >-->
            <!--              <span class="el-dropdown-link inner-dropdown-menu">-->
            <!--                <span>-->
            <!--                  <i class="el-icon-c-scale-to-original" />-->
            <!--                  <span>{{ $t('chart.dateStyle') }}</span>-->
            <!--                  <span class="summary-span-item">({{ $t('chart.' + item.dateStyle) }})</span>-->
            <!--                </span>-->
            <!--                <i class="el-icon-arrow-right el-icon&#45;&#45;right" />-->
            <!--              </span>-->
            <!--              <template #dropdown>-->
            <!--                <el-dropdown-menu>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('y')">{{-->
            <!--                    $t('chart.y')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item v-if="showDateExt" :command="beforeDateStyle('y_Q')">{{-->
            <!--                    $t('chart.y_Q')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('y_M')">{{-->
            <!--                    $t('chart.y_M')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item v-if="showDateExt" :command="beforeDateStyle('y_W')">{{-->
            <!--                    $t('chart.y_W')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('y_M_d')">{{-->
            <!--                    $t('chart.y_M_d')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('H_m_s')" divided>{{-->
            <!--                    $t('chart.H_m_s')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m')">{{-->
            <!--                    $t('chart.y_M_d_H_m')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                  <el-dropdown-item :command="beforeDateStyle('y_M_d_H_m_s')">{{-->
            <!--                    $t('chart.y_M_d_H_m_s')-->
            <!--                  }}</el-dropdown-item>-->
            <!--                </el-dropdown-menu>-->
            <!--              </template>-->
            <!--            </el-dropdown>-->
            <!--          </el-dropdown-item>-->
            <!--          <el-dropdown-item v-show="item.deType === 1">-->
            <!--            <el-dropdown-->
            <!--              placement="right-start"-->
            <!--              size="small"-->
            <!--              style="width: 100%"-->
            <!--              @command="datePattern"-->
            <!--            >-->
            <!--              <span class="el-dropdown-link inner-dropdown-menu">-->
            <!--                <span>-->
            <!--                  <i class="el-icon-timer" />-->
            <!--                  <span>{{ $t('chart.datePattern') }}</span>-->
            <!--                  <span class="summary-span-item">({{ $t('chart.' + item.datePattern) }})</span>-->
            <!--                </span>-->
            <!--                <i class="el-icon-arrow-right el-icon&#45;&#45;right" />-->
            <!--              </span>-->
            <!--              <template #dropdown>-->
            <!--                <el-dropdown-menu>-->
            <!--                  <el-dropdown-item :command="beforeDatePattern('date_sub')"-->
            <!--                    >{{ $t('chart.date_sub') }}(1990-01-01)</el-dropdown-item-->
            <!--                  >-->
            <!--                  <el-dropdown-item :command="beforeDatePattern('date_split')"-->
            <!--                    >{{ $t('chart.date_split') }}(1990/01/01)</el-dropdown-item-->
            <!--                  >-->
            <!--                </el-dropdown-menu>-->
            <!--              </template>-->
            <!--            </el-dropdown>-->
            <!--          </el-dropdown-item>-->

            <!--          <el-dropdown-item-->
            <!--            v-if="chart.render === 'antv' && chart.type.includes('table') && item.groupType === 'q'"-->
            <!--            icon="el-icon-notebook-2"-->
            <!--            divided-->
            <!--            :command="beforeClickItem('formatter')"-->
            <!--          >-->
            <!--            <span>{{ $t('chart.value_formatter') }}...</span>-->
            <!--          </el-dropdown-item>-->
            <!--          <el-dropdown-item-->
            <!--            icon="el-icon-edit-outline"-->
            <!--            divided-->
            <!--            :command="beforeClickItem('rename')"-->
            <!--          >-->
            <!--            <span>{{ $t('chart.show_name_set') }}</span>-->
            <!--          </el-dropdown-item>-->
            <!--          <el-dropdown-item icon="el-icon-delete" divided :command="beforeClickItem('remove')">-->
            <!--            <span>{{ $t('chart.delete') }}</span>-->
            <!--          </el-dropdown-item>-->
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
}

.summary-span-item {
  margin-left: 4px;
  color: #878d9f;
}
</style>
