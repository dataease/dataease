<script lang="ts" setup>
import { useI18n } from '@/hooks/web/useI18n'
import FunctionCfg from '@/views/chart/components/editor/editor-senior/components/FunctionCfg.vue'
import ScrollCfg from '@/views/chart/components/editor/editor-senior/components/ScrollCfg.vue'
import AssistLine from '@/views/chart/components/editor/editor-senior/components/AssistLine.vue'
import Threshold from '@/views/chart/components/editor/editor-senior/components/Threshold.vue'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { computed, PropType, ref, toRefs } from 'vue'
import LinkJumpSet from '@/components/visualization/LinkJumpSet.vue'
import LinkageSet from '@/components/visualization/LinkageSet.vue'
import { canvasSave } from '@/utils/canvasUtils'
import { updateJumpSetActive } from '@/api/visualization/linkJump'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { updateLinkageActive } from '@/api/visualization/linkage'
import { includesAny } from '../util/StringUtils'
import { ElMessage } from 'element-plus-secondary'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

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
  'onThresholdChange'
])

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  quotaData: {
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
  'linkage'
]
const noSenior = computed(() => {
  return !includesAny(properties.value, ...SENIOR_PROP)
})

const linkJumpActiveChange = () => {
  // 直接触发刷新
  const params = {
    sourceDvId: chart.value.sceneId,
    sourceViewId: chart.value.id,
    activeStatus: chart.value.jumpActive
  }
  updateJumpSetActive(params).then(rsp => {
    dvMainStore.setNowPanelJumpInfo(rsp.data)
  })
}
const linkageActiveChange = () => {
  const params = {
    dvId: chart.value.sceneId,
    sourceViewId: chart.value.id,
    activeStatus: chart.value.linkageActive
  }
  updateLinkageActive(params).then(rsp => {
    dvMainStore.setNowPanelTrackInfo(rsp.data)
  })
}
</script>

<template>
  <el-row class="view-panel" :class="'senior-' + themes">
    <div @keydown.stop @keyup.stop class="attr-style">
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
            v-if="showProperties('assist-line')"
            name="analyse"
            :title="t('chart.assist_line')"
          >
            <assist-line
              :chart="props.chart"
              :themes="themes"
              :quota-data="props.quotaData"
              :property-inner="propertyInnerAll['assist-line']"
              @onAssistLineChange="onAssistLineChange"
            />
          </el-collapse-item>

          <el-collapse-item
            :effect="themes"
            v-if="showProperties('scroll-cfg')"
            name="scroll"
            :title="t('chart.scroll_cfg')"
          >
            <scroll-cfg
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['scroll-cfg']"
              @onScrollCfgChange="onScrollCfgChange"
            />
          </el-collapse-item>

          <el-collapse-item
            :effect="themes"
            v-if="showProperties('threshold')"
            name="threshold"
            :title="t('chart.threshold')"
          >
            <threshold
              :themes="themes"
              :chart="props.chart"
              :property-inner="propertyInnerAll['threshold']"
              @onThresholdChange="onThresholdChange"
            />
          </el-collapse-item>

          <collapse-switch-item
            v-if="showProperties('linkage')"
            :themes="themes"
            name="linkage"
            :title="'联动设置'"
            v-model="chart.linkageActive"
            @modelChange="linkageActiveChange"
          >
            <span>联动设置</span>
            <el-button
              class="circle-button"
              :title="t('chart.edit')"
              type="text"
              size="small"
              :style="{ width: '24px', marginLeft: '4px', float: 'right' }"
              @click="linkageSetOpen"
              :disabled="!chart.linkageActive"
            >
              <template #icon>
                <Icon name="icon_edit_outlined"></Icon>
              </template>
            </el-button>
          </collapse-switch-item>
          <collapse-switch-item
            v-if="showProperties('jump-set')"
            :themes="themes"
            name="jumpSet"
            :title="'跳转设置'"
            v-model="chart.jumpActive"
            @modelChange="linkJumpActiveChange"
          >
            <span>跳转设置</span>
            <el-button
              class="circle-button"
              :title="t('chart.edit')"
              type="text"
              size="small"
              :style="{ width: '24px', marginLeft: '4px', float: 'right' }"
              @click="linkJumpSetOpen"
              :disabled="!chart.jumpActive"
            >
              <template #icon>
                <Icon name="icon_edit_outlined"></Icon>
              </template>
            </el-button>
          </collapse-switch-item>
        </el-collapse>
      </el-row>
    </div>
    <div v-show="noSenior" class="no-senior">
      {{ t('chart.chart_no_senior') }}
    </div>
    <!--跳转设置-->
    <link-jump-set ref="linkJumpRef"></link-jump-set>
    <!--联动设置-->
    <linkage-set ref="linkageRef"></linkage-set>
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

.de-collapse-style {
  :deep(.ed-collapse-item__content) {
    padding: 16px !important;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 16px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
  :deep(.ed-checkbox__inner) {
    width: 14px;
    height: 14px;
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
  border-right: 1px solid #e6e6e6;
  height: 100%;
}
:deep(.ed-collapse-item) {
  &:first-child {
    .ed-collapse-item__header {
      border-top: none;
    }
  }
}
</style>
