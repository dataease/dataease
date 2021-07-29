
<template>
  <div class="complex-table">
    <div v-if="$slots.header || header" class="complex-table__header">
      <slot name="header">{{ header }}</slot>
    </div>

    <div v-if="$slots.toolbar || searchConfig" class="complex-table__toolbar">
      <div>
        <slot name="toolbar" />
      </div>
      <fu-search-bar v-bind="searchConfig"  @exec="search" ref="search">
        <template #complex>
          <slot name="complex" />
        </template>
        <slot name="buttons" />
        <fu-table-column-select v-if="!hideColumns" :columns="columns" />
      </fu-search-bar>
    </div>

    <div class="complex-table__body">
      <fu-table v-bind="$attrs" :columns="columns" :local-key="localKey" v-on="$listeners">
        <slot />
      </fu-table>
    </div>

    <div v-if="$slots.pagination || paginationConfig" class="complex-table__pagination">
      <slot name="pagination">
        <fu-table-pagination
          :current-page.sync="paginationConfig.currentPage"
          :page-size.sync="paginationConfig.pageSize"
          v-bind="paginationConfig"
          @change="search"
        />
      </slot>
    </div>
  </div>
</template>

<script>

export default {
  name: 'ComplexTable',
  props: {
    columns: {
      type: Array,
      default: () => []
    },
    hideColumns: {
      type: Boolean,
      default: false
    },
    // eslint-disable-next-line vue/require-default-prop
    localKey: String, // 如果需要记住选择的列，则这里添加一个唯一的Key
    // eslint-disable-next-line vue/require-default-prop
    header: String,
    // eslint-disable-next-line vue/require-default-prop
    searchConfig: Object,
    // eslint-disable-next-line vue/require-default-prop
    paginationConfig: Object,
    transCondition: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      condition: {},
    }
  },
  mounted() {
    if (this.transCondition !== null) {
      this.$refs.search.setConditions(this.transCondition)
    }
  },
  methods: {
    search(condition, e) {
      if (condition) {
        this.condition = condition
      }
      this.$emit('search', this.condition, e)
    }
  }
}
</script>

<style lang="scss">
@import "~@/styles/mixin.scss";
@import "~@/styles/variables.scss";
.complex-table {
  .complex-table__header {
    @include flex-row(flex-start, center);
    line-height: 60px;
    font-size: 18px;
  }

  .complex-table__toolbar {
    @include flex-row(space-between, center);

    .fu-search-bar {
      width: auto;
    }
  }

  .complex-table__pagination {
    margin-top: 20px;
    @include flex-row(flex-end);
  }
}

</style>
