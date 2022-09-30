<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <el-form
      ref="DsConfig"
      :model="form"
      :rules="rule"
      size="small"
      :disabled="disabled"
      label-width="180px"
      label-position="right"
    >
      <template v-if="form.type == 'api'">
        <div class="de-row-rules flex-space">
          <span>{{ $t('datasource.data_table') }}</span>
           <el-button style="min-width: 80px;
    font-size: 14px;padding: 8px 15px;" icon="el-icon-plus" size="small" @click="() => addApiItem()" type="primary">{{ $t('commons.add') }}</el-button>
        </div>
        <el-empty
          v-if="!form.apiConfiguration.length"
          :image="noneImg"
          :description="$t('datasource.no_data_table')"
        />
        <template v-else>
          <div
            v-for="api in form.apiConfiguration"
            :key="api.id"
            :style="{ cursor: disabled ? 'not-allowed': 'pointer'}"
            class="api-card"
            @click="addApiItem(api)"
          >
            <el-row>
              <el-col style="display: flex" :span="19">
                <span class="name">{{ api.name }}</span>
                <span
                  v-if="api.status === 'Error'"
                  class="de-tag"
                  style="color: #646a73; background: rgba(31, 35, 41, 0.1)"
                >{{ $t('datasource.invalid') }}</span>
                <span
                  v-if="api.status === 'Success'"
                  class="de-tag"
                  style="color: green; background: rgba(52, 199, 36, 0.2)"
                >{{ $t('datasource.valid') }}</span>
              </el-col>
              <el-col style="text-align: right" :span="5">
                <svg-icon
                  icon-class="de-copy"
                  class="de-copy-icon"
                  @click.stop="copyItem(api)"
                />

                <span @click.stop>
                  <el-popover
                    :ref="`apiTable${api.name}`"
                    placement="top"
                    width="200"
                    popper-class="api-table-delete"
                    trigger="click"
                  >
                    <i class="el-icon-warning" />
                    <div class="tips">
                      {{ $t('datasource.delete_this_item') }}
                    </div>
                    <div class="foot">
                      <deBtn class="small" secondary @click="cancelItem(api)">{{
                        $t('fu.search_bar.cancel')
                      }}</deBtn>
                      <deBtn
                        class="small"
                        type="primary"
                        @click="deleteItem(api)"
                      >{{ $t('fu.search_bar.ok') }}</deBtn>
                    </div>
                    <svg-icon
                      slot="reference"
                      icon-class="de-delete"
                      class="de-delete-icon"
                    />
                  </el-popover>
                </span>
              </el-col>
            </el-row>
            <div class="req-title">
              <span>{{ $t('datasource.method') }}</span>
              <span>{{ $t('datasource.url') }}</span>
            </div>
            <div class="req-value">
              <span>{{ api.method }}</span>
              <span>{{ api.url }}</span>
            </div>
          </div>
        </template>
      </template>

      <el-form-item
        v-if="form.type !== 'es' && form.type !== 'api'"
        :label="$t('datasource.host')"
        prop="configuration.host"
      >
        <el-input
          v-model="form.configuration.host"
          :placeholder="$t('datasource._ip_address')"
          autocomplete="off"
        />
      </el-form-item>

      <el-form-item
        v-if="form.type == 'es'"
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
        v-if="form.type !== 'es' && form.type !== 'api'"
        :label="$t('datasource.data_base')"
        prop="configuration.dataBase"
      >
        <el-input
          v-model="form.configuration.dataBase"
          :placeholder="$t('datasource.please_input_data_base')"
          autocomplete="off"
        />
      </el-form-item>

      <el-form-item
        v-if="form.type == 'oracle' && form.type !== 'api'"
        :label="$t('datasource.connection_mode')"
        prop="configuration.connectionType"
      >
        <el-radio
          v-model="form.configuration.connectionType"
          label="sid"
        >{{ $t('datasource.oracle_sid') }}
        </el-radio>
        <el-radio
          v-model="form.configuration.connectionType"
          label="serviceName"
        >
          {{ $t('datasource.oracle_service_name') }}
        </el-radio>
      </el-form-item>

      <el-form-item
        v-if="form.type == 'hive'"
        :label="$t('datasource.auth_method')"
      >
        <el-select v-model="form.configuration.authMethod" class="de-select">
          <el-option
            v-for="item in authMethodList"
            :key="item.id"
            :label="item.label"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="
          form.type === 'hive' && form.configuration.authMethod === 'kerberos'
        "
        :label="$t('datasource.client_principal')"
      >
        <el-input v-model="form.configuration.username" autocomplete="off" />
      </el-form-item>

      <el-form-item
        v-if="
          form.type === 'hive' && form.configuration.authMethod === 'kerberos'
        "
        :label="$t('datasource.keytab_Key_path')"
      >
        <dePwd v-model="form.configuration.password" />
        <p>
          {{ $t('datasource.kerbers_info') }}
        </p>
      </el-form-item>

      <span
        v-if="
          form.type === 'hive' && form.configuration.authMethod === 'kerberos'
        "
      />

      <el-form-item
        v-if="
          form.type !== 'es' &&
            form.type !== 'api' &&
            form.configuration.authMethod !== 'kerberos'
        "
        :label="$t('datasource.user_name')"
      >
        <el-input
          v-model="form.configuration.username"
          :placeholder="$t('components.one_user_name')"
          autocomplete="off"
        />
      </el-form-item>

      <el-form-item
        v-if="
          form.type !== 'es' &&
            form.type !== 'api' &&
            form.configuration.authMethod !== 'kerberos'
        "
        :label="$t('datasource.password')"
      >
        <dePwd
          v-model="form.configuration.password"
          :placeholder="$t('components.input_a_password')"
        />
      </el-form-item>

      <el-form-item
        v-if="form.type === 'es'"
        :label="$t('datasource.user_name')"
      >
        <el-input
          v-model="form.configuration.esUsername"
          :placeholder="$t('components.one_user_name')"
          autocomplete="off"
        />
      </el-form-item>

      <el-form-item
        v-if="form.type === 'es'"
        :label="$t('datasource.password')"
      >
        <dePwd
          v-model="form.configuration.esPassword"
          :placeholder="$t('components.input_a_password')"
        />
      </el-form-item>

      <el-form-item
        v-if="
          form.type !== 'es' && form.type !== 'oracle' && form.type !== 'api'
        "
        :label="$t('datasource.extra_params')"
      >
        <el-input v-model="form.configuration.extraParams" :placeholder="$t('fu.search_bar.please_input') + $t('datasource.extra_params')" autocomplete="off" />
      </el-form-item>

      <el-form-item
        v-if="form.type !== 'es' && form.type !== 'api'"
        :label="$t('datasource.port')"
        prop="configuration.port"
      >
        <el-input
          v-model="form.configuration.port"
          autocomplete="off"
          type="number"
          :placeholder="$t('components.enter_the_port')"
          min="0"
        />
      </el-form-item>

      <el-form-item
        v-if="
          ['oracle', 'sqlServer', 'pg', 'redshift', 'db2'].includes(form.type)
        "
        class="schema-label"
      >
        <template slot="label">
          {{ $t('datasource.schema') }}
          <el-button
            type="text"
            icon="el-icon-plus"
            size="small"
            @click="getSchema()"
          >{{ $t('datasource.get_schema') }}
          </el-button>
        </template>
        <el-select
          v-model="form.configuration.schema"
          filterable
          :placeholder="$t('fu.search_bar.please_select')"
          class="de-select"
        >
          <el-option
            v-for="item in schemas"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="form.type == 'oracle'"
        :label="$t('datasource.charset')"
      >
        <el-select
          v-model="form.configuration.charset"
          filterable
          :placeholder="$t('datasource.please_choose_charset')"
          class="de-select"
        >
          <el-option
            v-for="item in datasourceType.charset"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>

      <el-form-item
        v-if="form.type == 'oracle'"
        :label="$t('datasource.targetCharset')"
      >
        <el-select
          v-model="form.configuration.targetCharset"
          filterable
          :placeholder="$t('datasource.please_choose_targetCharset')"
          class="de-select"
        >
          <el-option
            v-for="item in datasourceType.targetCharset"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select>
      </el-form-item>

      <span
        v-if="!['es', 'api', 'mongo'].includes(form.type)"
        class="de-expand de-mar0"
        @click="showPriority = !showPriority"
      >{{ $t('datasource.priority')
       }}<i v-if="showPriority" class="el-icon-arrow-up" />
        <i v-else class="el-icon-arrow-down" /></span>

      <template v-if="showPriority">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.initial_pool_size')"
              prop="configuration.initialPoolSize"
            >
              <el-input-number
                v-model="form.configuration.initialPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                :min="0"
                size="small"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.min_pool_size')"
              prop="configuration.minPoolSize"
            >
              <el-input-number
                v-model="form.configuration.minPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                :min="0"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item
              :label="$t('datasource.max_pool_size')"
              prop="configuration.maxPoolSize"
            >
              <el-input-number
                v-model="form.configuration.maxPoolSize"
                controls-position="right"
                autocomplete="off"
                type="number"
                :min="0"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item
              v-if="datasourceType.isJdbc"
              :label="$t('datasource.query_timeout')"
              prop="configuration.queryTimeout"
            >
              <el-input
                v-model="form.configuration.queryTimeout"
                autocomplete="off"
                type="number"
                :min="0"
              >
                <template slot="append">{{ $t('panel.second') }}</template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </template>
    </el-form>
    <el-drawer
      v-closePress
      :title="api_table_title"
      :visible.sync="edit_api_item"
      custom-class="api-datasource-drawer"
      size="840px"
      :before-close="closeEditItem"
      direction="rtl"
    >
      <el-steps :active="active" align-center :space="144">
        <el-step
          v-if="active === 1"
          :title="$t('datasource.api_step_1')"
        />
        <el-step
          v-else
          icon="el-icon-circle-check"
          :title="$t('datasource.api_step_1')"
        />
        <el-step :title="$t('datasource.api_step_2')" />
      </el-steps>

      <el-row v-show="active === 1">
        <el-form
          ref="apiItemBasicInfo"
          size="small"
          :model="apiItem"
          label-position="top"
          label-width="100px"
          :rules="rule"
        >
          <div class="row-rules">
            <span>{{ $t('datasource.base_info') }}</span>
          </div>
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="apiItem.name" :placeholder="$t('commons.input_name')" autocomplete="off" />
          </el-form-item>

          <el-form-item :label="$t('datasource.request')" prop="url">
            <el-input
              v-model="apiItem.url"
              :placeholder="$t('datasource.path_all_info')"
              class="input-with-select"
              size="small"
            >
              <el-select
                slot="prepend"
                v-model="apiItem.method"
                style="width: 100px"
                size="small"
              >
                <el-option
                  v-for="item in reqOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </el-input>
          </el-form-item>

          <div v-loading="loading">
            <div class="row-rules mr40">
              <span>{{ $t('datasource.req_param') }}</span>
            </div>
            <!-- HTTP 请求参数 -->
            <el-form-item>
              <api-http-request-form
                v-if="edit_api_item"
                :headers="apiItem.request.headers"
                :request="apiItem.request"
                :response="responseData"
              />
            </el-form-item>
          </div>
        </el-form>
      </el-row>
      <el-row v-show="active === 2">
        <el-form
          ref="apiItem"
          size="small"
          :model="apiItem"
          label-position="top"
          label-width="100px"
          :rules="rule"
        >
          <div class="row-rules">
            <span>{{ $t('datasource.column_info') }}</span>
          </div>
          <div class="table-container de-svg-in-table">
            <el-table
              ref="apiItemTable"
              :data="apiItem.jsonFields"
              style="width: 100%"
              row-key="jsonPath"
            >
              <el-table-column
                class-name="checkbox-table"
                prop="originName"
                :label="$t('dataset.parse_filed')"
                :show-overflow-tooltip="true"
                width="255"
              >
                <template slot-scope="scope">
                  <el-checkbox
                    :key="scope.row.jsonPath"
                    v-model="scope.row.checked"
                    :disabled="scope.row.disabled"
                    @change="handleCheckAllChange(scope.row)"
                  >
                    {{ scope.row.originName }}
                  </el-checkbox>
                </template>
              </el-table-column>
              <el-table-column prop="name" :label="$t('dataset.field_rename')">
                <template slot-scope="scope">
                  <el-input
                    v-model="scope.row.name"
                    :disabled="scope.row.children"
                    size="small"
                    type="text"
                    @change="fieldNameChange(scope.row)"
                  />
                </template>
              </el-table-column>

              <el-table-column
                prop="deExtractType"
                :label="$t('dataset.field_type')"
              >
                <template slot-scope="scope">
                  <el-select
                    v-model="scope.row.deExtractType"
                    :disabled="scope.row.children"
                    size="small"
                    class="select-type"
                    style="display: inline-block; width: 120px"
                    @change="fieldTypeChange(scope.row)"
                  >
                    <el-option
                      v-for="item in fieldOptions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    >
                      <span style="float: left">
                        <svg-icon
                          v-if="item.value === '0'"
                          icon-class="field_text"
                          class="field-icon-text"
                        />
                        <svg-icon
                          v-if="item.value === '2' || item.value === '3'"
                          icon-class="field_value"
                          class="field-icon-value"
                        />
                      </span>
                      <span
                        style="float: left; color: #8492a6; font-size: 12px"
                      >{{ item.label }}</span>
                    </el-option>
                  </el-select>
                  <span class="select-svg-icon">
                    <span v-if="scope.row.deType === 0">
                      <svg-icon
                        icon-class="field_text"
                        class="field-icon-text"
                      />
                    </span>
                    <span v-if="[ 2, 3 ].includes(scope.row.deType)">
                      <svg-icon
                        icon-class="field_value"
                        class="field-icon-value"
                      />
                    </span>
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="row-rules">
            <span>{{ $t('dataset.data_preview') }}</span>
          </div>
          <el-empty
            v-if="showEmpty"
            size="125"
            :image="noneImg"
            style="margin-top: 24px"
            :description="$t('暂无数据，请在数据结构勾选字段')"
          />
          <ux-grid
            v-else
            ref="plxTable"
            size="mini"
            style="width: 100%"
            :height="height"
            :checkbox-config="{ highlight: true }"
            :width-resize="true"
          >
            <ux-table-column
              v-for="field in apiItem.fields"
              :key="field.name + field.deExtractType"
              min-width="200px"
              :field="field.name"
              :resizable="true"
            >
              <template slot="header">
                <svg-icon
                  v-if="field.deExtractType === 0"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="field.deExtractType === 1"
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="field.deExtractType === 2 || field.deExtractType === 3"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <svg-icon
                  v-if="field.deExtractType === 5"
                  icon-class="field_location"
                  class="field-icon-location"
                />
                <span>{{ field.name }}</span>
              </template>
            </ux-table-column>
          </ux-grid>
        </el-form>
      </el-row>
      <div class="foot">
        <el-button class="btn normal" @click="closeEditItem">{{
          $t('commons.cancel')
        }}</el-button>
        <el-button
          v-show="active === 1"
          class="btn"
          type="primary"
          :disabled="disabledNext"
          @click="next"
        >{{ $t('fu.steps.next') }}
        </el-button>
        <el-button
          v-show="active === 2"
          class="btn"
          type="primary"
          @click="before"
        >{{ $t('fu.steps.prev') }}
        </el-button>
        <el-button
          v-show="active === 2"
          class="btn"
          type="primary"
          @click="saveItem"
        >{{ $t('commons.save') }}
        </el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import i18n from '@/lang'
import { checkApiDatasource, getSchema } from '@/api/system/datasource'
import ApiHttpRequestForm from '@/views/system/datasource/ApiHttpRequestForm'
import dePwd from '@/components/deCustomCm/dePwd.vue'
import msgCfm from '@/components/msgCfm'
export default {
  name: 'DsConfiguration',
  components: {
    ApiHttpRequestForm,
    dePwd
  },
  mixins: [msgCfm],
  props: {
    disabled: {
      type: Boolean,
      default() {
        return false
      }
    },
    method: String,
    request: {},
    response: {},
    datasourceType: {},
    showScript: {
      type: Boolean,
      default: true
    },
    form: {
      type: Object,
      default() {
        return {
          configuration: {
            initialPoolSize: 5,
            extraParams: '',
            minPoolSize: 5,
            maxPoolSize: 50,
            maxIdleTime: 30,
            acquireIncrement: 5,
            idleConnectionTestPeriod: 5,
            connectTimeout: 5,
            queryTimeout: 30
          },
          apiConfiguration: []
        }
      }
    }
  },
  data() {
    return {
      showPriority: false,
      noneImg: require('@/assets/None.png'),
      rule: {
        name: [
          {
            required: true,
            validator: this.nameRepeat,
            trigger: 'blur',
            message: i18n.t('commons.input_name')
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
        'configuration.queryTimeout': [
          {
            required: true,
            message: i18n.t('datasource.please_input_query_timeout'),
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
      api_table_title: '',
      schemas: [],
      showEmpty: false,
      canEdit: false,
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
        fields: [],
        jsonFields: [
          {
            deType: 0,
            size: 65535,
            children: null,
            name: 'comments',
            checked: false,
            extField: 0,
            jsonPath: '$[*].comments',
            type: 'STRING',
            originName: 'comments',
            deExtractType: 0
          }
        ]
      },
      reqOptions: [
        { id: 'GET', label: 'GET' },
        { id: 'POST', label: 'POST' }
      ],
      loading: false,
      responseData: { type: 'HTTP', responseResult: {}, subRequestResults: [] },
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
      errMsg: [],
      height: 500,
      disabledNext: false,
      authMethodList: [
        {
          id: 'passwd',
          label: i18n.t('datasource.passwd')
        },
        {
          id: 'kerberos',
          label: 'Kerberos'
        }
      ],
      fieldOptions: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.value'), value: 2 },
        {
          label:
            this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
          value: 3
        }
      ],
      certinKey: false
    }
  },
  watch: {},
  created() {},
  methods: {
    getSchema() {
      this.$refs.DsConfig.validate((valid) => {
        if (valid) {
          const data = JSON.parse(JSON.stringify(this.form))
          data.configuration = JSON.stringify(data.configuration)
          getSchema(data).then((res) => {
            this.schemas = res.data
            this.$success(i18n.t('commons.success'))
          })
        } else {
          return false
        }
      })
    },
    nameRepeat(rule, value, callback) {
      if (!value) {
        callback(new Error(i18n.t('commons.input_name')))
        return
      }
      let hasRepeatName = false
      this.form.apiConfiguration.forEach((item) => {
        if (
          item.name === this.apiItem.name &&
            item.serialNumber !== this.apiItem.serialNumber
        ) {
          hasRepeatName = true
        }
      })
      if (hasRepeatName) {
        callback(new Error(i18n.t('theme.name_repeat')))
        return
      }
      callback()
    },
    next() {
      if (this.active === 1) {
        let hasRepeatName = false
        this.form.apiConfiguration.forEach((item) => {
          if (
            item.name === this.apiItem.name &&
            item.serialNumber !== this.apiItem.serialNumber
          ) {
            hasRepeatName = true
          }
        })
        if (hasRepeatName) {
          this.$message.error(i18n.t('datasource.has_repeat_name'))
          return
        }
        this.$refs.apiItemBasicInfo.validate((valid) => {
          if (valid) {
            const data = JSON.parse(JSON.stringify(this.apiItem))
            this.loading = true
            this.disabledNext = true
            checkApiDatasource(data)
              .then((res) => {
                this.loading = false
                this.disabledNext = false
                this.apiItem.status = 'Success'
                this.$success(i18n.t('commons.success'))
                this.active++
                this.apiItem.jsonFields = res.data.jsonFields
                this.apiItem.fields = []
                this.handleFiledChange()
                this.previewData()
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
      this.$refs.apiItemBasicInfo.clearValidate()
      this.edit_api_item = false
    },
    saveItem() {
      if (this.apiItem.fields.length === 0) {
        this.$message.warning(i18n.t('datasource.api_field_not_empty'))
        return
      }
      for (var i = 0; i < this.apiItem.fields.length - 1; i++) {
        for (var j = i + 1; j < this.apiItem.fields.length; j++) {
          if (this.apiItem.fields[i].name === this.apiItem.fields[j].name) {
            this.$message.error(
              this.apiItem.fields[i].name +
                ', ' +
                i18n.t('datasource.has_repeat_field_name')
            )
            return
          }
        }
      }
      this.active = 0
      this.edit_api_item = false
      if (!this.add_api_item) {
        for (var i = 0; i < this.form.apiConfiguration.length; i++) {
          if (
            this.form.apiConfiguration[i].serialNumber ===
            this.apiItem.serialNumber
          ) {
            this.form.apiConfiguration[i] = JSON.parse(
              JSON.stringify(this.apiItem)
            )
            this.certinKey = !this.certinKey
          }
        }
      } else {
        this.form.apiConfiguration.push(this.apiItem)
      }
    },
    copyItem(item) {
      var newItem = JSON.parse(JSON.stringify(item))
      newItem.serialNumber =
        this.form.apiConfiguration[this.form.apiConfiguration.length - 1]
          .serialNumber + 1
      var reg = new RegExp(item.name + '_copy_' + '([0-9]*)', 'gim')
      var number = 0
      for (var i = 1; i < this.form.apiConfiguration.length; i++) {
        var match = this.form.apiConfiguration[i].name.match(reg)
        if (match !== null) {
          var num = match[0].substring(
            this.form.apiConfiguration[i].name.length + 5,
            match[0].length - 1
          )
          if (parseInt(num) != NaN && parseInt(num) > number) {
            number = parseInt(num)
          }
        }
      }
      number = number + 1
      newItem.name = item.name + '_copy_' + number
      this.form.apiConfiguration.push(newItem)
      this.openMessageSuccess('datasource.success_copy')
    },
    addApiItem(item) {
      if (this.disabled) {
        return
      }
      this.api_table_title = this.$t('datasource.data_table')
      if (item) {
        this.add_api_item = false
        this.apiItem = JSON.parse(JSON.stringify(item))
      } else {
        this.add_api_item = true
        this.apiItem = JSON.parse(JSON.stringify(this.defaultApiItem))
        this.apiItem.serialNumber =
          this.form.apiConfiguration.length > 0
            ? this.form.apiConfiguration[this.form.apiConfiguration.length - 1]
              .serialNumber + 1
            : 0
      }
      this.active = 1
      this.edit_api_item = true
    },
    deleteItem(item) {
      this.form.apiConfiguration.splice(
        this.form.apiConfiguration.indexOf(item),
        1
      )
      this.$refs[`apiTable${item.name}`][0].doClose()
      this.openMessageSuccess('commons.delete_success')
    },
    cancelItem({ name }) {
      this.$refs[`apiTable${name}`][0].doClose()
    },
    handleCheckAllChange(row) {
      this.errMsg = []
      this.handleCheckChange(row)
      this.apiItem.fields = []
      this.handleFiledChange(row)
      this.previewData()
      if (this.errMsg.length) {
        this.$message.error(
                 [...new Set(this.errMsg)].join(',') +
                   ', ' +
                   i18n.t('datasource.has_repeat_field_name')
               )
      }
    },
    handleFiledChange() {
      for (var i = 0; i < this.apiItem.jsonFields.length; i++) {
        if (
          this.apiItem.jsonFields[i].checked &&
          this.apiItem.jsonFields[i].children === undefined
        ) {
          this.apiItem.fields.push(this.apiItem.jsonFields[i])
        }
        if (this.apiItem.jsonFields[i].children !== undefined) {
          this.handleFiledChange2(this.apiItem.jsonFields[i].children)
        }
      }
    },
    handleFiledChange2(jsonFields) {
      for (var i = 0; i < jsonFields.length; i++) {
        if (jsonFields[i].checked && jsonFields[i].children === undefined) {
          for (var j = 0; j < this.apiItem.fields.length; j++) {
            if (this.apiItem.fields[j].name === jsonFields[i].name) {
              jsonFields[i].checked = false
              this.$nextTick(() => {
                jsonFields[i].checked = false
              })
              this.errMsg.push(jsonFields[i].name)
            }
          }
          this.apiItem.fields.push(jsonFields[i])
        }
        if (jsonFields[i].children !== undefined) {
          this.handleFiledChange2(jsonFields[i].children)
        }
      }
    },
    previewData() {
      this.showEmpty = false
      const datas = []
      let maxPreviewNum = 0
      for (let j = 0; j < this.apiItem.fields.length; j++) {
        if (
          this.apiItem.fields[j].value &&
          this.apiItem.fields[j].value.length > maxPreviewNum
        ) {
          maxPreviewNum = this.apiItem.fields[j].value.length
        }
      }
      for (let i = 0; i < maxPreviewNum; i++) {
        datas.push({})
      }
      for (let i = 0; i < this.apiItem.fields.length; i++) {
        for (let j = 0; j < this.apiItem.fields[i].value.length; j++) {
          this.$set(
            datas[j],
            this.apiItem.fields[i].name,
            this.apiItem.fields[i].value[j]
          )
        }
        this.$nextTick(() => {
          this.$refs.plxTable?.reloadData(datas)
        })
      }
      this.showEmpty = this.apiItem.fields.length === 0
    },
    handleCheckChange(node) {
      if (node.children !== undefined) {
        node.children.forEach((item) => {
          item.checked = node.checked
          this.handleCheckChange(item)
        })
      }
    },
    fieldNameChange(row) {
      this.previewData()
    },
    fieldTypeChange(row) {}
  }
}
</script>

<style lang="scss">
.api-table-delete {
  padding: 20px 24px !important;
  display: flex;
  flex-wrap: wrap;
  .small {
    height: 28px;
    min-width: 48px !important;
  }
  .tips {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    margin-left: 8.67px;
    color: var(--deTextPrimary, #1f2329);
  }
  i {
    font-size: 14.666666030883789px;
    color: var(--deWarning, #ff8800);
    line-height: 22px;
  }
  .foot {
    text-align: right;
    width: 100%;
    margin-top: 16px;
  }
}
.de-expand {
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #3370ff;
  cursor: pointer;
  margin: 12px 0 16px 0;
  display: inline-block;
}
.de-mar0 {
  margin-top: 0;
}
.api-datasource-drawer {
  .el-drawer__header {
    padding: 16px 24px;
    margin: 0;
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: #1f2329;
    position: relative;
    border-bottom: 1px solid rgba(187, 191, 196, 0.5);
    .el-drawer__close-btn {
      position: absolute;
      right: 24px;
      top: 16px;
      padding: 4px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .el-drawer__close-btn:hover {
      background: #e9e9ea;
    }
  }
  .el-drawer__body {
    padding: 0 24px 81px 24px;
  }
  .row-rules {
    display: flex;
    align-items: center;
    position: relative;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    padding-left: 10px;
    margin: 24px 0 16px 0;
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      height: 14px;
      width: 2px;
      background: #3370ff;
    }
  }
  .input-with-select {
    .el-input-group__prepend {
      background-color: #fff;
      border-color: #bbbfc4;
      .el-select {
        width: 84px !important;
        color: #1f2329;
      }
    }
  }
  ::v-deep .el-form-item__label {
    width: 100% !important;
    text-align: left;
  }
  .el-steps {
    margin-top: 23px !important;
    justify-content: center;
    .el-step__line {
      line-height: 1.5;
      text-align: center;
      box-sizing: inherit;
      position: absolute;
      background-color: rgba(187, 191, 196, 0.5);
      height: 1px;
      top: 13px;
      left: calc(50% + 22px) !important;
      width: 100px;
      margin-right: 0px;
      i {
        border-top: none;
      }
    }
    .el-step__icon.is-text {
      border: none;
      font-family: PingFang SC;
      font-weight: 400;
      color: #fff;
      width: 28px;
      height: 28px;
      background: #3370ff;
    }
    .el-step__icon.is-icon {
      i {
        font-size: 27px;
      }
    }
    .el-step__title {
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      text-align: center;
      color: #1f2329;
      margin-top: 4px;
    }
    .is-process {
      .el-step__icon.is-text {
        background: none;
        color: #8f959e;
        border: 1px solid #8f959e;
      }
    }
    .is-process.el-step__title {
      font-weight: 400;
      color: #8f959e;
    }
  }
  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label:before {
    display: none;
  }
  .el-form-item.is-required:not(.is-no-asterisk) > .el-form-item__label::after {
    content: '*';
    color: #f54a45;
    margin-left: 2px;
  }
  ::v-deep .el-form-item__content {
    margin-left: 0 !important;
  }
  .el-form-item {
    margin-bottom: 28px;
  }
  .el-form-item__label {
    padding-bottom: 8px;
  }
  .mr40 {
    margin-top: 40px;
  }
  .btn {
    border-radius: 4px;
    padding: 5px 26px 5px 26px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0px;
    text-align: center;
    border: none;
    box-sizing: border-box;
  }
  .normal {
    color: #1f2329;
    border: 1px solid #bbbfc4;
    margin-left: 12px;
  }
  .table-container {
    padding: 20px;
    border: 1px solid #dee0e3;
    .el-table__header-wrapper {
      background-color: #f5f6f7;
      border-top: 1px solid #dee0e3;
      // border-top: 1px solid rgba(31, 35, 41, 0.15);
    }
    .el-table__fixed-header-wrapper {
      th {
        background-color: var(--TableBG, #f5f6f7) !important;
      }
    }
  }
  .foot {
    width: 100%;
    z-index: 5;
    height: 80px;
    padding: 24px;
    position: absolute;
    right: 0;
    bottom: 0;
    text-align: right;
    background: #ffffff;
    box-shadow: 0px -1px 4px rgba(0, 0, 0, 0.05);
  }
  .el-checkbox__label {
    width: 150px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
.checkbox-table {
  .el-checkbox {
    display: flex;
    align-items: center;
    .el-checkbox__input {
      margin-top: 4px;
    }
  }
  .cell.el-tooltip {
    display: flex;
    align-items: center;
  }
}
</style>
<style lang="scss" scoped>
.api-card {
  height: 128px;
  width: 600px;
  border-radius: 4px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin-bottom: 12px;
  padding: 20px 16px;
  font-family: PingFang SC;
  .name {
    font-size: 16px;
    font-weight: 500;
    margin-right: 8px;
    max-width: 80%;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
  .req-title,
  .req-value {
    display: flex;
    font-size: 14px;
    font-weight: 400;
    :nth-child(1) {
      width: 56px;
    }
    :nth-child(2) {
      margin-left: 84px;
    }
  }
  .req-title {
    color: var(--deTextPrimary, #1f2329);
    margin: 16px 0 4px 0;
  }
  .req-value {
    color: var(--deTextSecondary, #646a73);
  }
  .de-copy-icon {
    cursor: pointer;
    margin-right: 20px;
    color: var(--deTextSecondary, #646a73);
  }
  .de-delete-icon {
    cursor: pointer;
    &:hover {
      color: var(--deDanger, #f54a45);
    }
  }
  .de-tag {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    border-radius: 2px;
    padding: 1px 6px;
    height: 24px;
  }
}
.flex-space {
  justify-content: space-between;
}
.el-input-number {
  width: 100%;
  ::v-deep .el-input__inner {
    text-align: left;
  }
  ::v-deep.el-input-number__decrease,
  ::v-deep.el-input-number__increase {
    background: transparent;
  }
  &.is-disabled {
    .el-input-number__decrease {
      border-right-color: #e4e7ed;
    }
    .el-input-number__increase {
      border-bottom-color: #e4e7ed;
    }
  }
}
.de-svg-in-table {
  .select-type {
    width: 180px;
    ::v-deep.el-input__inner {
      padding-left: 32px;
    }
  }
  .select-svg-icon {
    position: absolute;
    left: 24px;
    top: 15px;
  }

  ::v-deep.el-table__expand-icon {
    .el-icon-arrow-right::before {
      content: "\E791" !important;
    }
  }
}
</style>
