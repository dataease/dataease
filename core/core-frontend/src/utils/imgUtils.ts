import { presentError } from '@/config/axios/errorPresenter'
import { collectChartRuntimeCaches, getCanvasRuntime } from '@/hooks/web/useChartRuntimeCache'
import { withMapPictures } from './mapExport'
import { prepareViewportChartsForExport } from '@/hooks/web/useChartViewportLoad'
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

function prePareTemplateBaseData(dvId, callback, context?) {
  const current = context || {
    dvInfo: dvInfo.value,
    canvasStyleData: canvasStyleData.value,
    componentData: componentData.value
  }
  if (current.dvInfo.type === 'dataV' && current.canvasStyleData.screenAdaptor === 'full') {
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
      canvasDataResult: JSON.stringify(current.componentData),
      canvasStyleResult: JSON.stringify(current.canvasStyleData)
    })
  }
}

export async function download2AppTemplate(downloadType, canvasDom, name, attachParams, callBack?) {
  try {
    await prepareViewportChartsForExport(canvasDom)
    const localCanvas = getCanvasRuntime(canvasDom)
    const localCharts = collectChartRuntimeCaches(canvasDom)
    const canvasViewDataTemplate = deepCopy(localCanvas?.canvasViewInfo || canvasViewInfo.value)
    Object.keys(canvasViewDataTemplate).forEach(viewId => {
      const cache = localCharts.find(cache => cache.view?.id === viewId)
      // 有运行时上下文时不回退另一个画布的共享缓存；缺数据仍由既有校验提示。
      canvasViewDataTemplate[viewId].data = deepCopy(
        cache && !cache.legacy
          ? cache.data
          : cache?.legacy || !localCanvas
          ? canvasViewDataInfo.value[viewId]
          : undefined
      )
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
      return
    }
    const { canvasDataResult, canvasStyleResult } = await new Promise<{
      canvasDataResult: string
      canvasStyleResult: string
    }>(resolve => {
      if (
        localCanvas &&
        !(
          localCanvas.dvInfo.type === 'dataV' &&
          localCanvas.canvasStyleData.screenAdaptor === 'full'
        )
      ) {
        resolve({
          canvasDataResult: JSON.stringify(localCanvas.componentData),
          canvasStyleResult: JSON.stringify(localCanvas.canvasStyleData)
        })
      } else {
        prePareTemplateBaseData(localCanvas?.dvInfo.id || dvInfo.value.id, resolve, localCanvas)
      }
    })
    const materialBundle = useAppStoreWithOut().getXpackValid
      ? (await exportMaterialBundle(canvasStyleResult, canvasDataResult)).data
      : null
    const staticResource = materialBundle
      ? {}
      : await findStaticSource(JSON.parse(canvasStyleResult), JSON.parse(canvasDataResult))
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
      dvType: localCanvas?.dvInfo.type || dvInfo.value.type,
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
    presentError(e, t('material.export_failed'))
    console.error(e)
  } finally {
    callBack?.()
  }
}

export function downloadCanvas(type, canvasDom, name, callBack?) {
  if (canvasDom) {
    prepareViewportChartsForExport(canvasDom)
      .then(() => waitForResourceImages(canvasDom))
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
        presentError(error, useI18n().t('material.export_failed'))
        console.error('oops, something went wrong!', error)
        if (callBack) {
          callBack()
        }
      })
  }
}

export function downloadCanvas2(type, canvasDom, name, callBack?) {
  prepareViewportChartsForExport(canvasDom)
    .then(() =>
      withMapPictures(canvasDom, async () => {
        await waitForResourceImages(canvasDom)
        return domToPng(canvasDom, {
          width: canvasDom.offsetWidth,
          height: canvasDom.offsetHeight,
          // 按 2 倍(或更高)分辨率渲染,解决导出图片模糊问题
          scale: Math.max(3, window.devicePixelRatio || 1)
        })
      })
    )
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
      presentError(error, useI18n().t('material.export_failed'))
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

interface StaticImageComponent {
  component?: string
  innerType?: string
  commonBackground?: { outerImage?: string }
  mCommonBackground?: { outerImage?: string }
  propValue?: { url?: string; urlList?: { url?: string }[] } | StaticImageComponent[]
  componentData?: StaticImageComponent[]
}

interface StaticImageCanvasStyle {
  background?: string
  mobileSetting?: { background?: string }
  component?: { chartCommonStyle?: { outerImage?: string } }
}

async function findStaticSource(
  style: StaticImageCanvasStyle,
  components: StaticImageComponent[]
): Promise<Record<string, string>> {
  const paths = new Set<string>()
  const add = (url?: string) => {
    if (typeof url === 'string' && /(^|\/)static-resource\//.test(url)) paths.add(url)
  }

  const collect = (items?: StaticImageComponent[]) => {
    items?.forEach(item => {
      add(item.commonBackground?.outerImage)
      add(item.mCommonBackground?.outerImage)
      const { propValue } = item
      if (Array.isArray(propValue)) {
        if (item.component === 'Group') collect(propValue)
        if (item.component === 'DeTabs') propValue.forEach(tab => collect(tab.componentData))
      } else if (item.component === 'Picture') {
        add(propValue?.url)
      } else if (item.component === 'UserView' && item.innerType === 'picture-group') {
        propValue?.urlList?.forEach(image => add(image.url))
      }
    })
  }

  // 与 MaterialDocuments 使用相同的图片槽位，便于在没有素材库的环境再次导出。
  add(style?.background)
  add(style?.mobileSetting?.background)
  add(style?.component?.chartCommonStyle?.outerImage)
  collect(components)
  if (!paths.size) return {}
  const resourcePathList = [...paths]
  const { data } = await findResourceAsBase64({ resourcePathList })
  if (resourcePathList.some(path => typeof data?.[path] !== 'string' || !data[path])) {
    throw new Error('Required static resource is unavailable')
  }
  return data
}
