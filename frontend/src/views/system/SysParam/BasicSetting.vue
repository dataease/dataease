<template>
  <div>
    <operater title="system_parameter_setting.basic_setting">
      <deBtn type="primary" v-if="showEdit" @click="edit">{{
          $t("commons.edit")
      }}</deBtn>
      <deBtn v-if="showCancel" secondary @click="cancel">{{
          $t("commons.cancel")
      }}</deBtn>
      <deBtn v-if="showSave" type="primary" :disabled="disabledSave" size="small" @click="save('formInline')">
        {{ $t("commons.save") }}
      </deBtn>
    </operater>

    <!--基础配置表单-->
    <el-form ref="formInline" v-loading="loading" :model="formInline" :rules="rules"
      class="demo-form-inline de-form-item" :disabled="show" label-width="80px" label-position="right" size="small">
      <el-form-item prop="frontTimeOut">
        <template slot="label">
          {{ $t('system_parameter_setting.request_timeout')}}
          <el-tooltip class="item" effect="dark" :content="$t('system_parameter_setting.front_time_out')"
            placement="top">
            <i class="el-icon-warning-outline tips"></i>
          </el-tooltip>
        </template>
        <el-input v-model="formInline.frontTimeOut" :placeholder="$t('system_parameter_setting.empty_front')"><template
            slot="append">{{ $t("panel.second") }}</template></el-input>
      </el-form-item>
      <el-form-item :label="$t('system_parameter_setting.message_retention_time')" prop="msgTimeOut">
        <el-input v-model="formInline.msgTimeOut" :placeholder="$t('system_parameter_setting.empty_msg')"><template
            slot="append">{{ $t('components.day') }}</template></el-input>
      </el-form-item>

      <el-form-item v-if="loginTypes.length > 1" :label="$t('system_parameter_setting.login_type')" prop="loginType">
        <el-radio-group v-model="formInline.loginType">
          <el-radio :label="0" size="mini">{{
              $t("login.default_login")
          }}</el-radio>
          <el-radio v-if="loginTypes.includes(1)" :label="1" size="mini">LDAP</el-radio>
          <el-radio v-if="loginTypes.includes(2)" :label="2" size="mini">OIDC</el-radio>
          <el-radio v-if="loginTypes.includes(3)" :label="3" size="mini">CAS</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- <el-row v-if="loginTypes.includes(3)">
        <el-button class="pwd-tips" type="text">{{ $t('system_parameter_setting.cas_reset') + '[/cas/reset/{adminAcount}/{adminPwd}]' }}</el-button>
      </el-row> -->

      <el-form-item :label="
        $t('commons.yes') + $t('commons.no') + $t('display.openMarketPage')
      ">
        <el-radio-group v-model="formInline.openMarketPage">
          <el-radio label="true" size="mini">{{ $t("commons.yes") }}</el-radio>
          <el-radio label="false" size="mini">{{ $t("commons.no") }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="
        $t('commons.yes') + $t('commons.no') + $t('display.openHomePage')
      " prop="openHomePage">
        <el-radio-group v-model="formInline.openHomePage">
          <el-radio label="true" size="mini">{{ $t("commons.yes") }}</el-radio>
          <el-radio label="false" size="mini">{{ $t("commons.no") }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { basicInfo, updateInfo } from "@/api/system/basic";
import { ldapStatus, oidcStatus, casStatus } from "@/api/user";
import bus from "@/utils/bus";
import operater from "./operater";
import msgCfm from '@/components/msgCfm'
export default {
  name: "EmailSetting",
  mixins: [msgCfm],
  components: {
    operater,
  },
  data() {
    return {
      formInline: {},
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledSave: false,
      loading: false,
      loginTypes: [0],
      rules: {
        frontTimeOut: [
          {
            pattern: "^([0-9]|\\b[1-9]\\d\\b|\\b[1-2]\\d\\d\\b|\\b300\\b)$", // 修改了正则表达式，让其正确匹配0-300的数值
            message: this.$t("system_parameter_setting.front_error"),
            trigger: "blur",
          },
        ],
        msgTimeOut: [
          {
            pattern: "^([1-9]|[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])$",
            message: this.$t("system_parameter_setting.msg_error"),
            trigger: "blur",
          },
        ],
      },
      originLoginType: null,
    };
  },
  computed: {},
  beforeCreate() {
    ldapStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(1);
      }
    });

    oidcStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(2);
      }
    });

    casStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(3);
      }
    });
  },
  created() {
    this.query();
  },
  methods: {
    query() {
      basicInfo().then((response) => {
        this.formInline = response.data;

        if (this.formInline && !this.formInline.loginType) {
          this.formInline.loginType = 0;
        }
        if (!this.originLoginType) {
          this.originLoginType = this.formInline.loginType;
        }

        this.$nextTick(() => {
          this.$refs.formInline.clearValidate();
        });
      });
    },

    edit() {
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
      this.show = false;
    },
    save(formInline) {
      const param = [
        {
          paramKey: "basic.frontTimeOut",
          paramValue: this.formInline.frontTimeOut,
          type: "text",
          sort: 1,
        },
        {
          paramKey: "basic.msgTimeOut",
          paramValue: this.formInline.msgTimeOut,
          type: "text",
          sort: 2,
        },
        {
          paramKey: "basic.loginType",
          paramValue: this.formInline.loginType,
          type: "text",
          sort: 3,
        },
        {
          paramKey: "ui.openHomePage",
          paramValue: this.formInline.openHomePage,
          type: "text",
          sort: 13,
        },
        {
          paramKey: "ui.openMarketPage",
          paramValue: this.formInline.openMarketPage,
          type: "text",
          sort: 14,
        },
      ];

      this.$refs[formInline].validate((valid) => {
        if (valid) {
          const needWarn =
            this.formInline.loginType === 3 && this.originLoginType !== 3;
          if (needWarn) {
            this.$confirm(
              this.$t("system_parameter_setting.cas_selected_warn"),
              "",
              {
                confirmButtonText: this.$t("commons.confirm"),
                cancelButtonText: this.$t("commons.cancel"),
                type: "warning",
              }
            )
              .then(() => {
                this.saveHandler(param);
              })
              .catch(() => {
                // this.$info(this.$t('commons.delete_cancel'))
              });
            return;
          }
          this.saveHandler(param);
        } else {
          // this.result = false
        }
      });
    },
    saveHandler(param) {
      updateInfo(param).then((response) => {
        const flag = response.success;
        if (flag) {
          if (response.data && response.data.needLogout) {
            const casEnable = response.data.casEnable;
            bus.$emit("sys-logout", { casEnable });
            return;
          }
          this.openMessageSuccess("commons.save_success");
          this.showEdit = true;
          this.showCancel = false;
          this.showSave = false;
          this.show = true;
          window.location.reload();
        } else {
          this.openMessageSuccess("commons.save_failed", 'error');
        } 
      });
    },
    cancel() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      this.query();
    },
  },
};
</script>

<style lang="scss" scoped>
.demo-form-inline {
  .tips {
    margin-left: 2px;
    position: relative;
    z-index: 10;
  }

  .el-radio:not(:last-child) {
      margin-right: 0;
      width: 156px;
    }
}
</style>
<style lang="scss">
.de-i118 {
  .el-form-item__label::after {
    display: none;
  }
  .is-require::after {
    content: "*";
    color: #f54a45;
    margin-left: 2px;
  }
}
</style>