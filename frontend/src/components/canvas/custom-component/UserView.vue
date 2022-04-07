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
    <EditBarView v-if="editBarViewShowFlag" :is-edit="isEdit" :view-id="element.propValue.viewId" @showViewDetails="openChartDetailsDialog" />
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
      class="chart-class"
    />
    <chart-component
      v-else-if="charViewShowFlag"
      :ref="element.propValue.id"
      class="chart-class"
      :chart="chart"
      :track-menu="trackMenu"
      :search-count="searchCount"
      :terminal-type="scaleCoefficientType"
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
    <label-normal v-else-if="labelShowFlag" :ref="element.propValue.id" :chart="chart" class="table-class" />
    <label-normal-text v-else-if="labelTextShowFlag" :ref="element.propValue.id" :chart="chart" class="table-class" />
    <div style="position: absolute;left: 8px;bottom:8px;">
      <drill-path :drill-filters="drillFilters" @onDrillJump="drillJump" />
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
import { customAttrTrans, customStyleTrans, recursionTransObj } from '@/components/canvas/utils/style'
import ChartComponentS2 from '@/views/chart/components/ChartComponentS2'
import PluginCom from '@/views/system/plugin/PluginCom'
import LabelNormalText from '@/views/chart/components/normal/LabelNormalText'
export default {
  name: 'UserView',
  components: { LabelNormalText, PluginCom, ChartComponentS2, EditBarView, ChartComponent, TableNormal, LabelNormal, DrillPath, ChartComponentG2 },
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
    // eslint-disable-next-line vue/require-default-prop
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
    }
  },
  data() {
    return {
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
      preCanvasPanel: null,
      sourceCustomAttrStr: null,
      sourceCustomStyleStr: null
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
      return this.active && this.inTab && !this.mobileLayoutStatus
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
      this.chart.data && this.chart.data.sourceFields && this.chart.data.sourceFields.forEach(item => {
        const sourceInfo = this.chart.id + '#' + item.id
        if (this.nowPanelTrackInfo[sourceInfo]) {
          linkageCount++
        }
      })
      this.chart.data && this.chart.data.sourceFields && this.chart.data.sourceFields.forEach(item => {
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
      return this.canvasStyleData.panel.resultMode
    },
    resultCount() {
      return this.canvasStyleData.panel.resultCount
    },
    innerPadding() {
      return this.element.commonBackground && this.element.commonBackground.innerPadding || 0
    },
    ...mapState([
      'canvasStyleData',
      'nowPanelTrackInfo',
      'nowPanelJumpInfo',
      'publicLinkStatus',
      'previewCanvasScale',
      'mobileLayoutStatus',
      'componentData',
      'panelViewDetailsInfo'
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
        if (isChange(val1, val2) && !this.isFirstLoad) {
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
    // deep监听panel 如果改变 提交到 store
    canvasStyleData: {
      handler(newVal, oldVla) {
        this.mergeStyle()
        // 如果视图结果模式模式 或者 视图结果获取数量改变 刷新视图
        if (!this.preCanvasPanel || this.preCanvasPanel.resultCount !== newVal.panel.resultCount || this.preCanvasPanel.resultMode !== newVal.panel.resultMode) {
          this.getData(this.element.propValue.viewId, false)
        }
        // 如果gap有变化刷新
        if (this.preCanvasPanel && this.preCanvasPanel.gap !== newVal.panel.gap) {
          this.resizeChart()
        }
        this.preCanvasPanel = deepCopy(newVal.panel)
      },
      deep: true
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
      // this.isPlugin = this.plugins.some(plugin => plugin.value === this.chart.type)
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

  created() {
    this.refId = uuid.v1
    if (this.element && this.element.propValue && this.element.propValue.viewId) {
      // 如果watch.filters 已经进行数据初始化时候，此处放弃数据初始化

      this.getData(this.element.propValue.viewId, false)
    }
  },
  methods: {
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
    bindPluginEvent() {
      bus.$on('plugin-chart-click', param => {
        param.viewId && param.viewId === this.element.propValue.viewId && this.chartClick(param)
      })
      bus.$on('plugin-jump-click', param => {
        param.viewId && param.viewId === this.element.propValue.viewId && this.jumpClick(param)
      })
      bus.$on('plugin-add-view-track-filter', param => {
        param.viewId && param.viewId === this.element.propValue.viewId && this.addViewTrackFilter(param)
      })
      bus.$on('view-in-cache', param => {
        param.viewId && param.viewId === this.element.propValue.viewId && this.getDataEdit(param)
      })
    },

    addViewTrackFilter(linkageParam) {
      this.$store.commit('addViewTrackFilter', linkageParam)
    },
    // 根据仪表板的缩放比例，修改视图内部参数
    mergeScale() {
      const scale = Math.min(this.previewCanvasScale.scalePointWidth, this.previewCanvasScale.scalePointHeight) * this.scaleCoefficient
      const customAttrChart = JSON.parse(this.sourceCustomAttrStr)
      const customStyleChart = JSON.parse(this.sourceCustomStyleStr)
      recursionTransObj(customAttrTrans, customAttrChart, scale, this.scaleCoefficientType)
      recursionTransObj(customStyleTrans, customStyleChart, scale, this.scaleCoefficientType)

      // 移动端地图标签不显示
      if (this.chart.type === 'map' && this.scaleCoefficientType === 'mobile') {
        customAttrChart.label.show = false
      }
      this.chart = {
        ...this.chart,
        customAttr: JSON.stringify(customAttrChart),
        customStyle: JSON.stringify(customStyleChart)
      }
      this.mergeStyle()
    },
    mergeStyle() {
      if ((this.requestStatus === 'success' || this.requestStatus === 'merging') && this.chart.stylePriority === 'panel' && this.canvasStyleData.chart) {
        const customAttrChart = JSON.parse(this.chart.customAttr)
        const customStyleChart = JSON.parse(this.chart.customStyle)
        const customAttrPanel = JSON.parse(this.canvasStyleData.chart.customAttr)
        const customStylePanel = JSON.parse(this.canvasStyleData.chart.customStyle)
        // 组件样式-背景设置
        customStyleChart.background = customStylePanel.background
        // 图形属性-颜色设置
        if (this.chart.type.includes('table')) {
          customAttrChart.color = customAttrPanel.tableColor
        } else {
          customAttrChart.color = customAttrPanel.color
        }
        this.chart = {
          ...this.chart,
          customAttr: JSON.stringify(customAttrChart),
          customStyle: JSON.stringify(customStyleChart)
        }
      }
    },
    getData(id, cache = true) {
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
            this.chart['position'] = this.inTab ? 'tab' : 'panel'
            // 记录当前数据
            this.panelViewDetailsInfo[id] = JSON.stringify(this.chart)
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
          } else {
            this.httpRequest.status = err.response.data.success
            this.httpRequest.msg = err.response.data.message
            if (err && err.response && err.response.data) {
              this.message = err.response.data.message
            } else {
              this.message = err
            }
          }
          this.isFirstLoad = false
          return true
        })
      }
    },
    viewIdMatch(viewIds, viewId) {
      return !viewIds || viewIds.length === 0 || viewIds.includes(viewId)
    },
    openChartDetailsDialog() {
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
      eventBus.$emit('openChartDetailsDialog', { chart: this.chart, tableChart: tableChart })
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
      let dimension, jumpInfo, sourceInfo, jumpFieldName
      // 倒序取最后一个能匹配的
      for (let i = param.dimensionList.length - 1; i >= 0; i--) {
        dimension = param.dimensionList[i]
        sourceInfo = param.viewId + '#' + dimension.id
        jumpInfo = this.nowPanelJumpInfo[sourceInfo]
        if (jumpInfo) {
          break
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
          let url = jumpInfo.content
          // 是否追加点击参数
          if (jumpInfo.attachParams && this.chart.data && this.chart.data.sourceFields) {
            this.chart.data.sourceFields.forEach(item => {
              if (item.id === dimension.id) {
                jumpFieldName = item.name
              }
            })
            const urlAttachParams = jumpFieldName + '=' + dimension.value
            if (url.indexOf('?') > -1) {
              url = url + '&' + urlAttachParams
            } else {
              url = url + '?' + urlAttachParams
            }
          }
          window.open(url, jumpInfo.jumpType)
        }
      } else {
        this.$message({
          type: 'warn',
          message: '未获取跳转信息',
          showClose: true
        })
      }
    },

    resetDrill() {
      const length = this.drillClickDimensionList.length
      this.drillClickDimensionList = []
      if (this.chart.type === 'map' || this.chart.type === 'buddle-map') {
        this.backToParent(0, length)
        const current = this.$refs[this.element.propValue.id]

        if (this.chart.isPlugin) {
          current && current.callPluginInner && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: null })
        } else {
          current && current.registerDynamicMap && current.registerDynamicMap(null)
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
        current && current.callPluginInner && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: this.currentAcreaNode.code })
      } else {
        current && current.registerDynamicMap && current.registerDynamicMap(this.currentAcreaNode.code)
      }
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
          nextNode && current && current.callPluginInner && current.callPluginInner({ methodName: 'registerDynamicMap', methodParam: nextNode.code })
        } else {
          nextNode && current && current.registerDynamicMap && current.registerDynamicMap(nextNode.code)
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
      this.$store.state.styleChangeTimes++
      if (param.type === 'propChange') {
        this.getData(param.viewId, false)
      } else if (param.type === 'styleChange') {
        this.chart.customAttr = param.viewInfo.customAttr
        this.chart.customStyle = param.viewInfo.customStyle
        this.chart.senior = param.viewInfo.senior
        this.chart.title = param.viewInfo.title
        this.chart.stylePriority = param.viewInfo.stylePriority
        this.sourceCustomAttrStr = this.chart.customAttr
        this.sourceCustomStyleStr = this.chart.customStyle
        this.mergeScale()
        this.mergeStyle()
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

  .active > > > .icon-fangda {
    z-index: 2;
    display: block !important;
  }

  /*.rect-shape > i {*/
  /*  right: 5px;*/
  /*  color: gray;*/
  /*  position: absolute;*/
  /*}*/

  /*.rect-shape > > > i:hover {*/
  /*  color: red;*/
  /*}*/

  /*.rect-shape:hover > > > .icon-fangda {*/
  /*  z-index: 2;*/
  /*  display: block;*/
  /*}*/

  /*.rect-shape > > > .icon-fangda {*/
  /*  display: none*/
  /*}*/

  /*.rect-shape:hover > > > .icon-shezhi {*/
  /*  z-index: 2;*/
  /*  display: block;*/
  /*}*/

  /*.rect-shape > > > .icon-shezhi {*/
  /*  display: none*/
  /*}*/
</style>
