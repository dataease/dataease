<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <complex-table
      :data="data"
      :columns="columns"
      :buttons="buttons"
      :header="header"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
    >
      <template #buttons>
        <fu-table-button icon="el-icon-circle-plus-outline" :label="$t('datasource.create')" @click="create" />
      </template>

      <!-- <el-table-column type="selection" fix /> -->
      <el-table-column prop="name" :label="$t('commons.name')" />
      <el-table-column prop="desc" :label="$t('commons.description')" />
      <el-table-column prop="type" :label="$t('datasource.type')" />
      <fu-table-operations :buttons="buttons" :label="$t('commons.operating')" fix />

    </complex-table>

    <!-- add datasource form -->
    <el-dialog
      :close-on-click-modal="false"
      :title="formType=='add' ? $t('datasource.create') : $t('datasource.modify')"
      :visible.sync="dialogVisible"
      width="30%"
      :destroy-on-close="true"
      @closed="closeFunc"
    >
      <el-form ref="createDatasource" :model="form" label-position="right" label-width="100px" size="small">
        <el-form-item
          :label="$t('commons.name')"
          prop="name"
          :rules="[{required: true, message: this.$t('datasource.input_name'), trigger: 'blur'},
                   {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'}]"
        >
          <el-input v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item
          :label="$t('commons.description')"
          prop="desc"
          :rules="[{required: true, message: this.$t('datasource.input_desc'), trigger: 'blur'},
                   {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}]"
        >
          <el-input v-model="form.desc" autocomplete="off" type="textarea" />
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type" :rules="{required: true, message: $t('datasource.please_choose_type'), trigger: 'change'}">
          <el-select v-model="form.type" :placeholder="$t('datasource.please_choose_type')" class="select-width" @change="changeType()">
            <el-option
              v-for="item in allTypes"
              :key="item.name"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.data_base')" prop="configuration.dataBase" :rules="{required: true, message: $t('datasource.please_input_data_base'), trigger: 'blur'}">
          <el-input v-model="form.configuration.dataBase" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.user_name')" prop="configuration.username">
          <el-input v-model="form.configuration.username" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.password')" prop="configuration.password" :rules="{required: true, message: $t('datasource.please_input_password'), trigger: 'change'}">
          <el-input v-model="form.configuration.password" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.host')" prop="configuration.host" :rules="{required: true, message: $t('datasource.please_input_host'), trigger: 'change'}">
          <el-input v-model="form.configuration.host" autocomplete="off" />
        </el-form-item>
        <el-form-item v-show="form.configuration.dataSourceType=='jdbc'" :label="$t('datasource.port')" prop="configuration.port" :rules="{required: true, message: $t('datasource.please_input_port'), trigger: 'change'}">
          <el-input v-model="form.configuration.port" autocomplete="off" />
        </el-form-item>

      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="dialogVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="saveDatasource('createDatasource')">确认</el-button>
      </div>
    </el-dialog>

  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import { checkPermission } from '@/utils/permission'
import { formatCondition } from '@/utils/index'
import { dsGrid, addDs, editDs, delDs, validateDs } from '@/api/system/datasource'

export default {
  name: 'DEDatasource',
  components: {
    LayoutContent,
    ComplexTable
  },
  data() {
    return {
      formType: 'add',
      dialogVisible: false,
      data: [],
      form: { configuration: {}},
      allTypes: [{ name: 'mysql', type: 'jdbc' }, { name: 'sqlServer', type: 'jdbc' }],
      rule: {
        name: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur' }
        ],
        desc: [
          { required: true, message: this.$t('organization.input_name'), trigger: 'blur' },
          { max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur' }
        ]
      },
      header: '',
      columns: [],
      buttons: [
        {
          label: this.$t('commons.edit'), icon: 'el-icon-edit', click: this.edit,
          show: checkPermission(['datasource:edit'])
        }, {
          label: this.$t('commons.delete'), icon: 'el-icon-delete', type: 'danger', click: this.del,
          show: checkPermission(['datasource:del'])
        }
      ],
      searchConfig: {
        useQuickSearch: true,
        quickPlaceholder: '按名称搜索',
        combine: false,
        components: [
        //   { field: 'name', label: '姓名', component: 'FuComplexInput', defaultOperator: 'eq' },
          { field: 'name', label: '名称', component: 'FuComplexInput' },

          {
            field: 'type',
            label: '类型',
            component: 'FuComplexSelect',
            options: [{ label: 'mysql', value: 'mysql' }, { label: 'sqlServer', value: 'sqlServer' }],
            multiple: false
          }
        //   { field: 'deptId', label: '组织', component: conditionTable }
        ]
      },
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  activated() {
    this.search()
  },
  methods: {
    select(selection) {
      console.log(selection)
    },
    create() {
      this.formType = 'add'
      this.dialogVisible = true
    },

    edit(row) {
      this.formType = 'modify'
      this.dialogVisible = true
      this.form = Object.assign({}, row)
      this.form.configuration = JSON.parse(this.form.configuration)
    },

    _handleDelete(datasource) {
      this.$confirm(this.$t('datasource.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        delDs(datasource.id).then(res => {
          this.$success(this.$t('commons.delete_success'))
          this.search()
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    saveDatasource(createDatasourceForm) {
      this.$refs[createDatasourceForm].validate(valid => {
        if (valid) {
          const method = this.formType === 'add' ? addDs : editDs
          this.form.configuration = JSON.stringify(this.form.configuration)
          method(this.form).then(res => {
            this.$success(this.$t('commons.save_success'))
            this.search()
            this.dialogVisible = false
          })
        } else {
          return false
        }
      })
    },

    validaDatasource(datasourceForm) {
      this.$refs[datasourceForm].validate(valid => {
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
    quick_condition(condition) {
      const result = {}
      if (condition && condition.quick) {
        for (const [key, value] of Object.entries(condition)) {
          // console.log(`${key}`)
          if (`${key}` === 'quick') {
            const v_new = Object.assign({}, value)
            v_new['field'] = 'name'
            result['name'] = v_new
          } else {
            result[`${key}`] = value
          }
        }
        return result
      }
      return Object.assign({}, condition)
    },
    search(condition) {
      const temp_param = this.quick_condition(condition)
      const temp = formatCondition(temp_param)
      const param = temp || {}
      const { currentPage, pageSize } = this.paginationConfig
      dsGrid(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },

    closeFunc() {
      this.formType = 'add'
      // this.search()
      this.form = { configuration: {}}
      this.dialogVisible = false
    }

  }

}
</script>

<style scoped>

</style>
