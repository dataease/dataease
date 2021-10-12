<template>
  <el-col v-loading="loading">
    <el-row style="margin-top: 5px">
      <el-row style="margin-left: 5px;margin-right: 5px">
        <el-col :span="selectModel ? 23 : 16">
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
          @node-click="nodeClick"

          @check="checkChanged"

          @node-drag-end="dragEnd"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span>
              <span v-if="data.type==='scene'||data.type==='group'">
                <el-button
                  icon="el-icon-folder"
                  type="text"
                />
              </span>
              <span v-else>
                <svg-icon :icon-class="data.type" style="width: 14px;height: 14px" />
              </span>
              <span style="margin-left: 6px;font-size: 14px">{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </el-row>
      <!--      <el-row v-if="detailItem&&detailItem.snapshot" class="detail-class">-->
      <!--        <el-card class="filter-card-class">-->
      <!--          <div slot="header" class="button-div-class">-->
      <!--            <span>{{ detailItem.name }}</span>-->
      <!--          </div>-->
      <!--          <img draggable="false" class="view-list-thumbnails" :src="detailItem.snapshot" alt="">-->
      <!--        </el-card>-->
      <!--      </el-row>-->
    </el-row>
  </el-col>
</template>

<script>
import { tree, findOne } from '@/api/panel/view'
import componentList from '@/components/canvas/custom-component/component-list'
import { deepCopy } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import { mapState } from 'vuex'

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
      loading: false
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
    this.loadData()
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    nodeClick(data, node) {
      findOne(data.id).then(res => {
        this.detailItem = res.data
      })
    },
    loadData() {
      const param = {}
      this.loading = true
      tree(param).then(res => {
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
        id: node.data.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
      eventBus.$emit('startMoveIn')
    },
    dragEnd() {
      // console.log('dragEnd')
      this.$store.commit('clearDragComponentInfo')
    },
    // 判断节点能否被拖拽
    allowDrag(draggingNode) {
      if (draggingNode.data.type === 'scene' || draggingNode.data.type === 'group') {
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
        if (node.type === 'group') {
          node.disabled = true
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
          component = deepCopy(componentTemp)
        }
      })
      component.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      component.moveStatus = 'start'
      return component
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
</style>
