<template>
  <el-dialog
    ref="enlargeDialog"
    :append-to-body="true"
    :title="t('visualization.linkage_setting')"
    v-model="dialogShow"
    width="70vw"
    top="10vh"
    trigger="click"
  >
    <linkage-set-option
      v-if="curComponent && curComponent.actionSelection"
      :action-selection="customLinkageActive"
    ></linkage-set-option>
    <div v-loading="loading" @keydown.stop @keyup.stop v-if="state.initState" style="height: 550px">
      <el-row style="flex-direction: row">
        <div class="top-area">
          <span class="top-area-text" style="margin-left: 0">已选图表：</span>
          <span class="top-area-value">
            <Icon class-name="view-type-icon" :name="state.curLinkageViewInfo.type" />
            {{ state.curLinkageViewInfo.title }}</span
          >
          <span class="top-area-text">所用数据集：</span>
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
      <el-row>
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: auto">
            <el-row class="tree-head">
              <span class="head-text">选择图表</span>
              <span class="head-filter"
                >仅看已选 <el-switch size="small" v-model="state.showSelected" />
              </span>
            </el-row>
            <el-row class="tree-dataset-head" v-show="sameDsShow"
              ><span
                ><el-icon class="toggle-icon" @click="() => (toggleSameDs = !toggleSameDs)">
                  <CaretBottom v-show="toggleSameDs" />
                  <CaretRight v-show="!toggleSameDs" /> </el-icon
                ><span>同数据集</span></span
              >
              <el-checkbox
                v-model="sameDatasetComponentCheckAll"
                :indeterminate="checkAllIsIndeterminate"
                @change="batchSelectChange"
                >全选</el-checkbox
              ></el-row
            >
            <el-tree
              v-show="toggleSameDs && sameDsShow"
              class="custom-tree"
              menu
              ref="linkageInfoTree"
              :empty-text="'暂无可用图表'"
              :filter-node-method="filterNodeMethod"
              :data="curLinkageTargetViewsInfoSameDs"
              node-key="targetViewId"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClickPre($event, 'sameDs')"
            >
              <template #default="{ data }">
                <span class="custom-tree-node">
                  <span>
                    <div @click.stop>
                      <span class="auth-span">
                        <!--？？？-->
                        <el-checkbox
                          v-model="data.linkageActive"
                          @change="targetViewCheckedChange('sameDs', data)"
                        />
                      </span>
                    </div>
                  </span>
                  <span>
                    <span class="tree-select-field">
                      <Icon
                        class-name="view-type-icon"
                        style="margin-right: 4px"
                        :name="data.targetViewType"
                      />
                      {{ data.targetViewName }}
                    </span>
                  </span>
                </span>
              </template>
            </el-tree>
            <el-row class="tree-dataset-head tree-dataset-head-top" v-show="diffDsShow"
              ><span
                ><el-icon class="toggle-icon" @click="() => (toggleDiffDs = !toggleDiffDs)">
                  <CaretBottom v-show="toggleDiffDs" />
                  <CaretRight v-show="!toggleDiffDs" /> </el-icon
                ><span>不同数据集</span></span
              >
            </el-row>
            <el-tree
              v-show="toggleDiffDs && diffDsShow"
              class="custom-tree"
              menu
              ref="linkageInfoTreeDiffDs"
              :empty-text="'暂无可用图表'"
              :filter-node-method="filterNodeMethod"
              :data="curLinkageTargetViewsInfoDiffDs"
              node-key="targetViewId"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClickPre($event, 'diffDs')"
            >
              <template #default="{ data }">
                <span class="custom-tree-node">
                  <span>
                    <div @click.stop>
                      <span class="auth-span">
                        <!--？？？-->
                        <el-checkbox
                          v-model="data.linkageActive"
                          @change="targetViewCheckedChange('diffDs', data)"
                        />
                      </span>
                    </div>
                  </span>
                  <span>
                    <span class="tree-select-field">
                      <Icon
                        class-name="view-type-icon"
                        style="margin-right: 4px"
                        :name="data.targetViewType"
                      />
                      {{ data.targetViewName }}
                    </span>
                  </span>
                </span>
              </template>
            </el-tree>
          </el-col>
          <el-col :span="16" class="preview-show">
            <el-row class="content-head">配置图表间的字段关联关系</el-row>
            <el-row v-if="state.linkageInfo && state.linkageInfo.linkageActive">
              <el-row style="margin-top: 5px">
                <div style="display: flex" class="inner-content">
                  <div style="flex: 1">当前图表源字段</div>
                  <div style="width: 36px"></div>
                  <div style="flex: 1">
                    {{ t('visualization.link_view_field') }}
                  </div>
                  <div style="width: 32px"></div>
                </div>
                <div style="width: 100%; max-height: 350px; overflow-y: auto">
                  <div
                    style="display: flex; padding: 0 16px 8px"
                    v-for="(itemLinkage, index) in state.linkageInfo.linkageFields"
                    :key="index"
                  >
                    <div style="flex: 1">
                      <div class="select-filed">
                        <el-select
                          v-model="itemLinkage.sourceField"
                          :placeholder="'请选择字段'"
                          style="width: 100%"
                        >
                          <el-option
                            v-for="item in sourceLinkageInfoFilter"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                          >
                            <span class="custom-option">
                              <Icon
                                style="width: 14px; height: 14px"
                                :name="`field_${fieldType[item.deType]}`"
                                :className="`field-icon-${fieldType[item.deType]}`"
                              />
                              <span style="float: left; margin-left: 4px; font-size: 14px">{{
                                item.name
                              }}</span>
                            </span>
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
                          v-model="itemLinkage.targetField"
                          :placeholder="'请选择'"
                          style="width: 100%"
                        >
                          <el-option
                            v-for="item in state.linkageInfo.targetViewFields"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                          >
                            <span class="custom-option">
                              <Icon
                                style="width: 14px; height: 14px"
                                :name="`field_${fieldType[item.deType]}`"
                                :className="`field-icon-${fieldType[item.deType]}`"
                              />
                              <span style="float: left; margin-left: 4px; font-size: 14px">{{
                                item.name
                              }}</span>
                            </span>
                          </el-option>
                        </el-select>
                      </div>
                    </div>

                    <el-button class="m-del-icon-btn" text @click="deleteLinkageField(index)">
                      <el-icon size="20px">
                        <Icon name="icon_delete-trash_outlined" />
                      </el-icon>
                    </el-button>
                  </div>
                </div>
                <el-row style="width: 100%; padding-left: 16px">
                  <el-button type="primary" icon="Plus" text @click="addLinkageField('', '')">
                    追加联动依赖字段
                  </el-button>
                </el-row>
              </el-row>
            </el-row>
            <el-row v-else style="height: 100%" class="custom-position">
              <Icon style="width: 125px; height: 125px" name="dv-empty" />
              <span style="margin-top: 8px; font-size: 14px">请先勾选需要联动的图表</span>
            </el-row>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button size="mini" @click="cancel()">{{ t('common.cancel') }} </el-button>
        <el-button type="primary" size="mini" @click="saveLinkageSetting()">
          {{ t('dataset.confirm') }}
        </el-button>
      </el-row>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import { reactive, ref, nextTick, watch, computed } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { fieldType } from '@/utils/attr'
import {
  getPanelAllLinkageInfo,
  getViewLinkageGatherArray,
  saveLinkage
} from '@/api/visualization/linkage'
import { getDatasetDetails } from '@/api/dataset'
import { findAllViewsId } from '@/utils/canvasUtils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import LinkageSetOption from '@/components/visualization/LinkageSetOption.vue'
import { deepCopy } from '@/utils/utils'
import { ACTION_SELECTION } from '@/custom-component/component-list'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewInfo, componentData, curComponent } = storeToRefs(dvMainStore)
const linkageInfoTree = ref(null)
const linkageInfoTreeDiffDs = ref(null)
const { t } = useI18n()
const dialogShow = ref(false)
const loading = ref(false)
const curLinkageTargetViewsInfo = ref([])
const curLinkageTargetViewsInfoSameDs = ref([])
const curLinkageTargetViewsInfoDiffDs = ref([])
const snapshotStore = snapshotStoreWithOut()
const state = reactive({
  sourceLinkageInfo: {},
  showSelected: false,
  curLinkageViewInfo: {},
  curDatasetInfo: {},
  initState: false,
  viewId: null,
  tableId: null,
  treeProp: {
    id: 'targetViewId',
    label: 'targetViewName',
    children: 'children'
  },
  linkageInfo: null
})
const sameDatasetComponentCheckAll = ref(false)

const checkAllIsIndeterminate = ref(false)

const customLinkageActive = ref(deepCopy(ACTION_SELECTION))

const toggleSameDs = ref(true)

const toggleDiffDs = ref(true)

const sameDsTreeSelectedChange = () => {
  const checkedCount = curLinkageTargetViewsInfoSameDs.value.filter(
    viewInfo => viewInfo.linkageActive
  ).length
  sameDatasetComponentCheckAll.value = checkedCount === curLinkageTargetViewsInfoSameDs.value.length
  checkAllIsIndeterminate.value =
    checkedCount > 0 && checkedCount < curLinkageTargetViewsInfoSameDs.value.length
}

const batchSelectChange = value => {
  // do change
  curLinkageTargetViewsInfoSameDs.value.forEach(viewInfo => {
    if (value) {
      viewInfo.linkageActive = true
      sameDatasetComponentCheckAll.value = true
      linkageFieldAdaptor(viewInfo)
    } else {
      viewInfo.linkageActive = false
      sameDatasetComponentCheckAll.value = false
    }
  })
  checkAllIsIndeterminate.value = false
}

const sameDsShow = computed(
  () => curLinkageTargetViewsInfoSameDs.value && curLinkageTargetViewsInfoSameDs.value.length > 0
)

const diffDsShow = computed(
  () => curLinkageTargetViewsInfoDiffDs.value && curLinkageTargetViewsInfoDiffDs.value.length > 0
)

const dialogInit = viewItem => {
  state.showSelected = false
  dialogShow.value = true
  state.initState = false
  init(viewItem)
}

const linkageSetting = curViewId => {
  // sourceViewId 也加入查询
  const targetViewIds = []
  findAllViewsId(componentData.value, targetViewIds)

  // 获取当前仪表板当前图表联动信息
  const requestInfo = {
    dvId: dvInfo.value.id,
    sourceViewId: curViewId,
    targetViewIds: targetViewIds,
    linkageInfo: null
  }
  getViewLinkageGatherArray(requestInfo).then(rsp => {
    // 获取当前仪表板的图表(去掉当前图表)
    curLinkageTargetViewsInfo.value = rsp.data || []
    curLinkageTargetViewsInfo.value.forEach(item => {
      if (item.targetViewId === curViewId) {
        state.sourceLinkageInfo = item
      }
    })
    curLinkageTargetViewsInfo.value = curLinkageTargetViewsInfo.value.filter(
      viewInfo => viewInfo.targetViewId !== state.viewId
    )

    curLinkageTargetViewsInfoSameDs.value = curLinkageTargetViewsInfo.value.filter(
      viewInfo => viewInfo.tableId === state.tableId
    )

    curLinkageTargetViewsInfoDiffDs.value = curLinkageTargetViewsInfo.value.filter(
      viewInfo => viewInfo.tableId !== state.tableId
    )

    let firstNode
    let linkageTreeName
    if (curLinkageTargetViewsInfoSameDs.value && curLinkageTargetViewsInfoSameDs.value.length > 0) {
      firstNode = curLinkageTargetViewsInfoSameDs.value[0]
      linkageTreeName = 'sameDs'
    } else if (
      curLinkageTargetViewsInfoDiffDs.value &&
      curLinkageTargetViewsInfoDiffDs.value.length > 0
    ) {
      firstNode = curLinkageTargetViewsInfoDiffDs.value[0]
      linkageTreeName = 'diffDs'
    }
    state.initState = true
    nextTick(() => {
      if (firstNode) {
        const linkageTree =
          linkageTreeName === 'sameDs' ? linkageInfoTree.value : linkageInfoTreeDiffDs.value
        linkageTree.setCurrentKey(firstNode.targetViewId)
      }
      nodeClick(firstNode)
      sameDsTreeSelectedChange()
    })
  })
}

const init = viewItem => {
  state.initState = false
  state.viewId = viewItem.id
  curLinkageTargetViewsInfo.value = []
  const chartDetails = canvasViewInfo.value[state.viewId]
  state.curLinkageViewInfo = chartDetails
  if (chartDetails.tableId) {
    state.tableId = chartDetails.tableId
    // 获取当前数据集信息
    getDatasetDetails(chartDetails.tableId).then(res => {
      state.curDatasetInfo = res || {}
    })
  }
  customLinkageActive.value = curComponent.value.actionSelection
  linkageSetting(state.viewId)
}

const saveLinkageSetting = () => {
  // 字段检查
  let subCheckCountAll = 0
  curLinkageTargetViewsInfo.value.forEach(linkageInfo => {
    let subCheckCount = 0
    const linkageFields = linkageInfo['linkageFields']
    if (linkageFields && linkageInfo.linkageActive) {
      linkageFields.forEach(function (linkage) {
        if (!(linkage.sourceField && linkage.targetField)) {
          subCheckCount++
          subCheckCountAll++
        }
      })
    }

    if (subCheckCount > 0) {
      ElMessage.error(
        '【' + linkageInfo.targetViewName + '】-' + t('visualization.exit_un_march_linkage_field')
      )
      return
    }
  })
  if (subCheckCountAll) {
    return
  }

  const request = {
    dvId: dvInfo.value.id,
    sourceViewId: state.viewId,
    linkageInfo: curLinkageTargetViewsInfo.value
  }
  loading.value = true
  saveLinkage(request)
    .then(() => {
      curComponent.value.actionSelection.linkageActive = customLinkageActive.value.linkageActive
      snapshotStore.recordSnapshotCache()
      ElMessage.success('保存成功')
      // 刷新联动信息
      getPanelAllLinkageInfo(dvInfo.value.id).then(rsp => {
        dvMainStore.setNowPanelTrackInfo(rsp.data)
      })
      cancelLinkageSetting()
      // 刷新跳转信息
      queryVisualizationJumpInfo(dvInfo.value.id).then(rsp => {
        dvMainStore.setNowPanelJumpInfo(rsp.data)
        cancel()
      })
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

const cancelLinkageSetting = () => {
  dvMainStore.clearLinkageSettingInfo()
}

const nodeClickPre = (data, treeName) => {
  if (treeName === 'sameDs') {
    linkageInfoTree.value.setCurrentKey(data.targetViewId)
    linkageInfoTreeDiffDs.value.setCurrentKey(null)
  } else {
    linkageInfoTree.value.setCurrentKey(null)
    linkageInfoTreeDiffDs.value.setCurrentKey(data.targetViewId)
  }
  nodeClick(data)
}

const nodeClick = data => {
  state.linkageInfo = data
}

const addLinkageFieldAdaptor = (data, sourceFieldId?, targetFieldId?) => {
  const linkageFieldItem = {
    sourceField: sourceFieldId,
    targetField: targetFieldId
  }
  data.linkageFields.push(linkageFieldItem)
}

const addLinkageField = (sourceFieldId?, targetFieldId?) => {
  const linkageFieldItem = {
    sourceField: sourceFieldId,
    targetField: targetFieldId
  }
  state.linkageInfo.linkageFields.push(linkageFieldItem)
}
const deleteLinkageField = index => {
  state.linkageInfo.linkageFields.splice(index, 1)
}

const linkageFieldAdaptor = async data => {
  if (data.linkageActive) {
    // 初始化映射关系 如果当前是相同的数据集且没有关联关系，则自动补充映射关系
    const targetChartDetails = canvasViewInfo.value[data.targetViewId]
    if (targetChartDetails && targetChartDetails.tableId && data.linkageFields.length === 0) {
      if (state.curLinkageViewInfo.tableId === targetChartDetails.tableId) {
        // 只匹配联动字段为0的 避免已经匹配过的重新匹配
        if (data.linkageFields && data.linkageFields.length === 0) {
          const curCheckAllAxisStr =
            JSON.stringify(state.curLinkageViewInfo.xAxis) +
            JSON.stringify(state.curLinkageViewInfo.xAxisExt) +
            (state.curLinkageViewInfo.type.includes('chart-mix')
              ? JSON.stringify(state.curLinkageViewInfo.extBubble)
              : '')
          const targetCheckAllAxisStr =
            JSON.stringify(targetChartDetails.xAxis) +
            JSON.stringify(targetChartDetails.xAxisExt) +
            (targetChartDetails.type.includes('chart-mix')
              ? JSON.stringify(targetChartDetails.extBubble)
              : '')
          state.sourceLinkageInfo.targetViewFields.forEach(item => {
            if (
              curCheckAllAxisStr.includes(item.id) &&
              targetCheckAllAxisStr.includes(item.id) &&
              data.linkageFields
            ) {
              addLinkageFieldAdaptor(data, item.id, item.id)
            }
          })
        }
      } else {
        addLinkageFieldAdaptor(data, '', '')
      }
    }
  }
}

const sourceLinkageInfoFilter = computed(() => {
  if (state.sourceLinkageInfo.targetViewFields) {
    const curCheckAllAxisStr =
      JSON.stringify(state.curLinkageViewInfo.xAxis) +
      JSON.stringify(state.curLinkageViewInfo.drillFields) +
      JSON.stringify(state.curLinkageViewInfo.xAxisExt) +
      (state.curLinkageViewInfo.type.includes('chart-mix')
        ? JSON.stringify(state.curLinkageViewInfo.extBubble)
        : '') +
      (state.curLinkageViewInfo.type.includes('table-normal')
        ? JSON.stringify(state.curLinkageViewInfo.yAxis)
        : '')
    return state.sourceLinkageInfo.targetViewFields.filter(item =>
      curCheckAllAxisStr.includes(item.id)
    )
  } else {
    return []
  }
})

const targetViewCheckedChange = (treeName, data) => {
  nextTick(() => {
    if (treeName === 'sameDs') {
      linkageInfoTree.value.setCurrentKey(data.targetViewId)
      linkageInfoTreeDiffDs.value.setCurrentKey(null)
    } else {
      linkageInfoTree.value.setCurrentKey(null)
      linkageInfoTreeDiffDs.value.setCurrentKey(data.targetViewId)
    }
    nodeClick(data)
    linkageFieldAdaptor(data)
    sameDsTreeSelectedChange()
  })
}
const cancel = () => {
  dialogShow.value = false
  state.initState = false
}

const filterNodeMethod = (value, data) => {
  return !value || data.linkageActive
}

watch(
  () => state.showSelected,
  newValue => {
    linkageInfoTree.value?.filter(newValue)
    linkageInfoTreeDiffDs.value?.filter(newValue)
  }
)

defineExpose({
  dialogInit
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

.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 470px;
  background-size: 100% 100% !important;
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
  overflow: hidden; /*超出部分隐藏*/
  white-space: nowrap; /*不换行*/
  text-overflow: ellipsis; /*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  border-radius: 3px;
}

.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
  flex-direction: column;
  span {
    line-height: 22px;
    color: #646a73;
  }
}

.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
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
    display: flex;
    align-items: center;
    justify-content: end;
    margin-right: 16px;
    font-weight: 400;
    font-size: 12px;
    color: #646a73;
    .ed-switch {
      margin-left: 8px;
    }
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
  display: flex;
  flex-direction: row;
  align-items: center;
}

.top-area-text {
  font-weight: 400;
  font-size: 14px;
  color: #646a73;
  margin-left: 24px;
}

.top-area-value {
  font-weight: 400;
  font-size: 14px;
  color: #1f2329;
  display: flex;
  flex-direction: row;
  align-items: center;
}
.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}
.content-head {
  height: 22px;
  margin-top: 10px;
  margin-left: 16px;
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
  line-height: 32px;
  margin-right: 16px;
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
  height: 100%;
  overflow: hidden;
}
.url-text {
  width: 100%;
  line-height: 14px;
  margin-bottom: 8px;
}

.tree-select-field {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.custom-tree {
  max-height: 100%;
  overflow-y: auto;
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

.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.tree-dataset-head {
  height: 40px;
  font-size: 14px;
  align-items: center;
  padding: 0 14px;
  justify-content: space-between;
  span {
    font-size: 14px;
    font-weight: 400;
    text-align: left;
    color: #646a73;
  }
}

.tree-dataset-head-top {
  border-top: 1px solid rgba(31, 35, 41, 0.15);
}

.toggle-icon {
  cursor: pointer;
  margin-right: 8px;
}
</style>
