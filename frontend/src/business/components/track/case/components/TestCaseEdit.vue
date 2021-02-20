<template>

  <el-dialog :close-on-click-modal="false" class="case-dialog"
             @close="close"
             :title="operationType == 'edit' ? ( readOnly ? $t('test_track.case.view_case') : $t('test_track.case.edit_case')) : $t('test_track.case.create')"
             :visible.sync="dialogFormVisible" width="85%" v-if="dialogFormVisible">

    <template v-slot:title>
      <el-row>
        <el-col :span="4">
         <span>
           {{
             operationType == 'edit' ? (readOnly ? $t('test_track.case.view_case') : $t('test_track.case.edit_case')) : $t('test_track.case.create')
           }}
         </span>
        </el-col>
        <el-col class="head-right" :span="19">
          <ms-previous-next-button v-if="operationType == 'edit'" :index="index" @pre="handlePre" @next="handleNext" :list="testCases"/>
        </el-col>
      </el-row>
    </template>

    <el-row :gutter="10">
      <div>
        <el-col :span="17">
          <el-card class="container">
            <el-form :model="form" :rules="rules" ref="caseFrom" v-loading="result.loading" class="case-form">

              <el-row>
                <el-col :span="8" :offset="1">
                  <el-form-item
                    :placeholder="$t('test_track.case.input_name')"
                    :label="$t('test_track.case.name')"
                    :label-width="formLabelWidth"
                    prop="name">
                    <el-input class="case-name" :disabled="readOnly" v-model="form.name"></el-input>
                  </el-form-item>
                </el-col>

                <el-col :span="11" :offset="2">
                  <el-form-item :label="$t('test_track.case.module')" :label-width="formLabelWidth" prop="module">
                    <el-select
                      v-model="form.module"
                      :disabled="readOnly"
                      :placeholder="$t('test_track.case.input_module')"
                      filterable>
                      <el-option
                        v-for="item in moduleOptions"
                        :key="item.id"
                        :label="item.path"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="10" :offset="1">
                  <el-form-item :label="$t('test_track.case.maintainer')" :label-width="formLabelWidth" prop="maintainer">
                    <el-select :disabled="readOnly" v-model="form.maintainer"
                               :placeholder="$t('test_track.case.input_maintainer')" filterable>
                      <el-option
                        v-for="item in maintainerOptions"
                        :key="item.id"
                        :label="item.id + ' (' + item.name + ')'"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="$t('test_track.case.priority')" :label-width="formLabelWidth" prop="priority">
                    <el-select :disabled="readOnly" v-model="form.priority" clearable
                               :placeholder="$t('test_track.case.input_priority')">
                      <el-option label="P0" value="P0"></el-option>
                      <el-option label="P1" value="P1"></el-option>
                      <el-option label="P2" value="P2"></el-option>
                      <el-option label="P3" value="P3"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="10" :offset="1">
                  <el-form-item :label="$t('commons.tag')" :label-width="formLabelWidth" prop="tag">
                    <ms-input-tag :currentScenario="form" v-if="showInputTag" ref="tag"/>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row>
                <el-col :span="10" :offset="1">
                  <el-form-item :label="$t('test_track.case.type')" :label-width="formLabelWidth" prop="type">
                    <el-select @change="typeChange" :disabled="readOnly" v-model="form.type"
                               :placeholder="$t('test_track.case.input_type')">
                      <el-option :label="$t('commons.functional')" value="functional"></el-option>
                      <el-option :label="$t('commons.performance')" value="performance"></el-option>
                      <el-option :label="$t('commons.api')" value="api"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item :label="$t('test_track.case.method')" :label-width="formLabelWidth" prop="method">
                    <el-select :disabled="readOnly" v-model="form.method" :placeholder="$t('test_track.case.input_method')">
                      <el-option
                        v-for="item in methodOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="form.method && form.method == 'auto'">
                <el-col :span="9" :offset="1">
                  <el-form-item :label="$t('test_track.case.relate_test')" :label-width="formLabelWidth" prop="testId">
                    <el-select filterable :disabled="readOnly" v-model="form.testId"
                               :placeholder="$t('test_track.case.input_type')">
                      <el-option
                        v-for="item in testOptions"
                        :key="item.id"
                        :label="item.name"
                        :value="item.id">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="9" :offset="1" v-if="form.testId=='other'">
                  <el-form-item :label="$t('test_track.case.test_name')" :label-width="formLabelWidth" prop="testId">
                    <el-input v-model="form.otherTestName" :placeholder="$t('test_track.case.input_test_case')"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row style="margin-top: 15px;">
                <el-col :offset="2">{{ $t('test_track.case.prerequisite') }}:</el-col>
              </el-row>
              <el-row type="flex" justify="center" style="margin-top: 10px;">
                <el-col :span="20">
                  <el-form-item prop="prerequisite">
                    <el-input :disabled="readOnly" v-model="form.prerequisite"
                              type="textarea"
                              :autosize="{ minRows: 2, maxRows: 4}"
                              :rows="2"
                              :placeholder="$t('test_track.case.input_prerequisite')"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row v-if="form.method && form.method != 'auto'" style="margin-bottom: 10px">
                <el-col :offset="2">{{ $t('test_track.case.steps') }}:</el-col>
              </el-row>

              <el-row v-if="form.method && form.method != 'auto'" type="flex" justify="center">
                <el-col :span="20">
                  <el-table
                    v-if="isStepTableAlive"
                    :data="form.steps"
                    class="tb-edit"
                    border
                    size="mini"
                    :default-sort="{prop: 'num', order: 'ascending'}"
                    highlight-current-row>
                    <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="10%"></el-table-column>
                    <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="35%">
                      <template v-slot:default="scope">
                        <el-input
                          class="table-edit-input"
                          size="mini"
                          :disabled="readOnly"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 6}"
                          :rows="2"
                          v-model="scope.row.desc"
                          :placeholder="$t('commons.input_content')"
                          clearable/>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="35%">
                      <template v-slot:default="scope">
                        <el-input
                          class="table-edit-input"
                          size="mini"
                          :disabled="readOnly"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 6}"
                          :rows="2"
                          v-model="scope.row.result"
                          :placeholder="$t('commons.input_content')"
                          clearable/>
                      </template>
                    </el-table-column>
                    <el-table-column :label="$t('commons.input_content')" min-width="25%">
                      <template v-slot:default="scope">
                        <el-button
                          type="primary"
                          :disabled="readOnly"
                          icon="el-icon-plus"
                          circle size="mini"
                          @click="handleAddStep(scope.$index, scope.row)"></el-button>
                        <el-button
                          icon="el-icon-document-copy"
                          type="success"
                          :disabled="readOnly"
                          circle size="mini"
                          @click="handleCopyStep(scope.$index, scope.row)"></el-button>
                        <el-button
                          type="danger"
                          icon="el-icon-delete"
                          circle size="mini"
                          @click="handleDeleteStep(scope.$index, scope.row)"
                          :disabled="readOnly || (scope.$index == 0 && form.steps.length <= 1)"></el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-col>
              </el-row>

              <el-row style="margin-top: 15px;margin-bottom: 10px">
                <el-col :offset="2">{{ $t('commons.remark') }}:</el-col>
              </el-row>
              <el-row type="flex" justify="center">
                <el-col :span="20">
                  <el-form-item prop="remark">
                    <el-input v-model="form.remark"
                              :autosize="{ minRows: 2, maxRows: 4}"
                              type="textarea"
                              :disabled="readOnly"
                              :rows="2"
                              :placeholder="$t('commons.input_content')"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row style="margin-top: 15px;margin-bottom: 10px">
                <el-col :offset="2" :span="20">{{ $t('test_track.case.attachment') }}:
                  <el-upload
                    accept=".jpg,.jpeg,.png,.xlsx,.doc,.pdf,.docx"
                    action=""
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    :http-request="handleUpload"
                    :on-exceed="handleExceed"
                    multiple
                    :limit="8"
                    :file-list="fileList">
                    <el-button icon="el-icon-plus" size="mini"></el-button>
                    <span slot="tip" class="el-upload__tip"> {{ $t('test_track.case.upload_tip') }} </span>
                  </el-upload>
                </el-col>
                <el-col :offset="2" :span="20">
                  <test-case-attachment :table-data="tableData"
                                        :read-only="readOnly"
                                        :is-delete="true"
                                        @handleDelete="handleDelete"
                  />
                </el-col>
              </el-row>

            </el-form>
            <el-row style="float: right; margin-bottom: 20px;margin-top: 20px">
              <el-switch v-if="operationType == 'add'"
                         v-model="isCreateContinue"
                         :active-text="$t('test_track.case.save_create_continue')">
              </el-switch>
              <ms-dialog-footer v-if="!readOnly"
                                @cancel="dialogFormVisible = false"
                                @confirm="saveCase"/>
            </el-row>
          </el-card>
        </el-col>
        <el-col :span="7">
          <case-comment :case-id="testCase ? testCase.id : ''" class="comment-card"/>
        </el-col>
      </div>
    </el-row>

  </el-dialog>


</template>

<script>

import {TokenKey, WORKSPACE_ID} from '@/common/js/constants';
import MsDialogFooter from '../../../common/components/MsDialogFooter'
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
import {Message} from "element-ui";
import TestCaseAttachment from "@/business/components/track/case/components/TestCaseAttachment";
import {getCurrentProjectID} from "../../../../../common/js/utils";
import {buildNodePath} from "../../../api/definition/model/NodeTree";
import CaseComment from "@/business/components/track/case/components/CaseComment";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsPreviousNextButton from "../../../common/components/MsPreviousNextButton";

export default {
  name: "TestCaseEdit",
  components: {MsPreviousNextButton, MsInputTag, CaseComment, MsDialogFooter, TestCaseAttachment},
  data() {
    return {
      result: {},
      projectId: "",
      dialogFormVisible: false,
      form: {
        name: '',
        module: '',
        maintainer: '',
        priority: '',
        type: '',
        method: '',
        prerequisite: '',
        testId: '',
        otherTestName: '',
        steps: [{
          num: 1,
          desc: '',
          result: ''
        }],
        remark: '',
        tags: [],
      },
      moduleOptions: [],
      maintainerOptions: [],
      testOptions: [],
      workspaceId: '',
      fileList: [],
      tableData: [],
      uploadList: [],
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 255, message: this.$t('test_track.length_less_than') + '255', trigger: 'blur'}
        ],
        module: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        maintainer: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        priority: [{required: true, message: this.$t('test_track.case.input_priority'), trigger: 'change'}],
        type: [{required: true, message: this.$t('test_track.case.input_type'), trigger: 'change'}],
        testId: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        method: [{required: true, message: this.$t('test_track.case.input_method'), trigger: 'change'}],
        prerequisite: [{max: 500, message: this.$t('test_track.length_less_than') + '500', trigger: 'blur'}],
        remark: [{max: 500, message: this.$t('test_track.length_less_than') + '500', trigger: 'blur'}]
      },
      formLabelWidth: "120px",
      operationType: '',
      isCreateContinue: false,
      isStepTableAlive: true,
      methodOptions: [
        {value: 'auto', label: this.$t('test_track.case.auto')},
        {value: 'manual', label: this.$t('test_track.case.manual')}
      ],
      testCase: {},
      testCases: [],
      index: 0,
      showInputTag: true,
    };
  },
  props: {
    treeNodes: {
      type: Array
    },
    readOnly: {
      type: Boolean,
      default: true
    },
    selectNode: {
      type: Object
    },
    selectCondition: {
      type: Object
    },
  },
  mounted() {
    this.getSelectOptions();
  },
  watch: {
    treeNodes() {
      this.getModuleOptions();
    },
    moduleOptions() {
      this.$emit('setModuleOptions', this.moduleOptions);
    }
  },
  methods: {
    reload() {
      this.isStepTableAlive = false;
      this.$nextTick(() => (this.isStepTableAlive = true));
    },
    open(testCase) {
      this.projectId = getCurrentProjectID();
      if (window.history && window.history.pushState) {
        history.pushState(null, null, document.URL);
        window.addEventListener('popstate', this.close);
      }
      this.resetForm();
      listenGoBack(this.close);
      this.operationType = 'add';
      if (testCase) {
        //修改
        this.operationType = 'edit';
        //复制
        if (testCase.name === '') {
          this.operationType = 'add';
          this.setFormData(testCase);
          this.setTestCaseExtInfo(testCase);
          this.getSelectOptions();
          this.reload();
        } else {
          this.initTestCases(testCase);
        }
      } else {
        if (this.selectNode.data) {
          this.form.module = this.selectNode.data.id;
        } else {
          if (this.moduleOptions.length > 0) {
            this.form.module = this.moduleOptions[0].id;
          }
        }
        let user = JSON.parse(localStorage.getItem(TokenKey));
        this.form.priority = 'P3';
        this.form.type = 'functional';
        this.form.method = 'manual';
        this.form.maintainer = user.id;
        this.form.tags = [];
        this.getSelectOptions();
        this.reload();
      }
      this.dialogFormVisible = true;
    },
    handlePre() {
      this.index--;
      this.getTestCase(this.index)
    },
    handleNext() {
      this.index++;
      this.getTestCase(this.index);
    },
    initTestCases(testCase) {
      this.result = this.$post('/test/case/list/ids', this.selectCondition, response => {
        this.testCases = response.data;
        for (let i = 0; i < this.testCases.length; i++) {
          if (this.testCases[i].id === testCase.id) {
            this.index = i;
            this.getTestCase(i);
          }
        }
      });
    },
    getTestCase(index) {
      this.showInputTag = false;
      let testCase = this.testCases[index];
      this.result = this.$get('/test/case/get/' + testCase.id, response => {
        let testCase = response.data;
        this.setFormData(testCase);
        this.setTestCaseExtInfo(testCase);
        this.getSelectOptions();
        this.reload();
        this.$nextTick(() => {
          this.showInputTag = true
        })
      })
    },
    setFormData(testCase) {
      testCase.tags = JSON.parse(testCase.tags);
      let tmp = {};
      Object.assign(tmp, testCase);
      tmp.steps = JSON.parse(testCase.steps);
      Object.assign(this.form, tmp);
      this.form.module = testCase.nodeId;
      this.getFileMetaData(testCase);
    },
    setTestCaseExtInfo(testCase) {
      this.testCase = {};
      if (testCase) {
        // 复制 不查询评论
        this.testCase = testCase.isCopy ? {} : testCase;
      }
    },
    getFileMetaData(testCase) {
      this.fileList = [];
      this.tableData = [];
      this.uploadList = [];
      this.result = this.$get("test/case/file/metadata/" + testCase.id, response => {
        let files = response.data;

        if (!files) {
          return;
        }
        // deep copy
        this.fileList = JSON.parse(JSON.stringify(files));
        this.tableData = JSON.parse(JSON.stringify(files));
        this.tableData.map(f => {
          f.size = f.size + ' Bytes';
        });
      })
    },
    handleAddStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = "";
      step.result = "";
      this.form.steps.forEach(step => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.form.steps.splice(index + 1, 0, step);
    },
    handleCopyStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = data.desc;
      step.result = data.result;
      this.form.steps.forEach(step => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.form.steps.splice(index + 1, 0, step);
    },
    handleDeleteStep(index, data) {
      this.form.steps.splice(index, 1);
      this.form.steps.forEach(step => {
        if (step.num > data.num) {
          step.num--;
        }
      });
    },
    close() {
      //移除监听，防止监听其他页面
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    },
    saveCase() {
      this.$refs['caseFrom'].validate((valid) => {
        if (valid) {
          let param = this.buildParam();
          if (this.validate(param)) {
            let option = this.getOption(param);
            this.result = this.$request(option, () => {
              this.$success(this.$t('commons.save_success'));
              if (this.operationType == 'add' && this.isCreateContinue) {
                this.form.name = '';
                this.form.prerequisite = '';
                this.form.steps = [{
                  num: 1,
                  desc: '',
                  result: ''
                }];
                this.form.remark = '';
                this.uploadList = [];
                this.fileList = [];
                this.tableData = [];
                this.$emit("refresh");
                return;
              }
              this.dialogFormVisible = false;
              this.$emit("refresh");
              // 发送广播，刷新 head 上的最新列表
              TrackEvent.$emit(LIST_CHANGE);
            });
          }
        } else {
          return false;
        }
      });
    },
    buildParam() {
      let param = {};
      Object.assign(param, this.form);
      param.steps = JSON.stringify(this.form.steps);
      param.nodeId = this.form.module;
      this.moduleOptions.forEach(item => {
        if (this.form.module === item.id) {
          param.nodePath = item.path;
        }
      });
      if (this.projectId) {
        param.projectId = this.projectId;
      }
      param.name = param.name.trim();
      if (param.method != 'auto') {
        param.testId = null;
      }
      if (this.form.tags instanceof Array) {
        this.form.tags = JSON.stringify(this.form.tags);
      }
      param.tags = this.form.tags;
      return param;
    },
    getOption(param) {
      let formData = new FormData();
      let url = '/test/case/' + this.operationType;
      this.uploadList.forEach(f => {
        formData.append("file", f);
      });

      if (param.isCopy) {
        // 如果是copy，则把文件的ID传到后台进行文件复制
        param.fileIds = this.fileList.map(f => f.id);
      }

      param.updatedFileList = this.fileList;

      let requestJson = JSON.stringify(param, function (key, value) {
        return key === "file" ? undefined : value
      });

      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));

      return {
        method: 'POST',
        url: url,
        data: formData,
        headers: {
          'Content-Type': undefined
        }
      };
    },
    validate(param) {
      for (let i = 0; i < param.steps.length; i++) {
        if ((param.steps[i].desc && param.steps[i].desc.length > 300) ||
          (param.steps[i].result && param.steps[i].result.length > 300)) {
          this.$warning(this.$t('test_track.case.step_desc') + ","
            + this.$t('test_track.case.expected_results') + this.$t('test_track.length_less_than') + '300');
          return false;
        }
      }
      if (param.name == '') {
        this.$warning(this.$t('test_track.case.input_name'));
        return false;
      }
      return true;
    },
    typeChange() {
      this.form.testId = '';
      this.getTestOptions()
    },
    getModuleOptions() {
      let moduleOptions = [];
      this.treeNodes.forEach(node => {
        buildNodePath(node, {path: ''}, moduleOptions);
      });
      this.moduleOptions = moduleOptions;
    },
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.maintainerOptions = response.data;
      });
    },
    getTestOptions() {
      this.testOptions = [];
      if (this.projectId && this.form.type != '' && this.form.type != 'functional') {
        this.result = this.$get('/' + this.form.type + '/list/' + this.projectId, response => {
          this.testOptions = response.data;
          this.testOptions.unshift({id: 'other', name: this.$t('test_track.case.other')})
        });
      } else if (this.form.type === 'functional') {
        this.testOptions = [{id: 'other', name: this.$t('test_track.case.other')}];
        this.form.testId = 'other';
      }
    },
    getSelectOptions() {
      this.getModuleOptions();
      this.getMaintainerOptions();
      this.getTestOptions();
    },

    resetForm() {
      //防止点击修改后，点击新建触发校验
      if (this.$refs['caseFrom']) {
        this.$refs['caseFrom'].validate((valid) => {
          this.$refs['caseFrom'].resetFields();
          this._resetForm();
          return true;
        });
      } else {
        this._resetForm();
      }
    },
    _resetForm() {
      this.form.name = '';
      this.form.module = '';
      this.form.type = '';
      this.form.method = '';
      this.form.maintainer = '';
      this.form.priority = '';
      this.form.prerequisite = '';
      this.form.remark = '';
      this.form.testId = '';
      this.form.testName = '';
      this.form.steps = [{
        num: 1,
        desc: '',
        result: ''
      }];
      this.uploadList = [];
      this.fileList = [];
      this.tableData = [];
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    beforeUpload(file) {
      if (!this.fileValidator(file)) {
        /// todo: 显示错误信息
        return false;
      }

      if (this.tableData.filter(f => f.name === file.name).length > 0) {
        this.$error(this.$t('load_test.delete_file'));
        return false;
      }

      let type = file.name.substring(file.name.lastIndexOf(".") + 1);

      this.tableData.push({
        name: file.name,
        size: file.size + ' Bytes', /// todo: 按照大小显示Byte、KB、MB等
        type: type.toUpperCase(),
        updateTime: new Date().getTime(),
      });

      return true;
    },
    handleUpload(uploadResources) {
      this.uploadList.push(uploadResources.file);
    },
    handleDownload(file) {
      let data = {
        name: file.name,
        id: file.id,
      };
      let config = {
        url: '/test/case/file/download',
        method: 'post',
        data: data,
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
        const content = response.data;
        const blob = new Blob([content]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = file.name;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href)
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, this.filename)
        }
      }).catch(e => {
        Message.error({message: e.message, showClose: true});
      });
    },
    handleDelete(file, index) {
      this.$alert(this.$t('load_test.delete_file_confirm') + file.name + "？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this._handleDelete(file, index);
          }
        }
      });
    },
    _handleDelete(file, index) {
      this.fileList.splice(index, 1);
      this.tableData.splice(index, 1);
      let i = this.uploadList.findIndex(upLoadFile => upLoadFile.name === file.name);
      if (i > -1) {
        this.uploadList.splice(i, 1);
      }
    },
    fileValidator(file) {
      /// todo: 是否需要对文件内容和大小做限制
      return file.size > 0;
    },
  }
}
</script>

<style scoped>

.el-switch {
  margin-bottom: 10px;
}

.case-name {
  width: 194px;
}

.container {
  height: 100vh;
}

.case-form {
  height: 95%;
  overflow: auto;
}

.case-dialog >>> .el-dialog__body {
  padding: 0 20px 10px 20px;
}

.container >>> .el-card__body {
  height: calc(100vh - 120px);
}

.comment-card >>> .el-card__header {
  padding: 27px 20px;
}

.comment-card >>> .el-card__body {
  height: calc(100vh - 120px);
}

.head-right {
  text-align: right;
}

</style>
