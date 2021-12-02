<template>
  <layout-content :header="formType=='add' ? $t('datasource.create') : $t('datasource.modify')">
    <template v-slot:header>
      <el-icon name="back" class="back-button" @click.native="backToList" />
      {{
        params && params.id && params.showModel && params.showModel === 'show' && !canEdit ? $t('datasource.show_info') : formType == 'add' ? $t('datasource.create') : $t('datasource.modify')
      }}
    </template>
    <div>

      <el-form
        ref="dsForm"
        :model="form"
        :rules="rule"
        size="small"
        :disabled="params && params.id && params.showModel && params.showModel === 'show' && !canEdit "
        label-width="180px"
        label-position="right"
      >
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="desc">
          <el-input v-model="form.desc" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type">
          <el-select
            v-model="form.type"
            :placeholder="$t('datasource.please_choose_type')"
            class="select-width"
            :disabled="formType=='modify' || (formType==='add' && params && !!params.type)"
            @change="changeType()"
          >
            <el-option
              v-for="item in allTypes"
              :key="item.name"
              :label="item.label"
              :value="item.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="form.configuration.dataSourceType=='jdbc'"
          :label="$t('datasource.host')"
          prop="configuration.host"
        >
          <el-input v-model="form.configuration.host" autocomplete="off" />
        </el-form-item>
        <el-form-item
          v-if="form.configuration.dataSourceType=='es'"
          :label="$t('datasource.datasource_url')"
          prop="configuration.url"
        >
          <el-input
            v-model="form.configuration.url"
            :placeholder="$t('datasource.please_input_datasource_url')"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          v-if="form.configuration.dataSourceType=='jdbc'"
          :label="$t('datasource.data_base')"
          prop="configuration.dataBase"
        >
          <el-input v-model="form.configuration.dataBase" autocomplete="off" />
        </el-form-item>

        <el-form-item
          v-if="form.type=='oracle'"
          :label="$t('datasource.oracle_connection_type')"
          prop="configuration.connectionType"
        >
          <el-radio v-model="form.configuration.connectionType" label="sid">{{ $t('datasource.oracle_sid') }}</el-radio>
          <el-radio v-model="form.configuration.connectionType" label="serviceName">
            {{ $t('datasource.oracle_service_name') }}
          </el-radio>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')">
          <el-input v-model="form.configuration.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')">
          <el-input v-model="form.configuration.password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='es'" :label="$t('datasource.user_name')">
          <el-input v-model="form.configuration.esUsername" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='es'" :label="$t('datasource.password')">
          <el-input v-model="form.configuration.esPassword" autocomplete="off" show-password />
        </el-form-item>

        <el-form-item
          v-if="form.configuration.dataSourceType=='jdbc' && form.type!=='oracle'"
          :label="$t('datasource.extra_params')"
        >
          <el-input v-model="form.configuration.extraParams" autocomplete="off" />
        </el-form-item>

        <el-form-item
          v-if="form.configuration.dataSourceType=='jdbc'"
          :label="$t('datasource.port')"
          prop="configuration.port"
        >
          <el-input v-model="form.configuration.port" autocomplete="off" type="number" min="0" />
        </el-form-item>
        <el-form-item v-if="form.type=='oracle' || form.type=='sqlServer' || form.type=='pg' || form.type=='redshift'">
          <el-button icon="el-icon-plus" size="mini" @click="getSchema()">
            {{ $t('datasource.get_schema') }}
          </el-button>
        </el-form-item>

        <el-form-item
          v-if="form.type=='oracle' || form.type=='sqlServer' || form.type=='pg' || form.type=='redshift'"
          :label="$t('datasource.schema')"
        >
          <el-select
            v-model="form.configuration.schema"
            filterable
            :placeholder="$t('datasource.please_choose_schema')"
            class="select-width"
          >
            <el-option
              v-for="item in schemas"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-collapse v-if="form.configuration.dataSourceType=='jdbc'">
          <el-collapse-item :title="$t('datasource.priority')" name="1">
            <el-form-item :label="$t('datasource.initial_pool_size')" prop="configuration.initialPoolSize">
              <el-input
                v-model="form.configuration.initialPoolSize"
                autocomplete="off"
                type="number"
                min="0"
                size="small"
              />
            </el-form-item>
            <el-form-item :label="$t('datasource.min_pool_size')" prop="configuration.minPoolSize">
              <el-input v-model="form.configuration.minPoolSize" autocomplete="off" type="number" min="0" />
            </el-form-item>
            <el-form-item :label="$t('datasource.max_pool_size')" prop="configuration.maxPoolSize">
              <el-input v-model="form.configuration.maxPoolSize" autocomplete="off" type="number" min="0" />
            </el-form-item>

          </el-collapse-item>
        </el-collapse>
      </el-form>
      <div v-if="canEdit" slot="footer" class="dialog-footer">
        <el-button
          v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
          @click="validaDatasource"
        >{{ $t('commons.validate') }}
        </el-button>
        <el-button
          v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
          type="primary"
          @click="save"
        >{{ $t('commons.save') }}
        </el-button>
      </div>
      <div v-else slot="footer" class="dialog-footer">
        <el-button
          v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
          @click="validaDatasource"
        >{{ $t('commons.validate') }}
        </el-button>
        <el-button
          v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
          type="primary"
          @click="changeEdit"
        >{{ $t('commons.edit') }}
        </el-button>
      </div>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addDs, editDs, getSchema, validateDs, validateDsById } from '@/api/system/datasource'
import { $confirm } from '@/utils/message'
import i18n from '@/lang/index'

export default {
  name: 'DsForm',
  components: { LayoutContent },
  props: {
    params: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      form: {
        configuration: {
          initialPoolSize: 5,
          extraParams: '',
          minPoolSize: 5,
          maxPoolSize: 50,
          maxIdleTime: 30,
          acquireIncrement: 5,
          idleConnectionTestPeriod: 5,
          connectTimeout: 5
        }
      },
      rule: {
        name: [{ required: true, message: i18n.t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur' }],
        desc: [{ min: 0, max: 50, message: i18n.t('datasource.input_limit_0_50'), trigger: 'blur' }],
        type: [{ required: true, message: i18n.t('datasource.please_choose_type'), trigger: 'change' }],
        'configuration.dataBase': [{
          required: true,
          message: i18n.t('datasource.please_input_data_base'),
          trigger: 'blur'
        }],
        'configuration.connectionType': [{
          required: true,
          message: i18n.t('datasource.please_select_oracle_type'),
          trigger: 'blur'
        }],
        'configuration.username': [{
          required: true,
          message: i18n.t('datasource.please_input_user_name'),
          trigger: 'blur'
        }],
        'configuration.password': [{
          required: true,
          message: i18n.t('datasource.please_input_password'),
          trigger: 'change'
        }],
        'configuration.host': [{ required: true, message: i18n.t('datasource.please_input_host'), trigger: 'change' }],
        'configuration.url': [{ required: true, message: i18n.t('datasource.please_input_url'), trigger: 'change' }],
        'configuration.port': [{ required: true, message: i18n.t('datasource.please_input_port'), trigger: 'change' }],
        'configuration.initialPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_initial_pool_size'),
          trigger: 'change'
        }],
        'configuration.minPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_min_pool_size'),
          trigger: 'change'
        }],
        'configuration.maxPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_max_pool_size'),
          trigger: 'change'
        }],
        'configuration.maxIdleTime': [{
          required: true,
          message: i18n.t('datasource.please_input_max_idle_time'),
          trigger: 'change'
        }],
        'configuration.acquireIncrement': [{
          required: true,
          message: i18n.t('datasource.please_input_acquire_increment'),
          trigger: 'change'
        }],
        'configuration.connectTimeout': [{
          required: true,
          message: i18n.t('datasource.please_input_connect_timeout'),
          trigger: 'change'
        }]
      },
      allTypes: [
        {
          name: 'mysql',
          label: 'MySQL',
          type: 'jdbc',
          extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
        },
        { name: 'hive', label: 'Apache Hive', type: 'jdbc', extraParams: '' },
        { name: 'oracle', label: 'Oracle', type: 'jdbc' },
        { name: 'sqlServer', label: 'SQL Server', type: 'jdbc', extraParams: '' },
        { name: 'pg', label: 'PostgreSQL', type: 'jdbc', extraParams: '' },
        { name: 'es', label: 'Elasticsearch', type: 'es' },
        {
          name: 'mariadb',
          label: 'MariaDB',
          type: 'jdbc',
          extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
        },
        {
          name: 'ds_doris',
          label: 'Doris',
          type: 'jdbc',
          extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
        },
        { name: 'ck', label: 'ClickHouse', type: 'jdbc', extraParams: '' },
        { name: 'redshift', label: 'AWS Redshift', type: 'jdbc' },
        { name: 'mongo', label: 'MongoDB', type: 'jdbc', extraParams: '' }
      ],
      schemas: [],
      canEdit: false,
      originConfiguration: {}
    }
  },

  created() {
    if (this.params && this.params.id) {
      const row = this.params
      this.edit(row)
    } else {
      this.create()
      if (this.params && this.params.type) {
        this.setType()
      }
    }
  },
  mounted() {
  },
  methods: {
    setType() {
      this.form.type = this.params.type
      this.form.configuration = {
        initialPoolSize: 5,
        extraParams: '',
        minPoolSize: 5,
        maxPoolSize: 50,
        maxIdleTime: 30,
        acquireIncrement: 5,
        idleConnectionTestPeriod: 5,
        connectTimeout: 5
      }
      this.changeType()
    },
    changeEdit() {
      this.canEdit = true
      this.formType = 'modify'
    },
    create() {
      this.formType = 'add'
      this.canEdit = true
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
      this.originConfiguration = this.form.configuration
      this.form.configuration = JSON.parse(this.form.configuration)
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      if (!this.form.configuration.schema && (this.form.type === 'oracle' || this.form.type === 'sqlServer')) {
        this.$message.error(i18n.t('datasource.please_choose_schema'))
        return
      }
      if (this.form.configuration.dataSourceType === 'jdbc' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      if (this.form.configuration.initialPoolSize < 0 || this.form.configuration.minPoolSize < 0 || this.form.configuration.maxPoolSize < 0) {
        this.$message.error(i18n.t('datasource.no_less_then_0'))
        return
      }
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDs : editDs
          const form = JSON.parse(JSON.stringify(this.form))
          form.configuration = JSON.stringify(form.configuration)
          if (this.formType !== 'add' && this.originConfiguration !== form.configuration) {
            $confirm(i18n.t('datasource.edit_datasource_msg'), () => {
              method(form).then(res => {
                this.$success(i18n.t('commons.save_success'))
                this.refreshType(form)
                this.backToList()
              })
            })
          } else {
            method(form).then(res => {
              this.$success(i18n.t('commons.save_success'))
              this.refreshType(form)
              this.backToList()
            })
          }
        } else {
          return false
        }
      })
    },
    getSchema() {
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          getSchema(data).then(res => {
            this.schemas = res.data
            this.$success(i18n.t('commons.success'))
          })
        } else {
          return false
        }
      })
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
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          if (data.showModel === 'show' && !this.canEdit) {
            validateDsById(data.id).then(res => {
              if (res.success) {
                this.$success(i18n.t('datasource.validate_success'))
              } else {
                this.$error(res.message)
              }
              this.refreshType(data)
            }).catch(res => {
              this.$error(res.message)
            })
          } else {
            validateDs(data).then(res => {
              if (res.success) {
                this.$success(i18n.t('datasource.validate_success'))
              } else {
                this.$error(res.message)
              }
            }).catch(res => {
              this.$error(res.message)
            })
          }
        } else {
          return false
        }
      })
    },
    changeType() {
      for (let i = 0; i < this.allTypes.length; i++) {
        if (this.allTypes[i].name === this.form.type) {
          this.form.configuration.dataSourceType = this.allTypes[i].type
          this.form.configuration.extraParams = this.allTypes[i].extraParams
        }
      }
    },
    backToList() {
      this.$emit('switch-component', {})
    },
    refreshType(form) {
      this.$emit('refresh-type', form)
    }
  }
}
</script>
<style lang="scss" scoped>

.back-button {
  cursor: pointer;
  margin-right: 10px;
  font-weight: 600;

  &:active {
    transform: scale(0.85);
  }
}

.el-input {
  width: 300px;
}

.el-select {
  width: 300px;
}
</style>
