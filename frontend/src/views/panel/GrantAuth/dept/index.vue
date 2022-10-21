<template>
  <el-col>
    <el-row class="tree-head">
      <span style="float: left;padding-left: 10px">{{ $t('panel.all_org') }}</span>

    </el-row>
    <el-row style="margin-top: 5px">

      <el-tree
        ref="tree"
        :data="treeData"
        lazy
        :load="loadTree"
        style="width: 100%"
        :props="defaultProps"
        :default-expanded-keys="expandNodeIds"
        node-key="deptId"
      >
        <span
          slot-scope="{ node, data }"
          class="custom-tree-node"
        >
          <span>
            <span style="margin-left: 6px">{{ node.data.name }}</span>
          </span>
          <span @click.stop>

            <div>
              <span class="auth-span">
                <el-checkbox
                  v-model="data.checked"
                  @change="nodeStatusChange(data)"
                />
              </span>
            </div>
          </span>
        </span>
      </el-tree>
    </el-row>
  </el-col>
</template>

<script>
import { getDeptTree, loadTable } from '@/api/system/dept'
import { loadShares } from '@/api/panel/share'
export default {
  name: 'GrantDept',
  props: {
    resourceId: {
      type: String,
      default: null
    },
    keyWord: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      treeData: [],
      defaultCondition: {
        field: 'pid',
        operator: 'eq',
        value: 0
      },
      type: 2, // 类型2代表组织
      shares: [],
      changeIndex: 0,
      timeMachine: null,
      defaultProps: {
        children: 'children',
        label: 'name',
        isLeaf: (data, node) => {
          return !data.hasChildren
        }
      },
      expandNodeIds: [],
      sharesLoad: false
    }
  },
  watch: {
    keyWord(v, o) {
      this.destroyTimeMachine()
      this.changeIndex++
      this.searchWithKey(this.changeIndex)
    }
  },
  created() {
    this.search()
  },
  methods: {
    // 根据关键字搜索
    // 1500ms内 key不发生变化则执行查询
    searchWithKey(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.changeIndex) {
          const condition = {
            field: 'name',
            operator: 'like',
            value: this.keyWord
          }
          this.search(condition)
        }
        this.destroyTimeMachine()
      }, 1500)
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    loadTree(node, resolve) {
      if (!node || !node.data || !node.data.deptId) return
      getDeptTree(node.data.deptId).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })
        this.setCheckExpandNodes(data)
        resolve && resolve(data)
      })
    },

    // 加载表格数据
    search(condition) {
      this.treeData = []
      let param = {}
      if (condition && condition.value) {
        param = { conditions: [condition] }
      } else {
        param = { conditions: [this.defaultCondition] }
      }
      if (!this.sharesLoad) {
        this.queryShareNodeIds(() => {
          this.sharesLoad = true
          this.loadTreeData(param, condition)
        })
      } else {
        this.loadTreeData(param, condition)
      }
    },

    loadTreeData(param, condition) {
      loadTable(param).then(res => {
        let data = res.data
        data = data.map(obj => {
          if (obj.subCount > 0) {
            obj.hasChildren = true
          }
          return obj
        })

        this.setCheckExpandNodes(data)
        this.expandNodeIds = []
        if (condition && condition.value) {
          this.treeData = this.buildTree(data)
          this.$nextTick(() => {
            this.expandResult(this.treeData)
          })
        } else {
          this.treeData = data
        }
      })
    },

    expandResult(list) {
      list.forEach(node => {
        if (node.children && node.children.length > 0) {
          this.expandNodeIds.push(node.deptId)
          this.expandResult(node.children)
        }
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el.deptId] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点
        if (el.pid === null || el.pid === 0) {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el.pid]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]
      })
      return roots
    },

    getSelected() {
      return {
        deptIds: this.shares
      }
    },

    cancel() {
    },

    buildRequest(rows) {
      const targetIds = rows.map(row => row.deptId)
      const panelIds = [this.resourceId]
      return {
        targetIds: targetIds,
        panelIds: panelIds,
        type: this.type
      }
    },

    queryShareNodeIds(callBack) {
      const param = { resourceId: this.resourceId, type: this.type }
      loadShares(param).then(res => {
        const shares = res.data
        const nodeIds = shares.map(share => share.targetId)
        this.shares = nodeIds

        callBack && callBack()
      })
    },

    setCheckExpandNodes(rows) {
      rows.forEach(node => {
        const nodeId = node.deptId
        this.shares.includes(nodeId) && (node.checked = true)
      })
    },

    nodeStatusChange(val) {
      if (val.checked) {
        if (!this.shares.includes(val.deptId)) {
          this.shares.push(val.deptId)
        }
      } else {
        this.shares = this.shares.filter(deptId => deptId !== val.deptId)
      }
    }

  }
}
</script>

<style scoped>

.custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-left: 8px;
  }
  .tree-main{
    height:  calc(100vh - 210px);
    overflow-y: auto;
  }
  /* .tree-head{
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid #e6e6e6;
    background-color: #f7f8fa;
    font-size: 12px;
    color: #3d4d66 ;
  } */
  .tree-head{
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
    background-color: var(--SiderBG, #f7f8fa);
    font-size: 12px;
    color: var(--TableColor, #3d4d66) ;
  }

  .auth-span{
    float: right;
    width:50px;
    margin-right: 30px
  }
  .highlights-text {
    color: #faaa39 !important;
  }

.my_table ::v-deep .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table ::v-deep .el-table::before{
  /* 去除下边框 */
  height: 0;
}

.my_table::v-deep.el-table-column--selection .cell{
  text-align: center;
}
</style>
