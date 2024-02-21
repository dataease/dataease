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
