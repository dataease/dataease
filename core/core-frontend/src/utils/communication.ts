export interface EmbeddedData {
  'de-embedded': boolean
  embeddedToken?: string
  busiFlag?: string
  type?: string
  dvId?: string
  chartId?: string
  pid: string
}

export const communicationInit = cb => {
  window.addEventListener('message', event => {
    if (!event.data['de-embedded']) {
      return
    }
    const origin = event.origin
    console.log(origin)
    const embeddedData: EmbeddedData = event.data
    // validate origin
    if (cb) {
      cb(embeddedData)
    }
  })
  const readyData = {
    ready: true,
    msgOrigin: 'de-fit2cloud'
  }
  window.parent.postMessage(readyData, '*')
}

export const initOpenHandler = (newWindow, data) => {
  if (!data.embeddedToken) {
    return
  }
  window['uuid'] = new Date().getTime()
  newWindow['uuid'] = window['uuid'] + 1
  newWindow['name'] = 'de-new-resource-window'
  window.addEventListener('message', event => {
    if (
      event.data?.msgOrigin !== 'de-inner-fit2cloud' ||
      event.origin !== window.origin ||
      event.source['uuid'] !== newWindow['uuid']
    ) {
      return
    }
    data['de-inner-embedded'] = true
    if (event.data.ready) {
      newWindow.postMessage(data, '/')
    }
  })
}

export const newWindowReady = async cb => {
  return new Promise((resolve, reject) => {
    if (!window.opener || window['name'] !== 'de-new-resource-window') {
      return resolve(null)
    }
    window.addEventListener('message', event => {
      if (
        !event.data['de-inner-embedded'] ||
        event.origin !== window.origin ||
        window['uuid'] !== event.source['uuid'] - 1
      ) {
        return
      }
      if (cb) {
        cb(event.data)
        resolve(true)
      }
    })
    const readyData = {
      ready: true,
      msgOrigin: 'de-inner-fit2cloud'
    }
    window.opener.postMessage(readyData, '/')
  })
}
