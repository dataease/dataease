<template>
  <div
    v-loading="loadingFlag"
    :class="[
      {
        ['active']: active
      },
      'rect-shape'
    ]"
  >
    <EditBarView
      v-if="editBarViewShowFlag"
      :element="element"
      :show-position="showPosition"
      :panel-id="panelInfo.id"
      :chart-title="chart.title || chart.name"
      :is-edit="isEdit"
      :view-id="element.propValue.viewId"
      @showViewDetails="openChartDetailsDialog"
    />
    <div v-if="requestStatus==='error'" class="chart-error-class">
      <div class="chart-error-message-class">
        {{ message }},{{ $t('chart.chart_show_error') }}
        <br>
        {{ $t('chart.chart_error_tips') }}
      </div>
    </div>
    <plugin-com
      v-if="chart.isPlugin"
      :ref="element.propValue.id"
      :component-name="chart.type + '-view'"
      :obj="{chart, trackMenu, searchCount, terminalType: scaleCoefficientType}"
      :chart="chart"
      :track-menu="trackMenu"
      :search-count="searchCount"
      :terminal-type="scaleCoefficientType"
      :scale="scale"
      :theme-style="element.commonBackground"
      :canvas-style-data="canvasStyleData"
      class="chart-class"
      @onChartClick="chartClick"
      @onJumpClick="jumpClick"
      @trigger-edit-click="pluginEditHandler"
    />
    <de-rich-text-view
      v-else-if="richTextViewShowFlag"
      :ref="element.propValue.id"
      :element="element"
      :prop-value="element.propValue.textValue"
      :active="active"
      :edit-mode="editMode"
      :data-row-select="dataRowSelect"
      :data-row-name-select="dataRowNameSelect"
    />
    <chart-component
      v-else-if="charViewShowFlag"
      :ref="element.propValue.id"
      class="chart-class"
      :chart="chart"
      :track-menu="trackMenu"
      :search-count="searchCount"
      :terminal-type="scaleCoefficientType"
      :scale="scale"
      :theme-style="element.commonBackground"
      @onChartClick="chartClick"
      @onJumpClick="jumpClick"
    />
    <chart-component-g2
      v-else-if="charViewG2ShowFlag"
      :ref="element.propValue.id"
      class="chart-class"
      :chart="chart"
      :track-menu="trackMenu"
      :search-count="searchCount"
      :scale="scale"
      @onChartClick="chartClick"
      @onJumpClick="jumpClick"
    />
    <chart-component-s2
      v-else-if="charViewS2ShowFlag"
      :ref="element.propValue.id"
      class="chart-class"
      :chart="chart"
      :track-menu="trackMenu"
      :search-count="searchCount"
      @onChartClick="chartClick"
      @onJumpClick="jumpClick"
    />
    <table-normal
      v-else-if="tableShowFlag"
      :ref="element.propValue.id"
      :show-summary="chart.type === 'table-normal'"
      :chart="chart"
      class="table-class"
    />
    <label-normal
      v-else-if="labelShowFlag"
      :ref="element.propValue.id"
      :chart="chart"
      class="table-class"
    />
    <label-normal-text
      v-else-if="labelTextShowFlag"
      :ref="element.propValue.id"
      :chart="chart"
      class="table-class"
      :track-menu="trackMenu"
      :search-count="searchCount"
      @onChartClick="chartClick"
      @onJumpClick="jumpClick"
    />
    <div style="position: absolute;left: 8px;bottom:8px;">
      <drill-path :drill-filters="drillFilters" :theme-style="element.commonBackground" @onDrillJump="drillJump" />
    </div>
  </div>
</template>

<script>

import { viewData } from '@/api/panel/panel'
import { viewInfo } from '@/api/link'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
import TableNormal from '@/views/chart/components/table/TableNormal'
import LabelNormal from '../../../views/chart/components/normal/LabelNormal'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import { mapState } from 'vuex'
import { isChange } from '@/utils/conditionUtil'
import { BASE_CHART_STRING } from '@/views/chart/chart/chart'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy } from '@/components/canvas/utils/utils'
import { getToken, getLinkToken } from '@/utils/auth'
import DrillPath from '@/views/chart/view/DrillPath'
import { areaMapping } from '@/api/map/map'
import ChartComponentG2 from '@/views/chart/components/ChartComponentG2'
import EditBarView from '@/components/canvas/components/Editor/EditBarView'
import { adaptCurTheme, customAttrTrans, customStyleTrans, recursionTransObj } from '@/components/canvas/utils/style'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import PluginCom from '@/views/system/plugin/PluginCom'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
import { viewEditSave, viewPropsSave } from '@/api/chart/chart'
import { checkAddHttp } from '@/utils/urlUtils'
import DeRichTextView from '@/components/canvas/custom-component/DeRichTextView'
import Vue from 'vue'
import { formatterItem, valueFormatter } from '@/views/chart/chart/formatter'

export default {
  name: 'UserView',
  components: { DeRichTextView, LabelNormalText, PluginCom, ChartComponentS2, EditBarView, ChartComponent, TableNormal, LabelNormal, DrillPath, ChartComponentG2 },
  props: {
    element: {
      type: Object,
      default: null
    },
    outStyle: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    searchCount: {
      type: Number,
      required: false,
      default: 0
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    componentIndex: {
      type: Number,
      required: false
    },
    inTab: {
      type: Boolean,
      required: false,
      default: false
    },
    isEdit: {
      type: Boolean,
      require: false,
      default: true
    },
    terminal: {
      type: String,
      default: 'pc'
    },
    filters: {
      type: Array,
      default: () => []
    },
    canvasStyleData: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    },
    editMode: {
      type: String,
      require: false,
      default: 'preview'
    }
  },
  data() {
    return {
      dataRowNameSelect: {},
      dataRowSelect: {},
      curFields: [],
      isFirstLoad: true, // 是否是第一次加载
      refId: null,
      chart: BASE_CHART_STRING,
      requestStatus: 'success',
      message: null,
      drillClickDimensionList: [],
      drillFilters: [],
      drillFields: [],
      places: [],
      httpRequest: {
        status: true,
        msg: ''
      },
      timeMachine: null,
      scaleTimeMachine: null,
      changeIndex: 0,
      changeScaleIndex: 0,
      pre: null,
      // string
      sourceCustomAttrStr: null,
      // obj
      sourceCustomAttr: null,
      // string
      sourceCustomStyleStr: null,
      // obj
      sourceCustomStyle: null,
      scale: 1
    }
  },

  computed: {
    scaleCoefficient() {
      if (this.terminal === 'pc' && !this.mobileLayoutStatus) {
        return 1.1
      } else {
        return 4.5
      }
    },
    scaleCoefficientType() {
      if (this.terminal === 'pc' && !this.mobileLayoutStatus) {
        return 'pc'
      } else {
        return 'mobile'
      }
    },
    editBarViewShowFlag() {
      return (this.active && this.inTab && !this.mobileLayoutStatus) && !this.showPosition.includes('multiplexing') || this.showPosition.includes('email-task')
    },
    richTextViewShowFlag() {
      return this.httpRequest.status && this.chart.type && this.chart.type === 'richTextView'
    },
    charViewShowFlag() {
      return this.httpRequest.status && this.chart.type && !this.chart.type.includes('table') && !this.chart.type.includes('text') && this.chart.type !== 'label' && this.renderComponent() === 'echarts'
    },
    charViewG2ShowFlag() {
      return this.httpRequest.status && this.chart.type && !this.chart.type.includes('table') && !this.chart.type.includes('text') && this.chart.type !== 'label' && this.renderComponent() === 'antv'
    },
    charViewS2ShowFlag() {
      return this.httpRequest.status && this.chart.type && this.chart.type.includes('table') && !this.chart.type.includes('text') && this.chart.type !== 'label' && this.renderComponent() === 'antv'
    },
    tableShowFlag() {
      return this.httpRequest.status && this.chart.type && this.chart.type.includes('table') && this.renderComponent() === 'echarts'
    },
    labelShowFlag() {
      return this.httpRequest.status && this.chart.type && this.chart.type.includes('text')
    },
    labelTextShowFlag() {
      return this.httpRequest.status && this.chart.type && this.chart.type === 'label'
    },
    loadingFlag() {
      return (this.canvasStyleData.refreshViewLoading || this.searchCount === 0) && this.requestStatus === 'waiting'
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    filter() {
      const filter = {}
      filter.filter = this.isFirstLoad ? this.filters : this.cfilters
      filter.linkageFilters = this.element.linkageFilters
      filter.outerParamsFilters = this.element.outerParamsFilters
      filter.drill = this.drillClickDimensionList
      filter.resultCount = this.resultCount
      filter.resultMode = this.resultMode
      filter.queryFrom = 'panel'
      return filter
    },
    cfilters() {
      // 必要 勿删勿该  watch数组，哪怕发生变化 oldValue等于newValue ，深拷贝解决
      if (!this.element.filters) return []
      return JSON.parse(JSON.stringify(this.element.filters))
    },
    linkageFilters() {
      // 必要 勿删勿该  watch数组，哪怕发生变化 oldValue等于newValue ，深拷贝解决
      if (!this.element.linkageFilters) return []
      return JSON.parse(JSON.stringify(this.element.linkageFilters))
    },
    trackMenu() {
      const trackMenuInfo = []
      let linkageCount = 0
      let jumpCount = 0
      this.chart.data && this.chart.data.fields && this.chart.data.fields.forEach(item => {
        const sourceInfo = this.chart.id + '#' + item.id
        if (this.nowPanelTrackInfo[sourceInfo]) {
          linkageCount++
        }
      })
      this.chart.data && this.chart.data.fields && this.chart.data.fields.forEach(item => {
        const sourceInfo = this.chart.id + '#' + item.id
        if (this.nowPanelJumpInfo[sourceInfo]) {
          jumpCount++
        }
      })
      jumpCount && trackMenuInfo.push('jump')
      linkageCount && trackMenuInfo.push('linkage')
      this.drillFields.length && trackMenuInfo.push('drill')
      return trackMenuInfo
    },
    chartType() {
      return this.chart.type
    },
    hw() {
      return this.outStyle.width * this.outStyle.height
    },
    resultMode() {
      return this.canvasStyleData.panel && this.canvasStyleData.panel.resultMode || null
    },
    resultCount() {
      return this.canvasStyleData.panel && this.canvasStyleData.panel.resultCount || null
    },
    gap() {
      return this.canvasStyleData.panel && this.canvasStyleData.panel.gap || null
    },
    innerPadding() {
      return this.element.commonBackground && this.element.commonBackground.innerPadding || 0
    },
    ...mapState([
      'nowPanelTrackInfo',
      'nowPanelJumpInfo',
      'publicLinkStatus',
      'previewCanvasScale',
      'mobileLayoutStatus',
      'panelViewDetailsInfo',
      'componentViewsData',
      'curBatchOptComponents'
    ])
  },

  watch: {
    'innerPadding': {
      handler: function(val1, val2) {
        this.resizeChart()
      },
      deep: true
    },
    'cfilters': {
      handler: function(val1, val2) {
        if ((isChange(val1, val2) || isChange(val1, this.filters)) && !this.isFirstLoad) {
          this.getData(this.element.propValue.viewId)
        }
      },
      deep: true
    },
    linkageFilters: {
      handler(newVal, oldVal) {
        if (isChange(newVal, oldVal)) {
          this.getData(this.element.propValue.viewId)
        }
      },
      deep: true
    },
    resultCount: function() {
      this.getData(this.element.propValue.viewId, false)
    },
    resultMode: function() {
      this.getData(this.element.propValue.viewId, false)
    },
    gap: function() {
      this.resizeChart()
    },
    // 监听外部的样式变化 （非实时性要求）
    'hw': {
      handler(newVal, oldVla) {
        if (newVal !== oldVla && this.$refs[this.element.propValue.id]) {
          this.resizeChart()
        }
      },
      deep: true
    },
    // 监听外部的样式变化 （非实时性要求）
    outStyle: {
      handler(newVal, oldVla) {
      },
      deep: true
    },
    // 监听外部计时器变化
    searchCount: function(val1) {
      if (val1 > 0 && this.requestStatus !== 'waiting') {
        this.getData(this.element.propValue.viewId)
      }
    },
    'chartType': function(newVal, oldVal) {
      if ((newVal === 'map' || newVal === 'buddle-map') && newVal !== oldVal) {
        this.initAreas()
      }
    },
    // 监控缩放比例
    previewCanvasScale: {
      handler(newVal, oldVal) {
        this.destroyScaleTimeMachine()
        this.changeScaleIndex++
        this.chartScale(this.changeScaleIndex)
      },
      deep: true
    }
  },
  mounted() {
    this.bindPluginEvent()
  },

  beforeDestroy() {
    bus.$off('plugin-chart-click', this.pluginChartClick)
    bus.$off('plugin-jump-click', this.pluginJumpClick)
    bus.$off('plugin-add-view-track-filter', this.pluginAddViewTrackFilter)
    bus.$off('view-in-cache', this.viewInCache)
    bus.$off('batch-opt-change', this.batchOptChange)
    bus.$off('onSubjectChange', this.optFromBatchThemeChange)
    bus.$off('onThemeColorChange', this.optFromBatchThemeChange)
    bus.$off('onThemeAttrChange', this.optFromBatchSingleProp)
    bus.$off('clear_panel_linkage', this.clearPanelLinkage)
  },
  created() {
    this.refId = uuid.v1
    if (this.element && this.element.propValue && this.element.propValue.viewId) {
      // 如果watch.filters 已经进行数据初始化时候，此处放弃数据初始化

      this.getData(this.element.propValue.viewId, false)
    }
  },
  methods: {
    pluginEditHandler(e) {
      this.$emit('trigger-plugin-edit', { e, id: this.element.id })
    },
    batchOptChange(param) {
      if (this.curBatchOptComponents.includes(this.element.propValue.viewId)) {
        this.optFromBatchSingleProp(param)
      }
    },
    optFromBatchSingleProp(param) {
      this.$store.commit('canvasChange')
      const updateParams = { 'id': this.chart.id }
      if (param.custom === 'customAttr') {
        const sourceCustomAttr = JSON.parse(this.sourceCustomAttrStr)
        sourceCustomAttr[param.property][param.value.modifyName] = param.value[param.value.modifyName]
        this.sourceCustomAttrStr = JSON.stringify(sourceCustomAttr)
        this.chart.customAttr = this.sourceCustomAttrStr
        updateParams['customAttr'] = this.sourceCustomAttrStr
      } else if (param.custom === 'customStyle') {
        const sourceCustomStyle = JSON.parse(this.sourceCustomStyleStr)
        sourceCustomStyle[param.property][param.value.modifyName] = param.value[param.value.modifyName]
        this.sourceCustomStyleStr = JSON.stringify(sourceCustomStyle)
        this.chart.customStyle = this.sourceCustomStyleStr
        updateParams['customStyle'] = this.sourceCustomStyleStr
      }
      viewPropsSave(this.panelInfo.id, updateParams).then(rsp =>{
        this.active && bus.$emit('current-component-change')
      })
      this.$store.commit('recordViewEdit', { viewId: this.chart.id, hasEdit: true })
      this.mergeScale()
    },
    optFromBatchThemeChange() {
      const updateParams = { 'id': this.chart.id }
      const sourceCustomAttr = JSON.parse(this.sourceCustomAttrStr)
      const sourceCustomStyle = JSON.parse(this.sourceCustomStyleStr)
      adaptCurTheme(sourceCustomStyle, sourceCustomAttr, this.chart.type)
      this.sourceCustomAttrStr = JSON.stringify(sourceCustomAttr)
      this.chart.customAttr = this.sourceCustomAttrStr
      updateParams['customAttr'] = this.sourceCustomAttrStr
      this.sourceCustomStyleStr = JSON.stringify(sourceCustomStyle)
      this.chart.customStyle = this.sourceCustomStyleStr
      updateParams['customStyle'] = this.sourceCustomStyleStr
      viewPropsSave(this.panelInfo.id, updateParams).then(rsp =>{
        this.active && bus.$emit('current-component-change')
      })
      this.$store.commit('recordViewEdit', { viewId: this.chart.id, hasEdit: true })
      this.mergeScale()
    },
    resizeChart() {
      if (this.chart.type === 'map') {
        this.destroyTimeMachine()
        this.changeIndex++
        this.chartResize(this.changeIndex)
      } else if (this.$refs[this.element.propValue.id]) {
        this.chart.isPlugin
          ? this.$refs[this.element.propValue.id].callPluginInner({ methodName: 'chartResize' })
          : this.$refs[this.element.propValue.id].chartResize()
      }
    },
    pluginChartClick(param) {
      param.viewId && param.viewId === this.element.propValue.viewId && this.chartClick(param)
    },
    pluginJumpClick(param) {
      param.viewId && param.viewId === this.element.propValue.viewId && this.jumpClick(param)
    },
    pluginAddViewTrackFilter(param) {
      param.viewId && param.viewId === this.element.propValue.viewId && this.addViewTrackFilter(param)
    },
    viewInCache(param) {
      param.viewId && param.viewId === this.element.propValue.viewId && this.getDataEdit(param)
    },
    clearPanelLinkage(param) {
      if (param.viewId === 'all' || param.viewId === this.element.propValue.viewId) {
        this.$refs[this.element.propValue.id].reDrawView()
      }
    },
    bindPluginEvent() {
      bus.$on('plugin-chart-click', this.pluginChartClick)
      bus.$on('plugin-jump-click', this.pluginJumpClick)
      bus.$on('plugin-add-view-track-filter', this.pluginAddViewTrackFilter)
      bus.$on('view-in-cache', this.viewInCache)
      bus.$on('batch-opt-change', this.batchOptChange)
      bus.$on('onSubjectChange', this.optFromBatchThemeChange)
      bus.$on('onThemeColorChange', this.optFromBatchThemeChange)
      bus.$on('onThemeAttrChange', this.optFromBatchSingleProp)
      bus.$on('clear_panel_linkage', this.clearPanelLinkage)
    },
    addViewTrackFilter(linkageParam) {
      this.$store.commit('addViewTrackFilter', linkageParam)
    },
    // 根据仪表板的缩放比例，修改视图内部参数
    mergeScale() {
      this.scale = Math.min(this.previewCanvasScale.scalePointWidth, this.previewCanvasScale.scalePointHeight) * this.scaleCoefficient
      const customAttrChart = JSON.parse(this.sourceCustomAttrStr)
      const customStyleChart = JSON.parse(this.sourceCustomStyleStr)
      recursionTransObj(customAttrTrans, customAttrChart, this.scale, this.scaleCoefficientType)
      recursionTransObj(customStyleTrans, customStyleChart, this.scale, this.scaleCoefficientType)
      // 移动端地图标签不显示
      if (this.chart.type === 'map' && this.scaleCoefficientType === 'mobile') {
        customAttrChart.label.show = false
      }
      this.chart = {
        ...this.chart,
        customAttr: JSON.stringify(customAttrChart),
        customStyle: JSON.stringify(customStyleChart)
      }
    },
    getData(id, cache = true, dataBroadcast = false) {
      if (id) {
        this.requestStatus = 'waiting'
        this.message = null

        // 增加判断 仪表板公共连接中使用viewInfo 正常使用viewData
        let method = viewData
        const token = this.$store.getters.token || getToken()
        const linkToken = this.$store.getters.linkToken || getLinkToken()
        if (!token && linkToken) {
          method = viewInfo
        }
        const requestInfo = {
          ...this.filter,
          cache: cache,
          queryFrom: this.isEdit ? 'panel_edit' : 'panel'
        }
        if (this.panelInfo.proxy) {
          // method = viewInfo
          requestInfo.proxy = { userId: this.panelInfo.proxy }
        }
        method(id, this.panelInfo.id, requestInfo).then(response => {
          // 将视图传入echart组件
          if (response.success) {
            this.chart = response.data
            this.getDataOnly(response.data, dataBroadcast)
            this.chart['position'] = this.inTab ? 'tab' : 'panel'
            // 记录当前数据
            this.panelViewDetailsInfo[id] = JSON.stringify(this.chart)
            if (this.element.needAdaptor) {
              const customStyleObj = JSON.parse(this.chart.customStyle)
              const customAttrObj = JSON.parse(this.chart.customAttr)
              adaptCurTheme(customStyleObj, customAttrObj)
              this.chart.customStyle = JSON.stringify(customStyleObj)
              this.chart.customAttr = JSON.stringify(customAttrObj)
              viewEditSave(this.panelInfo.id, { id: this.chart.id, customStyle: this.chart.customStyle, customAttr: this.chart.customAttr })
              this.$store.commit('adaptorStatusDisable', this.element.id)
            }
            this.sourceCustomAttrStr = this.chart.customAttr
            this.sourceCustomStyleStr = this.chart.customStyle
            this.chart.drillFields = this.chart.drillFields ? JSON.parse(this.chart.drillFields) : []
            if (!response.data.drill) {
              this.drillClickDimensionList.splice(this.drillClickDimensionList.length - 1, 1)
              this.resetDrill()
            }
            this.drillFilters = JSON.parse(JSON.stringify(response.data.drillFilters ? response.data.drillFilters : []))
            this.drillFields = JSON.parse(JSON.stringify(response.data.drillFields))
            this.requestStatus = 'merging'
            this.mergeScale()
            this.initCurFields(this.chart)
            this.requestStatus = 'success'
            this.httpRequest.status = true
          } else {
            this.requestStatus = 'error'
            this.message = response.message
          }
          this.isFirstLoad = false
          return true
        }).catch(err => {
          this.requestStatus = 'error'
          if (err.message && err.message.indexOf('timeout') > -1) {
            this.message = this.$t('panel.timeout_refresh')
          } else if (!err.response) {
            this.httpRequest.status = false
          } else {
            if (err.response) {
              this.httpRequest.status = err.response.data.success
              this.httpRequest.msg = err.response.data.message
              if (err && err.response && err.response.data) {
                this.message = err.response.data.message
              } else {
                this.message = err
              }
            }
          }
          this.isFirstLoad = false
          return true
        })
      }
    },
    initCurFields(chartDetails) {
      this.curFields = []
      this.dataRowSelect = []
      this.dataRowNameSelect = []
      if (chartDetails.data && chartDetails.data.sourceFields) {
        const checkAllAxisStr = chartDetails.xaxis + chartDetails.xaxisExt + chartDetails.yaxis + chartDetails.yaxisExt
        chartDetails.data.sourceFields.forEach(field => {
          if (checkAllAxisStr.indexOf(field.id) > -1) {
            this.curFields.push(field)
          }
        })
        // Get the corresponding relationship between id and value
        const nameIdMap = chartDetails.data.fields.reduce((pre, next) => {
          pre[next['dataeaseName']] = next['id']
          return pre
        }, {})
        const sourceFieldNameIdMap = chartDetails.data.fields.reduce((pre, next) => {
          pre[next['dataeaseName']] = next['name']
          return pre
        }, {})
        const rowData = chartDetails.data.tableRow[0]
        if(chartDetails.type === 'richTextView'){
          let yAxis = []
          try {
            yAxis = JSON.parse(chartDetails.yaxis)
          } catch (err) {
            yAxis = JSON.parse(JSON.stringify(chartDetails.yaxis))
          }
          let yDataeaseNames = []
          let yDataeaseNamesCfg = []
          yAxis.forEach(yItem => {
            yDataeaseNames.push(yItem.dataeaseName)
            yDataeaseNamesCfg[yItem.dataeaseName]=yItem.formatterCfg
          })
          this.rowDataFormat(rowData,yDataeaseNames,yDataeaseNamesCfg)
        }
        for (const key in rowData) {
          this.dataRowSelect[nameIdMap[key]] = rowData[key]
          this.dataRowNameSelect[sourceFieldNameIdMap[key]] = rowData[key]
        }
      }
      Vue.set(this.element.propValue, 'innerType', chartDetails.type)
      Vue.set(this.element.propValue, 'render', chartDetails.render)
      if (chartDetails.type === 'richTextView') {
        this.$nextTick(() => {
          bus.$emit('initCurFields-' + this.element.id)
        })
      }
    },
    rowDataFormat(rowData,yDataeaseNames,yDataeaseNamesCfg) {
      for (const key in rowData) {
       if(yDataeaseNames.includes(key)){
         let formatterCfg = yDataeaseNamesCfg[key]
         let value = rowData[key]
         if (value === null || value === undefined) {
           rowData[key] = '-'
         }
         if (formatterCfg) {
           const v = valueFormatter(value, formatterCfg)
           rowData[key] = v.includes('NaN') ? value : v
         } else {
           const v = valueFormatter(value, formatterItem)
           rowData[key] =  v.includes('NaN') ? value : v
         }
       }
      }

    },
    viewIdMatch(viewIds, viewId) {
      return !viewIds || viewIds.length === 0 || viewIds.includes(viewId)
    },
    openChartDetailsDialog(params) {
      const tableChart = deepCopy(this.chart)
      tableChart.customAttr = JSON.parse(this.chart.customAttr)
      tableChart.customStyle = JSON.parse(this.chart.customStyle)
      tableChart.customAttr.color.tableHeaderBgColor = '#f8f8f9'
      tableChart.customAttr.color.tableItemBgColor = '#ffffff'
      tableChart.customAttr.color.tableFontColor = '#7c7e81'
      tableChart.customAttr.color.tableStripe = true
      tableChart.customStyle.text.show = false
      tableChart.customAttr = JSON.stringify(tableChart.customAttr)
      tableChart.customStyle = JSON.stringify(tableChart.customStyle)
      eventBus.$emit('openChartDetailsDialog', { chart: this.chart, tableChart: tableChart, openType: params.openType })
    },
    chartClick(param) {
      if (this.drillClickDimensionList.length < this.chart.drillFields.length - 1) {
        (this.chart.type === 'map' || this.chart.type === 'buddle-map') && this.sendToChildren(param)
        this.drillClickDimensionList.push({ dimensionList: param.data.dimensionList })
        this.getData(this.element.propValue.viewId)
      } else if (this.chart.drillFields.length > 0) {
        this.$message({
          type: 'error',
          message: this.$t('chart.last_layer'),
          showClose: true
        })
      }
    },

    jumpClick(param) {
      let dimension, jumpInfo, sourceInfo
      // 如果有名称name 获取和name匹配的dimension 否则倒序取最后一个能匹配的
      if (param.name) {
        param.dimensionList.forEach(dimensionItem => {
          if (dimensionItem.value === param.name) {
            dimension = dimensionItem
            sourceInfo = param.viewId + '#' + dimension.id
            jumpInfo = this.nowPanelJumpInfo[sourceInfo]
          }
        })
      } else {
        for (let i = param.dimensionList.length - 1; i >= 0; i--) {
          dimension = param.dimensionList[i]
          sourceInfo = param.viewId + '#' + dimension.id
          jumpInfo = this.nowPanelJumpInfo[sourceInfo]
          if (jumpInfo) {
            break
          }
        }
      }
      if (jumpInfo) {
        param.sourcePanelId = this.panelInfo.id
        param.sourceViewId = param.viewId
        param.sourceFieldId = dimension.id
        // 内部仪表板跳转
        if (jumpInfo.linkType === 'inner') {
          if (jumpInfo.targetPanelId) {
            localStorage.setItem('jumpInfoParam', JSON.stringify(param))
            if (this.publicLinkStatus) {
              // 判断是否有公共链接ID
              if (jumpInfo.publicJumpId) {
                const url = '/link/' + jumpInfo.publicJumpId
                window.open(url, jumpInfo.jumpType)
              } else {
                this.$message({
                  type: 'warn',
                  message: this.$t('panel.public_link_tips'),
                  showClose: true
                })
              }
            } else {
              const url = '#/preview/' + jumpInfo.targetPanelId
              window.open(url, jumpInfo.jumpType)
            }
          } else {
            this.$message({
              type: 'warn',
              message: '未指定跳转仪表板',
              showClose: true
            })
          }
        } else {
          const colList = [...param.dimensionList, ...param.quotaList]
          let url = this.setIdValueTrans('id', 'value', jumpInfo.content, colList)
          url = checkAddHttp(url)
          window.open(url, jumpInfo.jumpType)
        }
      } else {
        if (this.chart.type.indexOf('table') === -1) {
          this.$message({
            type: 'warn',
            message: '未获取跳转信息',
            showClose: true
          })
        }
      }
    },
    setIdValueTrans(from, to, content, colList) {
      if (!content) {
        return content
      }
      let name2Id = content
      const nameIdMap = colList.reduce((pre, next) => {
        pre[next[from]] = next[to]
        return pre
      }, {})
      const on = content.match(/\[(.+?)\]/g)
      if (on) {
        on.forEach(itm => {
          const ele = itm.slice(1, -1)
          name2Id = name2Id.replace(itm, nameIdMap[ele])
        })
      }
      return name2Id
    },

    resetDrill() {
      const length = this.drillClickDimensionList.length
      this.drillClickDimensionList = []
      if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
        this.backToParent(0, length)
        const current = this.$refs[this.element.propValue.id]

        if (this.chart.isPlugin) {
          current && current.callPluginInner && this.setDetailMapCode(null) && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: null })
        } else {
          current && current.registerDynamicMap && this.setDetailMapCode(null) && current.registerDynamicMap(null)
        }
      }
    },

    drillJump(index) {
      const length = this.drillClickDimensionList.length
      this.drillClickDimensionList = this.drillClickDimensionList.slice(0, index)
      if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
        this.backToParent(index, length)
      }
      this.getData(this.element.propValue.viewId)
    },
    // 回到父级地图
    backToParent(index, length) {
      if (length <= 0) return
      const times = length - 1 - index

      let temp = times
      let tempNode = this.currentAcreaNode
      while (temp >= 0) {
        tempNode = this.findEntityByCode(tempNode.pcode, this.places)
        temp--
      }

      this.currentAcreaNode = tempNode
      const current = this.$refs[this.element.propValue.id]
      if (this.chart.isPlugin) {
        current && current.callPluginInner && this.setDetailMapCode(this.currentAcreaNode.code) && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: this.currentAcreaNode.code })
      } else {
        current && current.registerDynamicMap && this.setDetailMapCode(this.currentAcreaNode.code) && current.registerDynamicMap(this.currentAcreaNode.code)
      }
    },

    setDetailMapCode(code) {
      this.element.DetailAreaCode = code
      bus.$emit('set-dynamic-area-code', code)
      return true
    },

    // 切换下一级地图
    sendToChildren(param) {
      const length = param.data.dimensionList.length
      const name = param.data.dimensionList[length - 1].value
      let aCode = null
      if (this.currentAcreaNode) {
        aCode = this.currentAcreaNode.code
      }
      const customAttr = JSON.parse(this.chart.customAttr)
      const currentNode = this.findEntityByCode(aCode || customAttr.areaCode, this.places)
      if (currentNode && currentNode.children && currentNode.children.length > 0) {
        const nextNode = currentNode.children.find(item => item.name === name)
        this.currentAcreaNode = nextNode
        const current = this.$refs[this.element.propValue.id]
        if (this.chart.isPlugin) {
          nextNode && current && current.callPluginInner && this.setDetailMapCode(nextNode.code) && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: nextNode.code })
        } else {
          nextNode && current && current.registerDynamicMap && this.setDetailMapCode(nextNode.code) && current.registerDynamicMap(nextNode.code)
        }
      }
    },

    findEntityByCode(code, array) {
      if (array === null || array.length === 0) array = this.places
      for (let index = 0; index < array.length; index++) {
        const node = array[index]
        if (node.code === code) return node
        if (node.children && node.children.length > 0) {
          const temp = this.findEntityByCode(code, node.children)
          if (temp) return temp
        }
      }
    },
    initAreas() {
      Object.keys(this.places).length === 0 && areaMapping().then(res => {
        this.places = res.data
      })
    },
    doMapLink(linkFilters) {
      if (!linkFilters && linkFilters.length === 0) return
      const value = linkFilters[0].value
      if (!value && value.length === 0) return
      const name = value[0]
      if (!name) return
      const areaNode = this.findEntityByname(name, [])
      if (!areaNode) return
      const current = this.$refs[this.element.propValue.id]

      if (this.chart.isPlugin) {
        current && current.callPluginInner && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: areaNode.code })
      } else {
        current && current.registerDynamicMap && current.registerDynamicMap(areaNode.code)
      }
    },
    // 根据地名获取areaCode
    findEntityByname(name, array) {
      if (array === null || array.length === 0) array = this.places
      for (let index = 0; index < array.length; index++) {
        const node = array[index]
        if (node.name === name) return node
        if (node.children && node.children.length > 0) {
          const temp = this.findEntityByname(name, node.children)
          if (temp) return temp
        }
      }
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },
    destroyScaleTimeMachine() {
      this.scaleTimeMachine && clearTimeout(this.scaleTimeMachine)
      this.scaleTimeMachine = null
    },

    // 边框变化
    chartResize(index) {
      if (this.$refs[this.element.propValue.id]) {
        this.timeMachine = setTimeout(() => {
          if (index === this.changeIndex) {
            this.chart.isPlugin
              ? this.$refs[this.element.propValue.id].callPluginInner({ methodName: 'chartResize' })
              : this.$refs[this.element.propValue.id].chartResize()
          }
          this.destroyTimeMachine()
        }, 50)
      }
    },

    // 边框变化 修改视图内部参数
    chartScale(index) {
      if (this.$refs[this.element.propValue.id]) {
        this.scaleTimeMachine = setTimeout(() => {
          if (index === this.changeScaleIndex) {
            this.mergeScale()
          }
          this.destroyScaleTimeMachine()
        }, 100)
      }
    },

    renderComponent() {
      return this.chart.render
    },
    getDataEdit(param) {
      this.$store.commit('canvasChange')
      if (param.type === 'propChange') {
        this.getData(param.viewId, false, true)
      } else if (param.type === 'styleChange') {
        this.chart.customAttr = param.viewInfo.customAttr
        this.chart.customStyle = param.viewInfo.customStyle
        this.chart.senior = param.viewInfo.senior
        this.chart.title = param.viewInfo.title
        this.chart.stylePriority = param.viewInfo.stylePriority
        this.sourceCustomAttrStr = this.chart.customAttr
        this.sourceCustomStyleStr = this.chart.customStyle
        if (this.componentViewsData[this.chart.id]) {
          this.componentViewsData[this.chart.id]['title'] = this.chart.title
          if (param.refreshProp) {
            this.componentViewsData[this.chart.id][param.refreshProp] = this.chart[param.refreshProp]
          }
        }
        this.mergeScale()
      }
    },
    getDataOnly(sourceResponseData, dataBroadcast) {
      if (this.isEdit) {
        if ((this.filter.filter && this.filter.filter.length) || (this.filter.linkageFilters && this.filter.linkageFilters.length)) {
          viewData(this.chart.id, this.panelInfo.id, {
            filter: [],
            drill: [],
            queryFrom: 'panel'
          }).then(response => {
            this.componentViewsData[this.chart.id] = response.data
            if (dataBroadcast) {
              bus.$emit('prop-change-data')
            }
          })
        } else {
          this.componentViewsData[this.chart.id] = sourceResponseData
          if (dataBroadcast) {
            bus.$emit('prop-change-data')
          }
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .rect-shape {
    width: 100%;
    height: 100%;
    overflow: hidden;
  }

  .chart-class {
    height: 100%;
    padding: 0px!important;
  }

  .table-class {
    height: 100%;
  }

  .chart-error-class {
    text-align: center;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #ece7e7;
  }

  .chart-error-message-class {
    font-size: 12px;
    color: #9ea6b2;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .active {

  }

  .active ::v-deep .icon-fangda {
    z-index: 2;
    display: block !important;
  }
</style>
