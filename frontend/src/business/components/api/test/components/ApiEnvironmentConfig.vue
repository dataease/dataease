<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.environment.environment_config')"
             :visible.sync="visible" class="environment-dialog" width="60%"
             @close="close" append-to-body ref="environmentConfig">
    <el-container v-loading="result.loading">
      <ms-aside-item :enable-aside-hidden="false" :title="$t('api_test.environment.environment_list')"
                     :data="environments" :item-operators="environmentOperators" :add-fuc="addEnvironment"
                     :delete-fuc="deleteEnvironment" @itemSelected="environmentSelected" ref="environmentItems"/>
      <environment-edit :environment="currentEnvironment" ref="environmentEdit" @close="close"/>
    </el-container>
  </el-dialog>
</template>

<script>
  import MsApiCollapse from "./collapse/ApiCollapse";
  import MsApiCollapseItem from "./collapse/ApiCollapseItem";
  import draggable from 'vuedraggable';
  import MsContainer from "../../../common/components/MsContainer";
  import MsAsideContainer from "../../../common/components/MsAsideContainer";
  import MsMainContainer from "../../../common/components/MsMainContainer";
  import MsAsideItem from "../../../common/components/MsAsideItem";
  import EnvironmentEdit from "./environment/EnvironmentEdit";
  import {deepClone, listenGoBack, removeGoBackListener} from "../../../../../common/js/utils";
  import {Environment, parseEnvironment} from "../model/EnvironmentModel";

  export default {
    name: "ApiEnvironmentConfig",
    components: {
      EnvironmentEdit,
      MsAsideItem,
      MsMainContainer, MsAsideContainer, MsContainer, MsApiCollapseItem, MsApiCollapse, draggable
    },
    data() {
      return {
        result: {},
        visible: false,
        projectId: '',
        environments: [],
        currentEnvironment: new Environment(),
        environmentOperators: [
          {
            icon: 'el-icon-document-copy',
            func: this.copyEnvironment
          },
          {
            icon: 'el-icon-delete',
            func: this.deleteEnvironment
          }
        ]
      }
    },
    methods: {
      open: function (projectId) {
        this.visible = true;
        this.projectId = projectId;
        this.getEnvironments();
        listenGoBack(this.close);
      },
      deleteEnvironment(environment, index) {
        if (environment.id) {
          this.result = this.$get('/api/environment/delete/' + environment.id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.getEnvironments();
          });
        }
        else {
          this.environments.splice(index, 1);
        }
      },
      copyEnvironment(environment) {
        this.currentEnvironment = environment;
        if (!environment.id) {
          this.$warning(this.$t('commons.please_save'));
          return;
        }
        let newEnvironment = {};
        newEnvironment = new Environment(environment);
        newEnvironment.id = null;
        newEnvironment.name = this.getNoRepeatName(newEnvironment.name);
        if (!this.validateEnvironment(newEnvironment)) {
          return;
        }
        this.$refs.environmentEdit._save(newEnvironment);
        this.environments.push(newEnvironment);
        this.$refs.environmentItems.itemSelected(this.environments.length - 1, newEnvironment);
      },
      validateEnvironment(environment) {
        if (!this.$refs.environmentEdit.validate()) {
          this.$error(this.$t('commons.formatErr'));
          return false;
        }
        return true;
      },
      getNoRepeatName(name) {
        for (let i in this.environments) {
          if (this.environments[i].name === name) {
            return this.getNoRepeatName(name + ' copy');
          }
        }
        return name;
      },
      addEnvironment() {
        let newEnvironment = new Environment({
          projectId: this.projectId
        });
        this.environments.push(newEnvironment);
        this.$refs.environmentItems.itemSelected(this.environments.length - 1, newEnvironment);
      },
      environmentSelected(environment) {
        this.getEnvironment(environment);
      },
      getEnvironments() {
        if (this.projectId) {
          this.result = this.$get('/api/environment/list/' + this.projectId, response => {
            this.environments = response.data;
            if (this.environments.length > 0) {
              this.$refs.environmentItems.itemSelected(0, this.environments[0]);
            } else {
              let item = new Environment({
                projectId: this.projectId
              });
              this.environments.push(item);
              this.$refs.environmentItems.itemSelected(0, item);
            }
          });
        }
      },
      getEnvironment(environment) {
        parseEnvironment(environment);
        this.currentEnvironment = environment;
      },
      close() {
        this.$emit('close');
        this.visible = false;
        this.$refs.environmentEdit.clearValidate();
        removeGoBackListener(this.close);
      }
    }
  }
</script>

<style scoped>

  .environment-dialog >>> .el-dialog__body {
    padding-top: 20px;
  }

  .el-container {
    position: relative;
  }

  .ms-aside-container {
    height: 100%;
    position: absolute;
  }

</style>
