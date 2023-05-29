<template>
  <div
    v-loading="loading"
    class="dataset-sql"
    @mouseup="mouseupDrag"
  >
    <div class="sql-editer">
      <el-row>
        <el-col :span="12">
          <el-select
            v-model="dataSource"
            filterable
            style="width: 215px"
            :placeholder="$t('dataset.pls_slc_data_source')"
            size="small"
            @change="changeDatasource()"
          >
            <el-option
              v-for="item in options"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-if="!param.tableId"
            v-model="mode"
            style="width: 120px; margin: 0 12px"
            filterable
            :placeholder="$t('dataset.connect_mode')"
            size="small"
          >
            <el-option
              :label="$t('dataset.direct_connect')"
              value="0"
            />
            <el-option
              :label="$t('dataset.sync_data')"
              value="1"
              v-if="engineMode !== 'simple'"
              :disabled="disabledSync"
            />
          </el-select>
          <el-select
            v-if="mode === '1'"
            v-model="syncType"
            style="width: 120px"
            filterable
            :placeholder="$t('dataset.connect_mode')"
            size="small"
          >
            <el-option
              :label="$t('dataset.sync_now')"
              value="sync_now"
              :disabled="engineMode === 'simple'"
            />
            <el-option
              :label="$t('dataset.sync_latter')"
              value="sync_latter"
            />
          </el-select>
        </el-col>
        <el-col
          style="text-align: right"
          :span="12"
        >
          <el-button
            type="text"
            size="small"
            class="de-text-btn"
            @click="dataReference = true"
          >
            <svg-icon icon-class="data-reference"/>
            {{ $t('deDataset.data_reference') }}
          </el-button>
          <el-button
            type="text"
            size="small"
            style="color: #1f2329"
            class="de-text-btn"
            @click="variableMgm"
          >
            <svg-icon icon-class="reference-setting"/>
            {{ $t('sql_variable.variable_mgm') }}
          </el-button>
          <el-divider direction="vertical"/>
          <el-button
            class="de-text-btn"
            type="text"
            size="small"
            @click="getSQLPreview"
          >
            <svg-icon icon-class="reference-play"/>
            {{ $t('deDataset.run_a_query') }}
          </el-button>
        </el-col>
      </el-row>
    </div>
    <div class="reference-sql-table">
      <div
        v-if="dataReference"
        class="data-reference"
      >
        <div class="table-database-name">
          <p>
            <span
              v-if="showTable"
              style="cursor: pointer"
              @click="
                showTable = false
                dataTable = ''
                ;keywords = ''
              "
            ><i class="el-icon-arrow-left"/> {{ $t('chart.back') }}</span>
            <span v-else>{{ $t('deDataset.data_reference') }}</span>
            <i
              style="cursor: pointer"
              class="el-icon-close"
              @click="
                showTable = false
                dataTable = ''
                dataReference = false
              "
            />
          </p>
          <p
            v-if="dataSource"
            style="margin-top: 16px"
          >
            <span
              :title="(showTable && dataTable) || selectedDatasource.name"
              class="grey-name"
            >
              <svg-icon icon-class="db-de"/>
              {{ (showTable && dataTable) || selectedDatasource.name }}
            </span>
            <span class="grey">
              <svg-icon
                :icon-class="showTable ? 'reference-table' : 'reference-field'"
              />
              {{ (showTable && fieldDataCopy.length) || tableDataCopy.length }}
            </span>
          </p>
        </div>
        <span
          v-if="!dataSource"
          class="no-select-datasource"
        >{{
            $t('deDataset.to_start_using')
          }}</span>
        <template v-else>
          <el-input :placeholder="$t('fu.search_bar.please_input')" style="padding: 5px" size="small"
                    v-model="keywords"
          ></el-input>
          <div
            v-if="dataSource && !dataTable"
            v-loading="tableLoading"
            class="item-list"
          >
            <div
              v-for="ele in tableDataCopy"
              :key="ele.name"
              class="table-or-field"
              @click="typeSwitch(ele)"
            >
            <span
              :title="`${ele.name}${ele.remark ? ':' + ele.remark : ''}`"
              class="name"
            >{{ ele.name }}</span>
              <i
                v-clipboard:copy="ele.name"
                v-clipboard:success="onCopy"
                v-clipboard:error="onError"
                class="el-icon-document-copy"
                @click.stop
              />
            </div>
          </div>
          <div
            v-else-if="dataSource && dataTable"
            v-loading="tableLoading"
            class="item-list"
          >
            <div
              v-for="ele in fieldDataCopy"
              :key="ele.fieldName"
              class="table-or-field field"
            >
            <span
              :title="`${ele.fieldName}${ele.remark ? ':' + ele.remark : ''}`"
              class="name"
            >{{ ele.fieldName }}</span>
              <i
                v-clipboard:copy="ele.fieldName"
                v-clipboard:success="onCopy"
                v-clipboard:error="onError"
                class="el-icon-document-copy"
                @click.stop
              />
            </div>
          </div>
        </template>

      </div>
      <div class="sql-table">
        <div
          class="code-container"
          :style="{ height: sqlHeight + 'px' }"
        >
          <codemirror
            ref="myCm"
            v-model="sql"
            class="codemirror"
            :options="sqlOption"
            @ready="onCmReady"
            @focus="onCmFocus"
            @input="onCmCodeChange"
          />
        </div>

        <div class="sql-result">
          <div class="sql-title">
            {{
              $t(
                tabActive === 'result'
                  ? 'deDataset.running_results'
                  : 'dataset.task.record'
              )
            }}
            <span
              v-if="tabActive === 'result'"
              class="result-num"
            >{{
                `(${$t('dataset.preview_show')} 1000 ${$t(
                  'dataset.preview_item'
                )})`
              }}</span>

            <span
              class="drag"
              @mousedown="mousedownDrag"
            />
          </div>
          <el-tabs
            v-model="tabActive"
            class="padding-24"
          >
            <el-tab-pane
              :label="$t('deDataset.running_results')"
              name="result"
            />
            <el-tab-pane
              :label="$t('dataset.task.record')"
              name="execLog"
            />
          </el-tabs>
          <div
            v-show="tabActive === 'result'"
            class="table-sql"
          >
            <el-empty
              v-if="initFlag"
              :image-size="125"
              style="margin-top: 22px"
              :image="initImg"
              :description="$t('deDataset.to_run_query')"
            >{{ $t('deDataset.the_running_results') }}
            </el-empty>
            <el-empty
              v-else-if="errMsg"
              :image-size="60"
              :image="errImg"
              :description="$t('deDataset.run_failed')"
            >{{ errMsgCont }}
            </el-empty>
            <div style="float: left" v-else-if="fields.length">
              <el-table
              :data="plxTableData"
              size="mini"
              border
            >
              <el-table-column
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :prop="field.fieldName"
                :label="field.remarks"
                resizable
              />
            </el-table>
            </div>
            <el-table
              :data="plxTableData"
              v-else
              size="mini"
              border
            >
              <el-table-column
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :prop="field.fieldName"
                :label="field.remarks"
                resizable
              />
            </el-table>
          </div>
          <div
            v-show="tabActive === 'execLog'"
            class="table-container"
          >
            <grid-table
              v-loading="loading"
              :table-data="sqlData"
              :show-pagination="!!param.tableId"
              :columns="[]"
              :pagination="paginationConfig"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            >
              <el-table-column
                key="startTimeTable"
                min-width="100px"
                prop="startTime"
                :label="$t('dataset.start_time')"
              >
                <template slot-scope="scope">
                  <span>{{ scope.row.startTime | timestampFormatDate }}</span>
                </template>
              </el-table-column>
              <el-table-column
                key="sql"
                prop="sql"
                show-overflow-tooltip
                :label="$t('dataset.sql')"
              />
              <el-table-column
                key="spend"
                prop="spend"
                :formatter="formatter"
                :label="$t('dataset.spend_time')"
              />
              <el-table-column
                key="status"
                prop="status"
                :label="$t('dataset.sql_result')"
              >
                <template slot-scope="scope">
                  <span
                    v-if="scope.row.status"
                    :class="[`de-${scope.row.status}-pre`, 'de-status']"
                  >{{ $t(`dataset.${scope.row.status.toLocaleLowerCase()}`) }}
                  </span>
                  <span v-else>-</span>
                </template>
              </el-table-column>

              <el-table-column
                slot="__operation"
                key="__operation"
                :label="$t('commons.operating')"
                fixed="right"
                width="100"
              >
                <template slot-scope="scope">
                  <el-button
                    class="de-text-btn mar3 mar6"
                    type="text"
                    @click="copy(scope.row.sql)"
                  >
                    {{ $t('commons.copy') }}
                  </el-button>
                </template>
              </el-table-column>
            </grid-table>
          </div>
          <el-drawer
            v-closePress
            :title="dialogTitle"
            :visible.sync="showVariableMgm"
            custom-class="de-user-drawer sql-dataset-drawer"
            size="870px"
            direction="rtl"
          >
            <div class="content">
              <i class="el-icon-info"/>
              {{ $t('dataset.sql_variable_limit_1') }}<br>
              {{ $t('dataset.sql_variable_limit_2') }}<br>
            </div>
            <el-table :data="variablesTmp">
              <el-table-column
                prop="variableName"
                :label="$t('panel.param_name')"
              />
              <el-table-column
                width="200"
                :label="$t('deDataset.parameter_type')"
              >
                <template slot-scope="scope">
                  <el-cascader
                    v-model="scope.row.type"
                    size="mini"
                    class="select-type"
                    :options="fieldOptions"
                    @change="variableTypeChange(scope.row)"
                  >
                    <template slot-scope="{ data }">
                      <svg-icon
                        v-if="data.value === 'TEXT'"
                        icon-class="field_text"
                        class="field-icon-text"
                      />
                      <svg-icon
                        v-if="
                          [
                            'DATETIME-YEAR',
                            'DATETIME-YEAR-MONTH',
                            'DATETIME',
                            'DATETIME-YEAR-MONTH-DAY'
                          ].includes(data.value)
                        "
                        icon-class="field_time"
                        class="field-icon-time"
                      />
                      <svg-icon
                        v-if="['LONG', 'DOUBLE'].includes(data.value)"
                        icon-class="field_value"
                        class="field-icon-value"
                      />
                      <span>{{ data.label }}</span>
                    </template>
                  </el-cascader>
                  <span class="select-svg-icon">
                    <svg-icon
                      v-if="scope.row.type[0] === 'TEXT'"
                      icon-class="field_text"
                      class="field-icon-text"
                    />
                    <svg-icon
                      v-if="
                        [
                          'DATETIME-YEAR',
                          'DATETIME-YEAR-MONTH',
                          'DATETIME',
                          'DATETIME-YEAR-MONTH-DAY'
                        ].includes(scope.row.type[0])
                      "
                      icon-class="field_time"
                      class="field-icon-time"
                    />
                    <svg-icon
                      v-if="['LONG', 'DOUBLE'].includes(scope.row.type[0])"
                      icon-class="field_value"
                      class="field-icon-value"
                    />
                  </span>
                </template>
              </el-table-column>
              <el-table-column
                min-width="350"
                prop="defaultValue"
                :label="$t('commons.params_value')"
              >
                <template #header>
                  {{ $t('commons.params_value') }}
                </template>
                <template slot-scope="scope">
                  <el-input
                    v-if="scope.row.type[0] === 'TEXT'"
                    v-model="scope.row.defaultValue"
                    size="small"
                    type="text"
                    :placeholder="$t('fu.search_bar.please_input')">
                    <el-select
                      slot="prepend"
                      v-model="scope.row.defaultValueScope"
                      style="width: 100px"
                      size="small"
                    >
                      <el-option
                        v-for="item in defaultValueScopeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                  </el-input>
                  <el-input
                    v-if="
                      scope.row.type[0] === 'LONG' ||
                        scope.row.type[0] === 'DOUBLE'
                    "
                    v-model="scope.row.defaultValue"
                    size="small"
                    :placeholder="$t('fu.search_bar.please_input')"
                    type="number"
                  >
                    <el-select
                      slot="prepend"
                      v-model="scope.row.defaultValueScope"
                      style="width: 100px"
                      size="small"
                    >
                      <el-option
                        v-for="item in defaultValueScopeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                  </el-input>
                  <div v-if="['DATETIME-YEAR', 'DATETIME-YEAR-MONTH', 'DATETIME-YEAR-MONTH-DAY', 'DATETIME'].includes(scope.row.type[0])" class="el-input-group el-input-group--prepend de-group__prepend">
                    <div class="el-input-group__prepend">
                      <el-select
                        v-model="scope.row.defaultValueScope"
                        style="width: 100px"
                        size="small"
                      >
                        <el-option
                          v-for="item in defaultValueScopeList"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        />
                      </el-select>
                    </div>
                  <el-date-picker
                    v-if="scope.row.type[0] === 'DATETIME-YEAR'"
                    v-model="scope.row.defaultValue"
                    type="year"
                    size="small"
                    value-format="yyyy"
                    :placeholder="$t('dataset.select_year')"
                  />

                  <el-date-picker
                    v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH'"
                    v-model="scope.row.defaultValue"
                    type="month"
                    size="small"
                    :format="scope.row.type[1]"
                    :value-format="scope.row.type[1]"
                    :placeholder="$t('dataset.select_month')"
                  />

                  <el-date-picker
                    v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH-DAY'"
                    v-model="scope.row.defaultValue"
                    type="date"
                    size="small"
                    :format="scope.row.type[1]"
                    :value-format="scope.row.type[1]"
                    :placeholder="$t('dataset.select_date')"
                  />

                  <el-date-picker
                    v-if="scope.row.type[0] === 'DATETIME'"
                    v-model="scope.row.defaultValue"
                    type="datetime"
                    size="small"
                    :format="scope.row.type[1]"
                    :value-format="scope.row.type[1]"
                    :placeholder="$t('dataset.select_time')"
                  />
                </div>
                </template>
              </el-table-column>
            </el-table>
            <div class="de-foot">
              <deBtn
                secondary
                @click="closeVariableMgm"
              >{{
                  $t('dataset.cancel')
                }}
              </deBtn>
              <deBtn
                type="primary"
                @click="saveVariable()"
              >{{
                  $t('dataset.confirm')
                }}
              </deBtn>
            </div>
          </el-drawer>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getTable, isKettleRunning, listDatasource, post } from '@/api/dataset/dataset'
import { codemirror } from 'vue-codemirror'
import { Base64 } from 'js-base64'
// 核心样式
import 'codemirror/lib/codemirror.css'
// 引入主题后还需要在 options 中指定主题才会生效
import 'codemirror/theme/solarized.css'
import 'codemirror/mode/sql/sql.js'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// closebrackets
import 'codemirror/addon/edit/closebrackets.js'
// keyMap
import 'codemirror/mode/clike/clike.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/emacs.js'
// 引入代码自动提示插件
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/sql-hint'
import 'codemirror/addon/hint/show-hint'
import { engineMode } from '@/api/system/engine'
import msgCfm from '@/components/msgCfm/index'
import cancelMix from './cancelMix'
import { pySort } from './util'
import _ from 'lodash'
import GridTable from '@/components/gridTable/index.vue'
import { updateCacheTree } from '@/components/canvas/utils/utils'

export default {
  name: 'AddSQL',
  components: { codemirror, GridTable },
  mixins: [msgCfm, cancelMix],
  props: {
    param: {
      type: Object,
      default: () => {
      }
    }
  },
  data() {
    return {
      tabActive: 'result',
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      keywords: '',
      sqlData: [],
      dataSource: '',
      loading: false,
      dataTable: '',
      initFlag: true,
      showTable: false,
      tableData: [],
      fieldData: [],
      errMsg: false,
      errMsgCont: '',
      options: [],
      sql: '',
      dataReference: true,
      plxTableData: [],
      sqlOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: {
          // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      tableDataCopy: [],
      fieldDataCopy: [],
      errImg: require('@/assets/error.png'),
      initImg: require('@/assets/None.png'),
      sqlHeight: 248,
      fields: [],
      mode: '0',
      tableLoading: false,
      syncType: 'sync_now',
      height: 500,
      kettleRunning: false,
      selectedDatasource: {},
      engineMode: 'local',
      disabledSync: true,
      showVariableMgm: false,
      dialogTitle: '',
      variables: [],
      variablesTmp: [],
      defaultValueScopeList: [
        { label: this.$t('dataset.scope_edit'), value: 'EDIT' },
        { label: this.$t('dataset.scope_all'), value: 'ALLSCOPE' }],
      fieldOptions: [
        { label: this.$t('dataset.text'), value: 'TEXT' },
        { label: this.$t('dataset.value'), value: 'LONG' },
        {
          label:
            this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')',
          value: 'DOUBLE'
        },
        { label: this.$t('dataset.time_year'), value: 'DATETIME-YEAR' },
        {
          label: this.$t('dataset.time_year_month'),
          value: 'DATETIME-YEAR-MONTH',
          children: [
            {
              value: 'yyyy-MM',
              label: 'YYYY-MM'
            },
            {
              value: 'yyyy/MM',
              label: 'YYYY/MM'
            }
          ]
        },
        {
          label: this.$t('dataset.time_year_month_day'),
          value: 'DATETIME-YEAR-MONTH-DAY',
          children: [
            {
              value: 'yyyy-MM-dd',
              label: 'YYYY-MM-DD'
            },
            {
              value: 'yyyy/MM/dd',
              label: 'YYYY/MM/DD'
            }
          ]
        },
        {
          label: this.$t('dataset.time_all'),
          value: 'DATETIME',
          children: [
            {
              value: 'yyyy-MM-dd HH:mm:ss',
              label: 'YYYY-MM-DD HH:MI:SS'
            },
            {
              value: 'yyyy/MM/dd HH:mm:ss',
              label: 'YYYY/MM/DD HH:MI:SS'
            }
          ]
        }
      ]
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {
    sqlHeight: {
      handler: function() {
        this.calHeight()
      }
    },
    keywords(val) {
      if (!val) {
        this.tableDataCopy = this.arrSort([...this.tableData], 'name')
        this.fieldDataCopy = this.arrSort([...this.fieldData])
        return
      }

      if (this.dataSource && !this.dataTable) {
        this.tableDataCopy = this.arrSort(this.tableData.filter(ele => ele.name.includes(val)), 'name')
      }

      if (this.dataSource && this.dataTable) {
        this.fieldDataCopy = this.arrSort(this.fieldData.filter(ele => ele.fieldName.includes(val)))
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calHeight)
  },
  async mounted() {
    window.addEventListener('resize', this.calHeight)
    this.calHeight()
    await this.initDataSource()
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })
    this.initTableInfo()
  },
  created() {
    this.kettleState()
    engineMode().then((res) => {
      this.engineMode = res.data
    })
  },
  methods: {
    copy(text) {
      this.$copyText(text).then(
        (e) => {
          this.openMessageSuccess('commons.copy_success')
        },
        (e) => {
          this.openMessageSuccess('commons.copy_success')
        }
      )
    },
    formatter(row, column, cellValue) {
      return cellValue ? `${cellValue} ${this.$t(`commons.millisecond`)}` : '-'
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.listSqlLog()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.listSqlLog()
    },
    getField(name) {
      this.tableLoading = true
      post('/dataset/table/getFields', {
        dataSourceId: this.dataSource,
        info: JSON.stringify({ table: name })
      }).then((res) => {
        this.fieldData = res.data
        this.fieldDataCopy = this.arrSort([...this.fieldData])
      })
        .finally(() => {
          this.tableLoading = false
        })
    },
    typeSwitch({ name }) {
      this.showTable = true
      this.dataTable = name
      this.keywords = ''
      this.getField(name)
    },
    mousedownDrag() {
      document
        .querySelector('.dataset-sql')
        .addEventListener('mousemove', this.calculateHeight)
    },
    mouseupDrag() {
      document
        .querySelector('.dataset-sql')
        .removeEventListener('mousemove', this.calculateHeight)
    },
    calculateHeight(e) {
      if (e.pageY - 120 < 248) {
        this.sqlHeight = 248
        return
      }
      if (e.pageY - 120 > document.documentElement.clientHeight - 170) {
        this.sqlHeight = document.documentElement.clientHeight - 170
        return
      }
      this.sqlHeight = e.pageY - 120
    },
    kettleState() {
      isKettleRunning().then((res) => {
        this.kettleRunning = res.data
      })
    },
    changeDatasource() {
      this.keywords = ''
      for (let i = 0; i < this.options.length; i++) {
        if (this.options[i].id === this.dataSource) {
          this.selectedDatasource = this.options[i]
          this.mode = '0'
          if (
            this.engineMode === 'simple' ||
            !this.kettleRunning ||
            this.selectedDatasource.calculationMode === 'DIRECT'
          ) {
            this.disabledSync = true
          } else {
            this.disabledSync = false
          }
        }
      }
      this.tableLoading = true
      post('/datasource/getTables/' + this.dataSource, {})
        .then((response) => {
          this.tableData = response.data
          this.tableDataCopy = this.arrSort([...this.tableData], 'name')
        })
        .finally(() => {
          this.tableLoading = false
        })
    },
    arrSort(arr = [], field = 'fieldName') {
      arr.sort((a, b) => {
        return a[field][0].toLowerCase().charCodeAt() - b[field][0].toLowerCase().charCodeAt()
      })

      return arr
    },
    calHeight: _.debounce(function() {
      const sqlHeight = Math.max(this.sqlHeight, 248)
      const currentHeight = document.documentElement.clientHeight
      this.height = currentHeight - sqlHeight - 56 - 54 - 36 - 64
    }, 200),
    initDataSource() {
      return listDatasource().then((response) => {
        this.options = pySort(response.data.filter((item) => item.type !== 'api'))
      })
    },

    initTableInfo() {
      const tableId = this.param.tableId || this.$route.query.id
      if (tableId) {
        this.$emit('datasourceLoading', true)
        getTable(tableId).then((response) => {
          const table = response.data
          this.dataSource = table.dataSourceId
          this.changeDatasource()
          this.mode = table.mode + ''

          if (JSON.parse(table.info).isBase64Encryption) {
            this.sql = Base64.decode(JSON.parse(table.info).sql)
          } else {
            this.sql = JSON.parse(
              table.info.replace(/\n/g, '\\n').replace(/\r/g, '\\r')
            ).sql
          }
          this.variables = JSON.parse(table.sqlVariableDetails)
        }).finally(() => {
          this.$emit('datasourceLoading', false)
        })
      }
    },
    getSQLPreview() {
      this.errMsg = false
      this.errMsgCont = ''
      this.initFlag = false
      if (!this.dataSource || this.datasource === '') {
        this.openMessageSuccess('dataset.pls_slc_data_source', 'error')
        return
      }
      this.parseVariable()
      this.fields = []
      this.plxTableData = []
      post(
        '/dataset/table/sqlPreview',
        {
          id: this.param.tableId,
          dataSourceId: this.dataSource,
          type: 'sql',
          mode: parseInt(this.mode),
          sqlVariableDetails: JSON.stringify(this.variables),
          info: JSON.stringify({
            sql: Base64.encode(this.sql.trim()),
            isBase64Encryption: true
          })
        },
        true,
        60000,
        true
      )
        .then((response) => {
          if (response.success) {
            this.fields = response.data.fields
            this.plxTableData = response.data.data || []
            if (!this.param.tableId) {
              this.sqlData.unshift(response.data.log)
            } else {
              this.listSqlLog()
            }
          } else {
            this.errMsgCont = response.message
            this.errMsg = true
            if (!this.param.tableId) {
              this.sqlData.unshift(response.data)
            } else {
              this.listSqlLog()
            }
          }
        })
        .catch((err, msg, response) => {
          this.errMsgCont = err
          this.errMsg = true
          if (!this.param.tableId) {
            this.sqlData.unshift(response.data)
          } else {
            this.listSqlLog()
          }
        })
    },
    listSqlLog() {
      post(
        '/dataset/table/sqlLog/' +
        this.paginationConfig.currentPage +
        '/' +
        this.paginationConfig.pageSize,
        { id: this.param.tableId, dataSourceId: this.dataSource }
      )
        .then((response) => {
          this.sqlData = response.data.listObject
          this.paginationConfig.total = response.data.itemCount
        })
        .catch(() => {
        })
    },
    save() {
      if (!this.dataSource || this.datasource === '') {
        this.openMessageSuccess('dataset.pls_slc_data_source', 'error')
        return
      }
      if (!this.param.name || this.param.name === '') {
        this.openMessageSuccess('dataset.pls_input_name', 'error')
        return
      }
      if (this.param.name.length > 50) {
        this.openMessageSuccess('dataset.char_can_not_more_50', 'error')
        return
      }
      this.parseVariable()
      if (this.loading) return
      this.loading = true
      const table = {
        id: this.param.tableId,
        name: this.param.name,
        sceneId: this.param.id,
        dataSourceId: this.dataSource,
        type: 'sql',
        syncType: this.syncType,
        mode: parseInt(this.mode),
        sqlVariableDetails: JSON.stringify(this.variables),
        info: JSON.stringify({
          sql: Base64.encode(this.sql.trim()),
          isBase64Encryption: true
        })
      }
      post('/dataset/table/update', table)
        .then((response) => {
          if (table.id) {
            const renameNode = { id: table.id, name: table.name, label: table.name }
            updateCacheTree('rename', 'dataset-tree', renameNode, JSON.parse(localStorage.getItem('dataset-tree')))
          } else {
            updateCacheTree('batchNew', 'dataset-tree', response.data, JSON.parse(localStorage.getItem('dataset-tree')))
          }
          this.openMessageSuccess('deDataset.set_saved_successfully')
          this.cancel(response.data)
        })
        .finally(() => {
          this.loading = false
        })
    },
    onCopy(e) {
      this.openMessageSuccess('commons.copy_success')
    },
    onError(e) {
    },
    showSQL(val) {
      this.sql = val || ''
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
    },
    onCmCodeChange(newCode) {
      this.sql = newCode
      this.$emit('codeChange', this.sql)
    },
    variableMgm() {
      this.parseVariable()
      this.dialogTitle = this.$t('sql_variable.variable_mgm') + ' '
      this.showVariableMgm = true
    },
    parseVariable() {
      this.variablesTmp = []
      var reg = new RegExp('\\${(.*?)}', 'gim')
      var match = this.sql.match(reg)
      const names = []
      if (match !== null) {
        for (let index = 0; index < match.length; index++) {
          var name = match[index].substring(2, match[index].length - 1)
          if (names.indexOf(name) < 0) {
            names.push(name)
            // eslint-disable-next-line
            var obj = undefined
            for (let i = 0; i < this.variables.length; i++) {
              if (this.variables[i].variableName === name) {
                obj = this.variables[i]
                if(!obj.hasOwnProperty("defaultValueScope")){
                  obj.defaultValueScope = 'EDIT'
                }
              }
            }
            if (obj === undefined) {
              obj = {
                variableName: name,
                alias: '',
                type: [],
                required: false,
                defaultValue: '',
                details: '',
                defaultValueScope: 'EDIT'
              }
              obj.type.push('TEXT')
            }
            this.variablesTmp.push(obj)
          }
        }
      }
      this.variables = JSON.parse(JSON.stringify(this.variablesTmp)).concat()
    },
    closeVariableMgm() {
      this.showVariableMgm = false
    },
    saveVariable() {
      this.variables = JSON.parse(JSON.stringify(this.variablesTmp)).concat()
      this.showVariableMgm = false
      this.openMessageSuccess('参数设置成功')
    },
    variableTypeChange(row) {
      row.defaultValue = ''
    }
  }
}
</script>

<style scoped>
.codemirror {
  height: 100%;
  overflow-y: auto;
}

.codemirror ::v-deep .CodeMirror-scroll {
  height: 100%;
  overflow-y: auto;
}
</style>

<style lang="scss">
.sql-dataset-drawer {
  .el-drawer__body {
    padding: 16px 24px;
    position: unset;
    overflow-y: auto;
    padding-bottom: 80px;
  }

  .de-group__prepend {
    width: 100%;
  }

  .el-date-editor {
    width: 100%;
    display: inline-block;
  }

  .select-type {
    width: 180px;

    .el-input__inner {
      padding-left: 32px;
    }
  }

  .select-svg-icon {
    position: absolute;
    left: 24px;
    top: 50%;
    transform: translateY(-50%);
  }

  .content {
    height: 62px;
    width: 822px;
    border-radius: 4px;
    background: #e1eaff;
    position: relative;
    padding: 9px 0 9px 40px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;

    i {
      position: absolute;
      top: 12.6px;
      left: 16.7px;
      font-size: 14px;
      color: var(--primary, #3370ff);
    }

    margin-bottom: 16px;
  }
}

.dataset-sql {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;

  .sql-editer {
    background: #f5f6f7;
    padding: 16px 24px;
  }

  .reference-sql-table {
    flex: 1;
    display: flex;
    flex-direction: row-reverse;
    overflow: hidden;

    .data-reference {
      width: 280px;
      height: 100%;
      overflow: hidden;
      border-left: 1px solid var(--deCardStrokeColor, #dee0e3);

      .no-select-datasource {
        font-family: PingFang SC;
        font-size: 14px;
        color: var(--deTextPrimary, #1f2329);
        font-weight: 400;
        display: inline-block;
        width: 100%;
        padding: 16px 12px;
      }

      .table-database-name {
        font-family: PingFang SC;
        font-size: 16px;
        font-weight: 500;
        color: var(--deTextPrimary, #1f2329);
        padding: 16px 12px;
        border-bottom: 1px solid var(--deCardStrokeColor, #dee0e3);

        p {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin: 0;
        }

        .grey-name {
          max-width: 80%;
          white-space: nowrap;
          text-overflow: ellipsis;
          overflow: hidden;
        }

        .grey {
          font-size: 14px;
          font-weight: 400;
          color: var(--deTextSecondary, #646a73);
        }
      }

      .item-list {
        padding: 16px 8px;
        height: calc(100vh - 242px);
        overflow: auto;

        .table-or-field {
          height: 40px;
          width: 100%;
          box-sizing: border-box;
          border-radius: 4px;
          color: var(--primary, #3370ff);
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          display: flex;
          align-items: center;
          padding-left: 4px;
          padding-right: 10px;
          display: flex;
          align-items: center;
          justify-content: space-between;

          i {
            display: none;
            cursor: pointer;
          }

          .name {
            cursor: pointer;
            width: 90%;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
          }

          &.field {
            color: var(--deTextPrimary, #1f2329);
          }

          &.field:hover {
            background: none;
          }

          &:hover {
            i {
              display: block;
            }

            background: rgba(31, 35, 41, 0.1);
          }
        }
      }
    }

    .sql-table {
      flex: 1;
      height: 100%;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .code-container {
        background: #f5f6f7;
        box-sizing: border-box;
        min-height: 248px;
        color: var(--deTextPrimary, #1f2329);

        .CodeMirror {
          height: 100% !important;
        }
      }
    }
  }

  .sql-result {
    font-family: PingFang SC;
    font-size: 14px;
    overflow-y: auto;
    box-sizing: border-box;
    flex: 1;

    .sql-title {
      user-select: none;
      height: 54px;
      display: flex;
      align-items: center;
      padding: 16px 24px;
      font-weight: 500;
      position: relative;
      color: var(--deTextPrimary, #1f2329);
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);

      .result-num {
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-left: 12px;
      }

      .drag {
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        height: 7px;
        width: 100px;
        border-radius: 3.5px;
        background: rgba(31, 35, 41, 0.1);
        cursor: row-resize;
      }
    }

    .padding-24 {
      .el-tabs__nav-scroll {
        padding-left: 24px;
      }
    }

    .table-sql {
      height: calc(100% - 128px);
      margin: 0 25px 18px 25px;
      overflow: auto;
      box-sizing: border-box;

      .el-empty__bottom,
      .el-empty__description p {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        margin-top: 0;
        color: var(--deTextSecondary, #646a73);
      }
    }
  }

  .table-container {
    height: calc(100% - 125px);
    padding: 0 24px;

    .mar6 {
      margin-right: 6px;
    }

    .mar3 {
      margin-left: -3px;
    }
  }

  .table-container-filter {
    height: calc(100% - 110px);
  }
}
</style>
