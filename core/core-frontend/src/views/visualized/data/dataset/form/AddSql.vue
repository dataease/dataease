<script lang="tsx" setup>
import { ref, reactive, onMounted, PropType, toRefs, watch, onBeforeUnmount, shallowRef } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { Base64 } from 'js-base64'
import FixedSizeList from 'element-plus-secondary/es/components/virtual-list/src/components/fixed-size-list.mjs'
import { useWindowSize } from '@vueuse/core'
import useClipboard from 'vue-clipboard3'
import { ElMessage, ElMessageBox, ElIcon } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { getTableField } from '@/api/dataset'
import CodeMirror from './CodeMirror.vue'
import type { Field, DataSource } from './util'
import { getDatasourceList, getTables, getPreviewSql } from '@/api/dataset'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { EmptyBackground } from '@/components/empty-background'
import { timestampFormatDate, defaultValueScopeList, fieldOptions } from './util'
import { fieldType } from '@/utils/attr'
export interface SqlNode {
  sql: string
  tableName: string
  datasourceId: string
  id: string
  changeFlag?: boolean
  variables?: Array<{
    variableName: string
    defaultValue: string
    defaultValueScope: string
  }>
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
  table: {
    name: '',
    id: ''
  },
  param: {
    tableId: 0
  }
})

const datasourceTableData = shallowRef([])

const paginationConfig = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const setActiveName = ({ name, enableCheck }) => {
  if (!enableCheck) return
  activeName.value = name
}
const { height: windowHeight } = useWindowSize()

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    deType: ele.deType,
    dataKey: ele.originName,
    title: ele.originName,
    width: 170,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center">
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon
            name={`field_${fieldType[column.deType]}`}
            className={`field-icon-${fieldType[column.deType]}`}
          ></Icon>
        </ElIcon>
        <span class="ellipsis" title={column.title} style={{ width: '120px' }}>
          {column.title}
        </span>
      </div>
    )
  }))

const referenceSetting = () => {
  showVariableMgm.value = true
  parseVariable()
}

onMounted(() => {
  dsChange(sqlNode.value.datasourceId)
  codeCom.value = myCm.value.codeComInit(Base64.decode(sqlNode.value.sql), true)
})

onBeforeUnmount(() => {
  codeCom.value.destroy?.()
})

const gridData = ref([])
const gridDataLoading = ref(false)

const getNodeField = ({ datasourceId, tableName }) => {
  gridDataLoading.value = true
  let info = {
    table: tableName,
    sql: ''
  }
  getTableField({ datasourceId, info: JSON.stringify(info), tableName, type: 'db' })
    .then(res => {
      gridData.value = res as unknown as Field[]
    })
    .finally(() => {
      gridDataLoading.value = false
    })
}

const getDatasource = () => {
  getDatasourceList().then(res => {
    const _list = (res as unknown as DataSource[]) || []
    if (_list && _list.length > 0 && _list[0].id === '0') {
      state.dataSourceList = _list[0].children
    } else {
      state.dataSourceList = _list
    }
  })
}
const dragHeight = ref(260)

const mousedownDragH = () => {
  document.querySelector('.sql-eidtor').addEventListener('mousemove', calculateHeight)
}

const calculateHeight = (e: MouseEvent) => {
  if (e.pageY - 164 < 64) {
    dragHeight.value = 64
    return
  }
  if (e.pageY - 164 > document.documentElement.clientHeight - 200) {
    dragHeight.value = document.documentElement.clientHeight - 200
    return
  }
  dragHeight.value = e.pageY - 164
}

const calculateWidth = (e: MouseEvent) => {
  if (e.pageX < 240) {
    LeftWidth.value = 240
    return
  }
  if (e.pageX > 400) {
    LeftWidth.value = 400
    return
  }
  LeftWidth.value = e.pageX
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
    state.variables = sqlNode.value.variables
    if (codeCom.value) {
      insertParamToCodeMirror(Base64.decode(sqlNode.value.sql))
    }
  },
  {
    immediate: true
  }
)

const treeProps = {
  children: 'children',
  label: 'name'
}

getDatasource()

const emits = defineEmits(['close', 'save'])

let changeFlag = false
const changeFlagCode = ref(false)
const setFlag = () => {
  changeFlag = true
  changeFlagCode.value = true
}
let sql = ''

const save = (cb?: () => void) => {
  if (!sqlNode.value.tableName.trim()) {
    ElMessage.error('SQL名字不能为空')
    return
  }

  parseVariable()
  sql = codeCom.value.state.doc.toString()
  sqlNode.value.changeFlag = true
  if (!sql.trim()) {
    ElMessage.error('SQL不能为空')
    return
  }
  sqlNode.value.sql = Base64.encode(sql)
  emits(
    'save',
    {
      ...sqlNode.value,
      sql: Base64.encode(sql),
      sqlVariableDetails: JSON.stringify(state.variables)
    },
    cb
  )
  changeFlag = false
  ElMessage.success(t('common.save_success'))
}

const close = () => {
  searchTable.value = ''
  state.plxTableData = []
  state.fields = []
  if (codeCom.value) {
    insertParamToCodeMirror(Base64.decode(sqlNode.value.sql))
  }
  emits('close')
}

const handleClose = () => {
  let sqlNew = codeCom.value.state.doc.toString()
  if (changeFlag || sql !== sqlNew || !sqlNew.trim()) {
    ElMessageBox.confirm(t('chart.tips'), {
      confirmButtonType: 'primary',
      tip: '你填写的信息未保存，确认退出吗？',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      close()
      changeFlag = false
      changeFlagCode.value = false
    })
  } else {
    close()
    changeFlagCode.value = false
  }
}

const dataPreviewLoading = ref(false)
const getSQLPreview = () => {
  parseVariable()
  dataPreviewLoading.value = true
  getPreviewSql({
    sql: Base64.encode(codeCom.value.state.doc.toString()),
    datasourceId: sqlNode.value.datasourceId,
    sqlVariableDetails: JSON.stringify(state.variables)
  })
    .then(res => {
      state.plxTableData = res.data.data
      state.fields = generateColumns(res.data.fields)
    })
    .finally(() => {
      dataPreviewLoading.value = false
    })
}

let tableList = []
watch(searchTable, val => {
  datasourceTableData.value = tableList.filter(ele => ele.tableName.includes(val))
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
  dsLoading.value = true
  getTables({ datasourceId: val })
    .then(res => {
      tableList = res || []
      datasourceTableData.value = [...tableList]
    })
    .finally(() => {
      dsLoading.value = false
    })
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
  dom.removeEventListener('mousemove', calculateWidth)
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
        for (let i = 0; i < state.variables?.length; i++) {
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
  changeFlagCode.value = true
  ElMessage.success('参数设置成功')
}
const mousedownDrag = () => {
  document.querySelector('.sql-eidtor').addEventListener('mousemove', calculateWidth)
}
</script>

<template>
  <div class="add-sql-name">
    <el-input class="name" ref="editerName" v-model="sqlNode.tableName" @change="setFlag" />
    <div class="save-or-cancel flex-align-center">
      <el-button @click="getSQLPreview" text style="color: #1f2329">
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
      <el-button :disabled="!changeFlagCode" @click="save(() => {})" type="primary">
        保存</el-button
      >
      <el-divider direction="vertical" />
      <el-icon class="hover-icon" @click="handleClose">
        <Icon name="icon_close_outlined"></Icon>
      </el-icon>
    </div>
  </div>

  <div class="sql-eidtor" @mouseup="mouseupDrag">
    <p v-show="!showLeft" class="arrow-right" @click="handleShowLeft">
      <el-icon>
        <Icon name="icon_right_outlined"></Icon>
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
      <div class="table-list-top">
        <p class="select-ds">
          当前数据源
          <span class="left-outlined">
            <el-icon style="color: #1f2329" @click="showLeft = false">
              <Icon name="icon_left_outlined" />
            </el-icon>
          </span>
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
        <p class="select-ds table-num">
          {{ t('datasource.data_table')
          }}<span class="num">
            <el-icon class="icon-color">
              <Icon name="reference-table"></Icon>
            </el-icon>
            {{ datasourceTableData.length }}
          </span>
        </p>
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
      </div>
      <div v-if="!datasourceTableData.length && searchTable !== ''" class="el-empty">
        <div
          class="el-empty__description"
          style="margin-top: 80px; color: #5e6d82; text-align: center"
        >
          没有找到相关内容
        </div>
      </div>
      <div v-else class="table-checkbox-list">
        <FixedSizeList
          :itemSize="40"
          :data="datasourceTableData"
          :total="datasourceTableData.length"
          :width="LeftWidth - 7"
          :height="windowHeight - 350"
          :scrollbarAlwaysOn="false"
          class-name="el-select-dropdown__list"
          layout="vertical"
        >
          <template #default="{ index, style }">
            <div
              :class="[{ active: activeName === datasourceTableData[index].tableName }]"
              class="list-item_primary"
              :style="style"
              :title="datasourceTableData[index].tableName"
              @click="setActiveName(datasourceTableData[index])"
            >
              <el-icon class="icon-color">
                <Icon name="icon_form_outlined"></Icon>
              </el-icon>
              <span class="label">{{ datasourceTableData[index].tableName }}</span>
              <span class="name-copy">
                <el-tooltip effect="dark" :content="t('common.copy')" placement="top">
                  <el-icon
                    class="hover-icon"
                    @click="copyInfo(datasourceTableData[index].tableName)"
                  >
                    <Icon name="icon_copy_outlined"></Icon>
                  </el-icon>
                </el-tooltip>

                <el-popover
                  popper-class="sql-table-info"
                  placement="right"
                  :width="502"
                  :persistent="false"
                  @show="getNodeField(datasourceTableData[index])"
                  trigger="click"
                >
                  <template #reference>
                    <el-icon class="hover-icon">
                      <Icon name="icon_info_outlined"></Icon>
                    </el-icon>
                  </template>
                  <div class="table-filed" v-loading="gridDataLoading">
                    <div class="top flex-align-center">
                      <div class="title ellipsis">
                        {{
                          datasourceTableData[index].name || datasourceTableData[index].tableName
                        }}
                      </div>
                      <el-icon
                        class="hover-icon de-hover-icon-primary"
                        @click.stop="
                          copyInfo(
                            datasourceTableData[index].name || datasourceTableData[index].tableName
                          )
                        "
                      >
                        <Icon name="icon_copy_outlined"></Icon>
                      </el-icon>
                      <div class="num flex-align-center">
                        <el-icon>
                          <Icon name="icon_text-box_outlined"></Icon>
                        </el-icon>
                        {{ gridData.length }}
                      </div>
                    </div>
                    <div class="table-grid">
                      <el-table
                        height="405"
                        style="width: 100%"
                        header-cell-class-name="header-cell"
                        :data="gridData"
                      >
                        <el-table-column label="物理字段名">
                          <template #default="scope">
                            <div class="flex-align-center icon">
                              <el-icon>
                                <Icon
                                  :className="`field-icon-${fieldType[scope.row.deType]}`"
                                  :name="`field_${fieldType[scope.row.deType]}`"
                                ></Icon>
                              </el-icon>
                              {{ scope.row.originName }}
                            </div>
                          </template>
                        </el-table-column>
                        <el-table-column :label="t('common.label')">
                          <template #default="scope">
                            {{ scope.row.description || '-' }}
                          </template>
                        </el-table-column>
                        <el-table-column :label="t('common.operate')">
                          <template #default="scope">
                            <el-icon
                              class="hover-icon de-hover-icon-primary"
                              @click.stop="copyInfo(scope.row.originName)"
                            >
                              <Icon name="icon_copy_outlined"></Icon>
                            </el-icon>
                          </template>
                        </el-table-column>
                      </el-table>
                    </div>
                  </div>
                </el-popover>
              </span>
            </div>
          </template>
        </FixedSizeList>
      </div>
    </div>
    <div class="sql-code-right" :style="{ width: `calc(100% - ${showLeft ? LeftWidth : 0}px)` }">
      <code-mirror
        @change="changeFlagCode = true"
        :height="`${dragHeight}px`"
        dom-id="sql-editor"
        ref="myCm"
      ></code-mirror>
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
            <el-auto-resizer>
              <template #default="{ height, width }">
                <el-table-v2
                  :columns="state.fields"
                  v-loading="dataPreviewLoading"
                  header-class="header-cell"
                  :data="state.plxTableData"
                  :width="width"
                  :height="height"
                  fixed
                  ><template #empty>
                    <empty-background description="暂无数据" img-type="noneWhite" /> </template
                ></el-table-v2>
              </template>
            </el-auto-resizer>
          </div>
          <template v-else>
            <empty-background description=" " img-type="noneWhite">
              <div class="sql-tips flex-align-center">
                点击上方
                <el-icon>
                  <icon name="icon_play-round_outlined"></icon>
                </el-icon>
                运行，即可查看运行结果
              </div>
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
                  {{ t('common.copy') }}
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
      <el-icon style="font-size: 16px">
        <Icon name="icon_info_colorful"></Icon>
      </el-icon>
      {{ t('dataset.sql_variable_limit_1') }}<br />
      {{ t('dataset.sql_variable_limit_2') }}<br />
    </div>
    <el-table header-cell-class-name="header-cell" :data="state.variablesTmp">
      <el-table-column width="220" prop="variableName" :label="t('visualization.param_name')" />
      <el-table-column width="200" :label="t('deDataset.parameter_type')">
        <template #default="scope">
          <el-cascader
            class="select-type"
            popper-class="cascader-panel no-scroll_bar"
            v-model="scope.row.type"
            :options="fieldOptions"
            @change="scope.row.defaultValue = ''"
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
          <div
            v-if="getIconName(scope.row.type[0]) === 'time'"
            class="ed-input ed-input--light ed-input-group ed-input-group--prepend de-group__prepend"
          >
            <div class="ed-input-group__prepend">
              <el-select v-model="scope.row.defaultValueScope" style="width: calc(100% + 22px)">
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
              format="YYYY"
              value-format="YYYY"
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
      <template #empty>
        <empty-background description="暂无数据" img-type="noneWhite" />
      </template>
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
    z-index: 5;
    cursor: pointer;
    margin: 0;
    display: flex;
    align-items: center;
    left: 0;
    height: 24px;
    width: 20px;
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    display: flex;
    align-items: center;
    padding-left: 2px;
    border-top-right-radius: 12px;
    border-bottom-right-radius: 12px;
    font-size: 12px;
    background: #fff;
    &:hover {
      padding-left: 4px;
      width: 24px;
      .ed-icon {
        color: var(--ed-color-primary, #3370ff);
      }
    }
  }

  .table-list {
    height: 100%;
    width: 240px;
    float: left;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    border-right: 1px solid rgba(31, 35, 41, 0.15);

    .list-item_primary {
      padding: 8px;
    }
    .table-list-top {
      padding: 16px;
      padding-bottom: 0;
    }

    .select-ds {
      font-size: 14px;
      font-weight: 500;
      display: flex;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);
      position: relative;

      i {
        cursor: pointer;
        font-size: 12px;
        color: var(--deTextPlaceholder, rgba(31, 35, 41, 0.15));
      }

      .left-outlined {
        position: absolute;
        font-size: 12px;
        right: -30px;
        top: -5px;
        height: 24px;
        border: 1px solid #dee0e3;
        width: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #fff;
        box-shadow: 0px 5px 10px 0px #1f23291a;
        z-index: 10;
        &:hover {
          .ed-icon {
            color: var(--ed-color-primary, #3370ff) !important;
          }
        }
      }
    }

    .table-num {
      .num {
        display: flex;
        align-items: center;
        font-weight: 400;
        font-size: 14px;
        color: #646a73;
        .ed-icon {
          margin-right: 5.33px;
        }
      }

      i {
        cursor: auto;
        font-size: 16px;
        color: var(--deTextPlaceholder, #646a73);
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
      padding: 0 8px;

      .list-item_primary {
        padding-right: 4px;
        .label {
          width: calc(100% - 4px);
        }
        &:hover {
          .label {
            width: calc(100% - 74px);
          }
        }
      }

      .not-allow {
        cursor: not-allowed;
        color: var(--deTextDisable, #bbbfc4);
      }

      .name-copy {
        display: none;
        line-height: 24px;
        margin-left: 4px;
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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
          top: 4px;
          left: 0;
          height: 7px;
          width: 100%;
          cursor: row-resize;
          &::after {
            content: '';
            height: 7px;
            width: 100px;
            border-radius: 3.5px;
            position: absolute;
            left: 50%;
            top: 0;
            transform: translateX(-50%);
            background: rgba(31, 35, 41, 0.1);
          }
        }
      }

      .padding-24 {
        width: calc(100% - 48px);
        .border-bottom-tab(24px);
      }

      .table-sql {
        margin-top: 1px;
        height: calc(100% - 46px);
        width: 100%;
        overflow: auto;
        box-sizing: border-box;

        .table-scroll {
          .ed-table-v2 {
            --ed-table-header-bg-color: #f5f6f7;
            :deep(.header-cell) {
              border-top: none;
            }
          }
          width: 100%;
          height: 100%;
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
          background: var(--ed-color-primary, #3370ff);
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
          color: var(--ed-color-primary, #3370ff);
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
  padding: 0 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  .name {
    width: 240px;
    margin: 8px;
  }

  .save-or-cancel {
    margin-left: auto;

    .ed-button--primary:focus {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-color-primary);
      background-color: var(--ed-color-primary);
    }

    .ed-button--primary:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
    }

    .ed-button--primary:active {
      color: var(--ed-button-active-text-color);
      border-color: var(--ed-button-active-border-color);
      background-color: var(--ed-button-active-bg-color);
    }
    .ed-divider--vertical {
      margin: 0 10px 0 16px;
    }

    .is-text:hover {
      background: rgba(31, 35, 41, 0.1);
    }
  }
}
.icon-color {
  color: #646a73;
}
</style>
<style lang="less">
.no-scroll_bar {
  .ed-cascader-menu__wrap.ed-scrollbar__wrap {
    height: 240px;
  }
}
.de-hover-icon-primary {
  color: var(--ed-color-primary) !important;
}
.sql-tips {
  color: #646a73;
  text-align: center;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
  margin-top: -35px;
  .ed-icon {
    margin: 0 4px;
  }
}
.sql-table-info {
  padding: 0 !important;
  height: 480px;
  .table-filed {
    height: 480px;
    .top {
      padding: 16px;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      .title {
        max-width: 50%;
      }
      .num {
        margin-left: auto;
        color: #646a73;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
        line-height: 22px;
        .ed-icon {
          margin-right: 4px;
          font-size: 16px;
        }
      }
    }

    .table-grid {
      padding: 16px;
      height: 423px;
      padding-bottom: 0;
      overflow-y: auto;
    }
  }
}
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
  .ed-empty__description {
    margin-top: 8px;
    p {
      line-height: 22px;
    }
  }
  .ed-input-group__prepend {
    padding: 0 11px;
  }
  .de-group__prepend {
    .ed-date-editor {
      flex: 1;
      .ed-input__wrapper {
        border-top-left-radius: 0;
        border-bottom-left-radius: 0;
        width: 100%;
      }
    }
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
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-weight: 400;

    .ed-icon {
      position: absolute;
      top: 10.6px;
      left: 16px;
      font-size: 14px;
      color: var(--ed-color-primary, #3370ff);
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
