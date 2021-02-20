<template>
  <ms-node-tree
    v-loading="result.loading"
    :tree-nodes="treeNodes"
    :type="'edit'"
    @add="add"
    @edit="edit"
    @drag="drag"
    @remove="remove"
    @nodeSelectEvent="nodeChange"
    @refresh="list"
    ref="nodeTree"/>
</template>

<script>
import NodeEdit from "./NodeEdit";
import {getCurrentProjectID} from "../../../../common/js/utils";
import MsNodeTree from "./NodeTree";

export default {
  name: "TestCaseNodeTree",
  components: {MsNodeTree, NodeEdit},
  data() {
    return {
      defaultProps: {
        children: "children",
        label: "label"
      },
      result: {},
      treeNodes: [],
        projectId: ""
      };
    },
    props: {
      type: {
        type: String,
        default: "view"
      },
    },
    watch: {
      treeNodes() {
        this.$emit('setTreeNodes', this.treeNodes);
      }
    },
    mounted() {
      this.projectId = getCurrentProjectID();
      this.list();
    },
    methods: {

      list() {
        if (this.projectId) {
          this.result = this.$get("/case/node/list/" + this.projectId, response => {
            this.treeNodes = response.data;
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter();
            }
          });
        }
      },
      edit(param) {
        param.projectId = this.projectId;
        this.$post("/case/node/edit", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.$emit("refreshTable");
        }, (error) => {
          this.list();
        });
      },
      add(param) {
        param.projectId = this.projectId;
        this.$post("/case/node/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        }, (error) => {
          this.list();
        });
      },
      remove(nodeIds) {
        this.$post("/case/node/delete", nodeIds, () => {
          this.list();
          this.$emit("refreshTable")
        }, (error) => {
          this.list();
        });
      },
      drag(param, list) {
        this.$post("/case/node/drag", param, () => {
          this.$post("/case/node/pos", list);
          this.list();
        }, (error) => {
          this.list();
        });
      },
      nodeChange(node, nodeIds, pNodes) {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      },
    }
  };
</script>

<style scoped>

</style>
