<template>
  <layout-content :header="headName">
    <template
      v-if="!optType"
      #header
    >
      <el-icon
        name="back"
        class="back-button"
        @click.native="backToList"
      />
      {{
        params && params.id && params.showModel && params.showModel === 'show' && !canEdit ? $t('datasource.show_info') : formType === 'add' ? $t('datasource.create') : $t('datasource.modify')
      }}
    </template>
    <template v-if="optType==='appApply'">
      <span>模板信息</span>
    </template>
    <div v-if="optType==='appApply'">
      <el-form
        ref="attachParamsForm"
        :model="attachForm"
        :rules="rule"
        size="small"
        label-width="180px"
        label-position="right"
      >
        <el-form-item
          :label="'仪表板位置'"
          prop="panelId"
        >
          <treeselect
            v-model="attachForm.panelId"
            :clearable="false"
            :options="panelGroupList"
            :normalizer="normalizer"
            :placeholder="$t('chart.select_group')"
            :no-children-text="$t('commons.treeselect.no_children_text')"
            :no-options-text="$t('commons.treeselect.no_options_text')"
            :no-results-text="$t('commons.treeselect.no_results_text')"
          />
        </el-form-item>
        <el-form-item
          :label="'仪表板名称'"
          prop="panelName"
        >
          <el-input v-model="attachForm.panelName" />
        </el-form-item>
        <el-form-item
          :label="'数据集位置'"
          prop="desc"
        >
          <treeselect
            v-model="attachForm.datasetGroupId"
            :clearable="false"
            :options="datasetGroupList"
            :normalizer="normalizer"
            :placeholder="$t('chart.select_group')"
            :no-children-text="$t('commons.treeselect.no_children_text')"
            :no-options-text="$t('commons.treeselect.no_options_text')"
            :no-results-text="$t('commons.treeselect.no_results_text')"
          />
        </el-form-item>
        <el-form-item
          :label="'数据集分组名称'"
          prop="datasetGroupName"
        >
          <el-input v-model="attachForm.datasetGroupName" />
        </el-form-item>
      </el-form>
    </div>

    <template v-if="optType==='appApply'">
      <span>数据源信息</span>
    </template>
    <div>
      <el-form
        ref="dsForm"
        :model="form"
        :rules="rule"
        size="small"
        :disabled="params && params.id && params.showModel && params.showModel === 'show' && !canEdit"
        label-width="180px"
        label-position="right"
      >
        <el-form-item
          :label="$t('commons.name')"
          prop="name"
        >
          <el-input
            v-model="form.name"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="$t('commons.description')"
          prop="desc"
        >
          <el-input
            v-model="form.desc"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="$t('datasource.type')"
          prop="type"
        >
          <el-select
            v-model="form.type"
            :placeholder="$t('datasource.please_choose_type')"
            class="select-width"
            style="width: 100%"
            :disabled="formType==='modify' || (formType==='add' && params && !!params.type)"
            filterable
            @change="changeType()"
          >
            <el-option
              v-for="item in dsTypes"
              :key="item.type"
              :label="item.name"
              :value="item.type"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          v-if="datasourceType.isJdbc"
          :label="$t('driver.driver')"
        >
          <el-select
            v-model="form.configuration.customDriver"
            :placeholder="$t('driver.please_choose_driver')"
            class="select-width"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="item in driverList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="!item.driverClass"
            />
          </el-select>
        </el-form-item>

        <ds-configuration
          v-if="!datasourceType.isPlugin"
          ref="dsConfig"
          :datasource-type="datasourceType"
          :form="form"
          :disabled="params && params.id && params.showModel && params.showModel === 'show' && !canEdit"
        />
        <plugin-com
          v-if="datasourceType.isPlugin"
          ref="pluginDsConfig"
          :component-name="datasourceType.type"
          :obj="{form, disabled }"
        />

      </el-form>

      <div
        v-if="canEdit"
        slot="footer"
        class="dialog-footer"
      >
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
      <div
        v-else
        slot="footer"
        class="dialog-footer"
      >
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
import {
  addDs,
  editDs,
  getSchema,
  validateDs,
  validateDsById,
  checkApiDatasource,
  listDriverByType
} from '@/api/system/datasource'
import { $confirm } from '@/utils/message'
import i18n from '@/lang/index'
import DsConfiguration from '@/views/system/datasource/DsConfiguration'
import PluginCom from '@/views/system/plugin/PluginCom'
import { groupTree, appApply } from '@/api/panel/panel'
import { dsGroupTree } from '@/api/dataset/dataset'
import { deepCopy } from '@/components/canvas/utils/utils'
import { Base64 } from 'js-base64'
export default {
  name: 'DsForm',
  components: {
    DsConfiguration,
    LayoutContent,
    PluginCom
  },
  props: {
    params: {
      type: Object,
      default: null
    },
    tData: {
      type: Array,
      default: null
    },
    dsTypes: {
      type: Array,
      default: null
    },
    attachParams: {
      type: Object,
      default: null
    },
    optType: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      disabled: false,
      attachRule: {
        datasetGroupId: [{ required: true, message: i18n.t('chart.select_group'), trigger: 'blur' }],
        panelId: [{ required: true, message: i18n.t('chart.select_group'), trigger: 'blur' }]
      },
      panelGroupList: [],
      datasetGroupList: [],
      attachForm: {
        appTemplateId: '',
        panelId: '',
        panelName: '',
        datasetGroupId: '0',
        datasetGroupName: ''
      },
      form: {
        configuration: {
          initialPoolSize: 5,
          extraParams: '',
          minPoolSize: 5,
          maxPoolSize: 50,
          maxIdleTime: 30,
          acquireIncrement: 5,
          idleConnectionTestPeriod: 5,
          connectTimeout: 5,
          customDriver: 'default',
          queryTimeout: 30
        },
        apiConfiguration: []
      },
      datasourceType: {},
      rule: {
        name: [{ required: true, message: i18n.t('datasource.input_name'), trigger: 'blur' },
          { min: 2, max: 25, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur' }],
        desc: [{ min: 2, max: 50, message: i18n.t('datasource.input_limit_2_50'), trigger: 'blur' }],
        type: [{ required: true, message: i18n.t('datasource.please_choose_type'), trigger: 'blur' }],
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
          trigger: 'blur'
        }],
        'configuration.host': [{ required: true, message: i18n.t('datasource.please_input_host'), trigger: 'blur' }],
        'configuration.url': [{ required: true, message: i18n.t('datasource.please_input_url'), trigger: 'blur' }],
        'configuration.port': [{ required: true, message: i18n.t('datasource.please_input_port'), trigger: 'blur' }],
        'configuration.initialPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_initial_pool_size'),
          trigger: 'blur'
        }],
        'configuration.minPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_min_pool_size'),
          trigger: 'blur'
        }],
        'configuration.maxPoolSize': [{
          required: true,
          message: i18n.t('datasource.please_input_max_pool_size'),
          trigger: 'blur'
        }],
        'configuration.maxIdleTime': [{
          required: true,
          message: i18n.t('datasource.please_input_max_idle_time'),
          trigger: 'blur'
        }],
        'configuration.acquireIncrement': [{
          required: true,
          message: i18n.t('datasource.please_input_acquire_increment'),
          trigger: 'blur'
        }],
        'configuration.connectTimeout': [{
          required: true,
          message: i18n.t('datasource.please_input_connect_timeout'),
          trigger: 'blur'
        }],
        'url': [{ required: true, message: i18n.t('datasource.please_input_url'), trigger: 'blur' }],
        'dataPath': [{ required: true, message: i18n.t('datasource.please_input_dataPath'), trigger: 'blur' }]
      },
      schemas: [],
      canEdit: false,
      originConfiguration: {},
      edit_api_item: false,
      add_api_item: true,
      active: 0,
      defaultApiItem: {
        name: '',
        url: '',
        method: 'GET',
        request: {
          headers: [{}],
          body: {
            'type': '',
            'raw': '',
            'kvs': []
          }
        },
        fields: []
      },
      apiItem: {
        status: '',
        name: '',
        url: '',
        method: 'GET',
        dataPath: '',
        request: {
          headers: [],
          body: {
            'type': '',
            'raw': '',
            'kvs': []
          },
          authManager: {}
        },
        fields: []
      },
      reqOptions: [{ id: 'GET', label: 'GET' }, { id: 'POST', label: 'POST' }],
      loading: false,
      responseData: { type: 'HTTP', responseResult: {}, subRequestResults: [] },
      api_table_title: '',
      api_step2_active_name: 'first',
      fieldTypes: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      height: 500,
      disabledNext: false,
      driverList: []
    }
  },
  computed: {
    headName() {
      if (this.optType === 'appApply') {
        return ''
      } else if (this.formType === 'add') {
        return this.$t('datasource.create')
      } else {
        return this.$t('datasource.modify')
      }
    }
  },
  created() {
    if (this.params && this.params.id) {
      const row = this.params
      this.edit(row)
      this.changeType(true)
    } else {
      this.create()
      if (this.params && this.params.type) {
        this.setType()
        this.changeType()
      }
    }
    this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
  },
  mounted() {
    if (this.optType === 'appApply') {
      this.getPanelGroupTree()
      this.getDatasetGroupTree()
      this.attachForm.appTemplateId = this.attachParams.appTemplateId
      this.attachForm.panelName = this.attachParams.name
      this.attachForm.datasetGroupName = this.attachParams.name
    }
  },
  methods: {
    normalizer(node) {
      // 去掉children=null的属性
      if (node.children === null || node.children === 'null') {
        delete node.children
      }
    },
    getDatasetGroupTree() {
      dsGroupTree({ nodeType: 'group' }).then(res => {
        this.datasetGroupList = [{
          id: '0',
          name: this.$t('dataset.dataset_group'),
          label: this.$t('dataset.dataset_group'),
          pid: '0',
          privileges: 'grant,manage,use',
          type: 'group',
          children: res.data
        }]
      })
    },
    getPanelGroupTree() {
      groupTree({ nodeType: 'folder' }).then(res => {
        this.panelGroupList = res.data
        if (this.panelGroupList && this.panelGroupList.length > 0) {
          this.attachForm.panelId = this.panelGroupList[0].id
        }
      })
    },
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
        connectTimeout: 5,
        customDriver: 'default',
        queryTimeout: 30
      }
    },
    changeEdit() {
      this.canEdit = true
      this.formType = 'modify'
      this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
    },
    create() {
      this.formType = 'add'
      this.canEdit = true
      this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
    },
    edit(row) {
      this.formType = 'modify'
      this.form = JSON.parse(JSON.stringify(row))
      if (row.type === 'api') {
        this.originConfiguration = JSON.parse(JSON.stringify(this.form.apiConfiguration))
        this.originConfiguration.forEach(item => {
          delete item.status
        })
        this.originConfiguration = JSON.stringify(this.originConfiguration)
      } else {
        this.form.configuration = JSON.parse(this.form.configuration)
        this.originConfiguration = JSON.stringify(this.form.configuration)
      }
      this.disabled = this.params && this.params.id && this.params.showModel && this.params.showModel === 'show' && !this.canEdit
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      if (!this.form.configuration.schema && (this.form.type === 'oracle' || this.form.type === 'sqlServer' || this.form.type === 'pg' || this.form.type === 'redshift' || this.form.type === 'db2')) {
        this.$message.error(i18n.t('datasource.please_choose_schema'))
        return
      }
      if (this.form.type !== 'es' && this.form.type !== 'api' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      if (this.form.configuration.initialPoolSize < 0 || this.form.configuration.minPoolSize < 0 || this.form.configuration.maxPoolSize < 0) {
        this.$message.error(i18n.t('datasource.no_less_then_0'))
        return
      }
      let repeat = false
      const repeatDsName = []
      if (!this.datasourceType.isPlugin) {
        this.tData.forEach(item => {
          if (item.id === this.form.type) {
            item.children.forEach(child => {
              if (this.formType === 'modify' && child.id === this.form.id) {
                return
              }
              const configuration = JSON.parse(child.configuration)
              if (!configuration) {
                return
              }
              switch (this.form.type) {
                case 'mysql':
                case 'TiDB':
                case 'StarRocks':
                case 'hive':
                case 'mariadb':
                case 'ds_doris':
                case 'ck':
                case 'mongo':
                case 'impala':
                  if (configuration.host === this.form.configuration.host && configuration.dataBase === this.form.configuration.dataBase && configuration.port === this.form.configuration.port) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                  break
                case 'pg':
                case 'sqlServer':
                case 'redshift':
                case 'oracle':
                case 'db2':
                  if (configuration.host === this.form.configuration.host && configuration.dataBase === this.form.configuration.dataBase && configuration.port === this.form.configuration.port && configuration.schema === this.form.configuration.schema) {
                    repeatDsName.push(child.name)
                    repeat = true
                  }
                  break
                case 'es':
                  if (configuration.url === this.form.configuration.url) {
                    repeatDsName.push(child.name)
                    repeat = true
                  }
                  break
                default:
                  break
              }
            })
          }
        })
      } else {
        if (this.datasourceType.isJdbc) {
          this.tData.forEach(item => {
            if (item.id === this.form.type) {
              item.children.forEach(child => {
                if (this.formType === 'modify' && child.id === this.form.id) {
                  return
                }
                const configuration = JSON.parse(child.configuration)
                if (!configuration) {
                  return
                }
                if (configuration.schema !== null) {
                  if (configuration.schema === this.form.configuration.schema && configuration.host === this.form.configuration.host && configuration.dataBase === this.form.configuration.dataBase && configuration.port === this.form.configuration.port) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                } else {
                  if (configuration.host === this.form.configuration.host && configuration.dataBase === this.form.configuration.dataBase && configuration.port === this.form.configuration.port) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                }
              })
            }
          })
        }
      }
      let status = null
      if (this.datasourceType.isPlugin) {
        status = this.$refs['pluginDsConfig'].callPluginInner({ methodName: 'validate' })
      } else {
        this.$refs['dsConfig'].$refs['DsConfig'].validate(valid => {
          status = valid
        })
      }
      if (!status) {
        return
      }
      if (this.optType === 'appApply') {
        this.$refs.attachParamsForm.validate(valid => {
          if (!valid) {
            return false
          }
        }
        )
      }
      this.$refs.dsForm.validate(valid => {
        if (!valid) {
          return false
        }
        const method = this.formType === 'add' ? addDs : editDs
        const form = JSON.parse(JSON.stringify(this.form))
        if (form.type === 'api') {
          if (this.form.apiConfiguration.length < 1) {
            this.$message.error(i18n.t('datasource.api_table_not_empty'))
            return
          }
          form.apiConfiguration.forEach(item => {
            delete item.status
          })
          form.configuration = JSON.stringify(form.apiConfiguration)
        } else {
          form.configuration = JSON.stringify(form.configuration)
        }
        if (this.optType === 'appApply') {
          const appApplyForm = {
            ...this.attachForm,
            datasourceList: [deepCopy(form)]
          }
          appApply(appApplyForm).then(res => {
            this.$success(i18n.t('commons.save_success'))
            this.$router.push({ name: 'panel', params: res.data })
          })
          return
        }
        if (this.formType === 'modify' && this.originConfiguration !== form.configuration) {
          if (repeat) {
            $confirm(i18n.t('datasource.repeat_datasource_msg') + '[' + repeatDsName.join(',') + '], ' + i18n.t('datasource.confirm_save'), () => {
              $confirm(i18n.t('datasource.edit_datasource_msg'), () => {
                this.method(method, form)
              })
            })
          } else {
            $confirm(i18n.t('datasource.edit_datasource_msg'), () => {
              this.method(method, form)
            })
          }
          return
        }
        if (repeat) {
          $confirm(i18n.t('datasource.repeat_datasource_msg') + '[' + repeatDsName.join(',') + '], ' + i18n.t('datasource.confirm_save'), () => {
            this.method(method, form)
          })
        } else {
          this.method(method, form)
        }
      })
    },
    method(method, form) {
      method(form).then(res => {
        this.$success(i18n.t('commons.save_success'))
        this.refreshType(form)
        this.backToList()
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
      if (this.form.type !== 'es' && this.form.type !== 'api' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      let status = null
      if (this.datasourceType.isPlugin) {
        status = this.$refs['pluginDsConfig'].callPluginInner({ methodName: 'validate' })
      } else {
        this.$refs['dsConfig'].$refs['DsConfig'].validate(valid => {
          status = valid
          if (!valid) {
            return
          }
        })
      }
      if (!status) {
        return
      }
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          if (data.type === 'api') {
            data.configuration = Base64.encode(JSON.stringify(data.apiConfiguration))
          } else {
            data.configuration = Base64.encode(JSON.stringify(data.configuration))
          }
          data.configurationEncryption = true
          if (data.showModel === 'show' && !this.canEdit) {
            validateDsById(data.id).then(res => {
              if (res.success) {
                this.$success(i18n.t('datasource.validate_success'))
              } else {
                if (res.message.length < 2500) {
                  this.$error(res.message)
                } else {
                  this.$error(res.message.substring(0, 2500) + '......')
                }
              }
              this.refreshType(data)
            })
          } else {
            validateDs(data).then(res => {
              if (res.success) {
                this.$success(i18n.t('datasource.validate_success'))
              } else {
                if (data.type === 'api') {
                  this.form.apiConfiguration = res.data.apiConfiguration
                }
                if (res.message.length < 2500) {
                  this.$error(res.message)
                } else {
                  this.$error(res.message.substring(0, 2500) + '......')
                }
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
    changeType(init) {
      for (let i = 0; i < this.dsTypes.length; i++) {
        if (this.dsTypes[i].type === this.form.type) {
          if (this.form.type !== 'api' && !init) {
            this.form.configuration.extraParams = this.dsTypes[i].extraParams
            this.form.configuration.customDriver = 'default'
          }
          this.datasourceType = this.dsTypes[i]
          if (this.datasourceType.isJdbc) {
            listDriverByType(this.datasourceType.type).then(res => {
              this.driverList = []
              this.driverList.push({ id: 'default', name: 'Default', driverClass: 'Default' })
              this.driverList = this.driverList.concat(res.data)
            })
          }
        }
      }
    },
    backToList() {
      this.$emit('switch-component', {})
    },
    refreshType(form) {
      this.$emit('refresh-type', form)
    },
    next() {
      if (this.active === 1) {
        let hasRepeatName = false
        if (this.add_api_item) {
          this.form.apiConfiguration.forEach(item => {
            if (item.name === this.apiItem.name) {
              hasRepeatName = true
            }
          })
        } else {
          const index = this.form.apiConfiguration.indexOf(this.apiItem)
          for (let i = 0; i < this.form.apiConfiguration.length; i++) {
            if (i !== index && this.form.apiConfiguration[i].name === this.apiItem.name) {
              hasRepeatName = true
            }
          }
        }
        if (hasRepeatName) {
          this.$message.error(i18n.t('datasource.has_repeat_name'))
          return
        }
        this.$refs.apiItem.validate(valid => {
          if (valid) {
            const data = JSON.parse(JSON.stringify(this.apiItem))
            data.request = JSON.stringify(data.request)
            this.loading = true
            this.disabledNext = true
            checkApiDatasource({'data': Base64.encode(JSON.stringify(data))}).then(res => {
              this.loading = false
              this.disabledNext = false
              this.apiItem.status = 'Success'
              this.$success(i18n.t('commons.success'))
              this.active++
              this.apiItem.fields = res.data.fields
              this.$refs.plxTable.reloadData(res.data.data)
            }).catch(res => {
              this.loading = false
              this.disabledNext = false
            })
          } else {
            this.apiItem.fields = []
            return false
          }
        })
      }
    },
    before() {
      this.active--
    },
    closeEditItem() {
      this.active = 0
      this.edit_api_item = false
    },
    saveItem() {
      this.active = 0
      this.edit_api_item = false
      if (this.add_api_item) {
        this.form.apiConfiguration.push(this.apiItem)
      }
    },
    addApiItem(item) {
      this.$nextTick(() => {
        this.$refs.apiItem.clearValidate()
      })
      if (item) {
        this.add_api_item = false
        this.api_table_title = this.$t('datasource.edit_api_table')
        this.apiItem = item
      } else {
        this.add_api_item = true
        this.apiItem = JSON.parse(JSON.stringify(this.defaultApiItem))
        this.api_table_title = this.$t('datasource.add_api_table')
      }
      this.active = 1
      this.edit_api_item = true
    },
    deleteItem(item) {
      this.form.apiConfiguration.splice(this.form.apiConfiguration.indexOf(item), 1)
    },
    validateApi(item) {
      this.$refs.apiItem.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.apiItem))
          data.request = JSON.stringify(data.request)
          this.loading = true
          checkApiDatasource({'data': Base64.encode(JSON.stringify(data))}).then(res => {
            this.loading = false
            this.$success(i18n.t('commons.success'))
            this.apiItem.fields = res.data.fields
            this.$refs.plxTable.reloadData(res.data.data)
          }).catch(res => {
            this.loading = false
          })
        } else {
          return false
        }
      })
    },
    handleClick(tab, event) {
    }
  }
}
</script>
<style scoped>
/* .el-input {
  width: 300px;
}
.el-select {
  width: 300px;
} */
.ms-http-input {
  width: 500px;
  margin-top: 5px;
}
.tip {
  padding: 3px 5px;
  font-size: 16px;
  border-radius: 0;
  border-left: 4px solid #409EFF;
  margin: 5px 5px 10px 5px;
}
.el-select ::v-deep input {
  padding-right: 10px;
}
.el-select ::v-deep .el-input__suffix {
  right: 0;
}
.dialog-css ::v-deep .el-dialog__header {
  padding: 10px 20px 0px;
}
.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 10px;
}
.dialog-footer ::v-deep .el-dialog__footer {
  padding: 10px 10px 10px;
}
</style>
