<template>
  <el-dialog
    v-loading="state.loading"
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
          <span class="top-area-text">{{ t('visualization.selected_view') }}：</span>
          <span class="top-area-value">
            <Icon class-name="view-type-icon"
              ><component
                class="svg-icon view-type-icon"
                :is="iconChartMap[state.curJumpViewInfo.type]"
              ></component
            ></Icon>
            {{ state.curJumpViewInfo.title }}</span
          >
          <span class="top-area-text margin-left">{{ t('visualization.used_dataset') }}：</span>
          <span class="top-area-value">
            <Icon name="dataset-outline"
              ><datasetOutline style="vertical-align: -0.2em" class="svg-icon view-type-icon"
            /></Icon>
            {{ state.curDatasetInfo.name }}</span
          >
        </div>
      </el-row>
      <el-row v-loading="state.loading">
        <el-row class="preview">
          <el-col :span="8" style="height: 100%; overflow-y: auto">
            <el-row class="tree-head">
              <span class="head-text">{{ t('visualization.to_select_view') }}</span>
              <span class="head-filter">
                {{ t('visualization.show_selected_only') }}
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
                  <span :title="data.sourceFieldName">
                    <span class="tree-select-field">
                      <el-icon style="margin-right: 4px">
                        <Icon
                          ><component
                            class="svg-icon"
                            :class="`field-icon-${fieldType[data.sourceDeType]}`"
                            :is="iconFieldMap[fieldType[data.sourceDeType]]"
                          ></component
                        ></Icon>
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
                  <el-radio-group
                    class="larger-radio"
                    v-if="state.linkJumpInfo"
                    v-model="state.linkJumpInfo.linkType"
                  >
                    <el-radio label="outer">{{ t('visualization.link_outer') }}</el-radio>
                    <el-radio label="inner">{{ t('visualization.dashboard_dataV') }}</el-radio>
                  </el-radio-group>
                  <el-radio-group class="larger-radio" v-if="!state.linkJumpInfo" disabled>
                    <el-radio label="outer">{{ t('visualization.link_outer') }}</el-radio>
                    <el-radio label="inner">{{ resourceType }}</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item class="radio-group-box">
                  <template #label>
                    <span class="title">{{ t('visualization.open_model') }}</span>
                  </template>
                  <el-radio-group
                    class="larger-radio"
                    v-if="state.linkJumpInfo"
                    v-model="state.linkJumpInfo.jumpType"
                  >
                    <el-radio label="_self">{{ t('visualization.now_window') }}</el-radio>
                    <el-radio label="_blank">{{ t('visualization.new_window') }}</el-radio>
                    <el-radio label="newPop">{{ t('visualization.pop_window') }}</el-radio>
                  </el-radio-group>
                  <el-radio-group class="larger-radio" v-if="!state.linkJumpInfo" disabled>
                    <el-radio label="_self">{{ t('visualization.now_window') }}</el-radio>
                    <el-radio label="_blank">{{ t('visualization.new_window') }}</el-radio>
                    <el-radio label="newPop">{{ t('visualization.pop_window') }}</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item
                  class="radio-group-box"
                  v-if="state.linkJumpInfo?.jumpType === 'newPop'"
                >
                  <template #label>
                    <span class="title">{{ t('visualization.window_size') }}</span>
                  </template>
                  <el-radio-group class="larger-radio" v-model="state.linkJumpInfo.windowSize">
                    <el-radio label="large">{{ t('visualization.window_size_large') }}</el-radio>
                    <el-radio label="middle">{{ t('visualization.window_size_middle') }}</el-radio>
                    <el-radio label="small">{{ t('visualization.window_size_small') }}</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-header>

              <el-main class="settings-main">
                <template v-if="state.linkJumpInfo">
                  <template v-if="state.linkJumpInfo.linkType === 'inner'">
                    <el-form label-position="top" class="main-form">
                      <div class="m-row">
                        <div style="flex: 1">
                          <el-form-item>
                            <template #label>
                              {{
                                dvInfo.type === 'dashboard'
                                  ? t('visualization.cur_dashboard')
                                  : t('visualization.cur_screen')
                              }}
                            </template>
                            <el-select style="width: 100%" v-model="dvInfo.name" disabled>
                              <el-option
                                :key="dvInfo.name"
                                :label="dvInfo.name"
                                :value="dvInfo.name"
                              >
                              </el-option>
                            </el-select>
                          </el-form-item>
                        </div>
                        <div class="icon-center">
                          <Icon name="dv-link-target"
                            ><dvLinkTarget style="width: 20px; height: 20px" class="svg-icon"
                          /></Icon>
                        </div>
                        <div style="flex: 1">
                          <el-form-item>
                            <template #label>
                              {{ targetSource }}
                            </template>
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
                                <div class="label-content-details">
                                  <el-icon
                                    size="18px"
                                    style="display: inline-block"
                                    v-if="data.leaf"
                                  >
                                    <Icon name="dv-dashboard-spine"
                                      ><dvDashboardSpine
                                        v-if="data.type === 'dashboard'"
                                        class="svg-icon"
                                      />
                                      <dvScreenSpine v-else class="svg-icon"> </dvScreenSpine>
                                    </Icon>
                                  </el-icon>
                                  <el-icon size="18px" style="display: inline-block" v-else>
                                    <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                                  </el-icon>
                                  <span
                                    style="margin-left: 8px; font-size: 14px"
                                    :title="node.label"
                                    >{{ node.label }}</span
                                  >
                                </div>
                              </template>
                            </el-tree-select>
                          </el-form-item>
                        </div>
                      </div>
                      <template v-if="state.linkJumpInfo.targetDvId">
                        <div class="jump-com-list">
                          <el-tabs size="small" v-model="state.activeCollapse">
                            <el-tab-pane :label="t('visualization.linkage_view')" name="view">
                            </el-tab-pane>
                            <el-tab-pane
                              :label="t('visualization.with_filter_params')"
                              name="filter"
                            >
                            </el-tab-pane>
                          </el-tabs>
                        </div>
                        <template v-if="state.activeCollapse === 'view'">
                          <el-row style="margin-bottom: 8px" :gutter="8">
                            <el-col :span="7"> {{ t('visualization.source_field') }} </el-col>
                            <el-col :span="2"></el-col>
                            <el-col :span="7" style="margin-left: -2.9%">
                              {{ t('visualization.link_view_field') }}
                            </el-col>
                            <el-col :span="8"></el-col>
                          </el-row>
                          <div
                            class="main-scrollbar-container"
                            :class="{
                              'main-scrollbar-container-min':
                                state.linkJumpInfo?.jumpType === 'newPop'
                            }"
                          >
                            <el-scrollbar
                              height="fit-content"
                              :max-height="
                                state.linkJumpInfo?.jumpType === 'newPop' ? '138px' : '178px'
                              "
                            >
                              <div
                                style="display: flex; margin-bottom: 6px"
                                v-for="(
                                  targetViewInfo, index
                                ) in state.linkJumpInfo.targetViewInfoList.filter(
                                  item => item.targetType === 'view'
                                )"
                                :key="index"
                              >
                                <div style="flex: 1">
                                  <el-select
                                    v-model="targetViewInfo.sourceFieldActiveId"
                                    :placeholder="t('chart.pls_select_field')"
                                    style="width: 100%"
                                  >
                                    <el-option
                                      v-for="curViewField in state.linkJumpCurViewFieldArray"
                                      :key="curViewField.id"
                                      :label="curViewField.name"
                                      :value="curViewField.id"
                                    >
                                      <span class="custom-option">
                                        <Icon
                                          ><component
                                            class="svg-icon"
                                            style="width: 14px; height: 14px"
                                            :class="`field-icon-${fieldType[curViewField.deType]}`"
                                            :is="iconFieldMap[fieldType[curViewField.deType]]"
                                          ></component
                                        ></Icon>
                                        <span
                                          style="float: left; margin-left: 4px; font-size: 14px"
                                          >{{ curViewField.name }}</span
                                        >
                                      </span>
                                    </el-option>
                                  </el-select>
                                </div>
                                <div class="icon-center">
                                  <Icon name="dv-link-target"
                                    ><dvLinkTarget
                                      style="width: 20px; height: 20px"
                                      class="svg-icon"
                                  /></Icon>
                                </div>
                                <div style="flex: 1">
                                  <el-select
                                    v-model="targetViewInfo.targetViewId"
                                    :disabled="!targetViewInfo.sourceFieldActiveId"
                                    :placeholder="t('visualization.select_view')"
                                    style="width: 100%"
                                    @change="viewInfoOnChange(targetViewInfo)"
                                  >
                                    <el-option
                                      v-for="item in state.currentLinkPanelViewArray.filter(
                                        item => item.type !== 'outerParams'
                                      )"
                                      :key="item.id"
                                      :label="item.title"
                                      :value="item.id"
                                    >
                                      <span class="custom-option">
                                        <Icon
                                          ><component
                                            class="svg-icon view-type-icon"
                                            style="width: 14px; height: 14px"
                                            :is="iconChartMap[item.type]"
                                          ></component
                                        ></Icon>
                                        <span
                                          style="float: left; margin-left: 4px; font-size: 14px"
                                          >{{ item.title }}</span
                                        >
                                      </span>
                                    </el-option>
                                  </el-select>
                                </div>
                                <div style="flex: 1; margin: 0 8px">
                                  <el-select
                                    v-model="targetViewInfo.targetFieldId"
                                    :placeholder="t('chart.pls_select_field')"
                                    :disabled="fieldIdDisabledCheck(targetViewInfo)"
                                    style="width: 100%"
                                  >
                                    <el-option
                                      v-for="viewField in state.viewIdFieldArrayMap[
                                        targetViewInfo.targetViewId
                                      ]"
                                      :key="viewField.id"
                                      :label="viewField.name"
                                      :value="viewField.id"
                                    >
                                      <span class="custom-option">
                                        <Icon
                                          ><component
                                            class="svg-icon"
                                            style="width: 14px; height: 14px"
                                            :class="`field-icon-${fieldType[viewField.deType]}`"
                                            :is="iconFieldMap[fieldType[viewField.deType]]"
                                          ></component
                                        ></Icon>
                                        <span
                                          style="float: left; margin-left: 4px; font-size: 14px"
                                          >{{ viewField.name }}</span
                                        >
                                      </span>
                                    </el-option>
                                  </el-select>
                                </div>

                                <el-button
                                  class="m-del-icon-btn"
                                  text
                                  @click="deleteLinkJumpFieldById(targetViewInfo.targetId)"
                                >
                                  <el-icon size="20px">
                                    <Icon name="icon_delete-trash_outlined"
                                      ><icon_deleteTrash_outlined class="svg-icon"
                                    /></Icon>
                                  </el-icon>
                                </el-button>
                              </div>
                            </el-scrollbar>
                            <el-button
                              style="margin-top: 8px"
                              :disabled="!state.linkJumpInfo.targetDvId"
                              type="primary"
                              icon="Plus"
                              text
                              @click="addLinkJumpField('view')"
                            >
                              {{ t('visualization.add_jump_field') }}
                            </el-button>
                          </div>
                        </template>
                        <template v-if="state.activeCollapse === 'filter'">
                          <template v-if="state.currentOutParams.length === 0">
                            <span
                              >{{ t('visualization.link_target_tips1')
                              }}<a
                                class="target_jump"
                                @click="resourceEdit(state.linkJumpInfo.targetDvId)"
                                >{{ t('visualization.link_target_tips2') }}</a
                              ></span
                            >
                          </template>
                          <template v-else-if="state.linkJumpCurFilterFieldArray.length === 0">
                            <span>{{ t('visualization.jump_no_banding_tips') }}</span>
                          </template>
                          <template v-else-if="state.currentOutParams.length > 0">
                            <el-row style="margin-bottom: 8px" :gutter="8">
                              <el-col :span="12"> {{ t('visualization.source_filter') }} </el-col>
                              <el-col :span="1"></el-col>
                              <el-col :span="10" style="margin-left: -2.9%">
                                {{ t('visualization.link_outer_params') }}
                              </el-col>
                            </el-row>
                            <div
                              class="main-scrollbar-container"
                              :class="{
                                'main-scrollbar-container-min':
                                  state.linkJumpInfo?.jumpType === 'newPop'
                              }"
                            >
                              <el-scrollbar height="fit-content" max-height="178px">
                                <div
                                  style="display: flex; margin-bottom: 6px"
                                  v-for="(
                                    targetViewInfo, index
                                  ) in state.linkJumpInfo.targetViewInfoList.filter(
                                    item => item.targetType === 'outerParams'
                                  )"
                                  :key="index"
                                >
                                  <div style="flex: 1">
                                    <el-select
                                      v-model="targetViewInfo.sourceFieldActiveId"
                                      :placeholder="t('chart.pls_select_field')"
                                      style="width: 100%"
                                    >
                                      <el-option
                                        v-for="curFilterField in state.linkJumpCurFilterFieldArray"
                                        :key="curFilterField.id"
                                        :label="curFilterField.name"
                                        :value="curFilterField.id"
                                      >
                                        <span class="custom-option">
                                          <Icon
                                            ><component
                                              class="svg-icon"
                                              style="width: 14px; height: 14px"
                                              :is="iconChartMap['filter']"
                                            ></component
                                          ></Icon>
                                          <span
                                            style="float: left; margin-left: 4px; font-size: 14px"
                                            >{{ curFilterField.name }}</span
                                          >
                                        </span>
                                      </el-option>
                                    </el-select>
                                  </div>
                                  <div class="icon-center">
                                    <Icon name="dv-link-target"
                                      ><dvLinkTarget
                                        style="width: 20px; height: 20px"
                                        class="svg-icon"
                                    /></Icon>
                                  </div>
                                  <div style="flex: 1">
                                    <el-select
                                      v-model="targetViewInfo.targetViewId"
                                      :disabled="!targetViewInfo.sourceFieldActiveId"
                                      :placeholder="t('visualization.select_param')"
                                      style="width: 100%"
                                      @change="viewInfoOnChange(targetViewInfo)"
                                    >
                                      <el-option
                                        v-for="item in state.currentOutParams"
                                        :key="item.id"
                                        :label="item.title"
                                        :value="item.id"
                                      >
                                        <span class="custom-option">
                                          <Icon
                                            ><component
                                              class="svg-icon view-type-icon"
                                              style="width: 14px; height: 14px"
                                              :is="iconChartMap[item.type]"
                                            ></component
                                          ></Icon>
                                          <span
                                            style="float: left; margin-left: 4px; font-size: 14px"
                                            >{{ item.title }}</span
                                          >
                                        </span>
                                      </el-option>
                                    </el-select>
                                  </div>

                                  <el-button
                                    class="m-del-icon-btn"
                                    text
                                    @click="deleteLinkJumpFieldById(targetViewInfo.targetId)"
                                  >
                                    <el-icon size="20px">
                                      <Icon name="icon_delete-trash_outlined"
                                        ><icon_deleteTrash_outlined class="svg-icon"
                                      /></Icon>
                                    </el-icon>
                                  </el-button>
                                </div>
                              </el-scrollbar>
                              <el-button
                                style="margin-top: 8px"
                                :disabled="!state.linkJumpInfo.targetDvId"
                                type="primary"
                                icon="Plus"
                                text
                                @click="addLinkJumpField('outerParams')"
                              >
                                {{ t('visualization.add_jump_field') }}
                              </el-button>
                            </div>
                          </template>
                        </template>
                      </template>
                      <template v-else>
                        <empty-background
                          style="height: auto"
                          :description="selectSourceTips"
                          img-type="noneWhite"
                        />
                      </template>
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
                            <el-icon size="16px" class="hint-icon">
                              <Icon name="icon_info_outlined"
                                ><icon_info_outlined class="svg-icon"
                              /></Icon>
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
                            <el-icon size="16px" class="hint-icon">
                              <Icon name="icon_info_outlined"
                                ><icon_info_outlined class="svg-icon"
                              /></Icon>
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
                          <el-scrollbar style="margin-top: 12px" height="250px">
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
                                  ><component
                                    class="svg-icon"
                                    :class="`field-icon-${fieldType[item.sourceDeType]}`"
                                    :is="iconFieldMap[fieldType[item.sourceDeType]]"
                                  ></component
                                ></Icon>
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
                  <empty-background
                    :description="t('visualization.select_dimension_hint')"
                    img-type="noneWhite"
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
    <XpackComponent
      ref="openHandler"
      jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI="
    />
  </el-dialog>
</template>

<script lang="ts" setup>
import { iconFieldMap } from '@/components/icon-group/field-list'
import { iconChartMap } from '@/components/icon-group/chart-list'
import datasetOutline from '@/assets/svg/dataset-outline.svg'
import dvLinkTarget from '@/assets/svg/dv-link-target.svg'
import dvDashboardSpine from '@/assets/svg/dv-dashboard-spine.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import dvScreenSpine from '@/assets/svg/dv-screen-spine.svg'
import {
  queryVisualizationJumpInfo,
  queryWithViewId,
  updateJumpSet,
  viewTableDetailList
} from '@/api/visualization/linkJump'
import { reactive, ref, nextTick, computed, watch } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { fieldType } from '@/utils/attr'
import { storeToRefs } from 'pinia'
import { findDvType, queryTreeApi } from '@/api/visualization/dataVisualization'
import { ElMessage, ElScrollbar } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetDetails, listFieldByDatasetGroup } from '@/api/dataset'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
import JumpSetOuterContentEditor from '@/components/visualization/JumpSetOuterContentEditor.vue'
import { Search } from '@element-plus/icons-vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { filterEmptyFolderTree } from '@/utils/canvasUtils'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useAppStoreWithOut } from '@/store/modules/app'
import { XpackComponent } from '@/components/plugin'
import { useCache } from '@/hooks/web/useCache'
import { useEmbedded } from '@/store/modules/embedded'
import { guid } from '@/views/visualized/data/dataset/form/util'
import treeSort from '@/utils/treeSortUtils'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, canvasViewInfo, componentData } = storeToRefs(dvMainStore)
const linkJumpInfoTree = ref(null)
const { t } = useI18n()
const dialogShow = ref(false)
const snapshotStore = snapshotStoreWithOut()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()

const resourceType = computed(() =>
  dvInfo.value.type === 'dashboard' ? t('work_branch.dashboard') : t('work_branch.big_data_screen')
)

const selectSourceTips = t('visualization.select_target_resource')

const targetSource = t('visualization.target_dashboard_dataV')

const state = reactive({
  curDataVWeight: 0,
  activeCollapse: 'view',
  loading: false,
  showSelected: false,
  curJumpViewInfo: {},
  curDatasetInfo: {},
  tempId: null,
  initState: false,
  viewId: null,
  name2Auto: [],
  searchField: '',
  searchFunction: '',
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
    isLeaf: 'leaf',
    disabled: 'disabled'
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
  linkJumpCurFilterFieldArray: [], //当前过滤条件明细
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
  quota: [],
  currentOutParams: []
})
const { wsCache } = useCache()

const outerContentEditor = ref(null)

const dialogInit = viewItem => {
  state.showSelected = false
  dialogShow.value = true
  state.initState = false
  init(viewItem)
}

const initCurFilterFieldArray = componentDataCheck => {
  componentDataCheck.forEach(componentItem => {
    if (componentItem.component === 'VQuery' && componentItem.propValue instanceof Array) {
      componentItem.propValue.forEach(filterItem => {
        if (filterItem.checkedFields.includes(state.viewId)) {
          state.linkJumpCurFilterFieldArray.push({
            id: filterItem.id,
            name: filterItem.name,
            deType: 'filter'
          })
        }
      })
    } else if (componentItem.component === 'Group') {
      initCurFilterFieldArray(componentItem.propValue)
    } else if (componentItem.component === 'DeTabs') {
      componentItem.propValue.forEach(tabItem => {
        initCurFilterFieldArray(tabItem.componentData)
      })
    }
  })
}

const init = viewItem => {
  state.initState = false
  state.viewId = viewItem.id
  state.activeCollapse = 'view'
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
  const request = { busiFlag: 'dashboard-dataV' } as BusiTreeRequest
  // 获取可关联的仪表板
  queryTreeApi(request).then(rsp => {
    if (rsp && rsp[0]?.id === '0') {
      state.panelList = rsp[0].children
    } else {
      state.panelList = rsp
    }
    state.panelList = filterEmptyFolderTree(state.panelList)
    const curSortType = wsCache.get(`TreeSort-${dvInfo.value.type}`) || 'time_asc'
    state.panelList = treeSort(state.panelList, curSortType)
  })

  // 获取当前过滤条件明细 过滤原则：1.在当前仪表板或者大屏 2.作用于当前图表
  state.linkJumpCurFilterFieldArray = []
  initCurFilterFieldArray(componentData.value)

  if (chartDetails.tableId) {
    // 获取当前数据集信息
    getDatasetDetails(chartDetails.tableId).then(res => {
      state.curDatasetInfo = res || {}
    })
    // 获取当前图表的字段信息
    listFieldByDatasetGroup(chartDetails.tableId).then(rsp => {
      state.linkJumpCurViewFieldArray = []
      const sourceCurViewFieldArray = rsp.data
      sourceCurViewFieldArray.forEach(fieldItem => {
        if (checkAllAxisStr.indexOf(fieldItem.id) > -1) {
          state.linkJumpCurViewFieldArray.push(fieldItem)
        }
      })
    })

    // 获取当前图表的关联信息
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
        linkJumpInfoTree.value.setCurrentKey(firstNode?.sourceFieldId)
        nodeClick(firstNode)
      })
    })
  }
}

const save = () => {
  // 字段检查
  let subCheckCountAll = 0
  state.linkJump.linkJumpInfoArray.forEach(linkJumpInfo => {
    if (linkJumpInfo.checked) {
      let subCheckCount = 0
      if (linkJumpInfo.linkType === 'inner') {
        if (!linkJumpInfo.targetDvId) {
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
        ElMessage.error(t('visualization.jump_null_tips', [linkJumpInfo.sourceFieldName]))
      }
    }
  })
  if (subCheckCountAll) {
    return
  }
  state.loading = true
  updateJumpSet(state.linkJump)
    .then(() => {
      snapshotStore.recordSnapshotCache('updateJumpSet')
      ElMessage.success(t('common.save_success'))
      // 刷新跳转信息
      queryVisualizationJumpInfo(dvInfo.value.id).then(rsp => {
        dvMainStore.setNowPanelJumpInfo(rsp.data)
        cancel()
      })
      state.loading = false
    })
    .catch(() => {
      state.loading = false
    })
}
const nodeClick = data => {
  state.linkJumpInfo = state.mapJumpInfoArray[data.sourceFieldId]
  if (!state.linkJumpInfo.windowSize) {
    state.linkJumpInfo.windowSize = 'middle'
  }
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

// 获取当前图表字段 关联仪表板的图表信息列表
const getPanelViewList = dvId => {
  viewTableDetailList(dvId).then(rsp => {
    state.viewIdFieldArrayMap = {}
    state.currentLinkPanelViewArray = rsp.data.visualizationViewTables
    if (state.currentLinkPanelViewArray) {
      state.currentLinkPanelViewArray.forEach(view => {
        state.viewIdFieldArrayMap[view.id] = view.tableFields
      })
    }
    // 外部参数 currentLinkPanelViewArray 也加入
    // 在图表侧进行隐藏 保存的时候直接保存currentLinkPanelViewArray 方便处理
    state.currentOutParams = rsp.data.outParamsJumpInfo || []
    if (state.currentOutParams && state.currentOutParams.length > 0) {
      state.currentOutParams.forEach(outerParamsItem => {
        state.currentLinkPanelViewArray.push(outerParamsItem)
        state.viewIdFieldArrayMap[outerParamsItem.id] = [
          { id: '1000001', name: t('visualization.out_params_no_select') }
        ]
      })
    }
    // 增加过滤组件匹配
    JSON.parse(rsp.data.bashComponentData).forEach(componentItem => {
      if (componentItem.component === 'VQuery' && componentItem.propValue instanceof Array) {
        componentItem.propValue.forEach(filterItem => {
          state.currentLinkPanelViewArray.push({
            id: filterItem.id,
            type: 'filter',
            name: filterItem.name,
            title: filterItem.name
          })
          state.viewIdFieldArrayMap[filterItem.id] = [
            { id: '1000001', name: t('visualization.filter_no_select') }
          ]
        })
      }
    })
  })
}

const dvNodeClick = data => {
  if (data.leaf) {
    state.curDataVWeight = data.weight
    state.linkJumpInfo.targetViewInfoList = []
    addLinkJumpField()
    getPanelViewList(data.id)
  }
}
const addLinkJumpField = (type = 'view') => {
  state.linkJumpInfo.targetViewInfoList.push({
    targetId: guid(),
    targetViewId: '',
    targetType: type,
    targetFieldId: ''
  })
}

const deleteLinkJumpFieldById = targetId => {
  if (targetId) {
    let indexResult
    state.linkJumpInfo.targetViewInfoList.forEach((item, index) => {
      if (targetId === item.targetId) {
        indexResult = index
      }
    })
    if (indexResult !== undefined) {
      state.linkJumpInfo.targetViewInfoList.splice(indexResult, 1)
    }
  }
}

const fieldIdDisabledCheck = targetViewInfo => {
  return (
    (state.viewIdFieldArrayMap[targetViewInfo.targetViewId] &&
      state.viewIdFieldArrayMap[targetViewInfo.targetViewId].length === 1 &&
      state.viewIdFieldArrayMap[targetViewInfo.targetViewId][0].id === '1000001') ||
    !targetViewInfo.sourceFieldActiveId
  )
}

const viewInfoOnChange = targetViewInfo => {
  if (
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId] &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId].length === 1 &&
    state.viewIdFieldArrayMap[targetViewInfo.targetViewId][0].id === '1000001'
  ) {
    targetViewInfo.targetFieldId = '1000001'
  } else {
    targetViewInfo.targetFieldId = null
  }
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

const insertFieldToCodeMirror = (value: string) => {
  outerContentEditor.value.insertFieldToCodeMirror(value)
}

const outerContentShow = computed(() => {
  return state.linkJumpInfo && state.linkJumpInfo.linkType === 'outer' && dialogShow.value
})

const filterNodeMethod = (value, data) => {
  return !value || data.checked
}

const isEmbedded = computed(() => appStore.getIsDataEaseBi || appStore.getIsIframe)
const openType = '_blank'

const resourceEdit = async resourceId => {
  if (state.curDataVWeight && state.curDataVWeight < 7) {
    ElMessage.error(t('visualization.no_edit_auth'))
    return
  }
  let busiFlagResult
  await findDvType(resourceId).then(res => {
    busiFlagResult = res.data
  })
  const baseUrl = busiFlagResult === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  if (isEmbedded.value) {
    embeddedStore.clearState()
    if (dvInfo.value.type === 'dataV') {
      embeddedStore.setDvId(resourceId)
    } else {
      embeddedStore.setResourceId(resourceId)
    }
    useEmitt().emitter.emit(
      'changeCurrentComponent',
      dvInfo.value.type === 'dataV' ? 'VisualizationEditor' : 'DashboardEditor'
    )
    return
  }
  const newWindow = window.open(baseUrl + resourceId, openType)
  initOpenHandler(newWindow)
}

const openHandler = ref(null)
const initOpenHandler = newWindow => {
  if (openHandler?.value) {
    const pm = {
      methodName: 'initOpenHandler',
      args: newWindow
    }
    openHandler.value.invokeMethod(pm)
  }
}

watch(
  () => state.showSelected,
  newValue => {
    linkJumpInfoTree.value?.filter(newValue)
  }
)

watch(
  () => outerContentShow.value,
  newValue => {
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
  display: flex;
  height: 28px;
  padding: 1px 8px;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;

  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;

  border-radius: 4px;
  border: 1px solid #dee0e3;

  background: #fff;

  color: var(--neutral-900, #1f2329);
  /* 中文/桌面端/正文 14 22 Regular */
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;

  cursor: pointer;
}

.item-dimension + .item-dimension {
  margin-top: 4px;
}

.item-dimension:hover {
  border: 1px solid var(--ed-color-primary, #3370ff);
  background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
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

  &.margin-left {
    margin-left: 24px;
  }
}
.settings-container {
  height: 100%;

  .settings-header {
    height: auto;
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
    overflow: hidden;
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

      .m-row {
        width: 100%;
        display: flex;
      }

      .icon-center {
        padding: 0 8px;
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

.main-scrollbar-container-min {
  :deep(.ed-scrollbar) {
    height: fit-content;
    max-height: 138px !important;
  }
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
  display: flex;
  align-items: center;
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
  overflow: hidden;
}

.label-content-details {
  width: 100%;
  display: flex;
  align-items: center;
}
.hint-icon {
  margin-left: 4px;
  cursor: pointer;
  color: #646a73;
}
.m-del-icon-btn {
  color: #646a73;
  margin-top: 2px;
  margin-left: -4px;

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
.larger-radio {
  .ed-radio__inner {
    width: 16px;
    height: 16px;
  }
}

.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.jump-com-list {
  width: 100%;
  margin-top: -18px;
  :deep(.ed-collapse) {
    --ed-collapse-header-font-size: 14px;
    --ed-collapse-content-font-size: 14px;
  }
  :deep(.ed-tabs__active-bar) {
    height: 2px;
  }

  & > :deep(.ed-tabs) {
    --ed-tabs-header-height: 36px;
    margin-bottom: 12px;
    position: sticky;
    background: #fff;
    .ed-tabs__header {
      &::before {
        content: '';
        width: 8px;
        height: 1px;
        position: absolute;
        bottom: 0;
        left: 0;
        background: #1f232926;
      }
    }
  }

  :deep(.ed-tabs__item) {
    font-size: 14px;
  }

  :deep(.ed-tabs__item):not(.is-active) {
    color: #646a73;
  }
}

.target_jump {
  color: var(--ed-color-primary);
  cursor: pointer;
}
</style>
