<template>
  <div class="children-node node-container" :style="{ height: nodeHeight }">
    <div class="node-line">
      <svg-icon
        v-if="childrenNode.unionToParent.unionType === 'left'"
        icon-class="left-join"
        class="join-icon"
        @click="unionEdit"
      />
      <svg-icon
        v-else-if="childrenNode.unionToParent.unionType === 'right'"
        icon-class="right-join"
        class="join-icon"
        @click="unionEdit"
      />
      <svg-icon
        v-else-if="childrenNode.unionToParent.unionType === 'inner'"
        icon-class="inner-join"
        class="join-icon"
        @click="unionEdit"
      />
      <svg-icon
        v-else
        icon-class="no-join"
        class="join-icon"
        @click="unionEdit"
      />

      <svg class="join-svg-container">
        <path fill="none" stroke="#dcdfe6" :d="pathParam + lineLength" />
      </svg>
    </div>

    <node-item
      :current-node="childrenNode"
      :node-index="nodeIndex"
      :origin-data="originData"
      @deleteNode="deleteNode"
      @notifyParent="calc"
      @editUnion="unionConfig"
      @cancelUnionEdit="cancelUnion"
    />
    <!--递归调用自身，完成树状结构-->
    <div>
      <union-node
        v-for="(item, index) in childrenNode.childrenDs"
        :key="index"
        :node-index="index"
        :children-node="item"
        :children-list="childrenNode.childrenDs"
        :parent-node="childrenNode"
        :origin-data="originData"
        @notifyParent="calc"
        @cancelUnionEdit="cancelUnion"
      />
    </div>

    <!--编辑关联关系-->
    <el-drawer
      v-if="editUnion"
      :title="
        unionParam.type === 'add'
          ? $t('dataset.add_union_relation')
          : $t('dataset.edit_union_relation')
      "
      :visible.sync="editUnion"
      custom-class="user-drawer union-dataset-drawer"
      size="840px"
      v-closePress
      direction="rtl"
    >
      <union-edit :union-param="unionParam" />
      <div class="de-foot">
        <deBtn secondary @click="closeEditUnion()">{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn type="primary" @click="confirmEditUnion()">{{
          $t('dataset.confirm')
        }}</deBtn>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import NodeItem from '@/views/dataset/add/union/NodeItem'
import UnionEdit from '@/views/dataset/add/union/UnionEdit'
export default {
  name: 'UnionNode',
  components: {
    UnionEdit,
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
    parentNode: {
      type: Object,
      required: true
    },
    nodeIndex: {
      type: Number,
      required: true
    },
    originData: {
      type: Array,
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
      pathParam: '',
      editUnion: false,
      unionParam: {},
      tempData: []
    }
  },
  watch: {
    'childrenNode.allChildCount': function () {
      this.calcNodeHeight()
      this.nodeLineHeight()
    },
    nodeIndex: function () {
      this.calcNodeHeight()
      this.nodeLineHeight()
    },
    childrenList: function () {
      this.calcNodeHeight()
      this.nodeLineHeight()
    }
  },
  mounted() {
    this.calcNodeHeight()
    this.nodeLineHeight()
  },
  methods: {
    unionEdit() {
      const param = {
        type: 'edit',
        nodeIndex: this.nodeIndex,
        node: this.childrenNode,
        parent: this.parentNode
      }
      this.unionConfig(param)
    },
    unionConfig(param) {
      this.tempData = JSON.parse(JSON.stringify(this.originData))
      this.unionParam = param
      this.editUnion = true
    },
    deleteNode(index) {
      this.childrenList.splice(index, 1)
    },
    // 计算连接线长度
    nodeLineHeight() {
      if (this.childrenList.length === 1 && this.nodeIndex === 0) {
        this.pathParam = this.path
        this.lineLength = ''
      } else {
        if (this.nodeIndex === 0) {
          this.pathParam = this.pathExt
          this.lineLength =
            this.childrenNode.allChildCount < 2
              ? ''
              : 'l0,' + (this.childrenNode.allChildCount - 1) * 40
        } else if (this.nodeIndex === this.childrenList.length - 1) {
          this.pathParam = this.pathMore
          this.lineLength = ''
        } else {
          this.pathParam = this.pathMoreExt
          this.lineLength =
            this.childrenNode.allChildCount < 2
              ? ''
              : 'l0,' + (this.childrenNode.allChildCount - 1) * 40
        }
      }
    },
    // 计算行高
    calcNodeHeight() {
      this.nodeHeight =
        this.childrenNode.allChildCount < 1
          ? '40px'
          : this.childrenNode.allChildCount * 40 + 'px'
    },
    calc(param) {
      this.notifyFirstParent(param)
    },
    // 判断每个node的状态等，并继续向父级传递
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

      // 传递到父级
      const p = JSON.parse(JSON.stringify(param))
      p.grandParentAdd = this.childrenNode.allChildCount > 1
      if (param.subCount > 1) {
        p.grandParentSub = true
      } else {
        p.grandParentSub = this.childrenNode.allChildCount !== 0
      }
      this.$emit('notifyParent', p)
    },

    closeEditUnion() {
      this.editUnion = false
      if (this.unionParam.type === 'add') {
        this.childrenNode.childrenDs.pop()
        // 添加关联的时候，如果关闭关联关系设置的界面，则删除子节点，同时向父级传递消息
        this.notifyFirstParent({
          type: 'delete',
          grandParentAdd: true,
          grandParentSub: true,
          subCount: 0
        })
      } else {
        // 向第一级传递
        this.$emit('cancelUnionEdit', this.tempData)
      }
    },
    confirmEditUnion() {
      // 校验关联关系与字段，必填
      if (this.checkUnion()) {
        this.editUnion = false
      } else {
        this.$message({
          message: this.$t('dataset.union_error'),
          type: 'error',
          showClose: true
        })
      }
    },
    // 向上级传递
    cancelUnion(val) {
      this.$emit('cancelUnionEdit', val)
    },

    checkUnion() {
      const union = this.unionParam.node.unionToParent
      if (!union.unionType) {
        return false
      }
      if (!union.unionFields || union.unionFields.length < 1) {
        return false
      }
      for (let i = 0; i < union.unionFields.length; i++) {
        const ele = union.unionFields[i]
        if (
          !ele.parentField ||
          !ele.parentField.id ||
          !ele.currentField ||
          !ele.currentField.id
        ) {
          return false
        }
      }
      return true
    }
  }
}
</script>

<style scoped>
.node-container {
  display: flex;
  position: relative;
}
.join-icon {
  height: 26px;
  font-size: 24px;
  line-height: 26px;
  position: absolute;
  left: 18px;
  color: #dcdfe6;
}
.join-svg-container {
  width: 60px;
}
.node-line {
  display: flex;
  position: relative;
}
.join-icon:hover {
  cursor: pointer;
  color: var(--Main, #2681ff);
}
</style>
<style lang="scss">
.union-dataset-drawer {
  .el-drawer__body {
    padding: 24px;
  }
}
</style>