<template>
  <div>
    <!--邮件表单-->
    <el-form
      ref="formInline"
      v-loading="loading"
      :model="formInline"
      :rules="rules"
      class="demo-form-inline"
      :disabled="show"
      size="small"
    >
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_host')" prop="host">
            <el-input
              v-model="formInline.host"
              :placeholder="$t('system_parameter_setting.SMTP_host')"
              @input="change()"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_port')" prop="port">
            <el-input
              v-model="formInline.port"
              :placeholder="$t('system_parameter_setting.SMTP_port')"
              @input="change()"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_account')" prop="account">
            <el-input
              v-model="formInline.account"
              :placeholder="$t('system_parameter_setting.SMTP_account')"
              @input="change()"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_password')" prop="password">
            <el-input
              ref="input"
              v-model="formInline.password"
              :placeholder="$t('system_parameter_setting.SMTP_password')"
              autocomplete="new-password"
              show-password
              type="text"
              @focus="changeType"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.test_recipients')">
            <el-input
              ref="input"
              v-model="formInline.recipient"
              :placeholder="$t('system_parameter_setting.test_recipients')"
              autocomplete="new-password"
              show-password
              type="text"
            />
            <p style="color: #8a8b8d">({{ $t('system_parameter_setting.tip') }})</p>
          </el-form-item>
        </el-col>
      </el-row>

      <!---->
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.ssl" :label="$t('system_parameter_setting.SSL')" />
      </div>
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.tls" :label="$t('system_parameter_setting.TLS')" />
      </div>
      <template v-slot:footer />
    </el-form>
    <div>
      <el-button type="primary" :disabled="disabledConnection" size="small" @click="testConnection('formInline')">
        {{ $t('system_parameter_setting.test_connection') }}
      </el-button>
      <el-button v-if="showEdit" size="small" @click="edit">{{ $t('commons.edit') }}</el-button>
      <el-button v-if="showSave" type="success" :disabled="disabledSave" size="small" @click="save('formInline')">
        {{ $t('commons.save') }}
      </el-button>
      <el-button v-if="showCancel" type="info" size="small" @click="cancel">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>

import { emailInfo, updateInfo, validate } from '@/api/system/email'

export default {
  name: 'EmailSetting',
  data() {
    return {
      formInline: {},
      input: '',
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
            message: this.$t('system_parameter_setting.host'),
            trigger: ['change', 'blur']
          }
        ],
        port: [
          {
            required: true,
            message: this.$t('system_parameter_setting.port'),
            trigger: ['change', 'blur']
          }
        ],
        account: [
          {
            required: true,
            message: this.$t('system_parameter_setting.account'),
            trigger: ['change', 'blur']
          }]
      }
    }
  },

  created() {
    this.query()
  },
  methods: {
    changeType() {
      this.$refs.input = 'password'
    },
    query() {
      emailInfo().then(response => {
        this.formInline = response.data
        this.formInline.ssl = this.formInline.ssl === 'true'
        this.formInline.tls = this.formInline.tls === 'true'
        this.$nextTick(() => {
          this.$refs.formInline.clearValidate()
        })
      })
    },
    change() {
      if (!this.formInline.host || !this.formInline.port || !this.formInline.account) {
        this.disabledConnection = true
        this.disabledSave = true
      } else {
        this.disabledConnection = false
        this.disabledSave = false
      }
    },
    testConnection(formInline) {
      const param = {
        'smtp.host': this.formInline.host,
        'smtp.port': this.formInline.port,
        'smtp.account': this.formInline.account,
        'smtp.password': this.formInline.password,
        'smtp.ssl': this.formInline.ssl,
        'smtp.tls': this.formInline.tls,
        'smtp.recipient': this.formInline.recipient
      }
      this.$refs[formInline].validate((valid) => {
        if (valid) {
          validate(param).then(response => {
            this.$success(this.$t('commons.connection_successful'))
          })
        } else {
          return false
        }
      })
    },
    edit() {
      this.showEdit = false
      this.showSave = true
      this.showCancel = true
      this.show = false
    },
    save(formInline) {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      const param = [
        { paramKey: 'smtp.host', paramValue: this.formInline.host, type: 'text', sort: 1 },
        { paramKey: 'smtp.port', paramValue: this.formInline.port, type: 'text', sort: 2 },
        { paramKey: 'smtp.account', paramValue: this.formInline.account, type: 'text', sort: 3 },
        { paramKey: 'smtp.password', paramValue: this.formInline.password, type: 'password', sort: 4 },
        { paramKey: 'smtp.ssl', paramValue: this.formInline.ssl, type: 'text', sort: 5 },
        { paramKey: 'smtp.tls', paramValue: this.formInline.tls, type: 'text', sort: 6 },
        { paramKey: 'smtp.recipient', paramValue: this.formInline.recipient, type: 'text', sort: 8 }

      ]

      this.$refs[formInline].validate(valid => {
        if (valid) {
          updateInfo(param).then(response => {
            const flag = response.success
            if (flag) {
              this.$success(this.$t('commons.save_success'))
            } else {
              this.$message.error(this.$t('commons.save_failed'))
            }
          })
        } else {
          // this.result = false
        }
      })
    },
    cancel() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.query()
    }

  }
}
</script>

<style scoped>

</style>
