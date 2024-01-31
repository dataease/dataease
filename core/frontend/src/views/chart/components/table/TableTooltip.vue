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
      this.table.emit('sort:range-sort', {
        sortFieldId: this.meta.field,
        sortMethod: type
      })
    },
    __t(key) {
      return i18n.t(key)
    }
  }
}
</script>
<style scoped>
.sort-btn {
  cursor: pointer;
}
</style>
