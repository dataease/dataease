<template>
  <div v-loading="result.loading">

    <slot name="header"></slot>

    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :type="isReadOnly ? 'view' : 'edit'"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
        <api-module-header
          :condition="condition"
          :current-module="currentModule"
          :is-read-only="isReadOnly"
          @exportAPI="exportAPI"
          @saveAsEdit="saveAsEdit"
          @refreshTable="$emit('refreshTable')"
          @refresh="refresh"
          @debug="debug"/>
      </template>

    </ms-node-tree>

  </div>

</template>

<script>
import MsAddBasisApi from "../basis/AddBasisApi";
import SelectMenu from "../../../../track/common/SelectMenu";
import {OPTIONS} from "../../model/JsonData";
import ApiImport from "../import/ApiImport";
import {getCurrentProjectID} from "@/common/js/utils";
import MsNodeTree from "../../../../track/common/NodeTree";
import ApiModuleHeader from "./ApiModuleHeader";
import {buildNodePath} from "../../model/NodeTree";

export default {
  name: 'MsApiModule',
  components: {
    ApiModuleHeader,
    MsNodeTree,
    MsAddBasisApi,
    SelectMenu,
    ApiImport
  },
  data() {
    return {
        result: {},
        condition: {
          protocol: OPTIONS[0].value,
          filterText: "",
          trashEnable: false
        },
        projectId: "",
        data: [],
        currentModule: {},
      }
    },
    props: {
      isReadOnly: {
        type: Boolean,
        default() {
          return false
        }
      },
      planId: String,
      relevanceProjectId: String,
    },
    computed: {
      isPlanModel() {
        return this.planId ? true : false;
      },
      isRelevanceModel() {
        return this.relevanceProjectId ? true : false;
      }
    },
    mounted() {
      this.projectId = getCurrentProjectID();
      this.$emit('protocolChange', this.condition.protocol);
      this.list();
    },
    watch: {
      'condition.filterText'(val) {
        this.$refs.nodeTree.filter(val);
      },
      'condition.protocol'() {
        this.$emit('protocolChange', this.condition.protocol);
        this.list();
      },
      'condition.trashEnable'() {
        this.$emit('enableTrash', this.condition.trashEnable);
      },
      planId() {
        this.list();
      },
      relevanceProjectId() {
        this.list();
      }
    },
    methods: {
      list() {
        let url = undefined;
        if (this.isPlanModel) {
          url = '/api/module/list/plan/' + this.planId + '/' + this.condition.protocol;
        } else if (this.isRelevanceModel) {
          url = "/api/module/list/" + this.relevanceProjectId + "/" + this.condition.protocol;
        } else {
          url = "/api/module/list/" + this.projectId + "/" + this.condition.protocol;
          if (!this.projectId) {
            return;
          }
        }
        this.result = this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            let moduleOptions = [];
            this.data.forEach(node => {
              buildNodePath(node, {path: ''}, moduleOptions);
            });
            this.$emit('setModuleOptions', moduleOptions);
            this.$emit('setNodeTree', this.data);
            if (this.$refs.nodeTree) {
              this.$refs.nodeTree.filter(this.condition.filterText);
            }
          }
        });
      },
      edit(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/module/edit", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      add(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/module/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        }, (error) => {
          this.list();
        });
      },
      remove(nodeIds) {
        this.$post("/api/module/delete", nodeIds, () => {
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      drag(param, list) {
        this.$post("/api/module/drag", param, () => {
          this.$post("/api/module/pos", list, () => {
            this.list();
          });
        }, (error) => {
          this.list();
        });
      },
      nodeChange(node, nodeIds, pNodes) {
        this.currentModule = node.data;
        this.condition.trashEnable = false;
        if (node.data.id === 'root') {
          this.$emit("nodeSelectEvent", node, [], pNodes);
        } else {
          this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
        }
      },
      //创建根目录的模块---供父类使用
      createRootModel() {
        let dataArr = this.$refs.nodeTree.extendTreeNodes;
        if (dataArr.length > 0) {
          this.$refs.nodeTree.append({}, dataArr[0]);
        }
      },
      exportAPI() {
        this.$emit('exportAPI');
      },
      debug() {
        this.$emit('debug');
      },
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      refresh() {
        this.list();
        this.$emit('refreshTable');
      },
    }
  }
</script>

<style scoped>

</style>
