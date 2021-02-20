<template>
  <relevance-dialog :title="$t('api_test.definition.api_import')" ref="relevanceDialog">

    <template v-slot:aside>
      <ms-api-module
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="refresh"
        @setModuleOptions="setModuleOptions"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

    <scenario-relevance-api-list
      v-if="isApiListEnable"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiList"/>

    <scenario-relevance-case-list
      v-if="!isApiListEnable"
      :current-protocol="currentProtocol"
      :select-node-ids="selectNodeIds"
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange"
      ref="apiCaseList"/>

    <template v-slot:footer>
      <el-button type="primary" @click="copy" @keydown.enter.native.prevent>{{ $t('commons.copy') }}</el-button>
      <el-button v-if="!isApiListEnable" type="primary" @click="reference" @keydown.enter.native.prevent>
        {{ $t('api_test.scenario.reference') }}
      </el-button>
    </template>

  </relevance-dialog>
</template>

<script>
import ScenarioRelevanceCaseList from "./RelevanceCaseList";
import MsApiModule from "../../../definition/components/module/ApiModule";
import MsContainer from "../../../../common/components/MsContainer";
import MsAsideContainer from "../../../../common/components/MsAsideContainer";
import MsMainContainer from "../../../../common/components/MsMainContainer";
import ScenarioRelevanceApiList from "./RelevanceApiList";
import RelevanceDialog from "../../../../track/plan/view/comonents/base/RelevanceDialog";

export default {
  name: "ApiRelevance",
  components: {
    RelevanceDialog,
    ScenarioRelevanceApiList,
    MsMainContainer, MsAsideContainer, MsContainer, MsApiModule, ScenarioRelevanceCaseList
  },
  data() {
    return {
      result: {},
      currentProtocol: null,
      selectNodeIds: [],
            moduleOptions: {},
            isApiListEnable: true,
          }
      },
      methods: {
        reference() {
          this.save('REF');
        },
        copy() {
          this.save('Copy');
        },
        save(reference) {
          if (this.isApiListEnable) {
            this.$emit('save', this.$refs.apiList.selectRows, 'API', reference);
            this.close();
          } else {
            let apiCases = this.$refs.apiCaseList.selectRows;
            let ids = Array.from(apiCases).map(row => row.id);
            this.result = this.$post("/api/testcase/get/request", {ids: ids}, (response) => {
              apiCases.forEach((item) => {
                item.request = response.data[item.id];
              });
              this.$emit('save', apiCases, 'CASE', reference);
              this.close();
            });
          }
        },
        close() {
          this.refresh();
          this.$refs.relevanceDialog.close();
        },
        open() {
          if (this.$refs.apiList) {
            this.$refs.apiList.clearSelection();
          }
          if (this.$refs.apiCaseList) {
            this.$refs.apiCaseList.clearSelection();
          }
          this.$refs.relevanceDialog.open();
        },
        isApiListEnableChange(data) {
          this.isApiListEnable = data;
        },
        nodeChange(node, nodeIds, pNodes) {
          this.selectNodeIds = nodeIds;
        },
        handleProtocolChange(protocol) {
          this.currentProtocol = protocol;
        },
        setModuleOptions(data) {
          this.moduleOptions = data;
        },
        refresh() {
          if (this.isApiListEnable) {
            this.$refs.apiList.initTable();
          } else {
            this.$refs.apiCaseList.initTable();
          }
        },
      }
    }
</script>

<style scoped>
/deep/ .filter-input {
  width: 140px !important;
}
</style>
