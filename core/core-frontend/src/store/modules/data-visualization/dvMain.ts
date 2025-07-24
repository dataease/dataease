import { defineStore } from 'pinia'
import { store } from '../../index'
import { deepCopy } from '@/utils/utils'
import {
  BASE_VIEW_CONFIG,
  DEFAULT_INDICATOR_NAME_STYLE,
  DEFAULT_INDICATOR_STYLE,
  SENIOR_STYLE_SETTING_LIGHT
} from '@/views/chart/components/editor/util/chart'
import {
  DEFAULT_CANVAS_STYLE_DATA_DARK,
  DEFAULT_CANVAS_STYLE_DATA_LIGHT,
  DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK
} from '@/views/chart/components/editor/util/dataVisualization'
import { useEmitt } from '@/hooks/web/useEmitt'
import chartViewManager from '@/views/chart/components/js/panel'
import {
  COMMON_COMPONENT_BACKGROUND_DARK,
  COMMON_COMPONENT_BACKGROUND_LIGHT,
  defaultStyleValue,
  findBaseDeFaultAttr
} from '@/custom-component/component-list'
import { get, set } from 'lodash-es'
import { viewFieldTimeTrans } from '@/utils/viewUtils'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { filterEnumParams } from '@/utils/componentUtils'
const { t } = useI18n()

export const dvMainStore = defineStore('dataVisualization', {
  state: () => {
    return {
      canvasAttachInfo: {}, // 仪表板附加信息
      fullscreenFlag: false, // 全屏启用标识
      staticResourcePath: '/static-resource/',
      canvasCollapse: {
        defaultSide: false,
        realTimeComponent: false,
        canvas: false,
        componentProp: false,
        chartAreaCollapse: false,
        datasetAreaCollapse: false
      },
      canvasState: {
        curPointArea: 'base' // 当前焦点所在画布区域  base 主画布区域 hidden 隐藏画布区域
      },
      embeddedCallBack: 'no', // 嵌入模式是否允许反馈参数
      editMode: 'preview', // 编辑器模式 edit preview
      mobileInPc: false,
      inMobile: false,
      firstLoadMap: [],
      canvasStyleData: { ...deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK), backgroundColor: null },
      appData: null, //应用信息
      // 当前展示画布缓存数据
      componentDataCache: null,
      // PC布局画布组件数据
      pcComponentData: [],
      // 移动端布局画布组件数据
      mobileComponentData: [],
      isInEditor: false, // 是否在编辑器中，用于判断复制、粘贴组件时是否生效，如果在编辑器外，则无视这些操作
      componentData: [], // 画布组件数据
      curComponent: null,
      curTabName: null, // 当前选中的tabName 大屏图层区域使用
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
        dataState: null,
        optType: null,
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        watermarkInfo: {},
        type: null,
        mobileLayout: false
      },
      // 图表信息
      canvasViewInfo: {},
      // 图表展示数据信息
      canvasViewDataInfo: {},
      // 图表实例信息
      canvasViewInstanceInfo: {},
      // 图表原始数据，未被联动、查询、下钻过滤
      canvasViewOriginDataInfo: {},
      // 图表最新请求信息
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
      // 当前仪表板的外部参数信息 兼容多仪表板嵌入式div
      nowPanelOuterParamsInfoV2: {},
      // 当前仪表板的外部参数基础信息 兼容多仪表板嵌入式div
      nowPanelOuterParamsBaseInfoV2: {},
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
        x: 72,
        y: 36
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
      // 图表是否编辑记录
      panelViewEditInfo: {},
      // 仪表板图表明细
      panelViewDetailsInfo: {},
      // 批量操作组件类型 单一类型 or mix 混合类型,如果类型是UserView 走图表批量逻辑，
      // mix 或者其他目前走 CommonAttr 公共属性处理逻辑
      batchOptComponentType: null,
      // panel edit batch operation status
      batchOptStatus: false,
      // 隐藏组件打开状态
      hiddenListStatus: false,
      // 最后隐藏组件
      lastHiddenComponent: [],
      // Currently selected components
      curBatchOptComponents: [],
      // Currently selected Multiplexing components
      curMultiplexingComponents: {},
      mixProperties: [],
      mixPropertiesInner: {},
      batchOptComponentInfo: null,
      batchOptComponents: {},
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
      multiplexingStyleAdapt: true, //复用样式跟随主题
      mainScrollTop: 0, //主画布运动量
      isIframe: false, // 当前是否在iframe中
      isPopWindow: false, // 当前是否在iframe弹框中
      viewPageInfo: {} //表格分页信息
    }
  },
  actions: {
    setLastHiddenComponent(value?) {
      if (value) {
        this.lastHiddenComponent = [value]
      } else if (this.lastHiddenComponent.length > 0) {
        this.lastHiddenComponent = []
      }
    },
    setIframeFlag(value) {
      this.isIframe = value
    },
    setIsPopWindow(value) {
      this.isPopWindow = value
    },
    setCanvasAttachInfo(value) {
      this.canvasAttachInfo = value
    },
    setEmbeddedCallBack(value) {
      this.embeddedCallBack = value
    },
    setPublicLinkStatus(value) {
      this.publicLinkStatus = value
    },
    setFirstLoadMap(value) {
      this.firstLoadMap = value
    },
    setDataPrepareState(value) {
      this.dataPrepareState = value
    },
    setBaseCellInfo(value) {
      this.baseCellInfo = value
    },
    aceSetCanvasData(value) {
      this.canvasStyleData = value
    },
    setInMobile(value) {
      this.inMobile = value
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
    setMobileInPc(mobileInPc) {
      this.mobileInPc = mobileInPc
    },

    setInEditorStatus(status) {
      this.isInEditor = status
    },

    setCanvasStyle(style) {
      style.component['seniorStyleSetting'] =
        style.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)
      this.canvasStyleData = style
    },
    setCanvasStyleScale(value) {
      this.canvasStyleData.scale = value
    },
    setCanvasViewInfo(canvasViewInfo) {
      this.canvasViewInfo = canvasViewInfo
    },
    getAppDataInfo() {
      return this.appData
    },
    setAppDataInfo(appDataInfo) {
      this.appData = appDataInfo
    },
    setCurComponentMobileConfig(component) {
      this.curComponent = component
    },
    setCurTabName(val) {
      this.curTabName = val
    },
    setCurComponent({ component, index }) {
      this.setCurTabName(null)
      this.setHiddenListStatus(false)
      if (!component && this.curComponent) {
        this.curComponent['editing'] = false
        this.curComponent['resizing'] = false
        this.curComponent['dragging'] = false
        this.curComponent['canvasActive'] = false
        // 如果当前组件不在主画布中 对应的分组的canvasActive 也要设置为false
        if (this.curComponent.canvasId !== 'canvas-main') {
          this.componentData.forEach(componentItem => {
            if (this.curComponent.canvasId.includes(componentItem.id)) {
              componentItem['canvasActive'] = false
            }
          })
        }
      }
      if (!this.curComponent) {
        this.componentData.forEach(componentItem => {
          componentItem['canvasActive'] = false
        })
      }
      if (component) {
        this.componentData.forEach(componentItem => {
          if (!component.canvasId.includes(componentItem.id)) {
            componentItem['canvasActive'] = false
          }
        })
        // Is the current component in editing status
        if (!this.curComponent) {
          component['editing'] = false
        } else if (component.id !== this.curComponent.id) {
          component['editing'] = false
        }
      }
      this.curComponent = component
      this.curComponentIndex = index
      // 更新当前活动区域
      if (this.curComponent && this.curComponent['category']) {
        // 如果是图片 且图片配置了切换显示区
        if (
          this.curComponent.component !== 'Picture' ||
          (this.curComponent.component === 'Picture' &&
            (!this.curComponent.events?.checked ||
              this.curComponent.events?.type !== 'displayChange'))
        ) {
          this.canvasState['curPointArea'] = this.curComponent['category']
        }
      }
      // 移动端通知
      if (this.mobileInPc) {
        useEmitt().emitter.emit('curComponentChange', {
          type: 'curComponentChange',
          value: this.curComponent ? JSON.parse(JSON.stringify(this.curComponent)) : null
        })
      }
    },
    setBashMatrixInfo(bashMatrixInfo) {
      this.bashMatrixInfo = bashMatrixInfo
    },

    setShapeStyle(
      { top, left, width, height, rotate },
      areaDataComponents = [],
      moveType = 'move',
      baseGroupComponentsRadio = {}
    ) {
      if (this.curComponent.component === 'GroupArea' && areaDataComponents.length > 0) {
        const topOffset = top - this.curComponent.style.top
        const leftOffset = left - this.curComponent.style.left
        const widthOffset = width - this.curComponent.style.width
        const heightOffset = height - this.curComponent.style.height
        if (moveType === 'move') {
          areaDataComponents.forEach(component => {
            component.style.top = component.style.top + topOffset
            component.style.left = component.style.left + leftOffset
            component.style.width = component.style.width + widthOffset
            component.style.height = component.style.height + heightOffset
          })
        } else {
          areaDataComponents.forEach(component => {
            const componentRadio = baseGroupComponentsRadio[component.id]
            if (componentRadio) {
              component.style.top = top + height * componentRadio.topRadio
              component.style.left = left + width * componentRadio.leftRadio
              component.style.width = width * componentRadio.widthRadio
              component.style.height = height * componentRadio.heightRadio
            }
          })
        }
      }
      if (this.dvInfo.type === 'dashboard') {
        if (top) this.curComponent.style.top = top < 0 ? 0 : Math.round(top)
        if (left) this.curComponent.style.left = left < 0 ? 0 : Math.round(left)
        if (width) this.curComponent.style.width = Math.round(width)
        if (height) this.curComponent.style.height = Math.round(height)
        if (rotate) this.curComponent.style.rotate = Math.round(rotate)
      } else {
        if (top) this.curComponent.style.top = top
        if (left) this.curComponent.style.left = left
        if (width) this.curComponent.style.width = width
        if (height) this.curComponent.style.height = height
        if (rotate) this.curComponent.style.rotate = rotate
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
      //组件组内部可能还有多个图表
      this.updateCopyCanvasView(idMap, canvasViewInfoPre)
    },
    updateCopyCanvasView(idMap, canvasViewInfoPre = this.canvasViewInfo) {
      // eslint-disable-next-line @typescript-eslint/no-this-alias
      const _this = this
      //组件组内部可能还有多个图表
      if (idMap) {
        Object.keys(idMap).forEach(function (oldComponentId) {
          if (canvasViewInfoPre[oldComponentId]) {
            const newComponentId = idMap[oldComponentId]
            const newView = {
              ...deepCopy(canvasViewInfoPre[oldComponentId]),
              id: newComponentId,
              linkageActive: false,
              jumpActive: false
            }
            newView['customAttrMobile'] = null
            newView['customStyleMobile'] = null
            _this.canvasViewInfo[newComponentId] = newView
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
      const currentFont = useAppearanceStoreWithOut().fontList.find(ele => ele.isDefault)
      //如果当前的组件是UserView 图表，则想canvasView中增加一项 UserView ID 和componentID保持一致
      if (component.component === 'UserView') {
        const defaultConfig = JSON.parse(JSON.stringify(BASE_VIEW_CONFIG))
        if (component.innerType === 'bar-range') {
          defaultConfig.customStyle.xAxis.axisLine.show = false
          defaultConfig.customStyle.xAxis.splitLine.show = true
          defaultConfig.customStyle.yAxis.axisLine.show = true
          defaultConfig.customStyle.yAxis.splitLine.show = false
        }
        let newView = {
          ...defaultConfig,
          id: component.id,
          type: component.innerType,
          render: component.render,
          isPlugin: component.isPlugin,
          plugin: {
            isPlugin: component.isPlugin,
            staticMap: component.staticMap
          }
        } as unknown as ChartObj
        // 处理配置项默认值，不同图表的同一配置项默认值不同
        const chartViewInstance = chartViewManager.getChartView(newView.render, newView.type)
        if (chartViewInstance) {
          newView = chartViewInstance.setupDefaultOptions(newView)
          newView['title'] = component.name
        }
        currentFont && (newView.customStyle.text.fontFamily = currentFont.name)
        this.canvasViewInfo[component.id] = newView
      }
      if (component.component === 'VQuery') {
        const { color, titleColor, labelColor, borderColor, bgColor, text, titleLayout, layout } =
          this.canvasStyleData.component.filterStyle || {}
        const newView = {
          ...JSON.parse(JSON.stringify(BASE_VIEW_CONFIG)),
          id: component.id,
          title: t('visualization.query_component'),
          type: component.innerType,
          customStyle: {
            component: {
              show: true,
              color,
              titleShow: false,
              text,
              textColorShow: false,
              labelColor,
              borderColor,
              title: '',
              borderWidth: 1,
              bgColor,
              titleColor,
              titleLayout,
              layout,
              btnList: ['sure'],
              fontSize: '14',
              labelShow: true,
              fontWeight: '',
              fontStyle: '',
              fontSizeBtn: '14',
              fontWeightBtn: '',
              fontStyleBtn: '',
              queryConditionWidth: 227,
              nameboxSpacing: 8,
              placeholderShow: true,
              placeholderSize: 14,
              queryConditionSpacing: 16,
              queryConditionHeight: 32,
              labelColorBtn: '#ffffff',
              btnColor: '#3370ff'
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
    setFullscreenFlag(val) {
      this.fullscreenFlag = val
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

    deleteComponentById(componentId, componentData = this.componentData, deep = false) {
      if (componentId) {
        const indexResult = []
        componentData.forEach((component, index) => {
          if (componentId === component.id) {
            indexResult.push(index)
          } else if (deep && component.component === 'Group') {
            this.deleteComponentById(componentId, component.propValue || [])
          } else if (deep && component.innerType === 'DeTabs') {
            component.propValue.forEach(ele => {
              this.deleteComponentById(componentId, ele.componentData || [])
            })
          }
        })
        indexResult.forEach(indexItem => {
          this.deleteComponent(indexItem, componentData)
        })
      }
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
    clearTargetViewLinkage(viewId, item) {
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
    },
    // 清除相同sourceViewId 的 联动条件
    clearViewLinkage(viewId) {
      this.componentData.forEach(item => {
        if (item.component === 'UserView' && item.innerType != 'VQuery') {
          this.clearTargetViewLinkage(viewId, item)
        } else if (item.component === 'Group') {
          item.propValue.forEach(groupItem => {
            this.clearTargetViewLinkage(viewId, groupItem)
          })
        } else if (item.component === 'DeTabs') {
          item.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              this.clearTargetViewLinkage(viewId, tabComponent)
            })
          })
        }
      })
    },
    addCurBatchComponent(componentInfo) {
      const id = componentInfo.id
      const componentType = componentInfo.component
      this.curBatchOptComponents.push(id)
      // 图表批量操作
      if (componentType === 'UserView') {
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
            // 补充历史指标卡缺失属性
            this.changeProperties.customAttr['indicator'] =
              this.changeProperties.customAttr.indicator || deepCopy(DEFAULT_INDICATOR_STYLE)
            this.changeProperties.customAttr['indicatorName'] =
              this.changeProperties.customAttr.indicatorName ||
              deepCopy(DEFAULT_INDICATOR_NAME_STYLE)
          }
          this.batchOptComponents[id] = {
            properties: chartViewInstance.properties,
            propertyInner: chartViewInstance.propertyInner,
            value: chartViewInstance.name,
            componentType: componentType
          }
          this.setBatchOptComponentInfo()
        }
      } else {
        // 其他组件批量操作 do other
        this.batchOptComponents[id] = findBaseDeFaultAttr(componentType)
        this.setBatchOptComponentInfo()
      }
      if (this.batchOptComponentType !== 'UserView') {
        this.batchOptComponentInfo = {
          collapseName: 'background',
          commonBackground: deepCopy(
            this.curOriginThemes === 'light'
              ? COMMON_COMPONENT_BACKGROUND_LIGHT
              : COMMON_COMPONENT_BACKGROUND_DARK
          ),
          style: {}
        }

        this.mixPropertiesInner['common-style']?.forEach(styleKey => {
          this.batchOptComponentInfo['style'][styleKey] = defaultStyleValue[styleKey]
        })
      }
    },
    setBatchOptComponentInfo() {
      if (this.batchOptComponents && JSON.stringify(this.batchOptComponents) !== '{}') {
        const batchAttachInfo = {
          render: null,
          type: null
        }
        this.mixPropertiesAdaptor(batchAttachInfo)
        // 根据batchOptComponentType类型判断 组装batchOptComponentInfo信息
        if (this.batchOptComponentType === 'UserView') {
          // Assembly history settings 'customAttr' & 'customStyle'
          this.batchOptComponentInfo = {
            ...deepCopy(BASE_VIEW_CONFIG),
            mode: 'batchOpt',
            render: batchAttachInfo.render,
            type: batchAttachInfo.type,
            commonBackground: deepCopy(
              this.curOriginThemes === 'light'
                ? COMMON_COMPONENT_BACKGROUND_LIGHT
                : COMMON_COMPONENT_BACKGROUND_DARK
            ),
            customAttr: this.changeProperties.customAttr,
            customStyle: this.changeProperties.customStyle
          }
        } else {
          this.batchOptComponentInfo = {
            collapseName: 'background',
            commonBackground: deepCopy(
              this.curOriginThemes === 'light'
                ? COMMON_COMPONENT_BACKGROUND_LIGHT
                : COMMON_COMPONENT_BACKGROUND_DARK
            ),
            style: {}
          }
          this.mixPropertiesInner['common-style']?.forEach(styleKey => {
            this.batchOptComponentInfo['style'][styleKey] = defaultStyleValue[styleKey]
          })
        }
      } else {
        this.batchOptComponentInfo = null
      }
    },
    mixPropertiesAdaptor(batchAttachInfo) {
      this.mixProperties = []
      this.mixPropertiesInner = {}
      let mixPropertiesTemp = []
      let mixPropertyInnerTemp = {}
      let componentType = null

      for (const key in this.batchOptComponents) {
        const componentInfo = this.batchOptComponents[key]
        if (componentType) {
          componentType =
            componentType === componentInfo.componentType ? componentInfo.componentType : 'mix'
        } else {
          componentType = componentInfo.componentType
        }
        if (mixPropertiesTemp.length > 0) {
          // If it exists , taking the intersection
          mixPropertiesTemp = mixPropertiesTemp.filter(
            property => componentInfo.properties.indexOf(property) > -1
          )
          // 根据当前的mixPropertiesTemp 再对 mixPropertyInnerTemp 进行过滤
          mixPropertiesTemp.forEach(propertyInnerItem => {
            if (
              mixPropertyInnerTemp[propertyInnerItem] &&
              componentInfo.propertyInner[propertyInnerItem]
            ) {
              mixPropertyInnerTemp[propertyInnerItem] = mixPropertyInnerTemp[
                propertyInnerItem
              ].filter(
                propertyInnerItemValue =>
                  componentInfo.propertyInner[propertyInnerItem].indexOf(propertyInnerItemValue) >
                  -1
              )
            }
          })
        } else {
          // If it doesn't exist, assignment directly
          mixPropertiesTemp = deepCopy(componentInfo.properties)
          mixPropertyInnerTemp = deepCopy(componentInfo.propertyInner)
        }
        batchAttachInfo.type =
          batchAttachInfo.type === null || batchAttachInfo.type === componentInfo.value
            ? componentInfo.value
            : 'mix'
      }
      mixPropertiesTemp.forEach(property => {
        if (mixPropertyInnerTemp[property]) {
          this.mixPropertiesInner[property] = mixPropertyInnerTemp[property]
          this.mixProperties.push(property)
        }
      })
      this.batchOptComponentType = componentType
    },
    setChangeProperties(propertyInfo) {
      if (this.batchOptComponentType === 'UserView') {
        if (propertyInfo.subProp) {
          const subValue = get(propertyInfo.value, propertyInfo.subProp)
          const target = this.changeProperties[propertyInfo.custom][propertyInfo.property]
          set(target, propertyInfo.subProp, subValue)
        } else {
          this.changeProperties[propertyInfo.custom][propertyInfo.property] = propertyInfo.value
        }
        // 修改对应图表的参数
        this.curBatchOptComponents.forEach(viewId => {
          const viewInfo = this.canvasViewInfo[viewId]
          //针对双轴图chart-mix
          if (
            viewInfo.type.includes('chart-mix') &&
            propertyInfo.property === 'basicStyle' &&
            propertyInfo.subProp
          ) {
            const subValue = get(propertyInfo.value, propertyInfo.subProp)
            const target = viewInfo[propertyInfo.custom][propertyInfo.property]
            set(target, propertyInfo.subProp, subValue)
            switch (propertyInfo.subProp) {
              case 'alpha':
                const subAlpha = get(propertyInfo.value, 'subAlpha')
                set(target, 'subAlpha', subAlpha)
                break
              case 'colorScheme':
                const subColorScheme = get(propertyInfo.value, 'subColorScheme')
                set(target, 'subColorScheme', subColorScheme)
                break
              case 'seriesColor':
                const subSeriesColor = get(propertyInfo.value, 'subSeriesColor')
                set(target, 'subSeriesColor', subSeriesColor)
                break
              case 'colors':
                const subColors = get(propertyInfo.value, 'subColors')
                set(target, 'subColors', subColors)
                break
              case 'lineWidth':
                const leftLineWidth = get(propertyInfo.value, 'leftLineWidth')
                set(target, 'leftLineWidth', leftLineWidth)
                break
              case 'lineSymbol':
                const leftLineSymbol = get(propertyInfo.value, 'leftLineSymbol')
                set(target, 'leftLineSymbol', leftLineSymbol)
                break
              case 'lineSymbolSize':
                const leftLineSymbolSize = get(propertyInfo.value, 'leftLineSymbolSize')
                set(target, 'leftLineSymbolSize', leftLineSymbolSize)
                break
              case 'lineSmooth':
                const leftLineSmooth = get(propertyInfo.value, 'leftLineSmooth')
                set(target, 'leftLineSmooth', leftLineSmooth)
                break
            }
          } else if (propertyInfo.subProp) {
            const subValue = get(propertyInfo.value, propertyInfo.subProp)
            const target = viewInfo[propertyInfo.custom][propertyInfo.property]
            set(target, propertyInfo.subProp, subValue)
          } else {
            viewInfo[propertyInfo.custom][propertyInfo.property] = propertyInfo.value
          }
          if (['tablePageMode', 'tablePageSize'].includes(propertyInfo.subProp)) {
            useEmitt().emitter.emit('calcData-' + viewId, viewInfo)
          } else {
            setTimeout(() => {
              useEmitt().emitter.emit('renderChart-' + viewId, viewInfo)
            }, 0)
          }
        })
      } else {
        this.componentData.forEach(component => {
          if (this.curBatchOptComponents.includes(component.id)) {
            if (propertyInfo.custom === 'commonBackground') {
              component.commonBackground = deepCopy(this.batchOptComponentInfo.commonBackground)
            } else if (propertyInfo.custom === 'style' && component.style[propertyInfo.property]) {
              component.style[propertyInfo.property] = propertyInfo.value
            }
          }
          if (component.component === 'Group') {
            component.propValue.forEach(groupItem => {
              if (this.curBatchOptComponents.includes(groupItem.id)) {
                if (propertyInfo.custom === 'commonBackground') {
                  groupItem.commonBackground = deepCopy(this.batchOptComponentInfo.commonBackground)
                } else if (
                  propertyInfo.custom === 'style' &&
                  groupItem.style[propertyInfo.property]
                ) {
                  groupItem.style[propertyInfo.property] = propertyInfo.value
                }
              }
            })
          } else if (component.component === 'DeTabs') {
            component.propValue.forEach(tabItem => {
              tabItem.componentData.forEach(tabComponent => {
                if (this.curBatchOptComponents.includes(tabComponent.id)) {
                  if (propertyInfo.custom === 'commonBackground') {
                    tabComponent.commonBackground = deepCopy(
                      this.batchOptComponentInfo.commonBackground
                    )
                  } else if (
                    propertyInfo.custom === 'style' &&
                    tabComponent.style[propertyInfo.property]
                  ) {
                    tabComponent.style[propertyInfo.property] = propertyInfo.value
                  }
                }
              })
            })
          }
        })
      }
    },
    setBatchChangeBackground(newBackground) {
      this.componentData.forEach(component => {
        if (component.component === 'UserView') {
          if (this.curBatchOptComponents.includes(component.id)) {
            component.commonBackground = deepCopy(newBackground)
          }
        } else if (component.component === 'Group') {
          component.propValue.forEach(groupItem => {
            if (this.curBatchOptComponents.includes(groupItem.id)) {
              groupItem.commonBackground = deepCopy(newBackground)
            }
          })
        } else if (component.component === 'DeTabs') {
          component.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              if (this.curBatchOptComponents.includes(tabComponent.id)) {
                tabComponent.commonBackground = deepCopy(newBackground)
              }
            })
          })
        }
      })
    },
    setBatchOptStatus(status) {
      this.batchOptStatus = status
      // Currently selected components
      this.curBatchOptComponents = []
      this.mixProperties = []
      this.mixPropertiesInner = {}
      this.batchOptComponentType = null
      this.batchOptComponentInfo = null
      this.batchOptComponents = {}
      this.componentViewsData = {}
      this.changeProperties = {
        customStyle: {},
        customAttr: {}
      }
    },
    setHiddenListStatus(status?) {
      if (status != undefined) {
        this.hiddenListStatus = !!status
      } else {
        this.hiddenListStatus = !this.hiddenListStatus
      }
      if (this.dvInfo.type === 'dashboard') {
        this.setBatchOptStatus(false)
      }
    },
    removeCurBatchComponentWithId(id) {
      for (let index = 0; index < this.curBatchOptComponents.length; index++) {
        const element = this.curBatchOptComponents[index]
        if (element === id) {
          delete this.batchOptComponents[id]
          this.curBatchOptComponents.splice(index, 1)
          break
        }
      }
      if (
        this.curBatchOptComponents.length === 1 &&
        this.canvasViewInfo &&
        this.canvasViewInfo[this.curBatchOptComponents[0]]
      ) {
        const lastViewId = this.curBatchOptComponents[0]
        const viewBaseInfo = this.canvasViewInfo[lastViewId]
        this.changeProperties.customAttr = viewBaseInfo.customAttr
        this.changeProperties.customStyle = viewBaseInfo.customStyle
      }
      if (this.curBatchOptComponents.length === 0) {
        this.changeProperties = {
          customStyle: {},
          customAttr: {}
        }
      }
      this.setBatchOptComponentInfo()
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
    setNowPanelJumpInfoInner(jumpInfo) {
      this.nowPanelJumpInfo = jumpInfo
    },
    setNowTargetPanelJumpInfo(jumpInfo) {
      this.nowPanelJumpInfoTargetPanel = jumpInfo.baseJumpInfoVisualizationMap
    },
    setNowPanelOuterParamsInfoV2(outerParamsInfo, dvId = this.dvInfo.id) {
      this.nowPanelOuterParamsInfoV2[dvId] = outerParamsInfo.outerParamsInfoMap
      this.nowPanelOuterParamsBaseInfoV2[dvId] = outerParamsInfo.outerParamsInfoBaseMap
    },
    // 添加联动 下钻 等查询组件
    addViewTrackFilter(data) {
      const viewId = data.viewId
      let trackInfo
      if (data.option === 'linkage') {
        // 维度日期类型转换
        viewFieldTimeTrans(this.canvasViewDataInfo[viewId], data)
        trackInfo = this.nowPanelTrackInfo
      } else {
        trackInfo = this.nowPanelJumpInfoTargetPanel
      }
      const preActiveComponentIds = []
      const checkQDList = [...data.dimensionList, ...data.quotaList]
      const customFilterInfo = data.customFilter
      for (let indexOuter = 0; indexOuter < this.componentData.length; indexOuter++) {
        const element = this.componentData[indexOuter]
        if (element.id !== viewId) {
          if (['UserView', 'VQuery'].includes(element.component)) {
            this.trackFilterCursor(
              element,
              checkQDList,
              trackInfo,
              preActiveComponentIds,
              viewId,
              customFilterInfo
            )
            this.componentData[indexOuter] = element
          } else if (element.component === 'Group') {
            element.propValue?.forEach((groupItem, index) => {
              this.trackFilterCursor(
                groupItem,
                checkQDList,
                trackInfo,
                preActiveComponentIds,
                viewId,
                customFilterInfo
              )
              element.propValue[index] = groupItem
            })
          } else if (element.component === 'DeTabs') {
            element.propValue?.forEach(tabItem => {
              tabItem.componentData.forEach((tabComponent, index) => {
                this.trackFilterCursor(
                  tabComponent,
                  checkQDList,
                  trackInfo,
                  preActiveComponentIds,
                  viewId,
                  customFilterInfo
                )
                tabItem.componentData[index] = tabComponent
              })
            })
          }
        }
      }
      preActiveComponentIds.forEach(viewId => {
        useEmitt().emitter.emit('query-data-' + viewId)
      })
    },
    addWebParamsFilter(params, curComponentData = this.componentData) {
      if (params) {
        for (let index = 0; index < curComponentData.length; index++) {
          const element = curComponentData[index]
          if (['UserView'].includes(element.component)) {
            this.trackWebFilterCursor(element, params)
            this.componentData[index] = element
          } else if (element.component === 'Group') {
            element.propValue?.forEach((groupItem, index) => {
              this.trackWebFilterCursor(groupItem, params)
              element.propValue[index] = groupItem
            })
          } else if (element.component === 'DeTabs') {
            element.propValue?.forEach(tabItem => {
              tabItem.componentData.forEach((tabComponent, index) => {
                this.trackWebFilterCursor(tabComponent, params)
                tabItem.componentData[index] = tabComponent
              })
            })
          }
        }
      }
    },
    // 添加外部参数的过滤条件
    addOuterParamsFilter(
      paramsPre,
      curComponentData = this.componentData,
      source = 'inner',
      dvId = this.dvInfo.id
    ) {
      // params 结构 {key1:value1,key2:value2}
      const params = {}
      const paramsVersion = (paramsPre && paramsPre['outerParamsVersion']) || 'v1'
      if (this.nowPanelOuterParamsBaseInfoV2[dvId]) {
        let errorCount = 0
        let errorMes = ''
        Object.keys(this.nowPanelOuterParamsBaseInfoV2[dvId]).forEach(key => {
          const targetInfo = this.nowPanelOuterParamsBaseInfoV2[dvId][key]
          const userParams = paramsPre ? paramsPre[key] : null
          const userParamsIsNull = !userParams || userParams.length === 0
          if (targetInfo.required && userParamsIsNull) {
            // 要求用户必填 但是用户没有输入参数
            errorCount++
            errorMes = errorMes + '[' + key + ']'
          } else if (
            userParamsIsNull &&
            targetInfo.enabledDefault &&
            targetInfo.defaultValue &&
            targetInfo.defaultValue.length > 0
          ) {
            // 非必填时 用户没有填写参数 但是启用默认值且有预设默认值时
            params[key] = JSON.parse(targetInfo.defaultValue)
          } else if (!userParamsIsNull) {
            params[key] = paramsPre[key]
          }
        })
        if (errorCount > 0) {
          ElMessage.error('参数' + errorMes + '不能为空')
          throw new Error('参数' + errorMes + '不能为空')
        }
      } else {
        return
      }

      if (params) {
        const preActiveComponentIds = []
        const trackInfo = this.nowPanelOuterParamsInfoV2[dvId]
        for (let index = 0; index < curComponentData.length; index++) {
          const element = curComponentData[index]
          if (['UserView', 'VQuery'].includes(element.component)) {
            this.trackOuterFilterCursor(
              element,
              params,
              preActiveComponentIds,
              trackInfo,
              source,
              paramsVersion
            )
            this.componentData[index] = element
          } else if (element.component === 'Group') {
            element.propValue?.forEach((groupItem, index) => {
              this.trackOuterFilterCursor(
                groupItem,
                params,
                preActiveComponentIds,
                trackInfo,
                source,
                paramsVersion
              )
              element.propValue[index] = groupItem
            })
          } else if (element.component === 'DeTabs') {
            element.propValue?.forEach(tabItem => {
              tabItem.componentData.forEach((tabComponent, index) => {
                this.trackOuterFilterCursor(
                  tabComponent,
                  params,
                  preActiveComponentIds,
                  trackInfo,
                  source,
                  paramsVersion
                )
                tabItem.componentData[index] = tabComponent
              })
            })
          }
        }
      }
    },
    trackWebFilterCursor(element, params) {
      if (params[element.id]) {
        element['webParamsFilters'] = params[element.id]
        useEmitt().emitter.emit('query-data-' + element.id)
      }
    },
    trackOuterFilterCursor(
      element,
      params,
      preActiveComponentIds,
      trackInfo,
      source,
      outerParamsVersion = 'v1'
    ) {
      // 弹窗区域禁用时 在弹窗区域的组件不生效
      if (
        !['UserView', 'VQuery'].includes(element.component) ||
        (element.category === 'hidden' && !this.canvasStyleData.popupAvailable)
      ) {
        return
      }
      const currentFilters = [] // 外部参数信息
      // 外部参数 可能会包含多个参数
      Object.keys(params).forEach(function (sourceInfo) {
        // 获取外部参数的值 sourceInfo 是外部参数名称 支持数组传入
        let operatorV2, paramValue, paramValueStr, parmaValueSource
        if (outerParamsVersion === 'v2') {
          operatorV2 = params[sourceInfo].operator
          paramValue = params[sourceInfo].value
          paramValueStr = params[sourceInfo].value
          parmaValueSource = params[sourceInfo].value
        } else {
          paramValue = params[sourceInfo]
          paramValueStr = params[sourceInfo]
          parmaValueSource = params[sourceInfo]
        }
        let operator = 'in'
        if (paramValue && !Array.isArray(paramValue)) {
          paramValue = [paramValue]
          operator = 'eq'
        } else if (paramValue && Array.isArray(paramValue)) {
          paramValueStr = ''
          paramValue.forEach((innerValue, index) => {
            if (index === 0) {
              paramValueStr = innerValue
            } else {
              paramValueStr = paramValueStr + ',' + innerValue
            }
          })
        }
        // 获取所有目标联动信息
        const targetInfoList = trackInfo[sourceInfo] || []

        targetInfoList.forEach(targetInfo => {
          const targetInfoArray = targetInfo.split('#')
          const targetViewId = targetInfoArray[0] // 目标图表
          // DE_EMPTY 为清空条件标志
          if (element.component === 'UserView' && element.id === targetViewId) {
            if ('DE_EMPTY' !== paramValueStr) {
              // 如果目标图表 和 当前循环组件id相等 则进行条件增减
              const targetFieldId = targetInfoArray[1] // 目标图表列ID
              const condition = {
                fieldId: targetFieldId,
                operator: operatorV2 || operator,
                value: paramValue,
                viewIds: [targetViewId]
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
            }
            preActiveComponentIds.push(element.id)
          }
          if (element.component === 'VQuery') {
            const defaultValueMap = {}
            element.propValue?.forEach(filterItem => {
              if (filterItem.id === targetViewId) {
                let queryParams = paramValue
                if (!['1', '7'].includes(filterItem.displayType)) {
                  // 查询组件除了时间组件 其他入参只支持文本 这里全部转为文本
                  queryParams = paramValue.map(number => String(number))
                }
                filterItem.defaultMapValue = []
                filterItem.mapValue = []
                filterItem.defaultValueCheck = true
                filterItem.timeType = 'fixed'
                if (['0', '2'].includes(filterItem.displayType)) {
                  const { optionValueSource, field, displayId } = filterItem
                  const queryMapFlag = optionValueSource === 1 && field.id !== displayId
                  let queryMapParams = queryParams
                  if (queryMapFlag) {
                    queryMapParams = filterEnumParams(queryParams, field.id)
                  }
                  // 0 文本类型 1 数字类型
                  if (filterItem.multiple) {
                    // multiple === true 多选
                    filterItem['selectValue'] = queryParams
                    filterItem['defaultValue'] = queryParams
                  } else {
                    // 单选
                    filterItem['selectValue'] = queryParams[0]
                    filterItem['defaultValue'] = queryParams[0]
                  }
                  filterItem['defaultMapValue'] = queryMapParams
                  filterItem['mapValue'] = queryMapParams
                } else if (filterItem.displayType === '1') {
                  // 1 时间类型
                  filterItem['selectValue'] = queryParams[0]
                  filterItem['defaultValue'] = queryParams[0]
                } else if (filterItem.displayType === '7') {
                  // 7 时间范围类型
                  filterItem['selectValue'] = queryParams
                  filterItem['defaultValue'] = queryParams
                } else if (filterItem.displayType === '8') {
                  // 8 文本搜索
                  filterItem['conditionValueF'] = parmaValueSource + ''
                  filterItem['defaultConditionValueF'] = parmaValueSource + ''
                } else if (filterItem.displayType === '9') {
                  // 9 下拉树
                  if (filterItem.multiple) {
                    // multiple === true 多选
                    filterItem['selectValue'] = queryParams
                    filterItem['defaultValue'] = queryParams
                  } else {
                    // 单选
                    filterItem['selectValue'] = queryParams[0]
                    filterItem['defaultValue'] = queryParams[0]
                  }
                } else if (filterItem.displayType === '22') {
                  filterItem['defaultNumValueStart'] = queryParams[0]
                  filterItem['defaultNumValueEnd'] = queryParams[1]
                  filterItem['numValueStart'] = queryParams[0]
                  filterItem['numValueEnd'] = queryParams[1]
                }
                if ('DE_EMPTY' === paramValueStr) {
                  filterItem['selectValue'] = null
                  filterItem['defaultValue'] = null
                  filterItem['conditionValueF'] = null
                  filterItem['defaultConditionValueF'] = null
                }
                if (filterItem['defaultValue']) {
                  defaultValueMap[filterItem.id] = filterItem['defaultValue']
                }
              }
            })
            // 级联条件处理
            if (element.cascade?.length && Object.keys(defaultValueMap).length) {
              element.cascade.forEach(cascadeItem => {
                Object.keys(defaultValueMap).forEach(key => {
                  const curDefaultValue = defaultValueMap[key]
                  if (cascadeItem.length) {
                    cascadeItem.forEach(itemInner => {
                      if (itemInner.datasetId.includes(key) && curDefaultValue) {
                        itemInner['currentSelectValue'] = Array.isArray(curDefaultValue)
                          ? curDefaultValue
                          : [curDefaultValue]
                        itemInner['selectValue'] = Array.isArray(curDefaultValue)
                          ? curDefaultValue
                          : [curDefaultValue]
                      } else {
                        itemInner['currentSelectValue'] = []
                        itemInner['selectValue'] = []
                      }
                    })
                  }
                })
              })
            }
          }
        })
      })
      if (element.component === 'UserView') {
        element['outerParamsFilters'] = currentFilters
      }
      if (source === 'outer') {
        preActiveComponentIds.forEach(viewId => {
          useEmitt().emitter.emit('query-data-' + viewId)
        })
      }
    },
    trackFilterCursor(
      element,
      checkQDList,
      trackInfo,
      preActiveComponentIds,
      viewId,
      customFilter?
    ) {
      let currentFilters = element.linkageFilters || [] // 当前联动filter
      if (['table-info', 'table-normal'].includes(element.innerType)) {
        currentFilters = []
      }
      if (currentFilters.length) {
        for (let i = currentFilters.length - 1; i >= 0; i--) {
          if (currentFilters[i].filterType === 3) {
            currentFilters.splice(i, 1)
          }
        }
      }
      // 联动的图表情况历史条件
      // const currentFilters = []
      checkQDList.forEach(QDItem => {
        const sourceInfo = viewId + '#' + QDItem.id
        // 获取所有目标联动信息
        const targetInfoList = trackInfo[sourceInfo] || []
        const paramValue = [QDItem.value]
        targetInfoList.forEach(targetInfo => {
          const targetInfoArray = targetInfo.split('#')
          const targetViewId = targetInfoArray[0] // 目标图表
          if (element.component === 'UserView' && element.id === targetViewId) {
            // 如果含有customFilter 仅加入customFilter
            if (customFilter) {
              currentFilters.push({
                filterType: 3,
                customFilter: customFilter
              })
            } else {
              // 如果目标图表 和 当前循环组件id相等 则进行条件增减
              const targetFieldId = targetInfoArray[1] // 目标图表列ID
              let condition
              if (QDItem.timeValue && Array.isArray(QDItem.timeValue)) {
                // 如果dimension.timeValue存在值且是数组 目前判断为是时间组件
                condition = {
                  fieldId: targetFieldId,
                  operator: 'between',
                  value: QDItem.timeValue,
                  viewIds: [targetViewId],
                  sourceViewId: viewId
                }
              } else if (QDItem.value !== null && QDItem.value !== '') {
                condition = {
                  fieldId: targetFieldId,
                  operator: 'eq',
                  value: [QDItem.value],
                  viewIds: [targetViewId],
                  sourceViewId: viewId
                }
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
            }
            preActiveComponentIds.includes(element.id) || preActiveComponentIds.push(element.id)
          }
          if (element.component === 'VQuery' && Array.isArray(element.propValue)) {
            element.propValue?.forEach(filterItem => {
              if (filterItem.id === targetViewId) {
                let queryParams = paramValue
                if (!['1', '7'].includes(filterItem.displayType)) {
                  // 查询组件除了时间组件 其他入参只支持文本 这里全部转为文本
                  queryParams = paramValue.map(number => String(number))
                }
                filterItem.defaultValueCheck = true
                filterItem.timeType = 'fixed'
                if (['0', '2'].includes(filterItem.displayType)) {
                  const { optionValueSource, field, displayId } = filterItem
                  const queryMapFlag = optionValueSource === 1 && field.id !== displayId
                  let queryMapParams = queryParams
                  if (queryMapFlag) {
                    queryMapParams = filterEnumParams(queryParams, field.id)
                  }
                  // 0 文本类型 1 数字类型
                  if (filterItem.multiple) {
                    // multiple === true 多选
                    filterItem['selectValue'] = queryParams
                    filterItem['defaultValue'] = queryParams
                  } else {
                    // 单选
                    filterItem['selectValue'] = queryParams[0]
                    filterItem['defaultValue'] = queryParams[0]
                  }
                  filterItem['defaultMapValue'] = queryMapParams
                  filterItem['mapValue'] = queryMapParams
                } else if (filterItem.displayType === '1') {
                  // 1 时间类型
                  filterItem['selectValue'] = queryParams[0]
                  filterItem['defaultValue'] = queryParams[0]
                } else if (filterItem.displayType === '7') {
                  // 7 时间范围类型
                  if (QDItem.timeValue && Array.isArray(QDItem.timeValue)) {
                    // 如果dimension.timeValue存在值且是数组 目前判断为是时间组件
                    filterItem['selectValue'] = QDItem.timeValue
                    filterItem['defaultValue'] = QDItem.timeValue
                  } else {
                    filterItem['selectValue'] = queryParams
                    filterItem['defaultValue'] = queryParams
                  }
                } else if (filterItem.displayType === '8') {
                  // 8 文本搜索
                  filterItem['conditionValueF'] = queryParams[0]
                  filterItem['defaultConditionValueF'] = queryParams[0]
                }
              }
            })
          }
        })
      })
      element.linkageFilters = currentFilters
    },

    clearPanelLinkageInfo() {
      this.componentData.forEach(item => {
        if (item.component === 'UserView') {
          if (item.linkageFilters && item.linkageFilters.length > 0) {
            item.linkageFilters.splice(0, item.linkageFilters.length)
            useEmitt().emitter.emit('query-data-' + item.id)
          }
        } else if (item.component === 'Group') {
          item.propValue.forEach(groupItem => {
            if (groupItem.linkageFilters && groupItem.linkageFilters.length > 0) {
              groupItem.linkageFilters.splice(0, groupItem.linkageFilters.length)
              useEmitt().emitter.emit('query-data-' + groupItem.id)
            }
          })
        } else if (item.component === 'DeTabs') {
          item.propValue.forEach(tabItem => {
            tabItem.componentData.forEach(tabComponent => {
              if (tabComponent.linkageFilters && tabComponent.linkageFilters.length > 0) {
                tabComponent.linkageFilters.splice(0, tabComponent.linkageFilters.length)
                useEmitt().emitter.emit('query-data-' + tabComponent.id)
              }
            })
          })
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
        dataState: null,
        optType: null,
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        watermarkInfo: {},
        type: null,
        mobileLayout: false,
        contentId: 0
      }
      this.mainScrollTop = 0
    },
    setViewDataDetails(viewId, chartDataInfo) {
      this.canvasViewDataInfo[viewId] = chartDataInfo.data
      const viewInfo = this.canvasViewInfo[viewId]
      if (viewInfo) {
        const oldCalParams = viewInfo.calParams
          ? viewInfo.calParams.reduce((map, params) => {
              map[params.id] = params.value
              return map
            }, {})
          : {}
        if (chartDataInfo.calParams) {
          chartDataInfo.calParams.forEach(paramsItem => {
            if (oldCalParams[paramsItem.id]) {
              paramsItem.value = oldCalParams[paramsItem.id]
            }
          })
        }
        this.canvasViewInfo[viewId]['calParams'] = chartDataInfo.calParams || null
      }
    },
    getViewDataDetails(viewId) {
      return this.canvasViewDataInfo[viewId]
    },
    setViewOriginData(viewId, dataInfo) {
      this.canvasViewOriginDataInfo[viewId] = dataInfo
    },
    getViewOriginData(viewId) {
      return this.canvasViewOriginDataInfo[viewId]
    },
    setViewInstanceInfo(viewId, instance) {
      this.canvasViewInstanceInfo[viewId] = instance
    },
    getViewInstanceInfo(viewId) {
      return this.canvasViewInstanceInfo[viewId]
    },
    setLastViewRequestInfo(viewId, viewRequestInfo) {
      this.lastViewRequestInfo[viewId] = viewRequestInfo
    },
    getLastViewRequestInfo(viewId) {
      return this.lastViewRequestInfo[viewId]
    },
    getViewDetails(viewId) {
      return this.canvasViewInfo[viewId]
    },
    updateDvInfoCall(status = 1, newId?, contentId?) {
      if (this.dvInfo) {
        this.dvInfo.dataState = 'ready'
        this.dvInfo.optType = null
        if (newId) {
          this.dvInfo.id = newId
        }
        if (contentId) {
          this.dvInfo.contentId = contentId
        }
        this.dvInfo.status = status
      }
    },
    popAreaActiveSwitch() {
      if (this.canvasState['curPointArea'] === 'base') {
        this.canvasState['curPointArea'] = 'hidden'
      } else {
        this.canvasState['curPointArea'] = 'base'
      }
    },
    canvasStateChange({ key, value }) {
      if (this.canvasState[key] && value) {
        this.canvasState[key] = value
      }
    },
    createInit(dvType, resourceId?, pid?, watermarkInfo?, preName) {
      const optName =
        dvType === 'dashboard' ? t('visualization.new_dashboard') : t('visualization.new_screen')
      const name = preName ? preName : optName
      this.hiddenListStatus = false
      this.dvInfo = {
        dataState: 'prepare',
        optType: null,
        id: resourceId,
        name: name,
        pid: pid,
        type: dvType,
        status: 0,
        selfWatermarkStatus: true,
        watermarkInfo: watermarkInfo,
        mobileLayout: false,
        contentId: '0',
        weight: 9
      }
      const canvasStyleDataNew =
        dvType === 'dashboard'
          ? DEFAULT_CANVAS_STYLE_DATA_LIGHT
          : DEFAULT_CANVAS_STYLE_DATA_SCREEN_DARK

      this.canvasStyleData = deepCopy(canvasStyleDataNew)
      this.componentData = []
      this.canvasViewInfo = {}
    },
    removeLinkageInfo(targetId) {
      if (!!targetId && !!this.nowPanelTrackInfo) {
        Object.keys(this.nowPanelTrackInfo).forEach(trackId => {
          const targetInfo = this.nowPanelTrackInfo[trackId]
          for (let i = 0; i < targetInfo.length; i++) {
            if (targetInfo[i].indexOf(targetId) > -1) {
              targetInfo.splice(i, 1)
              i--
            }
          }
        })
        Object.keys(this.nowPanelTrackInfo).forEach(trackId => {
          if (trackId.indexOf(targetId) > -1 || this.nowPanelTrackInfo[trackId].length === 0) {
            delete this.nowPanelTrackInfo[trackId]
          }
        })
      }
    },
    canvasDataInit() {
      this.canvasViewInfo = {}
      this.componentData = []
      this.dvInfo = {
        dataState: null,
        optType: null,
        id: null,
        name: null,
        pid: null,
        status: null,
        selfWatermarkStatus: null,
        watermarkInfo: {},
        type: null,
        mobileLayout: false,
        contentId: '0'
      }
      this.canvasStyleData = { ...deepCopy(DEFAULT_CANVAS_STYLE_DATA_DARK), backgroundColor: null }
    },
    removeGroupArea(curComponentData = this.componentData) {
      // 清理临时组件
      const groupAreaHis = curComponentData?.filter(ele => ele?.component === 'GroupArea')
      if (groupAreaHis && groupAreaHis.length > 0) {
        groupAreaHis.forEach(ele => {
          this.deleteComponentById(ele.id, curComponentData)
        })
      }
    },
    setViewPageInfo(viewId, pageInfo) {
      if (this.canvasViewInfo[viewId]) {
        this.canvasViewInfo[viewId].pageInfo = pageInfo
      }
    },
    getViewPageInfo(viewId) {
      return this.canvasViewInfo[viewId]?.pageInfo
    }
  }
})

export const dvMainStoreWithOut = () => {
  return dvMainStore(store)
}
