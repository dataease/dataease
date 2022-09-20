<template>
  <de-layout-content>
    <div class="de-template">
      <div
        class="tabs-container flex-tabs"
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      >
        <div class="de-tabs-left">
          <template-list
            ref="templateList"
            :template-type="currentTemplateType"
            :template-list="templateList"
            @templateDelete="templateListDelete"
            @templateEdit="templateEdit"
            @showCurrentTemplate="showCurrentTemplate"
            @templateImport="templateImport"
            @showTemplateEditDialog="showTemplateEditDialog"
          />
        </div>
        <div class="de-tabs-right">
          <div v-if="currentTemplateLabel" class="active-template">
            {{ currentTemplateLabel }}&nbsp;&nbsp;({{
              currentTemplateShowList.length
            }})
            <deBtn
              type="primary"
              @click="templateImport(currentTemplateId)"
              icon="el-icon-upload2"
            >
              {{$t('app_template.app_upload')}}
            </deBtn>
          </div>
          <el-empty
            :image="noneImg"
            v-if="!currentTemplateShowList.length"
            :description="$t('app_template.no_apps')"
          ></el-empty>
          <div
            id="template-box"
            v-show="currentTemplateId !== ''"
            class="template-box"
          >
            <template-item
              v-for="item in currentTemplateShowList"
              :key="item.id"
              :width="templateCurWidth"
              :model="item"
              @command="(key) => handleCommand(key, item)"
            />
          </div>
        </div>
      </div>
    </div>
    <el-dialog
      :title="dialogTitle"
      :visible.sync="editTemplate"
      append-to-body
      class="de-dialog-form"
      width="600px"
      destroy-on-close="true"
    >
      <el-form
        ref="templateEditForm"
        class="de-form-item"
        :model="templateEditForm"
        :rules="templateEditFormRules"
      >
        <el-form-item :label="dialogTitleLabel" prop="name">
          <el-input v-model="templateEditForm.name"/>
        </el-form-item>
        <el-form-item :label="$t('app_template.app_group_icon')" prop="icon">
          <el-col style="width: 148px!important;height: 148px!important;overflow: hidden">
            <el-upload
              action=""
              accept=".jpeg,.jpg,.png,.gif,.svg"
              class="avatar-uploader"
              list-type="picture-card"
              :class="{disabled:uploadDisabled}"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :http-request="upload"
              :file-list="fileList"
            >
              <i class="el-icon-plus"/>
            </el-upload>
            <el-dialog top="25vh" width="600px" :append-to-body="true" :destroy-on-close="true"
                       :visible.sync="dialogVisible">
              <img width="100%" :src="dialogImageUrl">
            </el-dialog>
          </el-col>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <deBtn secondary @click="close()">{{ $t("commons.cancel") }}</deBtn>
        <deBtn type="primary" @click="saveTemplateEdit(templateEditForm)"
        >{{ $t("commons.confirm") }}
        </deBtn>
      </div>
    </el-dialog>

    <!--导入templatedialog-->
    <el-dialog
      :title="templateDialog.title"
      :visible.sync="templateDialog.visible"
      :show-close="true"
      class="de-dialog-form"
      width="600px"
    >
      <template-import
        v-if="templateDialog.visible"
        :pid="templateDialog.pid"
        :opt-type="templateOptType"
        :app-template-info="currentAppTemplateInfo"
        @refresh="showCurrentTemplate(currentTemplateId,
        currentTemplateLabel)"
        @closeEditTemplateDialog="closeEditTemplateDialog"
      />
    </el-dialog>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from "@/components/business/DeLayoutContent";
import TemplateList from "./component/TemplateList";
import TemplateItem from "./component/TemplateItem";
import TemplateImport from "./component/TemplateImport";
import {save, update, templateDelete, find} from "@/api/system/appTemplate";
import elementResizeDetectorMaker from "element-resize-detector";
import msgCfm from "@/components/msgCfm/index";
import {uploadFileResult} from "@/api/staticResource/staticResource";
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  name: "AppTemplate",
  mixins: [msgCfm],
  components: {DeLayoutContent, TemplateList, TemplateItem, TemplateImport},
  data() {
    return {
      templateOptType:'add',
      currentAppTemplateInfo:null,
      fileList: [],
      dialogImageUrl: '',
      dialogVisible: false,
      uploadDisabled: false,
      showShare: false,
      currentTemplateShowList: [],
      noneImg: require('@/assets/None.png'),
      currentPid: "",
      currentTemplateType: "self",
      templateEditFormRules: {
        name: [
          {required: true, trigger: "blur", validator: this.roleValidator},
          {
            required: true,
            message: this.$t("commons.input_content"),
            trigger: "blur",
          },
          {
            max: 50,
            message: this.$t("commons.char_can_not_more_50"),
            trigger: "change",
          },
        ],
        icon: [
          {
            required: true,
            message: '请选择文件',
            trigger: "change",
          },
        ],
      },
      templateEditForm: {},
      editTemplate: false,
      dialogTitle: "",
      dialogTitleLabel: "",
      currentTemplateLabel: "",
      currentTemplateId: "",
      templateList: [],
      templateMiniWidth: 286,
      templateCurWidth: 286,
      formType: "",
      originName: "",
      templateDialog: {
        title: '导入应用',
        visible: false,
        pid: "",
      },
    };
  },
  computed: {
    nameList() {
      const {nodeType} = this.templateEditForm || {};
      if ("template" === nodeType) {
        return this.currentTemplateShowList.map((ele) => ele.name);
      }

      if ("folder" === nodeType) {
        return this.templateList.map((ele) => ele.name);
      }
      return [];
    },
  },
  mounted() {
    this.getTree();
    const _this = this;
    const erd = elementResizeDetectorMaker();
    const templateMainDom = document.getElementById("template-box");
    // 监听div变动事件
    erd.listenTo(templateMainDom, (element) => {
      _this.$nextTick(() => {
        const curSeparator = Math.trunc(
          templateMainDom.offsetWidth / _this.templateMiniWidth
        );
        _this.templateCurWidth =
          Math.trunc(templateMainDom.offsetWidth / curSeparator) - 24 - curSeparator;
      });
    });
  },
  methods: {
    roleValidator(rule, value, callback) {
      if (this.nameRepeat(value)) {
        const {nodeType} = this.templateEditForm || {};
        callback(
          new Error(
            this.$t(
              `system_parameter_setting.${
                "folder" === nodeType
                  ? "name_already_exists_type"
                  : "the_same_category"
              }`
            )
          )
        );
      } else {
        callback();
      }
    },
    nameRepeat(value) {
      if (!this.nameList || this.nameList.length === 0) {
        return false;
      }
      // 编辑场景 不能 因为名称重复而报错
      if (this.formType === "edit" && this.originName === value) {
        return false;
      }
      return this.nameList.some((name) => name === value);
    },
    handleCommand(key, data) {
      switch (key) {
        case "rename":
          this.templateEdit(data);
          break;
        case "delete":
          this.templateDeleteConfirm(data);
          break;
        case "update":
          this.updateAppTemplate(data);
          break;
        default:
          break;
      }
    },
    updateAppTemplate(data){
      this.templateOptType='update';
      this.templateDialog.visible = true
      this.currentAppTemplateInfo = data
      this.templateDialog.pid = data.pid;
    },
    templateDeleteConfirm(template) {
      const options = {
        title: "是否卸载当前应用？",
        type: "primary",
        cb: () => this.templateDelete(template.id),
      };
      this.handlerConfirm(options,"卸载");
    },
    handleClick(tab, event) {
      this.getTree();
    },
    showCurrentTemplate(pid, name) {
      this.currentTemplateId = pid;
      this.currentTemplateLabel = name;
      if (this.currentTemplateId) {
        find({pid: this.currentTemplateId}).then((response) => {
          this.currentTemplateShowList = response.data;
        });
      }
    },
    templateListDelete(id) {
      if (id) {
        templateDelete(id).then((response) => {
          this.openMessageSuccess("commons.delete_success");
          this.getTree();
        });
      }
    },

    templateDelete(id) {
      if (id) {
        templateDelete(id).then((response) => {
          this.openMessageSuccess("commons.delete_success");
          this.showCurrentTemplate(this.currentTemplateId, this.currentTemplateLabel);
        });
      }
    },
    showTemplateEditDialog(type, templateInfo) {
      this.templateEditForm = null;
      this.formType = type;
      if (type === "edit") {
        if (templateInfo.icon) {
          this.fileList.push({url: imgUrlTrans(templateInfo.icon)})
        }
        this.templateEditForm = JSON.parse(JSON.stringify(templateInfo));
        this.dialogTitle = this.$t(
          `system_parameter_setting.${
            "folder" === this.templateEditForm.nodeType
              ? "edit_classification"
              : "edit_template"
          }`
        );
        this.originName = this.templateEditForm.label;
      } else {
        this.fileList = []
        this.dialogTitle = this.$t("panel.add_app_category");
        this.templateEditForm = {
          name: "",
          nodeType: "folder",
          templateType: this.currentTemplateType,
          icon: "",
          level: 0,
          pid: 0,
        };
      }
      this.dialogTitleLabel = this.$t(
        `system_parameter_setting.${
          "folder" === this.templateEditForm.nodeType
            ? "classification_name"
            : "template_name"
        }`
      );
      this.editTemplate = true;
    },
    templateEdit(templateInfo) {
      this.showTemplateEditDialog("edit", templateInfo);
    },
    saveTemplateEdit(templateEditForm) {
      this.$refs["templateEditForm"].validate((valid) => {
          if (valid) {
            const method = templateEditForm.id ? update : save
            method(templateEditForm).then((response) => {
              this.close();
              this.openMessageSuccess(
                `system_parameter_setting.${
                  this.templateEditForm.id
                    ? "rename_succeeded"
                    : "added_successfully"
                }`
              );
              this.getTree();
            });
          } else {
            return false;
          }
      });
    },
    close() {
      this.$refs["templateEditForm"].resetFields();
      this.editTemplate = false;
      this.handleRemove()
    },
    getTree() {
      const request = {
        templateType: this.currentTemplateType,
        pid: "0",
      };
      find(request).then((res) => {
        this.templateList = res.data;
        this.showFirst();
      });
    },
    showFirst() {
      // 判断是否默认点击第一条
      if (this.templateList && this.templateList.length > 0) {
        let showFirst = true;
        this.templateList.forEach((template) => {
          if (template.id === this.currentTemplateId) {
            showFirst = false;
          }
        });
        if (showFirst) {
          this.$nextTick().then(() => {
            const [obj = {}] = this.templateList;
            this.$refs.templateList.nodeClick(obj);
          });
        } else {
          this.showCurrentTemplate(this.currentTemplateId, this.currentTemplateLabel);
        }
      } else {
        this.currentTemplateShowList = [];
      }
    },
    closeEditTemplateDialog() {
      this.templateDialog.visible = false;
    },
    templateImport(pid) {
      this.templateOptType='new';
      this.currentAppTemplateInfo = null;
      this.templateDialog.visible = true;
      this.templateDialog.pid = pid;
    },
    handleRemove(file, fileList) {
      this.uploadDisabled = false
      this.templateEditForm.icon = null
      this.fileList = []
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    upload(file) {
      const _this = this
      uploadFileResult(file.file, (fileUrl) => {
        _this.templateEditForm.icon = fileUrl
      })
    }
  },
};
</script>

<style lang="scss" scoped>
.de-template {
  height: 100%;
  background-color: var(--MainBG, #f5f6f7);

  .tabs-container {
    height: 100%;
    background: var(--ContentBG, #ffffff);
    overflow-x: auto;
  }

  .flex-tabs {
    display: flex;
    background: #f5f6f7;
  }

  .de-tabs-left {
    background: #fff;
    width: 269px;
    border-right: 1px solid rgba(31, 35, 41, 0.15);
    padding: 24px;
  }

  .de-tabs-right {
    flex: 1;
    background: #fff;
    padding: 24px 0 24px 24px;
    overflow: hidden;

    .template-box {
      display: flex;
      flex-wrap: wrap;
      overflow-y: auto;
      box-sizing: border-box;
      align-content: flex-start;
      height: calc(100% - 10px);
      width: 100%;
      padding-bottom: 24px;
    }

    .active-template {
      margin: 4px 0 20px 0;
      padding-right: 24px;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);
    }
  }
}

::v-deep .container-wrapper {
  padding: 0px !important;
}
</style>
