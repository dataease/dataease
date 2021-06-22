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
          <el-select v-model="form.type" :placeholder="$t('datasource.please_choose_type')" class="select-width" :disabled="formType=='modify'" @change="changeType()">
            <el-option
              v-for="item in allTypes"
              :key="item.name"
              :label="item.label"
              :value="item.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host">
          <el-input v-model="form.configuration.host" autocomplete="off" :disabled="formType=='modify'" />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase">
          <el-input v-model="form.configuration.dataBase" autocomplete="off" :disabled="formType=='modify'" />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
          <el-input v-model="form.configuration.username" autocomplete="off" :disabled="formType=='modify'" />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password">
          <el-input v-model="form.configuration.password" autocomplete="off" show-password />
        </el-form-item>
        <el-form-item v-if="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port">
          <el-input v-model="form.configuration.port" autocomplete="off" />
        </el-form-item>

      <!-- <el-form-item v-if="canEdit">
        <el-button @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button type="primary" @click="save">{{ $t('commons.save') }}</el-button>
      </el-form-item>

      <el-form-item v-else>
        <el-button @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button @click="changeEdit">{{ $t('commons.edit') }}</el-button>
      </el-form-item> -->
      </el-form>
      <div v-if="canEdit" slot="footer" class="dialog-footer">
        <el-button @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button type="primary" @click="save">{{ $t('commons.save') }}</el-button>
      </div>
      <div v-else slot="footer" class="dialog-footer">
        <el-button @click="validaDatasource">{{ $t('commons.validate') }}</el-button>
        <el-button type="primary" @click="changeEdit">{{ $t('commons.edit') }}</el-button>
      </div>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { addDs, editDs, validateDs } from '@/api/system/datasource'
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
        'configuration.username': [{ required: true, message: this.$t('datasource.please_input_user_name'), trigger: 'blur' }],
        'configuration.password': [{ required: true, message: this.$t('datasource.please_input_password'), trigger: 'change' }],
        'configuration.host': [{ required: true, message: this.$t('datasource.please_input_host'), trigger: 'change' }],
        'configuration.port': [{ required: true, message: this.$t('datasource.please_input_port'), trigger: 'change' }]
      },
      allTypes: [{ name: 'mysql', label: 'MySQL', type: 'jdbc' },
        { name: 'sqlServer', label: 'SQL Server', type: 'jdbc' }],
      canEdit: false
    }
  },

  created() {
    // if (this.$router.currentRoute.params && this.$router.currentRoute.params.id) {
    //   const row = this.$router.currentRoute.params
    //   this.edit(row)
    // } else {
    //   this.create()
    // }
    if (this.params && this.params.id) {
      const row = this.params
      this.edit(row)
    } else {
      this.create()
    }
  },
  mounted() {
    // if (this.params && this.params.type) {
    //   this.form.type = this.params.type
    //   this.$nextTick(() => {
    //     this.changeType()
    //   })
    // }
  },
  methods: {
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
      this.form.configuration = JSON.parse(this.form.configuration)
    },

    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDs : editDs
          // this.form.configuration = JSON.stringify(this.form.configuration)
          const form = JSON.parse(JSON.stringify(this.form))
          form.configuration = JSON.stringify(form.configuration)
          method(form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.refreshTree()
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
