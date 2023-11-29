export const loadScript = (url: string, jsId?: string) => {
  return new Promise(function (resolve, reject) {
    const scriptId = jsId || 'de-fit2cloud-script-id'
    let dom = document.getElementById(scriptId)
    if (dom) {
      dom.parentElement?.removeChild(dom)
      dom = null
    }
    const script = document.createElement('script')

    script.id = scriptId
    script.onload = function () {
      return resolve(null)
    }
    script.onerror = function () {
      return reject(new Error('Load script from '.concat(url, ' failed')))
    }
    script.src = url
    const head = document.head || document.getElementsByTagName('head')[0]
    ;(document.body || head).appendChild(script)
  })
}
