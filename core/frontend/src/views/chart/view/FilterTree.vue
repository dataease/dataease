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
              <rowAuth ref="rowAuth" />
            </div>
          </div>
          <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          @click="closeFilter"
        >{{ $t('chart.cancel') }}
        </el-button>
        <el-button
          type="primary"
          @click="changeFilter"
        >{{ $t('chart.confirm') }}
        </el-button>
      </div>
    </el-dialog>
</template>
<script>
import rowAuth from '@/views/dataset/data/components/rowAuth.vue'
export default {
  name: 'FilterTree',
  components: {
    rowAuth
  },
    data() {
        return {
            dialogVisible: false,
        }
    },
    methods: {
        closeFilter() {
            this.dialogVisible = false
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
            this.$emit('filter-data', { logic, items })
            this.dialogVisible = false
        },
        init(tree) {
          this.dialogVisible = true
          this.$nextTick(() => {
            this.$refs.rowAuth.init(tree || {})
            })
        }
    }
}
</script>

<style lang="scss">
.filter-tree-cont {
  .tree-cont {
      height: 200px;
      width: 100%;
      padding: 16px;
      border-radius: 4px;
      border: 1px solid var(--deBorderBase, #DCDFE6);
      overflow: auto;
  
      .content {
        height: 100%;
        width: 100%;
      }
    }
}
</style>