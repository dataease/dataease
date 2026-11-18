<template>
  <div class="pic-main">
    <chart-error v-if="isError" :err-msg="errMsg" />
    <img
      draggable="false"
      v-else-if="displayUrl"
      :style="imageAdapter"
      :src="imgUrlTrans(displayUrl)"
    />
    <template v-else>
      <chart-empty-info :view-icon="view.type"></chart-empty-info>
    </template>
  </div>
</template>

<script setup lang="ts">
import {
  CSSProperties,
  computed,
  nextTick,
  toRefs,
  reactive,
  ref,
  PropType,
  watch,
  onBeforeMount,
  onBeforeUnmount
} from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { getData } from '@/api/chart'
import { useLatestQuery } from '@/hooks/web/useQueryScope'
import { provideChartRuntimeCache } from '@/hooks/web/useChartRuntimeCache'
import { parseJson } from '@/views/chart/components/js/util'
import { mappingColorCustom } from '@/views/chart/components/js/panel/common/common_table'
import { storeToRefs } from 'pinia'
import ChartEmptyInfo from '@/views/chart/components/views/components/ChartEmptyInfo.vue'
import ChartError from '@/views/chart/components/views/components/ChartError.vue'
import { deepCopy } from '@/utils/utils'
const dvMainStore = dvMainStoreWithOut()
const { mobileInPc, fullscreenFlag } = storeToRefs(dvMainStore)
const state = reactive({
  emptyValue: '-',
  data: null,
  viewDataInfo: null,
  showUrl: null,
  firstRender: true,
  previewFirstRender: true,
  curImgList: []
})
const initReady = ref(true)
const props = defineProps({
  element: {
    type: Object,
    default() {
      return {
        propValue: { urlList: [] }
      }
    }
  },
  showPosition: {
    required: false,
    type: String,
    default: 'preview'
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  view: {
    type: Object as PropType<ChartObj>,
    default() {
      return {
        propValue: null
      }
    }
  }
})
const isError = ref(false)
const errMsg = ref('')
const dataRowSelect = ref({})
const dataRowNameSelect = ref({})
const dataRowFiledName = ref([])
const { element, view, showPosition } = toRefs(props)
let innerRefreshTimer = null
let innerSearchCount = 0
const isEditMode = computed(
  () =>
    (showPosition.value.includes('canvas') || ['edit', 'popEdit'].includes(showPosition.value)) &&
    !mobileInPc.value &&
    !fullscreenFlag.value
)

watch([() => props.searchCount], () => {
  // 内部计时器启动 忽略外部计时器
  if (!innerRefreshTimer) {
    calcData(view.value, () => {
      // do searchCount
    })
  }
})

// 编辑状态下 不启动刷新
const buildInnerRefreshTimer = (
  refreshViewEnable = false,
  refreshUnit = 'minute',
  refreshTime = 5
) => {
  if (showPosition.value === 'preview' && !innerRefreshTimer && refreshViewEnable) {
    innerRefreshTimer && clearInterval(innerRefreshTimer)
    const timerRefreshTime = refreshUnit === 'second' ? refreshTime * 1000 : refreshTime * 60000
    innerRefreshTimer = setInterval(() => {
      calcData(view.value, () => {
        // do innerRefreshTimer
      })
      innerSearchCount++
    }, timerRefreshTime)
  }
}

const images = computed<{ url: string }[]>(() => element.value.propValue.urlList || [])
const hasThreshold = computed(() => Boolean(parseJson(view.value?.senior)?.threshold?.enable))
const carouselIndex = ref(0)
const carouselActive = computed(
  () =>
    !isEditMode.value &&
    !element.value.materialPicture &&
    !hasThreshold.value &&
    element.value.carousel?.enable &&
    images.value.length > 1
)
const displayUrl = computed(() =>
  hasThreshold.value ? state.showUrl : images.value[carouselIndex.value]?.url
)

watch(
  [() => images.value.map(image => image.url), carouselActive, () => element.value.carousel?.time],
  ([urls, active, time], _previous, onCleanup) => {
    carouselIndex.value = 0
    if (!active) return
    const seconds = Math.max(1, Math.min(3600, Number(time) || 5))
    const timer = window.setInterval(() => {
      carouselIndex.value = (carouselIndex.value + 1) % urls.length
    }, seconds * 1000)
    onCleanup(() => window.clearInterval(timer))
  },
  { immediate: true }
)

const imageAdapter = computed(() => {
  const style: CSSProperties = {
    position: 'relative',
    width: '100%',
    height: '100%'
  }
  if (element.value.style?.adaptation === 'original') {
    style.width = 'auto'
    style.height = 'auto'
  } else if (element.value.style?.adaptation === 'equiratio') {
    style.objectFit = 'contain'
  }
  return style
})

const initCurFields = chartDetails => {
  dataRowFiledName.value = []
  dataRowSelect.value = {}
  dataRowNameSelect.value = {}
  if (chartDetails.data && chartDetails.data.sourceFields) {
    const checkAllAxisStr =
      JSON.stringify(chartDetails.xAxis) +
      JSON.stringify(chartDetails.xAxisExt) +
      JSON.stringify(chartDetails.yAxis) +
      JSON.stringify(chartDetails.yAxisExt)
    chartDetails.data.sourceFields.forEach(field => {
      if (checkAllAxisStr.indexOf(field.id) > -1) {
        dataRowFiledName.value.push(`[${field.name}]`)
      }
    })
    if (checkAllAxisStr.indexOf('"记录数*"') > -1) {
      dataRowFiledName.value.push(`[记录数*]`)
    }
    const sourceFieldNameIdMap = chartDetails.data.fields.reduce((pre, next) => {
      pre[next['dataeaseName']] = next['name']
      return pre
    }, {})
    const rowData = chartDetails.data.tableRow[0]
    for (const key in rowData) {
      dataRowNameSelect.value[sourceFieldNameIdMap[key]] = rowData[key]
    }
  }
  conditionAdaptor(view.value)
}

const conditionAdaptor = (chart: Chart) => {
  const images = element.value.propValue.urlList || []
  state.showUrl = element.value.materialPicture ? images[0]?.url : null
  if (!chart || !chart.senior) {
    return
  }
  const { threshold } = parseJson(chart.senior)
  if (!threshold.enable) {
    return
  }
  const conditions = threshold.tableThreshold ?? []
  if (conditions?.length > 0) {
    for (let i = 0; i < conditions.length; i++) {
      const field = deepCopy(conditions[i])
      let defaultValueColor = null
      field.conditions.sort((a, b) => {
        const aIsDefault = a.term === 'default'
        const bIsDefault = b.term === 'default'

        if (aIsDefault && !bIsDefault) return 1
        if (!aIsDefault && bIsDefault) return -1
        return 0
      })
      const checkResult = mappingColorCustom(
        dataRowNameSelect.value[field.field.name],
        defaultValueColor,
        field,
        'url'
      ).color
      if (
        checkResult &&
        (!element.value.materialPicture || images.some(image => image.url === checkResult))
      ) {
        state.showUrl = checkResult
      }
    }
  }
}

const withInit = () => {
  state.showUrl = images.value[0]?.url || null
}

const chartRuntime = provideChartRuntimeCache(
  () => props.view as Chart,
  () => props.showPosition
)
const dataQuery = useLatestQuery('chart-data')
const invalidateQuery = () => dataQuery.invalidate()

const calcData = (viewCalc: Chart, callback) => {
  const request = dataQuery.start()
  try {
    isError.value = false
    const { threshold } = parseJson(viewCalc.senior)
    if (!threshold.enable) {
      withInit()
      callback?.()
      request.finish()
      return
    }
    if (viewCalc.tableId || viewCalc['dataFrom'] === 'template') {
      const v = JSON.parse(JSON.stringify(viewCalc))
      v.type = 'table-info'
      v.render = 'antv'
      v.resultCount = 1
      return getData(v, request)
        .then(res => {
          if (!request.isCurrent()) return
          if (res.code && res.code !== 0) {
            isError.value = true
            errMsg.value = res.msg
          } else {
            res.type = 'picture-group'
            res.render = 'custom'
            state.data = res?.data
            state.viewDataInfo = res
            state.totalItems = res?.totalItems
            const curViewInfo = chartRuntime.view
            if (curViewInfo) curViewInfo['curFields'] = res.data.fields
            chartRuntime.setDetails(res)
            if (chartRuntime.publishToStore) dvMainStore.setViewDataDetails(element.value.id, res)
            initReady.value = true
            initCurFields(res)
          }
          callback?.()
          nextTick(() => {
            if (!request.isCurrent()) return
            initReady.value = true
          })
        })
        .catch(e => {
          if (!request.isCurrent()) return
          console.error(e)
          nextTick(() => {
            if (!request.isCurrent()) return
            initReady.value = true
          })
          callback?.()
        })
        .finally(request.finish)
    } else if (!viewCalc.tableId) {
      initReady.value = true
      withInit()
      callback?.()
      nextTick(() => {
        if (!request.isCurrent()) return
        initReady.value = true
      })
    } else {
      withInit()
      nextTick(() => {
        if (!request.isCurrent()) return
        initReady.value = true
      })
      callback?.()
    }
    request.finish()
  } catch (error) {
    request.finish()
    throw error
  }
}

// 初始化此处不必刷新
const renderChart = viewInfo => {
  //do renderView
}

onBeforeMount(() => buildInnerRefreshTimer())
onBeforeUnmount(() => {
  if (innerRefreshTimer) clearInterval(innerRefreshTimer)
})

defineExpose({
  invalidateQuery,
  calcData,
  renderChart
})
</script>

<style lang="less" scoped>
.pic-main {
  overflow: hidden;
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.pic-upload {
  display: flex;
  width: 100%;
  height: 100%;
  color: #5370af;
  align-items: center;
  justify-content: center;
}
</style>
