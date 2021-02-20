<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.api_import.title')" :visible.sync="visible" class="api-import" v-loading="result.loading" @close="close">

    <div class="header-bar">
      <div>{{$t('api_test.api_import.data_format')}}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <el-radio v-for="(item, index) in platforms" :key="index" :label="item.value">{{item.name}}</el-radio>
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain  @click="save">
          {{$t('commons.save')}}
        </el-button>
        <el-button class="cancel-button" type="warning" plain @click="visible = false">
          {{$t('commons.cancel')}}
        </el-button>
      </div>
    </div>

    <el-form :model="formData" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-col :span="11">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input size="small" class="name-input" v-model="formData.name" clearable show-word-limit/>
          </el-form-item>
          <el-form-item :label="$t('commons.project')" prop="projectId">
            <el-select size="small" v-model="formData.projectId" class="project-select" clearable>
              <el-option v-for="(project, index) in projects" :key="index" :label="project.name" :value="project.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-if="useEnvironment || selectedPlatformValue == 'Swagger2'" :label="$t('api_test.environment.environment_config')" prop="environmentId">
            <el-select v-if="showEnvironmentSelect" size="small"  v-model="formData.environmentId" class="environment-select" clearable>
              <el-option v-for="(environment, index) in environments" :key="index" :label="environment.name" :value="environment.id"/>
              <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">{{$t('api_test.environment.environment_config')}}</el-button>
              <template v-slot:empty>
                <div class="empty-environment">
                  <el-button :disabled="formData.projectId == ''" class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">{{$t('api_test.environment.environment_config')}}</el-button>
                </div>
              </template>
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedPlatformValue != 'Swagger2'" prop="useEnvironment">
            <el-checkbox v-model="useEnvironment">{{$t('api_test.environment.config_environment')}}</el-checkbox>
          </el-form-item>

          <el-form-item :label="'Swagger URL'" prop="wgerUrl" v-if="selectedPlatformValue == 'Swagger2' && swaggerUrlEable">
            <el-input size="small" v-model="formData.swaggerUrl" clearable show-word-limit />
          </el-form-item>

          <el-form-item  v-if="selectedPlatformValue == 'Swagger2'">
            <el-switch
              v-model="swaggerUrlEable"
              :active-text="$t('api_test.api_import.swagger_url_import')">
            </el-switch>
          </el-form-item>
        </el-col>

        <el-col :span="1" v-if="selectedPlatformValue != 'Swagger2' || (selectedPlatformValue == 'Swagger2' && !swaggerUrlEable)">
          <el-divider direction="vertical"/>
        </el-col>
        <el-col :span="12" v-if="selectedPlatformValue != 'Swagger2' || (selectedPlatformValue == 'Swagger2' && !swaggerUrlEable)">
            <el-upload
              class="api-upload"
              drag
              action=""
              :http-request="upload"
              :limit="1"
              :beforeUpload="uploadValidate"
              :on-remove="handleRemove"
              :file-list="fileList"
              :on-exceed="handleExceed"
              multiple>
              <i class="el-icon-upload"></i>
              <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
              <div class="el-upload__tip" slot="tip">{{$t('api_test.api_import.file_size_limit')}}</div>
            </el-upload>
        </el-col>
      </el-row>
    </el-form>

    <div class="format-tip">
      <div>
        <span>{{$t('api_test.api_import.tip')}}：{{selectedPlatform.tip}}</span>
      </div>
      <div>
        <span>{{$t('api_test.api_import.export_tip')}}：{{selectedPlatform.exportTip}}</span>
      </div>
    </div>

    <api-environment-config ref="environmentConfig" @close="getEnvironments"/>

  </el-dialog>
</template>

<script>
    import MsDialogFooter from "../../../../common/components/MsDialogFooter";
    import ApiEnvironmentConfig from "../ApiEnvironmentConfig";
    import {listenGoBack, removeGoBackListener} from "../../../../../../common/js/utils";
    export default {
      name: "ApiImport",
      components: {ApiEnvironmentConfig, MsDialogFooter},
      data() {
        return {
          visible: false,
          swaggerUrlEable: false,
          showEnvironmentSelect: true,
          platforms: [
            {
              name: 'Metersphere',
              value: 'Metersphere',
              tip: this.$t('api_test.api_import.ms_tip'),
              exportTip: this.$t('api_test.api_import.ms_export_tip'),
              suffixes: new Set(['json'])
            },
            {
              name: 'Postman',
              value: 'Postman',
              tip: this.$t('api_test.api_import.postman_tip'),
              exportTip: this.$t('api_test.api_import.post_export_tip'),
              suffixes: new Set(['json'])
            },
            {
              name: 'Swagger',
              value: 'Swagger2',
              tip: this.$t('api_test.api_import.swagger_tip'),
              exportTip: this.$t('api_test.api_import.swagger_export_tip'),
              suffixes: new Set(['json'])
            }
          ],
          selectedPlatform: {},
          selectedPlatformValue: 'Metersphere',
          result: {},
          projects: [],
          environments: [],
          useEnvironment: false,
          formData: {
            name: '',
            environmentId: '',
            projectId: '',
            file: undefined,
            swaggerUrl: ''
          },
          rules: {
            name: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 60, message: this.$t('commons.input_limit', [1, 60]), trigger: 'blur'}
            ],
            environmentId: [
              {required: true, message: this.$t('api_test.environment.select_environment'), trigger: 'blur'},
            ],
            projectId: [
              {required: true, message: this.$t('api_test.select_project'), trigger: 'blur'},
            ]
          },
          fileList: []
        }
      },
      activated() {
        this.selectedPlatform = this.platforms[0];
        this.getProjects();
      },
      watch: {
        selectedPlatformValue() {
          for (let i in this.platforms) {
            if (this.platforms[i].value === this.selectedPlatformValue) {
              this.selectedPlatform = this.platforms[i];
              break;
            }
          }
        },
        'formData.projectId'() {
          this.getEnvironments();
        }
      },
      methods: {
        open() {
          this.visible = true;
          listenGoBack(this.close);
        },
        upload(file) {
          this.formData.file = file.file;
        },
        handleExceed(files, fileList) {
          this.$warning(this.$t('test_track.case.import.upload_limit_count'));
        },
        handleRemove(file, fileList) {
          this.formData.file = undefined;
        },
        uploadValidate(file, fileList) {
          let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
          if (!this.selectedPlatform.suffixes.has(suffix)) {
            this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
            return false;
          }

          if (file.size / 1024 / 1024 > 20) {
            this.$warning(this.$t('test_track.case.import.upload_limit_size'));
            return false;
          }
          return true;
        },
        getEnvironments() {
          if (this.formData.projectId) {
            this.$get('/api/environment/list/' + this.formData.projectId, response => {
              this.environments = response.data;
              let hasEnvironmentId = false;
              this.environments.forEach(env => {
                if (env.id === this.formData.environmentId) {
                  hasEnvironmentId = true;
                }
              });
              if (!hasEnvironmentId) {
                this.formData.environmentId = '';
              }
            });
          } else {
            this.environments = [];
            this.formData.environmentId = '';
          }
        },
        getProjects() {
          this.result = this.$get("/project/listAll", response => {
            this.projects = response.data;
          })
        },
        openEnvironmentConfig() {
          if (!this.formData.projectId) {
            this.$error(this.$t('api_test.select_project'));
            return;
          }
          this.showEnvironmentSelect = false;
          this.$refs.environmentConfig.open(this.formData.projectId);
          this.showEnvironmentSelect = true;
        },
        save() {
          this.$refs.form.validate(valid => {
            if (valid) {
              let param = {};
              Object.assign(param, this.formData);
              param.platform = this.selectedPlatformValue;
              param.useEnvironment = this.useEnvironment;
              if ((this.selectedPlatformValue != 'Swagger2' || (this.selectedPlatformValue == 'Swagger2' && !this.swaggerUrlEable)) && !this.formData.file) {
                this.$warning(this.$t('commons.please_upload'));
                return ;
              }
              if (!this.swaggerUrlEable) {
                param.swaggerUrl = undefined;
              }
              this.result = this.$fileUpload('/api/import', param.file, null, param,response => {
                let res = response.data;
                this.$success(this.$t('test_track.case.import.success'));
                this.visible = false;
                this.$router.push({path: '/api/test/edit', query: {id: res.id}});
              });
            } else {
              return false;
            }
          });
        },
        close() {
          this.formData =  {
            name: '',
            environmentId: '',
            projectId: '',
            file: undefined,
            swaggerUrl: ''
          };
          this.fileList = [];
          removeGoBackListener(this.close);
          this.visible = false;
        }
      }
    }
</script>

<style scoped>

  .format-tip {
    background: #EDEDED;
  }

  .api-upload {
    text-align: center;
    margin: auto 0;
  }

  .api-upload >>> .el-upload {
    width: 100%;
    max-width: 350px;
  }

  .api-upload  >>> .el-upload-dragger {
    width: 100%;
  }

  .el-radio-group {
    margin: 10px 0;
  }

  .header-bar,.format-tip,.el-form {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }

  .header-bar {
    padding: 10px 30px;
  }

  .api-import >>> .el-dialog__body {
    padding: 15px 25px;
  }

  .operate-button {
    float: right;
  }

  .save-button {
    margin-left: 10px;
  }

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

  .empty-environment {
    padding: 10px 0px;
  }

  .el-form {
    padding: 30px 10px;
  }

  .el-divider {
    height: 200px;
  }

  .name-input {
    max-width: 195px;
  }

  .dialog-footer {
    float: right;
  }

</style>
