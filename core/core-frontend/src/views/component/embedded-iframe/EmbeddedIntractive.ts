export interface EmbeddedIntractive {
  methodName?: string
  args?: Object[]
  eventName?: string 
}

export const interactive = (param: EmbeddedIntractive) => {
  if (!window['dataease-embedded-host']) {
    return
  }
  const { methodName, eventName, args } = param
  if (methodName) {
    return
  }
  if (eventName) {
    const data = {
      msgOrigin: 'de-fit2cloud',
      type: 'dataease-embedded-interactive',
      eventName,
      args
    }
    window['dataease-embedded-host'].postMessage(data, '*')
    return
  }
}