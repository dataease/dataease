<script lang="ts" setup>
import { ref, reactive, onMounted, PropType, toRefs, watch, onBeforeUnmount } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { Base64 } from 'js-base64'
import useClipboard from 'vue-clipboard3'
import { ElMessage } from 'element-plus-secondary'
import CodeMirror from './CodeMirror.vue'
import { getDatasourceList, getTables, getPreviewSql } from '@/api/dataset'
import type { DataSource } from './index.vue'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { EmptyBackground } from '@/components/empty-background'
import { timestampFormatDate, defaultValueScopeList, fieldOptions } from './util.js'

export interface SqlNode {
  sql: string
  tableName: string
  datasourceId: string
  id: string
  sqlVariableDetails?: string
}

const props = defineProps({
  sqlNode: {
    type: Object as PropType<SqlNode>,
    default: () => ({})
  }
})

const { sqlNode } = toRefs(props)
const { toClipboard } = useClipboard()
const { t } = useI18n()
const activeName = ref('')
const myCm = ref()
const codeCom = ref()
const dialogTitle = t('sql_variable.variable_mgm') + ' '
const tabActive = ref('result')
const searchTable = ref('')
const showVariableMgm = ref(false)
const dsLoading = ref(false)
const loading = ref(false)
const LeftWidth = ref(240)
const showLeft = ref(true)
const editerName = ref()
const state = reactive({
  plxTableData: [],
  variables: [],
  fields: [],
  sqlData: [],
  variablesTmp: [],
  dataSourceList: [],
  tableData: [],
  table: {
    name: '',
    id: ''
  },
  param: {
    tableId: 0
  }
})

const paginationConfig = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const setActiveName = ({ name, enableCheck }) => {
  if (!enableCheck) return
  activeName.value = name
}
const handleSizeChange = pageSize => {
  paginationConfig.currentPage = 1
  paginationConfig.pageSize = pageSize
  listSqlLog()
}
const handleCurrentChange = currentPage => {
  paginationConfig.currentPage = currentPage
  listSqlLog()
}

const referenceSetting = () => {
  showVariableMgm.value = true
  parseVariable()
}

onMounted(() => {
  dsChange(sqlNode.value.datasourceId)
  codeCom.value = myCm.value.codeComInit(Base64.decode(sqlNode.value.sql))
})

onBeforeUnmount(() => {
  codeCom.value.destroy?.()
})

const getDatasource = () => {
  getDatasourceList().then(res => {
    state.dataSourceList = (res as unknown as DataSource[]) || []
  })
}
const dragHeight = ref(280)

const mousedownDragH = () => {
  document.querySelector('.sql-eidtor').addEventListener('mousemove', calculateHeight)
}

const calculateHeight = (e: MouseEvent) => {
  if (e.pageY - 164 < 280) {
    dragHeight.value = 280
    return
  }
  if (e.pageY - 164 > document.documentElement.clientHeight - 270) {
    dragHeight.value = document.documentElement.clientHeight - 270
    return
  }
  dragHeight.value = e.pageY - 164
}

const insertParamToCodeMirror = (value: string) => {
  codeCom.value.dispatch({
    changes: { from: 0, to: codeCom.value.state.doc.toString().length, insert: '' }
  })
  codeCom.value.dispatch({
    changes: { from: codeCom.value.viewState.state.selection.ranges[0].from, insert: value },
    selection: { anchor: codeCom.value.viewState.state.selection.ranges[0].from }
  })
}

watch(
  () => sqlNode.value.datasourceId,
  val => {
    dsChange(val)
  }
)

watch(
  () => sqlNode.value.id,
  () => {
    if (codeCom.value) {
      insertParamToCodeMirror(Base64.decode(sqlNode.value.sql))
    }
  }
)

const treeProps = {
  children: 'children',
  label: 'name'
}

getDatasource()

const emits = defineEmits(['close', 'save'])

const saveClose = () => {
  save()
  close()
}

const save = () => {
  parseVariable()
  emits('save', {
    ...sqlNode.value,
    sql: Base64.encode(codeCom.value.state.doc.toString()),
    sqlVariableDetails: JSON.stringify(state.variables)
  })
}

const close = () => {
  emits('close')
}
const getSQLPreview = () => {
  parseVariable()
  getPreviewSql({
    sql: Base64.encode(codeCom.value.state.doc.toString()),
    datasourceId: sqlNode.value.datasourceId,
    sqlVariableDetails: JSON.stringify(state.variables)
  }).then(res => {
    state.plxTableData = res.data.data
    state.fields = res.data.fields
  })
}

let tableList = []
watch(searchTable, val => {
  state.tableData = tableList.filter(ele => ele.name.includes(val))
})

const getIconName = (type: string) => {
  if (
    ['DATETIME-YEAR', 'DATETIME-YEAR-MONTH', 'DATETIME', 'DATETIME-YEAR-MONTH-DAY'].includes(type)
  ) {
    return 'time'
  }

  if (type === 'TEXT') {
    return 'text'
  }

  if (['LONG', 'DOUBLE'].includes(type)) {
    return 'value'
  }
}

const formatter = (_, __, cellValue) => {
  return cellValue ? `${cellValue} ${t(`commons.millisecond`)}` : '-'
}

const handleShowLeft = () => {
  showLeft.value = !showLeft.value
  LeftWidth.value = showLeft.value ? 240 : 0
}

const dsChange = (val: string) => {
  getTables(val).then(res => {
    tableList = res || []
    state.tableData = [...tableList]
  })
}

const listSqlLog = () => {
  console.log(123)
}
const copyInfo = async (value: string) => {
  try {
    await toClipboard(value)
    ElMessage.success('复制成功')
  } catch (e) {
    ElMessage.warning('您的浏览器不支持复制：', e)
  }
}

const mouseupDrag = () => {
  const dom = document.querySelector('.sql-eidtor')
  dom.removeEventListener('mousemove', calculateHeight)
}

const parseVariable = () => {
  state.variablesTmp = []
  const reg = new RegExp('\\${(.*?)}', 'gim')
  const match = codeCom.value.state.doc.toString().match(reg)
  const names = []
  if (match !== null) {
    for (let index = 0; index < match.length; index++) {
      let name = match[index].substring(2, match[index].length - 1)
      if (names.indexOf(name) < 0) {
        names.push(name)
        // eslint-disable-next-line
            let obj = undefined
        for (let i = 0; i < state.variables.length; i++) {
          if (state.variables[i].variableName === name) {
            obj = state.variables[i]
            if (!obj.hasOwnProperty('defaultValueScope')) {
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
        state.variablesTmp.push(obj)
      }
    }
  }
  state.variables = JSON.parse(JSON.stringify(state.variablesTmp))
}

const saveVariable = () => {
  state.variables = JSON.parse(JSON.stringify(state.variablesTmp))
  showVariableMgm.value = false
  ElMessage.success('参数设置成功')
}

const mousedownDrag = () => {
  document.querySelector('.sql-eidtor').addEventListener('mousemove', calculateHeight)
}
</script>

<template>
  <div class="add-sql-name">
    <el-input class="name" ref="editerName" v-model="sqlNode.tableName" />
    <div class="run-params-config">
      <el-button @click="getSQLPreview" text>
        <template #icon>
          <el-icon>
            <Icon name="reference-play"></Icon>
          </el-icon>
        </template>
        运行
      </el-button>
      <el-button @click="referenceSetting()" style="color: #1f2329" text>
        <template #icon>
          <el-icon>
            <Icon name="reference-setting"></Icon>
          </el-icon>
        </template>
        参数设置
      </el-button>
    </div>
    <div class="save-or-cancel">
      <el-button @click="close" secondary> 取消</el-button>
      <el-button @click="save" type="primary"> 保存</el-button>
      <el-button @click="saveClose" type="primary"> 保存并关闭</el-button>
    </div>
  </div>

  <div class="sql-eidtor" @mouseup="mouseupDrag">
    <p v-show="!showLeft" class="arrow-right" @click="handleShowLeft">
      <el-icon>
        <Icon name="icon_down-right_outlined"></Icon>
      </el-icon>
    </p>
    <div
      v-show="showLeft"
      :style="{ left: LeftWidth + 'px' }"
      class="drag-left"
      @mousedown="mousedownDrag"
    />
    <div
      v-loading="dsLoading"
      v-show="showLeft"
      class="table-list"
      :style="{ width: LeftWidth + 'px' }"
    >
      <p class="select-ds">
        选择数据源
        <el-icon @click="handleShowLeft">
          <Icon name="icon_up-left_outlined"></Icon>
        </el-icon>
      </p>
      <el-tree-select
        :check-strictly="false"
        @change="dsChange"
        :placeholder="t('dataset.pls_slc_data_source')"
        class="ds-list"
        popper-class="tree-select-ds_popper"
        v-model="sqlNode.datasourceId"
        node-key="id"
        :props="treeProps"
        :data="state.dataSourceList"
        :render-after-expand="false"
      />
      <p class="select-ds">{{ t('datasource.data_table') }}</p>
      <el-input
        v-model="searchTable"
        class="search"
        :placeholder="t('deDataset.by_table_name')"
        clearable
      >
        <template #prefix>
          <el-icon>
            <Icon name="icon_search-outline_outlined"></Icon>
          </el-icon>
        </template>
      </el-input>
      <div v-if="!state.tableData.length && searchTable !== ''" class="el-empty">
        <div class="el-empty__description" style="margin-top: 80px; color: #5e6d82">
          没有找到相关内容
        </div>
      </div>
      <div v-else class="table-checkbox-list">
        <template v-for="ele in state.tableData" :key="ele.name">
          <div
            :class="[{ active: activeName === ele.name }]"
            class="list-item_primary"
            :title="ele.name"
            @click="setActiveName(ele)"
          >
            <span class="label">{{ ele.name }}</span>
            <span class="name-copy">
              <el-icon @click="copyInfo(ele.name)">
                <Icon name="icon_copy_outlined"></Icon>
              </el-icon>
            </span>
          </div>
        </template>
      </div>
    </div>
    <div class="sql-code-right" :style="{ width: `calc(100% - ${LeftWidth}px)` }">
      <code-mirror :height="`${dragHeight}px`" dom-id="sql-editor" ref="myCm"></code-mirror>
      <div class="sql-result" :style="{ height: `calc(100% - ${dragHeight}px)` }">
        <div class="sql-title">
          <span class="drag" @mousedown="mousedownDragH" />
        </div>
        <div class="padding-24">
          <el-tabs v-model="tabActive">
            <el-tab-pane :label="t('deDataset.running_results')" name="result" />
          </el-tabs>
        </div>
        <div v-show="tabActive === 'result'" class="table-sql">
          <div class="table-scroll" v-if="state.fields.length">
            <el-table
              style="width: 100%"
              header-cell-class-name="header-cell"
              :data="state.plxTableData"
              border
            >
              <el-table-column
                v-for="field in state.fields"
                :key="field.originName"
                min-width="200px"
                :prop="field.originName"
                :label="field.originName"
                resizable
              />
            </el-table>
          </div>
          <template v-else>
            <empty-background description="点击运行查询" img-type="table">
              即可查看运行结果
            </empty-background>
          </template>
        </div>
        <div v-show="tabActive === 'execLog'" class="table-container">
          <grid-table
            v-loading="loading"
            :table-data="state.sqlData"
            :show-pagination="!!state.param.tableId"
            :columns="[]"
            :pagination="paginationConfig"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          >
            <el-table-column
              key="startTimeTable"
              min-width="100px"
              prop="startTime"
              :label="t('dataset.start_time')"
            >
              <template #default="scope">
                <span>{{ timestampFormatDate(scope.row.startTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column key="sql" prop="sql" show-overflow-tooltip :label="t('dataset.sql')" />
            <el-table-column
              key="spend"
              prop="spend"
              :formatter="formatter"
              :label="t('dataset.spend_time')"
            />
            <el-table-column key="status" prop="status" :label="t('dataset.sql_result')">
              <template #default="scope">
                <span v-if="scope.row.status" :class="[`de-${scope.row.status}-pre`, 'de-status']"
                  >{{ t(`dataset.${scope.row.status.toLocaleLowerCase()}`) }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>

            <el-table-column
              key="__operation"
              :label="t('commons.operating')"
              fixed="right"
              width="100"
            >
              <template #default="scope">
                <el-button text @click="copyInfo(scope.row.sql)">
                  {{ t('commons.copy') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
      </div>
    </div>
  </div>
  <el-drawer
    :title="dialogTitle"
    v-model="showVariableMgm"
    custom-class="sql-dataset-drawer"
    size="870px"
    direction="rtl"
  >
    <div class="content">
      <el-icon>
        <Icon name="icon_info_outlined"></Icon>
      </el-icon>
      {{ t('dataset.sql_variable_limit_1') }}<br />
      {{ t('dataset.sql_variable_limit_2') }}<br />
    </div>
    <el-table :data="state.variablesTmp">
      <el-table-column prop="variableName" :label="t('visualization.param_name')" />
      <el-table-column width="200" :label="t('deDataset.parameter_type')">
        <template #default="scope">
          <el-cascader
            class="select-type"
            popper-class="cascader-panel"
            v-model="scope.row.type"
            :options="fieldOptions"
            @change="scope.row = ''"
          >
            <template v-slot="{ data }">
              <el-icon>
                <Icon
                  :className="`field-icon-${getIconName(data.value)}`"
                  :name="`field_${getIconName(data.value)}`"
                ></Icon>
              </el-icon>
              <span>{{ data.label }}</span>
            </template>
          </el-cascader>
          <span class="select-svg-icon">
            <el-icon>
              <Icon
                :className="`field-icon-${getIconName(scope.row.type[0])}`"
                :name="`field_${getIconName(scope.row.type[0])}`"
              ></Icon>
            </el-icon>
          </span>
        </template>
      </el-table-column>
      <el-table-column min-width="350" prop="defaultValue" :label="t('commons.params_value')">
        <template #header>
          {{ t('commons.params_value') }}
        </template>
        <template #default="scope">
          <el-input
            v-if="getIconName(scope.row.type[0]) === 'text'"
            v-model="scope.row.defaultValue"
            type="text"
            :placeholder="t('common.please_input')"
          >
            <template #prepend>
              <el-select v-model="scope.row.defaultValueScope" style="width: calc(100% + 22px)">
                <el-option
                  v-for="item in defaultValueScopeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </template>
          </el-input>
          <el-input
            v-if="getIconName(scope.row.type[0]) === 'value'"
            v-model="scope.row.defaultValue"
            :placeholder="t('common.please_input')"
            type="number"
          >
            <template #prepend>
              <el-select v-model="scope.row.defaultValueScope" style="width: 100px">
                <el-option
                  v-for="item in defaultValueScopeList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </template>
          </el-input>
          <div
            v-if="getIconName(scope.row.type[0]) === 'time'"
            class="el-input-group el-input-group--prepend de-group__prepend"
          >
            <div class="el-input-group__prepend">
              <el-select v-model="scope.row.defaultValueScope" style="width: 100px" size="small">
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
              value-format="yyyy"
              :placeholder="t('dataset.select_year')"
            />

            <el-date-picker
              v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH'"
              v-model="scope.row.defaultValue"
              type="month"
              :format="scope.row.type[1]"
              :value-format="scope.row.type[1]"
              :placeholder="t('dataset.select_month')"
            />

            <el-date-picker
              v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH-DAY'"
              v-model="scope.row.defaultValue"
              type="date"
              :format="scope.row.type[1]"
              :value-format="scope.row.type[1]"
              :placeholder="t('dataset.select_date')"
            />

            <el-date-picker
              v-if="scope.row.type[0] === 'DATETIME'"
              v-model="scope.row.defaultValue"
              type="datetime"
              :format="scope.row.type[1]"
              :value-format="scope.row.type[1]"
              :placeholder="t('dataset.select_time')"
            />
          </div>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button secondary @click="showVariableMgm = false">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="saveVariable()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-drawer>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';
.sql-eidtor {
  width: 100%;
  height: calc(100vh - 156px);
  position: relative;
  .drag-left {
    position: absolute;
    height: calc(100vh - 156px);
    width: 2px;
    top: 0;
    z-index: 5;
    cursor: col-resize;
  }

  .arrow-right {
    position: absolute;
    top: 15px;
    z-index: 2;
    cursor: pointer;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    height: 24px;
    width: 20px;
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-top-right-radius: 13px;
    border-bottom-right-radius: 13px;
  }

  .table-list {
    height: 100%;
    width: 240px;
    float: left;
    padding: 16px 12px;
    font-family: PingFang SC;
    border-right: 1px solid rgba(31, 35, 41, 0.15);

    .select-ds {
      font-size: 14px;
      font-weight: 500;
      display: flex;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);

      i {
        cursor: pointer;
        font-size: 12px;
        color: var(--deTextPlaceholder, #8f959e);
      }
    }

    .search {
      margin: 12px 0;
    }

    .ds-list {
      margin: 12px 0 24px 0;
      width: 100%;
    }

    .table-checkbox-list {
      height: calc(100% - 190px);
      overflow-y: auto;

      .not-allow {
        cursor: not-allowed;
        color: var(--deTextDisable, #bbbfc4);
      }

      .name-copy {
        display: none;
        line-height: 24px;
        margin-left: auto;
      }

      .list-item_primary:hover {
        .name-copy {
          display: inline;
        }
      }
    }
  }

  .sql-code-right {
    float: right;
    height: calc(100vh - 156px);
    .sql-result {
      font-family: PingFang SC;
      font-size: 14px;
      overflow-y: auto;
      box-sizing: border-box;
      width: 100%;

      .sql-title {
        user-select: none;
        display: flex;
        align-items: center;
        position: relative;
        z-index: 5;
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
        width: calc(100% - 48px);
        .border-bottom-tab(24px);
      }

      .table-sql {
        height: calc(100% - 46px);
        width: 100%;
        padding: 16px 25px 18px 25px;
        overflow: auto;
        box-sizing: border-box;

        .table-scroll {
          float: left;
        }
      }

      .de-status {
        position: relative;
        margin-left: 15px;

        &::before {
          content: '';
          position: absolute;
          top: 50%;
          left: -13px;
          transform: translateY(-50%);
          width: 5px;
          height: 5px;
          border-radius: 50%;
        }
      }

      .de-Pending-result,
      .de-Underway-result {
        &::before {
          background: var(--deTextPlaceholder, #8f959e);
        }
      }

      .de-Exec-result,
      .de-Underway-pre {
        &::before {
          background: var(--primary, #3370ff);
        }
      }

      .de-Stopped-result,
      .de-Completed-pre {
        &::before {
          background: var(--deSuccess, #34c724);
        }
      }

      .de-Error-pre {
        &::before {
          background: var(--deDanger, #f54a45);
        }

        .ed-icon-s-order {
          color: var(--primary, #3370ff);
          cursor: pointer;
        }
      }
    }

    .table-container {
      height: calc(100% - 46px);
      padding: 16px 24px;
    }
  }
}

.add-sql-name {
  height: 56px;
  width: 100%;
  padding: 0 24px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  .name {
    width: 240px;
    margin: 8px;
  }

  .save-or-cancel {
    margin-left: auto;
  }
}
</style>
<style lang="less">
.tree-select-ds_popper {
  .ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after {
    display: none !important;
  }
}
.sql-eidtor {
  .cm-scroller {
    height: 250px;
    width: 100%;
    overflow-y: auto;
  }

  .cm-focused {
    outline: none;
  }
}
.sql-dataset-drawer {
  .de-group__prepend {
    width: 100%;
  }

  .ed-date-editor {
    width: 100%;
    display: inline-block;
  }

  .select-type {
    .ed-input__wrapper {
      padding-left: 32px !important;
    }
  }

  .select-svg-icon {
    position: absolute;
    left: 24px;
    top: 15px;
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
.cascader-panel {
  .ed-cascader-node__label {
    display: flex;
    align-items: center;
    .ed-icon {
      margin-right: 5px;
    }
  }
}
</style>
