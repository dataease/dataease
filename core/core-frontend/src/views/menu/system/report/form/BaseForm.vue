<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{ t('datasource.base_info') }}</span>
    </div>
    <el-form
      ref="reportBaseForm"
      class="report-form"
      :model="formState"
      :rules="baseRules"
      label-width="180px"
      label-position="top"
      :scroll-to-error="true"
    >
      <el-form-item :label="t('report.task_name')" prop="name">
        <el-input
          v-model="formState.name"
          :placeholder="t('sync_task.please_enter_task_name')"
          @input="taskNameChange"
        />
      </el-form-item>

      <el-form-item :label="t('report.form.title')" prop="title">
        <el-input
          v-model="formState.title"
          :placeholder="t('common.please_input') + t('common.empty') + t('report.form.title')"
          @change="titleChange"
        />
      </el-form-item>

      <el-form-item :label="t('report.form.content')" prop="content">
        <tinymce-editor
          class="report-content-tinymce"
          v-model="formState.content"
          :inline="false"
        />
      </el-form-item>

      <el-form-item :label="t('report.form.send_content')" prop="rtid">
        <el-radio-group v-model="formState.rtid" @change="rtidChange">
          <el-radio :label="0">{{ t('work_branch.dashboard') }}</el-radio>
          <el-radio :label="1">{{ t('work_branch.big_screen') }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        style="position: relative"
        :label="formState.rtid ? t('work_branch.big_screen') : t('work_branch.dashboard')"
        prop="rid"
      >
        <el-button
          @click="setDefaultConditionOpen"
          text
          type="primary"
          style="position: absolute; right: 0; top: -32px"
          >{{ t('report.form.filter') }}</el-button
        >
        <el-tree-select
          v-model="formState.rid"
          :data="state.panelList"
          :props="state.dvSelectProps"
          :render-after-expand="false"
          filterable
          @node-click="dvNodeClick"
          class="dv-selector"
        >
          <template #default="{ node, data }">
            <div class="label-content-details">
              <el-icon size="18px" style="display: inline-block" v-if="data.leaf">
                <Icon name="dv-dashboard-spine"><dvDashboardSpine class="svg-icon" /></Icon>
              </el-icon>
              <el-icon size="18px" style="display: inline-block" v-else>
                <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
              </el-icon>
              <span style="margin-left: 8px; font-size: 14px" :title="node.label">{{
                node.label
              }}</span>
            </div>
          </template>
        </el-tree-select>
      </el-form-item>

      <el-form-item v-if="panelWatermark" :label="t('report.form.water_mask')" prop="showWatermark">
        <el-checkbox v-model="formState.showWatermark" :label="t('report.form.show_water_mask')" />
      </el-form-item>

      <el-form-item :label="t('report.form.data_permission')" prop="dataPermission">
        <template v-slot:label>
          <div class="basic-form-info-tips">
            <span class="custom-form-item__label">{{ t('report.form.data_permission') }}</span>
            <el-tooltip
              effect="dark"
              :content="t('report.form.data_permission_tips')"
              placement="top"
            >
              <el-icon
                ><Icon name="dv-info"><dvInfo class="svg-icon" /></Icon
              ></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-radio-group v-model="formState.dataPermission" @change="dataPermissionChange">
          <el-radio :label="0">{{ t('report.form.creator_permission') }}</el-radio>
          <el-radio :label="1">{{ t('report.form.reci_permission') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="t('report.form.format')" prop="format">
        <el-radio-group v-model="formState.format">
          <el-radio :label="0">jpeg</el-radio>
          <el-radio :label="1">pdf</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
        style="position: relative"
        :label="t('report.form.view_data')"
        prop="viewIdList"
      >
        <el-checkbox
          v-model="formState.viewDataRange"
          :true-value="0"
          :false-value="1"
          :label="t('report.form.all_data')"
          style="position: absolute; right: 0; top: -28px"
        />
        <el-select
          ref="viewSelect"
          v-model="formState.viewIdList"
          multiple
          collapse-tags
          @change="setHeight"
          collapse-tags-tooltip
          :max-collapse-tags="3"
          :placeholder="t('common.please_select') + t('common.empty') + t('report.form.view_data')"
          style="width: 100%"
          @remove-tag="removeViewTag"
        >
          <el-option
            v-for="item in state.viewOptions"
            :key="item.id"
            :label="item.title"
            :value="item.id"
          >
            <span class="custom-option">
              <Icon class-name="view-type-icon" style="width: 14px; height: 14px"
                ><component
                  :is="iconMap[item.type]"
                  style="width: 14px; height: 14px"
                  class="svg-icon"
                ></component
              ></Icon>
              <span style="float: left; margin-left: 4px; font-size: 14px">
                {{ item.title }}
              </span>
            </span>
          </el-option>
          <template #tag="{ data }">
            <div ref="viewTags" class="ed-select__tags" :style="data">
              <div class="ed-select-tags-wrapper has-prefix">
                <el-tag
                  @close="removeViewTag(id)"
                  v-for="id in formState.viewIdList"
                  :key="id"
                  type="info"
                  closable
                >
                  <div v-if="state.viewMap[id]">
                    <span class="custom-option">
                      <Icon class-name="view-type-icon" style="width: 14px; height: 14px"
                        ><component
                          :is="iconMap[state.viewMap[id].type]"
                          style="width: 14px; height: 14px"
                          class="svg-icon"
                        ></component
                      ></Icon>
                      <span style="float: left; margin-left: 4px; font-size: 14px">{{
                        state.viewMap[id].title
                      }}</span>
                    </span>
                  </div>
                </el-tag>
              </div>
            </div>
          </template>
        </el-select>
      </el-form-item>
      <!-- <el-form-item label="图表数据范围" prop="viewDataRange">
        <el-radio-group v-model="formState.viewDataRange">
          <el-radio :label="0">展示数据</el-radio>
          <el-radio :label="1">全部数据</el-radio>
        </el-radio-group>
      </el-form-item> -->
      <el-form-item :label="t('report.form.pixel')" prop="pixel">
        <el-select
          v-model="formState.pixel"
          style="width: 100%"
          filterable
          clearable
          allow-create
          :placeholder="t('common.please_select') + t('common.empty') + t('report.form.pixel')"
          :popper-append-to-body="false"
        >
          <el-option-group v-for="group in pixelOptions" :key="group.label" :label="group.label">
            <el-option
              v-for="item in group.options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-option-group>
        </el-select>
      </el-form-item>
      <el-form-item :label="t('report.form.ext_wait_time')" prop="extWaitTime">
        <el-input-number
          style="width: 100%"
          v-model="formState.extWaitTime"
          autocomplete="off"
          step-strictly
          class="text-left"
          :min="0"
          :max="600"
          :placeholder="t('common.please_input') + t('report.form.ext_wait_time')"
          controls-position="right"
          type="number"
        />
      </el-form-item>
      <el-form-item :label="t('report.form.render_time')" prop="renderTime">
        <el-input-number
          style="width: 100%"
          v-model="formState.renderTime"
          autocomplete="off"
          step-strictly
          class="text-left"
          :min="0"
          :max="30"
          :placeholder="t('common.please_input') + t('report.form.render_time')"
          controls-position="right"
          type="number"
        />
      </el-form-item>
    </el-form>
    <condition-default-set
      ref="conditionDefaultSetRef"
      @reportFilterChange="reportFilterChange"
      :filter-info="state.customFilter"
    ></condition-default-set>
  </div>
</template>

<script lang="ts" setup>
import areaStack from '@/assets/svg/area-stack.svg'
import area from '@/assets/svg/area.svg'
import barGroupStack from '@/assets/svg/bar-group-stack.svg'
import barGroup from '@/assets/svg/bar-group.svg'
import barHorizontal from '@/assets/svg/bar-horizontal.svg'
import barRange from '@/assets/svg/bar-range.svg'
import barStackHorizontal from '@/assets/svg/bar-stack-horizontal.svg'
import barStack from '@/assets/svg/bar-stack.svg'
import bar from '@/assets/svg/bar.svg'
import bidirectionalBar from '@/assets/svg/bidirectional-bar.svg'
import bubbleMap from '@/assets/svg/bubble-map.svg'
import chartMixGroup from '@/assets/svg/chart-mix-group.svg'
import chartMixStack from '@/assets/svg/chart-mix-stack.svg'
import chartMixDualLine from '@/assets/svg/chart-mix-dual-line.svg'
import chartMix from '@/assets/svg/chart-mix.svg'
import flowMap from '@/assets/svg/flow-map.svg'
import funnel from '@/assets/svg/funnel.svg'
import gauge from '@/assets/svg/gauge.svg'
import heatMap from '@/assets/svg/heat-map.svg'
import indicator from '@/assets/svg/indicator.svg'
import line from '@/assets/svg/line.svg'
import liquid from '@/assets/svg/liquid.svg'
import map from '@/assets/svg/map.svg'
import percentageBarStackHorizontal from '@/assets/svg/percentage-bar-stack-horizontal.svg'
import percentageBarStack from '@/assets/svg/percentage-bar-stack.svg'
import pieDonutRose from '@/assets/svg/pie-donut-rose.svg'
import pieDonut from '@/assets/svg/pie-donut.svg'
import pieRose from '@/assets/svg/pie-rose.svg'
import pie from '@/assets/svg/pie.svg'
import progressBar from '@/assets/svg/progress-bar.svg'
import quadrant from '@/assets/svg/quadrant.svg'
import radar from '@/assets/svg/radar.svg'
import richText from '@/assets/svg/rich-text.svg'
import sankey from '@/assets/svg/sankey.svg'
import scatter from '@/assets/svg/scatter.svg'
import stockLine from '@/assets/svg/stock-line.svg'
import symbolicMap from '@/assets/svg/symbolic-map.svg'
import tableInfo from '@/assets/svg/table-info.svg'
import tableNormal from '@/assets/svg/table-normal.svg'
import tablePivot from '@/assets/svg/table-pivot.svg'
import treemap from '@/assets/svg/treemap.svg'
import waterfall from '@/assets/svg/waterfall.svg'
import wordCloud from '@/assets/svg/word-cloud.svg'
import tHeatmap from '@/assets/svg/t-heatmap.svg'
import bulletGraph from '@/assets/svg/bullet-graph.svg'
import circlePacking from '@/assets/svg/circle-packing.svg'
import dvDashboardSpine from '@/assets/svg/dv-dashboard-spine.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import { reactive, ref, onMounted, nextTick } from 'vue'
import { findById, queryTreeApi } from '@/api/visualization/dataVisualization'
import TinymceEditor from '@/components/rich-text/TinymceEditor.vue'
import { pixelOptions, baseRules } from './formUtil'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import { viewOptionApi } from '../api'
import ConditionDefaultSet from './ConditionDefaultSet.vue'
import defaultConditionTrans from '@/utils/CanvasInfoTransUtils'
import dvInfo from '@/assets/svg/dv-info.svg'
const { t } = useI18n()
const conditionDefaultSetRef = ref(null)

const props = defineProps({
  baseFormData: {
    type: Object,
    default: () => {}
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})
interface ViewItem {
  id: string
  title: string
  type: string
  pid?: string
}
const viewSelect = ref()
const viewTags = ref()
const customTitle = ref(false)
const reportBaseForm = ref(null)
const canvasInfo = ref(null)
const state = reactive({
  dvSelectProps: {
    label: 'name',
    children: 'children',
    value: 'id',
    isLeaf: 'leaf',
    disabled: 'disabled'
  },
  panelList: [],
  viewOptions: [] as ViewItem[],
  viewMap: {},
  customFilter: {
    sourceFilter: [],
    defaultFilter: []
  }
})
const panelWatermark = ref(false)
const reportFilterChange = reportFilter => {
  formState.value['reportFilter'] = reportFilter
}

const iconMap = {
  'area-stack': areaStack,
  area: area,
  'bar-group-stack': barGroupStack,
  'bar-group': barGroup,
  'bar-horizontal': barHorizontal,
  'bar-range': barRange,
  'bar-stack-horizontal': barStackHorizontal,
  'bar-stack': barStack,
  bar: bar,
  'bidirectional-bar': bidirectionalBar,
  'bubble-map': bubbleMap,
  'chart-mix-group': chartMixGroup,
  'chart-mix-stack': chartMixStack,
  'chart-mix-dual-line': chartMixDualLine,
  'chart-mix': chartMix,
  'flow-map': flowMap,
  funnel: funnel,
  gauge: gauge,
  'heat-map': heatMap,
  indicator: indicator,
  line: line,
  liquid: liquid,
  map: map,
  'percentage-bar-stack-horizontal': percentageBarStackHorizontal,
  'percentage-bar-stack': percentageBarStack,
  'pie-donut-rose': pieDonutRose,
  'pie-donut': pieDonut,
  'pie-rose': pieRose,
  pie: pie,
  'progress-bar': progressBar,
  quadrant: quadrant,
  radar: radar,
  'rich-text': richText,
  sankey: sankey,
  scatter: scatter,
  'stock-line': stockLine,
  'symbolic-map': symbolicMap,
  'table-info': tableInfo,
  'table-normal': tableNormal,
  'table-pivot': tablePivot,
  treemap: treemap,
  waterfall: waterfall,
  'word-cloud': wordCloud,
  't-heatmap': tHeatmap,
  'bullet-graph': bulletGraph,
  'circle-packing': circlePacking
}

const defaultFormData = ref({
  name: null,
  title: null,
  content: '',
  rtid: 0,
  rid: null,
  format: 0,
  viewIdList: [] as string[],
  viewDataRange: 1,
  pixel: '1920 * 1080',
  showWatermark: false,
  dataPermission: 0,
  extWaitTime: 0,
  renderTime: 2
})
const formState = ref({
  ...defaultFormData.value
})

// method area
const filterEmptyFolderTree = nodes => {
  return nodes.filter(node => {
    if (node.leaf) {
      // return true;
      return !!node.extraFlag1
    } else if (node.children && node.children.length > 0) {
      node.children = filterEmptyFolderTree(node.children)
      return true
    } else {
      return false
    }
  })
}
const setHeight = () => {
  setTimeout(() => {
    const input = viewSelect.value.$el.querySelector('input') as HTMLInputElement
    const _tags = viewTags.value
    nextTick(() => {
      input.style.minHeight = `${_tags.clientHeight + 6}px`
    })
  }, 0)
}
const taskNameChange = val => {
  if (!customTitle.value) {
    formState.value.title = val
  }
}
const titleChange = () => {
  customTitle.value = true
}
const rtidChange = () => {
  formState.value.rid = null
  loadRTree()
}
const dataPermissionChange = () => {
  console.log('dataPermissionChange', formState.value.dataPermission)
}
const loadRTree = () => {
  const busiFlag = formState?.value?.rtid ? 'dataV' : 'dashboard'
  const request = { busiFlag, resourceTable: 'core', weight: 7 }
  return queryTreeApi(request).then(rsp => {
    if (rsp && rsp[0]?.id === '0') {
      state.panelList = rsp[0].children
    } else {
      state.panelList = rsp
    }
    state.panelList = filterEmptyFolderTree(state.panelList)
    if (!validateRid()) {
      ElMessage.error(`当前${busiFlag}[${formState.value.rid}]失效，已自动清空！`)
      formState.value.rid = null
    }
    loadViewOption()
  })
}
const validateRid = () => {
  if (formState.value.rid) {
    const stack = [...state.panelList]
    while (stack.length) {
      const item = stack.pop()
      if (item?.id === formState.value.rid) {
        return true
      }
      if (item.children?.length) {
        item.children.forEach(kid => stack.push(kid))
      }
    }
    return false
  }
  return true
}
const validateViewIdList = () => {
  let result = true
  if (formState.value.viewIdList?.length) {
    let len = formState.value.viewIdList.length
    while (len--) {
      const item = formState.value.viewIdList[len]
      if (!state.viewMap[item]) {
        formState.value.viewIdList.splice(len, 1)
        result = false
      }
    }
    setHeight()
    return result
  }
  setHeight()
  return result
}
const setDefaultConditionOpen = async () => {
  if (formState.value.rid) {
    conditionDefaultSetRef.value?.open()
  }
}
const loadCanvasInfo = async resourceId => {
  panelWatermark.value = false
  if (resourceId) {
    const busiType = formState.value.rtid === 0 ? 'dashboard' : 'dataV'
    await findById(resourceId, busiType, {
      source: 'report',
      taskId: props.baseFormData.taskId
    })
      .then(rsp => {
        canvasInfo.value = rsp.data
        const watermarkInfo = rsp.data.watermarkInfo
        if (watermarkInfo?.settingContent) {
          const info = JSON.parse(watermarkInfo.settingContent)
          panelWatermark.value =
            info.enable && (!info.enablePanelCustom || rsp.data.selfWatermarkStatus)
        }
        state.customFilter = defaultConditionTrans(canvasInfo.value)
      })
      .catch(() => {
        // 仪表板已失效（如被删除）：loadRTree 的 validateRid 已提示并清空 rid，此处静默兜底
      })
  }
}

const loadViewOption = (rid?: string) => {
  const resourceId = rid || formState.value.rid
  state.viewMap = {}
  if (!resourceId) {
    formState.value.viewIdList = []
    setHeight()
    return
  }
  viewOptionApi(resourceId).then(res => {
    state.viewOptions = res.data
    res.data.forEach(item => {
      state.viewMap[item.id] = item
    })
    validateViewIdList()
  })
}
const dvNodeClick = data => {
  if (data.leaf && data.id !== formState.value.rid) {
    loadViewOption(data.id)
    loadCanvasInfo(data.id)
  }
}

const getFormData = async () => {
  hideMceDialog()
  const p = new Promise((r, e) => {
    reportBaseForm?.value?.validate(valid => {
      r(valid && formState.value)
    })
  })
  return await p
}
const hideMceDialog = () => {
  const pdom = document.getElementsByClassName('tox-tinymce-aux')
  if (!pdom?.length || !pdom[0].childNodes?.length) {
    return
  }
  const id = pdom[0].childNodes[0]['getAttribute']('id')
  if (id) {
    const btn = document.querySelector(`[aria-controls="${id}"]`)
    if (btn) {
      btn['click']()
    }
  }
}

const formatBase2Form = () => {
  if (!props.isEdit || !props.baseFormData) {
    return
  }
  for (const key in formState.value) {
    if (props.baseFormData[key] !== undefined) {
      formState.value[key] = props.baseFormData[key]
    }
  }
  customTitle.value = formState.value['name'] === formState.value['title']
}
const removeViewTag = (val: string) => {
  const index = formState.value.viewIdList.indexOf(val)
  if (index > -1) {
    formState.value.viewIdList.splice(index, 1)
    setHeight()
  }
}

defineExpose({
  getFormData
})
onMounted(async () => {
  formatBase2Form()
  // 先加载树并校验 rid（失效 rid 会被清空），避免对已删除仪表板发起 findById
  await loadRTree()
  loadCanvasInfo(formState.value.rid)
})
</script>
<style lang="less">
.tox-editor-container {
  z-index: 2000;
}
</style>
<style scoped lang="less">
.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}
.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}
.report-form-container {
  height: 100%;
  margin: 0 auto;
  width: 600px;
  .report-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin-top: 24px;
    margin-bottom: 16px;
    .title-flag {
      height: 16px;
      line-height: 16px;
      border-left: 2px solid var(--ed-color-primary, #3370ff);
    }
    .form-title {
      color: #1f2329;
      font-weight: 500;
      font-family: var(--de-custom_font, 'PingFang');
      line-height: 24px;
      font-size: 16px;
      padding-left: 8px;
    }
  }

  .report-form {
    width: 600px;
    padding-bottom: 16px;
    .ed-form-item {
      margin-bottom: 16px;
    }
    .is-error {
      margin-bottom: 40px !important;
    }

    .ed-form-item__label {
      .basic-form-info-tips {
        width: fit-content;
        display: inline-flex;
        align-items: center;
        column-gap: 4px;
      }
    }
  }
  :deep(.ed-form-item__label) {
    line-height: 22px;
    height: 22px;
  }
  :deep(.report-view-dialog) {
    height: calc(100% - 120px);
    .ed-dialog__header {
      display: none !important;
    }
    .ed-dialog__body {
      padding: 0 0;
      height: 100% !important;
      .report-view-container {
        height: 100% !important;
        position: relative;
        .report-view-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          height: 56px;
          padding: 16px 24px;
          span {
            height: 24px;
            line-height: 24px;
            font-family: var(--de-custom_font, 'PingFang');
            font-size: 16px;
            font-weight: 500;
            line-height: 24px;
            text-align: left;
            color: #1f2329;
          }
          i {
            width: 20px;
            height: 20px;
            color: #646a73;
            padding: 3px;
            cursor: pointer;
          }
        }
        .report-view-main {
          height: calc(100% - 120px);
          background-color: #eff0f1;
          padding: 16px 24px 0px;
        }
        .report-view-footer {
          display: flex;
          align-items: center;
          justify-content: space-between;
          height: 64px;
          border-radius: 0px, 0px, 4px, 4px;
          padding: 16px 24px;
          .report-view-tips {
            height: 22px;
            line-height: 22px;
            font-size: 14px;
            font-weight: 400;
            color: #646a73;
          }
        }
      }
    }
  }
}

.dv-selector {
  width: 100%;
}

.label-content-details {
  width: 100%;
  display: flex;
  align-items: center;
}
.ed-select__tags {
  .ed-select-tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }
  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}
.report-content-tinymce {
  height: fit-content;
  max-height: 300px;
  overflow-y: scroll;
  padding: 0;
  :deep(.tox-statusbar) {
    display: none;
  }
}
</style>
