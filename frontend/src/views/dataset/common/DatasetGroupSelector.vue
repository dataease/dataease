<template>
  <el-col>
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
              v-model="filterText"
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
            ref="tree"
            :default-expanded-keys="expandedArray"
            :data="data"
            node-key="id"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node">
              <span style="display: flex;flex: 1;width: 0;">
                <span v-if="data.type === 'scene'">
                  <!--                  <el-button-->
                  <!--                    icon="el-icon-folder-opened"-->
                  <!--                    type="text"-->
                  <!--                    size="mini"-->
                  <!--                  />-->
                  <svg-icon icon-class="scene" class="ds-icon-scene" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>
    </el-col>

    <!--scene-->
    <el-col v-if="sceneMode" v-loading="dsLoading">
      <el-row class="title-css scene-title">
        <span class="title-text scene-title-name" :title="currGroup.name">
          {{ currGroup.name }}
        </span>
        <el-button icon="el-icon-back" size="mini" style="float: right" circle @click="back">
          <!--          {{ $t('dataset.back') }}-->
        </el-button>
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
            :data="tableData"
            node-key="id"
            :expand-on-click-node="true"
            class="tree-list"
            highlight-current
            @node-click="sceneClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node-list">
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

export default {
  name: 'DatasetGroupSelector',
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
      treeStyle: this.fixHeight ? {
        height: '200px',
        overflow: 'auto'
      } : {},
      filterText: ''
    }
  },
  computed: {},
  watch: {
    'unionData': function() {
      this.unionDataChange()
    },
    'table': function() {
      if (this.table && this.table.sceneId) {
        post('dataset/group/getScene/' + this.table.sceneId, {}, false).then(response => {
          this.currGroup = response.data

          this.$nextTick(function() {
            this.sceneMode = true
            this.tableTree()
          })
        })
      }
    },
    search(val) {
      if (val && val !== '') {
        this.tableData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) })))
      } else {
        this.tableData = JSON.parse(JSON.stringify(this.tables))
      }
    },
    filterText(val) {
      this.$refs.tree.filter(val)
    }
  },
  mounted() {
    this.tree(this.groupForm)
    this.tableTree()
  },
  created() {
    this.kettleState()
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
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

    tree(group) {
      this.dsLoading = true
      post('/dataset/group/tree', group, false).then(response => {
        this.data = response.data
        this.dsLoading = false
      })
    },

    tableTree() {
      this.tableData = []
      if (this.currGroup) {
        this.dsLoading = true
        post('/dataset/table/list', {
          sort: 'type asc,name asc,create_time desc',
          sceneId: this.currGroup.id,
          mode: this.mode < 0 ? null : this.mode,
          typeFilter: this.customType ? this.customType : null
        }, false).then(response => {
          this.tables = response.data.filter(ele => {
            return !(ele.mode === 0 && ele.type === 'sql')
          })
          for (let i = 0; i < this.tables.length; i++) {
            if (this.tables[i].mode === 1 && this.kettleRunning === false) {
              this.$set(this.tables[i], 'disabled', true)
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
      // if (data.type === 'scene') {
      this.filterText = ''
      this.sceneMode = true
      this.currGroup = data
      this.tableTree()
      // }
      // if (node.expanded) {
      //   this.expandedArray.push(data.id)
      // } else {
      //   const index = this.expandedArray.indexOf(data.id)
      //   if (index > -1) {
      //     this.expandedArray.splice(index, 1)
      //   }
      // }
      // console.log(this.expandedArray);
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
</style>
