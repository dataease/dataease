<template>
  <div class="children-node node-container" :style="{height:nodeHeight}">
    <div class="node-line">
      <svg-icon v-if="childrenNode.unionToParent.unionType === 'left'" icon-class="left-join" class="join-icon" @click="unionConfig" />
      <svg-icon v-else-if="childrenNode.unionToParent.unionType === 'right'" icon-class="right-join" class="join-icon" @click="unionConfig" />
      <svg-icon v-else-if="childrenNode.unionToParent.unionType === 'inner'" icon-class="inner-join" class="join-icon" @click="unionConfig" />
      <svg-icon v-else icon-class="no-join" class="join-icon" @click="unionConfig" />

      <svg class="join-svg-container">
        <path fill="none" stroke="#dcdfe6" :d="pathParam + lineLength" />
      </svg>
    </div>

    <node-item :current-node="childrenNode" :node-index="nodeIndex" @deleteNode="deleteNode" @notifyParent="calc" />
    <!--递归调用自身，完成树状结构-->
    <div>
      <union-node v-for="(item,index) in childrenNode.childrenDs" :key="index" :node-index="index" :children-node="item" :children-list="childrenNode.childrenDs" @notifyParent="calc" />
    </div>
  </div>
</template>

<script>
import NodeItem from '@/views/dataset/add/union/NodeItem'
export default {
  name: 'UnionNode',
  components: {
    NodeItem
  },
  props: {
    childrenList: {
      type: Array,
      required: true
    },
    childrenNode: {
      type: Object,
      required: true
    },
    nodeIndex: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      path: 'm0,13 l18,0 m24,0 l18,0',
      pathExt: 'm0,13 l18,0 m24,0 l18,0 M9,13 l0,27', // 向下直线
      pathMore: 'M9,0 l0,13 l9,0 m24,0 l18,0', // 向上直线
      pathMoreExt: 'M9,0 l0,13 l9,0 m24,0 l18,0 M9,13 l0,27',
      nodeHeight: '40px',
      lineLength: '',
      pathParam: ''
    }
  },
  watch: {
    'childrenNode.allChildCount': function() {
      this.calcNodeHeight()
      this.nodeLineHeight()
    },
    nodeIndex: function() {
      this.calcNodeHeight()
      this.nodeLineHeight()
    },
    childrenList: function() {
      this.calcNodeHeight()
      this.nodeLineHeight()
    }
  },
  mounted() {
    this.calcNodeHeight()
    this.nodeLineHeight()
  },
  methods: {
    unionConfig() {
      console.log('union config')
    },
    deleteNode(index) {
      this.childrenList.splice(index, 1)
    },
    nodeLineHeight() {
      if (this.childrenList.length === 1 && this.nodeIndex === 0) {
        this.pathParam = this.path
        this.lineLength = ''
      } else {
        if (this.nodeIndex === 0) {
          this.pathParam = this.pathExt
          this.lineLength = this.childrenNode.allChildCount < 2 ? '' : ('l0,' + ((this.childrenNode.allChildCount - 1) * 40))
        } else if (this.nodeIndex === (this.childrenList.length - 1)) {
          this.pathParam = this.pathMore
          this.lineLength = ''
        } else {
          this.pathParam = this.pathMoreExt
          this.lineLength = this.childrenNode.allChildCount < 2 ? '' : ('l0,' + ((this.childrenNode.allChildCount - 1) * 40))
        }
      }
    },
    calcNodeHeight() {
      this.nodeHeight = this.childrenNode.allChildCount < 1 ? '40px' : (this.childrenNode.allChildCount * 40 + 'px')
    },
    calc(param) {
      this.notifyFirstParent(param)
    },
    notifyFirstParent(param) {
      if (param.type === 'union') {
        if (param.grandParentAdd) {
          this.childrenNode.allChildCount++
        }
      } else if (param.type === 'delete') {
        if (param.grandParentSub) {
          if (param.subCount > 1) {
            this.childrenNode.allChildCount -= param.subCount
          } else {
            this.childrenNode.allChildCount--
          }
        }
      }
      const p = JSON.parse(JSON.stringify(param))
      // 传递到父级
      p.grandParentAdd = this.childrenNode.allChildCount > 1
      if (param.subCount > 1) {
        p.grandParentSub = true
      } else {
        p.grandParentSub = this.childrenNode.allChildCount !== 0
      }
      this.$emit('notifyParent', p)
    }
  }
}
</script>

<style scoped>
.node-container{
  display: flex;
  position: relative;
}
.join-icon{
  height: 26px;
  font-size: 24px;
  line-height: 26px;
  position: absolute;
  left: 18px;
  color:#dcdfe6;
}
.join-svg-container{
  width:60px;
}
.node-line{
  display: flex;
  position: relative;
}
.join-icon:hover{
  cursor: pointer;
  color: var(--Main,#2681ff);
}
</style>
