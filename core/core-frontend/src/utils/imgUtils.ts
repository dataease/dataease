import html2canvas from 'html2canvas'
import JsPDF from 'jspdf'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useEmbedded } from '@/store/modules/embedded'
import { storeToRefs } from 'pinia'
import { findResourceAsBase64 } from '@/api/staticResource'
import FileSaver from 'file-saver'
import { deepCopy } from '@/utils/utils'
import { toPng } from 'html-to-image'
const embeddedStore = useEmbedded()
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData, componentData, canvasViewInfo, canvasViewDataInfo, dvInfo } =
  storeToRefs(dvMainStore)
const basePath = import.meta.env.VITE_API_BASEPATH

export function formatterUrl(url: string) {
  return url.replace('//de2api', '/de2api')
}
export function imgUrlTrans(url) {
  if (url) {
    if (typeof url === 'string' && url.indexOf('static-resource') > -1) {
      const rawUrl = url
        ? (basePath.endsWith('/') ? basePath.substring(0, basePath.length - 1) : basePath) + url
        : null
      return formatterUrl(
        embeddedStore.baseUrl
          ? `${embeddedStore.baseUrl}${
              rawUrl.startsWith('/api') ? rawUrl.slice(5) : rawUrl
            }`.replace('com//', 'com/')
          : rawUrl
      )
    } else {
      return formatterUrl(url.replace('com//', 'com/'))
    }
  }
}

export function download2AppTemplate(downloadType, canvasDom, name, attachParams, callBack?) {
  try {
    findStaticSource(function (staticResource) {
      html2canvas(canvasDom).then(canvas => {
        const canvasViewDataTemplate = deepCopy(canvasViewInfo.value)
        Object.keys(canvasViewDataTemplate).forEach(viewId => {
          canvasViewDataTemplate[viewId].data = canvasViewDataInfo.value[viewId]
        })
        const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.1是图片质量
        if (snapshot !== '') {
          const templateInfo = {
            name: name,
            templateType: 'self',
            snapshot: snapshot,
            dvType: dvInfo.value.type,
            nodeType: downloadType,
            version: 3,
            canvasStyleData: JSON.stringify(canvasStyleData.value),
            componentData: JSON.stringify(componentData.value),
            dynamicData: JSON.stringify(canvasViewDataTemplate),
            staticResource: JSON.stringify(staticResource || {}),
            appData: attachParams ? JSON.stringify(attachParams) : null
          }
          const blob = new Blob([JSON.stringify(templateInfo)], { type: '' })
          if (downloadType === 'template') {
            FileSaver.saveAs(blob, name + '-TEMPLATE.DET2')
          } else if (downloadType === 'app') {
            FileSaver.saveAs(blob, name + '-APP.DET2APP')
          }
        }
        if (callBack) {
          callBack()
        }
      })
    })
  } catch (e) {
    if (callBack) {
      callBack()
    }
    console.error(e)
  }
}

export function downloadCanvas2(type, canvasDom, name, callBack?) {
  toPng(canvasDom)
    .then(dataUrl => {
      const a = document.createElement('a')
      a.setAttribute('download', name)
      a.href = dataUrl
      if (type === 'img') {
        const a = document.createElement('a')
        a.setAttribute('download', name)
        a.href = dataUrl
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

export function downloadCanvas(type, canvasDom, name, callBack?) {
  // const canvasDom = document.getElementById(canvasId)
  if (canvasDom) {
    html2canvas(canvasDom)
      .then(canvas => {
        const dom = document.body.appendChild(canvas)
        dom.style.display = 'none'
        document.body.removeChild(dom)
        const dataUrl = dom.toDataURL('image/png', 1)
        if (type === 'img') {
          const a = document.createElement('a')
          a.setAttribute('download', name)
          a.href = dataUrl
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
  componentData.value.forEach(item => {
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
    }
  })
  if (staticResource.length > 0) {
    try {
      findResourceAsBase64({ resourcePathList: staticResource }).then(rsp => {
        callBack(rsp.data)
      })
    } catch (e) {
      console.error('findResourceAsBase64 error', e)
      callBack()
    }
  } else {
    setTimeout(() => {
      callBack()
    }, 0)
  }
}
