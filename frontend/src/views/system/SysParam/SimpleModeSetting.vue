<template>
  <div>
    <el-form ref="form" v-loading="loading"
             :model="form"
             :rules="rules"
             class="demo-form-inline"
             :disabled="show"
             label-width="180px"
             label-position="top"
             size="small"
    >
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.type')" prop="type">
            <el-select
              v-model="form.type"
              :placeholder="$t('datasource.please_choose_type')"

              @change="changeType()"
              filterable
            >
              <el-option
                v-for="item in allTypes"
                :key="item.name"
                :label="item.label"
                :value="item.name"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.host')" prop="configuration.host">
            <el-input v-model="form.configuration.host"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.data_base')" prop="configuration.dataBase">
            <el-input v-model="form.configuration.dataBase"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.user_name')">
            <el-input v-model="form.configuration.username"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.password')">
            <el-input v-model="form.configuration.password" show-password/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.port')" prop="configuration.port">
            <el-input v-model="form.configuration.port" autocomplete="off" type="number" min="0"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('datasource.extra_params')">
            <el-input v-model="form.configuration.extraParams"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div>
      <el-button type="primary" size="small" @click="validaDatasource">
        {{ $t('commons.validate') }}
      </el-button>
      <el-button v-if="showEdit" size="small" @click="edit">
        {{ $t('commons.edit') }}
      </el-button>
      <el-button v-if="showSave" type="success" size="small" @click="save">
        {{ $t('commons.save') }}
      </el-button>
      <el-button v-if="showCancel" type="info" size="small" @click="cancel">
        {{ $t('commons.cancel') }}
      </el-button>
    </div>
  </div>
</template>

<script>

import {engineInfo, validate, save} from '@/api/system/engine'
import i18n from "@/lang";
import msgCfm from '@/components/msgCfm'

export default {
  name: 'SimpleMode',
  mixins: [msgCfm],
  data() {
    return {
      form:
        {
          type: 'engine_mysql',
          configuration: {
            host: '',
            dataBase: '',
            username: '',
            password: '',
            port: '',
            extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull'
          }
        },
      originConfiguration: {
        host: '',
        dataBase: '',
        username: '',
        password: '',
        port: '',
        extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull'
      },
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
        'configuration.host': [
          {
            required: true,
            message: this.$t('datasource.please_input_host'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.port': [
          {
            required: true,
            message: this.$t('datasource.please_input_port'),
            trigger: ['change', 'blur']
          }
        ],
        'configuration.dataBase': [
          {
            required: true,
            message: this.$t('datasource.please_input_data_base'),
            trigger: ['change', 'blur']
          }
        ]
      },
      allTypes: [
        {
          name: 'engine_mysql',
          label: 'MySQL',
          type: 'jdbc',
          extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
        }
      ]
    }
  },

  created() {
    this.query()
  },
  methods: {
    query() {
      engineInfo().then(response => {
        if (response.data.id) {
          this.form = JSON.parse(JSON.stringify(response.data))
          this.form.configuration = JSON.parse(this.form.configuration)
          this.originConfiguration = JSON.parse(JSON.stringify(this.form.configuration))
        }
        this.$nextTick(() => {
          this.$refs.form.clearValidate()
        })
      })
    },
    edit() {
      this.showEdit = false
      this.showSave = true
      this.showCancel = true
      this.show = false
    },
    save() {
      if (this.form.configuration.dataSourceType === 'jdbc' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      if (this.form.configuration.initialPoolSize < 0 || this.form.configuration.minPoolSize < 0 || this.form.configuration.maxPoolSize < 0) {
        this.$message.error(i18n.t('datasource.no_less_then_0'))
        return
      }
      this.$refs.form.validate(valid => {
        if (!valid) {
          return false
        }
        const form = JSON.parse(JSON.stringify(this.form))
        form.configuration = JSON.stringify(form.configuration)
        save(form).then(res => {
          this.showEdit = true
          this.showCancel = false
          this.showSave = false
          this.show = true
          this.originConfiguration = JSON.parse(JSON.stringify(this.form.configuration))
          this.openMessageSuccess("commons.save_success");
        })
      })
    },
    cancel() {
      this.showEdit = true
      this.showCancel = false
      this.showSave = false
      this.show = true
      this.form.configuration = JSON.parse(JSON.stringify(this.originConfiguration))
    },
    changeType() {
      for (let i = 0; i < this.allTypes.length; i++) {
        if (this.allTypes[i].name === this.form.type) {
          this.form.configuration.dataSourceType = this.allTypes[i].type
          this.form.configuration.extraParams = this.allTypes[i].extraParams
        }
      }
    },
    validaDatasource() {
      if (!this.form.configuration.schema && this.form.type === 'oracle') {
        this.$message.error(i18n.t('datasource.please_choose_schema'))
        return
      }
      if (this.form.configuration.dataSourceType === 'jdbc' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      this.$refs.form.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          validate(data).then(res => {
            if (res.success) {
              this.openMessageSuccess("datasource.validate_success");
            } else {
              if (res.message.length < 2500) {
                this.$error(res.message)
              } else {
                this.$error(res.message.substring(0, 2500) + '......')
              }
            }
          })
        } else {
          return false
        }
      })
    },
  }
}
</script>

<style scoped>

</style>
