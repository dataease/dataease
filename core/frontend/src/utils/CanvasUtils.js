import { toPng } from 'html-to-image'

export function toPngUrl(refContainer, callBack) {
  toPng(refContainer)
    .then(dataUrl => {
      callBack(dataUrl)
    })
    .catch(error => {
      console.error('oops, toPngUrl went wrong!', error)
    })
}

export function dataURLToBlobCheck(dataurl) { // ie 图片转格式
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

export function colorReverseCheck(OldColorValue) {
  OldColorValue = '0x' + OldColorValue.replace(/#/g, '')
  const str = '000000' + (0xFFFFFF - OldColorValue).toString(16)
  return '#' + str.substring(str.length - 6, str.length)
}

export function imgUrlTransCheck(url) {
  if (url && typeof url === 'string' && url.indexOf('static-resource') > -1) {
    return process.env.VUE_APP_BASE_API + url.replace('/static-resource', 'static-resource')
  } else {
    return url
  }
}
