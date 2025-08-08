import { cloneDeep } from 'lodash-es'
import componentList, {
  ACTION_SELECTION,
  BASE_CAROUSEL,
  BASE_EVENTS,
  COMMON_COMPONENT_BACKGROUND_DARK,
  COMMON_COMPONENT_BACKGROUND_LIGHT,
  COMMON_TAB_TITLE_BACKGROUND,
  MULTI_DIMENSIONAL
} from '@/custom-component/component-list'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import {
  appCanvasNameCheck,
  checkCanvasChange,
  decompression,
  dvNameCheck,
  findById,
  findCopyResource,
  saveCanvas,
  updateCanvas
} from '@/api/visualization/dataVisualization'
import { storeToRefs } from 'pinia'
import { getPanelAllLinkageInfo } from '@/api/visualization/linkage'
import { queryVisualizationJumpInfo } from '@/api/visualization/linkJump'
import {
  getViewConfig,
  SENIOR_STYLE_SETTING_LIGHT
} from '@/views/chart/components/editor/util/chart'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { deepCopy, nameTrim } from '@/utils/utils'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { guid } from '@/views/visualized/data/dataset/form/util'
const dvMainStore = dvMainStoreWithOut()
const {
  inMobile,
  dvInfo: curDvInfo,
  canvasStyleData,
  componentData,
  canvasViewInfo,
  appData
} = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
import { useI18n } from '@/hooks/web/useI18n'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useCache } from '@/hooks/web/useCache'
import { isDesktop } from '@/utils/ModelUtil'
const { t } = useI18n()
const appearanceStore = useAppearanceStoreWithOut()
const { wsCache } = useCache()
export function chartTransStr2Object(targetIn, copy) {
  const target = copy === 'Y' ? cloneDeep(targetIn) : targetIn
  return target
}

export function chartTransObject2Str(targetIn, copy) {
  const target = copy === 'Y' ? cloneDeep(targetIn) : targetIn
  return target
}

export function findDragComponent(componentInfo) {
  const componentInfoArray = componentInfo.split('&')
  const componentName = componentInfoArray[0]
  const innerType = componentInfoArray[1]
  return findNewComponent(componentName, innerType)
}

export function findNewComponent(componentName, innerType, staticMap?) {
  let newComponent
  componentList.forEach(comp => {
    if (comp.component === componentName || comp.component === innerType) {
      newComponent = cloneDeep(comp)
      if (['DeTabs', 'DeScreen'].includes(newComponent.component)) {
        newComponent.propValue[0].name = guid()
        newComponent['titleBackground'] = deepCopy(COMMON_TAB_TITLE_BACKGROUND)
      }
      newComponent.innerType = innerType
      if (newComponent.innerType === 'richText') {
        newComponent.propValue = {
          textValue: ''
        }
      }
      if (dvMainStore.curOriginThemes === 'light') {
        newComponent['commonBackground'] = cloneDeep(COMMON_COMPONENT_BACKGROUND_LIGHT)
      } else {
        newComponent['commonBackground'] = cloneDeep(COMMON_COMPONENT_BACKGROUND_DARK)
      }
    }
  })
  if (componentName === 'UserView') {
    const viewConfig = getViewConfig(innerType)
    newComponent.name = viewConfig?.title
    newComponent.label = viewConfig?.title
    newComponent.render = viewConfig?.render
    newComponent.isPlugin = !!staticMap
    if (newComponent.isPlugin) {
      newComponent.staticMap = staticMap
    }
  } else if (['DeDecoration', 'DynamicBackground'].includes(componentName)) {
    newComponent.style.borderWidth = 0
    newComponent.style.innerPadding = 0
  }
  return newComponent
}

export function commonHandleDragStart(e, dvModel) {
  const componentInfo = e.target.dataset.id
  if (dvModel === 'dashboard') {
    // 仪表板使用组件消息传输方式
    eventBus.emit('handleDragStartMoveIn-canvas-main', componentInfo)
  } else {
    // 大屏使用组件消息传输方式
    e.dataTransfer.setData('id', componentInfo)
  }
}
export function commonHandleDragEnd(e, dvModel) {
  if (dvModel === 'dashboard') {
    // 仪表板结束消息传输方式(用来清理未移入的组件)
    eventBus.emit('handleDragEnd-canvas-main', e)
  }
}

function matrixAdaptor(componentItem) {
  componentItem.x = 1 + (componentItem.x - 1) * 2
  componentItem.y = 1 + (componentItem.y - 1) * 2
  componentItem.sizeX = componentItem.sizeX * 2
  componentItem.sizeY = componentItem.sizeY * 2
  componentItem['mx'] = 1 + (componentItem.mx - 1) * 2
  componentItem['my'] = 1 + (componentItem.my - 1) * 2
  componentItem['mSizeX'] = componentItem.mSizeX * 2
  componentItem['mSizeY'] = componentItem.mSizeY * 2
  if (componentItem.component === 'Group') {
    componentItem.propValue?.forEach(groupItem => {
      matrixAdaptor(groupItem)
    })
  } else if (componentItem.component === 'DeTabs') {
    componentItem.propValue?.forEach(tabItem => {
      tabItem.componentData?.forEach(tabComponent => {
        matrixAdaptor(tabComponent)
      })
    })
  }
}

export function historyItemAdaptor(
  componentItem,
  reportFilterInfo,
  attachInfo,
  canvasVersion,
  canvasInfo
) {
  componentItem['canvasActive'] = false
  if (componentItem.component === 'VQuery') {
    componentItem['freeze'] = componentItem['freeze'] || false // 冻结字段适配
  }
  // 定时报告过滤组件适配 如果当前是定时报告默认切有设置对应的过滤组件默认值，则替换过滤组件
  if (
    componentItem.component === 'VQuery' &&
    attachInfo?.source === 'report' &&
    !!reportFilterInfo &&
    componentItem.propValue?.forEach
  ) {
    componentItem.propValue?.forEach((filterItem, index) => {
      if (reportFilterInfo[filterItem.id]) {
        componentItem.propValue[index] = JSON.parse(reportFilterInfo[filterItem.id].filterInfo)
      }
    })
  }

  if (componentItem.component === 'DeTabs') {
    componentItem['editableTabsValue'] = componentItem['editableTabsValue'] || ''
    componentItem.style['showTabTitle'] =
      componentItem.style['showTabTitle'] === undefined ? true : componentItem.style['showTabTitle']
  }

  componentItem['expand'] = componentItem['expand'] || false
  componentItem['resizeInnerKeep'] = componentItem['resizeInnerKeep'] || false

  if (componentItem.component === 'Picture') {
    componentItem.style['adaptation'] = componentItem.style['adaptation'] || 'adaptation'
  }
  // 样式设置
  componentItem.style['adaptation'] = componentItem.style['adaptation'] || 'adaptation'
  if (componentItem.style['borderActive'] === undefined) {
    componentItem.style['borderActive'] = false
    componentItem.style['borderWidth'] = 1
    componentItem.style['borderRadius'] = 5
    componentItem.style['borderStyle'] = 'solid'
    componentItem.style['borderColor'] = '#cccccc'
  } else {
    componentItem.style['borderWidth'] =
      componentItem.style['borderWidth'] === undefined ? 1 : componentItem.style['borderWidth']
    componentItem.style['borderRadius'] =
      componentItem.style['borderRadius'] === undefined ? 5 : componentItem.style['borderRadius']
    componentItem.style['borderStyle'] =
      componentItem.style['borderStyle'] === undefined
        ? 'solid'
        : componentItem.style['borderStyle']
    componentItem.style['borderColor'] =
      componentItem.style['borderColor'] === undefined
        ? '#cccccc'
        : componentItem.style['borderColor']
  }

  // public
  componentItem['dashboardHidden'] = componentItem['dashboardHidden'] || false
  componentItem['maintainRadio'] = componentItem['maintainRadio'] || false
  componentItem['multiDimensional'] =
    componentItem['multiDimensional'] || deepCopy(MULTI_DIMENSIONAL)
  componentItem['carousel'] = componentItem['carousel'] || deepCopy(BASE_CAROUSEL)
  componentItem['aspectRatio'] = componentItem['aspectRatio'] || 1
  if (componentItem.component === 'UserView') {
    componentItem.actionSelection = componentItem.actionSelection || deepCopy(ACTION_SELECTION)
  }
  // 2 为基础版本 此处需要增加仪表板矩阵密度
  if ((!canvasVersion || canvasVersion === 2) && canvasInfo?.type === 'dashboard') {
    matrixAdaptor(componentItem)
  }
  // 组件事件适配
  componentItem.events =
    componentItem.events &&
    componentItem.events.checked !== undefined &&
    componentItem.events.type !== 'displayChange'
      ? componentItem.events
      : deepCopy(BASE_EVENTS)

  componentItem.events['jump'].type = componentItem.events['jump'].type || '_blank'
  componentItem['category'] = componentItem['category'] || 'base'

  if (componentItem.component === 'DeTabs') {
    componentItem['titleBackground'] =
      componentItem['titleBackground'] || deepCopy(COMMON_TAB_TITLE_BACKGROUND)
    componentItem.style.fontStyle = componentItem.style.fontStyle || 'normal'
    componentItem.style.fontWeight = componentItem.style.fontWeight || 'normal'
    componentItem.style.textDecoration = componentItem.style.textDecoration || 'none'
    componentItem.propValue?.forEach(tabItem => {
      tabItem.componentData?.forEach(tabComponent => {
        historyItemAdaptor(tabComponent, reportFilterInfo, attachInfo, canvasVersion, canvasInfo)
      })
    })
  } else if (componentItem.component === 'Group') {
    componentItem.propValue?.forEach(groupItem => {
      historyItemAdaptor(groupItem, reportFilterInfo, attachInfo, canvasVersion, canvasInfo)
    })
  }
}

export function historyAdaptor(
  canvasStyleResult,
  canvasDataResult,
  canvasInfo,
  attachInfo,
  canvasVersion
) {
  // 防止出现主画布canvasId 不一致情况
  if (attachInfo?.resourceTable === 'snapshot') {
    canvasDataResult.forEach(componentItem => {
      componentItem.canvasId = 'canvas-main'
    })
  }
  const curVersion = wsCache.get('x-de-execute-version')
  // 含有定时报告过滤项每次都需要匹配
  const reportFilterInfo = canvasInfo?.reportFilterInfo
  if (canvasInfo?.checkVersion === curVersion && !reportFilterInfo) {
    return
  }
  //历史字段适配
  canvasStyleResult.component['seniorStyleSetting'] =
    canvasStyleResult.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)
  canvasStyleResult['fontFamily'] = canvasStyleResult['fontFamily'] || 'PingFang'
  canvasStyleResult.dashboard['showGrid'] = canvasStyleResult.dashboard['showGrid'] || false
  canvasStyleResult.dashboard['matrixBase'] = canvasStyleResult.dashboard['matrixBase'] || 4
  canvasStyleResult.dashboard['gapMode'] = canvasStyleResult.dashboard['gapMode'] || 'middle'
  canvasStyleResult.component['seniorStyleSetting'] =
    canvasStyleResult.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)
  canvasStyleResult['suspensionButtonAvailable'] =
    canvasStyleResult['suspensionButtonAvailable'] === undefined
      ? false
      : canvasStyleResult['suspensionButtonAvailable']
  canvasStyleResult['screenAdaptor'] = canvasStyleResult['screenAdaptor'] || 'widthFirst'
  canvasStyleResult['dashboardAdaptor'] =
    canvasStyleResult['dashboardAdaptor'] || 'keepHeightAndWidth'
  canvasStyleResult['refreshBrowserEnable'] =
    canvasStyleResult['refreshBrowserEnable'] === undefined
      ? false
      : canvasStyleResult['refreshBrowserEnable']
  canvasStyleResult['refreshBrowserUnit'] = canvasStyleResult['refreshBrowserUnit'] || 'minute'
  canvasStyleResult['refreshBrowserTime'] = canvasStyleResult['refreshBrowserTime'] || 5
  // 同步宽高比例(大屏使用)
  canvasStyleResult['scaleWidth'] = canvasStyleResult['scale']
  canvasStyleResult['scaleHeight'] = canvasStyleResult['scale']
  canvasStyleResult['popupAvailable'] =
    canvasStyleResult['popupAvailable'] === undefined ? true : canvasStyleResult['popupAvailable'] //兼容弹框区域开关
  canvasStyleResult['popupButtonAvailable'] =
    canvasStyleResult['popupButtonAvailable'] === undefined
      ? true
      : canvasStyleResult['popupButtonAvailable'] //兼容弹框区域按钮开关
  canvasDataResult.forEach(componentItem => {
    historyItemAdaptor(componentItem, reportFilterInfo, attachInfo, canvasVersion, canvasInfo)
  })
}

// 重置仪表板、大屏中的其他组件
export function refreshOtherComponent(dvId, busiFlag) {
  // 富文本 跑马灯组件进行刷新
  const refreshComponentList = componentData.value.filter(
    ele => ['ScrollText'].includes(ele.component) || ele.innerType === 'rich-text'
  )
  if (refreshComponentList && refreshComponentList.length > 0) {
    const refreshIdList = refreshComponentList.map(ele => ele.id)
    findById(dvId, busiFlag, {}).then(rsp => {
      const canvasInfo = rsp.data
      const canvasDataResult = JSON.parse(canvasInfo.componentData)
      const canvasDataResultMap = canvasDataResult.reduce((acc, comp) => {
        acc[comp.id] = comp
        return acc
      }, {})
      // 遍历数组并替换
      for (let i = 0; i < componentData.value.length; i++) {
        const component = componentData.value[i]
        if (refreshIdList.includes(component.id) && canvasDataResultMap[component.id]) {
          if (inMobile.value) {
            componentData.value[i].propValue = canvasDataResultMap[component.id].propValue
          } else {
            const { top, left, height, width, fontSize } = componentData.value[i].style
            const { linkageFilters, outerParamsFilters, webParamsFilters } = componentData.value[i]
            canvasDataResultMap[component.id].style.top = top
            canvasDataResultMap[component.id].style.left = left
            canvasDataResultMap[component.id].style.height = height
            canvasDataResultMap[component.id].style.width = width
            canvasDataResultMap[component.id]['linkageFilters'] = linkageFilters
            canvasDataResultMap[component.id]['outerParamsFilters'] = outerParamsFilters
            canvasDataResultMap[component.id]['webParamsFilters'] = webParamsFilters
            if (fontSize) {
              canvasDataResultMap[component.id].style.fontSize = fontSize
            }
            componentData.value[i] = canvasDataResultMap[component.id]
          }
        }
      }
    })
  }
}

export function initCanvasDataPrepare(dvId, params, callBack) {
  const busiFlag = params.busiFlag
  const copyFlag = busiFlag != null && busiFlag.includes('-copy')
  const busiFlagCustom = copyFlag ? busiFlag.split('-')[0] : busiFlag
  const method = copyFlag ? findCopyResource : findById
  let attachInfo = { source: params.source ? params.source : 'main' }
  if (dvMainStore.canvasAttachInfo && !!dvMainStore.canvasAttachInfo.taskId) {
    attachInfo = { source: 'report', taskId: dvMainStore.canvasAttachInfo.taskId }
    const showWatermarkExist =
      dvMainStore.canvasAttachInfo.hasOwnProperty('showWatermark') &&
      typeof dvMainStore.canvasAttachInfo.showWatermark !== 'undefined' &&
      dvMainStore.canvasAttachInfo.showWatermark !== null
    if (showWatermarkExist) {
      const enable = dvMainStore.canvasAttachInfo.showWatermark === 'true'
      attachInfo['showWatermark'] = enable
    }
  }
  attachInfo['resourceTable'] = params.resourceTable ? params.resourceTable : 'core'
  method(dvId, busiFlagCustom, attachInfo).then(res => {
    const canvasInfo = res.data
    const watermarkInfo = {
      ...canvasInfo.watermarkInfo,
      settingContent: canvasInfo.watermarkInfo?.settingContent
        ? JSON.parse(canvasInfo.watermarkInfo.settingContent)
        : {}
    }

    const dvInfo = {
      id: canvasInfo.id,
      name: canvasInfo.name,
      pid: canvasInfo.pid,
      status: canvasInfo.status,
      selfWatermarkStatus: canvasInfo.selfWatermarkStatus,
      type: canvasInfo.type,
      creatorName: canvasInfo.creatorName,
      updateName: canvasInfo.updateName,
      createTime: canvasInfo.createTime,
      updateTime: canvasInfo.updateTime,
      watermarkInfo: watermarkInfo,
      weight: canvasInfo.weight,
      ext: canvasInfo.ext,
      contentId: canvasInfo.contentId,
      mobileLayout: canvasInfo.mobileLayout || false
    }
    const canvasVersion = canvasInfo.version

    const canvasDataResult = JSON.parse(canvasInfo.componentData)
    const canvasStyleResult = JSON.parse(canvasInfo.canvasStyleData)
    const canvasViewInfoPreview = canvasInfo.canvasViewInfo
    historyAdaptor(canvasStyleResult, canvasDataResult, canvasInfo, attachInfo, canvasVersion)
    const curPreviewGap =
      dvInfo.type === 'dashboard' && canvasStyleResult['dashboard'].gap === 'yes'
        ? canvasStyleResult['dashboard'].gapSize
        : 0
    appearanceStore.setCurrentFont(canvasStyleResult.fontFamily)
    document.documentElement.style.setProperty(
      '--de-canvas_custom_font',
      `${canvasStyleResult.fontFamily}`
    )
    callBack({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview, curPreviewGap })
  })
}

export async function initCanvasData(dvId, params, callBack) {
  initCanvasDataPrepare(
    dvId,
    params,
    function ({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview }) {
      if (!params.onlyPreview) {
        dvMainStore.setComponentData(canvasDataResult)
        dvMainStore.setCanvasStyle(canvasStyleResult)
        dvMainStore.updateCurDvInfo(dvInfo)
        dvMainStore.setCanvasViewInfo(canvasViewInfoPreview)
        // 刷新联动信息
        getPanelAllLinkageInfo(dvInfo.id, params.resourceTable).then(rsp => {
          dvMainStore.setNowPanelTrackInfo(rsp.data)
        })
        // 刷新跳转信息
        queryVisualizationJumpInfo(dvInfo.id, params.resourceTable).then(rsp => {
          dvMainStore.setNowPanelJumpInfo(rsp.data)
        })
      }
      callBack({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview })
    }
  )
}

export async function backCanvasData(dvId, mobileViewInfo, busiFlag, callBack) {
  initCanvasDataPrepare(
    dvId,
    { busiFlag },
    function ({ canvasDataResult, canvasStyleResult, canvasViewInfoPreview }) {
      const componentDataCopy = canvasDataResult.filter(ele => !!ele.inMobile)
      const componentDataId = componentDataCopy.map(ele => ele.id)
      componentData.value.forEach(ele => {
        ele.inMobile = componentDataId.includes(ele.id)
        if (ele.inMobile) {
          const { mx, my, mSizeX, mSizeY, mPropValue, mEvents, mCommonBackground } =
            componentDataCopy.find(itx => itx.id === ele.id)
          ele.mx = mx
          ele.my = my
          ele.mSizeX = mSizeX
          ele.mSizeY = mSizeY
          ele.mEvents = mEvents
          ele.mCommonBackground = mCommonBackground
          if (ele.component === 'VQuery') {
            ele.mPropValue = mPropValue
          }
        }
      })
      Object.keys(canvasViewInfoPreview).forEach(key => {
        if (canvasViewInfo.value[key] && canvasViewInfoPreview[key]) {
          const { customAttrMobile, customStyleMobile } = canvasViewInfoPreview[key]
          // 此处作为还原移动设计使用
          canvasViewInfo.value[key]['customStyleMobile'] = customStyleMobile
          canvasViewInfo.value[key]['customAttrMobile'] = customAttrMobile
        }
      })
      dvMainStore.setComponentData(componentData.value)
      const canvasStyleDataCopy = cloneDeep(canvasStyleData.value)
      if (!canvasStyleDataCopy.mobileSetting) {
        canvasStyleDataCopy.mobileSetting = {
          backgroundColorSelect: false,
          background: '',
          color: '#ffffff',
          backgroundImageEnable: false,
          customSetting: false
        }
      } else {
        canvasStyleDataCopy.mobileSetting = canvasStyleResult.mobileSetting
      }
      dvMainStore.setCanvasStyle(canvasStyleDataCopy)
      callBack()
    }
  )
}

export function initCanvasDataMobile(dvId, params, callBack) {
  initCanvasDataPrepare(
    dvId,
    params,
    function ({ canvasDataResult, canvasStyleResult, dvInfo, canvasViewInfoPreview }) {
      const componentData = canvasDataResult.filter(ele => !!ele.inMobile)
      canvasDataResult.forEach(ele => {
        const {
          mx,
          my,
          mSizeX,
          mSizeY,
          mStyle,
          mPropValue,
          mEvents,
          mCommonBackground,
          style,
          propValue,
          events,
          commonBackground
        } = ele
        ele.x = mx
        ele.y = my
        ele.sizeX = mSizeX
        ele.sizeY = mSizeY
        ele.style = mStyle || style
        ele.propValue = mPropValue || propValue
        ele.events = mEvents || events
        ele.commonBackground = mCommonBackground || commonBackground
        if (ele.component === 'DeTabs') {
          ele.propValue?.forEach(tabItem => {
            tabItem.componentData?.forEach(tabComponent => {
              tabComponent.style = tabComponent.mStyle || tabComponent.style
              tabComponent.propValue = tabComponent.mPropValue || tabComponent.propValue
              tabComponent.events = tabComponent.mEvents || tabComponent.events
              tabComponent.commonBackground =
                tabComponent.mCommonBackground || tabComponent.commonBackground
            })
          })
        }
      })
      if (!!canvasViewInfoPreview) {
        Object.keys(canvasViewInfoPreview).forEach(key => {
          const viewInfo = canvasViewInfoPreview[key]
          const { customAttrMobile, customStyleMobile } = viewInfo
          viewInfo['customAttr'] = customAttrMobile || viewInfo['customAttr']
          viewInfo['customStyle'] = customStyleMobile || viewInfo['customStyle']
        })
      }
      dvMainStore.setComponentData(componentData)
      dvMainStore.setCanvasStyle(canvasStyleResult)
      dvMainStore.updateCurDvInfo(dvInfo)
      dvMainStore.setCanvasViewInfo(canvasViewInfoPreview)
      // 刷新联动信息
      getPanelAllLinkageInfo(dvInfo.id, params.resourceTable).then(rsp => {
        dvMainStore.setNowPanelTrackInfo(rsp.data)
      })
      // 刷新跳转信息
      queryVisualizationJumpInfo(dvInfo.id).then(rsp => {
        dvMainStore.setNowPanelJumpInfo(rsp.data)
      })
      callBack({
        canvasDataResult: componentData,
        canvasStyleResult,
        dvInfo,
        canvasViewInfoPreview
      })
    }
  )
}

export function checkCanvasChangePre(callBack) {
  // do pre
  const isUpdate = curDvInfo.value.id && curDvInfo.value.optType !== 'copy'
  // 桌面版为单人模式不需要检查
  if (isUpdate && !isDesktop()) {
    const params = { ...curDvInfo.value, watermarkInfo: null }
    const tips =
      (curDvInfo.value.type === 'dashboard'
        ? t('work_branch.dashboard')
        : t('work_branch.big_data_screen')) + t('visualization.save_conflict_tips')
    checkCanvasChange(params).then(rsp => {
      if (rsp && rsp.data === 'Repeat') {
        ElMessageBox.confirm(tips, {
          confirmButtonType: 'danger',
          type: 'warning',
          autofocus: false,
          showClose: false
        }).then(() => {
          callBack()
        })
      } else {
        callBack()
      }
    })
  } else {
    callBack()
  }
}

export async function canvasSave(callBack) {
  await canvasSaveWithParams(null, callBack)
}

export async function canvasSaveWithParams(params, callBack) {
  dvMainStore.removeGroupArea()
  const componentDataToSave = cloneDeep(componentData.value)
  componentDataToSave.forEach(item => {
    if (item.component === 'UserView') {
      item.linkageFilters = []
    } else if (item.component === 'Group') {
      item.propValue?.forEach(groupItem => {
        groupItem.linkageFilters = []
      })
    } else if (item.component === 'DeTabs') {
      item.propValue?.forEach(tabItem => {
        tabItem.componentData?.forEach(tabComponent => {
          tabComponent.linkageFilters = []
        })
      })
    }
  })
  const newContentId = guid()
  const canvasInfo = {
    canvasStyleData: JSON.stringify(canvasStyleData.value),
    componentData: JSON.stringify(componentDataToSave),
    canvasViewInfo: canvasViewInfo.value,
    appData: appData.value,
    ...curDvInfo.value,
    checkVersion: wsCache.get('x-de-execute-version'),
    contentId: newContentId,
    watermarkInfo: null
  }

  let dsNameCheck = 'success'
  if (appData.value) {
    await appCanvasNameCheck({
      datasetFolderPid: canvasInfo.datasetFolderPid,
      datasetFolderName: canvasInfo.datasetFolderName
    }).then(rsp => {
      dsNameCheck = rsp.data
    })
  }
  if (dsNameCheck === 'repeat') {
    ElMessage.error('数据集分组名称已存在')
    return
  }
  nameTrim(curDvInfo.value, t('components.length_1_64_characters'))
  const method =
    curDvInfo.value.id && curDvInfo.value.optType !== 'copy' ? updateCanvas : saveCanvas
  if (method === updateCanvas) {
    await dvNameCheck({
      opt: 'edit',
      nodeType: 'leaf',
      name: curDvInfo.value.name,
      type: curDvInfo.value.type,
      id: curDvInfo.value.id
    })
  }
  method(canvasInfo).then(res => {
    if (method === updateCanvas) {
      // saveCanvas 为初次保存 状态为0 updateCanvas为二次保存状态为2 当存在传入状态时，则修改对应的传入状态
      const status = params?.status ? params?.status : res.data?.status
      dvMainStore.updateDvInfoCall(status, null, newContentId)
    } else {
      dvMainStore.updateDvInfoCall(0, res.data, newContentId)
    }
    snapshotStore.resetStyleChangeTimes()
    callBack(res)
  })
}

export function checkAddHttp(url) {
  if (!url) {
    return url
  } else if (/^(http(s)?:\/\/)/.test(url.toLowerCase())) {
    return url
  } else {
    return 'http://' + url
  }
}

export function setIdValueTrans(from, to, content, colList) {
  if (!content) {
    return content
  }
  let name2Id = content
  const nameIdMap = colList.reduce((pre, next) => {
    pre[next[from]] = next[to]
    return pre
  }, {})
  const on = content?.match(/\[(.+?)\]/g) || []
  if (on) {
    on.forEach(itm => {
      const ele = itm.slice(1, -1)
      name2Id = name2Id.replace(itm, nameIdMap[ele])
    })
  }
  return name2Id
}

export function isMainCanvas(canvasId) {
  return canvasId === 'canvas-main'
}
// 检查是否可以加入到分组
export function checkJoinGroup(item) {
  if (item.component === 'DeTabs') {
    let result = true
    item.propValue?.forEach(tabItem => {
      tabItem.componentData?.forEach(tabComponent => {
        if (tabComponent.component === 'Group') {
          result = false
        }
      })
    })
    return result
  } else {
    return true
  }
}
// 检查是否可以移入tab
export function checkJoinTab(item) {
  if (item.component === 'Group') {
    let result = true
    item.propValue?.forEach(groupItem => {
      if (groupItem.component === 'DeTabs') {
        result = false
      }
    })
    return result
  } else {
    return true
  }
}

// 目前仅允许group中还有一层Tab 或者 Tab中含有一层group
export function itemCanvasPathCheck(item, checkType) {
  if (checkType === 'canvas-main') {
    return isMainCanvas(item.canvasId)
  }
  const pathMap = {}
  componentData.value.forEach(componentItem => {
    canvasIdMapCheck(componentItem, null, pathMap)
  })

  // 父组件是Tab且在group中
  if (checkType === 'pTabGroup') {
    return Boolean(
      pathMap[item.id] &&
        pathMap[item.id].component === 'DeTabs' &&
        pathMap[pathMap[item.id].id] &&
        pathMap[pathMap[item.id].id].component === 'Group'
    )
  }
  // 当前组件是group且在Tab中
  if (checkType === 'groupInTab') {
    return Boolean(
      item.component === 'Group' &&
        pathMap[pathMap[item.id].id] &&
        pathMap[pathMap[item.id].id].component === 'DeTabs'
    )
  }

  // 当前组件是Tab且在Group中
  if (checkType === 'tabInGroup') {
    return Boolean(
      item.component === 'DeTabs' && pathMap[item.id] && pathMap[item.id].component === 'Group'
    )
  }
  return false
}

export function canvasIdMapCheck(item, pItem, pathMap) {
  pathMap[item.id] = pItem
  if (item.component === 'DeTabs') {
    item.propValue?.forEach(tabItem => {
      tabItem.componentData?.forEach(tabComponent => {
        canvasIdMapCheck(tabComponent, item, pathMap)
      })
    })
  } else if (item.component === 'Group') {
    item.propValue?.forEach(groupItem => {
      canvasIdMapCheck(groupItem, item, pathMap)
    })
  }
}

export function isSameCanvas(item, canvasId) {
  return item.canvasId === canvasId
}

export function isGroupOrTabCanvas(canvasId) {
  return isGroupCanvas(canvasId) || isTabCanvas(canvasId)
}

export function isGroupCanvas(canvasId) {
  return canvasId && canvasId.includes('Group')
}

export function isTabCanvas(canvasId) {
  return canvasId && !canvasId.includes('Group') && !isMainCanvas(canvasId)
}

export function findComponentIndexByIdWithFilterHidden(
  componentId,
  componentDataMatch = componentData.value
) {
  let indexResult = -1
  componentDataMatch
    .filter(item => !item.dashboardHidden)
    .forEach((component, index) => {
      if (component.id === componentId) {
        indexResult = index
      }
    })
  return indexResult
}

export function findComponentIndexById(componentId, componentDataMatch = componentData.value) {
  let indexResult = -1
  componentDataMatch.forEach((component, index) => {
    if (component.id === componentId) {
      indexResult = index
    }
  })
  return indexResult
}

export function canvasChangeAdaptor(component, matrixBase, usePointShadow = false) {
  const targetDomComponent = document.querySelector(
    usePointShadow ? '#point-shadow-main' : '#shape-id-' + component.id
  )
  const componentWidth = targetDomComponent['offsetWidth']
  const componentHeight = targetDomComponent['offsetHeight']
  component.sizeX = Math.round(componentWidth / matrixBase.baseWidth)
  component.sizeY = Math.round(componentHeight / matrixBase.baseHeight)
  component.style.width = componentWidth
  component.style.height = componentHeight
  if (usePointShadow) {
    const componentLeft = targetDomComponent['offsetLeft']
    const componentTop = targetDomComponent['offsetTop']
    component.x = Math.round(componentLeft / matrixBase.baseWidth)
    component.y = Math.round(componentTop / matrixBase.baseHeight)
    component.style.left = componentLeft
    component.style.height = componentTop
  }
}

export function findAllViewsId(componentData, idArray) {
  componentData.forEach(item => {
    if (item.component === 'UserView' && item.innerType != 'VQuery') {
      idArray.push(item.id)
    } else if (item.component === 'Group') {
      item.propValue?.forEach(groupItem => {
        idArray.push(groupItem.id)
      })
    } else if (item.component === 'DeTabs') {
      item.propValue?.forEach(tabItem => {
        tabItem.componentData?.forEach(tabComponent => {
          idArray.push(tabComponent.id)
        })
      })
    }
  })
}

export function markTreeFolder(elementInfo) {
  if (elementInfo) {
    if (elementInfo instanceof Array) {
      elementInfo.forEach(elementSon => {
        elementSon['disabled'] = !elementSon['leaf']
        if (elementSon['children']) {
          markTreeFolder(elementSon['children'])
        }
      })
    } else {
      elementInfo['disabled'] = !elementInfo['leaf']
    }
  }
}

export function filterEmptyFolderTree(nodes) {
  // 递归过滤树节点数据，只显示包含子文件夹或文件的文件夹
  return nodes.filter(node => {
    if (node.leaf) {
      return true
    } else if (node.children && node.children.length > 0) {
      // 如果节点有子节点，继续递归过滤子节点
      node.children = filterEmptyFolderTree(node.children)
      return true
    } else {
      return false // 不显示空文件夹
    }
  })
}

export function findParentIdByChildIdRecursive(tree, targetChildId) {
  function findParentId(node, targetChildId) {
    if (node.children) {
      for (const childNode of node.children) {
        if (childNode.id === targetChildId) {
          return node.id // 找到匹配的子节点，返回其父节点的 ID
        }
        const parentId = findParentId(childNode, targetChildId)
        if (parentId !== null) {
          return parentId // 在子节点中找到匹配的父节点
        }
      }
    }
    return null // 没有找到匹配的子节点
  }

  for (const node of tree) {
    const parentId = findParentId(node, targetChildId)
    if (parentId !== null) {
      return parentId // 在整个树中找到匹配的父节点
    }
  }

  return null // 没有找到匹配的子节点
}

export async function decompressionPre(params, callBack) {
  let deTemplateData
  await decompression(params)
    .then(response => {
      const deTemplateDataTemp = response.data
      const sourceComponentData = JSON.parse(deTemplateDataTemp['componentData'])
      const appData = deTemplateDataTemp['appData']
      const sourceCanvasStyle = JSON.parse(deTemplateDataTemp['canvasStyleData'])
      sourceComponentData.forEach(componentItem => {
        // 2 为基础版本 此处需要增加仪表板矩阵密度
        if (
          (!deTemplateDataTemp.version || deTemplateDataTemp.version === 2) &&
          deTemplateDataTemp.type === 'dashboard'
        ) {
          matrixAdaptor(componentItem)
        }
      })
      //历史字段适配
      sourceCanvasStyle.component['seniorStyleSetting'] =
        sourceCanvasStyle.component['seniorStyleSetting'] || deepCopy(SENIOR_STYLE_SETTING_LIGHT)
      sourceCanvasStyle['scaleWidth'] = sourceCanvasStyle['scale']
      sourceCanvasStyle['scaleHeight'] = sourceCanvasStyle['scaleHeight']
      deTemplateData = {
        canvasStyleData: sourceCanvasStyle,
        componentData: sourceComponentData,
        canvasViewInfo: deTemplateDataTemp['canvasViewInfo'],
        appData: appData,
        baseInfo: {
          preName: deTemplateDataTemp.name
        }
      }
    })
    .catch(e => {
      console.error(e)
    })
  historyAdaptor(
    deTemplateData.canvasStyleData,
    deTemplateData.componentData,
    null,
    { resourceTable: 'snapshot' },
    null
  )
  callBack(deTemplateData)
}

export function isDashboard() {
  return curDvInfo.value.type === 'dashboard'
}

export function trackBarStyleCheck(element, trackbarStyle, _scale, trackMenuNumber) {
  const { width, height } = element.style
  const widthReal = width
  const heightReal = height
  // 浮窗高度
  function calculateTrackHeight(trackMenuNumber) {
    if (trackMenuNumber === 2) {
      return 75
    } else {
      const increment = Math.floor(trackMenuNumber - 2) * 35
      return 75 + increment
    }
  }
  if (trackbarStyle.left < 0) {
    trackbarStyle.left = 0
  } else if (widthReal - trackbarStyle.left < 60) {
    trackbarStyle.left = trackbarStyle.left - 60
  }
  const trackMenuHeight = calculateTrackHeight(trackMenuNumber)
  if (trackbarStyle.top < 0) {
    trackbarStyle.top = 0
  } else if (trackbarStyle.top + trackMenuHeight + 60 > heightReal) {
    trackbarStyle.top = trackbarStyle.top - trackMenuHeight
  }
}

// 优化仪表板图层排序 根据所处的Y轴位置预先进行排序再渲染矩阵 防止出现串位
export function componentPreSort(componentData) {
  if (componentData && Array.isArray(componentData)) {
    componentData.sort((c1, c2) => c1.y - c2.y)
    componentData.forEach(componentItem => {
      if (componentItem.component === 'DeTabs') {
        componentItem.propValue?.forEach(tabItem => {
          componentPreSort(tabItem.componentData)
        })
      }
    })
  }
}

export function maxYComponentCount() {
  if (componentData.value.length === 0) {
    return 1
  } else {
    return componentData.value
      .filter(item => item.y)
      .map(item => item.y + item.sizeY) // 计算每个元素的 y + sizeY
      .reduce((max, current) => Math.max(max, current), 0)
  }
}

export function componentSwitch(componentData, changeComponent) {
  componentData.map(obj => (obj.id === changeComponent.id ? changeComponent : obj))
}

export function findComponentById(componentId) {
  let result
  componentData.value.forEach(item => {
    if (item.id === componentId) {
      result = item
    } else if (item.component === 'Group') {
      item.propValue?.forEach(groupItem => {
        if (groupItem.id === componentId) {
          result = groupItem
        }
      })
    } else if (item.component === 'DeTabs') {
      item.propValue?.forEach(tabItem => {
        tabItem.componentData?.forEach(tabComponent => {
          if (tabComponent.id === componentId) {
            result = tabComponent
          }
        })
      })
    }
  })
  return result
}

export function onInitReady(params, eventName = 'canvas_init_ready') {
  try {
    console.info('event:' + eventName)
    const targetPm = {
      type: 'dataease-embedded-interactive',
      eventName: eventName,
      args: params
    }
    window.parent.postMessage(targetPm, '*')
  } catch (e) {
    console.warn('de_inner_params send error')
  }
}

export function mobileViewStyleSwitch(component) {
  if (component) {
    const viewInfo = canvasViewInfo.value[component.id]
    viewInfo.customStyle = component.customStyle
    viewInfo.customAttr = component.customAttr
    viewInfo.title = component.title
    viewInfo.name = component.name
  }
}

export function syncViewTitle(element) {
  if (element && canvasViewInfo.value[element.id]) {
    if (['UserView'].includes(element.component)) {
      canvasViewInfo.value[element.id].title = element.name
    } else if (['VQuery'].includes(element.component)) {
      canvasViewInfo.value[element.id].title = element.name
      canvasViewInfo.value[element.id].customStyle.component.title = element.name
    }
  }
}
