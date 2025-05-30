<template>
  <el-dialog
    class="params-class"
    :append-to-body="true"
    :title="t('visualization.outer_param_set')"
    v-model="state.outerParamsSetVisible"
    width="80vw"
    top="10vh"
    trigger="click"
  >
    <el-row style="height: 550px">
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="6" class="preview-left">
            <el-row class="tree-head">
              <span class="head-text">{{ t('visualization.params_list') }}</span>
              <span class="head-filter">
                <el-button type="primary" icon="Plus" text @click="addOuterParamsInfo"> </el-button>
              </span>
            </el-row>
            <el-row class="tree-content">
              <el-tree
                class="custom-tree"
                menu
                ref="outerParamsInfoTree"
                :data="state.outerParamsInfoArray"
                node-key="paramsInfoId"
                highlight-current
                :props="state.treeProp"
                @node-click="nodeClick"
              >
                <template #default="{ node, data }">
                  <span class="custom-tree-node" style="display: flex">
                    <span v-if="!(curEditDataId === data.paramsInfoId)">
                      <div @click.stop>
                        <span class="auth-span">
                          <el-checkbox
                            v-model="data.checked"
                            @change="sourceFieldCheckedChange(data)"
                          />
                        </span>
                      </div>
                    </span>
                    <span :id="'paramName-' + data.paramsInfoId" style="flex: 1">
                      <div
                        v-if="curEditDataId === data.paramsInfoId"
                        style="width: 100%; padding: 0 15px"
                      >
                        <el-input
                          style="width: 100%"
                          v-model="data.paramName"
                          :placeholder="$t('visualization.input_param_name')"
                          @blur="closeEdit(data)"
                        />
                      </div>
                      <span class="tree-select-field" v-else-if="data.paramName">
                        {{ data.paramName }}
                      </span>
                      <span class="tree-select-field" v-else
                        >{{ t('visualization.no_setting_params_name_tip') }}
                      </span>
                    </span>
                    <span class="icon-more" v-if="!(curEditDataId === data.paramsInfoId)">
                      <handle-more
                        style="margin-right: 15px"
                        @handle-command="cmd => outerParamsOperation(cmd, node, data)"
                        :menu-list="state.optMenu"
                        :icon-name="icon_more_vertical_outlined"
                        placement="bottom-start"
                      />
                    </span>
                  </span>
                </template>
              </el-tree>
            </el-row>
          </el-col>
          <el-col :span="13" class="preview-show">
            <el-row v-if="state.curNodeId">
              <el-row class="new-params-title">
                {{ t('visualization.select_params_connect_component') }}
              </el-row>
              <el-row class="new-params-filter" v-if="state.outerParamsInfo?.filterInfo?.length">
                <div style="display: flex" class="inner-content">
                  <div style="width: 16px; margin-top: 2px" class="expand-custom-outer">
                    <div class="expand-custom">
                      <el-icon @click="() => (state.filterExpand = !state.filterExpand)"
                        ><CaretBottom v-show="state.filterExpand" />
                        <CaretRight v-show="!state.filterExpand" />
                      </el-icon>
                    </div>
                  </div>
                  <div style="flex: 1">{{ t('visualization.filter_component') }}</div>
                  <div style="flex: 1">{{ t('visualization.connection_condition') }}</div>
                </div>
                <div class="outer-filter-content">
                  <div
                    v-show="state.filterExpand"
                    style="display: flex"
                    class="inner-filter-content"
                    v-for="(baseFilter, index) in state.outerParamsInfo?.filterInfo"
                    :key="index"
                  >
                    <div style="width: 16px"></div>
                    <div style="flex: 1; line-height: 32px">
                      <Icon name="filter-params"
                        ><filterParams style="margin-top: 4px" class="svg-icon view-type-icon"
                      /></Icon>
                      <span>{{ findFilterName(baseFilter.id) }}</span>
                    </div>
                    <div style="flex: 1">
                      <el-select
                        v-model="baseFilter.filterSelected"
                        filterable
                        style="width: 100%"
                        :placeholder="t('v_query.select_query_condition')"
                        clearable
                      >
                        <el-option
                          v-for="item in baseFilter.propValue"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        >
                          <span style="font-size: 12px"> {{ item.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </div>
                </div>
              </el-row>
              <el-row class="new-params-ds" v-if="state.outerParamsInfo?.datasetInfo?.length">
                <div style="display: flex" class="inner-content">
                  <div style="width: 16px; margin-top: 2px" class="expand-custom-outer">
                    <div class="expand-custom">
                      <el-icon @click="() => (state.datasetExpand = !state.datasetExpand)"
                        ><CaretBottom v-show="state.datasetExpand" />
                        <CaretRight v-show="!state.datasetExpand" />
                      </el-icon>
                    </div>
                  </div>
                  <div style="flex: 1">{{ t('visualization.view') }}</div>
                  <div style="flex: 1">{{ t('visualization.connection_params_fields') }}</div>
                </div>
                <div class="outer-dataset-content">
                  <div
                    v-show="state.datasetExpand"
                    class="inner-dataset-content"
                    v-for="(baseDatasetInfo, index) in state.outerParamsInfo?.datasetInfo"
                    :key="index"
                  >
                    <div style="display: flex; width: 100%">
                      <div style="width: 16px; margin-top: 7px" class="expand-custom-outer">
                        <div class="expand-custom">
                          <el-icon
                            @click="
                              () => (baseDatasetInfo.viewExpand = !baseDatasetInfo.viewExpand)
                            "
                            ><CaretBottom v-show="baseDatasetInfo.viewExpand" />
                            <CaretRight v-show="!baseDatasetInfo.viewExpand" />
                          </el-icon>
                        </div>
                      </div>
                      <div style="flex: 1; display: flex; line-height: 32px">
                        <div style="width: 16px; margin-top: 2px; margin-right: 4px">
                          <el-icon>
                            <Icon name="icon_dataset"><icon_dataset class="svg-icon" /></Icon>
                          </el-icon>
                        </div>
                        <span>{{ baseDatasetInfo.name }}</span>
                      </div>
                      <div style="flex: 1; margin-left: -16px">
                        <el-select
                          v-model="baseDatasetInfo.fieldIdSelected"
                          filterable
                          clearable
                          style="width: 100%"
                          :placeholder="t('common.selectText')"
                        >
                          <template #header>
                            <el-tabs
                              class="params-select--header"
                              v-model="baseDatasetInfo.activelist"
                            >
                              <el-tab-pane
                                :label="t('visualization.fields')"
                                name="dimensionList"
                              ></el-tab-pane>
                              <el-tab-pane
                                :label="t('visualization.params')"
                                name="parameterList"
                              ></el-tab-pane>
                            </el-tabs>
                          </template>
                          <el-option
                            v-for="item in findFields(
                              baseDatasetInfo.activelist,
                              baseDatasetInfo.datasetFields
                            )"
                            :key="item.attachId"
                            :label="item.name"
                            :value="item.attachId"
                          >
                            <Icon
                              ><component
                                class="svg-icon"
                                style="width: 14px; height: 14px"
                                :class="`field-icon-${fieldType[item.deType]}`"
                                :is="iconFieldMap[fieldType[item.deType]]"
                              ></component
                            ></Icon>
                            <span style="font-size: 12px">{{ item.name }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </div>

                    <div class="ds-view-content" v-show="baseDatasetInfo.viewExpand">
                      <div style="display: flex; width: 100%; height: 22px">
                        <div class="ds-content-title">
                          {{ t('visualization.select_params_connect_view') }}
                        </div>
                        <div class="custom-view-diver"></div>
                        <div>
                          <el-checkbox
                            style="margin-top: -4px"
                            v-model="baseDatasetInfo.checkAll"
                            :indeterminate="baseDatasetInfo.checkAllIsIndeterminate"
                            :disabled="!baseDatasetInfo.fieldIdSelected"
                            @change="batchSelectChange($event, baseDatasetInfo)"
                            >{{ t('visualization.select_all') }}</el-checkbox
                          >
                        </div>
                      </div>
                      <div style="display: flex; flex-wrap: wrap; width: 100%">
                        <div
                          class="view-item"
                          v-for="viewInfo in baseDatasetInfo.datasetViews"
                          :key="viewInfo"
                        >
                          <div>
                            <el-checkbox
                              v-model="viewInfo.checked"
                              :disabled="!baseDatasetInfo.fieldIdSelected"
                              @change="datasetInfoChange(baseDatasetInfo)"
                            />
                          </div>
                          <div>
                            <Icon
                              ><component
                                class="svg-icon view-type-icon"
                                style="margin: 0 4px"
                                :is="iconChartMap[viewInfo.chartType]"
                              ></component
                            ></Icon>
                          </div>
                          <span style="font-size: 12px"> {{ viewInfo.chartName }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </el-row>
            </el-row>
            <div v-else class="empty">
              <empty-background
                :description="t('visualization.setting_params_tips')"
                img-type="noneWhite"
              />
            </div>
          </el-col>
          <el-col :span="5" class="params-attach-setting">
            <el-row v-if="state.curNodeId">
              <el-row class="new-params-title">{{ t('visualization.setting_params') }} </el-row>
              <el-row class="params-attach-content">
                <el-row>
                  <el-checkbox v-model="state.outerParamsInfo.required"
                    >{{ t('visualization.required') }}
                  </el-checkbox>
                </el-row>
                <el-row>
                  <el-checkbox v-model="state.outerParamsInfo.enabledDefault"
                    >{{ t('visualization.default_value') }}
                  </el-checkbox>
                  <el-tooltip class="item" placement="bottom">
                    <template #content>
                      <div>
                        {{ t('visualization.default_value_tips1') }} <br />
                        {{ t('visualization.default_value_tips2') }}
                      </div>
                    </template>
                    <el-icon class="hint-icon">
                      <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </el-row>
                <el-input
                  :ref="el => setArgRef(el, state.outerParamsInfo.paramsInfoId)"
                  :placeholder="t('visualization.default_value_tips3')"
                  v-model="state.outerParamsInfo.defaultValue"
                  type="textarea"
                  :autosize="{ minRows: 4, maxRows: 8 }"
                  @change="
                    val =>
                      validateArgs(
                        state.outerParamsInfo.defaultValue,
                        state.outerParamsInfo.paramsInfoId
                      )
                  "
                />
              </el-row>
            </el-row>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button @click="cancel()">{{ t('commons.cancel') }} </el-button>
        <el-button type="primary" @click="save()">{{ t('commons.confirm') }} </el-button>
      </el-row>
    </el-row>
  </el-dialog>
</template>

<script setup lang="ts">
import _delete from '@/assets/svg/icon_delete-trash_outlined.svg'
import edit from '@/assets/svg/icon_rename_outlined.svg'
import icon_more_vertical_outlined from '@/assets/svg/icon_more-vertical_outlined.svg'
import filterParams from '@/assets/svg/filter-params.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import { ref, reactive, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElCol, ElIcon, ElInput, ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { queryWithVisualizationId, updateOuterParamsSet } from '@/api/visualization/outerParams'
import { queryOuterParamsDsInfo, viewDetailList } from '@/api/visualization/dataVisualization'
import HandleMore from '@/components/handle-more/src/HandleMore.vue'
import { fieldType } from '@/utils/attr'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { iconChartMap } from '../icon-group/chart-list'
import { iconFieldMap } from '../icon-group/field-list'
import Icon from '../icon-custom/src/Icon.vue'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, componentData } = storeToRefs(dvMainStore)
const outerParamsInfoTree = ref(null)
const { t } = useI18n()
const curEditDataId = ref(null)
const snapshotStore = snapshotStoreWithOut()
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'

const state = reactive({
  filterExpand: true,
  datasetExpand: true,
  loading: false,
  outerParamsSetVisible: false,
  optMenu: [
    {
      label: t('visualization.rename'),
      svgName: edit,
      command: 'rename'
    },
    {
      label: t('visualization.delete'),
      svgName: _delete,
      command: 'delete'
    }
  ],
  treeProp: {
    id: 'paramsInfoId',
    label: 'paramName',
    children: 'children'
  },
  outerParams: {
    checked: false,
    outerParamsInfoArray: []
  },
  baseDatasetInfo: [],
  baseFilterInfo: [],
  outerParamsInfoArray: [],
  mapOuterParamsInfoArray: {},
  panelList: [],
  curNodeId: null,
  outerParamsInfo: {
    content: '',
    linkType: '',
    required: false,
    enabledDefault: false,
    defaultValue: null,
    targetViewInfoList: [],
    paramsInfoId: null
  },
  currentFiledTreeNode: null,
  defaultOuterParamsInfo: {
    paramName: '',
    checked: true,
    required: false,
    enabledDefault: false,
    defaultValue: null,
    targetViewInfoList: []
  },
  defaultTargetViewInfo: {
    targetViewId: null,
    targetFieldId: null
  },
  currentLinkPanelViewArray: [],
  viewIdFieldArrayMap: {},
  widgetSubjectsTrans: {
    timeYearWidget: t('visualization.time_year_widget'),
    timeMonthWidget: t('visualization.time_month_widget'),
    timeDateWidget: t('visualization.time_date_widget'),
    timeDateRangeWidget: t('visualization.time_date_range_widget'),
    textSelectWidget: t('visualization.text_select_widget'),
    textSelectGridWidget: t('visualization.time_year_widget'),
    textInputWidget: t('visualization.text_input_widget'),
    textSelectTreeWidget: t('visualization.text_select_tree_widget'),
    numberSelectWidget: t('visualization.number_select_widget'),
    numberSelectGridWidget: t('visualization.number_select_grid_widget'),
    numberRangeWidget: t('visualization.number_range_widget')
  }
})

const argRefs = ref({})

const setArgRef = (el, id) => {
  if (el) {
    argRefs.value[id] = el
  }
}

const validateArgs = (val, id) => {
  const cref = argRefs.value[id]
  const e = cref.input
  if (val === null || val === '' || typeof val === 'undefined') {
    e.style.color = null
    e.parentNode.removeAttribute('style')
    const child = e.parentNode.querySelector('.error-msg')
    if (child) {
      e.parentNode.removeChild(child)
    }
    return true
  }
  try {
    JSON.parse(val)
    e.style.color = null
    e.parentNode.removeAttribute('style')
    const child = e.parentNode.querySelector('.error-msg')
    if (child) {
      e.parentNode.removeChild(child)
    }
    return true
  } catch (error) {
    e.style.color = 'red'
    e.parentNode.setAttribute('style', 'box-shadow: 0 0 0 1px red inset;')
    const child = e.parentNode.querySelector('.error-msg')
    if (!child) {
      const errorDom = document.createElement('div')
      errorDom.className = 'error-msg'
      errorDom.innerText = t('visualization.format_error')
      e.parentNode.appendChild(errorDom)
    }
    return false
  }
}

const closeEdit = params => {
  if (!params.paramName || params.paramName.length < 2 || params.paramName.length > 25) {
    ElMessage({
      message: t('commons.params_value') + t('common.input_limit', [2, 25]),
      type: 'warning',
      showClose: true
    })
    if (params.paramName.length > 25) {
      params.paramName = params.paramName.splice(0.25)
    }
    return
  }
  curEditDataId.value = null
}

const outerParamsOperation = (cmd, node, data) => {
  if (cmd === 'rename') {
    curEditDataId.value = data.paramsInfoId
  } else if (cmd === 'delete') {
    removeOuterParamsInfo(node, data)
  }
}

const initParams = async () => {
  state.baseFilterInfo = []
  state.baseDatasetInfo = []
  // 同步过滤组件信息
  componentData.value.forEach(componentItem => {
    if (componentItem.component === 'VQuery') {
      state.baseFilterInfo.push(componentItem)
    } else if (componentItem.component === 'Group') {
      componentItem.propValue.forEach(groupItem => {
        if (groupItem.component === 'VQuery') {
          state.baseFilterInfo.push(groupItem)
        }
      })
    } else if (componentItem.component === 'DeTabs') {
      componentItem.propValue.forEach(tabItem => {
        tabItem.componentData.forEach(tabComponent => {
          if (tabComponent.component === 'VQuery') {
            state.baseFilterInfo.push(tabComponent)
          }
        })
      })
    }
  })
  // 同步基础数据集信息
  await queryOuterParamsDsInfo(dvInfo.value.id).then(rsp => {
    state.baseDatasetInfo = rsp.data
  })
  // 获取当前仪表板外部跳转信息
  queryWithVisualizationId(dvInfo.value.id).then(rsp => {
    state.outerParams = rsp.data
    state.outerParamsInfoArray = state.outerParams?.outerParamsInfoArray
    if (state.outerParamsInfoArray.length >= 1) {
      state.outerParamsInfoArray.forEach(outerParamsInfo => {
        const newBaseFilterInfo = deepCopy(state.baseFilterInfo)
        const newBaseDatasetInfo = deepCopy(state.baseDatasetInfo)
        paramsCheckedAdaptor(outerParamsInfo, newBaseFilterInfo, newBaseDatasetInfo)
        state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
      })
      state.curNodeId = null
      const firstNode = state.outerParamsInfoArray[0]
      nextTick(() => {
        outerParamsInfoTree.value.setCurrentKey(firstNode.paramsInfoId)
        nodeClick(firstNode)
      })
    }
  })

  getPanelViewList(dvInfo.value.id)
}

const findFields = (type, datasetFields) => {
  if (type === 'parameterList') {
    return datasetFields.filter(field => field.attachId.indexOf('DE') > -1)
  } else {
    return datasetFields.filter(field => field.attachId.indexOf('DE') === -1)
  }
}

const datasetInfoChange = datasetInfo => {
  let viewCheckCount = 0
  datasetInfo.datasetViews.forEach(dsView => {
    if (dsView['checked']) {
      viewCheckCount++
    }
  })
  datasetInfo['checkAll'] = viewCheckCount === datasetInfo.datasetViews.length
  datasetInfo['checkAllIsIndeterminate'] =
    viewCheckCount > 0 && viewCheckCount < datasetInfo.datasetViews.length
}

const paramsCheckedAdaptor = (outerParamsInfo, newBaseFilterInfo, newBaseDatasetInfo) => {
  const dsFieldIdSelected = {}
  const viewMatchIds = []
  outerParamsInfo.targetViewInfoList.forEach(targetViewInfo => {
    viewMatchIds.push(targetViewInfo.targetViewId)
    dsFieldIdSelected[targetViewInfo.targetDsId] =
      targetViewInfo.targetFieldId === 'empty'
        ? targetViewInfo.targetViewId
        : targetViewInfo.targetFieldId
  })
  if (newBaseDatasetInfo) {
    newBaseDatasetInfo.forEach(datasetInfo => {
      datasetInfo['fieldIdSelected'] = dsFieldIdSelected[datasetInfo.id]
      datasetInfo['viewExpand'] = true
      let viewCheckCount = 0
      datasetInfo.datasetViews.forEach(dsView => {
        if (viewMatchIds.includes(dsView.chartId)) {
          dsView['checked'] = true
          viewCheckCount++
        } else {
          dsView['checked'] = false
        }
      })
      datasetInfo['checkAll'] = viewCheckCount === datasetInfo.datasetViews.length
      datasetInfo['checkAllIsIndeterminate'] =
        viewCheckCount > 0 && viewCheckCount < datasetInfo.datasetViews.length
      if (datasetInfo['fieldIdSelected'] && datasetInfo['fieldIdSelected'].indexOf('DE') > -1) {
        datasetInfo['activelist'] = 'parameterList'
      } else {
        datasetInfo['activelist'] = 'dimensionList'
      }
    })
  }
  if (newBaseFilterInfo) {
    newBaseFilterInfo.forEach(filterInfo => {
      filterInfo['filterSelected'] = dsFieldIdSelected[filterInfo.id]
    })
  }
  outerParamsInfo['filterInfo'] = newBaseFilterInfo
  outerParamsInfo['datasetInfo'] = newBaseDatasetInfo
}

const cancel = () => {
  state.outerParamsSetVisible = false
}

const jsonArrayCheck = params => {
  try {
    const result = JSON.parse(params)
    return result instanceof Array
  } catch (error) {
    return false
  }
}

const save = () => {
  const outerParamsCopy = deepCopy(state.outerParams)
  let checkErrorNum = 0
  let checkNullErrorNum = 0
  let checkMessage = ''
  const paramNameArray = []
  outerParamsCopy.outerParamsInfoArray?.forEach(outerParamsInfo => {
    if (!outerParamsInfo.paramName || paramNameArray.includes(outerParamsInfo.paramName)) {
      checkNullErrorNum++
    }
    paramNameArray.push(outerParamsInfo.paramName)
    if (outerParamsInfo.defaultValue && !jsonArrayCheck(outerParamsInfo.defaultValue)) {
      checkErrorNum++
      checkMessage = checkMessage + `【${outerParamsInfo.paramName}】`
    }
    outerParamsInfo.targetViewInfoList = []
    outerParamsInfo.filterInfo?.forEach(baseFilterInfo => {
      // 存在过滤器选项被选
      if (baseFilterInfo.filterSelected) {
        outerParamsInfo.targetViewInfoList.push({
          targetViewId: baseFilterInfo.filterSelected,
          targetDsId: baseFilterInfo.id,
          targetFieldId: 'empty'
        })
      }
    })
    outerParamsInfo.datasetInfo?.forEach(baseDatasetInfo => {
      // 存在数据集字段被选中
      if (baseDatasetInfo.fieldIdSelected) {
        baseDatasetInfo.datasetViews?.forEach(dsView => {
          if (dsView.checked) {
            outerParamsInfo.targetViewInfoList.push({
              targetViewId: dsView.chartId,
              targetDsId: baseDatasetInfo.id,
              targetFieldId: baseDatasetInfo.fieldIdSelected
            })
          }
        })
      }
    })
  })
  if (checkErrorNum > 0) {
    ElMessage({
      message: t('visualization.params_setting_check_message'),
      type: 'warning',
      showClose: true
    })
    return
  }
  if (checkNullErrorNum > 0) {
    ElMessage({
      message: t('visualization.params_setting_check_message_tips'),
      type: 'warning',
      showClose: true
    })
    return
  }
  updateOuterParamsSet(outerParamsCopy).then(() => {
    ElMessage({
      message: t('commons.save_success'),
      type: 'success',
      showClose: true
    })
    snapshotStore.recordSnapshotCache('renderChart')
    cancel()
  })
}

const nodeClick = data => {
  state.outerParamsInfo = state.mapOuterParamsInfoArray[data.paramsInfoId]
  state.curNodeId = data.paramsInfoId
}

// 获取当前图表字段 关联仪表板的图表信息列表
const getPanelViewList = dvId => {
  viewDetailList(dvId).then(rsp => {
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = rsp.data
    if (state.currentLinkPanelViewArray) {
      state.currentLinkPanelViewArray.forEach(view => {
        state.viewIdFieldArrayMap[view.id] = view.tableFields
      })
    }
    // 增加过滤组件匹配
    componentData.value.forEach(componentItem => {
      if (componentItem.component === 'VQuery') {
        componentItem.propValue.forEach(filterItem => {
          state.currentLinkPanelViewArray.push({
            id: filterItem.id,
            type: 'filter',
            name: filterItem.name,
            title: filterItem.name
          })
          state.viewIdFieldArrayMap[filterItem.id] = [
            { id: 'empty', name: t('visualization.filter_no_select') }
          ]
        })
      }
    })
  })
}

const initSelected = data => {
  nextTick(() => {
    outerParamsInfoTree.value.setCurrentKey(data.paramsInfoId)
    nodeClick(data)
  })
}

const sourceFieldCheckedChange = data => {
  if (data.checked) {
    state.outerParams.checked = true
  }
  nextTick(() => {
    outerParamsInfoTree.value.setCurrentKey(data.paramsInfoId)
    nodeClick(data)
  })
}

const addOuterParamsInfo = () => {
  state.outerParams.checked = true
  const outerParamsInfo = deepCopy(state.defaultOuterParamsInfo)
  outerParamsInfo['paramsInfoId'] = generateID()
  const newBaseFilterInfo = deepCopy(state.baseFilterInfo)
  const newBaseDatasetInfo = deepCopy(state.baseDatasetInfo)
  paramsCheckedAdaptor(outerParamsInfo, newBaseFilterInfo, newBaseDatasetInfo)
  state.outerParamsInfoArray.push(outerParamsInfo)
  state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
  curEditDataId.value = outerParamsInfo['paramsInfoId']
  initSelected(outerParamsInfo)
}

const removeOuterParamsInfo = (node, data) => {
  const parent = node.parent
  const children = parent.data.children || parent.data
  const index = children.findIndex(d => d.paramsInfoId === data.paramsInfoId)
  children.splice(index, 1)
  if (data.paramsInfoId === state.outerParamsInfo.paramsInfoId) {
    delete state.mapOuterParamsInfoArray[data.paramsInfoId]
    state.curNodeId = null
  }
}
const batchSelectChange = (value, baseDatasetInfo) => {
  // do change
  baseDatasetInfo.datasetViews.forEach(viewInfo => {
    viewInfo.checked = value
  })
  baseDatasetInfo.checkAll = value
  baseDatasetInfo.checkAllIsIndeterminate = false
}

const optInit = () => {
  initParams()
  state.outerParamsSetVisible = true
}

const findFilterName = id => {
  return dvMainStore.canvasViewInfo[id]?.title
}

defineExpose({
  optInit
})
</script>

<style scoped lang="less">
.root-class {
  margin: 15px 0px 5px;
  justify-content: right;
}

.preview {
  margin-top: 5px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  height: 470px !important;
  overflow: hidden;
  background-size: 100% 100% !important;
}

.tree-head {
  height: 40px;
  line-height: 40px;
  font-size: 12px;
  color: #3d4d66;
  border-bottom: 1px solid #e6e6e6;
  .head-text {
    margin-left: 16px;
    font-weight: 500;
    font-size: 14px;
    color: #1f2329;
  }
  .head-filter {
    flex: 1;
    text-align: right;
    margin-right: 16px;
    font-weight: 400;
    font-size: 12px;
    color: #646a73;
  }
}

:deep(.ed-row) {
  width: 100%;
}

.m-del-icon-btn {
  color: #646a73;
  margin-top: 4px;
  margin-left: 4px;

  &:hover {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:focus {
    background: rgba(31, 35, 41, 0.1) !important;
  }
  &:active {
    background: rgba(31, 35, 41, 0.2) !important;
  }
}

.empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}
.preview-show {
  border-left: 1px solid #e6e6e6;
  background-size: 100% 100% !important;
  height: 100%;
  overflow-y: auto;
}

.view-type-icon {
  color: #3370ff;
  width: 22px;
  height: 14px;
}

.custom-tree {
  height: 100%;
  width: 100%;
  overflow-y: auto;
  background: none;
  :deep(.ed-tree-node__expand-icon) {
    display: none;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;

  .icon-more {
    margin-left: auto;
    display: none;
  }

  &:hover .icon-more {
    margin-left: auto;
    display: unset;
  }
}

.link-icon-join {
  font-size: 20px;
  margin-top: 7px;
  margin-left: 8px;
  margin-right: 8px;
}

.inner-content {
  width: 100%;
  font-size: 14px;
}

.outer-filter-content {
  width: 100%;
}

.outer-dataset-content {
  width: 100%;
  padding-left: 16px;
}

.inner-filter-content {
  width: 100%;
  margin-top: 12px;
}

.inner-dataset-content {
  width: 100%;
  margin-top: 12px;
}

.slot-class {
  color: white;
}

.bottom {
  margin-top: 15px;
  text-align: center;
}

.ellip {
  /*width: 100%;*/
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  text-align: center;
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed {
  /*width: 100%;*/
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
}

:deep(.ed-popover) {
  height: 200px;
  overflow: auto;
}

.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
}

.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}

:deep(.vue-treeselect__control) {
  height: 28px;
}

:deep(.vue-treeselect__single-value) {
  color: #606266;
  line-height: 28px !important;
}

.auth-span {
  float: right;
  width: 16px;
  margin-right: 8px;
  margin-left: 16px;
}

.tree-content {
  height: calc(100% - 70px);
  overflow-y: auto;
}

.tree-bottom {
  margin-top: 7px;
  text-align: center;
}

:deep(.vue-treeselect__placeholder) {
  line-height: 28px;
}

:deep(.ed-tree--highlight-current .ed-tree-node.is-current > .ed-tree-node__content) {
  background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1)) !important;
}

.tree-content ::deep(.ed-input__inner) {
  background: transparent;
  border: 0px !important;
}

.params-class ::deep(.ed-dialog__title) {
  font-size: 14px;
}

.params-class ::deep(.ed-dialog__headerbtn) {
  z-index: 2;
}

.params-class ::deep(.ed-dialog__header) {
  padding: 20px 20px 0;
}

.params-class ::deep(.ed-dialog__body) {
  padding: 10px 20px 20px;
}

.new-params-title {
  height: 56px;
  font-size: 14px;
  font-weight: 500;
  padding: 16px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}

.new-params-filter {
  padding: 16px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}

.new-params-ds {
  padding: 16px;
}

.expand-custom {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  padding: 0px 1px;
  color: rgba(100, 106, 115, 1);
  &:hover {
    background: rgba(31, 35, 41, 0.1);
    cursor: pointer;
  }
}

.ds-view-content {
  width: calc(100% - 16px);
  border-radius: 4px;
  margin: 8px 16px 0 16px;
  padding: 12px;
  background: rgba(245, 246, 247, 1);
}

.ds-content-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(100, 106, 115, 1);
}

.custom-view-diver {
  width: 1px;
  margin: 4px 8px;
  height: 14px;
  background: rgba(31, 35, 41, 0.15);
}

.preview-left {
  background: rgba(245, 246, 247, 1);
  height: 100%;
  overflow-y: hidden;
}

.view-item {
  display: flex;
  width: 50%;
  line-height: 32px;
}

.ed-select-dropdown__header {
  padding: 0 8px;
  .params-select--header {
    --ed-tabs-header-height: 32px;
    .ed-tabs__item {
      font-weight: 400;
      font-size: 15px;
    }
  }
}

.expand-custom-outer {
  margin-right: 4px;
}

.params-attach-setting {
  border-left: 1px solid #e6e6e6;
}

.params-attach-content {
  padding: 16px;
}

:deep(.error-msg) {
  color: red;
  position: fixed;
  z-index: 9;
  font-size: 10px;
  height: 10px;
  margin-bottom: 12px;
  margin-right: -80px;
}

.hint-icon {
  cursor: pointer;
  font-size: 14px;
  color: #646a73;
  margin: 3px 0 0 4px;
}
</style>
