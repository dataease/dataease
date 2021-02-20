<template>
  <el-card>
    <el-container class="test-container">

      <el-header>
        <el-row type="flex" align="middle">
          <el-input :disabled="true" class="test-name" v-model="test.name" maxlength="60" :placeholder="$t('api_test.input_name')"
                    show-word-limit>
            <el-select :disabled="true" class="test-project" v-model="project.name" slot="prepend"
                       :placeholder="$t('api_test.select_project')">
            </el-select>
          </el-input>
          <el-button :disabled="isReadOnly" type="primary" plain @click="runTest">
            {{$t('api_test.run')}}
          </el-button>
        </el-row>
      </el-header>

      <ms-api-scenario-config :test="test" :project-id="test.projectId" :is-read-only="true" :scenarios="test.scenarioDefinition" ref="config"/>

  </el-container>
  </el-card>
</template>

<script>
import {Test} from "../../../../../api/test/model/ScenarioModel"
import MsApiScenarioConfig from "../../../../../api/test/components/ApiScenarioConfig";
import MsContainer from "../../../../../common/components/MsContainer";
import MsMainContainer from "../../../../../common/components/MsMainContainer";

export default {
  name: "ApiTestDetail",
  components: {MsMainContainer, MsContainer, MsApiScenarioConfig},
  props: {
    id: String,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
      return {
        result: {},
        test: new Test(),
        project: {}
      }
    },
    methods: {
      init() {
        this.project = {};
        if (this.id) {
          this.getTest(this.id);
        } else {
          this.test = new Test();
          if (this.$refs.config) {
            this.$refs.config.reset();
          }
        }
      },
      getTest(id) {
        this.result = this.$get("/api/get/" + id, response => {
          if (response.data) {
            let item = response.data;
            this.test = new Test({
              id: item.id,
              projectId: item.projectId,
              name: item.name,
              status: item.status,
              scenarioDefinition: JSON.parse(item.scenarioDefinition),
            });
            this.getProject(item.projectId);
            this.$refs.config.reset();
          } else {
            this.test = new Test();
          }
        });
      },
      getProject(projectId) {
        this.$get("/project/get/" + projectId, response => {
            this.project = response.data;
        });
      },
      runTest() {
        this.result = this.$post("/api/run", {id: this.test.id, triggerMode: 'MANUAL'}, (response) => {
          this.$success(this.$t('api_test.running'));
          this.$emit('runTest', response.data)
        });
      }
    }
  }
</script>

<style scoped>
  .test-container {
    height: calc(100vh - 155px);
    min-height: 600px;
    padding: 15px;
  }

  .test-name {
    width: 600px;
    margin-left: -20px;
    margin-right: 20px;
  }

  .test-project {
    min-width: 150px;
  }
</style>
