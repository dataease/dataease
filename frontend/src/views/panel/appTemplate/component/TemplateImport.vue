<template>
  <div
    class="template-import"
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <el-form
      ref="templateImportForm"
      class="de-form-item"
      :model="templateInfo"
      :rules="templateInfoRules"
    >
      <el-form-item :label="$t('app_template.app_name')" prop="name">
        <div class="flex-template">
          <el-input v-model="templateInfo.name" clearable size="small" />
            <deBtn
              style="margin-left: 10px"
              class="el-icon-upload2"
              secondary
              @click="goFile"
              >
              {{$t('app_template.app_upload')}}
            </deBtn
            >
            <input
              id="input"
              ref="files"
              type="file"
              accept=".DEAPP"
              hidden
              @change="handleFileChange"
            />
        </div>
      </el-form-item>
    </el-form>
    <el-row class="preview" :style="classBackground" />
    <el-row class="de-root-class">
      <deBtn secondary @click="cancel()">{{
        $t("commons.cancel")
      }}</deBtn>
      <deBtn type="primary" @click="save()">{{
        $t("commons.confirm")
      }}</deBtn>
    </el-row>
  </div>
</template>

<script>
import { save,update, nameCheck } from "@/api/system/appTemplate";
import msgCfm from "@/components/msgCfm/index";
import { find } from "@/api/system/template";
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  mixins: [msgCfm],
  props: {
    pid: {
      type: String,
      required: true,
    },
    optType: {
      type: String,
      required: false
    },
    appTemplateInfo: {
      type: Object,
      required: false,
    },
  },
  mounted() {
    if('update'===this.optType){
      this.templateInfo.id = this.appTemplateInfo.id
      this.templateInfo.name = this.appTemplateInfo.name
      this.templateInfo.snapshot = this.appTemplateInfo.snapshot
      this.templateInfo.pid = this.appTemplateInfo.pid
      this.templateInfo.level = 1
    }
  },
  data() {
    return {
      appResultInfo: {},
      importTemplateInfo: {
        snapshot: "",
      },
      templateInfoRules: {
        name: [
          {
            required: true,
            message: this.$t("commons.input_content"),
            trigger: "change",
          },
        ],
      },
      recover: false,
      templateInfo: {
        id:null,
        level: "1",
        pid: this.pid,
        name: "",
        templateStyle: null,
        templateData: null,
        dynamicData: null,
        staticResource: null,
        snapshot: "",
      },
    };
  },
  computed: {
    classBackground() {
      if (this.templateInfo.snapshot) {
        return {
          background: `url(${imgUrlTrans(this.templateInfo.snapshot)}) no-repeat`,
        };
      } else {
        return {};
      }
    },
  },
  created() {
    this.showCurrentTemplate(this.pid);
  },
  methods: {
    showCurrentTemplate(pid) {
      find({ pid }).then((response) => {
        this.nameList = response.data;
      });
    },
    cancel() {
      this.$emit("closeEditTemplateDialog");
    },
    save() {
      if (!this.templateInfo.name) {
        this.$warning(this.$t("chart.name_can_not_empty"));
        return false;
      }
      if ('update'!==this.optType && !this.templateInfo.templateData) {
        this.$warning(this.$t("chart.template_can_not_empty"));
        return false;
      }
      const nameCheckRequest = {
        pid: this.templateInfo.pid,
        name: this.templateInfo.name,
        optType: "insert",
      };
      if('update'===this.optType){
        nameCheckRequest.optType = 'update'
        nameCheckRequest['id'] = this.templateInfo.id
        this.appResultInfo['id'] = this.templateInfo.id
        this.appResultInfo['optType'] = 'update'
      }
      this.appResultInfo['pid'] =this.templateInfo.pid
      this.appResultInfo['level'] =this.templateInfo.level
      this.appResultInfo['name'] =this.templateInfo.name,
      this.appResultInfo['snapshot'] =this.templateInfo.snapshot
      const method = 'update'===this.optType?update:save
      const successMsg = 'update'===this.optType?'更新成功':this.$t('system_parameter_setting.import_succeeded')
      const _this = this
      nameCheck(nameCheckRequest).then((response) => {
        if (response.data.indexOf("exist") > -1) {
          this.$message({
            message: '当前名称已经存在',
            type: 'error',
            showClose: true
          })
        } else {
          method(_this.appResultInfo).then((response) => {
            this.openMessageSuccess(successMsg);
            this.$emit("refresh");
            this.$emit("closeEditTemplateDialog");
          });
        }
      });
    },
    handleFileChange(e) {
      const file = e.target.files[0];
      const reader = new FileReader();
      const _this = this
      reader.onload = (res) => {
        _this.appResultInfo = JSON.parse(res.target.result);
        _this.importTemplateInfo = JSON.parse(this.appResultInfo.panelInfo)
        _this.templateInfo.name = this.importTemplateInfo.name;
        _this.templateInfo.templateStyle = this.importTemplateInfo.panelStyle;
        _this.templateInfo.templateData = this.importTemplateInfo.panelData;
        _this.templateInfo.snapshot = this.importTemplateInfo.snapshot;
        _this.templateInfo.dynamicData = this.importTemplateInfo.dynamicData;
        _this.templateInfo.staticResource =
          _this.importTemplateInfo.staticResource;
        _this.templateInfo.nodeType = "template";
      };
      reader.readAsText(file);
    },
    goFile() {
      this.$refs.files.click();
    },
  },
};
</script>

<style scoped>
.my_table ::v-deep .el-table__row > td {
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
  border: none;
}
.my_table ::v-deep .el-table::before {
  /* 去除下边框 */
  height: 0;
}

.de-root-class {
  margin-top: 24px;
  text-align: right;
}
.preview {
  margin-top: -12px;
  border: 1px solid #e6e6e6;
  height: 300px !important;
  overflow: auto;
  background-size: 100% 100% !important;
  border-radius: 4px;
}
.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 300px;
  background-size: 100% 100% !important;
}
</style>

<style lang="scss">
.flex-template {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .el-input {
    margin-right: 2px;
    flex: 1;
  }
}
</style>
