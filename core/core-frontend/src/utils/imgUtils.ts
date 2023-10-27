import html2canvas from 'html2canvas'
import JsPDF from 'jspdf'

const basePath = import.meta.env.VITE_API_BASEPATH

export function imgUrlTrans(url) {
  if (url && typeof url === 'string' && url.indexOf('static-resource') > -1) {
    const rawUrl = url
      ? (basePath.endsWith('/') ? basePath.substring(0, basePath.length - 1) : basePath) + url
      : null
    return window.DataEaseBi
      ? `${window.DataEaseBi.baseUrl}${
          rawUrl.startsWith('/api') ? rawUrl.slice(5) : rawUrl
        }`.replace('com//', 'com/')
      : rawUrl
  } else {
    return url.replace('com//', 'com/')
  }
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

export function dataURLToBlob(dataurl) {
  // ie 图片转格式
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}
