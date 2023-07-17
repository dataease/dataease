<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import { computed, onBeforeMount, onMounted, reactive, ref, toRefs, watch } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA } from '@/views/chart/components/js/util.js'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/components/editor/util/chart'
import DrillPath from '@/views/chart/components/views/components/DrillPath.vue'
import { ElMessage } from 'element-plus-secondary'
import { nextTick } from 'vue'
import { checkIsBatchOptView } from '@/utils/canvasUtils'
import { useFilter } from '@/hooks/web/useFilter'
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache()
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { cloneDeep } from 'lodash-es'

const g2 = ref<any>()

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()

const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  view: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  showPosition: {
    type: String,
    required: false,
    default: 'canvas'
  }
})

const { view, showPosition, element } = toRefs(props)

const state = reactive({
  title_show: true,
  title_class: {
    margin: '0 0',
    width: '100%',
    fontSize: '18px',
    color: '#303133',
    textAlign: 'left',
    fontStyle: 'normal',
    fontWeight: 'normal',
    background: ''
  },
  drillFilters: [],
  drillClickDimensionList: []
})

watch(
  [() => view.value],
  () => {
    initTitle()
  },
  { deep: true }
)

const initTitle = () => {
  if (view.value.customStyle) {
    const customStyle = view.value.customStyle
    if (customStyle.text) {
      state.title_show = customStyle.text.show
      state.title_class.fontSize = customStyle.text.fontSize + 'px'
      state.title_class.color = customStyle.text.color
      state.title_class.textAlign = customStyle.text.hPosition
      state.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
      state.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

      state.title_class.fontFamily = customStyle.text.fontFamily
        ? customStyle.text.fontFamily
        : DEFAULT_TITLE_STYLE.fontFamily
      state.title_class.letterSpacing =
        (customStyle.text.letterSpace
          ? customStyle.text.letterSpace
          : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
      state.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
    }
    if (customStyle.background) {
      state.title_class.background = hexColorToRGBA(
        customStyle.background.color,
        customStyle.background.alpha
      )
    }
  }
}

const drillJump = index => {
  // const length = state.drillClickDimensionList.length
  state.drillClickDimensionList = state.drillClickDimensionList.slice(0, index)
  // if (props.view.type === 'map' || props.view.type === 'buddle-map') {
  //   this.backToParent(index, length)
  // }
  view.value.chartExtRequest = filter()
  g2?.value?.calcData(view.value)
}

const chartClick = param => {
  if (state.drillClickDimensionList.length < props.view.drillFields.length - 1) {
    state.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
    view.value.chartExtRequest = filter()
    g2?.value?.calcData(view.value)
    // this.getData(this.element.propValue.viewId)
  } else if (props.view.drillFields.length > 0) {
    ElMessage.error(t('chart.last_layer'))
  }
}

// 仪表板和大屏所有额外过滤参数都在此处
const filter = (firstLoad: boolean) => {
  const { filter } = useFilter(view.value.id, firstLoad)
  return {
    user: wsCache.get('user.uid'),
    filter,
    linkageFilters: element.value.linkageFilters,
    // outerParamsFilters: this.element.outerParamsFilters,
    drill: state.drillClickDimensionList
    // resultCount: this.resultCount,
    // resultMode: this.resultMode,
    // queryFrom: 'panel'
  }
}

const onDrillFilters = param => {
  state.drillFilters = param ? param : []
  nextTick(() => {
    g2?.value?.renderChart(view.value)
  })
}

const queryData = (firstLoad = false) => {
  const queryFilter = filter(firstLoad)
  let params = cloneDeep(view.value)
  params['chartExtRequest'] = queryFilter
  g2?.value?.calcData(params)
}

onBeforeMount(() => {
  useEmitt({
    name: `query-data-${view.value.id}`,
    callback: queryData
  })
})

onMounted(() => {
  queryData(true)
  useEmitt({
    name: 'snapshotChangeToView',
    callback: function (cacheViewInfo) {
      initTitle()
      nextTick(() => {
        if (
          cacheViewInfo.snapshotCacheViewCalc.includes(view.value.id) ||
          cacheViewInfo.snapshotCacheViewCalc.includes('all')
        ) {
          view.value.chartExtRequest = filter(false)
          g2?.value?.calcData(view.value)
        } else if (
          cacheViewInfo.snapshotCacheViewRender.includes(view.value.id) ||
          cacheViewInfo.snapshotCacheViewRender.includes('all')
        ) {
          g2?.value?.renderChart(view.value)
        }
      })
    }
  })
  useEmitt({
    name: 'calcData-' + view.value.id,
    callback: function (val) {
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        g2?.value?.calcData(val)
      })
    }
  })
  useEmitt({
    name: 'renderChart-' + view.value.id,
    callback: function (val) {
      initTitle()
      nextTick(() => {
        g2?.value?.renderChart(val)
      })
    }
  })
  useEmitt({
    name: 'resetDrill-' + view.value.id,
    callback: function (val) {
      drillJump(val)
    }
  })
})

initTitle()
</script>

<template>
  <div class="chart-area">
    <p v-if="state.title_show" :style="state.title_class">{{ view.title }}</p>
    <!--这里去渲染不同图库的视图-->
    <chart-component-g2-plot
      style="flex: 1"
      :view="view"
      :show-position="showPosition"
      v-if="view?.render === 'antv'"
      ref="g2"
      @onChartClick="chartClick"
      @onDrillFilters="onDrillFilters"
    />
    <drill-path :drill-filters="state.drillFilters" @onDrillJump="drillJump" />
  </div>
</template>

<style lang="less" scoped>
.chart-area {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
