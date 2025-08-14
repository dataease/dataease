<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_linkRecord_outlined from '@/assets/svg/icon_link-record_outlined.svg'
import icon_viewinchat_outlined from '@/assets/svg/icon_viewinchat_outlined.svg'
import icon_drilling_outlined from '@/assets/svg/icon_drilling_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import DeIndicator from '@/custom-component/indicator/DeIndicator.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
import { XpackComponent } from '@/components/plugin'
import { PluginComponent } from '@/components/plugin'
import {
  computed,
  CSSProperties,
  nextTick,
  onBeforeMount,
  onMounted,
  PropType,
  provide,
  reactive,
  ref,
  shallowRef,
  toRefs,
  watch
} from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA, parseJson } from '@/views/chart/components/js/util.js'
import {
  CHART_FONT_FAMILY_MAP,
  DEFAULT_TITLE_STYLE
} from '@/views/chart/components/editor/util/chart'
import DrillPath from '@/views/chart/components/views/components/DrillPath.vue'
import { ElIcon, ElInput, ElMessage } from 'element-plus-secondary'
import { useFilter } from '@/hooks/web/useFilter'
import { useCache } from '@/hooks/web/useCache'

import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { cloneDeep } from 'lodash-es'
import ChartComponentS2 from '@/views/chart/components/views/components/ChartComponentS2.vue'
import { ChartLibraryType } from '@/views/chart/components/js/panel/types'
import chartViewManager from '@/views/chart/components/js/panel'
import { storeToRefs } from 'pinia'
import { checkAddHttp, setIdValueTrans } from '@/utils/canvasUtils'
import { Base64 } from 'js-base64'
import DeRichTextView from '@/custom-component/rich-text/DeRichTextView.vue'
import DePictureGroup from '@/custom-component/picture-group/Component.vue'
import ChartEmptyInfo from '@/views/chart/components/views/components/ChartEmptyInfo.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { viewFieldTimeTrans } from '@/utils/viewUtils'
import { CHART_TYPE_CONFIGS } from '@/views/chart/components/editor/util/chart'
import request from '@/config/axios'
import { store } from '@/store'
import { clearExtremum } from '@/views/chart/components/js/extremumUitl'
import DePreviewPopDialog from '@/components/visualization/DePreviewPopDialog.vue'
import { useRoute } from 'vue-router_2'
const route = useRoute()
const { wsCache } = useCache()
const chartComponent = ref<any>()
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { emitter } = useEmitt()
const dePreviewPopDialogRef = ref(null)
let innerRefreshTimer = null
let innerSearchCount = 0
const appStore = useAppStoreWithOut()
const appearanceStore = useAppearanceStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)
const isIframe = computed(() => appStore.getIsIframe)

const emit = defineEmits(['onPointClick', 'onComponentEvent'])

const {
  nowPanelJumpInfo,
  publicLinkStatus,
  dvInfo,
  curComponent,
  canvasStyleData,
  mobileInPc,
  inMobile,
  editMode
} = storeToRefs(dvMainStore)

const props = defineProps({
  // 公共参数集
  commonParams: {
    type: Object,
    required: false
  },
  active: {
    type: Boolean,
    default: false
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  view: {
    type: Object as PropType<ChartObj>,
    default() {
      return {
        propValue: null
      }
    }
  },
  themes: {
    type: String,
    required: false,
    default: 'dark'
  },
  showPosition: {
    type: String,
    required: false,
    default: 'preview'
  },
  // 仪表板刷新计时器
  searchCount: {
    type: Number,
    required: false,
    default: 0
  },
  disabled: {
    type: Boolean,
    required: false,
    default: false
  },
  scale: {
    type: Number,
    required: false,
    default: 1
  },
  suffixId: {
    type: String,
    required: false,
    default: 'common'
  },
  fontFamily: {
    type: String,
    required: false,
    default: 'inherit'
  },
  optType: {
    type: String,
    required: false
  }
})
const dynamicAreaId = ref('')
const { view, showPosition, element, active, searchCount, scale, suffixId } = toRefs(props)
const titleShow = computed(() => {
  return (
    !['rich-text', 'picture-group'].includes(element.value.innerType) &&
    state.title_show &&
    showPosition.value !== 'viewDialog'
  )
})
const snapshotStore = snapshotStoreWithOut()

const state = reactive({
  initReady: true, //curComponent 切换期间 不接收外部的calcData 和 renderChart 事件
  title_show: true,
  title_remark: {
    show: false,
    remark: ''
  },
  title_class: {
    fontSize: '18px',
    color: '#303133',
    textAlign: 'left',
    fontStyle: 'normal',
    fontWeight: 'normal',
    background: '',
    fontFamily: '',
    textShadow: 'none',
    letterSpacing: '0px',
    fontSynthesis: 'style weight',
    width: 'fit-content',
    maxWidth: '100%',
    wordBreak: 'break-word',
    whiteSpace: 'pre-wrap'
  } as CSSProperties,
  drillFilters: [],
  viewInfoData: null,
  drillClickDimensionList: []
})

const drillClickLength = computed(() => state.drillClickDimensionList.length)

const titleAlign = computed<string>(() => {
  if (!titleShow.value) {
    return 'flex-start'
  }

  if (state.title_class.textAlign === 'center') {
    return 'center'
  } else if (state.title_class.textAlign === 'right') {
    return 'flex-end'
  }

  return 'flex-start'
})

const trackMenu = computed<Array<string>>(() => {
  return chartComponent?.value?.trackMenu ?? []
})

const hasLinkIcon = computed(() => {
  return trackMenu.value.indexOf('linkage') > -1 || trackMenu.value.indexOf('linkageAndDrill') > -1
})
const hasJumpIcon = computed(() => {
  return trackMenu.value.indexOf('jump') > -1 && !mobileInPc.value
})
const hasDrillIcon = computed(() => {
  return trackMenu.value.indexOf('drill') > -1 || trackMenu.value.indexOf('linkageAndDrill') > -1
})

const loading = ref(false)

const resultMode = computed(() => {
  return canvasStyleData.value.dashboard?.resultMode || null
})

const resultCount = computed(() => {
  return canvasStyleData.value.dashboard?.resultCount || null
})

const embeddedStore = useEmbedded()
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
      clearViewLinkage()
      queryData()
      innerSearchCount++
    }, timerRefreshTime)
  }
}

// 清除相同sourceViewId 的 联动条件
const clearViewLinkage = () => {
  dvMainStore.clearViewLinkage(element.value.id)
  useEmitt().emitter.emit('clearPanelLinkage', { viewId: element.value.id })
}

watch([() => scale.value], () => {
  initTitle()
})

watch([() => searchCount.value], () => {
  // 内部计时器启动 忽略外部计时器
  if (!innerRefreshTimer) {
    queryData()
  }
})
// 仪表板的查询结果设置变化 图表数据需要刷新
watch([() => resultCount.value], () => {
  queryData()
})

watch([() => resultMode.value], () => {
  queryData()
})

watch([() => scale.value], () => {
  nextTick(() => {
    chartComponent?.value?.renderChart?.(view.value)
  })
})

watch([() => curComponent.value], () => {
  if (curComponent.value && curComponent.value.id === view.value.id) {
    state.initReady = false
    nextTick(() => {
      state.initReady = true
    })
  }
})

const chartExtRequest = shallowRef(null)
provide('chartExtRequest', chartExtRequest)

const initTitle = () => {
  if (view.value.customStyle) {
    const customStyle = view.value.customStyle
    if (customStyle.text) {
      state.title_show = customStyle.text.show
      state.title_class.fontSize = customStyle.text.fontSize * scale.value + 'px'
      state.title_class.color = customStyle.text.color
      state.title_class.textAlign = customStyle.text.hPosition as CSSProperties['textAlign']
      state.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
      state.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'
      if (!!appearanceStore.fontList.length) {
        appearanceStore.fontList.forEach(ele => {
          CHART_FONT_FAMILY_MAP[ele.name] = ele.name
        })
      }
      state.title_class.fontFamily = customStyle.text.fontFamily
        ? CHART_FONT_FAMILY_MAP[customStyle.text.fontFamily]
        : DEFAULT_TITLE_STYLE.fontFamily
      if (!CHART_FONT_FAMILY_MAP[customStyle.text.fontFamily]) {
        state.title_class.fontFamily = appearanceStore.fontList.find(ele => ele.isDefault)?.name
        customStyle.text.fontFamily = state.title_class.fontFamily
      }
      appearanceStore.setCurrentFont(state.title_class.fontFamily)
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

    state.title_remark.show = customStyle.text.show && customStyle.text.remarkShow
    state.title_remark.remark = customStyle.text.remark
  }
}

const drillJump = (index: number) => {
  state.drillClickDimensionList.splice(index)
  view.value.chartExtRequest = filter()
  calcData(view.value)
}

const onPointClick = param => {
  try {
    const msg = {
      sourceDvId: dvInfo.value.id,
      sourceViewId: view.value.id,
      message: Base64.encode(JSON.stringify(param))
    }
    emit('onPointClick', msg)
  } catch (e) {
    console.warn('de_inner_params send error')
  }
}

const chartClick = param => {
  // 下钻字段第一个没有在维度中不允许下钻
  const xIds = view.value.xAxis.map(ele => ele.id)
  if (xIds.indexOf(props.view.drillFields[0].id) == -1) {
    ElMessage.error(t('chart.drill_field_error'))
    return
  }
  if (view.value.type === 'circle-packing' && param.data.name === t('commons.all')) {
    ElMessage.error(t('chart.last_layer'))
    return
  }
  if (state.drillClickDimensionList.length < props.view.drillFields.length - 1) {
    state.drillClickDimensionList.push({
      dimensionList: param.data.dimensionList,
      extra: param.extra
    })
    view.value.chartExtRequest = filter()
    calcData(view.value)
  } else if (props.view.drillFields.length > 0) {
    ElMessage.error(t('chart.last_layer'))
  }
}

// 仪表板和大屏所有额外过滤参数都在此处
const filter = (firstLoad?: boolean) => {
  const { filter } = useFilter(view.value.id, firstLoad)
  const result = {
    user: wsCache.get('user.uid'),
    filter,
    linkageFilters: element.value.linkageFilters,
    outerParamsFilters: element.value.outerParamsFilters,
    webParamsFilters: element.value.webParamsFilters,
    drill: state.drillClickDimensionList,
    resultCount: resultCount.value,
    resultMode: resultMode.value
  }
  // 定时报告相关勿动
  if (route.path === '/preview' && route.query.taskId) {
    const sceneId = view.value['sceneId']
    const filterJson = window[`de-report-filter-${sceneId}`]
    let filterObj = {}
    if (filterJson) {
      filterObj = JSON.parse(filterJson)
    }
    filterObj[view.value.id] = result
    window[`de-report-filter-${sceneId}`] = JSON.stringify(filterObj)
  }
  return result
}

const onDrillFilters = param => {
  state.drillFilters = param ? param : []
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

const divEmbedded = type => {
  useEmitt().emitter.emit('changeCurrentComponent', type)
}

const windowsJump = (url, jumpType, size = 'middle') => {
  try {
    let newWindow
    if ('newPop' === jumpType) {
      dePreviewPopDialogRef.value.previewInit({ url, size })
    } else if ('_self' === jumpType) {
      newWindow = window.open(url, jumpType)
      if (inMobile.value) {
        window.location.reload()
      }
    } else {
      newWindow = window.open(url, jumpType)
    }
    initOpenHandler(newWindow)
  } catch (e) {
    console.warn(t('visualization.url_check_error') + ':' + url)
  }
}

const jumpClick = param => {
  let dimension, jumpInfo, sourceInfo
  // 如果有名称name 获取和name匹配的dimension 否则倒序取最后一个能匹配的
  if (param.name) {
    param.dimensionList.forEach(dimensionItem => {
      if (dimensionItem.id === param.name || dimensionItem.value === param.name) {
        dimension = dimensionItem
        sourceInfo = param.viewId + '#' + dimension.id
        jumpInfo = nowPanelJumpInfo.value[sourceInfo]
      }
    })
  } else {
    for (let i = param.dimensionList.length - 1; i >= 0; i--) {
      dimension = param.dimensionList[i]
      sourceInfo = param.viewId + '#' + dimension.id
      jumpInfo = nowPanelJumpInfo.value[sourceInfo]
      if (jumpInfo) {
        break
      }
    }
  }
  if (jumpInfo) {
    // 维度日期类型转换
    viewFieldTimeTrans(dvMainStore.getViewDataDetails(param.viewId), param)
    param.sourceDvId = dvInfo.value.id
    param.sourceViewId = param.viewId
    param.sourceFieldId = dimension.id
    let embeddedBaseUrl = ''
    const divSelf = isDataEaseBi.value && jumpInfo.jumpType === '_self'
    const iframeSelf = isIframe.value && jumpInfo.jumpType === '_self'
    if (isDataEaseBi.value) {
      embeddedBaseUrl = embeddedStore.baseUrl
    }
    const jumpInfoParam = `&jumpInfoParam=${encodeURIComponent(
      Base64.encode(JSON.stringify(param))
    )}`

    // 内部仪表板跳转
    if (jumpInfo.linkType === 'inner') {
      if (jumpInfo.targetDvId) {
        const editPreviewParams = ['canvas', 'edit-preview'].includes(showPosition.value)
          ? '&editPreview=true'
          : ''
        const filterOuterParams = {}
        const curFilter = dvMainStore.getLastViewRequestInfo(param.viewId)
        const targetViewInfoList = jumpInfo.targetViewInfoList
        if (
          curFilter &&
          curFilter.filter &&
          curFilter.filter.length > 0 &&
          targetViewInfoList &&
          targetViewInfoList.length > 0
        ) {
          // do filter
          curFilter.filter.forEach(filterItem => {
            targetViewInfoList.forEach(targetViewInfo => {
              if (targetViewInfo.sourceFieldActiveId === filterItem.filterId) {
                filterOuterParams[targetViewInfo.outerParamsName] = {
                  operator: filterItem.operator,
                  value: filterItem.value
                }
              }
            })
          })
        }
        let attachParamsInfo
        if (Object.keys(filterOuterParams).length > 0) {
          filterOuterParams['outerParamsVersion'] = 'v2'
          attachParamsInfo =
            '&attachParams=' + encodeURIComponent(Base64.encode(JSON.stringify(filterOuterParams)))
        }
        // 携带外部参数
        if (publicLinkStatus.value) {
          // 判断是否有公共链接ID
          if (jumpInfo.publicJumpId) {
            let url = `${embeddedBaseUrl}#/de-link/${jumpInfo.publicJumpId}?fromLink=true&dvType=${jumpInfo.targetDvType}`
            if (attachParamsInfo) {
              url = url + attachParamsInfo + jumpInfoParam + editPreviewParams
            } else {
              url = url + '&ignoreParams=true' + jumpInfoParam + editPreviewParams
            }
            const currentUrl = window.location.href
            localStorage.setItem('beforeJumpUrl', currentUrl)
            windowsJump(url, jumpInfo.jumpType, jumpInfo.windowSize)
          } else {
            ElMessage.warning(t('visualization.public_link_tips'))
          }
        } else {
          let url = `${embeddedBaseUrl}#/preview?dvId=${jumpInfo.targetDvId}&fromLink=true&dvType=${jumpInfo.targetDvType}`
          if (attachParamsInfo) {
            url = url + attachParamsInfo + jumpInfoParam + editPreviewParams
          } else {
            url = url + '&ignoreParams=true' + jumpInfoParam + editPreviewParams
          }
          const currentUrl = window.location.href
          localStorage.setItem('beforeJumpUrl', currentUrl)
          if (divSelf || iframeSelf) {
            embeddedStore.setDvId(jumpInfo.targetDvId)
            embeddedStore.setJumpInfoParam(encodeURIComponent(Base64.encode(JSON.stringify(param))))
            divEmbedded('Preview')
            return
          }
          windowsJump(url, jumpInfo.jumpType, jumpInfo.windowSize)
        }
      } else {
        ElMessage.warning('未指定跳转仪表板')
      }
    } else {
      const colList = [...param.dimensionList, ...param.quotaList]
      let url = setIdValueTrans('id', 'value', jumpInfo.content, colList)
      url = checkAddHttp(url)

      if (isIframe.value || isDataEaseBi.value) {
        embeddedStore.clearState()
      }
      if (divSelf) {
        embeddedStore.setOuterUrl(url)
        divEmbedded('Iframe')
        return
      }

      windowsJump(url, jumpInfo.jumpType, jumpInfo.windowSize)
    }
  } else {
  }
}

const queryData = (firstLoad = false) => {
  if (loading.value) {
    return
  }
  const searched = dvMainStore.firstLoadMap.includes(element.value.id)
  const queryFilter = filter(searched ? false : firstLoad)
  let params = cloneDeep(view.value)
  params['chartExtRequest'] = queryFilter
  chartExtRequest.value = queryFilter
  calcData(params)
}

const calcData = params => {
  dvMainStore.setLastViewRequestInfo(params.id, params.chartExtRequest)
  if (chartComponent?.value) {
    loading.value = true
    if (view.value.isPlugin) {
      chartComponent?.value?.invokeMethod({
        methodName: 'calcData',
        args: [
          params,
          () => {
            loading.value = false
          }
        ]
      })
    } else {
      chartComponent?.value?.calcData?.(params, () => {
        loading.value = false
      })
    }
  }
}

const showChartView = (...libs: ChartLibraryType[]) => {
  if (view.value?.render && view.value?.type) {
    const chartView = chartViewManager.getChartView(view.value.render, view.value.type)
    return chartView && libs?.includes(chartView.library)
  } else {
    return false
  }
}

onBeforeMount(() => {
  if (!showPosition.value.includes('viewDialog')) {
    nextTick(() => {
      useEmitt({
        name: `query-data-${view.value.id}`,
        callback: queryData
      })
    })
  }
})
// 部分场景不需要更新图表，例如放大页面
const listenerEnable = computed(() => {
  return !showPosition.value.includes('viewDialog')
})
// 存储所有数据集字段，用于判断图表拖入的字段是否存在
const viewAllDatasetFields = new Map()
const showEmpty = ref(false)
const checkFieldIsAllowEmpty = (allField?) => {
  showEmpty.value = false
  if (view.value?.render && view.value?.type) {
    const chartView = chartViewManager.getChartView(view.value.render, view.value.type)
    // 插件
    if (!chartView) {
      return
    }
    const map = parseJson(view.value.customAttr).map
    if (['bubble-map', 'map'].includes(view.value?.type) && !map?.id) {
      showEmpty.value = true
      return
    }
    const axisConfigMap = new Map(Object.entries(chartView.axisConfig))
    // 验证拖入的字段是否包含在当前数据集字段中，如果有一个不在数据集字段中，则显示空图表
    let includeDatasetField = false
    if (allField && allField.length > 0) {
      viewAllDatasetFields.set(view.value.id, allField)
      outerLoop: for (const [key, value] of axisConfigMap) {
        // 只判断必须的
        if (value['allowEmpty']) continue
        if (!view.value?.[key]?.length) continue
        for (const item of view.value[key]) {
          if (!allField.find(field => field.id === item.id)) {
            includeDatasetField = true
            break outerLoop
          }
        }
      }
    }
    if (includeDatasetField) {
      showEmpty.value = true
      return
    }
    for (const [key, value] of axisConfigMap) {
      // 跳过允许为空的配置项
      if (value['allowEmpty']) continue

      // 如果有数据集字段并且字段值存在且不为空
      if (viewAllDatasetFields.get(view.value?.id)) {
        if (view.value?.[key]?.length) {
          // 检查图表字段是否有不在数据集中
          for (const item of view.value[key]) {
            if (!viewAllDatasetFields.get(view.value?.id).find(field => field.id === item.id)) {
              includeDatasetField = true
              break
            }
          }
        }
        // 如果有不在数据集中
        if (includeDatasetField) {
          showEmpty.value = true
          break
        }
      }

      // 如果没有限制长度，且值为空，标记为空并跳出
      if (!value['limit'] && view.value?.[key]?.length === 0) {
        showEmpty.value = true
        break
      }

      // 如果有限制长度，且字段长度不足，标记为空并跳出
      if (
        value['limit'] &&
        (!view.value?.[key] || view.value?.[key]?.length < parseInt(value['limit']))
      ) {
        showEmpty.value = true
        break
      }

      // 如果是table-info类型且字段为空，标记为空并跳出
      if (view.value?.type === 'table-info' && view.value?.[key]?.length === 0) {
        showEmpty.value = true
        break
      }
    }
  }
}
const changeChartType = () => {
  checkFieldIsAllowEmpty()
}
const changeDataset = () => {
  checkFieldIsAllowEmpty()
}

const loadPlugin = ref(false)

onMounted(() => {
  if (!view.value.isPlugin) {
    state.drillClickDimensionList = view.value?.chartExtRequest?.drill ?? []
    queryData(!showPosition.value.includes('viewDialog'))
  } else {
    const searched = dvMainStore.firstLoadMap.includes(element.value.id)
    const queryFilter = filter(!searched)
    view.value['chartExtRequest'] = queryFilter
    chartExtRequest.value = queryFilter
    loadPlugin.value = true
  }
  if (!listenerEnable.value) {
    return
  }
  useEmitt({
    name: 'checkShowEmpty',
    callback: param => {
      if (param.view?.id === view.value.id) {
        checkFieldIsAllowEmpty(param.allFields)
      }
    }
  })
  useEmitt({ name: 'chart-type-change', callback: changeChartType })
  useEmitt({ name: 'dataset-change', callback: changeDataset })
  useEmitt({
    name: 'clearPanelLinkage',
    callback: function (param) {
      if (param.viewId === 'all' || param.viewId === element.value.id) {
        chartComponent?.value?.clearLinkage?.()
      }
    }
  })
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
          calcData(view.value)
        } else if (
          cacheViewInfo.snapshotCacheViewRender.includes(view.value.id) ||
          cacheViewInfo.snapshotCacheViewRender.includes('all')
        ) {
          chartComponent?.value?.renderChart?.(view.value)
        }
      })
    }
  })

  useEmitt({
    name: 'calcData-' + view.value.id,
    callback: function (val) {
      if (!state.initReady) {
        return
      }
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        const targetVal = val || view.value
        calcData(targetVal)
      })
    }
  })

  useEmitt({
    name: 'calcData-all',
    callback: function () {
      if (!state.initReady) {
        return
      }
      initTitle()
      nextTick(() => {
        view.value.chartExtRequest = filter(false)
        calcData(view.value)
      })
    }
  })
  useEmitt({
    name: 'renderChart-' + view.value.id,
    callback: function (val) {
      if (!state.initReady) {
        return
      }
      initTitle()
      const viewInfo = val ? val : view.value
      nextTick(() => {
        if (view.value?.plugin?.isPlugin) {
          chartComponent?.value?.invokeMethod({
            methodName: 'renderChart',
            args: [viewInfo]
          })
          return
        }
        chartComponent?.value?.renderChart?.(viewInfo)
      })
    }
  })
  useEmitt({
    name: 'resetDrill-' + view.value.id,
    callback: function (val) {
      nextTick(() => {
        drillJump(val)
      })
    }
  })
  useEmitt({
    name: 'tabCanvasChange-' + element.value.canvasId,
    callback: function () {
      if (!state.initReady && !view.value.type.includes('table')) {
        return
      }
      setTimeout(function () {
        chartComponent?.value?.renderChart?.(view.value)
      }, 200)
    }
  })
  useEmitt({
    name: 'updateTitle-' + view.value.id,
    callback: () => {
      initTitle()
    }
  })
  useEmitt({
    name: 'chart-type-change-' + view.value.id,
    callback: () => {
      const chart = cloneDeep(view.value)
      chart.container =
        'container-' + showPosition.value + '-' + view.value.id + '-' + suffixId.value
      clearExtremum(chart)
    }
  })

  const { refreshViewEnable, refreshUnit, refreshTime } = view.value
  buildInnerRefreshTimer(refreshViewEnable, refreshUnit, refreshTime)

  initTitle()
})

// 1.开启仪表板刷新 2.首次加载（searchCount =0 ）3.正在请求数据 则显示加载状态
const loadingFlag = computed(() => {
  return (
    (canvasStyleData.value.refreshViewLoading ||
      (searchCount.value === 0 && innerSearchCount === 0)) &&
    loading.value
  )
})

const chartAreaShow = computed(() => {
  if (view.value.tableId) {
    if (element.value['state'] === undefined || element.value['state'] === 'ready') {
      return true
    }
  }
  if (['rich-text', 'picture-group'].includes(view.value.type)) {
    return true
  }
  if (view.value?.isPlugin) {
    return true
  }
  if (view.value['dataFrom'] === 'template') {
    return true
  }
  if (view.value.customAttr?.map?.id) {
    const MAP_CHARTS = ['map', 'bubble-map', 'flow-map']
    if (MAP_CHARTS.includes(view.value.type)) {
      return true
    }
  }
  return false
})

const titleInputRef = ref()
const titleEditStatus = ref(false)
function changeEditTitle() {
  if (!props.active) {
    return
  }
  if (!titleEditStatus.value) {
    titleEditStatus.value = true
    nextTick(() => {
      titleInputRef.value?.focus()
      element.value['editing'] = true
    })
  }
}

function onLeaveTitleInput() {
  element.value['editing'] = false
  titleEditStatus.value = false
}

//v-click-outside 指令
const vClickOutside = {
  beforeMount(el, binding) {
    // 在元素上绑定一个事件监听器
    el.clickOutsideEvent = function (event) {
      // 判断点击事件是否发生在元素外部
      if (!(el === event.target || el.contains(event.target))) {
        // 如果是外部点击，则执行绑定的函数
        binding.value(event)
      }
    }
    // 在全局添加点击事件监听器
    document.addEventListener('click', el.clickOutsideEvent)
  },
  unmounted(el) {
    // 在组件销毁前，移除事件监听器以避免内存泄漏
    document.removeEventListener('click', el.clickOutsideEvent)
  }
}

function onTitleChange() {
  element.value.name = view.value.title
  element.value.label = view.value.title
  snapshotStore.recordSnapshotCache('onTitleChange')
}

const toolTip = computed(() => {
  return props.themes === 'dark' ? 'light' : 'dark'
})

const marginBottom = computed<string | 0>(() => {
  if (!titleShow.value) {
    return 0
  }
  if (titleShow.value || trackMenu.value.length > 0 || state.title_remark.show) {
    return 12 * scale.value + 'px'
  }
  return 0
})

const iconSize = computed<string>(() => {
  return 16 * scale.value + 'px'
})
/**
 * 修改透明度
 * 边框透明度为0时会是存色，顾配置低透明度
 * @param {boolean} isBorder 是否为边框
 */
const modifyAlpha = isBorder => {
  const { backgroundColor, backgroundType, backgroundImageEnable, backgroundColorSelect } =
    element.value.commonBackground
  // 透明
  const transparent = 'rgba(0,0,0,0.01)'
  // 背景图时，设置透明度为0.01
  if (backgroundType === 'outerImage' && backgroundImageEnable) return transparent
  // hex转rgba
  if (backgroundColor.includes('#'))
    return isBorder || !backgroundColorSelect ? transparent : backgroundColor
  const match = backgroundColor.match(/rgba\((\d+), (\d+), (\d+), (\d+|0.\d+)\)/)
  if (!match) return backgroundColor
  const [r, g, b, a] = match.slice(1).map(Number)
  // 边框或者不设置背景色时，设置透明度为0.01，否则原透明度
  return `rgba(${r}, ${g}, ${b}, ${!backgroundColorSelect || isBorder ? 0.01 : a})`
}

const titleIconStyle = computed(() => {
  const bgColor = modifyAlpha(false)
  const borderColor = modifyAlpha(true)
  // 不显示标题时，图标的样式
  const style = {
    position: 'absolute',
    border: `1px solid ${borderColor}`,
    'background-color': bgColor,
    'border-radius': '2px',
    padding: '0 2px 0 2px',
    'z-index': 1,
    top: '2px',
    left: '2px',
    ...(trackMenu.value.length ? {} : { display: 'none' })
  }
  return {
    color: canvasStyleData.value.component.seniorStyleSetting.linkageIconColor,
    ...(titleShow.value ? {} : style)
  }
})
const chartHover = ref(false)
const showActionIcons = computed(() => {
  if (!chartHover.value) {
    return false
  }
  return trackMenu.value.length > 0 || state.title_remark.show
})
const chartConfigs = ref(CHART_TYPE_CONFIGS)
const pluginLoaded = computed(() => {
  let result = false
  chartConfigs.value.forEach(cat => {
    result = cat.details.find(chart => view.value?.type === chart.value) !== undefined
  })
  return result
})
// TODO 统一加载
const loadPluginCategory = data => {
  data.forEach(item => {
    const { category, title, render, chartValue, chartTitle, icon, staticMap } = item
    const node = {
      render,
      category,
      icon,
      value: chartValue,
      title: chartTitle,
      isPlugin: true,
      staticMap
    }
    if (view.value?.type === node.value) {
      view.value.plugin = {
        isPlugin: true,
        staticMap
      }
    }
    const stack = [...chartConfigs.value]
    let findParent = false
    while (stack?.length) {
      const parent = stack.pop()
      if (parent.category === category) {
        const chart = parent.details.find(chart => chart.value === node.value)
        if (!chart) {
          parent.details.push(node)
        }
        findParent = true
      }
    }
    if (!findParent) {
      stack.push({
        category,
        title,
        display: 'show',
        details: [node]
      })
    }
  })
}

const allEmptyCheck = computed(() => {
  return ['rich-text', 'picture-group'].includes(element.value.innerType)
})
/**
 * 标题提示的最大宽度
 */
const titleTooltipWidth = computed(() => {
  if (inMobile.value) {
    return `${screen.width - 10}px`
  }
  if (mobileInPc.value) {
    return '270px'
  }
  return '500px'
})
const clearG2Tooltip = () => {
  const g2TooltipWrapper = document.getElementById('g2-tooltip-wrapper')
  if (g2TooltipWrapper) {
    for (const ele of g2TooltipWrapper.children) {
      ele.style.display = 'none'
    }
  }
}
</script>

<template>
  <div
    class="chart-area report-load"
    :class="{ 'report-load-finish': !loadingFlag }"
    v-loading="loadingFlag"
    element-loading-background="rgba(0,0,0,0)"
    @mouseover="chartHover = true"
    @mouseleave="chartHover = false"
  >
    <div
      class="title-container"
      :style="{ 'justify-content': titleAlign, 'margin-bottom': marginBottom }"
    >
      <template v-if="!titleEditStatus">
        <p class="ellipsis" v-if="titleShow" :style="state.title_class" @dblclick="changeEditTitle">
          {{ view.title }}
        </p>
      </template>
      <template v-else>
        <el-input
          style="flex: 1"
          :effect="canvasStyleData.dashboard.themeColor"
          ref="titleInputRef"
          v-model="view.title"
          @keydown.stop
          @keydown.enter="onLeaveTitleInput"
          v-click-outside="onLeaveTitleInput"
          @change="onTitleChange"
        />
      </template>
      <transition name="fade">
        <div
          class="icons-container"
          :class="{ 'is-editing': titleEditStatus }"
          :style="titleIconStyle"
          v-show="showActionIcons"
        >
          <el-tooltip :effect="toolTip" placement="top" v-if="state.title_remark.show">
            <template #content>
              <div
                :style="{
                  maxWidth: titleTooltipWidth,
                  wordBreak: 'break-all',
                  wordWrap: 'break-word',
                  whiteSpace: 'pre-wrap'
                }"
                v-html="state.title_remark.remark"
              ></div>
            </template>
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
          <el-tooltip :effect="toolTip" placement="top" content="已设置联动" v-if="hasLinkIcon">
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_link-record_outlined"
                ><icon_linkRecord_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </el-tooltip>
          <el-tooltip
            :effect="toolTip"
            placement="top"
            :content="t('visualization.jump_set_tips')"
            v-if="hasJumpIcon"
          >
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_viewinchat_outlined"
                ><icon_viewinchat_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </el-tooltip>
          <el-tooltip
            :effect="toolTip"
            placement="top"
            :content="t('visualization.drill_set_tips')"
            v-if="hasDrillIcon"
          >
            <el-icon :size="iconSize" class="inner-icon">
              <Icon name="icon_drilling_outlined"><icon_drilling_outlined class="svg-icon" /></Icon>
            </el-icon>
          </el-tooltip>
        </div>
      </transition>
    </div>
    <!--这里去渲染不同图库的图表-->
    <div v-if="allEmptyCheck || (chartAreaShow && !showEmpty)" style="flex: 1; overflow: hidden">
      <plugin-component
        v-if="view.plugin?.isPlugin && loadPlugin"
        :jsname="view.plugin.staticMap['index']"
        :scale="scale"
        :dynamic-area-id="dynamicAreaId"
        :view="view"
        :show-position="showPosition"
        :element="element"
        :request="request"
        :emitter="emitter"
        :store="store"
        :suffixId="suffixId"
        :active="active"
        :disabled="!['canvas', 'canvasDataV'].includes(showPosition) || disabled"
        :edit-mode="editMode"
        ref="chartComponent"
        @onChartClick="chartClick"
        @onPointClick="onPointClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        @resetLoading="() => (loading = false)"
      />
      <de-picture-group
        v-else-if="showChartView(ChartLibraryType.PICTURE_GROUP)"
        :themes="canvasStyleData.dashboard.themeColor"
        ref="chartComponent"
        :element="element"
        :active="active"
        :view="view"
        :show-position="showPosition"
        :suffixId="suffixId"
      >
      </de-picture-group>
      <de-rich-text-view
        v-else-if="showChartView(ChartLibraryType.RICH_TEXT)"
        :scale="scale"
        :themes="canvasStyleData.dashboard.themeColor"
        ref="chartComponent"
        :element="element"
        :disabled="!['canvas', 'canvasDataV'].includes(showPosition) || disabled"
        :active="active"
        :show-position="showPosition"
        :edit-mode="editMode"
        :suffixId="suffixId"
      />
      <de-indicator
        :scale="scale"
        v-else-if="showChartView(ChartLibraryType.INDICATOR)"
        :themes="canvasStyleData.dashboard.themeColor"
        ref="chartComponent"
        :view="view"
        :element="element"
        :show-position="showPosition"
        :suffixId="suffixId"
        :font-family="fontFamily"
        :common-params="commonParams"
        @touchstart="clearG2Tooltip"
        @onChartClick="chartClick"
        @onPointClick="onPointClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        @onComponentEvent="() => emit('onComponentEvent')"
      />
      <chart-component-g2-plot
        :scale="scale"
        :dynamic-area-id="dynamicAreaId"
        :view="view"
        :show-position="showPosition"
        :element="element"
        :suffixId="suffixId"
        :font-family="fontFamily"
        :active="active"
        v-else-if="
          showChartView(ChartLibraryType.G2_PLOT, ChartLibraryType.L7_PLOT, ChartLibraryType.L7)
        "
        ref="chartComponent"
        @onChartClick="chartClick"
        @onPointClick="onPointClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        @resetLoading="() => (loading = false)"
      />
      <chart-component-s2
        :view="view"
        :scale="scale"
        :show-position="showPosition"
        :element="element"
        :drill-length="drillClickLength"
        :font-family="fontFamily"
        v-else-if="showChartView(ChartLibraryType.S2)"
        ref="chartComponent"
        @onPointClick="onPointClick"
        @onChartClick="chartClick"
        @onDrillFilters="onDrillFilters"
        @onJumpClick="jumpClick"
        :suffixId="suffixId"
      />
    </div>
    <chart-empty-info
      v-if="(!chartAreaShow || showEmpty) && !allEmptyCheck"
      :themes="canvasStyleData.dashboard.themeColor"
      :view-icon="view.type"
      @touchstart="clearG2Tooltip"
    ></chart-empty-info>
    <drill-path
      :disabled="optType === 'enlarge'"
      :drill-filters="state.drillFilters"
      @onDrillJump="drillJump"
    />
    <XpackComponent
      ref="openHandler"
      jsname="L2NvbXBvbmVudC9lbWJlZGRlZC1pZnJhbWUvT3BlbkhhbmRsZXI="
    />
    <XpackComponent
      v-if="!pluginLoaded && view.isPlugin"
      jsname="L2NvbXBvbmVudC9wbHVnaW5zLWhhbmRsZXIvVmlld0NhdGVnb3J5SGFuZGxlcg=="
      @load-plugin-category="loadPluginCategory"
    />
    <DePreviewPopDialog ref="dePreviewPopDialogRef"></DePreviewPopDialog>
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
.title-container {
  position: relative;
  margin: 0;
  width: 100%;

  display: inline-flex;
  flex-wrap: nowrap;
  justify-content: center;

  gap: 8px;

  .icons-container {
    display: inline-flex;
    flex-direction: row;
    align-items: center;
    flex-wrap: nowrap;
    gap: 8px;

    color: #646a73;

    &.icons-container__dark {
      color: #a6a6a6;
    }

    &.is-editing {
      gap: 6px;
    }

    .inner-icon {
      cursor: pointer;
    }
  }
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.ellipsis {
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
}
</style>
