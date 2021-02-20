<template>
  <api-base-component
    v-loading="loading"
    @copy="copyRow"
    @remove="remove"
    @active="active"
    :data="scenario"
    :show-collapse="false"
    :is-show-name-input="!isDeletedOrRef"
    :is-disabled="true"
    color="#606266"
    background-color="#F4F4F5"
    :title="$t('api_test.automation.scenario_import')">

    <template v-slot:behindHeaderLeft>
      <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='Deleted'" type="danger">{{$t('api_test.automation.reference_deleted')}}</el-tag>
      <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='Copy'">{{ $t('commons.copy') }}</el-tag>
      <el-tag size="mini" style="margin-left: 20px" v-if="scenario.referenced==='REF'">{{ $t('api_test.scenario.reference') }}</el-tag>
    </template>

  </api-base-component>
</template>

<script>
  import MsSqlBasisParameters from "../../../definition/components/request/database/BasisParameters";
  import MsTcpBasisParameters from "../../../definition/components/request/tcp/TcpBasisParameters";
  import MsDubboBasisParameters from "../../../definition/components/request/dubbo/BasisParameters";
  import MsApiRequestForm from "../../../definition/components/request/http/ApiHttpRequestForm";
  import ApiBaseComponent from "../common/ApiBaseComponent";

  export default {
    name: "ApiScenarioComponent",
    props: {
      scenario: {},
      node: {},
      draggable: {
        type: Boolean,
        default: false,
      },
    },
    watch: {},
    created() {
      if (this.scenario.id && this.scenario.referenced === 'REF' && !this.scenario.loaded) {
        this.result = this.$get("/api/automation/getApiScenario/" + this.scenario.id, response => {
          if (response.data) {
            this.scenario.loaded = true;
            if (response.data.scenarioDefinition) {
              this.scenario.hashTree = JSON.parse(response.data.scenarioDefinition).hashTree;
            }
            if (this.scenario.hashTree) {
              this.setDisabled(this.scenario.hashTree);
            }
            this.scenario.disabled = true;
            this.scenario.name = response.data.name;
            this.$emit('refReload');
          } else {
            this.scenario.referenced = "Deleted";
          }
        })
      }
    },
    components: {ApiBaseComponent, MsSqlBasisParameters, MsTcpBasisParameters, MsDubboBasisParameters, MsApiRequestForm},
    data() {
      return {
        loading: false,
        isShowInput: false
      }
    },
    computed: {
      isDeletedOrRef() {
        if (this.scenario.referenced != undefined && this.scenario.referenced === 'Deleted' || this.scenario.referenced === 'REF') {
          return true
        }
        return false;
      }
    },
    methods: {
      remove() {
        this.$emit('remove', this.scenario, this.node);
      },
      active(item) {
        item.active = !item.active;
        this.reload();
      },
      copyRow() {
        this.$emit('copyRow', this.scenario, this.node);
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      recursive(arr) {
        for (let i in arr) {
          arr[i].disabled = true;
          if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
            this.recursive(arr[i].hashTree);
          }
        }
      },
      setDisabled(scenarioDefinition) {
        for (let i in scenarioDefinition) {
          scenarioDefinition[i].disabled = true;
          if (scenarioDefinition[i].hashTree != undefined && scenarioDefinition[i].hashTree.length > 0) {
            this.recursive(scenarioDefinition[i].hashTree);
          }
        }
      },
    }
  }
</script>

<style scoped>

  /deep/ .el-card__body {
    padding: 15px;
  }

  .icon.is-active {
    transform: rotate(90deg);
  }

</style>
