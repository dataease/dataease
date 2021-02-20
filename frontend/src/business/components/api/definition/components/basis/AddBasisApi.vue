<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.definition.request.title')" :visible.sync="httpVisible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="httpForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="httpForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="httpForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>

      <!--HTTP 协议特有参数-->
      <el-form-item :label="$t('api_report.request')" prop="path" v-if="currentProtocol==='HTTP'">
        <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="httpForm.path"
                  class="ms-http-input" size="small">
          <el-select v-model="httpForm.method" slot="prepend" style="width: 100px" size="small">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-input>
      </el-form-item>

      <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
        <el-select v-model="httpForm.userId"
                   :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                   style="width: 100%">
          <el-option
            v-for="item in maintainerOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>

      </el-form-item>
      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input class="ms-http-textarea" v-model="httpForm.description"
                  type="textarea"
                  :autosize="{ minRows: 2, maxRows: 10}"
                  :rows="2" size="small"/>
      </el-form-item>
      <el-form-item class="create-tip">
        {{$t('api_test.definition.create_tip')}}
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="httpVisible = false"
        :isShow="true"
        title="编辑详情"
        @saveAsEdit="saveApi(true)"
        @confirm="saveApi">
      </ms-dialog-footer>

    </template>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {REQ_METHOD} from "../../model/JsonData";
  import {getCurrentProjectID, getCurrentUser} from "../../../../../../common/js/utils";
  import {createComponent, Request} from "../jmeter/components";

  export default {
    name: "MsAddBasisApi",
    components: {MsDialogFooter},
    props: {
      currentProtocol: {
        type: String,
        default: "HTTP"
      },
    },
    data() {
      let validateURL = (rule, value, callback) => {
        if (!this.httpForm.path.startsWith("/") || this.httpForm.path.match(/\s/) != null) {
          callback(this.$t('api_test.definition.request.path_valid_info'));
        }
        callback();
      };
      return {
        httpForm: {environmentId: ""},
        httpVisible: false,
        currentModule: {},
        maintainerOptions: [],
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
          ],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {validator: validateURL, trigger: 'blur'}],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        },
        value: REQ_METHOD[0].id,
        options: REQ_METHOD,
      }
    }
    ,
    methods: {
      saveApi(saveAs) {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            if (this.httpForm.path && this.httpForm.path.match(/\s/) != null) {
              this.$error(this.$t("api_test.definition.request.path_valid_info"));
              return false;
            }
            let bodyFiles = [];
            let path = "/api/definition/create";
            this.setParameter();
            this.result = this.$fileUpload(path, null, bodyFiles, this.httpForm, () => {
              this.httpVisible = false;
              if (saveAs) {
                this.httpForm.request = JSON.stringify(this.httpForm.request);
                this.$emit('saveAsEdit', this.httpForm);
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
        switch (this.currentProtocol) {
          case Request.TYPES.SQL:
            this.initSQL();
            break;
          case Request.TYPES.DUBBO:
            this.initDUBBO();
            break;
          case Request.TYPES.TCP:
            this.initTCP();
            break;
          default:
            this.initHTTP();
            break;
        }
        this.httpForm.bodyUploadIds = [];
        this.httpForm.projectId = getCurrentProjectID();
        this.httpForm.id = this.httpForm.request.id;
        this.httpForm.protocol = this.currentProtocol;

        this.httpForm.request.name = this.httpForm.name;
        this.httpForm.request.protocol = this.currentProtocol;
        if (this.currentProtocol === 'HTTP') {
          this.httpForm.request.method = this.httpForm.method;
          this.httpForm.request.path = this.httpForm.path;
        }
        if (this.currentModule != null) {
          this.httpForm.modulePath = this.currentModule.method != undefined ? this.currentModule.method : null;
          this.httpForm.moduleId = this.currentModule.id;
        }

      },
      initHTTP() {
        let request = createComponent("HTTPSamplerProxy");
        request.path = this.httpForm.path;
        this.httpForm.request = request;
      },
      initSQL() {
        this.httpForm.method = Request.TYPES.SQL;
        this.httpForm.request = createComponent("JDBCSampler");
      },
      initTCP() {
        this.httpForm.method = Request.TYPES.TCP;
        this.httpForm.request = createComponent("TCPSampler");
      },
      initDUBBO() {
        this.httpForm.method = "dubbo://";
        this.httpForm.request = createComponent("DubboSampler");
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      open(currentModule) {
        this.httpForm = {method: REQ_METHOD[0].id, userId: getCurrentUser().id};
        this.currentModule = currentModule;
        this.getMaintainerOptions();
        this.httpVisible = true;
      }
      ,
    }
  }
</script>

<style scoped>

  .create-tip {
    color: #8c939d;
  }

</style>
