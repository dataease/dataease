<template>
  <div>
    <operater title="system_parameter_setting.mailbox_service_settings">
      <deBtn v-if="showCancel" secondary  @click="cancel">{{
        $t("commons.cancel")
      }}</deBtn>
      <deBtn
        secondary
        :disabled="disabledConnection"
        @click="testConnection('formInline')"
      >
        {{ $t("system_parameter_setting.test_connection") }}
      </deBtn>
      <deBtn type="primary" v-if="showEdit"  @click="edit">{{
        $t("commons.edit")
      }}</deBtn>
      <deBtn
        v-if="showSave"
        type="primary"
        :disabled="disabledSave"
        
        @click="save('formInline')"
      >
        {{ $t("commons.save") }}
      </deBtn>
    </operater>
    <!--邮件表单-->
    <el-form
      ref="formInline"
      v-loading="loading"
      :model="formInline"
      :rules="rules"
      class="demo-form-inline de-form-item"
      :disabled="show"
      size="small"
    >
      <el-form-item
        :label="$t('system_parameter_setting.SMTP_host')"
        prop="host"
      >
        <el-input
          v-model="formInline.host"
          :placeholder="$t('system_parameter_setting.SMTP_host')"
          @input="change()"
        />
      </el-form-item>
      <el-form-item
        :label="$t('system_parameter_setting.SMTP_port')"
        prop="port"
      >
        <el-input
          v-model="formInline.port"
          :placeholder="$t('system_parameter_setting.SMTP_port')"
          @input="change()"
        />
      </el-form-item>
      <el-form-item
        :label="$t('system_parameter_setting.SMTP_account')"
        prop="account"
      >
        <el-input
          v-model="formInline.account"
          :placeholder="$t('system_parameter_setting.SMTP_account')"
          @input="change()"
        />
      </el-form-item>
      <el-form-item
        :label="$t('system_parameter_setting.SMTP_password')"
        prop="password"
      >
        <dePwd
          v-model="formInline.password"
          :placeholder="$t('system_parameter_setting.SMTP_password')"
        />
      </el-form-item>
      <el-form-item :label="$t('system_parameter_setting.test_recipients')">
        <template slot="label">
          {{ $t("system_parameter_setting.test_recipients") }}
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('system_parameter_setting.test_mail_recipient')"
            placement="top"
          >
            <i class="el-icon-warning-outline tips-not-absolute"></i>
          </el-tooltip>
        </template>
        <dePwd
          v-model="formInline.recipient"
          :placeholder="$t('system_parameter_setting.test_recipients')"
        />
      </el-form-item>
      <el-form-item label="邮箱服务器配置">
        <el-checkbox v-model="formInline.ssl"
          >{{ $t('chart.open') }}SSL
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('system_parameter_setting.to_enable_ssl')"
            placement="top"
          >
            <i class="el-icon-warning-outline tips-not-absolute"></i>
          </el-tooltip>
        </el-checkbox>

        <el-checkbox v-model="formInline.tls">
          {{ $t('chart.open') }}TSL
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('system_parameter_setting.to_enable_tsl')"
            placement="top"
          >
            <i class="el-icon-warning-outline tips-not-absolute"></i>
          </el-tooltip>
        </el-checkbox>
      </el-form-item>
      <!---->
      <template v-slot:footer />
    </el-form>
  </div>
</template>

<script>
import { emailInfo, updateInfo, validate } from "@/api/system/email";
import operater from "./operater";
import msgCfm from '@/components/msgCfm'
import dePwd from '@/components/deCustomCm/dePwd.vue'
export default {
  name: "EmailSetting",
  mixins: [msgCfm],
  components: {
    operater,
    dePwd
  },
  data() {
    return {
      formInline: {},
      input: "",
      visible: true,
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledConnection: false,
      disabledSave: false,
      loading: false,
      rules: {
        host: [
          {
            required: true,
            message: this.$t("system_parameter_setting.host"),
            trigger: ["change", "blur"],
          },
        ],
        port: [
          {
            required: true,
            message: this.$t("system_parameter_setting.port"),
            trigger: ["change", "blur"],
          },
        ],
        account: [
          {
            required: true,
            message: this.$t("system_parameter_setting.account"),
            trigger: ["change", "blur"],
          },
        ],
      },
    };
  },

  created() {
    this.query();
  },
  methods: {
    query() {
      emailInfo().then((response) => {
        this.formInline = response.data;
        this.formInline.ssl = this.formInline.ssl === "true";
        this.formInline.tls = this.formInline.tls === "true";
        this.$nextTick(() => {
          this.$refs.formInline.clearValidate();
        });
      });
    },
    change() {
      if (
        !this.formInline.host ||
        !this.formInline.port ||
        !this.formInline.account
      ) {
        this.disabledConnection = true;
        this.disabledSave = true;
      } else {
        this.disabledConnection = false;
        this.disabledSave = false;
      }
    },
    testConnection(formInline) {
      const param = {
        "smtp.host": this.formInline.host,
        "smtp.port": this.formInline.port,
        "smtp.account": this.formInline.account,
        "smtp.password": this.formInline.password,
        "smtp.ssl": this.formInline.ssl,
        "smtp.tls": this.formInline.tls,
        "smtp.recipient": this.formInline.recipient,
      };
      this.$refs[formInline].validate((valid) => {
        if (valid) {
          validate(param).then((response) => {
          this.openMessageSuccess("commons.connection_successful");
          });
        } else {
          return false;
        }
      });
    },
    edit() {
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
      this.show = false;
    },
    save(formInline) {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      const param = [
        {
          paramKey: "smtp.host",
          paramValue: this.formInline.host,
          type: "text",
          sort: 1,
        },
        {
          paramKey: "smtp.port",
          paramValue: this.formInline.port,
          type: "text",
          sort: 2,
        },
        {
          paramKey: "smtp.account",
          paramValue: this.formInline.account,
          type: "text",
          sort: 3,
        },
        {
          paramKey: "smtp.password",
          paramValue: this.formInline.password,
          type: "password",
          sort: 4,
        },
        {
          paramKey: "smtp.ssl",
          paramValue: this.formInline.ssl,
          type: "text",
          sort: 5,
        },
        {
          paramKey: "smtp.tls",
          paramValue: this.formInline.tls,
          type: "text",
          sort: 6,
        },
        {
          paramKey: "smtp.recipient",
          paramValue: this.formInline.recipient,
          type: "text",
          sort: 8,
        },
      ];

      this.$refs[formInline].validate((valid) => {
        if (valid) {
          updateInfo(param).then((response) => {
            const flag = response.success;
            if (flag) {
              this.openMessageSuccess("commons.save_success");
            } else {
              this.$message.error(this.$t("commons.save_failed"));
            }
          });
        } else {
          // this.result = false
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

<style scoped>
</style>
