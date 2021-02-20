<template>
  <ms-container v-if="renderComponent">
    <ms-aside-container>
      <ms-api-scenario-module
        @nodeSelectEvent="nodeChange"
        @refreshTable="refresh"
        @saveAsEdit="editScenario"
        @setModuleOptions="setModuleOptions"
        @setNodeTree="setNodeTree"
        @enableTrash="enableTrash"
        :type="'edit'"
        ref="nodeTree"/>
    </ms-aside-container>

    <ms-main-container>
      <el-tabs v-model="activeName" @tab-click="addTab" @tab-remove="removeTab">
        <el-tab-pane name="default" :label="$t('api_test.automation.scenario_list')">
          <ms-api-scenario-list
            :module-tree="nodeTree"
            :module-options="moduleOptions"
            :select-node-ids="selectNodeIds"
            :trash-enable="trashEnable"
            :checkRedirectID="checkRedirectID"
            :isRedirectEdit="isRedirectEdit"
            @openScenario="editScenario"
            @edit="editScenario"
            @changeSelectDataRangeAll="changeSelectDataRangeAll"
            ref="apiScenarioList"/>
        </el-tab-pane>

        <el-tab-pane
          :key="item.name"
          v-for="(item) in tabs"
          :label="item.label"
          :name="item.name"
          closable>
          <div class="ms-api-scenario-div">
            <ms-edit-api-scenario @refresh="refresh" :currentScenario="item.currentScenario"
                                  :moduleOptions="moduleOptions" ref="autoScenarioConfig"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="add">
          <template v-slot:label>
            <el-dropdown @command="handleCommand" v-tester>
              <el-button type="primary" plain icon="el-icon-plus" size="mini" v-tester/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="ADD">{{ $t('api_test.automation.add_scenario') }}</el-dropdown-item>
                <el-dropdown-item command="CLOSE_ALL">{{ $t('api_test.definition.request.close_all_label') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-tab-pane>
      </el-tabs>
    </ms-main-container>
  </ms-container>
</template>

<script>

  import MsContainer from "@/business/components/common/components/MsContainer";
  import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
  import MsMainContainer from "@/business/components/common/components/MsMainContainer";
  import MsApiScenarioList from "@/business/components/api/automation/scenario/ApiScenarioList";
  import {getUUID} from "@/common/js/utils";
  import MsApiScenarioModule from "@/business/components/api/automation/scenario/ApiScenarioModule";
  import MsEditApiScenario from "./scenario/EditApiScenario";
  import {getCurrentProjectID} from "../../../../common/js/utils";

  export default {
    name: "ApiAutomation",
    components: {
      MsApiScenarioModule,
      MsApiScenarioList,
      MsMainContainer,
      MsAsideContainer,
      MsContainer,
      MsEditApiScenario
    },
    comments: {},
    computed: {
      checkRedirectID: function () {
        let redirectIDParam = this.$route.params.redirectID;
        this.changeRedirectParam(redirectIDParam);
        return redirectIDParam;
      },
      isRedirectEdit: function () {
        let redirectParam = this.$route.params.dataSelectRange;
        this.checkRedirectEditPage(redirectParam);
        return redirectParam;
      }
    },
    data() {
      return {
        redirectID: '',
        renderComponent: true,
        isHide: true,
        activeName: 'default',
        redirectFlag: 'none',
        currentModule: null,
        moduleOptions: [],
        tabs: [],
        trashEnable: false,
        selectNodeIds: [],
        nodeTree: []
      }
    },
    watch: {
      redirectID() {
        this.renderComponent = false;
        this.$nextTick(() => {
          // 在 DOM 中添加 my-component 组件
          this.renderComponent = true;
        });
      },
      '$route'(to, from) {  //  路由改变时，把接口定义界面中的 ctrl s 保存快捷键监听移除
        if (to.path.indexOf('/api/automation') == -1) {
          if (this.$refs && this.$refs.autoScenarioConfig) {
            console.log(this.$refs.autoScenarioConfig);
            this.$refs.autoScenarioConfig.forEach(item => {
              item.removeListener();
            });
          }
        }
      }
    },
    methods: {
      checkRedirectEditPage(redirectParam) {
        if (redirectParam != null) {
          let selectParamArr = redirectParam.split("edit:");
          if (selectParamArr.length == 2) {
            let scenarioId = selectParamArr[1];
            let projectId = getCurrentProjectID();
            //查找单条数据，跳转修改页面
            let url = "/api/automation/list/" + 1 + "/" + 1;
            this.$post(url, {id: scenarioId, projectId: projectId}, response => {
              let data = response.data;
              if (data != null) {
                //如果树未加载
                if (JSON.stringify(this.moduleOptions) === '{}') {
                  this.$refs.nodeTree.list();
                }
                let row = data.listObject[0];
                row.tags = JSON.parse(row.tags);
                this.editScenario(row);
              }
            });
          }
        }
      },
      changeRedirectParam(redirectIDParam) {
        this.redirectID = redirectIDParam;
        if(redirectIDParam!=null){
          if(this.redirectFlag == "none"){
            this.activeName = "default";
            this.addListener();
            this.redirectFlag = "redirected";
          }
        }else{
          this.redirectFlag = "none";
        }
      },
      addTab(tab) {
        if (!getCurrentProjectID()) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        if (tab.name === 'add') {
          let label = this.$t('api_test.automation.add_scenario');
          let name = getUUID().substring(0, 8);
          this.activeName = name;
          this.tabs.push({label: label, name: name, currentScenario: {apiScenarioModuleId: "", id: getUUID()}});
        }
        if (tab.name === 'edit') {
          let label = this.$t('api_test.automation.add_scenario');
          let name = getUUID().substring(0, 8);
          this.activeName = name;
          label = tab.currentScenario.name;
          this.tabs.push({label: label, name: name, currentScenario: tab.currentScenario});
        }
        if (this.$refs && this.$refs.autoScenarioConfig) {
          this.$refs.autoScenarioConfig.forEach(item => {
            item.removeListener();
          });  //  删除所有tab的 ctrl + s 监听
          this.addListener();
        }
      },
      addListener() {
        let index = this.tabs.findIndex(item => item.name === this.activeName); //  找到当前选中tab的index
        if(index != -1) {   //  为当前选中的tab添加监听
          this.$nextTick(()=>{
            this.$refs.autoScenarioConfig[index].addListener();
          });
        }
      },
      handleTabClose() {
        this.tabs = [];
        this.activeName = "default";
        this.refresh();
      },
      handleCommand(e) {
        switch (e) {
          case "ADD":
            this.addTab({name: 'add'});
            break;
          case "CLOSE_ALL":
            this.handleTabClose();
            break;
          default:
            this.addTab({name: 'add'});
            break;
        }
      },
      removeTab(targetName) {
        this.tabs = this.tabs.filter(tab => tab.name !== targetName);
        if (this.tabs.length > 0) {
          this.activeName = this.tabs[this.tabs.length - 1].name;
          this.addListener(); //  自动切换当前标签时，也添加监听
        } else {
          this.activeName = "default"
        }
      },
      setTabLabel(data) {
        for (const tab of this.tabs) {
          if (tab.name === this.activeName) {
            tab.label = data.name;
            break;
          }
        }
      },
      selectModule(data) {
        this.currentModule = data;
      },
      saveScenario(data) {
        this.setTabLabel(data);
        this.$refs.apiScenarioList.search(data);
      },
      refresh(data) {
        this.setTabTitle(data);
        this.$refs.apiScenarioList.search(data);
      },
      setTabTitle(data) {
        for (let index in this.tabs) {
          let tab = this.tabs[index];
          if (tab.name === this.activeName) {
            tab.label = data.name;
            break;
          }
        }
      },
      editScenario(row) {
        this.addTab({name: 'edit', currentScenario: row});
      },

      nodeChange(node, nodeIds, pNodes) {
        this.selectNodeIds = nodeIds;
      },
      setModuleOptions(data) {
        this.moduleOptions = data;
      },
      setNodeTree(data) {
        this.nodeTree = data;
      },
      changeSelectDataRangeAll(tableType) {
        this.$route.params.dataSelectRange = 'all';
      },
      enableTrash(data) {
        this.trashEnable = data;
      }
    }
  }
</script>

<style scoped>
  /deep/ .el-tabs__header {
    margin: 0 0 0px;
  }
</style>
