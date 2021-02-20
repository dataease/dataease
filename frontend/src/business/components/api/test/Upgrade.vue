<template>
  <el-dialog
    :title="$t('commons.module.select_module')"
    :visible.sync="oneClickOperationVisible"
    width="600px"
    left
    :destroy-on-close="true"
    show-close
    @closed="handleClose" v-loading="loading">

    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('commons.module.default_module')"
      @add="add"
      :type="'edit'"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
        <el-input :placeholder="$t('test_track.module.search')" v-model="condition.filterText" size="small">
        </el-input>
      </template>

    </ms-node-tree>
    <span slot="footer" class="dialog-footer">
   <ms-dialog-footer
     @cancel="oneClickOperationVisible = false"
     @confirm="confirm"/>
  </span>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from '../../common/components/MsDialogFooter'
  import MsApiScenarioConfig from "./components/ApiScenarioConfig";
  import MsApiReportStatus from "../report/ApiReportStatus";
  import MsApiReportDialog from "./ApiReportDialog";
  import {getUUID, getCurrentProjectID} from "@/common/js/utils";
  import SelectMenu from "../../track/common/SelectMenu";
  import MsNodeTree from "../../track/common/NodeTree";
  import {buildNodePath} from "../definition/model/NodeTree";

  export default {
    name: "MsUpgrade",
    components: {
      MsApiReportDialog, MsApiReportStatus, MsApiScenarioConfig,
      MsDialogFooter,
      MsNodeTree,
      SelectMenu,
    },
    data() {
      return {
        oneClickOperationVisible: false,
        moduleOptions: [],
        loading: false,
        result: {},
        data: [],
        condition: {
          filterText: "",
          trashEnable: false
        },
        currentModule: undefined,
      };
    },
    props: {
      selectIds: {
        type: Set
      },
      selectProjectNames: {
        type: Set
      },
      selectProjectId: {
        type: Set
      }
    },
    watch: {
      'condition.filterText'(val) {
        this.$refs.nodeTree.filter(val);
      },
    },
    methods: {
      openOneClickOperation() {
        this.initModule();
        this.oneClickOperationVisible = true;
      },
      getPath(id) {
        let path = this.moduleOptions.filter(function (item) {
          return item.id === id ? item.path : "";
        });
        return path[0].path;
      },
      confirm() {
        if (!this.currentModule) {
          this.$warning("请选择一个模块");
          return;
        }
        if (this.currentModule.id === "root") {
          this.$warning("不能选默认模块，请重新选择一个模块");
          return;
        }
        this.loading = true;
        let arr = Array.from(this.selectIds);
        let obj = {testIds: arr, projectId: getCurrentProjectID(), modulePath: this.getPath(this.currentModule.id), moduleId: this.currentModule.id};
        this.$post("/api/historicalDataUpgrade", obj, response => {
          this.loading = false;
          this.$success(this.$t('organization.integration.successful_operation'));
          this.oneClickOperationVisible = false;
        })
      },
      initModule() {
        let url = "/api/automation/module/list/" + getCurrentProjectID();
        this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            let modules = [];
            this.data.forEach(node => {
              buildNodePath(node, {path: ''}, modules);
            });
            this.moduleOptions = modules;
          }
        });
      },
      handleClose() {
        this.ruleForm = {}
      },
      edit(param) {
        param.projectId = getCurrentProjectID();
        param.protocol = this.condition.protocol;
        this.$post("/api/automation/module/edit", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.initModule();
          this.refresh();
        }, (error) => {
          this.initModule();
        });
      },
      add(param) {
        param.projectId = getCurrentProjectID();
        param.protocol = this.condition.protocol;
        this.$post("/api/automation/module/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.initModule();
        }, (error) => {
          this.initModule();
        });
      },
      remove(nodeIds) {
        this.$post("/api/automation/module/delete", nodeIds, () => {
          this.initModule();
          this.refresh();
        }, (error) => {
          this.initModule();
        });
      },
      drag(param, list) {
        this.$post("/api/automation/module/drag", param, () => {
          this.$post("/api/automation/module/pos", list, () => {
            this.initModule();
          });
        }, (error) => {
          this.initModule();
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
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      refresh() {
        this.$emit("refreshTable");
      },
      addScenario() {
        if (!getCurrentProjectID()) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.$refs.basisScenario.open(this.currentModule);
      },
      enableTrash() {
        this.condition.trashEnable = true;
      }
    }
  }
</script>

<style scoped>

</style>
