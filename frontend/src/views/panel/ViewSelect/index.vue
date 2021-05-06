<template>
  <div>
    <div class="my-top">
      <el-input
        v-model="filterText"
        placeholder="按名称搜索"
      />
      <el-tree
        ref="tree"
        class="filter-tree"
        :data="data"
        :props="defaultProps"
        :render-content="renderNode"
        draggable
        :allow-drop="allowDrop"
        :allow-drag="allowDrag"
        :filter-node-method="filterNode"
        @node-drag-start="handleDragStart"
      />
    </div>

    <div v-if="showdetail" class="detail-class">
      <el-card class="filter-card-class">
        <div slot="header" class="button-div-class">
          <span>{{ detailItem.name }}</span>
          <div style="float: right; padding: 1px 5px; cursor:pointer; " @click="closeDetail">
            <i class="el-icon-close" />
          </div>
        </div>
        <img class="view-list-thumbnails" :src="'/common-files/images/'+detailItem.id+'/VIEW_DEFAULT_IMAGE'" alt="">
      </el-card>

    </div>
  </div>
</template>

<script>
import { tree } from '@/api/panel/view'
import { addClass, removeClass } from '@/utils'
import bus from '@/utils/bus'
export default {
  name: 'ViewSelect',
  data() {
    return {
      filterText: null,
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
    filterText(val) {
      this.$refs.tree.filter(val)
    },
    showdetail(val) {
      const dom = document.querySelector('.my-top')
      if (val) {
        addClass(dom, 'top-div-class')
      } else {
        removeClass(dom, 'top-div-class')
      }
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
    loadData() {
      const param = {}
      tree(param).then(res => {
        // let arr = []
        // for (let index = 0; index < 10; index++) {
        //   arr = arr.concat(res.data)
        // }
        // this.data = arr
        this.data = res.data
      })
    },
    renderNode(h, { node, data, store }) {
      return (
        <div class='custom-tree-node' on-click={() => this.detail(data)} on-dblclick={() => this.addView2Drawing(data.id)}>
          <span class='label-span' >{node.label}</span>
          {data.type !== 'group' && data.type !== 'scene' ? (

            <svg-icon icon-class={data.type} class='chart-icon' />
          ) : (
            ''
          )}
        </div>
      )
    },
    detail(data) {
      this.showdetail = true
      this.detailItem = data
    },
    closeDetail() {
      this.showdetail = false
      this.detailItem = null
    },
    addView2Drawing(viewId) {
      //   viewInfo(viewId).then(res => {
      //     const info = res.data
      //     this.$emit('panel-view-add', info)
      //   })
      bus.$emit('panel-view-add', { id: viewId })
      //   this.$emit('panel-view-add', viewId)
    },
    handleDragStart(node, ev) {
      ev.dataTransfer.effectAllowed = 'copy'
      const dataTrans = {
        type: 'view',
        id: node.data.id
      }
      ev.dataTransfer.setData('componentInfo', JSON.stringify(dataTrans))
      // bus.$emit('component-on-drag')
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
    width: 100%;
    position: fixed;
    bottom: 0px;
  }
  .view-list-thumbnails {
    width: 100%;
    height: 100%;
  }
</style>
