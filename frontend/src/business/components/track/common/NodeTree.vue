<template>
  <div>
    <slot name="header">
      <el-input :placeholder="$t('test_track.module.search')" v-model="filterText" size="small" :clearable="true"/>
    </slot>

    <el-tree
      class="filter-tree node-tree"
      :data="extendTreeNodes"
      :default-expanded-keys="expandedNode"
      node-key="id"
      @node-drag-end="handleDragEnd"
      @node-expand="nodeExpand"
      @node-collapse="nodeCollapse"
      :filter-node-method="filterNode"
      :expand-on-click-node="false"
      highlight-current
      :draggable="!disabled"
      ref="tree">

      <template v-slot:default="{node,data}">
      <span class="custom-tree-node father" @click="handleNodeSelect(node)">

        <span v-if="data.isEdit" @click.stop>
          <el-input  @blur.stop="save(node, data)" v-model="data.name" class="name-input" size="mini" ref="nameInput"/>
        </span>

        <span v-if="!data.isEdit" class="node-icon">
          <i class="el-icon-folder"/>
        </span>
        <span v-if="!data.isEdit" class="node-title" v-text="data.name"/>

        <span v-if="!disabled" class="node-operate child">
          <el-tooltip
            v-if="data.id != 'root'"
            class="item"
            effect="dark"
            :open-delay="200"
            :content="$t('test_track.module.rename')"
            placement="top">
            <i @click.stop="edit(node, data)" class="el-icon-edit"></i>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            :open-delay="200"
            :content="$t('test_track.module.add_submodule')"
            placement="top">
            <i @click.stop="append(node, data)" class="el-icon-circle-plus-outline"></i>
          </el-tooltip>
          <el-tooltip
            v-if="data.id != 'root'"
            class="item" effect="dark"
            :open-delay="200"
            :content="$t('commons.delete')"
            placement="top">
            <i @click.stop="remove(node, data)" class="el-icon-delete"></i>
          </el-tooltip>
        </span>
      </span>
      </template>
    </el-tree>
  </div>
</template>

<script>
import {checkoutTestManagerOrTestUser} from "../../../../common/js/utils";

export default {
  name: "MsNodeTree",
  components: {},
  data() {
    return {
      result: {},
      filterText: "",
      expandedNode: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      extendTreeNodes: []
    };
  },
  props: {
    type: {
      type: String,
      default: "view"
    },
    treeNodes: {
      type: Array
    },
    allLabel: {
      type: String,
      default() {
        return this.$t("commons.all_label.case");
      }
    },
  },
  watch: {
    treeNodes() {
      this.init();
    },
    filterText(val) {
      this.filter(val);
    }
  },
  computed: {
    disabled() {
      return this.type != 'edit' || !checkoutTestManagerOrTestUser();
    }
  },
  methods: {
    init() {
      this.extendTreeNodes = [];
      this.extendTreeNodes.unshift({
        "id": "root",
        "name": this.allLabel,
        "level": 0,
        "children": this.treeNodes,
      });
      if (this.expandedNode.length === 0) {
        this.expandedNode.push("root");
      }
    },
    handleNodeSelect(node) {
      let nodeIds = [];
      let pNodes = [];
      this.getChildNodeId(node.data, nodeIds);
      this.getParentNodes(node, pNodes);
      this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
    },
    filterNode(value, data) {
      if (!value) return true;
      if (data.label) {
        return data.label.indexOf(value) !== -1;
      }
      return false;
    },
    filter(val) {
      this.$nextTick(() => {
        this.$refs.tree.filter(val);
      });
    },
    nodeExpand(data) {
      if (data.id) {
        this.expandedNode.push(data.id);
      }
    },
    nodeCollapse(data) {
      if (data.id) {
        this.expandedNode.splice(this.expandedNode.indexOf(data.id), 1);
      }
    },
    edit(node, data, isAppend) {
      this.$set(data, 'isEdit', true);
      this.$nextTick(() => {
        this.$refs.nameInput.focus();
        if (!isAppend) {
          this.$nextTick(() => {
            this.filter(this.filterText);
          });
        }
      });
    },
    append(node, data) {
      const newChild = {
        id: undefined,
        isEdit: false,
        name: "",
        children: []
      };
      if (!data.children) {
        this.$set(data, 'children', [])
      }
      data.children.push(newChild);
      this.edit(node, newChild, true);
      node.expanded = true;
      this.$nextTick(() => {
        this.$refs.nameInput.focus();
      });
    },
    save(node, data) {
      if (data.name.trim() === '') {
        this.$warning(this.$t('test_track.case.input_name'));
        return;
      }
      if (data.name.trim().length > 50) {
        this.$warning(this.$t('test_track.length_less_than') + '50');
        return;
      }
      let param = {};
      this.buildSaveParam(param, node.parent.data, data);
      if (param.type === 'edit') {
        this.$emit('edit', param);
      } else {
        this.$emit('add', param);
      }
      this.$set(data, 'isEdit', false);
    },
    remove(node, data) {
      if (data.label === undefined) {
        this.$refs.tree.remove(node);
        return;
      }
      let tip =  '确定删除节点 ' + data.label + ' 及其子节点下所有资源' + '？';
      // let info =  this.$t("test_track.module.delete_confirm") + data.label + "，" + this.$t("test_track.module.delete_all_resource") + "？";
      this.$alert(tip, "", {
          confirmButtonText: this.$t("commons.confirm"),
          callback: action => {
            if (action === "confirm") {
              let nodeIds = [];
              this.getChildNodeId(node.data, nodeIds);
              this.$emit('remove', nodeIds);
            }
          }
        }
      );
    },
    handleDragEnd(draggingNode, dropNode, dropType, ev) {
      if (dropType === "none" || dropType === undefined) {
        return;
      }
      if (dropNode.data.id === 'root' && dropType === 'before') {
        this.$emit('refresh');
        return false;
      }
      let param = this.buildParam(draggingNode, dropNode, dropType);
      let list = [];
      this.getNodeTree(this.treeNodes, draggingNode.data.id, list);
      if (param.parentId === 'root') {
        param.parentId = undefined;
      }
      this.$emit('drag', param, list);
    },
    buildSaveParam(param, parentData, data) {
      if (data.id) {
        param.nodeIds = [];
        param.type = 'edit';
        param.id = data.id;
        param.level = data.level;
        this.getChildNodeId(data, param.nodeIds);
      } else {
        param.level = 1;
        param.type = 'add';
        if (parentData.id != 'root') {
          // 非根节点
          param.parentId = parentData.id;
          param.level = parentData.level + 1;
        }
      }
      param.name = data.name.trim();
      param.label = data.name;
    },
    buildParam(draggingNode, dropNode, dropType) {
      let param = {};
      param.id = draggingNode.data.id;
      param.name = draggingNode.data.name;
      param.projectId = draggingNode.data.projectId;
      if (dropType === "inner") {
        param.parentId = dropNode.data.id;
        param.level = dropNode.data.level + 1;
      } else {
        if (!dropNode.parent.id || dropNode.parent.id === 0) {
          param.parentId = 0;
          param.level = 1;
        } else {
          param.parentId = dropNode.parent.data.id;
          param.level = dropNode.parent.data.level + 1;
        }
      }
      let nodeIds = [];
      this.getChildNodeId(draggingNode.data, nodeIds);
      if (dropNode.data.level == 1 && dropType != "inner") {
        // nodeTree 为需要修改的子节点
        param.nodeTree = draggingNode.data;
      } else {
        for (let i = 0; i < this.treeNodes.length; i++) {
          param.nodeTree = this.findTreeByNodeId(this.treeNodes[i], dropNode.data.id);
          if (param.nodeTree) {
            break;
          }
        }
      }
      param.nodeIds = nodeIds;
      return param;
    },
    getNodeTree(nodes, id, list) {
      if (!nodes) {
        return;
      }
      for (let i = 0; i < nodes.length; i++) {
        if (nodes[i].id === id) {
          i - 1 >= 0 ? list[0] = nodes[i-1].id : list[0] = "";
          list[1] = nodes[i].id;
          i + 1 < nodes.length ? list[2] = nodes[i+1].id : list[2] = "";
          return;
        }
        if (nodes[i].children) {
          this.getNodeTree(nodes[i].children, id, list);
        }
      }
    },
    findTreeByNodeId(rootNode, nodeId) {
      if (rootNode.id == nodeId) {
        return rootNode;
      }
      if (rootNode.children) {
        for (let i = 0; i < rootNode.children.length; i++) {
          if (this.findTreeByNodeId(rootNode.children[i], nodeId)) {
            return rootNode;
          }
        }
      }
    },
    getChildNodeId(rootNode, nodeIds) {
      //递归获取所有子节点ID
      nodeIds.push(rootNode.id);
      if (rootNode.children) {
        for (let i = 0; i < rootNode.children.length; i++) {
          this.getChildNodeId(rootNode.children[i], nodeIds);
        }
      }
    },
    getParentNodes(rootNode, pNodes) {
      if (rootNode.parent && rootNode.parent.id != 0) {
        this.getParentNodes(rootNode.parent, pNodes);
      }
      if (rootNode.data.name && rootNode.data.name != "") {
        pNodes.push(rootNode.data);
      }
    },
  }
};
</script>

<style scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #409eff;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-tree {
  margin-top: 15px;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

.node-title {
  width: 0px;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0px 5px;
  overflow: hidden;
}

.node-operate > i {
  color: #409eff;
  margin: 0px 5px;
}

.name-input {
  height: 25px;
  line-height: 25px;
}

.name-input >>> .el-input__inner {
  height: 25px;
  line-height: 25px;
}
</style>
