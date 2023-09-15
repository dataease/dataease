import { defineStore } from 'pinia'
import { store } from '../../index'
import { deepCopy } from '@/utils/utils'
import { BASE_VIEW_CONFIG } from '@/views/chart/components/editor/util/chart'
import {
  DEFAULT_CANVAS_STYLE_DATA_DARK,
  DEFAULT_CANVAS_STYLE_DATA_LIGHT,
  DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK
} from '@/views/chart/components/editor/util/dataVisualiztion'
import { useEmitt } from '@/hooks/web/useEmitt'
import chartViewManager from '@/views/chart/components/js/panel'

export const dvMainStore = defineStore('dataVisualization', {
  state: () => {
    return {
      staticResourcePath: '/static-resource/',
      canvasCollapse: {
        defaultSide: false,
        realTimeComponent: false,
        canvas: false,
        componentProp: false,
        chartAreaCollapse: false,
        datasetAreaCollapse: false
      },
      editMode: 'edit', // 编辑器模式 edit preview
      canvasStyleData: { ...deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK), backgroundColor: null },
      // 当前展示画布缓存数据
      componentDataCache: null,
      // PC布局画布组件数据
      pcComponentData: [],
      // 移动端布局画布组件数据
      mobileComponentData: [],
      isInEditor: false, // 是否在编辑器中，用于判断复制、粘贴组件时是否生效，如果在编辑器外，则无视这些操作
      componentData: [], // 画布组件数据
      curComponent: null,
      curComponentIndex: null,
      curCanvasScaleMap: {},
      // 预览仪表板缩放信息
      previewCanvasScale: {
        scalePointWidth: 1,
        scalePointHeight: 1
      },
      // 点击画布时是否点中组件，主要用于取消选中组件用。
      // 如果没点中组件，并且在画布空白处弹起鼠标，则取消当前组件的选中状态
      isClickComponent: false,
      // 大屏基础信息
      dvInfo: {
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        type: null
      },
      // 视图信息
      canvasViewInfo: {},
      // 视图展示数据信息
      canvasViewDataInfo: {},
      // 视图最新请求信息
      lastViewRequestInfo: {},
      // 仪表板基础矩阵信息
      bashMatrixInfo: {
        baseWidth: 0,
        baseHeight: 0,
        baseMarginLeft: 0,
        baseMarginTop: 0
      },
      // 当前tab页内组件
      curActiveTabInner: null,
      // 联动设置状态
      linkageSettingStatus: false,
      // 当前设置联动的组件
      curLinkageView: null,
      // 和当前组件联动的目标组件
      targetLinkageInfo: [],
      // 当前仪表板联动 下钻 上卷等信息
      nowPanelTrackInfo: {},
      // 当前仪表板的跳转信息基础信息
      nowPanelJumpInfo: {},
      // 当前仪表板的跳转信息(只包括仪表板)
      nowPanelJumpInfoTargetPanel: {},
      // 当前仪表板的外部参数信息
      nowPanelOuterParamsInfo: {},
      // 拖拽的组件信息
      dragComponentInfo: null,
      // 移动端布局状态
      mobileLayoutStatus: false,
      // 公共链接状态(当前是否是公共链接打开)
      publicLinkStatus: false,
      pcTabMatrixCount: {
        x: 36,
        y: 36
      },
      basePcScreenSize: {
        width: 1920,
        height: 1080
      },
      pcMatrixCount: {
        x: 36,
        y: 18
      },
      mobileMatrixCount: {
        x: 6,
        y: 12
      },
      mobileLayoutStyle: {
        x: 300,
        y: 600
      },
      scrollAutoMove: 0,
      // 系统管理菜单是否收缩
      isCollapse: false,
      // 视图是否编辑记录
      panelViewEditInfo: {},
      // 仪表板视图明细
      panelViewDetailsInfo: {},
      // panel edit batch operation status
      batchOptStatus: false,
      // Currently selected components
      curBatchOptComponents: [],
      // Currently selected Multiplexing components
      curMultiplexingComponents: {},
      mixProperties: [],
      mixPropertiesInner: {},
      batchOptChartInfo: null,
      batchOptViews: {},
      // properties changed
      changeProperties: {
        customStyle: {},
        customAttr: {}
      },
      allViewRender: [],
      tabCollisionActiveId: null, // 当前在碰撞的Tab组件ID
      tabMoveInActiveId: null, // 当前在移入的Tab ID
      tabMoveOutActiveId: null, // 当前在移出的Tab ID
      tabMoveOutComponentId: null, // 当前在移出Tab de组件ID
      tabActiveTabNameMap: {}, // 编辑器中 tab组件中的活动tab页,
      // 鼠标处于drag状态的坐标点
      mousePointShadowMap: {
        mouseX: 0,
        mouseY: 0,
        width: 0,
        height: 0
      },
      previewVisible: false,
      previewComponentData: [],
      currentCanvasNewId: [],
      // 初始状态下当前默认的系统色 dvInfo.type ==== 'dashboard'?'light':'dark'
      curOriginThemes: 'light',
      // 基础网格信息
      baseCellInfo: {},
      dataPrepareState: false, //数据准备状态
      multiplexingStyleAdapt: true //复用样式跟随主题
    }
  },
  actions: {
    setDataPrepareState(value) {
      this.dataPrepareState = value
    },
    setBaseCellInfo(value) {
      this.baseCellInfo = value
    },
    aceSetCanvasData(value) {
      this.canvasStyleData = value
    },

    aceSetCurComponent(value) {
      for (let i = 0; i < this.componentData.length; i++) {
        if (this.componentData[i].id === value.id) {
          this.componentData.splice(i, 1)
        }
      }
      this.componentData.push(value)
      this.curComponent = value
    },

    setClickComponentStatus(status) {
      this.isClickComponent = status
    },

    setEditMode(mode) {
      this.editMode = mode
    },

    setInEditorStatus(status) {
      this.isInEditor = status
    },

    setCanvasStyle(style) {
      this.canvasStyleData = style
    },
    setCanvasViewInfo(canvasViewInfo) {
      this.canvasViewInfo = canvasViewInfo
    },

    setCurComponent({ component, index }) {
      if (!component && this.curComponent) {
        this.curComponent['editing'] = false
      }
      if (component) {
        // Is the current component in editing status
        if (!this.curComponent) {
          component['editing'] = false
        } else if (component.id !== this.curComponent.id) {
          component['editing'] = false
        }
      }
      this.curComponent = component
      this.curComponentIndex = index
    },
    setBashMatrixInfo(bashMatrixInfo) {
      this.bashMatrixInfo = bashMatrixInfo
    },

    setShapeStyle({ top, left, width, height, rotate }) {
      if (this.dvInfo.type === 'dashboard') {
        if (top) this.curComponent.style.top = top < 0 ? 0 : Math.round(top)
        if (left) this.curComponent.style.left = left < 0 ? 0 : Math.round(left)
        if (width) this.curComponent.style.width = Math.round(width)
        if (height) this.curComponent.style.height = Math.round(height)
        if (rotate) this.curComponent.style.rotate = Math.round(rotate)
      } else {
        if (top) this.curComponent.style.top = Math.round(top)
        if (left) this.curComponent.style.left = Math.round(left)
        if (width) this.curComponent.style.width = Math.round(width)
        if (height) this.curComponent.style.height = Math.round(height)
        if (rotate) this.curComponent.style.rotate = Math.round(rotate)
      }
    },

    setShapeSingleStyle({ key, value }) {
      this.curComponent.style[key] = value
    },

    setComponentData(componentData = []) {
      this.componentData = componentData
    },

    addCopyComponent(component, idMap, canvasViewInfoPre = this.canvasViewInfo) {
      // 查找所属画布
      if (component.canvasId === 'canvas-main') {
        this.componentData.push(component)
      } else {
        this.componentData.forEach(componentItem => {
          if (
            component.canvasId.includes(componentItem.id) &&
            componentItem.component == 'DeTabs'
          ) {
            componentItem.propValue.forEach(tabItem => {
              if (component.canvasId.includes(tabItem.name)) {
                tabItem.componentData.push(component)
              }
            })
          }
        })
      }
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const _this = this
      //组件组内部可能还有多个视图
      if (idMap) {
        Object.keys(idMap).forEach(function (oldComponentId) {
          if (canvasViewInfoPre[oldComponentId]) {
            const newComponentId = idMap[oldComponentId]
            _this.canvasViewInfo[newComponentId] = {
              ...deepCopy(canvasViewInfoPre[oldComponentId]),
              id: newComponentId,
              linkageActive: false,
              jumpActive: false
            }
          }
        })
      }
    },

    addComponent({ component, index, isFromGroup = false, componentData = this.componentData }) {
      if (isFromGroup) {
        componentData.push(component)
        return
      }
      if (index !== undefined) {
        componentData.splice(index, 0, component)
        this.setCurComponent({ component: component, index: index })
      } else {
        componentData.push(component)
        this.setCurComponent({ component: component, index: componentData.length - 1 })
      }
      //如果当前的组件是UserView 视图，则想canvasView中增加一项 UserView ID 和componentID保持一致
      if (component.component === 'UserView') {
        let newView = {
          ...JSON.parse(JSON.stringify(BASE_VIEW_CONFIG)),
          id: component.id,
          type: component.innerType,
          render: component.render
        } as unknown as ChartObj
        // 处理配置项默认值，不同视图的同一配置项默认值不同
        const chartViewInstance = chartViewManager.getChartView(newView.render, newView.type)
        if (chartViewInstance) {
          newView = chartViewInstance.setupDefaultOptions(newView)
          newView['title'] = component.name
        }
        this.canvasViewInfo[component.id] = newView
      }
      if (component.component === 'VQuery') {
        const newView = {
          ...JSON.parse(JSON.stringify(BASE_VIEW_CONFIG)),
          id: component.id,
          title: '查询组件',
          type: component.innerType,
          customStyle: {
            component: {
              show: true,
              titleShow: false,
              borderShow: false,
              borderColor: '',
              title: '',
              borderWidth: 1,
              bgColorShow: false,
              bgColor: '',
              layout: 'horizontal',
              btnList: ['sure']
            }
          }
        }
        this.canvasViewInfo[component.id] = newView
      }
    },
    setLinkageTargetInfo(targetLinkageInfo) {
      this.linkageSettingStatus = true
      this.curLinkageView = this.curComponent
      this.targetLinkageInfo = targetLinkageInfo
    },
    removeViewFilter(componentId) {
      this.componentData = this.componentData.map(item => {
        const newItem = item
        newItem.filters =
          (newItem.filters &&
            newItem.filters.filter(filter => filter.componentId !== componentId)) ||
          []
        return newItem
      })
    },

    deleteComponentById(componentId, componentData = this.componentData) {
      let indexResult
      componentData.forEach((component, index) => {
        if (componentId === component.id) {
          indexResult = index
        }
      })
      this.deleteComponent(indexResult, componentData)
    },

    deleteComponent(index?, componentData = this.componentData) {
      if (index === undefined) {
        index = this.curComponentIndex
      }

      if (/\d/.test(index)) {
        this.curComponentIndex = null
        componentData.splice(index, 1)
      }
    },
    updateCurDvInfo(dvInfo) {
      this.dvInfo = dvInfo
      this.curOriginThemes = dvInfo.type === 'dashboard' ? 'light' : 'dark'
    },
    matrixSizeAdaptor() {
      const { baseWidth, baseHeight, baseMarginLeft, baseMarginTop } = this.bashMatrixInfo
      this.componentData.forEach(function (component) {
        component.style.width = baseWidth * component.sizeX - baseMarginLeft
        component.style.height = baseHeight * component.sizeY - baseMarginTop
        component.style.left = baseWidth * (component.x - 1) + baseMarginLeft
        component.style.top = baseHeight * (component.y - 1) + baseMarginTop
      })
    },
    // 清除相同sourceViewId 的 联动条件
    clearViewLinkage(viewId) {
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          const historyLinkageFiltersLength = item.linkageFilters.length
          const newList = item.linkageFilters.filter(linkage => linkage.sourceViewId !== viewId)
          item.linkageFilters.splice(0, item.linkageFilters.length)
          // 重新push 可保证数组指针不变 可以watch到
          if (newList.length > 0) {
            newList.forEach(newLinkage => {
              item.linkageFilters.push(newLinkage)
            })
          }
          // 如果linkageFilters内容长度有变化 则需要重新查询
          if (historyLinkageFiltersLength !== newList.length) {
            useEmitt().emitter.emit('query-data-' + item.id)
          }
        }
      })
    },
    addCurBatchComponent(id) {
      if (id) {
        this.curBatchOptComponents.push(id)
        // get view base info
        const viewBaseInfo = this.canvasViewInfo[id]
        // get properties
        const chartViewInstance = chartViewManager.getChartView(
          viewBaseInfo.render,
          viewBaseInfo.type
        )
        if (chartViewInstance) {
          if (this.curBatchOptComponents.length === 1) {
            this.changeProperties.customAttr = viewBaseInfo.customAttr
            this.changeProperties.customStyle = viewBaseInfo.customStyle
          }
          this.batchOptViews[id] = {
            properties: chartViewInstance.properties,
            propertyInner: chartViewInstance.propertyInner,
            value: chartViewInstance.name
          }
          this.setBatchOptChartInfo()
        }
      }
    },
    setBatchOptChartInfo() {
      const render = null
      let type = null
      this.mixProperties = []
      this.mixPropertiesInner = {}
      let mixPropertiesTemp = []
      let mixPropertyInnerTemp = {}
      if (this.batchOptViews && JSON.stringify(this.batchOptViews) !== '{}') {
        for (const key in this.batchOptViews) {
          if (mixPropertiesTemp.length > 0) {
            // If it exists , taking the intersection
            mixPropertiesTemp = mixPropertiesTemp.filter(
              property => this.batchOptViews[key].properties.indexOf(property) > -1
            )
            // 根据当前的mixPropertiesTemp 再对 mixPropertyInnerTemp 进行过滤
            mixPropertiesTemp.forEach(propertyInnerItem => {
              if (
                mixPropertyInnerTemp[propertyInnerItem] &&
                this.batchOptViews[key].propertyInner[propertyInnerItem]
              ) {
                mixPropertyInnerTemp[propertyInnerItem] = mixPropertyInnerTemp[
                  propertyInnerItem
                ].filter(
                  propertyInnerItemValue =>
                    this.batchOptViews[key].propertyInner[propertyInnerItem].indexOf(
                      propertyInnerItemValue
                    ) > -1
                )
              }
            })
          } else {
            // If it doesn't exist, assignment directly
            mixPropertiesTemp = deepCopy(this.batchOptViews[key].properties)
            mixPropertyInnerTemp = deepCopy(this.batchOptViews[key].propertyInner)
          }
          type = this.batchOptViews[key].value
        }
        mixPropertiesTemp.forEach(property => {
          if (mixPropertyInnerTemp[property] && mixPropertyInnerTemp[property].length) {
            this.mixPropertiesInner[property] = mixPropertyInnerTemp[property]
            this.mixProperties.push(property)
          }
        })

        // Assembly history settings 'customAttr' & 'customStyle'
        this.batchOptChartInfo = {
          mode: 'batchOpt',
          render: render,
          type: type,
          customAttr: this.changeProperties.customAttr,
          customStyle: this.changeProperties.customStyle
        }
      } else {
        this.batchOptChartInfo = null
      }
    },
    setChangeProperties(propertyInfo) {
      this.changeProperties[propertyInfo.custom][propertyInfo.property] = propertyInfo.value
      // 修改对应视图的参数
      this.curBatchOptComponents.forEach(viewId => {
        const viewInfo = this.canvasViewInfo[viewId]
        viewInfo[propertyInfo.custom][propertyInfo.property] = propertyInfo.value
        useEmitt().emitter.emit('renderChart-' + viewId, viewInfo)
      })
    },
    setBatchChangeBackground(newBackground) {
      this.componentData.forEach(component => {
        if (this.curBatchOptComponents.includes(component.id)) {
          component.commonBackground = deepCopy(newBackground)
        }
      })
    },
    setBatchOptStatus(status) {
      this.batchOptStatus = status
      // Currently selected components
      this.curBatchOptComponents = []
      this.mixProperties = []
      this.mixPropertiesInner = {}
      this.batchOptChartInfo = null
      this.batchOptViews = {}
      this.changeProperties = {
        customStyle: {},
        customAttr: {}
      }
    },
    removeCurBatchComponentWithId(id) {
      for (let index = 0; index < this.curBatchOptComponents.length; index++) {
        const element = this.curBatchOptComponents[index]
        if (element === id) {
          delete this.batchOptViews[id]
          this.curBatchOptComponents.splice(index, 1)
          break
        }
      }
      if (this.curBatchOptComponents.length === 1) {
        const lastViewId = this.curBatchOptComponents[0]
        const viewBaseInfo = this.componentViewsData[lastViewId]
        this.changeProperties.customAttr = JSON.parse(viewBaseInfo.customAttr)
        this.changeProperties.customStyle = JSON.parse(viewBaseInfo.customStyle)
      }
      if (this.curBatchOptComponents.length === 0) {
        this.changeProperties = {
          customStyle: {},
          customAttr: {}
        }
      }
      this.setBatchOptChartInfo()
    },
    removeCurMultiplexingComponentWithId(id) {
      delete this.curMultiplexingComponents[id]
    },
    addCurMultiplexingComponent(componentInfo) {
      if (componentInfo.componentId) {
        this.curMultiplexingComponents[componentInfo.componentId] = componentInfo.component
      }
    },
    initCurMultiplexingComponents() {
      this.curMultiplexingComponents = {}
    },
    addCanvasViewInfo(viewId, viewInfo) {
      this.canvasViewInfo[viewId] = viewInfo
    },
    removeCanvasViewInfo(viewId) {
      delete this.canvasViewInfo[viewId]
    },
    clearLinkageSettingInfo() {
      this.linkageSettingStatus = false
      this.curLinkageView = null
      this.targetLinkageInfo = []
    },
    setNowPanelTrackInfo(trackInfo) {
      this.nowPanelTrackInfo = trackInfo
    },
    setNowPanelJumpInfo(jumpInfo) {
      this.nowPanelJumpInfo = jumpInfo.baseJumpInfoMap
    },
    setNowTargetPanelJumpInfo(jumpInfo) {
      this.nowPanelJumpInfoTargetPanel = jumpInfo.baseJumpInfoVisualizationMap
    },
    // 添加联动 下钻 等过滤组件
    addViewTrackFilter(data) {
      const viewId = data.viewId
      let trackInfo
      if (data.option === 'linkage') {
        trackInfo = this.nowPanelTrackInfo
      } else {
        trackInfo = this.nowPanelJumpInfoTargetPanel
      }
      const preActiveComponentIds = []
      for (let index = 0; index < this.componentData.length; index++) {
        const element = this.componentData[index]
        if (!element.component || element.component !== 'UserView') continue
        const currentFilters = element.linkageFilters || [] // 当前联动filter
        // 联动的视图情况历史条件
        // const currentFilters = []

        const checkQDList = [...data.dimensionList, ...data.quotaList]
        checkQDList.forEach(QDItem => {
          const sourceInfo = viewId + '#' + QDItem.id
          // 获取所有目标联动信息
          const targetInfoList = trackInfo[sourceInfo] || []
          targetInfoList.forEach(targetInfo => {
            const targetInfoArray = targetInfo.split('#')
            const targetViewId = targetInfoArray[0] // 目标视图
            if (element.id === targetViewId) {
              // 如果目标视图 和 当前循环组件id相等 则进行条件增减
              const targetFieldId = targetInfoArray[1] // 目标视图列ID
              const condition = {
                fieldId: targetFieldId,
                operator: 'eq',
                value: [QDItem.value],
                viewIds: [targetViewId],
                sourceViewId: viewId
              }
              let j = currentFilters.length
              while (j--) {
                const filter = currentFilters[j]
                // 兼容性准备 viewIds 只会存放一个值
                if (targetFieldId === filter.fieldId && filter.viewIds.includes(targetViewId)) {
                  currentFilters.splice(j, 1)
                }
              }
              // 不存在该条件 且 条件有效 直接保存该条件
              // !filterExist && vValid && currentFilters.push(condition)
              currentFilters.push(condition)
              preActiveComponentIds.includes(element.id) || preActiveComponentIds.push(element.id)
            }
          })
        })
        element.linkageFilters = currentFilters
        this.componentData[index] = element
      }
      preActiveComponentIds.forEach(viewId => {
        useEmitt().emitter.emit('query-data-' + viewId)
      })
    },
    clearPanelLinkageInfo() {
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          item.linkageFilters.splice(0, item.linkageFilters.length)
          useEmitt().emitter.emit('query-data-' + item.id)
        }
      })
    },
    setTabCollisionActiveId(tabId) {
      this.tabCollisionActiveId = tabId
    },
    setTabMoveInActiveId(tabId) {
      this.tabMoveInActiveId = tabId
    },
    setMousePointShadowMap(mousePoint) {
      this.mousePointShadowMap.mouseX = mousePoint.mouseX
      this.mousePointShadowMap.mouseY = mousePoint.mouseY
      this.mousePointShadowMap.width = mousePoint.width
      this.mousePointShadowMap.height = mousePoint.height
    },
    setTabMoveOutComponentId(componentId) {
      this.tabMoveOutComponentId = componentId
    },
    resetDvInfo() {
      this.dvInfo = {
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        type: null
      }
    },
    setViewDataDetails(viewId, dataInfo) {
      this.canvasViewDataInfo[viewId] = dataInfo
    },
    getViewDataDetails(viewId) {
      return this.canvasViewDataInfo[viewId]
    },
    setLastViewRequestInfo(viewId, viewRequestInfo) {
      this.lastViewRequestInfo[viewId] = viewRequestInfo.requestInfo
    },
    getLastViewRequestInfo(viewId) {
      return this.lastViewRequestInfo[viewId]
    },
    getViewDetails(viewId) {
      return this.canvasViewInfo[viewId]
    },
    updateDvInfoId(newId) {
      if (this.dvInfo) {
        this.dvInfo.id = newId
      }
    },
    createInit(dvType) {
      const optName = dvType === 'dashboard' ? '新建仪表板' : '新建数据大屏'
      this.dvInfo = {
        id: null,
        name: optName,
        pid: -1,
        type: dvType,
        status: 1,
        selfWatermarkStatus: 0
      }
      const canvasStyleDataNew =
        dvType === 'dashboard'
          ? DEFAULT_CANVAS_STYLE_DATA_LIGHT
          : DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK

      this.canvasStyleData = deepCopy(canvasStyleDataNew)
      this.componentData = []
      this.canvasViewInfo = {}
    },
    canvasDataInit() {
      this.canvasViewInfo = {}
      this.componentData = []
      this.dvInfo = {
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        type: null
      }
      this.canvasStyleData = { ...deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK), backgroundColor: null }
    }
  }
})

export const dvMainStoreWithOut = () => {
  return dvMainStore(store)
}
