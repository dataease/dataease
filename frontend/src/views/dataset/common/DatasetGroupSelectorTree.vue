<template>
  <el-col class="tree-style">
    <!-- group -->
    <el-col v-if="!sceneMode" v-loading="dsLoading">
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('dataset.datalist') }}
        </span>
      </el-row>
      <el-divider />

      <el-row>
        <el-form>
          <el-form-item class="form-item">
            <el-input
              v-model="search"
              size="mini"
              :placeholder="$t('dataset.search')"
              prefix-icon="el-icon-search"
              clearable
            />
          </el-form-item>
        </el-form>
      </el-row>

      <el-col class="custom-tree-container">
        <div class="block" :style="treeStyle">
          <el-tree
            :default-expanded-keys="expandedArray"
            :data="data"
            node-key="id"
            :expand-on-click-node="true"
            :load="loadNode"
            lazy
            :props="treeProps"
            highlight-current
            @node-click="nodeClick"
          >
            <span v-if="data.type === 'group'" slot-scope="{ node, data }" class="custom-tree-node">
              <span style="display: flex;flex: 1;width: 0;">
                <span v-if="data.type === 'scene'">
                  <svg-icon icon-class="scene" class="ds-icon-scene" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
              </span>
            </span>
            <span v-else slot-scope="{ node, data }" class="custom-tree-node-list">
              <span :id="data.id" style="display: flex;flex: 1;width: 0;">
                <span>
                  <svg-icon v-if="data.type === 'db'" icon-class="ds-db" class="ds-icon-db" />
                  <svg-icon v-if="data.type === 'sql'" icon-class="ds-sql" class="ds-icon-sql" />
                  <svg-icon v-if="data.type === 'excel'" icon-class="ds-excel" class="ds-icon-excel" />
                  <svg-icon v-if="data.type === 'custom'" icon-class="ds-custom" class="ds-icon-custom" />
                </span>
                <span v-if="data.type === 'db' || data.type === 'sql'">
                  <span v-if="data.mode === 0" style="margin-left: 6px"><i class="el-icon-s-operation" /></span>
                  <span v-if="data.mode === 1" style="margin-left: 6px"><i class="el-icon-alarm-clock" /></span>
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>
    </el-col>
  </el-col>
</template>

<script>
import { isKettleRunning, post } from '@/api/dataset/dataset'
import { hasDataPermission } from '@/utils/permission'

export default {
  name: 'DatasetGroupSelectorTree',
  props: {
    fixHeight: {
      type: Boolean,
      required: false,
      default: false
    },
    customType: {
      type: Array,
      required: false,
      default: null
    },
    mode: {
      type: Number,
      required: false,
      default: -1
    },
    type: {
      type: String,
      required: false,
      default: null
    },
    unionData: {
      type: Array,
      required: false,
      default: null
    },
    checkedList: {
      type: Array,
      required: false,
      default: null
    },
    table: {
      type: Object,
      required: false,
      default: null
    },
    showMode: {
      type: String,
      required: false,
      default: null
    },
    privileges: {
      type: String,
      required: false,
      default: 'use'
    }
  },
  data() {
    return {
      kettleRunning: false,
      sceneMode: false,
      search: '',
      data: [],
      tableData: [],
      tables: [],
      currGroup: null,
      expandedArray: [],
      groupForm: {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      tableForm: {
        name: '',
        sort: 'type asc,create_time desc,name asc'
      },
      dsLoading: false,
      treeProps: {
        label: 'name',
        children: 'children',
        isLeaf: 'isLeaf',
        id: 'id',
        parentId: 'pid'
      },
      isTreeSearch: false,
      treeStyle: this.fixHeight ? {
        height: '200px',
        overflow: 'auto'
      } : {}
    }
  },
  computed: {},
  watch: {
    'unionData': function() {
      this.unionDataChange()
    },
    'table': function() {
      // if (this.table && this.table.sceneId) {
      //   post('dataset/group/getScene/' + this.table.sceneId, {}, false).then(response => {
      //     this.currGroup = response.data
      //
      //     this.$nextTick(function() {
      //       this.sceneMode = true
      //       this.tableTree()
      //     })
      //   })
      // }
      this.treeNode(this.groupForm)
    },
    search(val) {
      this.$emit('switchComponent', { name: '' })
      this.data = []
      this.expandedArray = []
      if (this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.getTreeData(val)
      }, (val && val !== '') ? 500 : 0)
    }
  },
  mounted() {
    this.treeNode(this.groupForm)
  },
  created() {
    this.kettleState()
  },
  methods: {
    kettleState() {
      isKettleRunning(false).then(res => {
        this.kettleRunning = res.data
      })
    },
    close() {
      this.editGroup = false
      this.groupForm = {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      }
    },

    closeTable() {
      this.editTable = false
      this.tableForm = {
        name: ''
      }
    },

    // tree(group) {
    //   this.dsLoading = true
    //   post('/dataset/group/tree', group, false).then(response => {
    //     this.data = response.data
    //     this.dsLoading = false
    //   })
    // },

    treeNode(group) {
      post('/dataset/group/treeNode', group).then(res => {
        this.data = res.data
        this.dsLoading = false
      })
    },

    tableTree() {
      this.tableData = []
      if (this.currGroup) {
        this.dsLoading = true
        this.tables = []
        post('/dataset/table/list', {
          sort: 'type asc,name asc,create_time desc',
          sceneId: this.currGroup.id,
          mode: this.mode < 0 ? null : this.mode,
          typeFilter: this.customType ? this.customType : null
        }, false).then(response => {
          for (let i = 0; i < response.data.length; i++) {
            if (response.data[i].mode === 1 && this.kettleRunning === false) {
              this.$set(response.data[i], 'disabled', true)
            }
            if (hasDataPermission(this.privileges, response.data[i].privileges)) {
              this.tables.push(response.data[i])
            }
          }
          this.tableData = JSON.parse(JSON.stringify(this.tables))

          this.$nextTick(function() {
            this.unionDataChange()
          })
          this.dsLoading = false
        }).catch(res => {
          this.dsLoading = false
        })
      }
    },

    nodeClick(data, node) {
      if (data.type !== 'group') {
        this.sceneClick(data, node)
      }
    },

    back() {
      this.sceneMode = false
    },

    sceneClick(data, node) {
      if (data.disabled) {
        this.$message({
          type: 'warning',
          message: this.$t('dataset.invalid_dataset'),
          showClose: true
        })
        return
      }
      // check mode=1的数据集是否创建doris表
      if (data.mode === 1 && !this.showMode) {
        post('/dataset/table/checkDorisTableIsExists/' + data.id, {}, false).then(response => {
          if (response.data) {
            this.$nextTick(function() {
              this.$emit('getTable', data)
            })
          } else {
            this.$message({
              type: 'error',
              message: this.$t('dataset.invalid_table_check'),
              showClose: true
            })
            this.$emit('getTable', {})
          }
        })
      } else {
        this.$emit('getTable', data)
      }
    },

    unionDataChange() {
      if (!this.sceneMode) {
        return
      }
      if (!this.checkedList || this.checkedList.length === 0) {
        this.tableData.forEach(ele => {
          const span = document.getElementById(ele.id).parentNode
          const div1 = span.parentNode
          const div2 = div1.parentNode
          span.style.removeProperty('color')
          div1.style.removeProperty('cursor')
          div2.style.removeProperty('pointer-events')
        })
        return
      }
      const tableList = this.tableData.map(ele => {
        return ele.id
      })
      const unionList = this.unionData.map(ele => {
        return ele.targetTableId
      })
      unionList.push(this.checkedList[0].tableId)
      const notUnionList = tableList.concat(unionList).filter(v => tableList.includes(v) && !unionList.includes(v))

      notUnionList.forEach(ele => {
        const span = document.getElementById(ele).parentNode
        const div1 = span.parentNode
        const div2 = div1.parentNode
        span.style.setProperty('color', '#c0c4cc')
        div1.style.setProperty('cursor', 'not-allowed')
        div2.style.setProperty('pointer-events', 'none')
      })
    },

    nodeExpand(data) {
      if (data.id) {
        this.expandedArray.push(data.id)
      }
    },
    nodeCollapse(data) {
      if (data.id) {
        this.expandedArray.splice(this.expandedArray.indexOf(data.id), 1)
      }
    },

    loadNode(node, resolve) {
      if (!this.isTreeSearch) {
        if (node.data.id) {
          this.dsLoading = true
          this.tables = []
          post('/dataset/table/listAndGroup', {
            sort: 'type asc,name asc,create_time desc',
            sceneId: node.data.id,
            mode: this.mode < 0 ? null : this.mode,
            type: this.type,
            typeFilter: this.customType ? this.customType : null
          }, false).then(response => {
            for (let i = 0; i < response.data.length; i++) {
              if (response.data[i].mode === 1 && this.kettleRunning === false) {
                this.$set(response.data[i], 'disabled', true)
              }
              if (hasDataPermission(this.privileges, response.data[i].privileges)) {
                this.tables.push(response.data[i])
              }
            }

            this.tableData = JSON.parse(JSON.stringify(this.tables))
            this.$nextTick(function() {
              this.unionDataChange()
            })
            this.dsLoading = false
            resolve(this.tableData)
          }).catch(res => {
            this.dsLoading = false
          })
        }
      } else {
        node.data.children ? resolve(node.data.children) : resolve([])
      }
    },

    refreshNodeBy(id) {
      if (this.isTreeSearch) {
        this.data = []
        this.expandedArray = []
        this.searchTree(this.search)
      } else {
        if (!id || id === '0') {
          this.treeNode(this.groupForm)
        } else {
          const node = this.$refs.asyncTree.getNode(id) // 通过节点id找到对应树节点对象
          node.loaded = false
          node.expand() // 主动调用展开节点方法，重新查询该节点下的所有子节点
        }
      }
    },

    searchTree(val) {
      const queryCondition = {
        // withExtend: 'parent',
        // modelType: 'dataset',
        name: val
      }
      // authModel(queryCondition).then(res => {
      //   this.data = this.buildTree(res.data)
      // })
      post('/dataset/table/search', queryCondition).then(res => {
        this.data = this.buildTree(res.data)
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el[this.treeProps.id]] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点 ###
        // el.type = el.modelInnerType
        // el.isLeaf = el.leaf
        if (el[this.treeProps.parentId] === null || el[this.treeProps.parentId] === 0 || el[this.treeProps.parentId] === '0') {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el[this.treeProps.parentId]]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]

        // 设置展开节点 如果没有子节点则不进行展开
        if (parentEl.children.length > 0) {
          this.expandedArray.push(parentEl[this.treeProps.id])
        }
      })
      return roots
    },

    // 高亮显示搜索内容
    highlights(data) {
      if (data && this.search && this.search.length > 0) {
        const replaceReg = new RegExp(this.search, 'g')// 匹配关键字正则
        const replaceString = '<span style="color: #0a7be0">' + this.search + '</span>' // 高亮替换v-html值
        data.forEach(item => {
          item.name = item.name.replace(replaceReg, replaceString) // 开始替换
          item.label = item.label.replace(replaceReg, replaceString) // 开始替换
        })
      }
    },

    getTreeData(val) {
      if (val) {
        this.isTreeSearch = true
        this.searchTree(val)
      } else {
        this.isTreeSearch = false
        this.treeNode(this.groupForm)
      }
    }
  }
}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0
  }

  .search-input {
    padding: 12px 0;
  }

  .tree-list>>>.el-tree-node__expand-icon.is-leaf{
    display: none;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }

  .custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }

  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    font-size: 14px;
    flex-flow: row nowrap;
  }

  .form-item {
    margin-bottom: 0;
  }

  .title-css {
    height: 26px;
  }

  .title-text {
    line-height: 26px;
  }

  .scene-title{
    width: 100%;
    display: flex;
  }
  .scene-title-name{
    width: 100%;
    overflow: hidden;
    display: inline-block;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  .tree-style {
    padding: 10px;
    height: 100%;
    overflow-y: auto;
  }
</style>
