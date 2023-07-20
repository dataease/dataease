<template>
  <el-dialog
    ref="enlargeDialog"
    :append-to-body="true"
    v-model="dialogShow"
    width="70vw"
    trigger="click"
  >
    <el-row v-if="state.initState" style="height: 430px">
      <el-row>
        <span class="set-name-area">{{ t('visualization.jump_set') }}</span>
        <el-checkbox v-model="state.linkJump.checked">{{
          t('visualization.enable_jump')
        }}</el-checkbox>
      </el-row>
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: auto">
            <el-row class="tree-head">
              <span style="float: left; margin-left: 30px">{{
                t('visualization.column_name')
              }}</span>
              <span style="float: right; margin-right: 10px">{{
                t('visualization.enable_column')
              }}</span>
            </el-row>
            <el-tree
              menu
              ref="linkJumpInfoTree"
              :data="state.linkJumpInfoXArray"
              node-key="sourceFieldId"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClick"
            >
              <template #default="{ data }">
                <span class="custom-tree-node">
                  <span>
                    <span style="margin-left: 6px">{{ data.sourceFieldName }}</span>
                  </span>
                  <span @click.stop>
                    <div>
                      <span class="auth-span">
                        <el-checkbox
                          v-model="data.checked"
                          @change="sourceFieldCheckedChange(data)"
                        />
                      </span>
                    </div>
                  </span>
                </span>
              </template>
            </el-tree>
          </el-col>
          <el-col :span="16" class="preview-show">
            <el-row v-if="state.linkJumpInfo">
              <el-row style="height: 30px; margin-top: 10px">
                <el-col :span="4" style="margin-left: 20px">
                  {{ t('visualization.link_type') }}：
                </el-col>
                <el-col :span="10">
                  <el-radio-group v-model="state.linkJumpInfo.linkType" size="mini">
                    <el-radio label="outer">{{ t('visualization.link_outer') }}</el-radio>
                    <el-radio label="inner">{{ t('visualization.link_panel') }}</el-radio>
                  </el-radio-group>
                </el-col>
                <el-col v-if="state.linkJumpInfo.linkType === 'inner'" :span="9">
                  <span>仪表板：</span>
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
                </el-col>
              </el-row>
              <el-row style="height: 30px; margin-top: 10px">
                <el-col :span="4" style="margin-left: 20px">
                  {{ t('visualization.open_model') }}：
                </el-col>
                <el-col :span="10">
                  <el-radio-group v-model="state.linkJumpInfo.jumpType" size="mini">
                    <el-radio label="_self">{{ t('visualization.now_window') }}</el-radio>
                    <el-radio label="_blank">{{ t('visualization.new_window') }}</el-radio>
                  </el-radio-group>
                </el-col>
              </el-row>
              <el-row
                v-if="state.linkJumpInfo.linkType === 'inner'"
                style="margin-top: 5px"
                class="top_border"
              >
                <el-row style="margin-top: 10px">
                  <el-col :span="7">
                    <div class="ellip">原字段</div>
                  </el-col>
                  <el-col :span="7">
                    <div class="ellip">{{ t('visualization.link_view') }}</div>
                  </el-col>
                  <el-col :span="8">
                    <div class="ellip">{{ t('visualization.link_view_field') }}</div>
                  </el-col>
                </el-row>
                <el-row style="display: inline-block; height: 180px; overflow-y: auto">
                  <el-row
                    v-for="(targetViewInfo, index) in state.linkJumpInfo.targetViewInfoList"
                    :key="index"
                  >
                    <el-col :span="7">
                      <div class="select-filed">
                        <el-select
                          v-model="targetViewInfo.sourceFieldActiveId"
                          style="width: 100%"
                          size="mini"
                        >
                          <el-option
                            v-for="curViewField in state.linkJumpCurViewFieldArray"
                            :key="curViewField.id"
                            :label="curViewField.name"
                            :value="curViewField.id"
                          >
                            <span style="float: left; font-size: 12px">{{
                              curViewField.name
                            }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </el-col>
                    <el-col :span="7">
                      <div class="select-filed">
                        <el-select
                          v-model="targetViewInfo.targetViewId"
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
                              <svg-icon :icon-class="item.type" style="width: 14px; height: 14px" />
                            </span>
                            <span style="float: left; font-size: 12px">{{ item.title }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="select-filed">
                        <el-select
                          v-model="targetViewInfo.targetFieldId"
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
                            <span style="float: left; font-size: 12px">{{ viewField.name }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </el-col>
                    <el-col :span="2">
                      <el-icon
                        style="margin-top: 10px; cursor: pointer"
                        @click="deleteLinkJumpField(index)"
                      >
                        <Delete />
                      </el-icon>
                    </el-col>
                  </el-row>
                </el-row>

                <el-row class="bottom">
                  <el-button
                    :disabled="!state.linkJumpInfo.targetDvId"
                    size="mini"
                    type="success"
                    icon="el-icon-plus"
                    round
                    @click="addLinkJumpField"
                    >{{ t('visualization.add_jump_field') }}
                  </el-button>
                </el-row>
              </el-row>
              <el-row
                v-if="state.linkJumpInfo.linkType === 'outer'"
                style="padding: 14px; border-top: 1px solid #e6e6e6"
              >
                <el-row style="height: 230px; border: 1px solid #e6e6e6">
                  <el-col :span="18" style="height: 100%">
                    <el-row>
                      <span>
                        {{ t('visualization.target_url') }}
                        <el-tooltip class="item" effect="dark" placement="bottom">
                          <template #content>
                            <div>
                              {{ t('visualization.target_url_tips') }}
                            </div>
                          </template>
                          <i class="el-icon-info" style="cursor: pointer" />
                        </el-tooltip>
                      </span>
                      <span>codemirror</span>
                    </el-row>
                  </el-col>
                  <el-col :span="6" style="height: 100%; border-left: 1px solid #e6e6e6">
                    <el-col :span="24" style="height: 100%" class="padding-lr">
                      <span>
                        {{ t('visualization.select_world') }}
                        <el-tooltip class="item" effect="dark" placement="bottom">
                          <template #content>
                            <div v-html="t('chart.reference_field_tip')" />
                          </template>
                          <i class="el-icon-info" style="cursor: pointer" />
                        </el-tooltip>
                      </span>
                      <el-input
                        v-model="state.searchField"
                        size="mini"
                        :placeholder="t('dataset.search')"
                        prefix-icon="el-icon-search"
                        clearable
                      />
                      <div class="field-height">
                        <el-divider />
                        <span>drage group state.linkJumpInfoArray</span>
                      </div>
                    </el-col>
                  </el-col>
                </el-row>
              </el-row>
            </el-row>
            <el-row v-else style="height: 100%" class="custom-position">
              {{ t('visualization.select_dimension') }}
            </el-row>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button size="mini" @click="cancel()">{{ t('commons.cancel') }} </el-button>
        <el-button type="primary" size="mini" @click="save()"
          >{{ t('commons.confirm') }}
        </el-button>
      </el-row>
    </el-row>
  </el-dialog>
</template>

<script lang="ts" setup>
import {
  queryVisualizationJumpInfo,
  queryWithViewId,
  updateJumpSet,
  viewTableDetailList
} from '@/api/visualization/linkJump'
import { reactive, ref, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { listFieldByDatasetGroup } from '@/api/dataset'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewInfo } = storeToRefs(dvMainStore)
const linkJumpInfoTree = ref(null)
const { t } = useI18n()
const dialogShow = ref(false)

const state = reactive({
  tempId: null,
  initState: false,
  viewId: null,
  codemirrorShow: true,
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
    linkType: 'inner',
    jumpType: '_self',
    targetViewInfoList: []
  },
  defaultTargetViewInfo: {
    targetViewId: null,
    targetFieldId: null
  },
  currentLinkPanelViewArray: [],
  viewIdFieldArrayMap: {},
  cmOption: {
    tabSize: 2,
    styleActiveLine: true,
    lineNumbers: true,
    line: true,
    mode: 'text/x-textile',
    theme: 'solarized',
    hintOptions: {
      // 自定义提示选项
      completeSingle: false // 当匹配只有一项的时候是否自动补全
    }
  }
})

const emits = defineEmits(['closeJumpSetDialog'])

const dialogInit = viewItem => {
  dialogShow.value = true
  state.initState = false
  init(viewItem)
}

const init = viewItem => {
  state.initState = false
  state.viewId = viewItem.id
  const chartDetails = canvasViewInfo.value[state.viewId]
  let checkAllAxisStr =
    JSON.stringify(chartDetails.xAxis) +
    JSON.stringify(chartDetails.xAxisExt) +
    JSON.stringify(chartDetails.yAxis) +
    JSON.stringify(chartDetails.yAxisExt) +
    JSON.stringify(chartDetails.drillFields)
  let checkJumpStr
  // 堆叠图的可选参数分两种情况 1.如果有堆叠项 则指标只有第一个可选 2.如果没有堆叠项泽所有指标都可以选
  if (chartDetails.type.indexOf('stack') > -1 && chartDetails.extStack.length > 2) {
    const yAxisArray = chartDetails.yaxis
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
    checkJumpStr = chartDetails.xAxis + chartDetails.drillFields
  } else {
    checkJumpStr = checkAllAxisStr
  }
  const request = { busyFlag: dvInfo.value.type } as BusiTreeRequest
  // 获取可关联的仪表板
  queryTreeApi(request).then(rsp => {
    state.panelList = rsp
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

const save = () => {
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
    state.linkJumpInfo.linkType = 'inner'
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
  if (data.checked) {
    state.linkJump.checked = true
  }
  nextTick(() => {
    linkJumpInfoTree.value.setCurrentKey(data.sourceFieldId)
    nodeClick(data)
  })
}
const cancel = () => {
  dialogShow.value = false
  state.initState = false
}

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
  text-align: center;
}

.preview {
  margin-top: 5px;
  border: 1px solid #e6e6e6;
  height: 350px !important;
  overflow: hidden;
  background-size: 100% 100% !important;
}

.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 350px;
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
  margin-left: 10px;
  margin-right: 10px;
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
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}

.auth-span {
  float: right;
  width: 30px;
  margin-right: 5px;
}

.tree-head {
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid #e6e6e6;
  background-color: #f7f8fa;
  font-size: 12px;
  color: #3d4d66;
}

.padding-lr {
  padding: 0 4px;
}

.field-height {
  height: calc(100% - 25px);
  margin-top: 4px;
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
}

.item-dimension {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
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
  /*background-color: rgba(35,46,64,.05);*/
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

.dv-selector :deep(.ed-input__inner) {
  height: 24px;
  width: 110px;
}
</style>
