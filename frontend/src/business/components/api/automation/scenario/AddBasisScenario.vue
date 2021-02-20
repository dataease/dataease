<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.automation.add_scenario')" :visible.sync="visible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="scenarioForm" label-position="right" label-width="80px" size="small" :rules="rule"
             ref="scenarioForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="scenarioForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.principal')" prop="principal">
        <el-select v-model="scenarioForm.principal"
                   :placeholder="$t('api_test.automation.scenario.principal')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.follow_people')" prop="followPeople">
        <el-select v-model="scenarioForm.followPeople"
                   :placeholder="$t('api_test.automation.scenario.follow_people')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input class="ms-http-textarea" v-model="scenarioForm.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="visible = false"
        :isShow="true"
        title="编辑详情"
        @saveAsEdit="saveScenario(true)"
        @confirm="saveScenario">
      </ms-dialog-footer>

    </template>

  </el-dialog>
</template>

<script>
  import {WORKSPACE_ID} from '@/common/js/constants';
  import {getCurrentUser, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

  export default {
    name: "MsAddBasisScenario",
    components: {MsDialogFooter},
    props: {},
    data() {
      return {
        scenarioForm: {},
        visible: false,
        currentModule: {},
        userOptions: [],
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
          ],
          principal: [{
            required: true,
            message: this.$t('api_test.automation.scenario.select_principal'),
            trigger: 'change'
          }],
        },
      }
    },
    methods: {
      saveScenario(saveAs) {
        this.$refs['scenarioForm'].validate((valid) => {
          if (valid) {
            let path = "/api/automation/create";
            this.setParameter();
            this.$fileUpload(path, null, [], this.scenarioForm, () => {
              this.visible = false;
              if (saveAs) {
                this.scenarioForm.request = JSON.stringify(this.scenarioForm.request);
                this.$emit('saveAsEdit', this.scenarioForm);
              } else {
                this.$emit('refresh');
              }
            });
          } else {
            return false;
          }
        })
      },
      setParameter() {
        this.scenarioForm.projectId = getCurrentProjectID();
        this.scenarioForm.id = getUUID().substring(0, 8);
        this.scenarioForm.protocol = this.currentProtocol;

        if (this.currentModule && this.currentModule.id != "root") {
          this.scenarioForm.modulePath = this.currentModule.method !== undefined ? this.currentModule.method : null;
          this.scenarioForm.apiScenarioModuleId = this.currentModule.id;
        }
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.userOptions = response.data;
        });
      },
      open(currentModule) {
        this.scenarioForm = {principal: getCurrentUser().id};
        this.currentModule = currentModule;
        this.getMaintainerOptions();
        this.visible = true;
      }
    }
  }
</script>
