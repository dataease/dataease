<template>
  <div style="height: 100%">
    <div class="de-ds-cont">
      <div class="de-ds-inner">
        <div class="w600">
          <el-form
            v-if="positionCheck('appMarket')"
            ref="attachParamsForm"
            :model="attachForm"
            :rules="attachRule"
            class="de-form-item"
            label-width="180px"
            label-position="right"
          >
            <div
              class="de-row-rules"
              style="margin: 0 0 16px 0;"
            >
              <span>{{ $t('datasource.basic_info') }}</span>
            </div>
            <el-form-item
              :label="$t('app_template.panel_position')"
              prop="panelGroupPid"
            >
              <treeselect
                v-model="attachForm.panelGroupPid"
                :disabled="baseInfoDisabledCheck(outerParams.panelPrivileges)"
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
              :label="$t('app_template.panel_name')"
              prop="panelName"
            >
              <el-input
                v-model="attachForm.panelName"
                :disabled="baseInfoDisabledCheck(outerParams.panelPrivileges)"
              />
            </el-form-item>
            <el-form-item
              :label="$t('app_template.dataset_group_position')"
              prop="datasetGroupPid"
            >
              <treeselect
                v-model="attachForm.datasetGroupPid"
                :disabled="baseInfoDisabledCheck(outerParams.datasetPrivileges)"
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
              :label="$t('app_template.dataset_group_name')"
              prop="datasetGroupName"
            >
              <el-input
                v-model="attachForm.datasetGroupName"
                :disabled="baseInfoDisabledCheck(outerParams.datasetPrivileges)"
              />
            </el-form-item>
          </el-form>
          <div class="de-row-rules">
            <span>{{
                positionCheck('appMarket') ? $t('app_template.datasource_info') : $t('datasource.basic_info')
              }}</span>
          </div>
          <el-form
            ref="historyDsForm"
            :model="attachForm"
            :rules="historyFormRule"
            size="small"
            :disabled="formType === 'modify'"
            class="de-form-item"
            label-width="180px"
            label-position="right"
          >
            <el-form-item
              v-if="positionCheck('appMarket')"
              :label="$t('app_template.datasource_from')"
              prop="datasourceFrom"
            >
              <el-radio-group
                v-model="attachForm.datasourceFrom"
                size="mini"
              >
                <el-radio :label="'new'">
                  {{ $t('app_template.datasource_new') }}
                </el-radio>
                <el-radio :label="'history'">
                  {{ $t('app_template.datasource_history') }}
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item
              v-if="positionCheck('appMarket') && attachForm.datasourceFrom === 'history'"
              :label="$t('app_template.datasource')"
              prop="datasourceHistoryId"
            >
              <el-select
                v-model="attachForm.datasourceHistoryId"
                filterable
                :placeholder="$t('dataset.pls_slc_data_source')"
                style="width: 100%"
              >
                <el-option
                  v-for="item in appMarketDatasource"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                  <span
                    v-if="
                      item.status !== 'Error' &&
                      item.status !== 'Warning'
                  "
                  >
                  <svg-icon
                    icon-class="db-de"
                  />
                </span>
                  <span v-if="item.status === 'Error'">
                  <svg-icon
                    icon-class="de-ds-error"
                    class="ds-icon-scene"
                  />
                </span>
                  <span v-if="item.status === 'Warning'">
                  <svg-icon
                    icon-class="de-ds-warning"
                    class="ds-icon-scene"
                  />
                </span>
                  <span>
                    {{ item.name }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>

          <el-form
            v-if="!positionCheck('appMarket') || (positionCheck('appMarket')&& attachForm.datasourceFrom === 'new')"
            ref="dsForm"
            :model="form"
            :rules="rule"
            size="small"
            :disabled="disabled"
            class="de-form-item"
            label-width="180px"
            label-position="right"
          >
            <el-form-item
              :label="$t('datasource.display_name')"
              prop="name"
            >
              <el-input
                v-model="form.name"
                autocomplete="off"
                :placeholder="$t('datasource.input_name')"
              />
            </el-form-item>
            <el-form-item
              :label="$t('datasource.type')"
              prop="type"
            >
              <el-select
                v-model="form.type"
                :placeholder="$t('datasource.please_choose_type')"
                style="width: 100%"
                disabled
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
              :label="$t('commons.description')"
              prop="desc"
            >
              <deTextarea
                v-model="form.desc"
                :maxlength="50"
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
              :disabled="disabled"
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
    <div
      v-if="positionCheck('appMarket') && attachForm.datasourceFrom === 'new'"
      class="de-ds-bottom"
    >
      <div
        class="apply"
        style="width: 100%"
      >
        <template v-if="canEdit">
          <deBtn
            secondary
            @click="closeDraw"
          >{{ $t('commons.cancel') }}
          </deBtn>
          <deBtn
            v-if="formType === 'add' || hasDataPermission('manage', params.privileges)"
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
                : (hasDataPermission('manage', outerParams.panelPrivileges)
                ||hasDataPermission('manage', outerParams.datasetPrivileges))
            "
            @click="validaDatasource"
          >{{ $t('commons.validate') }}
          </deBtn>
        </template>
      </div>
    </div>

    <div
      v-if="positionCheck('appMarket') && attachForm.datasourceFrom === 'history'"
      class="de-ds-bottom"
    >
      <div
        class="apply"
        style="width: 100%"
      >
        <template v-if="canEdit">
          <deBtn
            secondary
            @click="closeDraw"
          >{{ $t('commons.cancel') }}
          </deBtn>
          <deBtn
            v-if="
              formType === 'add'
                ? true
                : hasDataPermission('manage', params.privileges)
            "
            type="primary"
            @click="saveAppMarketHistory"
          >{{ $t('commons.save') }}
          </deBtn>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import {
  addDs,
  checkApiDatasource,
  editDs,
  getDatasourceDetail,
  getSchema,
  listDatasource,
  listDatasourceType,
  listDriverByType,
  validateDs,
  validateDsById
} from '@/api/system/datasource'
import { $confirm } from '@/utils/message'
import i18n from '@/lang/index'
import DsConfiguration from '@/views/system/datasource/DsConfiguration'
import PluginCom from '@/views/system/plugin/PluginCom'
import deTextarea from '@/components/deCustomCm/DeTextarea.vue'
import msgCfm from '@/components/msgCfm'
import { dsGroupTree } from '@/api/dataset/dataset'
import { appApply, appEdit, groupTree } from '@/api/panel/panel'
import { deepCopy } from '@/components/canvas/utils/utils'
import { hasDataPermission } from '@/utils/permission'
import { Base64 } from 'js-base64'

export default {
  name: 'DsForm',
  components: {
    DsConfiguration,
    PluginCom,
    deTextarea
  },
  mixins: [msgCfm],
  props: {
    referencePosition: {
      type: String,
      default: 'datasource'
    },
    outerParams: {
      type: Object,
      request: false
    },
    configFromTabs: {
      type: Object,
      default: () => {
      }
    },
    canEdit: {
      type: Boolean,
      default: false
    },
    formLoading: {
      type: Boolean,
      default: false
    },
    typeMap: {
      type: String,
      default: ''
    },
    formType: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      appMarketDatasource: [],
      appMarketEdit: true,
      attachRule: {
        panelName: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        datasetGroupName: [
          {
            required: true,
            min: 2,
            max: 25,
            message: i18n.t('datasource.input_limit_2_25', [2, 25]),
            trigger: 'blur'
          }
        ],
        datasetGroupPid: [{ required: true, message: i18n.t('chart.select_group'), trigger: 'blur' }],
        panelGroupPid: [{ required: true, message: i18n.t('chart.select_group'), trigger: 'blur' }]
      },
      panelGroupList: [],
      datasetGroupList: [],
      attachForm: {
        appTemplateId: '',
        panelGroupPid: null,
        panelName: '',
        datasetGroupPid: null,
        datasetGroupId: null,
        datasetGroupName: '',
        datasourceFrom: 'new',
        datasourceHistoryId: null
      },
      disabled: false,
      historyFormRule: {
        datasourceFrom: [
          {
            required: true,
            trigger: 'blur'
          }
        ],
        datasourceHistoryId: [{ required: true, message: i18n.t('dataset.pls_slc_data_source'), trigger: 'blur' }]
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
            min: 0,
            max: 50,
            message: i18n.t('datasource.input_limit', { num: '0~50' }),
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
      params: {}
    }
  },
  async created() {
    await this.datasourceTypes()
    this.queryTreeData()
    const params = this.configFromTabs?.id ? this.configFromTabs : this.$route.query
    let { id, showModel, type, name } = params
    this.params = params
    const appMarketCheck = this.positionCheck('appMarket')
    if (appMarketCheck) {
      this.appMarketEdit = this.outerParams.appMarketEdit === undefined ? true : this.outerParams.appMarketEdit
      id = this.outerParams.datasourceId
      showModel = this.outerParams.showModel
      type = this.outerParams.datasourceType
      name = this.outerParams.name
      this.attachForm.appTemplateId = this.outerParams.appTemplateId
      this.attachForm.panelGroupPid = this.outerParams.panelGroupPid
      this.attachForm.panelId = this.outerParams.panelId
      this.attachForm.panelName = this.outerParams.panelName
      this.attachForm.datasetGroupPid = this.outerParams.datasetGroupPid ? this.outerParams.datasetGroupPid : '0'
      this.attachForm.datasetGroupId = this.outerParams.datasetGroupId
      this.attachForm.datasetGroupName = this.outerParams.datasetGroupName
      this.attachForm.datasourceFrom = this.outerParams.datasourceFrom ? this.outerParams.datasourceFrom : 'new'
      this.attachForm.datasourceHistoryId = this.outerParams.datasourceHistoryId
      this.params = {
        id: this.outerParams.datasourceId,
        showModel: this.outerParams.showModel,
        type: this.outerParams.datasourceType,
        name: this.outerParams.name,
        privileges: this.outerParams.datasourcePrivileges
      }
      this.getPanelGroupTree()
      this.getDatasetGroupTree()
    }
    if (id) {
      await this.getDatasourceDetail(id, showModel)
      this.edit(this.params)
      this.changeType(true)
    } else {
      this.$emit('update:canEdit', true)
      if (type) {
        this.$emit('update:typeMap', name)
        this.setType()
        this.changeType()
      }
    }
    this.$emit('setParams', { ...this.params })
    this.$nextTick(() => {
      this.disabled = appMarketCheck ? !this.appMarketEdit : (Boolean(id) && showModel === 'show' && !this.canEdit)
      if (this.configFromTabs?.editor === 'editor') {
        this.$emit('editeTodisable', true)
      }
    })
  },
  methods: {
    editDatasource(type) {
      this.$emit('update:canEdit', type)
      this.disabled = !type
    },
    baseInfoDisabledCheck(privileges) {
      return !(this.formType === 'add' ? true : hasDataPermission('manage', privileges))
    },
    normalizer(node) {
      // 去掉children=null的属性
      if (node.children === null || node.children === 'null') {
        delete node.children
      }
    },
    getDatasourceOptions() {
      return listDatasource().then((response) => {
        this.options = response.data.filter((item) => item.type !== 'api')
      })
    },
    getDatasetGroupTree() {
      dsGroupTree({ nodeType: 'group', excludedId: this.attachForm.datasetGroupId }).then(res => {
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
        if (!this.attachForm.panelGroupPid && this.panelGroupList && this.panelGroupList.length > 0) {
          this.attachForm.panelGroupPid = this.panelGroupList[0].id
        }
      })
    },
    positionCheck(referencePosition) {
      return this.referencePosition === referencePosition
    },
    datasourceTypes() {
      return listDatasourceType().then((res) => {
        this.dsTypes = res.data || []
      })
    },
    getDatasourceDetail(id, showModel) {
      this.$emit('update:formLoading', true)
      return getDatasourceDetail(id).then((res) => {
        if (res.data.configuration) {
          res.data.configuration = Base64.decode(res.data.configuration)
        }
        if (res.data.apiConfigurationStr) {
          res.data.apiConfiguration = JSON.parse(Base64.decode(res.data.apiConfigurationStr))
        }
        this.params = { ...res.data, showModel }
        if (showModel === 'copy') {
          this.params.id = ''
        }
        this.$emit('setParams', { ...this.params })
      }).finally(() => {
        this.$emit('update:formLoading', false)
      })
    },
    queryTreeData() {
      listDatasource().then((res) => {
        this.tData = this.buildTree(res.data)
        if (this.positionCheck('appMarket') && res.data) {
          this.appMarketDatasource = res.data.filter((item) => item.type === this.outerParams.datasourceType)
        }
      })
    },
    buildTree(array) {
      const types = {}
      const newArr = []
      for (let index = 0; index < array.length; index++) {
        const element = array[index]
        if (element.configuration) {
          element.configuration = Base64.decode(element.configuration)
        }
        if (element.apiConfigurationStr) {
          element.apiConfiguration = Base64.decode(element.apiConfigurationStr)
        }
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
      this.$set(this.form, 'type', this.params.type)
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
      if (this.form.type === 'oracle') {
        this.$set(this.form.configuration, 'charset', 'Default')
        this.$set(this.form.configuration, 'targetCharset', 'Default')
        this.$set(this.form.configuration, 'connectionType', 'sid')
      }
    },
    edit(row) {
      this.$emit('update:formType', 'modify')
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
      this.$emit('update:canEdit', true)
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    saveAppMarketHistory() {
      this.$refs.historyDsForm.validate(valid => {
          if (!valid) {
            return false
          }
          const appApplyForm = {
            ...this.attachForm,
            ...this.historyDsForm
          }
          const method = this.formType === 'add' ? appApply : appEdit
          this.appApplyMethod(method, appApplyForm)
        }
      )
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
                case 'impala':
                  if (
                    configuration.host === this.form.configuration.host &&
                    configuration.dataBase ===
                    this.form.configuration.dataBase &&
                    configuration.port === this.form.configuration.port
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
                    configuration.host === this.form.configuration.host &&
                    configuration.dataBase ===
                    this.form.configuration.dataBase &&
                    configuration.port === this.form.configuration.port &&
                    configuration.schema === this.form.configuration.schema
                  ) {
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
                if (configuration.schema !== null) {
                  if (
                    configuration.schema === this.form.configuration.schema &&
                    configuration.host === this.form.configuration.host &&
                    configuration.dataBase ===
                    this.form.configuration.dataBase &&
                    configuration.port === this.form.configuration.port
                  ) {
                    repeat = true
                    repeatDsName.push(child.name)
                  }
                } else {
                  if (
                    configuration.host === this.form.configuration.host &&
                    configuration.dataBase ===
                    this.form.configuration.dataBase &&
                    configuration.port === this.form.configuration.port
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
      this.$refs.dsForm.validate()
      if (!status) {
        return
      }
      if (this.positionCheck('appMarket')) {
        this.$refs.attachParamsForm.validate(valid => {
            if (!valid) {
              return false
            }
          }
        )
      }
      this.$refs.dsForm.validate((valid) => {
        if (!valid) {
          return false
        }
        let method = this.formType === 'add' ? addDs : editDs

        const form = JSON.parse(JSON.stringify(this.form))
        if (form.type === 'api') {
          if (this.form.apiConfiguration.length < 1) {
            this.$message.error(i18n.t('datasource.api_table_not_empty'))
            return
          }
          form.apiConfiguration.forEach((item) => {
            delete item.status
          })
          form.configuration = Base64.encode(JSON.stringify(form.apiConfiguration))
          form.apiConfiguration = []
        } else {
          form.configuration = Base64.encode(JSON.stringify(form.configuration))
        }
        form.configurationEncryption = true
        const isAppMarket = this.positionCheck('appMarket')
        let appApplyForm
        if (isAppMarket) {
          if (typeof form.desc === 'object') {
            form.desc = ''
          }
          appApplyForm = {
            ...this.attachForm,
            ...this.historyDsForm,
            datasourceList: [deepCopy(form)]
          }
          method = this.formType === 'add' ? appApply : appEdit
        }
        if (
          this.formType === 'modify' && this.$route.query.showModel !== 'copy' &&
          this.originConfiguration !== form.configuration
        ) {
          if (repeat && !isAppMarket) {
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
              isAppMarket ? this.appApplyMethod(method, appApplyForm) : this.method(method, form)
            })
          }
          return
        }
        if (repeat && !isAppMarket) {
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
          isAppMarket ? this.appApplyMethod(method, appApplyForm) : this.method(method, form)
        }
      })
    },
    appApplyMethod(method, form) {
      this.$emit('update:formLoading', true)
      method(form).then((res) => {
        this.$success(i18n.t('commons.save_success'))
        if (this.formType === 'add') {
          this.$router.push({ name: 'panel', params: res.data })
        } else {
          this.closeDraw()
        }
      }).finally(() => {
        this.$emit('update:formLoading', false)
      })
    },
    method(method, form) {
      this.$emit('update:formLoading', true)
      method(form).then((res) => {
        this.$success(i18n.t('commons.save_success'))
        this.refreshType(form)
        this.backToList()
      }).finally(() => {
        this.$emit('update:formLoading', false)
      })
    },
    getSchema() {
      this.$refs.dsForm.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = Base64.encode(JSON.stringify(data.configuration))
          data.configurationEncryption = true
          getSchema(data).then((res) => {
            this.schemas = res.data
            this.openMessageSuccess('commons.success')
          })
        } else {
          return false
        }
      })
    },
    reloadStatus(statusMap = {}) {
      this.form.apiConfiguration.forEach(ele => {
         ele.status = statusMap[ele.name] || ele.status
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
      this.$refs.dsForm.validate()
      if (!status) {
        return
      }
      if (this.positionCheck('appMarket')) {
        this.$refs.attachParamsForm.validate(valid => {
            if (!valid) {
              return false
            }
          }
        )
      }
      this.$refs.dsForm.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          if (data.type === 'api') {
            data.configuration = Base64.encode(JSON.stringify(data.apiConfiguration))
            data.apiConfiguration = []
          } else {
            data.configuration = Base64.encode(JSON.stringify(data.configuration))
          }
          data.configurationEncryption = true
          if (data.showModel === 'show' && !this.canEdit) {
            validateDsById(data.id).then((res) => {
              if (data.type === 'api') {
                this.reloadStatus(JSON.parse(res.data?.status || '{}'))
              }
              if (res.success) {
                this.openMessageSuccess('datasource.validate_success')
              } else {
                if (res.message.length < 2500) {
                  this.openMessageSuccess(res.message, 'error')
                } else {
                  this.openMessageSuccess(
                    res.message.substring(0, 2500) + '......',
                    'error'
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
                      'error'
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
      this.$emit('editeTodisable', false)
    },

    closeDraw() {
      this.$emit('closeDraw')
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
                this.$refs.plxTable.reloadData(res.data.data)
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
              this.$refs.plxTable.reloadData(res.data.data)
            })
            .catch((res) => {
              this.loading = false
            })
        } else {
          return false
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.de-ds-cont {
  display: flex;
  width: 100%;
  height: 100%;

  .de-ds-inner {
    padding: 12px 24px 24px 24px;
    width: 100%;
    height: 100%;
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

.de-ds-bottom {
  display: flex;
  text-align: right;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 12px 24px;
  box-shadow: 2px 2px 4px rgba(31, 35, 41, 0.08);

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
</style>
