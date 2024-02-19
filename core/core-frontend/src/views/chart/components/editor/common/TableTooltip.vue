<script setup lang="ts">
import { SpreadSheet, Node } from '@antv/s2'
import { PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { S2Event } from '@antv/s2'
import { SortUp, SortDown, Sort } from '@element-plus/icons-vue'

const { t } = useI18n()
const props = defineProps({
  table: {
    type: Object as PropType<SpreadSheet>,
    required: true
  },
  meta: {
    type: Object as PropType<Node>
  }
})
const sort = type => {
  props.table.updateSortMethodMap(props.meta.field, type, true)
  props.table.emit(S2Event.RANGE_SORT, [
    {
      sortFieldId: props.meta.field,
      sortMethod: type
    }
  ])
}
</script>
<template>
  <el-col>
    <el-row align="middle">
      <el-icon><SortUp /></el-icon>
      <span class="sort-btn" @click="() => sort('asc')">{{ t('chart.asc') }}</span>
    </el-row>
    <el-row align="middle">
      <el-icon><SortDown /></el-icon>
      <span class="sort-btn" @click="() => sort('desc')">{{ t('chart.desc') }}</span>
    </el-row>
    <el-row align="middle">
      <el-icon><Sort /></el-icon>
      <span class="sort-btn" @click="() => sort()">{{ t('chart.default') }}</span>
    </el-row>
  </el-col>
</template>
<style scoped>
.sort-btn {
  cursor: pointer;
}
</style>
