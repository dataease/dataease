<template>
  <div v-loading="loading" class="dataset-sql" @mouseup="mouseupDrag">
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
            <el-option :label="$t('dataset.direct_connect')" value="0" />
            <el-option
              :label="$t('dataset.sync_data')"
              value="1"
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
            <el-option :label="$t('dataset.sync_latter')" value="sync_latter" />
          </el-select>
        </el-col>
        <el-col style="text-align: right" :span="12">
          <el-button
            type="text"
            size="small"
            class="de-text-btn"
            @click="dataReference = true"
          >
            <svg-icon icon-class="data-reference" />
            {{ $t('deDataset.data_reference') }}
          </el-button>
          <el-button
            type="text"
            size="small"
            style="color: #1f2329"
            class="de-text-btn"
            @click="variableMgm"
          >
            <svg-icon icon-class="reference-setting" />
            {{ $t('sql_variable.variable_mgm') }}
          </el-button>
          <el-divider direction="vertical" />
          <el-button
            class="de-text-btn"
            type="text"
            size="small"
            @click="getSQLPreview"
          >
            <svg-icon icon-class="reference-play" />
            {{ $t('deDataset.run_a_query') }}
          </el-button>
        </el-col>
      </el-row>
    </div>
    <div class="refrence-sql-table">
      <div v-if="dataReference" class="data-reference">
        <div class="table-database-name">
          <p>
            <span
              v-if="showTable"
              style="cursor: pointer"
              @click="
                showTable = false
                dataTable = ''
              "
            ><i class="el-icon-arrow-left" /> {{ $t('chart.back') }}</span>
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
          <p v-if="dataSource" style="margin-top: 16px">
            <span>
              <svg-icon icon-class="db-de" />
              {{ (showTable && dataTable) || selectedDatasource.name }}
            </span>
            <span class="grey">
              <svg-icon
                :icon-class="showTable ? 'reference-table' : 'reference-field'"
              />
              {{ (showTable && fieldData.length) || tableData.length }}
            </span>
          </p>
        </div>
        <span v-if="!dataSource" class="no-select-datasource">{{
          $t('deDataset.to_start_using')
        }}</span>
        <div v-else-if="dataSource && !dataTable" v-loading="tableLoading" class="item-list">
          <div
            v-for="ele in tableData"
            :key="ele.name"
            class="table-or-field"
            @click="typeSwitch(ele)"
          >
            {{ ele.name }}
          </div>
        </div>
        <div v-else-if="dataSource && dataTable" class="item-list">
          <div
            v-for="ele in fieldData"
            :key="ele.fieldName"
            class="table-or-field"
          >
            {{ ele.fieldName }}
          </div>
        </div>
      </div>
      <div class="sql-table">
        <div class="code-container" :style="{ height: sqlHeight + 'px' }">
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
            {{ $t('deDataset.running_results') }}
            <span class="result-num">{{
              `(${$t('dataset.preview_show')} 1000 ${$t(
                'dataset.preview_item'
              )})`
            }}</span>

            <span class="drag" @mousedown="mousedownDrag" />
          </div>
          <div class="table-sql">
            <el-empty
              v-if="initFlag"
              :image-size="125"
              style="margin-top: 80px"
              :image="initImg"
              :description="$t('deDataset.to_run_query')"
            >{{ $t('deDataset.the_running_results') }}
            </el-empty>
            <el-empty
              v-else-if="errMsg"
              :image-size="60"
              :image="errImg"
              :description="$t('deDataset.run_failed')"
            >{{ errMsgCont }}</el-empty>
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
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :field="field.fieldName"
                :title="field.remarks"
                :resizable="true"
              />
            </ux-grid>
          </div>
          <el-drawer
            v-closePress
            :title="dialogTitle"
            :visible.sync="showVariableMgm"
            custom-class="user-drawer sql-dataset-drawer"
            size="840px"
            direction="rtl"
          >
            <div class="content">
              <i class="el-icon-info" />
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
                <template slot-scope="scope">
                  <el-input
                    v-if="scope.row.type[0] === 'TEXT'"
                    v-model="scope.row.defaultValue"
                    size="small"
                    type="text"
                    :placeholder="$t('fu.search_bar.please_input')"
                  />
                  <el-input
                    v-if="
                      scope.row.type[0] === 'LONG' ||
                        scope.row.type[0] === 'DOUBLE'
                    "
                    v-model="scope.row.defaultValue"
                    size="small"
                    :placeholder="$t('fu.search_bar.please_input')"
                    type="number"
                  />

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
                </template>
              </el-table-column>
            </el-table>
            <div class="de-foot">
              <deBtn secondary @click="closeVariableMgm">{{
                $t('dataset.cancel')
              }}</deBtn>
              <deBtn type="primary" @click="saveVariable()">{{
                $t('dataset.confirm')
              }}</deBtn>
            </div>
          </el-drawer>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { post, listDatasource, isKettleRunning } from '@/api/dataset/dataset'
import { codemirror } from 'vue-codemirror'
import { getTable } from '@/api/dataset/dataset'
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
import _ from 'lodash'
export default {
  name: 'AddSQL',
  components: { codemirror },
  mixins: [msgCfm, cancelMix],
  props: {
    param: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
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
      dataReference: false,
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
      data: [],
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
    },
    dataSourceDetail() {}
  },
  watch: {
    sqlHeight: {
      handler: function() {
        this.calHeight()
      }
    }
  },
  async mounted() {
    window.onresize = () => {
      this.calHeight()
    }
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
    getField(name) {
      post('/dataset/table/getFields', {
        dataSourceId: this.dataSource,
        info: JSON.stringify({ table: name })
      }).then((res) => {
        this.fieldData = res.data
      })
    },
    typeSwitch({ name }) {
      this.showTable = true
      this.dataTable = name
      this.getField(name)
    },
    mousedownDrag() {
      document
        .querySelector('.dataset-sql')
        .addEventListener('mousemove', this.caculateHeight)
    },
    mouseupDrag() {
      document
        .querySelector('.dataset-sql')
        .removeEventListener('mousemove', this.caculateHeight)
    },
    caculateHeight(e) {
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
        })
        .finally(() => {
          this.tableLoading = false
        })
    },
    calHeight: _.debounce(function () {
      const sqlHeight = Math.max(this.sqlHeight, 248)
      const currentHeight = document.documentElement.clientHeight
      this.height = currentHeight - sqlHeight - 56 - 54 - 36 - 64
    }, 200),
    initDataSource() {
      return listDatasource().then((response) => {
        this.options = response.data.filter((item) => item.type !== 'api')
      })
    },

    initTableInfo() {
      if (this.param.tableId) {
        getTable(this.param.tableId).then((response) => {
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
          this.getSQLPreview()
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
      this.$refs.plxTable?.reloadData([])
      post('/dataset/table/sqlPreview', {
        dataSourceId: this.dataSource,
        type: 'sql',
        sqlVariableDetails: JSON.stringify(this.variables),
        info: JSON.stringify({
          sql: Base64.encode(this.sql.trim()),
          isBase64Encryption: true
        })
      }, true, 60000, true)
        .then((response) => {
          this.fields = response.data.fields
          this.data = response.data.data
          const datas = this.data
          this.$nextTick(() => {
            this.$refs.plxTable?.reloadData(datas)
          })
        })
        .catch((err, msg) => {
          this.errMsgCont = err
          this.errMsg = true
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
          this.openMessageSuccess('deDataset.set_saved_successfully')
          this.cancel(response.data)
        })
        .finally(() => {
          this.loading = false
        })
    },
    showSQL(val) {
      this.sql = val || ''
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {},
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
            var obj = undefined
            for (let i = 0; i < this.variables.length; i++) {
              if (this.variables[i].variableName === name) {
                obj = this.variables[i]
              }
            }
            if (obj === undefined) {
              obj = {
                variableName: name,
                alias: '',
                type: [],
                required: false,
                defaultValue: '',
                details: ''
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

  .el-date-editor {
    width: 100%;
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
    width: 792px;
    border-radius: 4px;
    background: #e1eaff;
    position: relative;
    padding: 9px 19px 9px 40px;
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

  .refrence-sql-table {
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

        :nth-child(2)p {
          margin-top: 16px;
        }
        .grey {
          font-size: 14px;
          font-weight: 400;
          color: var(--deTextSecondary, #646a73);
        }
      }

      .item-list {
        padding: 16px 8px;
        height: calc(100vh - 200px);
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

          &.field {
            color: var(--deTextPrimary, #1f2329);
          }

          &.field:hover {
            background: none;
          }

          &:hover {
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

    .table-sql {
      height: calc(100% - 64px);
      padding: 18px 25px;
      overflow-y: auto;
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
}
</style>
