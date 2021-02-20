<template>
  <ms-container>

    <ms-aside-container>
      <test-case-node-tree
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @setTreeNodes="setTreeNodes"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <test-case-list
        :module-options="moduleOptions"
        :select-node-ids="selectNodeIds"
        :select-parent-nodes="selectParentNodes"
        :tree-nodes="treeNodes"
        @testCaseEdit="editTestCase"
        @testCaseCopy="copyTestCase"
        @testCaseDetail="showTestCaseDetail"
        @refresh="refresh"
        @refreshAll="refreshAll"
        @setCondition="setCondition"
        ref="testCaseList">
      </test-case-list>
    </ms-main-container>

    <test-case-edit
      @refresh="refreshTable"
      @setModuleOptions="setModuleOptions"
      :read-only="testCaseReadOnly"
      :tree-nodes="treeNodes"
      :select-node="selectNode"
      :select-condition="condition"
      ref="testCaseEditDialog">
    </test-case-edit>


  </ms-container>

</template>

<script>

import NodeTree from '../common/NodeTree';
import TestCaseEdit from './components/TestCaseEdit';
import TestCaseList from "./components/TestCaseList";
import SelectMenu from "../common/SelectMenu";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsMainContainer from "../../common/components/MsMainContainer";
import {checkoutTestManagerOrTestUser, getCurrentProjectID, hasRoles} from "../../../../common/js/utils";
import TestCaseNodeTree from "../common/TestCaseNodeTree";
import {TrackEvent,LIST_CHANGE} from "@/business/components/common/head/ListEvent";

export default {
  name: "TestCase",
  components: {
    TestCaseNodeTree,
    MsMainContainer,
    MsAsideContainer, MsContainer, TestCaseList, NodeTree, TestCaseEdit, SelectMenu
  },
  comments: {},
  data() {
    return {
      result: {},
      projects: [],
      treeNodes: [],
      selectNodeIds: [],
      selectParentNodes: [],
      testCaseReadOnly: true,
      selectNode: {},
      condition: {},
      moduleOptions: []
    }
  },
  mounted() {
    this.init(this.$route);
  },
  watch: {
    '$route'(to, from) {
      this.init(to);
    },
  },
  methods: {
    init(route) {
      let path = route.path;
      if (path.indexOf("/track/case/edit") >= 0 || path.indexOf("/track/case/create") >= 0) {
        this.testCaseReadOnly = false;
        if (!checkoutTestManagerOrTestUser()) {
          this.testCaseReadOnly = true;
        }
        let caseId = this.$route.params.caseId;
        if (!getCurrentProjectID()) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.openRecentTestCaseEditDialog(caseId);
        this.$router.push('/track/case/all');
      }
    },
    nodeChange(node, nodeIds, pNodes) {
      this.selectNodeIds = nodeIds;
      this.selectNode = node;
      this.selectParentNodes = pNodes;
    },
    refreshTable() {
      this.$refs.testCaseList.initTableData();
    },
    editTestCase(testCase) {
      this.testCaseReadOnly = false;
      if (this.treeNodes.length < 1) {
        this.$warning(this.$t('test_track.case.create_module_first'));
        return;
      }
      this.$refs.testCaseEditDialog.open(testCase);
    },
    copyTestCase(testCase) {
      this.testCaseReadOnly = false;
      let item = {};
      Object.assign(item, testCase);
      item.name = '';
      item.isCopy = true;
      this.$refs.testCaseEditDialog.open(item);
    },
    showTestCaseDetail(testCase) {
      this.testCaseReadOnly = true;
      this.$refs.testCaseEditDialog.open(testCase);
    },
    refresh() {
      this.selectNodeIds = [];
      this.selectParentNodes = [];
      this.selectNode = {};
      this.refreshTable();
    },
    refreshAll() {
      this.$refs.nodeTree.list();
      this.refresh();
    },
    openRecentTestCaseEditDialog(caseId) {
      if (caseId) {
        // this.getProjectByCaseId(caseId);
        this.$get('/test/case/get/' + caseId, response => {
          if (response.data) {
            this.$refs.testCaseEditDialog.open(response.data);
          }
        });
      } else {
        this.$refs.testCaseEditDialog.open();
      }
    },
    setTreeNodes(data) {
      this.treeNodes = data;
    },
    setCondition(data) {
      this.condition = data;
    },
    setModuleOptions(data) {
      this.moduleOptions = data;
    }
  }
}
</script>

<style scoped>

.el-main {
  padding: 15px;
}

</style>
