<template>
  <div class="flex-table">
    <el-table
      ref="table"
      v-bind="$attrs"
      height="2000"
      :data="tableData"
      :style="{ width: '100%' }"
      v-on="tableEvent"
    >
      <table-body :columns="columns">
        <slot />
      </table-body>
      <slot name="__operation" />
    </el-table>
    <div
      v-if="showPagination"
      class="pagination-cont"
    >
      <el-pagination
        background
        v-bind="paginationDefault"
        v-on="paginationEvent"
      />
    </div>
  </div>
</template>

<script>
import tableBody from './TableBody'
export default {
  components: { tableBody },
  props: {
    columns: {
      type: Array,
      default: () => []
    },
    showPagination: {
      type: Boolean,
      default: true
    },
    multipleSelection: {
      type: Array,
      default: () => []
    },
    pagination: {
      type: Object,
      default: () => {}
    },
    isRememberSelected: {
      type: Boolean,
      default: false
    },
    selectedFlags: {
      type: String,
      default: 'id'
    },
    tableData: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      paginationEvent: {},
      paginationDefault: {
        currentPage: 1,
        pageSizes: [10, 20, 50, 100],
        pageSize: 10,
        layout: 'total, prev, pager, next, sizes, jumper',
        total: 0
      },
      multipleSelectionCache: [],
      tableEvent: {}
    }
  },
  computed: {
    multipleSelectionAll() {
      return [...this.multipleSelectionCache, ...this.multipleSelection]
    }
  },
  watch: {
    pagination: {
      handler() {
        this.paginationDefault = {
          ...this.paginationDefault,
          ...this.pagination
        }
      },
      deep: true,
      immediate: true
    },
    tableData: {
      handler() {
        this.$nextTick(() => {
          this.$refs.table.doLayout()
        })
        if (!this.isRememberSelected) return
        // 先拷贝 重新加载数据会触发SelectionChange 导致this.multipleSelection为空
        const multipleSelection = [...this.multipleSelection]
        this.$nextTick(() => {
          this.handlerSelected(multipleSelection)
        })
      },
      deep: true
    },
    columns: {
      handler() {
        this.$nextTick(() => {
          this.$refs.table.doLayout()
          this.$emit('columnsChange')
        })
      },
      deep: true
    }
  },
  created() {
    this.handleListeners()
  },
  methods: {
    toggleRowSelection(row) {
      this.$refs.table.toggleRowSelection(row, true)
    },
    handlerSelected(multipleSelection) {
      this.multipleSelectionCache = [
        ...this.multipleSelectionCache,
        ...multipleSelection
      ]
      const flags = this.multipleSelectionCache.map(
        (ele) => ele[this.selectedFlags]
      )
      // 当前页的选中项索引
      const notCurrentArr = []
      this.tableData.forEach((ele) => {
        const resultIndex = flags.indexOf(ele[this.selectedFlags])
        if (resultIndex !== -1) {
          this.$refs.table.toggleRowSelection(ele, true)
          notCurrentArr.push(resultIndex)
        }
      })
      notCurrentArr.sort().reduceRight((pre, next) => {
        this.multipleSelectionCache.splice(next, 1)
      }, 0)
    },
    handleListeners() {
      Object.keys(this.$listeners).forEach((key) => {
        if (
          [
            'size-change',
            'current-change',
            'prev-click',
            'next-click'
          ].includes(key)
        ) {
          this.paginationEvent[key] = this.$listeners[key]
        } else {
          this.tableEvent[key] = this.$listeners[key]
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.flex-table {
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: space-between;

  ::v-deep.el-table-column--selection .cell {
    padding: 0 14px;
  }

  .el-table::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    z-index: 10;
    border-top: 1px solid rgba(31, 35, 41, 0.15);;
  }

  ::v-deep .el-table__fixed-body-wrapper {
    tr {
        background-color: var(--TableBG, #ffffff) !important;
    }
  }

  ::v-deep .el-table__fixed-right::before {
    display: none;
  }
  .pagination-cont {
    text-align: right;
    margin-top: 10px;
    ::v-deep .el-pager li {
      background-color: #fff;
      border: 1px solid #bbbfc4;
      border-radius: 4px;
      color: #1f2329;
      box-sizing: border-box;
      line-height: 26px;
      font-family: SF Pro Text;
      font-size: 14px;
      font-weight: 400;
    }

    ::v-deep .btn-prev,
    ::v-deep .btn-next {
      background: #fff;
      background-color: #fff;
      border: 1px solid #bbbfc4;
      border-radius: 4px;
      color: #bbbfc4;
    }

    ::v-deep .el-pagination__total {
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 22px;
      color: #1f2329;
      line-height: 28px;
    }

    ::v-deep .number.active,
    ::v-deep .el-input__inner:hover {
      border-color: #3370ff;
      color: #3370ff !important;
      background-color: #fff !important;
    }

    ::v-deep .el-icon-more {
      border: none !important;
    }
  }
}
</style>
