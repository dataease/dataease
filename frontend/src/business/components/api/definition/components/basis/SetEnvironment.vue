<template>
  <div>
    <el-dialog
      :title="$t('api_test.environment.select_environment')"
      :visible.sync="dialogVisible"
      width="15%"
      :destroy-on-close="true"
      @close="handleClose"
    >
      <el-form  ref="form">
        <el-form-item prop="type">
          <el-select v-model="environmentId" value-key="id"  class="ms-htt-width"
                     :placeholder="$t('api_test.definition.request.run_env')"
                     clearable>
            <el-option v-for="(environment, index) in environments" :key="index"
                       :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                       :value="environment.id"/>
            <template v-slot:empty>
              <div class="empty-environment">
              </div>
            </template>
          </el-select>
        </el-form-item>
      </el-form>

      <template v-slot:footer>
        <!--        <el-button onclick="this.handleClose">{{ $t('commons.cancel') }}</el-button>-->
        <el-button type="primary" @click="createPerformance" @keydown.enter.native.prevent>
          {{ $t('commons.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";
import {getUUID, getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "MsSetEnvironment",
  components: {},
  props: {
    testCase: Object,
    row: Object,
  },
  data() {
    return {
      dialogVisible: false,
      filterable: false,
      environments: [],
      environmentId: "",
      dialogTitle: {
        type: String,
        default() {
          return this.$t('api_test.environment.select_environment')
        }
      }
    }
  },
  watch: {
    environmentId() {
      this.environmentChange(this.environmentId);
    },
  },
  methods: {
    environmentChange(value) {
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          this.environment = this.environments[i];
          break;
        }
      }
    },
    getEnvironments() {
      let selectProjectId = getCurrentProjectID();
      if (selectProjectId) {
        this.$get('/api/environment/list/' + selectProjectId, response => {
          this.environments = response.data;
          this.environments.forEach(environment => {
            parseEnvironment(environment);
          });
        });
      } else {
        this.environment = undefined;
      }
    },
    open() {
      this.dialogVisible = true;
      this.getEnvironments();
      listenGoBack(this.handleClose);
    },
    handleClose() {
      this.form = {};
      this.options = [];
      removeGoBackListener(this.handleClose);
    },
    createPerformance() {
      this.$get('/api/testcase/findById/' + this.testCase.id, response => {
        let testCaseInfo = response.data;
        if(testCaseInfo!=null){
          this.$emit("createPerformance", testCaseInfo, this.environment);
        }
      });
    },
  }
}
</script>

<style scoped>

</style>
