<template>
  <div v-loading="formLoading" class="de-ds-form">
    <div class="de-ds-top">
      <span class="name">
        <i class="el-icon-arrow-left" @click="logOutTips" />
        {{
          params &&
            params.id &&
            params.showModel &&
            params.showModel === 'show' &&
            !canEdit
            ? $t('datasource.show_info')
            : formType == 'add'
              ? `${$t('commons.create') + typeMap }${ $t('commons.datasource')}`
              : $t('datasource.modify')
        }}
      </span>
      <div class="apply">
        <template v-if="canEdit">
          <deBtn
            secondary
            @click="logOutTips"
          >{{ $t('commons.cancel') }}
          </deBtn>
          <deBtn
            v-if="
              formType === 'add' ||
                hasDataPermission('manage', params.privileges)
            "
            secondary
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
          <deBtn
            v-if="
              formType === 'add' ||
                hasDataPermission('manage', params.privileges)
            "
            type="primary"
            @click="save"
          >{{ $t('commons.save') }}
          </deBtn>
        </template>
        <template v-else>
          <deBtn
            v-if="
              formType === 'add'
                ? true
                : hasDataPermission('manage', params.privileges)
            "
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
        </template>
      </div>
    </div>
    <div class="de-ds-cont">
      <div class="de-ds-inner">
        <div class="w600">
          <el-form
            ref="dsForm"
            :model="form"
            :rules="rule"
            size="small"
            :disabled="
              params &&
                params.id &&
                params.showModel &&
                params.showModel === 'show' &&
                !canEdit
            "
            class="de-form-item"
            label-width="180px"
            label-position="right"
          >
            <div class="de-row-rules">
              <span>{{ $t('datasource.basic_info') }}</span>
            </div>
            <el-form-item :label="$t('datasource.display_name')" prop="name">
              <el-input
                v-model="form.name"
                autocomplete="off"
                :placeholder="$t('commons.input_name')"
              />
            </el-form-item>
            <el-form-item :label="$t('commons.description')" prop="desc">
              <deTextarea
                v-model="form.desc"
                class="w100-textarea"
              />
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
              :disabled="
                params &&
                  params.id &&
                  params.showModel &&
                  params.showModel === 'show' &&
                  !canEdit
              "
            />
            <plugin-com
              v-if="datasourceType.isPlugin"
              ref="pluginDsConfig"
              :component-name="datasourceType.type"
              :obj="{ form, disabled }"
            />
          </el-form>
        </div>
      </div>
    </div>
  </div>
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
  listDriverByType,
  getDatasourceDetail
} from '@/api/system/datasource'
import { $confirm } from '@/utils/message'
import i18n from '@/lang/index'
import ApiHttpRequestForm from '@/views/system/datasource/ApiHttpRequestForm'
import DsConfiguration from '@/views/system/datasource/DsConfiguration'
import PluginCom from '@/views/system/plugin/PluginCom'
import { listDatasourceType, listDatasource } from '@/api/system/datasource'
import deTextarea from '@/components/deCustomCm/deTextarea.vue'
import msgCfm from '@/components/msgCfm'
export default {
  name: 'DsForm',
  components: {
    DsConfiguration,
    LayoutContent,
    ApiHttpRequestForm,
    PluginCom,
    deTextarea
  },
  mixins: [msgCfm],
  data() {
    return {
      disabled: false,
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
          queryTimeout: 30,
          host: ''
        },
        apiConfiguration: []
      },
      datasourceType: {},
      rule: {
        name: [
          {
            required: true,
            validator: this.nameRepeat,
            message: i18n.t('datasource.input_name'),
            trigger: 'blur'
          },
          {
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        desc: [
          {
            min: 2,
            max: 50,
            message: i18n.t('datasource.input_limit_2_50'),
            trigger: 'blur'
          }
        ],
        type: [
          {
            required: true,
            message: i18n.t('datasource.please_choose_type'),
            trigger: 'blur'
          }
        ],
        'configuration.dataBase': [
          {
            required: true,
            message: i18n.t('datasource.please_input_data_base'),
            trigger: 'blur'
          }
        ],
        'configuration.connectionType': [
          {
            required: true,
            message: i18n.t('datasource.please_select_oracle_type'),
            trigger: 'blur'
          }
        ],
        'configuration.username': [
          {
            required: true,
            message: i18n.t('datasource.please_input_user_name'),
            trigger: 'blur'
          }
        ],
        'configuration.password': [
          {
            required: true,
            message: i18n.t('datasource.please_input_password'),
            trigger: 'blur'
          }
        ],
        'configuration.host': [
          {
            required: true,
            message: i18n.t('datasource.please_input_host'),
            trigger: 'blur'
          }
        ],
        'configuration.url': [
          {
            required: true,
            message: i18n.t('datasource.please_input_url'),
            trigger: 'blur'
          }
        ],
        'configuration.port': [
          {
            required: true,
            message: i18n.t('datasource.please_input_port'),
            trigger: 'blur'
          }
        ],
        'configuration.initialPoolSize': [
          {
            required: true,
            message: i18n.t('datasource.please_input_initial_pool_size'),
            trigger: 'blur'
          }
        ],
        'configuration.minPoolSize': [
          {
            required: true,
            message: i18n.t('datasource.please_input_min_pool_size'),
            trigger: 'blur'
          }
        ],
        'configuration.maxPoolSize': [
          {
            required: true,
            message: i18n.t('datasource.please_input_max_pool_size'),
            trigger: 'blur'
          }
        ],
        'configuration.maxIdleTime': [
          {
            required: true,
            message: i18n.t('datasource.please_input_max_idle_time'),
            trigger: 'blur'
          }
        ],
        'configuration.acquireIncrement': [
          {
            required: true,
            message: i18n.t('datasource.please_input_acquire_increment'),
            trigger: 'blur'
          }
        ],
        'configuration.connectTimeout': [
          {
            required: true,
            message: i18n.t('datasource.please_input_connect_timeout'),
            trigger: 'blur'
          }
        ],
        url: [
          {
            required: true,
            message: i18n.t('datasource.please_input_url'),
            trigger: 'blur'
          }
        ],
        dataPath: [
          {
            required: true,
            message: i18n.t('datasource.please_input_dataPath'),
            trigger: 'blur'
          }
        ]
      },
      schemas: [],
      typeMap: '',
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
            type: '',
            raw: '',
            kvs: []
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
            type: '',
            raw: '',
            kvs: []
          },
          authManager: {}
        },
        fields: []
      },
      reqOptions: [
        { id: 'GET', label: 'GET' },
        { id: 'POST', label: 'POST' }
      ],
      loading: false,
      formLoading: false,
      responseData: { type: 'HTTP', responseResult: {}, subRequestResults: [] },
      api_table_title: '',
      api_step2_active_name: 'first',
      fieldTypes: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        {
          label:
            this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
          value: 3
        },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      height: 500,
      disabledNext: false,
      driverList: [],
      tData: [],
      dsTypes: [],
      msgNodeId: '',
      params: {},
      formType: 'add'
    }
  },
  async created() {
    await this.datasourceTypes()
    this.queryTreeDatas()
    const { id, showModel, msgNodeId, type, name } = this.$route.query
    this.params = this.$route.query
    if (id) {
      await this.getDatasourceDetail(id, showModel)
      this.edit(this.params)
      this.changeType(true)
    } else {
      this.canEdit = true
      this.disabled = false
      if (type) {
        this.typeMap = name
        this.setType()
        this.changeType()
      }
    }
    this.disabled = Boolean(id) && showModel === 'show' && !this.canEdit
  },
  methods: {
    datasourceTypes() {
      return listDatasourceType().then((res) => {
        this.dsTypes = res.data || []
      })
    },
    getDatasourceDetail(id, showModel) {
      return getDatasourceDetail(id).then((res) => {
        this.params = { ...res.data, showModel }
      })
    },
    queryTreeDatas() {
      listDatasource().then((res) => {
        this.tData = this.buildTree(res.data)
      })
    },
    buildTree(array) {
      const types = {}
      const newArr = []
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (this.msgNodeId) {
          if (element.id === this.msgNodeId) {
            element.msgNode = true
          }
        }
        if (!(element.type in types)) {
          types[element.type] = []
          // newArr.push(...element, ...{ children: types[element.type] })
          newArr.push({
            id: element.type,
            name: element.typeDesc,
            type: 'folder',
            children: types[element.type]
          })
        }
        types[element.type].push(element)
        // newArr.children.push({ id: element.id, label: element.name })
      }
      return newArr
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
      if (this.form.type == 'oracle') {
        this.$set(this.form.configuration, 'charset', 'Default')
        this.$set(this.form.configuration, 'targetCharset', 'Default')
      }
    },
    changeEdit() {
      this.canEdit = true
      this.formType = 'modify'
      this.disabled =
        this.params &&
        this.params.id &&
        this.params.showModel &&
        this.params.showModel === 'show' &&
        !this.canEdit
    },
    edit(row) {
      this.formType = 'modify'
      this.form = JSON.parse(JSON.stringify(row))
      if (row.type === 'api') {
        this.originConfiguration = JSON.parse(
          JSON.stringify(this.form.apiConfiguration)
        )
        this.originConfiguration.forEach((item) => {
          delete item.status
        })
        this.originConfiguration = JSON.stringify(this.originConfiguration)
      } else {
        this.form.configuration = JSON.parse(this.form.configuration)
        this.originConfiguration = JSON.stringify(this.form.configuration)
      }
      this.disabled =
        this.params &&
        this.params.id &&
        this.params.showModel &&
        this.params.showModel === 'show' &&
        !this.canEdit
      this.changeEdit()
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      if (
        !this.form.configuration.schema &&
        (this.form.type === 'oracle' ||
          this.form.type === 'sqlServer' ||
          this.form.type === 'pg' ||
          this.form.type === 'redshift' ||
          this.form.type === 'db2')
      ) {
        this.$message.error(i18n.t('datasource.please_choose_schema'))
        return
      }
      if (
        this.form.type !== 'es' &&
        this.form.type !== 'api' &&
        this.form.configuration.port <= 0
      ) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      if (
        this.form.configuration.initialPoolSize < 0 ||
        this.form.configuration.minPoolSize < 0 ||
        this.form.configuration.maxPoolSize < 0
      ) {
        this.$message.error(i18n.t('datasource.no_less_then_0'))
        return
      }
      let repeat = false
      const repeatDsName = []
      if (!this.datasourceType.isPlugin) {
        this.tData.forEach((item) => {
          if (item.id === this.form.type) {
            item.children.forEach((child) => {
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
                case 'mariadb':
                case 'impala':
                  if (
                    configuration.host == this.form.configuration.host &&
                    configuration.dataBase ==
                      this.form.configuration.dataBase &&
                    configuration.port == this.form.configuration.port
                  ) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                  break
                case 'pg':
                case 'sqlServer':
                case 'redshift':
                case 'oracle':
                case 'db2':
                  if (
                    configuration.host == this.form.configuration.host &&
                    configuration.dataBase ==
                      this.form.configuration.dataBase &&
                    configuration.port == this.form.configuration.port &&
                    configuration.schema == this.form.configuration.schema
                  ) {
                    repeatDsName.push(child.name)
                    repeat = true
                  }
                  break
                case 'es':
                  if (configuration.url == this.form.configuration.url) {
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
          this.tData.forEach((item) => {
            if (item.id === this.form.type) {
              item.children.forEach((child) => {
                if (this.formType === 'modify' && child.id === this.form.id) {
                  return
                }
                const configuration = JSON.parse(child.configuration)
                if (!configuration) {
                  return
                }
                if (configuration.schema != null) {
                  if (
                    configuration.schema == this.form.configuration.schema &&
                    configuration.host == this.form.configuration.host &&
                    configuration.dataBase ==
                      this.form.configuration.dataBase &&
                    configuration.port == this.form.configuration.port
                  ) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                } else {
                  if (
                    configuration.host == this.form.configuration.host &&
                    configuration.dataBase ==
                      this.form.configuration.dataBase &&
                    configuration.port == this.form.configuration.port
                  ) {
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
        status = this.$refs['pluginDsConfig'].callPluginInner({
          methodName: 'validate'
        })
      } else {
        this.$refs['dsConfig'].$refs['DsConfig'].validate((valid) => {
          status = valid
        })
      }
      if (!status) {
        return
      }
      this.$refs.dsForm.validate((valid) => {
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
          form.apiConfiguration.forEach((item) => {
            delete item.status
          })
          form.configuration = JSON.stringify(form.apiConfiguration)
        } else {
          form.configuration = JSON.stringify(form.configuration)
        }
        if (
          this.formType === 'modify' &&
          this.originConfiguration !== form.configuration
        ) {
          if (repeat) {
            $confirm(
              i18n.t('datasource.repeat_datasource_msg') +
                '[' +
                repeatDsName.join(',') +
                '], ' +
                i18n.t('datasource.confirm_save'),
              () => {
                $confirm(i18n.t('datasource.edit_datasource_msg'), () => {
                  this.method(method, form)
                })
              }
            )
          } else {
            $confirm(i18n.t('datasource.edit_datasource_msg'), () => {
              this.method(method, form)
            })
          }
          return
        }
        if (repeat) {
          $confirm(
            i18n.t('datasource.repeat_datasource_msg') +
              '[' +
              repeatDsName.join(',') +
              '], ' +
              i18n.t('datasource.confirm_save'),
            () => {
              this.method(method, form)
            }
          )
        } else {
          this.method(method, form)
        }
      })
    },
    method(method, form) {
      this.formLoading = true
      method(form).then((res) => {
        this.$success(i18n.t('commons.save_success'))
        this.refreshType(form)
        this.backToList()
      }).finally(() => {
        this.formLoading = false
      })
    },
    getSchema() {
      this.$refs.dsForm.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          getSchema(data).then((res) => {
            this.schemas = res.data
            this.openMessageSuccess('commons.success')
          })
        } else {
          return false
        }
      })
    },
    validaDatasource() {
      if (!this.form.configuration.schema && this.form.type === 'oracle') {
        this.openMessageSuccess('datasource.please_choose_schema', 'error')
        return
      }
      if (
        this.form.type !== 'es' &&
        this.form.type !== 'api' &&
        this.form.configuration.port <= 0
      ) {
        this.openMessageSuccess('datasource.port_no_less_then_0', 'error')
        return
      }
      let status = null
      if (this.datasourceType.isPlugin) {
        status = this.$refs['pluginDsConfig'].callPluginInner({
          methodName: 'validate'
        })
      } else {
        this.$refs['dsConfig'].$refs['DsConfig'].validate((valid) => {
          status = valid
          if (!valid) {
            return
          }
        })
      }
      if (!status) {
        return
      }
      this.$refs.dsForm.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          if (data.type === 'api') {
            data.configuration = JSON.stringify(data.apiConfiguration)
          } else {
            data.configuration = JSON.stringify(data.configuration)
          }
          if (data.showModel === 'show' && !this.canEdit) {
            validateDsById(data.id).then((res) => {
              if (res.success) {
                this.openMessageSuccess('datasource.validate_success')
              } else {
                if (res.message.length < 2500) {
                  this.openMessageSuccess(res.message, 'error')
                } else {
                  this.openMessageSuccess(
                    res.message.substring(0, 2500) + '......',
                    'danger'
                  )
                }
              }
              this.refreshType(data)
            })
          } else {
            validateDs(data)
              .then((res) => {
                if (res.success) {
                  this.$success(i18n.t('datasource.validate_success'))
                } else {
                  if (data.type === 'api') {
                    this.form.apiConfiguration = res.data.apiConfiguration
                  }
                  if (res.message.length < 2500) {
                    this.openMessageSuccess(res.message, 'error')
                  } else {
                    this.openMessageSuccess(
                      res.message.substring(0, 2500) + '......',
                      'danger'
                    )
                  }
                }
              })
              .catch((res) => {
                this.openMessageSuccess(res.message, 'error')
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
            listDriverByType(this.datasourceType.type).then((res) => {
              this.driverList = []
              this.driverList.push({
                id: 'default',
                name: 'Default',
                driverClass: 'Default'
              })
              this.driverList = this.driverList.concat(res.data)
            })
          }
        }
      }
    },
    backToList() {
      this.$router.push('/datasource/index')
    },
    logOutTips() {
      const options = {
        title: 'role.tips',
        confirmButtonText: this.$t('commons.confirm'),
        content: 'system_parameter_setting.sure_to_exit',
        type: 'primary',
        cb: () => {
          this.backToList()
        }
      }
      this.handlerConfirm(options)
    },
    refreshType(form) {
      this.$emit('refresh-type', form)
    },
    next() {
      if (this.active === 1) {
        let hasRepeatName = false
        if (this.add_api_item) {
          this.form.apiConfiguration.forEach((item) => {
            if (item.name === this.apiItem.name) {
              hasRepeatName = true
            }
          })
        } else {
          const index = this.form.apiConfiguration.indexOf(this.apiItem)
          for (let i = 0; i < this.form.apiConfiguration.length; i++) {
            if (
              i !== index &&
              this.form.apiConfiguration[i].name === this.apiItem.name
            ) {
              hasRepeatName = true
            }
          }
        }
        if (hasRepeatName) {
          this.$message.error(i18n.t('datasource.has_repeat_name'))
          return
        }
        this.$refs.apiItem.validate((valid) => {
          if (valid) {
            const data = JSON.parse(JSON.stringify(this.apiItem))
            data.request = JSON.stringify(data.request)
            this.loading = true
            this.disabledNext = true
            checkApiDatasource(data)
              .then((res) => {
                this.loading = false
                this.disabledNext = false
                this.apiItem.status = 'Success'
                this.$success(i18n.t('commons.success'))
                this.active++
                this.apiItem.fields = res.data.fields
                this.$refs.plxTable.reloadData(res.data.datas)
              })
              .catch((res) => {
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
      this.form.apiConfiguration.splice(
        this.form.apiConfiguration.indexOf(item),
        1
      )
    },
    validateApi(item) {
      if (undefined) {
      } else {
        this.$refs.apiItem.validate((valid) => {
          if (valid) {
            const data = JSON.parse(JSON.stringify(this.apiItem))
            data.request = JSON.stringify(data.request)
            this.loading = true
            checkApiDatasource(data)
              .then((res) => {
                this.loading = false
                this.$success(i18n.t('commons.success'))
                this.apiItem.fields = res.data.fields
                this.$refs.plxTable.reloadData(res.data.datas)
              })
              .catch((res) => {
                this.loading = false
              })
          } else {
            return false
          }
        })
      }
    },
    handleClick(tab, event) {}
  }
}
</script>
<style lang="scss" scoped>
.de-ds-form {
  width: 100vw;
  height: 100vh;
  .de-ds-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 56px;
    padding: 12px 24px;
    box-shadow: 0px 2px 4px rgba(31, 35, 41, 0.08);
    .name {
      font-family: 'PingFang SC';
      font-style: normal;
      font-weight: 500;
      font-size: 16px;
      line-height: 24px;
      color: var(--deTextPrimary, #1f2329);
    }
    i {
      cursor: pointer;
    }
  }
  .de-ds-cont {
    display: flex;
    width: 100%;
    height: calc(100% - 56px);
    padding: 12px 24px 24px 24px;
    background: #f5f6f7;
    .de-ds-inner {
      width: 100%;
      height: 100;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow-y: auto;
    }
    .w600 {
      width: 600px;
      height: 100%;
      padding-top: 24px;
    }
  }
}
</style>
