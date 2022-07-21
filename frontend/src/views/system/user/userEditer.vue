<template>
  <!-- <layout-content :header="formType=='add' ? $t('user.create') : $t('user.modify')" back-name="system-user">
  </layout-content> -->
  <el-dialog
    :title="formType == 'add' ? $t('user.create') : $t('user.modify')"
    :visible.sync="dialogVisible"
    class="user-editer-form"
    width="840px"
    :before-close="reset"
  >
    <div v-if="formType === 'add'" class="editer-form-title">
      <i class="el-icon-info"></i>
      <span class="pwd" type="text">{{
        $t("commons.default_pwd") + "：" + defaultPWD
      }}</span>
      <el-button
        v-clipboard:copy="defaultPWD"
        v-clipboard:success="onCopy"
        v-clipboard:error="onError"
        class="btn-text"
        type="text"
      >
        {{ $t("commons.copy") }}
      </el-button>
    </div>

    <el-form
      ref="createUserForm"
      :model="form"
      :rules="rule"
      size="small"
      label-width="80px"
      label-position="right"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.nick_name')" prop="nickName">
            <el-input
              :placeholder="$t('user.input_name')"
              v-model="form.nickName"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="ID" prop="username">
            <el-input
              :placeholder="$t('user.input_id')"
              v-model="form.username"
              :disabled="formType !== 'add'"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.email')" prop="email">
            <el-input
              :placeholder="$t('user.input_email')"
              v-model="form.email"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('commons.mobile_phone_number')" prop="phone">
            <el-input
              :placeholder="$t('commons.mobile_phone')"
              v-model="form.phone"
              class="input-with-select"
            >
              <el-select
                v-model="form.phonePrefix"
                slot="prepend"
                :placeholder="$t('fu.search_bar.please_select')"
              >
                <el-option label="+86" value="+86"></el-option>
              </el-select>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="24">
        <el-col :span="12">
          <el-form-item :label="$t('commons.gender')" prop="gender">
            <el-select
              class="form-gender-select"
              v-model="form.gender"
              :placeholder="$t('user.select_gender')"
            >
              <el-option :label="$t('commons.man')" value="男"> </el-option>
              <el-option :label="$t('commons.woman')" value="女"> </el-option>
              <el-option :label="$t('commons.keep_secret')" value="保密">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            v-show="isPluginLoaded"
            :label="$t('commons.organization')"
            prop="deptId"
          >
            <treeselect
              ref="deptTreeSelect"
              v-model="form.deptId"
              :options="depts"
              :load-options="loadDepts"
              :auto-load-root-options="false"
              :placeholder="$t('user.choose_org')"
              :no-children-text="$t('commons.treeselect.no_children_text')"
              :no-options-text="$t('commons.treeselect.no_options_text')"
              :no-results-text="$t('commons.treeselect.no_results_text')"
              @open="filterData"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- <el-form-item v-if="formType !== 'modify'" :label="$t('commons.password')" prop="password">
        <el-input v-model="form.password" autocomplete="off" show-password />
      </el-form-item>
      <el-form-item v-if="formType !== 'modify'" :label="$t('commons.confirmPassword')" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" autocomplete="off" show-password />
      </el-form-item> -->
      <el-form-item
        v-show="isPluginLoaded"
        :label="$t('commons.role')"
        prop="roleIds"
      >
        <el-select
          ref="roleSelect"
          v-model="form.roleIds"
          style="width: 100%"
          :disabled="formType !== 'add' && form.isAdmin"
          multiple
          :placeholder="$t('user.input_roles')"
          @remove-tag="deleteTag"
          @change="changeRole"
        >
          <el-option
            v-for="item in roles"
            :key="item.name"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="$t('commons.status')" prop="enabled">
        <el-switch
          :disabled="formType !== 'add' && form.isAdmin"
          v-model="form.enabled"
          :active-value="1"
          :inactive-value="0"
        >
        </el-switch>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button class="btn normal" @click="reset">{{
        $t("commons.cancel")
      }}</el-button>
      <el-button class="btn" type="primary" @click="save">{{
        $t("commons.confirm")
      }}</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { PHONE_REGEX } from "@/utils/validate";
import { getDeptTree, treeByDeptId } from "@/api/system/dept";
import { addUser, editUser, allRoles } from "@/api/system/user";
import { pluginLoaded, defaultPwd } from "@/api/user";
import {
  LOAD_CHILDREN_OPTIONS,
  LOAD_ROOT_OPTIONS,
} from "@riophae/vue-treeselect";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";
export default {
  components: { Treeselect },
  data() {
    return {
      form: {
        roles: [
          {
            id: "",
          },
        ],
      },
      rule: {
        username: [
          {
            required: true,
            message: this.$t("user.id_mandatory"),
            trigger: "blur",
          },
          {
            min: 1,
            max: 50,
            message: this.$t("commons.input_limit", [1, 50]),
            trigger: "blur",
          },
          {
            required: true,
            pattern: "^[^\u4e00-\u9fa5]+$",
            message: this.$t("user.special_characters_are_not_supported"),
            trigger: "blur",
          },
        ],
        nickName: [
          {
            required: true,
            message: this.$t("user.name_mandatory"),
            trigger: "blur",
          },
          {
            min: 2,
            max: 50,
            message: this.$t("commons.input_limit", [2, 50]),
            trigger: "blur",
          },
          {
            required: true,
            message: this.$t("user.special_characters_are_not_supported"),
            trigger: "blur",
          },
        ],
        phone: [
          {
            pattern: PHONE_REGEX,
            message: this.$t("user.phone_format"),
            trigger: "blur",
          },
        ],
        email: [
          {
            required: true,
            message: this.$t("user.email_mandatory"),
            trigger: "blur",
          },
          {
            required: true,
            pattern: /^[a-zA-Z0-9_._-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/,
            message: this.$t("user.email_format_is_incorrect"),
            trigger: "blur",
          },
        ],
        password: [
          {
            required: true,
            message: this.$t("user.input_password"),
            trigger: "blur",
          },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t("member.password_format_is_incorrect"),
            trigger: "blur",
          },
        ],
        confirmPassword: [
          {
            required: true,
            message: this.$t("user.input_password"),
            trigger: "blur",
          },
          { required: true, validator: this.repeatValidator, trigger: "blur" },
        ],
        newPassword: [
          {
            required: true,
            message: this.$t("user.input_password"),
            trigger: "blur",
          },
          {
            required: true,
            pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,30}$/,
            message: this.$t("member.password_format_is_incorrect"),
            trigger: "blur",
          },
        ],
        roleIds: [
          {
            required: true,
            message: this.$t("user.role_mandatory"),
            trigger: "change",
          },
        ],
        deptId: [],
        gender: [],
        enabled: [{ required: true, trigger: "change" }],
      },
      defaultForm: {
        id: null,
        username: null,
        nickName: null,
        gender: "男",
        email: null,
        enabled: 1,
        deptId: null,
        phone: null,
        phonePrefix: "+86",
        roleIds: [2],
      },
      depts: null,
      roles: [],
      roleDatas: [],
      userRoles: [],
      formType: "add",
      isPluginLoaded: false,
      defaultPWD: "DataEase123..",
      dialogVisible: false,
    };
  },
  beforeCreate() {
    pluginLoaded().then((res) => {
      this.isPluginLoaded = res.success && res.data;
    });
    defaultPwd().then((res) => {
      if (res && res.data) {
        this.defaultPWD = res.data;
      }
    });
  },
  methods: {
    repeatValidator(rule, value, callback) {
      if (value !== this.form.password) {
        callback(new Error(this.$t("member.inconsistent_passwords")));
      } else {
        callback();
      }
    },
    create() {
      this.depts = null;
      this.formType = "add";
      this.form = Object.assign({}, this.defaultForm);
    },
    init(row) {
      this.initRoles();
      this.dialogVisible = true;
      if (!row) {
        this.create();
        return;
      }
      this.depts = null;
      this.formType = "modify";
      this.dialogVisible = true;
      this.form = Object.assign({}, row);
      this.form.password = "";
      if (this.form.deptId === 0) {
        this.form.deptId = null;
      }

      if (!this.form.phonePrefix) {
        this.form.phonePrefix = '+86';
      }
      this.initDeptTree();
    },
    initRoles() {
      allRoles().then((res) => {
        this.roles = res.data;
      });
    },
    initDeptTree() {
      treeByDeptId(this.form.deptId || 0).then((res) => {
        const results = res.data.map((node) => {
          if (node.hasChildren && !node.children) {
            node.children = null;
            // delete node.children
          }
          return node;
        });
        this.depts = results;
      });
    },
    // 获取弹窗内部门数据
    loadDepts({ action, parentNode, callback }) {
      if (action === "LOAD_ROOT_OPTIONS" && !this.form.deptId) {
        const _self = this;
        treeByDeptId(0).then((res) => {
          const results = res.data.map((node) => {
            if (node.hasChildren && !node.children) {
              node.children = null;
            }
            return node;
          });
          _self.depts = results;
          callback();
        });
      }

      if (action === "LOAD_CHILDREN_OPTIONS") {
        const _self = this;
        getDeptTree(parentNode.id).then((res) => {
          parentNode.children = res.data.map(function (obj) {
            return _self.normalizer(obj);
          });
          callback();
        });
      }
    },
    normalizer(node) {
      if (node.hasChildren) {
        node.children = null;
      }
      return {
        id: node.deptId,
        label: node.name,
        children: node.children,
      };
    },
    deleteTag(value) {
      this.userRoles.forEach(
        function (data, index) {
          if (data.id === value) {
            this.userRoles.splice(index, value);
          }
        }.bind(this)
      );
    },
    changeRole(value) {
      this.userRoles = [];
      value.forEach(
        function (data, index) {
          const role = { id: data };
          this.userRoles.push(role);
        }.bind(this)
      );
    },
    reset() {
      this.$refs.createUserForm.resetFields();
      this.dialogVisible = false;
    },
    save() {
      this.$refs.createUserForm.validate((valid) => {
        if (valid) {
          // !this.form.deptId && (this.form.deptId = 0)
          const method = this.formType === "add" ? addUser : editUser;
          method(this.form).then((res) => {
            this.$success(this.$t("commons.save_success"));
            this.reset();
            this.$emit('saved')
          });
        } else {
          return false;
        }
      });
    },
    filterData(instanceId) {
      this.$refs.roleSelect &&
        this.$refs.roleSelect.blur &&
        this.$refs.roleSelect.blur();
      if (!this.depts) {
        return;
      }
      const results = this.depts.map((node) => {
        if (node.hasChildren) {
          node.children = null;
        }
        return node;
      });
      this.depts = results;
    },
    onCopy(e) {
      this.$success(this.$t("commons.copy_success"));
    },
    onError(e) {},
  },
};
</script>

<style lang="scss" scoped>
.user-editer-form {
  ::v-deep .el-dialog__body {
    padding: 0 24px 24px 24px;
  }

  ::v-deep .el-dialog__header {
    padding: 24px 24px 16px 24px;
  }

  ::v-deep .el-dialog__footer {
    padding-top: 0;
  }

  .editer-form-title {
    width: 100%;
    border-radius: 4px;
    background: #e1eaff;
    padding: 9px 16px;
    margin-bottom: 16px;

    i {
      color: #3370ff;
      font-size: 14.666666030883789px;
    }

    .pwd,
    .btn-text {
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      text-align: left;
      color: #1f2329;
    }

    .pwd {
      margin: 0 8px;
    }

    .btn-text {
      padding: 0;
      border: none;
    }
  }

  ::v-deep .el-form-item__label {
    width: 100% !important;
    text-align: left;
  }

  ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label:before {
    display: none;
  }

  ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label::after {
    content: "*";
    color: #f54a45;
    margin-left: 2px;
  }

  ::v-deep .el-form-item__content {
    margin-left: 0 !important;
  }

  .input-with-select {
    ::v-deep .el-input-group__prepend {
      background-color: #fff;
    }
    .el-select {
      ::v-deep .el-input__inner {
        width: 72px;
      }
    }
  }

  .btn {
    border-radius: 4px;
    padding: 5px 26px 5px 26px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0px;
    text-align: center;
    border: none;
    box-sizing: border-box;
  }

  .normal {
    color: #1f2329;
    border: 1px solid #bbbfc4;
    margin-left: 12px;
  }

  .form-gender-select {
    width: 100%;
  }

  .btn {
  }
}
</style>
