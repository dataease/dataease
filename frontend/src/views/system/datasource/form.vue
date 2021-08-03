<template>
  <layout-content :header="formType=='add' ? $t('datasource.create') : $t('datasource.modify')">
    <template v-slot:header>
      <el-icon name="back" class="back-button" @click.native="backToList" />
      {{ params && params.id && params.showModel && params.showModel === 'show' && !canEdit ? $t('datasource.show_info') : formType=='add' ? $t('datasource.create') : $t('datasource.modify') }}
    </template>
    <div>

      <el-form ref="dsForm" :model="form" :rules="rule" size="small" :disabled="params && params.id && params.showModel && params.showModel === 'show' && !canEdit " label-width="auto" label-position="right">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="desc">

          <el-input v-model="form.desc" autocomplete="off" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type">
          <el-select v-model="form.type" :placeholder="$t('datasource.please_choose_type')" class="select-width" :disabled="formType=='modify' || (formType==='add' && params && !!params.type)" @change="changeType()">
            <el-option
              v-for="item in allTypes"
              :key="item.name"
              :label="item.label"
              :value="item.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host">
          <el-input v-model="form.configuration.host" autocomplete="off"  />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase">
          <el-input v-model="form.configuration.dataBase" autocomplete="off"  />
        </el-form-item>

        <el-form-item v-if="form.type=='oracle'" :label="$t('datasource.oracle_connection_type')" prop="configuration.connectionType">
          <el-radio v-model="form.configuration.connectionType" label="sid">{{ $t('datasource.oracle_sid') }}</el-radio>
          <el-radio v-model="form.configuration.connectionType" label="serviceName">{{ $t('datasource.oracle_service_name') }}</el-radio>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
          <el-input v-model="form.configuration.username" autocomplete="off"  />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password">
          <el-input v-model="form.configuration.password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port">
          <el-input v-model="form.configuration.port" autocomplete="off" />
        </el-form-item>
        <el-form-item v-if="form.type=='oracle'">
          <el-button icon="el-icon-plus" size="mini" @click="getSchema()">
            {{ $t('datasource.get_schema') }}
          </el-button>
        </el-form-item>

        <el-form-item v-if="form.type=='oracle'" :label="$t('datasource.schema')">
          <el-select filterable v-model="form.configuration.schema" :placeholder="$t('datasource.please_choose_schema')" class="select-width">
            <el-option
              v-for="item in schemas"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <div v-if="canEdit" slot="footer" class="dialog-footer">
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" type="primary" @click="save">{{ $t('commons.save') }}</el-button>
      </div>
      <div v-else slot="footer" class="dialog-footer">
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" type="primary" @click="changeEdit">{{ $t('commons.edit') }}</el-button>
      </div>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addDs, editDs, getSchema, validateDs } from '@/api/system/datasource'
import { $confirm } from '@/utils/message'

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
      form: { configuration: {}},
      rule: {
        name: [{ required: true, message: this.$t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur' }],
        desc: [{ min: 0, max: 50, message: this.$t('datasource.input_limit_0_50'), trigger: 'blur' }],
        type: [{ required: true, message: this.$t('datasource.please_choose_type'), trigger: 'change' }],
        'configuration.dataBase': [{ required: true, message: this.$t('datasource.please_input_data_base'), trigger: 'blur' }],
        'configuration.connectionType': [{ required: true, message: this.$t('datasource.please_select_oracle_type'), trigger: 'blur' }],
        'configuration.username': [{ required: true, message: this.$t('datasource.please_input_user_name'), trigger: 'blur' }],
        'configuration.password': [{ required: true, message: this.$t('datasource.please_input_password'), trigger: 'change' }],
        'configuration.host': [{ required: true, message: this.$t('datasource.please_input_host'), trigger: 'change' }],
        'configuration.port': [{ required: true, message: this.$t('datasource.please_input_port'), trigger: 'change' }]
      },
      allTypes: [{ name: 'mysql', label: 'MySQL', type: 'jdbc' }, { name: 'oracle', label: 'Oracle', type: 'jdbc' }],
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
      this.form.configuration = {}
      this.changeType()
      console.log(this.form)
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
      if (!this.form.configuration.schema && this.form.type === 'oracle') {
        this.$message.error(this.$t('datasource.please_choose_schema'))
        return
      }
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDs : editDs
          const form = JSON.parse(JSON.stringify(this.form))
          form.configuration = JSON.stringify(form.configuration)
          if(this.formType !== 'add' && this.originConfiguration !== form.configuration) {
            $confirm(this.$t('datasource.edit_datasource_msg'), () => {
              method(form).then(res => {
                this.$success(this.$t('commons.save_success'))
                this.refreshTree()
                this.backToList()
              })
            })
          }else {
            method(form).then(res => {
              this.$success(this.$t('commons.save_success'))
              this.refreshTree()
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
            this.$success(this.$t('commons.success'))
          })
        } else {
          return false
        }
      })
    },
    validaDatasource() {
      if (!this.form.configuration.schema && this.form.type === 'oracle') {
        this.$message.error(this.$t('datasource.please_choose_schema'))
        return
      }
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          validateDs(data).then(res => {
            this.$success(this.$t('datasource.validate_success'))
          })
        } else {
          return false
        }
      })
    },
    changeType() {
      for (let i = 0; i < this.allTypes.length; i++) {
        if (this.allTypes[i].name === this.form.type) {
          this.form.configuration.dataSourceType = this.allTypes[i].type
        }
      }
    },
    backToList() {
      this.$emit('switch-component', { })
      // this.$router.push({ name: 'datasource' })
    },
    refreshTree() {
      this.$emit('refresh-left-tree')
    }
  }
}
</script>
<style lang="scss" scoped>
@import "~@/styles/mixin.scss";
@import "~@/styles/variables.scss";

.back-button {
  cursor: pointer;
  margin-right: 10px;
  font-weight: 600;

  &:active {
    transform: scale(0.85);
  }
}
</style>
