<template>
  <layout-content :header="formType=='add' ? $t('datasource.create') : $t('datasource.modify')" back-name="数据源1">
    <el-form ref="dsForm" :model="form" :rules="rule" size="small" label-width="auto" label-position="right">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="form.name" autocomplete="off" />
      </el-form-item>
      <el-form-item :label="$t('commons.description')" prop="desc">

        <el-input v-model="form.desc" autocomplete="off" type="textarea" />
      </el-form-item>
      <el-form-item :label="$t('datasource.type')" prop="type">
        <el-select v-model="form.type" :placeholder="$t('datasource.please_choose_type')" class="select-width" @change="changeType()">
          <el-option
            v-for="item in allTypes"
            :key="item.name"
            :label="item.name"
            :value="item.name"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase">
        <el-input v-model="form.configuration.dataBase" autocomplete="off" />
      </el-form-item>
      <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
        <el-input v-model="form.configuration.username" autocomplete="off" />
      </el-form-item>
      <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password">
        <el-input v-model="form.configuration.password" autocomplete="off" />
      </el-form-item>
      <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host">
        <el-input v-model="form.configuration.host" autocomplete="off" />
      </el-form-item>
      <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port">
        <el-input v-model="form.configuration.port" autocomplete="off" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="save">保存</el-button>
        <el-button type="primary" @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addDs, editDs, validateDs } from '@/api/system/datasource'
export default {

  components: { LayoutContent },
  data() {
    return {
      form: { configuration: {}},
      rule: {
        name: [{ required: true, message: this.$t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur' }],
        desc: [{ required: true, message: this.$t('datasource.input_desc'), trigger: 'blur' },
          { min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur' }],
        type: [{ required: true, message: this.$t('datasource.please_choose_type'), trigger: 'change' }],

        'configuration.dataBase': [{ required: true, message: this.$t('datasource.please_input_data_base'), trigger: 'blur' }],
        'configuration.username': [{ required: true, message: this.$t('datasource.please_input_user_name'), trigger: 'blur' }],
        'configuration.password': [{ required: true, message: this.$t('datasource.please_input_password'), trigger: 'change' }],
        'configuration.host': [{ required: true, message: this.$t('datasource.please_input_host'), trigger: 'change' }],
        'configuration.port': [{ required: true, message: this.$t('datasource.please_input_port'), trigger: 'change' }]
      },
      allTypes: [{ name: 'mysql', type: 'jdbc' }, { name: 'sqlServer', type: 'jdbc' }]
    }
  },

  created() {
    if (this.$router.currentRoute.params && this.$router.currentRoute.params.id) {
      const row = this.$router.currentRoute.params
      this.edit(row)
    } else {
      this.create()
    }
  },
  methods: {
    create() {
      this.formType = 'add'
    },
    edit(row) {
      this.formType = 'modify'
      this.form = Object.assign({}, row)
      this.form.configuration = JSON.parse(this.form.configuration)
    },

    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDs : editDs
          this.form.configuration = JSON.stringify(this.form.configuration)
          method(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.backToList()
          })
        } else {
          return false
        }
      })
    },
    validaDatasource() {
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
      this.$router.push({ name: '数据源1' })
    }
  }
}
</script>
