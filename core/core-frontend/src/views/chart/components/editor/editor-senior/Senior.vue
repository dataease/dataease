<script lang="ts" setup>
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
import { updateJumpSetActive } from '@/api/visualization/linkJump'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { updateLinkageActive } from '@/api/visualization/linkage'
import { includesAny } from '../util/StringUtils'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import { storeToRefs } from 'pinia'
import { BASE_VIEW_CONFIG } from '../util/chart'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import BubbleAnimateCfg from '@/views/chart/components/editor/editor-senior/components/BubbleAnimateCfg.vue'
const dvMainStore = dvMainStoreWithOut()

const { nowPanelTrackInfo, nowPanelJumpInfo, dvInfo, componentData } = storeToRefs(dvMainStore)

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
  console.log(val)
  emit('onBubbleAnimateChange', val)
}

const showProperties = (prop: EditorProperty) => {
  return properties?.value?.includes(prop)
}

const linkJumpSetOpen = () => {
  if (!dvInfo.value.id) {
    ElMessage.warning('请先保存当前页面')
    return
  }
  //跳转设置需要先触发保存
  canvasSave(() => {
    linkJumpRef.value.dialogInit({ id: chart.value.id })
  })
}
const linkageSetOpen = () => {
  if (!dvInfo.value.id) {
    ElMessage.warning('请先保存当前页面')
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
const noSenior = computed(() => {
  return !includesAny(properties.value, ...SENIOR_PROP)
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

          <collapse-switch-item
            v-if="showProperties('linkage')"
            :themes="themes"
            name="linkage"
            :title="'联动设置'"
            v-model="chart.linkageActive"
            @modelChange="linkageActiveChange"
          >
            <div class="inner-container">
              <span class="label" :class="'label-' + props.themes">联动设置</span>
              <span class="right-btns">
                <template v-if="seniorCounts.linkageCount > 0">
                  <span class="set-text-info" :class="{ 'set-text-info-dark': themes === 'dark' }">
                    已设置
                  </span>
                </template>
                <el-button
                  class="circle-button font14"
                  :title="t('chart.edit')"
                  :class="'label-' + props.themes"
                  text
                  size="small"
                  :style="{ width: '24px', marginLeft: '6px' }"
                  @click="linkageSetOpen"
                  :disabled="!chart.linkageActive"
                >
                  <template #icon>
                    <el-icon size="14px">
                      <Icon name="icon_edit_outlined" />
                    </el-icon>
                  </template>
                </el-button>
              </span>
            </div>
          </collapse-switch-item>
          <collapse-switch-item
            v-if="showProperties('jump-set') && !isDataEaseBi"
            :themes="themes"
            name="jumpSet"
            :title="'跳转设置'"
            v-model="chart.jumpActive"
            @modelChange="linkJumpActiveChange"
          >
            <div class="inner-container">
              <span class="label" :class="'label-' + props.themes">跳转设置</span>
              <span class="right-btns">
                <template v-if="seniorCounts.jumpCount">
                  <span class="set-text-info" :class="{ 'set-text-info-dark': themes === 'dark' }">
                    已设置
                  </span>
                  <!--                  <el-button
                    class="circle-button font14"
                    :title="t('chart.delete')"
                    :class="'label-' + props.themes"
                    text
                    size="small"
                    :style="{ width: '24px', marginLeft: '6px' }"
                    @click="linkJumpSetOpen"
                  >
                    <template #icon>
                      <el-icon size="14px">
                        <Icon name="icon_delete-trash_outlined" />
                      </el-icon>
                    </template>
                  </el-button>-->
                </template>
                <el-button
                  class="circle-button font14"
                  :title="t('chart.edit')"
                  :class="'label-' + props.themes"
                  text
                  size="small"
                  :style="{ width: '24px', marginLeft: '6px' }"
                  @click="linkJumpSetOpen"
                  :disabled="!chart.jumpActive"
                >
                  <template #icon>
                    <el-icon size="14px">
                      <Icon name="icon_edit_outlined" />
                    </el-icon>
                  </template>
                </el-button>
              </span>
            </div>
          </collapse-switch-item>
          <collapse-switch-item
            :effect="themes"
            title="气泡动效"
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
}

.label-dark {
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-style: normal;
  font-weight: 400;
  line-height: 20px;
  color: #a6a6a6 !important;
  &.ed-button {
    color: var(--ed-color-primary) !important;
  }
  &.is-disabled {
    color: #5f5f5f !important;
  }
}

.font14 {
  :deep(.ed-icon) {
    font-size: 14px;
  }
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
