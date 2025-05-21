<script lang="ts" setup>
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import FunctionCfg from '@/views/chart/components/editor/editor-senior/components/FunctionCfg.vue'
import ScrollCfg from '@/views/chart/components/editor/editor-senior/components/ScrollCfg.vue'
import AssistLine from '@/views/chart/components/editor/editor-senior/components/AssistLine.vue'
import Threshold from '@/views/chart/components/editor/editor-senior/components/Threshold.vue'
import MapMapping from '@/views/chart/components/editor/editor-senior/components/MapMapping.vue'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { useAppStoreWithOut } from '@/store/modules/app'
import { computed, PropType, ref, toRefs, watch } from 'vue'
import LinkJumpSet from '@/components/visualization/LinkJumpSet.vue'
import LinkageSet from '@/components/visualization/LinkageSet.vue'
import { canvasSave } from '@/utils/canvasUtils'
import {
  queryVisualizationJumpInfo,
  removeJumpSet,
  updateJumpSetActive
} from '@/api/visualization/linkJump'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import {
  getPanelAllLinkageInfo,
  removeLinkage,
  updateLinkageActive
} from '@/api/visualization/linkage'
import { includesAny } from '../util/StringUtils'
import { ElCollapseItem, ElIcon, ElMessage } from 'element-plus-secondary'
import { storeToRefs } from 'pinia'
import { BASE_VIEW_CONFIG } from '../util/chart'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import BubbleAnimateCfg from '@/views/chart/components/editor/editor-senior/components/BubbleAnimateCfg.vue'
import { XpackComponent } from '@/components/plugin'
import CarouselSetting from '@/custom-component/common/CarouselSetting.vue'
import { Icon } from 'vant'
import CommonEvent from '@/custom-component/common/CommonEvent.vue'
const dvMainStore = dvMainStoreWithOut()

const { nowPanelTrackInfo, nowPanelJumpInfo, dvInfo, curComponent, batchOptStatus } =
  storeToRefs(dvMainStore)

const { t } = useI18n()
const linkJumpRef = ref(null)
const linkageRef = ref(null)

const state = {
  attrActiveNames: [],
  styleActiveNames: []
}

const emit = defineEmits([
  'onFunctionCfgChange',
  'onAssistLineChange',
  'onScrollCfgChange',
  'onThresholdChange',
  'onMapMappingChange',
  'onBubbleAnimateChange'
])

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
    required: true
  },
  quotaData: {
    type: Array,
    required: true
  },
  quotaExtData: {
    type: Array,
    required: true
  },
  fieldsData: {
    type: Array,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  properties: {
    type: Array as PropType<EditorProperty[]>,
    required: false,
    default: () => {
      return []
    }
  },
  propertyInnerAll: {
    type: Object as PropType<EditorPropertyInner>,
    required: false,
    default: () => {
      return {}
    }
  },
  eventInfo: {
    type: Object,
    required: false
  }
})

const { chart, themes, properties, propertyInnerAll } = toRefs(props)
watch(
  () => chart.value?.senior,
  () => {
    chart.value.senior = defaultsDeep(chart.value?.senior || {}, cloneDeep(BASE_VIEW_CONFIG.senior))
  }
)
const seniorCounts = computed(() => {
  let linkageCount = 0
  let jumpCount = 0
  props.fieldsData?.forEach(item => {
    const sourceInfo = props.chart.id + '#' + item.id
    if (nowPanelTrackInfo.value[sourceInfo]) {
      linkageCount++
    }
    if (nowPanelJumpInfo.value[sourceInfo]) {
      jumpCount++
    }
  })

  return {
    linkageCount,
    jumpCount
  }
})

const eventsShow = computed(() => {
  return (
    !batchOptStatus.value &&
    ['indicator', 'rich-text'].includes(chart.value.type) &&
    props.eventInfo
  )
})

const onFunctionCfgChange = val => {
  emit('onFunctionCfgChange', val)
}

const onAssistLineChange = val => {
  emit('onAssistLineChange', val)
}

const onScrollCfgChange = val => {
  emit('onScrollCfgChange', val)
}

const onThresholdChange = val => {
  emit('onThresholdChange', val)
}

const onMapMappingChange = val => {
  emit('onMapMappingChange', val)
}

const onBubbleAnimateChange = val => {
  emit('onBubbleAnimateChange', val)
}

const showProperties = (prop: EditorProperty) => {
  return properties?.value?.includes(prop)
}

const linkJumpSetOpen = () => {
  if (!dvInfo.value.id) {
    ElMessage.warning(t('visualization.save_page_tips'))
    return
  }
  //跳转设置需要先触发保存
  canvasSave(() => {
    linkJumpRef.value.dialogInit({ id: chart.value.id })
  })
}
const linkageSetOpen = () => {
  if (!dvInfo.value.id) {
    ElMessage.warning(t('visualization.save_page_tips'))
    return
  }
  //跳转设置需要先触发保存
  canvasSave(() => {
    linkageRef.value.dialogInit({ id: chart.value.id })
  })
}

const SENIOR_PROP: EditorProperty[] = [
  'map-mapping',
  'function-cfg',
  'assist-line',
  'scroll-cfg',
  'threshold',
  'jump-set',
  'linkage',
  'bubble-animate'
]
const excludeTypeList = ['chart-mix', 'chart-mix-stack', 'chart-mix-group']
const noSenior = computed(() => {
  return (
    !includesAny(properties.value, ...SENIOR_PROP) && excludeTypeList.includes(chart.value.type)
  )
})

const linkJumpActiveChange = () => {
  // 直接触发刷新
  const params = {
    sourceDvId: dvInfo.value.id,
    sourceViewId: chart.value.id,
    activeStatus: chart.value.jumpActive
  }
  updateJumpSetActive(params).then(rsp => {
    dvMainStore.setNowPanelJumpInfo(rsp.data)
  })
}
const linkageActiveChange = () => {
  const params = {
    dvId: dvInfo.value.id,
    sourceViewId: chart.value.id,
    activeStatus: chart.value.linkageActive
  }
  updateLinkageActive(params).then(rsp => {
    dvMainStore.setNowPanelTrackInfo(rsp.data)
  })
}
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

const removeLinkageSenior = () => {
  removeLinkage({ dvId: dvInfo.value.id, sourceViewId: chart.value.id }).then(() => {
    // 刷新联动信息
    getPanelAllLinkageInfo(dvInfo.value.id).then(rsp => {
      dvMainStore.setNowPanelTrackInfo(rsp.data)
    })
  })
}

const removeJumpSenior = () => {
  removeJumpSet({ sourceDvId: dvInfo.value.id, sourceViewId: chart.value.id }).then(() => {
    // 刷新跳转信息
    queryVisualizationJumpInfo(dvInfo.value.id).then(rsp => {
      dvMainStore.setNowPanelJumpInfo(rsp.data)
    })
  })
}
</script>

<template>
  <el-row class="view-panel" :class="'senior-' + themes">
    <div @keydown.stop @keyup.stop class="attr-style" v-if="!noSenior">
      <el-row class="de-collapse-style">
        <el-collapse v-model="state.attrActiveNames" class="style-collapse">
          <el-collapse-item
            :effect="themes"
            v-if="showProperties('function-cfg')"
            name="function"
            :title="t('chart.function_cfg')"
            @modelChange="onFunctionCfgChange"
          >
            <function-cfg
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['function-cfg']"
              @onFunctionCfgChange="onFunctionCfgChange"
            />
          </el-collapse-item>

          <el-collapse-item
            :effect="themes"
            v-if="showProperties('map-mapping')"
            name="mapMapping"
            :title="t('chart.place_name_mapping')"
            @modelChange="onFunctionCfgChange"
          >
            <map-mapping
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['function-cfg']"
              @onMapMappingChange="onMapMappingChange"
            />
          </el-collapse-item>

          <collapse-switch-item
            :effect="themes"
            :title="t('chart.assist_line')"
            :change-model="chart.senior.assistLineCfg"
            v-if="showProperties('assist-line')"
            v-model="chart.senior.assistLineCfg.enable"
            name="analyse"
            @modelChange="val => onAssistLineChange({ data: val })"
          >
            <assist-line
              :chart="props.chart"
              :themes="themes"
              :quota-data="props.quotaData"
              :quota-ext-data="props.quotaExtData"
              :property-inner="propertyInnerAll['assist-line']"
              @onAssistLineChange="onAssistLineChange"
            />
          </collapse-switch-item>

          <collapse-switch-item
            :effect="themes"
            :title="t('chart.scroll_cfg')"
            :change-model="chart.senior.scrollCfg"
            v-if="showProperties('scroll-cfg')"
            v-model="chart.senior.scrollCfg.open"
            name="scroll"
            @modelChange="onScrollCfgChange"
          >
            <scroll-cfg
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['scroll-cfg']"
              @onScrollCfgChange="onScrollCfgChange"
            />
          </collapse-switch-item>

          <collapse-switch-item
            :effect="themes"
            :title="t('chart.threshold')"
            :change-model="chart.senior.threshold"
            v-model="chart.senior.threshold.enable"
            v-if="showProperties('threshold')"
            name="threshold"
            @modelChange="onThresholdChange"
          >
            <threshold
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['threshold']"
              @onThresholdChange="onThresholdChange"
            />
          </collapse-switch-item>

          <xpack-component
            v-if="chart.id"
            :chart="chart"
            :themes="themes"
            :is-screen="dvInfo.type === 'dataV'"
            :resource-table="'snapshot'"
            jsname="L2NvbXBvbmVudC90aHJlc2hvbGQtd2FybmluZy9TZW5pb3JIYW5kbGVy"
          />

          <collapse-switch-item
            v-if="showProperties('linkage')"
            :themes="themes"
            name="linkage"
            :title="t('visualization.linkage_setting')"
            v-model="chart.linkageActive"
            @modelChange="linkageActiveChange"
          >
            <div class="inner-container">
              <span class="label" :class="'label-' + props.themes">{{
                t('visualization.linkage_setting')
              }}</span>
              <span class="right-btns">
                <template v-if="seniorCounts.linkageCount > 0">
                  <span class="set-text-info" :class="{ 'set-text-info-dark': themes === 'dark' }">
                    {{ t('visualization.already_setting') }}
                  </span>
                  <button
                    :class="'label-' + props.themes"
                    class="circle-button_icon"
                    :title="t('chart.delete')"
                    :style="{ margin: '0 8px' }"
                    @click="removeLinkageSenior"
                  >
                    <el-icon>
                      <Icon
                        ><icon_deleteTrash_outlined
                          :class="chart.linkageActive && 'primary-color'"
                          class="svg-icon"
                      /></Icon>
                    </el-icon>
                  </button>
                </template>
                <button
                  :class="'label-' + props.themes"
                  class="circle-button_icon"
                  :title="t('chart.edit')"
                  @click="linkageSetOpen"
                  :disabled="!chart.linkageActive"
                >
                  <el-icon>
                    <Icon
                      ><icon_edit_outlined
                        :class="chart.linkageActive && 'primary-color'"
                        class="svg-icon"
                    /></Icon>
                  </el-icon>
                </button>
              </span>
            </div>
          </collapse-switch-item>
          <collapse-switch-item
            v-if="showProperties('jump-set') && !isDataEaseBi"
            :themes="themes"
            name="jumpSet"
            :title="t('visualization.jump_set')"
            v-model="chart.jumpActive"
            @modelChange="linkJumpActiveChange"
          >
            <div class="inner-container">
              <span class="label" :class="'label-' + props.themes">{{
                t('visualization.jump_set')
              }}</span>
              <span class="right-btns">
                <template v-if="seniorCounts.jumpCount">
                  <span class="set-text-info" :class="{ 'set-text-info-dark': themes === 'dark' }">
                    {{ t('visualization.already_setting') }}
                  </span>
                  <button
                    :class="'label-' + props.themes"
                    class="circle-button_icon"
                    :title="t('chart.delete')"
                    :style="{ margin: '0 8px' }"
                    @click="removeJumpSenior"
                  >
                    <el-icon>
                      <Icon
                        ><icon_deleteTrash_outlined
                          :class="chart.jumpActive && 'primary-color'"
                          class="svg-icon"
                      /></Icon>
                    </el-icon>
                  </button>
                </template>
                <button
                  :class="'label-' + props.themes"
                  class="circle-button_icon"
                  :title="t('chart.edit')"
                  @click="linkJumpSetOpen"
                  :disabled="!chart.jumpActive"
                >
                  <el-icon>
                    <Icon
                      ><icon_edit_outlined
                        :class="chart.jumpActive && 'primary-color'"
                        class="svg-icon"
                    /></Icon>
                  </el-icon>
                </button>
              </span>
            </div>
          </collapse-switch-item>
          <collapse-switch-item
            :effect="themes"
            :title="t('visualization.bubble_dynamic_effect')"
            :change-model="chart.senior.bubbleCfg"
            v-if="showProperties('bubble-animate')"
            v-model="chart.senior.bubbleCfg.enable"
            name="bubbleAnimate"
            @modelChange="onBubbleAnimateChange"
          >
            <bubble-animate-cfg
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['bubble-animate']"
              @onBubbleAnimateChange="onBubbleAnimateChange"
            />
          </collapse-switch-item>
          <carousel-setting
            v-if="curComponent?.innerType === 'picture-group'"
            :element="curComponent"
            :themes="themes"
          ></carousel-setting>
          <el-collapse-item
            :effect="themes"
            name="events"
            :title="t('visualization.event')"
            v-if="eventsShow"
          >
            <common-event :themes="themes" :events-info="eventInfo"></common-event>
          </el-collapse-item>
        </el-collapse>
      </el-row>
    </div>
    <div v-if="noSenior" class="no-senior">
      {{ t('chart.chart_no_senior') }}
    </div>
    <!--跳转设置-->
    <link-jump-set ref="linkJumpRef" />
    <!--联动设置-->
    <linkage-set ref="linkageRef" />
  </el-row>
</template>

<style lang="less" scoped>
.ed-row {
  display: block;
}

span {
  font-size: 12px;
}

.view-panel {
  display: flex;
  height: 100%;
  width: 100%;
  .attr-style {
    width: 100%;
  }
}

.prop {
  border-bottom: 1px solid @side-outline-border-color;
}
.prop-top {
  border-top: 1px solid @side-outline-border-color;
}
.no-senior {
  width: 100%;
  text-align: center;
  font-size: 12px;
  padding-top: 40px;
  overflow: auto;
  height: 100%;

  color: #646a73;
}

.de-collapse-style {
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 8px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
  :deep(.style-collapse) {
    border-bottom: none;
  }
}

.label-dark {
  font-family: var(--de-custom_font, 'PingFang');
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
  color: #a6a6a6;
}

.inner-container {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-direction: row;
  justify-content: space-between;

  .label {
    cursor: default;
    color: #646a73;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    line-height: 20px;
  }

  .right-btns {
    display: flex;
    align-items: center;
    flex-direction: row;
  }

  .set-text-info {
    cursor: default;
    padding: 1.5px 4px;
    border-radius: 2px;
    background: rgba(31, 35, 41, 0.1);

    color: #646a73;

    font-size: 10px;
    font-style: normal;
    font-weight: 500;
    line-height: 13px;

    &.set-text-info-dark {
      color: #a6a6a6;
      background: rgba(235, 235, 235, 0.1);
    }
  }
}
</style>

<style>
.senior-dark {
  .label-dark {
    color: #a6a6a6 !important;
  }
}
</style>
