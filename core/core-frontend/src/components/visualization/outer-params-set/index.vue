<template>
  <el-row style="height: 430px">
    <el-row>
      <span style="font-weight: 600; margin-right: 20px">{{
        t('visualization.outer_param_set')
      }}</span>
      <el-checkbox v-model="state.outerParams?.checked">{{
        t('visualization.enable_outer_param_set')
      }}</el-checkbox>
    </el-row>
    <el-row v-loading="state.loading">
      <el-row class="preview">
        <el-col :span="8" style="height: 100%; overflow-y: hidden">
          <el-row class="tree-head">
            <span style="float: left; margin-left: 30px">{{ t('visualization.param_name') }}</span>
            <span style="float: right; margin-right: 10px">{{
              t('visualization.enable_param')
            }}</span>
          </el-row>
          <el-row class="tree-content">
            <el-tree
              ref="outerParamsInfoTree"
              :data="state.outerParamsInfoArray"
              node-key="id"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClick"
            >
              <template v-slot="{ node, data }">
                <span class="custom-tree-node">
                  <span>
                    <span style="margin-left: 6px"
                      ><el-input
                        v-model="data.paramName"
                        size="mini"
                        :placeholder="t('visualization.input_param_name')"
                    /></span>
                  </span>
                  <span @click.stop>
                    <div>
                      <span class="auth-span">
                        <el-checkbox
                          v-model="data.checked"
                          style="margin-right: 10px"
                          @change="sourceFieldCheckedChange(data)"
                        />
                        <el-button
                          icon="el-icon-delete"
                          type="text"
                          size="small"
                          @click="removeOuterParamsInfo(node, data)"
                        />
                      </span>
                    </div>
                  </span>
                </span>
              </template>
            </el-tree>
          </el-row>
          <el-row class="tree-bottom">
            <el-button
              size="mini"
              type="success"
              icon="el-icon-plus"
              round
              @click="addOuterParamsInfo"
              >{{ t('visualization.add_param') }}
            </el-button>
          </el-row>
        </el-col>
        <el-col :span="16" class="preview-show">
          <el-row v-if="state.outerParamsInfo">
            <el-row class="top_border">
              <el-row style="margin-top: 10px">
                <el-col :span="11">
                  <div class="ellip">{{ t('visualization.link_component') }}</div>
                </el-col>
                <el-col :span="11">
                  <div class="ellip">{{ t('visualization.link_component_field') }}</div>
                </el-col>
              </el-row>
              <el-row style="height: 266px; overflow-y: auto">
                <el-row
                  v-for="(targetViewInfo, index) in state.outerParamsInfo.targetViewInfoList"
                  :key="index"
                >
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select
                        v-model="targetViewInfo.targetViewId"
                        filterable
                        style="width: 100%"
                        size="mini"
                        :placeholder="t('fu.search_bar.please_select')"
                        @change="viewInfoOnChange(targetViewInfo)"
                      >
                        <el-option
                          v-for="item in state.currentLinkPanelViewArray.filter(
                            curItem =>
                              !viewSelectedField.includes(curItem.id) ||
                              curItem.id === targetViewInfo.targetViewId
                          )"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        >
                          <span v-if="item.isPlugin" style="float: left">
                            <svg-icon
                              :icon-class="
                                item.type !== 'buddle-map'
                                  ? '/api/pluginCommon/staticInfo/' + item.type + '/svg'
                                  : item.type
                              "
                              style="width: 14px; height: 14px"
                            />
                          </span>
                          <span v-else style="float: left">
                            <svg-icon :icon-class="item.type" style="width: 14px; height: 14px" />
                          </span>
                          <span style="float: left; font-size: 12px"> {{ item.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select
                        v-model="targetViewInfo.targetFieldId"
                        filterable
                        :disabled="
                          state.viewIdFieldArrayMap[targetViewInfo.targetViewId] &&
                          state.viewIdFieldArrayMap[targetViewInfo.targetViewId].length === 1 &&
                          state.viewIdFieldArrayMap[targetViewInfo.targetViewId][0].id === 'empty'
                        "
                        style="width: 100%"
                        size="mini"
                        :placeholder="t('fu.search_bar.please_select')"
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
                    <div>
                      <el-button
                        icon="el-icon-delete"
                        type="text"
                        size="small"
                        style="float: left"
                        @click="deleteOuterParamsField(index)"
                      />
                    </div>
                  </el-col>
                </el-row>
              </el-row>

              <el-row class="bottom">
                <el-button
                  size="mini"
                  type="success"
                  icon="el-icon-plus"
                  round
                  @click="addOuterParamsField"
                  >{{ t('visualization.add_param_link_field') }}
                </el-button>
              </el-row>

              <!--    <el-button slot="reference">T</el-button>-->
              <template v-slot:reference>
                <i class="icon iconfont icon-edit slot-class" />
              </template>
            </el-row>
            <el-row v-if="state.outerParamsInfo.linkType === 'outer'" style="height: 300px">
              <el-input
                v-model="state.outerParamsInfo.content"
                :autosize="{ minRows: 14 }"
                type="textarea"
                :placeholder="t('visualization.input_jump_link')"
              />
            </el-row>
          </el-row>
          <el-row
            v-else
            style="height: 100%; background-color: var(--MainContentBG)"
            class="custom-position"
          >
            {{ t('visualization.select_param') }}
          </el-row>
        </el-col>
      </el-row>
    </el-row>
    <el-row class="root-class">
      <el-button size="mini" @click="cancel()">{{ t('commons.cancel') }} </el-button>
      <el-button type="primary" size="mini" @click="save()">{{ t('commons.confirm') }} </el-button>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { deepCopy } from '@/utils/utils'
import generateID from '@/utils/generateID'
import { queryWithDvId, updateOuterParamsSet } from '@/api/visualization/outerParams'
import { detailList } from '@/api/visualization/dataVisualization'
import checkArrayRepeat from '@/utils/check'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, componentData, canvasStyleData } = storeToRefs(dvMainStore)
const outerParamsInfoTree = ref(null)
const emits = defineEmits(['outerParamsSetVisibleChange'])
const { t } = useI18n()

const state = reactive({
  loading: false,
  treeProp: {
    id: 'paramsInfoId',
    label: 'paramName',
    children: 'children'
  },
  outerParams: {},
  outerParamsInfoArray: null,
  mapOuterParamsInfoArray: {},
  panelList: [],
  outerParamsInfo: null,
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

const init = () => {
  // 获取当前仪表板外部跳转蚕食信息
  queryWithDvId(dvInfo.id).then(rsp => {
    state.outerParams = rsp.data
    state.outerParamsInfoArray = state.outerParams?.outerParamsInfoArray
    if (state.outerParamsInfoArray.length > 0) {
      state.outerParamsInfoArray.forEach(outerParamsInfo => {
        state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
      })
      const firstNode = state.outerParamsInfoArray[0]
      nextTick(() => {
        outerParamsInfoTree.value.setCurrentKey(firstNode.paramsInfoId)
        nodeClick(firstNode)
      })
    }
  })
  getPanelViewList(dvInfo.id)
}

const cancel = () => {
  emits('outerParamsSetVisibleChange', false)
}

const save = () => {
  if (checkArrayRepeat(state.outerParams.outerParamsInfoArray, 'paramName')) {
    ElMessage.warning({
      message: t('visualization.repeat_params'),
      showClose: true
    })
    return
  }
  updateOuterParamsSet(state.outerParams).then(rsp => {
    ElMessage({
      message: t('commons.save_success'),
      type: 'success',
      showClose: true
    })
    cancel()
  })
}

const nodeClick = data => {
  state.outerParamsInfo = state.mapOuterParamsInfoArray[data.paramsInfoId]
}

// 获取当前视图字段 关联仪表板的视图信息列表
const getPanelViewList = dvId => {
  detailList(dvId).then(rsp => {
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = rsp.data
    if (state.currentLinkPanelViewArray) {
      state.currentLinkPanelViewArray.forEach(view => {
        state.viewIdFieldArrayMap[view.id] = view.tableFields
      })
    }
    // 增加过滤组件匹配
    componentData.value.forEach(componentItem => {
      if (componentItem.type === 'custom') {
        state.currentLinkPanelViewArray.push({
          id: componentItem.id,
          type: 'filter',
          name: componentItem.options.attrs.title
            ? componentItem.options.attrs.title
            : state.widgetSubjectsTrans[componentItem.serviceName]
        })
        state.viewIdFieldArrayMap[componentItem.id] = [
          { id: 'empty', name: t('visualization.filter_no_select') }
        ]
      }
    })
  })
}

const panelNodeClick = (data, node) => {
  state.outerParamsInfo.targetViewInfoList = []
  getPanelViewList(data.id)
}

const inputVal = value => {
  if (!value) {
    state.outerParamsInfo.targetViewInfoList = []
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = []
  }
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

const normalizer = node => {
  // 去掉children=null的属性
  if (node.children === null || node.children === 'null') {
    delete node.children
  }
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
    state.outerParams?.checked = true
  }
  nextTick(() => {
    outerParamsInfoTree.value.setCurrentKey(data.paramsInfoId)
    nodeClick(data)
  })
}

const addOuterParamsInfo = () => {
  outerParamsInfoTree.value.checked = true
  const outerParamsInfo = deepCopy(state.defaultOuterParamsInfo)
  outerParamsInfo['paramsInfoId'] = generateID()
  state.outerParamsInfoArray.push(outerParamsInfo)
  state.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
}

const removeOuterParamsInfo = (node, data) => {
  const parent = node.parent
  const children = parent.data.children || parent.data
  const index = children.findIndex(d => d.paramsInfoId === data.paramsInfoId)
  children.splice(index, 1)
  if (data.paramsInfoId === state.outerParamsInfo.paramsInfoId) {
    delete state.mapOuterParamsInfoArray[data.paramsInfoId]
    state.outerParamsInfo = null
  }
}
</script>

<style scoped lang="less">
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

v-deep(.el-popover) {
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

v-deep(.vue-treeselect__control) {
  height: 28px;
}

v-deep(.vue-treeselect__single-value) {
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
  width: 40px;
  margin-right: 5px;
}

.tree-head {
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
  background-color: var(--SiderBG, #f7f8fa);
  font-size: 12px;
  color: var(--TableColor, #3d4d66);
}

.tree-content {
  height: calc(100% - 70px);
  overflow-y: auto;
}

.tree-bottom {
  margin-top: 7px;
  text-align: center;
}

v-deep(.vue-treeselect__placeholder {
  line-height: 28px;
}

v-deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: #8dbbef !important;
}

.tree-content v-deep(.el-input__inner) {
  background: transparent;
  border: 0px !important;
}
</style>
