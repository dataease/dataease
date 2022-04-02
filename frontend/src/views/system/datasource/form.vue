<template>
  <layout-content :header="formType=='add' ? $t('datasource.create') : $t('datasource.modify')">
    <template v-slot:header>
      <el-icon name="back" class="back-button" @click.native="backToList"/>
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
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="desc">
          <el-input v-model="form.desc" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('datasource.type')" prop="type">
          <el-select
            v-model="form.type"
            :placeholder="$t('datasource.please_choose_type')"
            class="select-width"
            :disabled="formType=='modify' || (formType==='add' && params && !!params.type)"
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

        <el-form-item v-if="form.type == 'api'" :label="$t('datasource.data_table')">
          <el-col>
            <el-button size="mini" icon="el-icon-plus" type="text" @click="addApiItem(undefined)"/>
            <el-table :data="form.apiConfiguration" class="my_table" max-height="300" height="300">
              <el-table-column prop="name" :label="$t('datasource.data_table_name')" width="150"
                               show-overflow-tooltip></el-table-column>
              <el-table-column prop="method" :label="$t('datasource.method')" width="150"
                               show-overflow-tooltip></el-table-column>
              <el-table-column prop="url" :label="$t('datasource.url')" width="150"
                               show-overflow-tooltip></el-table-column>
              <el-table-column prop="status" :label="$t('commons.status')" width="150">
                <template slot-scope="scope">
                  <span v-if="scope.row.status === 'Success'" style="color: green">
                    {{ $t('datasource.valid') }}
                  </span>
                  <span v-if="scope.row.status === 'Error'" style="color: red">
                    {{ $t('datasource.invalid') }}
                  </span>
                </template>
              </el-table-column>


              <el-table-column :label="$t('dataset.operate')">
                <template slot-scope="scope" style="float: right">
                  <el-button size="mini" type="primary" icon="el-icon-edit" circle @click="addApiItem(scope.row)"/>
                  <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="deleteItem(scope.row)"/>
                </template>
              </el-table-column>
            </el-table>
          </el-col>
        </el-form-item>

        <el-dialog :title="api_table_title" :visible="edit_api_item" :before-close="closeEditItem" width="60%"
                   class="dialog-css" append-to-body>
          <el-steps :active="active" align-center>
            <el-step title="步骤 1"></el-step>
            <el-step title="步骤 2"></el-step>
          </el-steps>

          <el-row v-show="active === 1">
            <el-form ref="apiItem" size="small" :model="apiItem" label-width="100px" :rules="rule">
              <p class="tip">{{ $t('datasource.base_info') }} </p>

              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input v-model="apiItem.name" autocomplete="off"/>
              </el-form-item>

              <el-form-item :label="$t('datasource.request')" prop="url">
                <el-input :placeholder="$t('datasource.path_all_info')" v-model="apiItem.url" class="ms-http-input"
                          size="small">
                  <el-select v-model="apiItem.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
                  </el-select>
                </el-input>
              </el-form-item>

              <div v-loading="loading">
                <p class="tip">{{ $t('datasource.req_param') }} </p>
                <!-- HTTP 请求参数 -->
                <el-form-item>
                  <api-http-request-form :headers="apiItem.request.headers" :request="apiItem.request"
                                         :response="responseData"/>
                </el-form-item>
              </div>

              <el-form-item :label="$t('datasource.data_path')" prop="dataPath">
                <el-input :placeholder="$t('datasource.data_path_desc')" v-model="apiItem.dataPath" autocomplete="off"/>
              </el-form-item>
              <!--              <el-button style="margin-top: 12px;" @click="validateApi(undefined)" v-show="active === 1">{{ $t('commons.validate') }}</el-button>-->
            </el-form>
          </el-row>
          <el-row v-show="active === 2">
            <el-tabs v-model="api_step2_active_name" @tab-click="handleClick">
              <el-tab-pane :label="$t('dataset.data_preview')" name="first">
                <ux-grid ref="plxTable" size="mini" style="width: 100%;" :height="height"
                         :checkbox-config="{highlight: true}" :width-resize="true">
                  <ux-table-column v-for="field in apiItem.fields" :key="field.originName" min-width="200px"
                                   :field="field.originName" :resizable="true">
                    <template slot="header">
                      <svg-icon v-if="field.deExtractType === 0" icon-class="field_text" class="field-icon-text"/>
                      <svg-icon v-if="field.deExtractType === 1" icon-class="field_time" class="field-icon-time"/>
                      <svg-icon v-if="field.deExtractType === 2 || field.deExtractType === 3" icon-class="field_value"
                                class="field-icon-value"/>
                      <svg-icon v-if="field.deExtractType === 5" icon-class="field_location"
                                class="field-icon-location"/>
                      <span>{{ field.name }}</span>
                    </template>
                  </ux-table-column>
                </ux-grid>
              </el-tab-pane>
              <!--              暂时屏蔽掉字段管理-->
              <!--              <el-tab-pane label="字段管理" name="second">-->
              <!--                <el-table :data="apiItem.fields" size="mini">-->
              <!--                  <el-table-column property="name" :label="$t('dataset.field_name')" width="180">-->
              <!--                    <template slot-scope="scope">-->
              <!--                      <el-input v-model="scope.row.name" size="mini"/>-->
              <!--                    </template>-->
              <!--                  </el-table-column>-->

              <!--                  <el-table-column property="originName" :label="$t('dataset.field_origin_name')" width="100">-->
              <!--                    <template slot-scope="scope">-->
              <!--                      <span v-if="scope.row.extField === 0" :title="scope.row.originName" class="field-class" style="width: 100%;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;">-->
              <!--                        <span style="font-size: 12px;">{{ scope.row.originName }}</span>-->
              <!--                      </span>-->
              <!--                    </template>-->
              <!--                  </el-table-column>-->

              <!--                  <el-table-column property="deExtractType" :label="$t('dataset.field_type')" width="140">-->
              <!--                    <template slot-scope="scope">-->
              <!--                      <el-select v-model="scope.row.deExtractType" size="mini" style="display: inline-block;width: 26px;">-->
              <!--                        <el-option v-for="item in fieldTypes" :key="item.value" :label="item.label" :value="item.value">-->
              <!--                        <span style="float: left">-->
              <!--                          <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />-->
              <!--                          <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />-->
              <!--                          <svg-icon v-if="item.value === 2 || item.value === 3" icon-class="field_value" class="field-icon-value" />-->
              <!--                          <svg-icon v-if="item.value === 5" icon-class="field_location" class="field-icon-location" />-->
              <!--                        </span>-->
              <!--                          <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>-->
              <!--                        </el-option>-->
              <!--                      </el-select>-->
              <!--                      <span style="margin-left: 8px;">-->
              <!--                      <span v-if="scope.row.deExtractType === 0">-->
              <!--                        <svg-icon icon-class="field_text" class="field-icon-text" />-->
              <!--                        <span class="field-class">{{ $t('dataset.text') }}</span>-->
              <!--                      </span>-->
              <!--                      <span v-if="scope.row.deExtractType === 1">-->
              <!--                        <svg-icon v-if="scope.row.deExtractType === 1" icon-class="field_time" class="field-icon-time" />-->
              <!--                        <span class="field-class">{{ $t('dataset.time') }}</span>-->
              <!--                      </span>-->
              <!--                      <span v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3">-->
              <!--                        <svg-icon v-if="scope.row.deExtractType === 2 || scope.row.deExtractType === 3" icon-class="field_value" class="field-icon-value" />-->
              <!--                        <span v-if="scope.row.deExtractType === 2" class="field-class">{{ $t('dataset.value') }}</span>-->
              <!--                        <span v-if="scope.row.deExtractType === 3" class="field-class">{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>-->
              <!--                      </span>-->
              <!--                      <span v-if="scope.row.deExtractType === 5">-->
              <!--                        <svg-icon v-if="scope.row.deExtractType === 5" icon-class="field_location" class="field-icon-location" />-->
              <!--                        <span class="field-class">{{ $t('dataset.location') }}</span>-->
              <!--                      </span>-->
              <!--                    </span>-->
              <!--                    </template>-->
              <!--                  </el-table-column>-->
              <!--                </el-table>-->
              <!--              </el-tab-pane>-->
            </el-tabs>
          </el-row>
          <div slot="footer" class="dialog-footer">
            <el-button @click="next" :disabled="disabledNext" v-show="active === 1">{{ $t('fu.steps.next') }}</el-button>
            <el-button @click="before" v-show="active === 2">{{ $t('fu.steps.prev') }}</el-button>
            <el-button @click="saveItem" v-show="active === 2">{{ $t('commons.save') }}</el-button>
          </div>

        </el-dialog>


        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api'"
                      :label="$t('datasource.host')" prop="configuration.host">
          <el-input v-model="form.configuration.host" autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='es' && form.type !== 'api'"
                      :label="$t('datasource.datasource_url')" prop="configuration.url">
          <el-input v-model="form.configuration.url" :placeholder="$t('datasource.please_input_datasource_url')"
                    autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api'"
                      :label="$t('datasource.data_base')" prop="configuration.dataBase">
          <el-input v-model="form.configuration.dataBase" autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.type=='oracle' && form.type !== 'api'" :label="$t('datasource.oracle_connection_type')"
                      prop="configuration.connectionType">
          <el-radio v-model="form.configuration.connectionType" label="sid">{{ $t('datasource.oracle_sid') }}</el-radio>
          <el-radio v-model="form.configuration.connectionType" label="serviceName">
            {{ $t('datasource.oracle_service_name') }}
          </el-radio>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api'"
                      :label="$t('datasource.user_name')">
          <el-input v-model="form.configuration.username" autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api'"
                      :label="$t('datasource.password')">
          <el-input v-model="form.configuration.password" autocomplete="off" show-password/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='es' && form.type !== 'api'"
                      :label="$t('datasource.user_name')">
          <el-input v-model="form.configuration.esUsername" autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='es' && form.type !== 'api'"
                      :label="$t('datasource.password')">
          <el-input v-model="form.configuration.esPassword" autocomplete="off" show-password/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type!=='oracle' && form.type !== 'api'"
                      :label="$t('datasource.extra_params')">
          <el-input v-model="form.configuration.extraParams" autocomplete="off"/>
        </el-form-item>

        <el-form-item v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api'"
                      :label="$t('datasource.port')" prop="configuration.port">
          <el-input v-model="form.configuration.port" autocomplete="off" type="number" min="0"/>
        </el-form-item>

        <el-form-item
          v-if="form.type=='oracle' || form.type=='sqlServer' || form.type=='pg' || form.type=='redshift' || form.type=='db2'">
          <el-button icon="el-icon-plus" size="mini" @click="getSchema()">{{ $t('datasource.get_schema') }}</el-button>
        </el-form-item>

        <el-form-item
          v-if="form.type=='oracle' || form.type=='sqlServer' || form.type=='pg' || form.type=='redshift' || form.type=='db2'"
          :label="$t('datasource.schema')">
          <el-select v-model="form.configuration.schema" filterable :placeholder="$t('datasource.please_choose_schema')"
                     class="select-width">
            <el-option v-for="item in schemas" :key="item" :label="item" :value="item"/>
          </el-select>
        </el-form-item>

        <el-collapse v-if="form.configuration.dataSourceType=='jdbc' && form.type !== 'api' && form.type !== 'mongo'">
          <el-collapse-item :title="$t('datasource.priority')" name="1">
            <el-form-item :label="$t('datasource.initial_pool_size')" prop="configuration.initialPoolSize">
              <el-input v-model="form.configuration.initialPoolSize" autocomplete="off" type="number" min="0"
                        size="small"/>
            </el-form-item>
            <el-form-item :label="$t('datasource.min_pool_size')" prop="configuration.minPoolSize">
              <el-input v-model="form.configuration.minPoolSize" autocomplete="off" type="number" min="0"/>
            </el-form-item>
            <el-form-item :label="$t('datasource.max_pool_size')" prop="configuration.maxPoolSize">
              <el-input v-model="form.configuration.maxPoolSize" autocomplete="off" type="number" min="0"/>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </el-form>
      <div v-if="canEdit" slot="footer" class="dialog-footer">
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
                   @click="validaDatasource">{{ $t('commons.validate') }}
        </el-button>
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" type="primary"
                   @click="save">{{ $t('commons.save') }}
        </el-button>
      </div>
      <div v-else slot="footer" class="dialog-footer">
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)"
                   @click="validaDatasource">{{ $t('commons.validate') }}
        </el-button>
        <el-button v-if="formType==='add'?true: hasDataPermission('manage',params.privileges)" type="primary"
                   @click="changeEdit">{{ $t('commons.edit') }}
        </el-button>
      </div>
    </div>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import {addDs, editDs, getSchema, validateDs, validateDsById, checkApiDatasource} from '@/api/system/datasource'
import {$confirm} from '@/utils/message'
import i18n from '@/lang/index'
import ApiHttpRequestForm from '@/views/system/datasource/ApiHttpRequestForm'

export default {
  name: 'DsForm',
  components: {
    LayoutContent,
    ApiHttpRequestForm
  },
  props: {
    params: {
      type: Object,
      default: null
    },
    tData: {
      type: Array,
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
        },
        apiConfiguration: []
      },
      rule: {
        name: [{required: true, message: i18n.t('datasource.input_name'), trigger: 'blur'},
          {min: 2, max: 25, message: i18n.t('datasource.input_limit_2_25', [2, 25]), trigger: 'blur'}],
        desc: [{min: 2, max: 50, message: i18n.t('datasource.input_limit_2_50'), trigger: 'blur'}],
        type: [{required: true, message: i18n.t('datasource.please_choose_type'), trigger: 'blur'}],
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
        'configuration.host': [{required: true, message: i18n.t('datasource.please_input_host'), trigger: 'blur'}],
        'configuration.url': [{required: true, message: i18n.t('datasource.please_input_url'), trigger: 'blur'}],
        'configuration.port': [{required: true, message: i18n.t('datasource.please_input_port'), trigger: 'blur'}],
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
        'url': [{required: true, message: i18n.t('datasource.please_input_url'), trigger: 'blur'}],
        'dataPath': [{required: true, message: i18n.t('datasource.please_input_dataPath'), trigger: 'blur'}]
      },
      allTypes: [
        {name: 'mysql', label: 'MySQL', type: 'jdbc', extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'},
        {name: 'TiDB', label: 'TiDB', type: 'jdbc', extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'},
        {name: 'hive', label: 'Apache Hive', type: 'jdbc', extraParams: ''},
        {name: 'impala', label: 'Apache Impala', type: 'jdbc', extraParams: 'AuthMech=0'},
        {name: 'oracle', label: 'Oracle', type: 'jdbc'},
        {name: 'sqlServer', label: 'SQL Server', type: 'jdbc', extraParams: ''},
        {name: 'pg', label: 'PostgreSQL', type: 'jdbc', extraParams: ''},
        {name: 'es', label: 'Elasticsearch', type: 'es'},
        {name: 'mariadb', label: 'MariaDB', type: 'jdbc', extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'},
        {name: 'StarRocks', label: 'StarRocks', type: 'jdbc', extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'},
        {name: 'ds_doris', label: 'Doris', type: 'jdbc', extraParams: 'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'},
        {name: 'ck', label: 'ClickHouse', type: 'jdbc', extraParams: ''},
        {name: 'redshift', label: 'AWS Redshift', type: 'jdbc'},
        {name: 'mongo', label: 'MongoDB', type: 'jdbc', extraParams: 'rebuildschema=true&authSource=admin'},
        {name: 'db2', label: 'Db2', type: 'jdbc', extraParams: ''},
        {name: 'api', label: 'API', type: 'api', extraParams: ''}
      ],
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
            "type": "",
            "raw": "",
            "kvs": []
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
            "type": "",
            "raw": "",
            "kvs": []
          },
          authManager: {}
        },
        fields: []
      },
      reqOptions: [{id: 'GET', label: 'GET'}, {id: 'POST', label: 'POST'}],
      loading: false,
      responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
      api_table_title: '',
      api_step2_active_name: 'first',
      fieldTypes: [
        {label: this.$t('dataset.text'), value: 0},
        {label: this.$t('dataset.time'), value: 1},
        {label: this.$t('dataset.value'), value: 2},
        {label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3},
        {label: this.$t('dataset.location'), value: 5}
      ],
      height: 500,
      disabledNext: false
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
      this.form = JSON.parse(JSON.stringify(row))

      this.originConfiguration = this.form.configuration
      if (row.type === 'api') {
      } else {
        this.form.configuration = JSON.parse(this.form.configuration)
      }
    },
    reset() {
      this.$refs.dsForm.resetFields()
    },
    save() {
      if (!this.form.configuration.schema && (this.form.type === 'oracle' || this.form.type === 'sqlServer' || this.form.type === 'pg' || this.form.type === 'redshift' || this.form.type === 'db2')) {
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
      let repeat = false
      let repeatDsName = []
      this.tData.forEach(item => {
        if (item.id === this.form.type) {
          item.children.forEach(child => {
            if (this.formType === 'modify' && child.id === this.form.id) {
              return
            }
            let configuration = JSON.parse(child.configuration)
            if(!configuration){
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
                if (configuration.host == this.form.configuration.host && configuration.dataBase == this.form.configuration.dataBase && configuration.port == this.form.configuration.port) {
                  repeat = true
                  repeatDsName.push(child.name)
                }
                break
              case 'pg':
              case 'sqlServer':
              case 'redshift':
              case 'oracle':
              case 'db2':
                if (configuration.host == this.form.configuration.host && configuration.dataBase == this.form.configuration.dataBase && configuration.port == this.form.configuration.port && configuration.schema == this.form.configuration.schema) {
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
      if (this.form.configuration.dataSourceType === 'jdbc' && this.form.configuration.port <= 0) {
        this.$message.error(i18n.t('datasource.port_no_less_then_0'))
        return
      }
      this.$refs.dsForm.validate(valid => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          if (data.type === 'api') {
            data.configuration = JSON.stringify(data.apiConfiguration)
          } else {
            data.configuration = JSON.stringify(data.configuration)
          }
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
          let index = this.form.apiConfiguration.indexOf(this.apiItem)
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
            checkApiDatasource(data).then(res => {
              this.loading = false
              this.disabledNext = false
              this.apiItem.status = 'Success'
              this.$success(i18n.t('commons.success'))
              this.active++
              this.apiItem.fields = res.data.fields
              this.$refs.plxTable.reloadData(res.data.datas)
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
      if (undefined) {

      } else {
        this.$refs.apiItem.validate(valid => {
          if (valid) {
            const data = JSON.parse(JSON.stringify(this.apiItem))
            data.request = JSON.stringify(data.request)
            this.loading = true
            checkApiDatasource(data).then(res => {
              this.loading = false
              this.$success(i18n.t('commons.success'))
              this.apiItem.fields = res.data.fields
              this.$refs.plxTable.reloadData(res.data.datas)
            }).catch(res => {
              this.loading = false
            })
          } else {
            return false
          }
        })
      }
    },
    handleClick(tab, event) {
    }
  }
}
</script>
<style scoped>
.el-input {
  width: 300px;
}

.el-select {
  width: 300px;
}

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

.el-select >>> input {
  padding-right: 10px;
}

.el-select >>> .el-input__suffix {
  right: 0;
}

.dialog-css >>> .el-dialog__header {
  padding: 10px 20px 0px;
}

.dialog-css >>> .el-dialog__body {
  padding: 10px 20px 10px;
}

.dialog-footer >>> .el-dialog__footer {
  padding: 10px 10px 10px;
}

</style>
