import { withMapPictures } from './mapExport'
import html2canvas from 'html2canvas'
import JsPDF from 'jspdf'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { authenticatedImageUrl } from './resourceImages'
import { waitForResourceImages } from './resourceImageCache'
import { findResourceAsBase64 } from '@/api/staticResource'
import { exportMaterialBundle, remapMaterialDocument } from '@/api/material'
import { useAppStoreWithOut } from '@/store/modules/app'
import FileSaver from 'file-saver'
import { deepCopy } from '@/utils/utils'
import { toPng } from 'html-to-image'
import { domToPng } from 'modern-screenshot'
import { initCanvasDataPrepare } from '@/utils/canvasUtils'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, componentData, canvasViewInfo, canvasViewDataInfo, dvInfo } =
  storeToRefs(dvMainStore)

export function formatterUrl(url: string) {
  return url.replace('//de2api', '/de2api')
}
export function imgUrlTrans(url: string) {
  if (!url) return url
  return authenticatedImageUrl(formatterUrl(url.replace('com//', 'com/')))
}

function prePareTemplateBaseData(dvId, callback) {
  if (dvInfo.value.type === 'dataV' && canvasStyleData.value.screenAdaptor === 'full') {
    initCanvasDataPrepare(
      dvId,
      { busiFlag: 'dataV', resourceTable: 'core' },
      function ({ canvasDataResult, canvasStyleResult }) {
        callback({
          canvasDataResult: JSON.stringify(canvasDataResult),
          canvasStyleResult: JSON.stringify(canvasStyleResult)
        })
      }
    )
  } else {
    callback({
      canvasDataResult: JSON.stringify(componentData.value),
      canvasStyleResult: JSON.stringify(canvasStyleData.value)
    })
  }
}

export async function download2AppTemplate(downloadType, canvasDom, name, attachParams, callBack?) {
  const canvasViewDataTemplate = deepCopy(canvasViewInfo.value)
  Object.keys(canvasViewDataTemplate).forEach(viewId => {
    canvasViewDataTemplate[viewId].data = deepCopy(canvasViewDataInfo.value[viewId])
  })
  const missingDataId = Object.keys(canvasViewDataTemplate).find(viewId => {
    const view = canvasViewDataTemplate[viewId]
    const needsData =
      view.type !== 'VQuery' &&
      (view.tableId ||
        (view['dataFrom'] === 'template' && !['rich-text', 'picture-group'].includes(view.type)))
    return needsData && view.data == null
  })
  if (missingDataId) {
    const { t } = useI18n()
    ElMessage.warning(
      t('visualization.template_data_not_ready', [
        canvasViewDataTemplate[missingDataId].title || missingDataId
      ])
    )
    callBack?.()
    return
  }
  try {
    const { canvasDataResult, canvasStyleResult } = await new Promise<{
      canvasDataResult: string
      canvasStyleResult: string
    }>(resolve => prePareTemplateBaseData(dvInfo.value.id, resolve))
    const materialBundle = useAppStoreWithOut().getXpackValid
      ? (await exportMaterialBundle(canvasStyleResult, canvasDataResult)).data
      : null
    const staticResource = materialBundle
      ? {}
      : await new Promise<Record<string, string>>((resolve, reject) => {
          findStaticSource(resources => resolve(resources || {})).catch(reject)
        })
    const dataUrl = await withMapPictures(canvasDom, async () => {
      await waitForResourceImages(canvasDom)
      return domToPng(canvasDom, {
        width: canvasDom.offsetWidth,
        height: canvasDom.offsetHeight,
        scale: Math.max(1, window.devicePixelRatio || 1)
      })
    })
    if (!dataUrl) throw new Error('Template snapshot unavailable')
    const templateName = attachParams?.appName || name
    const templateInfo = {
      name: templateName,
      templateType: 'self',
      snapshot: dataUrl,
      dvType: dvInfo.value.type,
      nodeType: downloadType,
      version: 3,
      canvasStyleData: remapMaterialDocument(canvasStyleResult, materialBundle?.referenceMap),
      componentData: remapMaterialDocument(canvasDataResult, materialBundle?.referenceMap),
      dynamicData: remapMaterialDocument(
        JSON.stringify(canvasViewDataTemplate),
        materialBundle?.referenceMap
      ),
      staticResource: JSON.stringify(staticResource),
      materialBundle: materialBundle
        ? { version: materialBundle.version, files: materialBundle.files }
        : undefined,
      appData: attachParams ? JSON.stringify(attachParams) : null
    }
    const blob = new Blob([JSON.stringify(templateInfo)], { type: '' })
    if (downloadType === 'template') FileSaver.saveAs(blob, name + '-TEMPLATE.DET2')
    else if (downloadType === 'app') FileSaver.saveAs(blob, templateName + '-APP.DET2APP')
  } catch (e) {
    const { t } = useI18n()
    ElMessage.error(t('material.export_failed'))
    console.error(e)
  } finally {
    callBack?.()
  }
}

export function downloadCanvas(type, canvasDom, name, callBack?) {
  if (canvasDom) {
    waitForResourceImages(canvasDom)
      .then(() => html2canvas(canvasDom))
      .then(canvas => {
        const dom = document.body.appendChild(canvas)
        dom.style.display = 'none'
        document.body.removeChild(dom)
        const dataUrl = dom.toDataURL('image/png', 1)
        if (type === 'img') {
          const a = document.createElement('a')
          a.setAttribute('download', name)
          a.href = dataUrl
          document.body.appendChild(a)
          a.click()
          document.body.removeChild(a)
        } else {
          const contentWidth = canvasDom.offsetWidth
          const contentHeight = canvasDom.offsetHeight
          const lp = contentWidth > contentHeight ? 'l' : 'p'
          const PDF = new JsPDF(lp, 'pt', [contentWidth, contentHeight])
          PDF.addImage(dataUrl, 'PNG', 0, 0, contentWidth, contentHeight)
          PDF.save(name + '.pdf')
        }
        if (callBack) {
          callBack()
        }
      })
      .catch(error => {
        console.error('oops, something went wrong!', error)
        if (callBack) {
          callBack()
        }
      })
  }
}

export function downloadCanvas2(type, canvasDom, name, callBack?) {
  withMapPictures(canvasDom, async () => {
    await waitForResourceImages(canvasDom)
    return domToPng(canvasDom, {
      width: canvasDom.offsetWidth,
      height: canvasDom.offsetHeight,
      // 按 2 倍(或更高)分辨率渲染,解决导出图片模糊问题
      scale: Math.max(3, window.devicePixelRatio || 1)
    })
  })
    .then(dataUrl => {
      if (type === 'img') {
        const a = document.createElement('a')
        a.setAttribute('download', name + '.png')
        a.href = dataUrl
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
      } else {
        const contentWidth = canvasDom.offsetWidth
        const contentHeight = canvasDom.offsetHeight
        const lp = contentWidth > contentHeight ? 'l' : 'p'
        const PDF = new JsPDF(lp, 'pt', [contentWidth, contentHeight])
        PDF.addImage(dataUrl, 'PNG', 0, 0, contentWidth, contentHeight)
        PDF.save(name + '.pdf')
      }
      if (callBack) {
        callBack()
      }
    })
    .catch(error => {
      if (callBack) {
        callBack()
      }
      console.error('oops, something went wrong!', error)
    })
}

export function dataURLToBlob(dataUrl) {
  // ie 图片转格式
  const arr = dataUrl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bStr = atob(arr[1])
  let n = bStr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bStr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}

function findStaticSourceInner(componentDataInfo, staticResource) {
  componentDataInfo?.forEach(item => {
    if (
      typeof item.commonBackground.outerImage === 'string' &&
      item.commonBackground.outerImage.indexOf('static-resource') > -1
    ) {
      staticResource.push(item.commonBackground.outerImage)
    }
    if (
      item.component === 'Picture' &&
      item.propValue['url'] &&
      typeof item.propValue['url'] === 'string' &&
      item.propValue['url'].indexOf('static-resource') > -1
    ) {
      staticResource.push(item.propValue['url'])
    } else if (
      item.component === 'UserView' &&
      item.innerType === 'picture-group' &&
      item.propValue['urlList'] &&
      item.propValue['urlList'].length > 0
    ) {
      item.propValue['urlList'].forEach(urlInfo => {
        if (urlInfo.url.indexOf('static-resource') > -1) {
          staticResource.push(urlInfo.url)
        }
      })
    } else if (item.component === 'Group') {
      findStaticSourceInner(item.propValue, staticResource)
    } else if (item.component === 'DeTabs') {
      item.propValue?.forEach(tabItem => {
        findStaticSourceInner(tabItem.componentData, staticResource)
      })
    }
  })
}

// 解析静态文件
export function findStaticSource(callBack) {
  const staticResource = []
  // 系统背景文件
  if (
    typeof canvasStyleData.value.background === 'string' &&
    canvasStyleData.value.background.indexOf('static-resource') > -1
  ) {
    staticResource.push(canvasStyleData.value.background)
  }
  findStaticSourceInner(componentData.value, staticResource)
  if (staticResource.length > 0) {
    return findResourceAsBase64({ resourcePathList: staticResource }).then(rsp => {
      callBack(rsp.data)
    })
  }
  return Promise.resolve().then(() => callBack())
}
