<template>
  <el-col>
    <el-row>
      <i class="el-icon-sort-up" />
      <span
        class="sort-btn"
        @click="() => sort('asc')"
      >{{ __t('chart.asc') }}</span>
    </el-row>
    <el-row>
      <i class="el-icon-sort-down" />
      <span
        class="sort-btn"
        @click="() => sort('desc')"
      >{{ __t('chart.desc') }}</span>
    </el-row>
    <el-row>
      <i class="el-icon-sort" />
      <span
        class="sort-btn"
        @click="() => sort()"
      >{{ __t('chart.default') }}</span>
    </el-row>
  </el-col>
</template>
<script>
import i18n from '@/lang'
import { S2Event } from '@antv/s2'
import { cloneDeep } from 'lodash'
export default {
  name: 'TableTooltip',
  props: {
    table: {
      type: Object,
      required: true
    },
    meta: {
      type: Object,
      required: true
    }
  },
  methods: {
    sort(type) {
      this.table.updateSortMethodMap(this.meta.field, type, true)
      this.table.emit(S2Event.RANGE_SORT, [{
        sortFieldId: this.meta.field,
        sortMethod: type,
        sortFunc: this.sortFunc,
        meta: this.meta
      }])
    },
    __t(key) {
      return i18n.t(key)
    },
    sortFunc(sortParams) {
      if (!sortParams.sortMethod) {
        return sortParams.data
      }
      const data = cloneDeep(sortParams.data)
      return data.sort((a, b) => {
        if (a === b) {
          return 0
        }
        if (a.SUMMARY) {
          return 1
        }
        if (sortParams.sortMethod === 'asc') {
          if (typeof a === 'number') {
            return a - b
          }
          return a < b ? 1 : -1
        }
        if (typeof a === 'number') {
          return b - a
        }
        return a > b ? 1 : -1
      })
    }
  }
}
</script>
<style scoped>
.sort-btn {
  cursor: pointer;
}
</style>
