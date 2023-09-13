<template>
  <el-dialog
    ref="enlargeDialog"
    :append-to-body="true"
    :title="t('visualization.jump_set')"
    v-model="dialogShow"
    width="70vw"
    top="10vh"
    trigger="click"
  >
    <div @keydown.stop @keyup.stop v-if="state.initState" style="height: 550px">
      <el-row style="flex-direction: row">
        <div class="top-area">
          <span class="top-area-text">已选图表：</span>
          <span class="top-area-value">
            <Icon class-name="view-type-icon" :name="state.curJumpViewInfo.type" />
            {{ state.curJumpViewInfo.title }}</span
          >
          <span class="top-area-text margin-left">所用数据集：</span>
          <span class="top-area-value">
            <Icon
              style="vertical-align: -0.2em"
              class-name="view-type-icon"
              name="dataset-outline"
            />
            {{ state.curDatasetInfo.name }}</span
          >
        </div>
      </el-row>
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: auto">
            <el-row class="tree-head">
              <span class="head-text">选择字段</span>
              <span class="head-filter">
                仅看已选
                <el-switch size="small" v-model="state.showSelected" />
              </span>
            </el-row>
            <el-tree
              menu
              ref="linkJumpInfoTree"
              :filter-node-method="filterNodeMethod"
              :data="state.linkJumpInfoXArray"
              node-key="sourceFieldId"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClick"
            >
              <template #default="{ data }">
                <span class="custom-tree-node">
                  <span>
                    <div @click.stop>
                      <span class="auth-span">
                        <el-checkbox
                          v-model="data.checked"
                          @change="sourceFieldCheckedChange(data)"
                        />
                      </span>
                    </div>
                  </span>
                  <span>
                    <span class="tree-select-field">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType[data.sourceDeType]}`"
                          :className="`field-icon-${fieldType[data.sourceDeType]}`"
                        />
                      </el-icon>
                      {{ data.sourceFieldName }}
                    </span>
                  </span>
                </span>
              </template>
            </el-tree>
          </el-col>
          <el-col :span="16" class="preview-show">
            <el-container class="settings-container">
              <el-header class="settings-header">
                <el-form-item class="radio-group-box">
                  <template #label>
                    <span class="title">{{ t('visualization.link_type') }}</span>
                  </template>
                  <el-radio-group v-if="state.linkJumpInfo" v-model="state.linkJumpInfo.linkType">
                    <el-radio label="outer">{{ t('visualization.link_outer') }}</el-radio>
                    <el-radio label="inner">{{ t('visualization.link_panel') }}</el-radio>
                  </el-radio-group>
                  <el-radio-group v-if="!state.linkJumpInfo" disabled>
                    <el-radio label="outer">{{ t('visualization.link_outer') }}</el-radio>
                    <el-radio label="inner">{{ t('visualization.link_panel') }}</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item class="radio-group-box">
                  <template #label>
                    <span class="title">{{ t('visualization.open_model') }}</span>
                  </template>
                  <el-radio-group v-if="state.linkJumpInfo" v-model="state.linkJumpInfo.jumpType">
                    <el-radio label="_self">{{ t('visualization.now_window') }}</el-radio>
                    <el-radio label="_blank">{{ t('visualization.new_window') }}</el-radio>
                  </el-radio-group>
                  <el-radio-group v-if="!state.linkJumpInfo" disabled>
                    <el-radio label="_self">{{ t('visualization.now_window') }}</el-radio>
                    <el-radio label="_blank">{{ t('visualization.new_window') }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-header>

              <el-main class="settings-main">
                <template v-if="state.linkJumpInfo">
                  <template v-if="state.linkJumpInfo.linkType === 'inner'">
                    <el-form label-position="top" class="main-form">
                      <el-row :gutter="8">
                        <el-col :span="11">
                          <el-form-item>
                            <template #label> 当前仪表板 </template>
                            <el-select style="width: 100%" v-model="dvInfo.name" disabled>
                              <el-option
                                :key="dvInfo.name"
                                :label="dvInfo.name"
                                :value="dvInfo.name"
                              >
                              </el-option>
                            </el-select>
                          </el-form-item>
                        </el-col>
                        <el-col :span="2" class="icon-center">
                          <Icon style="width: 20px; height: 20px" name="dv-link-target" />
                        </el-col>
                        <el-col :span="11">
                          <el-form-item>
                            <template #label> 目标仪表板 </template>
                            <el-tree-select
                              v-model="state.linkJumpInfo.targetDvId"
                              :data="state.panelList"
                              :props="state.dvSelectProps"
                              :render-after-expand="false"
                              filterable
                              @node-click="dvNodeClick"
                              class="dv-selector"
                            >
                              <template #default="{ node, data }">
                                <el-icon v-if="data.leaf">
                                  <Icon name="dv-dashboard-spine"></Icon>
                                </el-icon>
                                <el-icon v-else>
                                  <Icon name="dv-folder"></Icon>
                                </el-icon>
                                <span :title="node.label">{{ node.label }}</span>
                              </template>
                            </el-tree-select>
                          </el-form-item>
                        </el-col>
                      </el-row>

                      <el-row :gutter="8">
                        <el-col :span="7"> 源字段 </el-col>
                        <el-col :span="2"></el-col>
                        <el-col :span="7">
                          {{ t('visualization.link_view_field') }}
                        </el-col>
                        <el-col :span="8"></el-col>
                      </el-row>

                      <div class="main-scrollbar-container">
                        <el-scrollbar height="fit-content" max-height="208px">
                          <el-row
                            :gutter="8"
                            style="margin-bottom: 6px"
                            v-for="(targetViewInfo, index) in state.linkJumpInfo.targetViewInfoList"
                            :key="index"
                          >
                            <el-col :span="8">
                              <el-select
                                v-model="targetViewInfo.sourceFieldActiveId"
                                :placeholder="'请选择字段'"
                                style="width: 100%"
                              >
                                <el-option
                                  v-for="curViewField in state.linkJumpCurViewFieldArray"
                                  :key="curViewField.id"
                                  :label="curViewField.name"
                                  :value="curViewField.id"
                                >
                                  <span style="float: left; font-size: 12px">
                                    {{ curViewField.name }}
                                  </span>
                                </el-option>
                              </el-select>
                            </el-col>
                            <el-col :span="1" class="icon-center">
                              <Icon style="width: 20px; height: 20px" name="dv-link-target" />
                            </el-col>
                            <el-col :span="7">
                              <el-select
                                v-model="targetViewInfo.targetViewId"
                                :disabled="!targetViewInfo.sourceFieldActiveId"
                                :placeholder="'请选择视图'"
                                style="width: 100%"
                                size="mini"
                                @change="viewInfoOnChange(targetViewInfo)"
                              >
                                <el-option
                                  v-for="item in state.currentLinkPanelViewArray"
                                  :key="item.id"
                                  :label="item.title"
                                  :value="item.id"
                                >
                                  <span style="float: left">
                                    <svg-icon
                                      :icon-class="item.type"
                                      style="width: 14px; height: 14px"
                                    />
                                  </span>
                                  <span style="float: left; font-size: 12px">{{ item.title }}</span>
                                </el-option>
                              </el-select>
                            </el-col>
                            <el-col :span="7">
                              <el-select
                                v-model="targetViewInfo.targetFieldId"
                                :placeholder="'请选择字段'"
                                :disabled="!targetViewInfo.sourceFieldActiveId"
                                style="width: 100%"
                                size="mini"
                              >
                                <el-option
                                  v-for="viewField in state.viewIdFieldArrayMap[
                                    targetViewInfo.targetViewId
                                  ]"
                                  :key="viewField.id"
                                  :label="viewField.name"
                                  :value="viewField.id"
                                >
                                  <span style="float: left">
                                    <svg-icon
                                      v-if="viewField.deType === 0"
                                      icon-class="field_text"
                                      class="field-icon-text"
                                    />
                                    <svg-icon
                                      v-if="viewField.deType === 1"
                                      icon-class="field_time"
                                      class="field-icon-time"
                                    />
                                    <svg-icon
                                      v-if="viewField.deType === 2 || viewField.deType === 3"
                                      icon-class="field_value"
                                      class="field-icon-value"
                                    />
                                    <svg-icon
                                      v-if="viewField.deType === 5"
                                      icon-class="field_location"
                                      class="field-icon-location"
                                    />
                                  </span>
                                  <span style="float: left; font-size: 12px">
                                    {{ viewField.name }}
                                  </span>
                                </el-option>
                              </el-select>
                            </el-col>
                            <el-col :span="1">
                              <el-icon
                                style="margin-top: 10px; cursor: pointer"
                                @click="deleteLinkJumpField(index)"
                              >
                                <Delete />
                              </el-icon>
                            </el-col>
                          </el-row>
                        </el-scrollbar>
                        <el-button
                          style="margin-top: 8px"
                          :disabled="!state.linkJumpInfo.targetDvId"
                          type="primary"
                          icon="Plus"
                          text
                          @click="addLinkJumpField"
                        >
                          {{ t('visualization.add_jump_field') }}
                        </el-button>
                      </div>
                    </el-form>
                  </template>

                  <template v-if="outerContentShow">
                    <el-row :gutter="8" class="main-form">
                      <el-col :span="16" style="height: 100%">
                        <div class="url-text">
                          {{ t('visualization.target_url') }}
                          <el-tooltip class="item" effect="dark" placement="bottom">
                            <template #content>
                              {{ $t('visualization.target_url_tips') }}
                            </template>
                            <el-icon>
                              <Icon name="icon_info_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div class="outer-content-mirror">
                          <jump-set-outer-content-editor
                            ref="outerContentEditor"
                            :link-jump-info="state.linkJumpInfo"
                            :link-jump-info-array="state.linkJumpInfoArray"
                          />
                        </div>
                      </el-col>
                      <el-col :span="8" style="height: 100%">
                        <div class="url-text">
                          {{ t('visualization.select_world') }}
                          <el-tooltip class="item" effect="dark" placement="bottom">
                            <template #content>
                              <span v-html="$t('chart.reference_field_tip')"></span>
                            </template>
                            <el-icon>
                              <Icon name="icon_info_outlined" />
                            </el-icon>
                          </el-tooltip>
                        </div>
                        <div class="outer-content-right">
                          <el-input
                            v-model="state.searchField"
                            :placeholder="t('dataset.search')"
                            :prefix-icon="Search"
                            clearable
                          />
                          <el-scrollbar style="margin-top: 8px" height="250px">
                            <span
                              v-for="item in state.linkJumpInfoArray.filter(
                                item =>
                                  !state.searchField ||
                                  item.sourceFieldName.indexOf(state.searchField) > -1
                              )"
                              :key="item.sourceFieldId"
                              class="item-dimension"
                              :title="item.sourceFieldName"
                              @click="insertFieldToCodeMirror('[' + item.sourceFieldName + ']')"
                            >
                              <el-icon>
                                <Icon
                                  :name="`field_${fieldType[item.sourceDeType]}`"
                                  :className="`field-icon-${fieldType[item.sourceDeType]}`"
                                />
                              </el-icon>
                              {{ item.sourceFieldName }}
                            </span>
                          </el-scrollbar>
                        </div>
                      </el-col>
                    </el-row>
                  </template>
                </template>
                <div v-else class="empty">
                  <el-empty
                    :description="t('visualization.select_dimension_hint')"
                    :image-size="125"
                  />
                </div>
              </el-main>
            </el-container>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button size="mini" @click="cancel()">{{ t('common.cancel') }} </el-button>
        <el-button type="primary" size="mini" @click="save()"
          >{{ t('dataset.confirm') }}
        </el-button>
      </el-row>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import {
  queryVisualizationJumpInfo,
  queryWithViewId,
  updateJumpSet,
  viewTableDetailList
} from '@/api/visualization/linkJump'
import { reactive, ref, nextTick, onMounted, computed, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { fieldType } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { ElContainer, ElFormItem, ElHeader, ElMessage, ElScrollbar } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetDetails, listFieldByDatasetGroup } from '@/api/dataset'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
import { CalcFieldType } from '@/views/visualized/data/dataset/form/CalcFieldEdit.vue'
import JumpSetOuterContentEditor from '@/components/visualization/JumpSetOuterContentEditor.vue'
import { Search } from '@element-plus/icons-vue'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewInfo } = storeToRefs(dvMainStore)
const linkJumpInfoTree = ref(null)
const { t } = useI18n()
const dialogShow = ref(false)
const searchField = ref('')

const state = reactive({
  showSelected: false,
  curJumpViewInfo: {},
  curDatasetInfo: {},
  tempId: null,
  initState: false,
  viewId: null,
  name2Auto: [],
  searchField: '',
  searchFunction: '',
  loading: false,
  inputType: 'self',
  fieldName: 'name',
  tableRadio: null,
  keyWordSearch: '',
  columnLabel: t('visualization.belong_to_category'),
  templateList: [],
  importTemplateInfo: {
    snapshot: ''
  },
  sourceViewFields: [],
  dvSelectProps: {
    label: 'name',
    children: 'children',
    value: 'id',
    isLeaf: 'leaf'
  },
  treeProp: {
    id: 'sourceFieldId',
    label: 'sourceFieldName',
    children: 'children'
  },
  linkJump: null,
  linkJumpInfoArray: [],
  linkJumpInfoXArray: [],
  linkJumpCurViewFieldArray: [],
  mapJumpInfoArray: {},
  panelList: [],
  linkJumpInfo: null,
  currentFiledTreeNode: null,
  defaultLinkJumpInfo: {
    linkType: 'outer',
    jumpType: '_self',
    targetViewInfoList: []
  },
  defaultTargetViewInfo: {
    targetViewId: null,
    targetFieldId: null
  },
  currentLinkPanelViewArray: [],
  viewIdFieldArrayMap: {},
  dimensionData: [],
  dimensionList: [],
  quotaList: [],
  quotaData: [],
  dimension: [],
  quota: []
})

const emits = defineEmits(['closeJumpSetDialog'])
const outerContentEditor = ref(null)

const dialogInit = viewItem => {
  console.log(viewItem)
  dialogShow.value = true
  state.initState = false
  init(viewItem)
}

const init = viewItem => {
  state.initState = false
  state.viewId = viewItem.id
  const chartDetails = canvasViewInfo.value[state.viewId] as ChartObj
  state.curJumpViewInfo = chartDetails
  let checkAllAxisStr =
    JSON.stringify(chartDetails.xAxis) +
    JSON.stringify(chartDetails.xAxisExt) +
    JSON.stringify(chartDetails.yAxis) +
    JSON.stringify(chartDetails.yAxisExt) +
    JSON.stringify(chartDetails.drillFields)
  let checkJumpStr
  // 堆叠图的可选参数分两种情况 1.如果有堆叠项 则指标只有第一个可选 2.如果没有堆叠项泽所有指标都可以选
  if (chartDetails.type.indexOf('stack') > -1 && chartDetails.extStack.length > 2) {
    const yAxisArray = chartDetails.yAxis
    const yAxisNew = yAxisArray.length > 0 ? JSON.stringify(yAxisArray[0]) : '[]'
    checkAllAxisStr =
      JSON.stringify(chartDetails.xAxis) +
      JSON.stringify(chartDetails.xAxisExt) +
      JSON.stringify(yAxisNew) +
      JSON.stringify(chartDetails.yAxisExt) +
      JSON.stringify(chartDetails.drillFields)
    checkJumpStr = checkAllAxisStr
  } else if (chartDetails.type === 'table-pivot') {
    checkJumpStr = checkAllAxisStr
  } else if (chartDetails.type === 'table-info') {
    checkJumpStr = JSON.stringify(chartDetails.xAxis) + JSON.stringify(chartDetails.drillFields)
  } else {
    checkJumpStr = checkAllAxisStr
  }
  const request = { busiFlag: dvInfo.value.type } as BusiTreeRequest
  // 获取可关联的仪表板
  queryTreeApi(request).then(rsp => {
    state.panelList = rsp
  })

  if (chartDetails.tableId) {
    // 获取当前数据集信息
    getDatasetDetails(chartDetails.tableId).then(res => {
      state.curDatasetInfo = res || {}
    })

    // 获取当前视图的字段信息
    listFieldByDatasetGroup(chartDetails.tableId).then(rsp => {
      state.linkJumpCurViewFieldArray = []
      const sourceCurViewFieldArray = rsp.data
      sourceCurViewFieldArray.forEach(fieldItem => {
        if (checkAllAxisStr.indexOf(fieldItem.id) > -1) {
          state.linkJumpCurViewFieldArray.push(fieldItem)
        }
      })
    })

    // 获取当前视图的关联信息
    queryWithViewId(dvInfo.value.id, state.viewId).then(rsp => {
      state.linkJump = rsp.data
      state.linkJumpInfoArray = []
      state.linkJumpInfoXArray = []
      state.linkJump.linkJumpInfoArray.forEach(linkJumpInfo => {
        if (checkJumpStr.indexOf(linkJumpInfo.sourceFieldId) > -1) {
          state.mapJumpInfoArray[linkJumpInfo.sourceFieldId] = linkJumpInfo
          state.linkJumpInfoArray.push(linkJumpInfo)
          state.linkJumpInfoXArray.push(linkJumpInfo)
        } else if (checkAllAxisStr.indexOf(linkJumpInfo.sourceFieldId) > -1) {
          state.linkJumpInfoArray.push(linkJumpInfo)
        }
      })
      const firstNode = state.linkJumpInfoArray[0]
      state.initState = true
      nextTick(() => {
        linkJumpInfoTree.value.setCurrentKey(firstNode.sourceFieldId)
        nodeClick(firstNode)
      })
    })
  }
}

const save = () => {
  // 字段检查
  let subCheckCountAll = 0
  state.linkJump.linkJumpInfoArray.forEach(linkJumpInfo => {
    let subCheckCount = 0
    if (linkJumpInfo.linkType === 'inner') {
      if (linkJumpInfo.targetDvId) {
        subCheckCount++
        subCheckCountAll++
      }
      linkJumpInfo.targetViewInfoList &&
        linkJumpInfo.targetViewInfoList.forEach(function (link) {
          if (!(link.sourceFieldActiveId && link.targetFieldId && link.targetViewId)) {
            subCheckCount++
            subCheckCountAll++
          }
        })
    }
    if (linkJumpInfo.linkType === 'outer') {
      if (!linkJumpInfo.content) {
        subCheckCount++
        subCheckCountAll++
      }
    }
    if (subCheckCount > 0) {
      ElMessage.error('字段【' + linkJumpInfo.sourceFieldName + '】存在空配置，请先完善配置！')
    }
  })
  if (subCheckCountAll) {
    return
  }

  updateJumpSet(state.linkJump).then(rsp => {
    ElMessage.success('保存成功')
    // 刷新跳转信息
    queryVisualizationJumpInfo(dvInfo.value.id).then(rsp => {
      dvMainStore.setNowPanelJumpInfo(rsp.data)
      cancel()
    })
  })
}
const nodeClick = (data, node?) => {
  state.linkJumpInfo = state.mapJumpInfoArray[data.sourceFieldId]
  if (!state.linkJumpInfo.linkType) {
    state.linkJumpInfo.linkType = 'outer'
  }
  if (!state.linkJumpInfo.jumpType) {
    state.linkJumpInfo.jumpType = '_blank'
  }
  if (!state.linkJumpInfo.content) {
    state.linkJumpInfo.content = 'http://'
  }
  if (!state.linkJumpInfo.attachParams) {
    state.linkJumpInfo.attachParams = false
  }
  if (state.linkJumpInfo.targetDvId) {
    getPanelViewList(state.linkJumpInfo.targetDvId)
  }
  codeMirrorContentSet(state.linkJumpInfo.content)
}

const codeMirrorContentSet = content => {
  nextTick(() => {
    outerContentEditor.value?.editorInit(content)
  })
}

// 获取当前视图字段 关联仪表板的视图信息列表
const getPanelViewList = dvId => {
  viewTableDetailList(dvId).then(rsp => {
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = rsp.data
    if (state.currentLinkPanelViewArray) {
      state.currentLinkPanelViewArray.forEach(view => {
        state.viewIdFieldArrayMap[view.id] = view.tableFields
      })
    }
  })
}
const dvNodeClick = (data, node) => {
  if (data.nodeType !== 'folder') {
    state.linkJumpInfo.targetViewInfoList = []
    addLinkJumpField()
    getPanelViewList(data.id)
  }
}
const inputVal = value => {
  if (!value) {
    state.linkJumpInfo.targetViewInfoList = []
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = []
  }
}
const addLinkJumpField = () => {
  state.linkJumpInfo.targetViewInfoList.push({
    targetViewId: '',
    targetFieldId: ''
  })
}
const deleteLinkJumpField = index => {
  state.linkJumpInfo.targetViewInfoList.splice(index, 1)
}
const normalizer = node => {
  // 去掉children=null的属性
  if (node.children === null || node.children === 'null') {
    delete node.children
  }
}
const viewInfoOnChange = targetViewInfo => {
  targetViewInfo.targetFieldId = null
}
const sourceFieldCheckedChange = data => {
  nextTick(() => {
    linkJumpInfoTree.value.setCurrentKey(data.sourceFieldId)
    nodeClick(data)
  })
}
const cancel = () => {
  dialogShow.value = false
  state.initState = false
}

const defaultForm = {
  originName: '', // 物理字段名
  name: '', // 字段显示名
  groupType: 'd', // d=维度，q=指标
  type: 'VARCHAR',
  deType: 0, // 字段类型
  extField: 2,
  id: '',
  checked: true
}

const fieldForm = reactive<CalcFieldType>({ ...(defaultForm as CalcFieldType) })

const insertFieldToCodeMirror = (value: string) => {
  outerContentEditor.value.insertFieldToCodeMirror(value)
}

const outerContentShow = computed(() => {
  return state.linkJumpInfo && state.linkJumpInfo.linkType === 'outer' && dialogShow.value
})

const filterNodeMethod = (value, data) => {
  return !value || data.checked
}

watch(
  () => state.showSelected,
  (newValue, oldValue) => {
    linkJumpInfoTree.value.filter(newValue)
  }
)

watch(
  () => outerContentShow.value,
  (newValue, oldValue) => {
    if (newValue) {
      codeMirrorContentSet(state.linkJumpInfo.content)
    }
  }
)

defineExpose({
  dialogInit
})
</script>

<style scoped lang="less">
.my_table ::v-deep .el-table__row > td {
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}

.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
  border: none;
}

.my_table ::v-deep .el-table::before {
  /* 去除下边框 */
  height: 0;
}

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

.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 470px;
  background-size: 100% 100% !important;
}

.top_border {
  border-top: 1px solid #e6e6e6;
}

.slot-class {
  color: white;
}

.bottom {
  margin-top: 10px;
  justify-content: center;
}

.ellip {
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
  margin-right: 8px;
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
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

/deep/ .vue-treeselect__control {
  height: 28px;
}

/deep/ .vue-treeselect__single-value {
  color: #606266;
  line-height: 28px !important;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.auth-span {
  float: left;
  width: 30px;
  margin-left: -8px;
}

.tree-head {
  height: 40px;
  line-height: 40px;
  font-size: 12px;
  color: #3d4d66;
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

.padding-lr {
  padding: 0 4px;
}

.field-height {
  height: calc(100% - 25px);
  margin-top: 12px;
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
}

.item-dimension {
  display: flex !important;
  align-items: center;
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  font-size: 14px;
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.item-quota {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #b2d3a3;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
}

span {
  font-size: 12px;
}

.set-name-area {
  font-weight: 600;
  margin-right: 20px;
}

:deep(.ed-row) {
  width: 100%;
}

.dv-selector {
  width: 100%;
}

.top-area {
  float: left;
  line-height: 33px;
}

.top-area-text {
  font-weight: 400;
  font-size: 14px;
  color: #646a73;

  &.margin-left {
    margin-left: 24px;
  }
}
.settings-container {
  height: 100%;

  .settings-header {
    height: 92px;
    border-bottom: 1px solid #e6e6e6;

    .radio-group-box {
      margin-top: 8px;
      margin-bottom: 8px;

      .title {
        color: #646a73;
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
      }

      :deep(.ed-radio__label) {
        color: #1f2329;
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
      }
    }
  }

  .settings-main {
    padding: 16px;
    .empty {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: row;
      justify-content: center;
      align-items: center;
    }
    .main-form {
      height: 100%;
      width: 100%;

      .icon-center {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }

      .main-scrollbar-container {
        height: calc(100% - 132px);

        :deep(.ed-scrollbar) {
          height: fit-content;
          max-height: 208px;
        }
      }
    }
  }
}

.top-area-value {
  font-weight: 400;
  font-size: 14px;
  color: #1f2329;
}
.view-type-icon {
  color: #3370ff;
  width: 16px;
  height: 16px;
}
.content-head {
  height: 30px;
  margin-top: 10px;
  .content-head-text {
    margin-left: 16px;
    font-weight: 400;
    font-size: 14px;
    color: #646a73;
    line-height: 32px;
    margin-right: 16px;
  }
}
.link-icon-area {
  text-align: center;
  line-height: 35px;
}
.inner-content {
  width: 100%;
  padding: 16px 16px 8px 16px;
  font-size: 14px !important;
}

.outer-content {
  height: 340px;
  border-radius: 4px;
}

.padding-lr {
  height: 500px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  padding: 12px;
  box-sizing: border-box;
  margin-left: 12px;
  width: 214px;
  overflow-y: hidden;
}

.mb8 {
  margin-bottom: 8px;
  display: inline-flex;
  align-items: center;

  i {
    margin-left: 4.67px;
  }
}

.field-height {
  height: calc(50% - 41px);
  margin-top: 4px;
  overflow-y: auto;
}

.class-na {
  margin-top: 8px;
  text-align: center;
  font-size: 14px;
  color: var(--deTextDisable);
}
.outer-content-mirror {
  border: 1px solid #bbbfc4;
  border-radius: 4px;
  height: calc(100% - 30px);
  width: 100%;
  overflow: hidden;
}
.url-text {
  width: 100%;
  line-height: 22px;
  margin-bottom: 8px;
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  color: #1f2329;
}

.outer-content-right {
  border: 1px solid #bbbfc4;
  border-radius: 4px;
  height: calc(100% - 30px);
  width: 100%;
  padding: 12px;
}

.tree-select-field {
  font-size: 14px;
  display: flex;
  align-items: center;
}
</style>
