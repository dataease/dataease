<template>
    <el-dialog
        width="896px"
        append-to-body
        title="添加过滤"
        destroy-on-close
        class="de-dialog-form filter-tree-cont"
        :visible.sync="dialogVisible"
    >
    <div class="tree-cont">
            <div class="content">
              <rowAuth @execute-axios="executeAxios" ref="rowAuth"></rowAuth>
            </div>
          </div>
          <div
        slot="footer"
        class="dialog-footer"
      >
        <de-btn
          secondary
          @click="closeFilter"
        >{{ $t('chart.cancel') }}
        </de-btn>
        <de-btn
          type="primary"
          @click="changeFilter"
        >{{ $t('chart.confirm') }}
        </de-btn>
      </div>
    </el-dialog>
</template>
<script>
import rowAuth from './rowAuth.vue'
export default {
  name: 'FilterTree',
  inject: ['filedList'],
  components: {
    rowAuth
  },
    data() {
        return {
            dialogVisible: false,
        }
  },
  computed: {
    computedFiledList() {
      return this.filedList().reduce((pre, next) => {
        if (next.id !== 'count') {
          pre[next.id] = next
        }
          return pre
        }, {})
      }
    },
    methods: {
        closeFilter() {
            this.dialogVisible = false
        },executeAxios(url, type, data, callBack) {
      this.$emit("execute-axios", url, type, data, callBack);
    },
        changeFilter() {
            const { logic, items, errorMessage } = this.$refs.rowAuth.submit()
            if (errorMessage) {
              this.$message({
                message: errorMessage,
                type: 'error',
                showClose: true
              })
              return
            }
            this.dfsTreeDelete(items)
            this.$emit('filter-data', { logic, items })
            this.dialogVisible = false
      },
      dfsTreeDelete(arr) {
        arr.forEach((ele) => {
          if (ele?.subTree?.items?.length) {
            this.dfsTreeDelete(ele.subTree.items)
          } else {
            if (ele.field) {
               this.$delete(ele, 'field')
            }
          }
        })
      },
      dfsTree(arr) {
        arr.forEach((ele) => {
          if (ele?.subTree?.items?.length) {
            this.dfsTree(ele.subTree.items)
          } else {
            if (this.computedFiledList[ele.fieldId]) {
              ele.field = this.computedFiledList[ele.fieldId]
            }
          }
        })
      },
      init(tree) {
        this.dialogVisible = true
        this.$nextTick(() => {
          this.dfsTree(tree.items || [])
          this.$refs.rowAuth.init(tree || {})
        })
      }
    }
}
</script>

<style lang="scss">
.filter-tree-cont {
  .tree-cont {
      min-height: 67px;
      width: 100%;
      padding: 16px;
      border-radius: 4px;
      border: 1px solid var(--deBorderBase, #DCDFE6);
      overflow: auto;
      max-height: 500px;
      .content {
        height: 100%;
        width: 100%;
      }
    }
}
</style>