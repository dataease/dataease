import { defineStore } from 'pinia'
import { store } from '../../index'
import { deepCopy } from '@/utils/utils'
import {
  BASE_VIEW_CONFIG,
  COMMON_BACKGROUND,
  DEFAULT_COLOR_CASE,
  DEFAULT_TITLE_STYLE,
  FILTER_COMMON_STYLE,
  TAB_COMMON_STYLE
} from '@/views/chart/components/editor/util/chart'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/utils/canvasUtils'
import eventBus from '@/utils/eventBus'

export const PANEL_CHART_INFO = {
  chartTitle: DEFAULT_TITLE_STYLE,
  chartColor: DEFAULT_COLOR_CASE,
  chartCommonStyle: COMMON_BACKGROUND,
  filterStyle: FILTER_COMMON_STYLE,
  tabStyle: TAB_COMMON_STYLE
}

export const MOBILE_SETTING = {
  customSetting: false,
  color: '#ffffff',
  imageUrl: null,
  backgroundType: 'image'
}

export const DEFAULT_DASHBOARD_STYLE = {
  mobileSetting: MOBILE_SETTING,
  themeColor: 'light',
  gap: 'yes',
  gapSize: 10,
  resultMode: 'all', // 视图结果显示模式 all 视图 custom 仪表板自定义
  resultCount: 1000 // 视图结果显示条数
}

export const DEFAULT_CANVAS_STYLE_DATA = {
  // 页面全局数据
  themeId: 'system_0',
  width: 1920,
  height: 1080,
  refreshViewEnable: false, // 开启视图刷新（默认关闭）
  refreshViewLoading: true, // 仪表板视图loading提示
  refreshUnit: 'minute', // 仪表板刷新时间带外 默认 分钟
  refreshTime: 5, // 仪表板刷新时间 默认5分钟
  scale: 60,
  scaleWidth: 100,
  scaleHeight: 100,
  backgroundType: 'backgroundColor',
  background: '',
  openCommonStyle: true,
  color: '#fff',
  opacity: 1,
  backgroundColor: '#000',
  fontSize: 14,
  dashboard: DEFAULT_DASHBOARD_STYLE,
  component: PANEL_CHART_INFO
}

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
      canvasStyleData: deepCopy(DEFAULT_CANVAS_STYLE_DATA),
      // 当前展示画布缓存数据
      componentDataCache: null,
      // 当前展示画布视图信息
      componentViewsData: {},
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
      canvasCommonStyleData: DEFAULT_COMMON_CANVAS_STYLE_STRING,
      // 大屏基础信息
      dvInfo: {
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null
      },
      // 视图信息
      canvasViewInfo: {},
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
      // 仪表板组件间隙大小 px
      componentGap: 5,
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
      lastViewRequestInfo: {}
    }
  },
  actions: {
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
      this.curComponent = component
      this.curComponentIndex = index
    },
    setBashMatrixInfo(bashMatrixInfo) {
      this.bashMatrixInfo = bashMatrixInfo
    },

    setShapeStyle({ top, left, width, height, rotate }) {
      if (top) this.curComponent.style.top = Math.round(top)
      if (left) this.curComponent.style.left = Math.round(left)
      if (width) this.curComponent.style.width = Math.round(width)
      if (height) this.curComponent.style.height = Math.round(height)
      if (rotate) this.curComponent.style.rotate = Math.round(rotate)
    },

    setShapeSingleStyle({ key, value }) {
      this.curComponent.style[key] = value
    },

    setComponentData(componentData = []) {
      this.componentData = componentData
    },

    addComponent({ component, index }) {
      if (index !== undefined) {
        this.componentData.splice(index, 0, component)
        this.setCurComponent({ component: component, index: index })
      } else {
        this.componentData.push(component)
        this.setCurComponent({ component: component, index: this.componentData.length - 1 })
      }
      //如果当前的组件是UserView 视图，则想canvasView中增加一项 UserView ID 和componentID保持一致
      if (component.component === 'UserView') {
        const newView = {
          ...deepCopy(BASE_VIEW_CONFIG),
          id: component.id,
          type: component.innerType
        }
        this.canvasViewInfo[component.id] = newView
      }
    },
    setLinkageInfo(targetLinkageInfo) {
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

    deleteComponent(index?) {
      if (index === undefined) {
        index = this.curComponentIndex
      }

      if (index == this.curComponentIndex) {
        this.curComponentIndex = null
        this.curComponent = null
      }

      if (/\d/.test(index)) {
        this.componentData.splice(index, 1)
      }
    },
    updateCurDvInfo(dvInfo) {
      this.dvInfo = dvInfo
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
          const newList = item.linkageFilters.filter(linkage => linkage.sourceViewId !== viewId)
          item.linkageFilters.splice(0, item.linkageFilters.length)
          // 重新push 可保证数组指针不变 可以watch到
          if (newList.length > 0) {
            newList.forEach(newLinkage => {
              item.linkageFilters.push(newLinkage)
            })
          }
        }
      })
      eventBus.emit('clear_panel_linkage', { viewId: viewId })
    },
    addCurBatchComponent(id) {
      if (id) {
        this.curBatchOptComponents.push(id)
        // get view base info
        const viewBaseInfo = this.componentViewsData[id]
        // get properties
        const viewConfig = this.allViewRender.filter(
          item => item.render === viewBaseInfo.render && item.value === viewBaseInfo.type
        )
        if (viewConfig && viewConfig.length > 0) {
          if (this.curBatchOptComponents.length === 1) {
            this.changeProperties.customAttr = JSON.parse(viewBaseInfo.customAttr)
            this.changeProperties.customStyle = JSON.parse(viewBaseInfo.customStyle)
          }
          this.batchOptViews[id] = viewConfig[0]
          this.setBatchOptChartInfo()
        }
      }
    },
    setBatchOptChartInfo() {
      let render = null
      let type = null
      let allTypes = ''
      let isPlugin = null
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

          if (render && render !== this.batchOptViews[key].render) {
            render = 'mix'
          } else {
            render = this.batchOptViews[key].render
          }

          allTypes = allTypes + '-' + this.batchOptViews[key].value
          if (type && type !== this.batchOptViews[key].value) {
            type = 'mix'
          } else {
            type = this.batchOptViews[key].value
          }

          if (isPlugin && isPlugin !== this.batchOptViews[key].isPlugin) {
            isPlugin = 'mix'
          } else {
            isPlugin = this.batchOptViews[key].isPlugin
          }
        }
        mixPropertiesTemp.forEach(property => {
          if (mixPropertyInnerTemp[property] && mixPropertyInnerTemp[property].length) {
            this.mixPropertiesInner[property] = mixPropertyInnerTemp[property]
            this.mixProperties.push(property)
          }
        })

        if (type && type === 'mix') {
          type = type + '-' + allTypes
        }
        // Assembly history settings 'customAttr' & 'customStyle'
        this.batchOptChartInfo = {
          mode: 'batchOpt',
          render: render,
          type: type,
          isPlugin: isPlugin,
          customAttr: this.changeProperties.customAttr,
          customStyle: this.changeProperties.customStyle
        }
      } else {
        this.batchOptChartInfo = null
      }
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
    addCurMultiplexingComponent({ component, componentId }) {
      if (componentId) {
        if (component.type === 'custom-button' && component.serviceName === 'buttonSureWidget') {
          const copyComponent = deepCopy(component)
          copyComponent.options.attrs.customRange = false
          copyComponent.options.attrs.filterIds = []
          this.curMultiplexingComponents[componentId] = copyComponent
          return
        }
        this.curMultiplexingComponents[componentId] = component
      }
    }
  }
})

export const dvMainStoreWithOut = () => {
  return dvMainStore(store)
}
