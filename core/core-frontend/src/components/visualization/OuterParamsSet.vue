<template>
  <el-dialog
    class="params-class"
    :append-to-body="true"
    title="外部参数设置"
    v-model="state.outerParamsSetVisible"
    width="70vw"
    top="10vh"
    trigger="click"
  >
    <el-row style="height: 550px">
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: hidden">
            <el-row class="tree-head">
              <span class="head-text">参数列表</span>
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
                node-key="id"
                highlight-current
                :props="state.treeProp"
                @node-click="nodeClick"
              >
                <template #default="{ node, data }">
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
                    <span :id="'paramName-' + data.paramsInfoId">
                      <el-input
                        v-if="curEditDataId === data.paramsInfoId"
                        v-model="data.paramName"
                        size="small"
                        :placeholder="$t('visualization.input_param_name')"
                        @blur="closeEdit"
                      />
                      <span class="tree-select-field" v-else-if="data.paramName">
                        {{ data.paramName }}
                      </span>
                      <span class="tree-select-field" v-else> 未配置参数名 </span>
                    </span>
                    <span class="icon-more">
                      <handle-more
                        style="margin-right: 10px"
                        @handle-command="cmd => outerParamsOperation(cmd, node, data)"
                        :menu-list="state.optMenu"
                        icon-name="icon_more_outlined"
                        placement="bottom-start"
                      ></handle-more>
                    </span>
                  </span>
                </template>
              </el-tree>
            </el-row>
          </el-col>
          <el-col :span="16" class="preview-show">
            <el-row v-if="state.curNodeId">
              <el-row style="margin-top: 5px">
                <div style="display: flex" class="inner-content">
                  <div style="flex: 1">联动组件</div>
                  <div style="width: 36px"></div>
                  <div style="flex: 1">联动组件字段</div>
                  <div style="width: 32px"></div>
                </div>
                <div style="width: 100%; max-height: 350px; overflow-y: auto">
                  <div
                    style="display: flex; padding: 0 16px 8px"
                    v-for="(targetViewInfo, index) in state.outerParamsInfo.targetViewInfoList"
                    :key="index"
                  >
                    <div style="flex: 1">
                      <div class="select-filed">
                        <el-select
                          v-model="targetViewInfo.targetViewId"
                          filterable
                          style="width: 100%"
                          size="small"
                          :placeholder="t('visualization.please_select')"
                          @change="viewInfoOnChange(targetViewInfo)"
                        >
                          <el-option
                            v-for="item in state.currentLinkPanelViewArray.filter(
                              curItem =>
                                !viewSelectedField.includes(curItem.id) ||
                                curItem.id === targetViewInfo.targetViewId
                            )"
                            :key="item.id"
                            :label="item.title"
                            :value="item.id"
                          >
                            <Icon
                              class-name="view-type-icon"
                              style="margin-right: 4px"
                              :name="item.type"
                            />
                            <span style="font-size: 12px"> {{ item.title }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </div>
                    <el-icon class="link-icon-join">
                      <Icon style="width: 20px; height: 20px" name="dv-link-target" />
                    </el-icon>
                    <div style="flex: 1">
                      <div class="select-filed">
                        <el-select
                          v-model="targetViewInfo.targetFieldId"
                          filterable
                          :disabled="fieldIdDisabledCheck(targetViewInfo)"
                          style="width: 100%"
                          size="small"
                          :placeholder="t('visualization.please_select')"
                        >
                          <el-option
                            v-for="viewField in getFieldArray(targetViewInfo.targetViewId)"
                            :key="viewField.id"
                            :label="viewField.name"
                            :value="viewField.id"
                          >
                            <Icon
                              style="width: 14px; height: 14px"
                              :name="`field_${fieldType[viewField.deType]}`"
                              :className="`field-icon-${fieldType[viewField.deType]}`"
                            />
                            <span style="font-size: 12px">{{ viewField.name }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </div>
                    <el-button class="m-del-icon-btn" text @click="deleteOuterParamsField(index)">
                      <el-icon size="20px">
                        <Icon name="icon_delete-trash_outlined" />
                      </el-icon>
                    </el-button>
                  </div>
                </div>

                <el-row style="width: 100%; padding-left: 16px">
                  <el-button type="primary" icon="Plus" text @click="addOuterParamsField">
                    {{ t('visualization.add_param_link_field') }}
                  </el-button>
                </el-row>
              </el-row>
            </el-row>
            <div v-else class="empty">
              <empty-background description="请配置参数" img-type="noneWhite" />
            </div>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button size="small" @click="cancel()">{{ t('commons.cancel') }} </el-button>
        <el-button type="primary" size="small" @click="save()"
          >{{ t('commons.confirm') }}
        </el-button>
      </el-row>
    </el-row>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { queryWithVisualizationId, updateOuterParamsSet } from '@/api/visualization/outerParams'
import { viewDetailList } from '@/api/visualization/dataVisualization'
import checkArrayRepeat from '@/utils/check'
import HandleMore from '@/components/handle-more/src/HandleMore.vue'
import { fieldType } from '@/utils/attr'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, componentData } = storeToRefs(dvMainStore)
const outerParamsInfoTree = ref(null)
const { t } = useI18n()
const curEditDataId = ref(null)
const snapshotStore = snapshotStoreWithOut()

const state = reactive({
  loading: false,
  outerParamsSetVisible: false,
  optMenu: [
    {
      label: '重命名',
      svgName: 'edit',
      command: 'rename'
    },
    {
      label: '删除',
      svgName: 'delete',
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
  outerParamsInfoArray: [],
  mapOuterParamsInfoArray: {},
  panelList: [],
  curNodeId: null,
  outerParamsInfo: {
    content: '',
    linkType: '',
    targetViewInfoList: [],
    paramsInfoId: null
  },
  currentFiledTreeNode: null,
  defaultOuterParamsInfo: {
    paramName: '',
    checked: true,
    targetViewInfoList: []
  },
  defaultTargetViewInfo: {
    targetViewId: null,
    targetFieldId: null
  },
  currentLinkPanelViewArray: [],
  viewIdFieldArrayMap: {},
  widgetSubjectsTrans: {
    timeYearWidget: '年份过滤组件',
    timeMonthWidget: '年月过滤组件',
    timeDateWidget: '日期过滤组件',
    timeDateRangeWidget: '日期范围过滤组件',
    textSelectWidget: '文本下拉过滤组件',
    textSelectGridWidget: '文本列表过滤组件',
    textInputWidget: '文本搜索过滤组件',
    textSelectTreeWidget: '下拉树过滤组件',
    numberSelectWidget: '数字下拉过滤组件',
    numberSelectGridWidget: '数字列表过滤组件',
    numberRangeWidget: '数值区间过滤组件'
  }
})

const viewSelectedField = computed(() =>
  state.outerParamsInfo?.targetViewInfoList?.map(targetViewInfo => targetViewInfo.targetViewId)
)

const closeEdit = () => {
  curEditDataId.value = null
}

const outerParamsOperation = (cmd, node, data) => {
  if (cmd === 'rename') {
    curEditDataId.value = data.paramsInfoId
  } else if (cmd === 'delete') {
    removeOuterParamsInfo(node, data)
  }
}

const fieldIdDisabledCheck = targetViewInfo => {
  return (
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId] &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId].length === 1 &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId][0].id === 'empty'
  )
}

const getFieldArray = id => {
  return state.viewIdFieldArrayMap[id]
}

const initParams = () => {
  // 获取当前仪表板外部跳转信息
  queryWithVisualizationId(dvInfo.value.id).then(rsp => {
    state.outerParams = rsp.data
    state.outerParamsInfoArray = state.outerParams?.outerParamsInfoArray
    if (state.outerParamsInfoArray.length >= 1) {
      state.outerParamsInfoArray.forEach(outerParamsInfo => {
        state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
      })
      state.curNodeId = null
      nextTick(() => {
        // outerParamsInfoTree.value.setCurrentKey(firstNode.paramsInfoId)
        // nodeClick(firstNode)
      })
    }
  })
  getPanelViewList(dvInfo.value.id)
}

const cancel = () => {
  state.outerParamsSetVisible = false
}

const save = () => {
  if (checkArrayRepeat(state.outerParams.outerParamsInfoArray, 'paramName')) {
    ElMessage.warning({
      message: t('visualization.repeat_params'),
      showClose: true
    })
    return
  }
  updateOuterParamsSet(state.outerParams).then(() => {
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

// 获取当前视图字段 关联仪表板的视图信息列表
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

const addOuterParamsField = () => {
  state.outerParamsInfo.targetViewInfoList.push({
    targetViewId: '',
    targetFieldId: ''
  })
}
const deleteOuterParamsField = index => {
  state.outerParamsInfo.targetViewInfoList.splice(index, 1)
}

const viewInfoOnChange = targetViewInfo => {
  if (
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId] &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId].length === 1 &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId][0].id === 'empty'
  ) {
    targetViewInfo.targetFieldId = 'empty'
  } else {
    targetViewInfo.targetFieldId = null
  }
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
  state.outerParamsInfoArray.push(outerParamsInfo)
  state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
  curEditDataId.value = outerParamsInfo['paramsInfoId']
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

const optInit = () => {
  state.outerParamsSetVisible = true
  initParams()
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
}

.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}

.custom-tree {
  height: 100%;
  width: 100%;
  overflow-y: auto;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;

  .icon-more {
    margin-left: auto;
    visibility: hidden;
  }

  &:hover .icon-more {
    margin-left: auto;
    visibility: visible;
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
  padding: 16px 16px 8px 16px;
  font-size: 14px !important;
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
  width: 40px;
  margin-right: 5px;
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
  background-color: rgba(51, 112, 255, 0.1) !important;
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
</style>
