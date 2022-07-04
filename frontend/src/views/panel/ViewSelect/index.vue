<template>
  <el-col v-loading="loading">
    <el-row style="margin-top: 5px">
      <el-row style="margin-left: 5px;margin-right: 5px">
        <el-col :span="17">
          <el-input
            v-model="templateFilterText"
            :placeholder="$t('panel.filter_keywords')"
            size="mini"
            clearable
            prefix-icon="el-icon-search"
          />
        </el-col>
        <el-col v-if="!selectModel" :span="7">
          <el-button type="primary" size="mini" style="float: right" @click="newChart">新建 </el-button>
        </el-col>

      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="defaultExpandedKeys"
          :show-checkbox="selectModel"
          :data="data"
          node-key="id"
          :props="defaultProps"
          draggable
          :allow-drop="allowDrop"
          :allow-drag="allowDrag"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-drag-start="handleDragStart"
          @check="checkChanged"
          @node-drag-end="dragEnd"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node-list father">
            <span style="display: flex; flex: 1 1 0%; width: 0px;">
              <span v-if="data.modelInnerType==='history'">
                <i class="el-icon-collection" />
              </span>
              <span v-else-if="data.nodeType === 'spine'">
                <i class="el-icon-folder" />
              </span>
              <span v-else-if="data.modelType==='panel'&& data.nodeType === 'leaf'">
                <svg-icon icon-class="panel" class="ds-icon-scene" />
              </span>
              <span v-else>
                <svg-icon :icon-class="data.isPlugin && data.modelInnerType && data.modelInnerType !== 'buddle-map' ? ('/api/pluginCommon/staticInfo/' + data.modelInnerType + '/svg') : data.modelInnerType" style="width: 14px;height: 14px" />
              </span>
              <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
            </span>
            <span v-if="data.mode===1" class="child">
              <span @click.stop>
                <el-button
                  icon="el-icon-delete"
                  type="text"
                  size="small"
                  @click="deleteHistory(data, node)"
                />
              </span>
            </span>
          </span>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import componentList from '@/components/canvas/custom-component/component-list'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import { mapState } from 'vuex'
import { queryPanelViewTree } from '@/api/panel/panel'
import { deleteCircle } from '@/api/chart/chart'
import { pluginTypes } from '@/api/chart/chart'
import { matrixBaseChange } from '@/components/canvas/utils/utils'

export default {
  name: 'ViewSelect',
  props: {
    selectModel: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      templateFilterText: '',
      defaultExpandedKeys: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        disabled: 'disabled'
      },
      data: [],
      showdetail: false,
      detailItem: null,
      loading: false,
      plugins: null
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  watch: {
    templateFilterText(val) {
      this.$refs.templateTree.filter(val)
    }
  },
  created() {
    this.plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views'))
    if (this.plugins) {
      this.loadData()
    } else {
      pluginTypes().then(res => {
        this.plugins = res.data
        localStorage.setItem('plugin-views', JSON.stringify(res.data))
        this.loadData()
      }).catch(e => {
        localStorage.setItem('plugin-views', null)
        this.plugins = null
        this.loadData()
      })
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    loadData() {
      this.loading = true
      queryPanelViewTree().then(res => {
        const nodeDatas = res.data
        if (this.selectModel) {
          this.setParentDisable(nodeDatas)
        }
        this.data = nodeDatas
        this.loading = false
      })
    },
    handleDragStart(node, ev) {
      this.$store.commit('setDragComponentInfo', this.viewComponentInfo())
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'view',
        id: node.data.innerId
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
      eventBus.$emit('startMoveIn')
    },
    dragEnd() {
      this.$store.commit('clearDragComponentInfo')
    },
    // 判断节点能否被拖拽
    allowDrag(draggingNode) {
      if (draggingNode.data.modelType === 'panel' || draggingNode.data.nodeType === 'spine') {
        return false
      } else {
        return true
      }
    },
    allowDrop(draggingNode, dropNode, type) {
      return false
    },
    newChart() {
      this.$emit('newChart')
    },

    checkChanged(node, status) {
      this.$refs.templateTree.setCheckedNodes([])
      if (status.checkedKeys && status.checkedKeys.length > 0) {
        this.$refs.templateTree.setCheckedNodes([node])
      }
    },
    getCurrentSelected() {
      const nodes = this.$refs.templateTree.getCheckedNodes(true, false)
      return nodes
    },
    setParentDisable(nodes) {
      nodes.forEach(node => {
        if (node.modelType === 'panel' || node.nodeType === 'spine') {
          node.disabled = true
        }

        if (node.modelType === 'view' && node.modelInnerType && this.plugins && this.plugins.length) {
          node.isPlugin = this.plugins.some(plugin => plugin.value === node.modelInnerType)
        }

        if (node.children && node.children.length > 0) {
          this.setParentDisable(node.children)
        }
      })
    },
    viewComponentInfo() {
      let component
      // 用户视图设置 复制一个模板
      componentList.forEach(componentTemp => {
        if (componentTemp.type === 'view') {
          component = matrixBaseChange(deepCopy(componentTemp))
        }
      })
      component.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      component.moveStatus = 'start'
      return component
    },
    deleteHistory(data, node) {
      deleteCircle(data.id).then(() => {
        this.$success(this.$t('commons.delete_success'))
        this.remove(node, data)
        // this.loadData()
      })
    },
    remove(node, data) {
      const parent = node.parent
      const children = parent.data.children || parent.data
      const index = children.findIndex(d => d.id === data.id)
      children.splice(index, 1)
    }
  }
}
</script>

<style lang="scss" scoped>
  .top-div-class {
    max-height: calc(100vh - 335px);
    width: 100%;
    position: fixed;
    overflow-y : auto
  }
  .detail-class {
    width: 300px;
    position: fixed;
    bottom: 0px;
  }
  .view-list-thumbnails {
    width: 100%;
    height: 100%;
  }

  .father .child {
    /*display: none;*/
    visibility: hidden;
  }
  .father:hover .child {
    /*display: inline;*/
    visibility: visible;
  }

  .custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }
</style>
