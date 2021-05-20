<template>
  <el-col>
    <el-row style="margin-top: 5px">
      <el-row style="margin-left: 5px;margin-right: 5px">
        <el-input
          v-model="templateFilterText"
          :placeholder="$t('panel.filter_keywords')"
          size="mini"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="defaultExpandedKeys"
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
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span>
              <span v-if="data.type==='scene'">
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
      <el-row v-if="detailItem&&detailItem.snapshot" class="detail-class">
        <el-card class="filter-card-class">
          <div slot="header" class="button-div-class">
            <span>{{ detailItem.name }}</span>
          </div>
          <img draggable="false" class="view-list-thumbnails" :src="detailItem.snapshot" alt="">
        </el-card>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import { tree, findOne } from '@/api/panel/view'
export default {
  name: 'ViewSelect',
  data() {
    return {
      templateFilterText: '',
      defaultExpandedKeys: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      data: [],
      showdetail: false,
      detailItem: null
    }
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
      tree(param).then(res => {
        this.data = res.data
      })
    },
    handleDragStart(node, ev) {
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'view',
        id: node.data.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
    },

    // 判断节点能否被拖拽
    allowDrag(draggingNode) {
      if (draggingNode.data.type === 'scene') {
        return false
      } else {
        return true
      }
    },
    allowDrop(draggingNode, dropNode, type) {
      return false
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
