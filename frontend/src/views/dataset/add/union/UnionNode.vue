<template>
  <div class="children-node node-container" :style="{height:'40px'}">
    <div class="node-line">
      <svg-icon icon-class="inner-join" class="join-icon" @click="unionConfig" />
      <svg class="join-svg-container">
        <path fill="none" stroke="#dcdfe6" :d="pathExt" />
      </svg>
    </div>
    <node-item :current-node="childrenNode" :node-index="nodeIndex" @deleteNode="deleteNode" />
    <!--递归调用自身-->
    <div>
      <union-node v-for="(item,index) in childrenNode.childrenDs" :key="index" :node-index="index" :children-node="item" :children-list="childrenNode.childrenDs" />
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
      path: 'm0,13 l28,0 m24,0 l28,0',
      pathExt: 'm0,13 l28,0 m24,0 l28,0 M14,13  l0,27',
      pathMore: 'M14,0 l0,13 l14,0 m24,0 l28,0',
      pathMoreExt: 'M14,0 l0,13 l14,0 m24,0 l28,0 M14,13  l0,27'
    }
  },
  methods: {
    unionConfig() {
      console.log('union config')
    },
    deleteNode(index) {
      this.childrenList.splice(index, 1)
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
  left: 28px;
  color:#dcdfe6;
}
.join-svg-container{
  width:80px;
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
