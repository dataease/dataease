<template>
  <div>
    <operater title="system_parameter_setting.basic_setting">
      <deBtn v-if="showEdit" type="primary" @click="edit">{{
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
    <el-form
      ref="formInline"
      v-loading="loading"
      :model="formInline"
      :rules="rules"
      class="demo-form-inline de-form-item"
      :disabled="show"
      label-width="80px"
      label-position="right"
      size="small"
    >
      <el-form-item prop="frontTimeOut">
        <template slot="label">
          {{ $t('system_parameter_setting.request_timeout') }}
          <el-tooltip
            class="item"
            effect="dark"
            :content="$t('system_parameter_setting.front_time_out')"
            placement="top"
          >
            <i class="el-icon-warning-outline tips" />
          </el-tooltip>
        </template>
        <el-input v-model="formInline.frontTimeOut" :placeholder="$t('system_parameter_setting.empty_front')"><template
          slot="append"
        >{{ $t("panel.second") }}</template></el-input>
      </el-form-item>
      <el-form-item :label="$t('system_parameter_setting.message_retention_time')" prop="msgTimeOut">
        <el-input v-model="formInline.msgTimeOut" :placeholder="$t('system_parameter_setting.empty_msg')"><template
          slot="append"
        >{{ $t('components.day') }}</template></el-input>
      </el-form-item>

      <el-form-item :label="$t('system_parameter_setting.ds_check_time')">
        <el-form :inline="true" :disabled="show" class="demo-form-inline-ds">

          <el-form-item>
            <el-input v-model="formInline.dsCheckInterval" size="mini" type="number" min="1" @change="onSimpleCronChange()" />
          </el-form-item>

          <el-form-item class="form-item">
            <el-select v-model="formInline.dsCheckIntervalType" filterable size="mini" @change="onSimpleCronChange()">
              <el-option :label="$t('cron.minute_default')" value="minute" />
              <el-option :label="$t('cron.hour_default')" value="hour" />
            </el-select>
          </el-form-item>
          <el-form-item class="form-item" :label="$t('cron.every_exec')" />
        </el-form>
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

      <plugin-com v-if="isPluginLoaded" ref="LoginLimitSetting" :form="formInline" component-name="LoginLimitSetting" />

      <el-form-item
        :label="
          $t('commons.yes') + $t('commons.no') + $t('display.openMarketPage')
        "
      >
        <el-radio-group v-model="formInline.openMarketPage">
          <el-radio label="true" size="mini">{{ $t("commons.yes") }}</el-radio>
          <el-radio label="false" size="mini">{{ $t("commons.no") }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item
        :label="
          $t('commons.yes') + $t('commons.no') + $t('display.openHomePage')
        "
        prop="openHomePage"
      >
        <el-radio-group v-model="formInline.openHomePage">
          <el-radio label="true" size="mini">{{ $t("commons.yes") }}</el-radio>
          <el-radio label="false" size="mini">{{ $t("commons.no") }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { basicInfo, updateInfo } from '@/api/system/basic'
import { ldapStatus, oidcStatus, casStatus } from '@/api/user'
import bus from '@/utils/bus'
import operater from './operater'
import msgCfm from '@/components/msgCfm'
import PluginCom from '@/views/system/plugin/PluginCom'
export default {
  name: 'EmailSetting',
  components: {
    operater,
    PluginCom
  },
  mixins: [msgCfm],
  props: {
    isPluginLoaded: {
      type: Boolean,
      default: false
    }
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
            pattern: '^([0-9]|\\b[1-9]\\d\\b|\\b[1-2]\\d\\d\\b|\\b300\\b)$',
            message: this.$t('system_parameter_setting.front_error'),
            trigger: 'blur'
          }
        ],
        msgTimeOut: [
          {
            pattern: '^([1-9]|[1-9][0-9]|[1-2][0-9][0-9]|3[0-5][0-9]|36[0-5])$',
            message: this.$t('system_parameter_setting.msg_error'),
            trigger: 'blur'
          }
        ],
        limitTimes: [

          { validator: this.validateNumber, trigger: 'blur' }
        ],
        relieveTimes: [
          { validator: this.validateNumber, trigger: 'blur' }
        ]
      },
      originLoginType: null
    }
  },
  computed: {},
  beforeCreate() {
    ldapStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(1)
      }
    })

    oidcStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(2)
      }
    })

    casStatus().then((res) => {
      if (res.success && res.data) {
        this.loginTypes.push(3)
      }
    })
  },
  created() {
    this.query()
  },
  methods: {
    validateNumber(rule, value, callback) {
      if (value != null && value !== '') {
        const reg = new RegExp('^([1-9]|[1-9]\\d|100)$')
        if (!reg.test(value)) {
          const msg = this.$t('system_parameter_setting.relieve_times_error')
          callback(new Error(msg))
          return
        }
      }
      callback()
    },
    query() {
      basicInfo().then((response) => {
        this.formInline = response.data

        if (this.formInline && !this.formInline.loginType) {
          this.formInline.loginType = 0
        }
        if (!this.originLoginType) {
          this.originLoginType = this.formInline.loginType
        }
        this.formInline.open = (this.formInline.open && this.formInline.open === 'true')

        this.$nextTick(() => {
          this.$refs.formInline.clearValidate()
        })
      })
    },

    edit() {
      this.showEdit = false
      this.showSave = true
      this.showCancel = true
      this.show = false
    },
    save(formInline) {
      const param = [
        {
          paramKey: 'basic.frontTimeOut',
          paramValue: this.formInline.frontTimeOut,
          type: 'text',
          sort: 1
        },
        {
          paramKey: 'basic.msgTimeOut',
          paramValue: this.formInline.msgTimeOut,
          type: 'text',
          sort: 2
        },
        {
          paramKey: 'basic.loginType',
          paramValue: this.formInline.loginType,
          type: 'text',
          sort: 3
        },
        {
          paramKey: 'basic.dsCheckInterval',
          paramValue: this.formInline.dsCheckInterval,
          type: 'text',
          sort: 4
        },
        {
          paramKey: 'basic.dsCheckIntervalType',
          paramValue: this.formInline.dsCheckIntervalType,
          type: 'text',
          sort: 5
        },
        {
          paramKey: 'ui.openHomePage',
          paramValue: this.formInline.openHomePage,
          type: 'text',
          sort: 13
        },
        {
          paramKey: 'ui.openMarketPage',
          paramValue: this.formInline.openMarketPage,
          type: 'text',
          sort: 14
        },

        {
          paramKey: 'loginlimit.limitTimes',
          paramValue: this.formInline.limitTimes,
          type: 'text',
          sort: 1
        },
        {
          paramKey: 'loginlimit.relieveTimes',
          paramValue: this.formInline.relieveTimes,
          type: 'text',
          sort: 2
        },
        {
          paramKey: 'loginlimit.open',
          paramValue: this.formInline.open,
          type: 'text',
          sort: 3
        }
      ]

      this.$refs[formInline].validate((valid) => {
        if (valid) {
          const needWarn =
            this.formInline.loginType === 3 && this.originLoginType !== 3
          if (needWarn) {
            this.$confirm(
              this.$t('system_parameter_setting.cas_selected_warn'),
              '',
              {
                confirmButtonText: this.$t('commons.confirm'),
                cancelButtonText: this.$t('commons.cancel'),
                type: 'warning'
              }
            )
              .then(() => {
                this.saveHandler(param)
              })
              .catch(() => {
                // this.$info(this.$t('commons.delete_cancel'))
              })
            return
          }
          this.saveHandler(param)
        } else {
          // this.result = false
        }
      })
    },
    saveHandler(param) {
      updateInfo(param).then((response) => {
        const flag = response.success
        if (flag) {
          if (response.data && response.data.needLogout) {
            const casEnable = response.data.casEnable
            bus.$emit('sys-logout', { casEnable })
            return
          }
          this.openMessageSuccess('commons.save_success')
          this.showEdit = true
          this.showCancel = false
          this.showSave = false
          this.show = true
          window.location.reload()
        } else {
          this.openMessageSuccess('commons.save_failed', 'error')
        }
      })
    },
    cancel() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.query()
    },
    onSimpleCronChange() {
      if (this.formInline.dsCheckIntervalType === 'minute') {
        const pattern = '^([1-9]|[1-5][0-9])$'
        if (!new RegExp(pattern).test(this.formInline.dsCheckInterval)) {
          this.$message({ message: this.$t('cron.minute_limit'), type: 'warning', showClose: true })
          this.formInline.dsCheckInterval = 1
        }
        return
      }
      if (this.formInline.dsCheckIntervalType === 'hour') {
        const pattern = '^([1-9]|[1-2][0-3])$'
        if (!new RegExp(pattern).test(this.formInline.dsCheckInterval)) {
          this.$message({ message: this.$t('cron.hour_limit'), type: 'warning', showClose: true })
          this.formInline.dsCheckInterval = 1
        }
        return
      }
    }
  }
}
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

.demo-form-inline-ds {
  .el-form-item {
    margin-bottom: 0px !important;
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
