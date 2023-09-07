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
    <el-row v-if="state.initState" style="height: 550px">
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
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: auto">
            <el-row class="tree-head">
              <span class="head-text">选择视图</span>
              <span class="head-filter"
                >仅看已选 <el-switch size="small" v-model="state.showSelected" />
              </span>
            </el-row>
            <el-tree
              class="custom-tree"
              menu
              ref="linkageInfoTree"
              :empty-text="'暂无可用图表'"
              :filter-node-method="filterNodeMethod"
              :data="curLinkageOnlyTargetViewsInfo"
              node-key="targetViewId"
              highlight-current
              :props="state.treeProp"
              @node-click="nodeClick"
            >
              <template #default="{ data }">
                <span class="custom-tree-node">
                  <span>
                    <div @click.stop>
                      <span class="auth-span">
                        <!--？？？-->
                        <el-checkbox
                          v-model="data.linkageActive"
                          @change="targetViewCheckedChange(data)"
                        />
                      </span>
                    </div>
                  </span>
                  <span>
                    <span class="tree-select-field">
                      <Icon
                        class-name="view-type-icon"
                        style="margin-right: 2px"
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
            <el-row class="content-head">配置视图间的字段关联关系</el-row>
            <el-row v-if="state.linkageInfo">
              <el-row style="margin-top: 5px">
                <el-row class="inner-content">
                  <el-col :span="11"> 当前视图源字段 </el-col>
                  <el-col :span="2"></el-col>
                  <el-col :span="11">
                    {{ t('visualization.link_view_field') }}
                  </el-col>
                </el-row>
                <el-row style="display: inline-block; max-height: 350px; overflow-y: auto">
                  <el-row
                    style="padding: 0 16px 8px"
                    v-for="(itemLinkage, index) in state.linkageInfo.linkageFields"
                    :key="index"
                  >
                    <el-col :span="10">
                      <div class="select-filed">
                        <el-select
                          v-model="itemLinkage.sourceField"
                          :placeholder="'请选择字段'"
                          style="width: 100%"
                        >
                          <el-option
                            v-for="item in state.sourceLinkageInfo.targetViewFields"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                          >
                            <span style="float: left">
                              <Icon
                                style="width: 14px; height: 14px"
                                :name="`field_${fieldType[item.deType]}`"
                                :className="`field-icon-${fieldType[item.deType]}`"
                              ></Icon>
                            </span>
                            <span style="float: left; font-size: 12px">{{ item.name }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </el-col>
                    <el-col :span="2" class="link-icon-area">
                      <Icon style="width: 20px; height: 20px" name="dv-link-target"></Icon>
                    </el-col>
                    <el-col :span="11">
                      <div class="select-filed">
                        <el-select
                          v-model="itemLinkage.targetField"
                          :placeholder="'请选择'"
                          style="width: 100%"
                          size="mini"
                        >
                          <el-option
                            v-for="item in state.linkageInfo.targetViewFields"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                          >
                            <span style="float: left">
                              <Icon
                                style="width: 14px; height: 14px"
                                :name="`field_${fieldType[item.deType]}`"
                                :className="`field-icon-${fieldType[item.deType]}`"
                              ></Icon>
                            </span>
                            <span style="float: left; font-size: 12px">{{ item.name }}</span>
                          </el-option>
                        </el-select>
                      </div>
                    </el-col>
                    <el-col :span="1">
                      <el-icon
                        style="margin-top: 10px; cursor: pointer"
                        @click="deleteLinkageField(index)"
                      >
                        <Delete />
                      </el-icon>
                    </el-col>
                  </el-row>
                </el-row>
                <el-row style="width: 100%; padding-left: 16px">
                  <el-button
                    type="primary"
                    size="mini"
                    icon="Plus"
                    text
                    @click="addLinkageField('', '')"
                    >追加联动依赖字段
                  </el-button>
                </el-row>
              </el-row>
            </el-row>
            <el-row v-else style="height: 100%" class="custom-position">
              <Icon style="width: 116px; height: 100px" name="dv-empty"></Icon>
              <span>请先勾选需要联动的图表</span>
            </el-row>
          </el-col>
        </el-row>
      </el-row>
      <el-row class="root-class">
        <el-button size="mini" @click="cancel()">{{ t('common.cancel') }} </el-button>
        <el-button type="primary" size="mini" @click="saveLinkageSetting()"
          >{{ t('dataset.confirm') }}
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
import { reactive, ref, nextTick, onMounted, computed, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { fieldType } from '@/utils/attr'
import { CalcFieldType } from '@/views/visualized/data/dataset/form/CalcFieldEdit.vue'
import {
  getPanelAllLinkageInfo,
  getViewLinkageGatherArray,
  saveLinkage
} from '@/api/visualization/linkage'
import { getDatasetDetails } from '@/api/dataset'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewInfo, componentData } = storeToRefs(dvMainStore)
const linkageInfoTree = ref(null)
const { t } = useI18n()
const dialogShow = ref(false)
const searchField = ref('')

const state = reactive({
  sourceLinkageInfo: {},
  curLinkageTargetViewsInfo: [],
  showSelected: false,
  curLinkageViewInfo: {},
  curDatasetInfo: {},
  initState: false,
  viewId: null,
  loading: false,
  treeProp: {
    id: 'targetViewId',
    label: 'targetViewName',
    children: 'children'
  },
  linkageInfo: null
})

const dialogInit = viewItem => {
  dialogShow.value = true
  state.initState = false
  init(viewItem)
}

const linkageSetting = curViewId => {
  // sourceViewId 也加入查询
  const targetViewIds = componentData.value
    .filter(item => item.component === 'UserView')
    .map(item => item.id)

  // 获取当前仪表板当前视图联动信息
  const requestInfo = {
    dvId: dvInfo.value.id,
    sourceViewId: curViewId,
    targetViewIds: targetViewIds,
    linkageInfo: null
  }
  getViewLinkageGatherArray(requestInfo).then(rsp => {
    // 获取当前仪表板的视图(去掉当前视图)
    state.curLinkageTargetViewsInfo = rsp.data
    state.curLinkageTargetViewsInfo.forEach(item => {
      if (item.targetViewId === curViewId) {
        state.sourceLinkageInfo = item
      }
    })
    let firstNode
    if (curLinkageOnlyTargetViewsInfo.value && curLinkageOnlyTargetViewsInfo.value.length > 0) {
      firstNode = curLinkageOnlyTargetViewsInfo.value[0]
    }
    state.initState = true
    nextTick(() => {
      if (firstNode) {
        linkageInfoTree.value.setCurrentKey(firstNode.targetViewId)
      }
      nodeClick(firstNode)
    })
  })
}

const init = viewItem => {
  state.initState = false
  state.viewId = viewItem.id
  state.curLinkageTargetViewsInfo = []
  const chartDetails = canvasViewInfo.value[state.viewId]
  state.curLinkageViewInfo = chartDetails
  if (chartDetails.tableId) {
    // 获取当前数据集信息
    getDatasetDetails(chartDetails.tableId).then(res => {
      state.curDatasetInfo = res || {}
    })
  }
  linkageSetting(state.viewId)
}

const saveLinkageSetting = () => {
  // 字段检查
  let subCheckCountAll = 0
  state.curLinkageTargetViewsInfo.forEach(linkageInfo => {
    let subCheckCount = 0
    const linkageFields = linkageInfo['linkageFields']
    if (linkageFields) {
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
    linkageInfo: state.curLinkageTargetViewsInfo
  }
  saveLinkage(request).then(rsp => {
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
  })
}

const cancelLinkageSetting = () => {
  dvMainStore.clearLinkageSettingInfo()
}

const nodeClick = (data, node?) => {
  state.linkageInfo = data
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
const targetViewCheckedChange = data => {
  nextTick(() => {
    linkageInfoTree.value.setCurrentKey(data.targetViewId)
    nodeClick(data)
  })
}
const cancel = () => {
  dialogShow.value = false
  state.initState = false
}

const filterNodeMethod = (value, data) => {
  return !value || data.linkageActive
}

const curLinkageOnlyTargetViewsInfo = computed(() =>
  state.curLinkageTargetViewsInfo.filter(viewInfo => viewInfo.targetViewId !== state.viewId)
)

watch(
  () => state.showSelected,
  (newValue, oldValue) => {
    linkageInfoTree.value.filter(newValue)
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
  margin-left: 24px;
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
  height: 22px;
  margin-top: 10px;
  margin-left: 16px;
  font-weight: 500;
  font-size: 14px;
  color: #1f2329;
  line-height: 32px;
  margin-right: 16px;
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
  height: 100%;
  overflow-y: auto;
}
</style>
